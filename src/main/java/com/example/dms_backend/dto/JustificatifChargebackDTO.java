package com.example.dms_backend.dto;

import com.example.dms_backend.model.JustificatifChargeback;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO pour l'entité JustificatifChargeback
 * Représente les documents et justificatifs d'un chargeback
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JustificatifChargebackDTO {

    @JsonProperty("id")
    private Long id;

    @NotNull(message = "L'ID du litige est obligatoire")
    @JsonProperty("litigeId")
    private Long litigeId;

    @NotBlank(message = "Le nom du fichier est obligatoire")
    @Size(max = 255, message = "Le nom du fichier ne peut pas dépasser 255 caractères")
    @JsonProperty("nomFichier")
    private String nomFichier;

    @NotBlank(message = "Le type de justificatif est obligatoire")
    @Size(max = 50, message = "Le type de justificatif ne peut pas dépasser 50 caractères")
    @JsonProperty("typeJustificatif")
    private String typeJustificatif;

    @NotBlank(message = "La phase de litige est obligatoire")
    @Size(max = 50, message = "La phase de litige ne peut pas dépasser 50 caractères")
    @JsonProperty("phaseLitige")
    private String phaseLitige;

    @NotBlank(message = "Le chemin du fichier est obligatoire")
    @Size(max = 500, message = "Le chemin du fichier ne peut pas dépasser 500 caractères")
    @JsonProperty("cheminFichier")
    private String cheminFichier;

    @Min(value = 0, message = "La taille du fichier ne peut pas être négative")
    @JsonProperty("tailleFichier")
    private Long tailleFichier;

    @Size(max = 10, message = "Le format du fichier ne peut pas dépasser 10 caractères")
    @JsonProperty("formatFichier")
    private String formatFichier;

    @JsonProperty("transmisParUtilisateurId")
    private Long transmisParUtilisateurId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateAjout")
    private LocalDateTime dateAjout;

    @JsonProperty("valide")
    private Boolean valide;

    @JsonProperty("commentaires")
    private String commentaires;

    @JsonProperty("visiblePourAutrePartie")
    private Boolean visiblePourAutrePartie;

    // Champs calculés pour l'affichage
    @JsonProperty("tailleFormatee")
    private String tailleFormatee;

    @JsonProperty("extension")
    private String extension;

    @JsonProperty("typeDocument")
    private String typeDocument;

    @JsonProperty("statutValidation")
    private String statutValidation;

    @JsonProperty("iconeDocument")
    private String iconeDocument;

    @JsonProperty("couleurStatut")
    private String couleurStatut;

    @JsonProperty("urlPrevisualisation")
    private String urlPrevisualisation;

    @JsonProperty("urlTelechargement")
    private String urlTelechargement;

    @JsonProperty("obligatoire")
    private Boolean obligatoire;

    @JsonProperty("transmisParNom")
    private String transmisParNom;

    // Constructeurs
    public JustificatifChargebackDTO() {
        this.valide = false;
        this.visiblePourAutrePartie = true;
    }

    public JustificatifChargebackDTO(Long litigeId, String nomFichier, String typeJustificatif,
                                     String phaseLitige, String cheminFichier) {
        this();
        this.litigeId = litigeId;
        this.nomFichier = nomFichier;
        this.typeJustificatif = typeJustificatif;
        this.phaseLitige = phaseLitige;
        this.cheminFichier = cheminFichier;
        this.calculateDerivedFields();
    }

    public JustificatifChargebackDTO(Long litigeId, String nomFichier, String typeJustificatif,
                                     String phaseLitige, String cheminFichier, Long transmisParUtilisateurId) {
        this(litigeId, nomFichier, typeJustificatif, phaseLitige, cheminFichier);
        this.transmisParUtilisateurId = transmisParUtilisateurId;
    }

    public JustificatifChargebackDTO(Long id, Long litigeId, String nomFichier, String typeJustificatif,
                                     String phaseLitige, String cheminFichier, Long tailleFichier, String formatFichier,
                                     Long transmisParUtilisateurId, LocalDateTime dateAjout, Boolean valide,
                                     String commentaires, Boolean visiblePourAutrePartie) {
        this.id = id;
        this.litigeId = litigeId;
        this.nomFichier = nomFichier;
        this.typeJustificatif = typeJustificatif;
        this.phaseLitige = phaseLitige;
        this.cheminFichier = cheminFichier;
        this.tailleFichier = tailleFichier;
        this.formatFichier = formatFichier;
        this.transmisParUtilisateurId = transmisParUtilisateurId;
        this.dateAjout = dateAjout;
        this.valide = valide;
        this.commentaires = commentaires;
        this.visiblePourAutrePartie = visiblePourAutrePartie;
        this.calculateDerivedFields();
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

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
        this.calculateDerivedFields();
    }

    public String getTypeJustificatif() {
        return typeJustificatif;
    }

    public void setTypeJustificatif(String typeJustificatif) {
        this.typeJustificatif = typeJustificatif;
        this.calculateDerivedFields();
    }

    public String getPhaseLitige() {
        return phaseLitige;
    }

    public void setPhaseLitige(String phaseLitige) {
        this.phaseLitige = phaseLitige;
    }

    public String getCheminFichier() {
        return cheminFichier;
    }

    public void setCheminFichier(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }

    public Long getTailleFichier() {
        return tailleFichier;
    }

    public void setTailleFichier(Long tailleFichier) {
        this.tailleFichier = tailleFichier;
        this.calculateDerivedFields();
    }

    public String getFormatFichier() {
        return formatFichier;
    }

    public void setFormatFichier(String formatFichier) {
        this.formatFichier = formatFichier;
        this.calculateDerivedFields();
    }

    public Long getTransmisParUtilisateurId() {
        return transmisParUtilisateurId;
    }

    public void setTransmisParUtilisateurId(Long transmisParUtilisateurId) {
        this.transmisParUtilisateurId = transmisParUtilisateurId;
    }

    public LocalDateTime getDateAjout() {
        return dateAjout;
    }

    public void setDateAjout(LocalDateTime dateAjout) {
        this.dateAjout = dateAjout;
    }

    public Boolean getValide() {
        return valide;
    }

    public void setValide(Boolean valide) {
        this.valide = valide;
        this.calculateDerivedFields();
    }

    public String getCommentaires() {
        return commentaires;
    }

    public void setCommentaires(String commentaires) {
        this.commentaires = commentaires;
    }

    public Boolean getVisiblePourAutrePartie() {
        return visiblePourAutrePartie;
    }

    public void setVisiblePourAutrePartie(Boolean visiblePourAutrePartie) {
        this.visiblePourAutrePartie = visiblePourAutrePartie;
    }

    // Getters pour les champs calculés
    public String getTailleFormatee() {
        return tailleFormatee;
    }

    public String getExtension() {
        return extension;
    }

    public String getTypeDocument() {
        return typeDocument;
    }

    public String getStatutValidation() {
        return statutValidation;
    }

    public String getIconeDocument() {
        return iconeDocument;
    }

    public String getCouleurStatut() {
        return couleurStatut;
    }

    public String getUrlPrevisualisation() {
        return urlPrevisualisation;
    }

    public void setUrlPrevisualisation(String urlPrevisualisation) {
        this.urlPrevisualisation = urlPrevisualisation;
    }

    public String getUrlTelechargement() {
        return urlTelechargement;
    }

    public void setUrlTelechargement(String urlTelechargement) {
        this.urlTelechargement = urlTelechargement;
    }

    public Boolean getObligatoire() {
        return obligatoire;
    }

    public void setObligatoire(Boolean obligatoire) {
        this.obligatoire = obligatoire;
    }

    public String getTransmisParNom() {
        return transmisParNom;
    }

    public void setTransmisParNom(String transmisParNom) {
        this.transmisParNom = transmisParNom;
    }

    // Méthodes utilitaires
    public boolean isValide() {
        return Boolean.TRUE.equals(valide);
    }

    public boolean isVisiblePourAutrePartie() {
        return Boolean.TRUE.equals(visiblePourAutrePartie);
    }

    public boolean isObligatoire() {
        return Boolean.TRUE.equals(obligatoire);
    }

    /**
     * Calcule les champs dérivés basés sur l'état actuel
     */
    private void calculateDerivedFields() {
        // Calcul de l'extension
        this.extension = calculerExtension();

        // Calcul du type de document
        this.typeDocument = determinerTypeDocument();

        // Calcul de la taille formatée
        this.tailleFormatee = formaterTaille();

        // Calcul du statut de validation
        this.statutValidation = determinerStatutValidation();

        // Calcul de l'icône
        this.iconeDocument = determinerIcone();

        // Calcul de la couleur du statut
        this.couleurStatut = determinerCouleurStatut();
    }

    private String calculerExtension() {
        if (nomFichier != null && nomFichier.contains(".")) {
            return nomFichier.substring(nomFichier.lastIndexOf(".") + 1).toLowerCase();
        }
        return formatFichier != null ? formatFichier.toLowerCase() : "";
    }

    private String determinerTypeDocument() {
        String ext = extension != null ? extension.toLowerCase() : "";

        if ("jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext) ||
                "gif".equals(ext) || "bmp".equals(ext) || "webp".equals(ext)) {
            return "Image";
        } else if ("pdf".equals(ext)) {
            return "PDF";
        } else if ("doc".equals(ext) || "docx".equals(ext)) {
            return "Document Word";
        } else if ("xls".equals(ext) || "xlsx".equals(ext)) {
            return "Tableur Excel";
        } else if ("ppt".equals(ext) || "pptx".equals(ext)) {
            return "Présentation PowerPoint";
        } else if ("txt".equals(ext)) {
            return "Fichier texte";
        } else if ("zip".equals(ext) || "rar".equals(ext) || "7z".equals(ext)) {
            return "Archive";
        } else {
            return "Document";
        }
    }

    private String formaterTaille() {
        if (tailleFichier == null) {
            return "Inconnue";
        }

        if (tailleFichier < 1024) {
            return tailleFichier + " B";
        } else if (tailleFichier < 1024 * 1024) {
            return String.format("%.1f KB", tailleFichier / 1024.0);
        } else if (tailleFichier < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", tailleFichier / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", tailleFichier / (1024.0 * 1024.0 * 1024.0));
        }
    }

    private String determinerStatutValidation() {
        if (valide == null) {
            return "En attente";
        } else if (Boolean.TRUE.equals(valide)) {
            return "Validé";
        } else {
            return "Rejeté";
        }
    }

    private String determinerIcone() {
        String ext = extension != null ? extension.toLowerCase() : "";

        switch (ext) {
            case "pdf":
                return "file-pdf";
            case "doc":
            case "docx":
                return "file-word";
            case "xls":
            case "xlsx":
                return "file-excel";
            case "ppt":
            case "pptx":
                return "file-powerpoint";
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
            case "bmp":
            case "webp":
                return "file-image";
            case "zip":
            case "rar":
            case "7z":
                return "file-archive";
            case "txt":
                return "file-text";
            default:
                return "file";
        }
    }

    private String determinerCouleurStatut() {
        if (valide == null) {
            return "orange"; // En attente
        } else if (Boolean.TRUE.equals(valide)) {
            return "green"; // Validé
        } else {
            return "red"; // Rejeté
        }
    }

    /**
     * Vérifie si le fichier est une image
     */
    public boolean isImage() {
        String ext = extension != null ? extension.toLowerCase() : "";
        return "jpg".equals(ext) || "jpeg".equals(ext) || "png".equals(ext) ||
                "gif".equals(ext) || "bmp".equals(ext) || "webp".equals(ext);
    }

    /**
     * Vérifie si le fichier est un PDF
     */
    public boolean isPdf() {
        return "pdf".equals(extension);
    }

    /**
     * Vérifie si le fichier est un document Office
     */
    public boolean isDocumentOffice() {
        String ext = extension != null ? extension.toLowerCase() : "";
        return "doc".equals(ext) || "docx".equals(ext) ||
                "xls".equals(ext) || "xlsx".equals(ext) ||
                "ppt".equals(ext) || "pptx".equals(ext);
    }

    /**
     * Vérifie si le fichier peut être prévisualisé
     */
    public boolean peutEtrePrevisualisé() {
        return isImage() || isPdf();
    }

    /**
     * Retourne les formats acceptés pour ce type de justificatif
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

    /**
     * Convertit ce DTO en entité JustificatifChargeback
     */
    public JustificatifChargeback toEntity() {
        JustificatifChargeback entity = new JustificatifChargeback();
        entity.setId(this.id);
        entity.setLitigeId(this.litigeId);
        entity.setNomFichier(this.nomFichier);
        entity.setTypeJustificatif(this.typeJustificatif);
        entity.setPhaseLitige(this.phaseLitige);
        entity.setCheminFichier(this.cheminFichier);
        entity.setTailleFichier(this.tailleFichier);
        entity.setFormatFichier(this.formatFichier);
        entity.setTransmisParUtilisateurId(this.transmisParUtilisateurId);
        entity.setDateAjout(this.dateAjout);
        entity.setValide(this.valide);
        entity.setCommentaires(this.commentaires);
        entity.setVisiblePourAutrePartie(this.visiblePourAutrePartie);
        return entity;
    }

    /**
     * Crée un DTO à partir d'une entité JustificatifChargeback
     */
    public static JustificatifChargebackDTO fromEntity(JustificatifChargeback entity) {
        if (entity == null) {
            return null;
        }

        return new JustificatifChargebackDTO(
                entity.getId(),
                entity.getLitigeId(),
                entity.getNomFichier(),
                entity.getTypeJustificatif(),
                entity.getPhaseLitige(),
                entity.getCheminFichier(),
                entity.getTailleFichier(),
                entity.getFormatFichier(),
                entity.getTransmisParUtilisateurId(),
                entity.getDateAjout(),
                entity.getValide(),
                entity.getCommentaires(),
                entity.getVisiblePourAutrePartie()
        );
    }

    /**
     * Crée un DTO avec URLs de téléchargement/prévisualisation
     */
    public static JustificatifChargebackDTO fromEntityWithUrls(JustificatifChargeback entity,
                                                               String baseUrl) {
        JustificatifChargebackDTO dto = fromEntity(entity);
        if (dto != null && baseUrl != null) {
            dto.setUrlTelechargement(baseUrl + "/download/" + entity.getId());
            if (dto.peutEtrePrevisualisé()) {
                dto.setUrlPrevisualisation(baseUrl + "/preview/" + entity.getId());
            }
        }
        return dto;
    }

    /**
     * Crée un DTO minimal pour les listes
     */
    public static JustificatifChargebackDTO minimal(JustificatifChargeback entity) {
        if (entity == null) {
            return null;
        }

        JustificatifChargebackDTO dto = new JustificatifChargebackDTO();
        dto.setId(entity.getId());
        dto.setNomFichier(entity.getNomFichier());
        dto.setTypeJustificatif(entity.getTypeJustificatif());
        dto.setPhaseLitige(entity.getPhaseLitige());
        dto.setValide(entity.getValide());
        dto.setDateAjout(entity.getDateAjout());
        return dto;
    }

    /**
     * Met à jour une entité existante avec les données de ce DTO
     */
    public void updateEntity(JustificatifChargeback entity) {
        if (entity != null) {
            entity.setNomFichier(this.nomFichier);
            entity.setTypeJustificatif(this.typeJustificatif);
            entity.setPhaseLitige(this.phaseLitige);
            entity.setTailleFichier(this.tailleFichier);
            entity.setFormatFichier(this.formatFichier);
            entity.setValide(this.valide);
            entity.setCommentaires(this.commentaires);
            entity.setVisiblePourAutrePartie(this.visiblePourAutrePartie);
        }
    }

    /**
     * Vérifie si ce DTO contient les données minimales requises
     */
    public boolean isValid() {
        return litigeId != null &&
                nomFichier != null && !nomFichier.trim().isEmpty() &&
                typeJustificatif != null && !typeJustificatif.trim().isEmpty() &&
                phaseLitige != null && !phaseLitige.trim().isEmpty() &&
                cheminFichier != null && !cheminFichier.trim().isEmpty();
    }

    /**
     * Retourne un résumé pour l'affichage
     */
    public String getResume() {
        StringBuilder sb = new StringBuilder();
        sb.append(nomFichier);

        if (tailleFormatee != null) {
            sb.append(" (").append(tailleFormatee).append(")");
        }

        sb.append(" - ").append(statutValidation);

        return sb.toString();
    }

    /**
     * Retourne une description complète
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(typeJustificatif).append(" : ").append(nomFichier);

        if (typeDocument != null) {
            sb.append(" (").append(typeDocument).append(")");
        }

        return sb.toString();
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JustificatifChargebackDTO that = (JustificatifChargebackDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(litigeId, that.litigeId) &&
                Objects.equals(nomFichier, that.nomFichier) &&
                Objects.equals(cheminFichier, that.cheminFichier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, litigeId, nomFichier, cheminFichier);
    }

    @Override
    public String toString() {
        return "JustificatifChargebackDTO{" +
                "id=" + id +
                ", litigeId=" + litigeId +
                ", nomFichier='" + nomFichier + '\'' +
                ", typeJustificatif='" + typeJustificatif + '\'' +
                ", phaseLitige='" + phaseLitige + '\'' +
                ", valide=" + valide +
                ", tailleFormatee='" + tailleFormatee + '\'' +
                ", typeDocument='" + typeDocument + '\'' +
                ", statutValidation='" + statutValidation + '\'' +
                '}';
    }
}