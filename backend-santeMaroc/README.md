# Plateforme de Coordination de Soins de Santé

## 🎯 Vue d'ensemble

Plateforme centralisée permettant une meilleure interaction entre patients et prestataires de santé, avec gestion unifiée des dossiers médicaux, système de demandes/réponses avec notifications, et génération de certificats médicaux en PDF.

### Objectifs
- Communication fluide patient–médecin
- Coordination entre acteurs de santé
- Gestion sécurisée des dossiers médicaux
- Système de demandes/réponses avec notifications
- Différenciation des patients par provider
- Génération de certificats médicaux en PDF

---

## 🏗️ Architecture

### Architecture microservices

```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway (8080)                       │
│              Spring Cloud Gateway + Eureka                    │
└──────────────────────┬──────────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┬──────────────┐
        │              │              │              │
        ▼              ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│   Patient    │ │   Provider   │ │   Medical    │ │   Request    │
│   Service    │ │   Service    │ │   Record     │ │   Service    │
│   (8081)     │ │   (8082)     │ │   Service    │ │   (8084)     │
│              │ │              │ │   (8083)     │ │              │
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘ └──────┬───────┘
       │                │                │                │
       └────────────────┼────────────────┼────────────────┘
                        │                │
            ┌───────────┴───────────┐    │
            │                       │    │
            ▼                       ▼    ▼
    ┌──────────────┐       ┌──────────────┐
    │   MongoDB     │       │   RabbitMQ    │
    │  (27017)      │       │   (5672)      │
    └──────────────┘       └──────────────┘
            │                       │
            ▼                       ▼
    ┌──────────────┐       ┌──────────────┐
    │ Eureka Server │       │ Config Server │
    │   (8761)      │       │   (8888)      │
    └──────────────┘       └──────────────┘
```

### Composants principaux

1. **API Gateway** (8080) : Point d'entrée unique
2. **Eureka Server** (8761) : Service discovery
3. **Config Server** (8888) : Configuration centralisée
4. **Patient-Service** (8081) : Gestion patients, authentification, notifications
5. **Provider-Service** (8082) : Gestion prestataires, assignation patients
6. **MedicalRecord-Service** (8083) : Gestion dossiers médicaux
7. **Request-Service** (8084) : Demandes, réponses, certificats
8. **RabbitMQ** (5672) : Communication asynchrone
9. **MongoDB** (27017) : Base de données NoSQL

---

## 🔧 Microservices

### 1. Eureka Server (8761)
**Rôle :** Service discovery et registry

- **Console :** http://localhost:8761
- Enregistrement automatique des microservices
- Load balancing via Gateway

**Configuration minimale :**
```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
```

### 2. Config Server (8888)
**Rôle :** Configuration centralisée (Spring Cloud Config)

- **Mode :** Native (filesystem)
- **Dépôt :** `Config-server/config-repo/`
- **Accès :** http://localhost:8888/{application}/{profile}

**Configuration client (`bootstrap.properties`) :**
```properties
spring.application.name=patient-service
spring.cloud.config.uri=http://localhost:8888
spring.cloud.config.enabled=true
```

### 3. API Gateway (8080)
**Rôle :** Point d'entrée unique (Spring Cloud Gateway)

**Routes :**
- `/api/patient/**`, `/api/auth/**`, `/api/requests/**`, `/api/notifications/**` → `lb://patient-service`
- `/api/providers/**`, `/api/provider/**` → `lb://provider-service`
- `/api/records/**` → `lb://medicalrecord-service`
- `/api/requests/**`, `/api/certificates/**` → `lb://request-service`

### 4. Patient-Service (8081)
**Rôle :** Gestion patients, authentification, notifications email

**Technologies :** Spring Boot 3.2.1, Spring Security, MongoDB, RabbitMQ, JWT, Swagger, Spring Mail

**Endpoints principaux :**
- **Authentification :** `POST /api/auth/register`, `POST /api/auth/login`
- **Profil :** `GET /api/patient/profile`, `PUT /api/patient/complete-profile`
- **Demandes :** `POST /api/requests` (compte ACTIVE requis)
- **Notifications :** `GET /api/notifications` (compte ACTIVE requis)

**RabbitMQ :**
- Publie : `patient-exchange` (routing: `patient.sync.request`), `request-exchange` (routing: `patient.request.created`)
- Écoute : `notification.queue`

**Swagger :** http://localhost:8081/swagger-ui/index.html

### 5. Provider-Service (8082)
**Rôle :** Gestion prestataires, assignation patients, création dossiers médicaux

**Technologies :** Spring Boot 3.5.5, Spring Security, MongoDB, RabbitMQ, JWT, Swagger

**Endpoints principaux :**
- **Authentification :** `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/providers/list`
- **Patients :** 
  - `GET /api/providers/patients/all` (tous)
  - `GET /api/providers/patients/assigned` (mes patients)
  - `GET /api/providers/patients/unassigned` (non assignés)
  - `POST /api/providers/patients/{id}/assign` (assigner)
  - `DELETE /api/providers/patients/{id}/assign` (désassigner)
