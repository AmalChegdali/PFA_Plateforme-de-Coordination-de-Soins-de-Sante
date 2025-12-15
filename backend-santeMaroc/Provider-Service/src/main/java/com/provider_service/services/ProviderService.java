package com.provider_service.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.provider_service.dto.ProfileCompletionRequest;
import com.provider_service.dto.RegisterRequest;
import com.provider_service.models.Provider;
import com.provider_service.repository.ProviderRepository;

@Service
public class ProviderService implements UserDetailsService {
	@Autowired
	private ProviderRepository providerRepository;
	
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		return providerRepository.findByEmail(email)
			.orElseThrow(() -> new UsernameNotFoundException("Provider not found with email: " + email));
	}

	public Provider registerProvider(String email, String password) {
		if (providerRepository.existsByEmail(email)) {
			throw new RuntimeException("Provider already exists with email: " + email);
		}

		Provider provider = new Provider();
		provider.setEmail(email);
		provider.setPassword(passwordEncoder.encode(password));

		return providerRepository.save(provider);
	}
	
	public Provider registerProvider(RegisterRequest request) {
		if (providerRepository.existsByEmail(request.getEmail())) {
			throw new RuntimeException("Provider already exists with email: " + request.getEmail());
		}

		Provider provider = new Provider();
		provider.setEmail(request.getEmail());
		provider.setPassword(passwordEncoder.encode(request.getPassword()));
		
		// Remplir les informations optionnelles du registerRequest
		if (request.getFullName() != null) {
			provider.setFullName(request.getFullName());
		}
		if (request.getContactNumber() != null) {
			provider.setContactNumber(request.getContactNumber());
		}
		if (request.getProfessionalTitle() != null) {
			provider.setProfessionalTitle(request.getProfessionalTitle());
		}
		if (request.getSpecialty() != null) {
			provider.setSpecialty(request.getSpecialty());
		}

		return providerRepository.save(provider);
	}
	
	// Complete provider profile (provider fills this)
	public Provider completeProviderProfile(String providerId, ProfileCompletionRequest request) {
		Provider provider = providerRepository.findById(providerId)
			.orElseThrow(() -> new RuntimeException("Provider not found with ID: " + providerId));

		// Update provider information
		provider.setFullName(request.getFullName());
		provider.setProfessionalTitle(request.getProfessionalTitle());
		provider.setSpecialty(request.getSpecialty());
		provider.setSubSpecialties(request.getSubSpecialties());
		provider.setStateLicenses(request.getStateLicenses());
		provider.setPrimaryClinicName(request.getPrimaryClinicName());
		provider.setClinicAddress(request.getClinicAddress());
		provider.setContactNumber(request.getContactNumber());

		Provider savedProvider = providerRepository.save(provider);

		return savedProvider;
	}

	public Provider findByEmail(String email) {
		return providerRepository.findByEmail(email)
			.orElseThrow(() -> new RuntimeException("Provider not found with email: " + email));
	}

	/**
	 * Récupère tous les providers (pour la liste publique).
	 * 
	 * @return Liste de tous les providers
	 */
	public List<Provider> getAllProviders() {
		return providerRepository.findAll();
	}

}
