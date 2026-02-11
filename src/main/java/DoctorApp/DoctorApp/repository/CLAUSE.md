Ce code met en place un **système de sécurité complet** pour ton application DoctorApp avec Spring Security. Voici l'explication détaillée :

---

## 🔐 **1. SecurityConfig.java** - Configuration de la sécurité

### Deux systèmes d'authentification (problématique !)

```java
@Bean
public InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder passwordEncoder) {
    // ⚠️ Utilisateurs en MÉMOIRE (pour tests)
    return new InMemoryUserDetailsManager(
        User.withUsername("use1").password(...).roles("USER").build(),
        ...
    );
}

@Bean
public UserDetailsService userDetailsService(UtilisateursRepository repository) {
    // ✅ Utilisateurs en BASE DE DONNÉES (production)
    return new CustomUserDetailsService(repository);
}
```

**Problème** : Tu as **2 beans UserDetailsService** ! Spring va utiliser celui de la base de données et ignorer celui en mémoire. Il faut **supprimer** `inMemoryUserDetailsManager` si tu veux utiliser la BDD.

### Règles d'autorisation

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) {
    return http
        .formLogin(Customizer.withDefaults())  // Formulaire de connexion par défaut
        .authorizeHttpRequests(auth -> auth
            // 🔴 ADMIN seulement
            .requestMatchers("/admin/**").hasRole("ADMIN")
            
            // 🟡 DOCTOR ou ADMIN
            .requestMatchers("/appointments/**", "/prescriptions/**").hasAnyRole("DOCTOR", "ADMIN")
            
            // 🟢 Public (pas de connexion)
            .requestMatchers("/", "/login", "/css/**").permitAll()
            
            // 🔵 Tout le reste → connexion obligatoire
            .anyRequest().authenticated()
        )
        .build();
}
```

---

## 👤 **2. CustomUserDetailsService.java** - Charger les utilisateurs depuis la BDD

```java
@Override
public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    // 1️⃣ Chercher l'utilisateur par email dans la BDD
    Utilisateur user = utilisateursRepository.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));

    // 2️⃣ Convertir en objet UserDetails de Spring Security
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),           // username
        user.getPassword(),        // mot de passe encodé
        user.isEnabled(),          // compte activé ?
        true,                      // compte non expiré
        true,                      // credentials non expirés
        !user.isAccountLocked(),   // compte non verrouillé
        user.getAuthorities()      // rôles et permissions
    );
}
```

**Rôle** : Quand quelqu'un se connecte, Spring appelle cette méthode pour vérifier l'utilisateur.

---

## 🧑‍💼 **3. Utilisateur.java** - Entité utilisateur

### Implémente `UserDetails` (interface de Spring Security)

```java
public class Utilisateur implements UserDetails {
    private String email;
    private String password;
    private boolean enabled = false;       // Email confirmé ?
    private boolean accountLocked = false; // Compte bloqué ?
    
    // 🔢 Gestion OTP (authentification à 2 facteurs)
    private String otpCode;
    private LocalDateTime otpExpiration;
    
    // 🔐 Sécurité
    private int loginAttempts = 0;         // Nombre de tentatives échouées
    private LocalDateTime lastLogin;       // Dernière connexion
    
    // 👥 Relations
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();
```

### Méthode importante : `getAuthorities()`

```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = new HashSet<>();
    
    for (Role role : roles) {
        if (role.isActive()) {
            // Ajoute le rôle : ROLE_ADMIN, ROLE_DOCTOR, etc.
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getNom()));
        }
        
        // Ajoute aussi les permissions du rôle
        for (Permission perm : role.getPermissions()) {
            if (perm.isActive()) {
                authorities.add(new SimpleGrantedAuthority(perm.getNom()));
            }
        }
    }
    return authorities;
}
```

**Rôle** : Génère la liste des droits (rôles + permissions) de l'utilisateur.

---

## 🎭 **4. Role.java** - Rôles (ADMIN, DOCTOR, PATIENT)

```java
public class Role {
    private String nom;              // "ADMIN", "DOCTOR", "PATIENT"
    private String description;
    private boolean active = true;
    
    @ManyToMany(mappedBy = "roles")
    private Set<Utilisateur> utilisateurs;  // Utilisateurs ayant ce rôle
    
    @ManyToMany
    private Set<Permission> permissions;    // Permissions de ce rôle
}
```

**Exemple** : Un rôle `DOCTOR` pourrait avoir les permissions `patient:read`, `appointment:create`.

---

## 🔑 **5. Permission.java** - Permissions granulaires

```java
public class Permission {
    private String nom;  // "patient:read", "patient:delete", "appointment:create"
    private String description;
    private boolean active = true;
    
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles;  // Rôles ayant cette permission
}
```

**Exemple de permissions** :
- `patient:read` → Lire les patients
- `patient:delete` → Supprimer un patient
- `appointment:create` → Créer un rendez-vous

---

## 🎯 **Fonctionnement global**

1. **Connexion** : L'utilisateur entre son email/mot de passe
2. **Vérification** : `CustomUserDetailsService` charge l'utilisateur depuis la BDD
3. **Chargement des droits** : `getAuthorities()` récupère ses rôles + permissions
4. **Autorisation** : Spring vérifie si l'URL demandée est autorisée selon `SecurityFilterChain`

---

## ⚠️ **Problème à corriger**

**Supprime** le bean `inMemoryUserDetailsManager` dans `SecurityConfig` car tu utilises la base de données :

```java
// ❌ À SUPPRIMER
@Bean
public InMemoryUserDetailsManager inMemoryUserDetailsManager(...) { ... }
```

Garde seulement :
```java
// ✅ À GARDER
@Bean
public UserDetailsService userDetailsService(UtilisateursRepository repository) {
    return new CustomUserDetailsService(repository);
}
```

Besoin d'aide pour corriger ou ajouter des fonctionnalités ? 😊


Parfait ! Voici les corrections et améliorations à apporter :

---

## 🔧 **Corrections à faire**

### 1️⃣ **SecurityConfig.java** - Supprimer les utilisateurs en mémoire

Remplace ton fichier `SecurityConfig.java` par ceci :

```java
package DoctorApp.DoctorApp.Security;

