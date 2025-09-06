package com.example.dms_backend.controller;
import com.example.dms_backend.model.JustificatifChargeback;
import com.example.dms_backend.model.LitigeChargeback;
import com.example.dms_backend.repository.JustificatifChargebackRepository;
import com.example.dms_backend.repository.LitigeChargebackRepository;
import com.example.dms_backend.repository.LitigeRepository;
import com.example.dms_backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import com.example.dms_backend.dto.*;
import com.example.dms_backend.model.Arbitrage;
import com.example.dms_backend.model.EchangeLitige;
import com.example.dms_backend.service.ChargebackWorkflowService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/chargebacks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ChargebackController {

    private final ChargebackWorkflowService chargebackWorkflowService;
    private final LitigeChargebackRepository litigeChargebackRepository;
    private final LitigeRepository litigeRepository;
    private final UserRepository userRepository;
    private final JustificatifChargebackRepository justificatifChargebackRepository;

    // =========================================================
    // 🚀 INITIATION DU CHARGEBACK
    // =========================================================
    /*
    @PostMapping("/initiate")
    public ResponseEntity<?> initierChargeback(@RequestBody InitiationChargebackRequest request) {
        log.info("🎯 [API] Initiation chargeback - LitigeId: {}", request.getLitigeId());
        try {
            LitigeChargebackDTO result = chargebackWorkflowService.initierChargeback(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "code", "VALIDATION_ERROR"));
        } catch (Exception e) {
            log.error("❌ Erreur initiation chargeback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'initiation du chargeback", "details", e.getMessage()));
        }
    }
     */
// V2: (demo)
    @PostMapping("/initiate")
    public ResponseEntity<?> initierChargeback(
            @jakarta.validation.Valid @RequestBody InitiationChargebackRequest request) {
        log.info("🎯 [API] Initiation chargeback - LitigeId: {}", request.getLitigeId());

        try {
            LitigeChargebackDTO result = chargebackWorkflowService.initierChargeback(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);
        } catch (IllegalArgumentException e) {
            log.error("❌ Erreur validation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "code", "VALIDATION_ERROR"));
        } catch (IllegalStateException e) {
            log.error("❌ Erreur état: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage(), "code", "STATE_ERROR"));
        } catch (Exception e) {
            log.error("❌ Erreur interne", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'initiation du chargeback", "details", e.getMessage()));
        }
    }

    // =========================================================
    // 🔄 REPRESENTATION
    // =========================================================
    @PostMapping("/representation")
    public ResponseEntity<?> traiterRepresentation(@RequestBody RepresentationRequest request) {
        log.info("🔄 [CONTROLLER] ===== DÉBUT REPRÉSENTATION =====");
        log.info("🔄 [CONTROLLER] Requête reçue: {}", request);

        try {
            // Validation de base de la requête
            if (request == null) {
                log.error("❌ [CONTROLLER] Requête nulle reçue");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Requête de représentation nulle"));
            }

            log.info("✅ [CONTROLLER] Requête valide, appel du service...");

            // Appel du service avec gestion d'erreur détaillée
            LitigeChargebackDTO result = chargebackWorkflowService.traiterRepresentation(request);

            log.info("✅ [CONTROLLER] Service terminé avec succès - ID: {}", result.getId());
            log.info("✅ [CONTROLLER] ===== FIN REPRÉSENTATION SUCCÈS =====");

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.error("❌ [CONTROLLER] Erreur de validation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));

        } catch (IllegalStateException e) {
            log.error("❌ [CONTROLLER] Erreur d'état: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));

        } catch (RuntimeException e) {
            log.error("❌ [CONTROLLER] Erreur runtime: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur interne: " + e.getMessage()));

        } catch (Exception e) {
            log.error("❌ [CONTROLLER] Erreur inattendue: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur inattendue lors du traitement"));
        }
    }
    // =========================================================
// 🧪 ENDPOINT DE TEST POUR DIAGNOSTIC
// =========================================================
    @PostMapping("/representation/test")
    public ResponseEntity<?> testRepresentation(@RequestBody RepresentationRequest request) {
        log.info("🧪 [TEST] ===== TEST REPRÉSENTATION =====");
        log.info("🧪 [TEST] LitigeId: {}", request.getLitigeId());
        log.info("🧪 [TEST] UtilisateurId: {}", request.getUtilisateurAcquereurId());
        log.info("🧪 [TEST] BanqueId: {}", request.getBanqueAcquereuseId());

        try {
            // Test 1: Vérifier existence litige
            log.info("🧪 [TEST-1] Vérification existence litige...");
            var litige = litigeRepository.findById(request.getLitigeId()).orElse(null);
            if (litige == null) {
                log.error("❌ [TEST-1] Litige non trouvé: {}", request.getLitigeId());
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Litige non trouvé: " + request.getLitigeId(),
                        "step", "1 - Vérification litige"
                ));
            }
            log.info("✅ [TEST-1] Litige trouvé: {}", litige.getId());

            // Test 2: Vérifier existence chargeback
            log.info("🧪 [TEST-2] Vérification existence chargeback...");
            var chargebackOpt = litigeChargebackRepository.findByLitigeId(request.getLitigeId());
            if (!chargebackOpt.isPresent()) {
                log.error("❌ [TEST-2] Chargeback non trouvé pour litige: {}", request.getLitigeId());

                // Lister tous les chargebacks disponibles
                var allChargebacks = litigeChargebackRepository.findAll();
                log.info("🔍 [TEST-2] Chargebacks disponibles: {}",
                        allChargebacks.stream().map(cb -> "ID:" + cb.getId() + ",LitigeId:" + cb.getLitigeId()).toList());

                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Chargeback non trouvé pour litige: " + request.getLitigeId(),
                        "step", "2 - Vérification chargeback",
                        "availableChargebacks", allChargebacks.stream()
                                .map(cb -> Map.of("id", cb.getId(), "litigeId", cb.getLitigeId()))
                                .toList()
                ));
            }

            var chargeback = chargebackOpt.get();
            log.info("✅ [TEST-2] Chargeback trouvé - ID: {}, Phase: {}", chargeback.getId(), chargeback.getPhaseActuelle());

            // Test 3: Vérifier utilisateur
            log.info("🧪 [TEST-3] Vérification utilisateur...");
            var user = userRepository.findById(request.getUtilisateurAcquereurId()).orElse(null);
            if (user == null) {
                log.error("❌ [TEST-3] Utilisateur non trouvé: {}", request.getUtilisateurAcquereurId());
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Utilisateur non trouvé: " + request.getUtilisateurAcquereurId(),
                        "step", "3 - Vérification utilisateur"
                ));
            }
            log.info("✅ [TEST-3] Utilisateur trouvé: {}", user.getNom());

            // Test 4: Vérifier transaction et banques
            log.info("🧪 [TEST-4] Vérification transaction et banques...");
            var transaction = litige.getTransaction();
            if (transaction == null) {
                log.error("❌ [TEST-4] Transaction non trouvée pour le litige");
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Transaction non trouvée pour le litige",
                        "step", "4 - Vérification transaction"
                ));
            }

            log.info("✅ [TEST-4] Transaction trouvée: {}", transaction.getReference());
            log.info("🔍 [TEST-4] Banque émettrice: {}",
                    transaction.getBanqueEmettrice() != null ? transaction.getBanqueEmettrice().getNom() : "null");
            log.info("🔍 [TEST-4] Banque acquéreuse: {}",
                    transaction.getBanqueAcquereuse() != null ? transaction.getBanqueAcquereuse().getNom() : "null");

            // Test 5: Vérifier phase du chargeback
            log.info("🧪 [TEST-5] Vérification phase chargeback...");
            String phase = chargeback.getPhaseActuelle();
            boolean phaseValide = "CHARGEBACK_INITIAL".equals(phase);
            log.info("🔍 [TEST-5] Phase actuelle: {}, Valide pour représentation: {}", phase, phaseValide);

            // Résultat du test
            var result = Map.of(
                    "success", true,
                    "message", "Tous les tests sont passés",
                    "litige", Map.of("id", litige.getId(), "description", litige.getDescription()),
                    "chargeback", Map.of("id", chargeback.getId(), "phase", chargeback.getPhaseActuelle()),
                    "utilisateur", Map.of("id", user.getId(), "nom", user.getNom()),
                    "transaction", Map.of("id", transaction.getId(), "reference", transaction.getReference()),
                    "phaseValide", phaseValide
            );

            log.info("✅ [TEST] Tous les tests réussis");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("❌ [TEST] Erreur lors du test: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "error", "Erreur test: " + e.getMessage(),
                            "step", "Exception inattendue",
                            "type", e.getClass().getSimpleName()
                    ));
        }
    }

    // =========================================================
    // ⚡ SECOND PRESENTMENT (PRE-ARBITRAGE)
    // =========================================================
    @PostMapping("/second-presentment")
    public ResponseEntity<?> traiterSecondPresentment(@RequestBody SecondPresentmentRequest request) {
        log.info("🎯 [API] Second Presentment - LitigeId: {}", request.getLitigeId());
        try {
            LitigeChargebackDTO result = chargebackWorkflowService.traiterSecondPresentment(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "code", "VALIDATION_ERROR"));
        } catch (Exception e) {
            log.error("❌ Erreur second presentment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du second presentment", "details", e.getMessage()));
        }
    }

    // =========================================================
    // ⚖️ DEMANDE D'ARBITRAGE
    // =========================================================
    @PostMapping("/arbitrage")
    public ResponseEntity<?> demanderArbitrage(@RequestBody InitiationArbitrageRequest request) {
        log.info("🎯 [API] Demande arbitrage - LitigeId: {}", request.getLitigeId());
        try {
            ArbitrageDTO result = chargebackWorkflowService.demanderArbitrage(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "code", "VALIDATION_ERROR"));
        } catch (Exception e) {
            log.error("❌ Erreur demande arbitrage", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la demande d'arbitrage", "details", e.getMessage()));
        }
    }

    // =========================================================
    // 🏛️ DECISION ARBITRAGE (ADMIN)
    // =========================================================
    @PutMapping("/arbitrage/{arbitrageId}/decision")
    public ResponseEntity<?> rendreDecisionArbitrage(
            @PathVariable Long arbitrageId,
            @RequestParam Arbitrage.Decision decision,
            @RequestParam String motifs,
            @RequestParam Arbitrage.RepartitionFrais repartitionFrais,
            @RequestParam Long adminId) {

        log.info("🎯 [API] Décision arbitrage - ArbitrageId: {}", arbitrageId);
        try {
            ArbitrageDTO result = chargebackWorkflowService.rendreDecisionArbitrage(arbitrageId, decision, motifs, repartitionFrais, adminId);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "code", "VALIDATION_ERROR"));
        } catch (Exception e) {
            log.error("❌ Erreur décision arbitrage", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du rendu de décision d'arbitrage", "details", e.getMessage()));
        }
    }

    // =========================================================
    // 📊 LISTE ET STATISTIQUES
    // =========================================================
    @GetMapping("/institution/{institutionId}")
    public ResponseEntity<List<LitigeChargebackDTO>> getLitigesChargebackParInstitution(@PathVariable Long institutionId) {
        log.info("📊 Liste chargebacks pour institution {}", institutionId);
        return ResponseEntity.ok(chargebackWorkflowService.getLitigesChargebackParInstitution(institutionId));
    }

    @GetMapping("/phase/{phase}")
    public ResponseEntity<List<LitigeChargebackDTO>> getLitigesChargebackParPhase(@PathVariable String phase) {
        log.info("📊 Liste chargebacks en phase {}", phase);
        return ResponseEntity.ok(chargebackWorkflowService.getLitigesChargebackParPhase(phase));
    }

    @GetMapping("/urgent")
    public ResponseEntity<List<LitigeChargebackDTO>> getLitigesChargebackUrgents() {
        log.info("🚨 Liste chargebacks urgents");
        return ResponseEntity.ok(chargebackWorkflowService.getLitigesChargebackUrgents());
    }

    @GetMapping("/stats/{institutionId}")
    public ResponseEntity<?> getStatistiquesChargeback(@PathVariable Long institutionId) {
        log.info("📊 Stats chargebacks pour institution {}", institutionId);
        Object[] stats = chargebackWorkflowService.getStatistiquesChargebackInstitution(institutionId);
        return ResponseEntity.ok(Map.of(
                "total", stats[0],
                "enCours", stats[1],
                "finalises", stats[2],
                "urgents", stats[3],
                "montantTotal", stats[4]
        ));
    }

    @GetMapping("/history/{litigeId}")
    public ResponseEntity<List<EchangeLitige>> getHistoriqueComplet(@PathVariable Long litigeId) {
        log.info("📋 Historique complet du litige {}", litigeId);
        return ResponseEntity.ok(chargebackWorkflowService.getHistoriqueComplet(litigeId));
    }

    // =========================================================
    // 🚫 ANNULATION CHARGEBACK
    // =========================================================
    @PutMapping("/{litigeId}/cancel")
    public ResponseEntity<?> annulerChargeback(
            @PathVariable Long litigeId,
            @RequestParam Long utilisateurId,
            @RequestParam String motif) {
        log.info("🚫 Annulation chargeback - LitigeId: {}", litigeId);
        boolean result = chargebackWorkflowService.annulerChargeback(litigeId, utilisateurId, motif);
        if (result) {
            return ResponseEntity.ok(Map.of("message", "Chargeback annulé avec succès"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Impossible d'annuler ce chargeback"));
    }
    // =========================================================
    // 🔧 ENDPOINTS SUPPLÉMENTAIRES POUR LE FRONTEND
    // =========================================================
/*
    @PostMapping("/{chargebackId}/decision-arbitrage")
    public ResponseEntity<?> deciderArbitrage(
            @PathVariable Long chargebackId,
            @RequestBody DecisionArbitrageRequest request) {
        log.info("🏛️ [API] Décision arbitrage via POST - ChargebackId: {}", chargebackId);

        try {
            // Conversion des strings en enum
            Arbitrage.Decision decision = Arbitrage.Decision.valueOf(request.getDecision());
            Arbitrage.RepartitionFrais repartition = Arbitrage.RepartitionFrais.valueOf(request.getRepartitionFrais());

            ArbitrageDTO result = chargebackWorkflowService.rendreDecisionArbitrage(
                    chargebackId, decision, request.getMotifsDecision(), repartition, 1L);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ Erreur décision arbitrage POST", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la décision d'arbitrage", "details", e.getMessage()));
        }
    }
*/
    @GetMapping("/{litigeId}/historique")
    public ResponseEntity<?> getHistoriqueChargeback(@PathVariable Long litigeId) {
        log.info("📋 [API] Historique chargeback du litige {}", litigeId);

        try {
            List<EchangeLitige> result = chargebackWorkflowService.getHistoriqueComplet(litigeId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ Erreur historique chargeback", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération de l'historique", "details", e.getMessage()));
        }
    }

    // =========================================================
    // 🔍 ENDPOINT DEBUG POUR DIAGNOSTIC SÉPARATION
    // =========================================================
    @GetMapping("/debug/{institutionId}")
    public ResponseEntity<?> debugChargebacksSeparation(@PathVariable Long institutionId) {
        log.info("🔍 [DEBUG-API] Debug chargebacks pour institution {}", institutionId);

        try {
            // Appel de la méthode debug qui va logger toutes les informations
            chargebackWorkflowService.debugChargebacksSeparation(institutionId);

            // Retourner aussi les données pour l'API
            List<LitigeChargebackDTO> chargebacks = chargebackWorkflowService.getLitigesChargebackParInstitution(institutionId);

            return ResponseEntity.ok(Map.of(
                    "message", "Debug terminé - Vérifiez les logs du serveur",
                    "institutionId", institutionId,
                    "totalChargebacks", chargebacks.size(),
                    "chargebacks", chargebacks
            ));

        } catch (Exception e) {
            log.error("❌ Erreur debug chargebacks", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du debug", "details", e.getMessage()));
        }
    }
    // =========================================================
    // 📎 UPLOAD JUSTIFICATIFS
    // =========================================================
    @PostMapping("/{chargebackId}/upload-justificatifs")
    public ResponseEntity<?> uploadJustificatifs(
            @PathVariable Long chargebackId,
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("phase") String phase) {
        log.info("📎 [API] Upload justificatifs - ChargebackId: {}, Phase: {}", chargebackId, phase);

        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Aucun fichier fourni"));
            }

            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Fichier vide détecté"));
                }
                log.info("Fichier reçu: {} ({})", file.getOriginalFilename(), file.getSize());
            }

            // TODO: Implémenter la sauvegarde des fichiers
            return ResponseEntity.ok(Map.of(
                    "message", "Fichiers uploadés avec succès",
                    "count", files.length,
                    "chargebackId", chargebackId,
                    "phase", phase
            ));

        } catch (Exception e) {
            log.error("❌ Erreur upload justificatifs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de l'upload", "details", e.getMessage()));
        }
    }

    @GetMapping("/{chargebackId}/justificatifs")
    public ResponseEntity<?> getJustificatifs(@PathVariable Long chargebackId) {
        log.info("📎 [API] Récupération justificatifs - ChargebackId: {}", chargebackId);

        try {
            // Récupérer le chargeback pour obtenir le litigeId
            LitigeChargeback chargeback = litigeChargebackRepository.findById(chargebackId)
                    .orElseThrow(() -> new IllegalArgumentException("Chargeback non trouvé"));

            // Récupérer les justificatifs par litigeId
            List<JustificatifChargeback> justificatifs = justificatifChargebackRepository
                    .findByLitigeIdOrderByDateAjoutDesc(chargeback.getLitigeId());

            log.info("✅ [API] {} justificatifs trouvés", justificatifs.size());
            return ResponseEntity.ok(justificatifs);

        } catch (Exception e) {
            log.error("❌ Erreur récupération justificatifs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors de la récupération", "details", e.getMessage()));
        }
    }
}
