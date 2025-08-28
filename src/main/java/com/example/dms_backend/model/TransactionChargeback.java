package com.example.dms_backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entité de liaison pour étendre les fonctionnalités chargeback d'une transaction
 * Table: TRANSACTIONS_CHARGEBACK
 */
@Entity
@Table(name = "TRANSACTIONS_CHARGEBACK",
        indexes = {
                @Index(name = "IDX_TRANSACTIONS_CHARGEBACK_ACTIF", columnList = "A_LITIGE_ACTIF"),
                @Index(name = "IDX_TRANSACTIONS_CHARGEBACK_NOMBRE", columnList = "NOMBRE_CHARGEBACKS")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "UK_TRANSACTIONS_CHARGEBACK_TXN", columnNames = "TRANSACTION_ID")
        })
public class TransactionChargeback {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transactions_chargeback_seq")
    @SequenceGenerator(name = "transactions_chargeback_seq", sequenceName = "SEQ_TRANSACTIONS_CHARGEBACK", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @NotNull(message = "L'ID de la transaction est obligatoire")
    @Column(name = "TRANSACTION_ID", nullable = false, unique = true)
    private Long transactionId;

    @Column(name = "A_LITIGE_ACTIF", nullable = false)
    private Boolean aLitigeActif = false;

    @Min(value = 0, message = "Le nombre de chargebacks ne peut pas être négatif")
    @Column(name = "NOMBRE_CHARGEBACKS", nullable = false)
    private Integer nombreChargebacks = 0;

    @DecimalMin(value = "0.0", message = "Le montant total contesté ne peut pas être négatif")
    @Column(name = "MONTANT_TOTAL_CONTESTE", precision = 15, scale = 2, nullable = false)
    private BigDecimal montantTotalConteste = BigDecimal.ZERO;

    @Column(name = "HISTORIQUE_CHARGEBACKS")
    @Lob
    private String historiqueChargebacks;

    @CreationTimestamp
    @Column(name = "DATE_CREATION", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "DATE_MODIFICATION")
    private LocalDateTime dateModification;

    // Constructeurs
    public TransactionChargeback() {
    }

    public TransactionChargeback(Long transactionId) {
        this.transactionId = transactionId;
        this.aLitigeActif = false;
        this.nombreChargebacks = 0;
        this.montantTotalConteste = BigDecimal.ZERO;
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
    }

    public Integer getNombreChargebacks() {
        return nombreChargebacks;
    }

    public void setNombreChargebacks(Integer nombreChargebacks) {
        this.nombreChargebacks = nombreChargebacks;
    }

    public BigDecimal getMontantTotalConteste() {
        return montantTotalConteste;
    }

    public void setMontantTotalConteste(BigDecimal montantTotalConteste) {
        this.montantTotalConteste = montantTotalConteste;
    }

    public String getHistoriqueChargebacks() {
        return historiqueChargebacks;
    }

    public void setHistoriqueChargebacks(String historiqueChargebacks) {
        this.historiqueChargebacks = historiqueChargebacks;
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

    /**
     * Active le flag de litige actif
     */
    public void activerLitige() {
        this.aLitigeActif = true;
    }

    /**
     * Désactive le flag de litige actif
     */
    public void desactiverLitige() {
        this.aLitigeActif = false;
    }

    /**
     * Incrémente le nombre de chargebacks
     */
    public void incrementerChargebacks() {
        if (this.nombreChargebacks == null) {
            this.nombreChargebacks = 0;
        }
        this.nombreChargebacks++;
    }

    /**
     * Décrémente le nombre de chargebacks (minimum 0)
     */
    public void decrementerChargebacks() {
        if (this.nombreChargebacks != null && this.nombreChargebacks > 0) {
            this.nombreChargebacks--;
        }
    }

    /**
     * Ajoute un montant au total contesté
     */
    public void ajouterMontantConteste(BigDecimal montant) {
        if (montant != null && montant.compareTo(BigDecimal.ZERO) > 0) {
            if (this.montantTotalConteste == null) {
                this.montantTotalConteste = BigDecimal.ZERO;
            }
            this.montantTotalConteste = this.montantTotalConteste.add(montant);
        }
    }

    /**
     * Soustrait un montant du total contesté (minimum 0)
     */
    public void soustraireMontantConteste(BigDecimal montant) {
        if (montant != null && montant.compareTo(BigDecimal.ZERO) > 0) {
            if (this.montantTotalConteste != null) {
                this.montantTotalConteste = this.montantTotalConteste.subtract(montant);
                if (this.montantTotalConteste.compareTo(BigDecimal.ZERO) < 0) {
                    this.montantTotalConteste = BigDecimal.ZERO;
                }
            }
        }
    }

    /**
     * Remet à zéro tous les compteurs
     */
    public void reinitialiser() {
        this.aLitigeActif = false;
        this.nombreChargebacks = 0;
        this.montantTotalConteste = BigDecimal.ZERO;
        this.historiqueChargebacks = null;
    }

    /**
     * Ajoute une entrée à l'historique des chargebacks
     */
    public void ajouterHistorique(String entree) {
        if (entree != null && !entree.trim().isEmpty()) {
            if (this.historiqueChargebacks == null || this.historiqueChargebacks.trim().isEmpty()) {
                this.historiqueChargebacks = entree;
            } else {
                this.historiqueChargebacks += "\n" + entree;
            }
        }
    }

    // equals, hashCode et toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransactionChargeback that = (TransactionChargeback) o;
        return Objects.equals(id, that.id) && Objects.equals(transactionId, that.transactionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, transactionId);
    }

    @Override
    public String toString() {
        return "TransactionChargeback{" +
                "id=" + id +
                ", transactionId=" + transactionId +
                ", aLitigeActif=" + aLitigeActif +
                ", nombreChargebacks=" + nombreChargebacks +
                ", montantTotalConteste=" + montantTotalConteste +
                ", dateCreation=" + dateCreation +
                '}';
    }
}