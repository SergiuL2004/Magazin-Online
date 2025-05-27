package service;

import App.FirebaseInitializer;

import com.google.cloud.firestore.*;
import model.Offer;
import model.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class OfferService {
    private final Firestore db = FirebaseInitializer.initialize();

    @Autowired
    private ProductService productService;

    public OfferService() throws Exception {
    }

    public OfferService(ProductService productService) throws Exception {
        this.productService = productService;
    }

    public boolean makeOffer(Offer offer) throws ExecutionException, InterruptedException {
        DocumentSnapshot productSnap = db.collection("products").document(offer.getProductId()).get().get();
        if (!productSnap.exists()) return false;

        Product product = productSnap.toObject(Product.class);
        if (!product.isNegotiable() || offer.getOfferedPrice() < product.getMinPrice()) return false;

        db.collection("offers").add(offer).get();
        return true;
    }

    public List<Offer> getOffersByProductId(String productId) throws ExecutionException, InterruptedException {
        return db.collection("offers")
                .whereEqualTo("productId", productId)
                .get().get().getDocuments().stream()
                .map(doc -> {
                    Offer offer = doc.toObject(Offer.class);
                    offer.setId(doc.getId()); // Set the document ID as the offer ID
                    return offer;
                })
                .collect(Collectors.toList());
    }

    public List<Offer> getOffersBySeller(String sellerEmail) throws ExecutionException, InterruptedException {
        // Get all products by this seller
        List<Product> sellerProducts = productService.getProductsBySeller(sellerEmail);

        // Get all offers for these products
        return sellerProducts.stream()
                .flatMap(product -> {
                    try {
                        return getOffersByProductId(product.getId()).stream();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .collect(Collectors.toList());
    }

    public List<Offer> getOffersByBuyer(String buyerEmail) throws ExecutionException, InterruptedException {
        return db.collection("offers")
                .whereEqualTo("buyerEmail", buyerEmail)
                .get().get().getDocuments().stream()
                .map(doc -> {
                    Offer offer = doc.toObject(Offer.class);
                    offer.setId(doc.getId()); // Set the document ID as the offer ID
                    return offer;
                })
                .collect(Collectors.toList());
    }

    public void approveOffer(String offerId, SaleHistoryService saleHistoryService) throws ExecutionException, InterruptedException {
        // Get the offer
        DocumentSnapshot offerSnap = db.collection("offers").document(offerId).get().get();
        if (!offerSnap.exists()) return;

        Offer offer = offerSnap.toObject(Offer.class);

        // Update the offer status to APPROVED
        offer.setStatus(Offer.Status.APPROVED);
        db.collection("offers").document(offerId).update("status", Offer.Status.APPROVED).get();

        // Record the sale
        saleHistoryService.recordSale(offer.getProductId(), offer.getBuyerEmail(), offer.getOfferedPrice());
    }

    public void rejectOffer(String offerId) throws ExecutionException, InterruptedException {
        // Update the offer status to REJECTED
        db.collection("offers").document(offerId).update("status", Offer.Status.REJECTED).get();
    }

    public void updateCourierData(String offerId, String courierName, String trackingNumber) throws ExecutionException, InterruptedException {
        db.collection("offers").document(offerId).update(
                "courierName", courierName,
                "trackingNumber", trackingNumber,
                "courierDataEntered", true
        ).get();
    }

    public Offer getOfferById(String offerId) throws ExecutionException, InterruptedException {
        DocumentSnapshot offerSnap = db.collection("offers").document(offerId).get().get();
        if (!offerSnap.exists()) return null;

        Offer offer = offerSnap.toObject(Offer.class);
        offer.setId(offerSnap.getId());
        return offer;
    }

    public void deleteOffersByProductId(String productId) throws ExecutionException, InterruptedException {
        db.collection("offers").whereEqualTo("productId", productId).get().get().getDocuments()
                .forEach(doc -> doc.getReference().delete());
    }
}
