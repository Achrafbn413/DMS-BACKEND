package com.example.dms_backend.service;

import com.example.dms_backend.dto.LitigeRequest;
import com.example.dms_backend.model.*;
import com.example.dms_backend.repository.LitigeRepository;
import com.example.dms_backend.repository.MetaTransactionRepository;
import com.example.dms_backend.repository.TransactionRepository;
import com.example.dms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class LitigeService {

    private static final Logger logger = LoggerFactory.getLogger(LitigeService.class);

    private final TransactionRepository transactionRepository;
    private final MetaTransactionRepository metaTransactionRepository;
    private final UserRepository utilisateurRepository;
    private final LitigeRepository litigeRepository;

    public Litige flagTransaction(LitigeRequest request) {
        logger.info("🔍 flagTransaction() appelé avec transactionId={} et utilisateurId={}",
                request.getTransactionId(), request.getUtilisateurId());

        // ✅ 1. D'abord essayer de trouver directement la Transaction par ID
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElse(null);

        if (transaction != null) {
            logger.info("✅ Transaction trouvée directement avec ID={}", transaction.getId());
        } else {
            // ✅ 2. Fallback: chercher via MetaTransaction par strCode
            logger.info("🔄 Transaction non trouvée par ID, recherche via MetaTransaction avec strCode={}",
                    request.getTransactionId());

            MetaTransaction meta = metaTransactionRepository.findByStrCode(request.getTransactionId())
                    .orElseThrow(() -> new RuntimeException(
                            "Transaction non trouvée avec ID=" + request.getTransactionId()
                                    + " ni MetaTransaction avec strCode=" + request.getTransactionId()));

            transaction = meta.getTransaction();
            if (transaction == null) {
                throw new RuntimeException("Transaction associée non trouvée via MetaTransaction.");
            }
            logger.info("✅ Transaction trouvée via MetaTransaction: transactionId={}", transaction.getId());
        }

        // ✅ 3. Vérifier si la transaction a déjà un litige
        if (transaction.getLitige() != null) {
            throw new IllegalStateException("Cette transaction a déjà un litige associé.");
        }

        // ✅ 4. Vérifier l'utilisateur
        Utilisateur utilisateur = utilisateurRepository.findById(request.getUtilisateurId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec ID=" + request.getUtilisateurId()));

        // ✅ 5. Créer le litige
        Litige litige = Litige.builder()
                .transaction(transaction)
                .declarePar(utilisateur)
                .statut(StatutLitige.OUVERT)
                .dateCreation(LocalDate.now())
                .description(request.getDescription())
                .type(request.getType())
                .build();

        // ✅ 6. Mettre à jour le statut de la transaction
        transaction.setStatut(StatutTransaction.AVEC_LITIGE);
        transaction.setLitige(litige);

        // ✅ 7. Sauvegarder
        transactionRepository.save(transaction);
        Litige savedLitige = litigeRepository.save(litige);

        logger.info("✅ Litige créé avec succès - ID={}, TransactionID={}, UtilisateurID={}",
                savedLitige.getId(), transaction.getId(), utilisateur.getId());

        return savedLitige;
    }
}