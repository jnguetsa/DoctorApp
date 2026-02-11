package DoctorApp.DoctorApp.Entity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.*;

@Getter @Setter  @Builder @AllArgsConstructor @NoArgsConstructor @Entity
public class Utilisateur implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // 🔐 Etat du compte
    private boolean enabled = false;          // email confirmé ?
    private boolean accountLocked = false;

    // 🔁 Première connexion
    private boolean firstLogin = true;

    // 🔢 OTP
    private String otpCode;
    private LocalDateTime otpExpiration;

    // 🔐 Sécurité
    private int loginAttempts = 0;
    private LocalDateTime lastLogin;

    // 🧑‍💼 Rôles
    @ManyToMany(fetch = FetchType.EAGER)
    private Set<Role> roles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();

        for (Role role : roles) {
            if (role.isActive()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getNom()));
            }

            for (Permission perm : role.getPermissions()) {
                if (perm.isActive()) {
                    authorities.add(new SimpleGrantedAuthority(perm.getNom()));
                }
            }
        }
        return authorities;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

