package com.example.dms_backend.auth;

import com.example.dms_backend.model.NiveauAcces;
import com.example.dms_backend.model.TypeRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Le nom est requis.")
    private String nom;

    @NotBlank(message = "L'email est requis.")
    @Email(message = "L'email est invalide.")
    private String email;

    @NotBlank(message = "Le mot de passe est requis.")
    private String password;

    @NotNull(message = "Le rôle est requis.")
    private TypeRole role;

    @NotNull(message = "Le niveau d'accès est requis.")
    private NiveauAcces niveaux;

    @NotNull(message = "L'ID de l'institution est requis.")
    private Long institutionId;
}
