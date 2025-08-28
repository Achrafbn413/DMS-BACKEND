package com.example.dms_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entité représentant les échanges et communications d'un litige
 * Table: ECHANGES_LITIGE
 */
@Entity
@Table(name = "ECHANGES_LITIGE",
        indexes = {
                @Index(name = "IDX_ECHANGES_LITIGE", columnList = "LITIGE_ID"),
                @Index(name = "IDX_ECHANGES_DATE", columnList = "DATE_ECHANGE"),
                @Index(name = "IDX_ECHANGES_TYPE", columnList = "TYPE_ECHANGE"),
                @Index(name = "IDX_ECHANGES_AUTEUR", columnList = "AUTEUR_UTILISATEUR_ID"),
                @Index(name = "IDX_ECHANGES_PHASE", columnList = "PHASE_LITIGE")
        })
public class EchangeLitige {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "echanges_litige_seq")
    @SequenceGenerator(name = "echanges_litige_seq", sequenceName = "SEQ_ECHANGES_LITIGE", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @NotNull(message = "L'ID du litige est obligatoire")
    @Column(name = "LITIGE_ID", nullable = false)
    private Long litigeId;

    @NotBlank(message = "Le contenu de l'échange est obligatoire")
    @Column(name = "CONTENU", nullable = false)
    @Lob
    private String contenu;

    @Column(name = "AUTEUR_UTILISATEUR_ID")
    private Long auteurUtilisateurId;

    @Column(name = "INSTITUTION_ID")
    private Long institutionId;

    @CreationTimestamp
    @Column(name = "DATE_ECHANGE", nullable = false, updatable = false)
    private LocalDateTime dateEchange;

    @Size(max = 50, message = "La phase de litige ne peut pas dépasser 50 caractères")
    @Column(name = "PHASE_LITIGE", length = 50)
    private String phaseLitige;

    // ✅ CORRECTION : Suppression des annotations invalides sur enum
    @NotNull(message = "Le type d'échange est obligatoire")
    @Column(name = "TYPE_ECHANGE", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TypeEchange typeEchange;

    @Column(name = "PIECE_JOINTE_JUSTIFICATIF_ID")
    private Long pieceJointeJustificatifId;

    @Column(name = "VISIBLE", nullable = false)
    private Boolean visible = true;

    @Column(name = "LU_PAR_AUTRE_PARTIE", nullable = false)
    private Boolean luParAutrePartie = false;

    // Énumération pour les types d'échange
    public enum TypeEchange {
        MESSAGE("Message"),
        ACTION("Action"),
        ESCALADE("Escalade"),
        DECISION("Décision");

        private final String libelle;

        TypeEchange(String libelle) {
            this.libelle = libelle;
        }

        public String getLibelle() {
            return libelle;
        }
    }

    // Constructeurs
    public EchangeLitige() {
    }

    public EchangeLitige(Long litigeId, String contenu, TypeEchange typeEchange) {
        this.litigeId = litigeId;
        this.contenu = contenu;
        this.typeEchange = typeEchange;
        this.visible = true;
        this.luParAutrePartie = false;
    }

    public EchangeLitige(Long litigeId, String contenu, TypeEchange typeEchange,
                         Long auteurUtilisateurId, Long institutionId) {
        this(litigeId, contenu, typeEchange);
        this.auteurUtilisateurId = auteurUtilisateurId;
        this.institutionId = institutionId;
    }

    public EchangeLitige(Long litigeId, String contenu, TypeEchange typeEchange,
                         String phaseLitige, Long auteurUtilisateurId) {
        this(litigeId, contenu, typeEchange, auteurUtilisateurId, null);
        this.phaseLitige = phaseLitige;
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
    }

    public String getPhaseLitige() {
        return phaseLitige;
    }

    public void setPhaseLitige(String phaseLitige) {
        this.phaseLitige = phaseLitige;
    }

    public TypeEchange getTypeEchange() {
        return typeEchange;
    }

    public void setTypeEchange(TypeEchange typeEchange) {
        this.typeEchange = typeEchange;
    }

    public Long getPieceJointeJustificatifId() {
        return pieceJointeJustificatifId;
    }

    public void setPieceJointeJustificatifId(Long pieceJointeJustificatifId) {
        this.pieceJointeJustificatifId = pieceJointeJustificatifId;
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
        return TypeEchange.MESSAGE.equals(typeEchange);
    }

    public boolean isAction() {
        return TypeEchange.ACTION.equals(typeEchange);
    }

    public boolean isEscalade() {
        return TypeEchange.ESCALADE.equals(typeEchange);
    }

    public boolean isDecision() {
        return TypeEchange.DECISION.equals(typeEchange);
    }

    /**
     * Marque l'échange comme lu par l'autre partie
     */
    public void marquerCommeLu() {
        this.luParAutrePartie = true;
    }

    /**
     * Marque l'échange comme non lu par l'autre partie
     */
    public void marquerCommeNonLu() {
        this.luParAutrePartie = false;
    }

    /**
     * Rend l'échange visible
     */
    public void rendreVisible() {
        this.visible = true;
    }

    /**
     * Rend l'échange invisible
     */
    public void rendreInvisible() {
        this.visible = false;
    }

    /**
     * Attache une pièce jointe à cet échange
     */
    public void attacherPieceJointe(Long justificatifId) {
        this.pieceJointeJustificatifId = justificatifId;
    }

    /**
     * Détache la pièce jointe de cet échange
     */
    public void detacherPieceJointe() {
        this.pieceJointeJustificatifId = null;
    }

    /**
     * Retourne le type d'échange sous forme de chaîne
     */
    public String getTypeEchangeLibelle() {
        return typeEchange != null ? typeEchange.getLibelle() : "";
    }

    /**
     * Retourne un résumé court du contenu (100 premiers caractères)
     */
    public String getResume() {
        if (contenu == null || contenu.trim().isEmpty()) {
            return "";
        }

        String contenuNettoye = contenu.trim();
        if (contenuNettoye.length() <= 100) {
            return contenuNettoye;
        }

        return contenuNettoye.substring(0, 97) + "...";
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

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EchangeLitige that = (EchangeLitige) o;
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
        return "EchangeLitige{" +
                "id=" + id +
                ", litigeId=" + litigeId +
                ", typeEchange=" + typeEchange +
                ", phaseLitige='" + phaseLitige + '\'' +
                ", auteurUtilisateurId=" + auteurUtilisateurId +
                ", dateEchange=" + dateEchange +
                ", visible=" + visible +
                '}';
    }
}