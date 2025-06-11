package com.example.dms_backend.auth;

import com.example.dms_backend.model.NiveauAcces;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;  // Ajouter l'ID utilisateur
    private String nom;
    private String email;
    private String role;
    private NiveauAcces niveaux;
    private String institution;
    private Long institutionId;  // Ajouter l'ID de l'institution
}
