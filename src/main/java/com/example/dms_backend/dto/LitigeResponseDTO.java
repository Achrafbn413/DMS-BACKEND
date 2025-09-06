package com.example.dms_backend.dto;

import com.example.dms_backend.model.StatutLitige;
import com.example.dms_backend.model.TypeLitige;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LitigeResponseDTO {
    private Long id;
    private TypeLitige type;
    private StatutLitige statut;
    private String description;
    private LocalDate dateCreation;
    private String banqueDeclaranteNom;

    // ✅ Nouveau champ ajouté
    private String institutionDeclarantNom;


}
