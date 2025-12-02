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
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│   Patient    │ │   Provider   │ │   Medical    │
│   Service    │ │   Service    │ │   Record     │
│   (8081)     │ │   (8082)     │ │   Service    │
│              │ │              │ │   (8083)     │
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘
       │                │                │
       └────────────────┼────────────────┘
                        │
            ┌───────────┴───────────┐
            │                       │
            ▼                       ▼
    ┌──────────────┐       ┌──────────────┐
    │   MongoDB     │       │   RabbitMQ    │
    │  (27017)      │       │   (5672)      │
    └──────────────┘       └──────────────┘
            │                       │
            │                       │
            ▼                       ▼
    ┌──────────────┐       ┌──────────────┐
    │  PostgreSQL   │       │ Eureka Server │
    │   (5432)      │       │   (8761)      │
    └──────────────┘       └──────────────┘
```

### Composants principaux

1. **API Gateway** : Point d'entrée unique pour toutes les requêtes
2. **Eureka Server** : Service discovery pour la localisation des microservices
3. **Patient-Service** : Gestion des patients
4. **Provider-Service** : Gestion des prestataires de santé
5. **MedicalRecord-Service** : Gestion des dossiers médicaux
6. **RabbitMQ** : Message broker pour la communication asynchrone
7. **MongoDB** : Base de données NoSQL pour Patient-Service et Provider-Service
8. **PostgreSQL** : Base de données relationnelle pour MedicalRecord-Service

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
| Patient-Service | `/api/patient/**`, `/api/auth/**`, `/api/requests/**` | http://localhost:8081 |
| Provider-Service | `/api/providers/**`, `/api/provider/**` | http://localhost:8082 |
| MedicalRecord-Service | `/api/records/**` | http://localhost:8083 |

#### Accès
- **Base URL :** http://localhost:8080
- Toutes les requêtes passent par la Gateway

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

#### Endpoints

##### Authentification (`/api/auth`)

| Méthode | Endpoint | Description | Authentification |
|---------|----------|-------------|-------------------|
| POST | `/api/auth/register` | Inscription d'un nouveau patient | Public |
| POST | `/api/auth/login` | Connexion patient | Public |

##### Profil Patient (`/api/patient`)

| Méthode | Endpoint | Description | Authentification |
|---------|----------|-------------|-------------------|
| GET | `/api/patient/profile` | Récupérer le profil du patient | Patient |
| GET | `/api/patient/profile-status` | Statut du profil | Patient |
| PUT | `/api/patient/complete-profile` | Compléter le profil | Patient |

##### Demandes (`/api/requests`)

| Méthode | Endpoint | Description | Authentification |
|---------|----------|-------------|-------------------|
| POST | `/api/requests` | Soumettre une demande | Patient |
| GET | `/api/requests` | Lister les demandes du patient | Patient |
| POST | `/api/requests/{requestId}/message` | Ajouter un message à une demande | Patient |

#### Communication RabbitMQ
- **Publie sur :** `patient-exchange` avec routing key `patient.sync.request`
- **Queue :** `patient.sync.queue`
- **Écoute :** `patient.sync.queue` (pour les réponses)

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

#### Endpoints

##### Authentification (`/api/auth`)

| Méthode | Endpoint | Description | Authentification |
|---------|----------|-------------|-------------------|
| POST | `/api/auth/register` | Inscription d'un nouveau provider | Public |
| POST | `/api/auth/login` | Connexion provider | Public |
| GET | `/api/auth/profile` | Récupérer le profil du provider | Provider |
| PUT | `/api/auth/complete-profile` | Compléter le profil provider | Provider |

##### Gestion des Patients (`/api/providers`)

| Méthode | Endpoint | Description | Authentification |
|---------|----------|-------------|-------------------|
| GET | `/api/providers/patients` | Lister les patients (filtrés par statut) | Provider |
| GET | `/api/providers/patients/{patientId}` | Détails d'un patient | Provider |
| PUT | `/api/providers/patients/{patientId}/status` | Mettre à jour le statut d'un patient | Provider |

##### Actions sur les Patients (`/api/providers/patient`)

| Méthode | Endpoint | Description | Authentification |
|---------|----------|-------------|-------------------|
| POST | `/api/providers/patient/{patientId}/activate` | Activer un patient | Provider |
| POST | `/api/providers/patient/{patientId}/suspend` | Suspendre un patient | Provider |

#### Communication RabbitMQ
- **Écoute :** `patient.sync.queue` (reçoit les nouveaux patients)
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
- **Base de données :** PostgreSQL (jdbc:postgresql://localhost:5432/medicaldb)
- **JWT Validation :** Via OAuth2 Resource Server

#### Technologies
- Spring Boot 3.2.4
- Spring Security (OAuth2 Resource Server)
- PostgreSQL (base de données relationnelle)
- Swagger/OpenAPI

#### Endpoints

##### Opérations CRUD (`/api/records`)

| Méthode | Endpoint | Description | Authentification |
|---------|----------|-------------|-------------------|
| POST | `/api/records` | Créer un nouveau dossier médical | Provider |
| GET | `/api/records` | Récupérer tous les dossiers | Public |
| GET | `/api/records/{id}` | Récupérer un dossier par ID | Public |
| PUT | `/api/records/{id}` | Mettre à jour un dossier | Provider |
| DELETE | `/api/records/{id}` | Supprimer un dossier | Provider |

##### Recherche (`/api/records/read`)

| Méthode | Endpoint | Description | Authentification |
|---------|----------|-------------|-------------------|
| GET | `/api/records/read/patient/{patientId}` | Dossiers d'un patient | Public |
| GET | `/api/records/read/search` | Recherche avancée | Public |

**Paramètres de recherche :**
- `patientId` (optionnel) : ID du patient
- `providerId` (optionnel) : ID du provider
- `from` (optionnel) : Date de début (format ISO)
- `to` (optionnel) : Date de fin (format ISO)
- `limit` (optionnel) : Nombre maximum de résultats

#### Swagger UI
- **URL :** http://localhost:8083/swagger-ui/index.html
- **API Docs :** http://localhost:8083/v3/api-docs

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

### Base de données
- **Patient-Service & Provider-Service :** MongoDB (NoSQL)
  - **Port :** 27017
  - **Base de données :** MaBase
- **MedicalRecord-Service :** PostgreSQL (SQL relationnel)
  - **Port :** 5432
  - **Base de données :** medicaldb
  - **Username :** postgres
  - **Password :** (configuré dans application.properties)

### Communication
- **Message Broker :** RabbitMQ 3-management
- **Service Discovery :** Netflix Eureka
- **API Gateway :** Spring Cloud Gateway

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

#### 3. Démarrer les bases de données

**MongoDB** (pour Patient-Service et Provider-Service) :
```bash
# Windows
mongod

# Linux/Mac
sudo systemctl start mongod
```

**PostgreSQL** (pour MedicalRecord-Service) :
```bash
# Windows (si installé comme service, il démarre automatiquement)
# Sinon, utiliser pg_ctl

# Linux/Mac
sudo systemctl start postgresql

# Créer la base de données
psql -U postgres
CREATE DATABASE medicaldb;
\q
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

5. **Gateway-Service**
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

3. **RabbitMQ Management :** http://localhost:15672
   - Vérifier les queues et exchanges

---

## 📡 Communication RabbitMQ

### Vue d'ensemble

La communication entre Patient-Service et Provider-Service se fait via RabbitMQ en utilisant un **Topic Exchange**.

### Configuration

#### Exchange
- **Nom :** `patient-exchange`
- **Type :** Topic Exchange
- **Durabilité :** Durable

#### Queues

| Queue | Description | Service |
|-------|-------------|---------|
| `patient.sync.queue` | Synchronisation des nouveaux patients | Patient ↔ Provider |
| `patient.requests.queue` | Demandes de patients (futures fonctionnalités) | Patient → Provider |

#### Routing Keys

| Routing Key | Description | Direction |
|-------------|-------------|-----------|
| `patient.sync.request` | Nouveau patient inscrit | Patient → Provider |
| `patient.sync.response` | Réponse de synchronisation | Provider → Patient |
| `patient.requests.*` | Pattern pour les demandes | Patient → Provider |

### Flux de communication

```
1. Patient s'inscrit dans Patient-Service
   ↓
2. Patient-Service publie le patient sur RabbitMQ
   Exchange: patient-exchange
   Routing Key: patient.sync.request
   ↓
3. Provider-Service reçoit le patient via le listener
   Queue: patient.sync.queue
   ↓
4. Provider-Service ajoute le patient à sa liste locale
```

### Format des messages

Les messages sont sérialisés en JSON avec le format suivant :

```json
{
  "id": "patient-id",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "accountStatus": "PENDING",
  "dateOfBirth": "1990-01-01",
  "gender": "MALE",
  ...
}
```

Pour plus de détails, voir [RABBITMQ_COMMUNICATION.md](RABBITMQ_COMMUNICATION.md)

---

## ⚙️ Configuration

### Variables d'environnement importantes

#### JWT Configuration
- **Secret :** `mySecretKey123456789012345678901234567890`
- **Expiration :** 86400000 ms (24 heures)
- **Algorithme :** HS256

#### Bases de données

**MongoDB** (Patient-Service & Provider-Service) :
- **URI :** `mongodb://localhost:27017/MaBase`
- **Port :** 27017

**PostgreSQL** (MedicalRecord-Service) :
- **URL :** `jdbc:postgresql://localhost:5432/medicaldb`
- **Port :** 5432
- **Username :** postgres
- **Password :** (configuré dans application.properties)

#### RabbitMQ
- **Host :** localhost
- **Port :** 5672
- **Username :** guest
- **Password :** guest
- **Management UI :** http://localhost:15672

#### Eureka
- **Server URL :** http://localhost:8761/eureka/
- **Port :** 8761

### Fichiers de configuration

Chaque service a son propre fichier `application.properties` :
- `Patient-Service/src/main/resources/application.properties`
- `Provider-Service/src/main/resources/application.properties`
- `Medicalrecord-Service/src/main/resources/application.properties`
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
| Patient-Service | `/api/requests/**` | PATIENT |
| Provider-Service | `/api/providers/**` | PROVIDER |
| MedicalRecord-Service | POST/PUT/DELETE `/api/records/**` | PROVIDER |

### Utilisation du token

```bash
# Exemple de requête avec token
curl -X GET http://localhost:8080/api/patient/profile \
  -H "Authorization: Bearer <your-jwt-token>"
```

---

## 📚 Documentation supplémentaire

- [RABBITMQ_COMMUNICATION.md](RABBITMQ_COMMUNICATION.md) - Documentation détaillée de la communication RabbitMQ
- [REFACTORING_SUMMARY.md](REFACTORING_SUMMARY.md) - Résumé du refactoring effectué

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

### Vérifier la communication RabbitMQ

1. Inscrire un patient via Patient-Service
2. Vérifier dans RabbitMQ Management UI (http://localhost:15672) que le message est dans la queue `patient.sync.queue`
3. Vérifier dans les logs de Provider-Service que le patient a été reçu

---

## 📝 Notes importantes

- Tous les services doivent être démarrés dans l'ordre recommandé
- **MongoDB** doit être en cours d'exécution avant de démarrer Patient-Service et Provider-Service
- **PostgreSQL** doit être en cours d'exécution avant de démarrer MedicalRecord-Service
- **RabbitMQ** doit être démarré avant Patient-Service et Provider-Service
- **Eureka Server** doit être démarré en premier pour le service discovery
- Les ports doivent être libres :
  - Services : 8080, 8081, 8082, 8083, 8761
  - Bases de données : 27017 (MongoDB), 5432 (PostgreSQL)
  - RabbitMQ : 5672, 15672

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
