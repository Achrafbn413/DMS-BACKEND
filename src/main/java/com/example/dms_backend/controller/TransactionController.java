package com.example.dms_backend.controller;

import com.example.dms_backend.dto.TransactionDTO;
import com.example.dms_backend.model.Transaction;
import com.example.dms_backend.model.Utilisateur;
import com.example.dms_backend.repository.TransactionRepository;
import com.example.dms_backend.repository.UserRepository;
import com.example.dms_backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @GetMapping("/mine")
    public List<Transaction> getMesTransactions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Utilisateur user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        return transactionService.getTransactionsByAccessLevel(user);
    }

    @GetMapping("/dashboard")
    public List<TransactionDTO> getTransactionsForDashboard() {
        return transactionRepository.findAll().stream()
                .map(t -> new TransactionDTO(
                        t.getId(),
                        t.getReference(),
                        t.getMontant(),
                        t.getDateTransaction(),
                        t.getType().toString(),
                        t.getStatut().toString()
                ))
                .collect(Collectors.toList());
    }
}
