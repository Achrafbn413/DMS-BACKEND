package com.example.dms_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entité représentant les modèles de justificatifs requis par phase et motif
 * Table: MODELES_JUSTIFICATIFS
 */
@Entity
@Table(name = "MODELES_JUSTIFICATIFS",
        indexes = {
                @Index(name = "IDX_MODELES_PHASE_MOTIF", columnList = "PHASE_LITIGE, MOTIF_CHARGEBACK"),
                @Index(name = "IDX_MODELES_TYPE", columnList = "TYPE_JUSTIFICATIF")
        })
public class ModeleJustificatif {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "modeles_justificatifs_seq")
    @SequenceGenerator(name = "modeles_justificatifs_seq", sequenceName = "SEQ_MODELES_JUSTIFICATIFS", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "La phase de litige est obligatoire")
    @Size(max = 50, message = "La phase ne peut pas dépasser 50 caractères")
    @Column(name = "PHASE_LITIGE", nullable = false, length = 50)
    private String phaseLitige;

    @NotBlank(message = "Le motif de chargeback est obligatoire")
    @Size(max = 50, message = "Le motif ne peut pas dépasser 50 caractères")
    @Column(name = "MOTIF_CHARGEBACK", nullable = false, length = 50)
    private String motifChargeback;

    @NotBlank(message = "Le type de justificatif est obligatoire")
    @Size(max = 50, message = "Le type ne peut pas dépasser 50 caractères")
    @Column(name = "TYPE_JUSTIFICATIF", nullable = false, length = 50)
    private String typeJustificatif;

    @Column(name = "OBLIGATOIRE", nullable = false)
    private Boolean obligatoire = false;

    @Column(name = "DESCRIPTION")
    @Lob
    private String description;

    @Size(max = 255, message = "L'exemple de nom de fichier ne peut pas dépasser 255 caractères")
    @Column(name = "EXEMPLE_NOM_FICHIER", length = 255)
    private String exempleNomFichier;

    @CreationTimestamp
    @Column(name = "DATE_CREATION", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    // Constructeurs
    public ModeleJustificatif() {
    }

    public ModeleJustificatif(String phaseLitige, String motifChargeback, String typeJustificatif) {
        this.phaseLitige = phaseLitige;
        this.motifChargeback = motifChargeback;
        this.typeJustificatif = typeJustificatif;
        this.obligatoire = false;
    }

    public ModeleJustificatif(String phaseLitige, String motifChargeback, String typeJustificatif, Boolean obligatoire) {
        this(phaseLitige, motifChargeback, typeJustificatif);
        this.obligatoire = obligatoire;
    }

    public ModeleJustificatif(String phaseLitige, String motifChargeback, String typeJustificatif,
                              Boolean obligatoire, String description) {
        this(phaseLitige, motifChargeback, typeJustificatif, obligatoire);
        this.description = description;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhaseLitige() {
        return phaseLitige;
    }

    public void setPhaseLitige(String phaseLitige) {
        this.phaseLitige = phaseLitige;
    }

    public String getMotifChargeback() {
        return motifChargeback;
    }

    public void setMotifChargeback(String motifChargeback) {
        this.motifChargeback = motifChargeback;
    }

    public String getTypeJustificatif() {
        return typeJustificatif;
    }

    public void setTypeJustificatif(String typeJustificatif) {
        this.typeJustificatif = typeJustificatif;
    }

    public Boolean getObligatoire() {
        return obligatoire;
    }

    public void setObligatoire(Boolean obligatoire) {
        this.obligatoire = obligatoire;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExempleNomFichier() {
        return exempleNomFichier;
    }

    public void setExempleNomFichier(String exempleNomFichier) {
        this.exempleNomFichier = exempleNomFichier;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    // Méthodes utilitaires
    public boolean isObligatoire() {
        return Boolean.TRUE.equals(obligatoire);
    }

    public void rendreObligatoire() {
        this.obligatoire = true;
    }

    public void rendreOptionnel() {
        this.obligatoire = false;
    }

    /**
     * Vérifie si ce modèle correspond à une phase et un motif donnés
     */
    public boolean correspondA(String phase, String motif) {
        return Objects.equals(this.phaseLitige, phase) &&
                Objects.equals(this.motifChargeback, motif);
    }

    /**
     * Génère une clé unique pour ce modèle basée sur phase, motif et type
     */
    public String genererCleUnique() {
        return String.format("%s_%s_%s", phaseLitige, motifChargeback, typeJustificatif);
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModeleJustificatif that = (ModeleJustificatif) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(phaseLitige, that.phaseLitige) &&
                Objects.equals(motifChargeback, that.motifChargeback) &&
                Objects.equals(typeJustificatif, that.typeJustificatif);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, phaseLitige, motifChargeback, typeJustificatif);
    }

    @Override
    public String toString() {
        return "ModeleJustificatif{" +
                "id=" + id +
                ", phaseLitige='" + phaseLitige + '\'' +
                ", motifChargeback='" + motifChargeback + '\'' +
                ", typeJustificatif='" + typeJustificatif + '\'' +
                ", obligatoire=" + obligatoire +
                ", dateCreation=" + dateCreation +
                '}';
    }
}