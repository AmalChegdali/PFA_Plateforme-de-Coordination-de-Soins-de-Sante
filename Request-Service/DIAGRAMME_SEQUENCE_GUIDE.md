# Guide : Diagramme de Séquence pour Request-Service

## Vue d'ensemble

Le Request-Service gère les demandes de patients et les interactions avec les providers. Il communique via :
- **RabbitMQ** : pour recevoir les demandes depuis Patient-Service et envoyer les réponses
- **REST API** : pour que les providers consultent et répondent aux demandes
- **MongoDB** : pour stocker les demandes

---

## Scénarios à modéliser

### 1. Réception d'une demande depuis Patient-Service
### 2. Consultation des demandes par un Provider
### 3. Réponse à une demande par un Provider
### 4. Ajout d'un message à une demande

---

## Étape 1 : Identifier les acteurs et composants

### Acteurs externes :
- **Patient-Service** : Envoie les demandes via RabbitMQ
- **Provider** : Utilise l'API REST pour consulter/répondre

### Composants internes :
- **RequestListener** : Écoute les messages RabbitMQ
- **RequestController** : Contrôleur REST
- **PatientRequestService** : Logique métier
- **PatientRequestRepository** : Accès à MongoDB
- **RabbitMQ** : Message broker
- **MongoDB** : Base de données

---

## Étape 2 : Scénario 1 - Réception d'une demande

### Participants (de gauche à droite) :
```
Patient-Service | RabbitMQ | RequestListener | PatientRequestService | PatientRequestRepository | MongoDB
```

### Séquence :
1. **Patient-Service** → **RabbitMQ** : `publish(PatientRequestMessageDTO)` 
   - Exchange: `patient.requests.exchange`
   - Routing Key: `patient.requests.key`
   - Queue: `patient.requests.queue`

2. **RabbitMQ** → **RequestListener** : `handlePatientRequest(Map<String, Object>)`
   - Message reçu dans la queue

3. **RequestListener** : Convertit le Map en `PatientRequestMessageDTO`

4. **RequestListener** → **PatientRequestService** : `createRequest(PatientRequestMessageDTO)`

5. **PatientRequestService** : Convertit DTO → Entity (`PatientRequest`)
   - Définit `status = "EN_ATTENTE"`
   - Définit `createdAt` et `updatedAt`

6. **PatientRequestService** → **PatientRequestRepository** : `save(PatientRequest)`

7. **PatientRequestRepository** → **MongoDB** : `insert(PatientRequest)`

8. **MongoDB** → **PatientRequestRepository** : `PatientRequest` (sauvegardé)

9. **PatientRequestRepository** → **PatientRequestService** : `PatientRequest` (retourné)

10. **PatientRequestService** → **RequestListener** : `PatientRequest` (confirmé)

---

## Étape 3 : Scénario 2 - Consultation des demandes

### Participants :
```
Provider | RequestController | PatientRequestService | PatientRequestRepository | MongoDB
```

### Cas d'usage : GET /api/requests

1. **Provider** → **RequestController** : `GET /api/requests` (avec JWT token)

2. **RequestController** : Valide l'authentification JWT
   - Vérifie le rôle `PROVIDER`

3. **RequestController** → **PatientRequestService** : `getAllRequests()`

4. **PatientRequestService** → **PatientRequestRepository** : `findAll()`

5. **PatientRequestRepository** → **MongoDB** : `find({})`

6. **MongoDB** → **PatientRequestRepository** : `List<PatientRequest>`

7. **PatientRequestRepository** → **PatientRequestService** : `List<PatientRequest>`

8. **PatientRequestService** : Convertit Entity → DTO (`List<PatientRequestMessageDTO>`)

9. **PatientRequestService** → **RequestController** : `List<PatientRequestMessageDTO>`

10. **RequestController** → **Provider** : `ResponseEntity.ok(requests)`

### Variantes :
- **GET /api/requests/{requestId}** : `getRequestById(requestId)`
- **GET /api/requests/status/{status}** : `getRequestsByStatus(status)`
- **GET /api/requests/provider/{providerId}** : `getRequestsByProviderId(providerId)`
- **GET /api/requests/patient/{patientId}** : `getRequestsByPatientId(patientId)`

---

## Étape 4 : Scénario 3 - Réponse à une demande

### Participants :
```
Provider | RequestController | PatientRequestService | PatientRequestRepository | MongoDB | RabbitMQ | Patient-Service
```

### Séquence :

