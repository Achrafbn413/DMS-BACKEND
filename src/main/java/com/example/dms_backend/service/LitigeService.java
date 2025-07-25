package com.example.dms_backend.service;

import com.example.dms_backend.dto.LitigeRequest;
import com.example.dms_backend.dto.LitigeDetailsResponse;
import com.example.dms_backend.model.*;
import com.example.dms_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.dms_backend.service.impl.SatimTransactionServiceImpl.getTypeTransaction;

@Service
@RequiredArgsConstructor
public class LitigeService {

    private static final Logger logger = LoggerFactory.getLogger(LitigeService.class);

    private final TransactionRepository transactionRepository;
    private final MetaTransactionRepository metaTransactionRepository;
    private final SatimTransactionRepository satimTransactionRepository;
    private final UserRepository utilisateurRepository;
    private final LitigeRepository litigeRepository;

    // ✅ CORRECTION PRINCIPALE : Utiliser NotificationService au lieu du repository direct
    private final NotificationService notificationService;

    /**
     * ✅ SOLUTION 1: Suppression du synchronized qui peut causer des problèmes
     * ✅ SOLUTION 2: Utilisation d'une seule transaction pour éviter les doublons
     * ✅ SOLUTION 3: Vérification stricte avant création
     */
    @Transactional
    public Litige flagTransaction(LitigeRequest request) {
        logger.info("📩 flagTransaction() reçu : transactionId={}, utilisateurId={}",
                request.getTransactionId(), request.getUtilisateurId());

        // 🔎 Vérification utilisateur
        Utilisateur utilisateur = utilisateurRepository.findById(request.getUtilisateurId())
                .orElseThrow(() -> {
                    logger.error("❌ Utilisateur ID={} non trouvé", request.getUtilisateurId());
                    return new IllegalArgumentException("Utilisateur non trouvé");
                });

        // 🔍 Recherche transaction avec fallback
        Transaction transaction = findTransactionByMultipleStrategies(request.getTransactionId());

        // ✅ SOLUTION PRINCIPALE: Vérification ET création atomique
        return createLitigeIfNotExists(transaction, utilisateur, request);
    }

