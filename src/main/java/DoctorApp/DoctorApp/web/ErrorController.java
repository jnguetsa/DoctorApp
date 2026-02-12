package DoctorApp.DoctorApp.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/notFound")
    public String notFound() {
        return "notfound";   // → templates/notfound.html
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";   // une autre page si tu veux différencier 403 et 404
    }
}