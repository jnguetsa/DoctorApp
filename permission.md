Voici un cours clair et structuré sur **les méthodes et fonctions** utilisées dans ton code Spring Security (configuration avec `InMemoryAuthentication`).

On va les expliquer une par une, avec leur rôle, où elles sont utilisées et pourquoi elles sont importantes.

### 1. Classes et annotations principales

| Annotation / Classe                     | Rôle principal                                                                 | Où dans ton code ?                     |
|-----------------------------------------|--------------------------------------------------------------------------------|----------------------------------------|
| `@Configuration`                        | Indique que cette classe contient des définitions de beans                     | `SecurityConfig`                       |
| `@EnableWebSecurity`                    | Active la sécurité web Spring Security (filtre de sécurité principal)         | `SecurityConfig`                       |
| `@EnableMethodSecurity`                 | Active la sécurité au niveau des méthodes (`@PreAuthorize`, `@Secured`, etc.) | `SecurityConfig` (prePostEnabled=true) |
| `HttpSecurity`                          | Objet central pour configurer la sécurité HTTP (form login, authorize, etc.)   | Dans `securityFilterChain()`           |
| `InMemoryUserDetailsManager`            | Gestionnaire d'utilisateurs en mémoire (pas de base de données)                | Bean `inMemoryUserDetailsManager()`    |

### 2. Méthodes importantes de `HttpSecurity`

C’est la partie la plus importante de la configuration de sécurité web.

| Méthode                                      | Ce qu’elle fait                                                                                 | Exemple dans ton code                              | Remarque importante                                      |
|----------------------------------------------|--------------------------------------------------------------------------------------------------|----------------------------------------------------|------------------------------------------------------------------|
| `formLogin()`                                | Active le formulaire de login par défaut (page /login)                                          | `http.formLogin();`                                | On peut personnaliser `.loginPage("/mon-login")` etc.           |
| `authorizeHttpRequests()`                    | Permet de définir les règles d’autorisation par URL                                             | `.authorizeHttpRequests()`                         | Remplace l’ancien `.authorizeRequests()` depuis Spring Security 5.8+ |
| `requestMatchers()`                          | Définit quelles URLs on veut protéger / autoriser                                               | `.requestMatchers("/admin/**")`                    | Accepte aussi `AntPathRequestMatcher`, MVC patterns, etc.        |
| `hasRole("ROLE_XXX")`                        | Autorise uniquement les utilisateurs ayant le rôle spécifié (Spring ajoute "ROLE_" automatiquement) | `.hasRole("ADMIN")`                                | Écrire `"ADMIN"` → Spring cherche `ROLE_ADMIN`                   |
| `hasAnyRole("A", "B")`                       | Autorise si l’utilisateur a au moins un des rôles                                               | (non utilisé ici)                                  | Variante pratique                                               |
| `authenticated()`                            | Demande simplement que l’utilisateur soit connecté (n’importe quel rôle)                        | `.anyRequest().authenticated()`                    | Règle très courante pour tout le reste                           |
| `anyRequest()`                               | Règle catch-all : s’applique à toutes les requêtes non matchées avant                           | `.anyRequest().authenticated()`                    | Toujours mettre en dernier !                                     |
| `exceptionHandling()`                        | Configure le comportement en cas d’erreur d’autorisation ou d’authentification                 | `.exceptionHandling()`                             | —                                                                |
| `accessDeniedPage("/notAuthorized")`         | Page affichée quand l’utilisateur est authentifié mais n’a pas les droits suffisants            | `.accessDeniedPage("/notAuthorized")`              | Peut aussi utiliser `.accessDeniedHandler()`                     |

Ordre important des règles (ton code est correct) :

```text
1. /admin/**    → doit avoir ROLE_ADMIN
2. /user/**     → doit avoir ROLE_USER
3. tout le reste → doit être authentifié
```

### 3. Construction des utilisateurs en mémoire

Méthodes de la classe `User` (org.springframework.security.core.userdetails.User)

| Méthode                              | Rôle                                              | Exemple dans ton code                                      |
|--------------------------------------|---------------------------------------------------|------------------------------------------------------------|
| `withUsername(String)`               | Définit le nom d’utilisateur                      | `User.withUsername("admin")`                               |
| `password(String)`                   | Définit le mot de passe (doit être déjà encodé !) | `.password(passwordEncoder.encode("1234"))`                |
| `roles(String... roles)`             | Ajoute des rôles (Spring ajoute automatiquement "ROLE_") | `.roles("USER", "ADMIN")`                                  |
| `build()`                            | Crée l’objet `UserDetails` final                  | `.build()`                                                 |

Exemple complet d’un utilisateur :

```java
User.withUsername("admin")
    .password(passwordEncoder.encode("1234"))
    .roles("USER", "ADMIN")
    .build()
```

→ devient en interne : utilisateur `admin` avec mot de passe encodé + rôles `ROLE_USER` et `ROLE_ADMIN`

### 4. PasswordEncoder (très important)

| Méthode / Bean                        | Rôle                                                                 |
|---------------------------------------|----------------------------------------------------------------------|
| `BCryptPasswordEncoder`               | Implémentation recommandée – hachage fort + sel aléatoire           |
| `passwordEncoder.encode("1234")`      | Transforme "1234" en hachage du style `$2a$10$...`                   |
| `passwordEncoder.matches("1234", hash)` | Vérifie si le mot de passe entré correspond au hash stocké (utilisé automatiquement par Spring) |

**Règle d’or : ne jamais stocker le mot de passe en clair !**

### 5. Résumé – Les méthodes les plus utilisées dans ce type de config

- `HttpSecurity`
    - `formLogin()`
    - `authorizeHttpRequests()`
    - `requestMatchers()`
    - `hasRole() / hasAnyRole()`
    - `authenticated()`
    - `anyRequest()`
    - `exceptionHandling().accessDeniedPage()`

- `User` (pour in-memory)
    - `withUsername()`
    - `password()`
    - `roles()`
    - `build()`

