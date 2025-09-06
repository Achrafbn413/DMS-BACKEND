package com.example.dms_backend.dto;

import com.example.dms_backend.model.LitigeChargeback;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * DTO pour l'entité LitigeChargeback
 * Représente les informations étendues d'un litige en mode chargeback
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LitigeChargebackDTO {

    @JsonProperty("id")
    private Long id;

    @NotNull(message = "L'ID du litige est obligatoire")
    @JsonProperty("litigeId")
    private Long litigeId;

    @Size(max = 50, message = "La phase actuelle ne peut pas dépasser 50 caractères")
    @JsonProperty("phaseActuelle")
    private String phaseActuelle;

    @Size(max = 50, message = "Le motif de chargeback ne peut pas dépasser 50 caractères")
    @JsonProperty("motifChargeback")
    private String motifChargeback;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant contesté doit être positif")
    @JsonProperty("montantConteste")
    private BigDecimal montantConteste;

    @JsonProperty("peutEtreEscalade")
    private Boolean peutEtreEscalade;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("deadlineActuelle")
    private LocalDateTime deadlineActuelle;

    @JsonProperty("joursRestantsCalcule")
    private Integer joursRestantsCalcule;

    @DecimalMin(value = "0.0", message = "Les frais d'arbitrage estimés ne peuvent pas être négatifs")
    @JsonProperty("fraisArbitrageEstime")
    private BigDecimal fraisArbitrageEstime;

    @JsonProperty("versionWorkflow")
    private Integer versionWorkflow;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateDerniereAction")
    private LocalDateTime dateDerniereAction;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateCreation")
    private LocalDateTime dateCreation;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateModification")
    private LocalDateTime dateModification;

    // Listes des entités liées (optionnelles selon le contexte)
    @JsonProperty("justificatifs")
    private List<JustificatifChargebackDTO> justificatifs;

    @JsonProperty("echanges")
    private List<EchangeLitigeDTO> echanges;

    @JsonProperty("delais")
    private List<DelaiLitigeDTO> delais;

    @JsonProperty("arbitrages")
    private List<ArbitrageDTO> arbitrages;

    // Champs calculés pour l'affichage
    @JsonProperty("statutWorkflow")
    private String statutWorkflow;

    @JsonProperty("prochainePhasePossible")
    private String prochainePhasePossible;

    @JsonProperty("deadlineDepassee")
    private Boolean deadlineDepassee;

    @JsonProperty("nombreJustificatifsValides")
    private Integer nombreJustificatifsValides;

    @JsonProperty("nombreEchangesNonLus")
    private Integer nombreEchangesNonLus;

    @JsonProperty("enArbitrage")
    private Boolean enArbitrage;

    // === NOUVELLES PROPRIÉTÉS TRANSACTION ===
    @JsonProperty("transactionRef")
    private String transactionRef;

    @JsonProperty("banqueEmettrice")
    private Long banqueEmettrice;

    @JsonProperty("banqueAcquereuse")
    private Long banqueAcquereuse;

    @JsonProperty("montantOriginal")
    private BigDecimal montantOriginal;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonProperty("dateTransaction")
    private LocalDateTime dateTransaction;
    // === OBJET TRANSACTION COMPLET POUR COMPATIBILITÉ FRONTEND ===
    @JsonProperty("transaction")
    private TransactionMinimalDTO transaction;

    // Classe interne pour représenter les données transaction nécessaires au frontend
    public static class TransactionMinimalDTO {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("reference")
        private String reference;

        @JsonProperty("montant")
        private BigDecimal montant;

        @JsonProperty("dateTransaction")
        private LocalDateTime dateTransaction;

        @JsonProperty("banqueEmettrice")
        private BanqueMinimalDTO banqueEmettrice;

        @JsonProperty("banqueAcquereuse")
        private BanqueMinimalDTO banqueAcquereuse;

        // Constructeurs
        public TransactionMinimalDTO() {}

        public TransactionMinimalDTO(String reference, Long banqueEmettriceId, Long banqueAcquereuseId,
                                     BigDecimal montant, LocalDateTime dateTransaction) {
            this.reference = reference;
            this.montant = montant;
            this.dateTransaction = dateTransaction;

            if (banqueEmettriceId != null) {
                this.banqueEmettrice = new BanqueMinimalDTO(banqueEmettriceId);
            }

            if (banqueAcquereuseId != null) {
                this.banqueAcquereuse = new BanqueMinimalDTO(banqueAcquereuseId);
            }
        }

        // Getters et Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getReference() { return reference; }
        public void setReference(String reference) { this.reference = reference; }

        public BigDecimal getMontant() { return montant; }
        public void setMontant(BigDecimal montant) { this.montant = montant; }

        public LocalDateTime getDateTransaction() { return dateTransaction; }
        public void setDateTransaction(LocalDateTime dateTransaction) { this.dateTransaction = dateTransaction; }

        public BanqueMinimalDTO getBanqueEmettrice() { return banqueEmettrice; }
        public void setBanqueEmettrice(BanqueMinimalDTO banqueEmettrice) { this.banqueEmettrice = banqueEmettrice; }

        public BanqueMinimalDTO getBanqueAcquereuse() { return banqueAcquereuse; }
        public void setBanqueAcquereuse(BanqueMinimalDTO banqueAcquereuse) { this.banqueAcquereuse = banqueAcquereuse; }
    }
    // Getter et Setter pour la transaction
    public TransactionMinimalDTO getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionMinimalDTO transaction) {
        this.transaction = transaction;
    }

    // Classe interne pour représenter les banques
    public static class BanqueMinimalDTO {
        @JsonProperty("id")
        private Long id;

        @JsonProperty("nom")
        private String nom;

        // Constructeurs
        public BanqueMinimalDTO() {}

        public BanqueMinimalDTO(Long id) {
            this.id = id;
            // Mapping simple des noms selon les IDs
            switch (id.intValue()) {
                case 1: this.nom = "CIH BANK"; break;
                case 2: this.nom = "ATTIJARIWAFA BANK"; break;
                case 3: this.nom = "BMCE BANK"; break;
                default: this.nom = "Banque #" + id; break;
            }
        }

        // Getters et Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getNom() { return nom; }
        public void setNom(String nom) { this.nom = nom; }
    }
    // === GETTERS/SETTERS TRANSACTION ===
    public String getTransactionRef() {
        return transactionRef;
    }

    public void setTransactionRef(String transactionRef) {
        this.transactionRef = transactionRef;
    }

    public Long getBanqueEmettrice() {
        return banqueEmettrice;
    }

    public void setBanqueEmettrice(Long banqueEmettrice) {
        this.banqueEmettrice = banqueEmettrice;
    }

    public Long getBanqueAcquereuse() {
        return banqueAcquereuse;
    }

    public void setBanqueAcquereuse(Long banqueAcquereuse) {
        this.banqueAcquereuse = banqueAcquereuse;
    }

    public BigDecimal getMontantOriginal() {
        return montantOriginal;
    }

    public void setMontantOriginal(BigDecimal montantOriginal) {
        this.montantOriginal = montantOriginal;
    }

    public LocalDateTime getDateTransaction() {
        return dateTransaction;
    }

    public void setDateTransaction(LocalDateTime dateTransaction) {
        this.dateTransaction = dateTransaction;
    }

    // Constructeurs
    public LitigeChargebackDTO() {
        this.justificatifs = new ArrayList<>();
        this.echanges = new ArrayList<>();
        this.delais = new ArrayList<>();
        this.arbitrages = new ArrayList<>();
    }

    public LitigeChargebackDTO(Long litigeId) {
        this();
        this.litigeId = litigeId;
        this.phaseActuelle = "CHARGEBACK_INITIAL";
        this.peutEtreEscalade = true;
        this.versionWorkflow = 1;
    }

    public LitigeChargebackDTO(Long litigeId, String motifChargeback, BigDecimal montantConteste) {
        this(litigeId);
        this.motifChargeback = motifChargeback;
        this.montantConteste = montantConteste;
    }

    public LitigeChargebackDTO(Long id, Long litigeId, String phaseActuelle, String motifChargeback,
                               BigDecimal montantConteste, Boolean peutEtreEscalade, LocalDateTime deadlineActuelle,
                               Integer joursRestantsCalcule, BigDecimal fraisArbitrageEstime, Integer versionWorkflow,
                               LocalDateTime dateDerniereAction, LocalDateTime dateCreation, LocalDateTime dateModification) {
        this();
        this.id = id;
        this.litigeId = litigeId;
        this.phaseActuelle = phaseActuelle;
        this.motifChargeback = motifChargeback;
        this.montantConteste = montantConteste;
        this.peutEtreEscalade = peutEtreEscalade;
        this.deadlineActuelle = deadlineActuelle;
        this.joursRestantsCalcule = joursRestantsCalcule;
        this.fraisArbitrageEstime = fraisArbitrageEstime;
        this.versionWorkflow = versionWorkflow;
        this.dateDerniereAction = dateDerniereAction;
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

    public Long getLitigeId() {
        return litigeId;
    }

    public void setLitigeId(Long litigeId) {
        this.litigeId = litigeId;
    }

    public String getPhaseActuelle() {
        return phaseActuelle;
    }

    public void setPhaseActuelle(String phaseActuelle) {
        this.phaseActuelle = phaseActuelle;
        this.calculateDerivedFields();
    }

    public String getMotifChargeback() {
        return motifChargeback;
    }

    public void setMotifChargeback(String motifChargeback) {
        this.motifChargeback = motifChargeback;
    }

    public BigDecimal getMontantConteste() {
        return montantConteste;
    }

    public void setMontantConteste(BigDecimal montantConteste) {
        this.montantConteste = montantConteste;
    }

    public Boolean getPeutEtreEscalade() {
        return peutEtreEscalade;
    }

    public void setPeutEtreEscalade(Boolean peutEtreEscalade) {
        this.peutEtreEscalade = peutEtreEscalade;
    }

    public LocalDateTime getDeadlineActuelle() {
        return deadlineActuelle;
    }

    public void setDeadlineActuelle(LocalDateTime deadlineActuelle) {
        this.deadlineActuelle = deadlineActuelle;
        this.calculateDerivedFields();
    }

    public Integer getJoursRestantsCalcule() {
        return joursRestantsCalcule;
    }

    public void setJoursRestantsCalcule(Integer joursRestantsCalcule) {
        this.joursRestantsCalcule = joursRestantsCalcule;
    }

    public BigDecimal getFraisArbitrageEstime() {
        return fraisArbitrageEstime;
    }

    public void setFraisArbitrageEstime(BigDecimal fraisArbitrageEstime) {
        this.fraisArbitrageEstime = fraisArbitrageEstime;
    }

    public Integer getVersionWorkflow() {
        return versionWorkflow;
    }

    public void setVersionWorkflow(Integer versionWorkflow) {
        this.versionWorkflow = versionWorkflow;
    }

    public LocalDateTime getDateDerniereAction() {
        return dateDerniereAction;
    }

    public void setDateDerniereAction(LocalDateTime dateDerniereAction) {
        this.dateDerniereAction = dateDerniereAction;
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

    public List<JustificatifChargebackDTO> getJustificatifs() {
        return justificatifs;
    }

    public void setJustificatifs(List<JustificatifChargebackDTO> justificatifs) {
        this.justificatifs = justificatifs != null ? justificatifs : new ArrayList<>();
        this.calculateDerivedFields();
    }

    public List<EchangeLitigeDTO> getEchanges() {
        return echanges;
    }

    public void setEchanges(List<EchangeLitigeDTO> echanges) {
        this.echanges = echanges != null ? echanges : new ArrayList<>();
        this.calculateDerivedFields();
    }

    public List<DelaiLitigeDTO> getDelais() {
        return delais;
    }

    public void setDelais(List<DelaiLitigeDTO> delais) {
        this.delais = delais != null ? delais : new ArrayList<>();
    }

    public List<ArbitrageDTO> getArbitrages() {
        return arbitrages;
    }

    public void setArbitrages(List<ArbitrageDTO> arbitrages) {
        this.arbitrages = arbitrages != null ? arbitrages : new ArrayList<>();
        this.calculateDerivedFields();
    }

    // Getters pour les champs calculés
    public String getStatutWorkflow() {
        return statutWorkflow;
    }

    public String getProchainePhasePossible() {
        return prochainePhasePossible;
    }

    public Boolean getDeadlineDepassee() {
        return deadlineDepassee;
    }

    public Integer getNombreJustificatifsValides() {
        return nombreJustificatifsValides;
    }

    public Integer getNombreEchangesNonLus() {
        return nombreEchangesNonLus;
    }

    public Boolean getEnArbitrage() {
        return enArbitrage;
    }

    // Méthodes utilitaires
    public boolean isPeutEtreEscalade() {
        return Boolean.TRUE.equals(peutEtreEscalade);
    }

    public boolean isDeadlineDepassee() {
        return Boolean.TRUE.equals(deadlineDepassee);
    }

    public boolean isPhaseFinale() {
        return "FINALISE".equals(phaseActuelle);
    }

    public boolean isEnArbitrage() {
        return Boolean.TRUE.equals(enArbitrage);
    }

    /**
     * Calcule les champs dérivés basés sur l'état actuel
     */
    private void calculateDerivedFields() {
        // Calcul du statut workflow
        this.statutWorkflow = determinerStatutWorkflow();

        // Calcul de la prochaine phase possible
        this.prochainePhasePossible = determinerProchainePhasePossible();

        // Vérification deadline
        this.deadlineDepassee = deadlineActuelle != null && deadlineActuelle.isBefore(LocalDateTime.now());

        // Comptage justificatifs valides
        this.nombreJustificatifsValides = (int) justificatifs.stream()
                .mapToLong(j -> Boolean.TRUE.equals(j.getValide()) ? 1 : 0)
                .sum();

        // Comptage échanges non lus
        this.nombreEchangesNonLus = (int) echanges.stream()
                .mapToLong(e -> Boolean.FALSE.equals(e.getLuParAutrePartie()) ? 1 : 0)
                .sum();

        // Vérification arbitrage
        /*this.enArbitrage = "ARBITRAGE".equals(phaseActuelle) ||
                arbitrages.stream().anyMatch(a -> !"DECIDE".equals(a.getStatut()));*/
    }

    private String determinerStatutWorkflow() {
        if (phaseActuelle == null) {
            return "Non défini";
        }

        switch (phaseActuelle) {
            case "CHARGEBACK_INITIAL":
                return "En attente de justificatifs";
            case "REPRESENTATION":
                return "En cours de représentation";
            case "PRE_ARBITRAGE":
                return "Négociation en cours";
            case "ARBITRAGE":
                return "En arbitrage";
            case "FINALISE":
                return "Finalisé";
            default:
                return "Phase inconnue";
        }
    }

    private String determinerProchainePhasePossible() {
        if (phaseActuelle == null || isPhaseFinale()) {
            return null;
        }

        switch (phaseActuelle) {
            case "CHARGEBACK_INITIAL":
                return "REPRESENTATION";
            case "REPRESENTATION":
                return isPeutEtreEscalade() ? "PRE_ARBITRAGE" : "FINALISE";
            case "PRE_ARBITRAGE":
                return "ARBITRAGE";
            case "ARBITRAGE":
                return "FINALISE";
            default:
                return null;
        }
    }

    /**
     * Convertit ce DTO en entité LitigeChargeback
     */
    public LitigeChargeback toEntity() {
        LitigeChargeback entity = new LitigeChargeback();
        entity.setId(this.id);
        entity.setLitigeId(this.litigeId);
        entity.setPhaseActuelle(this.phaseActuelle);
        entity.setMotifChargeback(this.motifChargeback);
        entity.setMontantConteste(this.montantConteste);
        entity.setPeutEtreEscalade(this.peutEtreEscalade);
        entity.setDeadlineActuelle(this.deadlineActuelle);
        entity.setJoursRestantsCalcule(this.joursRestantsCalcule);
        entity.setFraisArbitrageEstime(this.fraisArbitrageEstime);
        entity.setVersionWorkflow(this.versionWorkflow);
        entity.setDateDerniereAction(this.dateDerniereAction);
        entity.setDateCreation(this.dateCreation);
        entity.setDateModification(this.dateModification);
        return entity;
    }

    /**
     * Crée un DTO à partir d'une entité LitigeChargeback
     */
    /**
     * Crée un DTO à partir d'une entité LitigeChargeback
     */
    public static LitigeChargebackDTO fromEntity(LitigeChargeback entity) {
        if (entity == null) {
            return null;
        }

        return new LitigeChargebackDTO(
                entity.getId(),
                entity.getLitigeId(),
                entity.getPhaseActuelle(),
                entity.getMotifChargeback(),
                entity.getMontantConteste(),
                entity.getPeutEtreEscalade(),
                entity.getDeadlineActuelle(),
                entity.getJoursRestantsCalcule(),
                entity.getFraisArbitrageEstime(),
                entity.getVersionWorkflow(),
                entity.getDateDerniereAction(),
                entity.getDateCreation(),
                entity.getDateModification()
        );
    }


    /**
     * Crée un DTO à partir d'une entité LitigeChargeback avec données transaction
     */
    public static LitigeChargebackDTO fromEntityWithTransaction(LitigeChargeback entity,
                                                                String transactionRef,
                                                                Long banqueEmettrice,
                                                                Long banqueAcquereuse,
                                                                BigDecimal montantOriginal,
                                                                LocalDateTime dateTransaction) {
        if (entity == null) {
            return null;
        }

        LitigeChargebackDTO dto = new LitigeChargebackDTO(
                entity.getId(),
                entity.getLitigeId(),
                entity.getPhaseActuelle(),
                entity.getMotifChargeback(),
                entity.getMontantConteste(),
                entity.getPeutEtreEscalade(),
                entity.getDeadlineActuelle(),
                entity.getJoursRestantsCalcule(),
                entity.getFraisArbitrageEstime(),
                entity.getVersionWorkflow(),
                entity.getDateDerniereAction(),
                entity.getDateCreation(),
                entity.getDateModification()
        );

        // Enrichissement avec les données transaction
        dto.setTransactionRef(transactionRef);
        dto.setBanqueEmettrice(banqueEmettrice);
        dto.setBanqueAcquereuse(banqueAcquereuse);
        dto.setMontantOriginal(montantOriginal);
        dto.setDateTransaction(dateTransaction);

        // ✅ NOUVEAU : Création de l'objet transaction complet pour le frontend
        dto.setTransaction(new TransactionMinimalDTO(
                transactionRef,
                banqueEmettrice,
                banqueAcquereuse,
                montantOriginal,
                dateTransaction
        ));

        return dto;
    }

    /**
     * Crée un DTO complet avec les relations
     */
    public static LitigeChargebackDTO fromEntityWithRelations(LitigeChargeback entity) {
        LitigeChargebackDTO dto = fromEntity(entity);
        if (dto != null && entity != null) {
            // Conversion des listes (sera implémentée après création des autres DTOs)
            dto.setJustificatifs(new ArrayList<>()); // À implémenter
            dto.setEchanges(new ArrayList<>());      // À implémenter
            dto.setDelais(new ArrayList<>());        // À implémenter
            dto.setArbitrages(new ArrayList<>());    // À implémenter
        }
        return dto;
    }

    /**
     * Crée un DTO minimal pour les listes
     */
    public static LitigeChargebackDTO minimal(LitigeChargeback entity) {
        if (entity == null) {
            return null;
        }

        LitigeChargebackDTO dto = new LitigeChargebackDTO();
        dto.setId(entity.getId());
        dto.setLitigeId(entity.getLitigeId());
        dto.setPhaseActuelle(entity.getPhaseActuelle());
        dto.setMotifChargeback(entity.getMotifChargeback());
        dto.setMontantConteste(entity.getMontantConteste());
        dto.setDeadlineActuelle(entity.getDeadlineActuelle());
        dto.setDateCreation(entity.getDateCreation());
        return dto;
    }

    /**
     * Met à jour une entité existante avec les données de ce DTO
     */
    public void updateEntity(LitigeChargeback entity) {
        if (entity != null) {
            entity.setPhaseActuelle(this.phaseActuelle);
            entity.setMotifChargeback(this.motifChargeback);
            entity.setMontantConteste(this.montantConteste);
            entity.setPeutEtreEscalade(this.peutEtreEscalade);
            entity.setDeadlineActuelle(this.deadlineActuelle);
            entity.setJoursRestantsCalcule(this.joursRestantsCalcule);
            entity.setFraisArbitrageEstime(this.fraisArbitrageEstime);
            entity.setVersionWorkflow(this.versionWorkflow);
        }
    }

    /**
     * Vérifie si ce DTO contient les données minimales requises
     */
    public boolean isValid() {
        return litigeId != null &&
                phaseActuelle != null && !phaseActuelle.trim().isEmpty();
    }

    /**
     * Retourne un résumé pour l'affichage
     */
    public String getResume() {
        StringBuilder sb = new StringBuilder();
        sb.append("Chargeback #").append(litigeId);

        if (motifChargeback != null) {
            sb.append(" - ").append(motifChargeback);
        }

        if (montantConteste != null) {
            sb.append(" (").append(montantConteste).append(" €)");
        }

        return sb.toString();
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LitigeChargebackDTO that = (LitigeChargebackDTO) o;
        return Objects.equals(id, that.id) && Objects.equals(litigeId, that.litigeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, litigeId);
    }

    @Override
    public String toString() {
        return "LitigeChargebackDTO{" +
                "id=" + id +
                ", litigeId=" + litigeId +
                ", phaseActuelle='" + phaseActuelle + '\'' +
                ", motifChargeback='" + motifChargeback + '\'' +
                ", montantConteste=" + montantConteste +
                ", deadlineActuelle=" + deadlineActuelle +
                ", statutWorkflow='" + statutWorkflow + '\'' +
                '}';
    }
}
