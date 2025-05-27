package controller;

import model.Offer;
import model.Product;
import model.SaleHistory;
import model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.OfferService;
import service.ProductService;
import service.SaleHistoryService;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/seller")
public class SellerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OfferService offerService;

    @Autowired
    private SaleHistoryService saleHistoryService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != User.Role.SELLER || !currentUser.isApproved()) {
            return "redirect:/login";
        }

        model.addAttribute("title", "Seller Dashboard");
        model.addAttribute("currentUser", currentUser);

        try {
            model.addAttribute("myProducts", productService.getProductsBySeller(currentUser.getEmail()));
            model.addAttribute("myOffers", offerService.getOffersBySeller(currentUser.getEmail()));
            model.addAttribute("mySales", saleHistoryService.getSalesBySeller(currentUser.getEmail()));
        } catch (Exception e) {
            model.addAttribute("error", "A apărut o eroare la încărcarea datelor.");
        }

        return "seller/dashboard";
    }

    @GetMapping("/add-product")
    public String addProductForm(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != User.Role.SELLER || !currentUser.isApproved()) {
            return "redirect:/login";
        }

        model.addAttribute("title", "Adaugă Produs");
        model.addAttribute("product", new Product());
        model.addAttribute("currentUser", currentUser);

        return "seller/add-product";
    }

    @PostMapping("/add-product")
    public String addProduct(@ModelAttribute Product product, 
                            @RequestParam(value = "imageUrl1", required = true) String imageUrl1,
                            @RequestParam(value = "imageUrl2", required = false) String imageUrl2,
                            @RequestParam(value = "imageUrl3", required = false) String imageUrl3,
                            @RequestParam(value = "contactSeller", required = false, defaultValue = "false") boolean contactSeller,
                            HttpSession session, 
                            RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != User.Role.SELLER || !currentUser.isApproved()) {
            return "redirect:/login";
        }

        try {
            // If product is not negotiable, set minPrice to 0 to avoid conversion errors
            if (!product.isNegotiable()) {
                product.setMinPrice(0);
            }

            product.setId(UUID.randomUUID().toString());
            product.setSellerEmail(currentUser.getEmail());

            // Add image URLs to the product
            List<String> imageUrls = new ArrayList<>();
            if (imageUrl1 != null && !imageUrl1.trim().isEmpty()) {
                imageUrls.add(imageUrl1.trim());
            }
            if (imageUrl2 != null && !imageUrl2.trim().isEmpty()) {
                imageUrls.add(imageUrl2.trim());
            }
            if (imageUrl3 != null && !imageUrl3.trim().isEmpty()) {
                imageUrls.add(imageUrl3.trim());
            }
            product.setImageUrls(imageUrls);

            // Store the contactSeller preference in the session for this product
            if (contactSeller) {
                session.setAttribute("contactSeller_" + product.getId(), true);
            }

            productService.addProduct(product);
            redirectAttributes.addFlashAttribute("success", "Produsul a fost adăugat cu succes.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la adăugarea produsului: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/seller/dashboard";
    }

    @GetMapping("/delete-product/{id}")
    public String deleteProduct(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != User.Role.SELLER || !currentUser.isApproved()) {
            return "redirect:/login";
        }

        try {
            Product product = productService.getProductById(id);
            if (product == null || !product.getSellerEmail().equals(currentUser.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Produsul nu există sau nu vă aparține.");
                return "redirect:/seller/dashboard";
            }

            productService.deleteProduct(id);
            offerService.deleteOffersByProductId(id);
            redirectAttributes.addFlashAttribute("success", "Produsul a fost șters cu succes.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la ștergerea produsului.");
        }

        return "redirect:/seller/dashboard";
    }

    @GetMapping("/approve-offer/{id}")
    public String approveOffer(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != User.Role.SELLER || !currentUser.isApproved()) {
            return "redirect:/login";
        }

        try {
            offerService.approveOffer(id, saleHistoryService);
            redirectAttributes.addFlashAttribute("success", "Oferta a fost aprobată cu succes.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la aprobarea ofertei.");
        }

        return "redirect:/seller/dashboard";
    }

    @GetMapping("/reject-offer/{id}")
    public String rejectOffer(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != User.Role.SELLER || !currentUser.isApproved()) {
            return "redirect:/login";
        }

        try {
            offerService.rejectOffer(id);
            redirectAttributes.addFlashAttribute("success", "Oferta a fost respinsă cu succes.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la respingerea ofertei.");
        }

        return "redirect:/seller/dashboard";
    }
}
