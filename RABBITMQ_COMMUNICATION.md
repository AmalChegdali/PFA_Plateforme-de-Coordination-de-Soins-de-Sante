# Communication RabbitMQ entre Patient-Service et Provider-Service

## 📋 Vue d'ensemble

La communication entre les microservices Patient et Provider se fait via RabbitMQ en utilisant un **Topic Exchange** nommé `patient-exchange`.

## 🔄 Flux de communication

### 1. Publication d'un nouveau patient (Patient-Service → Provider-Service)

**Quand :** Lorsqu'un nouveau patient s'inscrit via `/api/auth/register`

**Service émetteur :** `Patient-Service`
- **Classe :** `PatientPublisherService`
- **Méthode :** `publishPatient(PatientDTO patientDTO)`
- **Exchange :** `patient-exchange`
- **Routing Key :** `patient.sync.request`
- **Queue cible :** `patient.sync.queue`

**Service récepteur :** `Provider-Service`
- **Classe :** `ProviderPatientService`
- **Méthode :** `receivePatientFromQueue(PatientDTO patient)`
- **Queue :** `patient.sync.queue`

**Format du message :**
```json
{
  "id": "patient-id",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "accountStatus": "PENDING",
  ...
}
```

## ⚙️ Configuration

### Patient-Service (`Patient-Service/src/main/java/com/patient_service/config/RabbitConfig.java`)

```java
// Exchange
PATIENT_EXCHANGE = "patient-exchange"

// Queues
PATIENT_SYNC_QUEUE = "patient.sync.queue"
PATIENT_STATUS_QUEUE = "patient.status.queue"
PATIENT_SYNC_RESPONSE_QUEUE = "patient.sync.response.queue"

// Routing Keys
PATIENT_SYNC_ROUTING_KEY = "patient.sync.request"
PATIENT_STATUS_ROUTING_KEY = "patient.status.update"
PATIENT_SYNC_RESPONSE_ROUTING_KEY = "patient.sync.response"
```

### Provider-Service (`Provider-Service/src/main/java/com/provider_service/config/RabbitConfig.java`)

```java
// Exchange
PATIENT_EXCHANGE = "patient-exchange"

// Queues
PATIENT_SYNC_QUEUE = "patient.sync.queue"
PATIENT_REQUESTS_QUEUE = "patient.requests.queue"

// Routing Keys
PATIENT_SYNC_ROUTING_KEY = "patient.sync.request"
```

## 🔧 Composants techniques

### Message Converter
Les deux services utilisent `Jackson2JsonMessageConverter` pour sérialiser/désérialiser les objets JSON.

### DTOs utilisés

**Patient-Service :** `com.patient_service.dto.PatientDTO`
- Contient `firstName` et `lastName` (pas de `fullName`)

**Provider-Service :** `com.provider_service.dto.PatientDTO`
- Contient `fullName`, `firstName`, et `lastName`
- Le listener construit automatiquement `fullName` à partir de `firstName` + `lastName` si nécessaire

## ✅ Vérifications effectuées

1. ✅ Configuration RabbitMQ corrigée dans Provider-Service
2. ✅ Message converter ajouté dans Provider-Service
3. ✅ Queue `patient.sync.queue` correctement configurée et bindée
4. ✅ Listener mis à jour pour gérer la conversion firstName/lastName → fullName
5. ✅ Méthode `addOrUpdatePatient` améliorée pour mettre à jour tous les champs

## 🧪 Test de la communication

Pour tester la communication :

1. **Démarrer RabbitMQ :**
   ```bash
   cd docker
   docker-compose up -d
   ```

2. **Démarrer les services :**
   - Patient-Service (port 8081)
   - Provider-Service (port 8082)

3. **Créer un nouveau patient :**
   ```bash
   POST http://localhost:8081/api/auth/register
   {
     "email": "test@example.com",
     "password": "password123",
     "firstName": "John",
     "lastName": "Doe"
   }
   ```

4. **Vérifier dans les logs :**
   - Patient-Service : `📤 Patient publié dans RabbitMQ : test@example.com`
   - Provider-Service : `✅ Patient reçu depuis RabbitMQ : test@example.com (John Doe)`

5. **Vérifier dans RabbitMQ Management UI :**
   - URL : http://localhost:15672
   - Login : guest / guest
   - Vérifier la queue `patient.sync.queue` pour les messages

## 📝 Notes importantes

- Les deux services doivent utiliser le même exchange (`patient-exchange`)
- Le routing key doit correspondre entre l'émetteur et le binding du récepteur
- Les DTOs peuvent avoir des champs différents, mais les champs communs doivent avoir les mêmes noms
- Le message converter JSON gère automatiquement la sérialisation/désérialisation

