package com.example.dms_backend.repository;

import com.example.dms_backend.model.Institution;
import com.example.dms_backend.model.Litige;
import com.example.dms_backend.model.StatutLitige;
import com.example.dms_backend.model.TypeLitige;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface LitigeRepository extends JpaRepository<Litige, Long> {

    List<Litige> findByTransactionBanqueAcquereuse(Institution institution);

    // ✅ MÉTHODE PRINCIPALE : Tous les litiges d'une institution (émis + reçus)
    @Query("SELECT l FROM Litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante " +
            "LEFT JOIN FETCH l.transaction t " +
            "LEFT JOIN FETCH t.banqueAcquereuse " +
            "LEFT JOIN FETCH t.banqueEmettrice " +
            "LEFT JOIN FETCH l.declarePar dp " +
            "LEFT JOIN FETCH dp.institution " +
            "WHERE (t.banqueAcquereuse.id = :institutionId OR " +
            "t.banqueEmettrice.id = :institutionId) " +
            "ORDER BY l.dateCreation DESC")
    List<Litige> findByInstitutionId(@Param("institutionId") Long institutionId);

    // 🔥 NOUVELLE MÉTHODE : Litiges émis par notre institution
    @Query("SELECT l FROM Litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante bd " +
            "LEFT JOIN FETCH l.transaction t " +
            "LEFT JOIN FETCH t.banqueAcquereuse ba " +
            "LEFT JOIN FETCH t.banqueEmettrice be " +
            "LEFT JOIN FETCH l.declarePar dp " +
            "LEFT JOIN FETCH dp.institution " +
            "WHERE bd.id = :institutionId " +
            "ORDER BY l.dateCreation DESC")
    List<Litige> findLitigesEmisParInstitution(@Param("institutionId") Long institutionId);

    // 🔥 NOUVELLE MÉTHODE : Litiges reçus d'autres banques
    @Query("SELECT l FROM Litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante bd " +
            "LEFT JOIN FETCH l.transaction t " +
            "LEFT JOIN FETCH t.banqueAcquereuse ba " +
            "LEFT JOIN FETCH t.banqueEmettrice be " +
            "LEFT JOIN FETCH l.declarePar dp " +
            "LEFT JOIN FETCH dp.institution " +
            "WHERE (t.banqueAcquereuse.id = :institutionId OR t.banqueEmettrice.id = :institutionId) " +
            "AND (bd.id != :institutionId AND bd.id IS NOT NULL) " +
            "ORDER BY l.dateCreation DESC")
    List<Litige> findLitigesRecusParInstitution(@Param("institutionId") Long institutionId);

    // ✅ ANCIEN ENDPOINT CORRIGÉ (pour compatibilité)
    @Query("SELECT l FROM Litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante bd " +
            "LEFT JOIN FETCH l.transaction t " +
            "LEFT JOIN FETCH t.banqueAcquereuse ba " +
            "LEFT JOIN FETCH t.banqueEmettrice be " +
            "LEFT JOIN FETCH l.declarePar dp " +
            "LEFT JOIN FETCH dp.institution " +
            "WHERE t.banqueAcquereuse.id = :banqueAcquereuseId " +
            "AND (bd.id != :banqueDeclaranteId AND bd.id IS NOT NULL) " +
            "ORDER BY l.dateCreation DESC")
    List<Litige> findLitigesRecusAvecBanqueDeclarante(@Param("banqueAcquereuseId") Long banqueAcquereuseId,
                                                      @Param("banqueDeclaranteId") Long banqueDeclaranteId);

    @Query("SELECT l FROM Litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante bd " +
            "LEFT JOIN FETCH l.transaction t " +
            "LEFT JOIN FETCH t.banqueAcquereuse ba " +
            "LEFT JOIN FETCH t.banqueEmettrice be " +
            "LEFT JOIN FETCH l.declarePar dp " +
            "LEFT JOIN FETCH dp.institution " +
            "WHERE (t.banqueAcquereuse.id = :institutionId OR " +
            "t.banqueEmettrice.id = :institutionId) " +
            "ORDER BY l.dateCreation DESC")
    List<Litige> findAllLitigesByInstitutionWithDetails(@Param("institutionId") Long institutionId);

    // 🔥 NOUVELLE MÉTHODE : Notifications non lues corrigée
    @Query("SELECT l FROM Litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante bd " +
            "LEFT JOIN FETCH l.transaction t " +
            "LEFT JOIN FETCH t.banqueAcquereuse ba " +
            "LEFT JOIN FETCH t.banqueEmettrice be " +
            "LEFT JOIN FETCH l.declarePar dp " +
            "LEFT JOIN FETCH dp.institution " +
            "WHERE (t.banqueAcquereuse.id = :institutionId OR t.banqueEmettrice.id = :institutionId) " +
            "AND (bd.id != :institutionId AND bd.id IS NOT NULL) " +
            "AND l.statut NOT IN (com.example.dms_backend.model.StatutLitige.VU, " +
            "com.example.dms_backend.model.StatutLitige.RESOLU, " +
            "com.example.dms_backend.model.StatutLitige.FERME) " +
            "ORDER BY l.dateCreation DESC")
    List<Litige> findNotificationsNonLues(@Param("institutionId") Long institutionId);

    // ✅ ANCIENNE MÉTHODE (gardée pour compatibilité)
    @Query("SELECT l FROM Litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante " +
            "LEFT JOIN FETCH l.transaction t " +
            "LEFT JOIN FETCH t.banqueAcquereuse " +
            "LEFT JOIN FETCH t.banqueEmettrice " +
            "LEFT JOIN FETCH l.declarePar dp " +
            "LEFT JOIN FETCH dp.institution " +
            "WHERE (t.banqueAcquereuse.id = :institutionId OR " +
            "t.banqueEmettrice.id = :institutionId) AND " +
            "l.statut NOT IN (com.example.dms_backend.model.StatutLitige.VU, " +
            "com.example.dms_backend.model.StatutLitige.RESOLU, " +
            "com.example.dms_backend.model.StatutLitige.FERME) " +
            "ORDER BY l.dateCreation DESC")
    List<Litige> findUnreadByInstitutionId(@Param("institutionId") Long institutionId);

    // ✅ Les méthodes existantes restent inchangées
    @Query("SELECT COUNT(l) > 0 FROM Litige l WHERE l.transaction.id = :transactionId")
    boolean existsByTransactionId(@Param("transactionId") Long transactionId);

    @Query("SELECT COUNT(l) > 0 FROM Litige l WHERE l.transaction.id = :transactionId AND l.declarePar.id = :userId")
    boolean existsByTransactionIdAndUserId(@Param("transactionId") Long transactionId, @Param("userId") Long userId);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT l FROM Litige l WHERE l.transaction.id = :transactionId AND l.declarePar.id = :userId")
    Optional<Litige> findByTransactionIdAndUserId(@Param("transactionId") Long transactionId, @Param("userId") Long userId);

    @Query("SELECT l FROM Litige l WHERE l.transaction.id = :transactionId AND l.declarePar.id = :userId")
    Optional<Litige> findByTransactionIdAndUserIdNoLock(@Param("transactionId") Long transactionId, @Param("userId") Long userId);

    @Query("SELECT l FROM Litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante " +
            "LEFT JOIN FETCH l.transaction t " +
            "LEFT JOIN FETCH t.banqueAcquereuse " +
            "LEFT JOIN FETCH t.banqueEmettrice " +
            "LEFT JOIN FETCH l.declarePar dp " +
            "LEFT JOIN FETCH dp.institution " +
            "WHERE (t.banqueAcquereuse.id = :institutionId OR " +
            "t.banqueEmettrice.id = :institutionId) AND " +
            "l.statut = :statut " +
            "ORDER BY l.dateCreation DESC")
    List<Litige> findByInstitutionIdAndStatut(@Param("institutionId") Long institutionId,
                                              @Param("statut") StatutLitige statut);

    @Query("SELECT COUNT(l) FROM Litige l WHERE " +
            "(l.transaction.banqueAcquereuse.id = :institutionId OR " +
            "l.transaction.banqueEmettrice.id = :institutionId) AND " +
            "l.statut NOT IN (com.example.dms_backend.model.StatutLitige.VU, " +
            "com.example.dms_backend.model.StatutLitige.RESOLU, " +
            "com.example.dms_backend.model.StatutLitige.FERME)")
    Long countUnreadByInstitutionId(@Param("institutionId") Long institutionId);

    @Query("SELECT l FROM Litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante " +
            "LEFT JOIN FETCH l.transaction t " +
            "LEFT JOIN FETCH t.banqueAcquereuse " +
            "LEFT JOIN FETCH t.banqueEmettrice " +
            "WHERE l.declarePar.id = :userId " +
            "ORDER BY l.dateCreation DESC")
    List<Litige> findByDeclarePar(@Param("userId") Long userId);

    @Query("SELECT l FROM Litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante " +
            "LEFT JOIN FETCH l.transaction t " +
            "LEFT JOIN FETCH t.banqueAcquereuse " +
            "LEFT JOIN FETCH t.banqueEmettrice " +
            "LEFT JOIN FETCH l.declarePar dp " +
            "LEFT JOIN FETCH dp.institution " +
            "WHERE (t.banqueAcquereuse.id = :institutionId OR " +
            "t.banqueEmettrice.id = :institutionId) AND " +
            "l.type = :type " +
            "ORDER BY l.dateCreation DESC")
    List<Litige> findByInstitutionIdAndType(@Param("institutionId") Long institutionId,
                                            @Param("type") TypeLitige type);

    @Query("SELECT l FROM Litige l WHERE l.transaction.id = :transactionId")
    List<Litige> findAllByTransactionId(@Param("transactionId") Long transactionId);

    @Query(value = """
        SELECT EXISTS(
            SELECT 1 FROM litige l 
            WHERE l.transaction_id = :transactionId 
            AND l.declare_par_id = :userId
        )
    """, nativeQuery = true)
    boolean existsByTransactionAndUserNative(@Param("transactionId") Long transactionId, @Param("userId") Long userId);

    @Query("SELECT l.transaction.id FROM Litige l WHERE l.declarePar.id = :userId")
    List<Long> findTransactionIdsByUser(@Param("userId") Long userId);

    @Query("SELECT l FROM Litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante " +
            "LEFT JOIN FETCH l.transaction t " +
            "LEFT JOIN FETCH t.banqueAcquereuse " +
            "LEFT JOIN FETCH t.banqueEmettrice " +
            "WHERE l.declarePar.id = :userId " +
            "ORDER BY l.dateCreation DESC")
    List<Litige> findByDeclareParId(@Param("userId") Long userId);

    @Deprecated
    List<Litige> findByTransaction_BanqueAcquereuse_IdAndBanqueDeclarante_IdNot(Long banqueAcquereuseId, Long banqueDeclaranteId);
}