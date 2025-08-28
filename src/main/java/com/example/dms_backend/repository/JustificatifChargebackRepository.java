package com.example.dms_backend.repository;

import com.example.dms_backend.model.JustificatifChargeback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des justificatifs chargeback
 * Gestion des documents, validations et métadonnées
 *
 * @author Système DMS Bancaire Maroc
 * @version 1.0 Production
 */
@Repository
public interface JustificatifChargebackRepository extends JpaRepository<JustificatifChargeback, Long> {

    // ====================================================================
    // 🔍 RECHERCHES PAR LITIGE
    // ====================================================================

    /**
     * ✅ MÉTHODE PRINCIPALE : Tous les justificatifs d'un litige
     */
    List<JustificatifChargeback> findByLitigeId(Long litigeId);

    /**
     * ✅ TRIÉS PAR DATE : Justificatifs d'un litige par date d'ajout
     */
    List<JustificatifChargeback> findByLitigeIdOrderByDateAjout(Long litigeId);

    /**
     * ✅ TRIÉS DESC : Justificatifs d'un litige (plus récents en premier)
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.litigeId = :litigeId " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findByLitigeIdOrderByDateAjoutDesc(@Param("litigeId") Long litigeId);

    /**
     * ✅ COMPTAGE : Nombre de justificatifs par litige
     */
    long countByLitigeId(Long litigeId);

    /**
     * ✅ VÉRIFICATION : Vérifie si un litige a des justificatifs
     */
    boolean existsByLitigeId(Long litigeId);

    // ====================================================================
    // 📁 RECHERCHES PAR TYPE DE JUSTIFICATIF
    // ====================================================================

    /**
     * ✅ TYPE SIMPLE : Justificatifs par type
     */
    List<JustificatifChargeback> findByTypeJustificatif(String type);

    /**
     * 🔍 LITIGE + TYPE : Justificatifs d'un litige par type
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.litigeId = :litigeId " +
            "AND j.typeJustificatif = :type " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findByLitigeIdAndType(@Param("litigeId") Long litigeId,
                                                       @Param("type") String type);

    /**
     * 📊 STATISTIQUES : Répartition par type de justificatif
     */
    @Query("SELECT j.typeJustificatif, COUNT(j) " +
            "FROM JustificatifChargeback j " +
            "GROUP BY j.typeJustificatif " +
            "ORDER BY COUNT(j) DESC")
    List<Object[]> getStatistiquesParType();

    /**
     * 🔍 RECHERCHE TYPE : Recherche partielle dans le type
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE LOWER(j.typeJustificatif) LIKE LOWER(CONCAT('%', :type, '%')) " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findByTypeJustificatifContaining(@Param("type") String type);

    // ====================================================================
    // 🔄 RECHERCHES PAR PHASE LITIGE
    // ====================================================================

    /**
     * ✅ PHASE SIMPLE : Justificatifs par phase
     */
    List<JustificatifChargeback> findByPhaseLitige(String phase);

    /**
     * 🔍 LITIGE + PHASE : Justificatifs d'un litige par phase
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.litigeId = :litigeId " +
            "AND j.phaseLitige = :phase " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findByLitigeIdAndPhase(@Param("litigeId") Long litigeId,
                                                        @Param("phase") String phase);

    /**
     * 📊 PHASE STATS : Nombre de justificatifs par phase
     */
    @Query("SELECT j.phaseLitige, COUNT(j) " +
            "FROM JustificatifChargeback j " +
            "GROUP BY j.phaseLitige " +
            "ORDER BY COUNT(j) DESC")
    List<Object[]> getStatistiquesParPhase();

    /**
     * 🔥 INITIATION : Justificatifs de la phase d'initiation
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.phaseLitige = 'CHARGEBACK_INITIAL' " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findJustificatifsInitiation();

    /**
     * 🔥 REPRÉSENTATION : Justificatifs de la phase de représentation
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.phaseLitige = 'REPRESENTATION' " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findJustificatifsRepresentation();

    /**
     * 🔥 PRÉ-ARBITRAGE : Justificatifs de la phase de pré-arbitrage
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.phaseLitige = 'PRE_ARBITRAGE' " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findJustificatifsPreArbitrage();

    // ====================================================================
    // ✅ RECHERCHES PAR VALIDATION
    // ====================================================================

    /**
     * ✅ VALIDÉS : Justificatifs validés seulement
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.valide = true " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findJustificatifsValides();

    /**
     * ❌ NON VALIDÉS : Justificatifs en attente de validation
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.valide = false " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findJustificatifsNonValides();

    /**
     * 🔍 LITIGE + VALIDATION : Justificatifs d'un litige par statut validation
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.litigeId = :litigeId " +
            "AND j.valide = :valide " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findByLitigeIdAndValide(@Param("litigeId") Long litigeId,
                                                         @Param("valide") Boolean valide);

    /**
     * 📊 VALIDATION STATS : Compteurs de validation
     */
    @Query("SELECT j.valide, COUNT(j) " +
            "FROM JustificatifChargeback j " +
            "GROUP BY j.valide")
    List<Object[]> getStatistiquesValidation();

