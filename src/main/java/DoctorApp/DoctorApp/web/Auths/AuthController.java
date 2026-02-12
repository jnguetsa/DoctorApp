package DoctorApp.DoctorApp.web.Auths;

import DoctorApp.DoctorApp.DTO.Utilisateur.RegisterFormDTO;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurResponseDTO;
import DoctorApp.DoctorApp.Exception.EmailAlreadyExistsException;
import DoctorApp.DoctorApp.Service.IUtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final IUtilisateurService utilisateurService;

    /**
     * Page de connexion
     */
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /**
     * Page d'inscription (GET)
     */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerForm", new RegisterFormDTO());
        return "register";
    }

    /**
     * Traitement de l'inscription (POST)
     */
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerForm") RegisterFormDTO registerDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        // Vérifier les erreurs de validation
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            // Créer l'utilisateur
            UtilisateurResponseDTO user = utilisateurService.register(registerDto);

            // Message de succès
            redirectAttributes.addFlashAttribute("success",
                    "Inscription réussie ! Vous pouvez maintenant vous connecter.");

            return "redirect:/login";

        } catch (RuntimeException | EmailAlreadyExistsException e) {
            // Message d'erreur
            bindingResult.rejectValue("email", "error.registerForm", e.getMessage());
            return "register";
        }
    }

    /**
     * Tableau de bord après connexion
     */
    @GetMapping("/")
    public String index(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            String email = authentication.getName();

            // Charger les infos de l'utilisateur
            UtilisateurResponseDTO user = utilisateurService.getByEmail(email);
            model.addAttribute("user", user);

            // Mettre à jour la dernière connexion
            utilisateurService.updateLastLogin(email);

            // Réinitialiser les tentatives de connexion
            utilisateurService.resetLoginAttempts(email);
        }


        return "index";

    }
}