1. **Provider** → **RequestController** : `PUT /api/requests/{requestId}/respond`
   - Body: `{"status": "TRAITÉ", "responseMessage": "..."}`
   - JWT token dans header

2. **RequestController** : 
   - Valide JWT
   - Extrait `providerId` et `providerName` du JWT
   - Valide le body (status requis)

3. **RequestController** → **PatientRequestService** : `updateRequestStatus(requestId, status, responseMessage, providerId, providerName)`

4. **PatientRequestService** → **PatientRequestRepository** : `findByRequestId(requestId)`

5. **PatientRequestRepository** → **MongoDB** : `findOne({requestId: ...})`

6. **MongoDB** → **PatientRequestRepository** : `Optional<PatientRequest>`

7. **PatientRequestRepository** → **PatientRequestService** : `Optional<PatientRequest>`

8. **PatientRequestService** : 
   - Met à jour : `status`, `providerId`, `providerName`, `responseMessage`, `responseDate`, `updatedAt`

9. **PatientRequestService** → **PatientRequestRepository** : `save(PatientRequest)`

10. **PatientRequestRepository** → **MongoDB** : `update(PatientRequest)`

11. **MongoDB** → **PatientRequestRepository** : `PatientRequest` (mis à jour)

12. **PatientRequestRepository** → **PatientRequestService** : `PatientRequest`

13. **PatientRequestService** : Convertit Entity → DTO

14. **PatientRequestService** → **RabbitMQ** : `publishResponseToPatient(PatientRequest)`
   - Crée `RequestResponseDTO(requestId, status, responseMessage)`
   - Exchange: `request.responses.exchange`
   - Routing Key: `request.responses.key`

15. **RabbitMQ** → **Patient-Service** : Message de réponse

16. **PatientRequestService** → **RequestController** : `PatientRequestMessageDTO`

17. **RequestController** → **Provider** : `ResponseEntity.ok(updated)`

---

## Étape 5 : Scénario 4 - Ajout d'un message

### Participants :
```
Provider | RequestController | PatientRequestService | PatientRequestRepository | MongoDB
```

### Séquence :

1. **Provider** → **RequestController** : `POST /api/requests/{requestId}/messages`
   - Body: `{"content": "Message du provider"}`
   - JWT token

2. **RequestController** : 
   - Valide JWT
   - Extrait `providerId` du JWT
   - Valide le body (content requis)

3. **RequestController** → **PatientRequestService** : `addMessage(requestId, providerId, "PROVIDER", content)`

4. **PatientRequestService** → **PatientRequestRepository** : `findByRequestId(requestId)`

5. **PatientRequestRepository** → **MongoDB** : `findOne({requestId: ...})`

6. **MongoDB** → **PatientRequestRepository** : `Optional<PatientRequest>`

7. **PatientRequestRepository** → **PatientRequestService** : `Optional<PatientRequest>`

8. **PatientRequestService** : 
   - Crée un nouveau `RequestMessage`
   - Ajoute au `List<RequestMessage>` de la demande
   - Met à jour `updatedAt`

9. **PatientRequestService** → **PatientRequestRepository** : `save(PatientRequest)`

10. **PatientRequestRepository** → **MongoDB** : `update(PatientRequest)`

11. **MongoDB** → **PatientRequestRepository** : `PatientRequest` (mis à jour)

12. **PatientRequestRepository** → **PatientRequestService** : `PatientRequest`

13. **PatientRequestService** : Convertit Entity → DTO

14. **PatientRequestService** → **RequestController** : `PatientRequestMessageDTO`

15. **RequestController** → **Provider** : `ResponseEntity.ok(updated)`

---

## Étape 6 : Outils recommandés

### Pour créer le diagramme :

1. **PlantUML** (recommandé)
   - Syntaxe textuelle
   - Intégration avec documentation
   - Exemple fourni ci-dessous

2. **Draw.io / diagrams.net**
   - Interface graphique
   - Export en PNG/SVG

3. **Lucidchart**
   - Outil en ligne
   - Collaboration

4. **Visual Paradigm**
   - Outil professionnel
   - Support UML complet

---

## Étape 7 : Template PlantUML

### Scénario 1 - Réception d'une demande

