package DoctorApp.DoctorApp.DTO;

import jakarta.persistence.Column;
import lombok.*;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class RoleDTO {
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;           // ex: ADMIN, DOCTOR, PATIENT

    private String description;
    private boolean active = true;
}
