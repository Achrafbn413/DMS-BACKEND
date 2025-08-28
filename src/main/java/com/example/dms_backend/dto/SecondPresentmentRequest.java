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
 * DTO de requête pour le Second Presentment (Pre-Arbitrage)
 * Étape 3 du workflow : Banque Émettrice rejette la representation
 * L'employé de la banque émettrice renforce ses arguments avant arbitrage
 *
 * @author Système DMS Bancaire Maroc
 * @version 1.0 Production
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecondPresentmentRequest {

    // ========================================
    // IDENTIFICATION (Automatique)
    // ========================================

    @NotNull(message = "L'ID du litige est obligatoire")
    @JsonProperty("litigeId")
    private Long litigeId;

    @NotNull(message = "L'ID de la banque émettrice est obligatoire")
    @JsonProperty("banqueEmettriceId")
    private Long banqueEmettriceId;

    @NotNull(message = "L'ID de l'utilisateur émetteur est obligatoire")
    @JsonProperty("utilisateurEmetteurId")
    private Long utilisateurEmetteurId; // Employé qui traite le second presentment

    // Alias pour compatibilité
    @JsonProperty("employeId")
    private Long employeId;

    // ========================================
    // REJET DE LA REPRESENTATION (Interface Employé)
    // ========================================

    @NotNull(message = "Le motif de rejet est obligatoire")
    @Size(min = 20, max = 500, message = "Le motif de rejet doit contenir entre 20 et 500 caractères")
    @JsonProperty("motifRejet")
    private String motifRejet;
    /* Exemples de motifs :
     * - "Les preuves fournies ne démontrent pas l'autorisation du client"
     * - "Le ticket signé ne correspond pas à l'écriture du client"
     * - "L'authentification 3D Secure a échoué selon nos logs"
     * - "La date/heure de la transaction ne correspond pas aux déclarations"
     */

    @NotNull(message = "La réfutation détaillée est obligatoire")
    @Size(min = 50, max = 4000, message = "La réfutation doit contenir entre 50 et 4000 caractères")
    @JsonProperty("refutationDetaillee")
    private String refutationDetaillee;

    @JsonProperty("refutationPointParPoint")
    private String refutationPointParPoint; // Alias pour compatibilité
    /* Exemple :
     * "Après analyse approfondie de la representation fournie par la banque acquéreuse :
     *
     * 1. CONCERNANT LE TICKET SIGNÉ :
     * L'écriture ne correspond pas aux échantillons de signature du client en notre possession.
     * Expertise graphologique recommandée.
     *
     * 2. CONCERNANT L'AUTHENTIFICATION 3D SECURE :
     * Nos logs montrent un échec d'authentification à 14h29, soit 1 minute avant la transaction.
     * Ceci indique une possible utilisation frauduleuse.
     *
     * 3. CONCERNANT LA GÉOLOCALISATION :
     * Le client était en déplacement professionnel à Rabat selon son employeur.
     * La transaction à Casablanca est donc géographiquement impossible.
     *
     * Nous maintenons donc notre demande de chargeback."
     */

    // ========================================
    // NOUVEAUX ARGUMENTS ET PREUVES
    // ========================================

    @Size(max = 2000, message = "Les arguments supplémentaires ne peuvent pas dépasser 2000 caractères")
    @JsonProperty("argumentsSupplementaires")
    private String argumentsSupplementaires;

    @JsonProperty("nouvellesSpreuves")
    private List<String> nouvellesSpreuves; // Liste des noms de fichiers (pour compatibilité service)

    @JsonProperty("nouvellesPreuves")
    private List<MultipartFile> nouvellesPreuves; // Nouvelles preuves à l'appui

    @JsonProperty("typesNouvellesPreuves")
    private List<String> typesNouvellesPreuves;
    /* Types de nouvelles preuves :
     * - "EXPERTISE_SIGNATURE" : Rapport d'expertise graphologique
     * - "LOGS_AUTHENTIFICATION" : Logs détaillés des tentatives d'auth
     * - "GEOLOCALISATION" : Preuves de localisation du client
     * - "ATTESTATION_EMPLOYEUR" : Attestation de présence ailleurs
     * - "RAPPORT_FRAUDE" : Rapport d'enquête fraude interne
     * - "HISTORIQUE_CLIENT" : Habitudes de consommation du client
     * - "ANALYSE_COMPORTEMENTALE" : Analyse des patterns de transaction
     * - "TEMOIGNAGE_CLIENT" : Déclaration assermentée du client
     */

    @JsonProperty("commentairesNouvellesPreuves")
    private List<String> commentairesNouvellesPreuves;

    // ========================================
    // ANALYSE TECHNIQUE
    // ========================================

    @Size(max = 1500, message = "L'analyse technique ne peut pas dépasser 1500 caractères")
    @JsonProperty("analyseTechnique")
    private String analyseTechnique; // Analyse technique détaillée

    @JsonProperty("indicateursFraude")
    private List<String> indicateursFraude;
    /* Indicateurs possibles :
     * - "GEOLOCALISATION_INCOHERENTE"
     * - "HORAIRE_INHABITUEL"
     * - "MONTANT_ATYPIQUE"
     * - "ECHEC_AUTHENTIFICATION_PREALABLE"
     * - "CARTE_DECLAREE_EN_POSSESSION"
     * - "PATTERN_TRANSACTION_SUSPECT"
     */

    @JsonProperty("scoreRisqueFraude")
    private Integer scoreRisqueFraude; // Score de 0 à 100

    // ========================================
    // MONTANT ET IMPACT
    // ========================================

    @DecimalMin(value = "0.01", message = "Le montant final contesté doit être supérieur à 0")
    @JsonProperty("montantFinalConteste")
    private BigDecimal montantFinalConteste;

    @Size(max = 500, message = "La justification du montant ne peut pas dépasser 500 caractères")
    @JsonProperty("justificationMontant")
    private String justificationMontant;

    @JsonProperty("impactClient")
    private String impactClient; // Impact sur le client (financier, moral, etc.)

    // ========================================
    // STRATÉGIE ET ESCALADE
    // ========================================

    @NotNull(message = "La décision d'escalade est obligatoire")
    @JsonProperty("demandeArbitrage")
    private Boolean demandeArbitrage = false; // Demander arbitrage immédiat ?

    @Pattern(regexp = "BASSE|NORMALE|HAUTE|CRITIQUE|URGENTE", message = "Priorité d'escalade invalide")
    @JsonProperty("prioriteEscalade")
    private String prioriteEscalade = "NORMALE";

    @Size(max = 1000, message = "La stratégie d'arbitrage ne peut pas dépasser 1000 caractères")
    @JsonProperty("strategieArbitrage")
    private String strategieArbitrage; // Stratégie si arbitrage

    @DecimalMin(value = "0.0", message = "Le coût estimé ne peut pas être négatif")
    @JsonProperty("coutArbitrageEstime")
    private BigDecimal coutArbitrageEstime; // Coût estimé de l'arbitrage

    // ========================================
    // NÉGOCIATION ET ALTERNATIVES
    // ========================================

    @JsonProperty("propositionNegociation")
    private Boolean propositionNegociation = false; // Proposer négociation ?

    @DecimalMin(value = "0.0", message = "Le montant de négociation ne peut pas être négatif")
    @JsonProperty("montantNegociation")
    private BigDecimal montantNegociation; // Montant proposé en négociation

    @Size(max = 1000, message = "Les termes de négociation ne peuvent pas dépasser 1000 caractères")
    @JsonProperty("termesNegociation")
    private String termesNegociation;

    @JsonProperty("delaiReponseNegociation")
    private Integer delaiReponseNegociation = 5; // Délai en jours pour réponse

    // ========================================
    // CONTEXTE ET HISTORIQUE
    // ========================================

    @Size(max = 1000, message = "Le contexte client ne peut pas dépasser 1000 caractères")
    @JsonProperty("contexteClient")
    private String contexteClient; // Contexte particulier du client

    @JsonProperty("historiqueSimilaire")
    private Boolean historiqueSimilaire = false; // Cas similaires dans le passé ?

    @Size(max = 500, message = "La référence historique ne peut pas dépasser 500 caractères")
    @JsonProperty("referenceHistorique")
    private String referenceHistorique; // Référence aux cas similaires

    // ========================================
    // NOTIFICATIONS ET SUIVI
    // ========================================

    @JsonProperty("notifierClient")
    private Boolean notifierClient = true;

    @JsonProperty("notifierHierarchie")
    private Boolean notifierHierarchie = false; // Notifier la hiérarchie ?

    @JsonProperty("niveauEscalade")
    private String niveauEscalade = "STANDARD"; // STANDARD, MANAGER, DIRECTION

    @Size(max = 100, message = "La référence interne ne peut pas dépasser 100 caractères")
    @JsonProperty("referenceInterne")
    private String referenceInterne;

    // ========================================
    // MÉTHODES GETTER PERSONNALISÉES
    // ========================================

    /**
     * Getter pour compatibilité avec le service - retourne utilisateurEmetteurId
     */
    public Long getUtilisateurEmetteurId() {
        return utilisateurEmetteurId != null ? utilisateurEmetteurId : employeId;
    }

    /**
     * Getter pour compatibilité avec le service - retourne nouvellesSpreuves
     */
    public List<String> getNouvellesSpreuves() {
        return nouvellesSpreuves;
    }

    /**
     * Getter alternatif pour compatibilité - avec S supplémentaire
     */
    public List<String> getNouvellesSPreuves() {
        return nouvellesSpreuves;
    }

    /**
     * Getter alternatif pour compatibilité - variante sans S
     */
    public List<String> getNouvelleSPreuves() {
        return nouvellesSpreuves;
    }

    /**
     * Getter pour compatibilité avec le service - retourne refutationPointParPoint
     */
    public String getRefutationPointParPoint() {
        return refutationPointParPoint != null ? refutationPointParPoint : refutationDetaillee;
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    /**
     * Détermine si c'est un cas critique nécessitant arbitrage
     */
    public boolean isCasCritique() {
        return "CRITIQUE".equals(prioriteEscalade) ||
                "URGENTE".equals(prioriteEscalade) ||
                (montantFinalConteste != null && montantFinalConteste.compareTo(new BigDecimal("10000")) > 0) ||
                (scoreRisqueFraude != null && scoreRisqueFraude >= 80);
    }

    /**
     * Vérifie si l'arbitrage est inévitable
     */
    public boolean isArbitrageInevitable() {
        return Boolean.TRUE.equals(demandeArbitrage) ||
                isCasCritique() ||
                !Boolean.TRUE.equals(propositionNegociation);
    }

    /**
     * Retourne le nombre de nouvelles preuves
     */
    public int getNombreNouvellesPreuves() {
        if (nouvellesSpreuves != null && !nouvellesSpreuves.isEmpty()) {
            return nouvellesSpreuves.size();
        }
        return nouvellesPreuves != null ? nouvellesPreuves.size() : 0;
    }

    /**
     * Calcule le niveau de confiance dans le dossier
     */
    public String getNiveauConfiance() {
        if (scoreRisqueFraude != null) {
            if (scoreRisqueFraude >= 90) return "TRES_ELEVE";
            if (scoreRisqueFraude >= 70) return "ELEVE";
            if (scoreRisqueFraude >= 50) return "MOYEN";
            return "FAIBLE";
        }

        int points = 0;
        if (getNombreNouvellesPreuves() > 0) points += 30;
        if (analyseTechnique != null && !analyseTechnique.isEmpty()) points += 25;
        if (indicateursFraude != null && indicateursFraude.size() > 2) points += 25;
        if (argumentsSupplementaires != null && argumentsSupplementaires.length() > 500) points += 20;

        if (points >= 80) return "TRES_ELEVE";
        if (points >= 60) return "ELEVE";
        if (points >= 40) return "MOYEN";
        return "FAIBLE";
    }

    /**
     * Vérifie si le dossier est complet pour arbitrage
     */
    public boolean isDossierCompletPourArbitrage() {
        return motifRejet != null &&
                refutationDetaillee != null &&
                montantFinalConteste != null &&
                (getNombreNouvellesPreuves() > 0 ||
                        (analyseTechnique != null && !analyseTechnique.isEmpty()));
    }
}