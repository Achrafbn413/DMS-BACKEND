package com.example.dms_backend.controller;

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

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public/chargebacks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class ChargebackController {

    private final ChargebackWorkflowService chargebackWorkflowService;

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
        log.info("🎯 [API] Représentation - LitigeId: {}", request.getLitigeId());
        try {
            LitigeChargebackDTO result = chargebackWorkflowService.traiterRepresentation(request);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage(), "code", "VALIDATION_ERROR"));
        } catch (Exception e) {
            log.error("❌ Erreur représentation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur lors du traitement de la représentation", "details", e.getMessage()));
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
}
