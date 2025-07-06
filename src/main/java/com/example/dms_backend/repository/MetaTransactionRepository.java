package com.example.dms_backend.repository;

import com.example.dms_backend.model.MetaTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    // Ajoutez ces méthodes à votre MetaTransactionRepository.java existant

    /**
     * ✅ NOUVELLE MÉTHODE: Recherche par strCode avec transaction liée
     * Utile pour le signalement de litiges
     */
    @Query("SELECT mt FROM MetaTransaction mt WHERE mt.strCode = :strCode AND mt.transaction IS NOT NULL")
    Optional<MetaTransaction> findByStrCodeWithTransaction(@Param("strCode") Long strCode);

    /**
     * ✅ NOUVELLE MÉTHODE: Recherche toutes les MetaTransactions d'une transaction
     */
    @Query("SELECT mt FROM MetaTransaction mt WHERE mt.transaction.id = :transactionId")
    List<MetaTransaction> findAllByTransactionId(@Param("transactionId") Long transactionId);

    /**
     * ✅ NOUVELLE MÉTHODE: Compter les MetaTransactions sans Transaction liée
     * Utile pour diagnostiquer les problèmes d'import
     */
    @Query("SELECT COUNT(mt) FROM MetaTransaction mt WHERE mt.transaction IS NULL")
    long countOrphanedMetaTransactions();

    /**
     * ✅ NOUVELLE MÉTHODE: Trouver les MetaTransactions sans Transaction liée
     * Pour cleanup/diagnostic
     */
    @Query("SELECT mt FROM MetaTransaction mt WHERE mt.transaction IS NULL")
    List<MetaTransaction> findOrphanedMetaTransactions();

    /**
     * ✅ MÉTHODE DE DEBUG: Trouver par strRecoCode avec informations de liaison
     */
    @Query("SELECT mt FROM MetaTransaction mt LEFT JOIN FETCH mt.transaction WHERE mt.strRecoCode = :strRecoCode")
    List<MetaTransaction> findByStrRecoCodeWithTransaction(@Param("strRecoCode") String strRecoCode);
}