package service;

import App.FirebaseInitializer;
import com.google.cloud.firestore.*;
import model.SaleHistory;
import model.Product;
import model.Offer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class SaleHistoryService {
    private final Firestore db = FirebaseInitializer.initialize();

    @Autowired
    private ProductService productService;

    @Autowired
    private OfferService offerService;

    public SaleHistoryService() throws Exception {
    }

    public SaleHistoryService(ProductService productService, OfferService offerService) throws Exception {
        this.productService = productService;
        this.offerService = offerService;
    }

    public void recordSale(String productId, String buyerEmail, double price) throws ExecutionException, InterruptedException {
        // Get the product to get the seller email
        Product product = productService.getProductById(productId);
        if (product == null) return;

        String sellerEmail = product.getSellerEmail();

        // Create and save the sale record
        SaleHistory saleHistory = new SaleHistory(productId, buyerEmail, sellerEmail, price);
        db.collection("sales").add(saleHistory).get();

        // Delete the product and any associated offers
        productService.deleteProduct(productId);
        offerService.deleteOffersByProductId(productId);
    }

    public List<SaleHistory> getAllSales() throws ExecutionException, InterruptedException {
        return db.collection("sales").get().get().getDocuments().stream()
                .map(doc -> doc.toObject(SaleHistory.class))
                .collect(Collectors.toList());
    }

    public List<SaleHistory> getSalesByBuyer(String buyerEmail) throws ExecutionException, InterruptedException {
        return db.collection("sales")
                .whereEqualTo("buyerEmail", buyerEmail)
                .get().get().getDocuments().stream()
                .map(doc -> doc.toObject(SaleHistory.class))
                .collect(Collectors.toList());
    }

    public List<SaleHistory> getSalesBySeller(String sellerEmail) throws ExecutionException, InterruptedException {
        return db.collection("sales")
                .whereEqualTo("sellerEmail", sellerEmail)
                .get().get().getDocuments().stream()
                .map(doc -> doc.toObject(SaleHistory.class))
                .collect(Collectors.toList());
    }
}
