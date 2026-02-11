package DoctorApp.DoctorApp.DTO;

import jakarta.persistence.Column;
import lombok.*;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class PermissionDTO {

    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;           // ex: patient:read, patient:delete, appointment:create

    private String description;
    private boolean active = true;
}