    /**
     * ⏳ EN ATTENTE : Justificatifs en attente de validation (avec commentaires)
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.valide = false " +
            "AND (j.commentaires IS NULL OR j.commentaires = '') " +
            "ORDER BY j.dateAjout ASC")
    List<JustificatifChargeback> findJustificatifsEnAttenteValidation();

    // ====================================================================
    // 👤 RECHERCHES PAR UTILISATEUR
    // ====================================================================

    /**
     * ✅ UTILISATEUR : Justificatifs transmis par un utilisateur
     */
    List<JustificatifChargeback> findByTransmisParUtilisateurId(Long utilisateurId);

    /**
     * ✅ UTILISATEUR TRIÉS : Justificatifs d'un utilisateur par date
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.transmisParUtilisateurId = :utilisateurId " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findByTransmisParUtilisateurIdOrderByDateAjout(@Param("utilisateurId") Long utilisateurId);

    /**
     * 📊 UTILISATEUR STATS : Nombre de justificatifs par utilisateur
     */
    long countByTransmisParUtilisateurId(Long utilisateurId);

    /**
     * 🔍 UTILISATEUR + LITIGE : Justificatifs d'un utilisateur pour un litige spécifique
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.litigeId = :litigeId " +
            "AND j.transmisParUtilisateurId = :utilisateurId " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findByLitigeIdAndTransmisParUtilisateurId(@Param("litigeId") Long litigeId,
                                                                           @Param("utilisateurId") Long utilisateurId);

    // ====================================================================
    // 📄 RECHERCHES PAR FICHIER
    // ====================================================================

    /**
     * ✅ NOM FICHIER : Recherche par nom de fichier exact
     */
    Optional<JustificatifChargeback> findByNomFichier(String nomFichier);

    /**
     * ✅ CHEMIN FICHIER : Recherche par chemin exact
     */
    Optional<JustificatifChargeback> findByCheminFichier(String cheminFichier);

    /**
     * 🔍 RECHERCHE FICHIER : Recherche partielle dans le nom
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE LOWER(j.nomFichier) LIKE LOWER(CONCAT('%', :nomFichier, '%')) " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findByNomFichierContaining(@Param("nomFichier") String nomFichier);

    /**
     * 📁 FORMAT : Justificatifs par format de fichier
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.formatFichier = :format " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findByFormatFichier(@Param("format") String format);

    /**
     * 📊 FORMATS STATS : Répartition par format de fichier
     */
    @Query("SELECT j.formatFichier, COUNT(j) " +
            "FROM JustificatifChargeback j " +
            "WHERE j.formatFichier IS NOT NULL " +
            "GROUP BY j.formatFichier " +
            "ORDER BY COUNT(j) DESC")
    List<Object[]> getStatistiquesParFormat();

    /**
     * 💾 TAILLE : Justificatifs par taille (plus de X octets)
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.tailleFichier > :tailleLimite " +
            "ORDER BY j.tailleFichier DESC")
    List<JustificatifChargeback> findByTailleFichierGreaterThan(@Param("tailleLimite") Long tailleLimite);

    // ====================================================================
    // 👁️ RECHERCHES PAR VISIBILITÉ
    // ====================================================================

    /**
     * 👁️ VISIBLES : Justificatifs visibles pour l'autre partie
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.visiblePourAutrePartie = true " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findJustificatifsVisibles();

    /**
     * 🔒 PRIVÉS : Justificatifs privés (non visibles pour l'autre partie)
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.visiblePourAutrePartie = false " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findJustificatifsPrives();

    /**
     * 🔍 LITIGE + VISIBILITÉ : Justificatifs d'un litige par visibilité
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.litigeId = :litigeId " +
            "AND j.visiblePourAutrePartie = :visible " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findByLitigeIdAndVisiblePourAutrePartie(@Param("litigeId") Long litigeId,
                                                                         @Param("visible") Boolean visible);

    /**
     * 📊 VISIBILITÉ STATS : Compteurs de visibilité
     */
    @Query("SELECT j.visiblePourAutrePartie, COUNT(j) " +
            "FROM JustificatifChargeback j " +
            "GROUP BY j.visiblePourAutrePartie")
    List<Object[]> getStatistiquesVisibilite();

    // ====================================================================
    // 📅 RECHERCHES PAR PÉRIODE
    // ====================================================================

    /**
     * ✅ PÉRIODE : Justificatifs ajoutés dans une période
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.dateAjout BETWEEN :dateDebut AND :dateFin " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findByPeriodeAjout(@Param("dateDebut") LocalDateTime dateDebut,
                                                    @Param("dateFin") LocalDateTime dateFin);

    /**
     * 🆕 RÉCENTS : Justificatifs ajoutés dans les X derniers jours
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.dateAjout >= :dateLimit " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findJustificatifsRecents(@Param("dateLimit") LocalDateTime dateLimit);

    /**
     * 📊 ÉVOLUTION : Nombre de justificatifs ajoutés par mois
     */
    @Query("SELECT YEAR(j.dateAjout), MONTH(j.dateAjout), COUNT(j) " +
            "FROM JustificatifChargeback j " +
            "WHERE j.dateAjout BETWEEN :dateDebut AND :dateFin " +
            "GROUP BY YEAR(j.dateAjout), MONTH(j.dateAjout) " +
            "ORDER BY YEAR(j.dateAjout), MONTH(j.dateAjout)")
    List<Object[]> getEvolutionMensuelle(@Param("dateDebut") LocalDateTime dateDebut,
                                         @Param("dateFin") LocalDateTime dateFin);

