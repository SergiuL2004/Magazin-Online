package controller;

import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.UserService;

import javax.servlet.http.HttpSession;
import java.util.concurrent.ExecutionException;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("title", "Autentificare");
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("email") String email, 
                        @ModelAttribute("password") String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            // Use the authenticateUser method from UserService
            User user = userService.authenticateUser(email, password);

            if (user == null) {
                redirectAttributes.addFlashAttribute("error", 
                    "Email sau parolă incorecte sau cont dezactivat.");
                return "redirect:/login";
            }

            // Authentication successful, set user in session
            session.setAttribute("currentUser", user);

            // Redirect based on role
            switch (user.getRole()) {
                case ADMIN:
                    return "redirect:/admin/dashboard";
                case SELLER:
                    if (user.isApproved()) {
                        return "redirect:/seller/dashboard";
                    } else {
                        redirectAttributes.addFlashAttribute("error", 
                            "Contul dvs. de vânzător este în așteptare pentru aprobare.");
                        return "redirect:/login";
                    }
                case BUYER:
                    return "redirect:/buyer/dashboard";
                default:
                    return "redirect:/";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "A apărut o eroare la autentificare: " + e.getMessage());
            e.printStackTrace(); // Log the error for debugging
            return "redirect:/login";
        }
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("title", "Înregistrare");
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user,
                          RedirectAttributes redirectAttributes) {
        try {
            // Set default values
            boolean approved = user.getRole() != User.Role.SELLER;
            user.setApproved(approved);
            user.setActive(true);

            userService.registerUser(user);

            if (user.getRole() == User.Role.SELLER) {
                redirectAttributes.addFlashAttribute("success", 
                    "Înregistrare reușită! Contul dvs. de vânzător va fi verificat de administrator.");
            } else {
                redirectAttributes.addFlashAttribute("success", 
                    "Înregistrare reușită! Vă puteți autentifica acum.");
            }
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", 
                "A apărut o eroare la înregistrare. Vă rugăm să încercați din nou.");
            return "redirect:/register";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
