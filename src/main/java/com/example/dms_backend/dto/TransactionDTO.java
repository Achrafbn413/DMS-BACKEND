package com.example.dms_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDTO {
    private Long id;
    private String reference;
    private BigDecimal montant;
    private LocalDate dateTransaction;
    private String type;
    private String statut;
}
