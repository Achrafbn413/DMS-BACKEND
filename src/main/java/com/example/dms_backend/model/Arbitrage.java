package com.example.dms_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Entité représentant les arbitrages d'un litige
 * Table: ARBITRAGES
 */
@Entity
@Table(name = "ARBITRAGES",
        indexes = {
                @Index(name = "IDX_ARBITRAGES_LITIGE", columnList = "LITIGE_ID"),
                @Index(name = "IDX_ARBITRAGES_STATUT", columnList = "STATUT"),
                @Index(name = "IDX_ARBITRAGES_DEMANDEUR", columnList = "DEMANDE_PAR_INSTITUTION_ID"),
                @Index(name = "IDX_ARBITRAGES_ARBITRE", columnList = "ARBITRE_UTILISATEUR_ID"),
                @Index(name = "IDX_ARBITRAGES_DATE_DEMANDE", columnList = "DATE_DEMANDE"),
                @Index(name = "IDX_ARBITRAGES_DATE_DECISION", columnList = "DATE_DECISION")
        })
public class Arbitrage {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "arbitrages_seq")
    @SequenceGenerator(name = "arbitrages_seq", sequenceName = "SEQ_ARBITRAGES", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @NotNull(message = "L'ID du litige est obligatoire")
    @Column(name = "LITIGE_ID", nullable = false)
    private Long litigeId;

    @Column(name = "DEMANDE_PAR_INSTITUTION_ID")
    private Long demandeParInstitutionId;

    @CreationTimestamp
    @Column(name = "DATE_DEMANDE", nullable = false, updatable = false)
    private LocalDateTime dateDemande;

    @Column(name = "DATE_DECISION")
    private LocalDateTime dateDecision;

    @Size(max = 30, message = "La décision ne peut pas dépasser 30 caractères")
    @Column(name = "DECISION", length = 30)
    @Enumerated(EnumType.STRING)
    private Decision decision;

    @Column(name = "MOTIFS_DECISION")
    @Lob
    private String motifsDecision;

    @DecimalMin(value = "0.0", message = "Le coût d'arbitrage ne peut pas être négatif")
    @Column(name = "COUT_ARBITRAGE", precision = 10, scale = 2)
    private BigDecimal coutArbitrage;

    @Size(max = 20, message = "La répartition des frais ne peut pas dépasser 20 caractères")
    @Column(name = "REPARTITION_FRAIS", length = 20)
    @Enumerated(EnumType.STRING)
    private RepartitionFrais repartitionFrais;

    @Column(name = "ARBITRE_UTILISATEUR_ID")
    private Long arbitreUtilisateurId;

    @Size(max = 20, message = "Le statut ne peut pas dépasser 20 caractères")
    @Column(name = "STATUT", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StatutArbitrage statut = StatutArbitrage.DEMANDE;

    // Énumérations
    public enum Decision {
        FAVORABLE_EMETTEUR("Favorable à la banque émettrice"),
        FAVORABLE_ACQUEREUR("Favorable à la banque acquéreuse");

        private final String libelle;

        Decision(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public enum RepartitionFrais {
        PERDANT("Payé par la partie perdante"),
        EMETTEUR("Payé par la banque émettrice"),
        ACQUEREUR("Payé par la banque acquéreuse"),
        PARTAGE("Partagé entre les deux parties");

        private final String libelle;

        RepartitionFrais(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    public enum StatutArbitrage {
        DEMANDE("Demande d'arbitrage"),
        EN_COURS("Arbitrage en cours"),
        DECIDE("Décision rendue");

        private final String libelle;

        StatutArbitrage(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    // Constructeurs
    public Arbitrage() {
    }

    public Arbitrage(Long litigeId, Long demandeParInstitutionId) {
        this.litigeId = litigeId;
        this.demandeParInstitutionId = demandeParInstitutionId;
        this.statut = StatutArbitrage.DEMANDE;
    }

    public Arbitrage(Long litigeId, Long demandeParInstitutionId, BigDecimal coutArbitrage) {
        this(litigeId, demandeParInstitutionId);
        this.coutArbitrage = coutArbitrage;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getLitigeId() {
        return litigeId;
    }

    public void setLitigeId(Long litigeId) {
        this.litigeId = litigeId;
    }

    public Long getDemandeParInstitutionId() {
        return demandeParInstitutionId;
    }

    public void setDemandeParInstitutionId(Long demandeParInstitutionId) {
        this.demandeParInstitutionId = demandeParInstitutionId;
    }

    public LocalDateTime getDateDemande() {
        return dateDemande;
    }

    public void setDateDemande(LocalDateTime dateDemande) {
        this.dateDemande = dateDemande;
    }

    public LocalDateTime getDateDecision() {
        return dateDecision;
    }

    public void setDateDecision(LocalDateTime dateDecision) {
        this.dateDecision = dateDecision;
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }

    public String getMotifsDecision() {
        return motifsDecision;
    }

    public void setMotifsDecision(String motifsDecision) {
        this.motifsDecision = motifsDecision;
    }

    public BigDecimal getCoutArbitrage() {
        return coutArbitrage;
    }

    public void setCoutArbitrage(BigDecimal coutArbitrage) {
        this.coutArbitrage = coutArbitrage;
    }

    public RepartitionFrais getRepartitionFrais() {
        return repartitionFrais;
    }

    public void setRepartitionFrais(RepartitionFrais repartitionFrais) {
        this.repartitionFrais = repartitionFrais;
    }

    public Long getArbitreUtilisateurId() {
        return arbitreUtilisateurId;
    }

    public void setArbitreUtilisateurId(Long arbitreUtilisateurId) {
        this.arbitreUtilisateurId = arbitreUtilisateurId;
    }

    public StatutArbitrage getStatut() {
        return statut;
    }

    public void setStatut(StatutArbitrage statut) {
        this.statut = statut;
    }

    // Méthodes utilitaires
    public boolean isDemande() {
        return StatutArbitrage.DEMANDE.equals(statut);
    }

    public boolean isEnCours() {
        return StatutArbitrage.EN_COURS.equals(statut);
    }

    public boolean isDecide() {
        return StatutArbitrage.DECIDE.equals(statut);
    }

    public boolean hasDecision() {
        return decision != null && dateDecision != null;
    }

    public boolean hasArbitre() {
        return arbitreUtilisateurId != null;
    }

    public boolean hasCout() {
        return coutArbitrage != null && coutArbitrage.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isFavorableEmetteur() {
        return Decision.FAVORABLE_EMETTEUR.equals(decision);
    }

    public boolean isFavorableAcquereur() {
        return Decision.FAVORABLE_ACQUEREUR.equals(decision);
    }

    /**
     * Démarre l'arbitrage en assignant un arbitre
     */
    public void demarrer(Long arbitreId) {
        this.arbitreUtilisateurId = arbitreId;
        this.statut = StatutArbitrage.EN_COURS;
    }

    /**
     * Rend une décision d'arbitrage
     */
    public void rendreDecision(Decision decision, String motifs) {
        this.decision = decision;
        this.motifsDecision = motifs;
        this.dateDecision = LocalDateTime.now();
        this.statut = StatutArbitrage.DECIDE;
    }

    /**
     * Rend une décision d'arbitrage avec répartition des frais
     */
    public void rendreDecision(Decision decision, String motifs, RepartitionFrais repartition) {
        rendreDecision(decision, motifs);
        this.repartitionFrais = repartition;
    }

    /**
     * Calcule la durée de l'arbitrage en jours
     */
    public long calculerDureeArbitrageJours() {
        if (dateDecision != null) {
            return ChronoUnit.DAYS.between(dateDemande, dateDecision);
        } else {
            return ChronoUnit.DAYS.between(dateDemande, LocalDateTime.now());
        }
    }

    /**
     * Calcule la durée de l'arbitrage en heures
     */
    public long calculerDureeArbitrageHeures() {
        if (dateDecision != null) {
            return ChronoUnit.HOURS.between(dateDemande, dateDecision);
        } else {
            return ChronoUnit.HOURS.between(dateDemande, LocalDateTime.now());
        }
    }

    /**
     * Vérifie si l'arbitrage est en retard (plus de X jours)
     */
    public boolean isEnRetard(int delaiMaxJours) {
        return !isDecide() && calculerDureeArbitrageJours() > delaiMaxJours;
    }

    /**
     * Détermine qui doit payer les frais d'arbitrage
     */
    public String determinerPayeurFrais() {
        if (repartitionFrais == null) {
            return "Non déterminé";
        }

        switch (repartitionFrais) {
            case PERDANT:
                if (decision == null) {
                    return "En attente de décision";
                }
                return decision == Decision.FAVORABLE_EMETTEUR ?
                        "Banque acquéreuse (partie perdante)" :
                        "Banque émettrice (partie perdante)";
            case EMETTEUR:
                return "Banque émettrice";
            case ACQUEREUR:
                return "Banque acquéreuse";
            case PARTAGE:
                return "Partagé entre les deux banques";
            default:
                return "Non défini";
        }
    }

    /**
     * Calcule le montant à payer par chaque partie
     */
    public String calculerRepartitionMontants() {
        if (coutArbitrage == null || coutArbitrage.compareTo(BigDecimal.ZERO) == 0) {
            return "Coût non défini";
        }

        if (repartitionFrais == null) {
            return "Répartition non définie";
        }

        switch (repartitionFrais) {
            case PERDANT:
                return String.format("100%% (%s €) - %s",
                        coutArbitrage.toString(),
                        determinerPayeurFrais());
            case EMETTEUR:
                return String.format("100%% (%s €) - Banque émettrice", coutArbitrage.toString());
            case ACQUEREUR:
                return String.format("100%% (%s €) - Banque acquéreuse", coutArbitrage.toString());
            case PARTAGE:
                BigDecimal moitie = coutArbitrage.divide(BigDecimal.valueOf(2), 2, BigDecimal.ROUND_HALF_UP);
                return String.format("50%% chacune (%s € par banque)", moitie.toString());
            default:
                return "Répartition non définie";
        }
    }

    /**
     * Retourne un résumé de l'arbitrage
     */
    public String getResume() {
        StringBuilder resume = new StringBuilder();
        resume.append("Arbitrage ").append(statut.getLibelle().toLowerCase());

        if (hasDecision()) {
            resume.append(" - Décision: ").append(decision.getLibelle());
        }

        if (hasCout()) {
            resume.append(" - Coût: ").append(coutArbitrage).append(" €");
        }

        return resume.toString();
    }

    /**
     * Vérifie si l'arbitrage a été demandé par une institution spécifique
     */
    public boolean estDemandePar(Long institutionId) {
        return Objects.equals(this.demandeParInstitutionId, institutionId);
    }

    /**
     * Vérifie si l'arbitrage est géré par un arbitre spécifique
     */
    public boolean estGerePar(Long arbitreId) {
        return Objects.equals(this.arbitreUtilisateurId, arbitreId);
    }

    /**
     * Annule l'arbitrage (retour au statut demande)
     */
    public void annuler() {
        this.statut = StatutArbitrage.DEMANDE;
        this.arbitreUtilisateurId = null;
        this.decision = null;
        this.dateDecision = null;
        this.motifsDecision = null;
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arbitrage arbitrage = (Arbitrage) o;
        return Objects.equals(id, arbitrage.id) &&
                Objects.equals(litigeId, arbitrage.litigeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, litigeId);
    }

    @Override
    public String toString() {
        return "Arbitrage{" +
                "id=" + id +
                ", litigeId=" + litigeId +
                ", statut=" + statut +
                ", decision=" + decision +
                ", dateDemande=" + dateDemande +
                ", dateDecision=" + dateDecision +
                ", coutArbitrage=" + coutArbitrage +
                '}';
    }
}