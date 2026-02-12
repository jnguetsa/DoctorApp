package DoctorApp.DoctorApp.Service;

import DoctorApp.DoctorApp.DTO.Utilisateur.RegisterFormDTO;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurResponseDTO;
import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.Exception.EmailAlreadyExistsException;
import DoctorApp.DoctorApp.Exception.UserNotFoundException;
import DoctorApp.DoctorApp.Mapper.UtilisateurMapper;
import DoctorApp.DoctorApp.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements IUtilisateurService {

    private final UtilisateursRepository utilisateursRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final PasswordEncoder passwordEncoder;


    /**
     * Inscription d'un nouvel utilisateur (via formulaire web)
     *
     * Étapes :
     * 1. Vérifie que l'email n'est pas déjà pris
     * 2. Convertit le DTO en entité (avec rôle PATIENT par défaut)
     * 3. Encode le mot de passe
     * 4. Sauvegarde l'utilisateur en base
     * 5. Retourne les informations de l'utilisateur créé (sans mot de passe)
     *
     * Utilisé par : contrôleur d'inscription web
     */
    @Override
    @Transactional
    public UtilisateurResponseDTO register(RegisterFormDTO registerDTO) throws EmailAlreadyExistsException {

        log.info("📝 Inscription d'un nouvel utilisateur : {}", registerDTO.getEmail());

        // Vérification unicité de l'email
        if (utilisateursRepository.existsByEmail(registerDTO.getEmail())) {
            throw new EmailAlreadyExistsException("Cet email est déjà utilisé");
        }

        // Conversion DTO → Entity (rôle PATIENT attribué automatiquement)
        Utilisateur utilisateur = utilisateurMapper.toEntity(registerDTO);

        // Encodage sécurisé du mot de passe (BCrypt)
        utilisateur.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        // Persistance en base de données
        Utilisateur saved = utilisateursRepository.save(utilisateur);

        log.info("✅ Utilisateur créé avec succès : {}", saved.getEmail());

        // Retour de la réponse (données utilisateur sans mot de passe)
        return utilisateurMapper.toDto(saved);
    }


    /**
     * Recherche un utilisateur par son email
     *
     * → Utilisé principalement pour :
     *   - Vérifier si un utilisateur existe lors du login
     *   - Charger les données pour l'authentification
     *
     * Lance une exception si l'utilisateur n'existe pas
     */
    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponseDTO getByEmail(String email) {
        Utilisateur utilisateur = utilisateursRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'email : " + email));

        return utilisateurMapper.toDto(utilisateur);
    }


    /**
     * Recherche un utilisateur par son ID
     *
     * → Utilisé quand on a déjà l'ID (par exemple après authentification JWT,
     *   dans des endpoints qui récupèrent le profil de l'utilisateur connecté)
     *
     * Lance une exception si l'utilisateur n'existe pas
     */
    @Override
    @Transactional(readOnly = true)
    public UtilisateurResponseDTO getById(Long id) {
        Utilisateur utilisateur = utilisateursRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID : " + id));

        return utilisateurMapper.toDto(utilisateur);
    }


    /**
     * Met à jour la date de dernière connexion de l'utilisateur
     *
     * Appelée après chaque authentification réussie
     * (généralement dans le filtre JWT ou dans le service d'authentification)
     */
    @Override
    @Transactional
    public void updateLastLogin(String email) {
        utilisateursRepository.findByEmail(email).ifPresent(user -> {
            user.setLastLogin(LocalDateTime.now());
            utilisateursRepository.save(user);
            log.info("🕒 Dernière connexion mise à jour pour : {}", email);
        });
    }


    /**
     * Incrémente le compteur de tentatives de connexion échouées
     *
     * Appelée à chaque tentative de login incorrecte
     *
     * Si le nombre de tentatives atteint 5 (ou la limite définie) :
     *   → le compte est bloqué (accountLocked = true)
     */
    @Override
    @Transactional
    public void incrementLoginAttempts(String email) {
        utilisateursRepository.findByEmail(email).ifPresent(user -> {
            user.setLoginAttempts(user.getLoginAttempts() + 1);

            // Seuil de blocage (ici 5 tentatives)
            if (user.getLoginAttempts() >= 5) {
                user.setAccountLocked(true);
                log.warn("🔒 Compte bloqué après 5 tentatives : {}", email);
            }

            utilisateursRepository.save(user);
        });
    }


    /**
     * Réinitialise à zéro le compteur de tentatives de connexion
     *
     * Appelée quand :
     *   - L'utilisateur se connecte avec succès
     *   - (optionnel) Après déblocage manuel par un admin
     */
    @Override
    @Transactional
    public void resetLoginAttempts(String email) {
        utilisateursRepository.findByEmail(email).ifPresent(user -> {
            user.setLoginAttempts(0);
            utilisateursRepository.save(user);
            log.info("🔓 Tentatives de connexion réinitialisées pour : {}", email);
        });
    }
}