package com.provider_service.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.provider_service.dto.AuthRequest;
import com.provider_service.dto.AuthResponse;
import com.provider_service.dto.ProfileCompletionRequest;
import com.provider_service.dto.ProviderProfileDTO;
import com.provider_service.dto.RegisterRequest;
import com.provider_service.models.Provider;
import com.provider_service.services.JwtService;
import com.provider_service.services.ProviderService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "Endpoints for provider authentication and profile management")
public class AuthController {

    @Autowired
    private ProviderService providerService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    @Operation(summary = "Register provider", description = "Registers a provider and returns a JWT token")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            Provider provider = providerService.registerProvider(request.getEmail(), request.getPassword());
            String token = jwtService.generateToken(provider);

            return ResponseEntity.ok(new AuthResponse(token, "Registration successful", provider.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, "Registration failed: " + e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login provider", description = "Authenticates provider and returns a JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            Provider provider = (Provider) authentication.getPrincipal();
            String token = jwtService.generateToken(provider);

            return ResponseEntity.ok(new AuthResponse(token, "Login successful", provider.getEmail()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new AuthResponse(null, "Invalid credentials", null));
        }
    }

    @GetMapping("/profile")
    @Operation(summary = "Get provider profile", description = "Returns the authenticated provider profile")
    public ResponseEntity<Provider> getProfile(Authentication authentication) {
        Provider provider = (Provider) authentication.getPrincipal();
        return ResponseEntity.ok(provider);
    }

    @PutMapping("/complete-profile")
    @Operation(summary = "Complete provider profile", description = "Completes provider profile information")
    public ResponseEntity<ProviderProfileDTO> completeProfile(
            @RequestBody ProfileCompletionRequest profileUpdates,
            Authentication authentication) {
        try {
            Provider currentProvider = (Provider) authentication.getPrincipal();
            Provider updatedProvider = providerService
                    .completeProviderProfile(currentProvider.getId(), profileUpdates);

            ProviderProfileDTO profileDTO = convertToProfileDTO(updatedProvider);
            return ResponseEntity.ok(profileDTO);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // ---------- MAPPER ----------
    private ProviderProfileDTO convertToProfileDTO(Provider provider) {
        ProviderProfileDTO dto = new ProviderProfileDTO();
        dto.setProviderID(provider.getId());
        dto.setEmail(provider.getEmail());
        dto.setFullName(provider.getFullName());
        dto.setProfessionalTitle(provider.getProfessionalTitle());
        dto.setSpecialty(provider.getSpecialty());
        dto.setSubSpecialties(provider.getSubSpecialties());
        dto.setStateLicenses(provider.getStateLicenses());
        dto.setPrimaryClinicName(provider.getPrimaryClinicName());
        dto.setClinicAddress(provider.getClinicAddress());
        dto.setContactNumber(provider.getContactNumber());
        return dto;
    }
}
