package com.example.dms_backend.repository;

import com.example.dms_backend.model.TransactionChargeback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionChargebackRepository extends JpaRepository<TransactionChargeback, Long> {

    // ====================================================================
    // 🔍 RECHERCHES PAR TRANSACTION
    // ====================================================================

    Optional<TransactionChargeback> findByTransactionId(Long transactionId);

    boolean existsByTransactionId(Long transactionId);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.transactionId IN :transactionIds " +
            "ORDER BY tc.dateCreation DESC")
    List<TransactionChargeback> findByTransactionIds(@Param("transactionIds") List<Long> transactionIds);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.aLitigeActif = true " +
            "ORDER BY tc.dateModification DESC")
    List<TransactionChargeback> findTransactionsAvecLitigeActif();

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.aLitigeActif = false " +
            "ORDER BY tc.dateCreation DESC")
    List<TransactionChargeback> findTransactionsSansLitigeActif();

    @Query("SELECT COUNT(tc) FROM TransactionChargeback tc WHERE tc.aLitigeActif = true")
    long countTransactionsAvecLitigeActif();

    @Query("SELECT COUNT(tc) FROM TransactionChargeback tc WHERE tc.aLitigeActif = false")
    long countTransactionsSansLitigeActif();

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.aLitigeActif = true " +
            "AND tc.dateModification >= :dateLimit " +
            "ORDER BY tc.dateModification DESC")
    List<TransactionChargeback> findLitigesActifsRecents(@Param("dateLimit") LocalDateTime dateLimit);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.nombreChargebacks > 0 " +
            "ORDER BY tc.nombreChargebacks DESC")
    List<TransactionChargeback> findTransactionsAvecChargebacks();

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.nombreChargebacks = 0 " +
            "ORDER BY tc.dateCreation DESC")
    List<TransactionChargeback> findTransactionsSansChargebacks();

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.nombreChargebacks = :nombre " +
            "ORDER BY tc.dateModification DESC")
    List<TransactionChargeback> findByNombreChargebacks(@Param("nombre") Integer nombre);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.nombreChargebacks >= :nombreMin " +
            "ORDER BY tc.nombreChargebacks DESC")
    List<TransactionChargeback> findByNombreChargebacksGreaterThanEqual(@Param("nombreMin") Integer nombreMin);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.nombreChargebacks BETWEEN :nombreMin AND :nombreMax " +
            "ORDER BY tc.nombreChargebacks DESC")
    List<TransactionChargeback> findByNombreChargebacksBetween(@Param("nombreMin") Integer nombreMin,
                                                               @Param("nombreMax") Integer nombreMax);

    @Query("SELECT tc.nombreChargebacks, COUNT(tc) " +
            "FROM TransactionChargeback tc " +
            "GROUP BY tc.nombreChargebacks " +
            "ORDER BY tc.nombreChargebacks")
    List<Object[]> getStatistiquesParNombreChargebacks();

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.nombreChargebacks > 1 " +
            "ORDER BY tc.nombreChargebacks DESC, tc.dateModification DESC")
    List<TransactionChargeback> findTransactionsMultiplesChargebacks();

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.montantTotalConteste > 0 " +
            "ORDER BY tc.montantTotalConteste DESC")
    List<TransactionChargeback> findTransactionsAvecMontantConteste();

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.montantTotalConteste = 0 " +
            "ORDER BY tc.dateCreation DESC")
    List<TransactionChargeback> findTransactionsSansMontantConteste();

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.montantTotalConteste > :montantSeuil " +
            "ORDER BY tc.montantTotalConteste DESC")
    List<TransactionChargeback> findByMontantTotalContesteGreaterThan(@Param("montantSeuil") BigDecimal montantSeuil);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.montantTotalConteste BETWEEN :montantMin AND :montantMax " +
            "ORDER BY tc.montantTotalConteste DESC")
    List<TransactionChargeback> findByMontantTotalContesteBetween(@Param("montantMin") BigDecimal montantMin,
                                                                  @Param("montantMax") BigDecimal montantMax);

    @Query("SELECT " +
            "SUM(tc.montantTotalConteste) as somme, " +
            "AVG(tc.montantTotalConteste) as moyenne, " +
            "MIN(tc.montantTotalConteste) as minimum, " +
            "MAX(tc.montantTotalConteste) as maximum " +
            "FROM TransactionChargeback tc " +
            "WHERE tc.montantTotalConteste > 0")
    Object[] getStatistiquesMontants();

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.montantTotalConteste > 0 " +
            "ORDER BY tc.montantTotalConteste DESC " +
            "LIMIT 10")
    List<TransactionChargeback> findTopMontantsContestes();

    // ====================================================================
    // 📅 RECHERCHES PAR PÉRIODE
    // ====================================================================

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.dateCreation BETWEEN :dateDebut AND :dateFin " +
            "ORDER BY tc.dateCreation DESC")
    List<TransactionChargeback> findByPeriodeCreation(@Param("dateDebut") LocalDateTime dateDebut,
                                                      @Param("dateFin") LocalDateTime dateFin);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.dateModification BETWEEN :dateDebut AND :dateFin " +
            "ORDER BY tc.dateModification DESC")
    List<TransactionChargeback> findByPeriodeModification(@Param("dateDebut") LocalDateTime dateDebut,
                                                          @Param("dateFin") LocalDateTime dateFin);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.dateCreation >= :dateLimit " +
            "ORDER BY tc.dateCreation DESC")
    List<TransactionChargeback> findTransactionsRecentes(@Param("dateLimit") LocalDateTime dateLimit);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.dateModification >= :dateLimit " +
            "ORDER BY tc.dateModification DESC")
    List<TransactionChargeback> findTransactionsModifieesRecemment(@Param("dateLimit") LocalDateTime dateLimit);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.dateCreation >= CURRENT_TIMESTAMP " +
            "ORDER BY tc.dateCreation DESC")
    List<TransactionChargeback> findTransactionsAujourdHui();

    @Query("SELECT YEAR(tc.dateCreation), MONTH(tc.dateCreation), COUNT(tc) " +
            "FROM TransactionChargeback tc " +
            "WHERE tc.dateCreation BETWEEN :dateDebut AND :dateFin " +
            "GROUP BY YEAR(tc.dateCreation), MONTH(tc.dateCreation) " +
            "ORDER BY YEAR(tc.dateCreation), MONTH(tc.dateCreation)")
    List<Object[]> getEvolutionMensuelle(@Param("dateDebut") LocalDateTime dateDebut,
                                         @Param("dateFin") LocalDateTime dateFin);

    // ====================================================================
    // 📝 RECHERCHES PAR HISTORIQUE - SECTION CORRIGÉE
    // ====================================================================

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.historiqueChargebacks IS NOT NULL " +
            "AND LENGTH(tc.historiqueChargebacks) > 0 " +
            "ORDER BY tc.dateModification DESC")
    List<TransactionChargeback> findTransactionsAvecHistorique();

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.historiqueChargebacks IS NULL " +
            "OR LENGTH(tc.historiqueChargebacks) = 0 " +
            "ORDER BY tc.dateCreation DESC")
    List<TransactionChargeback> findTransactionsSansHistorique();

    /**
     * 🔍 RECHERCHE HISTORIQUE : Recherche dans l'historique - VERSION CORRIGÉE
     * ✅ CORRECTION : Suppression de LOWER() pour éviter l'erreur CLOB
     */
    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.historiqueChargebacks IS NOT NULL " +
            "AND tc.historiqueChargebacks LIKE CONCAT('%', :motCle, '%') " +
            "ORDER BY tc.dateModification DESC")
    List<TransactionChargeback> findByHistoriqueContaining(@Param("motCle") String motCle);

    // ====================================================================
    // 🏦 RECHERCHES PAR INSTITUTION
    // ====================================================================

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "JOIN Transaction t ON tc.transactionId = t.id " +
            "WHERE t.banqueEmettrice.id = :institutionId " +
            "ORDER BY tc.dateModification DESC")
    List<TransactionChargeback> findByBanqueEmettriceId(@Param("institutionId") Long institutionId);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "JOIN Transaction t ON tc.transactionId = t.id " +
            "WHERE t.banqueAcquereuse.id = :institutionId " +
            "ORDER BY tc.dateModification DESC")
    List<TransactionChargeback> findByBanqueAcquereuseId(@Param("institutionId") Long institutionId);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "JOIN Transaction t ON tc.transactionId = t.id " +
            "WHERE t.banqueEmettrice.id = :institutionId " +
            "OR t.banqueAcquereuse.id = :institutionId " +
            "ORDER BY tc.dateModification DESC")
    List<TransactionChargeback> findByInstitutionId(@Param("institutionId") Long institutionId);

    @Query("SELECT " +
            "CASE WHEN t.banqueEmettrice.id = :institutionId THEN 'EMETTRICE' ELSE 'ACQUEREUR' END as role, " +
            "COUNT(tc) as nombre, " +
            "SUM(tc.nombreChargebacks) as totalChargebacks, " +
            "SUM(tc.montantTotalConteste) as montantTotal " +
            "FROM TransactionChargeback tc " +
            "JOIN Transaction t ON tc.transactionId = t.id " +
            "WHERE t.banqueEmettrice.id = :institutionId OR t.banqueAcquereuse.id = :institutionId " +
            "GROUP BY CASE WHEN t.banqueEmettrice.id = :institutionId THEN 'EMETTRICE' ELSE 'ACQUEREUR' END")
    List<Object[]> getStatistiquesParInstitution(@Param("institutionId") Long institutionId);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "JOIN Transaction t ON tc.transactionId = t.id " +
            "WHERE t.reference = :reference")
    Optional<TransactionChargeback> findByTransactionReference(@Param("reference") String reference);

    // ====================================================================
    // 🔍 RECHERCHES COMPLEXES MULTICRITÈRES
    // ====================================================================

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE (:litigeActif IS NULL OR tc.aLitigeActif = :litigeActif) " +
            "AND (:nombreMin IS NULL OR tc.nombreChargebacks >= :nombreMin) " +
            "AND (:nombreMax IS NULL OR tc.nombreChargebacks <= :nombreMax) " +
            "AND (:montantMin IS NULL OR tc.montantTotalConteste >= :montantMin) " +
            "AND (:montantMax IS NULL OR tc.montantTotalConteste <= :montantMax) " +
            "AND (:dateDebutCreation IS NULL OR tc.dateCreation >= :dateDebutCreation) " +
            "AND (:dateFinCreation IS NULL OR tc.dateCreation <= :dateFinCreation) " +
            "ORDER BY tc.dateModification DESC")
    List<TransactionChargeback> findByMultiplesCriteres(@Param("litigeActif") Boolean litigeActif,
                                                        @Param("nombreMin") Integer nombreMin,
                                                        @Param("nombreMax") Integer nombreMax,
                                                        @Param("montantMin") BigDecimal montantMin,
                                                        @Param("montantMax") BigDecimal montantMax,
                                                        @Param("dateDebutCreation") LocalDateTime dateDebutCreation,
                                                        @Param("dateFinCreation") LocalDateTime dateFinCreation);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE (tc.nombreChargebacks > :seuilChargebacks) " +
            "OR (tc.montantTotalConteste > :seuilMontant) " +
            "OR (tc.aLitigeActif = true AND tc.dateModification < :dateInactivite) " +
            "ORDER BY tc.nombreChargebacks DESC, tc.montantTotalConteste DESC")
    List<TransactionChargeback> findTransactionsProblematiques(@Param("seuilChargebacks") Integer seuilChargebacks,
                                                               @Param("seuilMontant") BigDecimal seuilMontant,
                                                               @Param("dateInactivite") LocalDateTime dateInactivite);

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE tc.nombreChargebacks > 0 " +
            "AND tc.dateModification >= :dateRecente " +
            "ORDER BY tc.nombreChargebacks DESC, tc.dateModification DESC")
    List<TransactionChargeback> findTransactionsHauteActivite(@Param("dateRecente") LocalDateTime dateRecente);

    // ====================================================================
    // 📊 STATISTIQUES ET DASHBOARD
    // ====================================================================

    @Query("SELECT " +
            "COUNT(tc) as totalTransactions, " +
            "SUM(CASE WHEN tc.aLitigeActif = true THEN 1 ELSE 0 END) as avecLitigeActif, " +
            "SUM(CASE WHEN tc.nombreChargebacks > 0 THEN 1 ELSE 0 END) as avecChargebacks, " +
            "SUM(tc.nombreChargebacks) as totalChargebacks, " +
            "SUM(tc.montantTotalConteste) as montantTotalConteste, " +
            "AVG(tc.nombreChargebacks) as moyenneChargebacks, " +
            "MAX(tc.nombreChargebacks) as maxChargebacks " +
            "FROM TransactionChargeback tc")
    Object[] getStatistiquesDashboard();

    @Query("SELECT " +
            "YEAR(tc.dateCreation), " +
            "MONTH(tc.dateCreation), " +
            "COUNT(tc) as nombreTransactions, " +
            "SUM(tc.nombreChargebacks) as totalChargebacks, " +
            "SUM(tc.montantTotalConteste) as montantTotal " +
            "FROM TransactionChargeback tc " +
            "WHERE tc.dateCreation BETWEEN :dateDebut AND :dateFin " +
            "GROUP BY YEAR(tc.dateCreation), MONTH(tc.dateCreation) " +
            "ORDER BY YEAR(tc.dateCreation), MONTH(tc.dateCreation)")
    List<Object[]> getKpiMensuels(@Param("dateDebut") LocalDateTime dateDebut,
                                  @Param("dateFin") LocalDateTime dateFin);

    @Query("SELECT " +
            "DATE(tc.dateCreation) as jour, " +
            "COUNT(tc) as nombreTransactions, " +
            "SUM(CASE WHEN tc.aLitigeActif = true THEN 1 ELSE 0 END) as litigesActifs, " +
            "SUM(tc.nombreChargebacks) as totalChargebacks " +
            "FROM TransactionChargeback tc " +
            "WHERE tc.dateCreation >= :dateLimit " +
            "GROUP BY DATE(tc.dateCreation) " +
            "ORDER BY DATE(tc.dateCreation)")
    List<Object[]> getTendancesQuotidiennes(@Param("dateLimit") LocalDateTime dateLimit);

    // ====================================================================
    // 🔧 MÉTHODES UTILITAIRES
    // ====================================================================

    @Query("SELECT COUNT(tc) > 0 FROM TransactionChargeback tc " +
            "WHERE tc.transactionId = :transactionId " +
            "AND tc.aLitigeActif = false")
    boolean canAddChargeback(@Param("transactionId") Long transactionId);

    @Query("SELECT tc.id FROM TransactionChargeback tc ORDER BY tc.id ASC")
    List<Long> findAllIds();

    @Query("SELECT tc.transactionId FROM TransactionChargeback tc ORDER BY tc.transactionId ASC")
    List<Long> findAllTransactionIds();

    @Query("SELECT tc FROM TransactionChargeback tc " +
            "WHERE NOT EXISTS (SELECT 1 FROM Transaction t WHERE t.id = tc.transactionId)")
    List<TransactionChargeback> findOrphanedTransactionChargebacks();
}