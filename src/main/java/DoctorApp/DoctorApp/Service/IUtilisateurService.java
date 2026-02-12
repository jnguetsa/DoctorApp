package DoctorApp.DoctorApp.Service;

import DoctorApp.DoctorApp.DTO.Utilisateur.RegisterFormDTO;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurResponseDTO;
import DoctorApp.DoctorApp.Exception.EmailAlreadyExistsException;

/**
 * Interface des services utilisateur
 */
public interface IUtilisateurService {

    /**
     * Enregistre un nouvel utilisateur (formulaire web)
     */
    UtilisateurResponseDTO register(RegisterFormDTO registerDto) throws  EmailAlreadyExistsException;

    /**
     * Récupère un utilisateur par son email
     */
    UtilisateurResponseDTO getByEmail(String email);

    /**
     * Récupère un utilisateur par son ID
     */
    UtilisateurResponseDTO getById(Long id);

    /**
     * Met à jour la date de dernière connexion
     */
    void updateLastLogin(String email);

    /**
     * Incrémente le compteur de tentatives échouées
     */
    void incrementLoginAttempts(String email);

    /**
     * Réinitialise le compteur de tentatives
     */
    void resetLoginAttempts(String email);
}