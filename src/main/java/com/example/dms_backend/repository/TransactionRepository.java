package com.example.dms_backend.repository;

import com.example.dms_backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // ✅ MÉTHODES EXISTANTES (gardées)
    boolean existsByReference(String reference);
    Optional<Transaction> findByReference(String reference);

    // ✅ MÉTHODE EXISTANTE AMÉLIORÉE : Plus robuste avec gestion des nulls
    @Query("SELECT t FROM Transaction t " +
            "WHERE (t.banqueEmettrice IS NOT NULL AND LOWER(t.banqueEmettrice.nom) = LOWER(:institution)) " +
            "OR (t.banqueAcquereuse IS NOT NULL AND LOWER(t.banqueAcquereuse.nom) = LOWER(:institution))")
    List<Transaction> findByBanqueEmettriceNomOrBanqueAcquereuseNom(@Param("institution") String institution1, @Param("institution") String institution2);

    // ✅ NOUVELLE MÉTHODE : Recherche par ID (plus efficace)
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.banqueEmettrice.id = :emettriceId OR t.banqueAcquereuse.id = :acquereuseId")
    List<Transaction> findByBanqueEmettriceIdOrBanqueAcquereuseId(@Param("emettriceId") Long emettriceId, @Param("acquereuseId") Long acquereuseId);

    // ✅ NOUVELLE MÉTHODE : Chargement optimisé avec toutes les relations (évite N+1)
    @Query("SELECT DISTINCT t FROM Transaction t " +
            "LEFT JOIN FETCH t.litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante " +
            "LEFT JOIN FETCH l.declarePar dp " +
            "LEFT JOIN FETCH dp.institution " +
            "LEFT JOIN FETCH t.banqueEmettrice " +
            "LEFT JOIN FETCH t.banqueAcquereuse " +
            "ORDER BY t.dateTransaction DESC")
    List<Transaction> findAllWithLitigeInfo();

    // ✅ NOUVELLE MÉTHODE : Transactions avec litiges seulement
    @Query("SELECT DISTINCT t FROM Transaction t " +
            "INNER JOIN FETCH t.litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante " +
            "LEFT JOIN FETCH l.declarePar dp " +
            "LEFT JOIN FETCH dp.institution " +
            "LEFT JOIN FETCH t.banqueEmettrice " +
            "LEFT JOIN FETCH t.banqueAcquereuse " +
            "ORDER BY t.dateTransaction DESC")
    List<Transaction> findAllWithLitiges();

    // ✅ NOUVELLE MÉTHODE : Transactions par institution avec optimisation
    @Query("SELECT DISTINCT t FROM Transaction t " +
            "LEFT JOIN FETCH t.litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante " +
            "LEFT JOIN FETCH l.declarePar dp " +
            "LEFT JOIN FETCH dp.institution " +
            "LEFT JOIN FETCH t.banqueEmettrice " +
            "LEFT JOIN FETCH t.banqueAcquereuse " +
            "WHERE t.banqueEmettrice.id = :institutionId OR t.banqueAcquereuse.id = :institutionId " +
            "ORDER BY t.dateTransaction DESC")
    List<Transaction> findByInstitutionIdWithLitigeInfo(@Param("institutionId") Long institutionId);

    // ✅ NOUVELLE MÉTHODE : Transactions sans litige
    @Query("SELECT t FROM Transaction t " +
            "WHERE t.litige IS NULL " +
            "ORDER BY t.dateTransaction DESC")
    List<Transaction> findTransactionsWithoutLitige();

    // ✅ NOUVELLE MÉTHODE : Compter les transactions par institution
    @Query("SELECT COUNT(t) FROM Transaction t " +
            "WHERE t.banqueEmettrice.id = :institutionId OR t.banqueAcquereuse.id = :institutionId")
    Long countByInstitutionId(@Param("institutionId") Long institutionId);

    // ✅ NOUVELLE MÉTHODE : Compter les transactions avec litiges par institution
    @Query("SELECT COUNT(t) FROM Transaction t " +
            "WHERE (t.banqueEmettrice.id = :institutionId OR t.banqueAcquereuse.id = :institutionId) " +
            "AND t.litige IS NOT NULL")
    Long countTransactionsWithLitigeByInstitution(@Param("institutionId") Long institutionId);

    // ✅ NOUVELLE MÉTHODE : Recherche par référence avec relations
    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN FETCH t.litige l " +
            "LEFT JOIN FETCH l.banqueDeclarante " +
            "LEFT JOIN FETCH t.banqueEmettrice " +
            "LEFT JOIN FETCH t.banqueAcquereuse " +
            "WHERE t.reference = :reference")
    Optional<Transaction> findByReferenceWithDetails(@Param("reference") String reference);

    // ✅ NOUVELLE MÉTHODE : Recherche par plusieurs références (utile pour import)
    @Query("SELECT t FROM Transaction t WHERE t.reference IN :references")
    List<Transaction> findByReferences(@Param("references") List<String> references);

    // ✅ NOUVELLE MÉTHODE : Transactions récentes (VERSION ORACLE CORRIGÉE)
    @Query(value = "SELECT DISTINCT t.* FROM TRANSACTION t " +
            "LEFT JOIN LITIGE l ON t.id = l.transaction_id " +
            "LEFT JOIN INSTITUTION bd ON l.banque_declarante_id = bd.id " +
            "LEFT JOIN INSTITUTION be ON t.banque_emettrice_id = be.id " +
            "LEFT JOIN INSTITUTION ba ON t.banque_acquereuse_id = ba.id " +
            "WHERE t.date_transaction >= TRUNC(SYSDATE) - :days " +
            "ORDER BY t.date_transaction DESC",
            nativeQuery = true)
    List<Transaction> findRecentTransactions(@Param("days") int days);

    // ====================================================================
// 🆕 NOUVELLE MÉTHODE POUR FONCTIONNALITÉ DÉTAILS TRANSACTION
// ====================================================================

    /**
     * ✅ NOUVELLE MÉTHODE : Récupérer les premiers IDs pour debug
     * Utilisée pour diagnostiquer les IDs disponibles en cas d'erreur 404
     */
    @Query("SELECT t.id FROM Transaction t ORDER BY t.id ASC")
    List<Long> findAllIds();
}