# Plateforme de Coordination de Soins de Santé

## 📋 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture](#architecture)
3. [Microservices](#microservices)
4. [Fonctionnalités principales](#fonctionnalités-principales)
5. [Technologies utilisées](#technologies-utilisées)
6. [Installation et démarrage](#installation-et-démarrage)
7. [Documentation des API](#documentation-des-api)
8. [Communication RabbitMQ](#communication-rabbitmq)
9. [Configuration](#configuration)
10. [Sécurité](#sécurité)
11. [Tests](#tests)

---

## 🎯 Vue d'ensemble

### Contexte général

Avec la digitalisation du secteur médical, les patients recherchent des plateformes fiables pour interagir avec les professionnels de santé. Les médecins, quant à eux, ont besoin d'outils efficaces pour gérer le suivi médical et les dossiers patients.

### Problèmes identifiés

- Difficulté de communication patient–médecin
- Manque de coordination entre acteurs de santé
- Dispersion des dossiers médicaux
- Besoin de sécurisation élevée (confidentialité, permissions)
- Manque de traçabilité des demandes et réponses

### Objectifs du projet

Créer une plateforme centralisée permettant :
- Une meilleure interaction entre patients et prestataires
- Une gestion unifiée et sécurisée des dossiers médicaux
- Une communication fluide entre services via microservices
- Une architecture scalable et maintenable
- Un système de demandes/réponses avec notifications
- Une différenciation des patients par provider
- Une génération de certificats médicaux en PDF

---

## 🏗️ Architecture

### Architecture microservices

Le projet suit une architecture microservices avec les composants suivants :

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
            │                       │
            ▼                       ▼
    ┌──────────────┐
    │ Eureka Server │
    │   (8761)      │
    └──────────────┘
```

### Composants principaux

1. **API Gateway** : Point d'entrée unique pour toutes les requêtes
2. **Eureka Server** : Service discovery pour la localisation des microservices
3. **Patient-Service** : Gestion des patients, authentification, notifications
4. **Provider-Service** : Gestion des prestataires de santé et assignation des patients
5. **MedicalRecord-Service** : Gestion des dossiers médicaux
6. **Request-Service** : Gestion des demandes de patients, réponses des providers, certificats
7. **RabbitMQ** : Message broker pour la communication asynchrone
8. **MongoDB** : Base de données NoSQL pour tous les services

---

## 🔧 Microservices

### 1. Eureka Server

**Port :** 8761  
**Rôle :** Service discovery et registry pour les microservices

#### Configuration
- **Application Name :** Eureka-Server
- **Port :** 8761
- **Console Web :** http://localhost:8761

#### Fonctionnalités
- Enregistrement automatique des microservices
- Découverte de services
- Health checks
- Load balancing via Gateway

#### Services enregistrés
Tous les microservices suivants s'enregistrent automatiquement auprès d'Eureka :
- ✅ **patient-service** (port 8081)
- ✅ **provider-service** (port 8082)
- ✅ **medicalrecord-service** (port 8083)
- ✅ **request-service** (port 8084)

**Configuration minimale requise dans chaque service :**
```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
```

---

### 2. API Gateway

**Port :** 8080  
**Rôle :** Point d'entrée unique pour toutes les requêtes API

#### Configuration
- **Application Name :** api-gateway
- **Port :** 8080
- **Technologie :** Spring Cloud Gateway
- **Service Discovery :** Eureka Client (découverte automatique)

#### Routes configurées

| Service | Routes | Service Discovery |
|---------|--------|-------------------|
| Patient-Service | `/api/patient/**`, `/api/auth/**`, `/api/requests/**`, `/api/notifications/**` | `lb://patient-service` |
| Provider-Service | `/api/providers/**`, `/api/provider/**`, `/api/auth/**` | `lb://provider-service` |
| MedicalRecord-Service | `/api/records/**` | `lb://medicalrecord-service` |
| Request-Service | `/api/requests/**`, `/api/certificates/**` | `lb://request-service` |

**Avantages :**
- ✅ Découverte automatique des services via Eureka
- ✅ Load balancing automatique
- ✅ Pas besoin de configurer les URLs en dur
- ✅ Support de plusieurs instances du même service

#### Accès
- **Base URL :** http://localhost:8080
- Toutes les requêtes passent par la Gateway

---

### 3. Patient-Service

**Port :** 8081  
**Rôle :** Gestion des patients, authentification, notifications par email

#### Configuration
- **Application Name :** patient-service
- **Port :** 8081
- **Base de données :** MongoDB (mongodb://localhost:27017/MaBase)
- **JWT Secret :** `mySecretKey123456789012345678901234567890`
- **JWT Expiration :** 86400000 ms (24 heures)
- **Email SMTP :** Configuré pour Gmail (voir `CONFIGURATION_EMAIL.md`)

#### Technologies
- Spring Boot 3.2.1
- Spring Security
- MongoDB
- RabbitMQ
- JWT (JJWT 0.11.5)
- Swagger/OpenAPI
- Spring Mail (notifications email)

#### Endpoints principaux

##### Authentification (`/api/auth`)
- `POST /api/auth/register` : Inscription d'un nouveau patient
- `POST /api/auth/login` : Connexion patient

##### Profil Patient (`/api/patient`)
- `GET /api/patient/profile-status` : Statut du profil
- `GET /api/patient/profile` : Profil complet du patient
- `PUT /api/patient/complete-profile` : Compléter/Mettre à jour le profil
- `GET /api/patient/medical-history` : Historique médical (compte ACTIVE requis)

##### Demandes (`/api/requests`)
- `POST /api/requests` : Soumettre une demande (compte ACTIVE requis)
  - Permet de spécifier un `targetProviderId` pour cibler un provider spécifique
- `POST /api/requests/{requestId}/message` : Ajouter un message à une demande

##### Notifications (`/api/notifications`)
- `GET /api/notifications` : Lister toutes les notifications (compte ACTIVE requis)
- `GET /api/notifications/{requestId}` : Obtenir une notification par ID

**Fonctionnalités de notification :**
- ✅ Notifications par email automatiques lors des réponses des providers
- ✅ Cache en mémoire des notifications
- ✅ Support WebSocket (prévu pour le futur)

#### Communication RabbitMQ
- **Publie sur :** `patient-exchange` avec routing key `patient.sync.request` (synchronisation patients)
- **Publie sur :** `request-exchange` avec routing key `patient.request.created` (nouvelles demandes)
- **Écoute :** `notification.queue` (réponses aux demandes)

#### Swagger UI
- **URL :** http://localhost:8081/swagger-ui/index.html
- **API Docs :** http://localhost:8081/v3/api-docs

---

### 4. Provider-Service

**Port :** 8082  
**Rôle :** Gestion des prestataires de santé, assignation des patients, création de dossiers médicaux

#### Configuration
- **Application Name :** provider-service
- **Port :** 8082
- **Base de données :** MongoDB (mongodb://localhost:27017/MaBase)
- **JWT Secret :** `mySecretKey123456789012345678901234567890`
- **JWT Expiration :** 86400000 ms (24 heures)

#### Technologies
- Spring Boot 3.5.5
- Spring Security
- MongoDB
- RabbitMQ
- JWT (JJWT 0.11.5)
- Swagger/OpenAPI

#### Endpoints principaux

##### Authentification (`/api/auth`)
- `POST /api/auth/register` : Inscription d'un nouveau provider
- `POST /api/auth/login` : Connexion provider
- `GET /api/auth/profile` : Profil du provider connecté
- `PUT /api/auth/complete-profile` : Compléter le profil provider
- `GET /api/auth/providers/list` : Liste publique de tous les providers (pour que les patients choisissent)

##### Gestion des Patients (`/api/providers`)
- `GET /api/providers/patients/all` : Récupérer tous les patients (avec `assignedProviderId`)
- `GET /api/providers/patients` : Récupérer les patients par statut
- `GET /api/providers/patients/{patientId}` : Détails d'un patient
- `PUT /api/providers/patients/{patientId}/status` : Mettre à jour le statut d'un patient
- `POST /api/providers/patients/sync` : Synchroniser tous les patients
- `POST /api/providers/patient/{patientId}/activate` : Activer un patient
- `POST /api/providers/patient/{patientId}/suspend` : Suspendre un patient

##### Assignation des Patients (`/api/providers`)
- `GET /api/providers/patients/assigned` : **Mes patients assignés** (patients assignés au provider connecté)
- `GET /api/providers/patients/unassigned` : **Patients non assignés** (disponibles pour assignation)
- `POST /api/providers/patients/{patientId}/assign` : **Assigner un patient à moi**
- `DELETE /api/providers/patients/{patientId}/assign` : **Désassigner un patient**

**Système d'assignation :**
- Chaque patient a un champ `assignedProviderId` qui indique à quel provider il est assigné
- `null` = patient non assigné (visible par tous, disponible pour assignation)
- `"provider-id"` = patient assigné à un provider spécifique
- Tous les providers voient tous les patients, mais peuvent filtrer pour voir seulement leurs patients assignés

##### Dossiers Médicaux (`/api/providers/medical-records`)
- `POST /api/providers/medical-records` : Créer un dossier médical (via RabbitMQ vers MedicalRecord-Service)

#### Communication RabbitMQ
- **Écoute :** `patient.sync.queue` (reçoit les nouveaux patients)
- **Publie sur :** `medical-record-exchange` avec routing key `medical.record.create`
- **Exchange :** `patient-exchange`
- **Routing Key :** `patient.sync.request`

#### Swagger UI
- **URL :** http://localhost:8082/swagger-ui/index.html
- **API Docs :** http://localhost:8082/v3/api-docs

---

### 5. MedicalRecord-Service

**Port :** 8083  
**Rôle :** Gestion des dossiers médicaux

#### Configuration
- **Application Name :** medicalrecord-service
- **Port :** 8083
- **Base de données :** MongoDB (mongodb://localhost:27017/MaBase)
- **JWT Validation :** Via OAuth2 Resource Server avec clé secrète HS256

#### Technologies
- Spring Boot 3.2.4
- Spring Security (OAuth2 Resource Server)
- MongoDB
- RabbitMQ
- Swagger/OpenAPI

#### Endpoints principaux

##### Opérations de Lecture (`/api/records/read`)
- `GET /api/records/read/patient/{patientId}` : Dossiers d'un patient
- `GET /api/records/read/search` : Recherche avancée (paramètres : patientId, providerId, from, to, limit)

##### Opérations CRUD (`/api/records`)
- `GET /api/records` : Récupérer tous les dossiers
- `GET /api/records/{id}` : Récupérer un dossier par ID
- `PUT /api/records/{id}` : Mettre à jour un dossier (PROVIDER requis)
- `DELETE /api/records/{id}` : Supprimer un dossier (PROVIDER requis)

**Note :** La création de dossiers médicaux se fait **uniquement via RabbitMQ** depuis Provider-Service.

#### Communication RabbitMQ
- **Écoute :** `medical-record.queue` (reçoit les demandes de création de dossiers)

#### Swagger UI
- **URL :** http://localhost:8083/swagger-ui/index.html
- **API Docs :** http://localhost:8083/v3/api-docs

---

### 6. Request-Service

**Port :** 8084  
**Rôle :** Gestion des demandes de patients, réponses des providers, certificats médicaux

#### Configuration
- **Application Name :** request-service
- **Port :** 8084
- **Base de données :** MongoDB (mongodb://localhost:27017/MaBase)
- **JWT Secret :** `mySecretKey123456789012345678901234567890`
- **JWT Expiration :** 86400000 ms (24 heures)

#### Technologies
- Spring Boot 3.2.1
- Spring Security
- MongoDB
- RabbitMQ
- JWT (JJWT 0.11.5)
- Swagger/OpenAPI
- iText 7 (génération PDF)

#### Endpoints principaux

##### Endpoints Patients (`/api/requests`)
- `GET /api/requests/patient/{patientId}` : Récupérer les demandes d'un patient
  - **PATIENT** : Retourne uniquement ses propres demandes (vérification via JWT)
  - **PROVIDER** : Retourne toutes les demandes d'un patient spécifique

##### Endpoints Providers (`/api/requests`)
- `GET /api/requests` : Récupérer toutes les demandes (PROVIDER uniquement)
- `GET /api/requests/status/{status}` : Récupérer les demandes par statut (PROVIDER uniquement)
- `GET /api/requests/provider/{providerId}` : Récupérer les demandes d'un provider (PROVIDER uniquement)
- `GET /api/requests/provider/{providerId}/targeted` : Récupérer les demandes **destinées** à un provider (PROVIDER uniquement)
- `GET /api/requests/{requestId}` : Récupérer une demande par ID (PROVIDER uniquement)
- `PUT /api/requests/{requestId}/respond` : Répondre à une demande (PROVIDER uniquement)
- `POST /api/requests/{requestId}/messages` : Ajouter un message à une demande (PROVIDER uniquement)

**Identification des demandes ciblées :**
- Le champ `targetProviderId` indique si une demande est destinée à un provider spécifique
- Si `targetProviderId = null`, la demande est visible par tous les providers
- Si `targetProviderId = "provider-id"`, la demande est destinée à ce provider spécifique
- Le champ `providerId` indique quel provider a traité la demande (rempli lors de la réponse)

##### Certificats Médicaux (`/api/certificates`)
- `GET /api/certificates/{id}` : Récupérer un certificat par ID
- `GET /api/certificates/{id}/print` : **Générer un PDF de certificat**
  - **PATIENT** : Génère un PDF de son propre certificat
  - **PROVIDER** : Génère un PDF de n'importe quel certificat

**Fonctionnalités de certificats :**
- ✅ Génération de PDF professionnel avec iText 7
- ✅ Informations complètes (patient, provider, contenu, dates, signature)
- ✅ Téléchargement automatique du PDF
- ✅ Sécurité : Les patients ne peuvent voir que leurs propres certificats

#### Communication RabbitMQ
- **Écoute :** `request.queue` (reçoit les nouvelles demandes de patients)
- **Publie sur :** `notification-exchange` avec routing key `request.response` (envoie les réponses aux patients)

#### Swagger UI
- **URL :** http://localhost:8084/swagger-ui/index.html
- **API Docs :** http://localhost:8084/v3/api-docs

---

## ⭐ Fonctionnalités principales

### 1. Système d'Assignation des Patients

**Problème résolu :** Différencier les patients de chaque provider tout en gardant la visibilité globale.

**Fonctionnement :**
- Lorsqu'un patient s'enregistre, il est synchronisé vers **tous les providers** via RabbitMQ
- Par défaut, le patient a `assignedProviderId = null` (non assigné)
- Chaque provider peut **assigner** un patient à lui-même via `POST /api/providers/patients/{id}/assign`
- Les providers peuvent voir :
  - **Tous les patients** : `GET /api/providers/patients/all` (avec indication d'assignation)
  - **Leurs patients assignés** : `GET /api/providers/patients/assigned`
  - **Patients non assignés** : `GET /api/providers/patients/unassigned`

**Avantages :**
- ✅ Organisation claire : chaque provider gère ses propres patients
- ✅ Visibilité globale : tous les providers voient tous les patients
- ✅ Flexibilité : réassignation possible si nécessaire

### 2. Système de Demandes et Réponses

**Fonctionnement :**
1. **Patient soumet une demande** via `POST /api/requests` (Patient-Service)
   - Peut spécifier un `targetProviderId` pour cibler un provider spécifique
2. **Demande envoyée à Request-Service** via RabbitMQ
3. **Provider répond** via `PUT /api/requests/{id}/respond` (Request-Service)
4. **Réponse envoyée au patient** via RabbitMQ
5. **Patient reçoit une notification** (email + cache en mémoire)
6. **Patient consulte** via `GET /api/notifications` (Patient-Service)

**Types de demandes :**
- Consultation
- Suivi médical
- Prescription
- Certificat médical
- Autre

### 3. Notifications par Email

**Fonctionnement :**
- Lorsqu'un provider répond à une demande, le patient reçoit automatiquement un **email**
- Les notifications sont également stockées en cache pour consultation via l'API
- Configuration email dans `Patient-Service/src/main/resources/application.properties`
- Guide complet : `Patient-Service/CONFIGURATION_EMAIL.md`

**Configuration requise :**
- Compte Gmail avec mot de passe d'application
- Configuration SMTP dans `application.properties`

### 4. Génération de Certificats PDF

**Fonctionnement :**
- Les providers peuvent créer des certificats médicaux
- Les certificats peuvent être générés en PDF via `GET /api/certificates/{id}/print`
- Le PDF contient :
  - Informations du patient
  - Informations du provider
  - Contenu du certificat
  - Dates d'émission et d'expiration
  - Signature du provider

**Sécurité :**
- Les patients ne peuvent générer que leurs propres certificats
- Les providers peuvent générer tous les certificats

### 5. Service Discovery avec Eureka

**Fonctionnement :**
- Tous les microservices s'enregistrent automatiquement auprès d'Eureka
- La Gateway découvre les services dynamiquement
- Load balancing automatique si plusieurs instances sont disponibles
- Dashboard Eureka : http://localhost:8761

---

## 🛠️ Technologies utilisées

### Backend
- **Framework :** Spring Boot 3.x
- **Langage :** Java 17
- **Build Tool :** Maven
- **API Documentation :** Swagger/OpenAPI 3

### Sécurité
- **Authentification :** JWT (JSON Web Tokens)
- **Autorisation :** Spring Security avec RBAC
- **Algorithme JWT :** HS256 (HMAC)
- **Expiration JWT :** 24 heures
- **Claims JWT :** `patientId` (Patient-Service), `providerId` (Provider-Service)

### Base de données
- **Tous les services :** MongoDB (NoSQL)
  - **Port :** 27017
  - **Base de données :** MaBase
  - **URI :** mongodb://localhost:27017/MaBase

### Communication
- **Message Broker :** RabbitMQ 3-management (communication asynchrone)
- **Service Discovery :** Netflix Eureka (découverte automatique des services)
- **API Gateway :** Spring Cloud Gateway (avec intégration Eureka)
- **Load Balancing :** Intégré via Eureka et Spring Cloud Gateway

### Génération de documents
- **PDF :** iText 7 (certificats médicaux)

### Notifications
- **Email :** Spring Mail (SMTP Gmail)
- **Cache :** In-memory (ConcurrentHashMap)

### Outils de développement
- **Lombok :** Réduction du code boilerplate
- **Jackson :** Sérialisation JSON
- **SLF4J :** Logging

---

## 🚀 Installation et démarrage

### Prérequis

- Java 17 ou supérieur
- Maven 3.6+
- MongoDB (en cours d'exécution sur le port 27017)
- Docker et Docker Compose (pour RabbitMQ)
- Compte Gmail avec mot de passe d'application (pour les notifications email)

### Étapes d'installation

#### 1. Cloner le projet
```bash
git clone <repository-url>
cd Platefome_Sois_Sante
```

#### 2. Démarrer RabbitMQ avec Docker
```bash
cd docker
docker-compose up -d
```

Vérifier que RabbitMQ est démarré :
- **Management UI :** http://localhost:15672
- **Login :** guest / guest

#### 3. Démarrer MongoDB

**Windows :**
```bash
mongod
```

**Linux/Mac :**
```bash
sudo systemctl start mongod
```

**Créer la base de données (optionnel, créée automatiquement) :**
```bash
mongosh
use MaBase
```

#### 4. Configurer l'email (optionnel mais recommandé)

Voir le guide complet : `Patient-Service/CONFIGURATION_EMAIL.md

**Configuration rapide :**
1. Créer un mot de passe d'application Gmail
2. Modifier `Patient-Service/src/main/resources/application.properties`
3. Décommenter et remplir :
   ```properties
   spring.mail.username=votre-email@gmail.com
   spring.mail.password=votre-mot-de-passe-app
   ```

#### 5. Démarrer les microservices

**Ordre recommandé :**

1. **Eureka Server** (Port 8761)
```bash
cd Eureka-Server
mvn spring-boot:run
```
Vérifier : http://localhost:8761

2. **Patient-Service** (Port 8081)
```bash
cd Patient-Service
mvn spring-boot:run
```
Vérifier : http://localhost:8081/swagger-ui/index.html

3. **Provider-Service** (Port 8082)
```bash
cd Provider-Service
mvn spring-boot:run
```
Vérifier : http://localhost:8082/swagger-ui/index.html

4. **MedicalRecord-Service** (Port 8083)
```bash
cd Medicalrecord-Service
mvn spring-boot:run
```
Vérifier : http://localhost:8083/swagger-ui/index.html

5. **Request-Service** (Port 8084)
```bash
cd Request-Service
mvn spring-boot:run
```
Vérifier : http://localhost:8084/swagger-ui/index.html

6. **Gateway-Service** (Port 8080)
```bash
cd Gateway-Service
mvn spring-boot:run
```
Vérifier : http://localhost:8080

### Vérification

1. **Eureka Dashboard :** http://localhost:8761
   - Vérifier que tous les services sont enregistrés :
     - `PATIENT-SERVICE`
     - `PROVIDER-SERVICE`
     - `MEDICALRECORD-SERVICE`
     - `REQUEST-SERVICE`

2. **Swagger UI de chaque service :**
   - Patient-Service : http://localhost:8081/swagger-ui/index.html
   - Provider-Service : http://localhost:8082/swagger-ui/index.html
   - MedicalRecord-Service : http://localhost:8083/swagger-ui/index.html
   - Request-Service : http://localhost:8084/swagger-ui/index.html

3. **RabbitMQ Management :** http://localhost:15672
   - Vérifier les queues et exchanges

---

## 📡 Communication RabbitMQ

### Vue d'ensemble

La communication entre les services se fait via RabbitMQ en utilisant des **Topic Exchanges**.

### Configuration

#### Exchanges

| Exchange | Type | Description |
|----------|------|-------------|
| `patient-exchange` | Topic | Synchronisation des patients |
| `request-exchange` | Topic | Demandes de patients |
| `notification-exchange` | Topic | Notifications et réponses |
| `medical-record-exchange` | Topic | Dossiers médicaux |

#### Queues principales

| Queue | Description | Services |
|-------|-------------|----------|
| `patient.sync.queue` | Synchronisation des nouveaux patients | Patient → Provider |
| `request.queue` | Demandes de patients | Patient → Request |
| `notification.queue` | Notifications aux patients | Request → Patient |
| `medical-record.queue` | Création de dossiers médicaux | Provider → MedicalRecord |

#### Routing Keys

| Routing Key | Description | Direction |
|-------------|-------------|-----------|
| `patient.sync.request` | Nouveau patient inscrit | Patient → Provider |
| `patient.request.created` | Nouvelle demande créée | Patient → Request |
| `request.response` | Réponse à une demande | Request → Patient |
| `medical.record.create` | Création d'un dossier médical | Provider → MedicalRecord |

### Flux de communication

#### 1. Inscription d'un patient
```
Patient-Service (inscription)
   ↓
Publie sur patient-exchange (routing key: patient.sync.request)
   ↓
Provider-Service reçoit via patient.sync.queue
   ↓
Provider-Service ajoute le patient à sa liste locale
   ↓
assignedProviderId = null (non assigné par défaut)
```

#### 2. Soumission d'une demande
```
Patient-Service (POST /api/requests)
   ↓
Publie sur request-exchange (routing key: patient.request.created)
   ↓
Request-Service reçoit via request.queue
   ↓
Request-Service enregistre la demande
```

#### 3. Réponse à une demande
```
Request-Service (PUT /api/requests/{id}/respond)
   ↓
Publie sur notification-exchange (routing key: request.response)
   ↓
Patient-Service reçoit via notification.queue
   ↓
Patient-Service envoie une notification (email) au patient
   ↓
Patient-Service stocke la notification en cache
```

#### 4. Création d'un dossier médical
```
Provider-Service (POST /api/providers/medical-records)
   ↓
Publie sur medical-record-exchange (routing key: medical.record.create)
   ↓
MedicalRecord-Service reçoit via medical-record.queue
   ↓
MedicalRecord-Service crée le dossier médical
```

---

## ⚙️ Configuration

### Variables d'environnement importantes

#### JWT Configuration
- **Secret :** `mySecretKey123456789012345678901234567890`
- **Expiration :** 86400000 ms (24 heures)
- **Algorithme :** HS256
- **Claims :** `patientId` (Patient-Service), `providerId` (Provider-Service)

#### Bases de données

**MongoDB** (tous les services) :
- **URI :** `mongodb://localhost:27017/MaBase`
- **Port :** 27017

#### RabbitMQ
- **Host :** localhost
- **Port :** 5672
- **Management Port :** 15672
- **Username :** guest
- **Password :** guest
- **Management UI :** http://localhost:15672

#### Eureka
- **Server URL :** http://localhost:8761/eureka/
- **Port :** 8761
- **Dashboard :** http://localhost:8761
- **Services enregistrés :**
  - `patient-service` (port 8081)
  - `provider-service` (port 8082)
  - `medicalrecord-service` (port 8083)
  - `request-service` (port 8084)

**Configuration Eureka dans chaque service :**
```properties
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
eureka.client.register-with-eureka=true
eureka.client.fetch-registry=true
```

**Configuration Gateway avec Eureka :**
- La Gateway utilise `lb://service-name` pour la découverte de services
- Load balancing automatique si plusieurs instances d'un service sont disponibles

#### Email (Notifications)
- **SMTP Host :** smtp.gmail.com
- **Port :** 587
- **Configuration :** Voir `Patient-Service/CONFIGURATION_EMAIL.md`
- **Requis :** Compte Gmail avec mot de passe d'application

### Fichiers de configuration

Chaque service a son propre fichier de configuration :
- `Patient-Service/src/main/resources/application.properties`
- `Provider-Service/src/main/resources/application.properties`
- `Medicalrecord-Service/src/main/resources/application.properties`
- `Request-Service/src/main/resources/application.properties`
- `Gateway-Service/src/main/resources/application.yml`
- `Eureka-Server/src/main/resources/application.properties`

---

## 🔒 Sécurité

### Authentification

- **Méthode :** JWT (JSON Web Tokens)
- **Algorithme :** HS256 (HMAC)
- **Expiration :** 24 heures
- **Format :** Bearer Token dans le header `Authorization`

### Autorisation

#### Rôles
- **PATIENT :** Accès aux fonctionnalités patient
- **PROVIDER :** Accès aux fonctionnalités provider et gestion des patients

#### Endpoints sécurisés

| Service | Endpoint | Rôle requis |
|---------|----------|-------------|
| Patient-Service | `/api/patient/**` | PATIENT |
| Patient-Service | `/api/requests/**` | PATIENT (ACTIVE) |
| Patient-Service | `/api/notifications/**` | PATIENT (ACTIVE) |
| Provider-Service | `/api/providers/**` | PROVIDER |
| Request-Service | `/api/requests/**` | PROVIDER (sauf GET `/api/requests/patient/{id}`) |
| Request-Service | `/api/certificates/**` | PATIENT ou PROVIDER |
| MedicalRecord-Service | PUT/DELETE `/api/records/**` | PROVIDER |

### Statuts de compte patient

Certains endpoints nécessitent un compte **ACTIVE** :
- `GET /api/patient/medical-history`
- `POST /api/requests`
- `POST /api/requests/{requestId}/message`
- `GET /api/notifications`
- `GET /api/notifications/{requestId}`

Si le compte n'est pas ACTIVE, ces endpoints retournent **403 Forbidden**.

### Vérification des permissions

- **Patients :** Vérification via `patientId` dans le JWT
- **Providers :** Vérification via `providerId` dans le JWT
- **Assignation :** Un provider ne peut désassigner que ses propres patients

### Utilisation du token

```bash
# Exemple de requête avec token
curl -X GET http://localhost:8080/api/patient/profile \
  -H "Authorization: Bearer <your-jwt-token>"
```

---

## 📚 Documentation supplémentaire

- [RAPPORT_ENDPOINTS.md](RAPPORT_ENDPOINTS.md) - Documentation complète de tous les endpoints API
- [SYSTEME_ASSIGNATION_PATIENTS.md](SYSTEME_ASSIGNATION_PATIENTS.md) - Guide du système d'assignation des patients
- [Patient-Service/CONFIGURATION_EMAIL.md](Patient-Service/CONFIGURATION_EMAIL.md) - Guide de configuration email
- [Request-Service/DIAGRAMME_SEQUENCE_GUIDE.md](Request-Service/DIAGRAMME_SEQUENCE_GUIDE.md) - Diagrammes de séquence pour Request-Service

---

## 🧪 Tests

### Tester l'inscription d'un patient

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "0612345678",
    "dateOfBirth": "1990-01-01",
    "gender": "MALE"
  }'
```

### Tester la connexion

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "patient@example.com",
    "password": "password123"
  }'
```

### Tester l'assignation d'un patient (Provider)

```bash
# Assigner un patient à moi
curl -X POST http://localhost:8080/api/providers/patients/{patientId}/assign \
  -H "Authorization: Bearer <provider-token>"

# Voir mes patients assignés
curl -X GET http://localhost:8080/api/providers/patients/assigned \
  -H "Authorization: Bearer <provider-token>"
```

### Tester la soumission d'une demande (Patient)

```bash
curl -X POST http://localhost:8080/api/requests \
  -H "Authorization: Bearer <patient-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "Consultation",
    "priority": "High",
    "subject": "Consultation cardiologique",
    "description": "Suivi après le dernier bilan",
    "preferredDate": "2025-12-10",
    "targetProviderId": "provider-id-123"
  }'
```

### Tester la génération d'un certificat PDF

```bash
curl -X GET http://localhost:8080/api/certificates/{certificateId}/print \
  -H "Authorization: Bearer <token>" \
  --output certificat.pdf
```

### Vérifier la communication RabbitMQ

1. Inscrire un patient via Patient-Service
2. Vérifier dans RabbitMQ Management UI (http://localhost:15672) que le message est dans la queue `patient.sync.queue`
3. Vérifier dans les logs de Provider-Service que le patient a été reçu
4. Vérifier dans Eureka Dashboard que tous les services sont enregistrés

---

## 📝 Notes importantes

- Tous les services doivent être démarrés dans l'ordre recommandé
- **MongoDB** doit être en cours d'exécution avant de démarrer les services
- **RabbitMQ** doit être démarré avant Patient-Service, Provider-Service, Request-Service et MedicalRecord-Service
- **Eureka Server** doit être démarré en premier pour le service discovery
- Les ports doivent être libres :
  - Services : 8080, 8081, 8082, 8083, 8084, 8761
  - Bases de données : 27017 (MongoDB)
  - RabbitMQ : 5672, 15672
- Tous les services utilisent MongoDB (pas de PostgreSQL)
- La configuration email est optionnelle (le système fonctionne sans, mais les emails ne seront pas envoyés)

---

## 🎯 Cas d'usage typiques

### 1. Nouveau patient s'enregistre
1. Patient s'enregistre via `POST /api/auth/register`
2. Patient-Service publie le patient via RabbitMQ
3. Tous les providers reçoivent le patient avec `assignedProviderId = null`
4. Un provider peut assigner le patient à lui-même via `POST /api/providers/patients/{id}/assign`

### 2. Patient soumet une demande
1. Patient soumet une demande via `POST /api/requests` (peut cibler un provider spécifique)
2. Demande envoyée à Request-Service via RabbitMQ
3. Provider répond via `PUT /api/requests/{id}/respond`
4. Patient reçoit une notification (email + API)

### 3. Provider crée un certificat
1. Provider crée un certificat (via Request-Service)
2. Patient ou provider peut générer le PDF via `GET /api/certificates/{id}/print`
3. PDF téléchargé automatiquement

---

## 👥 Auteurs

- Équipe de développement PFA 2026

---

## 📄 Licence

Ce projet est développé dans le cadre d'un projet de fin d'année (PFA) 2026.

---

## 🔄 Version

**Version actuelle :** 1.0.0

**Dernière mise à jour :** Décembre 2025

---

## 📞 Support

Pour toute question ou problème, veuillez consulter la documentation ou contacter l'équipe de développement.
