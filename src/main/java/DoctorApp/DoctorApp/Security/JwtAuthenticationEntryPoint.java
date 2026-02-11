package DoctorApp.DoctorApp.Security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Cette classe gère les cas où une requête arrive sur une ressource protégée
 * ET que l'utilisateur n'est PAS authentifié (ou son authentification a échoué).
 *
 * C'est l'équivalent d'un "401 Unauthorized" personnalisé dans une API REST.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Méthode appelée automatiquement par Spring Security quand :
     * - Une requête arrive sur une URL protégée
     * - AUCUNE authentification valide n'est présente dans SecurityContext
     *   (pas de token, token invalide, token expiré, etc.)
     *
     * Rôle : renvoyer une réponse HTTP 401 avec un corps JSON clair et structuré
     * au lieu de la page HTML d'erreur par défaut de Spring Security.
     */
    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        // 1. On définit le statut HTTP à 401 Unauthorized
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // 2. On indique que la réponse est du JSON
        response.setContentType("application/json");

        // 3. On construit un objet JSON contenant des informations utiles
        Map<String, Object> data = new HashMap<>();

        data.put("timestamp", LocalDateTime.now().toString());     // Quand l'erreur s'est produite
        data.put("status", 401);                                   // Code HTTP
        data.put("error", "Unauthorized");                         // Type d'erreur (standard)
        data.put("message", "Token manquant ou invalide");         // Message clair pour le client
        data.put("path", request.getRequestURI());                 // URL qui a causé l'erreur

        // 4. On convertit cette Map en JSON grâce à Jackson
        ObjectMapper mapper = new ObjectMapper();

        // 5. On écrit le JSON directement dans la réponse HTTP
        response.getWriter().write(mapper.writeValueAsString(data));

        // Pas besoin d'appeler flush() ou close() ici, Spring s'en occupe
    }
}