import DoctorApp.DoctorApp.Service.CustomUserDetailsService;
import DoctorApp.DoctorApp.repository.UtilisateursRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    // ✅ Encoder de mot de passe
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ✅ Service d'authentification (BDD uniquement)
    @Bean
    public UserDetailsService userDetailsService(UtilisateursRepository repository) {
        return new CustomUserDetailsService(repository);
    }

    // ✅ Configuration de sécurité
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(form -> form
                        .loginPage("/login")              // Page de connexion personnalisée
                        .defaultSuccessUrl("/dashboard", true)  // Redirection après connexion
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .authorizeHttpRequests(auth -> auth
                        // 🔴 ADMIN seulement
                        .requestMatchers(
                                "/admin/deletePatient/**",
                                "/admin/savePatient/**",
                                "/admin/editPatient/**",
                                "/admin/users/**",
                                "/admin/settings/**"
                        ).hasRole("ADMIN")

                        // 🟡 Médecins (DOCTOR + ADMIN)
                        .requestMatchers(
                                "/appointments/**",
                                "/prescriptions/**",
                                "/patients/view/**"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        // 🟢 Pages publiques
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",      // Si tu as une page d'inscription
                                "/about",
                                "/contact",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**"
                        ).permitAll()

                        // 🔵 Tout le reste → connexion obligatoire
                        .anyRequest().authenticated()
                )
                .build();
    }
}
```

**Changements** :
- ❌ Supprimé `inMemoryUserDetailsManager`
- ✅ Ajouté `PasswordEncoder` bean
- ✅ Amélioré la configuration du formulaire de connexion
- ✅ Ajouté la gestion du logout

---

### 2️⃣ **CustomUserDetailsService.java** - Petites améliorations

```java
package DoctorApp.DoctorApp.Service;

import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.repository.UtilisateursRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateursRepository utilisateursRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        Utilisateur user = utilisateursRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .disabled(!user.isEnabled())           // Compte activé ?
                .accountLocked(user.isAccountLocked()) // Compte verrouillé ?
                .authorities(user.getAuthorities())    // Rôles + permissions
                .build();
    }
}
```

**Changements** :
- ✅ Utilisé le builder pour plus de clarté
- ✅ Ajouté `final` pour `utilisateursRepository`
- ✅ Supprimé `@Transactional` au niveau de la classe (gardé sur la méthode)

---

### 3️⃣ **UtilisateursRepository.java** - Ajouter la méthode

Vérifie que ton repository a bien cette méthode :

```java
package DoctorApp.DoctorApp.repository;

import DoctorApp.DoctorApp.Entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UtilisateursRepository extends JpaRepository<Utilisateur, Long> {
    
    Optional<Utilisateur> findByEmail(String email);
    
    // Optionnel : vérifier si un email existe déjà
    boolean existsByEmail(String email);
}
```

---

## 🎯 **Fonctionnalités supplémentaires recommandées**

### 4️⃣ **Initialisation des données** - Créer un admin par défaut

Crée une classe pour initialiser un admin au démarrage :

```java
package DoctorApp.DoctorApp.Config;

import DoctorApp.DoctorApp.Entity.Permission;
import DoctorApp.DoctorApp.Entity.Role;
import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.repository.PermissionRepository;
import DoctorApp.DoctorApp.repository.RoleRepository;
import DoctorApp.DoctorApp.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateursRepository utilisateursRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        
        // Créer les permissions si elles n'existent pas
        Permission patientRead = createPermissionIfNotExists("patient:read", "Lire les patients");
        Permission patientWrite = createPermissionIfNotExists("patient:write", "Modifier les patients");
        Permission patientDelete = createPermissionIfNotExists("patient:delete", "Supprimer les patients");
        Permission appointmentManage = createPermissionIfNotExists("appointment:manage", "Gérer les rendez-vous");

        // Créer les rôles si ils n'existent pas
        Role adminRole = createRoleIfNotExists(
                "ADMIN", 
                "Administrateur", 
                Set.of(patientRead, patientWrite, patientDelete, appointmentManage)
        );
        
        Role doctorRole = createRoleIfNotExists(
                "DOCTOR", 
                "Médecin", 
                Set.of(patientRead, patientWrite, appointmentManage)
        );
        
        Role patientRole = createRoleIfNotExists(
                "PATIENT", 
                "Patient", 
                Set.of(patientRead)
        );

        // Créer un admin par défaut si aucun utilisateur n'existe
        if (utilisateursRepository.count() == 0) {
            Utilisateur admin = Utilisateur.builder()
                    .nom("Administrateur")
                    .email("admin@doctorapp.com")
                    .password(passwordEncoder.encode("admin123"))
                    .enabled(true)
                    .accountLocked(false)
                    .firstLogin(true)
                    .roles(Set.of(adminRole))
                    .build();
            
            utilisateursRepository.save(admin);
            System.out.println("✅ Admin créé : admin@doctorapp.com / admin123");
        }
    }

    private Permission createPermissionIfNotExists(String nom, String description) {
        return permissionRepository.findByNom(nom)
                .orElseGet(() -> permissionRepository.save(
                        Permission.builder()
                                .nom(nom)
                                .description(description)
                                .active(true)
                                .build()
                ));
    }

    private Role createRoleIfNotExists(String nom, String description, Set<Permission> permissions) {
        return roleRepository.findByNom(nom)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .nom(nom)
                                .description(description)
                                .active(true)
                                .permissions(permissions)
                                .build()
                ));
    }
}
```

**Ce que ça fait** :
- ✅ Crée automatiquement les permissions et rôles au démarrage
- ✅ Crée un compte admin par défaut (`admin@doctorapp.com` / `admin123`)
- ✅ Ne recrée pas si les données existent déjà

---

### 5️⃣ **Repositories manquants**

Crée ces repositories si tu ne les as pas :

**RoleRepository.java** :
```java
package DoctorApp.DoctorApp.repository;

