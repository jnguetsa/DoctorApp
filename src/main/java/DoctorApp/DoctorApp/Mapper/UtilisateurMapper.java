package DoctorApp.DoctorApp.Mapper;


import DoctorApp.DoctorApp.DTO.Auth.RegisterRequestDTO;
import DoctorApp.DoctorApp.DTO.Utilisateur.RegisterFormDTO;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurResponseDTO;
import DoctorApp.DoctorApp.Entity.Role;
import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.Exception.RolePatientNotFound;
import DoctorApp.DoctorApp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper pour les conversions liées à l'entité Utilisateur
 */
@Component
@RequiredArgsConstructor
public class UtilisateurMapper {

    private final RoleRepository roleRepository;

    /**
     * Crée un nouvel utilisateur avec les valeurs par défaut et le rôle PATIENT
     * Méthode factorisée utilisée par les deux méthodes toEntity
     */
    private Utilisateur prepareNewUtilisateur() {
        Utilisateur entity = new Utilisateur();

        entity.setEnabled(true);
        entity.setAccountLocked(false);
        entity.setFirstLogin(true);
        entity.setLoginAttempts(0);

        // Attribution du rôle par défaut : PATIENT
        Role patientRole = roleRepository.findByNom("PATIENT")
                .orElseThrow(() -> new RolePatientNotFound("Rôle PATIENT non trouvé"));

        Set<Role> roles = new HashSet<>();
        roles.add(patientRole);
        entity.setRoles(roles);

        return entity;
    }

    /**
     * Entity → ResponseDto
     * Utilisé pour retourner les informations d'un utilisateur (API + Web)
     */
    public UtilisateurResponseDTO toDto(Utilisateur entity) {
        if (entity == null) {
            return null;
        }

        UtilisateurResponseDTO dto = new UtilisateurResponseDTO();

        // Copie automatique des champs ayant le même nom
        BeanUtils.copyProperties(entity, dto);

        // Séparation des rôles et permissions
        dto.setRoles(entity.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .collect(Collectors.toList()));

        dto.setPermissions(entity.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toList()));

        return dto;
    }

    /**
     * RegisterFormDto (formulaire web) → Entity
     */
    public Utilisateur toEntity(RegisterFormDTO dto) {
        if (dto == null) {
            return null;
        }

        Utilisateur entity = prepareNewUtilisateur();

        // Copie automatique des champs communs (nom, email, etc.)
        BeanUtils.copyProperties(dto, entity);

        // Sécurité : on s'assure que le mot de passe n'est pas défini ici
        entity.setPassword(null);

        return entity;
    }

    /**
     * RegisterRequestDto (API) → Entity
     */
    public Utilisateur toEntity(RegisterRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Utilisateur entity = prepareNewUtilisateur();

        // Copie automatique des champs communs (nom, email, etc.)
        BeanUtils.copyProperties(dto, entity);

        // Sécurité : on s'assure que le mot de passe n'est pas défini ici
        entity.setPassword(null);

        return entity;
    }
}