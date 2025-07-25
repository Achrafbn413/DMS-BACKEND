package com.example.dms_backend.repository;

import com.example.dms_backend.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Utilisateur, Long> {

    // ✅ MÉTHODE EXISTANTE (gardée)
    Optional<Utilisateur> findByEmail(String email);

    // ✅ NOUVELLE MÉTHODE CRITIQUE : Chargement avec institution (évite lazy loading)
    @Query("SELECT u FROM Utilisateur u " +
            "LEFT JOIN FETCH u.institution " +
            "WHERE u.email = :email")
    Optional<Utilisateur> findByEmailWithInstitution(@Param("email") String email);

    // ✅ NOUVELLE MÉTHODE : Chargement par ID avec institution
    @Query("SELECT u FROM Utilisateur u " +
            "LEFT JOIN FETCH u.institution " +
            "WHERE u.id = :id")
    Optional<Utilisateur> findByIdWithInstitution(@Param("id") Long id);

    // ✅ NOUVELLE MÉTHODE : Vérifier existence par email
    boolean existsByEmail(String email);

    // ✅ NOUVELLE MÉTHODE : Tous les utilisateurs d'une institution
    @Query("SELECT u FROM Utilisateur u " +
            "LEFT JOIN FETCH u.institution " +
            "WHERE u.institution.id = :institutionId")
    List<Utilisateur> findByInstitutionId(@Param("institutionId") Long institutionId);

    // ✅ NOUVELLE MÉTHODE : Utilisateurs d'une institution par nom
    @Query("SELECT u FROM Utilisateur u " +
            "LEFT JOIN FETCH u.institution i " +
            "WHERE i.nom = :institutionNom")
    List<Utilisateur> findByInstitutionNom(@Param("institutionNom") String institutionNom);

    // ✅ NOUVELLE MÉTHODE : Utilisateurs actifs avec institution
    @Query("SELECT u FROM Utilisateur u " +
            "LEFT JOIN FETCH u.institution " +
            "WHERE u.enabled = true")
    List<Utilisateur> findActiveUsersWithInstitution();

    // ✅ NOUVELLE MÉTHODE : Compter utilisateurs par institution
    @Query("SELECT COUNT(u) FROM Utilisateur u WHERE u.institution.id = :institutionId")
    Long countByInstitutionId(@Param("institutionId") Long institutionId);

    // ✅ NOUVELLE MÉTHODE : Utilisateurs par rôle et institution
    @Query("SELECT u FROM Utilisateur u " +
            "LEFT JOIN FETCH u.institution " +
            "WHERE u.role = :role AND u.institution.id = :institutionId")
    List<Utilisateur> findByRoleAndInstitutionId(@Param("role") String role, @Param("institutionId") Long institutionId);

    // ✅ NOUVELLE MÉTHODE : Recherche utilisateurs avec institution non nulle
    @Query("SELECT u FROM Utilisateur u " +
            "LEFT JOIN FETCH u.institution " +
            "WHERE u.institution IS NOT NULL")
    List<Utilisateur> findUsersWithInstitution();

    // ✅ NOUVELLE MÉTHODE : Utilisateurs sans institution (pour debug)
    @Query("SELECT u FROM Utilisateur u WHERE u.institution IS NULL")
    List<Utilisateur> findUsersWithoutInstitution();
}