package com.example.dms_backend.repository;

import com.example.dms_backend.model.Arbitrage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des arbitrages
 * Compatible avec l'entité Arbitrage existante
 *
 * @author Système DMS Bancaire Maroc
 * @version 1.0 Production
 */
@Repository
public interface ArbitrageRepository extends JpaRepository<Arbitrage, Long> {

    // ===========================================
    // RECHERCHES PAR STATUT
    // ===========================================

    /**
     * Trouve tous les arbitrages par statut
     */
    List<Arbitrage> findByStatut(Arbitrage.StatutArbitrage statut);

    /**
     * Compte les arbitrages par statut
     */
    long countByStatut(Arbitrage.StatutArbitrage statut);

    /**
     * Trouve les arbitrages en attente (statut DEMANDE)
     */
    @Query("SELECT a FROM Arbitrage a WHERE a.statut = :statut ORDER BY a.dateDemande ASC")
    List<Arbitrage> findArbitragesEnAttente(@Param("statut") Arbitrage.StatutArbitrage statut);

    /**
     * Trouve les arbitrages en cours (statut EN_COURS)
     */
    @Query("SELECT a FROM Arbitrage a WHERE a.statut = :statut ORDER BY a.dateDemande ASC")
    List<Arbitrage> findArbitragesEnCours(@Param("statut") Arbitrage.StatutArbitrage statut);

    /**
     * Trouve les arbitrages décidés (statut DECIDE)
     */
    @Query("SELECT a FROM Arbitrage a WHERE a.statut = :statut ORDER BY a.dateDecision DESC")
    List<Arbitrage> findArbitragesDecides(@Param("statut") Arbitrage.StatutArbitrage statut);

    // ===========================================
    // RECHERCHES PAR LITIGE
    // ===========================================

    /**
     * Trouve tous les arbitrages d'un litige
     */
    List<Arbitrage> findByLitigeId(Long litigeId);

    /**
     * Trouve le dernier arbitrage d'un litige
     */
    @Query("SELECT a FROM Arbitrage a WHERE a.litigeId = :litigeId ORDER BY a.dateDemande DESC")
    List<Arbitrage> findByLitigeIdOrderByDateDemandeDesc(@Param("litigeId") Long litigeId);

    /**
     * Trouve l'arbitrage actif d'un litige (non décidé)
     */
    @Query("SELECT a FROM Arbitrage a WHERE a.litigeId = :litigeId AND (a.statut = :demande OR a.statut = :enCours)")
    Optional<Arbitrage> findArbitrageActifByLitige(@Param("litigeId") Long litigeId,
                                                   @Param("demande") Arbitrage.StatutArbitrage demande,
                                                   @Param("enCours") Arbitrage.StatutArbitrage enCours);

    /**
     * Vérifie si un litige a un arbitrage en cours
     */
    @Query("SELECT COUNT(a) > 0 FROM Arbitrage a WHERE a.litigeId = :litigeId AND (a.statut = :demande OR a.statut = :enCours)")
    boolean existsArbitrageActifByLitige(@Param("litigeId") Long litigeId,
                                         @Param("demande") Arbitrage.StatutArbitrage demande,
                                         @Param("enCours") Arbitrage.StatutArbitrage enCours);

    // ===========================================
    // RECHERCHES PAR INSTITUTION
    // ===========================================

    /**
     * Trouve les arbitrages demandés par une institution
     */
    List<Arbitrage> findByDemandeParInstitutionId(Long institutionId);

    /**
     * Trouve les arbitrages demandés par une institution avec statut
     */
    List<Arbitrage> findByDemandeParInstitutionIdAndStatut(Long institutionId, Arbitrage.StatutArbitrage statut);

    /**
     * Compte les arbitrages demandés par une institution
     */
    long countByDemandeParInstitutionId(Long institutionId);
    // ===========================================
    // RECHERCHES PAR ARBITRE
    // ===========================================

    /**
     * Trouve les arbitrages assignés à un arbitre
     */
    List<Arbitrage> findByArbitreUtilisateurId(Long arbitreId);

    /**
     * Trouve les arbitrages en cours pour un arbitre
     */
    @Query("SELECT a FROM Arbitrage a WHERE a.arbitreUtilisateurId = :arbitreId AND a.statut = :statut")
    List<Arbitrage> findArbitragesEnCoursByArbitre(@Param("arbitreId") Long arbitreId,
                                                   @Param("statut") Arbitrage.StatutArbitrage statut);

