package com.example.dms_backend.dto;

import com.example.dms_backend.model.ModeleJustificatif;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO pour l'entité ModeleJustificatif
 * Utilisé pour définir les documents requis par phase et motif de chargeback
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ModeleJustificatifDTO {

    @JsonProperty("id")
    private Long id;

    @NotBlank(message = "La phase de litige est obligatoire")
    @Size(max = 50, message = "La phase ne peut pas dépasser 50 caractères")
    @JsonProperty("phaseLitige")
    private String phaseLitige;

    @NotBlank(message = "Le motif de chargeback est obligatoire")
    @Size(max = 50, message = "Le motif ne peut pas dépasser 50 caractères")
    @JsonProperty("motifChargeback")
    private String motifChargeback;

    @NotBlank(message = "Le type de justificatif est obligatoire")
    @Size(max = 50, message = "Le type ne peut pas dépasser 50 caractères")
    @JsonProperty("typeJustificatif")
    private String typeJustificatif;

    @JsonProperty("obligatoire")
    private Boolean obligatoire;

    @JsonProperty("description")
    private String description;

    @Size(max = 255, message = "L'exemple de nom de fichier ne peut pas dépasser 255 caractères")
    @JsonProperty("exempleNomFichier")
    private String exempleNomFichier;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateCreation")
    private LocalDateTime dateCreation;

    // Champs calculés pour l'affichage
    @JsonProperty("cleUnique")
    private String cleUnique;

    @JsonProperty("libellePriorite")
    private String libellePriorite;

    // Constructeurs
    public ModeleJustificatifDTO() {
    }

    public ModeleJustificatifDTO(String phaseLitige, String motifChargeback, String typeJustificatif) {
        this.phaseLitige = phaseLitige;
        this.motifChargeback = motifChargeback;
        this.typeJustificatif = typeJustificatif;
        this.obligatoire = false;
    }

    public ModeleJustificatifDTO(String phaseLitige, String motifChargeback, String typeJustificatif, Boolean obligatoire) {
        this(phaseLitige, motifChargeback, typeJustificatif);
        this.obligatoire = obligatoire;
    }

    public ModeleJustificatifDTO(String phaseLitige, String motifChargeback, String typeJustificatif,
                                 Boolean obligatoire, String description) {
        this(phaseLitige, motifChargeback, typeJustificatif, obligatoire);
        this.description = description;
    }

    public ModeleJustificatifDTO(Long id, String phaseLitige, String motifChargeback, String typeJustificatif,
                                 Boolean obligatoire, String description, String exempleNomFichier,
                                 LocalDateTime dateCreation) {
        this.id = id;
        this.phaseLitige = phaseLitige;
        this.motifChargeback = motifChargeback;
        this.typeJustificatif = typeJustificatif;
        this.obligatoire = obligatoire;
        this.description = description;
        this.exempleNomFichier = exempleNomFichier;
        this.dateCreation = dateCreation;
        this.cleUnique = genererCleUnique();
        this.libellePriorite = genererLibellePriorite();
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
        this.cleUnique = genererCleUnique();
    }

    public String getMotifChargeback() {
        return motifChargeback;
    }

    public void setMotifChargeback(String motifChargeback) {
        this.motifChargeback = motifChargeback;
        this.cleUnique = genererCleUnique();
    }

    public String getTypeJustificatif() {
        return typeJustificatif;
    }

    public void setTypeJustificatif(String typeJustificatif) {
        this.typeJustificatif = typeJustificatif;
        this.cleUnique = genererCleUnique();
        this.libellePriorite = genererLibellePriorite();
    }

    public Boolean getObligatoire() {
        return obligatoire;
    }

    public void setObligatoire(Boolean obligatoire) {
        this.obligatoire = obligatoire;
        this.libellePriorite = genererLibellePriorite();
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

    public String getCleUnique() {
        if (cleUnique == null) {
            cleUnique = genererCleUnique();
        }
        return cleUnique;
    }

    public void setCleUnique(String cleUnique) {
        this.cleUnique = cleUnique;
    }

    public String getLibellePriorite() {
        if (libellePriorite == null) {
            libellePriorite = genererLibellePriorite();
        }
        return libellePriorite;
    }

    public void setLibellePriorite(String libellePriorite) {
        this.libellePriorite = libellePriorite;
    }

    // Méthodes utilitaires
    public boolean isObligatoire() {
        return Boolean.TRUE.equals(obligatoire);
    }

    /**
     * Génère une clé unique pour ce modèle
     */
    private String genererCleUnique() {
        if (phaseLitige != null && motifChargeback != null && typeJustificatif != null) {
            return String.format("%s_%s_%s", phaseLitige, motifChargeback, typeJustificatif);
        }
        return null;
    }

    /**
     * Génère un libellé de priorité lisible
     */
    private String genererLibellePriorite() {
        if (obligatoire == null) {
            return "Non défini";
        }
        return Boolean.TRUE.equals(obligatoire) ? "Obligatoire" : "Optionnel";
    }

    /**
     * Vérifie si ce modèle correspond à une phase et un motif donnés
     */
    public boolean correspondA(String phase, String motif) {
        return Objects.equals(this.phaseLitige, phase) &&
                Objects.equals(this.motifChargeback, motif);
    }

    /**
     * Convertit ce DTO en entité ModeleJustificatif
     */
    public ModeleJustificatif toEntity() {
        ModeleJustificatif entity = new ModeleJustificatif();
        entity.setId(this.id);
        entity.setPhaseLitige(this.phaseLitige);
        entity.setMotifChargeback(this.motifChargeback);
        entity.setTypeJustificatif(this.typeJustificatif);
        entity.setObligatoire(this.obligatoire);
        entity.setDescription(this.description);
        entity.setExempleNomFichier(this.exempleNomFichier);
        entity.setDateCreation(this.dateCreation);
        return entity;
    }

    /**
     * Crée un DTO à partir d'une entité ModeleJustificatif
     */
    public static ModeleJustificatifDTO fromEntity(ModeleJustificatif entity) {
        if (entity == null) {
            return null;
        }

        return new ModeleJustificatifDTO(
                entity.getId(),
                entity.getPhaseLitige(),
                entity.getMotifChargeback(),
                entity.getTypeJustificatif(),
                entity.getObligatoire(),
                entity.getDescription(),
                entity.getExempleNomFichier(),
                entity.getDateCreation()
        );
    }

    /**
     * Met à jour une entité existante avec les données de ce DTO
     */
    public void updateEntity(ModeleJustificatif entity) {
        if (entity != null) {
            entity.setPhaseLitige(this.phaseLitige);
            entity.setMotifChargeback(this.motifChargeback);
            entity.setTypeJustificatif(this.typeJustificatif);
            entity.setObligatoire(this.obligatoire);
            entity.setDescription(this.description);
            entity.setExempleNomFichier(this.exempleNomFichier);
        }
    }

    /**
     * Crée un DTO minimal avec seulement les informations essentielles
     */
    public static ModeleJustificatifDTO minimal(ModeleJustificatif entity) {
        if (entity == null) {
            return null;
        }

        ModeleJustificatifDTO dto = new ModeleJustificatifDTO();
        dto.setId(entity.getId());
        dto.setPhaseLitige(entity.getPhaseLitige());
        dto.setMotifChargeback(entity.getMotifChargeback());
        dto.setTypeJustificatif(entity.getTypeJustificatif());
        dto.setObligatoire(entity.getObligatoire());
        return dto;
    }

    /**
     * Crée un DTO pour une liste de sélection
     */
    public static ModeleJustificatifDTO forSelection(ModeleJustificatif entity) {
        if (entity == null) {
            return null;
        }

        ModeleJustificatifDTO dto = new ModeleJustificatifDTO();
        dto.setId(entity.getId());
        dto.setTypeJustificatif(entity.getTypeJustificatif());
        dto.setObligatoire(entity.getObligatoire());
        dto.setDescription(entity.getDescription());
        return dto;
    }

    /**
     * Vérifie si ce DTO contient les données minimales requises
     */
    public boolean isValid() {
        return phaseLitige != null && !phaseLitige.trim().isEmpty() &&
                motifChargeback != null && !motifChargeback.trim().isEmpty() &&
                typeJustificatif != null && !typeJustificatif.trim().isEmpty();
    }

    /**
     * Retourne une représentation courte pour les logs
     */
    public String toShortString() {
        return String.format("ModeleJustificatif[%d: %s/%s/%s (%s)]",
                id, phaseLitige, motifChargeback, typeJustificatif,
                isObligatoire() ? "Obligatoire" : "Optionnel");
    }

    /**
     * Retourne un nom d'affichage formaté
     */
    public String getNomAffichage() {
        StringBuilder sb = new StringBuilder();
        sb.append(typeJustificatif);

        if (Boolean.TRUE.equals(obligatoire)) {
            sb.append(" (*)");
        }

        return sb.toString();
    }

    /**
     * Retourne les formats de fichiers acceptés basés sur le type
     */
    public String getFormatsAcceptes() {
        if (typeJustificatif == null) {
            return "Tous formats";
        }

        String type = typeJustificatif.toLowerCase();

        if (type.contains("facture") || type.contains("recu")) {
            return "PDF, JPG, PNG";
        } else if (type.contains("autorisation") || type.contains("signature")) {
            return "PDF uniquement";
        } else if (type.contains("photo") || type.contains("capture")) {
            return "JPG, PNG, GIF";
        } else {
            return "PDF, JPG, PNG, DOC, DOCX";
        }
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModeleJustificatifDTO that = (ModeleJustificatifDTO) o;
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
        return "ModeleJustificatifDTO{" +
                "id=" + id +
                ", phaseLitige='" + phaseLitige + '\'' +
                ", motifChargeback='" + motifChargeback + '\'' +
                ", typeJustificatif='" + typeJustificatif + '\'' +
                ", obligatoire=" + obligatoire +
                ", description='" + description + '\'' +
                ", exempleNomFichier='" + exempleNomFichier + '\'' +
                ", dateCreation=" + dateCreation +
                '}';
    }
}