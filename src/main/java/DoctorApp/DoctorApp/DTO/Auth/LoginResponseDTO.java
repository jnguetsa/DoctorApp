package DoctorApp.DoctorApp.DTO.Auth;

import java.util.List;

public class LoginResponseDTO {

    private String token;
    private String refreshToken;
    private String type;
    private Long expiresIn;
    private String email;
    private String nom;
    private List<String> roles;
    private List<String> permissions;
}
