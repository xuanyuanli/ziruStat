package cn.xuanyuanli.rentradar.model;

/**
 * 租房价格数据模型<br>
 * 封装房屋租赁价格相关信息，包括总价格、房屋面积和单位面积价格<br>
 * 提供自动计算每平方米价格的功能和数据有效性验证
 *
 * @author xuanyuanli
 */
@SuppressWarnings("unused")
public class RentalPrice {
    private double price;
    private double area;
    private double pricePerSquareMeter;

    public RentalPrice() {
    }

    /**
     * 构造函数<br>
     * 根据总价格和房屋面积创建RentalPrice实例，自动计算每平方米价格
     *
     * @param price 房屋总价格
     * @param area 房屋面积（平方米）
     */
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

    /**
     * 重新计算每平方米价格<br>
     * 当价格或面积发生变化时自动调用此方法重新计算单价
     */
    private void recalculatePricePerMeter() {
        if (area > 0) {
            this.pricePerSquareMeter = price / area;
        }
    }

    /**
     * 验证价格数据的有效性<br>
     * 检查价格、面积和每平方米价格是否都大于0
     * 
     * @return 数据有效返回true，否则返回false
     */
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