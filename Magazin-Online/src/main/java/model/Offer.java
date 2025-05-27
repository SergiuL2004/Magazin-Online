package model;

public class Offer {
    public enum Status { PENDING, APPROVED, REJECTED }

    private String id;
    private String productId;
    private String buyerEmail;
    private double offeredPrice;
    private Status status;
    private String courierName;
    private String trackingNumber;
    private boolean courierDataEntered;

    public Offer() {
        this.status = Status.PENDING;
        this.courierDataEntered = false;
    }

    public Offer(String productId, String buyerEmail, double offeredPrice) {
        this.productId = productId;
        this.buyerEmail = buyerEmail;
        this.offeredPrice = offeredPrice;
        this.status = Status.PENDING;
        this.courierDataEntered = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }

    public double getOfferedPrice() { return offeredPrice; }
    public void setOfferedPrice(double offeredPrice) { this.offeredPrice = offeredPrice; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getCourierName() { return courierName; }
    public void setCourierName(String courierName) { this.courierName = courierName; }

    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }

    public boolean isCourierDataEntered() { return courierDataEntered; }
    public void setCourierDataEntered(boolean courierDataEntered) { this.courierDataEntered = courierDataEntered; }
}
