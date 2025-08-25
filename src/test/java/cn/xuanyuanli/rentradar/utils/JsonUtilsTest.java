package cn.xuanyuanli.rentradar.utils;

import cn.xuanyuanli.rentradar.model.Subway;
import cn.xuanyuanli.rentradar.model.POI;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    void testToJsonString_ValidObject() {
        Subway subway = new Subway("国贸", "1号线", "http://test.url");
        subway.setSquareMeterOfPrice(85.5);
        
        String json = JsonUtils.toJsonString(subway);
        
        assertNotNull(json);
        assertFalse(json.isEmpty());
        assertTrue(json.contains("国贸"));
        assertTrue(json.contains("1号线"));
        assertTrue(json.contains("85.5"));
    }

    @Test
    void testToJsonString_NullObject() {
        String json = JsonUtils.toJsonString(null);
        
        // fastjson2对null返回"null"字符串，这是正常行为
        assertEquals("null", json, "null对象应该返回null字符串");
    }

    @Test
    void testParseObject_ValidJson() {
        String json = "{\"name\":\"国贸\",\"lineName\":\"1号线\",\"url\":\"http://test.url\",\"squareMeterOfPrice\":85.5}";
        
        Subway subway = JsonUtils.parseObject(json, Subway.class);
        
        assertNotNull(subway);
        assertEquals("国贸", subway.getName());
        assertEquals("1号线", subway.getLineName());
        assertEquals("http://test.url", subway.getUrl());
        assertEquals(85.5, subway.getSquareMeterOfPrice(), 0.001);
    }

    @Test
    void testParseObject_InvalidJson() {
        String invalidJson = "{invalid json}";
        
        Subway subway = JsonUtils.parseObject(invalidJson, Subway.class);
        
        assertNull(subway, "无效JSON应该返回null");
    }

    @Test
    void testParseObject_EmptyJson() {
        String emptyJson = "{}";
        
        Subway subway = JsonUtils.parseObject(emptyJson, Subway.class);
        
        assertNotNull(subway, "空JSON应该创建空对象");
        assertNull(subway.getName());
        assertNull(subway.getLineName());
    }

    @Test
    void testParseObject_NullJson() {
        Subway subway = JsonUtils.parseObject(null, Subway.class);
        
        assertNull(subway, "null字符串应该返回null");
    }

    @Test
    void testParseArray_ValidJsonArray() {
        String json = "[{\"name\":\"国贸\",\"lineName\":\"1号线\"},{\"name\":\"建国门\",\"lineName\":\"1号线\"}]";
        
        List<Subway> subways = JsonUtils.parseArray(json, Subway.class);
        
        assertNotNull(subways);
        assertEquals(2, subways.size());
        assertEquals("国贸", subways.get(0).getName());
        assertEquals("建国门", subways.get(1).getName());
    }

    @Test
    void testParseArray_EmptyArray() {
        String json = "[]";
        
        List<Subway> subways = JsonUtils.parseArray(json, Subway.class);
        
        assertNotNull(subways);
        assertTrue(subways.isEmpty());
    }

    @Test
    void testParseArray_InvalidJson() {
        String invalidJson = "[invalid json]";
        
        List<Subway> subways = JsonUtils.parseArray(invalidJson, Subway.class);
        
        assertNull(subways, "无效JSON数组应该返回null");
    }

    @Test
    void testParseArray_NullJson() {
        List<Subway> subways = JsonUtils.parseArray(null, Subway.class);
        
        assertNull(subways, "null字符串应该返回null");
    }

    @Test
    void testIsValidJson_ValidJson() {
        String validJson = "{\"name\":\"test\",\"value\":123}";
        
        assertTrue(JsonUtils.isValidJson(validJson));
    }

    @Test
    void testIsValidJson_ValidJsonArray() {
        String validJsonArray = "[{\"name\":\"test1\"},{\"name\":\"test2\"}]";
        
        assertTrue(JsonUtils.isValidJson(validJsonArray));
    }

    @Test
    void testIsValidJson_InvalidJson() {
        String invalidJson = "{invalid json}";
        
        assertFalse(JsonUtils.isValidJson(invalidJson));
    }

    @Test
    void testIsValidJson_NullJson() {
        // fastjson2认为null是有效JSON，返回true
        assertTrue(JsonUtils.isValidJson(null));
    }

    @Test
    void testIsValidJson_EmptyString() {
        // fastjson2认为空字符串也是有效JSON，返回true  
        assertTrue(JsonUtils.isValidJson(""));
    }

    @Test
    void testRoundTripSerialization_Subway() {
        Subway original = new Subway("国贸", "1号线", "http://test.url");
        original.setSquareMeterOfPrice(85.5);
        original.setLongitude("116.457");
        original.setLatitude("39.910");
        
        String json = JsonUtils.toJsonString(original);
        Subway deserialized = JsonUtils.parseObject(json, Subway.class);
        
        assertNotNull(deserialized);
        assertEquals(original.getName(), deserialized.getName());
        assertEquals(original.getLineName(), deserialized.getLineName());
        assertEquals(original.getUrl(), deserialized.getUrl());
        assertEquals(original.getSquareMeterOfPrice(), deserialized.getSquareMeterOfPrice(), 0.001);
        assertEquals(original.getLongitude(), deserialized.getLongitude());
        assertEquals(original.getLatitude(), deserialized.getLatitude());
    }

    @Test
    void testRoundTripSerialization_POI() {
        POI original = new POI("116.457", "39.910");
        original.setTel("010-12345678");
        original.setPostcode("100000");
        
        String json = JsonUtils.toJsonString(original);
        POI deserialized = JsonUtils.parseObject(json, POI.class);
        
        assertNotNull(deserialized);
        assertEquals(original.getLongitude(), deserialized.getLongitude());
        assertEquals(original.getLatitude(), deserialized.getLatitude());
        assertEquals(original.getTel(), deserialized.getTel());
        assertEquals(original.getPostcode(), deserialized.getPostcode());
    }

    @Test
    void testRoundTripSerialization_List() {
        List<Subway> original = Arrays.asList(
            new Subway("国贸", "1号线", "http://test1.url"),
            new Subway("建国门", "1号线", "http://test2.url")
        );
        
        String json = JsonUtils.toJsonString(original);
        List<Subway> deserialized = JsonUtils.parseArray(json, Subway.class);
        
        assertNotNull(deserialized);
        assertEquals(original.size(), deserialized.size());
        assertEquals(original.get(0).getName(), deserialized.get(0).getName());
        assertEquals(original.get(1).getName(), deserialized.get(1).getName());
    }

    @Test
    void testSpecialCharacters() {
        Subway subway = new Subway("国贸(中央商务区)", "1号线", "http://test.url?param=value&other=测试");
        
        String json = JsonUtils.toJsonString(subway);
        Subway deserialized = JsonUtils.parseObject(json, Subway.class);
        
        assertNotNull(deserialized);
        assertEquals("国贸(中央商务区)", deserialized.getName());
        assertEquals("http://test.url?param=value&other=测试", deserialized.getUrl());
    }
}