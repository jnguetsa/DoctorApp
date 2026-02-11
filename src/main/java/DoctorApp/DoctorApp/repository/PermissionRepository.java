package DoctorApp.DoctorApp.repository;

import DoctorApp.DoctorApp.Entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByNom(String nom);
}
