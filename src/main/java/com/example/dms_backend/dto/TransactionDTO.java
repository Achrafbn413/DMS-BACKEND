package com.example.dms_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TransactionDTO {
    private Long id;
    private String reference;
    private BigDecimal montant;
    private LocalDate dateTransaction;
    private String type;
    private String statut;
    private String banqueEmettrice;
    private String banqueAcquereuse;

    // ✅ Ajout du champ pour la banque déclarante (si litige)
    private String banqueDeclarante;


}
