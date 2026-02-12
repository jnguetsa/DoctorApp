package DoctorApp.DoctorApp.DTO.Utilisateur;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO pour retourner les informations d'un utilisateur
 * Utilisé par l'API REST et les pages web
 */
@Data
public class UtilisateurResponseDTO {

    private Long id;
    private String nom;
    private String email;

    // État du compte
    private boolean enabled;
    private boolean accountLocked;
    private boolean firstLogin;

    // Sécurité
    private int loginAttempts;
    private LocalDateTime lastLogin;

    // Rôles et permissions
    private List<String> roles;        // ["ROLE_ADMIN", "ROLE_DOCTOR"]
    private List<String> permissions;  // ["patient:read", "patient:write"]

    // ❌ PAS de password ici !
    // ❌ PAS de otpCode ici !
}