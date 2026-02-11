package DoctorApp.DoctorApp.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtil {

  @Value("${jwt.secret}")
  private String SECRET_KEY;          // Clé secrète lue depuis application.properties/yaml

  @Value("${jwt.expiration}")
  private Long JWT_EXPIRATION;        // Durée de vie du token en millisecondes (ex: 86400000 = 24h)


  /**
   * Transforme la clé secrète (String) en un objet SecretKey utilisable par JJWT
   * Utilise HMAC-SHA (généralement SHA-256 ou plus)
   */
  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
  }


  /**
   * Extrait le nom d'utilisateur (subject) contenu dans le token JWT
   * C'est généralement le champ "sub" du payload
   */
  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }


  /**
   * Récupère la date d'expiration du token
   */
  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }


  /**
   * Méthode générique qui permet d'extraire n'importe quelle information (claim)
   * du token en appliquant une fonction sur l'objet Claims
   *
   * Exemple : extractClaim(token, Claims::getSubject)    → username
   *          extractClaim(token, Claims::getExpiration) → date expiration
   *          extractClaim(token, claims -> claims.get("role")) → rôle custom
   */
  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }


  /**
   * Parse et vérifie la signature du token, puis retourne tous les claims (payload)
   * Si la signature est invalide ou le token malformé → exception levée
   */
  private Claims extractAllClaims(String token) {
    return Jwts.parser()
            .verifyWith(getSigningKey())           // Vérifie la signature avec la clé
            .build()
            .parseSignedClaims(token)              // Parse le token
            .getPayload();                         // Récupère le payload (claims)
  }


  /**
   * Vérifie si le token est déjà expiré
   * Compare la date d'expiration avec l'heure actuelle
   */
  private Boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }


  /**
   * Méthode principale : génère un token JWT pour un utilisateur
   *
   * - Récupère le username
   * - Ajoute les rôles/autorités dans une claim "authorities"
   * - Crée le token signé
   */
  public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();

    // On stocke la liste des rôles/autorités (ROLE_USER, ROLE_ADMIN, etc.)
    claims.put("authorities", userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList()));

    return createToken(claims, userDetails.getUsername());
  }


  /**
   * Crée concrètement le token JWT avec :
   *  - les claims personnalisés
   *  - le subject (username)
   *  - la date d'émission
   *  - la date d'expiration
   *  - la signature
   */
  private String createToken(Map<String, Object> claims, String subject) {
    Date now = new Date();
    Date expirationDate = new Date(now.getTime() + JWT_EXPIRATION);

    return Jwts.builder()
            .claims(claims)                     // claims personnalisés (roles, etc.)
            .subject(subject)                   // username
            .issuedAt(now)                      // date de création
            .expiration(expirationDate)         // date d'expiration
            .signWith(getSigningKey())          // signature avec la clé secrète
            .compact();                         // génère la chaîne finale
  }


  /**
   * Valide un token par rapport à un utilisateur spécifique
   * Critères de validation :
   * 1. Le username dans le token == username de l'utilisateur
   * 2. Le token n'est PAS expiré
   */
  public Boolean validateToken(String token, UserDetails userDetails) {
    try {
      final String username = extractUsername(token);
      return (username.equals(userDetails.getUsername())
              && !isTokenExpired(token));
    } catch (JwtException | IllegalArgumentException e) {
      log.error("❌ Token invalide : {}", e.getMessage());
      return false;
    }
  }


  /**
   * Version plus légère : valide juste que le token
   * - est bien signé
   * - n'est pas expiré
   *
   * (utilisé parfois pour des endpoints qui ne nécessitent pas un UserDetails)
   */
  public Boolean validateToken(String token) {
    try {
      extractAllClaims(token);           // → vérifie signature + format
      return !isTokenExpired(token);
    } catch (JwtException | IllegalArgumentException e) {
      log.error("❌ Token invalide : {}", e.getMessage());
      return false;
    }
  }
}