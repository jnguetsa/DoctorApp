package DoctorApp.DoctorApp.DTO;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

public class PatientDTO {



        private  Long id;
        @NotNull
        @Size(min = 2, max = 20)
        private  String nom;
        @NotNull @Size(min = 2, max = 20)
        private  String prenom;
        @Temporal(TemporalType.DATE)
        @DateTimeFormat(pattern = "yyyy-MM-dd")
        private Date dateNaissance;
        @Min(0)
        @Max(10)
        private int score;
        private  boolean malade;


}
