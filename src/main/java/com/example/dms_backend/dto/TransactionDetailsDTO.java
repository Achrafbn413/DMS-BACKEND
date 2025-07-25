package com.example.dms_backend.dto;

import com.example.dms_backend.model.Litige;
import com.example.dms_backend.model.MetaTransaction;
import com.example.dms_backend.model.SatimTransaction;
import com.example.dms_backend.model.Transaction;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailsDTO {
    private Transaction transaction;
    private MetaTransaction metaTransaction;
    private SatimTransaction satimTransaction;
    private Litige litige;
}
