package com.example.dms_backend.dto;

import com.example.dms_backend.model.PhaseLitige;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO pour l'entité PhaseLitige
 * Utilisé pour les transferts de données via l'API REST
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PhaseLitigeDTO {

    @JsonProperty("id")
    private Long id;

    @NotBlank(message = "Le nom de la phase est obligatoire")
    @Size(max = 50, message = "Le nom ne peut pas dépasser 50 caractères")
    @JsonProperty("nom")
    private String nom;

    @NotNull(message = "L'ordre de séquence est obligatoire")
    @Positive(message = "L'ordre de séquence doit être positif")
    @JsonProperty("ordreSequence")
    private Integer ordreSequence;

    @NotNull(message = "Le délai en jours par défaut est obligatoire")
    @Positive(message = "Le délai doit être positif")
    @JsonProperty("delaiJoursDefaut")
    private Integer delaiJoursDefaut;

    @JsonProperty("description")
    private String description;

    @JsonProperty("actif")
    private Boolean actif;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateCreation")
    private LocalDateTime dateCreation;

    // Constructeurs
    public PhaseLitigeDTO() {
    }

    public PhaseLitigeDTO(String nom, Integer ordreSequence, Integer delaiJoursDefaut) {
        this.nom = nom;
        this.ordreSequence = ordreSequence;
        this.delaiJoursDefaut = delaiJoursDefaut;
        this.actif = true;
    }

    public PhaseLitigeDTO(String nom, Integer ordreSequence, Integer delaiJoursDefaut, String description) {
        this(nom, ordreSequence, delaiJoursDefaut);
        this.description = description;
    }

    public PhaseLitigeDTO(Long id, String nom, Integer ordreSequence, Integer delaiJoursDefaut,
                          String description, Boolean actif, LocalDateTime dateCreation) {
        this.id = id;
        this.nom = nom;
        this.ordreSequence = ordreSequence;
        this.delaiJoursDefaut = delaiJoursDefaut;
        this.description = description;
        this.actif = actif;
        this.dateCreation = dateCreation;
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

    /**
     * Convertit ce DTO en entité PhaseLitige
     */
    public PhaseLitige toEntity() {
        PhaseLitige entity = new PhaseLitige();
        entity.setId(this.id);
        entity.setNom(this.nom);
        entity.setOrdreSequence(this.ordreSequence);
        entity.setDelaiJoursDefaut(this.delaiJoursDefaut);
        entity.setDescription(this.description);
        entity.setActif(this.actif);
        entity.setDateCreation(this.dateCreation);
        return entity;
    }

    /**
     * Crée un DTO à partir d'une entité PhaseLitige
     */
    public static PhaseLitigeDTO fromEntity(PhaseLitige entity) {
        if (entity == null) {
            return null;
        }

        return new PhaseLitigeDTO(
                entity.getId(),
                entity.getNom(),
                entity.getOrdreSequence(),
                entity.getDelaiJoursDefaut(),
                entity.getDescription(),
                entity.getActif(),
                entity.getDateCreation()
        );
    }

    /**
     * Met à jour une entité existante avec les données de ce DTO
     */
    public void updateEntity(PhaseLitige entity) {
        if (entity != null) {
            entity.setNom(this.nom);
            entity.setOrdreSequence(this.ordreSequence);
            entity.setDelaiJoursDefaut(this.delaiJoursDefaut);
            entity.setDescription(this.description);

            if (this.actif != null) {
                entity.setActif(this.actif);
            }
        }
    }

    /**
     * Crée un DTO minimal avec seulement les informations essentielles
     */
    public static PhaseLitigeDTO minimal(PhaseLitige entity) {
        if (entity == null) {
            return null;
        }

        PhaseLitigeDTO dto = new PhaseLitigeDTO();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setOrdreSequence(entity.getOrdreSequence());
        dto.setActif(entity.getActif());
        return dto;
    }

    /**
     * Vérifie si ce DTO contient les données minimales requises
     */
    public boolean isValid() {
        return nom != null && !nom.trim().isEmpty() &&
                ordreSequence != null && ordreSequence > 0 &&
                delaiJoursDefaut != null && delaiJoursDefaut > 0;
    }

    /**
     * Retourne une représentation courte pour les logs
     */
    public String toShortString() {
        return String.format("PhaseLitige[%d: %s (ordre: %d)]",
                id, nom, ordreSequence);
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhaseLitigeDTO that = (PhaseLitigeDTO) o;
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
        return "PhaseLitigeDTO{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", ordreSequence=" + ordreSequence +
                ", delaiJoursDefaut=" + delaiJoursDefaut +
                ", description='" + description + '\'' +
                ", actif=" + actif +
                ", dateCreation=" + dateCreation +
                '}';
    }
}