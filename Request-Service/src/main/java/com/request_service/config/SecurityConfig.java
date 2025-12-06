package com.request_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Configuration de sécurité pour Request-Service.
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtDecoder jwtDecoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        // Autoriser Swagger UI + OpenAPI (tous les chemins nécessaires)
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/swagger-ui/index.html",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/favicon.ico",
                                "/error",
                                "/actuator/**"
                        ).permitAll()

                        // API sécurisée - Les PATIENT peuvent voir leurs propres demandes
                        .requestMatchers("/api/requests/patient/**").hasAnyRole("PATIENT", "PROVIDER")
                        // API sécurisée - Certificats (PATIENT et PROVIDER)
                        .requestMatchers("/api/certificates/**").hasAnyRole("PATIENT", "PROVIDER")
                        // API sécurisée - Seuls les PROVIDER peuvent gérer toutes les demandes
                        .requestMatchers("/api/requests/**").hasRole("PROVIDER")
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    /**
     * Configure le convertisseur JWT pour mapper le rôle du token vers les authorities Spring Security.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // Extraire le rôle du claim "role"
            String role = jwt.getClaimAsString("role");
            if (role != null) {
                // Ajouter le préfixe "ROLE_" si nécessaire
                String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                return Collections.singletonList(new SimpleGrantedAuthority(authority));
            }
            return Collections.emptyList();
        });
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(List.of(
                "http://localhost:8081", // Patient Service
                "http://localhost:8082", // Provider Service
                "http://localhost:8084"  // Request Service
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}

