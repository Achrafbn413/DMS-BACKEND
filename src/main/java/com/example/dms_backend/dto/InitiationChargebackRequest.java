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
 * DTO de requête pour initier un chargeback (Interface Employé)
 * L'employé saisit le motif et upload les justificatifs
 *
 * @author Système DMS Bancaire Maroc
 * @version 1.0 Production
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitiationChargebackRequest {

    // ========================================
    // DONNÉES TRANSACTION (Automatiques)
    // ========================================

    @NotNull(message = "L'ID du litige est obligatoire")
    @JsonProperty("litigeId")
    private Long litigeId;

    @NotNull(message = "L'ID de la transaction est obligatoire")
    @JsonProperty("transactionId")
    private Long transactionId;

    @NotNull(message = "L'ID de la banque émettrice est obligatoire")
    @JsonProperty("banqueEmettriceId")
    private Long banqueEmettriceId;

    @NotNull(message = "L'ID de l'utilisateur émetteur est obligatoire")
    @JsonProperty("utilisateurEmetteurId")
    private Long utilisateurEmetteurId; // L'employé qui initie le chargeback

    // Alias pour compatibilité
    @JsonProperty("employeId")
    private Long employeId;

    // ========================================
    // SAISIE EMPLOYÉ (Interface)
    // ========================================

    @NotNull(message = "Le motif du chargeback est obligatoire")
    @Size(min = 10, max = 200, message = "Le motif doit contenir entre 10 et 200 caractères")
    @JsonProperty("motifChargeback")
    private String motifChargeback;
    /* Exemples de motifs :
     * - "Fraude suspectée - Transaction non autorisée par le client"
     * - "Service non fourni - Produit jamais reçu par le client"
     * - "Montant incorrect - Débité 500 MAD au lieu de 50 MAD"
     * - "Problème technique - Double débit sur le compte client"
     */

    @NotNull(message = "La description détaillée est obligatoire")
    @Size(min = 20, max = 3000, message = "La description doit contenir entre 20 et 3000 caractères")
    @JsonProperty("description")
    private String description;
    /* Exemple :
     * "Le client M. ALAMI a signalé une transaction de 1500 MAD
     * effectuée le 15/01/2024 chez MARJANE CASABLANCA qu'il n'a jamais autorisée.
     * Sa carte était en sa possession. Demande de remboursement immédiat."
     */

    // ========================================
    // UPLOAD JUSTIFICATIFS (Interface)
    // ========================================

    @JsonProperty("justificatifs")
    private List<String> justificatifs; // Liste des noms de fichiers

    @JsonProperty("fichiersJustificatifs")
    private List<MultipartFile> fichiersJustificatifs; // Upload de fichiers
    /* Types acceptés :
     * - PDF (déclaration client, courrier)
     * - JPG/PNG (photos, captures d'écran)
     * - DOC/DOCX (rapports, attestations)
     */

    @JsonProperty("typesJustificatifs")
    private List<String> typesJustificatifs; // Type de chaque fichier
    /* Types possibles :
     * - "DECLARATION_CLIENT"
     * - "RELEVE_BANCAIRE"
     * - "PHOTO_CARTE"
     * - "COURRIER_RECLAMATION"
     * - "PREUVE_NON_RECEPTION"
     * - "ATTESTATION_FRAUDE"
     */

    @JsonProperty("commentairesFichiers")
    private List<String> commentairesFichiers; // Commentaire pour chaque fichier

    // ========================================
    // PARAMÈTRES CHARGEBACK
    // ========================================

    @DecimalMin(value = "0.01", message = "Le montant contesté doit être supérieur à 0")
    @JsonProperty("montantConteste")
    private BigDecimal montantConteste;

    @NotNull(message = "La priorité est obligatoire")
    @Pattern(regexp = "BASSE|NORMALE|HAUTE|CRITIQUE", message = "Priorité invalide")
    @JsonProperty("priorite")
    private String priorite = "NORMALE";

    @JsonProperty("demandeUrgente")
    private Boolean demandeUrgente = false;

    // ========================================
    // INFORMATIONS CLIENT (Optionnelles)
    // ========================================

    @JsonProperty("clientId")
    private Long clientId;

    @Size(max = 500, message = "Le commentaire client ne peut pas dépasser 500 caractères")
    @JsonProperty("commentaireClient")
    private String commentaireClient; // Ce que le client a dit exactement

    @JsonProperty("numeroReclamation")
    private String numeroReclamation; // Numéro de réclamation interne

    @JsonProperty("canalReclamation")
    private String canalReclamation = "GUICHET"; // GUICHET, TELEPHONE, EMAIL, MOBILE_APP

    // ========================================
    //NOTIFICATIONS
    // ========================================

    @JsonProperty("notifierClient")
    private Boolean notifierClient = true; // Notifier le client de l'initiation

    @JsonProperty("notifierAutomatiquement")
    private Boolean notifierAutomatiquement = true; // Notifier la banque acquéreuse

    @Size(max = 100, message = "La référence ne peut pas dépasser 100 caractères")
    @JsonProperty("referenceInterne")
    private String referenceInterne; // Référence dossier interne banque

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
     * Getter pour compatibilité avec le service - retourne la liste des justificatifs
     */
    public List<String> getJustificatifs() {
        return justificatifs;
    }

    // ========================================
    // MÉTHODES UTILITAIRES
    // ========================================

    /**
     * Vérifie si le chargeback est urgent
     */
    public boolean isUrgent() {
        return Boolean.TRUE.equals(demandeUrgente) ||
                "CRITIQUE".equals(priorite) ||
                (montantConteste != null && montantConteste.compareTo(new BigDecimal("10000")) > 0);
    }

    /**
     * Retourne le nombre de justificatifs
     */
    public int getNombreJustificatifs() {
        if (justificatifs != null && !justificatifs.isEmpty()) {
            return justificatifs.size();
        }
        return fichiersJustificatifs != null ? fichiersJustificatifs.size() : 0;
    }

    /**
     * Vérifie si la demande est complète
     */
    public boolean isDemandeComplete() {
        return motifChargeback != null &&
                description != null &&
                montantConteste != null &&
                getNombreJustificatifs() > 0;
    }
}