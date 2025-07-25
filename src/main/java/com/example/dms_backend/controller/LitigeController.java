package com.example.dms_backend.controller;
import com.example.dms_backend.dto.LitigeDetailsResponse;

import com.example.dms_backend.dto.LitigeRequest;
import com.example.dms_backend.dto.LitigeResponseDTO;
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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.*;

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

    private final MetaTransactionRepository metaTransactionRepository;
    private final SatimTransactionRepository satimTransactionRepository;

    /**
     * ✅ Créer un litige sécurisé (méthode unique à utiliser)
     */
    @PostMapping("/flag")
    public ResponseEntity<?> flagTransaction(@RequestBody LitigeRequest request) {
        logger.info("🎯 [API] POST /flag - Reçu: transactionId={}, utilisateurId={}",
                request.getTransactionId(), request.getUtilisateurId());

        try {
            if (request.getTransactionId() == null || request.getUtilisateurId() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "TransactionId et UtilisateurId sont requis",
                        "code", "MISSING_PARAMETERS"
                ));
            }

            Litige litige = litigeService.flagTransaction(request);
            logger.info("✅ Litige signalé avec succès, ID={}", litige.getId());

            return ResponseEntity.ok(Map.of(
                    "message", "Transaction signalée avec succès",
                    "litigeId", litige.getId(),
                    "transactionId", litige.getTransaction().getId(),
                    "statut", litige.getStatut().toString()
            ));

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "code", "LITIGE_ALREADY_EXISTS"
            ));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Violation de contrainte de données",
                    "code", "DATA_INTEGRITY_VIOLATION",
                    "details", e.getMostSpecificCause().getMessage()
            ));
        } catch (TransactionSystemException e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erreur de transaction de base de données",
                    "code", "DATABASE_TRANSACTION_ERROR"
            ));
        } catch (DataAccessException e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erreur d'accès aux données",
                    "code", "DATABASE_ACCESS_ERROR"
            ));
        } catch (Exception e) {
            logger.error("❌ Erreur lors du signalement de litige", e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erreur interne du serveur",
                    "code", "INTERNAL_ERROR",
                    "timestamp", LocalDateTime.now().toString()
            ));
        }
    }

    /**
     * ✅ Récupérer tous les litiges
     */
    @GetMapping
    public List<Litige> getAllLitiges() {
        return litigeRepository.findAll();
    }

    /**
     * 🔥 CORRIGÉ : Litiges émis par notre institution (ce qu'on affiche dans le dashboard principal)
     */
    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<List<LitigeResponseDTO>> getLitigesByInstitution(@PathVariable Long institutionId) {
        logger.info("🔍 Récupération des litiges émis par l'institution: {}", institutionId);

        try {
            // ✅ CHANGEMENT : Utilise les litiges émis pour l'affichage principal
            List<Litige> litiges = litigeRepository.findLitigesEmisParInstitution(institutionId);

            List<LitigeResponseDTO> dtos = litiges.stream().map(l -> {
                String banqueDeclaranteNom = "Notre banque";
                String institutionDeclarantNom = "Nous";

                // ✅ Vérification améliorée pour banqueDeclarante
                if (l.getBanqueDeclarante() != null) {
                    banqueDeclaranteNom = l.getBanqueDeclarante().getNom();
                    logger.debug("✅ Banque déclarante trouvée: {}", banqueDeclaranteNom);
                } else {
                    logger.warn("⚠️ Banque déclarante manquante pour litige ID: {}", l.getId());
                }

                // Pour les litiges émis, on affiche contre qui on l'a déclaré
                if (l.getTransaction() != null) {
                    if (l.getTransaction().getBanqueAcquereuse() != null &&
                            !l.getTransaction().getBanqueAcquereuse().getId().equals(institutionId)) {
                        institutionDeclarantNom = "Contre " + l.getTransaction().getBanqueAcquereuse().getNom();
                    } else if (l.getTransaction().getBanqueEmettrice() != null &&
                            !l.getTransaction().getBanqueEmettrice().getId().equals(institutionId)) {
                        institutionDeclarantNom = "Contre " + l.getTransaction().getBanqueEmettrice().getNom();
                    }
                }

                return LitigeResponseDTO.builder()
                        .id(l.getId())
                        .type(l.getType())
                        .statut(l.getStatut())
                        .description(l.getDescription())
                        .dateCreation(l.getDateCreation())
                        .banqueDeclaranteNom(banqueDeclaranteNom)
                        .institutionDeclarantNom(institutionDeclarantNom)
                        .build();
            }).toList();

            logger.info("✅ {} litiges émis récupérés pour l'institution {}", dtos.size(), institutionId);
            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des litiges pour l'institution {}", institutionId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 🔥 NOUVEAU : Litiges émis par notre institution
     */
    @GetMapping("/emis/{institutionId}")
    public ResponseEntity<List<LitigeResponseDTO>> getLitigesEmis(@PathVariable Long institutionId) {
        logger.info("🔍 Récupération des litiges émis par l'institution: {}", institutionId);

        try {
            List<Litige> litiges = litigeRepository.findLitigesEmisParInstitution(institutionId);

            List<LitigeResponseDTO> dtos = litiges.stream().map(l -> {
                String banqueDeclaranteNom = "Nous";
                String institutionDeclarantNom = "Notre institution";

                if (l.getBanqueDeclarante() != null) {
                    banqueDeclaranteNom = l.getBanqueDeclarante().getNom();
                }

                // Pour les litiges émis, la banque cible est acquereuse/emettrice
                if (l.getTransaction() != null) {
                    if (l.getTransaction().getBanqueAcquereuse() != null &&
                            !l.getTransaction().getBanqueAcquereuse().getId().equals(institutionId)) {
                        institutionDeclarantNom = "Contre " + l.getTransaction().getBanqueAcquereuse().getNom();
                    } else if (l.getTransaction().getBanqueEmettrice() != null &&
                            !l.getTransaction().getBanqueEmettrice().getId().equals(institutionId)) {
                        institutionDeclarantNom = "Contre " + l.getTransaction().getBanqueEmettrice().getNom();
                    }
                }

                return LitigeResponseDTO.builder()
                        .id(l.getId())
                        .type(l.getType())
                        .statut(l.getStatut())
                        .description(l.getDescription())
                        .dateCreation(l.getDateCreation())
                        .banqueDeclaranteNom(banqueDeclaranteNom)
                        .institutionDeclarantNom(institutionDeclarantNom)
                        .build();
            }).toList();

            logger.info("✅ {} litiges émis récupérés pour l'institution {}", dtos.size(), institutionId);
            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des litiges émis pour l'institution {}", institutionId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 🔥 CORRIGÉ : Litiges reçus d'autres banques
     */
    @GetMapping("/reçus/{institutionId}")
    public ResponseEntity<List<LitigeResponseDTO>> getLitigesRecus(@PathVariable Long institutionId) {
        logger.info("🔍 Récupération des litiges reçus pour l'institution: {}", institutionId);

        try {
            // ✅ CHANGEMENT PRINCIPAL : Utilise la nouvelle méthode
            List<Litige> litiges = litigeRepository.findLitigesRecusParInstitution(institutionId);

            List<LitigeResponseDTO> dtoList = litiges.stream().map(l -> {
                String banqueDeclaranteNom = "Inconnue";
                String institutionDeclarantNom = "Institution inconnue";

                // ✅ LOGIQUE CORRIGÉE : banqueDeclarante existe maintenant
                if (l.getBanqueDeclarante() != null) {
                    banqueDeclaranteNom = l.getBanqueDeclarante().getNom();
                    institutionDeclarantNom = l.getBanqueDeclarante().getNom();
                    logger.debug("✅ Litige reçu - Banque déclarante: {}", banqueDeclaranteNom);
                } else {
                    logger.warn("⚠️ Litige reçu ID {} - Banque déclarante manquante", l.getId());
                }

                return LitigeResponseDTO.builder()
                        .id(l.getId())
                        .type(l.getType())
                        .statut(l.getStatut())
                        .description(l.getDescription())
                        .dateCreation(l.getDateCreation())
                        .banqueDeclaranteNom(banqueDeclaranteNom)
                        .institutionDeclarantNom(institutionDeclarantNom)
                        .build();
            }).toList();

            logger.info("✅ {} litiges reçus récupérés pour l'institution {}", dtoList.size(), institutionId);
            return ResponseEntity.ok(dtoList);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des litiges reçus pour l'institution {}", institutionId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 🔥 CORRIGÉ : Litiges non lus - Utilise la nouvelle méthode
     */
    @GetMapping("/unread/{institutionId}")
    public ResponseEntity<List<Litige>> getUnreadLitigesByInstitution(@PathVariable Long institutionId) {
        logger.info("🔍 Récupération des litiges non lus pour l'institution: {}", institutionId);

        try {
            // ✅ Utilise la nouvelle méthode pour les notifications
            List<Litige> litiges = litigeRepository.findNotificationsNonLues(institutionId);
            logger.info("✅ {} notifications non lues trouvées pour l'institution {}", litiges.size(), institutionId);
            return ResponseEntity.ok(litiges);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des notifications pour l'institution {}", institutionId, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * ✅ Marquer litige comme lu
     */
    @PutMapping("/{litigeId}/mark-read")
    public ResponseEntity<?> markAsRead(@PathVariable Long litigeId) {
        logger.info("📖 Marquage du litige {} comme lu", litigeId);

        try {
            Optional<Litige> litigeOpt = litigeRepository.findById(litigeId);
            if (litigeOpt.isEmpty()) {
                logger.warn("⚠️ Litige {} non trouvé", litigeId);
                return ResponseEntity.notFound().build();
            }

            Litige litige = litigeOpt.get();
            litige.setStatut(StatutLitige.VU);
            litigeRepository.save(litige);

            logger.info("✅ Litige {} marqué comme lu", litigeId);
            return ResponseEntity.ok(Map.of("message", "Litige marqué comme lu"));
        } catch (Exception e) {
            logger.error("❌ Erreur lors du marquage du litige {} comme lu", litigeId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erreur interne du serveur"
            ));
        }
    }

    /**
     * ✅ Récupérer les IDs des transactions signalées par un utilisateur donné
     */
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<Long>> getTransactionIdsSignaledByUser(@PathVariable Long userId) {
        logger.info("🔍 Récupération des transactions signalées par l'utilisateur: {}", userId);

        try {
            List<Long> transactionIds = litigeRepository.findTransactionIdsByUser(userId);
            logger.info("✅ {} transactions signalées trouvées pour l'utilisateur {}", transactionIds.size(), userId);
            return ResponseEntity.ok(transactionIds);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des transactions signalées par l'utilisateur {}", userId, e);
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    /**
     * ✅ Alternative pour récupérer les transactions signalées par un utilisateur
     */
    @GetMapping("/signaled-transactions/{userId}")
    public ResponseEntity<List<Long>> getSignaledTransactionsByUser(@PathVariable Long userId) {
        logger.info("📋 Récupération des transactions signalées par utilisateur: {}", userId);

        try {
            List<Long> transactionIds = litigeService.getTransactionIdsSignaledByUser(userId);
            logger.info("✅ {} transactions signalées trouvées pour utilisateur {}", transactionIds.size(), userId);
            return ResponseEntity.ok(transactionIds);
        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des transactions signalées pour utilisateur {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 🆕 NOUVEAU : Récupérer les détails complets d'un litige
     */
    @GetMapping("/details/{litigeId}")
    public ResponseEntity<LitigeDetailsResponse> getLitigeDetails(@PathVariable Long litigeId) {
        logger.info("🔍 Récupération des détails du litige ID: {}", litigeId);

        try {
            LitigeDetailsResponse details = litigeService.getLitigeCompletDetails(litigeId);
            return ResponseEntity.ok(details);
        } catch (RuntimeException e) {
            logger.error("❌ Erreur lors de la récupération des détails du litige {}: {}", litigeId, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("❌ Erreur interne lors de la récupération des détails du litige {}", litigeId, e);
            return ResponseEntity.status(500).build();
        }
    }


}