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
@Table(
        name = "litige",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"transaction_id", "declare_par_id"},
                name = "uk_litige_transaction_user"
        )
)
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Litige {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeLitige type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutLitige statut;

    @Column(length = 1000)
    private String description;

    @Column(name = "justificatif_path")
    private String justificatifPath;

    @Column(name = "date_creation", nullable = false)
    private LocalDate dateCreation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    @JsonIgnoreProperties("litige")
    private Transaction transaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "declare_par_id", nullable = false)
    @JsonIgnoreProperties("litiges")
    private Utilisateur declarePar;

    // 🔥 CORRECTION PRINCIPALE : Ajouter FetchType.EAGER et JsonIgnoreProperties
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "banque_declarante_id")
    @JsonIgnoreProperties({"litiges", "utilisateurs", "transactions"})
    private Institution banqueDeclarante;

    @Column(name = "date_resolution")
    private LocalDate dateResolution;

    @Column(name = "banque_d")
    private Long banqueD;


    /**
     * ✅ Méthode utilitaire pour obtenir le nom de l'utilisateur déclarant
     * Ne pas override le getter Lombok !
     */
    public String getNomDeclarant() {
        return declarePar != null ? declarePar.getNom() : "Utilisateur inconnu";
    }

    /**
     * ✅ Méthode utilitaire pour obtenir le nom de l'institution du déclarant
     */
    public String getNomInstitutionDeclarant() {
        return declarePar != null && declarePar.getInstitution() != null
                ? declarePar.getInstitution().getNom()
                : null;
    }

    @PrePersist
    public void prePersist() {
        if (dateCreation == null) {
            dateCreation = LocalDate.now();
        }
        if (statut == null) {
            statut = StatutLitige.OUVERT;
        }
        if (type == null) {
            type = TypeLitige.AUTRE;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Litige)) return false;
        Litige litige = (Litige) o;
        return transaction != null && declarePar != null &&
                transaction.getId() != null && declarePar.getId() != null &&
                transaction.getId().equals(litige.transaction.getId()) &&
                declarePar.getId().equals(litige.declarePar.getId());
    }

    @Override
    public int hashCode() {
        return transaction != null && declarePar != null ?
                transaction.getId().hashCode() + declarePar.getId().hashCode() : 0;
    }
}