package com.example.dms_backend.controller;

import com.example.dms_backend.dto.LitigeRequest;
import com.example.dms_backend.model.*;
import com.example.dms_backend.repository.LitigeRepository;
import com.example.dms_backend.repository.TransactionRepository;
import com.example.dms_backend.repository.UserRepository;
import com.example.dms_backend.service.LitigeService;
import com.example.dms_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/litiges")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LitigeController {

    private static final Logger logger = LoggerFactory.getLogger(LitigeController.class);

    private final TransactionRepository transactionRepository;
    private final LitigeRepository litigeRepository;
    private final UserRepository utilisateurRepository;
    private final NotificationService notificationService;
    private final LitigeService litigeService;

    @PostMapping
    public ResponseEntity<?> creerLitigeClient(@RequestBody LitigeRequest request) {
        logger.info("Tentative de création de litige avec transactionId={} et utilisateurId={}",
                request.getTransactionId(), request.getUtilisateurId());

        Optional<Transaction> transactionOpt = transactionRepository.findById(request.getTransactionId());
        Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findById(request.getUtilisateurId());

        if (transactionOpt.isEmpty() || utilisateurOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Transaction ou utilisateur introuvable.");
        }

        Transaction transaction = transactionOpt.get();
        Utilisateur utilisateur = utilisateurOpt.get();

        Litige litige = Litige.builder()
                .type(request.getType())
                .description(request.getDescription())
                .statut(StatutLitige.CREE)
                .dateCreation(LocalDate.now())
                .transaction(transaction)
                .declarePar(utilisateur)
                .build();

        transaction.setStatut(StatutTransaction.AVEC_LITIGE);
        transaction.setLitige(litige);

        litigeRepository.save(litige);
        transactionRepository.save(transaction);

        notificationService.notifierBanqueAcquereuse(litige);

        return ResponseEntity.ok("Litige créé avec succès.");
    }

    @PostMapping("/flag")
    public ResponseEntity<Litige> flagTransaction(@RequestBody LitigeRequest request) {
        logger.info("🎯 [API] POST /flag - Reçu: transactionId={}, utilisateurId={}",
                request.getTransactionId(), request.getUtilisateurId());

        try {
            Litige litige = litigeService.flagTransaction(request);
            logger.info("✅ Litige signalé avec succès, ID={}", litige.getId());
            return ResponseEntity.ok(litige);
        } catch (Exception e) {
            logger.error("❌ Erreur lors du signalement du litige: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }


    @GetMapping
    public List<Litige> getAllLitiges() {
        return litigeRepository.findAll();
    }
}
