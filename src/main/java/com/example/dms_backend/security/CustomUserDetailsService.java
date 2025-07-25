package com.example.dms_backend.security;

import com.example.dms_backend.model.Utilisateur;
import com.example.dms_backend.repository.UserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // ✅ CORRECTION CRITIQUE : Charger avec l'institution pour éviter lazy loading
        Utilisateur utilisateur = userRepository.findByEmailWithInstitution(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec email : " + email));

        return new CustomUserDetails(utilisateur);
    }
}