# 🌿 Smart Skin Care Analyzer — Backend

Backend Spring Boot complet pour l'application **SkinCare Smart Analyzer**.

---

## 🗂️ Architecture du projet

```
src/main/java/com/smart_skin/smart_skin_app_backend/
├── config/
│   ├── ApplicationConfig.java      # UserDetailsService bean
│   ├── CloudinaryConfig.java       # Cloudinary bean
│   ├── JacksonConfig.java          # ObjectMapper bean
│   └── SecurityConfig.java         # Spring Security + CORS + JWT
│
├── controllers/
│   ├── AuthController.java          # POST /api/auth/*
│   ├── UserController.java          # GET/PUT /api/users/me
│   ├── SkinAnalysisController.java  # POST /api/analyses
│   ├── AiCoachController.java       # POST /api/coach/chat
│   ├── ProductScanController.java   # POST /api/products/scan
│   ├── DashboardController.java     # GET  /api/dashboard
│   ├── NotificationController.java  # GET  /api/notifications
│   └── SkinReportController.java    # POST /api/reports/generate
│
├── dto/                             # Request/Response DTOs
├── enums/                           # SkinType, Severity, NotificationType
├── exception/                       # GlobalExceptionHandler, custom exceptions
├── mappers/                         # MapStruct UserMapper
├── models/                          # JPA Entities
│   ├── User.java
│   ├── SkinAnalysis.java
│   ├── SkinImage.java
│   ├── SkinProblem.java
│   ├── Recommandation.java
│   ├── SkinReport.java
│   ├── CoachMessage.java
│   ├── ProductScan.java
│   └── Notification.java
├── repos/                           # Spring Data JPA Repositories
├── security/                        # JwtUtil + JwtAuthenticationFilter
└── services/
    ├── UserService.java
    ├── SkinAnalysisService.java
    ├── AiCoachService.java
    ├── ProductScanService.java
    ├── DashboardService.java
    ├── NotificationService.java
    ├── SkinReportService.java
    ├── CloudinaryService.java
    └── imp/                         # Implementations
```

---

## ⚙️ Configuration requise

### Variables d'environnement (`.env` ou `application.yml`)

| Variable | Description |
|---|---|
| `DB_USERNAME` | PostgreSQL username |
| `DB_PASSWORD` | PostgreSQL password |
| `JWT_SECRET` | Clé secrète JWT (Base64, min 256 bits) |
| `CLOUDINARY_CLOUD_NAME` | Nom du cloud Cloudinary |
| `CLOUDINARY_API_KEY` | API Key Cloudinary |
| `CLOUDINARY_API_SECRET` | API Secret Cloudinary |
| `OPENAI_API_KEY` | Clé API OpenAI (GPT-4o-mini + Vision) |
| `MAIL_USERNAME` | Email Gmail pour reset password |
| `MAIL_PASSWORD` | App Password Gmail |

### Base de données PostgreSQL
```sql
CREATE DATABASE smart_skin_db;
```

---

## 🚀 Lancement

```bash
# Avec Maven
mvn spring-boot:run

# Ou avec variables d'env
OPENAI_API_KEY=sk-... DB_PASSWORD=postgres mvn spring-boot:run
```

---

## 📡 API Endpoints

### 🔐 Auth — `/api/auth`

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Inscription |
| POST | `/api/auth/login` | Connexion |
| POST | `/api/auth/refresh` | Rafraîchir le token |
| POST | `/api/auth/forgot-password` | Demande reset mot de passe |
| POST | `/api/auth/reset-password` | Réinitialiser mot de passe |

**Register body:**
```json
{
  "firstName": "Aya",
  "lastName": "Bensalem",
  "email": "aya@example.com",
  "password": "motdepasse123"
}
```

**Login response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJ...",
    "refreshToken": "eyJ...",
    "tokenType": "Bearer",
    "user": { "id": 1, "firstName": "Aya", ... }
  }
}
```

---

### 👤 Utilisateur — `/api/users`

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/api/users/me` | Profil courant |
| PUT | `/api/users/me` | Modifier profil |
| POST | `/api/users/me/onboarding` | Compléter l'onboarding |
| POST | `/api/users/me/avatar` | Upload photo profil (multipart) |
| PUT | `/api/users/me/password` | Changer mot de passe |
| PUT | `/api/users/me/fcm-token` | Token notifications push |

**Onboarding body:**
```json
{
  "skinType": "MIXTE",
  "skinConcerns": "acne,taches",
  "ethnicity": "nord-africaine",
  "routinePreference": "moderate",
  "effortLevel": "medium",
  "sunExposure": "moderate",
  "ingredientsToAvoid": "alcool,parfum"
}
```

---

