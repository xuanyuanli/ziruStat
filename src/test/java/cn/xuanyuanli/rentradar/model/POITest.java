package cn.xuanyuanli.rentradar.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class POITest {

    @Test
    void testConstructor_WithParameters() {
        POI poi = new POI("116.434", "39.906");
        
        assertEquals("116.434", poi.getLongitude());
        assertEquals("39.906", poi.getLatitude());
        assertNull(poi.getTel());
        assertNull(poi.getPostcode());
    }

    @Test
    void testConstructor_Default() {
        POI poi = new POI();
        
        assertNull(poi.getLongitude());
        assertNull(poi.getLatitude());
        assertNull(poi.getTel());
        assertNull(poi.getPostcode());
    }

    @Test
    void testSettersAndGetters() {
        POI poi = new POI();
        
        poi.setLongitude("116.434");
        poi.setLatitude("39.906");
        poi.setTel("010-12345678");
        poi.setPostcode("100000");
        
        assertEquals("116.434", poi.getLongitude());
        assertEquals("39.906", poi.getLatitude());
        assertEquals("010-12345678", poi.getTel());
        assertEquals("100000", poi.getPostcode());
    }

    @Test
    void testIsValid_ValidCoordinates() {
        POI poi = new POI("116.434", "39.906");
        
        assertTrue(poi.isValid());
    }

    @Test
    void testIsValid_NullLongitude() {
        POI poi = new POI(null, "39.906");
        
        assertFalse(poi.isValid());
    }

    @Test
    void testIsValid_NullLatitude() {
        POI poi = new POI("116.434", null);
        
        assertFalse(poi.isValid());
    }

    @Test
    void testIsValid_EmptyLongitude() {
        POI poi = new POI("", "39.906");
        
        assertFalse(poi.isValid());
    }

    @Test
    void testIsValid_EmptyLatitude() {
        POI poi = new POI("116.434", "");
        
        assertFalse(poi.isValid());
    }

    @Test
    void testIsValid_WhitespaceLongitude() {
        POI poi = new POI("   ", "39.906");
        
        assertFalse(poi.isValid());
    }

    @Test
    void testIsValid_WhitespaceLatitude() {
        POI poi = new POI("116.434", "   ");
        
        assertFalse(poi.isValid());
    }

    @Test
    void testIsValid_BothNull() {
        POI poi = new POI(null, null);
        
        assertFalse(poi.isValid());
    }

    @Test
    void testIsValid_BothEmpty() {
        POI poi = new POI("", "");
        
        assertFalse(poi.isValid());
    }

    @Test
    void testIsValid_WithOptionalFields() {
        POI poi = new POI("116.434", "39.906");
        poi.setTel("010-12345678");
        poi.setPostcode("100000");
        
        // isValid只检查经纬度，不检查tel和postcode
        assertTrue(poi.isValid());
    }

    @Test
    void testEquals_SameObject() {
        POI poi = new POI("116.434", "39.906");
        
        assertTrue(poi.equals(poi));
    }

    @Test
    void testEquals_EqualCoordinates() {
        POI poi1 = new POI("116.434", "39.906");
        POI poi2 = new POI("116.434", "39.906");
        
        assertTrue(poi1.equals(poi2));
    }

    @Test
    void testEquals_EqualCoordinates_DifferentOptionalFields() {
        POI poi1 = new POI("116.434", "39.906");
        poi1.setTel("010-11111111");
        poi1.setPostcode("100001");
        
        POI poi2 = new POI("116.434", "39.906");
        poi2.setTel("010-22222222");
        poi2.setPostcode("100002");
        
        // equals只比较经纬度
        assertTrue(poi1.equals(poi2));
    }

    @Test
    void testEquals_DifferentLongitude() {
        POI poi1 = new POI("116.434", "39.906");
        POI poi2 = new POI("116.435", "39.906");
        
        assertFalse(poi1.equals(poi2));
    }

    @Test
    void testEquals_DifferentLatitude() {
        POI poi1 = new POI("116.434", "39.906");
        POI poi2 = new POI("116.434", "39.907");
        
        assertFalse(poi1.equals(poi2));
    }

    @Test
    void testEquals_NullObject() {
        POI poi = new POI("116.434", "39.906");
        
        assertFalse(poi.equals(null));
    }

    @Test
    void testEquals_DifferentClass() {
        POI poi = new POI("116.434", "39.906");
        String other = "not a poi";
        
        assertFalse(poi.equals(other));
    }

    @Test
    void testEquals_BothHaveNullCoordinates() {
        POI poi1 = new POI(null, null);
        POI poi2 = new POI(null, null);
        
        assertTrue(poi1.equals(poi2));
    }

    @Test
    void testHashCode_EqualObjects() {
        POI poi1 = new POI("116.434", "39.906");
        POI poi2 = new POI("116.434", "39.906");
        
        assertEquals(poi1.hashCode(), poi2.hashCode());
    }

    @Test
    void testHashCode_DifferentCoordinates() {
        POI poi1 = new POI("116.434", "39.906");
        POI poi2 = new POI("116.435", "39.907");
        
        // 不同对象的hashCode应该不同（虽然不保证）
        assertNotEquals(poi1.hashCode(), poi2.hashCode());
    }

    @Test
    void testHashCode_NullCoordinates() {
        POI poi1 = new POI(null, null);
        POI poi2 = new POI(null, null);
        
        assertEquals(poi1.hashCode(), poi2.hashCode());
    }

    @Test
    void testToString_AllFields() {
        POI poi = new POI("116.434", "39.906");
        poi.setTel("010-12345678");
        poi.setPostcode("100000");
        
        String toString = poi.toString();
        
        assertTrue(toString.contains("116.434"));
        assertTrue(toString.contains("39.906"));
        assertTrue(toString.contains("010-12345678"));
        assertTrue(toString.contains("100000"));
    }

    @Test
    void testToString_MinimalFields() {
        POI poi = new POI("116.434", "39.906");
        
        String toString = poi.toString();
        
        assertTrue(toString.contains("POI{"));
        assertTrue(toString.contains("116.434"));
        assertTrue(toString.contains("39.906"));
        assertTrue(toString.contains("tel='null'"));
        assertTrue(toString.contains("postcode='null'"));
    }

    @Test
    void testToString_NullCoordinates() {
        POI poi = new POI();
        
        String toString = poi.toString();
        
        assertTrue(toString.contains("longitude='null'"));
        assertTrue(toString.contains("latitude='null'"));
    }

    @Test
    void testValidCoordinateFormats() {
        // 测试各种有效的坐标格式
        POI poi1 = new POI("116.4342", "39.9063");
        assertTrue(poi1.isValid());
        
        POI poi2 = new POI("116", "39");
        assertTrue(poi2.isValid());
        
        POI poi3 = new POI("116.123456789", "39.123456789");
        assertTrue(poi3.isValid());
        
        // 带符号的坐标
        POI poi4 = new POI("-116.434", "-39.906");
        assertTrue(poi4.isValid());
        
        // 零坐标
        POI poi5 = new POI("0", "0");
        assertTrue(poi5.isValid());
    }

    @Test
    void testSpecialCharacters() {
        POI poi = new POI("116.434", "39.906");
        poi.setTel("010-12345678（总机）");
        poi.setPostcode("100000");
        
        assertEquals("010-12345678（总机）", poi.getTel());
        assertTrue(poi.toString().contains("010-12345678（总机）"));
    }

    @Test
    void testCompleteScenario() {
        // 创建一个完整的POI对象
        POI poi = new POI("116.4342", "39.9063");
        poi.setTel("010-87654321");
        poi.setPostcode("100020");
        
        // 验证所有功能
        assertTrue(poi.isValid());
        assertEquals("116.4342", poi.getLongitude());
        assertEquals("39.9063", poi.getLatitude());
        assertEquals("010-87654321", poi.getTel());
        assertEquals("100020", poi.getPostcode());
        
        // 创建相同坐标的POI（不同的其他字段）
        POI samePoi = new POI("116.4342", "39.9063");
        samePoi.setTel("010-11111111");
        samePoi.setPostcode("100001");
        
        assertTrue(poi.equals(samePoi));
        assertEquals(poi.hashCode(), samePoi.hashCode());
        
        // 验证toString包含所有信息
        String str = poi.toString();
        assertTrue(str.contains("116.4342"));
        assertTrue(str.contains("39.9063"));
        assertTrue(str.contains("010-87654321"));
        assertTrue(str.contains("100020"));
    }
}