import DoctorApp.DoctorApp.Entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByNom(String nom);
}
```

**PermissionRepository.java** :
```java
package DoctorApp.DoctorApp.repository;

import DoctorApp.DoctorApp.Entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByNom(String nom);
}
```

---

## ✅ **Résumé des changements**

1. ✅ Supprimé les utilisateurs en mémoire
2. ✅ Ajouté `PasswordEncoder` bean
3. ✅ Amélioré la configuration de sécurité (logout, login personnalisé)
4. ✅ Créé un système d'initialisation des données (admin, rôles, permissions)
5. ✅ Ajouté les repositories manquants

**Maintenant tu peux te connecter avec** : `admin@doctorapp.com` / `admin123`

Tu veux que je t'aide à créer une page de connexion personnalisée ou à ajouter d'autres fonctionnalités ? 😊

# 📚 COURS COMPLET : SPRING SECURITY & GESTION DES UTILISATEURS

Basé sur ton application DoctorApp

---

## 📖 TABLE DES MATIÈRES

1. [Introduction à Spring Security](#1-introduction)
2. [Architecture de sécurité](#2-architecture)
3. [Authentification vs Autorisation](#3-auth)
4. [Les entités (Utilisateur, Role, Permission)](#4-entites)
5. [UserDetailsService - Le cœur de l'authentification](#5-userdetails)
6. [SecurityConfig - Configuration de la sécurité](#6-config)
7. [Le système RBAC (Role-Based Access Control)](#7-rbac)
8. [Encodage des mots de passe](#8-password)
9. [Gestion des sessions et du formulaire de connexion](#9-sessions)
10. [Exercices pratiques](#10-exercices)

---

<a name="1-introduction"></a>
## 1️⃣ INTRODUCTION À SPRING SECURITY

### Qu'est-ce que Spring Security ?

**Spring Security** est un framework de sécurité pour les applications Java/Spring qui gère :
- 🔐 **L'authentification** : Vérifier QUI tu es (username/password)
- 🛡️ **L'autorisation** : Vérifier ce que tu peux FAIRE (droits d'accès)
- 🔒 **La protection** : CSRF, XSS, Session Fixation, etc.

### Pourquoi en as-tu besoin ?

Dans ton application DoctorApp :
- Les **patients** ne doivent voir que leurs données
- Les **médecins** peuvent gérer les rendez-vous
- Les **admins** peuvent tout faire

**Sans Spring Security** → N'importe qui peut accéder à n'importe quelle page ! ❌

---

<a name="2-architecture"></a>
## 2️⃣ ARCHITECTURE DE SÉCURITÉ

### Le flux d'authentification

```
┌─────────────┐
│   Client    │
│ (Navigateur)│
└──────┬──────┘
       │ 1. POST /login (email + password)
       ▼
┌─────────────────────────────┐
│  Spring Security Filters    │  ← Intercepte TOUTES les requêtes
└──────┬──────────────────────┘
       │ 2. Appelle UserDetailsService
       ▼
┌─────────────────────────────┐
│ CustomUserDetailsService    │
│  loadUserByUsername()       │
└──────┬──────────────────────┘
       │ 3. Cherche dans la BDD
       ▼
┌─────────────────────────────┐
│  UtilisateursRepository     │
│   findByEmail()             │
└──────┬──────────────────────┘
       │ 4. Retourne Utilisateur
       ▼
┌─────────────────────────────┐
│   PasswordEncoder           │
│  Vérifie le mot de passe    │
└──────┬──────────────────────┘
       │ 5. Si OK → Crée session
       ▼
┌─────────────────────────────┐
│  SecurityContext            │
│  Stocke l'utilisateur       │
│  connecté en mémoire        │
└─────────────────────────────┘
```

---

<a name="3-auth"></a>
## 3️⃣ AUTHENTIFICATION vs AUTORISATION

### 🔐 Authentification (Authentication)

**Question** : "Qui es-tu ?"

**Processus** :
```java
// L'utilisateur entre son email/password
String email = "docteur@gmail.com";
String password = "12345";

