package com.example.dms_backend.service;

import com.example.dms_backend.model.Transaction;
import com.example.dms_backend.model.Utilisateur;

import java.util.List;

public interface TransactionService {
    List<Transaction> getTransactionsByAccessLevel(Utilisateur user);
}
