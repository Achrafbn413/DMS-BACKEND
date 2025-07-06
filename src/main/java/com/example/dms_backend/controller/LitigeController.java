package com.example.dms_backend.controller;

import com.example.dms_backend.dto.LitigeRequest;
import com.example.dms_backend.model.*;
import com.example.dms_backend.repository.LitigeRepository;
import com.example.dms_backend.repository.MetaTransactionRepository;
import com.example.dms_backend.repository.SatimTransactionRepository;
import com.example.dms_backend.repository.TransactionRepository;
import com.example.dms_backend.repository.UserRepository;
import com.example.dms_backend.service.LitigeService;
import com.example.dms_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/public/litiges")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LitigeController {

    private static final Logger logger = LoggerFactory.getLogger(LitigeController.class);

    private final TransactionRepository transactionRepository;
    private final LitigeRepository litigeRepository;
    private final UserRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final LitigeService litigeService;

    // ✅ AJOUT: Repositories nécessaires pour le diagnostic
    private final MetaTransactionRepository metaTransactionRepository;
    private final SatimTransactionRepository satimTransactionRepository;

    @PostMapping
    public ResponseEntity<?> creerLitigeClient(@RequestBody LitigeRequest request) {
        logger.info("Tentative de création de litige avec transactionId={} et utilisateurId={}",
                request.getTransactionId(), request.getUtilisateurId());

        Optional<Transaction> transactionOpt = transactionRepository.findById(request.getTransactionId());
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(request.getUtilisateurId());

        if (transactionOpt.isEmpty() || utilisateurOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Transaction ou utilisateur introuvable.");
        }

        Transaction transaction = transactionOpt.get();
        Utilisateur utilisateur = utilisateurOpt.get();

        Litige litige = Litige.builder()
                .type(request.getType())
                .description(request.getDescription())
                .statut(StatutLitige.CREE)
                .dateCreation(LocalDate.now())
                .transaction(transaction)
                .declarePar(utilisateur)
                .build();

        transaction.setStatut(StatutTransaction.AVEC_LITIGE);
        transaction.setLitige(litige);

        litigeRepository.save(litige);
        transactionRepository.save(transaction);

        notificationService.notifierBanqueAcquereuse(litige);

        return ResponseEntity.ok("Litige créé avec succès.");
    }

    @PostMapping("/flag")
    public ResponseEntity<?> flagTransaction(@RequestBody LitigeRequest request) {
        logger.info("🎯 [API] POST /flag - Reçu: transactionId={}, utilisateurId={}",
                request.getTransactionId(), request.getUtilisateurId());

        try {
            Litige litige = litigeService.flagTransaction(request);
            logger.info("✅ Litige signalé avec succès, ID={}", litige.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Transaction signalée avec succès",
                    "litigeId", litige.getId(),
                    "transactionId", litige.getTransaction().getId()
            ));
        } catch (IllegalStateException e) {
            logger.warn("⚠️ Tentative de signalement d'une transaction déjà signalée: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "code", "LITIGE_ALREADY_EXISTS"
            ));
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors du signalement du litige: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "code", "TRANSACTION_NOT_FOUND"
            ));
        } catch (Exception e) {
            logger.error("❌ Erreur interne lors du signalement du litige: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erreur interne du serveur",
                    "code", "INTERNAL_ERROR"
            ));
        }
    }

    @GetMapping
    public List<Litige> getAllLitiges() {
        return litigeRepository.findAll();
    }

    // ✅ NOUVEAU: Endpoint pour récupérer les litiges par institution
    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<List<Litige>> getLitigesByInstitution(@PathVariable Long institutionId) {
        try {
            logger.info("🏦 Récupération des litiges pour l'institution ID: {}", institutionId);
            List<Litige> litiges = litigeRepository.findByInstitutionId(institutionId);
            logger.info("✅ {} litiges trouvés pour l'institution {}", litiges.size(), institutionId);
            return ResponseEntity.ok(litiges);
        } catch (Exception e) {
            logger.error("❌ Erreur récupération litiges pour institution {}: {}", institutionId, e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ NOUVEAU: Endpoint pour récupérer les litiges non lus par institution
    @GetMapping("/unread/{institutionId}")
    public ResponseEntity<List<Litige>> getUnreadLitigesByInstitution(@PathVariable Long institutionId) {
        try {
            logger.info("🔔 Récupération des litiges non lus pour l'institution ID: {}", institutionId);
            List<Litige> litiges = litigeRepository.findUnreadByInstitutionId(institutionId);
            logger.info("✅ {} litiges non lus trouvés pour l'institution {}", litiges.size(), institutionId);
            return ResponseEntity.ok(litiges);
        } catch (Exception e) {
            logger.error("❌ Erreur récupération litiges non lus pour institution {}: {}", institutionId, e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    // ✅ NOUVEAU: Endpoint pour marquer un litige comme lu
    @PutMapping("/{litigeId}/mark-read")
    public ResponseEntity<?> markAsRead(@PathVariable Long litigeId) {
        try {
            logger.info("👁️ Marquage du litige {} comme lu", litigeId);

            Optional<Litige> litigeOpt = litigeRepository.findById(litigeId);
            if (litigeOpt.isEmpty()) {
                logger.warn("⚠️ Litige {} non trouvé", litigeId);
                return ResponseEntity.notFound().build();
            }

            Litige litige = litigeOpt.get();
            litige.setStatut(StatutLitige.VU); // Assuming you have this status
            litigeRepository.save(litige);

            logger.info("✅ Litige {} marqué comme lu", litigeId);
            return ResponseEntity.ok(Map.of("message", "Litige marqué comme lu"));
        } catch (Exception e) {
            logger.error("❌ Erreur marquage litige {} comme lu: {}", litigeId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erreur interne du serveur"
            ));
        }
    }

    /**
     * ✅ ENDPOINT DE DIAGNOSTIC: Vérifier l'existence d'une transaction par ID/strCode
     * Utile pour déboguer les problèmes de signalement
     */
    @GetMapping("/diagnostic/transaction/{transactionId}")
    public ResponseEntity<?> diagnosticTransaction(@PathVariable Long transactionId) {
        logger.info("🔍 [DIAGNOSTIC] Recherche de transaction avec ID/strCode: {}", transactionId);

        Map<String, Object> diagnostic = new HashMap<>();
        diagnostic.put("searchId", transactionId);
        diagnostic.put("timestamp", LocalDateTime.now());

        try {
            // 1. Recherche directe dans Transaction
            Optional<Transaction> directTransaction = transactionRepository.findById(transactionId);
            diagnostic.put("foundByDirectId", directTransaction.isPresent());
            if (directTransaction.isPresent()) {
                Transaction t = directTransaction.get();
                diagnostic.put("directTransaction", Map.of(
                        "id", t.getId(),
                        "reference", t.getReference() != null ? t.getReference() : "null",
                        "montant", t.getMontant() != null ? t.getMontant() : "null",
                        "statut", t.getStatut() != null ? t.getStatut() : "null",
                        "hasLitige", t.getLitige() != null
                ));
            }

            // 2. Recherche dans MetaTransaction par strCode
            Optional<MetaTransaction> metaByStrCode = metaTransactionRepository.findByStrCode(transactionId);
            diagnostic.put("foundMetaByStrCode", metaByStrCode.isPresent());
            if (metaByStrCode.isPresent()) {
                MetaTransaction mt = metaByStrCode.get();
                diagnostic.put("metaTransaction", Map.of(
                        "id", mt.getId() != null ? mt.getId() : "null",
                        "strCode", mt.getStrCode() != null ? mt.getStrCode() : "null",
                        "strRecoCode", mt.getStrRecoCode() != null ? mt.getStrRecoCode() : "null",
                        "hasLinkedTransaction", mt.getTransaction() != null
                ));

                if (mt.getTransaction() != null) {
                    Transaction linked = mt.getTransaction();
                    diagnostic.put("linkedTransaction", Map.of(
                            "id", linked.getId(),
                            "reference", linked.getReference() != null ? linked.getReference() : "null",
                            "statut", linked.getStatut() != null ? linked.getStatut() : "null",
                            "hasLitige", linked.getLitige() != null
                    ));
                }
            }

            // 3. Recherche dans SatimTransaction
            Optional<SatimTransaction> satimTransaction = satimTransactionRepository.findById(transactionId);
            diagnostic.put("foundInSatim", satimTransaction.isPresent());
            if (satimTransaction.isPresent()) {
                SatimTransaction st = satimTransaction.get();
                diagnostic.put("satimTransaction", Map.of(
                        "strCode", st.getStrCode() != null ? st.getStrCode() : "null",
                        "strRecoCode", st.getStrRecoCode() != null ? st.getStrRecoCode() : "null",
                        "strRecoNumb", st.getStrRecoNumb() != null ? st.getStrRecoNumb() : "null",
                        "strOperCode", st.getStrOperCode() != null ? st.getStrOperCode() : "null"
                ));
            }

            // 4. Recherche par référence (si transactionId peut être une référence)
            Optional<Transaction> byReference = transactionRepository.findByReference(transactionId.toString());
            diagnostic.put("foundByReference", byReference.isPresent());

            // 5. Statistiques générales
            long totalTransactions = transactionRepository.count();
            long totalMetaTransactions = metaTransactionRepository.count();
            long totalSatimTransactions = satimTransactionRepository.count();
            long orphanedMeta = metaTransactionRepository.countOrphanedMetaTransactions();

            diagnostic.put("statistics", Map.of(
                    "totalTransactions", totalTransactions,
                    "totalMetaTransactions", totalMetaTransactions,
                    "totalSatimTransactions", totalSatimTransactions,
                    "orphanedMetaTransactions", orphanedMeta
            ));

            // 6. Recommandations
            List<String> recommendations = new ArrayList<>();
            if (!directTransaction.isPresent() && !metaByStrCode.isPresent() && !satimTransaction.isPresent()) {
                recommendations.add("❌ Aucune transaction trouvée - Vérifiez l'import SATIM");
            }
            if (metaByStrCode.isPresent() && metaByStrCode.get().getTransaction() == null) {
                recommendations.add("⚠️ MetaTransaction trouvée mais non liée - Problème d'import");
            }
            if (satimTransaction.isPresent() && !metaByStrCode.isPresent()) {
                recommendations.add("🔧 SatimTransaction existe mais pas de MetaTransaction - Relancer l'import");
            }
            if (orphanedMeta > 0) {
                recommendations.add("🧹 " + orphanedMeta + " MetaTransactions orphelines détectées");
            }

            diagnostic.put("recommendations", recommendations);

            logger.info("✅ [DIAGNOSTIC] Diagnostic terminé pour transactionId={}", transactionId);
            return ResponseEntity.ok(diagnostic);

        } catch (Exception e) {
            logger.error("❌ [DIAGNOSTIC] Erreur lors du diagnostic: {}", e.getMessage(), e);
            diagnostic.put("error", e.getMessage());
            return ResponseEntity.status(500).body(diagnostic);
        }
    }
}