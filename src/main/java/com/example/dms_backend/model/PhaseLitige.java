package com.example.dms_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entité représentant les phases du workflow de chargeback
 * Table: PHASES_LITIGE
 */
@Entity
@Table(name = "PHASES_LITIGE")
public class PhaseLitige {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "phases_litige_seq")
    @SequenceGenerator(name = "phases_litige_seq", sequenceName = "SEQ_PHASES_LITIGE", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @NotBlank(message = "Le nom de la phase est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    @Column(name = "NOM", nullable = false, length = 50)
    private String nom;

    @NotNull(message = "L'ordre de séquence est obligatoire")
    @Column(name = "ORDRE_SEQUENCE", nullable = false)
    private Integer ordreSequence;

    @NotNull(message = "Le délai en jours par défaut est obligatoire")
    @Column(name = "DELAI_JOURS_DEFAUT", nullable = false)
    private Integer delaiJoursDefaut;

    @Column(name = "DESCRIPTION")
    @Lob
    private String description;

    @Column(name = "ACTIF", nullable = false)
    private Boolean actif = true;

    @CreationTimestamp
    @Column(name = "DATE_CREATION", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    // Constructeurs
    public PhaseLitige() {
    }

    public PhaseLitige(String nom, Integer ordreSequence, Integer delaiJoursDefaut) {
        this.nom = nom;
        this.ordreSequence = ordreSequence;
        this.delaiJoursDefaut = delaiJoursDefaut;
        this.actif = true;
    }

    public PhaseLitige(String nom, Integer ordreSequence, Integer delaiJoursDefaut, String description) {
        this(nom, ordreSequence, delaiJoursDefaut);
        this.description = description;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Integer getOrdreSequence() {
        return ordreSequence;
    }

    public void setOrdreSequence(Integer ordreSequence) {
        this.ordreSequence = ordreSequence;
    }

    public Integer getDelaiJoursDefaut() {
        return delaiJoursDefaut;
    }

    public void setDelaiJoursDefaut(Integer delaiJoursDefaut) {
        this.delaiJoursDefaut = delaiJoursDefaut;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getActif() {
        return actif;
    }

    public void setActif(Boolean actif) {
        this.actif = actif;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    // Méthodes utilitaires
    public boolean isActif() {
        return Boolean.TRUE.equals(actif);
    }

    public void activer() {
        this.actif = true;
    }

    public void desactiver() {
        this.actif = false;
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhaseLitige that = (PhaseLitige) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(nom, that.nom) &&
                Objects.equals(ordreSequence, that.ordreSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, ordreSequence);
    }

    @Override
    public String toString() {
        return "PhaseLitige{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", ordreSequence=" + ordreSequence +
                ", delaiJoursDefaut=" + delaiJoursDefaut +
                ", actif=" + actif +
                ", dateCreation=" + dateCreation +
                '}';
    }
}