    /**
     * ✅ MÉTHODE COMPLÈTEMENT CORRIGÉE : Logique simplifiée avec NotificationService
     */
    private Litige createLitigeIfNotExists(Transaction transaction, Utilisateur utilisateur, LitigeRequest request) {
        // Vérification stricte avec lock pessimiste pour éviter les conditions de course
        Optional<Litige> existing = litigeRepository.findByTransactionIdAndUserId(transaction.getId(), utilisateur.getId());

        if (existing.isPresent()) {
            logger.warn("⚠️ Litige déjà existant pour transaction {} et utilisateur {}",
                    transaction.getId(), utilisateur.getId());
            return existing.get();
        }

        // ✅ VALIDATION : L'utilisateur doit appartenir à une des banques de la transaction
        Institution institutionUtilisateur = utilisateur.getInstitution();
        if (institutionUtilisateur == null) {
            throw new IllegalArgumentException("L'utilisateur doit être associé à une institution");
        }

        // 🔍 DEBUG RENFORCÉ pour identifier le problème
        logger.info("🔍 DEBUG Transaction - Émettrice: {} (ID: {}), Acquéreuse: {} (ID: {})",
                transaction.getBanqueEmettrice() != null ? transaction.getBanqueEmettrice().getNom() : "null",
                transaction.getBanqueEmettrice() != null ? transaction.getBanqueEmettrice().getId() : "null",
                transaction.getBanqueAcquereuse() != null ? transaction.getBanqueAcquereuse().getNom() : "null",
                transaction.getBanqueAcquereuse() != null ? transaction.getBanqueAcquereuse().getId() : "null");

        logger.info("🔍 DEBUG Utilisateur - Institution: {} (ID: {})",
                institutionUtilisateur.getNom(), institutionUtilisateur.getId());

        // ✅ VALIDATION SIMPLIFIÉE : Vérifier que l'utilisateur peut signaler cette transaction
        boolean peutSignaler = false;

        if (transaction.getBanqueEmettrice() != null &&
                java.util.Objects.equals(transaction.getBanqueEmettrice().getId(), institutionUtilisateur.getId())) {
            peutSignaler = true;
            logger.info("✅ Signalement autorisé - Utilisateur appartient à la banque émettrice");
        } else if (transaction.getBanqueAcquereuse() != null &&
                java.util.Objects.equals(transaction.getBanqueAcquereuse().getId(), institutionUtilisateur.getId())) {
            peutSignaler = true;
            logger.info("✅ Signalement autorisé - Utilisateur appartient à la banque acquéreuse");
        }

        // ✅ VALIDATION ALTERNATIVE : Si les noms correspondent (fallback)
        if (!peutSignaler) {
            String nomInstitutionUtilisateur = institutionUtilisateur.getNom();

            if (transaction.getBanqueEmettrice() != null &&
                    nomInstitutionUtilisateur.equals(transaction.getBanqueEmettrice().getNom())) {
                peutSignaler = true;
                logger.info("✅ Signalement autorisé par nom - Banque émettrice");
            } else if (transaction.getBanqueAcquereuse() != null &&
                    nomInstitutionUtilisateur.equals(transaction.getBanqueAcquereuse().getNom())) {
                peutSignaler = true;
                logger.info("✅ Signalement autorisé par nom - Banque acquéreuse");
            }
        }

        if (!peutSignaler) {
            String errorMsg = String.format(
                    "Vous ne pouvez signaler que les transactions de votre institution. " +
                            "Transaction - Émettrice: %s (ID: %s), Acquéreuse: %s (ID: %s), " +
                            "Votre institution: %s (ID: %s)",
                    transaction.getBanqueEmettrice() != null ? transaction.getBanqueEmettrice().getNom() : "null",
                    transaction.getBanqueEmettrice() != null ? transaction.getBanqueEmettrice().getId() : "null",
                    transaction.getBanqueAcquereuse() != null ? transaction.getBanqueAcquereuse().getNom() : "null",
                    transaction.getBanqueAcquereuse() != null ? transaction.getBanqueAcquereuse().getId() : "null",
                    institutionUtilisateur.getNom(),
                    institutionUtilisateur.getId()
            );
            logger.error("❌ " + errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }

        // ✅ Création du litige avec toutes les informations nécessaires
        Litige litige = Litige.builder()
                .transaction(transaction)
                .declarePar(utilisateur)
                .banqueDeclarante(institutionUtilisateur) // ✅ Institution qui signale
                .dateCreation(LocalDate.now())
                .description(request.getDescription() != null ?
                        request.getDescription() : "Transaction signalée par " + utilisateur.getNom())
                .type(request.getType() != null ? request.getType() : TypeLitige.AUTRE)
                .statut(StatutLitige.OUVERT)
                .build();

        // ✅ Mise à jour statut transaction
        transaction.setStatut(StatutTransaction.AVEC_LITIGE);

        // 💾 Sauvegarde litige AVANT la transaction pour éviter les cycles
        Litige savedLitige = litigeRepository.save(litige);
        transactionRepository.save(transaction);

        // ✅ CORRECTION PRINCIPALE : Utiliser NotificationService pour logique intelligente
        try {
            String messagePersonnalise = String.format("Transaction signalée par %s (Réf: %s)",
                    institutionUtilisateur.getNom(),
                    transaction.getReference());

            Notification notification = notificationService.notifierAutreBanque(
                    transaction,
                    utilisateur,
                    messagePersonnalise
            );

            if (notification != null) {
                logger.info("✅ Notification créée avec succès via NotificationService pour transaction {}",
                        transaction.getReference());
            } else {
                logger.warn("⚠️ Aucune notification créée pour la transaction {}", transaction.getReference());
            }
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la création de notification : {}", e.getMessage());
            // Ne pas faire échouer le litige si la notification échoue
        }

        logger.info("✅ Litige créé avec succès : ID={}, TransactionID={}, UserID={}, BanqueDeclarante={}",
                savedLitige.getId(), transaction.getId(), utilisateur.getId(), institutionUtilisateur.getNom());

        return savedLitige;
    }

    public boolean litigeExistsPourTransaction(Long transactionId) {
        return litigeRepository.existsByTransactionId(transactionId);
    }

    public boolean litigeExistsPourUtilisateurEtTransaction(Long transactionId, Long userId) {
        return litigeRepository.existsByTransactionIdAndUserId(transactionId, userId);
    }

    /**
     * ✅ SOLUTION 5: Validation préalable pour éviter les appels inutiles
     */
    public void validateLitigeCreation(Long transactionId, Long userId) {
        Transaction transaction = findTransactionByMultipleStrategies(transactionId);

        if (litigeRepository.existsByTransactionId(transaction.getId())) {
            throw new IllegalStateException("Cette transaction a déjà un litige.");
        }

        if (litigeRepository.existsByTransactionIdAndUserId(transaction.getId(), userId)) {
            throw new IllegalStateException("Vous avez déjà créé un litige pour cette transaction.");
        }
    }

    /**
     * ✅ SOLUTION 6: Méthode optimisée avec moins de logs redondants
     */
    private Transaction findTransactionByMultipleStrategies(Long transactionId) {
        logger.debug("🔍 Recherche transaction ID/Code={}", transactionId);

        // Stratégie 1: Recherche directe par ID
        Optional<Transaction> transactionOpt = transactionRepository.findById(transactionId);
        if (transactionOpt.isPresent()) {
            logger.debug("✅ [STRAT 1] Transaction trouvée ID={}", transactionOpt.get().getId());
            return transactionOpt.get();
        }

        // Stratégie 2: Via MetaTransaction
        Optional<MetaTransaction> metaOpt = metaTransactionRepository.findByStrCode(transactionId);
        if (metaOpt.isPresent() && metaOpt.get().getTransaction() != null) {
            logger.debug("✅ [STRAT 2] via MetaTransaction ID={}", metaOpt.get().getTransaction().getId());
            return metaOpt.get().getTransaction();
        }

        // Stratégie 3: Via SatimTransaction
        Optional<SatimTransaction> satimOpt = satimTransactionRepository.findById(transactionId);
        if (satimOpt.isPresent()) {
            SatimTransaction satim = satimOpt.get();
            logger.debug("✅ [STRAT 3] SatimTransaction trouvée: {}", transactionId);

            Optional<Transaction> existingOpt = transactionRepository.findByReference(satim.getStrRecoCode());
            if (existingOpt.isPresent()) {
                createOrUpdateMetaTransaction(satim, existingOpt.get());
                return existingOpt.get();
            } else {
                return createTransactionFromSatim(satim);
            }
        }

        // Stratégie 4: Par référence
        Optional<Transaction> tByRefOpt = transactionRepository.findByReference(transactionId.toString());
        if (tByRefOpt.isPresent()) {
            logger.debug("✅ [STRAT 4] Transaction trouvée via référence: {}", tByRefOpt.get().getId());
            return tByRefOpt.get();
        }

        // Stratégie 5: Via MetaTransaction par RecoCode
        Optional<MetaTransaction> m2Opt = metaTransactionRepository.findByStrRecoCode(transactionId.toString());
        if (m2Opt.isPresent() && m2Opt.get().getTransaction() != null) {
            return m2Opt.get().getTransaction();
        }

        throw new RuntimeException("Transaction introuvable. Vérifiez les données importées.");
    }

    /**
     * ✅ SOLUTION 7: Vérification d'existence avant création MetaTransaction
     */
    private void createOrUpdateMetaTransaction(SatimTransaction satim, Transaction transaction) {
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
            logger.debug("🧩 MetaTransaction créée pour liaison");
        }
    }

