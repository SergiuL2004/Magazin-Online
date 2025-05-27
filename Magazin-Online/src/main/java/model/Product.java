package model;

import java.util.ArrayList;
import java.util.List;

public class Product {
    private String id;
    private String name;
    private String description;
    private double price;
    private String sellerEmail;
    private boolean negotiable;
    private double minPrice;
    private List<String> imageUrls;

    public Product() {
        this.imageUrls = new ArrayList<>();
    }

    public Product(String id, String name, String description, double price, String sellerEmail, boolean negotiable, double minPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.sellerEmail = sellerEmail;
        this.negotiable = negotiable;
        this.minPrice = minPrice;
        this.imageUrls = new ArrayList<>();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }

    public boolean isNegotiable() { return negotiable; }
    public void setNegotiable(boolean negotiable) { this.negotiable = negotiable; }

    public double getMinPrice() { return minPrice; }
    public void setMinPrice(double minPrice) { this.minPrice = minPrice; }

    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }
}
