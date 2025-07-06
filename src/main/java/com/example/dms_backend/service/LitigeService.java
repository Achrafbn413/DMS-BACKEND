package com.example.dms_backend.service;

import com.example.dms_backend.dto.LitigeRequest;
import com.example.dms_backend.model.*;
import com.example.dms_backend.repository.LitigeRepository;
import com.example.dms_backend.repository.MetaTransactionRepository;
import com.example.dms_backend.repository.SatimTransactionRepository;
import com.example.dms_backend.repository.TransactionRepository;
import com.example.dms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LitigeService {

    private static final Logger logger = LoggerFactory.getLogger(LitigeService.class);

    private final TransactionRepository transactionRepository;
    private final MetaTransactionRepository metaTransactionRepository;
    private final SatimTransactionRepository satimTransactionRepository;
    private final UserRepository utilisateurRepository;
    private final LitigeRepository litigeRepository;

    @Transactional
    public Litige flagTransaction(LitigeRequest request) {
        logger.info("🔍 flagTransaction() appelé avec transactionId={} et utilisateurId={}",
                request.getTransactionId(), request.getUtilisateurId());

        // ✅ 1. Vérifier l'utilisateur en premier
        Utilisateur utilisateur = utilisateurRepository.findById(request.getUtilisateurId())
                .orElseThrow(() -> {
                    logger.error("❌ Utilisateur non trouvé avec ID={}", request.getUtilisateurId());
                    return new RuntimeException("Utilisateur non trouvé avec ID=" + request.getUtilisateurId());
                });

        logger.info("✅ Utilisateur trouvé: {} ({})", utilisateur.getNom(), utilisateur.getEmail());

        // ✅ 2. Recherche de la transaction avec TOUTES les stratégies possibles
        Transaction transaction = findTransactionByMultipleStrategies(request.getTransactionId());

        // ✅ 3. Vérifier si la transaction a déjà un litige
        if (litigeRepository.existsByTransactionId(transaction.getId())) {
            logger.warn("⚠️ Transaction ID={} a déjà un litige associé", transaction.getId());
            throw new IllegalStateException("Cette transaction a déjà un litige associé.");
        }

        // ✅ 4. Créer le litige avec des valeurs par défaut sécurisées
        Litige litige = Litige.builder()
                .transaction(transaction)
                .declarePar(utilisateur)
                .statut(StatutLitige.OUVERT)
                .dateCreation(LocalDate.now())
                .description(request.getDescription() != null ?
                        request.getDescription() :
                        "Transaction signalée par " + utilisateur.getNom())
                .type(request.getType() != null ?
                        request.getType() :
                        TypeLitige.AUTRE)
                .build();

        // ✅ 5. Mettre à jour le statut de la transaction
        transaction.setStatut(StatutTransaction.AVEC_LITIGE);
        transaction.setLitige(litige);

        // ✅ 6. Sauvegarder dans l'ordre correct
        Transaction savedTransaction = transactionRepository.save(transaction);
        Litige savedLitige = litigeRepository.save(litige);

        logger.info("✅ Litige créé avec succès - ID={}, TransactionID={}, UtilisateurID={}",
                savedLitige.getId(), savedTransaction.getId(), utilisateur.getId());

        return savedLitige;
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Recherche avec 5 stratégies différentes
     */
    private Transaction findTransactionByMultipleStrategies(Long transactionId) {
        logger.info("🔍 Recherche de transaction avec ID/strCode: {}", transactionId);

        // 🎯 STRATÉGIE 1: Recherche directe par ID dans Transaction
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
        if (transaction != null) {
            logger.info("✅ [STRATÉGIE 1] Transaction trouvée directement avec ID={}", transaction.getId());
            return transaction;
        }

        // 🎯 STRATÉGIE 2: Recherche via MetaTransaction par strCode
        logger.info("🔄 [STRATÉGIE 2] Recherche via MetaTransaction avec strCode={}", transactionId);
        MetaTransaction meta = metaTransactionRepository.findByStrCode(transactionId).orElse(null);
        if (meta != null && meta.getTransaction() != null) {
            logger.info("✅ [STRATÉGIE 2] Transaction trouvée via MetaTransaction: ID={}",
                    meta.getTransaction().getId());
            return meta.getTransaction();
        }

        // 🎯 STRATÉGIE 3: Recherche dans SatimTransaction puis création/liaison
        logger.info("🔄 [STRATÉGIE 3] Recherche dans SatimTransaction avec strCode={}", transactionId);
        SatimTransaction satimTransaction = satimTransactionRepository.findById(transactionId).orElse(null);
        if (satimTransaction != null) {
            logger.info("✅ [STRATÉGIE 3] SatimTransaction trouvée avec strCode={}", transactionId);

            // Vérifier s'il existe déjà une Transaction avec cette référence
            Transaction existingTransaction = transactionRepository
                    .findByReference(satimTransaction.getStrRecoCode()).orElse(null);

            if (existingTransaction != null) {
                logger.info("✅ [STRATÉGIE 3] Transaction existante trouvée par référence: {}",
                        existingTransaction.getId());

                // Créer/mettre à jour la MetaTransaction pour lier les données
                createOrUpdateMetaTransaction(satimTransaction, existingTransaction);
                return existingTransaction;
            } else {
                // Créer une nouvelle Transaction basée sur SatimTransaction
                logger.info("🔧 [STRATÉGIE 3] Création d'une nouvelle Transaction depuis SatimTransaction");
                return createTransactionFromSatim(satimTransaction);
            }
        }

        // 🎯 STRATÉGIE 4: Recherche par référence (si transactionId est passé comme String)
        logger.info("🔄 [STRATÉGIE 4] Recherche par référence={}", transactionId.toString());
        Transaction transactionByRef = transactionRepository
                .findByReference(transactionId.toString()).orElse(null);
        if (transactionByRef != null) {
            logger.info("✅ [STRATÉGIE 4] Transaction trouvée par référence: ID={}",
                    transactionByRef.getId());
            return transactionByRef;
        }

        // 🎯 STRATÉGIE 5: Recherche dans toutes les MetaTransactions puis par Transaction liée
        logger.info("🔄 [STRATÉGIE 5] Recherche exhaustive dans MetaTransactions");
        MetaTransaction anyMeta = metaTransactionRepository.findByStrRecoCode(transactionId.toString()).orElse(null);
        if (anyMeta != null && anyMeta.getTransaction() != null) {
            logger.info("✅ [STRATÉGIE 5] Transaction trouvée via MetaTransaction par strRecoCode: ID={}",
                    anyMeta.getTransaction().getId());
            return anyMeta.getTransaction();
        }

        // ❌ Aucune transaction trouvée avec toutes les stratégies
        logger.error("❌ Aucune transaction trouvée avec TOUTES les stratégies pour ID/strCode={}",
                transactionId);
        throw new RuntimeException(
                "Transaction non trouvée avec ID/strCode=" + transactionId +
                        ". Vérifiez que les données ont été correctement importées."
        );
    }

    /**
     * ✅ Créer une MetaTransaction pour lier SatimTransaction et Transaction existante
     */
    private void createOrUpdateMetaTransaction(SatimTransaction satim, Transaction transaction) {
        // Vérifier si MetaTransaction existe déjà
        if (!metaTransactionRepository.existsByStrCode(satim.getStrCode())) {
            MetaTransaction meta = MetaTransaction.builder()
                    .strCode(satim.getStrCode())
                    .strRecoCode(satim.getStrRecoCode())
                    .strRecoNumb(satim.getStrRecoNumb())
                    .strOperCode(satim.getStrOperCode())
                    .strProcDate(satim.getStrProcDate())
                    .strTermIden(satim.getStrTermIden())
                    .transaction(transaction)
                    .build();

            metaTransactionRepository.save(meta);
            logger.info("✅ MetaTransaction créée pour lier SatimTransaction {} à Transaction {}",
                    satim.getStrCode(), transaction.getId());
        }
    }

    /**
     * ✅ Créer une nouvelle Transaction basée sur SatimTransaction
     */
    private Transaction createTransactionFromSatim(SatimTransaction satim) {
        logger.info("🔧 Création d'une nouvelle Transaction basée sur SatimTransaction strCode={}",
                satim.getStrCode());

        // Créer la Transaction
        Transaction transaction = Transaction.builder()
                .reference(satim.getStrRecoCode())
                .montant(satim.getStrRecoNumb() != null ?
                        java.math.BigDecimal.valueOf(satim.getStrRecoNumb()) :
                        java.math.BigDecimal.ZERO)
                .type(parseTypeTransaction(satim.getStrOperCode()))
                .statut(StatutTransaction.NORMALE)
                .dateTransaction(satim.getStrProcDate() != null ?
                        satim.getStrProcDate() :
                        LocalDate.now())
                .build();

        // Sauvegarder la Transaction
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Créer la MetaTransaction liée
        MetaTransaction meta = MetaTransaction.builder()
                .strCode(satim.getStrCode())
                .strRecoCode(satim.getStrRecoCode())
                .strRecoNumb(satim.getStrRecoNumb())
                .strOperCode(satim.getStrOperCode())
                .strProcDate(satim.getStrProcDate())
                .strTermIden(satim.getStrTermIden())
                .transaction(savedTransaction)
                .build();

        metaTransactionRepository.save(meta);

        logger.info("✅ Transaction créée (ID={}) et MetaTransaction liée depuis SatimTransaction",
                savedTransaction.getId());
        return savedTransaction;
    }

    /**
     * ✅ Parser le type de transaction
     */
    private TypeTransaction parseTypeTransaction(String operCode) {
        if (operCode == null || operCode.trim().isEmpty()) {
            return TypeTransaction.AUTRE;
        }

        try {
            return TypeTransaction.valueOf(operCode.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            logger.warn("Type de transaction inconnu: {}, utilisation de AUTRE", operCode);
            return TypeTransaction.AUTRE;
        }
    }
}