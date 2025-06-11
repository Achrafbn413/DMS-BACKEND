package com.example.dms_backend.service;

import com.example.dms_backend.model.Litige;
import com.example.dms_backend.model.Transaction;
import com.example.dms_backend.model.Utilisateur;
import com.example.dms_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final UserRepository userRepository;

    // Envoie les notifications aux employés de la banque acquéreuse
    public List<Utilisateur> getUtilisateursBanqueAcquereuse(Transaction transaction) {
        return userRepository.findAll().stream()
                .filter(u -> u.getInstitution().equals(transaction.getBanqueAcquereuse()))
                .collect(Collectors.toList());
    }

    public void notifierBanqueAcquereuse(Litige litige) {
        List<Utilisateur> destinataires = getUtilisateursBanqueAcquereuse(litige.getTransaction());
        destinataires.forEach(user -> {
            System.out.println("🔔 Notification envoyée à " + user.getEmail()
                    + " pour le litige sur la transaction " + litige.getTransaction().getReference());
        });
    }
}
