package com.gateway.gateway_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application principale de Gateway-Service.
 * 
 * Cette Gateway sert de point d'entrée unique pour tous les microservices.
 * Elle utilise Eureka pour la découverte de services et route les requêtes
 * vers les microservices appropriés.
 * 
 * Note: @EnableEurekaClient n'est plus nécessaire dans Spring Cloud 2022.0.4+
 * Eureka Client est activé automatiquement si la dépendance est présente.
 */
@SpringBootApplication
public class GatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayServiceApplication.class, args);
	}

}
