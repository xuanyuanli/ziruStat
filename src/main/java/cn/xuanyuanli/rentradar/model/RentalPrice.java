package cn.xuanyuanli.rentradar.model;

/**
 * @author xuanyuanli
 */
@SuppressWarnings("unused")
public class RentalPrice {
    private double price;
    private double area;
    private double pricePerSquareMeter;

    public RentalPrice() {
    }

    public RentalPrice(double price, double area) {
        this.price = price;
        this.area = area;
        this.pricePerSquareMeter = area > 0 ? price / area : 0;
    }

    // Getters and Setters
    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
        recalculatePricePerMeter();
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
        recalculatePricePerMeter();
    }

    public double getPricePerSquareMeter() {
        return pricePerSquareMeter;
    }

    public void setPricePerSquareMeter(double pricePerSquareMeter) {
        this.pricePerSquareMeter = pricePerSquareMeter;
    }

    private void recalculatePricePerMeter() {
        if (area > 0) {
            this.pricePerSquareMeter = price / area;
        }
    }

    public boolean isValid() {
        return price > 0 && area > 0 && pricePerSquareMeter > 0;
    }

    @Override
    public String toString() {
        return "RentalPrice{" +
                "price=" + price +
                ", area=" + area +
                ", pricePerSquareMeter=" + pricePerSquareMeter +
                '}';
    }
}