package App;

import model.*;
import service.*;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Main {
    private static Scanner scanner = new Scanner(System.in);
    private static UserService userService;
    private static ProductService productService;
    private static OfferService offerService;
    private static SaleHistoryService saleHistoryService;
    private static User currentUser = null;

    public static void main(String[] args) throws Exception {
        userService = new UserService();
        productService = new ProductService();
        offerService = new OfferService(productService);
        saleHistoryService = new SaleHistoryService(productService, offerService);

        while (true) {
            if (currentUser == null) {
                showLoginMenu();
            } else {
                switch (currentUser.getRole()) {
                    case ADMIN -> showAdminMenu();
                    case SELLER -> {
                        if (currentUser.isApproved()) {
                            showSellerMenu();
                        } else {
                            System.out.println("Contul dvs. de vanzator este in asteptare pentru aprobare.");
                            currentUser = null;
                        }
                    }
                    case BUYER -> showBuyerMenu();
                }
            }
        }
    }

    private static void showLoginMenu() throws Exception {
        System.out.println("\n=== MAGAZIN ONLINE ===");
        System.out.println("1. Autentificare");
        System.out.println("2. Inregistrare");
        System.out.println("0. Iesire");
        System.out.print("Alege optiunea: ");
        int opt = Integer.parseInt(scanner.nextLine());

        switch (opt) {
            case 1 -> login();
            case 2 -> register();
            case 0 -> System.exit(0);
            default -> System.out.println("Optiune invalida.");
        }
    }

    private static void login() throws ExecutionException, InterruptedException {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Parola: ");
        String password = scanner.nextLine();

        User user = userService.authenticateUser(email, password);
        if (user != null) {
            currentUser = user;
            System.out.println("Autentificare reusita!");
        } else {
            System.out.println("Email sau parola incorecte sau cont dezactivat.");
        }
    }

    private static void register() throws ExecutionException, InterruptedException {
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Parola: ");
        String password = scanner.nextLine();
        System.out.print("Rol (SELLER, BUYER): ");
        String roleStr = scanner.nextLine();

        if (!roleStr.equals("SELLER") && !roleStr.equals("BUYER")) {
            System.out.println("Rol invalid. Trebuie sa fie SELLER sau BUYER.");
            return;
        }

        User.Role role = User.Role.valueOf(roleStr);
        boolean approved = role != User.Role.SELLER; // Sellers need approval

        User user = new User(email, password, role, approved, true);
        userService.registerUser(user);
        System.out.println("Inregistrare reusita!");

        if (role == User.Role.SELLER) {
            System.out.println("Contul dvs. de vanzator va fi verificat de administrator.");
        }
    }

    private static void showAdminMenu() throws Exception {
        System.out.println("\n=== ADMIN MENU ===");
        System.out.println("1. Verifica vanzatori in asteptare");
        System.out.println("2. Aproba vanzator");
        System.out.println("3. Dezactiveaza vanzator");
        System.out.println("4. Afiseaza toti vanzatorii");
        System.out.println("5. Afiseaza toate produsele");
        System.out.println("6. Afiseaza istoricul vanzarilor");
        System.out.println("9. Deconectare");
        System.out.println("0. Iesire");
        System.out.print("Alege optiunea: ");
        int opt = Integer.parseInt(scanner.nextLine());

        switch (opt) {
            case 1 -> showPendingSellers();
            case 2 -> approveSeller();
            case 3 -> deactivateSeller();
            case 4 -> showAllSellers();
            case 5 -> showAllProducts();
            case 6 -> showSalesHistory();
            case 9 -> currentUser = null;
            case 0 -> System.exit(0);
            default -> System.out.println("Optiune invalida.");
        }
    }

    private static void showPendingSellers() throws ExecutionException, InterruptedException {
        List<User> pendingSellers = userService.getPendingSellers();
        if (pendingSellers.isEmpty()) {
            System.out.println("Nu exista vanzatori in asteptare.");
            return;
        }

        System.out.println("Vanzatori in asteptare:");
        for (User seller : pendingSellers) {
            System.out.println("Email: " + seller.getEmail());
        }
    }

    private static void approveSeller() throws ExecutionException, InterruptedException {
        System.out.print("Email vanzator: ");
        String email = scanner.nextLine();

        User seller = userService.getUser(email);
        if (seller == null || seller.getRole() != User.Role.SELLER) {
            System.out.println("Vanzator inexistent.");
            return;
        }

        userService.approveSeller(email);
        System.out.println("Vanzator aprobat.");
    }

    private static void deactivateSeller() throws ExecutionException, InterruptedException {
        System.out.print("Email vanzator: ");
        String email = scanner.nextLine();

        User seller = userService.getUser(email);
        if (seller == null || seller.getRole() != User.Role.SELLER) {
            System.out.println("Vanzator inexistent.");
            return;
        }

        userService.deactivateUser(email);
        System.out.println("Vanzator dezactivat.");
    }

    private static void showAllSellers() throws ExecutionException, InterruptedException {
        List<User> sellers = userService.getAllSellers();
        if (sellers.isEmpty()) {
            System.out.println("Nu exista vanzatori.");
            return;
        }

        System.out.println("Lista vanzatori:");
        for (User seller : sellers) {
            System.out.printf("Email: %s, Aprobat: %s, Activ: %s\n", 
                    seller.getEmail(), seller.isApproved(), seller.isActive());
        }
    }

    private static void showSellerMenu() throws Exception {
        System.out.println("\n=== SELLER MENU ===");
        System.out.println("1. Adauga produs");
        System.out.println("2. Afiseaza produsele mele");
        System.out.println("3. Sterge produs");
        System.out.println("4. Afiseaza oferte pentru produsele mele");
        System.out.println("5. Aproba oferta");
        System.out.println("6. Respinge oferta");
        System.out.println("7. Afiseaza istoricul vanzarilor mele");
        System.out.println("9. Deconectare");
        System.out.println("0. Iesire");
        System.out.print("Alege optiunea: ");
        int opt = Integer.parseInt(scanner.nextLine());

        switch (opt) {
            case 1 -> addProduct();
            case 2 -> showMyProducts();
            case 3 -> deleteProduct();
            case 4 -> showMyOffers();
            case 5 -> approveOffer();
            case 6 -> rejectOffer();
            case 7 -> showMySalesHistory();
            case 9 -> currentUser = null;
            case 0 -> System.exit(0);
            default -> System.out.println("Optiune invalida.");
        }
    }

    private static void addProduct() throws ExecutionException, InterruptedException {
        String id = UUID.randomUUID().toString();
        System.out.print("Nume produs: ");
        String name = scanner.nextLine();
        System.out.print("Descriere: ");
        String desc = scanner.nextLine();
        System.out.print("Pret: ");
        double price = Double.parseDouble(scanner.nextLine());
        System.out.print("Negociabil (true/false): ");
        boolean negotiable = Boolean.parseBoolean(scanner.nextLine());
        double minPrice = 0;
        if (negotiable) {
            System.out.print("Pret minim: ");
            minPrice = Double.parseDouble(scanner.nextLine());
        }

        Product product = new Product(id, name, desc, price, currentUser.getEmail(), negotiable, minPrice);
        productService.addProduct(product);
        System.out.println("Produs adaugat cu succes.");
    }

    private static void showMyProducts() throws ExecutionException, InterruptedException {
        List<Product> products = productService.getProductsBySeller(currentUser.getEmail());
        if (products.isEmpty()) {
            System.out.println("Nu aveti produse.");
            return;
        }

        System.out.println("Produsele mele:");
        for (Product p : products) {
            System.out.printf("ID: %s, Nume: %s, Pret: %.2f, Descriere: %s, Negociabil: %s\n", 
                    p.getId(), p.getName(), p.getPrice(), p.getDescription(), p.isNegotiable());
            if (p.isNegotiable()) {
                System.out.printf("Pret minim: %.2f\n", p.getMinPrice());
            }
        }
    }

    private static void deleteProduct() throws ExecutionException, InterruptedException {
        System.out.print("ID produs: ");
        String id = scanner.nextLine();

        Product product = productService.getProductById(id);
        if (product == null || !product.getSellerEmail().equals(currentUser.getEmail())) {
            System.out.println("Produs inexistent sau nu va apartine.");
            return;
        }

        productService.deleteProduct(id);
        offerService.deleteOffersByProductId(id);
        System.out.println("Produs sters cu succes.");
    }

    private static void showMyOffers() throws ExecutionException, InterruptedException {
        List<Offer> offers = offerService.getOffersBySeller(currentUser.getEmail());
        if (offers.isEmpty()) {
            System.out.println("Nu aveti oferte.");
            return;
        }

        System.out.println("Oferte pentru produsele mele:");
        for (Offer o : offers) {
            Product product = productService.getProductById(o.getProductId());
            if (product != null) {
                System.out.printf("ID Oferta: %s, Produs: %s, Cumparator: %s, Pret oferit: %.2f\n", 
                        o.getProductId(), product.getName(), o.getBuyerEmail(), o.getOfferedPrice());
            }
        }
    }

    private static void approveOffer() throws ExecutionException, InterruptedException {
        System.out.print("ID oferta: ");
        String id = scanner.nextLine();

        // In a real system, we would need to check if the offer belongs to this seller
        offerService.approveOffer(id, saleHistoryService);
        System.out.println("Oferta aprobata. Produsul a fost vandut.");
    }

    private static void rejectOffer() throws ExecutionException, InterruptedException {
        System.out.print("ID oferta: ");
        String id = scanner.nextLine();

        // In a real system, we would need to check if the offer belongs to this seller
        offerService.rejectOffer(id);
        System.out.println("Oferta respinsa.");
    }

    private static void showMySalesHistory() throws ExecutionException, InterruptedException {
        List<SaleHistory> sales = saleHistoryService.getSalesBySeller(currentUser.getEmail());
        if (sales.isEmpty()) {
            System.out.println("Nu aveti vanzari.");
            return;
        }

        System.out.println("Istoricul vanzarilor mele:");
        for (SaleHistory s : sales) {
            System.out.printf("Produs ID: %s, Cumparator: %s, Pret vandut: %.2f\n", 
                    s.getProductId(), s.getBuyerEmail(), s.getSoldPrice());
        }
    }

    private static void showBuyerMenu() throws Exception {
        System.out.println("\n=== BUYER MENU ===");
        System.out.println("1. Afiseaza toate produsele");
        System.out.println("2. Cumpara produs");
        System.out.println("3. Ofera pret pentru produs negociabil");
        System.out.println("4. Afiseaza istoricul cumparaturilor mele");
        System.out.println("9. Deconectare");
        System.out.println("0. Iesire");
        System.out.print("Alege optiunea: ");
        int opt = Integer.parseInt(scanner.nextLine());

        switch (opt) {
            case 1 -> showAllProducts();
            case 2 -> buyProduct();
            case 3 -> makeOffer();
            case 4 -> showMyPurchaseHistory();
            case 9 -> currentUser = null;
            case 0 -> System.exit(0);
            default -> System.out.println("Optiune invalida.");
        }
    }

    private static void showAllProducts() throws ExecutionException, InterruptedException {
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("Nu exista produse.");
            return;
        }

        System.out.println("Lista produse:");
        for (Product p : products) {
            System.out.printf("ID: %s, Nume: %s, Pret: %.2f, Descriere: %s, Vanzator: %s, Negociabil: %s\n", 
                    p.getId(), p.getName(), p.getPrice(), p.getDescription(), p.getSellerEmail(), p.isNegotiable());
        }
    }

    private static void buyProduct() throws ExecutionException, InterruptedException {
        System.out.print("ID produs: ");
        String id = scanner.nextLine();

        Product product = productService.getProductById(id);
        if (product == null) {
            System.out.println("Produs inexistent.");
            return;
        }

        if (product.isNegotiable()) {
            System.out.println("Acest produs este negociabil. Folositi optiunea 'Ofera pret pentru produs negociabil'.");
            return;
        }

        saleHistoryService.recordSale(id, currentUser.getEmail(), product.getPrice());
        System.out.println("Produs cumparat cu succes.");
    }

    private static void makeOffer() throws ExecutionException, InterruptedException {
        System.out.print("ID produs: ");
        String id = scanner.nextLine();

        Product product = productService.getProductById(id);
        if (product == null) {
            System.out.println("Produs inexistent.");
            return;
        }

        if (!product.isNegotiable()) {
            System.out.println("Acest produs nu este negociabil. Folositi optiunea 'Cumpara produs'.");
            return;
        }

        System.out.print("Pret oferit: ");
        double price = Double.parseDouble(scanner.nextLine());

        boolean accepted = offerService.makeOffer(new Offer(id, currentUser.getEmail(), price));
        if (accepted) {
            System.out.println("Oferta inregistrata. Asteptati aprobarea vanzatorului.");
        } else {
            System.out.println("Oferta respinsa automat. Pretul oferit este prea mic.");
        }
    }

    private static void showMyPurchaseHistory() throws ExecutionException, InterruptedException {
        List<SaleHistory> purchases = saleHistoryService.getSalesByBuyer(currentUser.getEmail());
        if (purchases.isEmpty()) {
            System.out.println("Nu aveti cumparaturi.");
            return;
        }

        System.out.println("Istoricul cumparaturilor mele:");
        for (SaleHistory s : purchases) {
            System.out.printf("Produs ID: %s, Pret platit: %.2f\n", 
                    s.getProductId(), s.getSoldPrice());
        }
    }

    private static void showSalesHistory() throws ExecutionException, InterruptedException {
        List<SaleHistory> sales = saleHistoryService.getAllSales();
        if (sales.isEmpty()) {
            System.out.println("Nu exista vanzari.");
            return;
        }

        System.out.println("Istoricul vanzarilor:");
        for (SaleHistory s : sales) {
            System.out.printf("Produs ID: %s, Cumparator: %s, Pret vandut: %.2f\n", 
                    s.getProductId(), s.getBuyerEmail(), s.getSoldPrice());
        }
    }
}
