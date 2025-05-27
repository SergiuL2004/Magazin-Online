package model;

public class SaleHistory {
    private String productId;
    private String buyerEmail;
    private String sellerEmail;
    private double soldPrice;

    public SaleHistory() {}

    public SaleHistory(String productId, String buyerEmail, String sellerEmail, double soldPrice) {
        this.productId = productId;
        this.buyerEmail = buyerEmail;
        this.sellerEmail = sellerEmail;
        this.soldPrice = soldPrice;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }

    public String getSellerEmail() { return sellerEmail; }
    public void setSellerEmail(String sellerEmail) { this.sellerEmail = sellerEmail; }

    public double getSoldPrice() { return soldPrice; }
    public void setSoldPrice(double soldPrice) { this.soldPrice = soldPrice; }
}
