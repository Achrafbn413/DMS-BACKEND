package com.example.dms_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "transaction")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String reference;

    @Column(name = "date_transaction", nullable = false)
    private LocalDate dateTransaction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeTransaction type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTransaction statut;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal montant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banque_emettrice_id")
    @JsonIgnoreProperties({"transactions", "litiges"})
    private Institution banqueEmettrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banque_acquereuse_id")
    @JsonIgnoreProperties({"transactions", "litiges"})
    private Institution banqueAcquereuse;

    /**
     * ✅ SOLUTION 1: Relation OneToOne optimisée SANS cascade automatique
     * Le cascade peut causer des insertions multiples
     */
    @OneToOne(mappedBy = "transaction", fetch = FetchType.LAZY)
    @JsonIgnoreProperties("transaction")
    private Litige litige;



    /**
     * ✅ SOLUTION 2: Constructeur optimisé
     */
    public Transaction(String reference, BigDecimal montant, TypeTransaction type,
                       StatutTransaction statut, LocalDate date, Institution emettrice,
                       Institution acquereuse) {
        this.reference = reference;
        this.montant = montant;
        this.type = type;
        this.statut = statut;
        this.dateTransaction = date;
        this.banqueEmettrice = emettrice;
        this.banqueAcquereuse = acquereuse;
    }

    /**
     * ✅ SOLUTION 3: Méthode utilitaire pour vérifier si transaction a un litige
     */
    public boolean hasLitige() {
        return litige != null;
    }

    /**
     * ✅ SOLUTION 4: Validation métier
     */
    @PrePersist
    public void prePersist() {
        if (dateTransaction == null) {
            dateTransaction = LocalDate.now();
        }
        if (statut == null) {
            statut = StatutTransaction.NORMALE;
        }
        if (montant == null) {
            montant = BigDecimal.ZERO;
        }
    }

    /**
     * ✅ SOLUTION 5: Equals et hashCode sur reference unique
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transaction)) return false;
        Transaction that = (Transaction) o;
        return reference != null && reference.equals(that.reference);
    }

    @Override
    public int hashCode() {
        return reference != null ? reference.hashCode() : 0;
    }
}