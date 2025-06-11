package com.example.dms_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Litige {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeLitige type;

    @Enumerated(EnumType.STRING)
    private StatutLitige statut;

    private String description;

    private String justificatifPath;

    private LocalDate dateCreation;

    @OneToOne
    @JoinColumn(name = "transaction_id")
    @JsonIgnoreProperties("litige")
    private Transaction transaction;

    @ManyToOne
    @JsonIgnoreProperties("litiges")
    private Utilisateur declarePar;
}
