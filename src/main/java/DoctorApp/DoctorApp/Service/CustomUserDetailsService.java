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

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateursRepository utilisateursRepository;

    @Override
    @Transactional(readOnly = true)  // ✅ AJOUT CRITIQUE
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("🔍 Tentative de chargement de l'utilisateur avec l'email : {}", email);

        Utilisateur utilisateur = utilisateursRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("❌ Utilisateur non trouvé : {}", email);
                    return new UsernameNotFoundException("Utilisateur non trouvé : " + email);
                });

        log.info("✅ Utilisateur trouvé !");
        log.info("   📧 Email: {}", utilisateur.getEmail());
        log.info("   👤 Nom: {}", utilisateur.getNom());
        log.info("   ✔️ Enabled: {}", utilisateur.isEnabled());
        log.info("   🔒 Account Locked: {}", utilisateur.isAccountLocked());

        // Force le chargement des rôles et permissions
        utilisateur.getRoles().size();
        utilisateur.getRoles().forEach(role -> {
            log.info("   🎭 Role: {}", role.getNom());
            role.getPermissions().size(); // Force le chargement des permissions
        });

        log.info("   🔑 Authorities: {}", utilisateur.getAuthorities());

        return new org.springframework.security.core.userdetails.User(
                utilisateur
                        .getEmail(), utilisateur.getPassword(), Collections
                        .singleton(new SimpleGrantedAuthority(utilisateur.getRoles().toString())));
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