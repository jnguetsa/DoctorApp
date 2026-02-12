package DoctorApp.DoctorApp.Service;


import DoctorApp.DoctorApp.DTO.Auth.LoginRequestDTO;
import DoctorApp.DoctorApp.DTO.Auth.LoginResponseDTO;
import DoctorApp.DoctorApp.DTO.Auth.RegisterRequestDTO;
import DoctorApp.DoctorApp.DTO.Utilisateur.UtilisateurResponseDTO;

public interface IAuthenticationService {

    LoginResponseDTO login(LoginRequestDTO request);

    UtilisateurResponseDTO register(RegisterRequestDTO request);

    UtilisateurResponseDTO getCurrentUser(String email);
}