    /**
     * 📅 AUJOURD'HUI : Justificatifs ajoutés aujourd'hui - VERSION ULTRA-SIMPLIFIÉE
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE j.dateAjout >= CURRENT_TIMESTAMP " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findJustificatifsAujourdHui();

    // ====================================================================
    // 🔍 RECHERCHES COMPLEXES MULTICRITÈRES
    // ====================================================================

    /**
     * 🔍 RECHERCHE AVANCÉE : Multicritères avec pagination
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "WHERE (:litigeId IS NULL OR j.litigeId = :litigeId) " +
            "AND (:type IS NULL OR j.typeJustificatif = :type) " +
            "AND (:phase IS NULL OR j.phaseLitige = :phase) " +
            "AND (:valide IS NULL OR j.valide = :valide) " +
            "AND (:visible IS NULL OR j.visiblePourAutrePartie = :visible) " +
            "AND (:utilisateurId IS NULL OR j.transmisParUtilisateurId = :utilisateurId) " +
            "AND (:dateDebutAjout IS NULL OR j.dateAjout >= :dateDebutAjout) " +
            "AND (:dateFinAjout IS NULL OR j.dateAjout <= :dateFinAjout) " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findByMultiplesCriteres(@Param("litigeId") Long litigeId,
                                                         @Param("type") String type,
                                                         @Param("phase") String phase,
                                                         @Param("valide") Boolean valide,
                                                         @Param("visible") Boolean visible,
                                                         @Param("utilisateurId") Long utilisateurId,
                                                         @Param("dateDebutAjout") LocalDateTime dateDebutAjout,
                                                         @Param("dateFinAjout") LocalDateTime dateFinAjout);

    // ====================================================================
    // 🏦 RECHERCHES PAR INSTITUTION (VIA LITIGE)
    // ====================================================================

    /**
     * 🏦 INSTITUTION : Justificatifs des litiges d'une institution
     */
    @Query("SELECT j FROM JustificatifChargeback j " +
            "JOIN Litige l ON j.litigeId = l.id " +
            "WHERE (l.transaction.banqueEmettrice.id = :institutionId OR " +
            "l.transaction.banqueAcquereuse.id = :institutionId) " +
            "ORDER BY j.dateAjout DESC")
    List<JustificatifChargeback> findByInstitutionId(@Param("institutionId") Long institutionId);

    /**
     * 📊 INSTITUTION STATS : Nombre de justificatifs par institution
     */
    @Query("SELECT CASE " +
            "WHEN l.transaction.banqueEmettrice.id = :institutionId THEN l.transaction.banqueEmettrice.nom " +
            "WHEN l.transaction.banqueAcquereuse.id = :institutionId THEN l.transaction.banqueAcquereuse.nom " +
            "END, COUNT(j) " +
            "FROM JustificatifChargeback j " +
            "JOIN Litige l ON j.litigeId = l.id " +
            "WHERE (l.transaction.banqueEmettrice.id = :institutionId OR " +
            "l.transaction.banqueAcquereuse.id = :institutionId) " +
            "GROUP BY l.transaction.banqueEmettrice.nom, l.transaction.banqueAcquereuse.nom")
    List<Object[]> getStatistiquesParInstitution(@Param("institutionId") Long institutionId);

    // ====================================================================
    // 🔧 MÉTHODES UTILITAIRES
    // ====================================================================

    /**
     * ✅ VALIDATION : Vérifie si un fichier existe déjà
     */
    boolean existsByCheminFichier(String cheminFichier);

    /**
     * ✅ VALIDATION : Vérifie si un nom de fichier existe pour un litige
     */
    @Query("SELECT COUNT(j) > 0 FROM JustificatifChargeback j " +
            "WHERE j.litigeId = :litigeId " +
            "AND j.nomFichier = :nomFichier")
    boolean existsByLitigeIdAndNomFichier(@Param("litigeId") Long litigeId,
                                          @Param("nomFichier") String nomFichier);

    /**
     * 🆔 DEBUG : Tous les IDs pour diagnostic
     */
    @Query("SELECT j.id FROM JustificatifChargeback j ORDER BY j.id ASC")
    List<Long> findAllIds();

    /**
     * 📊 DASHBOARD : Statistiques générales pour dashboard
     */
    @Query("SELECT " +
            "COUNT(j) as total, " +
            "SUM(CASE WHEN j.valide = true THEN 1 ELSE 0 END) as valides, " +
            "SUM(CASE WHEN j.visiblePourAutrePartie = true THEN 1 ELSE 0 END) as visibles, " +
            "COUNT(DISTINCT j.litigeId) as litiges_avec_justificatifs " +
            "FROM JustificatifChargeback j")
    Object[] getStatistiquesDashboard();
}