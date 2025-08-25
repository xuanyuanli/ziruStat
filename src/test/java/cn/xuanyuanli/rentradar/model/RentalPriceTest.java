package cn.xuanyuanli.rentradar.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RentalPriceTest {

    @Test
    void testConstructor_WithParameters() {
        RentalPrice price = new RentalPrice(3000.0, 35.0);
        
        assertEquals(3000.0, price.getPrice(), 0.001);
        assertEquals(35.0, price.getArea(), 0.001);
        assertEquals(3000.0 / 35.0, price.getPricePerSquareMeter(), 0.001);
    }

    @Test
    void testConstructor_Default() {
        RentalPrice price = new RentalPrice();
        
        assertEquals(0.0, price.getPrice(), 0.001);
        assertEquals(0.0, price.getArea(), 0.001);
        assertEquals(0.0, price.getPricePerSquareMeter(), 0.001);
    }

    @Test
    void testConstructor_ZeroArea() {
        RentalPrice price = new RentalPrice(3000.0, 0.0);
        
        assertEquals(3000.0, price.getPrice(), 0.001);
        assertEquals(0.0, price.getArea(), 0.001);
        assertEquals(0.0, price.getPricePerSquareMeter(), 0.001);
    }

    @Test
    void testConstructor_NegativeArea() {
        RentalPrice price = new RentalPrice(3000.0, -35.0);
        
        assertEquals(3000.0, price.getPrice(), 0.001);
        assertEquals(-35.0, price.getArea(), 0.001);
        assertEquals(0.0, price.getPricePerSquareMeter(), 0.001); // 负面积不计算单价
    }

    @Test
    void testSetPrice_ValidArea() {
        RentalPrice price = new RentalPrice();
        price.setArea(50.0);
        price.setPrice(4000.0);
        
        assertEquals(4000.0, price.getPrice(), 0.001);
        assertEquals(80.0, price.getPricePerSquareMeter(), 0.001);
    }

    @Test
    void testSetPrice_ZeroArea() {
        RentalPrice price = new RentalPrice();
        price.setArea(0.0);
        price.setPrice(4000.0);
        
        assertEquals(4000.0, price.getPrice(), 0.001);
        assertEquals(0.0, price.getPricePerSquareMeter(), 0.001);
    }

    @Test
    void testSetArea_ValidPrice() {
        RentalPrice price = new RentalPrice();
        price.setPrice(3000.0);
        price.setArea(30.0);
        
        assertEquals(30.0, price.getArea(), 0.001);
        assertEquals(100.0, price.getPricePerSquareMeter(), 0.001);
    }

    @Test
    void testSetArea_ZeroPrice() {
        RentalPrice price = new RentalPrice();
        price.setPrice(0.0);
        price.setArea(30.0);
        
        assertEquals(30.0, price.getArea(), 0.001);
        assertEquals(0.0, price.getPricePerSquareMeter(), 0.001);
    }

    @Test
    void testRecalculatePricePerMeter_BothPositive() {
        RentalPrice price = new RentalPrice();
        price.setPrice(2500.0);
        price.setArea(25.0);
        
        assertEquals(100.0, price.getPricePerSquareMeter(), 0.001);
    }

    @Test
    void testRecalculatePricePerMeter_UpdatePrice() {
        RentalPrice price = new RentalPrice(2000.0, 40.0);
        assertEquals(50.0, price.getPricePerSquareMeter(), 0.001);
        
        price.setPrice(3000.0);
        assertEquals(75.0, price.getPricePerSquareMeter(), 0.001);
    }

    @Test
    void testRecalculatePricePerMeter_UpdateArea() {
        RentalPrice price = new RentalPrice(3000.0, 30.0);
        assertEquals(100.0, price.getPricePerSquareMeter(), 0.001);
        
        price.setArea(50.0);
        assertEquals(60.0, price.getPricePerSquareMeter(), 0.001);
    }

    @Test
    void testSetPricePerSquareMeter_DirectSetting() {
        RentalPrice price = new RentalPrice();
        price.setPricePerSquareMeter(85.5);
        
        assertEquals(85.5, price.getPricePerSquareMeter(), 0.001);
        
        // 直接设置不应该影响price和area
        assertEquals(0.0, price.getPrice(), 0.001);
        assertEquals(0.0, price.getArea(), 0.001);
    }

    @Test
    void testIsValid_AllPositive() {
        RentalPrice price = new RentalPrice(3000.0, 35.0);
        
        assertTrue(price.isValid());
    }

    @Test
    void testIsValid_ZeroPrice() {
        RentalPrice price = new RentalPrice(0.0, 35.0);
        
        assertFalse(price.isValid());
    }

    @Test
    void testIsValid_ZeroArea() {
        RentalPrice price = new RentalPrice(3000.0, 0.0);
        
        assertFalse(price.isValid());
    }

    @Test
    void testIsValid_ZeroPricePerSquareMeter() {
        RentalPrice price = new RentalPrice();
        price.setPrice(3000.0);
        price.setArea(35.0);
        price.setPricePerSquareMeter(0.0); // 手动设置为0
        
        assertFalse(price.isValid());
    }

    @Test
    void testIsValid_NegativePrice() {
        RentalPrice price = new RentalPrice(-3000.0, 35.0);
        
        assertFalse(price.isValid());
    }

    @Test
    void testIsValid_NegativeArea() {
        RentalPrice price = new RentalPrice(3000.0, -35.0);
        
        assertFalse(price.isValid());
    }

    @Test
    void testIsValid_NegativePricePerSquareMeter() {
        RentalPrice price = new RentalPrice();
        price.setPrice(3000.0);
        price.setArea(35.0);
        price.setPricePerSquareMeter(-10.0); // 手动设置为负数
        
        assertFalse(price.isValid());
    }

    @Test
    void testToString_AllFields() {
        RentalPrice price = new RentalPrice(3500.0, 42.5);
        
        String toString = price.toString();
        
        assertTrue(toString.contains("RentalPrice{"));
        assertTrue(toString.contains("price=3500.0"));
        assertTrue(toString.contains("area=42.5"));
        assertTrue(toString.contains("pricePerSquareMeter=" + (3500.0 / 42.5)));
    }

    @Test
    void testToString_ZeroValues() {
        RentalPrice price = new RentalPrice();
        
        String toString = price.toString();
        
        assertTrue(toString.contains("price=0.0"));
        assertTrue(toString.contains("area=0.0"));
        assertTrue(toString.contains("pricePerSquareMeter=0.0"));
    }

    @Test
    void testPrecisionCalculation() {
        RentalPrice price = new RentalPrice(3333.33, 33.33);
        
        double expectedPricePerMeter = 3333.33 / 33.33;
        assertEquals(expectedPricePerMeter, price.getPricePerSquareMeter(), 0.00001);
        assertTrue(price.isValid());
    }

    @Test
    void testSmallValues() {
        RentalPrice price = new RentalPrice(0.01, 0.01);
        
        assertEquals(1.0, price.getPricePerSquareMeter(), 0.001);
        assertTrue(price.isValid());
    }

    @Test
    void testLargeValues() {
        RentalPrice price = new RentalPrice(1000000.0, 500.0);
        
        assertEquals(2000.0, price.getPricePerSquareMeter(), 0.001);
        assertTrue(price.isValid());
    }

    @Test
    void testSequentialUpdates() {
        RentalPrice price = new RentalPrice();
        
        // 初始状态
        assertFalse(price.isValid());
        
        // 设置价格
        price.setPrice(2000.0);
        assertFalse(price.isValid()); // 还没有面积
        
        // 设置面积
        price.setArea(40.0);
        assertTrue(price.isValid());
        assertEquals(50.0, price.getPricePerSquareMeter(), 0.001);
        
        // 更新价格
        price.setPrice(3000.0);
        assertEquals(75.0, price.getPricePerSquareMeter(), 0.001);
        
        // 更新面积
        price.setArea(50.0);
        assertEquals(60.0, price.getPricePerSquareMeter(), 0.001);
    }

    @Test
    void testBoundaryValues() {
        // 测试边界值
        RentalPrice price1 = new RentalPrice(0.1, 0.1);
        assertTrue(price1.isValid());
        assertEquals(1.0, price1.getPricePerSquareMeter(), 0.001);
        
        RentalPrice price2 = new RentalPrice(Double.MIN_VALUE, Double.MIN_VALUE);
        assertTrue(price2.isValid());
        assertEquals(1.0, price2.getPricePerSquareMeter(), 0.001);
        
        // 非常大的值
        RentalPrice price3 = new RentalPrice(1e6, 1e3);
        assertTrue(price3.isValid());
        assertEquals(1000.0, price3.getPricePerSquareMeter(), 0.001);
    }

    @Test
    void testCompleteScenario() {
        // 模拟真实租房价格场景
        RentalPrice price = new RentalPrice(4200.0, 55.0);
        
        // 验证计算结果
        assertTrue(price.isValid());
        assertEquals(4200.0, price.getPrice(), 0.001);
        assertEquals(55.0, price.getArea(), 0.001);
        assertEquals(4200.0 / 55.0, price.getPricePerSquareMeter(), 0.001);
        
        // 价格调整
        price.setPrice(4500.0);
        assertEquals(4500.0 / 55.0, price.getPricePerSquareMeter(), 0.001);
        assertTrue(price.isValid());
        
        // 面积调整
        price.setArea(60.0);
        assertEquals(4500.0 / 60.0, price.getPricePerSquareMeter(), 0.001);
        assertTrue(price.isValid());
        
        // 验证toString包含所有信息
        String str = price.toString();
        assertTrue(str.contains("4500.0"));
        assertTrue(str.contains("60.0"));
        assertTrue(str.contains("75.0")); // 4500/60 = 75
    }
}