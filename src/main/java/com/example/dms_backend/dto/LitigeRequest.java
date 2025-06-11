package com.example.dms_backend.dto;

import com.example.dms_backend.model.TypeLitige;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LitigeRequest {
    private Long transactionId;
    private Long utilisateurId;
    private String description;
    private TypeLitige type;
}
