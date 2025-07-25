package com.example.dms_backend;

import com.example.dms_backend.model.*;
import com.example.dms_backend.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader {

    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;

    @PostConstruct
    public void loadData() {

        // ✅ VÉRIFICATION D'EXISTENCE - Évite les doublons !
        if (institutionRepository.count() > 0) {
            log.info("🔄 Données institutions déjà présentes, DataLoader ignoré");
            return;
        }

        log.info("🚀 Initialisation des données de base...");

        // ✅ INSTITUTIONS CORRIGÉES - Types cohérents, plus de logique émettrice/acquéreuse fixe
        Institution cih = institutionRepository.save(
                Institution.builder()
                        .nom("CIH BANK")
                        .type(TypeInstitution.CENTRE)  // ✅ NEUTRE - Rôle déterminé par CSV
                        .enabled(true)
                        .build());

        Institution attijari = institutionRepository.save(
                Institution.builder()
                        .nom("ATTIJARIWAFA")
                        .type(TypeInstitution.CENTRE)  // ✅ NEUTRE - Rôle déterminé par CSV
                        .enabled(true)
                        .build());

        Institution bmce = institutionRepository.save(
                Institution.builder()
                        .nom("BMCE")
                        .type(TypeInstitution.CENTRE)  // ✅ NEUTRE - Rôle déterminé par CSV
                        .enabled(true)
                        .build());

        Institution bam = institutionRepository.save(
                Institution.builder()
                        .nom("BANK AL-MAGHRIB")
                        .type(TypeInstitution.CENTRE)  // ✅ Vraie banque centrale
                        .enabled(true)
                        .build());

        Institution apple = institutionRepository.save(
                Institution.builder()
                        .nom("Apple Pay")
                        .type(TypeInstitution.PORTEFEUILLE)  // ✅ OK
                        .enabled(true)
                        .build());

        Institution paypal = institutionRepository.save(
                Institution.builder()
                        .nom("PayPal")
                        .type(TypeInstitution.PORTEFEUILLE)  // ✅ OK
                        .enabled(true)
                        .build());

        log.info("✅ {} institutions créées", institutionRepository.count());

        // ✅ UTILISATEURS - Avec vérification d'existence
        if (userRepository.count() == 0) {

            userRepository.save(Utilisateur.builder()
                    .nom("Admin Centre")
                    .email("centre@dms.com")
                    .password(new BCryptPasswordEncoder().encode("admin123"))
                    .role("ROLE_ADMIN")
                    .enabled(true)
                    .institution(bam)  // Rattaché à la banque centrale
                    .niveaux(NiveauAcces.ELEVE)
                    .build());

            userRepository.save(Utilisateur.builder()
                    .nom("Employé CIH")
                    .email("employe@cih.com")
                    .password(new BCryptPasswordEncoder().encode("employe123"))
                    .role("ROLE_USER")
                    .enabled(true)
                    .institution(cih)  // Rattaché à CIH
                    .niveaux(NiveauAcces.MOYEN)
                    .build());

            log.info("✅ {} utilisateurs créés", userRepository.count());
        } else {
            log.info("🔄 Utilisateurs déjà présents, création ignorée");
        }

        log.info("🎯 DataLoader terminé avec succès !");
        log.info("📊 Résumé : {} institutions, {} utilisateurs",
                institutionRepository.count(), userRepository.count());
    }
}