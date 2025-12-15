package com.medicalrecord_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Configuration pour valider les JWT tokens avec HS256 (clé secrète partagée).
 * 
 * Cette configuration remplace la validation OAuth2 Resource Server avec JWK Set URI
 * par une validation directe avec la clé secrète partagée, compatible avec les tokens
 * générés par Provider-Service et Patient-Service.
 */
@Configuration
public class JwtConfig {

    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;

    /**
     * Configure le JwtDecoder pour valider les tokens JWT avec HS256.
     * Utilise la clé secrète partagée configurée dans application.properties.
     * 
     * @return JwtDecoder configuré pour HS256
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Convertir la clé secrète en SecretKey pour HS256
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        
        // Créer le decoder avec la clé secrète
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}

