package com.example.dms_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetaTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ✅ Ajout du champ strCode (si pas déjà présent)
    @Column(name = "str_code", unique = true) // Assurer l'unicité
    private Long strCode;

    private String strRecoCode;
    private Long strRecoNumb;
    private String strOperCode;
    private LocalDate strProcDate;
    private String strTermIden;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;
}