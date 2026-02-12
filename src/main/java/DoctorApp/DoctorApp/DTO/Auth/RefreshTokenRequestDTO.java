package DoctorApp.DoctorApp.DTO.Auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDTO {

    @NotBlank(message = "Le refresh token est obligatoire")
    private String refreshToken;
}