package DoctorApp.DoctorApp.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter @Setter  @Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Permission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String nom;           // ex: patient:read, patient:delete, appointment:create

  private String description;
  private boolean active = true;

  @ManyToMany
  private Set<Role> roles = new HashSet<>();

}
