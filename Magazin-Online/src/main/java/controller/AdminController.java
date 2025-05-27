package controller;

import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.OfferService;
import service.ProductService;
import service.SaleHistoryService;
import service.UserService;

import javax.servlet.http.HttpSession;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProductService productService;

    @Autowired
    private SaleHistoryService saleHistoryService;

    @Autowired
    private OfferService offerService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != User.Role.ADMIN) {
            return "redirect:/login";
        }

        model.addAttribute("title", "Admin Dashboard");
        model.addAttribute("currentUser", currentUser);

        try {
            model.addAttribute("pendingSellers", userService.getPendingSellers());
            model.addAttribute("allSellers", userService.getAllSellers());
            model.addAttribute("allProducts", productService.getAllProducts());
            model.addAttribute("salesHistory", saleHistoryService.getAllSales());
        } catch (Exception e) {
            model.addAttribute("error", "A apărut o eroare la încărcarea datelor.");
        }

        return "admin/dashboard";
    }

    @GetMapping("/approve-seller/{email}")
    public String approveSeller(@PathVariable String email, RedirectAttributes redirectAttributes) {
        try {
            userService.approveSeller(email);
            redirectAttributes.addFlashAttribute("success", "Vânzătorul a fost aprobat cu succes.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la aprobarea vânzătorului.");
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/deactivate-seller/{email}")
    public String deactivateSeller(@PathVariable String email, RedirectAttributes redirectAttributes) {
        try {
            userService.deactivateUser(email);
            redirectAttributes.addFlashAttribute("success", "Vânzătorul a fost dezactivat cu succes.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la dezactivarea vânzătorului.");
        }
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/delete-product/{id}")
    public String deleteProduct(@PathVariable String id, RedirectAttributes redirectAttributes) {
        try {
            // Delete any offers for this product first
            offerService.deleteOffersByProductId(id);

            // Then delete the product
            productService.deleteProduct(id);

            redirectAttributes.addFlashAttribute("success", "Produsul a fost șters cu succes.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la ștergerea produsului: " + e.getMessage());
        }
        return "redirect:/admin/dashboard";
    }
}