    /**
     * ✅ SOLUTION 8: Création atomique Transaction + MetaTransaction
     */
    private Transaction createTransactionFromSatim(SatimTransaction satim) {
        logger.info("🛠️ Création Transaction depuis SATIM: strCode={}", satim.getStrCode());

        Transaction transaction = Transaction.builder()
                .reference(satim.getStrRecoCode())
                .montant(satim.getStrRecoNumb() != null ?
                        java.math.BigDecimal.valueOf(satim.getStrRecoNumb()) :
                        java.math.BigDecimal.ZERO)
                .type(getTypeTransaction(satim.getStrOperCode(), logger))
                .statut(StatutTransaction.NORMALE)
                .dateTransaction(satim.getStrProcDate() != null ?
                        satim.getStrProcDate() :
                        LocalDate.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);

        // Création MetaTransaction associée
        MetaTransaction meta = MetaTransaction.builder()
                .strCode(satim.getStrCode())
                .strRecoCode(satim.getStrRecoCode())
                .strRecoNumb(satim.getStrRecoNumb())
                .strOperCode(satim.getStrOperCode())
                .strProcDate(satim.getStrProcDate())
                .strTermIden(satim.getStrTermIden())
                .transaction(saved)
                .build();

        metaTransactionRepository.save(meta);
        return saved;
    }

