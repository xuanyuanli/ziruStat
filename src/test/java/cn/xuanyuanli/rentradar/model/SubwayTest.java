package cn.xuanyuanli.rentradar.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubwayTest {

    @Test
    void testConstructor_WithParameters() {
        Subway subway = new Subway("国贸", "1号线", "http://test.url");
        
        assertEquals("国贸", subway.getName());
        assertEquals("1号线", subway.getLineName());
        assertEquals("http://test.url", subway.getUrl());
        assertEquals(0.0, subway.getSquareMeterOfPrice(), 0.001);
        assertNull(subway.getLongitude());
        assertNull(subway.getLatitude());
    }

    @Test
    void testConstructor_Default() {
        Subway subway = new Subway();
        
        assertNull(subway.getName());
        assertNull(subway.getLineName());
        assertNull(subway.getUrl());
        assertEquals(0.0, subway.getSquareMeterOfPrice(), 0.001);
        assertNull(subway.getLongitude());
        assertNull(subway.getLatitude());
    }

    @Test
    void testSettersAndGetters() {
        Subway subway = new Subway();
        
        subway.setName("建国门");
        subway.setLineName("2号线");
        subway.setUrl("http://example.com");
        subway.setSquareMeterOfPrice(85.5);
        subway.setLongitude("116.434");
        subway.setLatitude("39.906");
        
        assertEquals("建国门", subway.getName());
        assertEquals("2号线", subway.getLineName());
        assertEquals("http://example.com", subway.getUrl());
        assertEquals(85.5, subway.getSquareMeterOfPrice(), 0.001);
        assertEquals("116.434", subway.getLongitude());
        assertEquals("39.906", subway.getLatitude());
    }

    @Test
    void testHasValidLocation_ValidLocation() {
        Subway subway = new Subway();
        subway.setLongitude("116.434");
        subway.setLatitude("39.906");
        
        assertTrue(subway.hasValidLocation());
    }

    @Test
    void testHasValidLocation_NullLongitude() {
        Subway subway = new Subway();
        subway.setLongitude(null);
        subway.setLatitude("39.906");
        
        assertFalse(subway.hasValidLocation());
    }

    @Test
    void testHasValidLocation_NullLatitude() {
        Subway subway = new Subway();
        subway.setLongitude("116.434");
        subway.setLatitude(null);
        
        assertFalse(subway.hasValidLocation());
    }

    @Test
    void testHasValidLocation_EmptyLongitude() {
        Subway subway = new Subway();
        subway.setLongitude("");
        subway.setLatitude("39.906");
        
        assertFalse(subway.hasValidLocation());
    }

    @Test
    void testHasValidLocation_EmptyLatitude() {
        Subway subway = new Subway();
        subway.setLongitude("116.434");
        subway.setLatitude("");
        
        assertFalse(subway.hasValidLocation());
    }

    @Test
    void testHasValidLocation_WhitespaceLongitude() {
        Subway subway = new Subway();
        subway.setLongitude("   ");
        subway.setLatitude("39.906");
        
        assertFalse(subway.hasValidLocation());
    }

    @Test
    void testHasValidLocation_WhitespaceLatitude() {
        Subway subway = new Subway();
        subway.setLongitude("116.434");
        subway.setLatitude("   ");
        
        assertFalse(subway.hasValidLocation());
    }

    @Test
    void testHasValidLocation_BothNull() {
        Subway subway = new Subway();
        subway.setLongitude(null);
        subway.setLatitude(null);
        
        assertFalse(subway.hasValidLocation());
    }

    @Test
    void testHasValidPrice_ValidPrice() {
        Subway subway = new Subway();
        subway.setSquareMeterOfPrice(85.5);
        
        assertTrue(subway.hasValidPrice());
    }

    @Test
    void testHasValidPrice_ZeroPrice() {
        Subway subway = new Subway();
        subway.setSquareMeterOfPrice(0.0);
        
        assertFalse(subway.hasValidPrice());
    }

    @Test
    void testHasValidPrice_NegativePrice() {
        Subway subway = new Subway();
        subway.setSquareMeterOfPrice(-10.5);
        
        assertFalse(subway.hasValidPrice());
    }

    @Test
    void testHasValidPrice_VerySmallPositivePrice() {
        Subway subway = new Subway();
        subway.setSquareMeterOfPrice(0.01);
        
        assertTrue(subway.hasValidPrice());
    }

    @Test
    void testGetDisplayName() {
        Subway subway = new Subway("国贸", "1号线", "http://test.url");
        
        assertEquals("1号线 国贸", subway.getDisplayName());
    }

    @Test
    void testGetDisplayName_WithNulls() {
        Subway subway = new Subway();
        subway.setName(null);
        subway.setLineName(null);
        
        assertEquals("null null", subway.getDisplayName());
    }

    @Test
    void testEquals_SameObject() {
        Subway subway = new Subway("国贸", "1号线", "http://test.url");
        
        assertTrue(subway.equals(subway));
    }

    @Test
    void testEquals_EqualObjects() {
        Subway subway1 = new Subway("国贸", "1号线", "http://test1.url");
        Subway subway2 = new Subway("国贸", "1号线", "http://test2.url");
        
        // equals只比较name和lineName，不比较url
        assertTrue(subway1.equals(subway2));
    }

    @Test
    void testEquals_DifferentName() {
        Subway subway1 = new Subway("国贸", "1号线", "http://test.url");
        Subway subway2 = new Subway("建国门", "1号线", "http://test.url");
        
        assertFalse(subway1.equals(subway2));
    }

    @Test
    void testEquals_DifferentLineName() {
        Subway subway1 = new Subway("国贸", "1号线", "http://test.url");
        Subway subway2 = new Subway("国贸", "2号线", "http://test.url");
        
        assertFalse(subway1.equals(subway2));
    }

    @Test
    void testEquals_NullObject() {
        Subway subway = new Subway("国贸", "1号线", "http://test.url");
        
        assertFalse(subway.equals(null));
    }

    @Test
    void testEquals_DifferentClass() {
        Subway subway = new Subway("国贸", "1号线", "http://test.url");
        String other = "not a subway";
        
        assertFalse(subway.equals(other));
    }

    @Test
    void testHashCode_EqualObjects() {
        Subway subway1 = new Subway("国贸", "1号线", "http://test1.url");
        Subway subway2 = new Subway("国贸", "1号线", "http://test2.url");
        
        assertEquals(subway1.hashCode(), subway2.hashCode());
    }

    @Test
    void testHashCode_DifferentObjects() {
        Subway subway1 = new Subway("国贸", "1号线", "http://test.url");
        Subway subway2 = new Subway("建国门", "1号线", "http://test.url");
        
        // 不同对象的hashCode可能不同（虽然不保证）
        assertNotEquals(subway1.hashCode(), subway2.hashCode());
    }

    @Test
    void testToString_AllFields() {
        Subway subway = new Subway("国贸", "1号线", "http://test.url");
        subway.setSquareMeterOfPrice(85.5);
        subway.setLongitude("116.434");
        subway.setLatitude("39.906");
        
        String toString = subway.toString();
        
        assertTrue(toString.contains("国贸"));
        assertTrue(toString.contains("1号线"));
        assertTrue(toString.contains("http://test.url"));
        assertTrue(toString.contains("85.5"));
        assertTrue(toString.contains("116.434"));
        assertTrue(toString.contains("39.906"));
    }

    @Test
    void testToString_NullFields() {
        Subway subway = new Subway();
        
        String toString = subway.toString();
        
        assertTrue(toString.contains("Subway{"));
        assertTrue(toString.contains("name='null'"));
        assertTrue(toString.contains("lineName='null'"));
    }

    @Test
    void testSpecialCharacters() {
        Subway subway = new Subway("国贸(CBD)", "1号线", "http://test.url?param=测试");
        
        assertEquals("国贸(CBD)", subway.getName());
        assertEquals("1号线 国贸(CBD)", subway.getDisplayName());
        assertTrue(subway.toString().contains("国贸(CBD)"));
    }

    @Test
    void testCompleteScenario() {
        // 创建一个完整的地铁站对象
        Subway subway = new Subway("国贸", "1号线", "http://ziroom.com/station/guomao");
        subway.setSquareMeterOfPrice(85.5);
        subway.setLongitude("116.4342");
        subway.setLatitude("39.9063");
        
        // 验证所有功能
        assertTrue(subway.hasValidPrice());
        assertTrue(subway.hasValidLocation());
        assertEquals("1号线 国贸", subway.getDisplayName());
        
        // 创建相同的地铁站（不同URL）
        Subway sameStation = new Subway("国贸", "1号线", "http://different.url");
        assertTrue(subway.equals(sameStation));
        assertEquals(subway.hashCode(), sameStation.hashCode());
        
        // 验证toString包含所有信息
        String str = subway.toString();
        assertTrue(str.contains("国贸"));
        assertTrue(str.contains("1号线"));
        assertTrue(str.contains("85.5"));
        assertTrue(str.contains("116.4342"));
        assertTrue(str.contains("39.9063"));
    }
}