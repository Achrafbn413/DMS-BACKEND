package com.example.dms_backend.repository;

import com.example.dms_backend.model.LitigeChargeback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des litiges chargeback
 * Compatible avec le workflow bancaire international
 *
 * @author Système DMS Bancaire Maroc
 * @version 1.0 Production
 */
@Repository
public interface LitigeChargebackRepository extends JpaRepository<LitigeChargeback, Long> {

    // ====================================================================
    // 🔍 RECHERCHES PAR LITIGE
    // ====================================================================

    /**
     * ✅ MÉTHODE PRINCIPALE : Trouve un litige chargeback par ID de litige
     */
    Optional<LitigeChargeback> findByLitigeId(Long litigeId);

    /**
     * ✅ CHARGEMENT OPTIMISÉ : Avec toutes les relations (évite N+1)
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "LEFT JOIN FETCH lc.justificatifs j " +
            "WHERE lc.litigeId = :litigeId " +
            "ORDER BY lc.dateCreation DESC")
    Optional<LitigeChargeback> findByLitigeIdWithDetails(@Param("litigeId") Long litigeId);

    /**
     * ✅ VÉRIFICATION : Vérifie si un litige a un chargeback
     */
    boolean existsByLitigeId(Long litigeId);

    /**
     * ✅ LISTE : Tous les litiges chargeback avec relations
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "ORDER BY lc.dateCreation DESC")
    List<LitigeChargeback> findAllWithDetails();

    // ====================================================================
    // 🔄 RECHERCHES PAR PHASE WORKFLOW
    // ====================================================================

    /**
     * ✅ PHASE SIMPLE : Trouve tous les litiges par phase
     */
    List<LitigeChargeback> findByPhaseActuelle(String phase);

    /**
     * ✅ COMPTAGE : Compte les litiges par phase
     */
    long countByPhaseActuelle(String phase);

    /**
     * 🔥 PHASE INITIALE : Litiges en phase de chargeback initial
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE lc.phaseActuelle = 'CHARGEBACK_INITIAL' " +
            "ORDER BY lc.dateCreation DESC")
    List<LitigeChargeback> findLitigesPhaseInitiale();

    /**
     * 🔥 REPRÉSENTATION : Litiges en phase de représentation
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE lc.phaseActuelle = 'REPRESENTATION' " +
            "ORDER BY lc.dateDerniereAction DESC")
    List<LitigeChargeback> findLitigesEnRepresentation();

    /**
     * 🔥 PRÉ-ARBITRAGE : Litiges en phase de pré-arbitrage (second presentment)
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE lc.phaseActuelle = 'PRE_ARBITRAGE' " +
            "ORDER BY lc.dateDerniereAction DESC")
    List<LitigeChargeback> findLitigesEnPreArbitrage();

    /**
     * 🔥 ARBITRAGE : Litiges en phase d'arbitrage
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE lc.phaseActuelle = 'ARBITRAGE' " +
            "ORDER BY lc.dateDerniereAction DESC")
    List<LitigeChargeback> findLitigesEnArbitrage();

    /**
     * ✅ FINALISÉS : Litiges finalisés
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE lc.phaseActuelle = 'FINALISE' " +
            "ORDER BY lc.dateDerniereAction DESC")
    List<LitigeChargeback> findLitigesFinalises();

    // ====================================================================
    // ⏰ RECHERCHES PAR DÉLAIS ET URGENCE
    // ====================================================================

    /**
     * 🚨 URGENTS : Litiges avec deadline dépassée
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE lc.deadlineActuelle IS NOT NULL " +
            "AND lc.deadlineActuelle < CURRENT_TIMESTAMP " +
            "AND lc.phaseActuelle NOT IN ('FINALISE', 'ARBITRAGE') " +
            "ORDER BY lc.deadlineActuelle ASC")
    List<LitigeChargeback> findLitigesEnRetard();

    /**
     * 🚨 DEADLINE PROCHE : Litiges avec deadline dans les X heures
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE lc.deadlineActuelle IS NOT NULL " +
            "AND lc.deadlineActuelle BETWEEN CURRENT_TIMESTAMP AND :deadlineLimit " +
            "AND lc.phaseActuelle NOT IN ('FINALISE', 'ARBITRAGE') " +
            "ORDER BY lc.deadlineActuelle ASC")
    List<LitigeChargeback> findLitigesAvecDeadlineProche(@Param("deadlineLimit") LocalDateTime deadlineLimit);

    /**
     * ✅ COMPTAGE : Nombre de litiges en retard
     */
    @Query("SELECT COUNT(lc) FROM LitigeChargeback lc " +
            "WHERE lc.deadlineActuelle IS NOT NULL " +
            "AND lc.deadlineActuelle < CURRENT_TIMESTAMP " +
            "AND lc.phaseActuelle NOT IN ('FINALISE', 'ARBITRAGE')")
    long countLitigesEnRetard();

