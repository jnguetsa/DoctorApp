package DoctorApp.DoctorApp.web.Auths;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login"; // Renvoie vers le template Thymeleaf/HTML
    }
}
