package com.example.dms_backend.service.impl;

import com.example.dms_backend.model.Utilisateur;
import com.example.dms_backend.repository.UserRepository;
import com.example.dms_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Utilisateur getUserByUsername(String email) {
        log.info("🔍 Recherche utilisateur par email: {}", email);

        try {
            // ✅ AMÉLIORATION : Utiliser une méthode avec fetch join pour charger l'institution
            Utilisateur utilisateur = userRepository.findByEmailWithInstitution(email)
                    .orElseThrow(() -> {
                        log.warn("⚠️ Utilisateur non trouvé avec l'email: {}", email);
                        return new IllegalArgumentException("Utilisateur non trouvé avec l'email: " + email);
                    });

            log.info("✅ Utilisateur trouvé: {} (Institution: {})",
                    utilisateur.getNom(),
                    utilisateur.getInstitution() != null ? utilisateur.getInstitution().getNom() : "Aucune");

            // ✅ VALIDATION : S'assurer que l'utilisateur a une institution
            if (utilisateur.getInstitution() == null) {
                log.warn("⚠️ Utilisateur {} n'a pas d'institution associée", email);
                throw new IllegalStateException("L'utilisateur doit être associé à une institution");
            }

            return utilisateur;

        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération de l'utilisateur {}: {}", email, e.getMessage());
            throw e;
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Récupération par ID avec institution
     */
    public Utilisateur getUserById(Long userId) {
        log.info("🔍 Recherche utilisateur par ID: {}", userId);

        return userRepository.findByIdWithInstitution(userId)
                .orElseThrow(() -> {
                    log.warn("⚠️ Utilisateur non trouvé avec l'ID: {}", userId);
                    return new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId);
                });
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Vérifier si l'utilisateur existe
     */
    public boolean existsByEmail(String email) {
        boolean exists = userRepository.existsByEmail(email);
        log.debug("🔍 Utilisateur {} existe: {}", email, exists);
        return exists;
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Récupération avec fallback vers méthode simple
     */
    public Utilisateur getUserByUsernameWithFallback(String email) {
        log.info("🔍 Recherche utilisateur avec fallback pour email: {}", email);

        try {
            // Essayer d'abord avec l'institution
            return userRepository.findByEmailWithInstitution(email)
                    .orElseGet(() -> {
                        log.warn("⚠️ Fallback vers méthode simple pour utilisateur: {}", email);
                        return userRepository.findByEmail(email)
                                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé: " + email));
                    });
        } catch (Exception e) {
            log.error("❌ Erreur fallback pour utilisateur {}: {}", email, e.getMessage());
            throw e;
        }
    }
}