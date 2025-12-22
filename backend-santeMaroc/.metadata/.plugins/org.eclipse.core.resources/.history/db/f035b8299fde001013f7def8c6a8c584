package com.gateway.gateway_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuration de sécurité pour Gateway-Service.
 * 
 * La Gateway ne gère pas l'authentification elle-même,
 * elle transmet simplement les requêtes aux microservices
 * qui gèrent leur propre sécurité.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    /*	
        http
            .csrf(csrf -> csrf.disable())
            .authorizeExchange(exchanges -> exchanges
                .anyExchange().permitAll() // La Gateway transmet toutes les requêtes, la sécurité est gérée par chaque microservice
            );
        
        return http.build();
        
      */
    	return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchange -> exchange
                    .pathMatchers(HttpMethod.OPTIONS).permitAll()
                    .anyExchange().permitAll()
                )
                .build();
    }
}