- **Dossiers médicaux :** `POST /api/providers/medical-records`

**RabbitMQ :**
- Écoute : `patient.sync.queue`
- Publie : `medical-record-exchange` (routing: `medical.record.create`)

**Swagger :** http://localhost:8082/swagger-ui/index.html

### 6. MedicalRecord-Service (8083)
**Rôle :** Gestion dossiers médicaux

**Technologies :** Spring Boot 3.2.4, Spring Security (OAuth2), MongoDB, RabbitMQ, Swagger

**Endpoints principaux :**
- **Lecture :** `GET /api/records/read/patient/{id}`, `GET /api/records/read/search`
- **CRUD :** `GET /api/records`, `GET /api/records/{id}`, `PUT /api/records/{id}` (PROVIDER), `DELETE /api/records/{id}` (PROVIDER)

**Note :** Création uniquement via RabbitMQ depuis Provider-Service

**RabbitMQ :** Écoute `medical-record.queue`

**Swagger :** http://localhost:8083/swagger-ui/index.html

### 7. Request-Service (8084)
**Rôle :** Gestion demandes, réponses, certificats médicaux

**Technologies :** Spring Boot 3.2.1, Spring Security, MongoDB, RabbitMQ, JWT, Swagger, iText 7

**Endpoints principaux :**
- **Patients :** `GET /api/requests/patient/{id}` (ses propres demandes)
- **Providers :** 
  - `GET /api/requests` (toutes)
  - `GET /api/requests/status/{status}`
  - `GET /api/requests/provider/{id}/targeted` (demandes ciblées)
  - `PUT /api/requests/{id}/respond` (répondre)
- **Certificats :** `GET /api/certificates/{id}`, `GET /api/certificates/{id}/print` (génération PDF)

**RabbitMQ :**
- Écoute : `request.queue`
- Publie : `notification-exchange` (routing: `request.response`)

**Swagger :** http://localhost:8084/swagger-ui/index.html

---

## ⭐ Fonctionnalités principales

### 1. Système d'Assignation des Patients
- Chaque patient a un champ `assignedProviderId` (`null` = non assigné)
- Les providers peuvent assigner/désassigner des patients
- Visibilité globale avec filtrage par assignation

### 2. Système de Demandes et Réponses
1. Patient soumet une demande (peut cibler un provider via `targetProviderId`)
2. Demande envoyée à Request-Service via RabbitMQ
3. Provider répond via API
4. Réponse envoyée au patient via RabbitMQ
5. Patient reçoit notification (email + cache)

**Types :** Consultation, Suivi médical, Prescription, Certificat médical, Autre

### 3. Notifications par Email
- Notifications automatiques par email lors des réponses
- Cache en mémoire pour consultation via API
- Configuration : `Patient-Service/CONFIGURATION_EMAIL.md`

### 4. Génération de Certificats PDF
- Génération PDF professionnel avec iText 7
- Contient : patient, provider, contenu, dates, signature
- Sécurité : patients voient uniquement leurs certificats

### 5. Service Discovery avec Eureka
- Enregistrement automatique des microservices
- Découverte dynamique via Gateway
- Load balancing automatique
- Dashboard : http://localhost:8761

---

## 🛠️ Technologies

### Backend
- **Framework :** Spring Boot 3.x
- **Langage :** Java 17
- **Build :** Maven
- **API Docs :** Swagger/OpenAPI 3

### Sécurité
- **Authentification :** JWT (HS256, expiration 24h)
- **Autorisation :** Spring Security avec RBAC
- **Claims :** `patientId` (Patient), `providerId` (Provider)

### Base de données
- **MongoDB :** `mongodb://localhost:27017/MaBase`

### Communication
- **RabbitMQ :** Communication asynchrone (port 5672, Management 15672)
- **Eureka :** Service discovery (port 8761)
- **Config Server :** Configuration centralisée (port 8888)
- **Gateway :** Spring Cloud Gateway avec Eureka

### Autres
- **PDF :** iText 7 (certificats)
- **Email :** Spring Mail (SMTP Gmail)
- **Outils :** Lombok, Jackson, SLF4J

---

## 🚀 Installation et démarrage

### Prérequis
- Java 17+
- Maven 3.6+
- MongoDB (port 27017)
- Docker & Docker Compose (RabbitMQ)
- Compte Gmail avec mot de passe d'application (notifications)

### Étapes

#### 1. Cloner le projet
```bash
git clone <repository-url>
cd Platefome_Sois_Sante
```

#### 2. Démarrer RabbitMQ
```bash
cd docker
docker-compose up -d
```
**Vérifier :** http://localhost:15672 (guest/guest)

#### 3. Démarrer MongoDB
**Windows :** `mongod`  
**Linux/Mac :** `sudo systemctl start mongod`

#### 4. Configurer l'email (optionnel)
Voir : `Patient-Service/CONFIGURATION_EMAIL.md`

#### 5. Démarrer les microservices (ordre recommandé)

1. **Eureka Server** (8761)
```bash
cd Eureka-Server && mvn spring-boot:run
```

