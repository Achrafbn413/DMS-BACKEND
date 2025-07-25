package com.example.dms_backend.dto;

import com.example.dms_backend.model.StatutLitige;
import com.example.dms_backend.model.TypeLitige;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LitigeDetailsResponse {

    // Informations principales du litige
    private Long id;
    private TypeLitige type;
    private StatutLitige statut;
    private String description;
    private LocalDate dateCreation; // ✅ DATE (pas TIMESTAMP dans votre BD)
    private LocalDate dateResolution;
    private String banqueDeclaranteNom;
    private String utilisateurCreateur;
    private String justificatifPath;

    // Détails de la transaction associée
    private TransactionCompleteDetails transaction;

    // Métadonnées calculées
    private Long dureeDepuisCreationMinutes;
    private String priorite;
    private Boolean estLu;
    private Boolean peutEtreModifie;

    // Pour futures améliorations
    private List<String> historique;
    private List<String> actions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionCompleteDetails {
        private Long id;
        private String reference;
        private Double montant;
        private LocalDate dateTransaction; // ✅ DATE dans votre BD
        private String type;
        private String statut;
        private BanqueInfo banqueEmettrice;
        private BanqueInfo banqueAcquereuse;
        private SatimDataDTO satimData;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BanqueInfo {
        private Long id;
        private String nom;
        private String code;
        private String adresse;
        private String telephone;
        private String email;
        private String type;
        private Boolean enabled;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SatimDataDTO {
        // Champs principaux de SATIM_TRANSACTION
        private Long strCode;
        private String strRecoCode;
        private Double strRecoNumb;
        private String strProcDate;
        private String strOperCode;
        private String strTermIden;
        private String strIssuBanCode;
        private String strAcquBanCode;
        private String strAuthNumb;
        private String strMercIden;
        private String strMercLoca;
        private Double strPurcAmt;
        private String strCardNumb;
        private Long transactionId;
    }
}