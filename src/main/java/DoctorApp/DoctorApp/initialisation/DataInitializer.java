package DoctorApp.DoctorApp.initialisation;

import DoctorApp.DoctorApp.Entity.Permission;
import DoctorApp.DoctorApp.Entity.Role;
import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.repository.PermissionRepository;
import DoctorApp.DoctorApp.repository.RoleRepository;
import DoctorApp.DoctorApp.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UtilisateursRepository utilisateursRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional  // ✅ CRITIQUE : Garde la session ouverte
    public void run(String... args) throws Exception {

        log.info("🚀 Initialisation des données...");

        // ========================================
        // 1️⃣ CRÉER LES PERMISSIONS
        // ========================================
        Permission patientRead = createPermissionIfNotExists("patient:read", "Lire les patients");
        Permission patientWrite = createPermissionIfNotExists("patient:write", "Modifier les patients");
        Permission patientDelete = createPermissionIfNotExists("patient:delete", "Supprimer les patients");
        Permission appointmentManage = createPermissionIfNotExists("appointment:manage", "Gérer les rendez-vous");
        Permission appointmentRead = createPermissionIfNotExists("appointment:read", "Consulter les rendez-vous");
        Permission prescriptionManage = createPermissionIfNotExists("prescription:manage", "Gérer les prescriptions");

        log.info("✅ Permissions créées/vérifiées");

        // ========================================
        // 2️⃣ CRÉER LES RÔLES AVEC LEURS PERMISSIONS
        // ========================================

        // 🔴 ADMIN (tous les droits)
        Role adminRole = createOrUpdateRole(
                "ADMIN",
                "Administrateur du système",
               new HashSet<>( Set.of(patientRead, patientWrite, patientDelete,
                       appointmentManage, appointmentRead,
                       prescriptionManage))
        );

        // 🟡 DOCTOR (gestion médicale)
        Role doctorRole = createOrUpdateRole(
                "DOCTOR",
                "Médecin",
                new HashSet<>(Set.of(patientRead, patientWrite,
                        appointmentManage, appointmentRead,
                        prescriptionManage))
        );

        // 🟢 PATIENT (consultation uniquement)
        Role patientRole = createOrUpdateRole(
                "PATIENT",
                "Patient",
                new HashSet<>(Set.of(appointmentRead) ) // Juste consulter ses propres RDV
        );

        log.info("✅ Rôles créés/mis à jour avec leurs permissions");

        // ========================================
        // 3️⃣ CRÉER UN ADMIN PAR DÉFAUT
        // ========================================
        if (utilisateursRepository.count() == 0) {
            Utilisateur admin = Utilisateur.builder()
                    .nom("Junior NOUMEDEM")
                    .email("juniornoumedem02@gmail.com")
                    .password(passwordEncoder.encode("admin123"))
                    .enabled(true)
                    .accountLocked(false)
                    .firstLogin(true)
                    .roles(new HashSet<>(Set.of(adminRole)))
                    .build();

            utilisateursRepository.save(admin);

//            Utilisateur medecinRole = Utilisateur.builder()
//                    .nom("Junior Nguetsa")
//                    .email("jniornoumedem02@gmail.com")
//                    .password(passwordEncoder.encode("admin123"))
//                    .enabled(true)
//                    .accountLocked(false)
//                    .firstLogin(true)
//                    .roles(new HashSet<>(Set.of(doctorRole)))
//                    .build();
//
//            utilisateursRepository.save(medecinRole);

            log.info("✅ Admin créé : juniornoumedem02@gmail.com / admin123");
            log.info("   Rôles : {}", admin.getRoles().stream().map(Role::getNom).toList());
            log.info("   Permissions : {}", admin.getAuthorities());
//            log.info("   Rôles : {}", medecinRole.getRoles().stream().map(Role::getNom).toList());
//            log.info("   Permissions : {}", medecinRole.getAuthorities());
        } else {
            log.info("ℹ️  Des utilisateurs existent déjà, création ignorée.");
        }

        log.info("🎉 Initialisation terminée !");
    }

    /**
     * Crée une permission si elle n'existe pas
     */
    private Permission createPermissionIfNotExists(String nom, String description) {
        return permissionRepository.findByNom(nom)
                .orElseGet(() -> {
                    Permission perm = Permission.builder()
                            .nom(nom)
                            .description(description)
                            .active(true)
                            .build();
                    Permission saved = permissionRepository.save(perm);
                    log.debug("   ➕ Permission créée : {}", nom);
                    return saved;
                });
    }

    /**
     * Crée ou met à jour un rôle avec ses permissions
     */
    private Role createOrUpdateRole(String nom, String description, Set<Permission> permissions) {
        Role role = roleRepository.findByNom(nom)
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .nom(nom)
                            .description(description)
                            .active(true)
                            .permissions(new HashSet<>())  // ✅ Initialiser vide
                            .build();
                    log.debug("   ➕ Rôle créé : {}", nom);
                    return newRole;
                });

        // ✅ MISE À JOUR DES PERMISSIONS (crucial !)
        role.setPermissions(permissions);

        // ✅ SAUVEGARDER (persiste la relation ManyToMany)
        Role saved = roleRepository.save(role);

        log.debug("   🔄 Rôle {} mis à jour avec {} permissions",
                nom, permissions.size());

        return saved;
    }
}