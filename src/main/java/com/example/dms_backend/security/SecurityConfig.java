package com.example.dms_backend.security;

import com.example.dms_backend.security.jwt.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // OPTIONS requests pour CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Endpoints publics (authentification, documentation)
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/admin/institutions/public/**").permitAll()
                        .requestMatchers("/api/satim/upload").permitAll()

                        // ENDPOINTS TRANSACTIONS CORRIGÉS
                        .requestMatchers(HttpMethod.GET, "/api/transactions/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/transactions/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/transactions/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/transactions/**").authenticated()

                        // CORRECTION : Endpoints de litiges existants
                        .requestMatchers(HttpMethod.GET, "/api/public/litiges").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/public/litiges/institution/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/public/litiges/emis/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/public/litiges/reçus/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/public/litiges/unread/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/public/litiges/by-user/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/public/litiges/signaled-transactions/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/public/litiges/flag").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/public/litiges/*/mark-read").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/public/litiges/details/**").authenticated()

                        // ENDPOINTS CHARGEBACK CORRIGÉS - Ordre spécifique avant générique
                        .requestMatchers(HttpMethod.POST, "/api/public/chargebacks/initiate").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/public/chargebacks/representation").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/public/chargebacks/second-presentment").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/public/chargebacks/arbitrage").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/public/chargebacks/*/decision").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/public/chargebacks/*/cancel").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/public/chargebacks/institution/*").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/public/chargebacks/stats/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/public/chargebacks/*/upload-justificatifs").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/public/chargebacks/*/justificatifs").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/public/chargebacks/*/historique").authenticated()

                        // AJOUT : Endpoints SATIM
                        .requestMatchers(HttpMethod.GET, "/api/satim/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/satim/**").authenticated()

                        // Administration
                        .requestMatchers("/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // Tout le reste nécessite une authentification
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }




    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // ✅ ORIGINS CORRIGÉS pour développement et production
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "http://localhost:*"
        ));

        // ✅ MÉTHODES HTTP complètes
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"
        ));

        // ✅ HEADERS COMPLETS pour éviter les rejets CORS
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers",
                "X-Frame-Options"
        ));

        // ✅ HEADERS EXPOSÉS pour le client
        config.setExposedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Total-Count"
        ));

        config.setAllowCredentials(true);
        config.setMaxAge(86400L); // 24 heures pour réduire les preflight

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}