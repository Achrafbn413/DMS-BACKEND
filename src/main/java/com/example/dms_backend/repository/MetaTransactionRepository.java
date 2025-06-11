package com.example.dms_backend.repository;

import com.example.dms_backend.model.MetaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetaTransactionRepository extends JpaRepository<MetaTransaction, Long> {

    /**
     * Recherche par strRecoCode (String)
     */
    Optional<MetaTransaction> findByStrRecoCode(String strRecoCode);

    /**
     * ✅ AJOUT: Recherche par strCode (Long/Number)
     * Cette méthode est nécessaire pour le signalement de litiges
     */
    Optional<MetaTransaction> findByStrCode(Long strCode);

    /**
     * Vérification d'existence par strRecoCode
     */
    boolean existsByStrRecoCode(String strRecoCode);

    /**
     * ✅ AJOUT: Vérification d'existence par strCode
     */
    boolean existsByStrCode(Long strCode);

    /**
     * Requête personnalisée pour trouver une MetaTransaction
     * liée à une Transaction spécifique
     */
    @Query("SELECT mt FROM MetaTransaction mt WHERE mt.transaction.id = :transactionId")
    Optional<MetaTransaction> findByTransactionId(@Param("transactionId") Long transactionId);
}