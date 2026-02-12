package DoctorApp.DoctorApp.Service;

import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Service personnalisé qui implémente UserDetailsService
 *
 * Rôle principal : permettre à Spring Security de charger les informations d'un utilisateur
 * à partir de son email (et non username classique) lors de l'authentification.
 *
 * C'est cette classe qui est appelée quand on fait :
 *   authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password))
 *   ou quand le JwtAuthenticationFilter charge l'utilisateur à partir du token
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateursRepository utilisateursRepository;


    /**
     * Méthode principale exigée par l'interface UserDetailsService
     *
     * Spring Security appelle cette méthode quand il a besoin de :
     * - Vérifier les identifiants lors du login
     * - Charger les rôles/autorités d'un utilisateur déjà identifié via JWT
     *
     * @param email l'identifiant utilisé pour se connecter (dans ton cas : l'email)
     * @return UserDetails contenant : username, password, enabled, expired, locked, authorities
     * @throws UsernameNotFoundException si l'utilisateur n'existe pas
     */
    @Override
    @Transactional(readOnly = true)   // Lecture seule + ouvre une transaction (utile si relations lazy)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        log.info("🔍 Tentative de chargement de l'utilisateur : {}", email);

        // Recherche de l'utilisateur par email
        Utilisateur utilisateur = utilisateursRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé : " + email));

        // ────────────────────────────────────────────────────────────────
        // Construction des authorities (rôles + permissions)
        // ────────────────────────────────────────────────────────────────
        var authorities = utilisateur.getRoles().stream()

                // Pour chaque rôle de l'utilisateur
                .flatMap(role -> {

                    // 1. On crée toujours l'autorité ROLE_XXX (convention Spring Security)
                    //    Exemple : ROLE_ADMIN, ROLE_MEDECIN, ROLE_PATIENT
                    var roleAuthority = new SimpleGrantedAuthority("ROLE_" + role.getNom());

                    // 2. On ajoute toutes les permissions associées au rôle (si tu en utilises)
                    //    Exemple : "CREATE_PATIENT", "VIEW_DOSSIER_MEDICAL", etc.
                    var permissionAuthorities = role.getPermissions().stream()
                            .map(permission -> new SimpleGrantedAuthority(permission.getNom()));

                    // On combine le rôle + ses permissions dans un seul flux
                    return java.util.stream.Stream.concat(
                            java.util.stream.Stream.of(roleAuthority),
                            permissionAuthorities
                    );
                })
                // On collecte tout ça dans une liste immuable
                .toList();

        log.info("🔑 Authorities générées : {}", authorities);

        // ────────────────────────────────────────────────────────────────
        // Création de l'objet UserDetails attendu par Spring Security
        // ────────────────────────────────────────────────────────────────
        return new org.springframework.security.core.userdetails.User(
                utilisateur.getEmail(),                     // username (ici = email)
                utilisateur.getPassword(),                  // mot de passe hashé
                utilisateur.isEnabled(),                    // compte activé ?
                true,                               // accountNonExpired (on ne gère pas pour l'instant)
                true,                               // credentialsNonExpired (on ne gère pas pour l'instant)
                !utilisateur.isAccountLocked(),             // compte non verrouillé
                authorities                                 // liste des rôles + permissions
        );
    }
}


//        ## 📝 Explication
//
//L'annotation `@Transactional(readOnly = true)` :
//        - **Garde la session Hibernate ouverte** pendant toute la méthode
//- Permet le **chargement lazy** des collections (`roles` et `permissions`)
//- Les appels à `.size()` **forcent le chargement** des données avant la fermeture de la session
//
//## 🧪 Test
//
//1. **Redémarre l'application**
//        2. **Essaie de te connecter** avec :
//        - Email: `juniornoumedem02@gmail.com`
//        - Password: `admin123`
//
//Tu devrais maintenant voir dans les logs :
//        ```
//        ✅ Utilisateur trouvé !
//        🎭 Role: ADMIN
//   🔑 Authorities: [ROLE_ADMIN, patient:read, patient:write, ...]