// Spring Security vérifie dans la BDD
UserDetails user = userDetailsService.loadUserByUsername(email);

// Compare les mots de passe
if (passwordEncoder.matches(password, user.getPassword())) {
    // ✅ Authentifié !
}
```

### 🛡️ Autorisation (Authorization)

**Question** : "Que peux-tu faire ?"

**Processus** :
```java
// L'utilisateur authentifié demande /admin/deletePatient/5
// Spring vérifie ses rôles/permissions

if (user.hasRole("ADMIN")) {
    // ✅ Autorisé !
} else {
    // ❌ 403 Forbidden
}
```

### Exemple concret dans ton code

```java
.requestMatchers("/admin/deletePatient/**").hasRole("ADMIN")
```

Cette ligne dit :
- 🔐 **Authentification** : Il faut être connecté
- 🛡️ **Autorisation** : Il faut avoir le rôle `ADMIN`

---

<a name="4-entites"></a>
## 4️⃣ LES ENTITÉS (Utilisateur, Role, Permission)

### 🧑 Utilisateur.java - L'utilisateur du système

```java
@Entity
public class Utilisateur implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String nom;                    // "Dr. Martin"
    
    @Column(unique = true, nullable = false)
    private String email;                  // "martin@doctorapp.com"
    
    @Column(nullable = false)
    private String password;               // "$2a$10$xyz..." (encodé !)
    
    // 🔐 État du compte
    private boolean enabled = false;       // Email confirmé ?
    private boolean accountLocked = false; // Compte bloqué ?
    
    // 🔢 OTP (authentification à 2 facteurs)
    private String otpCode;                // "123456"
    private LocalDateTime otpExpiration;   // Expire dans 5 min
    
    // 🔐 Sécurité
    private int loginAttempts = 0;         // Nb de tentatives ratées
    private LocalDateTime lastLogin;       // Dernière connexion
    
    // 👥 Relations
    @ManyToMany(fetch = FetchType.EAGER)   // Charge IMMÉDIATEMENT les rôles
    private Set<Role> roles = new HashSet<>();
```

#### Pourquoi `implements UserDetails` ?

Spring Security a besoin de savoir :
- ✅ Le username (email)
- ✅ Le password
- ✅ Les autorités (rôles + permissions)
- ✅ L'état du compte (actif, verrouillé, expiré)

**Interface UserDetails** :
```java
public interface UserDetails {
    String getUsername();                           // → email
    String getPassword();                           // → password
    Collection<? extends GrantedAuthority> getAuthorities(); // → rôles + permissions
    boolean isAccountNonExpired();                  // → true
    boolean isAccountNonLocked();                   // → !accountLocked
    boolean isCredentialsNonExpired();              // → true
    boolean isEnabled();                            // → enabled
}
```

#### La méthode cruciale : `getAuthorities()`

```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<GrantedAuthority> authorities = new HashSet<>();
    
    // 1️⃣ Parcourir tous les rôles de l'utilisateur
    for (Role role : roles) {
        if (role.isActive()) {
            // Ajouter le rôle (préfixe ROLE_ obligatoire !)
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getNom()));
            // Exemple : "ROLE_ADMIN", "ROLE_DOCTOR"
        }
        
        // 2️⃣ Parcourir toutes les permissions du rôle
        for (Permission perm : role.getPermissions()) {
            if (perm.isActive()) {
                authorities.add(new SimpleGrantedAuthority(perm.getNom()));
                // Exemple : "patient:read", "patient:delete"
            }
        }
    }
    
    return authorities;
}
```

**Exemple** : Si un utilisateur a le rôle `ADMIN` avec permissions `patient:read` et `patient:delete` :

```java
authorities = [
    "ROLE_ADMIN",      // Le rôle
    "patient:read",    // Permission 1
    "patient:delete"   // Permission 2
]
```

---

### 🎭 Role.java - Les rôles du système

```java
@Entity
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nom;           // "ADMIN", "DOCTOR", "PATIENT"
    
    private String description;   // "Administrateur du système"
    private boolean active = true;
    
    @ManyToMany(mappedBy = "roles")
    private Set<Utilisateur> utilisateurs = new HashSet<>();
    
    @ManyToMany
    private Set<Permission> permissions = new HashSet<>();
}
```

**Exemples de rôles** :
- `ADMIN` : Gère tout le système
- `DOCTOR` : Gère les patients et rendez-vous
- `PATIENT` : Consulte ses propres données
- `RECEPTIONIST` : Gère les rendez-vous uniquement

---

### 🔑 Permission.java - Les permissions granulaires

```java
@Entity
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String nom;           // "patient:read", "patient:delete"
    
    private String description;   // "Supprimer un patient"
    private boolean active = true;
    
    @ManyToMany(mappedBy = "permissions")
    private Set<Role> roles = new HashSet<>();
}
```

**Convention de nommage** : `ressource:action`

Exemples :
- `patient:read` → Lire les patients
- `patient:write` → Créer/modifier un patient
- `patient:delete` → Supprimer un patient
- `appointment:create` → Créer un rendez-vous
- `prescription:manage` → Gérer les ordonnances

---

### 📊 Relation entre les entités

```
┌─────────────────┐
│   Utilisateur   │
│   Dr. Martin    │
└────────┬────────┘
         │ ManyToMany
         │
         ▼
┌─────────────────┐
│      Role       │
│     DOCTOR      │
└────────┬────────┘
         │ ManyToMany
         │
         ▼