    // ====================================================================
    // 💰 RECHERCHES PAR MONTANT
    // ====================================================================

    /**
     * 💰 MONTANT ÉLEVÉ : Litiges avec montant supérieur à X
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE lc.montantConteste > :montantSeuil " +
            "ORDER BY lc.montantConteste DESC")
    List<LitigeChargeback> findLitigesMontantEleve(@Param("montantSeuil") BigDecimal montantSeuil);

    /**
     * 💰 FOURCHETTE : Litiges dans une fourchette de montant
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE lc.montantConteste BETWEEN :montantMin AND :montantMax " +
            "ORDER BY lc.montantConteste DESC")
    List<LitigeChargeback> findByMontantContesteBetween(@Param("montantMin") BigDecimal montantMin,
                                                        @Param("montantMax") BigDecimal montantMax);

    /**
     * 💰 STATISTIQUES : Montant total contesté par phase
     */
    @Query("SELECT lc.phaseActuelle, SUM(lc.montantConteste) " +
            "FROM LitigeChargeback lc " +
            "WHERE lc.montantConteste IS NOT NULL " +
            "GROUP BY lc.phaseActuelle")
    List<Object[]> getMontantTotalParPhase();

    // ====================================================================
    // 🔍 RECHERCHES PAR MOTIF
    // ====================================================================

    /**
     * ✅ MOTIF SIMPLE : Litiges par motif de chargeback
     */
    List<LitigeChargeback> findByMotifChargeback(String motif);

    /**
     * 🔍 MOTIF RECHERCHE : Recherche partielle dans le motif
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE LOWER(lc.motifChargeback) LIKE LOWER(CONCAT('%', :motif, '%')) " +
            "ORDER BY lc.dateCreation DESC")
    List<LitigeChargeback> findByMotifChargebackContaining(@Param("motif") String motif);

    /**
     * 📊 STATISTIQUES : Répartition par motif
     */
    @Query("SELECT lc.motifChargeback, COUNT(lc) " +
            "FROM LitigeChargeback lc " +
            "WHERE lc.motifChargeback IS NOT NULL " +
            "GROUP BY lc.motifChargeback " +
            "ORDER BY COUNT(lc) DESC")
    List<Object[]> getStatistiquesParMotif();

    // ====================================================================
    // 📅 RECHERCHES PAR PÉRIODE
    // ====================================================================

    /**
     * ✅ PÉRIODE CRÉATION : Litiges créés dans une période
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE lc.dateCreation BETWEEN :dateDebut AND :dateFin " +
            "ORDER BY lc.dateCreation DESC")
    List<LitigeChargeback> findByPeriodeCreation(@Param("dateDebut") LocalDateTime dateDebut,
                                                 @Param("dateFin") LocalDateTime dateFin);

    /**
     * ✅ PÉRIODE ACTION : Litiges avec action dans une période
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE lc.dateDerniereAction BETWEEN :dateDebut AND :dateFin " +
            "ORDER BY lc.dateDerniereAction DESC")
    List<LitigeChargeback> findByPeriodeDerniereAction(@Param("dateDebut") LocalDateTime dateDebut,
                                                       @Param("dateFin") LocalDateTime dateFin);

    /**
     * 📊 ÉVOLUTION : Nombre de litiges créés par mois
     */
    @Query("SELECT YEAR(lc.dateCreation), MONTH(lc.dateCreation), COUNT(lc) " +
            "FROM LitigeChargeback lc " +
            "WHERE lc.dateCreation BETWEEN :dateDebut AND :dateFin " +
            "GROUP BY YEAR(lc.dateCreation), MONTH(lc.dateCreation) " +
            "ORDER BY YEAR(lc.dateCreation), MONTH(lc.dateCreation)")
    List<Object[]> getEvolutionMensuelle(@Param("dateDebut") LocalDateTime dateDebut,
                                         @Param("dateFin") LocalDateTime dateFin);

    // ====================================================================
    // 🏦 RECHERCHES PAR INSTITUTION (VIA LITIGE)
    // ====================================================================

