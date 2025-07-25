package com.example.dms_backend.repository;

import com.example.dms_backend.model.SatimTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SatimTransactionRepository extends JpaRepository<SatimTransaction, Long> {

    // ✅ MÉTHODES OPTIONNELLES pour robustesse future

    /**
     * Recherche par strRecoCode (référence transaction)
     */
    Optional<SatimTransaction> findByStrRecoCode(String strRecoCode);

    /**
     * Recherche par strCode (ID SATIM)
     */
    Optional<SatimTransaction> findByStrCode(Long strCode);

    /**
     * Vérification d'existence pour éviter les doublons lors de l'import
     */
    boolean existsByStrCode(Long strCode);
    boolean existsByStrRecoCode(String strRecoCode);

    /**
     * Recherche par terminal (utile pour les analyses)
     */
    List<SatimTransaction> findByStrTermIden(String strTermIden);

    /**
     * Recherche par plage de dates (pour les rapports)
     */
    @Query("SELECT s FROM SatimTransaction s WHERE s.strProcDate BETWEEN :startDate AND :endDate")
    List<SatimTransaction> findByDateRange(@Param("startDate") java.time.LocalDate startDate,
                                           @Param("endDate") java.time.LocalDate endDate);
}