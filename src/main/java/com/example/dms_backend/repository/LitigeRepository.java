package com.example.dms_backend.repository;

import com.example.dms_backend.model.Institution;
import com.example.dms_backend.model.Litige;
import com.example.dms_backend.model.StatutLitige;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LitigeRepository extends JpaRepository<Litige, Long> {

    // ✅ Méthode existante
    List<Litige> findByTransactionBanqueAcquereuse(Institution institution);

    // ✅ NOUVEAU: Récupérer tous les litiges pour une institution (émettrice OU acquéreuse)
    @Query("SELECT l FROM Litige l WHERE " +
            "l.transaction.banqueAcquereuse.id = :institutionId OR " +
            "l.transaction.banqueEmettrice.id = :institutionId " +
            "ORDER BY l.dateCreation DESC")
    List<Litige> findByInstitutionId(@Param("institutionId") Long institutionId);

    // ✅ NOUVEAU: Récupérer les litiges non lus pour une institution
    @Query("SELECT l FROM Litige l WHERE " +
            "(l.transaction.banqueAcquereuse.id = :institutionId OR " +
            "l.transaction.banqueEmettrice.id = :institutionId) AND " +
            "l.statut NOT IN ('VU', 'RESOLU', 'FERME') " +
            "ORDER BY l.dateCreation DESC")
    List<Litige> findUnreadByInstitutionId(@Param("institutionId") Long institutionId);

    // ✅ NOUVEAU: Vérifier si une transaction a déjà un litige
    @Query("SELECT COUNT(l) > 0 FROM Litige l WHERE l.transaction.id = :transactionId")
    boolean existsByTransactionId(@Param("transactionId") Long transactionId);

    // ✅ NOUVEAU: Récupérer les litiges par statut pour une institution
    @Query("SELECT l FROM Litige l WHERE " +
            "(l.transaction.banqueAcquereuse.id = :institutionId OR " +
            "l.transaction.banqueEmettrice.id = :institutionId) AND " +
            "l.statut = :statut " +
            "ORDER BY l.dateCreation DESC")
    List<Litige> findByInstitutionIdAndStatut(@Param("institutionId") Long institutionId,
                                              @Param("statut") StatutLitige statut);

    // ✅ NOUVEAU: Compter les litiges non lus pour une institution
    @Query("SELECT COUNT(l) FROM Litige l WHERE " +
            "(l.transaction.banqueAcquereuse.id = :institutionId OR " +
            "l.transaction.banqueEmettrice.id = :institutionId) AND " +
            "l.statut NOT IN ('VU', 'RESOLU', 'FERME')")
    Long countUnreadByInstitutionId(@Param("institutionId") Long institutionId);
}