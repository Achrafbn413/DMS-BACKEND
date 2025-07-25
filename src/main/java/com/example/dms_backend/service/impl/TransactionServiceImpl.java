package com.example.dms_backend.service.impl;
import com.example.dms_backend.dto.TransactionDetailsResponse;

import java.time.LocalDate;
import java.util.Optional;
import com.example.dms_backend.model.*;
import com.example.dms_backend.dto.TransactionDTO;
import com.example.dms_backend.repository.LitigeRepository;
import com.example.dms_backend.repository.MetaTransactionRepository;
import com.example.dms_backend.repository.SatimTransactionRepository;
import com.example.dms_backend.repository.TransactionRepository;
import com.example.dms_backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final MetaTransactionRepository metaTransactionRepository;
    private final SatimTransactionRepository satimTransactionRepository;
    private final LitigeRepository litigeRepository;

    @Override
    public List<Transaction> getTransactionsByAccessLevel(Utilisateur user) {
        if (user.getInstitution() == null) {
            throw new IllegalArgumentException("L'utilisateur doit être associé à une institution");
        }

        Long institutionId = user.getInstitution().getId();
        log.info("🔍 Récupération des transactions pour l'institution ID: {} ({})",
                institutionId, user.getInstitution().getNom());

        // ✅ CORRECTION : Utilise les IDs au lieu des noms
        List<Transaction> transactions = transactionRepository.findByBanqueEmettriceIdOrBanqueAcquereuseId(institutionId, institutionId);

        log.info("✅ {} transactions trouvées pour l'institution {}", transactions.size(), user.getInstitution().getNom());
        return transactions;
    }

    @Override
    public List<TransactionDTO> getAllTransactionsForDashboard() {
        log.info("🔍 Récupération de toutes les transactions pour dashboard (format simple)");

        List<Transaction> transactions = transactionRepository.findAll();
        log.info("✅ {} transactions trouvées", transactions.size());

        return transactions.stream()
                .map(t -> new TransactionDTO(
                        t.getId(),
                        t.getReference(),
                        t.getMontant(),
                        t.getDateTransaction(),
                        t.getType().toString(),
                        // ✅ CORRECTION : Vérifier la présence réelle d'un litige
                        t.getLitige() != null ? "AVEC_LITIGE" : "NORMALE",
                        t.getBanqueEmettrice() != null ? t.getBanqueEmettrice().getNom() : null,
                        t.getBanqueAcquereuse() != null ? t.getBanqueAcquereuse().getNom() : null,
                        null // Pas de banque déclarante dans cette méthode simple
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getAllTransactionsWithLitiges() {
        log.info("🔍 Récupération de toutes les transactions avec informations de litiges");

        // ✅ OPTIMISATION : Utiliser une requête avec fetch join pour éviter N+1
        List<Transaction> transactions = transactionRepository.findAllWithLitigeInfo();
        log.info("✅ {} transactions trouvées", transactions.size());

        return transactions.stream()
                .map(this::convertToTransactionDTO)
                .collect(Collectors.toList());
    }

    /**
     * ✅ MÉTHODE UTILITAIRE : Conversion Transaction → TransactionDTO avec logique complète
     */
    private TransactionDTO convertToTransactionDTO(Transaction t) {
        String banqueDeclarante = null;
        String statut = "NORMALE";

        // ✅ LOGIQUE COMPLÈTE pour déterminer le statut et la banque déclarante
        if (t.getLitige() != null) {
            statut = "AVEC_LITIGE";

            // Priorité 1: banqueDeclarante directe
            if (t.getLitige().getBanqueDeclarante() != null) {
                banqueDeclarante = t.getLitige().getBanqueDeclarante().getNom();
                log.debug("✅ Banque déclarante trouvée (directe): {} pour transaction {}",
                        banqueDeclarante, t.getReference());
            }
            // Priorité 2: via l'utilisateur déclarant
            else if (t.getLitige().getDeclarePar() != null &&
                    t.getLitige().getDeclarePar().getInstitution() != null) {
                banqueDeclarante = t.getLitige().getDeclarePar().getInstitution().getNom();
                log.debug("✅ Banque déclarante trouvée (via utilisateur): {} pour transaction {}",
                        banqueDeclarante, t.getReference());
            } else {
                log.warn("⚠️ Litige sans banque déclarante pour transaction {}", t.getReference());
            }
        }

        return new TransactionDTO(
                t.getId(),
                t.getReference(),
                t.getMontant(),
                t.getDateTransaction(),
                t.getType().toString(),
                statut,
                t.getBanqueEmettrice() != null ? t.getBanqueEmettrice().getNom() : null,
                t.getBanqueAcquereuse() != null ? t.getBanqueAcquereuse().getNom() : null,
                banqueDeclarante
        );
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Récupération par ID (pour le controller)
     */
    public TransactionDTO getTransactionById(Long id) {
        log.info("🔍 Récupération de la transaction ID: {}", id);

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction non trouvée: " + id));

        return convertToTransactionDTO(transaction);
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Récupération des transactions par institution (optimisée)
     */
    public List<TransactionDTO> getTransactionsByInstitution(Long institutionId) {
        log.info("🔍 Récupération des transactions pour l'institution ID: {}", institutionId);

        List<Transaction> transactions = transactionRepository.findByBanqueEmettriceIdOrBanqueAcquereuseId(institutionId, institutionId);

        return transactions.stream()
                .map(this::convertToTransactionDTO)
                .collect(Collectors.toList());
    }
    // ====================================================================
// 🆕 NOUVELLE MÉTHODE POUR FONCTIONNALITÉ DÉTAILS TRANSACTION
// ====================================================================


    @Override
    public TransactionDetailsResponse getTransactionCompletDetails(Long transactionId) {
        log.info("🔍 Récupération des détails complets de la transaction ID: {}", transactionId);

        try {
            // 1. Récupérer la transaction principale
            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction non trouvée avec l'ID: " + transactionId));

            log.debug("✅ Transaction trouvée: {}", transaction.getReference());

            // 2. Récupérer les données SATIM
            TransactionDetailsResponse.SatimDataDTO satimData = getSatimDataForTransaction(transaction);

            // 3. Récupérer les données META
            TransactionDetailsResponse.MetaDataDTO metaData = getMetaDataForTransaction(transaction);

            // 4. Construire les infos banques
            TransactionDetailsResponse.BanqueInfo banqueEmettrice = null;
            TransactionDetailsResponse.BanqueInfo banqueAcquereuse = null;

            if (transaction.getBanqueEmettrice() != null) {
                banqueEmettrice = buildBanqueInfo(transaction.getBanqueEmettrice());
            }

            if (transaction.getBanqueAcquereuse() != null) {
                banqueAcquereuse = buildBanqueInfo(transaction.getBanqueAcquereuse());
            }

            // 5. Récupérer les infos litige si existe
            TransactionDetailsResponse.LitigeBasicInfo litigeInfo = null;
            if (transaction.getLitige() != null) {
                litigeInfo = buildLitigeInfo(transaction.getLitige());
            }

            // 6. Calculer les métadonnées
            Long dureeJours = calculateDurationSinceCreation(transaction.getDateTransaction());
            String priorite = calculateTransactionPriority(transaction, dureeJours);

            // 7. Construire la réponse complète avec setters
            TransactionDetailsResponse response = new TransactionDetailsResponse();
            response.setId(transaction.getId());
            response.setReference(transaction.getReference());
            response.setMontant(transaction.getMontant());
            response.setDateTransaction(transaction.getDateTransaction());
            response.setType(transaction.getType().toString());
            response.setStatut(transaction.getStatut() != null ? transaction.getStatut().toString() : "NORMALE");
            response.setBanqueEmettrice(banqueEmettrice);
            response.setBanqueAcquereuse(banqueAcquereuse);
            response.setSatimData(satimData);
            response.setMetaData(metaData);
            response.setLitige(litigeInfo);
            response.setDureeDepuisCreationJours(dureeJours);
            response.setPriorite(priorite);
            response.setALitige(transaction.getLitige() != null);

            log.info("✅ Détails complets récupérés pour la transaction {}", transactionId);
            return response;

        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des détails de la transaction {}", transactionId, e);
            throw new RuntimeException("Erreur lors de la récupération des détails: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ MÉTHODE HELPER : Construire les informations d'une banque
     */
    private TransactionDetailsResponse.BanqueInfo buildBanqueInfo(Institution institution) {
        return TransactionDetailsResponse.BanqueInfo.builder()
                .id(institution.getId())
                .nom(institution.getNom())
                .code("BNK" + String.format("%03d", institution.getId()))
                .type(institution.getType() != null ? institution.getType().toString() : "AUTRE")
                .enabled(institution.isEnabled())
                .build();
    }

    /**
     * ✅ MÉTHODE HELPER : Récupérer les données SATIM pour une transaction
     */
    private TransactionDetailsResponse.SatimDataDTO getSatimDataForTransaction(Transaction transaction) {
        try {
            Optional<SatimTransaction> satimOpt = satimTransactionRepository
                    .findByStrRecoCode(transaction.getReference());

            if (satimOpt.isPresent()) {
                SatimTransaction satim = satimOpt.get();
                return TransactionDetailsResponse.SatimDataDTO.builder()
                        .strCode(satim.getStrCode())
                        .strRecoCode(satim.getStrRecoCode())
                        .strRecoNumb(satim.getStrRecoNumb() != null ? satim.getStrRecoNumb().doubleValue() : 0.0)
                        .strProcDate(satim.getStrProcDate() != null ? satim.getStrProcDate().toString() : null)
                        .strOperCode(satim.getStrOperCode())
                        .strTermIden(satim.getStrTermIden())
                        .strIssuBanCode(satim.getStrIssuBanCode())
                        .strAcquBanCode(satim.getStrAcquBanCode())
                        .build();
            }

            return null;
        } catch (Exception e) {
            log.warn("⚠️ Impossible de récupérer les données SATIM pour la transaction {}",
                    transaction.getReference(), e);
            return null;
        }
    }

    /**
     * ✅ MÉTHODE HELPER : Récupérer les données META pour une transaction
     */
    private TransactionDetailsResponse.MetaDataDTO getMetaDataForTransaction(Transaction transaction) {
        try {
            Optional<MetaTransaction> metaOpt = metaTransactionRepository
                    .findByTransactionId(transaction.getId());

            if (metaOpt.isPresent()) {
                MetaTransaction meta = metaOpt.get();
                return TransactionDetailsResponse.MetaDataDTO.builder()
                        .id(meta.getId())
                        .strCode(meta.getStrCode())
                        .strRecoCode(meta.getStrRecoCode())
                        .strRecoNumb(meta.getStrRecoNumb() != null ? meta.getStrRecoNumb().doubleValue() : 0.0)
                        .strOperCode(meta.getStrOperCode())
                        .strProcDate(meta.getStrProcDate() != null ? meta.getStrProcDate().toString() : null)
                        .strTermIden(meta.getStrTermIden())
                        .build();
            }

            return null;
        } catch (Exception e) {
            log.warn("⚠️ Impossible de récupérer les données META pour la transaction {}",
                    transaction.getReference(), e);
            return null;
        }
    }

    /**
     * ✅ MÉTHODE HELPER : Construire les informations basiques du litige
     */
    private TransactionDetailsResponse.LitigeBasicInfo buildLitigeInfo(Litige litige) {
        String banqueDeclaranteNom = "Banque inconnue";
        String utilisateurCreateur = "Utilisateur inconnu";

        if (litige.getBanqueDeclarante() != null) {
            banqueDeclaranteNom = litige.getBanqueDeclarante().getNom();
        }

        if (litige.getDeclarePar() != null) {
            utilisateurCreateur = litige.getDeclarePar().getNom();
        }

        return TransactionDetailsResponse.LitigeBasicInfo.builder()
                .id(litige.getId())
                .type(litige.getType() != null ? litige.getType().toString() : "AUTRE")
                .statut(litige.getStatut() != null ? litige.getStatut().toString() : "OUVERT")
                .description(litige.getDescription())
                .dateCreation(litige.getDateCreation())
                .banqueDeclaranteNom(banqueDeclaranteNom)
                .utilisateurCreateur(utilisateurCreateur)
                .build();
    }

    /**
     * ✅ MÉTHODE HELPER : Calculer la durée depuis création
     */
    private Long calculateDurationSinceCreation(LocalDate dateTransaction) {
        if (dateTransaction == null) return null;

        LocalDate now = LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(dateTransaction, now);
    }

    /**
     * ✅ MÉTHODE HELPER : Calculer la priorité d'une transaction
     */
    private String calculateTransactionPriority(Transaction transaction, Long dureeJours) {
        // Priorité HAUTE si litige ancien
        if (transaction.getLitige() != null && dureeJours != null && dureeJours > 7) {
            return "HAUTE";
        }

        // Priorité MOYENNE si transaction récente avec litige
        if (transaction.getLitige() != null) {
            return "MOYENNE";
        }

        // Priorité HAUTE si transaction très ancienne sans résolution
        if (dureeJours != null && dureeJours > 30) {
            return "HAUTE";
        }

        return "NORMALE";
    }
}