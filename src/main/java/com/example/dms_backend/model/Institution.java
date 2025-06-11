package com.example.dms_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Institution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @Enumerated(EnumType.STRING)
    private TypeInstitution type;

    // ✅ Spécifie une valeur par défaut en BDD avec "columnDefinition"
    @Column(nullable = false, columnDefinition = "NUMBER(1) DEFAULT 1")
    private boolean enabled = true;
}