```plantuml
@startuml Réception_Demande
participant "Patient-Service" as PS
participant "RabbitMQ" as MQ
participant "RequestListener" as RL
participant "PatientRequestService" as PRS
participant "PatientRequestRepository" as REPO
database "MongoDB" as DB

PS -> MQ: publish(PatientRequestMessageDTO)\nExchange: patient.requests.exchange
activate MQ
MQ -> RL: handlePatientRequest(Map)
activate RL
RL: convertToRequestDTO(Map)
RL -> PRS: createRequest(PatientRequestMessageDTO)
activate PRS
PRS: convertToEntity(DTO)\nsetStatus("EN_ATTENTE")
PRS -> REPO: save(PatientRequest)
activate REPO
REPO -> DB: insert(PatientRequest)
activate DB
DB --> REPO: PatientRequest
deactivate DB
REPO --> PRS: PatientRequest
deactivate REPO
PRS --> RL: PatientRequest
deactivate PRS
RL: log success
deactivate RL
deactivate MQ
@enduml
```

### Scénario 3 - Réponse à une demande

```plantuml
@startuml Réponse_Demande
participant "Provider" as P
participant "RequestController" as RC
participant "PatientRequestService" as PRS
participant "PatientRequestRepository" as REPO
database "MongoDB" as DB
participant "RabbitMQ" as MQ
participant "Patient-Service" as PS

P -> RC: PUT /api/requests/{id}/respond\n{status, responseMessage}\n+ JWT
activate RC
RC: validate JWT\nextract providerId
RC -> PRS: updateRequestStatus(id, status, msg, providerId, name)
activate PRS
PRS -> REPO: findByRequestId(id)
activate REPO
REPO -> DB: findOne({requestId})
activate DB
DB --> REPO: Optional<PatientRequest>
deactivate DB
REPO --> PRS: Optional<PatientRequest>
deactivate REPO
PRS: update fields\n(status, providerId, responseMessage, etc.)
PRS -> REPO: save(PatientRequest)
activate REPO
REPO -> DB: update(PatientRequest)
activate DB
DB --> REPO: PatientRequest
deactivate DB
REPO --> PRS: PatientRequest
deactivate REPO
PRS -> MQ: publishResponseToPatient()\nRequestResponseDTO
activate MQ
MQ -> PS: RequestResponseDTO
deactivate MQ
PRS: convertToDTO()
PRS --> RC: PatientRequestMessageDTO
deactivate PRS
RC --> P: ResponseEntity.ok(updated)
deactivate RC
@enduml
```

---

## Étape 8 : Checklist de validation

Avant de finaliser votre diagramme, vérifiez :

- [ ] Tous les acteurs sont identifiés
- [ ] Tous les messages sont nommés avec les méthodes réelles
- [ ] Les activations (lifelines) sont correctement représentées
- [ ] Les retours de méthodes sont indiqués (flèches en pointillés)
- [ ] Les conditions (alt, opt, loop) sont ajoutées si nécessaire
- [ ] Les notes explicatives sont ajoutées pour les points complexes
- [ ] Les noms des exchanges/queues RabbitMQ sont corrects
- [ ] Les endpoints REST sont précisés
- [ ] Les validations (JWT, body) sont représentées

---

## Étape 9 : Améliorations possibles

### Ajouter des alternatives (alt) :
```plantuml
alt Demande trouvée
    PRS -> REPO: save()
else Demande non trouvée
    PRS --> RC: null
    RC --> P: 404 Not Found
end
```

### Ajouter des boucles (loop) :
```plantuml
loop Pour chaque demande
    PRS: convertToDTO()
end
```

### Ajouter des notes :
```plantuml
note right of PRS
    Le statut est défini à "EN_ATTENTE"
    par défaut lors de la création
end note
```

---

## Étape 10 : Documentation finale

Une fois le diagramme créé :

1. **Ajoutez-le au README** du Request-Service
2. **Incluez les scénarios d'erreur** (404, 401, 403)
3. **Documentez les formats de messages** RabbitMQ
4. **Ajoutez des exemples de payloads** JSON
5. **Créez un glossaire** des termes techniques

---

## Ressources supplémentaires

- **PlantUML Documentation** : https://plantuml.com/sequence-diagram
- **UML Sequence Diagrams** : https://www.uml-diagrams.org/sequence-diagrams.html
- **RabbitMQ Patterns** : https://www.rabbitmq.com/getstarted.html

---

## Exemple complet - Scénario combiné

Pour un diagramme complet montrant le cycle de vie d'une demande :

1. Patient crée une demande → Patient-Service
2. Patient-Service publie → RabbitMQ
3. Request-Service reçoit → Crée en MongoDB
4. Provider consulte → Via API REST
5. Provider répond → Met à jour MongoDB + Publie réponse
6. Patient-Service reçoit la réponse → Notifie le patient

---

**Bon travail ! 🎯**


