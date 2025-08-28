package com.example.dms_backend.dto;

import com.example.dms_backend.model.TransactionChargeback;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * DTO pour l'entité TransactionChargeback
 * Représente les informations étendues de chargeback d'une transaction
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionChargebackDTO {

    @JsonProperty("id")
    private Long id;

    @NotNull(message = "L'ID de la transaction est obligatoire")
    @JsonProperty("transactionId")
    private Long transactionId;

    @JsonProperty("aLitigeActif")
    private Boolean aLitigeActif;

    @Min(value = 0, message = "Le nombre de chargebacks ne peut pas être négatif")
    @JsonProperty("nombreChargebacks")
    private Integer nombreChargebacks;

    @DecimalMin(value = "0.0", message = "Le montant total contesté ne peut pas être négatif")
    @JsonProperty("montantTotalConteste")
    private BigDecimal montantTotalConteste;

    @JsonProperty("historiqueChargebacks")
    private String historiqueChargebacks;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateCreation")
    private LocalDateTime dateCreation;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateModification")
    private LocalDateTime dateModification;

    // Champs calculés pour l'affichage
    @JsonProperty("statutLitige")
    private String statutLitige;

    @JsonProperty("niveauRisque")
    private String niveauRisque;

    @JsonProperty("pourcentageConteste")
    private Double pourcentageConteste;

    @JsonProperty("dernierChargebackDate")
    private LocalDateTime dernierChargebackDate;

    @JsonProperty("tendanceChargebacks")
    private String tendanceChargebacks;

    // Constructeurs
    public TransactionChargebackDTO() {
        this.aLitigeActif = false;
        this.nombreChargebacks = 0;
        this.montantTotalConteste = BigDecimal.ZERO;
    }

    public TransactionChargebackDTO(Long transactionId) {
        this();
        this.transactionId = transactionId;
    }

    public TransactionChargebackDTO(Long transactionId, Boolean aLitigeActif, Integer nombreChargebacks,
                                    BigDecimal montantTotalConteste) {
        this(transactionId);
        this.aLitigeActif = aLitigeActif;
        this.nombreChargebacks = nombreChargebacks;
        this.montantTotalConteste = montantTotalConteste;
        this.calculateDerivedFields();
    }

    public TransactionChargebackDTO(Long id, Long transactionId, Boolean aLitigeActif, Integer nombreChargebacks,
                                    BigDecimal montantTotalConteste, String historiqueChargebacks,
                                    LocalDateTime dateCreation, LocalDateTime dateModification) {
        this.id = id;
        this.transactionId = transactionId;
        this.aLitigeActif = aLitigeActif;
        this.nombreChargebacks = nombreChargebacks;
        this.montantTotalConteste = montantTotalConteste;
        this.historiqueChargebacks = historiqueChargebacks;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.calculateDerivedFields();
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Long transactionId) {
        this.transactionId = transactionId;
    }

    public Boolean getALitigeActif() {
        return aLitigeActif;
    }

    public void setALitigeActif(Boolean aLitigeActif) {
        this.aLitigeActif = aLitigeActif;
        this.calculateDerivedFields();
    }

    public Integer getNombreChargebacks() {
        return nombreChargebacks;
    }

    public void setNombreChargebacks(Integer nombreChargebacks) {
        this.nombreChargebacks = nombreChargebacks;
        this.calculateDerivedFields();
    }

    public BigDecimal getMontantTotalConteste() {
        return montantTotalConteste;
    }

    public void setMontantTotalConteste(BigDecimal montantTotalConteste) {
        this.montantTotalConteste = montantTotalConteste;
        this.calculateDerivedFields();
    }

    public String getHistoriqueChargebacks() {
        return historiqueChargebacks;
    }

    public void setHistoriqueChargebacks(String historiqueChargebacks) {
        this.historiqueChargebacks = historiqueChargebacks;
        this.calculateDerivedFields();
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public LocalDateTime getDateModification() {
        return dateModification;
    }

    public void setDateModification(LocalDateTime dateModification) {
        this.dateModification = dateModification;
    }

    // Getters pour les champs calculés
    public String getStatutLitige() {
        return statutLitige;
    }

    public String getNiveauRisque() {
        return niveauRisque;
    }

    public Double getPourcentageConteste() {
        return pourcentageConteste;
    }

    public LocalDateTime getDernierChargebackDate() {
        return dernierChargebackDate;
    }

    public String getTendanceChargebacks() {
        return tendanceChargebacks;
    }

    // Méthodes utilitaires
    public boolean hasLitigeActif() {
        return Boolean.TRUE.equals(aLitigeActif);
    }

    public boolean hasChargebacks() {
        return nombreChargebacks != null && nombreChargebacks > 0;
    }

    public boolean hasMontantConteste() {
        return montantTotalConteste != null && montantTotalConteste.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean hasHistorique() {
        return historiqueChargebacks != null && !historiqueChargebacks.trim().isEmpty();
    }

    /**
     * Calcule les champs dérivés basés sur l'état actuel
     */
    private void calculateDerivedFields() {
        // Calcul du statut litige
        this.statutLitige = determinerStatutLitige();

        // Calcul du niveau de risque
        this.niveauRisque = determinerNiveauRisque();

        // Calcul de la tendance
        this.tendanceChargebacks = determinerTendanceChargebacks();

        // Extraction de la dernière date de chargeback
        this.dernierChargebackDate = extraireDerniereDate();
    }

    private String determinerStatutLitige() {
        if (Boolean.TRUE.equals(aLitigeActif)) {
            return "Litige actif";
        } else if (hasChargebacks()) {
            return "Historique de litiges";
        } else {
            return "Aucun litige";
        }
    }

    private String determinerNiveauRisque() {
        if (!hasChargebacks()) {
            return "FAIBLE";
        }

        int nbChargebacks = nombreChargebacks != null ? nombreChargebacks : 0;
        BigDecimal montant = montantTotalConteste != null ? montantTotalConteste : BigDecimal.ZERO;

        if (Boolean.TRUE.equals(aLitigeActif)) {
            return "CRITIQUE";
        } else if (nbChargebacks >= 3 || montant.compareTo(BigDecimal.valueOf(10000)) > 0) {
            return "ÉLEVÉ";
        } else if (nbChargebacks >= 2 || montant.compareTo(BigDecimal.valueOf(1000)) > 0) {
            return "MOYEN";
        } else {
            return "FAIBLE";
        }
    }

    private String determinerTendanceChargebacks() {
        if (!hasChargebacks()) {
            return "STABLE";
        }

        // Analyse basique basée sur le nombre de chargebacks
        int nbChargebacks = nombreChargebacks != null ? nombreChargebacks : 0;

        if (Boolean.TRUE.equals(aLitigeActif)) {
            return "EN_HAUSSE";
        } else if (nbChargebacks >= 3) {
            return "RÉCURRENTE";
        } else if (nbChargebacks == 2) {
            return "SURVEILLÉE";
        } else {
            return "STABLE";
        }
    }

    private LocalDateTime extraireDerniereDate() {
        // Logique simplifiée - dans un cas réel, on analyserait l'historique JSON
        if (hasHistorique() && dateModification != null) {
            return dateModification;
        }
        return null;
    }

    /**
     * Calcule le pourcentage contesté par rapport au montant total de la transaction
     */
    public void calculerPourcentageConteste(BigDecimal montantTransaction) {
        if (montantTransaction != null && montantTransaction.compareTo(BigDecimal.ZERO) > 0 &&
                montantTotalConteste != null) {
            this.pourcentageConteste = montantTotalConteste
                    .divide(montantTransaction, 4, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        } else {
            this.pourcentageConteste = 0.0;
        }
    }

    /**
     * Retourne la couleur associée au niveau de risque
     */
    public String getCouleurRisque() {
        if (niveauRisque == null) {
            return "gray";
        }

        switch (niveauRisque) {
            case "CRITIQUE":
                return "red";
            case "ÉLEVÉ":
                return "orange";
            case "MOYEN":
                return "yellow";
            case "FAIBLE":
            default:
                return "green";
        }
    }

    /**
     * Retourne une description textuelle du risque
     */
    public String getDescriptionRisque() {
        if (niveauRisque == null) {
            return "Non évalué";
        }

        switch (niveauRisque) {
            case "CRITIQUE":
                return "Transaction avec litige actif - Attention requise";
            case "ÉLEVÉ":
                return "Historique de chargebacks importants - Surveillance renforcée";
            case "MOYEN":
                return "Quelques incidents - Surveillance normale";
            case "FAIBLE":
                return "Profil de risque faible - Surveillance standard";
            default:
                return "Profil de risque non déterminé";
        }
    }

    /**
     * Convertit ce DTO en entité TransactionChargeback
     */
    public TransactionChargeback toEntity() {
        TransactionChargeback entity = new TransactionChargeback();
        entity.setId(this.id);
        entity.setTransactionId(this.transactionId);
        entity.setALitigeActif(this.aLitigeActif);
        entity.setNombreChargebacks(this.nombreChargebacks);
        entity.setMontantTotalConteste(this.montantTotalConteste);
        entity.setHistoriqueChargebacks(this.historiqueChargebacks);
        entity.setDateCreation(this.dateCreation);
        entity.setDateModification(this.dateModification);
        return entity;
    }

    /**
     * Crée un DTO à partir d'une entité TransactionChargeback
     */
    public static TransactionChargebackDTO fromEntity(TransactionChargeback entity) {
        if (entity == null) {
            return null;
        }

        return new TransactionChargebackDTO(
                entity.getId(),
                entity.getTransactionId(),
                entity.getALitigeActif(),
                entity.getNombreChargebacks(),
                entity.getMontantTotalConteste(),
                entity.getHistoriqueChargebacks(),
                entity.getDateCreation(),
                entity.getDateModification()
        );
    }

    /**
     * Crée un DTO avec calcul du pourcentage contesté
     */
    public static TransactionChargebackDTO fromEntityWithPercentage(TransactionChargeback entity,
                                                                    BigDecimal montantTransaction) {
        TransactionChargebackDTO dto = fromEntity(entity);
        if (dto != null) {
            dto.calculerPourcentageConteste(montantTransaction);
        }
        return dto;
    }

    /**
     * Crée un DTO minimal pour les listes
     */
    public static TransactionChargebackDTO minimal(TransactionChargeback entity) {
        if (entity == null) {
            return null;
        }

        TransactionChargebackDTO dto = new TransactionChargebackDTO();
        dto.setId(entity.getId());
        dto.setTransactionId(entity.getTransactionId());
        dto.setALitigeActif(entity.getALitigeActif());
        dto.setNombreChargebacks(entity.getNombreChargebacks());
        dto.setMontantTotalConteste(entity.getMontantTotalConteste());
        return dto;
    }

    /**
     * Met à jour une entité existante avec les données de ce DTO
     */
    public void updateEntity(TransactionChargeback entity) {
        if (entity != null) {
            entity.setALitigeActif(this.aLitigeActif);
            entity.setNombreChargebacks(this.nombreChargebacks);
            entity.setMontantTotalConteste(this.montantTotalConteste);
            entity.setHistoriqueChargebacks(this.historiqueChargebacks);
        }
    }

    /**
     * Vérifie si ce DTO contient les données minimales requises
     */
    public boolean isValid() {
        return transactionId != null;
    }

    /**
     * Retourne un résumé pour l'affichage
     */
    public String getResume() {
        StringBuilder sb = new StringBuilder();
        sb.append("Transaction #").append(transactionId);

        if (hasLitigeActif()) {
            sb.append(" (LITIGE ACTIF)");
        } else if (hasChargebacks()) {
            sb.append(" (").append(nombreChargebacks).append(" chargeback(s))");
        } else {
            sb.append(" (Aucun litige)");
        }

        return sb.toString();
    }

    /**
     * Retourne les statistiques sous forme de texte
     */
    public String getStatistiques() {
        StringBuilder sb = new StringBuilder();

        if (hasChargebacks()) {
            sb.append(nombreChargebacks).append(" chargeback(s)");

            if (hasMontantConteste()) {
                sb.append(" pour ").append(montantTotalConteste).append(" €");
            }

            if (pourcentageConteste != null && pourcentageConteste > 0) {
                sb.append(" (").append(String.format("%.1f", pourcentageConteste)).append("%)");
            }
        } else {
            sb.append("Aucun chargeback");
        }

        return sb.toString();
    }

    /**
     * Indique si cette transaction nécessite une attention particulière
     */
    public boolean necessiteAttention() {
        return hasLitigeActif() ||
                "CRITIQUE".equals(niveauRisque) ||
                "ÉLEVÉ".equals(niveauRisque);
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionChargebackDTO that = (TransactionChargebackDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, transactionId);
    }

    @Override
    public String toString() {
        return "TransactionChargebackDTO{" +
                "id=" + id +
                ", transactionId=" + transactionId +
                ", aLitigeActif=" + aLitigeActif +
                ", nombreChargebacks=" + nombreChargebacks +
                ", montantTotalConteste=" + montantTotalConteste +
                ", niveauRisque='" + niveauRisque + '\'' +
                ", statutLitige='" + statutLitige + '\'' +
                '}';
    }
}