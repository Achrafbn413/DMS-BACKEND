package com.example.dms_backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class ParametreSysteme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String valeur;
}