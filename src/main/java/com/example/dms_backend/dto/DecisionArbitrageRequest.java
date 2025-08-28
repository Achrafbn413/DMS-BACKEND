package com.example.dms_backend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionArbitrageRequest {

    private Long litigeId;
    private String decision;           // "FAVORABLE_EMETTEUR" ou "FAVORABLE_ACQUEREUR"
    private String motifsDecision;     // Motifs de la décision
    private String repartitionFrais;   // "PERDANT", "EMETTEUR", "ACQUEREUR", "PARTAGE"

    // Constructeurs et getters/setters générés par Lombok
}