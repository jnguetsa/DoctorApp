package DoctorApp.DoctorApp.Security;


import DoctorApp.DoctorApp.Service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Dépendances injectées automatiquement grâce à @RequiredArgsConstructor
    private final JwtUtil jwtUtil;                    // Utilitaire pour manipuler les tokens JWT
    private final CustomUserDetailsService userDetailsService;  // Service qui charge les infos utilisateur depuis la base


    /**
     * Cette méthode est appelée pour **chaque requête HTTP** qui passe par le filtre
     * (grâce à OncePerRequestFilter → exécuté une seule fois par requête)
     *
     * Rôle principal : vérifier s'il y a un token JWT valide dans la requête
     * et si oui → authentifier l'utilisateur dans le contexte Spring Security
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. On récupère l'en-tête Authorization de la requête
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 2. Vérification du format classique du Bearer Token
        //    Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

            // On extrait la partie après "Bearer " (les 7 premiers caractères)
            jwt = authorizationHeader.substring(7);

            try {
                // On essaie d'extraire le username (subject) du token
                username = jwtUtil.extractUsername(jwt);
                log.debug("🔑 JWT détecté pour l'utilisateur : {}", username);
            } catch (Exception e) {
                // Token mal formé, signature invalide, expiré, etc.
                log.error("❌ Impossible d'extraire le username du JWT : {}", e.getMessage());
                // On continue quand même (on ne bloque pas ici)
            }
        }

        // 3. Si on a réussi à extraire un username ET qu'il n'y a PAS encore
        //    d'authentification dans le contexte Spring Security pour cette requête
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // On charge les informations complètes de l'utilisateur (rôles, etc.)
            // via notre service personnalisé
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 4. On valide vraiment le token par rapport à cet utilisateur
            //    (vérifie : username correspond + token non expiré + signature valide)
            if (jwtUtil.validateToken(jwt, userDetails)) {

                // 5. On crée l'objet d'authentification Spring Security
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,           // principal = l'utilisateur
                                null,                  // credentials = null car JWT
                                userDetails.getAuthorities()  // rôles / permissions
                        );

                // 6. On ajoute les informations de la requête (IP, session, etc.)
                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 7. On place l'authentification dans le contexte de sécurité
                //    → à partir de maintenant, l'utilisateur est considéré comme connecté
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("✅ Utilisateur {} authentifié via JWT", username);
            }
        }

        // 8. On passe la main au filtre suivant dans la chaîne
        //    (même si l'authentification a échoué → on laisse les filtres de sécurité décider)
        filterChain.doFilter(request, response);
    }
}
