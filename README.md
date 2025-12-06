# Plateforme de Coordination de Soins de Santé

## 📋 Table des matières

1. [Vue d'ensemble](#vue-densemble)
2. [Architecture](#architecture)
3. [Microservices](#microservices)
4. [Technologies utilisées](#technologies-utilisées)
5. [Installation et démarrage](#installation-et-démarrage)
6. [Documentation des API](#documentation-des-api)
7. [Communication RabbitMQ](#communication-rabbitmq)
8. [Configuration](#configuration)
9. [Sécurité](#sécurité)
10. [Tests](#tests)

---

## 🎯 Vue d'ensemble

### Contexte général

Avec la digitalisation du secteur médical, les patients recherchent des plateformes fiables pour interagir avec les professionnels de santé. Les médecins, quant à eux, ont besoin d'outils efficaces pour gérer le suivi médical et les dossiers patients.

### Problèmes identifiés

- Difficulté de communication patient–médecin
- Manque de coordination entre acteurs de santé
- Dispersion des dossiers médicaux
- Besoin de sécurisation élevée (confidentialité, permissions)

### Objectifs du projet

Créer une plateforme centralisée permettant :
- Une meilleure interaction entre patients et prestataires
- Une gestion unifiée et sécurisée des dossiers médicaux
- Une communication fluide entre services via microservices
- Une architecture scalable et maintenable

---

## 🏗️ Architecture

### Architecture microservices

Le projet suit une architecture microservices avec les composants suivants :

```
┌─────────────────────────────────────────────────────────────┐
│                    API Gateway (8080)                       │
│              Spring Cloud Gateway                            │
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
    ┌──────────────┐       ┌──────────────┐
    │ Eureka Server │       │ Config Server│
    │   (8761)      │       │  (optionnel) │
    └──────────────┘       └──────────────┘
```

### Composants principaux

1. **API Gateway** : Point d'entrée unique pour toutes les requêtes
2. **Eureka Server** : Service discovery pour la localisation des microservices
3. **Patient-Service** : Gestion des patients et authentification
4. **Provider-Service** : Gestion des prestataires de santé
5. **MedicalRecord-Service** : Gestion des dossiers médicaux
6. **Request-Service** : Gestion des demandes de patients et réponses des providers
7. **RabbitMQ** : Message broker pour la communication asynchrone
8. **MongoDB** : Base de données NoSQL pour tous les services
9. **Config-Server** : Service de configuration centralisée (optionnel)

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

---

### 2. API Gateway

**Port :** 8080  
**Rôle :** Point d'entrée unique pour toutes les requêtes API

#### Configuration
- **Application Name :** api-gateway
- **Port :** 8080
- **Technologie :** Spring Cloud Gateway

#### Routes configurées

| Service | Routes | URI cible |
|---------|--------|-----------|
| Patient-Service | `/api/patient/**`, `/api/auth/**`, `/api/requests/**`, `/api/notifications/**` | http://localhost:8081 |
| Provider-Service | `/api/providers/**`, `/api/provider/**` | http://localhost:8082 |
| MedicalRecord-Service | `/api/records/**` | http://localhost:8083 |
| Request-Service | *(Accès direct, non routé via Gateway)* | http://localhost:8084 |

#### Accès
- **Base URL :** http://localhost:8080
- Toutes les requêtes passent par la Gateway (sauf Request-Service)

---

### 3. Patient-Service

**Port :** 8081  
**Rôle :** Gestion des patients et de leur authentification

#### Configuration
- **Application Name :** patient-service
- **Port :** 8081
- **Base de données :** MongoDB (mongodb://localhost:27017/MaBase)
- **JWT Secret :** Configuré dans application.properties
- **JWT Expiration :** 86400000 ms (24 heures)

#### Technologies
- Spring Boot 3.2.1
- Spring Security
- MongoDB
- RabbitMQ
- JWT (JJWT 0.11.5)
- Swagger/OpenAPI

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
- `POST /api/requests/{requestId}/message` : Ajouter un message à une demande

##### Notifications (`/api/notifications`)
- `GET /api/notifications` : Lister toutes les notifications (compte ACTIVE requis)
- `GET /api/notifications/{requestId}` : Obtenir une notification par ID

#### Communication RabbitMQ
- **Publie sur :** `patient-exchange` avec routing key `patient.sync.request`
- **Publie sur :** `request-exchange` avec routing key `patient.request.created`
- **Écoute :** `notification.queue` (pour les réponses aux demandes)

#### Swagger UI
- **URL :** http://localhost:8081/swagger-ui/index.html
- **API Docs :** http://localhost:8081/v3/api-docs

---

### 4. Provider-Service

**Port :** 8082  
**Rôle :** Gestion des prestataires de santé et des patients

#### Configuration
- **Application Name :** provider-service
- **Port :** 8082
- **Base de données :** MongoDB (mongodb://localhost:27017/MaBase)
- **JWT Secret :** Configuré dans application.properties
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
- `GET /api/auth/providers/list` : Liste publique de tous les providers

##### Gestion des Patients (`/api/providers`)
- `GET /api/providers/patients/all` : Récupérer tous les patients
- `GET /api/providers/patients` : Récupérer les patients par statut
- `GET /api/providers/patients/{patientId}` : Détails d'un patient
- `PUT /api/providers/patients/{patientId}/status` : Mettre à jour le statut d'un patient
- `POST /api/providers/patients/sync` : Synchroniser tous les patients
- `POST /api/providers/patient/{patientId}/activate` : Activer un patient
- `POST /api/providers/patient/{patientId}/suspend` : Suspendre un patient

##### Dossiers Médicaux (`/api/providers/medical-records`)
- `POST /api/providers/medical-records` : Créer un dossier médical (via RabbitMQ)

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

**Note :** La création de dossiers médicaux se fait uniquement via RabbitMQ depuis Provider-Service.

#### Communication RabbitMQ
- **Écoute :** `medical-record.queue` (reçoit les demandes de création de dossiers)

#### Swagger UI
- **URL :** http://localhost:8083/swagger-ui/index.html
- **API Docs :** http://localhost:8083/v3/api-docs

---

### 6. Request-Service

**Port :** 8084  
**Rôle :** Gestion des demandes de patients et réponses des providers

#### Configuration
- **Application Name :** request-service
- **Port :** 8084
- **Base de données :** MongoDB (mongodb://localhost:27017/MaBase)
- **JWT Secret :** Configuré dans application.properties
- **JWT Expiration :** 86400000 ms (24 heures)

#### Technologies
- Spring Boot
- Spring Security
- MongoDB
- RabbitMQ
- JWT (JJWT)
- Swagger/OpenAPI

#### Endpoints principaux

##### Endpoints Patients (`/api/requests`)
- `GET /api/requests/patient/{patientId}` : Récupérer les demandes d'un patient
  - **PATIENT** : Retourne uniquement ses propres demandes
  - **PROVIDER** : Retourne toutes les demandes d'un patient spécifique

##### Endpoints Providers (`/api/requests`)
- `GET /api/requests` : Récupérer toutes les demandes (PROVIDER uniquement)
- `GET /api/requests/status/{status}` : Récupérer les demandes par statut (PROVIDER uniquement)
- `GET /api/requests/provider/{providerId}` : Récupérer les demandes d'un provider (PROVIDER uniquement)
- `GET /api/requests/provider/{providerId}/targeted` : Récupérer les demandes destinées à un provider (PROVIDER uniquement)
- `GET /api/requests/{requestId}` : Récupérer une demande par ID (PROVIDER uniquement)
- `PUT /api/requests/{requestId}/respond` : Répondre à une demande (PROVIDER uniquement)
- `POST /api/requests/{requestId}/messages` : Ajouter un message à une demande (PROVIDER uniquement)

**Notes importantes :**
- Le champ `targetProviderId` indique si une demande est destinée à un provider spécifique
- Si `targetProviderId = null`, la demande est visible par tous les providers
- Le champ `providerId` indique quel provider a traité la demande (rempli lors de la réponse)

#### Communication RabbitMQ
- **Écoute :** `request.queue` (reçoit les nouvelles demandes de patients)
- **Publie sur :** `notification-exchange` avec routing key `request.response` (envoie les réponses aux patients)

#### Swagger UI
- **URL :** http://localhost:8084/swagger-ui/index.html
- **API Docs :** http://localhost:8084/v3/api-docs

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

### Base de données
- **Tous les services :** MongoDB (NoSQL)
  - **Port :** 27017
  - **Base de données :** MaBase
  - **URI :** mongodb://localhost:27017/MaBase

### Communication
- **Message Broker :** RabbitMQ 3-management
- **Service Discovery :** Netflix Eureka
- **API Gateway :** Spring Cloud Gateway
- **Configuration :** Spring Cloud Config (optionnel)

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

#### 4. Démarrer les microservices

**Ordre recommandé :**

1. **Eureka Server**
```bash
cd Eureka-Server
mvn spring-boot:run
```
Vérifier : http://localhost:8761

2. **Patient-Service**
```bash
cd Patient-Service
mvn spring-boot:run
```
Vérifier : http://localhost:8081/swagger-ui/index.html

3. **Provider-Service**
```bash
cd Provider-Service
mvn spring-boot:run
```
Vérifier : http://localhost:8082/swagger-ui/index.html

4. **MedicalRecord-Service**
```bash
cd Medicalrecord-Service
mvn spring-boot:run
```
Vérifier : http://localhost:8083/swagger-ui/index.html

5. **Request-Service**
```bash
cd Request-Service
mvn spring-boot:run
```
Vérifier : http://localhost:8084/swagger-ui/index.html

6. **Gateway-Service**
```bash
cd Gateway-Service
mvn spring-boot:run
```
Vérifier : http://localhost:8080

### Vérification

1. **Eureka Dashboard :** http://localhost:8761
   - Vérifier que tous les services sont enregistrés

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
| `patient.sync.queue` | Synchronisation des nouveaux patients | Patient ↔ Provider |
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

### Format des messages

Les messages sont sérialisés en JSON. Pour plus de détails, voir [RAPPORT_ENDPOINTS.md](RAPPORT_ENDPOINTS.md)

---

## ⚙️ Configuration

### Variables d'environnement importantes

#### JWT Configuration
- **Secret :** `mySecretKey123456789012345678901234567890`
- **Expiration :** 86400000 ms (24 heures)
- **Algorithme :** HS256

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
| MedicalRecord-Service | PUT/DELETE `/api/records/**` | PROVIDER |

### Statuts de compte patient

Certains endpoints nécessitent un compte **ACTIVE** :
- `GET /api/patient/medical-history`
- `POST /api/requests`
- `POST /api/requests/{requestId}/message`
- `GET /api/notifications`
- `GET /api/notifications/{requestId}`

Si le compte n'est pas ACTIVE, ces endpoints retournent **403 Forbidden**.

### Utilisation du token

```bash
# Exemple de requête avec token
curl -X GET http://localhost:8080/api/patient/profile \
  -H "Authorization: Bearer <your-jwt-token>"
```

---

## 📚 Documentation supplémentaire

- [RAPPORT_ENDPOINTS.md](RAPPORT_ENDPOINTS.md) - Documentation complète de tous les endpoints API
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
    "lastName": "Doe"
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

### Tester la récupération du profil (avec token)

```bash
curl -X GET http://localhost:8080/api/patient/profile \
  -H "Authorization: Bearer <your-jwt-token>"
```

### Vérifier la communication RabbitMQ

1. Inscrire un patient via Patient-Service
2. Vérifier dans RabbitMQ Management UI (http://localhost:15672) que le message est dans la queue `patient.sync.queue`
3. Vérifier dans les logs de Provider-Service que le patient a été reçu

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
- Request-Service n'est pas routé via la Gateway (accès direct sur le port 8084)
- Tous les services utilisent MongoDB (pas de PostgreSQL)

---

## 👥 Auteurs

- Équipe de développement PFA 2026

---

## 📄 Licence

Ce projet est développé dans le cadre d'un projet de fin d'année (PFA) 2026.

---

## 🔄 Version

**Version actuelle :** 1.0.0

**Dernière mise à jour :** 2026

---

## 📞 Support

Pour toute question ou problème, veuillez consulter la documentation ou contacter l'équipe de développement.
