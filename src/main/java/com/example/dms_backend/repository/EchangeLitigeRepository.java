package com.example.dms_backend.repository;

import com.example.dms_backend.model.EchangeLitige;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des échanges et communications de litige
 * Gestion des messages, actions, escalades et décisions
 *
 * @author Système DMS Bancaire Maroc
 * @version 1.0 Production
 */
@Repository
public interface EchangeLitigeRepository extends JpaRepository<EchangeLitige, Long> {

    // ====================================================================
    // 🔍 RECHERCHES PAR LITIGE
    // ====================================================================

    /**
     * ✅ MÉTHODE PRINCIPALE : Tous les échanges d'un litige
     */
    List<EchangeLitige> findByLitigeId(Long litigeId);

    /**
     * ✅ CHRONOLOGIQUE : Échanges d'un litige par ordre chronologique
     */
    List<EchangeLitige> findByLitigeIdOrderByDateEchange(Long litigeId);

    /**
     * ✅ CHRONOLOGIQUE DESC : Échanges d'un litige (plus récents en premier)
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.litigeId = :litigeId " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findByLitigeIdOrderByDateEchangeDesc(@Param("litigeId") Long litigeId);

    /**
     * ✅ COMPTAGE : Nombre d'échanges par litige
     */
    long countByLitigeId(Long litigeId);

    /**
     * ✅ VÉRIFICATION : Vérifie si un litige a des échanges
     */
    boolean existsByLitigeId(Long litigeId);

    /**
     * 🔍 VISIBLES SEULEMENT : Échanges visibles d'un litige
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.litigeId = :litigeId " +
            "AND e.visible = true " +
            "ORDER BY e.dateEchange ASC")
    List<EchangeLitige> findVisibleByLitigeId(@Param("litigeId") Long litigeId);

    // ====================================================================
    // 🔄 RECHERCHES PAR TYPE D'ÉCHANGE
    // ====================================================================

    /**
     * ✅ TYPE SIMPLE : Échanges par type
     */
    List<EchangeLitige> findByTypeEchange(EchangeLitige.TypeEchange type);

    /**
     * 💬 MESSAGES : Tous les messages seulement
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.typeEchange = 'MESSAGE' " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findMessages();

    /**
     * ⚡ ACTIONS : Toutes les actions seulement
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.typeEchange = 'ACTION' " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findActions();

    /**
     * 🚨 ESCALADES : Toutes les escalades seulement
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.typeEchange = 'ESCALADE' " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findEscalades();

    /**
     * ⚖️ DÉCISIONS : Toutes les décisions seulement
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.typeEchange = 'DECISION' " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findDecisions();

    /**
     * 🔍 LITIGE + TYPE : Échanges d'un litige par type
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.litigeId = :litigeId " +
            "AND e.typeEchange = :type " +
            "ORDER BY e.dateEchange ASC")
    List<EchangeLitige> findByLitigeIdAndTypeEchange(@Param("litigeId") Long litigeId,
                                                     @Param("type") EchangeLitige.TypeEchange type);

    /**
     * 📊 STATISTIQUES : Répartition par type d'échange
     */
    @Query("SELECT e.typeEchange, COUNT(e) " +
            "FROM EchangeLitige e " +
            "GROUP BY e.typeEchange " +
            "ORDER BY COUNT(e) DESC")
    List<Object[]> getStatistiquesParType();

    // ====================================================================
    // 🔄 RECHERCHES PAR PHASE LITIGE
    // ====================================================================

    /**
     * ✅ PHASE SIMPLE : Échanges par phase
     */
    List<EchangeLitige> findByPhaseLitige(String phase);

    /**
     * 🔍 LITIGE + PHASE : Échanges d'un litige par phase
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.litigeId = :litigeId " +
            "AND e.phaseLitige = :phase " +
            "ORDER BY e.dateEchange ASC")
    List<EchangeLitige> findByLitigeIdAndPhaseLitige(@Param("litigeId") Long litigeId,
                                                     @Param("phase") String phase);

    /**
     * 📊 PHASE STATS : Nombre d'échanges par phase
     */
    @Query("SELECT e.phaseLitige, COUNT(e) " +
            "FROM EchangeLitige e " +
            "WHERE e.phaseLitige IS NOT NULL " +
            "GROUP BY e.phaseLitige " +
            "ORDER BY COUNT(e) DESC")
    List<Object[]> getStatistiquesParPhase();