    /**
     * Compte les arbitrages en cours pour un arbitre
     */
    @Query("SELECT COUNT(a) FROM Arbitrage a WHERE a.arbitreUtilisateurId = :arbitreId AND a.statut = :statut")
    long countArbitragesEnCoursByArbitre(@Param("arbitreId") Long arbitreId,
                                         @Param("statut") Arbitrage.StatutArbitrage statut);

    // ===========================================
    // RECHERCHES PAR DÉCISION
    // ===========================================

    /**
     * Trouve les arbitrages avec une décision spécifique
     */
    List<Arbitrage> findByDecision(Arbitrage.Decision decision);

    /**
     * Compte les arbitrages favorables à l'émetteur
     */
    @Query("SELECT COUNT(a) FROM Arbitrage a WHERE a.decision = :decision")
    long countDecisionsFavorablesEmetteur(@Param("decision") Arbitrage.Decision decision);

    /**
     * Compte les arbitrages favorables à l'acquéreur
     */
    @Query("SELECT COUNT(a) FROM Arbitrage a WHERE a.decision = :decision")
    long countDecisionsFavorablesAcquereur(@Param("decision") Arbitrage.Decision decision);

    // ===========================================
    // RECHERCHES PAR PÉRIODE
    // ===========================================

    /**
     * Trouve les arbitrages dans une période
     */
    @Query("SELECT a FROM Arbitrage a WHERE a.dateDemande BETWEEN :dateDebut AND :dateFin ORDER BY a.dateDemande DESC")
    List<Arbitrage> findByPeriodeDemande(@Param("dateDebut") LocalDateTime dateDebut,
                                         @Param("dateFin") LocalDateTime dateFin);

    /**
     * Trouve les arbitrages décidés dans une période
     */
    @Query("SELECT a FROM Arbitrage a WHERE a.dateDecision BETWEEN :dateDebut AND :dateFin ORDER BY a.dateDecision DESC")
    List<Arbitrage> findByPeriodeDecision(@Param("dateDebut") LocalDateTime dateDebut,
                                          @Param("dateFin") LocalDateTime dateFin);

    /**
     * Trouve les arbitrages demandés après une date
     */
    List<Arbitrage> findByDateDemandeAfter(LocalDateTime date);

    /**
     * Trouve les arbitrages demandés avant une date
     */
    List<Arbitrage> findByDateDemandeBefore(LocalDateTime date);

    // ===========================================
    // RECHERCHES PAR MONTANT
    // ===========================================

    /**
     * Trouve les arbitrages avec coût supérieur à un montant
     */
    @Query("SELECT a FROM Arbitrage a WHERE a.coutArbitrage > :montant ORDER BY a.coutArbitrage DESC")
    List<Arbitrage> findByCoutArbitrageGreaterThan(@Param("montant") BigDecimal montant);

    /**
     * Trouve les arbitrages avec coût dans une fourchette
     */
    @Query("SELECT a FROM Arbitrage a WHERE a.coutArbitrage BETWEEN :montantMin AND :montantMax")
    List<Arbitrage> findByCoutArbitrageBetween(@Param("montantMin") BigDecimal montantMin,
                                               @Param("montantMax") BigDecimal montantMax);

    // ===========================================
    // STATISTIQUES ET RAPPORTS
    // ===========================================

    /**
     * Calcule le délai moyen de décision en jours
     */
    @Query("SELECT AVG(CAST(a.dateDecision AS double) - CAST(a.dateDemande AS double)) FROM Arbitrage a WHERE a.dateDecision IS NOT NULL")
    Double calculateDelaiMoyenDecisionJours();

    /**
     * Calcule le coût total des arbitrages
     */
    @Query("SELECT SUM(a.coutArbitrage) FROM Arbitrage a WHERE a.coutArbitrage IS NOT NULL")
    BigDecimal calculateCoutTotalArbitrages();
    /**
     * Trouve les arbitrages en retard (plus de X jours)
     */
    @Query("SELECT a FROM Arbitrage a WHERE (a.statut = :demande OR a.statut = :enCours) AND a.dateDemande < :dateLimit")
    List<Arbitrage> findArbitragesEnRetard(@Param("dateLimit") LocalDateTime dateLimit,
                                           @Param("demande") Arbitrage.StatutArbitrage demande,
                                           @Param("enCours") Arbitrage.StatutArbitrage enCours);

    /**
     * Statistiques par mois
     */
    @Query("SELECT YEAR(a.dateDemande), MONTH(a.dateDemande), COUNT(a) " +
            "FROM Arbitrage a " +
            "WHERE a.dateDemande BETWEEN :dateDebut AND :dateFin " +
            "GROUP BY YEAR(a.dateDemande), MONTH(a.dateDemande) " +
            "ORDER BY YEAR(a.dateDemande), MONTH(a.dateDemande)")
    List<Object[]> getStatistiquesParMois(@Param("dateDebut") LocalDateTime dateDebut,
                                          @Param("dateFin") LocalDateTime dateFin);

