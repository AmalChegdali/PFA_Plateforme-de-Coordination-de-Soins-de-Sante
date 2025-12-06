package com.request_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

/**
 * Configuration OpenAPI/Swagger pour Request-Service.
 * 
 * Cette configuration permet :
 * - La documentation automatique de l'API
 * - L'autorisation JWT dans Swagger UI avec le bouton "Authorize"
 * - La sécurisation des endpoints avec Bearer Token
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@OpenAPIDefinition(
        info = @Info(
                title = "Request Service API",
                version = "1.0",
                description = "API pour la gestion des demandes de patients. " +
                            "Cette API permet aux providers de consulter et répondre aux demandes des patients. " +
                            "Tous les endpoints nécessitent une authentification JWT avec le rôle PROVIDER.",
                contact = @Contact(
                        name = "Request Service Team",
                        email = "support@request-service.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        security = @SecurityRequirement(name = "bearerAuth") // ✅ Active l'autorisation par défaut
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Authentication. Entrez votre token JWT obtenu depuis Provider-Service. " +
                     "Format: Bearer <token>",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // Cette classe sert uniquement de configuration via les annotations
}

