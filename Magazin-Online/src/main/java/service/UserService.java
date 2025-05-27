package service;

import App.FirebaseInitializer;
import com.google.cloud.firestore.*;
import model.User;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final Firestore db = FirebaseInitializer.initialize();

    public UserService() throws Exception {
        // Check if admin exists, if not create it
        User admin = getUser("admin@email.com");
        if (admin == null) {
            User adminUser = new User("admin@email.com", "admin", User.Role.ADMIN, true, true);
            registerUser(adminUser);
        }
    }

    public void registerUser(User user) throws ExecutionException, InterruptedException {
        db.collection("users").document(user.getEmail()).set(user).get();
    }

    public User getUser(String email) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = db.collection("users").document(email).get().get();
        return snapshot.exists() ? snapshot.toObject(User.class) : null;
    }

    public User authenticateUser(String email, String password) throws ExecutionException, InterruptedException {
        User user = getUser(email);
        if (user != null && user.getPassword().equals(password) && user.isActive()) {
            return user;
        }
        return null;
    }

    public void approveSeller(String email) throws ExecutionException, InterruptedException {
        db.collection("users").document(email).update("approved", true).get();
    }

    public void deactivateUser(String email) throws ExecutionException, InterruptedException {
        db.collection("users").document(email).update("active", false).get();
    }

    public List<User> getPendingSellers() throws ExecutionException, InterruptedException {
        return db.collection("users")
                .whereEqualTo("role", User.Role.SELLER)
                .whereEqualTo("approved", false)
                .whereEqualTo("active", true)
                .get().get().getDocuments().stream()
                .map(doc -> doc.toObject(User.class))
                .collect(Collectors.toList());
    }

    public List<User> getAllSellers() throws ExecutionException, InterruptedException {
        return db.collection("users")
                .whereEqualTo("role", User.Role.SELLER)
                .get().get().getDocuments().stream()
                .map(doc -> doc.toObject(User.class))
                .collect(Collectors.toList());
    }
}
