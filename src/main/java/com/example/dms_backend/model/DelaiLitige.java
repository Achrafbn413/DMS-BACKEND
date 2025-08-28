package com.example.dms_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Entité représentant les délais et échéances d'un litige
 * Table: DELAIS_LITIGE
 */
@Entity
@Table(name = "DELAIS_LITIGE",
        indexes = {
                @Index(name = "IDX_DELAIS_LITIGE", columnList = "LITIGE_ID"),
                @Index(name = "IDX_DELAIS_LIMITE", columnList = "DATE_LIMITE"),
                @Index(name = "IDX_DELAIS_STATUT", columnList = "STATUT_DELAI"),
                @Index(name = "IDX_DELAIS_PHASE", columnList = "PHASE_LITIGE")
        })
public class DelaiLitige {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "delais_litige_seq")
    @SequenceGenerator(name = "delais_litige_seq", sequenceName = "SEQ_DELAIS_LITIGE", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @NotNull(message = "L'ID du litige est obligatoire")
    @Column(name = "LITIGE_ID", nullable = false)
    private Long litigeId;

    @NotBlank(message = "La phase de litige est obligatoire")
    @Size(max = 50, message = "La phase de litige ne peut pas dépasser 50 caractères")
    @Column(name = "PHASE_LITIGE", nullable = false, length = 50)
    private String phaseLitige;

    @NotNull(message = "La date de début est obligatoire")
    @Column(name = "DATE_DEBUT", nullable = false)
    private LocalDateTime dateDebut;

    @NotNull(message = "La date limite est obligatoire")
    @Column(name = "DATE_LIMITE", nullable = false)
    private LocalDateTime dateLimite;

    @Min(value = 0, message = "La prolongation accordée ne peut pas être négative")
    @Column(name = "PROLONGATION_ACCORDEE", nullable = false)
    private Integer prolongationAccordee = 0;

    @Column(name = "MOTIF_PROLONGATION")
    @Lob
    private String motifProlongation;

    @Size(max = 20, message = "Le statut du délai ne peut pas dépasser 20 caractères")
    @Column(name = "STATUT_DELAI", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private StatutDelai statutDelai = StatutDelai.ACTIF;

    @CreationTimestamp
    @Column(name = "DATE_CREATION", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    // Énumération pour les statuts de délai
    public enum StatutDelai {
        ACTIF("Actif"),
        EXPIRE("Expiré"),
        PROLONGE("Prolongé");

        private final String libelle;

        StatutDelai(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    // Constructeurs
    public DelaiLitige() {
    }

    public DelaiLitige(Long litigeId, String phaseLitige, LocalDateTime dateDebut, LocalDateTime dateLimite) {
        this.litigeId = litigeId;
        this.phaseLitige = phaseLitige;
        this.dateDebut = dateDebut;
        this.dateLimite = dateLimite;
        this.prolongationAccordee = 0;
        this.statutDelai = StatutDelai.ACTIF;
    }

    public DelaiLitige(Long litigeId, String phaseLitige, LocalDateTime dateDebut, int dureeJours) {
        this(litigeId, phaseLitige, dateDebut, dateDebut.plusDays(dureeJours));
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

    public String getPhaseLitige() {
        return phaseLitige;
    }

    public void setPhaseLitige(String phaseLitige) {
        this.phaseLitige = phaseLitige;
    }

    public LocalDateTime getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDateTime dateDebut) {
        this.dateDebut = dateDebut;
    }

    public LocalDateTime getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(LocalDateTime dateLimite) {
        this.dateLimite = dateLimite;
    }

    public Integer getProlongationAccordee() {
        return prolongationAccordee;
    }

    public void setProlongationAccordee(Integer prolongationAccordee) {
        this.prolongationAccordee = prolongationAccordee;
    }

    public String getMotifProlongation() {
        return motifProlongation;
    }

    public void setMotifProlongation(String motifProlongation) {
        this.motifProlongation = motifProlongation;
    }

    public StatutDelai getStatutDelai() {
        return statutDelai;
    }

    public void setStatutDelai(StatutDelai statutDelai) {
        this.statutDelai = statutDelai;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    // Méthodes utilitaires
    public boolean isActif() {
        return StatutDelai.ACTIF.equals(statutDelai);
    }

    public boolean isExpire() {
        return StatutDelai.EXPIRE.equals(statutDelai);
    }

    public boolean isProlonge() {
        return StatutDelai.PROLONGE.equals(statutDelai);
    }

    public boolean hasProlongation() {
        return prolongationAccordee != null && prolongationAccordee > 0;
    }

    /**
     * Vérifie si le délai est dépassé par rapport à la date actuelle
     */
    public boolean isDepasseMaintenant() {
        return LocalDateTime.now().isAfter(dateLimite);
    }

    /**
     * Vérifie si le délai sera dépassé à une date donnée
     */
    public boolean isDepasseA(LocalDateTime dateReference) {
        return dateReference.isAfter(dateLimite);
    }

    /**
     * Calcule le nombre de jours restants (peut être négatif si dépassé)
     */
    public long calculerJoursRestants() {
        return ChronoUnit.DAYS.between(LocalDateTime.now(), dateLimite);
    }

    /**
     * Calcule le nombre d'heures restantes (peut être négatif si dépassé)
     */
    public long calculerHeuresRestantes() {
        return ChronoUnit.HOURS.between(LocalDateTime.now(), dateLimite);
    }

    /**
     * Calcule la durée totale du délai en jours
     */
    public long calculerDureeTotaleJours() {
        return ChronoUnit.DAYS.between(dateDebut, dateLimite);
    }

    /**
     * Calcule le pourcentage d'avancement du délai (0-100)
     */
    public double calculerPourcentageAvancement() {
        LocalDateTime maintenant = LocalDateTime.now();

        if (maintenant.isBefore(dateDebut)) {
            return 0.0;
        }

        if (maintenant.isAfter(dateLimite)) {
            return 100.0;
        }

        long dureeEcoulee = ChronoUnit.MINUTES.between(dateDebut, maintenant);
        long dureeTotale = ChronoUnit.MINUTES.between(dateDebut, dateLimite);

        if (dureeTotale == 0) {
            return 100.0;
        }

        return Math.min(100.0, (dureeEcoulee * 100.0) / dureeTotale);
    }

    /**
     * Prolonge le délai d'un certain nombre de jours
     */
    public void prolonger(int joursSupplementaires, String motif) {
        if (joursSupplementaires > 0) {
            this.dateLimite = this.dateLimite.plusDays(joursSupplementaires);
            this.prolongationAccordee = (this.prolongationAccordee == null ? 0 : this.prolongationAccordee) + joursSupplementaires;
            this.motifProlongation = motif;
            this.statutDelai = StatutDelai.PROLONGE;
        }
    }

    /**
     * Marque le délai comme expiré
     */
    public void marquerCommeExpire() {
        this.statutDelai = StatutDelai.EXPIRE;
    }

    /**
     * Réactive le délai
     */
    public void reactiver() {
        if (!isDepasseMaintenant()) {
            this.statutDelai = StatutDelai.ACTIF;
        }
    }

    /**
     * Met à jour automatiquement le statut basé sur la date actuelle
     */
    public void mettreAJourStatut() {
        if (isDepasseMaintenant() && isActif()) {
            marquerCommeExpire();
        }
    }

    /**
     * Retourne un libellé descriptif du statut avec information temporelle
     */
    public String getStatutAvecInfo() {
        long joursRestants = calculerJoursRestants();

        switch (statutDelai) {
            case ACTIF:
                if (joursRestants > 0) {
                    return String.format("Actif (%d jour(s) restant(s))", joursRestants);
                } else if (joursRestants == 0) {
                    return "Actif (expire aujourd'hui)";
                } else {
                    return String.format("Actif (dépassé de %d jour(s))", Math.abs(joursRestants));
                }
            case EXPIRE:
                return String.format("Expiré (dépassé de %d jour(s))", Math.abs(joursRestants));
            case PROLONGE:
                if (joursRestants > 0) {
                    return String.format("Prolongé (%d jour(s) restant(s))", joursRestants);
                } else {
                    return String.format("Prolongé (dépassé de %d jour(s))", Math.abs(joursRestants));
                }
            default:
                return statutDelai.getLibelle();
        }
    }

    /**
     * Vérifie si le délai est dans la zone critique (moins de X jours restants)
     */
    public boolean isEnZoneCritique(int seuilJours) {
        long joursRestants = calculerJoursRestants();
        return joursRestants >= 0 && joursRestants <= seuilJours;
    }

    /**
     * Vérifie si le délai est dans la zone d'alerte (moins de X% de temps restant)
     */
    public boolean isEnZoneAlerte(double seuilPourcentage) {
        double avancement = calculerPourcentageAvancement();
        return avancement >= (100.0 - seuilPourcentage);
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DelaiLitige that = (DelaiLitige) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(litigeId, that.litigeId) &&
                Objects.equals(phaseLitige, that.phaseLitige);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, litigeId, phaseLitige);
    }

    @Override
    public String toString() {
        return "DelaiLitige{" +
                "id=" + id +
                ", litigeId=" + litigeId +
                ", phaseLitige='" + phaseLitige + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateLimite=" + dateLimite +
                ", statutDelai=" + statutDelai +
                ", prolongationAccordee=" + prolongationAccordee +
                '}';
    }
}