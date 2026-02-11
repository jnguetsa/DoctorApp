
package DoctorApp.DoctorApp.Deburg;


import DoctorApp.DoctorApp.Entity.Utilisateur;
import DoctorApp.DoctorApp.repository.UtilisateursRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final UtilisateursRepository utilisateursRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers() {
        return utilisateursRepository.findAll().stream()
                .map(user -> {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("id", user.getId());
                    userMap.put("nom", user.getNom());
                    userMap.put("email", user.getEmail());
                    userMap.put("enabled", user.isEnabled());
                    userMap.put("accountLocked", user.isAccountLocked());
                    userMap.put("passwordHash", user.getPassword().substring(0, 20) + "...");
                    userMap.put("roles", user.getRoles().stream()
                            .map(role -> role.getNom())
                            .collect(Collectors.toList()));
                    return userMap;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/test-password")
    public Map<String, Object> testPassword() {
        String plainPassword = "admin123";
        String encodedPassword = passwordEncoder.encode(plainPassword);

        Map<String, Object> result = new HashMap<>();
        result.put("plainPassword", plainPassword);
        result.put("encodedPassword", encodedPassword);
        result.put("matches", passwordEncoder.matches(plainPassword, encodedPassword));

        // Test avec le password de l'admin en base
        utilisateursRepository.findByEmail("juniornoumedem02@gmail.com").ifPresent(user -> {
            boolean passwordMatches = passwordEncoder.matches(plainPassword, user.getPassword());
            result.put("adminPasswordMatches", passwordMatches);
            result.put("adminPasswordHash", user.getPassword().substring(0, 30) + "...");
        });

        return result;
    }
}