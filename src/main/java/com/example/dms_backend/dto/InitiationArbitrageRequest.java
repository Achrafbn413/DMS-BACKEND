package com.example.dms_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de requête pour initier un arbitrage
 * Étape 4 du workflow : Demande d'arbitrage à l'admin
 * Une des banques demande une décision finale
 *
 * @author Système DMS Bancaire Maroc
 * @version 1.0 Production
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiationArbitrageRequest {

    // ========================================
    // IDENTIFICATION (Automatique)
    // ========================================

    @NotNull(message = "L'ID du litige est obligatoire")
    @JsonProperty("litigeId")
    private Long litigeId;

    @NotNull(message = "L'ID de la banque demandeuse est obligatoire")
    @JsonProperty("banqueDemandeuse")
    private Long banqueDemandeuse; // Banque qui demande l'arbitrage

    @NotNull(message = "L'ID de l'utilisateur demandeur est obligatoire")
    @JsonProperty("utilisateurDemandeurId")
    private Long utilisateurDemandeurId; // Employé qui initie l'arbitrage

    // Alias pour compatibilité
    @JsonProperty("employeId")
    private Long employeId;

    // ========================================
    // JUSTIFICATION ARBITRAGE (Interface Employé)
    // ========================================

    @NotNull(message = "La justification de la demande est obligatoire")
    @Size(min = 50, max = 2000, message = "La justification doit contenir entre 50 et 2000 caractères")
    @JsonProperty("justificationDemande")
    private String justificationDemande;

    @NotNull(message = "La justification de l'arbitrage est obligatoire")
    @Size(min = 50, max = 2000, message = "La justification doit contenir entre 50 et 2000 caractères")
    @JsonProperty("justificationArbitrage")
    private String justificationArbitrage;
    /* Exemple :
     * "Après épuisement de toutes les procédures de négociation entre les parties :
     *
     * CONTEXTE :
     * - Chargeback initial pour fraude suspectée (montant: 1500 MAD)
     * - Representation de la banque acquéreuse avec preuves techniques
     * - Second presentment avec nouvelles expertises de notre part
     *
     * POINTS DE DÉSACCORD :
     * 1. Authenticité de la signature sur le ticket de vente
     * 2. Validité de l'authentification 3D Secure
     * 3. Géolocalisation du client au moment de la transaction
     *
     * ENJEUX :
     * - Montant significatif pour nos deux institutions
     * - Précédent important pour des cas similaires
     * - Nécessité d'une décision impartiale et définitive
     *
     * Nous sollicitons donc l'arbitrage du réseau carte pour trancher ce différend."
     */

    @Size(max = 1500, message = "Le résumé du différend ne peut pas dépasser 1500 caractères")
    @JsonProperty("resumeDifferend")
    private String resumeDifferend; // Résumé objectif du désaccord

    // ========================================
    // ARGUMENTS ET POSITION
    // ========================================

    @NotNull(message = "La position de la banque est obligatoire")
    @Size(min = 30, max = 2000, message = "La position doit contenir entre 30 et 2000 caractères")
    @JsonProperty("positionBanque")
    private String positionBanque; // Position finale de la banque

    @JsonProperty("argumentsCles")
    private List<String> argumentsCles; // Liste des arguments principaux

    @Size(max = 1000, message = "Les arguments juridiques ne peuvent pas dépasser 1000 caractères")
    @JsonProperty("argumentsJuridiques")
    private String argumentsJuridiques; // Références légales/réglementaires

    // ========================================
    // DOSSIER COMPLET POUR ARBITRAGE
    // ========================================

    @JsonProperty("documentsFinaux")
    private List<MultipartFile> documentsFinaux; // Dossier complet

    @JsonProperty("typesDocuments")
    private List<String> typesDocuments;
    /* Types de documents finaux :
     * - "DOSSIER_COMPLET" : Compilation de tout le dossier
     * - "CHRONOLOGIE_DETAILLEE" : Timeline complète des événements
     * - "EXPERTISE_TECHNIQUE" : Rapports d'experts
     * - "PREUVES_PRINCIPALES" : Preuves les plus importantes
     * - "CORRESPONDANCES" : Tous les échanges entre banques
     * - "ANALYSE_JURIDIQUE" : Analyse légale du cas
     * - "IMPACT_FINANCIER" : Évaluation des coûts/bénéfices
     */

    @JsonProperty("commentairesDocuments")
    private List<String> commentairesDocuments;

    // ========================================
    // COÛTS ET PRIORITÉ
    // ========================================

    @DecimalMin(value = "100.0", message = "Le coût estimé doit être d'au moins 100 MAD")
    @JsonProperty("coutEstime")
    private BigDecimal coutEstime; // Coût estimé de l'arbitrage

    @NotNull(message = "La priorité est obligatoire")
    @Pattern(regexp = "BASSE|NORMALE|HAUTE|CRITIQUE|URGENTE", message = "Priorité invalide")
    @JsonProperty("priorite")
    private String priorite = "NORMALE";

    @JsonProperty("demandeUrgente")
    private Boolean demandeUrgente = false;

    @Size(max = 500, message = "La justification d'urgence ne peut pas dépasser 500 caractères")
    @JsonProperty("justificationUrgence")
    private String justificationUrgence;

    // ========================================
    // DÉLAIS ET PLANNING
    // ========================================

    @Min(value = 1, message = "Le délai souhaité doit être d'au moins 1 jour")
    @Max(value = 90, message = "Le délai souhaité ne peut pas dépasser 90 jours")
    @JsonProperty("delaiSouhaite")
    private Integer delaiSouhaite = 30; // Délai souhaité pour la décision (en jours)

    @JsonProperty("contraintesDelai")
    private String contraintesDelai; // Contraintes spécifiques de délai

    // ========================================
    // TYPE ET COMPLEXITÉ
    // ========================================

    @Pattern(regexp = "STANDARD|EXPEDIE|COMPLEXE|TECHNIQUE", message = "Type d'arbitrage invalide")
    @JsonProperty("typeArbitrage")
    private String typeArbitrage = "STANDARD";

    @Pattern(regexp = "SIMPLE|MOYEN|COMPLEXE|TRES_COMPLEXE", message = "Niveau de complexité invalide")
    @JsonProperty("niveauComplexite")
    private String niveauComplexite = "MOYEN";

    @JsonProperty("expertiseRequise")
    private List<String> expertiseRequise; // Types d'expertise nécessaires
    /* Expertises possibles :
     * - "TECHNIQUE_BANCAIRE"
     * - "SECURITE_PAIEMENT"
     * - "ANALYSE_FRAUDE"
     * - "DROIT_BANCAIRE"
     * - "GRAPHOLOGIE"
     * - "INFORMATIQUE_FORENSIQUE"
     */

    // ========================================
    // PRÉFÉRENCES ARBITRAGE
    // ========================================

    @JsonProperty("arbitrePreferentiel")
    private String arbitrePreferentiel; // Arbitre souhaité (si applicable)

    @JsonProperty("modeArbitrage")
    private String modeArbitrage = "DOCUMENTAIRE"; // DOCUMENTAIRE, AUDITION, MIXTE

    @JsonProperty("audienceRequise")
    private Boolean audienceRequise = false; // Demande d'audience ?

    @Size(max = 500, message = "La justification d'audience ne peut pas dépasser 500 caractères")
    @JsonProperty("justificationAudience")
    private String justificationAudience;

    // ========================================
    // IMPACT ET CONSÉQUENCES
    // ========================================

    @Size(max = 1000, message = "L'impact business ne peut pas dépasser 1000 caractères")
    @JsonProperty("impactBusiness")
    private String impactBusiness; // Impact sur l'activité

    @JsonProperty("precedentImportant")
    private Boolean precedentImportant = false; // Cas faisant précédent ?

    @Size(max = 500, message = "La justification de précédent ne peut pas dépasser 500 caractères")
    @JsonProperty("justificationPrecedent")
    private String justificationPrecedent;

    @JsonProperty("impactClient")
    private String impactClient; // Impact sur le client final

    // ========================================
    // NOTIFICATIONS ET SUIVI
    // ========================================

    @JsonProperty("notifierHierarchie")
    private Boolean notifierHierarchie = true; // Notifier la hiérarchie

    @JsonProperty("niveauNotification")
    private String niveauNotification = "DIRECTION"; // MANAGER, DIRECTION, COMEX

    @JsonProperty("copieInteressees")
    private List<String> copieInteressees; // Emails des parties intéressées

    @Size(max = 100, message = "La référence interne ne peut pas dépasser 100 caractères")
    @JsonProperty("referenceInterne")
    private String referenceInterne;

    // ========================================
    // MÉTHODES GETTER PERSONNALISÉES
    // ========================================

    /**
     * Getter pour compatibilité avec le service - retourne utilisateurDemandeurId
     */
    public Long getUtilisateurDemandeurId() {
        return utilisateurDemandeurId != null ? utilisateurDemandeurId : employeId;
    }

    /**
     * Getter pour compatibilité avec le service - retourne justificationDemande
     */
    public String getJustificationDemande() {
        return justificationDemande != null ? justificationDemande : justificationArbitrage;
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    /**
     * Détermine si c'est un arbitrage critique
     */
    public boolean isArbitrageCritique() {
        return "CRITIQUE".equals(priorite) ||
                "URGENTE".equals(priorite) ||
                Boolean.TRUE.equals(demandeUrgente) ||
                Boolean.TRUE.equals(precedentImportant) ||
                (coutEstime != null && coutEstime.compareTo(new BigDecimal("5000")) > 0);
    }

    /**
     * Calcule le score de complexité
     */
    public int getScoreComplexite() {
        int score = 0;

        // Base selon niveau de complexité
        switch (niveauComplexite) {
            case "TRES_COMPLEXE": score += 40; break;
            case "COMPLEXE": score += 30; break;
            case "MOYEN": score += 20; break;
            case "SIMPLE": score += 10; break;
        }

        // Bonus selon critères
        if (expertiseRequise != null && expertiseRequise.size() > 2) score += 20;
        if (Boolean.TRUE.equals(audienceRequise)) score += 15;
        if (Boolean.TRUE.equals(precedentImportant)) score += 15;
        if (documentsFinaux != null && documentsFinaux.size() > 10) score += 10;

        return Math.min(score, 100);
    }

    /**
     * Retourne le nombre de documents fournis
     */
    public int getNombreDocuments() {
        return documentsFinaux != null ? documentsFinaux.size() : 0;
    }

    /**
     * Vérifie si le dossier est complet
     */
    public boolean isDossierComplet() {
        return (justificationDemande != null || justificationArbitrage != null) &&
                positionBanque != null &&
                coutEstime != null &&
                getNombreDocuments() > 0 &&
                (argumentsCles != null && !argumentsCles.isEmpty());
    }

    /**
     * Détermine la classe de priorité pour l'admin
     */
    public String getClassePriorite() {
        if (isArbitrageCritique()) return "priority-critical";
        if ("HAUTE".equals(priorite)) return "priority-high";
        if ("NORMALE".equals(priorite)) return "priority-normal";
        return "priority-low";
    }
}