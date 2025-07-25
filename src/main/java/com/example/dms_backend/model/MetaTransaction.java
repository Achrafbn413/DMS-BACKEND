
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
@Table(
        name = "meta_transaction",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"str_code"},
                name = "uk_meta_transaction_str_code"
        )
)
public class MetaTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ✅ SOLUTION 1: Contrainte d'unicité sur strCode
     */
    @Column(name = "str_code", unique = true, nullable = false)
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

    /**
     * ✅ SOLUTION 2: Relation OneToOne optimisée
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    /**
     * ✅ SOLUTION 3: Equals et hashCode sur strCode unique
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MetaTransaction)) return false;
        MetaTransaction that = (MetaTransaction) o;
        return strCode != null && strCode.equals(that.strCode);
    }

    @Override
    public int hashCode() {
        return strCode != null ? strCode.hashCode() : 0;
    }
}