┌─────────────────┐
│   Permission    │
│  patient:read   │
│ appointment:*   │
└─────────────────┘
```

**Exemple en BDD** :

**Table `utilisateur`** :
| id | nom        | email           | password  | enabled |
|----|------------|-----------------|-----------|---------|
| 1  | Dr. Martin | martin@mail.com | $2a$10... | true    |

**Table `role`** :
| id | nom   | description    |
|----|-------|----------------|
| 1  | ADMIN | Administrateur |
| 2  | DOCTOR| Médecin        |

**Table `permission`** :
| id | nom           | description        |
|----|---------------|--------------------|
| 1  | patient:read  | Lire patients      |
| 2  | patient:delete| Supprimer patients |

**Table `utilisateur_roles` (jointure)** :
| utilisateur_id | role_id |
|----------------|---------|
| 1              | 2       |

**Table `role_permissions` (jointure)** :
| role_id | permission_id |
|---------|---------------|
| 2       | 1             |

---

<a name="5-userdetails"></a>
## 5️⃣ UserDetailsService - LE CŒUR DE L'AUTHENTIFICATION

### CustomUserDetailsService.java

```java
@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateursRepository utilisateursRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) 
            throws UsernameNotFoundException {
        
        // 1️⃣ Chercher l'utilisateur dans la BDD
        Utilisateur user = utilisateursRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "Utilisateur non trouvé : " + username
                ));

        // 2️⃣ Convertir en UserDetails (objet Spring Security)
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())              // Email comme username
                .password(user.getPassword())           // Mot de passe encodé
                .disabled(!user.isEnabled())            // Compte désactivé ?
                .accountLocked(user.isAccountLocked())  // Compte verrouillé ?
                .authorities(user.getAuthorities())     // Rôles + Permissions
                .build();
    }
}
```

### Quand cette méthode est-elle appelée ?

```
1. Utilisateur soumet /login avec email + password
                    ↓
2. Spring Security appelle loadUserByUsername(email)
                    ↓
3. Cherche dans la BDD
                    ↓
4. Retourne UserDetails
                    ↓
5. Spring compare les passwords
                    ↓
