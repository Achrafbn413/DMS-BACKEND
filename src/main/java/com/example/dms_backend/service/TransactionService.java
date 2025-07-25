package com.example.dms_backend.service;

import com.example.dms_backend.dto.TransactionDTO;
import com.example.dms_backend.dto.TransactionDetailsResponse;
import com.example.dms_backend.model.Transaction;
import com.example.dms_backend.model.Utilisateur;

import java.util.List;

public interface TransactionService {

    List<Transaction> getTransactionsByAccessLevel(Utilisateur user);

    List<TransactionDTO> getAllTransactionsForDashboard();

    List<TransactionDTO> getAllTransactionsWithLitiges();
    // ====================================================================
// 🆕 NOUVELLE MÉTHODE POUR FONCTIONNALITÉ DÉTAILS TRANSACTION
// ====================================================================

    /**
     * ✅ NOUVELLE MÉTHODE : Récupérer les détails complets d'une transaction
     */
    TransactionDetailsResponse getTransactionCompletDetails(Long transactionId);



}
