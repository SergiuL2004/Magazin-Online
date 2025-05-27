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
import java.util.concurrent.ExecutionException;

@Controller
@RequestMapping("/buyer")
public class BuyerController {

    @Autowired
    private ProductService productService;

    @Autowired
    private OfferService offerService;

    @Autowired
    private SaleHistoryService saleHistoryService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != User.Role.BUYER) {
            return "redirect:/login";
        }

        model.addAttribute("title", "Buyer Dashboard");
        model.addAttribute("currentUser", currentUser);

        try {
            model.addAttribute("allProducts", productService.getAllProducts());
            model.addAttribute("myPurchases", saleHistoryService.getSalesByBuyer(currentUser.getEmail()));

            // Load the buyer's offers to show notifications about their status
            model.addAttribute("myOffers", offerService.getOffersByBuyer(currentUser.getEmail()));
        } catch (Exception e) {
            model.addAttribute("error", "A apărut o eroare la încărcarea datelor.");
        }

        return "buyer/dashboard";
    }

    @GetMapping("/buy-product/{id}")
    public String buyProduct(@PathVariable String id, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != User.Role.BUYER) {
            return "redirect:/login";
        }

        try {
            Product product = productService.getProductById(id);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Produsul nu există.");
                return "redirect:/buyer/dashboard";
            }

            if (product.isNegotiable()) {
                redirectAttributes.addFlashAttribute("error", 
                    "Acest produs este negociabil. Folosiți opțiunea 'Faceți o ofertă'.");
                return "redirect:/buyer/dashboard";
            }

            saleHistoryService.recordSale(id, currentUser.getEmail(), product.getPrice());
            redirectAttributes.addFlashAttribute("success", "Produsul a fost cumpărat cu succes.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la cumpărarea produsului.");
        }

        return "redirect:/buyer/dashboard";
    }

    @GetMapping("/make-offer/{id}")
    public String makeOfferForm(@PathVariable String id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != User.Role.BUYER) {
            return "redirect:/login";
        }

        try {
            Product product = productService.getProductById(id);
            if (product == null) {
                return "redirect:/buyer/dashboard";
            }

            if (!product.isNegotiable()) {
                return "redirect:/buyer/dashboard";
            }

            model.addAttribute("title", "Faceți o ofertă");
            model.addAttribute("product", product);
            model.addAttribute("offer", new Offer(id, currentUser.getEmail(), 0));
            model.addAttribute("currentUser", currentUser);
        } catch (Exception e) {
            return "redirect:/buyer/dashboard";
        }

        return "buyer/make-offer";
    }

    @PostMapping("/make-offer")
    public String makeOffer(@ModelAttribute Offer offer, HttpSession session, RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != User.Role.BUYER) {
            return "redirect:/login";
        }

        try {
            offer.setBuyerEmail(currentUser.getEmail());
            boolean accepted = offerService.makeOffer(offer);

            if (accepted) {
                redirectAttributes.addFlashAttribute("success", 
                    "Oferta dvs. a fost înregistrată. Așteptați aprobarea vânzătorului.");
            } else {
                redirectAttributes.addFlashAttribute("error", 
                    "Oferta dvs. a fost respinsă automat. Prețul oferit este prea mic.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la înregistrarea ofertei.");
        }

        return "redirect:/buyer/dashboard";
    }

    @GetMapping("/enter-courier-data/{id}")
    public String enterCourierDataForm(@PathVariable String id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != User.Role.BUYER) {
            return "redirect:/login";
        }

        try {
            Offer offer = offerService.getOfferById(id);
            if (offer == null || !offer.getBuyerEmail().equals(currentUser.getEmail())) {
                return "redirect:/buyer/dashboard";
            }

            if (offer.getStatus() != Offer.Status.APPROVED) {
                return "redirect:/buyer/dashboard";
            }

            model.addAttribute("title", "Introduceți datele pentru curier");
            model.addAttribute("offer", offer);
            model.addAttribute("currentUser", currentUser);

            // Get the product details to display
            Product product = productService.getProductById(offer.getProductId());
            model.addAttribute("product", product);

        } catch (Exception e) {
            return "redirect:/buyer/dashboard";
        }

        return "buyer/enter-courier-data";
    }

    @PostMapping("/enter-courier-data/")
    public String enterCourierData(@RequestParam("offerId") String offerId,
                                  @RequestParam("courierName") String courierName,
                                  @RequestParam("trackingNumber") String trackingNumber,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != User.Role.BUYER) {
            return "redirect:/login";
        }

        try {
            Offer offer = offerService.getOfferById(offerId);
            if (offer == null || !offer.getBuyerEmail().equals(currentUser.getEmail())) {
                redirectAttributes.addFlashAttribute("error", "Oferta nu există sau nu vă aparține.");
                return "redirect:/buyer/dashboard";
            }

            if (offer.getStatus() != Offer.Status.APPROVED) {
                redirectAttributes.addFlashAttribute("error", "Oferta nu este aprobată.");
                return "redirect:/buyer/dashboard";
            }

            offerService.updateCourierData(offerId, courierName, trackingNumber);
            redirectAttributes.addFlashAttribute("success", "Datele pentru curier au fost introduse cu succes. Comanda este pe drum!");

        } catch (Exception e) {
            e.printStackTrace(); // Log the exception for debugging
            redirectAttributes.addFlashAttribute("error", "A apărut o eroare la introducerea datelor pentru curier: " + e.getMessage());
        }

        return "redirect:/buyer/dashboard";
    }
}