6. Si OK → Crée session
```

### Pourquoi `@Transactional(readOnly = true)` ?

```java
@Transactional(readOnly = true)
```

- ✅ Ouvre une transaction en lecture seule (plus rapide)
- ✅ Permet de charger les relations `@ManyToMany` (roles, permissions)
- ✅ Évite l'erreur `LazyInitializationException`

**Sans `@Transactional`** :
```
org.hibernate.LazyInitializationException: 
failed to lazily initialize a collection of role: 
DoctorApp.Entity.Utilisateur.roles
```

---

<a name="6-config"></a>
## 6️⃣ SecurityConfig - CONFIGURATION DE LA SÉCURITÉ

### SecurityConfig.java - Explication ligne par ligne

```java
@Configuration               // Classe de configuration Spring
@EnableWebSecurity          // Active Spring Security
@EnableMethodSecurity(prePostEnabled = true)  // Active @PreAuthorize
public class SecurityConfig {
```

#### Bean 1 : PasswordEncoder

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Rôle** : Encoder les mots de passe

**Exemple** :
```java
String plainPassword = "12345";
String encoded = passwordEncoder.encode(plainPassword);
// → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"

// Vérification
boolean match = passwordEncoder.matches("12345", encoded);
// → true
```

**Pourquoi BCrypt ?**
- ✅ Utilise un "salt" aléatoire (protection contre rainbow tables)
- ✅ Coûteux en CPU (ralentit les attaques brute-force)
- ✅ Standard de l'industrie

---

#### Bean 2 : UserDetailsService

```java
@Bean
public UserDetailsService userDetailsService(UtilisateursRepository repository) {
    return new CustomUserDetailsService(repository);
}
```

**Rôle** : Dire à Spring Security comment charger les utilisateurs

Spring Security va automatiquement :
1. Appeler `loadUserByUsername(email)`
2. Récupérer le mot de passe
3. Comparer avec celui soumis

---

#### Bean 3 : SecurityFilterChain (LA CONFIGURATION PRINCIPALE)

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            // 🔐 Configuration du formulaire de connexion
            .formLogin(form -> form
                    .loginPage("/login")              // URL de la page de connexion
                    .defaultSuccessUrl("/dashboard", true)  // Redirection après login
                    .permitAll()                      // Tout le monde peut accéder à /login
            )
            
            // 🚪 Configuration de la déconnexion
            .logout(logout -> logout
                    .logoutUrl("/logout")             // URL de déconnexion
                    .logoutSuccessUrl("/login?logout") // Redirection après logout
                    .permitAll()
            )
            
            // 🛡️ Règles d'autorisation
            .authorizeHttpRequests(auth -> auth
                    // ...
            )
            .build();
}
```

### Les règles d'autorisation

```java
.authorizeHttpRequests(auth -> auth
    // 🔴 ADMIN seulement
    .requestMatchers(
        "/admin/deletePatient/**",
        "/admin/savePatient/**",
        "/admin/editPatient/**"
    ).hasRole("ADMIN")  // Requiert ROLE_ADMIN
    
    // 🟡 DOCTOR ou ADMIN
    .requestMatchers(
        "/appointments/**",
        "/prescriptions/**"
    ).hasAnyRole("DOCTOR", "ADMIN")  // L'un des deux suffit
    
    // 🟢 Pages publiques (pas de connexion)
    .requestMatchers(
        "/",
        "/login",
        "/css/**",
        "/js/**"
    ).permitAll()  // Accessible à tous
    
    // 🔵 Tout le reste → connexion obligatoire
    .anyRequest().authenticated()
)
```

### Ordre d'évaluation (TRÈS IMPORTANT !)

Spring Security évalue les règles **de haut en bas** et s'arrête à la première correspondance.

**❌ MAUVAIS ORDRE** :
```java
.anyRequest().authenticated()      // Tout le monde doit se connecter
.requestMatchers("/login").permitAll()  // ← JAMAIS ATTEINT !
```

**✅ BON ORDRE** :
```java
.requestMatchers("/login").permitAll()  // Public d'abord
.anyRequest().authenticated()           // Puis le reste
```

---

### Les méthodes d'autorisation

| Méthode | Description | Exemple |
|---------|-------------|---------|
| `permitAll()` | Accessible à tous | Pages publiques |
| `authenticated()` | Connexion requise | Dashboard |
| `hasRole("X")` | Rôle X requis | Admin only |
| `hasAnyRole("X", "Y")` | X ou Y requis | Doctor ou Admin |
| `hasAuthority("X")` | Permission X | patient:delete |
| `denyAll()` | Interdit à tous | Maintenance |

---

<a name="7-rbac"></a>
## 7️⃣ LE SYSTÈME RBAC (Role-Based Access Control)

### Qu'est-ce que le RBAC ?

**RBAC** = Contrôle d'accès basé sur les rôles

Au lieu de donner des permissions individuelles à chaque utilisateur :
- ❌ Jean → patient:read, patient:write, appointment:create...
- ❌ Marie → patient:read, patient:write, appointment:create...

On crée des **rôles** qui regroupent des permissions :
- ✅ DOCTOR → patient:*, appointment:*
- ✅ Jean → DOCTOR
- ✅ Marie → DOCTOR

### Hiérarchie dans DoctorApp

```
┌─────────────────────────────────────┐
│             ADMIN                   │  ← Tout pouvoir
│  - patient:*                        │
│  - appointment:*                    │
│  - prescription:*                   │
│  - user:manage                      │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│            DOCTOR                   │  ← Gestion médicale
│  - patient:read, patient:write      │
│  - appointment:*                    │
│  - prescription:*                   │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│          RECEPTIONIST               │  ← Accueil
│  - patient:read                     │
│  - appointment:create, read, update │
└─────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────┐
│            PATIENT                  │  ← Consultation uniquement
│  - appointment:read (ses RDV)       │
│  - prescription:read (ses ordo.)    │
└─────────────────────────────────────┘
```

### Utilisation dans le code

#### Dans SecurityConfig

```java
// Par RÔLE
.requestMatchers("/admin/**").hasRole("ADMIN")

// Par PERMISSION
.requestMatchers("/patients/delete/**").hasAuthority("patient:delete")

// Multiple rôles
.requestMatchers("/appointments/**").hasAnyRole("DOCTOR", "ADMIN", "RECEPTIONIST")
```

#### Dans les Controllers avec @PreAuthorize

```java
@Controller
public class PatientController {

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/deletePatient/{id}")
    public String deletePatient(@PathVariable Long id) {
        // Seuls les ADMIN peuvent exécuter cette méthode
    }
    
    @PreAuthorize("hasAuthority('patient:write')")
    @PostMapping("/patients/save")
    public String savePatient(@ModelAttribute Patient patient) {
        // Seuls ceux ayant la permission patient:write
    }
    
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @GetMapping("/patients/view/{id}")
    public String viewPatient(@PathVariable Long id, Model model) {
        // DOCTOR ou ADMIN peuvent consulter
    }
}
```

#### Dans les templates Thymeleaf

```html
<!-- Afficher seulement si ADMIN -->
<div sec:authorize="hasRole('ADMIN')">
    <a href="/admin/deletePatient/5">Supprimer</a>
</div>

<!-- Afficher seulement si permission patient:delete -->
<button sec:authorize="hasAuthority('patient:delete')">
    Supprimer
</button>

<!-- Afficher si DOCTOR ou ADMIN -->
<nav sec:authorize="hasAnyRole('DOCTOR', 'ADMIN')">
    <a href="/appointments">Rendez-vous</a>
</nav>
```

---

<a name="8-password"></a>
## 8️⃣ ENCODAGE DES MOTS DE PASSE

### Pourquoi encoder les mots de passe ?

**❌ JAMAIS stocker en clair** :
```sql
-- ❌ DANGER !
INSERT INTO utilisateur (email, password) 
VALUES ('user@mail.com', '12345');
```

Si un hacker accède à ta BDD → Tous les mots de passe sont visibles !

**✅ TOUJOURS encoder** :
```sql
-- ✅ SÉCURISÉ
INSERT INTO utilisateur (email, password) 
VALUES ('user@mail.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy');
```

---

### Comment encoder un mot de passe ?

```java
@Service
public class UtilisateurService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UtilisateursRepository repository;
    
    public void creerUtilisateur(String email, String plainPassword) {
        // 1️⃣ Encoder le mot de passe
        String encodedPassword = passwordEncoder.encode(plainPassword);
        
        // 2️⃣ Créer l'utilisateur
        Utilisateur user = Utilisateur.builder()
                .email(email)
                .password(encodedPassword)  // ← Encodé !
                .enabled(true)
                .build();
        
        // 3️⃣ Sauvegarder
        repository.save(user);
    }
}
```

---

### Comment vérifier un mot de passe ?

Spring Security le fait automatiquement, mais voici comment ça marche :

```java
// Mot de passe soumis par l'utilisateur
String plainPassword = "12345";

// Mot de passe encodé dans la BDD
String encodedPassword = "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy";

// Vérification
boolean match = passwordEncoder.matches(plainPassword, encodedPassword);

if (match) {
    System.out.println("✅ Mot de passe correct !");
} else {
    System.out.println("❌ Mot de passe incorrect !");
}
```

---

### BCrypt : Comment ça marche ?

**BCrypt** génère un hash unique même pour le même mot de passe :

```java
String password = "12345";

String hash1 = passwordEncoder.encode(password);
// → "$2a$10$abc123..."

String hash2 = passwordEncoder.encode(password);
// → "$2a$10$xyz789..."  ← DIFFÉRENT !
```

**Pourquoi ?** Un **salt** aléatoire est généré à chaque fois.

**Structure d'un hash BCrypt** :
```
$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
│  │  │ └─────────────────────────────────┬─────────────────────────┘
│  │  │                                   │
│  │  │                            Hash (31 chars)
│  │  │
│  │  Salt (22 chars)
│  │
│  Cost Factor (10 = 2^10 itérations)
│
Version (2a)
```

---

<a name="9-sessions"></a>
## 9️⃣ GESTION DES SESSIONS ET DU FORMULAIRE

### Comment fonctionne une session ?

```
1. Utilisateur se connecte → Spring crée une SESSION
                              ↓
2. Spring stocke JSESSIONID dans un cookie
                              ↓
3. À chaque requête, le navigateur envoie le cookie
                              ↓
4. Spring retrouve la session et l'utilisateur connecté
```

### Récupérer l'utilisateur connecté

#### Dans un Controller

```java
@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        // 1️⃣ Via Authentication
        String email = authentication.getName();  // Email de l'utilisateur
        
        // 2️⃣ Via Principal
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        
        model.addAttribute("email", email);
        model.addAttribute("roles", authorities);
        
        return "dashboard";
    }
    
    // OU via @AuthenticationPrincipal
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String email = userDetails.getUsername();
        model.addAttribute("email", email);
        return "profile";
    }
}
```

#### Dans un Service

```java
@Service
public class AppointmentService {

    public void createAppointment(Appointment appointment) {
        // Récupérer l'utilisateur connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        
        // Utiliser l'email...
    }
}
```

#### Dans un template Thymeleaf

```html
<!-- Afficher l'email de l'utilisateur connecté -->
<p>Bienvenue, <span sec:authentication="name"></span>!</p>

<!-- Afficher les rôles -->
<p>Rôles : <span sec:authentication="authorities"></span></p>

<!-- Conditionnel -->
<div sec:authorize="isAuthenticated()">
    <p>Vous êtes connecté</p>
</div>

<div sec:authorize="!isAuthenticated()">
    <a href="/login">Se connecter</a>
</div>
```

---

### Créer une page de connexion personnalisée

**login.html** :
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Connexion - DoctorApp</title>
</head>
<body>
    <h1>Connexion</h1>
    
