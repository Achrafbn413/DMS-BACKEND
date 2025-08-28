package com.example.dms_backend.dto;

import com.example.dms_backend.model.Arbitrage;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DTO pour l'entité Arbitrage
 * Représente les arbitrages et décisions finales d'un litige chargeback
 * Architecture de niveau ENTERPRISE avec workflow complet
 *
 * @author Système DMS Bancaire
 * @version 1.0 - Production Ready
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArbitrageDTO {

    @JsonProperty("id")
    private Long id;

    @NotNull(message = "L'ID du litige est obligatoire")
    @JsonProperty("litigeId")
    private Long litigeId;

    @JsonProperty("demandeParInstitutionId")
    private Long demandeParInstitutionId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateDemande")
    private LocalDateTime dateDemande;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateDecision")
    private LocalDateTime dateDecision;

    @JsonProperty("decision")
    private String decision;

    @Size(max = 2000, message = "Les motifs de décision ne peuvent pas dépasser 2000 caractères")
    @JsonProperty("motifsDecision")
    private String motifsDecision;

    @DecimalMin(value = "0.0", message = "Le coût d'arbitrage ne peut pas être négatif")
    @JsonProperty("coutArbitrage")
    private BigDecimal coutArbitrage;

    @JsonProperty("repartitionFrais")
    private String repartitionFrais;

    @JsonProperty("arbitreUtilisateurId")
    private Long arbitreUtilisateurId;

    @JsonProperty("statut")
    private String statut;

    // Champs calculés pour l'affichage et workflow
    @JsonProperty("statutLibelle")
    private String statutLibelle;

    @JsonProperty("decisionLibelle")
    private String decisionLibelle;

    @JsonProperty("repartitionFraisLibelle")
    private String repartitionFraisLibelle;

    @JsonProperty("dureeArbitrageJours")
    private Long dureeArbitrageJours;

    @JsonProperty("dureeArbitrageHeures")
    private Long dureeArbitrageHeures;

    @JsonProperty("dateDemandeFormatee")
    private String dateDemandeFormatee;

    @JsonProperty("dateDecisionFormatee")
    private String dateDecisionFormatee;

    @JsonProperty("payeurFrais")
    private String payeurFrais;

    @JsonProperty("repartitionMontants")
    private String repartitionMontants;

    @JsonProperty("resume")
    private String resume;

    @JsonProperty("enRetard")
    private Boolean enRetard;

    @JsonProperty("priorite")
    private String priorite;

    @JsonProperty("complexite")
    private String complexite;

    @JsonProperty("slaJours")
    private Integer slaJours;

    @JsonProperty("respecteSLA")
    private Boolean respecteSLA;

    @JsonProperty("pourcentageSLA")
    private Double pourcentageSLA;

    @JsonProperty("couleurStatut")
    private String couleurStatut;

    @JsonProperty("iconeStatut")
    private String iconeStatut;

    @JsonProperty("actionsDisponibles")
    private String[] actionsDisponibles;

    @JsonProperty("alertes")
    private List<String> alertes;

    @JsonProperty("historique")
    private List<ActionArbitrageDTO> historique;

    // Informations enrichies
    @JsonProperty("demandeParInstitutionNom")
    private String demandeParInstitutionNom;

    @JsonProperty("arbitreNom")
    private String arbitreNom;

    @JsonProperty("typeArbitrage")
    private String typeArbitrage;

    @JsonProperty("niveauArbitrage")
    private Integer niveauArbitrage;

    @JsonProperty("peutFaireAppel")
    private Boolean peutFaireAppel;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateLimiteAppel")
    private LocalDateTime dateLimiteAppel;

    // Énumérations avec métadonnées
    public enum Decision {
        FAVORABLE_EMETTEUR("Favorable à la banque émettrice", "check-circle", "green", "La banque émettrice obtient gain de cause"),
        FAVORABLE_ACQUEREUR("Favorable à la banque acquéreuse", "x-circle", "red", "La banque acquéreuse obtient gain de cause"),
        PARTAGE_RESPONSABILITE("Responsabilité partagée", "balance", "orange", "Les deux parties partagent la responsabilité");

        private final String libelle;
        private final String icone;
        private final String couleur;
        private final String description;

        Decision(String libelle, String icone, String couleur, String description) {
            this.libelle = libelle;
            this.icone = icone;
            this.couleur = couleur;
            this.description = description;
        }

        public String getLibelle() { return libelle; }
        public String getIcone() { return icone; }
        public String getCouleur() { return couleur; }
        public String getDescription() { return description; }
    }

    public enum RepartitionFrais {
        PERDANT("Payé par la partie perdante", "user-x", "red"),
        EMETTEUR("Payé par la banque émettrice", "building", "blue"),
        ACQUEREUR("Payé par la banque acquéreuse", "store", "purple"),
        PARTAGE("Partagé entre les deux parties", "users", "orange");

        private final String libelle;
        private final String icone;
        private final String couleur;

        RepartitionFrais(String libelle, String icone, String couleur) {
            this.libelle = libelle;
            this.icone = icone;
            this.couleur = couleur;
        }

        public String getLibelle() { return libelle; }
        public String getIcone() { return icone; }
        public String getCouleur() { return couleur; }
    }

    public enum StatutArbitrage {
        DEMANDE("Demande d'arbitrage", "clock", "blue", "L'arbitrage a été demandé mais pas encore assigné"),
        EN_COURS("Arbitrage en cours", "activity", "orange", "Un arbitre examine le dossier"),
        DECIDE("Décision rendue", "check", "green", "L'arbitrage est terminé avec une décision"),
        APPEL("En appel", "arrow-up", "purple", "Décision contestée, en cours d'appel"),
        ANNULE("Annulé", "x", "gray", "Arbitrage annulé ou retiré");

        private final String libelle;
        private final String icone;
        private final String couleur;
        private final String description;

        StatutArbitrage(String libelle, String icone, String couleur, String description) {
            this.libelle = libelle;
            this.icone = icone;
            this.couleur = couleur;
            this.description = description;
        }

        public String getLibelle() { return libelle; }
        public String getIcone() { return icone; }
        public String getCouleur() { return couleur; }
        public String getDescription() { return description; }
    }

    public enum PrioriteArbitrage {
        BASSE(45, "Priorité basse", "chevron-down", "blue"),
        NORMALE(30, "Priorité normale", "minus", "gray"),
        HAUTE(15, "Priorité haute", "chevron-up", "orange"),
        CRITIQUE(7, "Priorité critique", "alert-triangle", "red"),
        URGENTE(3, "Urgence absolue", "zap", "purple");

        private final int slaJours;
        private final String libelle;
        private final String icone;
        private final String couleur;

        PrioriteArbitrage(int slaJours, String libelle, String icone, String couleur) {
            this.slaJours = slaJours;
            this.libelle = libelle;
            this.icone = icone;
            this.couleur = couleur;
        }

        public int getSlaJours() { return slaJours; }
        public String getLibelle() { return libelle; }
        public String getIcone() { return icone; }
        public String getCouleur() { return couleur; }
    }

    // Classe interne pour l'historique des actions
    public static class ActionArbitrageDTO {
        @JsonProperty("action")
        private String action;

        @JsonProperty("date")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime date;

        @JsonProperty("utilisateurNom")
        private String utilisateurNom;

        @JsonProperty("details")
        private String details;

        @JsonProperty("icone")
        private String icone;

        @JsonProperty("couleur")
        private String couleur;

        // Constructeurs
        public ActionArbitrageDTO() {}

        public ActionArbitrageDTO(String action, LocalDateTime date, String utilisateurNom, String details) {
            this.action = action;
            this.date = date;
            this.utilisateurNom = utilisateurNom;
            this.details = details;
            this.determinerIconeEtCouleur();
        }

        private void determinerIconeEtCouleur() {
            if (action == null) return;

            switch (action.toLowerCase()) {
                case "creation":
                case "demande":
                    this.icone = "plus-circle";
                    this.couleur = "blue";
                    break;
                case "assignation":
                    this.icone = "user-check";
                    this.couleur = "green";
                    break;
                case "decision":
                    this.icone = "gavel";
                    this.couleur = "purple";
                    break;
                case "appel":
                    this.icone = "arrow-up";
                    this.couleur = "orange";
                    break;
                case "annulation":
                    this.icone = "x-circle";
                    this.couleur = "red";
                    break;
                default:
                    this.icone = "info";
                    this.couleur = "gray";
            }
        }

        // Getters et setters
        public String getAction() { return action; }
        public void setAction(String action) {
            this.action = action;
            this.determinerIconeEtCouleur();
        }

        public LocalDateTime getDate() { return date; }
        public void setDate(LocalDateTime date) { this.date = date; }

        public String getUtilisateurNom() { return utilisateurNom; }
        public void setUtilisateurNom(String utilisateurNom) { this.utilisateurNom = utilisateurNom; }

        public String getDetails() { return details; }
        public void setDetails(String details) { this.details = details; }

        public String getIcone() { return icone; }
        public void setIcone(String icone) { this.icone = icone; }

        public String getCouleur() { return couleur; }
        public void setCouleur(String couleur) { this.couleur = couleur; }
    }

    // Constructeurs
    public ArbitrageDTO() {
        this.statut = "DEMANDE";
        this.priorite = "NORMALE";
        this.niveauArbitrage = 1;
        this.peutFaireAppel = true;
        this.alertes = new ArrayList<>();
        this.historique = new ArrayList<>();
    }

    public ArbitrageDTO(Long litigeId, Long demandeParInstitutionId) {
        this();
        this.litigeId = litigeId;
        this.demandeParInstitutionId = demandeParInstitutionId;
        this.dateDemande = LocalDateTime.now();
        this.calculateDerivedFields();
    }

    public ArbitrageDTO(Long litigeId, Long demandeParInstitutionId, BigDecimal coutArbitrage) {
        this(litigeId, demandeParInstitutionId);
        this.coutArbitrage = coutArbitrage;
        this.calculateDerivedFields();
    }

    public ArbitrageDTO(Long id, Long litigeId, Long demandeParInstitutionId, LocalDateTime dateDemande,
                        LocalDateTime dateDecision, String decision, String motifsDecision,
                        BigDecimal coutArbitrage, String repartitionFrais, Long arbitreUtilisateurId,
                        String statut) {
        this();
        this.id = id;
        this.litigeId = litigeId;
        this.demandeParInstitutionId = demandeParInstitutionId;
        this.dateDemande = dateDemande;
        this.dateDecision = dateDecision;
        this.decision = decision;
        this.motifsDecision = motifsDecision;
        this.coutArbitrage = coutArbitrage;
        this.repartitionFrais = repartitionFrais;
        this.arbitreUtilisateurId = arbitreUtilisateurId;
        this.statut = statut;
        this.calculateDerivedFields();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) {
        this.id = id;
    }

    public Long getLitigeId() { return litigeId; }
    public void setLitigeId(Long litigeId) {
        this.litigeId = litigeId;
    }

    public Long getDemandeParInstitutionId() { return demandeParInstitutionId; }
    public void setDemandeParInstitutionId(Long demandeParInstitutionId) {
        this.demandeParInstitutionId = demandeParInstitutionId;
    }

    public LocalDateTime getDateDemande() { return dateDemande; }
    public void setDateDemande(LocalDateTime dateDemande) {
        this.dateDemande = dateDemande;
        this.calculateDerivedFields();
    }

    public LocalDateTime getDateDecision() { return dateDecision; }
    public void setDateDecision(LocalDateTime dateDecision) {
        this.dateDecision = dateDecision;
        this.calculateDerivedFields();
    }

    public String getDecision() { return decision; }
    public void setDecision(String decision) {
        this.decision = decision;
        this.calculateDerivedFields();
    }

    public String getMotifsDecision() { return motifsDecision; }
    public void setMotifsDecision(String motifsDecision) {
        this.motifsDecision = motifsDecision;
    }

    public BigDecimal getCoutArbitrage() { return coutArbitrage; }
    public void setCoutArbitrage(BigDecimal coutArbitrage) {
        this.coutArbitrage = coutArbitrage;
        this.calculateDerivedFields();
    }

    public String getRepartitionFrais() { return repartitionFrais; }
    public void setRepartitionFrais(String repartitionFrais) {
        this.repartitionFrais = repartitionFrais;
        this.calculateDerivedFields();
    }

    public Long getArbitreUtilisateurId() { return arbitreUtilisateurId; }
    public void setArbitreUtilisateurId(Long arbitreUtilisateurId) {
        this.arbitreUtilisateurId = arbitreUtilisateurId;
        this.calculateDerivedFields();
    }

    public String getStatut() { return statut; }
    public void setStatut(String statut) {
        this.statut = statut;
        this.calculateDerivedFields();
    }

    public String getPriorite() { return priorite; }
    public void setPriorite(String priorite) {
        this.priorite = priorite;
        this.calculateDerivedFields();
    }

    public String getComplexite() { return complexite; }
    public void setComplexite(String complexite) { this.complexite = complexite; }

    public Integer getSlaJours() { return slaJours; }
    public void setSlaJours(Integer slaJours) { this.slaJours = slaJours; }

    public String getDemandeParInstitutionNom() { return demandeParInstitutionNom; }
    public void setDemandeParInstitutionNom(String demandeParInstitutionNom) {
        this.demandeParInstitutionNom = demandeParInstitutionNom;
    }

    public String getArbitreNom() { return arbitreNom; }
    public void setArbitreNom(String arbitreNom) { this.arbitreNom = arbitreNom; }

    public String getTypeArbitrage() { return typeArbitrage; }
    public void setTypeArbitrage(String typeArbitrage) { this.typeArbitrage = typeArbitrage; }

    public Integer getNiveauArbitrage() { return niveauArbitrage; }
    public void setNiveauArbitrage(Integer niveauArbitrage) { this.niveauArbitrage = niveauArbitrage; }

    public Boolean getPeutFaireAppel() { return peutFaireAppel; }
    public void setPeutFaireAppel(Boolean peutFaireAppel) { this.peutFaireAppel = peutFaireAppel; }

    public LocalDateTime getDateLimiteAppel() { return dateLimiteAppel; }
    public void setDateLimiteAppel(LocalDateTime dateLimiteAppel) { this.dateLimiteAppel = dateLimiteAppel; }

    public List<ActionArbitrageDTO> getHistorique() { return historique; }
    public void setHistorique(List<ActionArbitrageDTO> historique) { this.historique = historique; }

    // Getters pour champs calculés (lecture seule)
    public String getStatutLibelle() { return statutLibelle; }
    public String getDecisionLibelle() { return decisionLibelle; }
    public String getRepartitionFraisLibelle() { return repartitionFraisLibelle; }
    public Long getDureeArbitrageJours() { return dureeArbitrageJours; }
    public Long getDureeArbitrageHeures() { return dureeArbitrageHeures; }
    public String getDateDemandeFormatee() { return dateDemandeFormatee; }
    public String getDateDecisionFormatee() { return dateDecisionFormatee; }
    public String getPayeurFrais() { return payeurFrais; }
    public String getRepartitionMontants() { return repartitionMontants; }
    public String getResume() { return resume; }
    public Boolean getEnRetard() { return enRetard; }
    public Boolean getRespecteSLA() { return respecteSLA; }
    public Double getPourcentageSLA() { return pourcentageSLA; }
    public String getCouleurStatut() { return couleurStatut; }
    public String getIconeStatut() { return iconeStatut; }
    public String[] getActionsDisponibles() { return actionsDisponibles; }
    public List<String> getAlertes() { return alertes; }

    // Méthodes utilitaires
    public boolean isDemande() { return "DEMANDE".equals(statut); }
    public boolean isEnCours() { return "EN_COURS".equals(statut); }
    public boolean isDecide() { return "DECIDE".equals(statut); }
    public boolean isAnnule() { return "ANNULE".equals(statut); }
    public boolean hasDecision() { return decision != null && dateDecision != null; }
    public boolean hasArbitre() { return arbitreUtilisateurId != null; }
    public boolean hasCout() { return coutArbitrage != null && coutArbitrage.compareTo(BigDecimal.ZERO) > 0; }
    public boolean isFavorableEmetteur() { return "FAVORABLE_EMETTEUR".equals(decision); }
    public boolean isFavorableAcquereur() { return "FAVORABLE_ACQUEREUR".equals(decision); }
    public boolean isEnRetard() { return Boolean.TRUE.equals(enRetard); }

    public boolean peutEncoreFaireAppel() {
        return Boolean.TRUE.equals(peutFaireAppel) &&
                dateLimiteAppel != null &&
                LocalDateTime.now().isBefore(dateLimiteAppel) &&
                isDecide();
    }

    public boolean isUrgent() {
        return "CRITIQUE".equals(priorite) || "URGENTE".equals(priorite);
    }

    public boolean isCoutEleve() {
        return hasCout() && coutArbitrage.compareTo(BigDecimal.valueOf(5000)) > 0;
    }

    /**
     * Calcule tous les champs dérivés basés sur l'état actuel
     */
    private void calculateDerivedFields() {
        // Calculs temporels
        this.dureeArbitrageJours = calculerDureeArbitrageJours();
        this.dureeArbitrageHeures = calculerDureeArbitrageHeures();

        // Formatage des dates
        this.dateDemandeFormatee = formaterDate(dateDemande);
        this.dateDecisionFormatee = formaterDate(dateDecision);

        // Libellés et descriptions
        this.statutLibelle = calculerStatutLibelle();
        this.decisionLibelle = calculerDecisionLibelle();
        this.repartitionFraisLibelle = calculerRepartitionFraisLibelle();

        // Calculs financiers
        this.payeurFrais = determinerPayeurFrais();
        this.repartitionMontants = calculerRepartitionMontants();

        // Gestion des délais et SLA
        this.slaJours = getSlaSelonPriorite();
        this.enRetard = verifierRetard(this.slaJours);
        this.respecteSLA = calculerRespectSLA();
        this.pourcentageSLA = calculerPourcentageSLA();

        // Interface utilisateur
        this.couleurStatut = calculerCouleurStatut();
        this.iconeStatut = calculerIconeStatut();
        this.actionsDisponibles = determinerActionsDisponibles();

        // Alertes et notifications
        this.alertes = genererAlertes();

        // Résumé
        this.resume = genererResume();
    }

    private Long calculerDureeArbitrageJours() {
        if (dateDemande == null) return null;
        LocalDateTime fin = dateDecision != null ? dateDecision : LocalDateTime.now();
        return ChronoUnit.DAYS.between(dateDemande, fin);
    }

    private Long calculerDureeArbitrageHeures() {
        if (dateDemande == null) return null;
        LocalDateTime fin = dateDecision != null ? dateDecision : LocalDateTime.now();
        return ChronoUnit.HOURS.between(dateDemande, fin);
    }

    private String formaterDate(LocalDateTime date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    private String calculerStatutLibelle() {
        if (statut == null) return "Non défini";
        try {
            return StatutArbitrage.valueOf(statut).getLibelle();
        } catch (IllegalArgumentException e) {
            return statut;
        }
    }

    private String calculerDecisionLibelle() {
        if (decision == null) return null;
        try {
            return Decision.valueOf(decision).getLibelle();
        } catch (IllegalArgumentException e) {
            return decision;
        }
    }

    private String calculerRepartitionFraisLibelle() {
        if (repartitionFrais == null) return null;
        try {
            return RepartitionFrais.valueOf(repartitionFrais).getLibelle();
        } catch (IllegalArgumentException e) {
            return repartitionFrais;
        }
    }

    private String determinerPayeurFrais() {
        if (repartitionFrais == null) return "Non déterminé";

        switch (repartitionFrais) {
            case "PERDANT":
                if (decision == null) return "En attente de décision";
                return "FAVORABLE_EMETTEUR".equals(decision) ?
                        "Banque acquéreuse (partie perdante)" :
                        "Banque émettrice (partie perdante)";
            case "EMETTEUR": return "Banque émettrice";
            case "ACQUEREUR": return "Banque acquéreuse";
            case "PARTAGE": return "Partagé entre les deux banques";
            default: return "Non défini";
        }
    }

    private String calculerRepartitionMontants() {
        if (coutArbitrage == null || coutArbitrage.compareTo(BigDecimal.ZERO) == 0) {
            return "Coût non défini";
        }
        if (repartitionFrais == null) return "Répartition non définie";

        switch (repartitionFrais) {
            case "PERDANT":
                return String.format("100%% (%s MAD) - %s", coutArbitrage, payeurFrais);
            case "EMETTEUR":
            case "ACQUEREUR":
                return String.format("100%% (%s MAD) - %s", coutArbitrage,
                        "EMETTEUR".equals(repartitionFrais) ? "Banque émettrice" : "Banque acquéreuse");
            case "PARTAGE":
                BigDecimal moitie = coutArbitrage.divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
                return String.format("50%% chacune (%s MAD par banque)", moitie);
            default: return "Répartition non définie";
        }
    }

    private int getSlaSelonPriorite() {
        if (priorite == null) return 30;
        try {
            return PrioriteArbitrage.valueOf(priorite).getSlaJours();
        } catch (IllegalArgumentException e) {
            return 30;
        }
    }

    private Boolean verifierRetard(int delaiMaxJours) {
        return !isDecide() && dureeArbitrageJours != null && dureeArbitrageJours > delaiMaxJours;
    }

    private Boolean calculerRespectSLA() {
        return !Boolean.TRUE.equals(enRetard);
    }

    private Double calculerPourcentageSLA() {
        if (dureeArbitrageJours == null || slaJours == null || slaJours == 0) return null;
        return Math.min(100.0, (dureeArbitrageJours * 100.0) / slaJours);
    }

    private String calculerCouleurStatut() {
        if (statut == null) return "gray";
        try {
            return StatutArbitrage.valueOf(statut).getCouleur();
        } catch (IllegalArgumentException e) {
            return "gray";
        }
    }

    private String calculerIconeStatut() {
        if (statut == null) return "help-circle";
        try {
            return StatutArbitrage.valueOf(statut).getIcone();
        } catch (IllegalArgumentException e) {
            return "help-circle";
        }
    }

    private String[] determinerActionsDisponibles() {
        List<String> actions = new ArrayList<>();

        if ("DEMANDE".equals(statut)) {
            actions.add("assigner_arbitre");
            actions.add("modifier_priorite");
            actions.add("annuler");
        } else if ("EN_COURS".equals(statut)) {
            actions.add("rendre_decision");
            actions.add("prolonger_delai");
            actions.add("changer_arbitre");
            actions.add("ajouter_commentaire");
        } else if ("DECIDE".equals(statut)) {
            if (peutEncoreFaireAppel()) {
                actions.add("faire_appel");
            }
            actions.add("consulter_decision");
            actions.add("telecharger_rapport");
        }

        actions.add("voir_historique");
        actions.add("notifier_parties");

        return actions.toArray(new String[0]);
    }

    private List<String> genererAlertes() {
        List<String> alertes = new ArrayList<>();

        if (Boolean.TRUE.equals(enRetard)) {
            alertes.add("DELAI_DEPASSE");
        }

        if (isUrgent()) {
            alertes.add("PRIORITE_ELEVEE");
        }

        if (isCoutEleve()) {
            alertes.add("COUT_ELEVE");
        }

        if (peutEncoreFaireAppel() && dateLimiteAppel != null) {
            long joursAvantLimiteAppel = ChronoUnit.DAYS.between(LocalDateTime.now(), dateLimiteAppel);
            if (joursAvantLimiteAppel <= 7) {
                alertes.add("DELAI_APPEL_PROCHE");
            }
        }

        // Alerte SLA
        if (pourcentageSLA != null && pourcentageSLA > 80 && !isDecide()) {
            alertes.add("SLA_CRITIQUE");
        }

        return alertes;
    }

    private String genererResume() {
        StringBuilder sb = new StringBuilder();
        sb.append("Arbitrage ").append(statutLibelle != null ? statutLibelle.toLowerCase() : "non défini");

        if (hasDecision()) {
            sb.append(" - ").append(decisionLibelle);
        }

        if (hasCout()) {
            sb.append(" - Coût: ").append(coutArbitrage).append(" MAD");
        }

        if (dureeArbitrageJours != null) {
            sb.append(" - Durée: ").append(dureeArbitrageJours).append(" jour(s)");
        }

        return sb.toString();
    }

    /**
     * Ajoute une action à l'historique
     */
    public void ajouterActionHistorique(String action, String utilisateurNom, String details) {
        if (this.historique == null) {
            this.historique = new ArrayList<>();
        }
        this.historique.add(new ActionArbitrageDTO(action, LocalDateTime.now(), utilisateurNom, details));
    }

    /**
     * Vérifie si l'arbitrage peut être modifié
     */
    public boolean peutEtreModifie() {
        return isDemande() || isEnCours();
    }

    /**
     * Vérifie si l'arbitrage peut être annulé
     */
    public boolean peutEtreAnnule() {
        return isDemande() || isEnCours();
    }

    /**
     * Calcule le délai restant en jours
     */
    public long getDelaiRestantJours() {
        if (isDecide() || slaJours == null || dateDemande == null) return 0;

        LocalDateTime dateLimite = dateDemande.plusDays(slaJours);
        long restant = ChronoUnit.DAYS.between(LocalDateTime.now(), dateLimite);
        return Math.max(0, restant);
    }

    /**
     * Détermine le niveau d'urgence
     */
    public String getNiveauUrgence() {
        if (isDecide()) return "TERMINE";

        long joursRestants = getDelaiRestantJours();
        if (joursRestants <= 0) return "DEPASSE";
        if (joursRestants <= 2) return "CRITIQUE";
        if (joursRestants <= 5) return "URGENT";
        if (joursRestants <= 10) return "ATTENTION";
        return "NORMAL";
    }

    /**
     * Factory methods et conversion
     */
    public Arbitrage toEntity() {
        Arbitrage entity = new Arbitrage();
        entity.setId(this.id);
        entity.setLitigeId(this.litigeId);
        entity.setDemandeParInstitutionId(this.demandeParInstitutionId);
        entity.setDateDemande(this.dateDemande);
        entity.setDateDecision(this.dateDecision);

        if (this.decision != null) {
            try {
                entity.setDecision(Arbitrage.Decision.valueOf(this.decision));
            } catch (IllegalArgumentException e) {
                // Log warning mais continue sans decision
            }
        }

        entity.setMotifsDecision(this.motifsDecision);
        entity.setCoutArbitrage(this.coutArbitrage);

        if (this.repartitionFrais != null) {
            try {
                entity.setRepartitionFrais(Arbitrage.RepartitionFrais.valueOf(this.repartitionFrais));
            } catch (IllegalArgumentException e) {
                // Log warning mais continue sans repartition
            }
        }

        entity.setArbitreUtilisateurId(this.arbitreUtilisateurId);

        if (this.statut != null) {
            try {
                entity.setStatut(Arbitrage.StatutArbitrage.valueOf(this.statut));
            } catch (IllegalArgumentException e) {
                entity.setStatut(Arbitrage.StatutArbitrage.DEMANDE);
            }
        }

        return entity;
    }

    public static ArbitrageDTO fromEntity(Arbitrage entity) {
        if (entity == null) return null;

        return new ArbitrageDTO(
                entity.getId(),
                entity.getLitigeId(),
                entity.getDemandeParInstitutionId(),
                entity.getDateDemande(),
                entity.getDateDecision(),
                entity.getDecision() != null ? entity.getDecision().name() : null,
                entity.getMotifsDecision(),
                entity.getCoutArbitrage(),
                entity.getRepartitionFrais() != null ? entity.getRepartitionFrais().name() : null,
                entity.getArbitreUtilisateurId(),
                entity.getStatut() != null ? entity.getStatut().name() : null
        );
    }

    public static ArbitrageDTO fromEntityWithNames(Arbitrage entity, String demandeParInstitutionNom,
                                                   String arbitreNom) {
        ArbitrageDTO dto = fromEntity(entity);
        if (dto != null) {
            dto.setDemandeParInstitutionNom(demandeParInstitutionNom);
            dto.setArbitreNom(arbitreNom);
            dto.calculateDerivedFields(); // Recalculer après ajout des noms
        }
        return dto;
    }

    public static ArbitrageDTO minimal(Arbitrage entity) {
        if (entity == null) return null;

        ArbitrageDTO dto = new ArbitrageDTO();
        dto.setId(entity.getId());
        dto.setLitigeId(entity.getLitigeId());
        dto.setStatut(entity.getStatut() != null ? entity.getStatut().name() : null);
        dto.setDecision(entity.getDecision() != null ? entity.getDecision().name() : null);
        dto.setDateDemande(entity.getDateDemande());
        dto.setDateDecision(entity.getDateDecision());
        dto.calculateDerivedFields();
        return dto;
    }

    /**
     * Créé un DTO pour nouvelle demande d'arbitrage
     */
    public static ArbitrageDTO nouveauArbitrage(Long litigeId, Long institutionId, String priorite) {
        ArbitrageDTO dto = new ArbitrageDTO(litigeId, institutionId);
        dto.setPriorite(priorite != null ? priorite : "NORMALE");
        dto.setTypeArbitrage("STANDARD");
        dto.setNiveauArbitrage(1);
        dto.setPeutFaireAppel(true);
        dto.ajouterActionHistorique("CREATION", "Système", "Demande d'arbitrage créée");
        return dto;
    }

    /**
     * Créé un DTO pour arbitrage avec coût estimé
     */
    public static ArbitrageDTO avecCoutEstime(Long litigeId, Long institutionId,
                                              BigDecimal montantLitige, String priorite) {
        // Calcul automatique du coût selon le montant et la priorité
        BigDecimal coutEstime = calculerCoutArbitrage(montantLitige, priorite);

        ArbitrageDTO dto = new ArbitrageDTO(litigeId, institutionId, coutEstime);
        dto.setPriorite(priorite != null ? priorite : "NORMALE");
        dto.ajouterActionHistorique("CREATION", "Système",
                "Demande d'arbitrage créée avec coût estimé: " + coutEstime + " MAD");
        return dto;
    }

    /**
     * Calcule le coût d'arbitrage selon le montant du litige et la priorité
     */
    private static BigDecimal calculerCoutArbitrage(BigDecimal montantLitige, String priorite) {
        if (montantLitige == null) return BigDecimal.valueOf(500); // Coût de base

        // Pourcentage de base selon la priorité
        BigDecimal pourcentage = switch (priorite != null ? priorite : "NORMALE") {
            case "URGENTE" -> BigDecimal.valueOf(0.05); // 5%
            case "CRITIQUE" -> BigDecimal.valueOf(0.04); // 4%
            case "HAUTE" -> BigDecimal.valueOf(0.03); // 3%
            case "NORMALE" -> BigDecimal.valueOf(0.02); // 2%
            case "BASSE" -> BigDecimal.valueOf(0.015); // 1.5%
            default -> BigDecimal.valueOf(0.02);
        };

        BigDecimal coutCalcule = montantLitige.multiply(pourcentage);

        // Coût minimum et maximum
        BigDecimal coutMin = BigDecimal.valueOf(500);
        BigDecimal coutMax = BigDecimal.valueOf(50000);

        if (coutCalcule.compareTo(coutMin) < 0) return coutMin;
        if (coutCalcule.compareTo(coutMax) > 0) return coutMax;

        return coutCalcule.setScale(2, RoundingMode.HALF_UP);
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArbitrageDTO that = (ArbitrageDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(litigeId, that.litigeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, litigeId);
    }

    @Override
    public String toString() {
        return "ArbitrageDTO{" +
                "id=" + id +
                ", litigeId=" + litigeId +
                ", statut='" + statut + '\'' +
                ", decision='" + decision + '\'' +
                ", priorite='" + priorite + '\'' +
                ", dateDemande=" + dateDemande +
                ", dateDecision=" + dateDecision +
                ", dureeArbitrageJours=" + dureeArbitrageJours +
                ", enRetard=" + enRetard +
                ", respecteSLA=" + respecteSLA +
                ", coutArbitrage=" + coutArbitrage +
                ", arbitreNom='" + arbitreNom + '\'' +
                '}';
    }
}