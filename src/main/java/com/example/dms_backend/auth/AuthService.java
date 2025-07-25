package com.example.dms_backend.auth;

import com.example.dms_backend.model.Institution;
import com.example.dms_backend.model.Utilisateur;
import com.example.dms_backend.repository.InstitutionRepository;
import com.example.dms_backend.repository.UserRepository;
import com.example.dms_backend.security.CustomUserDetails;
import com.example.dms_backend.security.CustomUserDetailsService;
import com.example.dms_backend.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        log.info("🔐 Tentative d'inscription pour email: {}", request.getEmail());

        try {
            // ✅ Vérifier si l'email existe déjà
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Un utilisateur avec cet email existe déjà");
            }

            Institution institution = institutionRepository.findById(request.getInstitutionId())
                    .orElseThrow(() -> new RuntimeException("Institution non trouvée avec l'ID: " + request.getInstitutionId()));

            Utilisateur user = Utilisateur.builder()
                    .nom(request.getNom())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .role(request.getRole().name())
                    .niveaux(request.getNiveaux())
                    .institution(institution)
                    .enabled(true)
                    .build();

            Utilisateur savedUser = userRepository.save(user);
            log.info("✅ Utilisateur créé avec succès: {} (Institution: {})",
                    savedUser.getEmail(), institution.getNom());

            String jwt = jwtUtil.generateToken(new CustomUserDetails(savedUser), savedUser.getRole());

            return new AuthResponse(
                    jwt,
                    savedUser.getId(),
                    savedUser.getNom(),
                    savedUser.getEmail(),
                    savedUser.getRole(),
                    savedUser.getNiveaux(),
                    institution.getNom(),
                    institution.getId()
            );
        } catch (Exception e) {
            log.error("❌ Erreur lors de l'inscription: {}", e.getMessage());
            throw e;
        }
    }

    public AuthResponse login(AuthRequest request) {
        log.info("🔐 Tentative de connexion pour email: {}", request.getEmail());

        try {
            // ✅ CORRECTION CRITIQUE : Charger avec l'institution
            Utilisateur user = userRepository.findByEmailWithInstitution(request.getEmail())
                    .orElseThrow(() -> {
                        log.warn("⚠️ Utilisateur non trouvé: {}", request.getEmail());
                        return new UsernameNotFoundException("Email invalide");
                    });

            // ✅ Vérification du mot de passe
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("⚠️ Mot de passe invalide pour: {}", request.getEmail());
                throw new BadCredentialsException("Mot de passe invalide");
            }

            // ✅ Vérification que l'utilisateur est activé
            if (!user.getEnabled()) {
                log.warn("⚠️ Compte désactivé pour: {}", request.getEmail());
                throw new RuntimeException("Compte désactivé");
            }

            // ✅ Génération du JWT
            String jwt = jwtUtil.generateToken(new CustomUserDetails(user), user.getRole());

            // ✅ Gestion robuste de l'institution
            String institutionName = "ADMIN"; // Par défaut
            Long institutionId = null;

            if (user.getInstitution() != null) {
                institutionName = user.getInstitution().getNom();
                institutionId = user.getInstitution().getId();
                log.info("✅ Connexion réussie: {} (Institution: {} - ID: {})",
                        user.getEmail(), institutionName, institutionId);
            } else {
                log.info("✅ Connexion réussie: {} (Administrateur sans institution)", user.getEmail());
            }

            return new AuthResponse(
                    jwt,
                    user.getId(),
                    user.getNom(),
                    user.getEmail(),
                    user.getRole(),
                    user.getNiveaux(),
                    institutionName,
                    institutionId
            );
        } catch (Exception e) {
            log.error("❌ Erreur lors de la connexion pour {}: {}", request.getEmail(), e.getMessage());
            throw e;
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Vérification de token (optionnelle)
     */
    public boolean isTokenValid(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            return username != null && userRepository.existsByEmail(username);
        } catch (Exception e) {
            log.warn("⚠️ Token invalide: {}", e.getMessage());
            return false;
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE : Refresh token (optionnelle)
     */
    public AuthResponse refreshToken(String token) {
        try {
            String email = jwtUtil.extractUsername(token);
            Utilisateur user = userRepository.findByEmailWithInstitution(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

            String newJwt = jwtUtil.generateToken(new CustomUserDetails(user), user.getRole());

            String institutionName = user.getInstitution() != null ? user.getInstitution().getNom() : "ADMIN";
            Long institutionId = user.getInstitution() != null ? user.getInstitution().getId() : null;

            return new AuthResponse(
                    newJwt,
                    user.getId(),
                    user.getNom(),
                    user.getEmail(),
                    user.getRole(),
                    user.getNiveaux(),
                    institutionName,
                    institutionId
            );
        } catch (Exception e) {
            log.error("❌ Erreur refresh token: {}", e.getMessage());
            throw e;
        }
    }
}