package com.example.dms_backend.dto;

import com.example.dms_backend.model.EchangeLitige;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * DTO pour l'entité EchangeLitige
 * Représente les communications et échanges d'un litige chargeback
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EchangeLitigeDTO {

    @JsonProperty("id")
    private Long id;

    @NotNull(message = "L'ID du litige est obligatoire")
    @JsonProperty("litigeId")
    private Long litigeId;

    @NotBlank(message = "Le contenu de l'échange est obligatoire")
    @JsonProperty("contenu")
    private String contenu;

    @JsonProperty("auteurUtilisateurId")
    private Long auteurUtilisateurId;

    @JsonProperty("institutionId")
    private Long institutionId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateEchange")
    private LocalDateTime dateEchange;

    @Size(max = 50, message = "La phase de litige ne peut pas dépasser 50 caractères")
    @JsonProperty("phaseLitige")
    private String phaseLitige;

    @NotBlank(message = "Le type d'échange est obligatoire")
    @JsonProperty("typeEchange")
    private String typeEchange;

    @JsonProperty("pieceJointeJustificatifId")
    private Long pieceJointeJustificatifId;

    @JsonProperty("visible")
    private Boolean visible;

    @JsonProperty("luParAutrePartie")
    private Boolean luParAutrePartie;

    // Champs calculés pour l'affichage
    @JsonProperty("typeEchangeLibelle")
    private String typeEchangeLibelle;

    @JsonProperty("auteurNom")
    private String auteurNom;

    @JsonProperty("institutionNom")
    private String institutionNom;

    @JsonProperty("resume")
    private String resume;

    @JsonProperty("dateFormatee")
    private String dateFormatee;

    @JsonProperty("dateRelative")
    private String dateRelative;

    @JsonProperty("iconeType")
    private String iconeType;

    @JsonProperty("couleurType")
    private String couleurType;

    @JsonProperty("priorite")
    private String priorite;

    @JsonProperty("statutLecture")
    private String statutLecture;

    @JsonProperty("hasPieceJointe")
    private Boolean hasPieceJointe;

    @JsonProperty("pieceJointeNom")
    private String pieceJointeNom;

    @JsonProperty("contenuHtml")
    private String contenuHtml;

    @JsonProperty("taggedUsers")
    private String[] taggedUsers;

    // Énumération pour les types d'échange (correspondant à l'entité)
    public enum TypeEchange {
        MESSAGE("Message", "chat", "blue", "NORMALE"),
        ACTION("Action", "activity", "green", "MOYENNE"),
        ESCALADE("Escalade", "alert-triangle", "orange", "HAUTE"),
        DECISION("Décision", "check-circle", "purple", "CRITIQUE");

        private final String libelle;
        private final String icone;
        private final String couleur;
        private final String priorite;

        TypeEchange(String libelle, String icone, String couleur, String priorite) {
            this.libelle = libelle;
            this.icone = icone;
            this.couleur = couleur;
            this.priorite = priorite;
        }

        public String getLibelle() {
            return libelle;
        }

        public String getIcone() {
            return icone;
        }

        public String getCouleur() {
            return couleur;
        }

        public String getPriorite() {
            return priorite;
        }
    }

    // Constructeurs
    public EchangeLitigeDTO() {
        this.visible = true;
        this.luParAutrePartie = false;
    }

    public EchangeLitigeDTO(Long litigeId, String contenu, String typeEchange) {
        this();
        this.litigeId = litigeId;
        this.contenu = contenu;
        this.typeEchange = typeEchange;
        this.calculateDerivedFields();
    }

    public EchangeLitigeDTO(Long litigeId, String contenu, String typeEchange,
                            Long auteurUtilisateurId, Long institutionId) {
        this(litigeId, contenu, typeEchange);
        this.auteurUtilisateurId = auteurUtilisateurId;
        this.institutionId = institutionId;
    }

    public EchangeLitigeDTO(Long litigeId, String contenu, String typeEchange,
                            String phaseLitige, Long auteurUtilisateurId) {
        this(litigeId, contenu, typeEchange, auteurUtilisateurId, null);
        this.phaseLitige = phaseLitige;
    }

    public EchangeLitigeDTO(Long id, Long litigeId, String contenu, Long auteurUtilisateurId,
                            Long institutionId, LocalDateTime dateEchange, String phaseLitige, String typeEchange,
                            Long pieceJointeJustificatifId, Boolean visible, Boolean luParAutrePartie) {
        this.id = id;
        this.litigeId = litigeId;
        this.contenu = contenu;
        this.auteurUtilisateurId = auteurUtilisateurId;
        this.institutionId = institutionId;
        this.dateEchange = dateEchange;
        this.phaseLitige = phaseLitige;
        this.typeEchange = typeEchange;
        this.pieceJointeJustificatifId = pieceJointeJustificatifId;
        this.visible = visible;
        this.luParAutrePartie = luParAutrePartie;
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

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
        this.calculateDerivedFields();
    }

    public Long getAuteurUtilisateurId() {
        return auteurUtilisateurId;
    }

    public void setAuteurUtilisateurId(Long auteurUtilisateurId) {
        this.auteurUtilisateurId = auteurUtilisateurId;
    }

    public Long getInstitutionId() {
        return institutionId;
    }

    public void setInstitutionId(Long institutionId) {
        this.institutionId = institutionId;
    }

    public LocalDateTime getDateEchange() {
        return dateEchange;
    }

    public void setDateEchange(LocalDateTime dateEchange) {
        this.dateEchange = dateEchange;
        this.calculateDerivedFields();
    }

    public String getPhaseLitige() {
        return phaseLitige;
    }

    public void setPhaseLitige(String phaseLitige) {
        this.phaseLitige = phaseLitige;
    }

    public String getTypeEchange() {
        return typeEchange;
    }

    public void setTypeEchange(String typeEchange) {
        this.typeEchange = typeEchange;
        this.calculateDerivedFields();
    }

    public Long getPieceJointeJustificatifId() {
        return pieceJointeJustificatifId;
    }

    public void setPieceJointeJustificatifId(Long pieceJointeJustificatifId) {
        this.pieceJointeJustificatifId = pieceJointeJustificatifId;
        this.calculateDerivedFields();
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean getLuParAutrePartie() {
        return luParAutrePartie;
    }

    public void setLuParAutrePartie(Boolean luParAutrePartie) {
        this.luParAutrePartie = luParAutrePartie;
        this.calculateDerivedFields();
    }

    // Getters pour les champs calculés
    public String getTypeEchangeLibelle() {
        return typeEchangeLibelle;
    }

    public String getAuteurNom() {
        return auteurNom;
    }

    public void setAuteurNom(String auteurNom) {
        this.auteurNom = auteurNom;
    }

    public String getInstitutionNom() {
        return institutionNom;
    }

    public void setInstitutionNom(String institutionNom) {
        this.institutionNom = institutionNom;
    }

    public String getResume() {
        return resume;
    }

    public String getDateFormatee() {
        return dateFormatee;
    }

    public String getDateRelative() {
        return dateRelative;
    }

    public String getIconeType() {
        return iconeType;
    }

    public String getCouleurType() {
        return couleurType;
    }

    public String getPriorite() {
        return priorite;
    }

    public String getStatutLecture() {
        return statutLecture;
    }

    public Boolean getHasPieceJointe() {
        return hasPieceJointe;
    }

    public String getPieceJointeNom() {
        return pieceJointeNom;
    }

    public void setPieceJointeNom(String pieceJointeNom) {
        this.pieceJointeNom = pieceJointeNom;
    }

    public String getContenuHtml() {
        return contenuHtml;
    }

    public String[] getTaggedUsers() {
        return taggedUsers;
    }

    // Méthodes utilitaires
    public boolean isVisible() {
        return Boolean.TRUE.equals(visible);
    }

    public boolean isLuParAutrePartie() {
        return Boolean.TRUE.equals(luParAutrePartie);
    }

    public boolean hasPieceJointe() {
        return pieceJointeJustificatifId != null;
    }

    public boolean isMessage() {
        return "MESSAGE".equals(typeEchange);
    }

    public boolean isAction() {
        return "ACTION".equals(typeEchange);
    }

    public boolean isEscalade() {
        return "ESCALADE".equals(typeEchange);
    }

    public boolean isDecision() {
        return "DECISION".equals(typeEchange);
    }

    /**
     * Calcule les champs dérivés basés sur l'état actuel
     */
    private void calculateDerivedFields() {
        // Calcul du libellé du type
        this.typeEchangeLibelle = calculerTypeEchangeLibelle();

        // Calcul de l'icône et couleur
        this.iconeType = calculerIconeType();
        this.couleurType = calculerCouleurType();
        this.priorite = calculerPriorite();

        // Calcul du résumé
        this.resume = calculerResume();

        // Calcul des dates formatées
        this.dateFormatee = formaterDate();
        this.dateRelative = calculerDateRelative();

        // Calcul du statut de lecture
        this.statutLecture = calculerStatutLecture();

        // Calcul pièce jointe
        this.hasPieceJointe = pieceJointeJustificatifId != null;

        // Conversion HTML du contenu
        this.contenuHtml = convertirContenuEnHtml();

        // Extraction des utilisateurs taggés
        this.taggedUsers = extraireUtilisateursTagges();
    }

    private String calculerTypeEchangeLibelle() {
        if (typeEchange == null) {
            return "Non défini";
        }

        try {
            return TypeEchange.valueOf(typeEchange).getLibelle();
        } catch (IllegalArgumentException e) {
            return typeEchange;
        }
    }

    private String calculerIconeType() {
        if (typeEchange == null) {
            return "message-circle";
        }

        try {
            return TypeEchange.valueOf(typeEchange).getIcone();
        } catch (IllegalArgumentException e) {
            return "message-circle";
        }
    }

    private String calculerCouleurType() {
        if (typeEchange == null) {
            return "gray";
        }

        try {
            return TypeEchange.valueOf(typeEchange).getCouleur();
        } catch (IllegalArgumentException e) {
            return "gray";
        }
    }

    private String calculerPriorite() {
        if (typeEchange == null) {
            return "NORMALE";
        }

        try {
            return TypeEchange.valueOf(typeEchange).getPriorite();
        } catch (IllegalArgumentException e) {
            return "NORMALE";
        }
    }

    private String calculerResume() {
        if (contenu == null || contenu.trim().isEmpty()) {
            return "";
        }

        String contenuNettoye = contenu.trim().replaceAll("\\s+", " ");
        if (contenuNettoye.length() <= 100) {
            return contenuNettoye;
        }

        return contenuNettoye.substring(0, 97) + "...";
    }

    private String formaterDate() {
        if (dateEchange == null) {
            return "";
        }

        return dateEchange.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String calculerDateRelative() {
        if (dateEchange == null) {
            return "";
        }

        LocalDateTime maintenant = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateEchange, maintenant);
        long heures = ChronoUnit.HOURS.between(dateEchange, maintenant);
        long jours = ChronoUnit.DAYS.between(dateEchange, maintenant);

        if (minutes < 1) {
            return "À l'instant";
        } else if (minutes < 60) {
            return "Il y a " + minutes + " minute(s)";
        } else if (heures < 24) {
            return "Il y a " + heures + " heure(s)";
        } else if (jours < 7) {
            return "Il y a " + jours + " jour(s)";
        } else {
            return formaterDate();
        }
    }

    private String calculerStatutLecture() {
        if (Boolean.TRUE.equals(luParAutrePartie)) {
            return "Lu";
        } else {
            return "Non lu";
        }
    }

    private String convertirContenuEnHtml() {
        if (contenu == null) {
            return "";
        }

        // Conversion basique : remplace les retours à la ligne par <br>
        String html = contenu.replaceAll("\n", "<br>");

        // Détection de liens (basique)
        html = html.replaceAll("(https?://[\\w\\.-]+(?:/[\\w\\.-]*)*(?:\\?[\\w&=%.-]*)?)",
                "<a href=\"$1\" target=\"_blank\">$1</a>");

        // Détection d'emails
        html = html.replaceAll("([\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,})",
                "<a href=\"mailto:$1\">$1</a>");

        return html;
    }

    private String[] extraireUtilisateursTagges() {
        if (contenu == null) {
            return new String[0];
        }

        // Extraction basique des mentions @utilisateur
        return contenu.replaceAll("@(\\w+)", "$1")
                .lines()
                .filter(line -> line.startsWith("@"))
                .map(line -> line.substring(1))
                .distinct()
                .toArray(String[]::new);
    }

    /**
     * Vérifie si l'échange a été créé par un utilisateur spécifique
     */
    public boolean estCreePar(Long utilisateurId) {
        return Objects.equals(this.auteurUtilisateurId, utilisateurId);
    }

    /**
     * Vérifie si l'échange appartient à une institution spécifique
     */
    public boolean appartientA(Long institutionId) {
        return Objects.equals(this.institutionId, institutionId);
    }

    /**
     * Marque l'échange comme lu
     */
    public void marquerCommeLu() {
        this.luParAutrePartie = true;
        this.calculateDerivedFields();
    }

    /**
     * Convertit ce DTO en entité EchangeLitige
     */
    public EchangeLitige toEntity() {
        EchangeLitige entity = new EchangeLitige();
        entity.setId(this.id);
        entity.setLitigeId(this.litigeId);
        entity.setContenu(this.contenu);
        entity.setAuteurUtilisateurId(this.auteurUtilisateurId);
        entity.setInstitutionId(this.institutionId);
        entity.setDateEchange(this.dateEchange);
        entity.setPhaseLitige(this.phaseLitige);

        // Conversion du type d'échange
        if (this.typeEchange != null) {
            try {
                entity.setTypeEchange(EchangeLitige.TypeEchange.valueOf(this.typeEchange));
            } catch (IllegalArgumentException e) {
                // Valeur par défaut si le type n'est pas reconnu
                entity.setTypeEchange(EchangeLitige.TypeEchange.MESSAGE);
            }
        }

        entity.setPieceJointeJustificatifId(this.pieceJointeJustificatifId);
        entity.setVisible(this.visible);
        entity.setLuParAutrePartie(this.luParAutrePartie);
        return entity;
    }

    /**
     * Crée un DTO à partir d'une entité EchangeLitige
     */
    public static EchangeLitigeDTO fromEntity(EchangeLitige entity) {
        if (entity == null) {
            return null;
        }

        String typeEchangeStr = entity.getTypeEchange() != null ?
                entity.getTypeEchange().name() : null;

        return new EchangeLitigeDTO(
                entity.getId(),
                entity.getLitigeId(),
                entity.getContenu(),
                entity.getAuteurUtilisateurId(),
                entity.getInstitutionId(),
                entity.getDateEchange(),
                entity.getPhaseLitige(),
                typeEchangeStr,
                entity.getPieceJointeJustificatifId(),
                entity.getVisible(),
                entity.getLuParAutrePartie()
        );
    }

    /**
     * Crée un DTO enrichi avec les noms d'utilisateur et institution
     */
    public static EchangeLitigeDTO fromEntityWithNames(EchangeLitige entity,
                                                       String auteurNom, String institutionNom) {
        EchangeLitigeDTO dto = fromEntity(entity);
        if (dto != null) {
            dto.setAuteurNom(auteurNom);
            dto.setInstitutionNom(institutionNom);
        }
        return dto;
    }

    /**
     * Crée un DTO minimal pour les listes
     */
    public static EchangeLitigeDTO minimal(EchangeLitige entity) {
        if (entity == null) {
            return null;
        }

        EchangeLitigeDTO dto = new EchangeLitigeDTO();
        dto.setId(entity.getId());
        dto.setContenu(entity.getContenu());
        dto.setDateEchange(entity.getDateEchange());
        dto.setTypeEchange(entity.getTypeEchange() != null ? entity.getTypeEchange().name() : null);
        dto.setLuParAutrePartie(entity.getLuParAutrePartie());
        return dto;
    }

    /**
     * Met à jour une entité existante avec les données de ce DTO
     */
    public void updateEntity(EchangeLitige entity) {
        if (entity != null) {
            entity.setContenu(this.contenu);
            entity.setPhaseLitige(this.phaseLitige);
            entity.setPieceJointeJustificatifId(this.pieceJointeJustificatifId);
            entity.setVisible(this.visible);
            entity.setLuParAutrePartie(this.luParAutrePartie);
        }
    }

    /**
     * Vérifie si ce DTO contient les données minimales requises
     */
    public boolean isValid() {
        return litigeId != null &&
                contenu != null && !contenu.trim().isEmpty() &&
                typeEchange != null && !typeEchange.trim().isEmpty();
    }

    /**
     * Retourne une signature unique pour cet échange
     */
    public String getSignature() {
        return String.format("%s_%s_%s",
                litigeId,
                dateEchange != null ? dateEchange.toString() : "null",
                auteurUtilisateurId);
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EchangeLitigeDTO that = (EchangeLitigeDTO) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(litigeId, that.litigeId) &&
                Objects.equals(dateEchange, that.dateEchange);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, litigeId, dateEchange);
    }

    @Override
    public String toString() {
        return "EchangeLitigeDTO{" +
                "id=" + id +
                ", litigeId=" + litigeId +
                ", typeEchange='" + typeEchange + '\'' +
                ", phaseLitige='" + phaseLitige + '\'' +
                ", auteurUtilisateurId=" + auteurUtilisateurId +
                ", dateEchange=" + dateEchange +
                ", resume='" + resume + '\'' +
                ", luParAutrePartie=" + luParAutrePartie +
                '}';
    }
}