package com.example.dms_backend.auth;

import com.example.dms_backend.model.Institution;
import com.example.dms_backend.model.Utilisateur;
import com.example.dms_backend.repository.InstitutionRepository;
import com.example.dms_backend.repository.UserRepository;
import com.example.dms_backend.security.CustomUserDetails;
import com.example.dms_backend.security.CustomUserDetailsService;
import com.example.dms_backend.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final InstitutionRepository institutionRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthResponse register(RegisterRequest request) {
        Institution institution = institutionRepository.findById(request.getInstitutionId())
                .orElseThrow(() -> new RuntimeException("Institution non trouvée"));

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

        String jwt = jwtUtil.generateToken(new CustomUserDetails(savedUser), savedUser.getRole());

        return new AuthResponse(
                jwt,
                savedUser.getId(),  // Inclure l'ID
                savedUser.getNom(),
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getNiveaux(),
                institution.getNom(),
                institution.getId()  // Inclure l'ID de l'institution
        );
    }

    public AuthResponse login(AuthRequest request) {
        Utilisateur user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Email invalide"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Mot de passe invalide");
        }

        String jwt = jwtUtil.generateToken(new CustomUserDetails(user), user.getRole());
        String institutionName = (user.getInstitution() != null) ? user.getInstitution().getNom() : "ADMIN";
        Long institutionId = (user.getInstitution() != null) ? user.getInstitution().getId() : null;

        return new AuthResponse(
                jwt,
                user.getId(),  // Inclure l'ID
                user.getNom(),
                user.getEmail(),
                user.getRole(),
                user.getNiveaux(),
                institutionName,
                institutionId  // Inclure l'ID de l'institution
        );
    }
}
