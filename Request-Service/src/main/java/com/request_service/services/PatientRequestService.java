package com.request_service.services;

import com.request_service.dto.PatientRequestMessageDTO;
import com.request_service.dto.RequestResponseDTO;
import com.request_service.models.PatientRequest;
import com.request_service.repository.PatientRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.request_service.config.RabbitConfig.REQUEST_RESPONSES_EXCHANGE;
import static com.request_service.config.RabbitConfig.REQUEST_RESPONSES_ROUTING_KEY;

/**
 * Service pour gérer les demandes de patients.
 * 
 * @author Request-Service Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PatientRequestService {

    private final PatientRequestRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final DataEnrichmentService enrichmentService;

    /**
     * Crée une nouvelle demande depuis un message RabbitMQ.
     * 
     * @param messageDTO Le DTO de la demande reçue
     * @return La demande créée
     */
    public PatientRequest createRequest(PatientRequestMessageDTO messageDTO) {
        log.info("Création d'une nouvelle demande : {} pour le patient {}", 
                messageDTO.getRequestId(), messageDTO.getPatientId());
        
        PatientRequest request = convertToEntity(messageDTO);
        request.setStatus("EN_ATTENTE");
        request.setCreatedAt(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        
        PatientRequest saved = repository.save(request);
        log.info("✅ Demande créée avec succès : {}", saved.getRequestId());
        
        return saved;
    }

    /**
     * Récupère toutes les demandes.
     * 
     * @return Liste de toutes les demandes
     */
    public List<PatientRequestMessageDTO> getAllRequests() {
        return repository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère une demande par son ID.
     * 
     * @param requestId L'ID de la demande
     * @return La demande ou null si non trouvée
     */
    public PatientRequestMessageDTO getRequestById(String requestId) {
        Optional<PatientRequest> request = repository.findByRequestId(requestId);
        return request.map(this::convertToDTO).orElse(null);
    }

    /**
     * Récupère les demandes filtrées par statut.
     * 
     * @param status Le statut à filtrer
     * @return Liste des demandes correspondant au statut
     */
    public List<PatientRequestMessageDTO> getRequestsByStatus(String status) {
        return repository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les demandes filtrées par provider (targetProviderId ou providerId).
     * 
     * @param providerId L'ID du provider
     * @return Liste des demandes destinées à ce provider ou traitées par ce provider
     */
    public List<PatientRequestMessageDTO> getRequestsByProviderId(String providerId) {
        return repository.findByTargetProviderIdOrProviderId(providerId, providerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère uniquement les demandes destinées à un provider spécifique (targetProviderId).
     * 
     * @param providerId L'ID du provider cible
     * @return Liste des demandes destinées à ce provider (pas celles qu'il a traitées)
     */
    public List<PatientRequestMessageDTO> getRequestsTargetedToProvider(String providerId) {
        return repository.findByTargetProviderId(providerId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les demandes d'un patient.
     * 
     * @param patientId L'ID du patient
     * @return Liste des demandes du patient
     */
    public List<PatientRequestMessageDTO> getRequestsByPatientId(String patientId) {
        return repository.findByPatientId(patientId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Met à jour le statut d'une demande et envoie la réponse au patient.
     * 
     * @param requestId L'ID de la demande
     * @param status Le nouveau statut
     * @param responseMessage Le message de réponse
     * @param providerId L'ID du provider qui répond
     * @param providerName Le nom du provider
     * @return La demande mise à jour
     */
    public PatientRequestMessageDTO updateRequestStatus(
            String requestId,
            String status,
            String responseMessage,
            String providerId,
            String providerName) {
        
        Optional<PatientRequest> requestOpt = repository.findByRequestId(requestId);
        if (requestOpt.isEmpty()) {
            log.warn("⚠️ Demande non trouvée : {}", requestId);
            return null;
        }
        
        PatientRequest request = requestOpt.get();
        request.setStatus(status);
        request.setProviderId(providerId);
        request.setProviderName(providerName);
        request.setResponseMessage(responseMessage);
        request.setResponseDate(LocalDateTime.now());
        request.setUpdatedAt(LocalDateTime.now());
        
        PatientRequest updated = repository.save(request);
        log.info("✅ Statut de la demande {} mis à jour : {}", requestId, status);
        
        // Publier la réponse vers Patient-Service via RabbitMQ
        publishResponseToPatient(updated);
        
        return convertToDTO(updated);
    }

    /**
     * Ajoute un message à une demande.
     * 
     * @param requestId L'ID de la demande
     * @param senderId L'ID de l'expéditeur
     * @param senderType Le type d'expéditeur (PATIENT ou PROVIDER)
     * @param content Le contenu du message
     * @return La demande mise à jour
     */
    public PatientRequestMessageDTO addMessage(
            String requestId,
            String senderId,
            String senderType,
            String content) {
        
        Optional<PatientRequest> requestOpt = repository.findByRequestId(requestId);
        if (requestOpt.isEmpty()) {
            log.warn("⚠️ Demande non trouvée : {}", requestId);
            return null;
        }
        
        PatientRequest request = requestOpt.get();
        PatientRequest.RequestMessage message = new PatientRequest.RequestMessage();
        message.setSenderId(senderId);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        
        request.getMessages().add(message);
        request.setUpdatedAt(LocalDateTime.now());
        
        PatientRequest updated = repository.save(request);
        log.info("✅ Message ajouté à la demande {}", requestId);
        
        return convertToDTO(updated);
    }

    /**
     * Publie la réponse vers Patient-Service via RabbitMQ.
     * 
     * @param request La demande avec la réponse
     */
    private void publishResponseToPatient(PatientRequest request) {
        try {
            RequestResponseDTO response = new RequestResponseDTO(
                    request.getRequestId(),
                    request.getPatientId(), // ✅ Inclure patientId pour la notification
                    request.getStatus(),
                    request.getResponseMessage() != null ? request.getResponseMessage() : request.getStatus(),
                    request.getProviderId(),
                    request.getProviderName()
            );
            
            rabbitTemplate.convertAndSend(
                    REQUEST_RESPONSES_EXCHANGE,
                    REQUEST_RESPONSES_ROUTING_KEY,
                    response
            );
            
            log.info("✅ Réponse publiée vers Patient-Service pour la demande {} - Patient: {}", 
                    request.getRequestId(), request.getPatientId());
        } catch (Exception e) {
            log.error("❌ Erreur lors de la publication de la réponse : {}", e.getMessage(), e);
        }
    }

    /**
     * Convertit un DTO en entité.
     */
    private PatientRequest convertToEntity(PatientRequestMessageDTO dto) {
        PatientRequest request = new PatientRequest();
        request.setRequestId(dto.getRequestId());
        request.setPatientId(dto.getPatientId());
        request.setPatientEmail(dto.getPatientEmail());
        request.setPatientName(dto.getPatientName());
        request.setType(dto.getType());
        request.setPriority(dto.getPriority());
        request.setSubject(dto.getSubject());
        request.setDescription(dto.getDescription());
        request.setPreferredDate(dto.getPreferredDate());
        request.setStatus(dto.getStatus());
        request.setTargetProviderId(dto.getTargetProviderId());
        request.setProviderId(dto.getProviderId());
        request.setProviderName(dto.getProviderName());
        request.setResponseMessage(dto.getResponseMessage());
        request.setResponseDate(dto.getResponseDate());
        request.setMetadata(dto.getMetadata());
        return request;
    }

    /**
     * Convertit une entité en DTO.
     * Enrichit automatiquement les données manquantes depuis Patient-Service et Provider-Service.
     */
    private PatientRequestMessageDTO convertToDTO(PatientRequest request) {
        PatientRequestMessageDTO dto = new PatientRequestMessageDTO();
        dto.setRequestId(request.getRequestId());
        dto.setPatientId(request.getPatientId());
        
        // Enrichir les informations patient si manquantes
        String patientEmail = request.getPatientEmail();
        String patientName = request.getPatientName();
        if ((patientEmail == null || patientName == null) && request.getPatientId() != null) {
            DataEnrichmentService.PatientInfo patientInfo = enrichmentService.getPatientInfo(request.getPatientId());
            if (patientInfo != null) {
                if (patientEmail == null) {
                    patientEmail = patientInfo.getEmail();
                }
                if (patientName == null) {
                    patientName = patientInfo.getName();
                }
            }
        }
        dto.setPatientEmail(patientEmail);
        dto.setPatientName(patientName);
        
        dto.setType(request.getType());
        dto.setPriority(request.getPriority());
        dto.setSubject(request.getSubject());
        dto.setDescription(request.getDescription());
        dto.setPreferredDate(request.getPreferredDate());
        dto.setStatus(request.getStatus());
        dto.setTargetProviderId(request.getTargetProviderId());
        dto.setProviderId(request.getProviderId());
        
        // Enrichir le nom du provider si manquant
        String providerName = request.getProviderName();
        if (providerName == null && request.getProviderId() != null) {
            providerName = enrichmentService.getProviderName(request.getProviderId());
        }
        dto.setProviderName(providerName);
        
        dto.setResponseMessage(request.getResponseMessage());
        dto.setResponseDate(request.getResponseDate());
        dto.setMetadata(request.getMetadata());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        
        // Convertir les messages
        if (request.getMessages() != null) {
            dto.setMessages(request.getMessages().stream()
                    .map(msg -> new PatientRequestMessageDTO.MessageDTO(
                            msg.getSenderId(),
                            msg.getSenderType(),
                            msg.getContent(),
                            msg.getTimestamp()
                    ))
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
}

