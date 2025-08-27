package cn.xuanyuanli.rentradar.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PriceSpriteDecoder 单元测试类
 *
 * @author xuanyuanli
 */
@DisplayName("CSS精灵图价格解码器测试")
class PriceSpriteDecoderTest {

    private List<Map<String, Object>> mockSpanDataV1;
    private List<Map<String, Object>> mockSpanDataV2;
    private List<Map<String, Object>> mockSpanDataRed;
    private List<Map<String, Object>> mockSpanDataNew;

    @BeforeEach
    void setUp() {
        // 准备测试数据 - 第一版精灵图 (价格: 1234)
        mockSpanDataV1 = new ArrayList<>();
        mockSpanDataV1.add(createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -107.0px center;"));  // 1
        mockSpanDataV1.add(createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -171.2px center;"));  // 2
        mockSpanDataV1.add(createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -192.6px center;"));  // 3
        mockSpanDataV1.add(createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -85.6px center;"));   // 4

        // 准备测试数据 - 第二版精灵图 (价格: 4978)
        mockSpanDataV2 = new ArrayList<>();
        mockSpanDataV2.add(createSpanData("background-image: url('f4c1f82540f8d287aa53492a44f5819b.png'); background-position: 0px center;"));       // 4
        mockSpanDataV2.add(createSpanData("background-image: url('f4c1f82540f8d287aa53492a44f5819b.png'); background-position: -21.4px center;"));   // 9
        mockSpanDataV2.add(createSpanData("background-image: url('f4c1f82540f8d287aa53492a44f5819b.png'); background-position: -42.8px center;"));   // 7
        mockSpanDataV2.add(createSpanData("background-image: url('f4c1f82540f8d287aa53492a44f5819b.png'); background-position: -64.2px center;"));   // 8

        // 准备测试数据 - 红色版精灵图 (价格: 8652)
        mockSpanDataRed = new ArrayList<>();
        mockSpanDataRed.add(createSpanData("background-image: url('img_pricenumber_list_red.png'); background-position: 0px center;"));     // 8
        mockSpanDataRed.add(createSpanData("background-image: url('img_pricenumber_list_red.png'); background-position: -20px center;"));   // 6
        mockSpanDataRed.add(createSpanData("background-image: url('img_pricenumber_list_red.png'); background-position: -40px center;"));   // 5
        mockSpanDataRed.add(createSpanData("background-image: url('img_pricenumber_list_red.png'); background-position: -60px center;"));   // 2

        // 准备测试数据 - 新版精灵图 (价格: 9310)
        mockSpanDataNew = new ArrayList<>();
        mockSpanDataNew.add(createSpanData("background-image: url('c4b718a0002eb143ea3484b373071495.png'); background-position: 0px center;"));       // 9
        mockSpanDataNew.add(createSpanData("background-image: url('c4b718a0002eb143ea3484b373071495.png'); background-position: -21.4px center;"));   // 3
        mockSpanDataNew.add(createSpanData("background-image: url('c4b718a0002eb143ea3484b373071495.png'); background-position: -42.8px center;"));   // 1
        mockSpanDataNew.add(createSpanData("background-image: url('c4b718a0002eb143ea3484b373071495.png'); background-position: -64.2px center;"));   // 0
    }

    private Map<String, Object> createSpanData(String style) {
        Map<String, Object> spanData = new HashMap<>();
        spanData.put("style", style);
        return spanData;
    }

    @Test
    @DisplayName("识别第一版精灵图类型")
    void testIdentifySpriteTypeV1() {
        String spriteType = PriceSpriteDecoder.identifySpriteType(mockSpanDataV1);
        assertEquals("a9da4f199beb8d74bffa9500762fd7b7", spriteType);
    }

    @Test
    @DisplayName("识别第二版精灵图类型")
    void testIdentifySpriteTypeV2() {
        String spriteType = PriceSpriteDecoder.identifySpriteType(mockSpanDataV2);
        assertEquals("f4c1f82540f8d287aa53492a44f5819b", spriteType);
    }

    @Test
    @DisplayName("识别红色版精灵图类型")
    void testIdentifySpriteTypeRed() {
        String spriteType = PriceSpriteDecoder.identifySpriteType(mockSpanDataRed);
        assertEquals("img_pricenumber_list_red", spriteType);
    }

    @Test
    @DisplayName("识别新版精灵图类型")
    void testIdentifySpriteTypeNew() {
        String spriteType = PriceSpriteDecoder.identifySpriteType(mockSpanDataNew);
        assertEquals("c4b718a0002eb143ea3484b373071495", spriteType);
    }

    @Test
    @DisplayName("解码第一版精灵图价格")
    void testDecodePriceV1() {
        String price = PriceSpriteDecoder.decodePrice(mockSpanDataV1);
        assertEquals("1234", price);
    }

    @Test
    @DisplayName("解码第二版精灵图价格")
    void testDecodePriceV2() {
        String price = PriceSpriteDecoder.decodePrice(mockSpanDataV2);
        assertEquals("4978", price);
    }

    @Test
    @DisplayName("解码红色版精灵图价格")
    void testDecodePriceRed() {
        String price = PriceSpriteDecoder.decodePrice(mockSpanDataRed);
        assertEquals("8652", price);
    }

    @Test
    @DisplayName("解码新版精灵图价格")
    void testDecodePriceNew() {
        String price = PriceSpriteDecoder.decodePrice(mockSpanDataNew);
        assertEquals("9310", price);
    }

    @Test
    @DisplayName("测试兼容性映射 - 整数格式px")
    void testCompatibilityMapping() {
        // 测试 -107px 格式（整数）与 -107.0px 格式的兼容性
        List<Map<String, Object>> compatData = new ArrayList<>();
        compatData.add(createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -107px center;"));  // 应该映射到 1
        
        String price = PriceSpriteDecoder.decodePrice(compatData);
        assertEquals("1", price);
    }

    @Test
    @DisplayName("测试空数据处理")
    void testDecodeEmptyData() {
        assertNull(PriceSpriteDecoder.decodePrice(null));
        assertNull(PriceSpriteDecoder.decodePrice(new ArrayList<>()));
    }

    @Test
    @DisplayName("测试无效样式数据")
    void testDecodeInvalidStyleData() {
        List<Map<String, Object>> invalidData = new ArrayList<>();
        invalidData.add(createSpanData(null));
        invalidData.add(createSpanData("invalid-style"));
        
        assertNull(PriceSpriteDecoder.decodePrice(invalidData));
    }

    @Test
    @DisplayName("测试未知精灵图识别")
    void testIdentifyUnknownSprite() {
        List<Map<String, Object>> unknownData = new ArrayList<>();
        unknownData.add(createSpanData("background-image: url('unknown_sprite_12345.png'); background-position: -21.4px center;"));
        
        String spriteType = PriceSpriteDecoder.identifySpriteType(unknownData);
        assertEquals("unknown_sprite_12345",spriteType);
    }

    @Test
    @DisplayName("测试价格有效性验证")
    void testIsValidPrice() {
        // 有效价格
        assertTrue(PriceSpriteDecoder.isValidPrice("1000"));
        assertTrue(PriceSpriteDecoder.isValidPrice("50000"));
        
        // 无效价格
        assertFalse(PriceSpriteDecoder.isValidPrice(null));
        assertFalse(PriceSpriteDecoder.isValidPrice(""));
        assertFalse(PriceSpriteDecoder.isValidPrice("abc"));
        assertFalse(PriceSpriteDecoder.isValidPrice("90")); // 太低
        assertFalse(PriceSpriteDecoder.isValidPrice("600000")); // 太高
    }

    @Test
    @DisplayName("测试混合样式数据解码")
    void testDecodeMixedStyleData() {
        List<Map<String, Object>> mixedData = new ArrayList<>();
        mixedData.add(createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -128.4px center;"));  // 5
        mixedData.add(createSpanData("invalid-style"));  // 无效数据
        mixedData.add(createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -64.2px center;"));   // 0
        mixedData.add(createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -64.2px center;"));   // 0
        mixedData.add(createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: 0px center;"));       // 8
        
        String price = PriceSpriteDecoder.decodePrice(mixedData);
        assertEquals("5008", price);
    }

    @Test
    @DisplayName("测试CSS样式顺序不影响解析")
    void testCssStyleOrder() {
        // 测试background-position和background-image属性顺序不同的情况
        List<Map<String, Object>> testData = new ArrayList<>();
        testData.add(createSpanData("background-position: -149.8px center; background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png');"));
        testData.add(createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -149.8px center;"));
        
        String price = PriceSpriteDecoder.decodePrice(testData);
        assertEquals("99", price); // -149.8px 对应数字 9
    }

    @Test
    @DisplayName("测试混合精灵图处理（每个span单独识别）")
    void testMixedSpriteTypes() {
        // 理论上每个span应该单独识别精灵图类型，但实际场景中通常是同一精灵图
        List<Map<String, Object>> mixedData = new ArrayList<>();
        // V1精灵图的数字1
        mixedData.add(createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -107.0px center;"));
        // V1精灵图的数字2  
        mixedData.add(createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -171.2px center;"));
        
        String price = PriceSpriteDecoder.decodePrice(mixedData);
        assertEquals("12", price);
    }

    @Test
    @DisplayName("测试未知精灵图处理（测试环境下不保存文件）")
    void testHandleUnknownSprite() {
        // 这里主要测试精灵图识别逻辑
        List<Map<String, Object>> unknownSpriteData = new ArrayList<>();
        unknownSpriteData.add(createSpanData("background-image: url('unknown_sprite_hash_12345.png'); background-position: -21.4px center;"));
        
        // 验证能正确识别为未知精灵图
        String decodePrice = PriceSpriteDecoder.decodePrice(unknownSpriteData);
        assertNull(decodePrice, "测试环境为null");
    }
}