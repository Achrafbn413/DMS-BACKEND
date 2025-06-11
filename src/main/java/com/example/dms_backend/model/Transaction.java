package com.example.dms_backend.model;

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
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String reference;

    private LocalDate dateTransaction;

    @Enumerated(EnumType.STRING)
    private TypeTransaction type;

    @Enumerated(EnumType.STRING)
    private StatutTransaction statut;

    private BigDecimal montant;

    @ManyToOne
    @JoinColumn(name = "banque_emettrice_id")
    private Institution banqueEmettrice;

    @ManyToOne
    @JoinColumn(name = "banque_acquereuse_id")
    private Institution banqueAcquereuse;

    @OneToOne(mappedBy = "transaction", cascade = CascadeType.ALL)
    private Litige litige;
    public Transaction(String reference, BigDecimal montant, TypeTransaction type, StatutTransaction statut,
                       LocalDate date, Institution emettrice, Institution acquereuse) {
        this.reference = reference;
        this.montant = montant;
        this.type = type;
        this.statut = statut;
        this.dateTransaction = date;
        this.banqueEmettrice = emettrice;
        this.banqueAcquereuse = acquereuse;
    }

}