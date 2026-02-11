package DoctorApp.DoctorApp;

import DoctorApp.DoctorApp.Entity.Patient;
import DoctorApp.DoctorApp.repository.PatientRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.stream.Stream;

@SpringBootApplication
//@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})

public class DoctorAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoctorAppApplication.class, args);
	}

	//@Bean
	CommandLineRunner start(PatientRepository patientRepository) {
		return args -> {
			Stream.of(
					Patient.builder().nom("junior").prenom("Doe").dateNaissance(new Date()).score(1).malade(true).build(),
					Patient.builder().nom("Lea").prenom("Smith").dateNaissance(new Date()).score(3).malade(true).build(),
					Patient.builder().nom("Franck").prenom("Johnson").dateNaissance(new Date()).score(4).malade(false).build(),
					Patient.builder().nom("Vicky").prenom("Brown").dateNaissance(new Date()).score(0).malade(false).build(),
					Patient.builder().nom("Noe").prenom("Davis").dateNaissance(new Date()).score(9).malade(true).build(),
					Patient.builder().nom("Chloe").prenom("Miller").dateNaissance(new Date()).score(5).malade(false).build(),
					Patient.builder().nom("Josue").prenom("Wilson").dateNaissance(new Date()).score(7).malade(true).build(),
					Patient.builder().nom("Christian").prenom("Moore").dateNaissance(new Date()).score(0).malade(true).build(),
					Patient.builder().nom("Marc").prenom("Taylor").dateNaissance(new Date()).score(2).malade(false).build(),
					Patient.builder().nom("Lionel").prenom("Anderson").dateNaissance(new Date()).score(9).malade(true).build()
			).forEach(patientRepository::save);
		};
	}

}
