package com.example.dms_backend.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "utilisateurs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "utilisateur_seq")
    @SequenceGenerator(name = "utilisateur_seq", sequenceName = "utilisateur_seq", allocationSize = 1)
    private Long id;

    @NotBlank(message = "Le nom est requis.")
    @Column(nullable = false)
    private String nom;

    @NotBlank(message = "L'email est requis.")
    @Email(message = "L'adresse email n'est pas valide.")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Le mot de passe est requis.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false)
    private String password;

    @NotBlank(message = "Le rôle est requis.")
    @Column(nullable = false)
    private String role = "USER";

    @NotNull(message = "L'état 'enabled' est requis.")
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @ManyToOne(optional = true)
    @JoinColumn(name = "institution_id", nullable = true)
    private Institution institution;




    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private NiveauAcces niveaux;



}