    /**
     * 🔥 INITIATION : Échanges de la phase d'initiation
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.phaseLitige = 'CHARGEBACK_INITIAL' " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findEchangesInitiation();

    /**
     * 🔥 REPRÉSENTATION : Échanges de la phase de représentation
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.phaseLitige = 'REPRESENTATION' " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findEchangesRepresentation();

    /**
     * 🔥 PRÉ-ARBITRAGE : Échanges de la phase de pré-arbitrage
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.phaseLitige = 'PRE_ARBITRAGE' " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findEchangesPreArbitrage();

    /**
     * ⚖️ ARBITRAGE : Échanges de la phase d'arbitrage
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.phaseLitige = 'ARBITRAGE' " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findEchangesArbitrage();

    // ====================================================================
    // 👤 RECHERCHES PAR UTILISATEUR
    // ====================================================================

    /**
     * ✅ UTILISATEUR : Échanges créés par un utilisateur
     */
    List<EchangeLitige> findByAuteurUtilisateurId(Long auteurId);

    /**
     * ✅ UTILISATEUR TRIÉS : Échanges d'un utilisateur par date
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.auteurUtilisateurId = :auteurId " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findByAuteurUtilisateurIdOrderByDateEchange(@Param("auteurId") Long auteurId);

    /**
     * 📊 UTILISATEUR STATS : Nombre d'échanges par utilisateur
     */
    long countByAuteurUtilisateurId(Long auteurId);

    /**
     * 🔍 UTILISATEUR + LITIGE : Échanges d'un utilisateur pour un litige spécifique
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.litigeId = :litigeId " +
            "AND e.auteurUtilisateurId = :auteurId " +
            "ORDER BY e.dateEchange ASC")
    List<EchangeLitige> findByLitigeIdAndAuteurUtilisateurId(@Param("litigeId") Long litigeId,
                                                             @Param("auteurId") Long auteurId);

    /**
     * 👥 ACTIVITÉ UTILISATEUR : Échanges récents d'un utilisateur
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.auteurUtilisateurId = :auteurId " +
            "AND e.dateEchange >= :dateLimit " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findRecentsByAuteurUtilisateur(@Param("auteurId") Long auteurId,
                                                       @Param("dateLimit") LocalDateTime dateLimit);

    // ====================================================================
    // 🏦 RECHERCHES PAR INSTITUTION
    // ====================================================================

    /**
     * ✅ INSTITUTION : Échanges d'une institution
     */
    List<EchangeLitige> findByInstitutionId(Long institutionId);

    /**
     * ✅ INSTITUTION TRIÉS : Échanges d'une institution par date
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.institutionId = :institutionId " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findByInstitutionIdOrderByDateEchange(@Param("institutionId") Long institutionId);

    /**
     * 📊 INSTITUTION STATS : Nombre d'échanges par institution
     */
    long countByInstitutionId(Long institutionId);

    /**
     * 🔍 INSTITUTION + LITIGE : Échanges d'une institution pour un litige
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.litigeId = :litigeId " +
            "AND e.institutionId = :institutionId " +
            "ORDER BY e.dateEchange ASC")
    List<EchangeLitige> findByLitigeIdAndInstitutionId(@Param("litigeId") Long litigeId,
                                                       @Param("institutionId") Long institutionId);

    /**
     * 👥 ACTIVITÉ INSTITUTION : Échanges récents d'une institution
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.institutionId = :institutionId " +
            "AND e.dateEchange >= :dateLimit " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findRecentsByInstitution(@Param("institutionId") Long institutionId,
                                                 @Param("dateLimit") LocalDateTime dateLimit);

    // ====================================================================
    // 👁️ RECHERCHES PAR VISIBILITÉ ET LECTURE
    // ====================================================================

    /**
     * 👁️ VISIBLES : Échanges visibles seulement
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.visible = true " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findEchangesVisibles();

    /**
     * 🔒 PRIVÉS : Échanges privés (non visibles)
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.visible = false " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findEchangesPrives();

    /**
     * 📖 NON LUS : Échanges non lus par l'autre partie
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.luParAutrePartie = false " +
            "AND e.visible = true " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findEchangesNonLus();

    /**
     * ✅ LUS : Échanges lus par l'autre partie
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.luParAutrePartie = true " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findEchangesLus();

    /**
     * 🔍 LITIGE + VISIBILITÉ : Échanges d'un litige par visibilité
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.litigeId = :litigeId " +
            "AND e.visible = :visible " +
            "ORDER BY e.dateEchange ASC")
    List<EchangeLitige> findByLitigeIdAndVisible(@Param("litigeId") Long litigeId,
                                                 @Param("visible") Boolean visible);

    /**
     * 🔍 LITIGE + LECTURE : Échanges d'un litige par statut de lecture
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.litigeId = :litigeId " +
            "AND e.luParAutrePartie = :lu " +
            "AND e.visible = true " +
            "ORDER BY e.dateEchange ASC")
    List<EchangeLitige> findByLitigeIdAndLuParAutrePartie(@Param("litigeId") Long litigeId,
                                                          @Param("lu") Boolean lu);

    /**
     * 📊 VISIBILITÉ STATS : Compteurs de visibilité
     */
    @Query("SELECT e.visible, COUNT(e) " +
            "FROM EchangeLitige e " +
            "GROUP BY e.visible")
    List<Object[]> getStatistiquesVisibilite();