- Autres beans importants
    - `PasswordEncoder` → `BCryptPasswordEncoder`
    - `InMemoryUserDetailsManager`

### Bonus – Équivalent plus moderne (Spring Boot 3 / Security 6)

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/user/**").hasRole("USER")
            .anyRequest().authenticated()
        )
        .formLogin(Customizer.withDefaults())
        .exceptionHandling(ex -> ex.accessDeniedPage("/notAuthorized"));
        
    return http.build();
}
```

C’est plus concis et c’est la syntaxe recommandée aujourd’hui.

Tu veux qu’on approfondisse une partie en particulier ?  
(ex : différence hasRole vs hasAuthority, comment ajouter logout, comment personnaliser le login, etc.) 😄


Voici une explication **claire et pratique** de la différence entre **`hasRole`** et **`hasAuthority`** dans Spring Security.

### 1. La différence fondamentale

| Critère               | `hasRole("ADMIN")`                              | `hasAuthority("ADMIN")`                          |
|-----------------------|--------------------------------------------------|---------------------------------------------------|
| Préfixe automatique   | Oui – Spring **ajoute automatiquement** "ROLE_" | Non – prend exactement la chaîne telle quelle    |
| Chaîne attendue       | `"ADMIN"` → cherche `ROLE_ADMIN`                 | `"ADMIN"` → cherche `ROLE_ADMIN` ou `ADMIN` (selon ce que tu as mis) |
| Usage le plus courant | Rôles classiques (gestion des rôles)             | Autorités / permissions fines (RBAC avancé)       |
| Convention            | On écrit **sans** le préfixe ROLE_               | On écrit **exactement** la valeur stockée         |
| Exemple typique       | `.hasRole("ADMIN")`                              | `.hasAuthority("ROLE_ADMIN")` ou `.hasAuthority("create:patient")` |

### 2. Ce que Spring fait en interne

Quand tu utilises :

```java
.hasRole("ADMIN")
```

Spring Security transforme automatiquement :

```
"ADMIN"  →  "ROLE_ADMIN"
```

Et vérifie si l'utilisateur possède l'autorité **ROLE_ADMIN**.

Quand tu utilises :

```java
.hasAuthority("ADMIN")
```

Spring **ne touche pas** à la chaîne et vérifie **exactement** si l'utilisateur a l'autorité `"ADMIN"`.

### 3. Exemples concrets

#### Cas classique : utilisation de rôles

```java
// Configuration
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasRole("ADMIN")           // → cherche ROLE_ADMIN
    .requestMatchers("/manager/**").hasRole("MANAGER")      // → cherche ROLE_MANAGER
    .anyRequest().authenticated()
);

// Création utilisateur
User.withUsername("chef")
    .password(encoder.encode("1234"))
    .roles("ADMIN", "MANAGER")           // → devient ROLE_ADMIN et ROLE_MANAGER
    .build();
```

→ `hasRole("ADMIN")` fonctionne parfaitement ici.

#### Cas avec permissions fines (pas de préfixe ROLE_)

```java
// Configuration
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/patients/write").hasAuthority("PATIENT_WRITE")
    .requestMatchers("/patients/read").hasAuthority("PATIENT_READ")
    .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")     // on met le préfixe explicitement
);

// Création utilisateur
User.withUsername("infirmier")
    .password(encoder.encode("1234"))
    .authorities("PATIENT_READ", "PATIENT_WRITE")     // pas de ROLE_
    .build();
```

Ici `hasRole()` ne fonctionnerait **pas** (car il cherche `ROLE_PATIENT_READ` qui n'existe pas).

### 4. Tableau récapitulatif rapide

| Tu veux autoriser...         | Tu utilises...               | Tu écris dans le code          | Ce que l'utilisateur doit avoir       |
|------------------------------|------------------------------|--------------------------------|----------------------------------------|
| Rôle classique               | `hasRole()`                  | `"ADMIN"`                      | `ROLE_ADMIN`                           |
| Rôle classique (explicite)   | `hasAuthority()`             | `"ROLE_ADMIN"`                 | `ROLE_ADMIN`                           |
| Permission fine / scope      | `hasAuthority()`             | `"PATIENT_CREATE"`             | `PATIENT_CREATE`                       |
| Permission avec préfixe      | `hasAuthority()`             | `"ROLE_SUPERUSER"`             | `ROLE_SUPERUSER`                       |
| Plusieurs rôles              | `hasAnyRole()`               | `"ADMIN", "MANAGER"`           | `ROLE_ADMIN` ou `ROLE_MANAGER`         |
| Plusieurs autorités          | `hasAnyAuthority()`          | `"PATIENT_READ", "PATIENT_WRITE"` | l'une des deux                         |

### 5. Règle pratique à retenir (la plus utilisée en 2024/2025)

- Tu travailles avec **les rôles classiques** (ADMIN, USER, MANAGER…) → utilise **`hasRole("ADMIN")`**
- Tu travailles avec des **permissions granulaires** (read:patient, write:appointment, manage:users…) → utilise **`hasAuthority("...")`**
- Tu mélanges les deux → utilise **toujours** `hasAuthority()` et mets explicitement `ROLE_` quand c’est un rôle

Exemple mixte très courant :

```java
.requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
.requestMatchers("/api/patients/**").hasAuthority("PATIENT_MANAGE")
.requestMatchers("/api/reports/**").hasAnyAuthority("REPORT_READ", "REPORT_WRITE")
```

### 6. Petit piège classique

Ce code **ne fonctionne pas** :

```java
.roles("ADMIN")                     // → crée ROLE_ADMIN
.hasAuthority("ADMIN")              // → cherche "ADMIN" → échec !
```

Correction :

```java
.roles("ADMIN")                     // → ROLE_ADMIN
.hasRole("ADMIN")                   // OK (ajoute ROLE_ automatiquement)
```

ou

```java
.authorities("ROLE_ADMIN")
.hasAuthority("ROLE_ADMIN")         // OK
```

Tu veux un exemple complet avec `@PreAuthorize` sur les méthodes (hasRole vs hasAuthority) ?






Voici une explication **claire et concise** de la différence entre **`hasAnyRole`** et **`hasAnyAuthority`** dans Spring Security.

### Comparaison rapide

| Critère                  | `hasAnyRole("A", "B")`                          | `hasAnyAuthority("A", "B")`                       |
|--------------------------|--------------------------------------------------|----------------------------------------------------|
| Préfixe automatique      | Oui – ajoute **ROLE_** à chaque valeur          | Non – utilise exactement les chaînes fournies      |
| Ce que Spring vérifie    | `ROLE_A` **ou** `ROLE_B`                         | `"A"` **ou** `"B"` (telles quelles)                |
| Usage principal          | Vérifier plusieurs **rôles** classiques         | Vérifier plusieurs **autorités/permissions** fines |
| Syntaxe courante         | `.hasAnyRole("ADMIN", "MODERATOR")`             | `.hasAnyAuthority("READ", "WRITE")`                |
| Préfixe ROLE_ obligatoire ? | Non (on l’écrit **sans**)                       | Oui si c’est un rôle → écrire `"ROLE_XXX"`         |

### Exemples concrets

#### 1. Avec des rôles classiques (le cas le plus fréquent)

```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPERADMIN")
    .requestMatchers("/moderation/**").hasAnyRole("MODERATOR", "ADMIN")
    ...
);
```

→ Spring vérifie si l’utilisateur a :

- `ROLE_ADMIN` **ou** `ROLE_SUPERADMIN`
- `ROLE_MODERATOR` **ou** `ROLE_ADMIN`

Quand tu crées l’utilisateur :

```java
User.withUsername("alice")
    .password(encoder.encode("pass"))
    .roles("ADMIN", "MODERATOR")     // → ROLE_ADMIN + ROLE_MODERATOR
    .build();
```

→ `hasAnyRole("ADMIN", "MODERATOR")` fonctionne parfaitement.

#### 2. Avec des permissions granulaires (sans préfixe ROLE_)

```java
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/patients/**").hasAnyAuthority("PATIENT_READ", "PATIENT_WRITE")
    .requestMatchers("/reports/export").hasAnyAuthority("REPORT_EXPORT", "ADMIN_REPORT")
);
```

Ici, l’utilisateur doit avoir **au moins une** des autorités listées :

- `PATIENT_READ` **ou** `PATIENT_WRITE`

```java
User.withUsername("doctor")
    .password(encoder.encode("pass"))
    .authorities("PATIENT_READ", "PATIENT_WRITE", "APPOINTMENT_CREATE")
    .build();
```

→ `hasAnyAuthority("PATIENT_READ", "PATIENT_WRITE")` → **OK**

#### 3. Mélange rôles + permissions (cas fréquent)

```java
.requestMatchers("/admin/**")
    .hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPERADMIN")     // on met ROLE_ explicitement

.requestMatchers("/api/patients/**")
    .hasAnyAuthority("PATIENT_MANAGE", "ROLE_ADMIN")      // mixte possible

.requestMatchers("/reports/**")
    .hasAnyRole("ADMIN", "ANALYST")                       // version rôles classiques
```

### Récapitulatif – Quelle méthode choisir ?

| Situation                                      | Méthode recommandée          | Exemple d’écriture                              |
|------------------------------------------------|--------------------------------|-------------------------------------------------|
| Tu vérifies plusieurs **rôles** classiques     | `hasAnyRole()`                 | `.hasAnyRole("ADMIN", "MANAGER")`               |
| Tu vérifies plusieurs **permissions fines**    | `hasAnyAuthority()`            | `.hasAnyAuthority("READ", "WRITE", "DELETE")`   |
| Tu veux mélanger rôles et permissions          | `hasAnyAuthority()`            | `.hasAnyAuthority("ROLE_ADMIN", "REPORT_EXPORT")` |
| Tu veux être **très explicite** (même avec rôles) | `hasAnyAuthority()`         | `.hasAnyAuthority("ROLE_ADMIN", "ROLE_MODERATOR")` |

### Bonus – Équivalent avec `@PreAuthorize`

```java
// Rôles classiques
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")

// Permissions fines
@PreAuthorize("hasAnyAuthority('PATIENT_WRITE', 'PATIENT_DELETE')")

// Mixte
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'REPORT_EXPORT')")
```

### Règle à retenir (la plus utilisée en pratique)

- **Rôles simples** (ADMIN, USER, MANAGER…) → **`hasAnyRole()`**
- **Permissions détaillées** ou mélange → **`hasAnyAuthority()`**

Tu veux voir un exemple complet avec les deux approches côte à côte dans une configuration réelle ?

Voici une explication claire et complète sur **comment configurer le logout** dans Spring Security (versions récentes, Spring Boot 2.x / 3.x et Spring Security 5.x / 6.x).

### Objectifs classiques du logout
- Invalider la session de l'utilisateur
- Supprimer le cookie de session (JSESSIONID)
- Rediriger l'utilisateur vers une page (souvent la page de login ou la page d'accueil)
- (Optionnel) Supprimer les informations de remember-me si activé
- (Optionnel) Invalider le token JWT si vous utilisez une authentification stateless

### 1. Configuration de base du logout (méthode recommandée)

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .requestMatchers("/user/**").hasRole("USER")
            .anyRequest().authenticated()
        )
        
        .formLogin(form -> form
            .loginPage("/login")
            .permitAll()
        )
        
        // ──────────────── Configuration du logout ────────────────
        .logout(logout -> logout
            .logoutUrl("/logout")                     // URL à appeler pour se déconnecter
            .logoutSuccessUrl("/login?logout")        // Où rediriger après déconnexion réussie
            .deleteCookies("JSESSIONID")              // Supprime le cookie de session
            .invalidateHttpSession(true)              // Invalide la session (très important)
            .clearAuthentication(true)                // Efface l'objet Authentication
            .permitAll()                              // Tout le monde peut se déconnecter
        )
        
        .exceptionHandling(ex -> ex
            .accessDeniedPage("/access-denied")
        );

    return http.build();
}
```

### 2. Les options les plus courantes pour `.logout()`

| Méthode                              | Description                                                                 | Valeur par défaut          | Recommandé ? |
|--------------------------------------|-----------------------------------------------------------------------------|----------------------------|--------------|
| `.logoutUrl("/logout")`              | URL déclenchant la déconnexion (généralement POST)                          | `/logout`                  | Oui          |
| `.logoutSuccessUrl("/login?logout")` | Redirection après déconnexion réussie                                       | `/login?logout`            | Oui          |
| `.logoutSuccessHandler(...)`         | Gestionnaire personnalisé (remplace logoutSuccessUrl)                       | —                          | Si besoin    |
| `.invalidateHttpSession(true)`       | Invalide la session HTTP                                                    | `true`                     | Oui          |
| `.deleteCookies("JSESSIONID")`       | Supprime les cookies spécifiés                                              | —                          | Souvent oui  |
| `.clearAuthentication(true)`         | Supprime l’objet Authentication du SecurityContext                          | `true`                     | Oui          |
| `.addLogoutHandler(...)`             | Ajoute un handler personnalisé (ex: déconnexion JWT, suppression token)     | —                          | Cas avancés  |
| `.permitAll()`                       | Autorise tout le monde à appeler l’URL de logout                            | —                          | Oui          |

### 3. Exemple avec page de succès personnalisée

```java
.logout(logout -> logout
    .logoutUrl("/logout")
    .logoutSuccessUrl("/goodbye")           // page personnalisée
    .deleteCookies("JSESSIONID")
    .invalidateHttpSession(true)
    .permitAll()
)
```

Puis dans ton controller :

```java
@GetMapping("/goodbye")
public String goodbye() {
    return "goodbye"; // → goodbye.html
}
```

### 4. Déclencher le logout depuis le front (HTML / Thymeleaf)

```html
<!-- Méthode POST recommandée (plus sécurisée) -->
<form action="/logout" method="post">
    <button type="submit">Se déconnecter</button>
    <input type="hidden" name="_csrf" th:value="${_csrf.token}"/>
</form>

<!-- Ou lien avec CSRF (Thymeleaf) -->
<a th:href="@{/logout(_csrf=${_csrf})}">Déconnexion</a>
```

**Important** : Spring Security protège le logout contre CSRF par défaut. Il faut donc inclure le token CSRF dans le formulaire ou dans l’URL (si GET, ce qui est moins recommandé).

### 5. Cas particulier : Logout avec JWT (stateless)

Si vous n’utilisez **pas** de session (JWT dans l’en-tête Authorization) :

```java
.logout(logout -> logout
    .logoutUrl("/api/logout")
    .addLogoutHandler((request, response, auth) -> {
        // Ici : récupérer le token et l'ajouter à une blacklist
        // ou simplement ne rien faire côté serveur (JWT stateless)
    })
    .logoutSuccessHandler((request, response, auth) -> {
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("{\"message\": \"Logout successful\"}");
        response.setContentType("application/json");
    })
    .permitAll()
)
```

### 6. Résumé – Configuration minimale recommandée (2024/2025)

```java
.logout(logout -> logout
    .logoutUrl("/logout")
    .logoutSuccessUrl("/login?logout=true")
    .invalidateHttpSession(true)
    .deleteCookies("JSESSIONID")
    .permitAll()
)
```

### Questions fréquentes

- **Je veux une page de confirmation avant logout ?**  
  → Gérer côté front (modal), puis appeler `/logout` en POST.

- **Le logout ne fonctionne pas ?**  
  Vérifie : méthode POST + token CSRF présent + bonne URL.

- **Je veux rediriger vers une page d’accueil publique ?**  
  → `.logoutSuccessUrl("/")`

Tu veux que je te montre une version complète avec :
- Thymeleaf + navbar avec bouton logout
- Gestion CSRF
- Page goodbye personnalisée
- Ou version API REST + JWT ?

Dis-moi ce qui t’intéresse ! 😄


Voici un exemple **complet et fonctionnel** d'implémentation du **logout** avec **Thymeleaf** dans une application Spring Boot + Spring Security.

### 1. Configuration Spring Security (avec logout)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasRole("USER")
                .requestMatchers("/css/**", "/js/**", "/images/**", "/login", "/logout").permitAll()
                .anyRequest().authenticated()
            )
            
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/home", true)
                .permitAll()
            )
            
            .logout(logout -> logout
                .logoutUrl("/logout")                       // URL déclenchée
                .logoutSuccessUrl("/login?logout=true")     // redirection après logout
                .invalidateHttpSession(true)                // invalide la session
                .deleteCookies("JSESSIONID")                // supprime le cookie de session
                .clearAuthentication(true)
                .permitAll()                                // tout le monde peut se déconnecter
            )
            
            .exceptionHandling(ex -> ex
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }

    // ... autres beans (UserDetailsService, PasswordEncoder, etc.)
}
```

### 2. Exemple de page avec navbar (Thymeleaf)

C’est généralement dans un fragment réutilisable (`fragments/navbar.html`) ou directement dans vos pages principales.

```html
<!DOCTYPE html>
<html lang="fr" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Mon Application</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>

<!-- Fragment navbar -->
<nav th:fragment="navbar">
    <div class="container">
        <div class="logo">MonApp</div>
        
        <div class="menu">
            <a href="/home">Accueil</a>
            <a href="/user/profile" th:if="${#authorization.expression('isAuthenticated()')}">Profil</a>
            <a href="/admin/dashboard" th:if="${#authorization.expression('hasRole(''ADMIN'')')}">Admin</a>
        </div>

        <div class="auth-section">
            <!-- Utilisateur connecté -->
            <div th:if="${#authorization.expression('isAuthenticated()')}">
                <span th:text="${#authentication.principal.username}"></span>
                
                <!-- Formulaire de logout (POST + CSRF) -->
                <form th:action="@{/logout}" method="post" style="display: inline;">
                    <button type="submit" class="btn-logout">Se déconnecter</button>
                    <!-- Le token CSRF est automatiquement ajouté par Thymeleaf -->
                    <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
                </form>
            </div>

            <!-- Utilisateur non connecté -->
            <div th:unless="${#authorization.expression('isAuthenticated()')}">
                <a href="/login">Se connecter</a>
            </div>
        </div>
    </div>
</nav>

<!-- Reste de la page -->
<main th:fragment="~{::main}">
    <!-- contenu principal -->
</main>

</body>
</html>
```

### 3. Page de login (exemple minimal)

```html
<!DOCTYPE html>
<html lang="fr" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Connexion</title>
    <link rel="stylesheet" href="/css/style.css">
</head>
<body>

<div class="login-container">
    <h2>Connexion</h2>

    <!-- Message de succès logout -->
    <div th:if="${param.logout}" class="alert success">
        Vous avez été déconnecté avec succès.
    </div>

    <!-- Message d'erreur -->
    <div th:if="${param.error}" class="alert error">
        Nom d'utilisateur ou mot de passe incorrect.
    </div>

    <form th:action="@{/login}" method="post">
        <div class="form-group">
            <label for="username">Nom d'utilisateur</label>
            <input type="text" id="username" name="username" required autofocus>
        </div>

        <div class="form-group">
            <label for="password">Mot de passe</label>
            <input type="password" id="password" name="password" required>
        </div>

        <!-- Token CSRF obligatoire pour POST -->
        <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>

        <button type="submit">Se connecter</button>
    </form>
</div>

</body>
</html>
```

### 4. Page d'accueil (home) – exemple

```html
<!DOCTYPE html>
<html lang="fr" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Accueil</title>
</head>
<body>

<!-- Inclusion du navbar -->
<div th:replace="~{fragments/navbar :: navbar}"></div>

<main>
    <h1>Bienvenue, <span th:text="${#authentication.principal.username}"></span> !</h1>
    
    <p>Vous êtes connecté avec les rôles : 
        <span th:each="role : ${#authentication.authorities}" th:text="${role.authority} + ' '"></span>
    </p>
</main>

</body>
</html>
```

### 5. Résumé – Les points importants à retenir

- Le logout **doit être en POST** (par défaut Spring Security le protège contre CSRF)
- Il faut inclure le **token CSRF** dans le formulaire :  
  `<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>`
- Utiliser `#authorization.expression()` pour afficher/masquer des éléments selon l’état de connexion ou les rôles
- Utiliser `#authentication.principal.username` pour afficher le nom de l’utilisateur connecté
- Paramètres utiles après logout : `?logout=true` pour afficher un message

### Variante simple (lien au lieu d’un bouton)

Si vous préférez un simple lien (moins recommandé car GET est plus vulnérable) :

```html
<a th:href="@{/logout(_csrf=${_csrf})}">Déconnexion</a>
```

Mais la **méthode POST avec bouton** reste la plus sécurisée et la plus courante.

Tu veux ajouter quelque chose de spécifique ?  
(ex : menu déroulant, icône utilisateur, confirmation avant logout, logout depuis une API AJAX, etc.)


**JWT** signifie **JSON Web Token**.

C’est l’une des méthodes les plus utilisées aujourd’hui pour gérer l’**authentification** et la **gestion de sessions** dans les applications modernes (surtout les API REST, les applications SPA, mobile apps, microservices, etc.).

Voici une explication claire et progressive :

### 1. C’est quoi concrètement un JWT ?

Un JWT est une **chaîne de caractères** qui contient trois parties séparées par des points (`.`) :

```
xxxxx.yyyyy.zzzzz
```

Ces trois parties s’appellent :

- **Header** (en-tête)
- **Payload** (données)
- **Signature** (signature)

Exemple réel (simplifié) :

```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

### 2. Les 3 parties décodées

| Partie     | Contenu (décodé en JSON)                              | Description                                                                 | Est-ce signé / protégé ? |
|------------|-------------------------------------------------------|-----------------------------------------------------------------------------|---------------------------|
| **Header** | `{"alg": "HS256", "typ": "JWT"}`                     | Type de token + algorithme de signature utilisé                             | Non (visible)             |
| **Payload** | `{"sub": "123", "name": "John", "role": "USER", "exp": 1735689600}` | Les informations utiles (données utilisateur, rôles, date d’expiration…)   | Non (visible)             |
| **Signature** | (longue chaîne illisible)                             | Sert à vérifier que personne n’a modifié le token                           | Oui (protégé)             |

→ **Tout le monde peut lire** le Header et le Payload (ils sont juste encodés en **Base64**).  
→ **Personne ne peut modifier** le token sans que la signature devienne invalide (sauf si on connaît la clé secrète).

### 3. Comment ça fonctionne dans une application ?

Étapes typiques :

1. L’utilisateur se connecte (login + mot de passe)
2. Le serveur vérifie les identifiants
3. Si OK → le serveur **crée un JWT** qui contient :
  - l’identifiant utilisateur (sub)
  - les rôles / permissions
  - la date d’expiration (exp)
4. Le serveur **renvoie ce JWT** au client (souvent dans la réponse JSON)
5. Le client (navigateur, appli mobile) **stocke le JWT** (souvent dans le localStorage, sessionStorage ou un cookie httpOnly)
6. À chaque requête suivante → le client envoie le JWT dans l’en-tête :

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

7. Le serveur vérifie la **signature** du JWT :
  - Est-ce que le token a été modifié ?
  - Est-ce qu’il n’est pas expiré ?
  - Si tout est OK → l’utilisateur est considéré comme authentifié

### 4. Avantages du JWT

- **Stateless** : pas besoin de stocker la session côté serveur (base de données, Redis, etc.)
- Très adapté aux **API REST** et microservices
- Fonctionne bien avec les applications **SPA** (React, Angular, Vue), mobile, etc.
- Peut transporter des informations (rôles, préférences) sans refaire de requête
- Facile à utiliser avec différents langages / frameworks

### 5. Inconvénients / choses à savoir

| Problème                          | Conséquence                                                                 | Solution courante                              |
|-----------------------------------|-----------------------------------------------------------------------------|------------------------------------------------|
| Impossible de déconnecter vraiment | Un JWT valide reste valide jusqu’à son expiration                           | Expiration courte + refresh token              |
| Stockage côté client              | Risque XSS si stocké dans localStorage                                     | Cookie httpOnly + secure + sameSite            |
| Token volé                        | Tant qu’il est valide, l’attaquant peut l’utiliser                         | Expiration courte (5–15 min) + refresh token   |
| Pas de révocation native          | Pas de liste noire facile                                                   | Blacklist (pas idéal), tokens très courts      |
| Payload visible                   | Ne jamais mettre de données sensibles (mot de passe, email privé, etc.)    | Ne mettre que l’essentiel                      |

### 6. Comparaison rapide : Session classique vs JWT

| Critère                 | Session classique (cookies)          | JWT (Bearer Token)                  |
|-------------------------|---------------------------------------|--------------------------------------|
| Stockage                | Cookie + serveur (session ID)         | Client (localStorage / cookie)       |
| État                    | Stateful (serveur garde l’état)       | Stateless (serveur ne garde rien)    |
| Scalabilité             | Plus compliqué (session sticky / redis) | Très facile                          |
| Déconnexion immédiate   | Facile (supprimer session serveur)    | Difficile (sauf refresh token)       |
| Usage principal         | Applications web traditionnelles      | API, SPA, mobile, microservices      |

### En résumé – JWT en une phrase

Un **JWT** est un **ticket numérique signé** que le client envoie à chaque requête pour prouver qui il est, sans que le serveur ait besoin de vérifier une session en base de données.

Tu veux qu’on voie :
- Comment créer / valider un JWT en Spring Boot (avec jjwt ou spring-security-jwt) ?
- Comment faire un système avec **access token + refresh token** ?
- Les bonnes pratiques de stockage (localStorage vs cookie httpOnly) ?
- Exemple concret avec code ?

Dis-moi ce qui t’intéresse ! 😄







Voici un exemple **complet et clair** pour implémenter l’authentification **JWT** dans une application **Spring Boot** (version 2024/2025 – Spring Boot 3.x / Spring Security 6.x).

### Objectif de cet exemple
- Login → générer un JWT
- Les requêtes protégées → valider le JWT envoyé dans l’en-tête `Authorization: Bearer ...`
- Pas de session (stateless)
- Utilisation d’un **access token** simple (pas de refresh token dans cet exemple de base)

### Étape 1 : Dépendances (pom.xml)

```xml
<dependencies>
    <!-- Spring Boot Starter Web + Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.6</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.6</version>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.6</version>
        <scope>runtime</scope>
    </dependency>
</dependencies>
```

### Étape 2 : Classe de configuration JWT (JwtService)

```java
@Service
public class JwtService {

    private static final String SECRET_KEY = "votre_cle_tres_longue_et_tres_secrete_au_moins_32_caracteres_ici";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 heure

    // Générer le token
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    // Vérifier si le token est valide
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    // Extraire le username (subject)
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extraire une claim spécifique
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### Étape 3 : Filtre JWT (JwtAuthenticationFilter)

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

### Étape 4 : Configuration de sécurité (SecurityConfig)

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // On désactive CSRF car stateless (JWT)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()      // login + register
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            PasswordEncoder passwordEncoder,
            UserDetailsService userDetailsService) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Étape 5 : Contrôleur d’authentification (AuthController)

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());

        String jwt = jwtService.generateToken(userDetails);

        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);

        return ResponseEntity.ok(response);
    }
}

// DTO simple
public record LoginRequest(String username, String password) {}
```

### Étape 6 : Exemple d’utilisation

1. **Login**  
   POST `/api/auth/login`

   ```json
   {
       "username": "john",
       "password": "1234"
   }
   ```

   Réponse :

   ```json
   {
       "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   }
   ```

2. **Accéder à une ressource protégée**  
   GET `/api/admin/dashboard`

   En-tête :

   ```
   Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```

### Résumé – Les éléments clés

- **JwtService** → crée et valide les tokens
- **JwtAuthenticationFilter** → lit le token dans chaque requête et met l’utilisateur dans le SecurityContext
- **SecurityConfig** → stateless + filtre JWT avant UsernamePasswordAuthenticationFilter
- Pas de session → `SessionCreationPolicy.STATELESS`
- CSRF désactivé (car stateless)

Tu veux que l’on ajoute :

- Gestion du **refresh token** ?
- Endpoints **register** ?
- Gestion des **rôles / permissions** plus fines ?
- Gestion des erreurs (token expiré, invalide…) avec `@ControllerAdvice` ?
- Stockage sécurisé de la clé secrète (application.properties + @Value) ?

Dis-moi ce que tu veux approfondir !










Voici un guide **clair et complet** pour implémenter un **UserDetailsService personnalisé** dans une application Spring Boot + Spring Security (2024/2025, Spring Boot 3.x).

### Pourquoi créer un UserDetailsService personnalisé ?

Le `UserDetailsService` est l’interface que Spring Security utilise pour **charger un utilisateur** à partir de son nom d’utilisateur (username) lors de la tentative d’authentification.

Par défaut, Spring propose :
- `InMemoryUserDetailsManager` (utilisateurs en mémoire)
- `JdbcUserDetailsManager` (utilisateurs en base SQL)

Mais dans la vraie vie, on veut presque toujours :
- Charger les utilisateurs depuis **notre propre base de données** (JPA, Mongo, etc.)
- Utiliser **notre entité métier** (`AppUser`, `Utilisateur`, etc.)

### Étape 1 : Créer l’entité utilisateur (exemple avec JPA)

```java
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;  // doit être encodé (BCrypt)

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles = new HashSet<>();

    private boolean enabled = true;
    private boolean accountNonExpired = true;
    private boolean accountNonLocked = true;
    private boolean credentialsNonExpired = true;
}
```

### Étape 2 : Créer le Repository

```java
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    
    Optional<AppUser> findByUsername(String username);
}
```

### Étape 3 : Implémenter UserDetailsService personnalisé

Il existe deux approches courantes :

#### Approche 1 : Utiliser la classe `User` de Spring (la plus simple et recommandée)

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        AppUser appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));

        if (!appUser.isEnabled()) {
            throw new DisabledException("Compte désactivé");
        }

        return User.builder()
                .username(appUser.getUsername())
                .password(appUser.getPassword())  // déjà encodé
                .authorities(mapRolesToAuthorities(appUser.getRoles()))
                .accountExpired(!appUser.isAccountNonExpired())
                .accountLocked(!appUser.isAccountNonLocked())
                .credentialsExpired(!appUser.isCredentialsNonExpired())
                .disabled(!appUser.isEnabled())
                .build();
    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Set<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))  // ou sans ROLE_ selon votre convention
                .collect(Collectors.toList());
    }
}
```

#### Approche 2 : Créer sa propre classe qui implémente UserDetails (plus de contrôle)

```java
public class AppUserDetails implements UserDetails {

    private final AppUser appUser;

    public AppUserDetails(AppUser appUser) {
        this.appUser = appUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return appUser.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return appUser.getPassword();
    }

    @Override
    public String getUsername() {
        return appUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return appUser.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return appUser.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return appUser.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return appUser.isEnabled();
    }

    // Méthode bonus utile
    public AppUser getAppUser() {
        return appUser;
    }
}
```

Puis le service :

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));

        return new AppUserDetails(appUser);
    }
}
```

### Étape 4 : Brancher le service dans la configuration Security

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(Customizer.withDefaults())
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### Étape 5 : Utiliser l’utilisateur dans un contrôleur (bonus)

```java
@GetMapping("/me")
public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
    // Approche 1 (User de Spring)
    // String username = userDetails.getUsername();

    // Approche 2 (si vous avez AppUserDetails)
    if (userDetails instanceof AppUserDetails appUserDetails) {
        AppUser appUser = appUserDetails.getAppUser();
        return ResponseEntity.ok(appUser);
    }

    return ResponseEntity.ok(userDetails);
}
```

### Récapitulatif rapide – Les choix les plus courants

| Besoin                                      | Recommandation                              |
|---------------------------------------------|---------------------------------------------|
| Configuration rapide, classique             | Utiliser `User.builder()` (Approche 1)      |
| Besoin d’accéder à l’entité complète        | Créer `AppUserDetails` (Approche 2)         |
| Beaucoup de champs personnalisés            | Approche 2 + implémenter `UserDetails`      |
| JWT + UserDetails                           | Approche 1 ou 2 (les deux fonctionnent)     |

Tu veux que l’on voie un exemple avec :
- JWT + ce UserDetailsService ?
- Gestion des exceptions plus fines ?
- Utilisation dans un système avec refresh token ?
- Comment tester la méthode `loadUserByUsername` ?

Dis-moi ce qui t’intéresse ensuite !






Voici une explication claire et un exemple concret pour **intégrer un JWT avec un UserDetailsService personnalisé** dans une application Spring Boot.

L’objectif est le suivant :
- L’utilisateur se connecte via `/api/auth/login` → reçoit un JWT
- Pour toutes les requêtes protégées, le JWT est envoyé dans l’en-tête `Authorization: Bearer ...`
- Le filtre JWT valide le token et charge l’utilisateur via **UserDetailsService**
- L’application reste **stateless** (pas de session)

### Architecture typique

```
Login Request → AuthController → AuthenticationManager → UserDetailsService → JWT généré
Requête protégée → JwtAuthenticationFilter → JwtService (valide token) → UserDetailsService → SecurityContext
```

### Étape 1 : Le UserDetailsService personnalisé

```java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));

        if (!user.isEnabled()) {
            throw new DisabledException("Compte désactivé");
        }

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())           // déjà encodé
                .authorities(mapRoles(user.getRoles()))
                .accountExpired(!user.isAccountNonExpired())
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(!user.isCredentialsNonExpired())
                .disabled(!user.isEnabled())
                .build();
    }

    private Collection<? extends GrantedAuthority> mapRoles(Set<String> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
    }
}
```

### Étape 2 : Service JWT (génération + validation)

```java
@Service
public class JwtService {

    private static final String SECRET_KEY = "votre_cle_super_longue_et_tres_securisee_min_256_bits_ici";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1 heure

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### Étape 3 : Filtre JWT (charge UserDetailsService)

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        String username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

### Étape 4 : Configuration de sécurité complète

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Désactivation CSRF (stateless)
            .csrf(csrf -> csrf.disable())

            // Autorisations
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )

            // Pas de session
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Ajout du filtre JWT AVANT le filtre par défaut d'authentification
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
```

### Étape 5 : Contrôleur d’authentification

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());

        String jwt = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }
}

record LoginRequest(String username, String password) {}
record AuthResponse(String token) {}
```

### Résumé du flux complet

1. POST `/api/auth/login` → username + password
2. `AuthenticationManager` appelle `CustomUserDetailsService.loadUserByUsername()`
3. Si OK → génération JWT avec `JwtService`
4. Client reçoit le token et l’envoie dans chaque requête suivante
5. `JwtAuthenticationFilter` :
    - Extrait le token
    - Extrait le username
    - Appelle à nouveau `loadUserByUsername()` pour charger les rôles/autorités
    - Valide le token
    - Met l’utilisateur dans le `SecurityContext`

### Questions fréquentes / améliorations possibles

- **Où stocker la clé secrète ?** → `application.yml` + `@Value` ou Vault / AWS Secrets Manager
- **Ajouter un refresh token ?** → Oui, très recommandé (token d’accès court + refresh token long)
- **Gérer token expiré ?** → `@ControllerAdvice` + `JwtException` personnalisée
- **Récupérer l’utilisateur dans le contrôleur ?** → `@AuthenticationPrincipal UserDetails user` ou `SecurityContextHolder`

Veux-tu qu’on approfondisse une de ces parties ?  
( refresh token, gestion d’erreurs, stockage sécurisé de la clé, tests, etc. )







Voici un guide **complet et concret** pour implémenter un système **JWT avec Access Token + Refresh Token** dans Spring Boot + Spring Security.

### Objectif du système
- **Access Token** : durée de vie courte (ex: 10–30 minutes) → utilisé pour authentifier les requêtes API
- **Refresh Token** : durée de vie longue (ex: 7 jours) → utilisé uniquement pour obtenir un nouvel Access Token quand l’ancien expire
- **Stateless** côté serveur (pas de session)
- Possibilité de révoquer un refresh token (ex: logout)

### Structure recommandée

```
POST /api/auth/login           → retourne { accessToken, refreshToken }
POST /api/auth/refresh-token   → prend refreshToken → retourne nouveau accessToken + nouveau refreshToken
POST /api/auth/logout          → invalide le refreshToken
```

### Étape 1 : Entité pour stocker les Refresh Tokens

```java
@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    private boolean revoked = false;

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
```

Repository :

```java
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(AppUser user);
}
```

### Étape 2 : Service Refresh Token

```java
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final AppUserRepository userRepository;

    private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(7);

    public RefreshToken createRefreshToken(String username) {
        AppUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Supprimer les anciens refresh tokens de cet utilisateur (optionnel)
        refreshTokenRepository.deleteByUser(user);

        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(LocalDateTime.now().plus(REFRESH_TOKEN_DURATION))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        if (refreshToken.isExpired()) {
            throw new RuntimeException("Refresh token expired");
        }

        return refreshToken;
    }

    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElse(null);
        if (refreshToken != null) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
        }
    }

    public void revokeAllUserTokens(AppUser user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
```

### Étape 3 : Mise à jour du JwtService (deux méthodes)

```java
@Service
public class JwtService {

    // Clé pour access token
    private static final String ACCESS_SECRET = "votre_cle_access_token_tres_longue_ici_256bits";
    private static final long ACCESS_EXPIRATION = 1000 * 60 * 15; // 15 minutes

    // Clé différente pour refresh token (recommandé)
    private static final String REFRESH_SECRET = "votre_cle_refresh_token_tres_longue_et_differente";

    // Méthode pour Access Token (comme avant)
    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = Map.of(
                "roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(ACCESS_SECRET)), Jwts.SIG.HS256)
                .compact();
    }

    // Méthode pour Refresh Token (simple UUID stocké en base)
    // → On n'utilise PAS JWT pour refresh token ici (plus sûr)

    // Validation Access Token (comme avant)
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(ACCESS_SECRET)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isAccessTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date exp = Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(ACCESS_SECRET)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
        return exp.before(new Date());
    }
}
```

### Étape 4 : AuthController avec login + refresh + logout

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.username());

        String accessToken = jwtService.generateAccessToken(userDetails);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(request.username());

        return ResponseEntity.ok(new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                refreshToken.getExpiryDate()
        ));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            RefreshToken refreshToken = refreshTokenService.verifyRefreshToken(request.refreshToken());

            UserDetails userDetails = userDetailsService.loadUserByUsername(
                    refreshToken.getUser().getUsername()
            );

            String newAccessToken = jwtService.generateAccessToken(userDetails);

            // Option 1 : rotation du refresh token (recommandé)
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(
                    refreshToken.getUser().getUsername()
            );

            return ResponseEntity.ok(new AuthResponse(
                    newAccessToken,
                    newRefreshToken.getToken(),
                    newRefreshToken.getExpiryDate()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RefreshTokenRequest request) {
        refreshTokenService.revokeRefreshToken(request.refreshToken());
        return ResponseEntity.ok(Map.of("message", "Déconnexion réussie"));
    }
}

// DTOs
record LoginRequest(String username, String password) {}
record RefreshTokenRequest(String refreshToken) {}
record AuthResponse(String accessToken, String refreshToken, LocalDateTime expiryDate) {}
```

### Étape 5 : Configuration Security (inchangée)

```java
// ... comme dans l'exemple précédent
.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
.csrf(csrf -> csrf.disable())
.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
```

### Bonnes pratiques & conseils

| Élément                     | Recommandation                                      | Pourquoi ?                                      |
|-----------------------------|-----------------------------------------------------|-------------------------------------------------|
| Durée Access Token          | 5 à 30 minutes                                      | Limite la fenêtre d’attaque si token volé       |
| Durée Refresh Token         | 1 à 30 jours                                        | Selon le niveau de sécurité requis              |
| Rotation refresh token      | Oui (créer nouveau à chaque refresh)                | Réduit les risques si refresh token compromis   |
| Stockage refresh token      | HttpOnly + Secure + SameSite=Strict cookie          | Protection contre XSS                           |
| Clés secrètes               | Dans variables d’environnement ou Vault             | Jamais en dur dans le code                      |
| Logout                      | Invalider tous les refresh tokens de l’utilisateur  | Déconnexion effective                           |

Veux-tu qu’on ajoute :

- Stockage du refresh token dans un **cookie httpOnly** ?
- Gestion centralisée des erreurs d’authentification ?
- Exemple de **front-end** (comment appeler refresh automatiquement) ?
- Blacklist des access tokens (cas très sensibles) ?

Dis-moi ce que tu veux approfondir !