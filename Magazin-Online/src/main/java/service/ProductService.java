package service;

import App.FirebaseInitializer;
import com.google.cloud.firestore.*;
import model.Product;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final Firestore db = FirebaseInitializer.initialize();

    public ProductService() throws Exception {}

    public void addProduct(Product product) throws ExecutionException, InterruptedException {
        db.collection("products").document(product.getId()).set(product).get();
    }

    public Product getProductById(String id) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = db.collection("products").document(id).get().get();
        return snapshot.exists() ? snapshot.toObject(Product.class) : null;
    }

    public List<Product> getAllProducts() throws ExecutionException, InterruptedException {
        return db.collection("products").get().get().getDocuments().stream()
                .map(doc -> doc.toObject(Product.class))
                .collect(Collectors.toList());
    }

    public List<Product> getProductsBySeller(String sellerEmail) throws ExecutionException, InterruptedException {
        return db.collection("products")
                .whereEqualTo("sellerEmail", sellerEmail)
                .get().get().getDocuments().stream()
                .map(doc -> doc.toObject(Product.class))
                .collect(Collectors.toList());
    }

    public void deleteProduct(String id) throws ExecutionException, InterruptedException {
        db.collection("products").document(id).delete().get();
    }
}
