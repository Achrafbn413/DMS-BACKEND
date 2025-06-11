package com.example.dms_backend.service.impl;

import com.example.dms_backend.model.Transaction;
import com.example.dms_backend.model.Utilisateur;
import com.example.dms_backend.repository.TransactionRepository;
import com.example.dms_backend.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;

    @Override
    public List<Transaction> getTransactionsByAccessLevel(Utilisateur user) {
        String userInstitution = user.getInstitution().getNom();

        // Utilisation d'une requête personnalisée pour de meilleures performances
        return transactionRepository.findByBanqueEmettriceNomOrBanqueAcquereuseNom(
                userInstitution, userInstitution);
    }
}