    /**
     * 📊 LECTURE STATS : Compteurs de lecture
     */
    @Query("SELECT e.luParAutrePartie, COUNT(e) " +
            "FROM EchangeLitige e " +
            "WHERE e.visible = true " +
            "GROUP BY e.luParAutrePartie")
    List<Object[]> getStatistiquesLecture();

    // ====================================================================
    // 📎 RECHERCHES PAR PIÈCES JOINTES
    // ====================================================================

    /**
     * 📎 AVEC PIÈCES JOINTES : Échanges avec pièces jointes
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.pieceJointeJustificatifId IS NOT NULL " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findEchangesAvecPiecesJointes();

    /**
     * 📝 SANS PIÈCES JOINTES : Échanges sans pièces jointes
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.pieceJointeJustificatifId IS NULL " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findEchangesSansPiecesJointes();

    /**
     * 🔍 PIÈCE JOINTE SPÉCIFIQUE : Échanges liés à un justificatif
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.pieceJointeJustificatifId = :justificatifId " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findByPieceJointeJustificatifId(@Param("justificatifId") Long justificatifId);

    /**
     * 📊 PIÈCES JOINTES STATS : Comptage avec/sans pièces jointes
     */
    @Query("SELECT " +
            "SUM(CASE WHEN e.pieceJointeJustificatifId IS NOT NULL THEN 1 ELSE 0 END) as avecPiecesJointes, " +
            "SUM(CASE WHEN e.pieceJointeJustificatifId IS NULL THEN 1 ELSE 0 END) as sansPiecesJointes " +
            "FROM EchangeLitige e")
    Object[] getStatistiquesPiecesJointes();

    // ====================================================================
    // 📅 RECHERCHES PAR PÉRIODE
    // ====================================================================

    /**
     * ✅ PÉRIODE : Échanges dans une période
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.dateEchange BETWEEN :dateDebut AND :dateFin " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findByPeriodeEchange(@Param("dateDebut") LocalDateTime dateDebut,
                                             @Param("dateFin") LocalDateTime dateFin);

    /**
     * 🆕 RÉCENTS : Échanges récents (X derniers jours)
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.dateEchange >= :dateLimit " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findEchangesRecents(@Param("dateLimit") LocalDateTime dateLimit);

    /**
     * 📅 AUJOURD'HUI : Échanges d'aujourd'hui - VERSION CORRIGÉE
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.dateEchange >= CURRENT_TIMESTAMP " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findEchangesAujourdHui();

    /**
     * 📊 ÉVOLUTION : Nombre d'échanges par mois
     */
    @Query("SELECT YEAR(e.dateEchange), MONTH(e.dateEchange), COUNT(e) " +
            "FROM EchangeLitige e " +
            "WHERE e.dateEchange BETWEEN :dateDebut AND :dateFin " +
            "GROUP BY YEAR(e.dateEchange), MONTH(e.dateEchange) " +
            "ORDER BY YEAR(e.dateEchange), MONTH(e.dateEchange)")
    List<Object[]> getEvolutionMensuelle(@Param("dateDebut") LocalDateTime dateDebut,
                                         @Param("dateFin") LocalDateTime dateFin);

    // ====================================================================
    // 🔍 RECHERCHES PAR CONTENU - VERSION CORRIGÉE
    // ====================================================================

