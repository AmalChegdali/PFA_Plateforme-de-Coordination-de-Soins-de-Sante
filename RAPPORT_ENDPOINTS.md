# 📋 Rapport Complet des Endpoints API

**Plateforme Soins Santé - Microservices Architecture**

**Date de génération** : 2026  
**Version** : 1.0

---

## 📑 Table des matières

1. [Patient-Service](#1-patient-service)
2. [Provider-Service](#2-provider-service)
3. [Request-Service](#3-request-service)
4. [Medicalrecord-Service](#4-medicalrecord-service)
5. [Résumé par méthode HTTP](#5-résumé-par-méthode-http)
6. [Authentification requise](#6-authentification-requise)

---

## 1. Patient-Service

**Base URL** : `http://localhost:8081` (par défaut)

### 1.1. Authentification (`/api/auth`)

| Méthode | Endpoint | Description | Auth | Rôle Requis |
|---------|----------|-------------|------|-------------|
| `POST` | `/api/auth/register` | Enregistrer un nouveau patient | ❌ | - |
| `POST` | `/api/auth/login` | Authentifier un patient | ❌ | - |

**Détails** :
- **POST `/api/auth/register`** : Crée un compte patient et publie le patient à RabbitMQ pour synchronisation avec Provider-Service. Retourne un JWT token.
- **POST `/api/auth/login`** : Authentifie un patient et retourne un JWT token avec les informations du compte (statut, accès historique médical).

---

### 1.2. Profil Patient (`/api/patient`)

| Méthode | Endpoint | Description | Auth | Rôle Requis |
|---------|----------|-------------|------|-------------|
| `GET` | `/api/patient/profile-status` | Obtenir le statut du profil | ✅ | PATIENT |
| `GET` | `/api/patient/profile` | Obtenir le profil complet | ✅ | PATIENT |
| `PUT` | `/api/patient/complete-profile` | Compléter/Mettre à jour le profil | ✅ | PATIENT |
| `GET` | `/api/patient/medical-history` | Obtenir l'historique médical | ✅ | PATIENT (ACTIVE) |

**Détails** :
- **GET `/api/patient/profile-status`** : Retourne le statut de complétion du profil.
- **GET `/api/patient/profile`** : Retourne toutes les informations du profil patient (données personnelles, adresse, etc.).
- **PUT `/api/patient/complete-profile`** : Met à jour les informations du profil patient.
- **GET `/api/patient/medical-history`** : Récupère tous les dossiers médicaux du patient depuis Medicalrecord-Service. **Nécessite un compte ACTIVE.**

---

### 1.3. Demandes (`/api/requests`)

| Méthode | Endpoint | Description | Auth | Rôle Requis |
|---------|----------|-------------|------|-------------|
| `POST` | `/api/requests` | Soumettre une demande | ✅ | PATIENT (ACTIVE) |
| `POST` | `/api/requests/{requestId}/message` | Ajouter un message à une demande | ✅ | PATIENT (ACTIVE) |

**Détails** :
- **POST `/api/requests`** : Soumet une nouvelle demande. Publie la demande à RabbitMQ pour Request-Service. **Nécessite un compte ACTIVE.**
- **POST `/api/requests/{requestId}/message`** : Ajoute un message à une demande existante. **Nécessite un compte ACTIVE.**

---

### 1.4. Notifications (`/api/notifications`)

| Méthode | Endpoint | Description | Auth | Rôle Requis |
|---------|----------|-------------|------|-------------|
| `GET` | `/api/notifications` | Lister toutes les notifications | ✅ | PATIENT (ACTIVE) |
| `GET` | `/api/notifications/{requestId}` | Obtenir une notification par ID | ✅ | PATIENT (ACTIVE) |

**Détails** :
- **GET `/api/notifications`** : Récupère toutes les notifications (réponses aux demandes) du patient connecté, triées par date (plus récentes en premier). **Nécessite un compte ACTIVE.**
- **GET `/api/notifications/{requestId}`** : Récupère une notification spécifique par l'ID de la demande. **Nécessite un compte ACTIVE.**

---

## 2. Provider-Service

**Base URL** : `http://localhost:8082` (par défaut)

### 2.1. Authentification (`/api/auth`)

| Méthode | Endpoint | Description | Auth | Rôle Requis |
|---------|----------|-------------|------|-------------|
| `POST` | `/api/auth/register` | Enregistrer un nouveau provider | ❌ | - |
| `POST` | `/api/auth/login` | Authentifier un provider | ❌ | - |
| `GET` | `/api/auth/profile` | Obtenir le profil du provider connecté | ✅ | PROVIDER |
| `PUT` | `/api/auth/complete-profile` | Compléter le profil provider | ✅ | PROVIDER |
| `GET` | `/api/auth/providers/list` | Lister tous les providers (public) | ❌ | - |

**Détails** :
- **POST `/api/auth/register`** : Crée un compte provider et retourne un JWT token.
- **POST `/api/auth/login`** : Authentifie un provider et retourne un JWT token.
- **GET `/api/auth/profile`** : Retourne le profil du provider authentifié.
- **PUT `/api/auth/complete-profile`** : Complète les informations du profil provider (spécialité, clinique, etc.).
- **GET `/api/auth/providers/list`** : **Endpoint public** - Retourne la liste de tous les providers (résumé). Utilisé par les patients pour choisir un provider lors de la soumission d'une demande.

---

### 2.2. Gestion des Patients (`/api/providers`)

| Méthode | Endpoint | Description | Auth | Rôle Requis |
|---------|----------|-------------|------|-------------|
| `GET` | `/api/providers/patients/all` | Récupérer tous les patients | ✅ | PROVIDER |
| `GET` | `/api/providers/patients` | Récupérer les patients par statut | ✅ | PROVIDER |
| `GET` | `/api/providers/patients/{patientId}` | Récupérer un patient par ID | ✅ | PROVIDER |
| `PUT` | `/api/providers/patients/{patientId}/status` | Mettre à jour le statut d'un patient | ✅ | PROVIDER |
| `POST` | `/api/providers/patients/sync` | Synchroniser tous les patients | ✅ | PROVIDER |

**Détails** :
- **GET `/api/providers/patients/all`** : Retourne tous les patients, quel que soit leur statut. Déclenche une synchronisation automatique si la liste est vide.
- **GET `/api/providers/patients?status={status}`** : Retourne les patients filtrés par statut (PENDING, ACTIVE, SUSPENDED, ou ALL). Par défaut : PENDING.
- **GET `/api/providers/patients/{patientId}`** : Retourne les détails d'un patient spécifique.
- **PUT `/api/providers/patients/{patientId}/status?status={status}`** : Met à jour le statut d'un patient (ACTIVE, SUSPENDED, etc.).
- **POST `/api/providers/patients/sync`** : Demande la synchronisation de tous les patients depuis Patient-Service via RabbitMQ.

---

### 2.3. Statut des Patients (`/api/providers/patient`)

| Méthode | Endpoint | Description | Auth | Rôle Requis |
|---------|----------|-------------|------|-------------|
| `POST` | `/api/providers/patient/{patientId}/activate` | Activer un patient | ✅ | PROVIDER |
| `POST` | `/api/providers/patient/{patientId}/suspend` | Suspendre un patient | ✅ | PROVIDER |

**Détails** :
- **POST `/api/providers/patient/{patientId}/activate`** : Active un patient (change le statut à ACTIVE).
- **POST `/api/providers/patient/{patientId}/suspend`** : Suspend un patient avec une raison (change le statut à SUSPENDED). Body : `{"reason": "..."}`.

---

### 2.4. Dossiers Médicaux (`/api/providers/medical-records`)

| Méthode | Endpoint | Description | Auth | Rôle Requis |
|---------|----------|-------------|------|-------------|
| `POST` | `/api/providers/medical-records` | Créer un dossier médical | ✅ | PROVIDER |

**Détails** :
- **POST `/api/providers/medical-records`** : Crée un nouveau dossier médical en envoyant la demande au Medicalrecord-Service via RabbitMQ. Le providerId est automatiquement extrait du JWT.

---

## 3. Request-Service

**Base URL** : `http://localhost:8083` (par défaut)

### 3.1. Demandes (`/api/requests`)

#### Endpoints Patients

| Méthode | Endpoint | Description | Auth | Rôle Requis |
|---------|----------|-------------|------|-------------|
| `GET` | `/api/requests/patient/{patientId}` | Récupérer les demandes d'un patient | ✅ | PATIENT ou PROVIDER |

**Détails** :
- **GET `/api/requests/patient/{patientId}`** : 
  - **PATIENT** : Retourne uniquement ses propres demandes (vérification via JWT).
  - **PROVIDER** : Retourne toutes les demandes d'un patient spécifique.
  - Chaque demande inclut `targetProviderId` pour identifier si la demande est destinée à un provider spécifique.

#### Endpoints Providers

| Méthode | Endpoint | Description | Auth | Rôle Requis |
|---------|----------|-------------|------|-------------|
| `GET` | `/api/requests` | Récupérer toutes les demandes | ✅ | PROVIDER |
| `GET` | `/api/requests/status/{status}` | Récupérer les demandes par statut | ✅ | PROVIDER |
| `GET` | `/api/requests/provider/{providerId}` | Récupérer les demandes d'un provider | ✅ | PROVIDER |
| `GET` | `/api/requests/provider/{providerId}/targeted` | Récupérer les demandes destinées à un provider | ✅ | PROVIDER |
| `GET` | `/api/requests/{requestId}` | Récupérer une demande par ID | ✅ | PROVIDER |
| `PUT` | `/api/requests/{requestId}/respond` | Répondre à une demande | ✅ | PROVIDER |
| `POST` | `/api/requests/{requestId}/messages` | Ajouter un message à une demande | ✅ | PROVIDER |

**Détails** :
- **GET `/api/requests`** : Retourne toutes les demandes. Chaque demande inclut `targetProviderId` pour identifier le provider cible.
- **GET `/api/requests/status/{status}`** : Retourne les demandes filtrées par statut (EN_ATTENTE, TRAITÉ, REFUSÉ, etc.).
- **GET `/api/requests/provider/{providerId}`** : Retourne les demandes destinées à un provider (targetProviderId) OU traitées par ce provider (providerId).
- **GET `/api/requests/provider/{providerId}/targeted`** : Retourne uniquement les demandes où `targetProviderId = providerId` (exclut les demandes traitées mais non destinées initialement).
- **GET `/api/requests/{requestId}`** : Retourne les détails d'une demande spécifique.
- **PUT `/api/requests/{requestId}/respond`** : Met à jour le statut d'une demande et envoie la réponse au patient via RabbitMQ. Body : `{"status": "...", "responseMessage": "..."}`.
- **POST `/api/requests/{requestId}/messages`** : Ajoute un message à une demande existante. Body : `{"content": "..."}`.

**Notes importantes** :
- Le champ `targetProviderId` indique si une demande est destinée à un provider spécifique.
- Si `targetProviderId = null`, la demande est visible par tous les providers.
- Le champ `providerId` indique quel provider a traité la demande (rempli lors de la réponse).

---

## 4. Medicalrecord-Service

**Base URL** : `http://localhost:8084` (par défaut)

### 4.1. Opérations de Lecture (`/api/records/read`)

| Méthode | Endpoint | Description | Auth | Rôle Requis |
|---------|----------|-------------|------|-------------|
| `GET` | `/api/records/read/patient/{patientId}` | Récupérer les dossiers d'un patient | ✅ | - |
| `GET` | `/api/records/read/search` | Recherche avancée de dossiers | ✅ | - |

**Détails** :
- **GET `/api/records/read/patient/{patientId}`** : Retourne tous les dossiers médicaux d'un patient spécifique.
- **GET `/api/records/read/search`** : Recherche avancée avec paramètres optionnels :
  - `patientId` (optionnel)
  - `providerId` (optionnel)
  - `from` (optionnel, format ISO datetime)
  - `to` (optionnel, format ISO datetime)
  - `limit` (optionnel, nombre max de résultats)

---

### 4.2. Opérations CRUD (`/api/records`)

| Méthode | Endpoint | Description | Auth | Rôle Requis |
|---------|----------|-------------|------|-------------|
| `GET` | `/api/records` | Récupérer tous les dossiers | ✅ | - |
| `GET` | `/api/records/{id}` | Récupérer un dossier par ID | ✅ | - |
| `PUT` | `/api/records/{id}` | Mettre à jour un dossier | ✅ | PROVIDER |
| `DELETE` | `/api/records/{id}` | Supprimer un dossier | ✅ | PROVIDER |

**Détails** :
- **GET `/api/records`** : Retourne tous les dossiers médicaux.
- **GET `/api/records/{id}`** : Retourne un dossier médical spécifique par son ID.
- **PUT `/api/records/{id}`** : Met à jour un dossier médical existant. **Nécessite le rôle PROVIDER.**
- **DELETE `/api/records/{id}`** : Supprime un dossier médical. **Nécessite le rôle PROVIDER.**

**Note importante** :
- La création de dossiers médicaux se fait uniquement via RabbitMQ depuis Provider-Service (POST `/api/providers/medical-records`).

---

## 5. Résumé par méthode HTTP

### GET (Lecture)

| Service | Nombre | Endpoints |
|---------|--------|-----------|
| Patient-Service | 5 | `/api/patient/profile-status`, `/api/patient/profile`, `/api/patient/medical-history`, `/api/notifications`, `/api/notifications/{requestId}` |
| Provider-Service | 6 | `/api/auth/profile`, `/api/auth/providers/list`, `/api/providers/patients/all`, `/api/providers/patients`, `/api/providers/patients/{patientId}` |
| Request-Service | 6 | `/api/requests/patient/{patientId}`, `/api/requests`, `/api/requests/status/{status}`, `/api/requests/provider/{providerId}`, `/api/requests/provider/{providerId}/targeted`, `/api/requests/{requestId}` |
| Medicalrecord-Service | 4 | `/api/records/read/patient/{patientId}`, `/api/records/read/search`, `/api/records`, `/api/records/{id}` |
| **TOTAL** | **21** | |

### POST (Création)

| Service | Nombre | Endpoints |
|---------|--------|-----------|
| Patient-Service | 3 | `/api/auth/register`, `/api/requests`, `/api/requests/{requestId}/message` |
| Provider-Service | 4 | `/api/auth/register`, `/api/providers/patients/sync`, `/api/providers/patient/{patientId}/activate`, `/api/providers/patient/{patientId}/suspend`, `/api/providers/medical-records` |
| Request-Service | 1 | `/api/requests/{requestId}/messages` |
| Medicalrecord-Service | 0 | *(Création via RabbitMQ uniquement)* |
| **TOTAL** | **8** | |

### PUT (Mise à jour)

| Service | Nombre | Endpoints |
|---------|--------|-----------|
| Patient-Service | 1 | `/api/patient/complete-profile` |
| Provider-Service | 2 | `/api/auth/complete-profile`, `/api/providers/patients/{patientId}/status` |
| Request-Service | 1 | `/api/requests/{requestId}/respond` |
| Medicalrecord-Service | 1 | `/api/records/{id}` |
| **TOTAL** | **5** | |

### DELETE (Suppression)

| Service | Nombre | Endpoints |
|---------|--------|-----------|
| Medicalrecord-Service | 1 | `/api/records/{id}` |
| **TOTAL** | **1** | |

---

## 6. Authentification requise

### Endpoints publics (sans authentification)

| Service | Endpoints |
|---------|-----------|
| Patient-Service | `POST /api/auth/register`, `POST /api/auth/login` |
| Provider-Service | `POST /api/auth/register`, `POST /api/auth/login`, `GET /api/auth/providers/list` |
| Request-Service | Aucun |
| Medicalrecord-Service | Aucun |

### Endpoints nécessitant le rôle PATIENT

| Service | Endpoints |
|---------|-----------|
| Patient-Service | Tous les endpoints `/api/patient/*`, `/api/requests/*`, `/api/notifications/*` |
| Request-Service | `GET /api/requests/patient/{patientId}` (pour voir ses propres demandes) |

### Endpoints nécessitant le rôle PROVIDER

| Service | Endpoints |
|---------|-----------|
| Provider-Service | Tous les endpoints `/api/providers/*`, `/api/auth/profile`, `/api/auth/complete-profile` |
| Request-Service | Tous les endpoints `/api/requests/*` (sauf GET `/api/requests/patient/{patientId}` qui est accessible aux deux) |
| Medicalrecord-Service | `PUT /api/records/{id}`, `DELETE /api/records/{id}` |

### Endpoints nécessitant un compte ACTIVE

Certains endpoints du Patient-Service nécessitent que le compte patient soit **ACTIVE** :
- `GET /api/patient/medical-history`
- `POST /api/requests`
- `POST /api/requests/{requestId}/message`
- `GET /api/notifications`
- `GET /api/notifications/{requestId}`

Si le compte n'est pas ACTIVE, ces endpoints retournent **403 Forbidden** avec un message explicatif.

---

## 7. Communication inter-services

### RabbitMQ

Les services communiquent via RabbitMQ pour :

1. **Patient-Service → Provider-Service** :
   - Publication de nouveaux patients lors de l'enregistrement

2. **Patient-Service → Request-Service** :
   - Publication de nouvelles demandes

3. **Request-Service → Patient-Service** :
   - Publication des réponses aux demandes (notifications)

4. **Provider-Service → Medicalrecord-Service** :
   - Publication de nouveaux dossiers médicaux

5. **Provider-Service → Patient-Service** :
   - Synchronisation des patients
   - Mise à jour des statuts des patients

### REST API (Inter-services)

- **Patient-Service → Medicalrecord-Service** :
  - `GET /api/records/read/patient/{patientId}` (via Feign/RestTemplate)

---

## 8. Codes de réponse HTTP

| Code | Signification | Utilisation |
|------|---------------|-------------|
| `200 OK` | Succès | Opérations réussies |
| `201 Created` | Ressource créée | *(Non utilisé actuellement)* |
| `202 Accepted` | Requête acceptée | `POST /api/requests` (Patient-Service) |
| `204 No Content` | Succès sans contenu | `DELETE /api/records/{id}` |
| `400 Bad Request` | Requête invalide | Erreurs de validation, données manquantes |
| `401 Unauthorized` | Non authentifié | Token JWT manquant ou invalide |
| `403 Forbidden` | Accès refusé | Rôle insuffisant, compte non ACTIVE |
| `404 Not Found` | Ressource non trouvée | ID inexistant |
| `500 Internal Server Error` | Erreur serveur | Erreurs internes |

---

## 9. Format des réponses

### Succès
```json
{
  "data": {...},
  "message": "Opération réussie"
}
```

### Erreur (Patient-Service - Compte non activé)
```json
{
  "error": "Account not activated",
  "message": "Votre compte n'est pas encore activé. Veuillez attendre l'approbation du prestataire de santé.",
  "accountStatus": "PENDING",
  "statusCode": 403
}
```

### Erreur (Validation)
```json
{
  "error": "Le statut est requis"
}
```

---

## 10. Notes importantes

1. **JWT Token** : Tous les endpoints authentifiés nécessitent un token JWT dans le header `Authorization: Bearer <token>`.

2. **CORS** : Les services Patient-Service et Provider-Service autorisent toutes les origines (`@CrossOrigin(origins = "*")`).

3. **Swagger/OpenAPI** : Tous les services utilisent Swagger/OpenAPI pour la documentation. Accédez à `/swagger-ui.html` ou `/swagger-ui/index.html` pour chaque service.

4. **Base de données** :
   - Patient-Service : MongoDB
   - Provider-Service : MongoDB
   - Request-Service : MongoDB
   - Medicalrecord-Service : MongoDB

5. **Synchronisation** : La synchronisation des patients entre Patient-Service et Provider-Service se fait via RabbitMQ. Si un provider ne voit pas de patients, il peut déclencher une synchronisation manuelle via `POST /api/providers/patients/sync`.

---

**Fin du rapport**