    <!-- Message d'erreur -->
    <div th:if="${param.error}">
        <p style="color: red;">Email ou mot de passe incorrect</p>
    </div>
    
    <!-- Message de succès après logout -->
    <div th:if="${param.logout}">
        <p style="color: green;">Vous êtes déconnecté</p>
    </div>
    
    <!-- Formulaire de connexion -->
    <form th:action="@{/login}" method="post">
        <div>
            <label>Email :</label>
            <input type="email" name="username" required />
        </div>
        
        <div>
            <label>Mot de passe :</label>
            <input type="password" name="password" required />
        </div>
        
        <div>
            <input type="checkbox" name="remember-me" />
            <label>Se souvenir de moi</label>
        </div>
        
        <button type="submit">Se connecter</button>
    </form>
</body>
</html>
```

**Points importants** :
- ✅ `name="username"` (même si c'est un email)
- ✅ `name="password"`
- ✅ `method="post"`
- ✅ `th:action="@{/login}"`

---

<a name="10-exercices"></a>
## 🎯 EXERCICES PRATIQUES

### Exercice 1 : Créer un nouvel utilisateur

**Objectif** : Créer un service pour enregistrer un nouvel utilisateur avec rôle PATIENT

```java
@Service
public class UtilisateurService {
    
    @Autowired
    private UtilisateursRepository utilisateursRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public Utilisateur creerPatient(String nom, String email, String password) {
        // TODO : 
        // 1. Vérifier si l'email existe déjà
        // 2. Encoder le mot de passe
        // 3. Récupérer le rôle PATIENT
        // 4. Créer l'utilisateur
        // 5. Sauvegarder et retourner
    }
}
```

**Solution** :
```java
public Utilisateur creerPatient(String nom, String email, String password) {
    // 1. Vérifier si l'email existe déjà
    if (utilisateursRepository.existsByEmail(email)) {
        throw new RuntimeException("Cet email est déjà utilisé");
    }
    
    // 2. Encoder le mot de passe
    String encodedPassword = passwordEncoder.encode(password);
    
    // 3. Récupérer le rôle PATIENT
    Role patientRole = roleRepository.findByNom("PATIENT")
            .orElseThrow(() -> new RuntimeException("Rôle PATIENT non trouvé"));
    
    // 4. Créer l'utilisateur
    Utilisateur user = Utilisateur.builder()
            .nom(nom)
            .email(email)
            .password(encodedPassword)
            .enabled(true)
            .accountLocked(false)
            .roles(Set.of(patientRole))
            .build();
    
    // 5. Sauvegarder et retourner
    return utilisateursRepository.save(user);
}
```

---

### Exercice 2 : Ajouter une permission

**Objectif** : Créer une nouvelle permission `prescription:manage` et l'ajouter au rôle DOCTOR

```java
@Service
public class RoleService {
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PermissionRepository permissionRepository;
    
