package com.patient_service.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                // ✅ Désactiver CSRF (API REST)
                .csrf(AbstractHttpConfigurer::disable)

                // ✅ Gestion des autorisations
                .authorizeHttpRequests(auth -> auth

                        // ✅ AUTH PUBLIC
                        .requestMatchers("/api/auth/register", "/api/auth/login").permitAll()

                        // ✅ SWAGGER PUBLIC
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // ✅ PATIENTS LIBRES (TEST)
                        .requestMatchers("/api/patients/**").permitAll()

                        // ✅ ERREUR
                        .requestMatchers("/error").permitAll()

                        // ✅ PROFIL => JWT OBLIGATOIRE ✅✅✅
                        .requestMatchers("/api/auth/profile").authenticated()

                        // ✅ TOUT LE RESTE PROTÉGÉ
                        .anyRequest().authenticated()
                )

                // ✅ SESSION JWT STATELESS
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ✅ FILTRE JWT
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ✅ Authentication manager
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ✅ Password encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
