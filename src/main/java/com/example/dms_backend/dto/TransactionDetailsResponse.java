package com.example.dms_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDetailsResponse {

    // Informations principales de la transaction
    private Long id;
    private String reference;
    private BigDecimal montant;
    private LocalDate dateTransaction;
    private String type;
    private String statut;

    // Informations bancaires
    private BanqueInfo banqueEmettrice;
    private BanqueInfo banqueAcquereuse;

    // Données SATIM enrichies
    private SatimDataDTO satimData;

    // Informations META
    private MetaDataDTO metaData;

    // Litige associé (si existe)
    private LitigeBasicInfo litige;

    // Métadonnées calculées
    private Long dureeDepuisCreationJours;
    private String priorite;
    private Boolean aLitige;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BanqueInfo {
        private Long id;
        private String nom;
        private String code;
        private String type;
        private Boolean enabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SatimDataDTO {
        private Long strCode;
        private String strRecoCode;
        private Double strRecoNumb;
        private String strProcDate;
        private String strOperCode;
        private String strTermIden;
        private String strIssuBanCode;
        private String strAcquBanCode;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetaDataDTO {
        private Long id;
        private Long strCode;
        private String strRecoCode;
        private Double strRecoNumb;
        private String strOperCode;
        private String strProcDate;
        private String strTermIden;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LitigeBasicInfo {
        private Long id;
        private String type;
        private String statut;
        private String description;
        private LocalDate dateCreation;
        private String banqueDeclaranteNom;
        private String utilisateurCreateur;
    }
}