    // ===========================================
    // RECHERCHES COMPLEXES
    // ===========================================

    /**
     * Trouve les arbitrages urgents (critères multiples)
     */
    @Query("SELECT a FROM Arbitrage a WHERE " +
            "(a.statut = :demande OR a.statut = :enCours) AND " +
            "(a.dateDemande < :dateUrgence OR a.coutArbitrage > :montantUrgent)")
    List<Arbitrage> findArbitragesUrgents(@Param("dateUrgence") LocalDateTime dateUrgence,
                                          @Param("montantUrgent") BigDecimal montantUrgent,
                                          @Param("demande") Arbitrage.StatutArbitrage demande,
                                          @Param("enCours") Arbitrage.StatutArbitrage enCours);

    /**
     * Dashboard admin - arbitrages nécessitant attention
     */
    @Query("SELECT a FROM Arbitrage a WHERE " +
            "a.statut = :statut AND " +
            "(a.dateDemande < :dateUrgence OR " +
            " a.coutArbitrage > :montantEleve) " +
            "ORDER BY a.dateDemande ASC")
    List<Arbitrage> findArbitragesPourDashboardAdmin(@Param("dateUrgence") LocalDateTime dateUrgence,
                                                     @Param("montantEleve") BigDecimal montantEleve,
                                                     @Param("statut") Arbitrage.StatutArbitrage statut);

    /**
     * Recherche multicritères avec pagination
     */
    @Query("SELECT a FROM Arbitrage a WHERE " +
            "(:statut IS NULL OR a.statut = :statut) AND " +
            "(:institutionId IS NULL OR a.demandeParInstitutionId = :institutionId) AND " +
            "(:arbitreId IS NULL OR a.arbitreUtilisateurId = :arbitreId) AND " +
            "(:dateDebutDemande IS NULL OR a.dateDemande >= :dateDebutDemande) AND " +
            "(:dateFinDemande IS NULL OR a.dateDemande <= :dateFinDemande) " +
            "ORDER BY a.dateDemande DESC")
    List<Arbitrage> findByMultiplesCriteres(@Param("statut") Arbitrage.StatutArbitrage statut,
                                            @Param("institutionId") Long institutionId,
                                            @Param("arbitreId") Long arbitreId,
                                            @Param("dateDebutDemande") LocalDateTime dateDebutDemande,
                                            @Param("dateFinDemande") LocalDateTime dateFinDemande);

    // ===========================================
    // VÉRIFICATIONS MÉTIER
    // ===========================================

    /**
     * Vérifie si un arbitrage peut être supprimé
     */
    @Query("SELECT COUNT(a) = 0 FROM Arbitrage a WHERE a.id = :arbitrageId AND a.statut = :statut")
    boolean canBeDeleted(@Param("arbitrageId") Long arbitrageId,
                         @Param("statut") Arbitrage.StatutArbitrage statut);

    /**
     * Trouve les arbitrages d'une institution dans une période
     */
    @Query("SELECT a FROM Arbitrage a WHERE " +
            "a.demandeParInstitutionId = :institutionId AND " +
            "a.dateDemande BETWEEN :dateDebut AND :dateFin " +
            "ORDER BY a.dateDemande DESC")
    List<Arbitrage> findByInstitutionEtPeriode(@Param("institutionId") Long institutionId,
                                               @Param("dateDebut") LocalDateTime dateDebut,
                                               @Param("dateFin") LocalDateTime dateFin);

    /**
     * Méthodes simplifiées pour éviter les erreurs d'enum
     */
    /**
     * Méthodes simplifiées pour éviter les erreurs d'enum
     */
    @Query("SELECT a FROM Arbitrage a WHERE a.statut = :statut ORDER BY a.dateDemande ASC")
    List<Arbitrage> findArbitragesEnAttenteSimple(@Param("statut") Arbitrage.StatutArbitrage statut);

    @Query("SELECT a FROM Arbitrage a WHERE a.statut = :statut ORDER BY a.dateDemande ASC")
    List<Arbitrage> findArbitragesEnCoursSimple(@Param("statut") Arbitrage.StatutArbitrage statut);

    @Query("SELECT a FROM Arbitrage a WHERE a.statut = :statut ORDER BY a.dateDecision DESC")
    List<Arbitrage> findArbitragesDecidesSimple(@Param("statut") Arbitrage.StatutArbitrage statut);
}