package com.example.dms_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "SATIM_TRANSACTION")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SatimTransaction {

    @Id
    @Column(name = "str_code")
    private Long strCode;

    @Column(name = "str_reco_code")
    private String strRecoCode;

    @Column(name = "str_reco_numb")
    private Long strRecoNumb;

    @Column(name = "str_oper_code")
    private String strOperCode;

    @Column(name = "str_proc_date")
    private LocalDate strProcDate;

    @Column(name = "str_term_iden")
    private String strTermIden;
}
