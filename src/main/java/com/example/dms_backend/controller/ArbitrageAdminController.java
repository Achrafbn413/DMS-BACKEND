package com.example.dms_backend.controller;

import com.example.dms_backend.dto.*;
import com.example.dms_backend.model.*;
import com.example.dms_backend.repository.*;
import com.example.dms_backend.service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * CONTRÔLEUR ADMIN ARBITRAGE - VERSION PRODUCTION
 * Interface d'administration pour la gestion des arbitrages
 */
@RestController
@RequestMapping("/api/admin/arbitrage")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class ArbitrageAdminController {

    private static final Logger logger = LoggerFactory.getLogger(ArbitrageAdminController.class);

    private final ArbitrageRepository arbitrageRepository;
    private final LitigeChargebackRepository litigeChargebackRepository;
    private final LitigeRepository litigeRepository;
    private final TransactionRepository transactionRepository;
    private final JustificatifChargebackRepository justificatifRepository;
    private final EchangeLitigeRepository echangeRepository;
    private final NotificationService notificationService;
    private final ChargebackWorkflowService chargebackWorkflowService;
    private final UserRepository userRepository;

    // ========================================
    // 📊 DASHBOARD ADMIN ARBITRAGE
    // ========================================

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardArbitrage() {
        logger.info("📊 [ADMIN] GET /dashboard - Dashboard arbitrage admin");

        try {
            // Statistiques globales
            List<Arbitrage> tousArbitrages = arbitrageRepository.findAll();
            long arbitragesEnAttente = arbitrageRepository.countByStatut(Arbitrage.StatutArbitrage.DEMANDE);
            long arbitragesEnCours = arbitrageRepository.countByStatut(Arbitrage.StatutArbitrage.EN_COURS);
            long arbitragesDecides = arbitrageRepository.countByStatut(Arbitrage.StatutArbitrage.DECIDE);

            // Arbitrages urgents (plus de 7 jours d'attente)
            long arbitragesUrgents = tousArbitrages.stream()
                    .filter(a -> a.getDateDemande().isBefore(LocalDateTime.now().minusDays(7)) &&
                            a.getStatut() != Arbitrage.StatutArbitrage.DECIDE)
                    .count();

            // Montant total en jeu
            BigDecimal montantTotal = tousArbitrages.stream()
                    .filter(a -> a.getCoutArbitrage() != null)
                    .map(Arbitrage::getCoutArbitrage)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // Construction du dashboard
            Map<String, Object> dashboard = new HashMap<>();
            dashboard.put("totalArbitrages", tousArbitrages.size());
            dashboard.put("arbitragesEnAttente", arbitragesEnAttente);
            dashboard.put("arbitragesEnCours", arbitragesEnCours);
            dashboard.put("arbitragesDecides", arbitragesDecides);
            dashboard.put("arbitragesUrgents", arbitragesUrgents);
            dashboard.put("montantTotalEnJeu", montantTotal);
            dashboard.put("delaiMoyenDecision", calculerDelaiMoyenDecision(tousArbitrages));
            dashboard.put("tauxTraitement", calculerTauxTraitement(tousArbitrages));
            dashboard.put("dateGeneration", LocalDateTime.now());

            // Alertes
            List<String> alertes = new ArrayList<>();
            if (arbitragesUrgents > 0) {
                alertes.add(arbitragesUrgents + " arbitrage(s) en retard");
            }
            if (arbitragesEnAttente > 10) {
                alertes.add("Nombre élevé d'arbitrages en attente");
            }
            dashboard.put("alertes", alertes);

            logger.info("✅ Dashboard admin généré : {} arbitrages total", tousArbitrages.size());
            return ResponseEntity.ok(dashboard);

        } catch (Exception e) {
            logger.error("❌ Erreur dashboard admin", e);
            return ResponseEntity.status(500).build();
        }
    }

    // ========================================
    // 📋 LISTE DES ARBITRAGES EN ATTENTE
    // ========================================

    @GetMapping("/en-attente")
    public ResponseEntity<List<Map<String, Object>>> getArbitragesEnAttente() {
        logger.info("📋 [ADMIN] GET /en-attente - Liste arbitrages en attente");

        try {
            List<Arbitrage> arbitrages = arbitrageRepository.findByStatut(Arbitrage.StatutArbitrage.DEMANDE);

            List<Map<String, Object>> result = arbitrages.stream()
                    .map(this::convertirArbitrageAvecDetails)
                    .sorted((a, b) -> {
                        // Tri par urgence puis par date
                        LocalDateTime dateA = (LocalDateTime) a.get("dateDemande");
                        LocalDateTime dateB = (LocalDateTime) b.get("dateDemande");
                        return dateA.compareTo(dateB); // Plus ancien en premier
                    })
                    .collect(Collectors.toList());

            logger.info("✅ {} arbitrages en attente récupérés", result.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            logger.error("❌ Erreur récupération arbitrages en attente", e);
            return ResponseEntity.status(500).build();
        }
    }

    // ========================================
    // 🔍 DÉTAIL COMPLET D'UN ARBITRAGE
    // ========================================

    @GetMapping("/{arbitrageId}/dossier-complet")
    public ResponseEntity<Map<String, Object>> getDossierComplet(@PathVariable Long arbitrageId) {
        logger.info("🔍 [ADMIN] GET /{}/dossier-complet", arbitrageId);

        try {
            Optional<Arbitrage> arbitrageOpt = arbitrageRepository.findById(arbitrageId);
            if (arbitrageOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Arbitrage arbitrage = arbitrageOpt.get();

            // Récupération du litige chargeback
            Optional<LitigeChargeback> litigeChargebackOpt =
                    litigeChargebackRepository.findByLitigeId(arbitrage.getLitigeId());

            // Récupération du litige principal
            Optional<Litige> litigeOpt = litigeRepository.findById(arbitrage.getLitigeId());

            // Construction du dossier complet
            Map<String, Object> dossier = new HashMap<>();
            dossier.put("arbitrage", ArbitrageDTO.fromEntity(arbitrage));

            if (litigeChargebackOpt.isPresent()) {
                dossier.put("litigeChargeback", convertirLitigeChargebackVersDTO(litigeChargebackOpt.get()));
            }

            if (litigeOpt.isPresent()) {
                Litige litige = litigeOpt.get();
                Transaction transaction = litige.getTransaction();

                // Conversion manuelle de Litige vers LitigeResponseDTO
                LitigeResponseDTO litigeDto = LitigeResponseDTO.builder()
                        .id(litige.getId())
                        .type(litige.getType())
                        .statut(litige.getStatut())
                        .description(litige.getDescription())
                        .dateCreation(litige.getDateCreation())
                        .banqueDeclaranteNom(litige.getBanqueDeclarante() != null ? litige.getBanqueDeclarante().getNom() : null)
                        .institutionDeclarantNom(litige.getBanqueDeclarante() != null ? litige.getBanqueDeclarante().getNom() : null)
                        .build();
                dossier.put("litige", litigeDto);

                // Conversion manuelle de Transaction vers TransactionDTO
                TransactionDTO transactionDto = new TransactionDTO(
                        transaction.getId(),
                        transaction.getReference(),
                        transaction.getMontant(),
                        transaction.getDateTransaction(),
                        transaction.getType() != null ? transaction.getType().name() : null,
                        transaction.getStatut() != null ? transaction.getStatut().name() : null,
                        transaction.getBanqueEmettrice() != null ? transaction.getBanqueEmettrice().getNom() : null,
                        transaction.getBanqueAcquereuse() != null ? transaction.getBanqueAcquereuse().getNom() : null,
                        litige.getBanqueDeclarante() != null ? litige.getBanqueDeclarante().getNom() : null
                );
                dossier.put("transaction", transactionDto);

                // Informations des banques
                dossier.put("banqueEmettrice", transaction.getBanqueEmettrice());
                dossier.put("banqueAcquereuse", transaction.getBanqueAcquereuse());
            }

            // Justificatifs - Conversion manuelle
            List<JustificatifChargeback> justificatifs =
                    justificatifRepository.findByLitigeId(arbitrage.getLitigeId());
            dossier.put("justificatifs", justificatifs.stream()
                    .map(this::convertirJustificatifVersDTO)
                    .collect(Collectors.toList()));

            // Échanges - Conversion manuelle
            List<EchangeLitige> echanges =
                    echangeRepository.findByLitigeId(arbitrage.getLitigeId());
            dossier.put("echanges", echanges.stream()
                    .map(this::convertirEchangeVersDTO)
                    .collect(Collectors.toList()));

            // Analyse technique
            dossier.put("analyseTechnique", genererAnalyseTechnique(arbitrage, litigeChargebackOpt.orElse(null)));
            dossier.put("recommandation", genererRecommandation(arbitrage, litigeChargebackOpt.orElse(null)));

            logger.info("✅ Dossier complet généré pour arbitrage {}", arbitrageId);
            return ResponseEntity.ok(dossier);

        } catch (Exception e) {
            logger.error("❌ Erreur récupération dossier complet {}", arbitrageId, e);
            return ResponseEntity.status(500).build();
        }
    }

    // ========================================
    // ⚖️ PRISE DE DÉCISION ADMIN
    // ========================================

    @PostMapping("/{arbitrageId}/decision")
    public ResponseEntity<Map<String, Object>> rendreDecision(
            @PathVariable Long arbitrageId,
            @Valid @RequestBody DecisionArbitrageRequest request) {

        logger.info("⚖️ [ADMIN] POST /{}/decision - Rendu de décision", arbitrageId);

        try {
            // Validation admin
            if (!estAdmin(1L)) {
                return ResponseEntity.status(403).body(Map.of(
                        "error", "Seuls les administrateurs peuvent rendre des décisions",
                        "code", "ADMIN_REQUIRED"
                ));
            }

            // Appel au service workflow
            Arbitrage.Decision decision = Arbitrage.Decision.valueOf(request.getDecision());
            Arbitrage.RepartitionFrais repartitionFrais = Arbitrage.RepartitionFrais.valueOf(request.getRepartitionFrais());

            ArbitrageDTO arbitrageDecide = chargebackWorkflowService.rendreDecisionArbitrage(
                    arbitrageId,
                    decision,
                    request.getMotifsDecision(),
                    repartitionFrais,
                    1L
            );

            logger.info("✅ Décision rendue pour arbitrage {}", arbitrageId);

            return ResponseEntity.ok(Map.of(
                    "message", "Décision d'arbitrage rendue avec succès",
                    "arbitrageId", arbitrageId,
                    "decision", request.getDecision(),
                    "statut", "DECIDE",
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalStateException e) {
            logger.warn("⚠️ État invalide pour décision: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "code", "INVALID_STATE"
            ));
        } catch (Exception e) {
            logger.error("❌ Erreur rendu décision", e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erreur interne lors du rendu de décision",
                    "code", "INTERNAL_ERROR"
            ));
        }
    }

    // ========================================
    // 📈 STATISTIQUES DÉTAILLÉES
    // ========================================

    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques(
            @RequestParam(required = false) LocalDateTime dateDebut,
            @RequestParam(required = false) LocalDateTime dateFin) {

        logger.info("📈 [ADMIN] GET /statistiques");

        try {
            if (dateDebut == null) dateDebut = LocalDateTime.now().minusMonths(3);
            if (dateFin == null) dateFin = LocalDateTime.now();

            final LocalDateTime dateDebutFinal = dateDebut;
            final LocalDateTime dateFinFinal = dateFin;

            List<Arbitrage> arbitragesPeriode = arbitrageRepository.findAll()
                    .stream()
                    .filter(a -> a.getDateDemande().isAfter(dateDebutFinal) &&
                            a.getDateDemande().isBefore(dateFinFinal))
                    .collect(Collectors.toList());

            Map<String, Object> stats = new HashMap<>();
            stats.put("periode", dateDebut.toLocalDate() + " - " + dateFin.toLocalDate());
            stats.put("totalArbitrages", arbitragesPeriode.size());
            stats.put("arbitragesDecides", arbitragesPeriode.stream()
                    .filter(a -> a.getStatut() == Arbitrage.StatutArbitrage.DECIDE)
                    .count());
            stats.put("delaiMoyenDecision", calculerDelaiMoyenDecision(arbitragesPeriode));
            stats.put("montantTotal", arbitragesPeriode.stream()
                    .filter(a -> a.getCoutArbitrage() != null)
                    .map(Arbitrage::getCoutArbitrage)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));

            // Répartition des décisions
            Map<String, Long> repartitionDecisions = new HashMap<>();
            for (Arbitrage arbitrage : arbitragesPeriode) {
                if (arbitrage.getDecision() != null) {
                    String decision = arbitrage.getDecision().name();
                    repartitionDecisions.put(decision,
                            repartitionDecisions.getOrDefault(decision, 0L) + 1);
                }
            }
            stats.put("repartitionDecisions", repartitionDecisions);
            stats.put("dateGeneration", LocalDateTime.now());

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("❌ Erreur génération statistiques", e);
            return ResponseEntity.status(500).build();
        }
    }

    // ========================================
    // 🔧 MÉTHODES DE CONVERSION DTO
    // ========================================

    private LitigeChargebackDTO convertirLitigeChargebackVersDTO(LitigeChargeback litigeChargeback) {
        LitigeChargebackDTO dto = new LitigeChargebackDTO();
        dto.setId(litigeChargeback.getId());
        dto.setLitigeId(litigeChargeback.getLitigeId());
        dto.setMotifChargeback(litigeChargeback.getMotifChargeback());
        dto.setMontantConteste(litigeChargeback.getMontantConteste());
        dto.setPhaseActuelle(litigeChargeback.getPhaseActuelle());
        // Note: Les champs suivants n'existent peut-être pas dans votre entité
        // dto.setDateDernierePhase(litigeChargeback.getDateDernierePhase());
        // dto.setDateLimiteReponse(litigeChargeback.getDateLimiteReponse());
        // dto.setStatut(litigeChargeback.getStatut());
        return dto;
    }

    private JustificatifChargebackDTO convertirJustificatifVersDTO(JustificatifChargeback justificatif) {
        JustificatifChargebackDTO dto = new JustificatifChargebackDTO();
        dto.setId(justificatif.getId());
        dto.setLitigeId(justificatif.getLitigeId());
        dto.setNomFichier(justificatif.getNomFichier());
        dto.setTypeJustificatif(justificatif.getTypeJustificatif());
        dto.setPhaseLitige(justificatif.getPhaseLitige());
        dto.setCheminFichier(justificatif.getCheminFichier());
        dto.setTailleFichier(justificatif.getTailleFichier());
        dto.setFormatFichier(justificatif.getFormatFichier());
        dto.setTransmisParUtilisateurId(justificatif.getTransmisParUtilisateurId());
        dto.setDateAjout(justificatif.getDateAjout());
        dto.setValide(justificatif.getValide());
        dto.setCommentaires(justificatif.getCommentaires());
        dto.setVisiblePourAutrePartie(justificatif.getVisiblePourAutrePartie());
        return dto;
    }

    private EchangeLitigeDTO convertirEchangeVersDTO(EchangeLitige echange) {
        EchangeLitigeDTO dto = new EchangeLitigeDTO();
        dto.setId(echange.getId());
        dto.setLitigeId(echange.getLitigeId());
        dto.setContenu(echange.getContenu());
        dto.setAuteurUtilisateurId(echange.getAuteurUtilisateurId());
        dto.setInstitutionId(echange.getInstitutionId());
        dto.setDateEchange(echange.getDateEchange());
        dto.setPhaseLitige(echange.getPhaseLitige());
        dto.setTypeEchange(echange.getTypeEchange() != null ? echange.getTypeEchange().name() : null);
        dto.setPieceJointeJustificatifId(echange.getPieceJointeJustificatifId());
        dto.setVisible(echange.getVisible());
        dto.setLuParAutrePartie(echange.getLuParAutrePartie());
        return dto;
    }

    // ========================================
    // 🔧 MÉTHODES UTILITAIRES
    // ========================================

    private Map<String, Object> convertirArbitrageAvecDetails(Arbitrage arbitrage) {
        Map<String, Object> details = new HashMap<>();

        details.put("id", arbitrage.getId());
        details.put("litigeId", arbitrage.getLitigeId());
        details.put("dateDemande", arbitrage.getDateDemande());
        details.put("statut", arbitrage.getStatut().name());
        details.put("coutArbitrage", arbitrage.getCoutArbitrage());

        // Calcul des jours d'attente
        long joursAttente = java.time.temporal.ChronoUnit.DAYS.between(
                arbitrage.getDateDemande(), LocalDateTime.now());
        details.put("joursAttente", joursAttente);

        // Urgence
        String urgence = joursAttente > 10 ? "CRITIQUE" :
                joursAttente > 7 ? "HAUTE" :
                        joursAttente > 5 ? "MOYENNE" : "NORMALE";
        details.put("urgence", urgence);

        // Informations du litige
        try {
            Optional<LitigeChargeback> litigeChargeback =
                    litigeChargebackRepository.findByLitigeId(arbitrage.getLitigeId());
            if (litigeChargeback.isPresent()) {
                details.put("motifChargeback", litigeChargeback.get().getMotifChargeback());
                details.put("montantConteste", litigeChargeback.get().getMontantConteste());
                details.put("phaseActuelle", litigeChargeback.get().getPhaseActuelle());
            }

            // Informations transaction
            Optional<Litige> litige = litigeRepository.findById(arbitrage.getLitigeId());
            if (litige.isPresent()) {
                Transaction transaction = litige.get().getTransaction();
                details.put("transactionReference", transaction.getReference());
                details.put("banqueEmettrice", transaction.getBanqueEmettrice().getNom());
                details.put("banqueAcquereuse", transaction.getBanqueAcquereuse().getNom());
            }
        } catch (Exception e) {
            logger.warn("Erreur enrichissement arbitrage {}: {}", arbitrage.getId(), e.getMessage());
        }

        return details;
    }

    private double calculerDelaiMoyenDecision(List<Arbitrage> arbitrages) {
        return arbitrages.stream()
                .filter(a -> a.getDateDecision() != null)
                .mapToLong(a -> java.time.temporal.ChronoUnit.DAYS.between(
                        a.getDateDemande(), a.getDateDecision()))
                .average()
                .orElse(0.0);
    }

    private double calculerTauxTraitement(List<Arbitrage> arbitrages) {
        if (arbitrages.isEmpty()) return 0.0;
        long decides = arbitrages.stream()
                .filter(a -> a.getStatut() == Arbitrage.StatutArbitrage.DECIDE)
                .count();
        return (decides * 100.0) / arbitrages.size();
    }

    private String genererAnalyseTechnique(Arbitrage arbitrage, LitigeChargeback litigeChargeback) {
        StringBuilder analyse = new StringBuilder();
        analyse.append("ANALYSE TECHNIQUE:\n\n");

        // Délai
        long joursAttente = java.time.temporal.ChronoUnit.DAYS.between(
                arbitrage.getDateDemande(), LocalDateTime.now());
        analyse.append("• Délai depuis demande: ").append(joursAttente).append(" jours\n");

        // Coût
        if (arbitrage.getCoutArbitrage() != null) {
            analyse.append("• Coût arbitrage: ").append(arbitrage.getCoutArbitrage()).append(" MAD\n");
        }

        // Phase
        if (litigeChargeback != null) {
            analyse.append("• Phase actuelle: ").append(litigeChargeback.getPhaseActuelle()).append("\n");
            if (litigeChargeback.getMontantConteste() != null) {
                analyse.append("• Montant contesté: ").append(litigeChargeback.getMontantConteste()).append(" MAD\n");
            }
        }

        return analyse.toString();
    }

    private String genererRecommandation(Arbitrage arbitrage, LitigeChargeback litigeChargeback) {
        StringBuilder recommandation = new StringBuilder();
        recommandation.append("RECOMMANDATION:\n\n");

        long joursAttente = java.time.temporal.ChronoUnit.DAYS.between(
                arbitrage.getDateDemande(), LocalDateTime.now());

        if (joursAttente > 10) {
            recommandation.append("🔴 PRIORITÉ CRITIQUE - Traitement immédiat\n");
        } else if (joursAttente > 7) {
            recommandation.append("🟠 PRIORITÉ HAUTE - Traitement dans les 24h\n");
        } else {
            recommandation.append("🟢 PRIORITÉ NORMALE - Traitement standard\n");
        }

        if (litigeChargeback != null && litigeChargeback.getMontantConteste() != null) {
            if (litigeChargeback.getMontantConteste().compareTo(BigDecimal.valueOf(1000)) < 0) {
                recommandation.append("💡 Montant faible - Médiation rapide possible\n");
            }
        }

        return recommandation.toString();
    }

    private boolean estAdmin(Long utilisateurId) {
        // TODO: Implémenter vérification rôle admin
        return true; // Simplifié pour l'instant
    }
}