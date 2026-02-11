package DoctorApp.DoctorApp.web;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import DoctorApp.DoctorApp.Entity.Patient;
import DoctorApp.DoctorApp.repository.PatientRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@AllArgsConstructor
public class PatientController {

    PatientRepository patientRepository;



    @GetMapping("/index")
    public String patient(Model model,
                          @RequestParam(name = "page", defaultValue = "0") int page,
                          @RequestParam(name = "size", defaultValue = "5") int size,
                          @RequestParam(name = "keyword", defaultValue = "") String keyword) {

        // LOGS POUR DÉBOGAGE
        System.out.println("\n========== /patient CONTROLLER ==========");

        // Vérifie l'authentification
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("1. User: " + auth.getName());
        System.out.println("2. Is authenticated: " + auth.isAuthenticated());
        System.out.println("3. Authorities: " + auth.getAuthorities());

        // Vérifie le repository
        System.out.println("4. Executing repository query...");
        Page<Patient> pagePatients = patientRepository.findByNomContainsIgnoreCaseOrPrenomContainsIgnoreCase(
                keyword, keyword, PageRequest.of(page, size));
        System.out.println("5. Found " + pagePatients.getTotalElements() + " patients");

        // Ajoute au modèle
        model.addAttribute("listepatient", pagePatients.getContent());
        model.addAttribute("patient", new Patient());
        model.addAttribute("pages", new int[pagePatients.getTotalPages()]);
        model.addAttribute("currentPage", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("size", size);

        System.out.println("6. Returning template name: 'patients'");
        System.out.println("=====================================\n");

        return "index";
    }

    @PostMapping("/admin/addPatient")
    public String savePatient(Model model, @Valid Patient patient, BindingResult bindingResult,
                              @RequestParam(name = "page", defaultValue = "0") int page,
                              @RequestParam(name = "size", defaultValue = "5") int size,
                              @RequestParam(name = "keyword", defaultValue = "") String keyword) {
        if (bindingResult.hasErrors()) {
            Page<Patient> pagePatients = patientRepository.findByNomContainsIgnoreCaseOrPrenomContainsIgnoreCase(keyword, keyword, PageRequest.of(page, size));
            model.addAttribute("listepatient", pagePatients.getContent());
            model.addAttribute("pages", new int[pagePatients.getTotalPages()]);
            model.addAttribute("currentPage", page);
            return "patients";
        }
        patientRepository.save(patient);
        return "redirect:/index?page=" + page + "&size=" + size + "&keyword=" + keyword;
    }


    @GetMapping("/admin/deletePatient")
    public String deletePatients(@RequestParam(name = "id") Long id){
            patientRepository.deleteById(id);
            return "redirect:/index";
}

}
