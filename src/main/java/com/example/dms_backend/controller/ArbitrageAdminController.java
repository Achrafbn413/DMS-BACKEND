/*package com.example.dms_backend.controller;
import com.example.dms_backend.dto.*;
import com.example.dms_backend.model.*;
import com.example.dms_backend.repository.*;
import com.example.dms_backend.service.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * Contrôleur REST spécialisé pour l'interface admin arbitrage
 * L'admin voit tous les arbitrages, consulte les dossiers complets et prend les décisions
 *
 * SÉCURITÉ : Accessible uniquement aux ADMIN
 *
 * @author Système DMS Bancaire Maroc
 * @version 1.0 Production

@RestController
@RequestMapping("/api/admin/arbitrage")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')") // Sécurité : Admin seulement
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

    // ========================================
    // 📊 DASHBOARD ADMIN ARBITRAGE
    // ========================================

    /**
     * Dashboard principal admin - Vue d'ensemble des arbitrages

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardArbitrageAdminDTO> getDashboardArbitrage() {
        logger.info("📊 [ADMIN] GET /dashboard - Dashboard arbitrage admin");

        try {
            // Statistiques globales
            List<Arbitrage> tousArbitrages = arbitrageRepository.findAll();
            long arbitragesEnAttente = arbitrageRepository.countByStatut(Arbitrage.StatutArbitrage.DEMANDE);
            long arbitragesEnCours = arbitrageRepository.countByStatut(Arbitrage.StatutArbitrage.EN_COURS);
            long arbitragesUrgents = compterArbitragesUrgents();

            // Construction du dashboard
            DashboardArbitrageAdminDTO dashboard = DashboardArbitrageAdminDTO.builder()
                    .totalArbitrages(tousArbitrages.size())
                    .arbitragesEnAttente(Math.toIntExact(arbitragesEnAttente))
                    .arbitragesEnCours(Math.toIntExact(arbitragesEnCours))
                    .arbitragesUrgents(Math.toIntExact(arbitragesUrgents))
                    .arbitragesDecides(Math.toIntExact(arbitrageRepository.countByStatut(Arbitrage.StatutArbitrage.DECIDE)))
                    .delaiMoyenDecision(calculerDelaiMoyenDecision(tousArbitrages))
                    .montantTotalEnJeu(calculerMontantTotalEnJeu(tousArbitrages))
                    .repartitionParPriorite(calculerRepartitionParPriorite())
                    .evolutionMensuelle(calculerEvolutionMensuelle())
                    .alertes(genererAlertes())
                    .dateGeneration(LocalDateTime.now())
                    .build();

            logger.info("✅ Dashboard admin généré : {} arbitrages total, {} en attente",
                    dashboard.getTotalArbitrages(), dashboard.getArbitragesEnAttente());

            return ResponseEntity.ok(dashboard);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la génération du dashboard admin", e);
            return ResponseEntity.status(500).build();
        }
    }

    // ========================================
    // 📋 LISTE DES ARBITRAGES EN ATTENTE
    // ========================================

    /**
     * Liste des arbitrages en attente de décision admin

    @GetMapping("/en-attente")
    public ResponseEntity<List<ArbitrageAdminDTO>> getArbitragesEnAttente() {
        logger.info("📋 [ADMIN] GET /en-attente - Liste arbitrages en attente");

        try {
            List<Arbitrage> arbitrages = arbitrageRepository.findByStatut(Arbitrage.StatutArbitrage.DEMANDE);

            List<ArbitrageAdminDTO> dtos = arbitrages.stream()
                    .map(this::convertirEnArbitrageAdminDTO)
                    .sorted((a, b) -> {
                        // Tri par urgence puis par date
                        int urgenceComp = comparerUrgence(a.getUrgence(), b.getUrgence());
                        if (urgenceComp != 0) return urgenceComp;
                        return b.getDateDemande().compareTo(a.getDateDemande());
                    })
                    .collect(Collectors.toList());

            logger.info("✅ {} arbitrages en attente récupérés pour admin", dtos.size());

            return ResponseEntity.ok(dtos);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des arbitrages en attente", e);
            return ResponseEntity.status(500).build();
        }
    }

    // ========================================
    // 🔔 NOTIFICATIONS ADMIN
    // ========================================

    /**
     * Notifications spécifiques admin pour les arbitrages

    @GetMapping("/notifications")
    public ResponseEntity<List<NotificationArbitrageAdminDTO>> getNotificationsArbitrage() {
        logger.info("🔔 [ADMIN] GET /notifications - Notifications arbitrage admin");

        try {
            List<NotificationArbitrageAdminDTO> notifications = new ArrayList<>();

            // Arbitrages en attente depuis plus de 7 jours
            List<Arbitrage> arbitragesEnRetard = arbitrageRepository.findByStatut(Arbitrage.StatutArbitrage.DEMANDE)
                    .stream()
                    .filter(a -> a.getDateDemande().isBefore(LocalDateTime.now().minusDays(7)))
                    .collect(Collectors.toList());

            for (Arbitrage arbitrage : arbitragesEnRetard) {
                notifications.add(NotificationArbitrageAdminDTO.builder()
                        .id(arbitrage.getId())
                        .type("ARBITRAGE_EN_RETARD")
                        .titre("Arbitrage en retard")
                        .message(String.format("L'arbitrage #%d est en attente depuis %d jours",
                                arbitrage.getId(), calculerJoursAttente(arbitrage)))
                        .urgence("HAUTE")
                        .arbitrageId(arbitrage.getId())
                        .dateCreation(LocalDateTime.now())
                        .actionRequise("PRENDRE_DECISION")
                        .icone("⚠️")
                        .couleur("orange")
                        .build());
            }

            // Nouveaux arbitrages (dernières 24h)
            List<Arbitrage> nouveauxArbitrages = arbitrageRepository.findByStatut(Arbitrage.StatutArbitrage.DEMANDE)
                    .stream()
                    .filter(a -> a.getDateDemande().isAfter(LocalDateTime.now().minusDays(1)))
                    .collect(Collectors.toList());

            for (Arbitrage arbitrage : nouveauxArbitrages) {
                notifications.add(NotificationArbitrageAdminDTO.builder()
                        .id(arbitrage.getId())
                        .type("NOUVEL_ARBITRAGE")
                        .titre("Nouvel arbitrage")
                        .message(String.format("Nouvel arbitrage #%d en attente de traitement", arbitrage.getId()))
                        .urgence("NORMALE")
                        .arbitrageId(arbitrage.getId())
                        .dateCreation(arbitrage.getDateDemande())
                        .actionRequise("EXAMINER")
                        .icone("🆕")
                        .couleur("blue")
                        .build());
            }

            // Tri par urgence puis date
            notifications.sort((a, b) -> {
                int urgenceComp = comparerUrgence(a.getUrgence(), b.getUrgence());
                if (urgenceComp != 0) return urgenceComp;
                return b.getDateCreation().compareTo(a.getDateCreation());
            });

            logger.info("✅ {} notifications arbitrage générées pour admin", notifications.size());

            return ResponseEntity.ok(notifications);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération des notifications admin", e);
            return ResponseEntity.status(500).build();
        }
    }

    // ========================================
    // 🔧 MÉTHODES UTILITAIRES (PARTIE 1)
    // ========================================

    private long compterArbitragesUrgents() {
        return arbitrageRepository.findByStatut(Arbitrage.StatutArbitrage.DEMANDE)
                .stream()
                .filter(a -> {
                    // Urgent si en attente depuis plus de 5 jours ou montant élevé
                    boolean enRetard = a.getDateDemande().isBefore(LocalDateTime.now().minusDays(5));
                    boolean montantEleve = a.getCoutArbitrage() != null &&
                            a.getCoutArbitrage().compareTo(BigDecimal.valueOf(5000)) > 0;
                    return enRetard || montantEleve;
                })
                .count();
    }

    private double calculerDelaiMoyenDecision(List<Arbitrage> arbitrages) {
        return arbitrages.stream()
                .filter(a -> a.getDateDecision() != null)
                .mapToLong(a -> java.time.temporal.ChronoUnit.DAYS.between(a.getDateDemande(), a.getDateDecision()))
                .average()
                .orElse(0.0);
    }

    private BigDecimal calculerMontantTotalEnJeu(List<Arbitrage> arbitrages) {
        return arbitrages.stream()
                .filter(a -> a.getCoutArbitrage() != null)
                .map(Arbitrage::getCoutArbitrage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, Long> calculerRepartitionParPriorite() {
        Map<String, Long> repartition = new HashMap<>();

        // Compter par priorité simulée basée sur les délais et montants
        long critique = arbitrageRepository.findByStatut(Arbitrage.StatutArbitrage.DEMANDE)
                .stream()
                .filter(a -> a.getDateDemande().isBefore(LocalDateTime.now().minusDays(10)))
                .count();

        repartition.put("CRITIQUE", critique);
        repartition.put("HAUTE", Math.max(0, compterArbitragesUrgents() - critique));
        repartition.put("NORMALE", arbitrageRepository.countByStatut(Arbitrage.StatutArbitrage.DEMANDE) - compterArbitragesUrgents());

        return repartition;
    }

    private List<Map<String, Object>> calculerEvolutionMensuelle() {
        // Evolution simplifiée des 3 derniers mois
        List<Map<String, Object>> evolution = new ArrayList<>();

        for (int i = 2; i >= 0; i--) {
            LocalDateTime debutMois = LocalDateTime.now().minusMonths(i).withDayOfMonth(1);
            LocalDateTime finMois = debutMois.plusMonths(1).minusDays(1);

            long count = arbitrageRepository.findAll()
                    .stream()
                    .filter(a -> a.getDateDemande().isAfter(debutMois) && a.getDateDemande().isBefore(finMois))
                    .count();

            evolution.add(Map.of(
                    "mois", debutMois.getMonth().name(),
                    "arbitrages", count
            ));
        }

        return evolution;
    }

    private List<String> genererAlertes() {
        List<String> alertes = new ArrayList<>();

        long enRetard = arbitrageRepository.findByStatut(Arbitrage.StatutArbitrage.DEMANDE)
                .stream()
                .filter(a -> a.getDateDemande().isBefore(LocalDateTime.now().minusDays(7)))
                .count();

        if (enRetard > 0) {
            alertes.add(String.format("%d arbitrage(s) en retard de traitement", enRetard));
        }

        if (compterArbitragesUrgents() > 5) {
            alertes.add("Nombre élevé d'arbitrages urgents");
        }

        return alertes;
    }

    private int comparerUrgence(String urgence1, String urgence2) {
        Map<String, Integer> ordreUrgence = Map.of(
                "CRITIQUE", 4,
                "HAUTE", 3,
                "MOYENNE", 2,
                "NORMALE", 1,
                "BASSE", 0
        );

        int ordre1 = ordreUrgence.getOrDefault(urgence1, 1);
        int ordre2 = ordreUrgence.getOrDefault(urgence2, 1);

        return Integer.compare(ordre2, ordre1); // Ordre décroissant
    }

    private long calculerJoursAttente(Arbitrage arbitrage) {
        return java.time.temporal.ChronoUnit.DAYS.between(
                arbitrage.getDateDemande(), LocalDateTime.now());
    }
    // ========================================
    // 📄 DOSSIER COMPLET D'ARBITRAGE
    // ========================================

    /**
     * Récupère le dossier COMPLET d'un arbitrage pour l'admin
     * Toutes les informations : transaction, workflow, justificatifs, échanges, timeline

    @GetMapping("/{arbitrageId}/dossier-complet")
    public ResponseEntity<DossierCompletArbitrageDTO> getDossierComplet(@PathVariable Long arbitrageId) {
        logger.info("📄 [ADMIN] GET /{}/dossier-complet - Dossier complet arbitrage", arbitrageId);

        try {
            // Récupération de l'arbitrage
            Optional<Arbitrage> arbitrageOpt = arbitrageRepository.findById(arbitrageId);
            if (arbitrageOpt.isEmpty()) {
                logger.warn("⚠️ Arbitrage {} non trouvé", arbitrageId);
                return ResponseEntity.notFound().build();
            }

            Arbitrage arbitrage = arbitrageOpt.get();

            // Récupération du litige chargeback
            Optional<LitigeChargeback> litigeChargebackOpt =
                    litigeChargebackRepository.findByLitigeIdWithDetails(arbitrage.getLitigeId());

            if (litigeChargebackOpt.isEmpty()) {
                logger.warn("⚠️ Litige chargeback {} non trouvé", arbitrage.getLitigeId());
                return ResponseEntity.badRequest().build();
            }

            LitigeChargeback litigeChargeback = litigeChargebackOpt.get();

            // Récupération du litige principal
            Optional<Litige> litigeOpt = litigeRepository.findById(arbitrage.getLitigeId());
            if (litigeOpt.isEmpty()) {
                logger.warn("⚠️ Litige {} non trouvé", arbitrage.getLitigeId());
                return ResponseEntity.badRequest().build();
            }

            Litige litige = litigeOpt.get();
            Transaction transaction = litige.getTransaction();

            // Construction du dossier complet
            DossierCompletArbitrageDTO dossier = construireDossierComplet(
                    arbitrage, litigeChargeback, litige, transaction
            );

            logger.info("✅ Dossier complet généré pour arbitrage {} - {} éléments",
                    arbitrageId, dossier.getNombreElementsDossier());

            return ResponseEntity.ok(dossier);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la récupération du dossier complet pour arbitrage {}", arbitrageId, e);
            return ResponseEntity.status(500).build();
        }
    }

    // ========================================
    // ⚖️ PRISE DE DÉCISION ADMIN
    // ========================================

    /**
     * L'admin rend sa décision d'arbitrage
     * Cette décision sera automatiquement notifiée aux 2 banques

    @PostMapping("/{arbitrageId}/decision")
    public ResponseEntity<?> rendreDecision(@PathVariable Long arbitrageId,
                                            @Valid @RequestBody DecisionArbitrageAdminRequest request) {
        logger.info("⚖️ [ADMIN] POST /{}/decision - Rendu de décision par admin", arbitrageId);
        logger.info("Décision: {}, Arbitre: {}", request.getDecision(), request.getArbitreAdminId());

        try {
            // Validation admin
            if (!estAdmin(request.getArbitreAdminId())) {
                logger.warn("⚠️ Utilisateur {} n'est pas admin", request.getArbitreAdminId());
                return ResponseEntity.status(403).body(Map.of(
                        "error", "Seuls les administrateurs peuvent rendre des décisions d'arbitrage",
                        "code", "ADMIN_REQUIRED"
                ));
            }

            // Appel au service workflow pour rendre la décision
            ArbitrageDTO arbitrageDecide = chargebackWorkflowService.rendreDecisionArbitrage(
                    arbitrageId,
                    request.getDecision(),
                    request.getMotifs(),
                    request.getRepartitionFrais(),
                    request.getArbitreAdminId()
            );

            // Notifications automatiques aux banques (gérées par le service)
            logger.info("🔔 Notifications automatiques envoyées aux banques concernées");

            // Création de l'historique admin
            creerHistoriqueDecisionAdmin(arbitrageId, request);

            logger.info("✅ Décision d'arbitrage rendue par admin pour arbitrage {}", arbitrageId);

            return ResponseEntity.ok(Map.of(
                    "message", "Décision d'arbitrage rendue avec succès",
                    "arbitrageId", arbitrageId,
                    "decision", request.getDecision(),
                    "statut", "DECIDE",
                    "notificationsBanques", "Envoyées automatiquement",
                    "timestamp", LocalDateTime.now()
            ));

        } catch (IllegalStateException e) {
            logger.warn("⚠️ État invalide pour décision arbitrage: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "code", "INVALID_STATE"
            ));
        } catch (Exception e) {
            logger.error("❌ Erreur lors du rendu de décision d'arbitrage", e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Erreur interne lors du rendu de décision",
                    "code", "INTERNAL_ERROR"
            ));
        }
    }

    // ========================================
    // 📈 STATISTIQUES ET RAPPORTS
    // ========================================

    /**
     * Statistiques détaillées pour l'admin

    @GetMapping("/statistiques")
    public ResponseEntity<StatistiquesArbitrageAdminDTO> getStatistiquesArbitrage(
            @RequestParam(required = false) LocalDateTime dateDebut,
            @RequestParam(required = false) LocalDateTime dateFin) {

        logger.info("📈 [ADMIN] GET /statistiques - Statistiques arbitrage admin");

        try {
            // Période par défaut : 3 derniers mois
            if (dateDebut == null) dateDebut = LocalDateTime.now().minusMonths(3);
            if (dateFin == null) dateFin = LocalDateTime.now();

            StatistiquesArbitrageAdminDTO stats = genererStatistiquesAdmin(dateDebut, dateFin);

            logger.info("✅ Statistiques générées pour période {} - {}", dateDebut, dateFin);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la génération des statistiques admin", e);
            return ResponseEntity.status(500).build();
        }
    }

    // ========================================
    // 🔧 MÉTHODES UTILITAIRES (PARTIE 2)
    // ========================================

    /**
     * Convertit un Arbitrage en ArbitrageAdminDTO avec données enrichies

    private ArbitrageAdminDTO convertirEnArbitrageAdminDTO(Arbitrage arbitrage) {
        // Récupération des informations complémentaires
        Optional<LitigeChargeback> litigeChargeback =
                litigeChargebackRepository.findByLitigeId(arbitrage.getLitigeId());

        Optional<Litige> litige = litigeRepository.findById(arbitrage.getLitigeId());

        String urgence = determinerUrgence(arbitrage, litigeChargeback.orElse(null));
        int complexite = calculerComplexite(arbitrage, litigeChargeback.orElse(null));

        return ArbitrageAdminDTO.builder()
                .id(arbitrage.getId())
                .litigeId(arbitrage.getLitigeId())
                .dateDemande(arbitrage.getDateDemande())
                .banqueDemandeuse(getNomBanqueDemandeuse(arbitrage))
                .montantEnJeu(arbitrage.getCoutArbitrage())
                .urgence(urgence)
                .complexite(complexite)
                .motifArbitrage(litigeChargeback.map(LitigeChargeback::getMotifChargeback).orElse("Non spécifié"))
                .phaseActuelle(litigeChargeback.map(LitigeChargeback::getPhaseActuelle).orElse("ARBITRAGE"))
                .joursAttente(Math.toIntExact(calculerJoursAttente(arbitrage)))
                .nombreJustificatifs(compterJustificatifs(arbitrage.getLitigeId()))
                .transactionReference(litige.map(l -> l.getTransaction().getReference()).orElse("N/A"))
                .resumeAuto(genererResumeAutomatique(arbitrage, litigeChargeback.orElse(null)))
                .actionRequise("EXAMINER_ET_DECIDER")
                .build();
    }

    /**
     * Construit le dossier complet pour l'admin

    private DossierCompletArbitrageDTO construireDossierComplet(Arbitrage arbitrage,
                                                                LitigeChargeback litigeChargeback,
                                                                Litige litige,
                                                                Transaction transaction) {

        // Récupération de tous les justificatifs
        List<JustificatifChargeback> justificatifs =
                justificatifRepository.findByLitigeIdOrderByDateAjout(arbitrage.getLitigeId());

        // Récupération de tous les échanges
        List<EchangeLitige> echanges =
                echangeRepository.findByLitigeIdOrderByDateEchange(arbitrage.getLitigeId());

        // Construction de la timeline complète
        List<EtapeTimelineDTO> timeline = construireTimelineComplete(
                arbitrage, litigeChargeback, justificatifs, echanges
        );

        return DossierCompletArbitrageDTO.builder()
                .arbitrage(ArbitrageDTO.fromEntity(arbitrage))
                .litigeChargeback(LitigeChargebackDTO.fromEntity(litigeChargeback))
                .litige(LitigeResponseDTO.fromEntity(litige))
                .transaction(TransactionDTO.fromEntity(transaction))
                .justificatifs(justificatifs.stream()
                        .map(JustificatifChargebackDTO::fromEntity)
                        .collect(Collectors.toList()))
                .echanges(echanges.stream()
                        .map(EchangeLitigeDTO::fromEntity)
                        .collect(Collectors.toList()))
                .timeline(timeline)
                .analyseTechnique(genererAnalyseTechnique(arbitrage, litigeChargeback))
                .recommandationSysteme(genererRecommandationSysteme(arbitrage, litigeChargeback))
                .scoreComplexite(calculerComplexite(arbitrage, litigeChargeback))
                .indicateursDecision(genererIndicateursDecision(arbitrage, litigeChargeback))
                .resumeExecutif(genererResumeExecutif(arbitrage, litigeChargeback, transaction))
                .nombreElementsDossier(justificatifs.size() + echanges.size() + timeline.size())
                .dateGenerationDossier(LocalDateTime.now())
                .build();
    }

    private boolean estAdmin(Long utilisateurId) {
        // TODO: Implémenter la vérification du rôle admin
        // Vérifier si l'utilisateur a le rôle ADMIN
        return true; // Simplifié pour l'instant
    }

    private void creerHistoriqueDecisionAdmin(Long arbitrageId, DecisionArbitrageAdminRequest request) {
        // TODO: Créer un historique de la décision admin
        logger.info("📝 Création historique décision admin pour arbitrage {}", arbitrageId);
    }

    private StatistiquesArbitrageAdminDTO genererStatistiquesAdmin(LocalDateTime dateDebut, LocalDateTime dateFin) {
        // Récupération des arbitrages dans la période
        List<Arbitrage> arbitragesPeriode = arbitrageRepository.findAll()
                .stream()
                .filter(a -> a.getDateDemande().isAfter(dateDebut) && a.getDateDemande().isBefore(dateFin))
                .collect(Collectors.toList());

        return StatistiquesArbitrageAdminDTO.builder()
                .periode(String.format("%s - %s", dateDebut.toLocalDate(), dateFin.toLocalDate()))
                .totalArbitrages(arbitragesPeriode.size())
                .arbitragesDecides(Math.toIntExact(arbitragesPeriode.stream()
                        .filter(a -> a.getStatut() == Arbitrage.StatutArbitrage.DECIDE)
                        .count()))
                .arbitragesEnCours(Math.toIntExact(arbitragesPeriode.stream()
                        .filter(a -> a.getStatut() == Arbitrage.StatutArbitrage.EN_COURS)
                        .count()))
                .delaiMoyenDecision(calculerDelaiMoyenDecision(arbitragesPeriode))
                .montantTotalArbitrages(calculerMontantTotalEnJeu(arbitragesPeriode))
                .tauxDecisionFavorableEmetteur(calculerTauxDecisionFavorable(arbitragesPeriode, "FAVORABLE_EMETTEUR"))
                .tauxDecisionFavorableAcquereur(calculerTauxDecisionFavorable(arbitragesPeriode, "FAVORABLE_ACQUEREUR"))
                .repartitionParDecision(calculerRepartitionParDecision(arbitragesPeriode))
                .evolutionMensuelle(calculerEvolutionMensuelle())
                .alertesPerformance(genererAlertesPerformance())
                .dateGeneration(LocalDateTime.now())
                .build();
    }
    // ========================================
    // 🔧 MÉTHODES UTILITAIRES (PARTIE 3 - FINALE)
    // ========================================

    private String determinerUrgence(Arbitrage arbitrage, LitigeChargeback litigeChargeback) {
        if (arbitrage.getDateDemande().isBefore(LocalDateTime.now().minusDays(10))) {
            return "CRITIQUE";
        }
        if (arbitrage.getDateDemande().isBefore(LocalDateTime.now().minusDays(7))) {
            return "HAUTE";
        }
        if (arbitrage.getCoutArbitrage() != null &&
                arbitrage.getCoutArbitrage().compareTo(java.math.BigDecimal.valueOf(10000)) > 0) {
            return "HAUTE";
        }
        return "NORMALE";
    }

    private int calculerComplexite(Arbitrage arbitrage, LitigeChargeback litigeChargeback) {
        int score = 50; // Base

        if (litigeChargeback != null) {
            if ("PRE_ARBITRAGE".equals(litigeChargeback.getPhaseActuelle())) score += 20;
            if (litigeChargeback.getMontantConteste() != null &&
                    litigeChargeback.getMontantConteste().compareTo(java.math.BigDecimal.valueOf(5000)) > 0) {
                score += 15;
            }
        }

        // Nombre de justificatifs
        int nbJustificatifs = compterJustificatifs(arbitrage.getLitigeId());
        if (nbJustificatifs > 10) score += 15;
        if (nbJustificatifs > 20) score += 10;

        return Math.min(score, 100);
    }

    private String getNomBanqueDemandeuse(Arbitrage arbitrage) {
        // TODO: Récupérer le vrai nom de la banque depuis la base
        return "Banque " + arbitrage.getDemandeParInstitutionId();
    }

    private int compterJustificatifs(Long litigeId) {
        return justificatifRepository.findByLitigeId(litigeId).size();
    }

    private String genererResumeAutomatique(Arbitrage arbitrage, LitigeChargeback litigeChargeback) {
        StringBuilder resume = new StringBuilder();
        resume.append("Arbitrage demandé le ")
                .append(arbitrage.getDateDemande().toLocalDate());

        if (litigeChargeback != null && litigeChargeback.getMontantConteste() != null) {
            resume.append(" pour un montant de ")
                    .append(litigeChargeback.getMontantConteste())
                    .append(" MAD");
        }

        if (litigeChargeback != null && litigeChargeback.getMotifChargeback() != null) {
            resume.append(" (Motif: ")
                    .append(litigeChargeback.getMotifChargeback())
                    .append(")");
        }

        return resume.toString();
    }

    private List<EtapeTimelineDTO> construireTimelineComplete(Arbitrage arbitrage,
                                                              LitigeChargeback litigeChargeback,
                                                              List<JustificatifChargeback> justificatifs,
                                                              List<EchangeLitige> echanges) {
        List<EtapeTimelineDTO> timeline = new ArrayList<>();

        // Étape 1: Initiation
        if (litigeChargeback != null) {
            timeline.add(EtapeTimelineDTO.builder()
                    .etape("INITIATION")
                    .date(litigeChargeback.getDateCreation())
                    .statut("COMPLETE")
                    .description("Chargeback initié")
                    .icone("🚀")
                    .couleur("blue")
                    .build());
        }

        // Étape 2: Représentation (si applicable)
        if (litigeChargeback != null && !"CHARGEBACK_INITIAL".equals(litigeChargeback.getPhaseActuelle())) {
            timeline.add(EtapeTimelineDTO.builder()
                    .etape("REPRESENTATION")
                    .date(litigeChargeback.getDateDerniereAction())
                    .statut("COMPLETE")
                    .description("Représentation traitée")
                    .icone("🔄")
                    .couleur("orange")
                    .build());
        }

        // Étape 3: Pre-arbitrage (si applicable)
        if (litigeChargeback != null && "PRE_ARBITRAGE".equals(litigeChargeback.getPhaseActuelle())) {
            timeline.add(EtapeTimelineDTO.builder()
                    .etape("PRE_ARBITRAGE")
                    .date(litigeChargeback.getDateDerniereAction())
                    .statut("COMPLETE")
                    .description("Second presentment")
                    .icone("⚡")
                    .couleur("red")
                    .build());
        }

        // Étape 4: Arbitrage demandé
        timeline.add(EtapeTimelineDTO.builder()
                .etape("DEMANDE_ARBITRAGE")
                .date(arbitrage.getDateDemande())
                .statut("COMPLETE")
                .description("Arbitrage demandé")
                .icone("⚖️")
                .couleur("purple")
                .build());

        // Étape 5: En attente admin
        String statutAdmin = arbitrage.getStatut() == Arbitrage.StatutArbitrage.DECIDE ? "COMPLETE" : "EN_COURS";
        timeline.add(EtapeTimelineDTO.builder()
                .etape("ATTENTE_ADMIN")
                .date(LocalDateTime.now())
                .statut(statutAdmin)
                .description(statutAdmin.equals("COMPLETE") ? "Décision rendue" : "En attente de décision admin")
                .icone(statutAdmin.equals("COMPLETE") ? "✅" : "⏳")
                .couleur(statutAdmin.equals("COMPLETE") ? "green" : "yellow")
                .build());

        return timeline;
    }

    private String genererAnalyseTechnique(Arbitrage arbitrage, LitigeChargeback litigeChargeback) {
        StringBuilder analyse = new StringBuilder();

        analyse.append("ANALYSE TECHNIQUE DU DOSSIER:\n\n");

        // Analyse temporelle
        long joursAttente = calculerJoursAttente(arbitrage);
        analyse.append("• Délai depuis demande: ").append(joursAttente).append(" jours\n");

        // Analyse financière
        if (arbitrage.getCoutArbitrage() != null) {
            analyse.append("• Coût arbitrage: ").append(arbitrage.getCoutArbitrage()).append(" MAD\n");
        }

        // Analyse complexité
        if (litigeChargeback != null) {
            analyse.append("• Phase actuelle: ").append(litigeChargeback.getPhaseActuelle()).append("\n");
            if (litigeChargeback.getMontantConteste() != null) {
                analyse.append("• Montant contesté: ").append(litigeChargeback.getMontantConteste()).append(" MAD\n");
            }
        }

        // Nombre d'éléments du dossier
        int nbJustificatifs = compterJustificatifs(arbitrage.getLitigeId());
        analyse.append("• Justificatifs fournis: ").append(nbJustificatifs).append("\n");

        return analyse.toString();
    }

    private String genererRecommandationSysteme(Arbitrage arbitrage, LitigeChargeback litigeChargeback) {
        StringBuilder recommandation = new StringBuilder();

        recommandation.append("RECOMMANDATION SYSTÈME:\n\n");

        // Analyse de l'urgence
        String urgence = determinerUrgence(arbitrage, litigeChargeback);
        if ("CRITIQUE".equals(urgence)) {
            recommandation.append("⚠️ PRIORITÉ CRITIQUE - Traitement immédiat recommandé\n");
        } else if ("HAUTE".equals(urgence)) {
            recommandation.append("🔴 PRIORITÉ HAUTE - Traitement dans les 24h\n");
        }

        // Analyse de la complexité
        int complexite = calculerComplexite(arbitrage, litigeChargeback);
        if (complexite > 80) {
            recommandation.append("🔧 Dossier complexe - Expertise approfondie requise\n");
        } else if (complexite > 60) {
            recommandation.append("📋 Dossier standard - Analyse normale\n");
        } else {
            recommandation.append("✅ Dossier simple - Traitement rapide possible\n");
        }

        // Recommandation basée sur les données
        if (litigeChargeback != null && litigeChargeback.getMontantConteste() != null) {
            if (litigeChargeback.getMontantConteste().compareTo(java.math.BigDecimal.valueOf(1000)) < 0) {
                recommandation.append("💡 Montant faible - Envisager médiation rapide\n");
            }
        }

        return recommandation.toString();
    }

    private List<String> genererIndicateursDecision(Arbitrage arbitrage, LitigeChargeback litigeChargeback) {
        List<String> indicateurs = new ArrayList<>();

        // Indicateurs basés sur les données disponibles
        if (litigeChargeback != null) {
            if (litigeChargeback.getMontantConteste() != null) {
                indicateurs.add("Montant contesté: " + litigeChargeback.getMontantConteste() + " MAD");
            }

            if (litigeChargeback.getMotifChargeback() != null) {
                indicateurs.add("Motif: " + litigeChargeback.getMotifChargeback());
            }

            indicateurs.add("Phase atteinte: " + litigeChargeback.getPhaseActuelle());
        }

        // Indicateurs temporels
        long joursAttente = calculerJoursAttente(arbitrage);
        indicateurs.add("Délai d'attente: " + joursAttente + " jours");

        // Indicateurs de complexité
        int nbJustificatifs = compterJustificatifs(arbitrage.getLitigeId());
        indicateurs.add("Justificatifs fournis: " + nbJustificatifs);

        // Indicateur d'urgence
        String urgence = determinerUrgence(arbitrage, litigeChargeback);
        indicateurs.add("Niveau d'urgence: " + urgence);

        return indicateurs;
    }

    private String genererResumeExecutif(Arbitrage arbitrage, LitigeChargeback litigeChargeback, Transaction transaction) {
        StringBuilder resume = new StringBuilder();

        resume.append("RÉSUMÉ EXÉCUTIF:\n\n");

        resume.append("Arbitrage #").append(arbitrage.getId())
                .append(" concernant la transaction ").append(transaction.getReference());

        if (litigeChargeback != null && litigeChargeback.getMontantConteste() != null) {
            resume.append(" d'un montant de ").append(litigeChargeback.getMontantConteste()).append(" MAD");
        }

        resume.append(".\n\nDemande d'arbitrage formulée par la banque ")
                .append(arbitrage.getDemandeParInstitutionId())
                .append(" le ").append(arbitrage.getDateDemande().toLocalDate()).append(".");

        if (litigeChargeback != null) {
            resume.append("\n\nÉtat du workflow: ").append(litigeChargeback.getPhaseActuelle());

            if (litigeChargeback.getMotifChargeback() != null) {
                resume.append("\nMotif du chargeback: ").append(litigeChargeback.getMotifChargeback());
            }
        }

        long joursAttente = calculerJoursAttente(arbitrage);
        resume.append("\n\nDélai d'attente actuel: ").append(joursAttente).append(" jours");

        return resume.toString();
    }

    private double calculerTauxDecisionFavorable(List<Arbitrage> arbitrages, String typeDecision) {
        long totalDecides = arbitrages.stream()
                .filter(a -> a.getDecision() != null)
                .count();

        if (totalDecides == 0) return 0.0;

        long favorables = arbitrages.stream()
                .filter(a -> typeDecision.equals(a.getDecision() != null ? a.getDecision().name() : null))
                .count();

        return (favorables * 100.0) / totalDecides;
    }

    private Map<String, Long> calculerRepartitionParDecision(List<Arbitrage> arbitrages) {
        Map<String, Long> repartition = new HashMap<>();

        for (Arbitrage arbitrage : arbitrages) {
            if (arbitrage.getDecision() != null) {
                String decision = arbitrage.getDecision().name();
                repartition.put(decision, repartition.getOrDefault(decision, 0L) + 1);
            }
        }

        return repartition;
    }

    private List<String> genererAlertesPerformance() {
        List<String> alertes = new ArrayList<>();

        long totalEnAttente = arbitrageRepository.countByStatut(Arbitrage.StatutArbitrage.DEMANDE);
        if (totalEnAttente > 10) {
            alertes.add("Nombre élevé d'arbitrages en attente: " + totalEnAttente);
        }

        // Calcul du délai moyen
        double delaiMoyen = calculerDelaiMoyenDecision(arbitrageRepository.findAll());
        if (delaiMoyen > 15) {
            alertes.add("Délai moyen de décision élevé: " + String.format("%.1f jours", delaiMoyen));
        }

        return alertes;
    }

    // Fermeture de la classe
}
    */
