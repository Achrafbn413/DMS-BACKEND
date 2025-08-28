package com.example.dms_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entité représentant les justificatifs d'un chargeback
 * Table: JUSTIFICATIFS_CHARGEBACK
 */
@Entity
@Table(name = "JUSTIFICATIFS_CHARGEBACK",
        indexes = {
                @Index(name = "IDX_JUSTIFICATIFS_LITIGE", columnList = "LITIGE_ID"),
                @Index(name = "IDX_JUSTIFICATIFS_PHASE", columnList = "PHASE_LITIGE"),
                @Index(name = "IDX_JUSTIFICATIFS_TYPE", columnList = "TYPE_JUSTIFICATIF"),
                @Index(name = "IDX_JUSTIFICATIFS_VALIDE", columnList = "VALIDE")
        })
public class JustificatifChargeback {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "justificatifs_chargeback_seq")
    @SequenceGenerator(name = "justificatifs_chargeback_seq", sequenceName = "SEQ_JUSTIFICATIFS_CHARGEBACK", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @NotNull(message = "L'ID du litige est obligatoire")
    @Column(name = "LITIGE_ID", nullable = false)
    private Long litigeId;

    @NotBlank(message = "Le nom du fichier est obligatoire")
    @Size(max = 255, message = "Le nom du fichier ne peut pas dépasser 255 caractères")
    @Column(name = "NOM_FICHIER", nullable = false, length = 255)
    private String nomFichier;

    @NotBlank(message = "Le type de justificatif est obligatoire")
    @Size(max = 50, message = "Le type de justificatif ne peut pas dépasser 50 caractères")
    @Column(name = "TYPE_JUSTIFICATIF", nullable = false, length = 50)
    private String typeJustificatif;

    @NotBlank(message = "La phase de litige est obligatoire")
    @Size(max = 50, message = "La phase de litige ne peut pas dépasser 50 caractères")
    @Column(name = "PHASE_LITIGE", nullable = false, length = 50)
    private String phaseLitige;

    @NotBlank(message = "Le chemin du fichier est obligatoire")
    @Size(max = 500, message = "Le chemin du fichier ne peut pas dépasser 500 caractères")
    @Column(name = "CHEMIN_FICHIER", nullable = false, length = 500)
    private String cheminFichier;

    @Min(value = 0, message = "La taille du fichier ne peut pas être négative")
    @Column(name = "TAILLE_FICHIER")
    private Long tailleFichier;

    @Size(max = 10, message = "Le format du fichier ne peut pas dépasser 10 caractères")
    @Column(name = "FORMAT_FICHIER", length = 10)
    private String formatFichier;

    @Column(name = "TRANSMIS_PAR_UTILISATEUR_ID")
    private Long transmisParUtilisateurId;

    @CreationTimestamp
    @Column(name = "DATE_AJOUT", nullable = false, updatable = false)
    private LocalDateTime dateAjout;

    @Column(name = "VALIDE", nullable = false)
    private Boolean valide = false;

    @Column(name = "COMMENTAIRES")
    @Lob
    private String commentaires;

    @Column(name = "VISIBLE_POUR_AUTRE_PARTIE", nullable = false)
    private Boolean visiblePourAutrePartie = true;

    // Constructeurs
    public JustificatifChargeback() {
    }

    public JustificatifChargeback(Long litigeId, String nomFichier, String typeJustificatif,
                                  String phaseLitige, String cheminFichier) {
        this.litigeId = litigeId;
        this.nomFichier = nomFichier;
        this.typeJustificatif = typeJustificatif;
        this.phaseLitige = phaseLitige;
        this.cheminFichier = cheminFichier;
        this.valide = false;
        this.visiblePourAutrePartie = true;
    }

    public JustificatifChargeback(Long litigeId, String nomFichier, String typeJustificatif,
                                  String phaseLitige, String cheminFichier, Long transmisParUtilisateurId) {
        this(litigeId, nomFichier, typeJustificatif, phaseLitige, cheminFichier);
        this.transmisParUtilisateurId = transmisParUtilisateurId;
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
    }

    public String getTypeJustificatif() {
        return typeJustificatif;
    }

    public void setTypeJustificatif(String typeJustificatif) {
        this.typeJustificatif = typeJustificatif;
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
    }

    public String getFormatFichier() {
        return formatFichier;
    }

    public void setFormatFichier(String formatFichier) {
        this.formatFichier = formatFichier;
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

    // Méthodes utilitaires
    public boolean isValide() {
        return Boolean.TRUE.equals(valide);
    }

    public boolean isVisiblePourAutrePartie() {
        return Boolean.TRUE.equals(visiblePourAutrePartie);
    }

    /**
     * Valide le justificatif avec des commentaires optionnels
     */
    public void valider(String commentaires) {
        this.valide = true;
        if (commentaires != null && !commentaires.trim().isEmpty()) {
            this.commentaires = commentaires;
        }
    }

    /**
     * Invalide le justificatif avec des commentaires obligatoires
     */
    public void invalider(String raisonRejet) {
        this.valide = false;
        this.commentaires = raisonRejet;
    }

    /**
     * Rend le justificatif visible pour l'autre partie
     */
    public void rendreVisible() {
        this.visiblePourAutrePartie = true;
    }

    /**
     * Rend le justificatif invisible pour l'autre partie
     */
    public void rendreInvisible() {
        this.visiblePourAutrePartie = false;
    }

    /**
     * Retourne la taille du fichier formatée en unités lisibles
     */
    public String getTailleFormatee() {
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

    /**
     * Retourne l'extension du fichier
     */
    public String getExtension() {
        if (nomFichier != null && nomFichier.contains(".")) {
            return nomFichier.substring(nomFichier.lastIndexOf(".") + 1).toLowerCase();
        }
        return formatFichier != null ? formatFichier.toLowerCase() : "";
    }

    /**
     * Vérifie si le fichier est une image
     */
    public boolean isImage() {
        String extension = getExtension();
        return "jpg".equals(extension) || "jpeg".equals(extension) ||
                "png".equals(extension) || "gif".equals(extension) ||
                "bmp".equals(extension) || "webp".equals(extension);
    }

    /**
     * Vérifie si le fichier est un PDF
     */
    public boolean isPdf() {
        return "pdf".equals(getExtension());
    }

    /**
     * Vérifie si le fichier est un document Office
     */
    public boolean isDocumentOffice() {
        String extension = getExtension();
        return "doc".equals(extension) || "docx".equals(extension) ||
                "xls".equals(extension) || "xlsx".equals(extension) ||
                "ppt".equals(extension) || "pptx".equals(extension);
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JustificatifChargeback that = (JustificatifChargeback) o;
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
        return "JustificatifChargeback{" +
                "id=" + id +
                ", litigeId=" + litigeId +
                ", nomFichier='" + nomFichier + '\'' +
                ", typeJustificatif='" + typeJustificatif + '\'' +
                ", phaseLitige='" + phaseLitige + '\'' +
                ", valide=" + valide +
                ", dateAjout=" + dateAjout +
                '}';
    }
}