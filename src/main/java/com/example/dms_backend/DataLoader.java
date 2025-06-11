package com.example.dms_backend;

import com.example.dms_backend.model.*;
import com.example.dms_backend.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataLoader {

    private final InstitutionRepository institutionRepository;
    private final UserRepository userRepository;

    @PostConstruct
    public void loadData() {
        // ✅ Institutions
        Institution cih = institutionRepository.save(
                Institution.builder().nom("CIH BANK").type(TypeInstitution.EMETTRICE).enabled(true).build());
        Institution attijari = institutionRepository.save(
                Institution.builder().nom("ATTIJARIWAFA").type(TypeInstitution.EMETTRICE).enabled(true).build());
        Institution bmce = institutionRepository.save(
                Institution.builder().nom("BMCE").type(TypeInstitution.ACQUEREUSE).enabled(true).build());
        Institution bam = institutionRepository.save(
                Institution.builder().nom("BANK AL-MAGHRIB").type(TypeInstitution.CENTRE).enabled(true).build());
        Institution apple = institutionRepository.save(
                Institution.builder().nom("Apple Pay").type(TypeInstitution.PORTEFEUILLE).enabled(true).build());
        Institution paypal = institutionRepository.save(
                Institution.builder().nom("PayPal").type(TypeInstitution.PORTEFEUILLE).enabled(true).build());

        // ✅ Utilisateurs
        userRepository.save(Utilisateur.builder()
                .nom("Admin Centre")
                .email("centre@dms.com")
                .password(new BCryptPasswordEncoder().encode("admin123"))
                .role("ROLE_ADMIN")
                .enabled(true)
                .institution(bam)
                .niveaux(NiveauAcces.ELEVE)
                .build());

        userRepository.save(Utilisateur.builder()
                .nom("Employé CIH")
                .email("employe@cih.com")
                .password(new BCryptPasswordEncoder().encode("employe123"))
                .role("ROLE_USER")
                .enabled(true)
                .institution(cih)
                .niveaux(NiveauAcces.MOYEN)
                .build());
    }
}