### 🔬 Analyses cutanées — `/api/analyses`

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/analyses` | Analyser une image (multipart `image`) |
| GET | `/api/analyses` | Historique paginé |
| GET | `/api/analyses/recent?limit=5` | Analyses récentes |
| GET | `/api/analyses/{id}` | Détail d'une analyse |
| DELETE | `/api/analyses/{id}` | Supprimer une analyse |

**Flutter — appel analyse:**
```dart
var request = http.MultipartRequest('POST', Uri.parse('$baseUrl/api/analyses'));
request.headers['Authorization'] = 'Bearer $token';
request.files.add(await http.MultipartFile.fromPath('image', imagePath));
var response = await request.send();
```

**Response exemple:**
```json
{
  "data": {
    "id": 42,
    "imageUrl": "https://res.cloudinary.com/...",
    "detectedSkinType": "MIXTE",
    "overallScore": 68,
    "hydrationScore": 62,
    "acneScore": 45,
    "pigmentationScore": 74,
    "wrinkleScore": 82,
    "poreScore": 55,
    "analysisDescription": "Votre peau présente...",
    "detectedProblems": [
      { "problemType": "acne", "severity": "LEGERE", "zone": "front", "confidence": 0.85 }
    ],
    "recommandations": [
      { "category": "nettoyant", "title": "Nettoyant doux", "activeIngredient": "acide salicylique", "priority": 1 }
    ],
    "analyzedAt": "2024-11-01T10:30:00"
  }
}
```

---

### 🤖 AI Skin Coach — `/api/coach`

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/coach/chat` | Envoyer un message |
| GET | `/api/coach/history?sessionId=xxx` | Historique session |
| DELETE | `/api/coach/session?sessionId=xxx` | Effacer session |

**Chat body:**
```json
{
  "message": "Mon acné s'aggrave le soir, que faire ?",
  "sessionId": "session-uuid-optionnel"
}
```

---

### 🧴 Scan Produit — `/api/products`

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/products/scan` | Scanner par ingrédients (JSON) |
| POST | `/api/products/scan/image` | Scanner étiquette (multipart) |
| GET | `/api/products/history` | Historique scans |

**Scan par ingrédients:**
```json
{
  "productName": "CeraVe Hydratant",
  "brand": "CeraVe",
  "ingredients": "Aqua, Glycerin, Niacinamide, Ceramide NP..."
}
```

---

### 📊 Dashboard — `/api/dashboard`

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/api/dashboard` | Stats + graphiques + dernière analyse |

**Response:**
```json
{
  "data": {
    "totalAnalyses": 12,
    "averageScore": 71.5,
    "scoreEvolution": +3.2,
    "currentStreak": 4,
    "lastAnalysis": { ... },
    "scoreHistory": [ { "date": "...", "overallScore": 68 } ],
    "topProblems": [ { "problemType": "acne", "count": 8 } ],
    "latestRecommandations": [ ... ],
    "unreadNotifications": 2
  }
}
```

---

### 🔔 Notifications — `/api/notifications`

| Méthode | Endpoint | Description |
|---|---|---|
| GET | `/api/notifications` | Liste paginée |
| GET | `/api/notifications/unread-count` | Nombre non lues |
| PUT | `/api/notifications/read-all` | Tout marquer lu |
| PUT | `/api/notifications/{id}/read` | Marquer une notification lue |

---

### 📋 Rapports — `/api/reports`

| Méthode | Endpoint | Description |
|---|---|---|
| POST | `/api/reports/generate?from=2024-01-01&to=2024-01-31` | Générer rapport |
| GET | `/api/reports` | Liste rapports |
| GET | `/api/reports/{id}` | Détail rapport |

---

## 🧠 Flux IA

```
Image Flutter
    ↓
POST /api/analyses (multipart)
    ↓
Cloudinary upload → URL publique
    ↓
OpenAI GPT-4o Vision
  - Prompt structuré en JSON
  - Contexte utilisateur (type de peau, ethnicity...)
    ↓
Parse JSON → SkinAnalysis + SkinProblems + Recommandations
    ↓
Sauvegarde PostgreSQL
    ↓
Notification créée
    ↓
Response Flutter
```

---

## 🔔 Notifications automatiques (Scheduler)

- **Rappel hebdomadaire** : Chaque lundi à 9h → tous les utilisateurs avec notifications activées
- **Analyse terminée** : après chaque analyse réussie
- **Rapport prêt** : après génération d'un rapport

---

## 📱 Intégration Flutter

Toutes les réponses sont enveloppées dans :
```json
{
  "success": true,
  "message": "...",
  "data": { ... },
  "timestamp": "2024-11-01T10:00:00"
}
```

Header requis pour routes protégées :
```
Authorization: Bearer <accessToken>
```

---

## 🛠️ Stack technique

| Composant | Technologie |
|---|---|
| Framework | Spring Boot 3.2 |
| Sécurité | Spring Security + JWT (jjwt 0.12) |
| Base de données | PostgreSQL + Spring Data JPA |
| Stockage images | Cloudinary |
| Intelligence Artificielle | OpenAI GPT-4o-mini (Vision + Chat) |
| Mapping | MapStruct |
| Build | Maven |
| Java | 17 |
