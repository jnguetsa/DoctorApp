package DoctorApp.DoctorApp.Security;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration principale de la sécurité Spring Security
 *
 * Cette classe définit DEUX chaînes de sécurité distinctes :
 *  1. Une pour l'API REST (/api/**) → authentification JWT + stateless
 *  2. Une pour l'application web classique → authentification par formulaire + sessions
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)   // Active @PreAuthorize, @Secured, @RolesAllowed sur les méthodes
@RequiredArgsConstructor
public class SecurityConfig {

    // Dépendances injectées
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;


    /**
     * Encodeur de mots de passe utilisé dans toute l'application
     * BCrypt est le choix le plus courant et recommandé en 2024/2025
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * Fournisseur d'authentification qui utilise :
     * - UserDetailsService pour charger l'utilisateur
     * - PasswordEncoder pour vérifier le mot de passe
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    /**
     * Exposition de l'AuthenticationManager (utile pour l'authentification manuelle,
     * par exemple dans le contrôleur de login)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    /**
     * Chaîne de sécurité N°1 (priorité la plus haute) → API REST
     *
     * Ne concerne que les URLs qui commencent par /api/
     * Utilise JWT → stateless → pas de session
     */
    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Limite cette configuration aux URLs /api/**
                .securityMatcher("/api/**")

                // Désactive CSRF (inutile et problématique avec JWT/stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Règles d'autorisation précises
                .authorizeHttpRequests(auth -> auth
                        // Endpoints d'authentification publics (login, register, refresh token...)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Routes réservées aux administrateurs
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Routes accessibles aux docteurs et admins
                        .requestMatchers("/api/patients/**", "/api/appointments/**")
                        .hasAnyRole("DOCTOR", "ADMIN")

                        // Tout le reste de l'API nécessite d'être authentifié
                        .anyRequest().authenticated()
                )

                // Gestion des erreurs d'authentification (401)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )

                // Pas de sessions → authentification par token à chaque requête
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Utilise notre DaoAuthenticationProvider
                .authenticationProvider(authenticationProvider())

                // Ajoute notre filtre JWT AVANT le filtre par défaut de formulaire
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    /**
     * Chaîne de sécurité N°2 → Application web classique (formulaire + sessions)
     *
     * Concerne toutes les autres URLs (/**)
     * Utilisée pour l'interface web (pages thymeleaf, login web, etc.)
     */
    @Bean
    @Order(2)
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // Concerne tout le reste (priorité inférieure à l'API)
                .securityMatcher("/**")

                .authenticationProvider(authenticationProvider())

                // Formulaire de login classique
                .formLogin(form -> form
                        .loginPage("/login")                    // Page personnalisée de login
                        .usernameParameter("email")             // Champ email au lieu de username
                        .passwordParameter("password")
                        .defaultSuccessUrl("/index", true)      // Redirection après succès
                        .failureUrl("/login?error=true")        // Redirection en cas d'échec
                        .permitAll()
                )

                // Gestion de la déconnexion
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")      // Retour à login avec message
                        .permitAll()
                )
                .exceptionHandling(ex->ex.accessDeniedPage("/notFound"))
                // Règles d'autorisation pour les pages web
                .authorizeHttpRequests(auth -> auth
                        // Routes admin
                        .requestMatchers(
                                "/admin/deletePatient/**",
                                "/admin/savePatient/**",
                                "/admin/editPatient/**",
                                "/admin/users/**",
                                "/admin/settings/**"
                        ).hasRole("ADMIN")

                        // Routes docteurs + admin
                        .requestMatchers(
                                "/appointments/**",
                                "/prescriptions/**",
                                "/patients/view/**"
                        ).hasAnyRole("DOCTOR", "ADMIN")

                        // Pages et ressources publiques
                        .requestMatchers(
                                "/",
                                "/login",
                                "/register",
                                "/about",
                                "/contact",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/webjars/**"
                        ).permitAll()

                        // Tout le reste nécessite d'être connecté
                        .anyRequest().authenticated()

                );

        return http.build();
    }
}