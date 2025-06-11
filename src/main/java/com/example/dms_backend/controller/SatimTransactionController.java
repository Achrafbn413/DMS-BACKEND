package com.example.dms_backend.controller;

import com.example.dms_backend.model.SatimTransaction;
import com.example.dms_backend.repository.SatimTransactionRepository;
import com.example.dms_backend.service.SatimTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/satim")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*") // À adapter selon vos besoins de sécurité
public class SatimTransactionController {

    private final SatimTransactionService satimTransactionService;
    private final SatimTransactionRepository satimTransactionRepository;

    /**
     * Upload et import d'un fichier CSV SATIM
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("❌ Fichier vide");
            }

            log.info("📁 Début import fichier SATIM: {}", file.getOriginalFilename());
            satimTransactionService.importFile(file);

            String message = "✅ Fichier importé avec succès: " + file.getOriginalFilename();
            log.info(message);
            return ResponseEntity.ok(message);

        } catch (Exception e) {
            String errorMessage = "❌ Erreur lors de l'import: " + e.getMessage();
            log.error(errorMessage, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

    /**
     * Récupérer toutes les transactions SATIM
     */
    @GetMapping("/all")
    public ResponseEntity<List<SatimTransaction>> getAllSatimTransactions() {
        try {
            List<SatimTransaction> transactions = satimTransactionRepository.findAll();
            log.info("📋 Récupération de {} transactions SATIM", transactions.size());
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("❌ Erreur récupération transactions SATIM", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Récupérer une transaction SATIM par ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SatimTransaction> getSatimTransaction(@PathVariable Long id) {
        try {
            return satimTransactionRepository.findById(id)
                    .map(transaction -> {
                        log.info("🔍 Transaction SATIM trouvée: {}", id);
                        return ResponseEntity.ok(transaction);
                    })
                    .orElseGet(() -> {
                        log.warn("⚠ Transaction SATIM non trouvée: {}", id);
                        return ResponseEntity.notFound().build();
                    });
        } catch (Exception e) {
            log.error("❌ Erreur récupération transaction SATIM {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Supprimer toutes les transactions SATIM (pour les tests)
     */
    @DeleteMapping("/all")
    public ResponseEntity<String> deleteAllSatimTransactions() {
        try {
            long count = satimTransactionRepository.count();
            satimTransactionRepository.deleteAll();
            String message = "🗑️ " + count + " transactions SATIM supprimées";
            log.info(message);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("❌ Erreur suppression transactions SATIM", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("❌ Erreur lors de la suppression");
        }
    }

    /**
     * Obtenir le nombre total de transactions SATIM
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countSatimTransactions() {
        try {
            long count = satimTransactionRepository.count();
            log.info("📊 Nombre de transactions SATIM: {}", count);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            log.error("❌ Erreur comptage transactions SATIM", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}