    public void ajouterPermissionAuRole(String nomRole, String nomPermission) {
        // TODO :
        // 1. Récupérer le rôle
        // 2. Récupérer la permission
        // 3. Ajouter la permission au rôle
        // 4. Sauvegarder
    }
}
```

**Solution** :
```java
public void ajouterPermissionAuRole(String nomRole, String nomPermission) {
    // 1. Récupérer le rôle
    Role role = roleRepository.findByNom(nomRole)
            .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));
    
    // 2. Récupérer la permission
    Permission permission = permissionRepository.findByNom(nomPermission)
            .orElseThrow(() -> new RuntimeException("Permission non trouvée"));
    
    // 3. Ajouter la permission au rôle
    role.getPermissions().add(permission);
    
    // 4. Sauvegarder
    roleRepository.save(role);
}
```

---

### Exercice 3 : Protéger un endpoint

**Objectif** : Créer un controller qui :
- Permet aux DOCTOR et ADMIN de créer un rendez-vous
- Permet uniquement aux ADMIN de le supprimer

```java
@Controller
@RequestMapping("/appointments")
public class AppointmentController {
    
    // TODO : Ajouter les annotations de sécurité
    
    @PostMapping("/create")
    public String createAppointment(@ModelAttribute Appointment appointment) {
        // Créer le rendez-vous
        return "redirect:/appointments";
    }
    
    @PostMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable Long id) {
        // Supprimer le rendez-vous
        return "redirect:/appointments";
    }
}
```

**Solution** :
```java
@Controller
@RequestMapping("/appointments")
public class AppointmentController {
    
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @PostMapping("/create")
    public String createAppointment(@ModelAttribute Appointment appointment) {
        // Créer le rendez-vous
        return "redirect:/appointments";
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/delete/{id}")
    public String deleteAppointment(@PathVariable Long id) {
        // Supprimer le rendez-vous
        return "redirect:/appointments";
    }
}
```

---

### Exercice 4 : Verrouiller un compte après 3 tentatives

**Objectif** : Bloquer un compte après 3 tentatives de connexion échouées

```java
@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {
    
    @Autowired
    private UtilisateursRepository utilisateursRepository;
    
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request, 
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {
        
        String email = request.getParameter("username");
        
        // TODO :
        // 1. Récupérer l'utilisateur
        // 2. Incrémenter loginAttempts
        // 3. Si >= 3, bloquer le compte (accountLocked = true)
        // 4. Sauvegarder
        // 5. Rediriger vers /login?error
    }
}
```

**Solution** :
```java
@Override
public void onAuthenticationFailure(
        HttpServletRequest request, 
        HttpServletResponse response,
        AuthenticationException exception) throws IOException {
    
    String email = request.getParameter("username");
    
    // 1. Récupérer l'utilisateur
    utilisateursRepository.findByEmail(email).ifPresent(user -> {
        // 2. Incrémenter loginAttempts
        user.setLoginAttempts(user.getLoginAttempts() + 1);
        
        // 3. Si >= 3, bloquer le compte
        if (user.getLoginAttempts() >= 3) {
            user.setAccountLocked(true);
        }
        
        // 4. Sauvegarder
        utilisateursRepository.save(user);
    });
    
    // 5. Rediriger
    response.sendRedirect("/login?error");
}
```

**Ajouter dans SecurityConfig** :
```java
.formLogin(form -> form
        .loginPage("/login")
        .failureHandler(loginFailureHandler)  // ← Ajouter ici
        .permitAll()
)
```

---

## 📝 RÉSUMÉ DU COURS

### Les concepts clés

1. **Authentification** = Vérifier QUI tu es
2. **Autorisation** = Vérifier ce que tu peux FAIRE
3. **UserDetails** = Interface que ton entité doit implémenter
4. **UserDetailsService** = Charge les utilisateurs depuis la BDD
5. **PasswordEncoder** = Encode/vérifie les mots de passe
6. **SecurityFilterChain** = Configure les règles de sécurité
7. **RBAC** = Rôles + Permissions pour gérer les droits

### Le flux complet

```
1. Utilisateur entre email/password
         ↓
2. Spring Security appelle UserDetailsService
         ↓
3. Cherche dans la BDD
         ↓
4. Récupère Utilisateur + Rôles + Permissions
         ↓
5. Vérifie le mot de passe avec PasswordEncoder
         ↓
6. Si OK → Crée session avec SecurityContext
         ↓
7. À chaque requête, vérifie les autorisations dans SecurityFilterChain
```

---

**Besoin de clarifications sur un point spécifique ? Pose-moi tes questions ! 😊**