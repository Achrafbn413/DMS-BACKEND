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
 * DTO de requête pour la representation (Banque Acquéreuse)
 * Étape 2 du workflow : Réponse avec preuves du commerçant
 * L'employé de la banque acquéreuse défend le commerçant
 *
 * @author Système DMS Bancaire Maroc
 * @version 1.0 Production
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepresentationRequest {

    // ========================================
    // IDENTIFICATION (Automatique)
    // ========================================

    @NotNull(message = "L'ID du litige est obligatoire")
    @JsonProperty("litigeId")
    private Long litigeId;

    @NotNull(message = "L'ID de la banque acquéreuse est obligatoire")
    @JsonProperty("banqueAcquereuseId")
    private Long banqueAcquereuseId;

    @NotNull(message = "L'ID de l'utilisateur acquéreur est obligatoire")
    @JsonProperty("utilisateurAcquereurId")
    private Long utilisateurAcquereurId; // Employé qui traite la representation

    // Alias pour compatibilité
    @JsonProperty("employeId")
    private Long employeId;

    // ========================================
    // DÉCISION DE RÉPONSE (Interface Employé)
    // ========================================

    @NotNull(message = "La décision de réponse est obligatoire")
    @Pattern(regexp = "ACCEPTATION_TOTALE|ACCEPTATION_PARTIELLE|CONTESTATION_TOTALE",
            message = "Type de réponse invalide")
    @JsonProperty("typeReponse")
    private String typeReponse;
    /*
     * ACCEPTATION_TOTALE : On accepte le chargeback, on rembourse
     * ACCEPTATION_PARTIELLE : On accepte une partie du montant
     * CONTESTATION_TOTALE : On conteste avec preuves du commerçant
     */

    @JsonProperty("accepteChargeback")
    private Boolean accepteChargeback = false; // Calculé selon typeReponse

    // ========================================
    // MONTANTS (Si acceptation partielle)
    // ========================================

    @DecimalMin(value = "0.0", message = "Le montant accepté ne peut pas être négatif")
    @JsonProperty("montantAccepte")
    private BigDecimal montantAccepte; // Si acceptation partielle

    @Size(max = 500, message = "La justification du montant ne peut pas dépasser 500 caractères")
    @JsonProperty("justificationMontant")
    private String justificationMontant; // Pourquoi ce montant seulement

    // ========================================
    // RÉPONSE DÉTAILLÉE (Interface Employé)
    // ========================================

    @NotNull(message = "La réponse détaillée est obligatoire")
    @Size(min = 20, max = 3000, message = "La réponse doit contenir entre 20 et 3000 caractères")
    @JsonProperty("reponseDetaillee")
    private String reponseDetaillee;
    /* Exemple pour contestation :
     * "Après analyse, la transaction est légitime. Le client a bien effectué cet achat
     * le 15/01/2024 à 14h30 chez MARJANE CASABLANCA. Nous disposons de :
     * - Ticket signé par le client
     * - Authentification 3D Secure réussie
     * - Caméras de surveillance confirmant la présence du client
     * Nous contestons donc ce chargeback."
     */

    @Size(max = 1000, message = "Les arguments ne peuvent pas dépasser 1000 caractères")
    @JsonProperty("argumentsDefense")
    private String argumentsDefense; // Arguments de défense

    @Size(max = 1000, message = "Les arguments ne peuvent pas dépasser 1000 caractères")
    @JsonProperty("argumentsJuridiques")
    private String argumentsJuridiques; // Arguments légaux/réglementaires

    // ========================================
    // UPLOAD PREUVES (Interface Employé)
    // ========================================

    @JsonProperty("justificatifsDefense")
    private List<String> justificatifsDefense; // Liste des noms de fichiers de défense

    @JsonProperty("fichiersPreuves")
    private List<MultipartFile> fichiersPreuves; // Preuves du commerçant

    @JsonProperty("typesPreuves")
    private List<String> typesPreuves; // Type de chaque preuve
    /* Types de preuves :
     * - "TICKET_SIGNE" : Ticket de caisse signé
     * - "PREUVE_LIVRAISON" : Bon de livraison, accusé réception
     * - "LOG_3D_SECURE" : Logs d'authentification 3D Secure
     * - "AUTORISATION_BANCAIRE" : Autorisation de paiement
     * - "CONTRAT_VENTE" : Contrat ou facture de vente
     * - "VIDEO_SURVEILLANCE" : Extraits vidéo (si autorisé)
     * - "COMMUNICATION_CLIENT" : Emails, SMS avec le client
     * - "PREUVE_REMBOURSEMENT" : Si déjà remboursé
     */

    @JsonProperty("commentairesPreuves")
    private List<String> commentairesPreuves; // Description de chaque preuve

    // ========================================
    // INFORMATIONS COMMERÇANT
    // ========================================

    @JsonProperty("commercantId")
    private Long commercantId; // ID du commerçant concerné

    @Size(max = 200, message = "Le nom du commerçant ne peut pas dépasser 200 caractères")
    @JsonProperty("nomCommercant")
    private String nomCommercant;

    @Size(max = 1000, message = "La déclaration commerçant ne peut pas dépasser 1000 caractères")
    @JsonProperty("declarationCommercant")
    private String declarationCommercant; // Ce que dit le commerçant

    @JsonProperty("commercantAccepteRemboursement")
    private Boolean commercantAccepteRemboursement = false; // Le commerçant accepte-t-il de rembourser ?

    // ========================================
    // PARAMÈTRES DE TRAITEMENT
    // ========================================

    @JsonProperty("demandeDelaiSupplementaire")
    private Boolean demandeDelaiSupplementaire = false; // Besoin de plus de temps ?

    @Min(value = 1, message = "Le délai supplémentaire doit être d'au moins 1 jour")
    @Max(value = 30, message = "Le délai supplémentaire ne peut pas dépasser 30 jours")
    @JsonProperty("joursDelaiSupplementaire")
    private Integer joursDelaiSupplementaire;

    @Size(max = 500, message = "La justification du délai ne peut pas dépasser 500 caractères")
    @JsonProperty("motifDelaiSupplementaire")
    private String motifDelaiSupplementaire;

    // ========================================
    // ESCALADE ET SUIVI
    // ========================================

    @JsonProperty("recommandeEscalade")
    private Boolean recommandeEscalade = false; // Recommander l'arbitrage ?

    @Size(max = 500, message = "La justification d'escalade ne peut pas dépasser 500 caractères")
    @JsonProperty("justificationEscalade")
    private String justificationEscalade;

    @JsonProperty("niveauConfiance")
    private String niveauConfiance = "MOYEN"; // FAIBLE, MOYEN, ELEVE - Confiance dans la défense

    // ========================================
    // NOTIFICATIONS
    // ========================================

    @JsonProperty("notifierCommercant")
    private Boolean notifierCommercant = true; // Notifier le commerçant de la réponse

    @JsonProperty("notifierBanqueEmettrice")
    private Boolean notifierBanqueEmettrice = true; // Notifier la banque émettrice

    @Size(max = 100, message = "La référence interne ne peut pas dépasser 100 caractères")
    @JsonProperty("referenceInterne")
    private String referenceInterne; // Référence dossier interne

    // ========================================
    // MÉTHODES GETTER PERSONNALISÉES
    // ========================================

    /**
     * Getter pour compatibilité avec le service - retourne utilisateurAcquereurId
     */
    public Long getUtilisateurAcquereurId() {
        return utilisateurAcquereurId != null ? utilisateurAcquereurId : employeId;
    }

    /**
     * Getter pour compatibilité avec le service - retourne argumentsDefense
     */
    public String getArgumentsDefense() {
        return argumentsDefense != null ? argumentsDefense : argumentsJuridiques;
    }

    /**
     * Getter pour compatibilité avec le service - retourne justificatifsDefense
     */
    public List<String> getJustificatifsDefense() {
        return justificatifsDefense;
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    /**
     * Calcule automatiquement accepteChargeback selon typeReponse
     */
    public void calculerAcceptation() {
        if (typeReponse != null) {
            this.accepteChargeback = typeReponse.startsWith("ACCEPTATION");
        }
    }

    /**
     * Vérifie si c'est une contestation
     */
    public boolean isContestation() {
        return "CONTESTATION_TOTALE".equals(typeReponse);
    }

    /**
     * Retourne le nombre de preuves fournies
     */
    public int getNombrePreuves() {
        if (justificatifsDefense != null && !justificatifsDefense.isEmpty()) {
            return justificatifsDefense.size();
        }
        return fichiersPreuves != null ? fichiersPreuves.size() : 0;
    }

    /**
     * Vérifie si la réponse est complète
     */
    public boolean isReponseComplete() {
        boolean baseComplete = typeReponse != null && reponseDetaillee != null;

        if (isContestation()) {
            return baseComplete && getNombrePreuves() > 0; // Contestation = besoin de preuves
        }

        return baseComplete; // Acceptation = pas forcément besoin de preuves
    }

    /**
     * Détermine si c'est un cas complexe nécessitant escalade
     */
    public boolean isCasComplexe() {
        return Boolean.TRUE.equals(recommandeEscalade) ||
                (montantAccepte != null && montantAccepte.compareTo(new BigDecimal("5000")) > 0) ||
                Boolean.TRUE.equals(demandeDelaiSupplementaire);
    }
}