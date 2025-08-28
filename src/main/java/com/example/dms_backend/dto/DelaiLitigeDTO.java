package com.example.dms_backend.dto;

import com.example.dms_backend.model.DelaiLitige;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * DTO pour l'entité DelaiLitige
 * Représente les délais et échéances d'un litige chargeback
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DelaiLitigeDTO {

    @JsonProperty("id")
    private Long id;

    @NotNull(message = "L'ID du litige est obligatoire")
    @JsonProperty("litigeId")
    private Long litigeId;

    @NotBlank(message = "La phase de litige est obligatoire")
    @Size(max = 50, message = "La phase de litige ne peut pas dépasser 50 caractères")
    @JsonProperty("phaseLitige")
    private String phaseLitige;

    @NotNull(message = "La date de début est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateDebut")
    private LocalDateTime dateDebut;

    @NotNull(message = "La date limite est obligatoire")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateLimite")
    private LocalDateTime dateLimite;

    @Min(value = 0, message = "La prolongation accordée ne peut pas être négative")
    @JsonProperty("prolongationAccordee")
    private Integer prolongationAccordee;

    @JsonProperty("motifProlongation")
    private String motifProlongation;

    @JsonProperty("statutDelai")
    private String statutDelai;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateCreation")
    private LocalDateTime dateCreation;

    // Champs calculés pour l'affichage
    @JsonProperty("statutDelaiLibelle")
    private String statutDelaiLibelle;

    @JsonProperty("joursRestants")
    private Long joursRestants;

    @JsonProperty("heuresRestantes")
    private Long heuresRestantes;

    @JsonProperty("dureeTotaleJours")
    private Long dureeTotaleJours;

    @JsonProperty("pourcentageAvancement")
    private Double pourcentageAvancement;

    @JsonProperty("dateDebutFormatee")
    private String dateDebutFormatee;

    @JsonProperty("dateLimiteFormatee")
    private String dateLimiteFormatee;

    @JsonProperty("statutAvecInfo")
    private String statutAvecInfo;

    @JsonProperty("niveauUrgence")
    private String niveauUrgence;

    @JsonProperty("couleurStatut")
    private String couleurStatut;

    @JsonProperty("iconeStatut")
    private String iconeStatut;

    @JsonProperty("messageUrgence")
    private String messageUrgence;

    @JsonProperty("actionsDisponibles")
    private String[] actionsDisponibles;

    @JsonProperty("enZoneCritique")
    private Boolean enZoneCritique;

    @JsonProperty("enZoneAlerte")
    private Boolean enZoneAlerte;

    @JsonProperty("depassementJours")
    private Long depassementJours;

    @JsonProperty("estimationFinPhase")
    private LocalDateTime estimationFinPhase;

    // Énumération pour les statuts de délai (correspondant à l'entité)
    public enum StatutDelai {
        ACTIF("Actif", "clock", "blue"),
        EXPIRE("Expiré", "alert-circle", "red"),
        PROLONGE("Prolongé", "calendar-plus", "orange");

        private final String libelle;
        private final String icone;
        private final String couleur;

        StatutDelai(String libelle, String icone, String couleur) {
            this.libelle = libelle;
            this.icone = icone;
            this.couleur = couleur;
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
    }

    // Constructeurs
    public DelaiLitigeDTO() {
        this.prolongationAccordee = 0;
        this.statutDelai = "ACTIF";
    }

    public DelaiLitigeDTO(Long litigeId, String phaseLitige, LocalDateTime dateDebut, LocalDateTime dateLimite) {
        this();
        this.litigeId = litigeId;
        this.phaseLitige = phaseLitige;
        this.dateDebut = dateDebut;
        this.dateLimite = dateLimite;
        this.calculateDerivedFields();
    }

    public DelaiLitigeDTO(Long litigeId, String phaseLitige, LocalDateTime dateDebut, int dureeJours) {
        this(litigeId, phaseLitige, dateDebut, dateDebut.plusDays(dureeJours));
    }

    public DelaiLitigeDTO(Long id, Long litigeId, String phaseLitige, LocalDateTime dateDebut,
                          LocalDateTime dateLimite, Integer prolongationAccordee, String motifProlongation,
                          String statutDelai, LocalDateTime dateCreation) {
        this.id = id;
        this.litigeId = litigeId;
        this.phaseLitige = phaseLitige;
        this.dateDebut = dateDebut;
        this.dateLimite = dateLimite;
        this.prolongationAccordee = prolongationAccordee;
        this.motifProlongation = motifProlongation;
        this.statutDelai = statutDelai;
        this.dateCreation = dateCreation;
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
        this.calculateDerivedFields();
    }

    public LocalDateTime getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(LocalDateTime dateLimite) {
        this.dateLimite = dateLimite;
        this.calculateDerivedFields();
    }

    public Integer getProlongationAccordee() {
        return prolongationAccordee;
    }

    public void setProlongationAccordee(Integer prolongationAccordee) {
        this.prolongationAccordee = prolongationAccordee;
        this.calculateDerivedFields();
    }

    public String getMotifProlongation() {
        return motifProlongation;
    }

    public void setMotifProlongation(String motifProlongation) {
        this.motifProlongation = motifProlongation;
    }

    public String getStatutDelai() {
        return statutDelai;
    }

    public void setStatutDelai(String statutDelai) {
        this.statutDelai = statutDelai;
        this.calculateDerivedFields();
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    // Getters pour les champs calculés
    public String getStatutDelaiLibelle() {
        return statutDelaiLibelle;
    }

    public Long getJoursRestants() {
        return joursRestants;
    }

    public Long getHeuresRestantes() {
        return heuresRestantes;
    }

    public Long getDureeTotaleJours() {
        return dureeTotaleJours;
    }

    public Double getPourcentageAvancement() {
        return pourcentageAvancement;
    }

    public String getDateDebutFormatee() {
        return dateDebutFormatee;
    }

    public String getDateLimiteFormatee() {
        return dateLimiteFormatee;
    }

    public String getStatutAvecInfo() {
        return statutAvecInfo;
    }

    public String getNiveauUrgence() {
        return niveauUrgence;
    }

    public String getCouleurStatut() {
        return couleurStatut;
    }

    public String getIconeStatut() {
        return iconeStatut;
    }

    public String getMessageUrgence() {
        return messageUrgence;
    }

    public String[] getActionsDisponibles() {
        return actionsDisponibles;
    }

    public Boolean getEnZoneCritique() {
        return enZoneCritique;
    }

    public Boolean getEnZoneAlerte() {
        return enZoneAlerte;
    }

    public Long getDepassementJours() {
        return depassementJours;
    }

    public LocalDateTime getEstimationFinPhase() {
        return estimationFinPhase;
    }

    // Méthodes utilitaires
    public boolean isActif() {
        return "ACTIF".equals(statutDelai);
    }

    public boolean isExpire() {
        return "EXPIRE".equals(statutDelai);
    }

    public boolean isProlonge() {
        return "PROLONGE".equals(statutDelai);
    }

    public boolean hasProlongation() {
        return prolongationAccordee != null && prolongationAccordee > 0;
    }

    public boolean isDepasseMaintenant() {
        return dateLimite != null && LocalDateTime.now().isAfter(dateLimite);
    }

    public boolean isEnZoneCritique() {
        return Boolean.TRUE.equals(enZoneCritique);
    }

    public boolean isEnZoneAlerte() {
        return Boolean.TRUE.equals(enZoneAlerte);
    }

    /**
     * Calcule les champs dérivés basés sur l'état actuel
     */
    private void calculateDerivedFields() {
        // Calculs de base
        this.joursRestants = calculerJoursRestants();
        this.heuresRestantes = calculerHeuresRestantes();
        this.dureeTotaleJours = calculerDureeTotaleJours();
        this.pourcentageAvancement = calculerPourcentageAvancement();

        // Formatage des dates
        this.dateDebutFormatee = formaterDate(dateDebut);
        this.dateLimiteFormatee = formaterDate(dateLimite);

        // Calcul du statut
        this.statutDelaiLibelle = calculerStatutLibelle();
        this.statutAvecInfo = calculerStatutAvecInfo();

        // Calcul de l'urgence
        this.niveauUrgence = calculerNiveauUrgence();
        this.couleurStatut = calculerCouleurStatut();
        this.iconeStatut = calculerIconeStatut();
        this.messageUrgence = calculerMessageUrgence();

        // Zones de risque
        this.enZoneCritique = verifierZoneCritique(3); // 3 jours par défaut
        this.enZoneAlerte = verifierZoneAlerte(80.0);  // 80% par défaut

        // Dépassement
        this.depassementJours = calculerDepassementJours();

        // Actions disponibles
        this.actionsDisponibles = determinerActionsDisponibles();

        // Estimation fin de phase
        this.estimationFinPhase = calculerEstimationFinPhase();
    }

    private Long calculerJoursRestants() {
        if (dateLimite == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDateTime.now(), dateLimite);
    }

    private Long calculerHeuresRestantes() {
        if (dateLimite == null) {
            return null;
        }
        return ChronoUnit.HOURS.between(LocalDateTime.now(), dateLimite);
    }

    private Long calculerDureeTotaleJours() {
        if (dateDebut == null || dateLimite == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(dateDebut, dateLimite);
    }

    private Double calculerPourcentageAvancement() {
        if (dateDebut == null || dateLimite == null) {
            return null;
        }

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

    private String formaterDate(LocalDateTime date) {
        if (date == null) {
            return "";
        }
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String calculerStatutLibelle() {
        if (statutDelai == null) {
            return "Non défini";
        }

        try {
            return StatutDelai.valueOf(statutDelai).getLibelle();
        } catch (IllegalArgumentException e) {
            return statutDelai;
        }
    }

    private String calculerStatutAvecInfo() {
        if (joursRestants == null) {
            return statutDelaiLibelle != null ? statutDelaiLibelle : "Non défini";
        }

        if ("ACTIF".equals(statutDelai)) {
            if (joursRestants > 0) {
                return String.format("Actif (%d jour(s) restant(s))", joursRestants);
            } else if (joursRestants == 0) {
                return "Actif (expire aujourd'hui)";
            } else {
                return String.format("Actif (dépassé de %d jour(s))", Math.abs(joursRestants));
            }
        } else if ("EXPIRE".equals(statutDelai)) {
            return String.format("Expiré (dépassé de %d jour(s))", Math.abs(joursRestants));
        } else if ("PROLONGE".equals(statutDelai)) {
            if (joursRestants > 0) {
                return String.format("Prolongé (%d jour(s) restant(s))", joursRestants);
            } else {
                return String.format("Prolongé (dépassé de %d jour(s))", Math.abs(joursRestants));
            }
        }

        return statutDelaiLibelle != null ? statutDelaiLibelle : "Non défini";
    }

    private String calculerNiveauUrgence() {
        if (joursRestants == null) {
            return "NORMALE";
        }

        if (joursRestants < 0) {
            return "CRITIQUE"; // Dépassé
        } else if (joursRestants == 0) {
            return "CRITIQUE"; // Expire aujourd'hui
        } else if (joursRestants <= 1) {
            return "URGENTE"; // 1 jour restant
        } else if (joursRestants <= 3) {
            return "ELEVEE";  // 2-3 jours restants
        } else if (joursRestants <= 7) {
            return "MOYENNE"; // 4-7 jours restants
        } else {
            return "NORMALE"; // Plus de 7 jours
        }
    }

    private String calculerCouleurStatut() {
        if (statutDelai == null) {
            return "gray";
        }

        try {
            return StatutDelai.valueOf(statutDelai).getCouleur();
        } catch (IllegalArgumentException e) {
            return "gray";
        }
    }

    private String calculerIconeStatut() {
        if (statutDelai == null) {
            return "clock";
        }

        try {
            return StatutDelai.valueOf(statutDelai).getIcone();
        } catch (IllegalArgumentException e) {
            return "clock";
        }
    }

    private String calculerMessageUrgence() {
        String urgence = niveauUrgence != null ? niveauUrgence : "NORMALE";

        switch (urgence) {
            case "CRITIQUE":
                if (joursRestants != null && joursRestants < 0) {
                    return "DÉLAI DÉPASSÉ ! Action immédiate requise.";
                } else {
                    return "DÉLAI CRITIQUE ! Expire aujourd'hui.";
                }
            case "URGENTE":
                return "URGENT ! Moins de 24h restantes.";
            case "ELEVEE":
                return "Attention ! Moins de 3 jours restants.";
            case "MOYENNE":
                return "À surveiller - Moins d'une semaine.";
            case "NORMALE":
            default:
                return "Délai sous contrôle.";
        }
    }

    private Boolean verifierZoneCritique(int seuilJours) {
        if (joursRestants == null) {
            return false;
        }
        return joursRestants >= 0 && joursRestants <= seuilJours;
    }

    private Boolean verifierZoneAlerte(double seuilPourcentage) {
        if (pourcentageAvancement == null) {
            return false;
        }
        return pourcentageAvancement >= (100.0 - seuilPourcentage);
    }

    private Long calculerDepassementJours() {
        if (joursRestants == null || joursRestants >= 0) {
            return null;
        }
        return Math.abs(joursRestants);
    }

    private String[] determinerActionsDisponibles() {
        if ("EXPIRE".equals(statutDelai)) {
            return new String[]{"prolonger", "cloturer"};
        } else if (isEnZoneCritique()) {
            return new String[]{"prolonger", "escalader", "notifier"};
        } else if (isEnZoneAlerte()) {
            return new String[]{"prolonger", "notifier"};
        } else {
            return new String[]{"modifier", "notifier"};
        }
    }

    private LocalDateTime calculerEstimationFinPhase() {
        if (pourcentageAvancement == null || pourcentageAvancement <= 0) {
            return dateLimite;
        }

        // Estimation basée sur la vitesse actuelle de traitement
        if (pourcentageAvancement >= 100.0) {
            return LocalDateTime.now();
        }

        // Calcul simple basé sur la progression actuelle
        long minutesEcoulees = ChronoUnit.MINUTES.between(dateDebut, LocalDateTime.now());
        long minutesEstimeesTotal = (long) (minutesEcoulees * 100.0 / pourcentageAvancement);

        return dateDebut.plusMinutes(minutesEstimeesTotal);
    }

    /**
     * Convertit ce DTO en entité DelaiLitige
     */
    public DelaiLitige toEntity() {
        DelaiLitige entity = new DelaiLitige();
        entity.setId(this.id);
        entity.setLitigeId(this.litigeId);
        entity.setPhaseLitige(this.phaseLitige);
        entity.setDateDebut(this.dateDebut);
        entity.setDateLimite(this.dateLimite);
        entity.setProlongationAccordee(this.prolongationAccordee);
        entity.setMotifProlongation(this.motifProlongation);

        // Conversion du statut
        if (this.statutDelai != null) {
            try {
                entity.setStatutDelai(DelaiLitige.StatutDelai.valueOf(this.statutDelai));
            } catch (IllegalArgumentException e) {
                entity.setStatutDelai(DelaiLitige.StatutDelai.ACTIF);
            }
        }

        entity.setDateCreation(this.dateCreation);
        return entity;
    }

    /**
     * Crée un DTO à partir d'une entité DelaiLitige
     */
    public static DelaiLitigeDTO fromEntity(DelaiLitige entity) {
        if (entity == null) {
            return null;
        }

        String statutDelaiStr = entity.getStatutDelai() != null ?
                entity.getStatutDelai().name() : null;

        return new DelaiLitigeDTO(
                entity.getId(),
                entity.getLitigeId(),
                entity.getPhaseLitige(),
                entity.getDateDebut(),
                entity.getDateLimite(),
                entity.getProlongationAccordee(),
                entity.getMotifProlongation(),
                statutDelaiStr,
                entity.getDateCreation()
        );
    }

    /**
     * Crée un DTO minimal pour les listes
     */
    public static DelaiLitigeDTO minimal(DelaiLitige entity) {
        if (entity == null) {
            return null;
        }

        DelaiLitigeDTO dto = new DelaiLitigeDTO();
        dto.setId(entity.getId());
        dto.setPhaseLitige(entity.getPhaseLitige());
        dto.setDateLimite(entity.getDateLimite());
        dto.setStatutDelai(entity.getStatutDelai() != null ? entity.getStatutDelai().name() : null);
        return dto;
    }

    /**
     * Met à jour une entité existante avec les données de ce DTO
     */
    public void updateEntity(DelaiLitige entity) {
        if (entity != null) {
            entity.setPhaseLitige(this.phaseLitige);
            entity.setDateDebut(this.dateDebut);
            entity.setDateLimite(this.dateLimite);
            entity.setProlongationAccordee(this.prolongationAccordee);
            entity.setMotifProlongation(this.motifProlongation);

            if (this.statutDelai != null) {
                try {
                    entity.setStatutDelai(DelaiLitige.StatutDelai.valueOf(this.statutDelai));
                } catch (IllegalArgumentException e) {
                    // Ignore si le statut n'est pas valide
                }
            }
        }
    }

    /**
     * Vérifie si ce DTO contient les données minimales requises
     */
    public boolean isValid() {
        return litigeId != null &&
                phaseLitige != null && !phaseLitige.trim().isEmpty() &&
                dateDebut != null &&
                dateLimite != null &&
                dateDebut.isBefore(dateLimite);
    }

    /**
     * Retourne un résumé pour l'affichage
     */
    public String getResume() {
        StringBuilder sb = new StringBuilder();
        sb.append("Phase ").append(phaseLitige);

        if (joursRestants != null) {
            if (joursRestants > 0) {
                sb.append(" - ").append(joursRestants).append(" jour(s) restant(s)");
            } else if (joursRestants == 0) {
                sb.append(" - Expire aujourd'hui");
            } else {
                sb.append(" - Dépassé de ").append(Math.abs(joursRestants)).append(" jour(s)");
            }
        }

        return sb.toString();
    }

    /**
     * Retourne une description complète du délai
     */
    public String getDescriptionComplete() {
        StringBuilder sb = new StringBuilder();
        sb.append("Délai pour la phase ").append(phaseLitige);
        sb.append(" : du ").append(dateDebutFormatee);
        sb.append(" au ").append(dateLimiteFormatee);

        if (hasProlongation()) {
            sb.append(" (prolongé de ").append(prolongationAccordee).append(" jour(s))");
        }

        sb.append(" - ").append(statutAvecInfo);

        return sb.toString();
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DelaiLitigeDTO that = (DelaiLitigeDTO) o;
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
        return "DelaiLitigeDTO{" +
                "id=" + id +
                ", litigeId=" + litigeId +
                ", phaseLitige='" + phaseLitige + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateLimite=" + dateLimite +
                ", statutDelai='" + statutDelai + '\'' +
                ", joursRestants=" + joursRestants +
                ", niveauUrgence='" + niveauUrgence + '\'' +
                ", enZoneCritique=" + enZoneCritique +
                '}';
    }
}