    public List<Long> getTransactionIdsSignaledByUser(Long userId) {
        logger.info("🔍 Recherche des transactions signalées par utilisateur: {}", userId);

        // Vérifier que l'utilisateur existe
        if (!utilisateurRepository.existsById(userId)) {
            logger.warn("⚠️ Utilisateur {} non trouvé", userId);
            throw new IllegalArgumentException("Utilisateur non trouvé");
        }

        // Récupérer tous les litiges créés par cet utilisateur
        List<Litige> litigesUtilisateur = litigeRepository.findByDeclareParId(userId);

        // Extraire les IDs des transactions
        List<Long> transactionIds = litigesUtilisateur.stream()
                .filter(litige -> litige.getTransaction() != null)
                .map(litige -> litige.getTransaction().getId())
                .distinct()
                .collect(Collectors.toList());

        logger.info("📊 Utilisateur {} a signalé {} transactions", userId, transactionIds.size());
        return transactionIds;
    }

    // ====================================================================
    // 🆕 NOUVELLES MÉTHODES POUR FONCTIONNALITÉ DÉTAILS LITIGE
    // ====================================================================

    /**
     * ✅ NOUVELLE MÉTHODE : Récupérer les détails complets d'un litige
     */
    public LitigeDetailsResponse getLitigeCompletDetails(Long litigeId) {
        logger.info("🔍 Récupération des détails complets du litige ID: {}", litigeId);

        try {
            // 1. Récupérer le litige principal
            Optional<Litige> litigeOpt = litigeRepository.findById(litigeId);
            if (litigeOpt.isEmpty()) {
                logger.warn("⚠️ Litige {} non trouvé", litigeId);
                throw new RuntimeException("Litige non trouvé avec l'ID: " + litigeId);
            }

            Litige litige = litigeOpt.get();
            logger.debug("✅ Litige trouvé: {}", litige.getId());

            // 2. Calculer la durée depuis création
            Long dureeMinutes = calculateDurationSinceCreation(litige.getDateCreation());

            // 3. Récupérer les détails de la transaction
            LitigeDetailsResponse.TransactionCompleteDetails transactionDetails = null;
            if (litige.getTransaction() != null) {
                transactionDetails = buildTransactionDetails(litige.getTransaction());
            }

            // 4. Récupérer le nom de l'utilisateur créateur
            String utilisateurCreateur = getUserCreatorName(litige.getDeclarePar());

            // 5. Récupérer la banque déclarante
            String banqueDeclaranteNom = getBanqueDeclaranteName(litige);

            // 6. Construire la réponse complète
            LitigeDetailsResponse response = LitigeDetailsResponse.builder()
                    .id(litige.getId())
                    .type(litige.getType())
                    .statut(litige.getStatut())
                    .description(litige.getDescription())
                    .dateCreation(litige.getDateCreation())
                    .dateResolution(litige.getDateResolution())
                    .banqueDeclaranteNom(banqueDeclaranteNom)
                    .utilisateurCreateur(utilisateurCreateur)
                    .justificatifPath(litige.getJustificatifPath())
                    .dureeDepuisCreationMinutes(dureeMinutes)
                    .transaction(transactionDetails)
                    .estLu(litige.getStatut() != StatutLitige.CREE)
                    .peutEtreModifie(canBeModified(litige))
                    .priorite(calculatePriority(dureeMinutes, litige.getType()))
                    .historique(Collections.emptyList())
                    .actions(Collections.emptyList())
                    .build();

            logger.info("✅ Détails complets récupérés pour le litige {}", litigeId);
            return response;

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des détails du litige {}", litigeId, e);
            throw new RuntimeException("Erreur lors de la récupération des détails: " + e.getMessage(), e);
        }
    }

