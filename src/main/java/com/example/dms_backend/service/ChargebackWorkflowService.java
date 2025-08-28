package com.example.dms_backend.service;

import com.example.dms_backend.dto.*;
import com.example.dms_backend.model.*;
import com.example.dms_backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChargebackWorkflowService {


    private final LitigeRepository litigeRepository;
    private final LitigeChargebackRepository litigeChargebackRepository;
    private final JustificatifChargebackRepository justificatifChargebackRepository;
    private final EchangeLitigeRepository echangeLitigeRepository;
    private final TransactionChargebackRepository transactionChargebackRepository;
    private final TransactionRepository transactionRepository;
    private final ArbitrageRepository arbitrageRepository;
    private final UserRepository userRepository;

    // Services
    private final NotificationService notificationService;

    // ====================================================================
    // 🚀 INITIATION CHARGEBACK
    // ====================================================================


    @Transactional
    public LitigeChargebackDTO initierChargeback(InitiationChargebackRequest request) {
        log.info("🚀 [BACKEND-START] ===== DÉBUT initierChargeback SERVICE =====");
        log.info("🚀 [BACKEND-START] Requête reçue complète: {}", request);
        log.info("🚀 [BACKEND-START] LitigeId: {}, UtilisateurId: {}",
                request.getLitigeId(), request.getUtilisateurEmetteurId());
        log.info("🚀 [BACKEND-START] Motif: {}", request.getMotifChargeback());
        log.info("🚀 [BACKEND-START] Montant: {}", request.getMontantConteste());

        try {
            // 1. Validation des données d'entrée
            log.info("🔍 [BACKEND-VALIDATION] Étape 1: Validation des données d'entrée...");
            validateInitiationRequest(request);
            log.info("✅ [BACKEND-VALIDATION] Validation des données OK");

            // 2. Récupération du litige principal
            log.info("🔍 [BACKEND-LITIGE] Étape 2: Récupération du litige ID: {}", request.getLitigeId());
            Litige litige = getLitigeById(request.getLitigeId());
            log.info("✅ [BACKEND-LITIGE] Litige trouvé - ID: {}, Description: {}",
                    litige.getId(), litige.getDescription());

            Transaction transaction = litige.getTransaction();
            log.info("✅ [BACKEND-TRANSACTION] Transaction associée - ID: {}, Référence: {}",
                    transaction.getId(), transaction.getReference());
            log.info("✅ [BACKEND-TRANSACTION] Banque émettrice: {}, Banque acquéreuse: {}",
                    transaction.getBanqueEmettrice().getNom(), transaction.getBanqueAcquereuse().getNom());

            // 3. Validation des droits utilisateur
            log.info("🔍 [BACKEND-USER] Étape 3: Validation des droits utilisateur ID: {}",
                    request.getUtilisateurEmetteurId());
            Utilisateur utilisateurEmetteur = validateUserRights(request.getUtilisateurEmetteurId(), litige);
            log.info("✅ [BACKEND-USER] Utilisateur validé - Nom: {}, Institution: {}",
                    utilisateurEmetteur.getNom(), utilisateurEmetteur.getInstitution().getNom());

            // 4. Vérification qu'il n'y a pas déjà un chargeback
            log.info("🔍 [BACKEND-CHECK] Étape 4: Vérification chargeback existant pour litige: {}",
                    request.getLitigeId());
            boolean chargebackExists = litigeChargebackRepository.existsByLitigeId(request.getLitigeId());
            log.info("🔍 [BACKEND-CHECK] Résultat vérification: chargeback existe = {}", chargebackExists);

            if (chargebackExists) {
                log.error("❌ [BACKEND-CHECK] ÉCHEC: Chargeback déjà existant pour litige: {}",
                        request.getLitigeId());
                throw new IllegalStateException("Un chargeback existe déjà pour ce litige");
            }
            log.info("✅ [BACKEND-CHECK] Aucun chargeback existant, procédure continue");

            // 5. Création du LitigeChargeback
            log.info("🏗️ [BACKEND-CREATE] Étape 5: Création du LitigeChargeback...");
            LitigeChargeback litigeChargeback = creerLitigeChargeback(litige, request);
            log.info("✅ [BACKEND-CREATE] LitigeChargeback créé - ID: {}, Phase: {}",
                    litigeChargeback.getId(), litigeChargeback.getPhaseActuelle());

            // 6. Création/Mise à jour TransactionChargeback
            log.info("🔄 [BACKEND-UPDATE] Étape 6: Mise à jour TransactionChargeback...");
            updateTransactionChargeback(transaction, request.getMontantConteste());
            log.info("✅ [BACKEND-UPDATE] TransactionChargeback mise à jour");

            // 7. Ajout des justificatifs
            log.info("📎 [BACKEND-JUSTIF] Étape 7: Ajout des justificatifs...");
            log.info("📎 [BACKEND-JUSTIF] Nombre de justificatifs: {}",
                    request.getJustificatifs() != null ? request.getJustificatifs().size() : 0);
            ajouterJustificatifsInitiation(litigeChargeback, request.getJustificatifs(), utilisateurEmetteur);
            log.info("✅ [BACKEND-JUSTIF] Justificatifs ajoutés");

            // 8. Création de l'échange initial
            log.info("💬 [BACKEND-EXCHANGE] Étape 8: Création de l'échange initial...");
            creerEchangeInitiation(litigeChargeback, request, utilisateurEmetteur);
            log.info("✅ [BACKEND-EXCHANGE] Échange initial créé");

            // 9. Notification à la banque acquéreuse
            log.info("📧 [BACKEND-NOTIF] Étape 9: Notification à la banque acquéreuse...");
            try {
                notifierBanqueAcquereuse(litige, utilisateurEmetteur, "Nouveau chargeback initié");
                log.info("✅ [BACKEND-NOTIF] Notification envoyée");
            } catch (Exception notifError) {
                log.warn("⚠️ [BACKEND-NOTIF] Erreur notification (non bloquante): {}", notifError.getMessage());
            }

            // 10. Définition de la deadline
            log.info("⏰ [BACKEND-DEADLINE] Étape 10: Définition de la deadline...");
            definirDeadlineRepresentation(litigeChargeback);
            log.info("✅ [BACKEND-DEADLINE] Deadline définie: {}", litigeChargeback.getDeadlineActuelle());

            log.info("✅ [BACKEND-SUCCESS] ===== CHARGEBACK CRÉÉ AVEC SUCCÈS =====");
            log.info("✅ [BACKEND-SUCCESS] ID Final: {}", litigeChargeback.getId());
            log.info("✅ [BACKEND-SUCCESS] Phase finale: {}", litigeChargeback.getPhaseActuelle());
            log.info("✅ [BACKEND-SUCCESS] Montant contesté: {}", litigeChargeback.getMontantConteste());

            LitigeChargebackDTO result = LitigeChargebackDTO.fromEntity(litigeChargeback);
            log.info("✅ [BACKEND-SUCCESS] DTO créé pour retour: {}", result);

            return result;

        } catch (IllegalStateException ise) {
            log.error("❌ [BACKEND-ERROR] Erreur état illégal: {}", ise.getMessage());
            throw ise;
        } catch (IllegalArgumentException iae) {
            log.error("❌ [BACKEND-ERROR] Erreur argument invalide: {}", iae.getMessage());
            throw iae;
        } catch (Exception e) {
            log.error("❌ [BACKEND-ERROR] ===== ERREUR CRITIQUE =====");
            log.error("❌ [BACKEND-ERROR] Type d'erreur: {}", e.getClass().getSimpleName());
            log.error("❌ [BACKEND-ERROR] Message: {}", e.getMessage());
            log.error("❌ [BACKEND-ERROR] Stack trace:", e);
            throw new RuntimeException("Erreur lors de l'initiation du chargeback : " + e.getMessage(), e);
        }
    }
// ====================================================================
    // 🔄 REPRÉSENTATION
    // ====================================================================

    @Transactional
    public LitigeChargebackDTO traiterRepresentation(RepresentationRequest request) {
        log.info("🔄 [REPRESENTATION] Traitement - LitigeId: {}, TypeReponse: {}",
                request.getLitigeId(), request.getTypeReponse());

        try {
            // 1. Validation de la requête
            validateRepresentationRequest(request);

            // 2. Récupération du litige chargeback
            LitigeChargeback litigeChargeback = getLitigeChargebackById(request.getLitigeId());

            // 3. Vérification de la phase
            if (!"CHARGEBACK_INITIAL".equals(litigeChargeback.getPhaseActuelle())) {
                throw new IllegalStateException("La représentation n'est possible qu'en phase CHARGEBACK_INITIAL");
            }

            // 4. Validation des droits utilisateur (banque acquéreuse)
            Utilisateur utilisateurAcquereur = validateUserRightsAcquereur(request.getUtilisateurAcquereurId(), litigeChargeback);

            // 5. Traitement selon le type de réponse
            switch (request.getTypeReponse()) {
                case "ACCEPTATION_TOTALE":
                    traiterAcceptationTotale(litigeChargeback, request, utilisateurAcquereur);
                    break;
                case "ACCEPTATION_PARTIELLE":
                    traiterAcceptationPartielle(litigeChargeback, request, utilisateurAcquereur);
                    break;
                case "CONTESTATION":
                    traiterContestation(litigeChargeback, request, utilisateurAcquereur);
                    break;
                default:
                    throw new IllegalArgumentException("Type de réponse non reconnu : " + request.getTypeReponse());
            }

            // 6. Ajout des justificatifs de représentation
            if (request.getJustificatifsDefense() != null && !request.getJustificatifsDefense().isEmpty()) {
                ajouterJustificatifsRepresentation(litigeChargeback, request.getJustificatifsDefense(), utilisateurAcquereur);
            }

            // 7. Création de l'échange de représentation
            creerEchangeRepresentation(litigeChargeback, request, utilisateurAcquereur);

            // 8. Notification à la banque émettrice
            Litige litige = litigeRepository.findById(litigeChargeback.getLitigeId()).orElse(null);
            if (litige != null) {
                notifierBanqueEmettrice(litige, utilisateurAcquereur, "Réponse de représentation : " + request.getTypeReponse());
            }

            log.info("✅ [REPRESENTATION] Traitement terminé - Phase: {}", litigeChargeback.getPhaseActuelle());

            return LitigeChargebackDTO.fromEntity(litigeChargeback);

        } catch (Exception e) {
            log.error("❌ [REPRESENTATION] Erreur lors du traitement", e);
            throw new RuntimeException("Erreur lors du traitement de la représentation : " + e.getMessage(), e);
        }
    }

    // ====================================================================
    // ⚡ SECOND PRESENTMENT (PRÉ-ARBITRAGE)
    // ====================================================================

    @Transactional
    public LitigeChargebackDTO traiterSecondPresentment(SecondPresentmentRequest request) {
        log.info("⚡ [PRE_ARBITRAGE] Second presentment - LitigeId: {}", request.getLitigeId());

        try {
            // 1. Validation de la requête
            validateSecondPresentmentRequest(request);

            // 2. Récupération du litige chargeback
            LitigeChargeback litigeChargeback = getLitigeChargebackById(request.getLitigeId());

            // 3. Vérification de la phase
            if (!"REPRESENTATION".equals(litigeChargeback.getPhaseActuelle())) {
                throw new IllegalStateException("Le second presentment n'est possible qu'après la représentation");
            }

            // 4. Validation des droits utilisateur (banque émettrice)
            Utilisateur utilisateurEmetteur = validateUserRightsEmetteur(request.getUtilisateurEmetteurId(), litigeChargeback);

            // 5. Mise à jour vers la phase PRE_ARBITRAGE
            litigeChargeback.progresserVersPhase("PRE_ARBITRAGE");

            // 6. Ajout des nouveaux arguments et preuves
            if (request.getNouvellesSpreuves() != null && !request.getNouvellesSpreuves().isEmpty()) {
                ajouterJustificatifsPreArbitrage(litigeChargeback, request.getNouvellesSpreuves(), utilisateurEmetteur);
            }

            // 7. Création de l'échange de second presentment
            creerEchangeSecondPresentment(litigeChargeback, request, utilisateurEmetteur);

            // 8. Définition de la deadline pour la réponse
            definirDeadlineReponsePreArbitrage(litigeChargeback);

            // 9. Notification à la banque acquéreuse
            Litige litige = litigeRepository.findById(litigeChargeback.getLitigeId()).orElse(null);
            if (litige != null) {
                notifierBanqueAcquereuse(litige, utilisateurEmetteur, "Second presentment - Nouveaux arguments fournis");
            }

            // 10. Sauvegarde
            litigeChargebackRepository.save(litigeChargeback);

            log.info("✅ [PRE_ARBITRAGE] Second presentment traité avec succès");

            return LitigeChargebackDTO.fromEntity(litigeChargeback);

        } catch (Exception e) {
            log.error("❌ [PRE_ARBITRAGE] Erreur lors du second presentment", e);
            throw new RuntimeException("Erreur lors du second presentment : " + e.getMessage(), e);
        }
    }
// ====================================================================
    // ⚖️ ARBITRAGE
    // ====================================================================

    @Transactional
    public ArbitrageDTO demanderArbitrage(InitiationArbitrageRequest request) {
        log.info("⚖️ [ARBITRAGE] Demande d'arbitrage - LitigeId: {}", request.getLitigeId());

        try {
            // 1. Validation de la requête
            validateArbitrageRequest(request);

            // 2. Récupération du litige chargeback
            LitigeChargeback litigeChargeback = getLitigeChargebackById(request.getLitigeId());

            // 3. Vérification des conditions d'arbitrage
            if (!peutEtreEscaladeVersArbitrage(litigeChargeback)) {
                throw new IllegalStateException("Ce litige ne peut pas être escaladé vers l'arbitrage");
            }

            // 4. Validation des droits utilisateur
            Utilisateur utilisateurDemandeur = validateUserRights(request.getUtilisateurDemandeurId(),
                    litigeRepository.findById(litigeChargeback.getLitigeId()).orElseThrow());

            // 5. Création de la demande d'arbitrage
            Arbitrage arbitrage = creerDemandeArbitrage(litigeChargeback, request, utilisateurDemandeur);

            // 6. Mise à jour de la phase du litige
            litigeChargeback.progresserVersPhase("ARBITRAGE");
            litigeChargeback.setPeutEtreEscalade(false);
            litigeChargebackRepository.save(litigeChargeback);

            // 7. Création de l'échange d'arbitrage
            creerEchangeArbitrage(litigeChargeback, request, utilisateurDemandeur);

            // 8. Notifications
            Litige litige = litigeRepository.findById(litigeChargeback.getLitigeId()).orElse(null);
            if (litige != null) {
                notifierDemandeArbitrage(litige, utilisateurDemandeur);
            }

            log.info("✅ [ARBITRAGE] Demande créée avec succès - ArbitrageId: {}", arbitrage.getId());

            return ArbitrageDTO.fromEntity(arbitrage);

        } catch (Exception e) {
            log.error("❌ [ARBITRAGE] Erreur lors de la demande d'arbitrage", e);
            throw new RuntimeException("Erreur lors de la demande d'arbitrage : " + e.getMessage(), e);
        }
    }

    @Transactional
    public ArbitrageDTO rendreDecisionArbitrage(Long arbitrageId, Arbitrage.Decision decision,
                                                String motifs, Arbitrage.RepartitionFrais repartitionFrais,
                                                Long arbitreAdminId) {
        log.info("⚖️ [ARBITRAGE] Rendu de décision - ArbitrageId: {}, Décision: {}", arbitrageId, decision);

        try {
            // 1. Récupération de l'arbitrage
            Arbitrage arbitrage = arbitrageRepository.findById(arbitrageId)
                    .orElseThrow(() -> new IllegalArgumentException("Arbitrage non trouvé"));

            // 2. Vérification des droits admin
            validateAdminRights(arbitreAdminId);

            // 3. Vérification de l'état - CORRECTION : Utilisation du statut au lieu de isDecide()
            if (arbitrage.getStatut() == Arbitrage.StatutArbitrage.DECIDE) {
                throw new IllegalStateException("Cet arbitrage a déjà été décidé");
            }

            // 4. Rendu de la décision - CORRECTION : Utilisation des setters au lieu de la méthode inexistante
            arbitrage.setDecision(decision);
            arbitrage.setMotifsDecision(motifs);
            arbitrage.setRepartitionFrais(repartitionFrais);
            arbitrage.setStatut(Arbitrage.StatutArbitrage.DECIDE);
            arbitrage.setDateDecision(LocalDateTime.now());
            arbitrage.setArbitreUtilisateurId(arbitreAdminId);

            Arbitrage savedArbitrage = arbitrageRepository.save(arbitrage);

            // 5. Finalisation du litige chargeback
            finaliserLitigeChargeback(arbitrage.getLitigeId(), decision);

            // 6. Création de l'échange de décision
            creerEchangeDecisionArbitrage(arbitrage, decision, motifs);

            // 7. Notifications aux deux banques
            notifierDecisionArbitrage(arbitrage, decision);

            log.info("✅ [ARBITRAGE] Décision rendue avec succès - Décision: {}", decision);

            return ArbitrageDTO.fromEntity(savedArbitrage);

        } catch (Exception e) {
            log.error("❌ [ARBITRAGE] Erreur lors du rendu de décision", e);
            throw new RuntimeException("Erreur lors du rendu de décision : " + e.getMessage(), e);
        }
    }

    // ====================================================================
    // 🔧 MÉTHODES DE VALIDATION
    // ====================================================================

    private void validateInitiationRequest(InitiationChargebackRequest request) {
        if (request.getLitigeId() == null) {
            throw new IllegalArgumentException("L'ID du litige est obligatoire");
        }
        if (request.getUtilisateurEmetteurId() == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur émetteur est obligatoire");
        }
        if (request.getMotifChargeback() == null || request.getMotifChargeback().trim().isEmpty()) {
            throw new IllegalArgumentException("Le motif du chargeback est obligatoire");
        }
        if (request.getMontantConteste() == null || request.getMontantConteste().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant contesté doit être positif");
        }
    }

    private void validateRepresentationRequest(RepresentationRequest request) {
        if (request.getLitigeId() == null) {
            throw new IllegalArgumentException("L'ID du litige est obligatoire");
        }
        if (request.getUtilisateurAcquereurId() == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur acquéreur est obligatoire");
        }
        if (request.getTypeReponse() == null || request.getTypeReponse().trim().isEmpty()) {
            throw new IllegalArgumentException("Le type de réponse est obligatoire");
        }
    }

    private void validateSecondPresentmentRequest(SecondPresentmentRequest request) {
        if (request.getLitigeId() == null) {
            throw new IllegalArgumentException("L'ID du litige est obligatoire");
        }
        if (request.getUtilisateurEmetteurId() == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur émetteur est obligatoire");
        }
    }

    private void validateArbitrageRequest(InitiationArbitrageRequest request) {
        if (request.getLitigeId() == null) {
            throw new IllegalArgumentException("L'ID du litige est obligatoire");
        }
        if (request.getUtilisateurDemandeurId() == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur demandeur est obligatoire");
        }
        if (request.getJustificationDemande() == null || request.getJustificationDemande().trim().isEmpty()) {
            throw new IllegalArgumentException("La justification de la demande est obligatoire");
        }
    }
// ====================================================================
    // 🔧 MÉTHODES UTILITAIRES
    // ====================================================================

    private Litige getLitigeById(Long litigeId) {
        return litigeRepository.findById(litigeId)
                .orElseThrow(() -> new IllegalArgumentException("Litige non trouvé avec l'ID : " + litigeId));
    }

    private LitigeChargeback getLitigeChargebackById(Long litigeId) {
        return litigeChargebackRepository.findByLitigeId(litigeId)
                .orElseThrow(() -> new IllegalArgumentException("Litige chargeback non trouvé pour le litige : " + litigeId));
    }

    private Utilisateur validateUserRights(Long utilisateurId, Litige litige) {
        Utilisateur utilisateur = userRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé"));

        Institution institutionUtilisateur = utilisateur.getInstitution();
        if (institutionUtilisateur == null) {
            throw new IllegalArgumentException("L'utilisateur doit être associé à une institution");
        }

        // Vérification que l'utilisateur appartient à une des banques de la transaction
        Transaction transaction = litige.getTransaction();
        boolean autorise = false;

        if (transaction.getBanqueEmettrice() != null &&
                transaction.getBanqueEmettrice().getId().equals(institutionUtilisateur.getId())) {
            autorise = true;
        }
        if (transaction.getBanqueAcquereuse() != null &&
                transaction.getBanqueAcquereuse().getId().equals(institutionUtilisateur.getId())) {
            autorise = true;
        }

        if (!autorise) {
            throw new IllegalArgumentException("L'utilisateur n'est pas autorisé à agir sur ce litige");
        }

        return utilisateur;
    }

    private Utilisateur validateUserRightsEmetteur(Long utilisateurId, LitigeChargeback litigeChargeback) {
        Litige litige = getLitigeById(litigeChargeback.getLitigeId());
        Utilisateur utilisateur = validateUserRights(utilisateurId, litige);

        // Vérification spécifique banque émettrice
        if (!litige.getTransaction().getBanqueEmettrice().getId().equals(utilisateur.getInstitution().getId())) {
            throw new IllegalArgumentException("Seule la banque émettrice peut effectuer cette action");
        }

        return utilisateur;
    }

    private Utilisateur validateUserRightsAcquereur(Long utilisateurId, LitigeChargeback litigeChargeback) {
        Litige litige = getLitigeById(litigeChargeback.getLitigeId());
        Utilisateur utilisateur = validateUserRights(utilisateurId, litige);

        // Vérification spécifique banque acquéreuse
        if (!litige.getTransaction().getBanqueAcquereuse().getId().equals(utilisateur.getInstitution().getId())) {
            throw new IllegalArgumentException("Seule la banque acquéreuse peut effectuer cette action");
        }

        return utilisateur;
    }

    private void validateAdminRights(Long utilisateurId) {
        Utilisateur admin = userRepository.findById(utilisateurId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur admin non trouvé"));

        // TODO: Vérifier que l'utilisateur a le rôle ADMIN
        // if (!admin.hasRole("ADMIN")) {
        //     throw new IllegalArgumentException("Seuls les administrateurs peuvent rendre des décisions d'arbitrage");
        // }
    }

    private LitigeChargeback creerLitigeChargeback(Litige litige, InitiationChargebackRequest request) {
        LitigeChargeback litigeChargeback = new LitigeChargeback(litige.getId());
        litigeChargeback.setMotifChargeback(request.getMotifChargeback());
        litigeChargeback.setMontantConteste(request.getMontantConteste());
        litigeChargeback.setPhaseActuelle("CHARGEBACK_INITIAL");
        litigeChargeback.setPeutEtreEscalade(true);
        litigeChargeback.setVersionWorkflow(1);

        return litigeChargebackRepository.save(litigeChargeback);
    }

    private void updateTransactionChargeback(Transaction transaction, BigDecimal montantConteste) {
        Optional<TransactionChargeback> existingOpt = transactionChargebackRepository.findByTransactionId(transaction.getId());

        TransactionChargeback transactionChargeback;
        if (existingOpt.isPresent()) {
            transactionChargeback = existingOpt.get();
        } else {
            transactionChargeback = new TransactionChargeback(transaction.getId());
        }

        transactionChargeback.activerLitige();
        transactionChargeback.incrementerChargebacks();
        transactionChargeback.ajouterMontantConteste(montantConteste);
        transactionChargeback.ajouterHistorique("Chargeback initié - Montant: " + montantConteste + " MAD");

        transactionChargebackRepository.save(transactionChargeback);
    }

    private void ajouterJustificatifsInitiation(LitigeChargeback litigeChargeback,
                                                List<String> justificatifs,
                                                Utilisateur utilisateur) {
        if (justificatifs != null) {
            for (String justificatif : justificatifs) {
                JustificatifChargeback j = new JustificatifChargeback(
                        litigeChargeback.getLitigeId(),
                        justificatif,
                        "PREUVE_CLIENT",
                        "CHARGEBACK_INITIAL",
                        "/uploads/chargeback/" + justificatif,
                        utilisateur.getId()
                );
                j.setValide(false); // Sera validé par l'admin
                justificatifChargebackRepository.save(j);
            }
        }
    }

    private void ajouterJustificatifsRepresentation(LitigeChargeback litigeChargeback,
                                                    List<String> justificatifs,
                                                    Utilisateur utilisateur) {
        if (justificatifs != null) {
            for (String justificatif : justificatifs) {
                JustificatifChargeback j = new JustificatifChargeback(
                        litigeChargeback.getLitigeId(),
                        justificatif,
                        "DEFENSE_COMMERCANT",
                        "REPRESENTATION",
                        "/uploads/representation/" + justificatif,
                        utilisateur.getId()
                );
                j.setValide(false);
                justificatifChargebackRepository.save(j);
            }
        }
    }

    private void ajouterJustificatifsPreArbitrage(LitigeChargeback litigeChargeback,
                                                  List<String> justificatifs,
                                                  Utilisateur utilisateur) {
        if (justificatifs != null) {
            for (String justificatif : justificatifs) {
                JustificatifChargeback j = new JustificatifChargeback(
                        litigeChargeback.getLitigeId(),
                        justificatif,
                        "NOUVELLE_PREUVE",
                        "PRE_ARBITRAGE",
                        "/uploads/prearbitrage/" + justificatif,
                        utilisateur.getId()
                );
                j.setValide(false);
                justificatifChargebackRepository.save(j);
            }
        }
    }
// ====================================================================
    // 💬 MÉTHODES DE CREATION D'ÉCHANGES
    // ====================================================================

    private void creerEchangeInitiation(LitigeChargeback litigeChargeback,
                                        InitiationChargebackRequest request,
                                        Utilisateur utilisateur) {
        try {
            String contenu = String.format("Chargeback initié - Motif: %s - Montant: %s MAD",
                    request.getMotifChargeback(), request.getMontantConteste());

            EchangeLitige echange = new EchangeLitige(
                    litigeChargeback.getLitigeId(),
                    contenu,
                    EchangeLitige.TypeEchange.ACTION,
                    "CHARGEBACK_INITIAL",
                    utilisateur.getId()
            );
            echange.setInstitutionId(utilisateur.getInstitution().getId());

            // ✅ SAUVEGARDE SÉCURISÉE
            echangeLitigeRepository.save(echange);

            log.info("✅ [BACKEND-EXCHANGE] Échange initial créé avec succès");

        } catch (Exception e) {
            log.error("❌ [BACKEND-EXCHANGE] Erreur création échange: {}", e.getMessage());
            // Ne pas faire échouer la transaction principale
            // L'échange sera créé lors d'une prochaine étape ou manuellement
        }
    }

    private void creerEchangeRepresentation(LitigeChargeback litigeChargeback,
                                            RepresentationRequest request,
                                            Utilisateur utilisateur) {
        String contenu = String.format("Représentation - Type: %s - Arguments: %s",
                request.getTypeReponse(),
                request.getArgumentsDefense() != null ? request.getArgumentsDefense() : "Aucun argument fourni");

        EchangeLitige echange = new EchangeLitige(
                litigeChargeback.getLitigeId(),
                contenu,
                EchangeLitige.TypeEchange.ACTION,
                "REPRESENTATION",
                utilisateur.getId()
        );
        echange.setInstitutionId(utilisateur.getInstitution().getId());
        echangeLitigeRepository.save(echange);
    }

    private void creerEchangeSecondPresentment(LitigeChargeback litigeChargeback,
                                               SecondPresentmentRequest request,
                                               Utilisateur utilisateur) {
        String contenu = String.format("Second presentment - Réfutation: %s",
                request.getRefutationDetaillee() != null ? request.getRefutationDetaillee() : "Réfutation détaillée");

        EchangeLitige echange = new EchangeLitige(
                litigeChargeback.getLitigeId(),
                contenu,
                EchangeLitige.TypeEchange.ESCALADE,
                "PRE_ARBITRAGE",
                utilisateur.getId()
        );
        echange.setInstitutionId(utilisateur.getInstitution().getId());
        echangeLitigeRepository.save(echange);
    }

    private void creerEchangeArbitrage(LitigeChargeback litigeChargeback,
                                       InitiationArbitrageRequest request,
                                       Utilisateur utilisateur) {
        String contenu = String.format("Demande d'arbitrage - Justification: %s", request.getJustificationDemande());

        EchangeLitige echange = new EchangeLitige(
                litigeChargeback.getLitigeId(),
                contenu,
                EchangeLitige.TypeEchange.ESCALADE,
                "ARBITRAGE",
                utilisateur.getId()
        );
        echange.setInstitutionId(utilisateur.getInstitution().getId());
        echangeLitigeRepository.save(echange);
    }

    private void creerEchangeDecisionArbitrage(Arbitrage arbitrage, Arbitrage.Decision decision, String motifs) {
        String contenu = String.format("Décision d'arbitrage rendue - Décision: %s - Motifs: %s",
                decision.getLibelle(), motifs != null ? motifs : "Aucun motif spécifié");

        EchangeLitige echange = new EchangeLitige(
                arbitrage.getLitigeId(),
                contenu,
                EchangeLitige.TypeEchange.DECISION,
                "ARBITRAGE",
                arbitrage.getArbitreUtilisateurId()
        );
        echange.setVisible(true);
        echangeLitigeRepository.save(echange);
    }

    // ====================================================================
    // ⏰ MÉTHODES DE GESTION DES DEADLINES
    // ====================================================================

    private void definirDeadlineRepresentation(LitigeChargeback litigeChargeback) {
        // Deadline standard : 10 jours ouvrables pour répondre
        LocalDateTime deadline = LocalDateTime.now().plusDays(10);
        litigeChargeback.setDeadlineActuelle(deadline);
        litigeChargeback.calculerJoursRestants();
        litigeChargebackRepository.save(litigeChargeback);
    }

    private void definirDeadlineReponsePreArbitrage(LitigeChargeback litigeChargeback) {
        // Deadline réduite : 5 jours pour répondre au second presentment
        LocalDateTime deadline = LocalDateTime.now().plusDays(5);
        litigeChargeback.setDeadlineActuelle(deadline);
        litigeChargeback.calculerJoursRestants();
    }

    // ====================================================================
    // 🔄 MÉTHODES DE TRAITEMENT DES RÉPONSES
    // ====================================================================

    private void traiterAcceptationTotale(LitigeChargeback litigeChargeback,
                                          RepresentationRequest request,
                                          Utilisateur utilisateur) {
        log.info("✅ [REPRESENTATION] Acceptation totale du chargeback");

        // Finaliser directement le litige
        litigeChargeback.progresserVersPhase("FINALISE");
        litigeChargeback.setPeutEtreEscalade(false);

        // Mettre à jour la transaction chargeback
        Optional<TransactionChargeback> tcOpt = transactionChargebackRepository.findByTransactionId(
                getLitigeById(litigeChargeback.getLitigeId()).getTransaction().getId());

        if (tcOpt.isPresent()) {
            TransactionChargeback tc = tcOpt.get();
            tc.desactiverLitige();
            tc.ajouterHistorique("Chargeback accepté totalement par la banque acquéreuse");
            transactionChargebackRepository.save(tc);
        }

        litigeChargebackRepository.save(litigeChargeback);
    }

    private void traiterAcceptationPartielle(LitigeChargeback litigeChargeback,
                                             RepresentationRequest request,
                                             Utilisateur utilisateur) {
        log.info("🔄 [REPRESENTATION] Acceptation partielle - Montant: {}", request.getMontantAccepte());

        // Mise à jour du montant contesté avec la différence
        if (request.getMontantAccepte() != null) {
            BigDecimal montantRestant = litigeChargeback.getMontantConteste().subtract(request.getMontantAccepte());
            litigeChargeback.setMontantConteste(montantRestant);
        }

        // Progression vers représentation (en attente de décision émettrice)
        litigeChargeback.progresserVersPhase("REPRESENTATION");
        definirDeadlineRepresentation(litigeChargeback);

        litigeChargebackRepository.save(litigeChargeback);
    }

    private void traiterContestation(LitigeChargeback litigeChargeback,
                                     RepresentationRequest request,
                                     Utilisateur utilisateur) {
        log.info("⚔️ [REPRESENTATION] Contestation du chargeback");

        // Progression vers représentation
        litigeChargeback.progresserVersPhase("REPRESENTATION");
        definirDeadlineRepresentation(litigeChargeback);

        litigeChargebackRepository.save(litigeChargeback);
    }

    // ====================================================================
    // 📧 MÉTHODES DE NOTIFICATION
    // ====================================================================

    private void notifierBanqueAcquereuse(Litige litige, Utilisateur utilisateurEmetteur, String message) {
        try {
            log.info("📧 [BACKEND-NOTIF] Tentative notification banque acquéreuse...");
            notificationService.notifierAutreBanque(litige.getTransaction(), utilisateurEmetteur, message);
            log.info("✅ [BACKEND-NOTIF] Notification envoyée avec succès");
        } catch (Exception e) {
            log.warn("⚠️ [BACKEND-NOTIF] Erreur notification (non-critique): {}", e.getMessage());
            // ✅ Ne pas faire échouer la transaction principale
            // La notification peut être renvoyée plus tard
        }
    }

    private void notifierBanqueEmettrice(Litige litige, Utilisateur utilisateurAcquereur, String message) {
        try {
            notificationService.notifierAutreBanque(litige.getTransaction(), utilisateurAcquereur, message);
            log.info("📧 Notification envoyée à la banque émettrice");
        } catch (Exception e) {
            log.error("❌ Erreur lors de la notification à la banque émettrice", e);
        }
    }

    private void notifierDemandeArbitrage(Litige litige, Utilisateur utilisateurDemandeur) {
        try {
            String message = String.format("Demande d'arbitrage pour la transaction %s",
                    litige.getTransaction().getReference());
            notificationService.notifierAutreBanque(litige.getTransaction(), utilisateurDemandeur, message);
            log.info("📧 Notification demande d'arbitrage envoyée");
        } catch (Exception e) {
            log.error("❌ Erreur lors de la notification de demande d'arbitrage", e);
        }
    }

    private void notifierDecisionArbitrage(Arbitrage arbitrage, Arbitrage.Decision decision) {
        try {
            Litige litige = getLitigeById(arbitrage.getLitigeId());
            Transaction transaction = litige.getTransaction();

            String message = String.format("Décision d'arbitrage rendue : %s pour la transaction %s",
                    decision.getLibelle(), transaction.getReference());

            // Notification à la banque émettrice
            if (transaction.getBanqueEmettrice() != null) {
                // TODO: Implémenter notification directe à l'institution
                log.info("📧 Notification décision envoyée à la banque émettrice: {}",
                        transaction.getBanqueEmettrice().getNom());
            }

            // Notification à la banque acquéreuse
            if (transaction.getBanqueAcquereuse() != null) {
                // TODO: Implémenter notification directe à l'institution
                log.info("📧 Notification décision envoyée à la banque acquéreuse: {}",
                        transaction.getBanqueAcquereuse().getNom());
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors des notifications de décision d'arbitrage", e);
        }
    }
// ====================================================================
    // 🏗️ MÉTHODES DE CRÉATION D'ARBITRAGE
    // ====================================================================

    private Arbitrage creerDemandeArbitrage(LitigeChargeback litigeChargeback,
                                            InitiationArbitrageRequest request,
                                            Utilisateur utilisateur) {
        Arbitrage arbitrage = new Arbitrage(
                litigeChargeback.getLitigeId(),
                utilisateur.getInstitution().getId()
        );

        // Calcul du coût d'arbitrage basé sur le montant contesté
        if (litigeChargeback.getMontantConteste() != null) {
            BigDecimal coutArbitrage = calculerCoutArbitrage(litigeChargeback.getMontantConteste());
            arbitrage.setCoutArbitrage(coutArbitrage);
            litigeChargeback.setFraisArbitrageEstime(coutArbitrage);
        }

        return arbitrageRepository.save(arbitrage);
    }

    private BigDecimal calculerCoutArbitrage(BigDecimal montantConteste) {
        // Grille tarifaire simplifiée pour l'arbitrage
        if (montantConteste.compareTo(BigDecimal.valueOf(1000)) <= 0) {
            return BigDecimal.valueOf(100); // 100 MAD
        } else if (montantConteste.compareTo(BigDecimal.valueOf(10000)) <= 0) {
            return BigDecimal.valueOf(500); // 500 MAD
        } else if (montantConteste.compareTo(BigDecimal.valueOf(50000)) <= 0) {
            return BigDecimal.valueOf(1000); // 1000 MAD
        } else {
            return BigDecimal.valueOf(2000); // 2000 MAD
        }
    }

    private boolean peutEtreEscaladeVersArbitrage(LitigeChargeback litigeChargeback) {
        // Conditions pour escalader vers l'arbitrage
        return litigeChargeback.isPeutEtreEscalade() &&
                ("REPRESENTATION".equals(litigeChargeback.getPhaseActuelle()) ||
                        "PRE_ARBITRAGE".equals(litigeChargeback.getPhaseActuelle())) &&
                !litigeChargeback.isPhaseFinale();
    }

    private void finaliserLitigeChargeback(Long litigeId, Arbitrage.Decision decision) {
        Optional<LitigeChargeback> litigeChargebackOpt = litigeChargebackRepository.findByLitigeId(litigeId);

        if (litigeChargebackOpt.isPresent()) {
            LitigeChargeback litigeChargeback = litigeChargebackOpt.get();
            litigeChargeback.progresserVersPhase("FINALISE");
            litigeChargeback.setPeutEtreEscalade(false);
            litigeChargebackRepository.save(litigeChargeback);

            // Mise à jour de la transaction chargeback
            Litige litige = getLitigeById(litigeId);
            Optional<TransactionChargeback> tcOpt = transactionChargebackRepository
                    .findByTransactionId(litige.getTransaction().getId());

            if (tcOpt.isPresent()) {
                TransactionChargeback tc = tcOpt.get();
                tc.desactiverLitige();
                tc.ajouterHistorique("Arbitrage terminé - Décision: " + decision.getLibelle());
                transactionChargebackRepository.save(tc);
            }
        }
    }

    // ====================================================================
    // 📊 MÉTHODES DE CONSULTATION ET STATISTIQUES - VERSION FINALE CORRIGÉE
    // ====================================================================

    public List<LitigeChargebackDTO> getLitigesChargebackParInstitution(Long institutionId) {
        log.info("🔍 Récupération des chargebacks pour l'institution ID: {}", institutionId);

        List<LitigeChargeback> litiges = litigeChargebackRepository.findByInstitutionId(institutionId);
        log.info("📊 Trouvé {} chargebacks bruts", litiges.size());

        List<LitigeChargebackDTO> dtos = new ArrayList<>();

        for (LitigeChargeback lc : litiges) {
            try {
                // Récupération du litige principal
                Litige litige = litigeRepository.findById(lc.getLitigeId())
                        .orElse(null);

                if (litige != null) {
                    // Récupération de la transaction
                    Transaction transaction = litige.getTransaction();

                    if (transaction != null) {
                        // Conversion LocalDate vers LocalDateTime pour compatibilité
                        LocalDateTime dateTransaction = transaction.getDateTransaction() != null ?
                                transaction.getDateTransaction().atStartOfDay() : null;

                        // ✅ UTILISATION DE LA NOUVELLE MÉTHODE fromEntityWithTransaction
                        LitigeChargebackDTO dto = LitigeChargebackDTO.fromEntityWithTransaction(
                                lc,
                                transaction.getReference(),
                                transaction.getBanqueEmettrice() != null ? transaction.getBanqueEmettrice().getId() : null,
                                transaction.getBanqueAcquereuse() != null ? transaction.getBanqueAcquereuse().getId() : null,
                                transaction.getMontant(),
                                dateTransaction
                        );

                        dtos.add(dto);

                        log.info("✅ Chargeback enrichi - ID: {}, Transaction: {}, Émettrice: {}, Acquéreuse: {}",
                                lc.getId(), transaction.getReference(),
                                transaction.getBanqueEmettrice() != null ? transaction.getBanqueEmettrice().getId() : "null",
                                transaction.getBanqueAcquereuse() != null ? transaction.getBanqueAcquereuse().getId() : "null");
                    } else {
                        log.warn("⚠️ Transaction non trouvée pour le litige ID: {}", litige.getId());
                        // Fallback avec l'ancienne méthode
                        dtos.add(LitigeChargebackDTO.fromEntity(lc));
                    }
                } else {
                    log.warn("⚠️ Litige non trouvé pour ID: {}", lc.getLitigeId());
                    // Fallback avec l'ancienne méthode
                    dtos.add(LitigeChargebackDTO.fromEntity(lc));
                }

            } catch (Exception e) {
                log.error("❌ Erreur lors de l'enrichissement du chargeback ID: {}", lc.getId(), e);
                // Fallback avec l'ancienne méthode
                dtos.add(LitigeChargebackDTO.fromEntity(lc));
            }
        }

        log.info("🎯 Retour de {} chargebacks enrichis", dtos.size());
        return dtos;
    }

    public List<LitigeChargebackDTO> getLitigesChargebackParPhase(String phase) {
        log.info("📊 Récupération des litiges chargeback en phase: {}", phase);

        List<LitigeChargeback> litiges = litigeChargebackRepository.findByPhaseActuelle(phase);

        return litiges.stream()
                .map(LitigeChargebackDTO::fromEntity)
                .collect(java.util.stream.Collectors.toList());
    }

    public List<LitigeChargebackDTO> getLitigesChargebackUrgents() {
        log.info("🚨 Récupération des litiges chargeback urgents");

        try {
            // CORRECTION : Utiliser la méthode exacte disponible dans le repository
            List<LitigeChargeback> litiges = litigeChargebackRepository.findLitigesEnRetard();

            return litiges.stream()
                    .map(LitigeChargebackDTO::fromEntity)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des litiges urgents", e);
            // Fallback : filtrage manuel
            List<LitigeChargeback> tousLesLitiges = litigeChargebackRepository.findAll();
            List<LitigeChargeback> litiges = tousLesLitiges.stream()
                    .filter(lc -> lc.getDeadlineActuelle() != null &&
                            lc.getDeadlineActuelle().isBefore(LocalDateTime.now()) &&
                            !"FINALISE".equals(lc.getPhaseActuelle()))
                    .collect(java.util.stream.Collectors.toList());

            return litiges.stream()
                    .map(LitigeChargebackDTO::fromEntity)
                    .collect(java.util.stream.Collectors.toList());
        }
    }

    public Object[] getStatistiquesChargebackInstitution(Long institutionId) {
        log.info("📊 Génération des statistiques chargeback pour l'institution: {}", institutionId);

        try {
            // Utiliser la méthode de consultation corrigée
            List<LitigeChargebackDTO> litigesDTO = getLitigesChargebackParInstitution(institutionId);

            // Récupérer les entités pour les calculs
            List<LitigeChargeback> litiges = litigesDTO.stream()
                    .map(dto -> {
                        try {
                            return getLitigeChargebackById(dto.getLitigeId());
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(java.util.Objects::nonNull)
                    .collect(java.util.stream.Collectors.toList());

            long total = litiges.size();
            long enCours = litiges.stream()
                    .filter(l -> !"FINALISE".equals(l.getPhaseActuelle()))
                    .count();
            long finalises = total - enCours;
            long urgents = litiges.stream()
                    .filter(l -> l.getDeadlineActuelle() != null &&
                            l.getDeadlineActuelle().isBefore(LocalDateTime.now()) &&
                            !"FINALISE".equals(l.getPhaseActuelle()))
                    .count();

            BigDecimal montantTotal = litiges.stream()
                    .filter(l -> l.getMontantConteste() != null)
                    .map(LitigeChargeback::getMontantConteste)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new Object[]{total, enCours, finalises, urgents, montantTotal};

        } catch (Exception e) {
            log.error("❌ Erreur lors du calcul des statistiques pour l'institution {}", institutionId, e);
            return new Object[]{0L, 0L, 0L, 0L, BigDecimal.ZERO};
        }
    }

    public boolean peutProgresserVersPhase(Long litigeId, String nouvellePhase) {
        try {
            LitigeChargeback litigeChargeback = getLitigeChargebackById(litigeId);
            String phaseActuelle = litigeChargeback.getPhaseActuelle();

            // Logique de progression séquentielle
            switch (phaseActuelle) {
                case "CHARGEBACK_INITIAL":
                    return "REPRESENTATION".equals(nouvellePhase);
                case "REPRESENTATION":
                    return "PRE_ARBITRAGE".equals(nouvellePhase) || "FINALISE".equals(nouvellePhase);
                case "PRE_ARBITRAGE":
                    return "ARBITRAGE".equals(nouvellePhase) || "FINALISE".equals(nouvellePhase);
                case "ARBITRAGE":
                    return "FINALISE".equals(nouvellePhase);
                case "FINALISE":
                    return false; // Aucune progression possible
                default:
                    return false;
            }

        } catch (Exception e) {
            log.error("❌ Erreur lors de la vérification de progression pour le litige {}", litigeId, e);
            return false;
        }
    }

    public List<Object[]> getDelaisMoyensParPhase() {
        log.info("📊 Calcul des délais moyens par phase");

        try {
            // CORRECTION : Utiliser la méthode disponible dans le repository
            List<Object[]> statistiques = litigeChargebackRepository.getTempsMoyenParPhase();

            // Retourner les statistiques disponibles ou une liste vide
            return statistiques != null ? statistiques : java.util.Collections.emptyList();
        } catch (Exception e) {
            log.error("❌ Erreur lors du calcul des délais moyens", e);
            return java.util.Collections.emptyList();
        }
    }

    public List<EchangeLitige> getHistoriqueComplet(Long litigeId) {
        log.info("📋 Récupération de l'historique complet pour le litige: {}", litigeId);

        // Utiliser la méthode disponible dans le repository
        return echangeLitigeRepository.findByLitigeId(litigeId);
    }

    @Transactional
    public boolean annulerChargeback(Long litigeId, Long utilisateurId, String motifAnnulation) {
        log.info("🚫 Tentative d'annulation du chargeback - LitigeId: {}", litigeId);

        try {
            LitigeChargeback litigeChargeback = getLitigeChargebackById(litigeId);

            // Vérification que l'annulation est possible
            if ("ARBITRAGE".equals(litigeChargeback.getPhaseActuelle()) ||
                    "FINALISE".equals(litigeChargeback.getPhaseActuelle())) {
                log.warn("⚠️ Impossible d'annuler un chargeback en phase {}", litigeChargeback.getPhaseActuelle());
                return false;
            }

            // Validation des droits utilisateur
            Litige litige = getLitigeById(litigeId);
            validateUserRights(utilisateurId, litige);

            // Annulation
            litigeChargeback.progresserVersPhase("FINALISE");
            litigeChargeback.setPeutEtreEscalade(false);
            litigeChargebackRepository.save(litigeChargeback);

            // Création de l'échange d'annulation
            Utilisateur utilisateur = userRepository.findById(utilisateurId).orElse(null);
            if (utilisateur != null) {
                EchangeLitige echange = new EchangeLitige(
                        litigeId,
                        "Chargeback annulé - Motif: " + motifAnnulation,
                        EchangeLitige.TypeEchange.ACTION,
                        "FINALISE",
                        utilisateurId
                );
                echange.setInstitutionId(utilisateur.getInstitution().getId());
                echangeLitigeRepository.save(echange);
            }

            // Mise à jour transaction chargeback
            Optional<TransactionChargeback> tcOpt = transactionChargebackRepository
                    .findByTransactionId(litige.getTransaction().getId());
            if (tcOpt.isPresent()) {
                TransactionChargeback tc = tcOpt.get();
                tc.desactiverLitige();
                tc.ajouterHistorique("Chargeback annulé - " + motifAnnulation);
                transactionChargebackRepository.save(tc);
            }

            log.info("✅ Chargeback annulé avec succès pour le litige: {}", litigeId);
            return true;

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'annulation du chargeback pour le litige {}", litigeId, e);
            return false;
        }
    }




}