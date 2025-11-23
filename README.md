# Plateforme de Coordination de Soins de Santé

## 📝 Table des matières
1. Contexte et objectifs
2. Description du système
3. Fonctionnalités principales
4. Architecture Technique
5. Documentation des Endpoints (Provisoire)

## 1. Contexte et objectifs

### Contexte général
Avec la digitalisation du secteur médical, les patients recherchent des plateformes fiables pour interagir avec les professionnels de santé. Les médecins, quant à eux, ont besoin d’outils efficaces pour gérer le suivi médical et les dossiers patients.

Problèmes identifiés :
- Difficulté de communication patient–médecin
- Manque de coordination entre acteurs de santé
- Dispersion des dossiers médicaux
- Besoin de sécurisation élevée (confidentialité, permissions)

### Objectifs du projet
Créer une plateforme centralisée permettant :
- Une meilleure interaction entre patients et prestataires
- Une gestion unifiée et sécurisée des dossiers médicaux
- Une communication fluide entre services via microservices

---

## 2. Description du système

- **Type** : Application Web  
- **Langues** : Arabe, Français, Anglais  
- **Technologies** :
  - Frontend : React ou Angular
  - Backend : Spring Boot (microservices) + RabbitMQ
  - Base de données : MySQL / PostgreSQL / MongoDB
  - Hébergement : AWS / Azure / GCP

---

## 3. Fonctionnalités principales

### Patients
- Création de compte sécurisé (JWT, RBAC)
- Authentification & gestion profil
- Consultation dossier médical
- Soumission des demandes (rendez‑vous, consultations…)
- Téléchargement de documents (ordonnances, rapports PDF)
- Communication minimale avec médecin

### Médecins / Spécialistes
- Création & gestion du compte professionnel
- Authentification sécurisée (rôle PROVIDER)
- Gestion des spécialités, horaires, localisation
- Accès aux patients et dossiers médicaux
- Traitement des demandes via RabbitMQ
- Gestion du statut des patients
- Téléversement de documents médicaux

### Fonctionnalités transversales
- Sécurité : JWT + RBAC
- RabbitMQ pour échanges asynchrones
- Audit / logs
- Notifications

---

## 4. Architecture Technique

Architecture microservices :
- **PatientService** (8080)
- **ProviderService** (8081)
- **MedicalRecordService** (8082)
- **RabbitMQ** pour la communication interne
- **Frontend** consommant les API REST

RabbitMQ gère :
- Les événements d’inscription
- L’activation de comptes
- Les mises à jour de dossiers médicaux

---

## 5. Documentation Technique des Endpoints

### PatientService (8080)

#### Authentication
| Méthode | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Inscription patient |
| POST | /api/auth/login | Connexion |
| GET | /api/auth/status | Statut |
| GET | /api/auth/profile | Profil |

#### Dossier médical
| Méthode | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/auth/medical-history | Historique complet |
| GET | /api/auth/medical-history/recent?days=30 | Historique récent |
| GET | /api/auth/medical-history/latest | Dernière entrée |

#### Demandes
| Méthode | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/requests/create | Créer une demande |
| GET | /api/requests/my-requests | Voir demandes |
| DELETE | /api/requests/{id} | Annuler |

---

### ProviderService (8081)

#### Authentication
| Méthode | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Inscription médecin |
| POST | /api/auth/login | Connexion |

#### Gestion des patients
| Méthode | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/provider/pending-patients | Patients à activer |
| GET | /api/provider/my-patients | Liste des patients |
| GET | /api/provider/patient/{id} | Détails du patient |
| PUT | /api/provider/patient/{id}/profile | Modifier |
| POST | /api/provider/patient/{id}/activate | Activer |
| POST | /api/provider/patient/{id}/suspend | Suspendre |
| POST | /api/provider/patient/{id}/disable | Désactiver |

---

### MedicalRecordService (8082)

#### Dossier médical
| Méthode | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/records | Créer dossier |
| GET | /api/records/{patientId} | Liste des dossiers |
| GET | /api/records/{patientId}/latest | Dernier dossier |
| PUT | /api/records/{recordId} | Modifier |
| DELETE | /api/records/{recordId} | Supprimer |

#### Pièces jointes
| Méthode | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/records/{recordId}/attachments | Ajouter fichier |
| GET | /api/records/{recordId}/attachments | Lister |
| GET | /api/records/{recordId}/attachments/{fileId} | Télécharger |
| DELETE | /api/records/{recordId}/attachments/{fileId} | Supprimer |