    /**
     * ✅ MÉTHODE HELPER : Calculer la durée depuis la création
     */
    private Long calculateDurationSinceCreation(LocalDate dateCreation) {
        if (dateCreation == null) return null;

        LocalDate now = LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(dateCreation, now) * 24 * 60; // convertir en minutes
    }

    /**
     * ✅ MÉTHODE HELPER : Construire les détails de la transaction
     */
    private LitigeDetailsResponse.TransactionCompleteDetails buildTransactionDetails(Transaction transaction) {
        logger.debug("🔍 Construction des détails de la transaction ID: {}", transaction.getId());

        try {
            LitigeDetailsResponse.BanqueInfo banqueEmettrice = null;
            LitigeDetailsResponse.BanqueInfo banqueAcquereuse = null;

            if (transaction.getBanqueEmettrice() != null) {
                banqueEmettrice = buildBanqueInfo(transaction.getBanqueEmettrice());
            }

            if (transaction.getBanqueAcquereuse() != null) {
                banqueAcquereuse = buildBanqueInfo(transaction.getBanqueAcquereuse());
            }

            LitigeDetailsResponse.SatimDataDTO satimData = getSatimDataForTransaction(transaction);

            return LitigeDetailsResponse.TransactionCompleteDetails.builder()
                    .id(transaction.getId())
                    .reference(transaction.getReference())
                    .montant(transaction.getMontant() != null ? transaction.getMontant().doubleValue() : 0.0)
                    .dateTransaction(transaction.getDateTransaction())
                    .type(transaction.getType() != null ? transaction.getType().toString() : "AUTRE")
                    .statut(transaction.getStatut() != null ? transaction.getStatut().toString() : "NORMALE")
                    .banqueEmettrice(banqueEmettrice)
                    .banqueAcquereuse(banqueAcquereuse)
                    .satimData(satimData)
                    .build();

        } catch (Exception e) {
            logger.error("❌ Erreur construction détails transaction {}", transaction.getId(), e);
            return null;
        }
    }

    /**
     * ✅ MÉTHODE HELPER : Construire les informations d'une banque
     */
    private LitigeDetailsResponse.BanqueInfo buildBanqueInfo(Institution institution) {
        return LitigeDetailsResponse.BanqueInfo.builder()
                .id(institution.getId())
                .nom(institution.getNom())
                .code("BNK" + String.format("%03d", institution.getId())) // Code généré
                .adresse("Non renseignée") // Pas d'adresse dans votre modèle
                .telephone("Non renseigné") // Pas de téléphone dans votre modèle
                .email("Non renseigné") // Pas d'email dans votre modèle
                .type(institution.getType() != null ? institution.getType().toString() : "AUTRE")
                .enabled(institution.isEnabled())
                .build();
    }

