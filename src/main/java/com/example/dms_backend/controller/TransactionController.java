package com.example.dms_backend.controller;

import com.example.dms_backend.dto.TransactionDetailsResponse;
import com.example.dms_backend.repository.TransactionRepository;
import com.example.dms_backend.dto.TransactionDTO;
import com.example.dms_backend.model.Transaction;
import com.example.dms_backend.model.Utilisateur;
import com.example.dms_backend.security.jwt.JwtUtil;
import com.example.dms_backend.service.TransactionService;
import com.example.dms_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    // ✅ AJOUT : Injection du repository
    private final TransactionRepository transactionRepository;

    /**
     * ✅ ENDPOINT PRINCIPAL : Utilisé par Angular dashboard
     * Retourne toutes les transactions avec statut de litige
     */
    @GetMapping("/all")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions() {
        try {
            log.info("🔍 Récupération de toutes les transactions avec statut litiges");
            List<TransactionDTO> transactions = transactionService.getAllTransactionsWithLitiges();
            log.info("✅ {} transactions récupérées", transactions.size());
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des transactions", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * ✅ ENDPOINT SÉCURISÉ : Transactions par niveau d'accès utilisateur
     */
    @GetMapping("/dashboard")
    public ResponseEntity<List<TransactionDTO>> getTransactionsForDashboard(@RequestHeader("Authorization") String authHeader) {
        try {
            String jwt = authHeader.substring(7);
            String username = jwtUtil.extractUsername(jwt);
            Utilisateur user = userService.getUserByUsername(username);

            log.info("🔍 Récupération des transactions pour l'utilisateur: {} (Institution: {})",
                    username, user.getInstitution() != null ? user.getInstitution().getNom() : "null");

            List<Transaction> transactions = transactionService.getTransactionsByAccessLevel(user);

            // ✅ CONVERSION en DTOs pour cohérence
            List<TransactionDTO> transactionDTOs = transactions.stream()
                    .map(this::convertToDTO)
                    .toList();

            log.info("✅ {} transactions récupérées pour l'utilisateur {}", transactionDTOs.size(), username);
            return ResponseEntity.ok(transactionDTOs);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des transactions dashboard", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * ✅ ENDPOINT POUR TESTS : Format basique sans authentification
     */
    @GetMapping("/simple")
    public ResponseEntity<List<TransactionDTO>> getAllTransactionsSimple() {
        try {
            log.info("🔍 Récupération simple de toutes les transactions");
            List<TransactionDTO> transactions = transactionService.getAllTransactionsForDashboard();
            log.info("✅ {} transactions récupérées (format simple)", transactions.size());
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des transactions simples", e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * ✅ ENDPOINT POUR DÉTAILS : Récupération d'une transaction spécifique
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactionById(@PathVariable Long id) {
        try {
            log.info("🔍 Récupération de la transaction ID: {}", id);
            // Vous devrez ajouter cette méthode dans TransactionService
            // TransactionDTO transaction = transactionService.getTransactionById(id);
            // return ResponseEntity.ok(transaction);

            // Temporairement retourner 501 jusqu'à l'implémentation
            return ResponseEntity.status(501).build();
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération de la transaction {}", id, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * ✅ NOUVEAU ENDPOINT SPÉCIFIQUE : Détails complets d'une transaction
     * URL: /api/transactions/details/{id}
     */
    @GetMapping("/details/{id}")
    public ResponseEntity<TransactionDetailsResponse> getTransactionDetails(@PathVariable Long id) {
        try {
            log.info("🔍 [DEBUG] Recherche transaction ID: {}", id);

            // ✅ DEBUG : Vérifier si la transaction existe
            boolean exists = transactionRepository.existsById(id);
            log.info("📊 Transaction {} existe: {}", id, exists);

            if (!exists) {
                // ✅ DEBUG : Afficher les IDs disponibles
                List<Long> availableIds = transactionRepository.findAllIds()
                        .stream()
                        .limit(10)
                        .collect(Collectors.toList());
                log.warn("⚠️ Transaction {} introuvable. Premiers IDs disponibles: {}", id, availableIds);
                return ResponseEntity.notFound().build();
            }

            TransactionDetailsResponse details = transactionService.getTransactionCompletDetails(id);
            log.info("✅ Détails complets récupérés avec succès pour la transaction {}", id);
            return ResponseEntity.ok(details);
        } catch (RuntimeException e) {
            log.error("❌ Transaction non trouvée: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération des détails de la transaction {}", id, e);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * ✅ MÉTHODE UTILITAIRE : Conversion Transaction → TransactionDTO
     */
    private TransactionDTO convertToDTO(Transaction transaction) {
        String banqueDeclarante = null;

        // ✅ Déterminer la banque déclarante si litige existe
        if (transaction.getLitige() != null) {
            if (transaction.getLitige().getBanqueDeclarante() != null) {
                banqueDeclarante = transaction.getLitige().getBanqueDeclarante().getNom();
            } else if (transaction.getLitige().getDeclarePar() != null &&
                    transaction.getLitige().getDeclarePar().getInstitution() != null) {
                banqueDeclarante = transaction.getLitige().getDeclarePar().getInstitution().getNom();
            }
        }

        return new TransactionDTO(
                transaction.getId(),
                transaction.getReference(),
                transaction.getMontant(),
                transaction.getDateTransaction(),
                transaction.getType().toString(),
                transaction.getLitige() != null ? "AVEC_LITIGE" : "NORMALE",
                transaction.getBanqueEmettrice() != null ? transaction.getBanqueEmettrice().getNom() : null,
                transaction.getBanqueAcquereuse() != null ? transaction.getBanqueAcquereuse().getNom() : null,
                banqueDeclarante
        );
    }

}