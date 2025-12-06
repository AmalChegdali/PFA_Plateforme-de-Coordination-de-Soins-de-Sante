# 📧 Guide de Configuration Email

Ce guide explique comment configurer l'envoi d'emails pour les notifications aux patients.

## ⚠️ IMPORTANT : Différence entre les emails

**Il y a DEUX types d'emails différents :**

1. **Email du SERVEUR SMTP (Expéditeur)** → Celui que vous configurez dans `application.properties`
   - C'est le compte email qui **ENVOIE** les emails
   - Exemple : `plateforme-soins@gmail.com`
   - C'est celui-ci que vous devez configurer

2. **Email du PATIENT (Destinataire)** → Stocké dans MongoDB
   - C'est l'email du patient qui **REÇOIT** les emails
   - Exemple : `amal@gmail.com` (l'email que le patient a utilisé lors de l'inscription)
   - Celui-ci est déjà dans la base de données, vous n'avez rien à configurer

**Exemple concret :**
- Vous configurez : `spring.mail.username=plateforme-soins@gmail.com`
- Un patient s'inscrit avec : `amal@gmail.com`
- Quand le provider répond, le système :
  - **ENVOIE depuis** : `plateforme-soins@gmail.com` (votre serveur SMTP)
  - **VERS** : `amal@gmail.com` (l'email du patient dans MongoDB)

## 🎯 Pourquoi configurer l'email ?

Lorsqu'un provider répond à une demande d'un patient, le système envoie automatiquement un email de notification au patient. Pour que cela fonctionne, vous devez configurer un compte email.

---

## 📋 Méthode 1 : Configuration dans `application.properties` (Simple)

### Étape 1 : Ouvrir le fichier
Ouvrez le fichier : `Patient-Service/src/main/resources/application.properties`

### Étape 2 : Décommenter et remplir les lignes
Trouvez la section `# EMAIL CONFIGURATION` et modifiez comme suit :

```properties
# Décommentez ces lignes et remplacez par vos informations
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=votre-email@gmail.com
spring.mail.password=votre-mot-de-passe-app
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### ⚠️ Important pour Gmail
Si vous utilisez Gmail, vous **NE POUVEZ PAS** utiliser votre mot de passe Gmail normal. Vous devez créer un **"Mot de passe d'application"** :

1. Allez sur https://myaccount.google.com/security
2. Activez la **Validation en 2 étapes** (si ce n'est pas déjà fait)
3. Allez dans **Mots de passe des applications**
4. Créez un nouveau mot de passe d'application
5. Utilisez ce mot de passe (16 caractères) dans la configuration

---

## 📋 Méthode 2 : Variables d'environnement (Recommandé - Plus sécurisé)

Cette méthode est plus sécurisée car elle évite de mettre le mot de passe dans le fichier.

### Sur Windows (PowerShell) :
```powershell
$env:MAIL_USERNAME="votre-email@gmail.com"
$env:MAIL_PASSWORD="votre-mot-de-passe-app"
```

### Sur Windows (CMD) :
```cmd
set MAIL_USERNAME=votre-email@gmail.com
set MAIL_PASSWORD=votre-mot-de-passe-app
```

### Sur Linux/Mac :
```bash
export MAIL_USERNAME="votre-email@gmail.com"
export MAIL_PASSWORD="votre-mot-de-passe-app"
```

**Note** : Ces variables sont temporaires. Pour les rendre permanentes :
- **Windows** : Ajoutez-les dans les Variables d'environnement système
- **Linux/Mac** : Ajoutez-les dans `~/.bashrc` ou `~/.zshrc`

---

## 🔧 Configuration pour d'autres serveurs email

### Outlook/Hotmail :
```properties
spring.mail.host=smtp-mail.outlook.com
spring.mail.port=587
spring.mail.username=votre-email@outlook.com
spring.mail.password=votre-mot-de-passe
```

### Yahoo :
```properties
spring.mail.host=smtp.mail.yahoo.com
spring.mail.port=587
spring.mail.username=votre-email@yahoo.com
spring.mail.password=votre-mot-de-passe-app
```

---

## ✅ Vérification

Une fois configuré, redémarrez l'application. Lorsqu'un provider répond à une demande :

1. ✅ Un email sera envoyé au patient
2. ✅ Vous verrez dans les logs : `✅ Email envoyé avec succès à : patient@email.com`
3. ✅ Si l'email n'est pas configuré, vous verrez : `⚠️ Service email non configuré - Email non envoyé`

---

## 🚫 Que se passe-t-il si l'email n'est pas configuré ?

**Rien de grave !** Le système continue de fonctionner normalement :
- ✅ Les réponses sont toujours enregistrées
- ✅ Les notifications sont loggées
- ❌ Seuls les emails ne seront pas envoyés

---

## 📝 Exemple complet pour Gmail

Dans `application.properties`, remplacez :
```properties
spring.mail.username=${MAIL_USERNAME:}
spring.mail.password=${MAIL_PASSWORD:}
```

Par :
```properties
spring.mail.username=mon-email@gmail.com
spring.mail.password=abcd efgh ijkl mnop
```
*(Utilisez le mot de passe d'application de 16 caractères, sans espaces)*

---

## 🆘 Problèmes courants

### Erreur : "Authentication failed"
- Vérifiez que vous utilisez un **mot de passe d'application** (pas votre mot de passe Gmail)
- Vérifiez que la validation en 2 étapes est activée

### Erreur : "Connection timeout"
- Vérifiez votre connexion internet
- Vérifiez que le port 587 n'est pas bloqué par un firewall

### Aucune erreur mais pas d'email reçu
- Vérifiez le dossier spam
- Vérifiez les logs pour voir si l'email a été envoyé
- Vérifiez que l'adresse email du patient est correcte