    /**
     * ✅ MÉTHODE HELPER : Récupérer les données SATIM pour une transaction
     */
    private LitigeDetailsResponse.SatimDataDTO getSatimDataForTransaction(Transaction transaction) {
        try {
            // Rechercher par référence dans les données SATIM
            Optional<SatimTransaction> satimOpt = satimTransactionRepository
                    .findByStrRecoCode(transaction.getReference());

            if (satimOpt.isPresent()) {
                SatimTransaction satim = satimOpt.get();
                return LitigeDetailsResponse.SatimDataDTO.builder()
                        .strCode(satim.getStrCode())
                        .strRecoCode(satim.getStrRecoCode())
                        .strRecoNumb(satim.getStrRecoNumb() != null ? satim.getStrRecoNumb().doubleValue() : 0.0)
                        .strProcDate(satim.getStrProcDate() != null ? satim.getStrProcDate().toString() : null)
                        .strOperCode(satim.getStrOperCode())
                        .strTermIden(satim.getStrTermIden())
                        .strIssuBanCode(satim.getStrIssuBanCode())
                        .strAcquBanCode(satim.getStrAcquBanCode())
                        .strAuthNumb("AUTH_" + (satim.getStrCode() != null ? satim.getStrCode().toString().substring(0, Math.min(6, satim.getStrCode().toString().length())) : "000000"))
                        .strMercIden("MERC_" + transaction.getId())
                        .strMercLoca("Location non disponible")
                        .strPurcAmt(transaction.getMontant() != null ? transaction.getMontant().doubleValue() : 0.0)
                        .strCardNumb("**** **** **** " + (satim.getStrCode() != null ? satim.getStrCode().toString().substring(Math.max(0, satim.getStrCode().toString().length() - 4)) : "0000"))
                        .transactionId(transaction.getId())
                        .build();
            }

            return null;
        } catch (Exception e) {
            logger.warn("⚠️ Impossible de récupérer les données SATIM pour la transaction {}",
                    transaction.getReference(), e);
            return null;
        }
    }

    /**
     * ✅ MÉTHODE HELPER : Récupérer le nom de l'utilisateur créateur
     */
    private String getUserCreatorName(Utilisateur utilisateur) {
        if (utilisateur == null) return "Utilisateur inconnu";

        StringBuilder nom = new StringBuilder();
        if (utilisateur.getNom() != null) {
            nom.append(utilisateur.getNom());
        }
        if (utilisateur.getEmail() != null) {
            nom.append(" (").append(utilisateur.getEmail()).append(")");
        }

        return nom.length() > 0 ? nom.toString().trim() : "Utilisateur #" + utilisateur.getId();
    }

    /**
     * ✅ MÉTHODE HELPER : Récupérer le nom de la banque déclarante
     */
    private String getBanqueDeclaranteName(Litige litige) {
        if (litige.getBanqueDeclarante() != null) {
            return litige.getBanqueDeclarante().getNom();
        }

        // Fallback : essayer de déterminer depuis l'utilisateur
        if (litige.getDeclarePar() != null && litige.getDeclarePar().getInstitution() != null) {
            return litige.getDeclarePar().getInstitution().getNom();
        }

        return "Banque inconnue";
    }

    /**
     * ✅ MÉTHODE HELPER : Vérifier si le litige peut être modifié
     */
    private Boolean canBeModified(Litige litige) {
        // Un litige peut être modifié s'il n'est pas résolu ou fermé
        return litige.getStatut() != StatutLitige.RESOLU &&
                litige.getStatut() != StatutLitige.FERME;
    }

    /**
     * ✅ MÉTHODE HELPER : Calculer la priorité basée sur la durée et le type
     */
    private String calculatePriority(Long dureeMinutes, TypeLitige type) {
        if (dureeMinutes == null) return "NORMALE";

        // Priorité basée sur la durée (en jours)
        long dureeJours = dureeMinutes / (24 * 60);
        if (dureeJours > 3) {
            return "HAUTE";
        } else if (dureeJours > 1) {
            return "MOYENNE";
        }

        // Priorité basée sur le type
        if (type == TypeLitige.FRAUDE_SUSPECTE || type == TypeLitige.TRANSACTION_NON_AUTORISEE) {
            return "HAUTE";
        }

        return "NORMALE";
    }
}