package DoctorApp.DoctorApp.Service.initialisation;


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
        // Créer les permissions
        Permission patientRead = createPermissionIfNotExists("patient:read", "Lire les patients");
        Permission patientWrite = createPermissionIfNotExists("patient:write", "Modifier les patients");
        Permission patientDelete = createPermissionIfNotExists("patient:delete", "Supprimer les patients");
        Permission appointmentManage = createPermissionIfNotExists("appointment:manage", "Gérer les rendez-vous");

        // Créer les rôles
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

        // ✅ Créer un admin par défaut
        if (utilisateursRepository.count() == 0) {
            Utilisateur admin = Utilisateur.builder()
                    .nom("Administrateur")
                    .email("juniornoumedem02@gmail.com")
                    .password(passwordEncoder.encode("admin123"))
                    .enabled(true)              // ✅ IMPORTANT
                    .accountLocked(false)       // ✅ IMPORTANT
                    .firstLogin(true)
                    .roles(Set.of(adminRole))
                    .build();

            utilisateursRepository.save(admin);
            System.out.println("✅ Admin créé : juniornoumedem02@gmail.com / admin123");
        } else {
            System.out.println("ℹ️ Des utilisateurs existent déjà, initialisation ignorée.");
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