    /**
     * 🔍 RECHERCHE CONTENU : Recherche dans le contenu des échanges - VERSION CORRIGÉE
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.contenu LIKE CONCAT('%', :motCle, '%') " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findByContenuContaining(@Param("motCle") String motCle);

    /**
     * 🔍 LITIGE + CONTENU : Recherche dans le contenu pour un litige - VERSION CORRIGÉE
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.litigeId = :litigeId " +
            "AND e.contenu LIKE CONCAT('%', :motCle, '%') " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findByLitigeIdAndContenuContaining(@Param("litigeId") Long litigeId,
                                                           @Param("motCle") String motCle);

    // ====================================================================
    // 🔍 RECHERCHES COMPLEXES MULTICRITÈRES
    // ====================================================================

    /**
     * 🔍 RECHERCHE AVANCÉE : Multicritères avec pagination
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE (:litigeId IS NULL OR e.litigeId = :litigeId) " +
            "AND (:type IS NULL OR e.typeEchange = :type) " +
            "AND (:phase IS NULL OR e.phaseLitige = :phase) " +
            "AND (:auteurId IS NULL OR e.auteurUtilisateurId = :auteurId) " +
            "AND (:institutionId IS NULL OR e.institutionId = :institutionId) " +
            "AND (:visible IS NULL OR e.visible = :visible) " +
            "AND (:lu IS NULL OR e.luParAutrePartie = :lu) " +
            "AND (:dateDebutEchange IS NULL OR e.dateEchange >= :dateDebutEchange) " +
            "AND (:dateFinEchange IS NULL OR e.dateEchange <= :dateFinEchange) " +
            "ORDER BY e.dateEchange DESC")
    List<EchangeLitige> findByMultiplesCriteres(@Param("litigeId") Long litigeId,
                                                @Param("type") EchangeLitige.TypeEchange type,
                                                @Param("phase") String phase,
                                                @Param("auteurId") Long auteurId,
                                                @Param("institutionId") Long institutionId,
                                                @Param("visible") Boolean visible,
                                                @Param("lu") Boolean lu,
                                                @Param("dateDebutEchange") LocalDateTime dateDebutEchange,
                                                @Param("dateFinEchange") LocalDateTime dateFinEchange);

    // ====================================================================
    // 📊 DASHBOARD ET STATISTIQUES
    // ====================================================================

    /**
     * 📊 ACTIVITÉ : Échanges par jour (derniers X jours)
     */
    @Query("SELECT DATE(e.dateEchange), COUNT(e) " +
            "FROM EchangeLitige e " +
            "WHERE e.dateEchange >= :dateLimit " +
            "GROUP BY DATE(e.dateEchange) " +
            "ORDER BY DATE(e.dateEchange)")
    List<Object[]> getActiviteParJour(@Param("dateLimit") LocalDateTime dateLimit);

    /**
     * 📊 TOP UTILISATEURS : Utilisateurs les plus actifs
     */
    @Query("SELECT e.auteurUtilisateurId, COUNT(e) " +
            "FROM EchangeLitige e " +
            "WHERE e.auteurUtilisateurId IS NOT NULL " +
            "GROUP BY e.auteurUtilisateurId " +
            "ORDER BY COUNT(e) DESC")
    List<Object[]> getTopUtilisateursActifs();

    /**
     * 📊 TOP INSTITUTIONS : Institutions les plus actives
     */
    @Query("SELECT e.institutionId, COUNT(e) " +
            "FROM EchangeLitige e " +
            "WHERE e.institutionId IS NOT NULL " +
            "GROUP BY e.institutionId " +
            "ORDER BY COUNT(e) DESC")
    List<Object[]> getTopInstitutionsActives();

    // ====================================================================
    // 🔧 MÉTHODES UTILITAIRES
    // ====================================================================

    /**
     * 🔍 DERNIER ÉCHANGE : Dernier échange d'un litige
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.litigeId = :litigeId " +
            "ORDER BY e.dateEchange DESC " +
            "LIMIT 1")
    Optional<EchangeLitige> findLastByLitigeId(@Param("litigeId") Long litigeId);

    /**
     * 🔍 PREMIER ÉCHANGE : Premier échange d'un litige
     */
    @Query("SELECT e FROM EchangeLitige e " +
            "WHERE e.litigeId = :litigeId " +
            "ORDER BY e.dateEchange ASC " +
            "LIMIT 1")
    Optional<EchangeLitige> findFirstByLitigeId(@Param("litigeId") Long litigeId);

    /**
     * 🆔 DEBUG : Tous les IDs pour diagnostic
     */
    @Query("SELECT e.id FROM EchangeLitige e ORDER BY e.id ASC")
    List<Long> findAllIds();

    /**
     * 📊 DASHBOARD : Statistiques générales pour dashboard
     */
    @Query("SELECT " +
            "COUNT(e) as total, " +
            "SUM(CASE WHEN e.visible = true THEN 1 ELSE 0 END) as visibles, " +
            "SUM(CASE WHEN e.luParAutrePartie = false AND e.visible = true THEN 1 ELSE 0 END) as nonLus, " +
            "SUM(CASE WHEN e.pieceJointeJustificatifId IS NOT NULL THEN 1 ELSE 0 END) as avecPiecesJointes, " +
            "COUNT(DISTINCT e.litigeId) as litiges_avec_echanges " +
            "FROM EchangeLitige e")
    Object[] getStatistiquesDashboard();
}