    /**
     * 🏦 INSTITUTION : Litiges chargeback d'une institution
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "JOIN Litige l ON lc.litigeId = l.id " +
            "WHERE (l.transaction.banqueEmettrice.id = :institutionId OR " +
            "l.transaction.banqueAcquereuse.id = :institutionId) " +
            "ORDER BY lc.dateCreation DESC")
    List<LitigeChargeback> findByInstitutionId(@Param("institutionId") Long institutionId);

    /**
     * 🏦 INSTITUTION + PHASE : Litiges chargeback d'une institution par phase
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "JOIN Litige l ON lc.litigeId = l.id " +
            "WHERE (l.transaction.banqueEmettrice.id = :institutionId OR " +
            "l.transaction.banqueAcquereuse.id = :institutionId) " +
            "AND lc.phaseActuelle = :phase " +
            "ORDER BY lc.dateDerniereAction DESC")
    List<LitigeChargeback> findByInstitutionIdAndPhase(@Param("institutionId") Long institutionId,
                                                       @Param("phase") String phase);

    // ====================================================================
    // 🔧 MÉTHODES UTILITAIRES ET WORKFLOW
    // ====================================================================

    /**
     * ✅ ESCALADABLES : Litiges qui peuvent être escaladés
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE lc.peutEtreEscalade = true " +
            "AND lc.phaseActuelle NOT IN ('ARBITRAGE', 'FINALISE') " +
            "ORDER BY lc.dateDerniereAction DESC")
    List<LitigeChargeback> findLitigesEscaladables();

    /**
     * 🔄 WORKFLOW VERSION : Litiges par version de workflow
     */
    List<LitigeChargeback> findByVersionWorkflow(Integer version);

    /**
     * 🆕 RÉCENTS : Litiges créés dans les X derniers jours
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE lc.dateCreation >= :dateLimit " +
            "ORDER BY lc.dateCreation DESC")
    List<LitigeChargeback> findLitigesRecents(@Param("dateLimit") LocalDateTime dateLimit);

    // ====================================================================
    // 📊 DASHBOARD ET STATISTIQUES
    // ====================================================================

    /**
     * 📊 DASHBOARD : Compteurs par phase pour dashboard
     */
    @Query("SELECT lc.phaseActuelle, COUNT(lc) " +
            "FROM LitigeChargeback lc " +
            "GROUP BY lc.phaseActuelle")
    List<Object[]> getCompteursParPhase();

    /**
     * 📊 KPI : Temps moyen par phase (en jours) - VERSION CORRIGÉE
     */
    @Query("SELECT lc.phaseActuelle, COUNT(lc) " +
            "FROM LitigeChargeback lc " +
            "GROUP BY lc.phaseActuelle")
    List<Object[]> getTempsMoyenParPhase();

    /**
     * 🚨 ALERTES : Litiges nécessitant attention immédiate
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE (lc.deadlineActuelle IS NOT NULL AND lc.deadlineActuelle < CURRENT_TIMESTAMP) " +
            "OR (lc.montantConteste > :montantCritique) " +
            "OR (lc.dateDerniereAction < :dateInactivite) " +
            "AND lc.phaseActuelle NOT IN ('FINALISE', 'ARBITRAGE') " +
            "ORDER BY lc.deadlineActuelle ASC NULLS LAST")
    List<LitigeChargeback> findLitigesAlertes(@Param("montantCritique") BigDecimal montantCritique,
                                              @Param("dateInactivite") LocalDateTime dateInactivite);

    // ====================================================================
    // 🔍 RECHERCHES COMPLEXES MULTICRITÈRES
    // ====================================================================

    /**
     * 🔍 RECHERCHE AVANCÉE : Multicritères avec pagination
     */
    @Query("SELECT lc FROM LitigeChargeback lc " +
            "WHERE (:phase IS NULL OR lc.phaseActuelle = :phase) " +
            "AND (:motif IS NULL OR LOWER(lc.motifChargeback) LIKE LOWER(CONCAT('%', :motif, '%'))) " +
            "AND (:montantMin IS NULL OR lc.montantConteste >= :montantMin) " +
            "AND (:montantMax IS NULL OR lc.montantConteste <= :montantMax) " +
            "AND (:dateDebutCreation IS NULL OR lc.dateCreation >= :dateDebutCreation) " +
            "AND (:dateFinCreation IS NULL OR lc.dateCreation <= :dateFinCreation) " +
            "ORDER BY lc.dateCreation DESC")
    List<LitigeChargeback> findByMultiplesCriteres(@Param("phase") String phase,
                                                   @Param("motif") String motif,
                                                   @Param("montantMin") BigDecimal montantMin,
                                                   @Param("montantMax") BigDecimal montantMax,
                                                   @Param("dateDebutCreation") LocalDateTime dateDebutCreation,
                                                   @Param("dateFinCreation") LocalDateTime dateFinCreation);

    /**
     * ✅ VALIDATION : Vérifie si un litige peut changer de phase
     */
    @Query("SELECT COUNT(lc) > 0 FROM LitigeChargeback lc " +
            "WHERE lc.id = :id " +
            "AND lc.peutEtreEscalade = true " +
            "AND lc.phaseActuelle NOT IN ('ARBITRAGE', 'FINALISE')")
    boolean canChangePhase(@Param("id") Long id);

    /**
     * 🆔 DEBUG : Tous les IDs pour diagnostic
     */
    @Query("SELECT lc.id FROM LitigeChargeback lc ORDER BY lc.id ASC")
    List<Long> findAllIds();
}