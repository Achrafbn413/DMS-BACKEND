package com.example.dms_backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "L'email est requis.")
    @Email(message = "L'email est invalide.")
    private String email;

    @NotBlank(message = "Le mot de passe est requis.")
    private String password;
}
