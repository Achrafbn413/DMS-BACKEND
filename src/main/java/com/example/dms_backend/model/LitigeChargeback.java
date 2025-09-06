package com.example.dms_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.util.Arrays;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entité de liaison pour étendre les fonctionnalités chargeback d'un litige
 * Table: LITIGES_CHARGEBACK
 */
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "LITIGES_CHARGEBACK",
        indexes = {
                @Index(name = "IDX_LITIGES_CHARGEBACK_PHASE", columnList = "PHASE_ACTUELLE"),
                @Index(name = "IDX_LITIGES_CHARGEBACK_DEADLINE", columnList = "DEADLINE_ACTUELLE"),
                @Index(name = "IDX_LITIGES_CHARGEBACK_MOTIF", columnList = "MOTIF_CHARGEBACK")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_LITIGES_CHARGEBACK_LITIGE", columnNames = "LITIGE_ID")
        })
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LitigeChargeback {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "litiges_chargeback_seq")
    @SequenceGenerator(name = "litiges_chargeback_seq", sequenceName = "SEQ_LITIGES_CHARGEBACK", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @NotNull(message = "L'ID du litige est obligatoire")
    @Column(name = "LITIGE_ID", nullable = false, unique = true)
    private Long litigeId;

    @Size(max = 50, message = "La phase actuelle ne peut pas dépasser 50 caractères")
    @Column(name = "PHASE_ACTUELLE", length = 50)
    private String phaseActuelle = "CHARGEBACK_INITIAL";

    @Size(max = 50, message = "Le motif de chargeback ne peut pas dépasser 50 caractères")
    @Column(name = "MOTIF_CHARGEBACK", length = 50)
    private String motifChargeback;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant contesté doit être positif")
    @Column(name = "MONTANT_CONTESTE", precision = 15, scale = 2)
    private BigDecimal montantConteste;

    @Column(name = "PEUT_ETRE_ESCALADE", nullable = false)
    private Boolean peutEtreEscalade = true;

    @Column(name = "DEADLINE_ACTUELLE")
    private LocalDateTime deadlineActuelle;

    @Column(name = "JOURS_RESTANTS_CALCULE")
    private Integer joursRestantsCalcule;

    @DecimalMin(value = "0.0", message = "Les frais d'arbitrage estimés ne peuvent pas être négatifs")
    @Column(name = "FRAIS_ARBITRAGE_ESTIME", precision = 10, scale = 2)
    private BigDecimal fraisArbitrageEstime;

    @Column(name = "VERSION_WORKFLOW", nullable = false)
    private Integer versionWorkflow = 1;

    @UpdateTimestamp
    @Column(name = "DATE_DERNIERE_ACTION")
    private LocalDateTime dateDerniereAction;

    @CreationTimestamp
    @Column(name = "DATE_CREATION", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "DATE_MODIFICATION")
    private LocalDateTime dateModification;

    // Relations vers les autres entités chargeback
    @OneToMany(mappedBy = "litigeId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<JustificatifChargeback> justificatifs = new ArrayList<>();

    @OneToMany(mappedBy = "litigeId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EchangeLitige> echanges = new ArrayList<>();

    @OneToMany(mappedBy = "litigeId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DelaiLitige> delais = new ArrayList<>();

    @OneToMany(mappedBy = "litigeId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Arbitrage> arbitrages = new ArrayList<>();

    // Constructeurs
    public LitigeChargeback() {
    }

    public LitigeChargeback(Long litigeId) {
        this.litigeId = litigeId;
        this.phaseActuelle = "CHARGEBACK_INITIAL";
        this.peutEtreEscalade = true;
        this.versionWorkflow = 1;
    }

    public LitigeChargeback(Long litigeId, String motifChargeback, BigDecimal montantConteste) {
        this(litigeId);
        this.motifChargeback = motifChargeback;
        this.montantConteste = montantConteste;
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

    public String getPhaseActuelle() {
        return phaseActuelle;
    }

    public void setPhaseActuelle(String phaseActuelle) {
        // Utiliser la méthode de progression avec validation
        this.progresserVersPhase(phaseActuelle);
    }

    public String getMotifChargeback() {
        return motifChargeback;
    }

    public void setMotifChargeback(String motifChargeback) {
        this.motifChargeback = motifChargeback;
    }

    public BigDecimal getMontantConteste() {
        return montantConteste;
    }

    public void setMontantConteste(BigDecimal montantConteste) {
        this.montantConteste = montantConteste;
    }

    public Boolean getPeutEtreEscalade() {
        return peutEtreEscalade;
    }

    public void setPeutEtreEscalade(Boolean peutEtreEscalade) {
        this.peutEtreEscalade = peutEtreEscalade;
    }

    public LocalDateTime getDeadlineActuelle() {
        return deadlineActuelle;
    }

    public void setDeadlineActuelle(LocalDateTime deadlineActuelle) {
        this.deadlineActuelle = deadlineActuelle;
    }

    public Integer getJoursRestantsCalcule() {
        return joursRestantsCalcule;
    }

    public void setJoursRestantsCalcule(Integer joursRestantsCalcule) {
        this.joursRestantsCalcule = joursRestantsCalcule;
    }

    public BigDecimal getFraisArbitrageEstime() {
        return fraisArbitrageEstime;
    }

    public void setFraisArbitrageEstime(BigDecimal fraisArbitrageEstime) {
        this.fraisArbitrageEstime = fraisArbitrageEstime;
    }

    public Integer getVersionWorkflow() {
        return versionWorkflow;
    }

    public void setVersionWorkflow(Integer versionWorkflow) {
        this.versionWorkflow = versionWorkflow;
    }

    public LocalDateTime getDateDerniereAction() {
        return dateDerniereAction;
    }

    public void setDateDerniereAction(LocalDateTime dateDerniereAction) {
        this.dateDerniereAction = dateDerniereAction;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    public List<JustificatifChargeback> getJustificatifs() {
        return justificatifs;
    }

    public void setJustificatifs(List<JustificatifChargeback> justificatifs) {
        this.justificatifs = justificatifs;
    }

    public List<EchangeLitige> getEchanges() {
        return echanges;
    }

    public void setEchanges(List<EchangeLitige> echanges) {
        this.echanges = echanges;
    }

    public List<DelaiLitige> getDelais() {
        return delais;
    }

    public void setDelais(List<DelaiLitige> delais) {
        this.delais = delais;
    }

    public List<Arbitrage> getArbitrages() {
        return arbitrages;
    }

    public void setArbitrages(List<Arbitrage> arbitrages) {
        this.arbitrages = arbitrages;
    }

    // Méthodes utilitaires
    public boolean isPeutEtreEscalade() {
        return Boolean.TRUE.equals(peutEtreEscalade);
    }

   /* public boolean isDeadlineDepassee() {
        return deadlineActuelle != null && deadlineActuelle.isBefore(LocalDateTime.now());
    }*/

    public boolean isPhaseFinale() {
        return "FINALISE".equals(phaseActuelle);
    }

    public boolean isEnArbitrage() {
        return "ARBITRAGE".equals(phaseActuelle);
    }

    /**
     * Met à jour la phase et la date de dernière action
     */
    /**
     * Met à jour la phase et la date de dernière action avec validation
     */
    public void progresserVersPhase(String nouvellePhase) {
        // Validation des phases autorisées
        java.util.List<String> phasesValides = java.util.Arrays.asList(
                "CHARGEBACK_INITIAL", "REPRESENTATION", "PRE_ARBITRAGE", "ARBITRAGE", "FINALISE"
        );

        if (nouvellePhase == null || nouvellePhase.trim().isEmpty()) {
            throw new IllegalArgumentException("La nouvelle phase ne peut pas être nulle ou vide");
        }

        if (!phasesValides.contains(nouvellePhase)) {
            throw new IllegalArgumentException("Phase non autorisée: " + nouvellePhase +
                    ". Phases valides: " + phasesValides);
        }

        // Log de la progression (optionnel - supprimer si pas de logging)
        String ancienePhase = this.phaseActuelle;

        this.phaseActuelle = nouvellePhase;
        this.dateDerniereAction = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();

        // Marquer comme phase finale si nécessaire
        if ("FINALISE".equals(nouvellePhase)) {
            this.peutEtreEscalade = false;
        }

        // Recalculer les jours restants après changement de phase
        this.calculerJoursRestants();
    }

    /**
     * Calcule les jours restants jusqu'à la deadline
     */
    /**
     * Calcule les jours restants jusqu'à la deadline
     */
    /**
     * Calcule les jours restants jusqu'à la deadline
     */
    public void calculerJoursRestants() {
        if (deadlineActuelle != null && !isPhaseFinale()) {
            long joursRestants = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDateTime.now().toLocalDate(),
                    deadlineActuelle.toLocalDate()
            );
            this.joursRestantsCalcule = (int) Math.max(0, joursRestants);
        } else {
            this.joursRestantsCalcule = null;
        }
    }

    /**
     * Vérifie si la deadline est urgente (moins de 3 jours)
     */
    public boolean isDeadlineUrgente() {
        if (deadlineActuelle == null || isPhaseFinale()) {
            return false;
        }

        long joursRestants = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDateTime.now().toLocalDate(),
                deadlineActuelle.toLocalDate()
        );

        return joursRestants <= 3 && joursRestants >= 0;
    }

    /**
     * Vérifie si la deadline est dépassée
     */
    public boolean isDeadlineDepassee() {
        return deadlineActuelle != null &&
                !isPhaseFinale() &&
                deadlineActuelle.isBefore(LocalDateTime.now());
    }
    /**
     * Vérifie si la deadline est urgente (moins de 3 jours)
   /*
    public boolean isDeadlineUrgente() {
        if (deadlineActuelle == null || isPhaseFinale()) {
            return false;
        }

        long joursRestants = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDateTime.now().toLocalDate(),
                deadlineActuelle.toLocalDate()
        );

        return joursRestants <= 3 && joursRestants >= 0;
    }
*/
    /**
     * Vérifie si la deadline est dépassée
     *//*
    @Override
    public boolean isDeadlineDepassee() {
        return deadlineActuelle != null &&
                !isPhaseFinale() &&
                deadlineActuelle.isBefore(LocalDateTime.now());
    }*/

    /**
     * Ajoute un justificatif à ce litige chargeback
     */
    public void ajouterJustificatif(JustificatifChargeback justificatif) {
        justificatifs.add(justificatif);
        justificatif.setLitigeId(this.litigeId);
    }

    /**
     * Ajoute un échange à ce litige chargeback
     */
    public void ajouterEchange(EchangeLitige echange) {
        echanges.add(echange);
        echange.setLitigeId(this.litigeId);
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LitigeChargeback that = (LitigeChargeback) o;
        return Objects.equals(id, that.id) && Objects.equals(litigeId, that.litigeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, litigeId);
    }

    @Override
    public String toString() {
        return "LitigeChargeback{" +
                "id=" + id +
                ", litigeId=" + litigeId +
                ", phaseActuelle='" + phaseActuelle + '\'' +
                ", motifChargeback='" + motifChargeback + '\'' +
                ", montantConteste=" + montantConteste +
                ", peutEtreEscalade=" + peutEtreEscalade +
                ", deadlineActuelle=" + deadlineActuelle +
                ", joursRestantsCalcule=" + joursRestantsCalcule +
                ", dateCreation=" + dateCreation +
                ", dateModification=" + dateModification +
                '}';
    }

    /**
     * Vérifie si une progression vers une nouvelle phase est autorisée
     */
    public boolean peutProgresserVers(String nouvellePhase) {
        if (nouvellePhase == null || this.phaseActuelle == null) {
            return false;
        }

        // Si déjà finalisé, aucune progression possible
        if (isPhaseFinale()) {
            return false;
        }

        // Logique de progression séquentielle
        switch (this.phaseActuelle) {
            case "CHARGEBACK_INITIAL":
                return "REPRESENTATION".equals(nouvellePhase);
            case "REPRESENTATION":
                return "PRE_ARBITRAGE".equals(nouvellePhase) || "FINALISE".equals(nouvellePhase);
            case "PRE_ARBITRAGE":
                return "ARBITRAGE".equals(nouvellePhase) || "FINALISE".equals(nouvellePhase);
            case "ARBITRAGE":
                return "FINALISE".equals(nouvellePhase);
            default:
                return false;
        }
    }
    /**
     * Vérifie si le chargeback est en phase initiale
     */
    public boolean isPhaseInitiale() {
        return "CHARGEBACK_INITIAL".equals(phaseActuelle);
    }

    /**
     * Vérifie si le chargeback est en phase de représentation
     */
    public boolean isEnRepresentation() {
        return "REPRESENTATION".equals(phaseActuelle);
    }

    /**
     * Vérifie si le chargeback est en pré-arbitrage
     */
    public boolean isEnPreArbitrage() {
        return "PRE_ARBITRAGE".equals(phaseActuelle);
    }

    /**
     * Obtient le libellé français de la phase actuelle
     */
    public String getPhaseLibelle() {
        if (phaseActuelle == null) {
            return "Phase inconnue";
        }

        switch (phaseActuelle) {
            case "CHARGEBACK_INITIAL":
                return "Chargeback Initial";
            case "REPRESENTATION":
                return "Représentation";
            case "PRE_ARBITRAGE":
                return "Pré-Arbitrage";
            case "ARBITRAGE":
                return "Arbitrage";
            case "FINALISE":
                return "Finalisé";
            default:
                return "Phase: " + phaseActuelle;
        }
    }
}