2. **Config Server** (8888)
```bash
cd Config-server && mvn spring-boot:run
```

3. **Patient-Service** (8081)
```bash
cd Patient-Service && mvn spring-boot:run
```

4. **Provider-Service** (8082)
```bash
cd Provider-Service && mvn spring-boot:run
```

5. **MedicalRecord-Service** (8083)
```bash
cd Medicalrecord-Service && mvn spring-boot:run
```

6. **Request-Service** (8084)
```bash
cd Request-Service && mvn spring-boot:run
```

7. **Gateway-Service** (8080)
```bash
cd Gateway-Service && mvn spring-boot:run
```

### Vérification
- **Eureka :** http://localhost:8761
- **Config Server :** http://localhost:8888/patient-service/default
- **Swagger UI :** 
  - Patient: http://localhost:8081/swagger-ui/index.html
  - Provider: http://localhost:8082/swagger-ui/index.html
  - MedicalRecord: http://localhost:8083/swagger-ui/index.html
  - Request: http://localhost:8084/swagger-ui/index.html
- **RabbitMQ :** http://localhost:15672

---

## 📡 Communication RabbitMQ

### Exchanges et Queues

| Exchange | Type | Description |
|----------|------|-------------|
| `patient-exchange` | Topic | Synchronisation patients |
| `request-exchange` | Topic | Demandes de patients |
| `notification-exchange` | Topic | Notifications et réponses |
| `medical-record-exchange` | Topic | Dossiers médicaux |

| Queue | Description | Direction |
|-------|-------------|-----------|
| `patient.sync.queue` | Synchronisation patients | Patient → Provider |
| `request.queue` | Demandes de patients | Patient → Request |
| `notification.queue` | Notifications | Request → Patient |
| `medical-record.queue` | Création dossiers | Provider → MedicalRecord |

### Routing Keys
- `patient.sync.request` : Nouveau patient → Provider
- `patient.request.created` : Nouvelle demande → Request
- `request.response` : Réponse → Patient
- `medical.record.create` : Création dossier → MedicalRecord

### Flux principaux

**Inscription patient :**
```
Patient-Service → patient-exchange (patient.sync.request) 
→ Provider-Service (patient.sync.queue)
```

**Soumission demande :**
```
Patient-Service → request-exchange (patient.request.created) 
→ Request-Service (request.queue)
```

**Réponse provider :**
```
Request-Service → notification-exchange (request.response) 
→ Patient-Service (notification.queue) → Email + Cache
```

**Création dossier médical :**
```
Provider-Service → medical-record-exchange (medical.record.create) 
→ MedicalRecord-Service (medical-record.queue)
```

---

## ⚙️ Configuration

### Variables importantes

**JWT :**
- Secret : `mySecretKey123456789012345678901234567890`
- Expiration : 86400000 ms (24h)
- Algorithme : HS256

**MongoDB :**
- URI : `mongodb://localhost:27017/MaBase`
- Port : 27017

**RabbitMQ :**
- Host : localhost
- Port : 5672
- Management : http://localhost:15672 (guest/guest)

**Eureka :**
- URL : http://localhost:8761/eureka/
- Dashboard : http://localhost:8761

**Config Server :**
- URL : http://localhost:8888
- Dépôt : `Config-server/config-repo/`

**Email :**
- SMTP : smtp.gmail.com:587
- Guide : `Patient-Service/CONFIGURATION_EMAIL.md`

### Fichiers de configuration
- **Centralisée :** `Config-server/config-repo/*.properties`
- **Bootstrap :** `*/src/main/resources/bootstrap.properties`
- **Application :** `*/src/main/resources/application.properties`

---

## 🔒 Sécurité

### Authentification
- **Méthode :** JWT (Bearer Token)
- **Algorithme :** HS256
- **Expiration :** 24 heures

### Autorisation

**Rôles :**
- **PATIENT :** Accès fonctionnalités patient
- **PROVIDER :** Accès fonctionnalités provider + gestion patients

**Endpoints sécurisés :**

| Service | Endpoint | Rôle requis |
|---------|----------|-------------|
| Patient-Service | `/api/patient/**` | PATIENT |
| Patient-Service | `/api/requests/**`, `/api/notifications/**` | PATIENT (ACTIVE) |
| Provider-Service | `/api/providers/**` | PROVIDER |
| Request-Service | `/api/requests/**` | PROVIDER (sauf GET patient) |
| Request-Service | `/api/certificates/**` | PATIENT ou PROVIDER |
| MedicalRecord-Service | PUT/DELETE `/api/records/**` | PROVIDER |

**Statuts patient :**
- Compte **ACTIVE** requis pour : historique médical, demandes, notifications
- Sinon : **403 Forbidden**

**Vérification :**
- Patients : `patientId` dans JWT
- Providers : `providerId` dans JWT
- Assignation : provider ne peut désassigner que ses propres patients

**Exemple d'utilisation :**
```bash
curl -X GET http://localhost:8080/api/patient/profile \
  -H "Authorization: Bearer <jwt-token>"
```

---
