package cn.xuanyuanli.rentradar.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PriceSpriteDecoder 单元测试类
 * 
 * @author xuanyuanli
 */
@DisplayName("CSS精灵图价格解码器测试")
class PriceSpriteDecoderTest {

    @Nested
    @DisplayName("核心价格解码功能测试")
    class PriceDecodingTests {

        @Test
        @DisplayName("V1版本精灵图价格解码 - 正常流程")
        void testDecodePriceWithSpriteV1() {
            // 构造V1版本精灵图数据 - 价格1234
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -107.0px center;"), // 1
                createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -171.2px center;"), // 2
                createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -192.6px center;"), // 3
                createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -85.6px center;")   // 4
            );
            String result = PriceSpriteDecoder.decodePrice(priceSpanData);
            assertEquals("1234", result);
        }

        @Test
        @DisplayName("V2版本精灵图价格解码 - 正常流程")
        void testDecodePriceWithSpriteV2() {
            // 构造V2版本精灵图数据 - 价格5678
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-image: url('f4c1f82540f8d287aa53492a44f5819b.png'); background-position: -128.4px center;"), // 5
                createSpanData("background-image: url('f4c1f82540f8d287aa53492a44f5819b.png'); background-position: -21.4px center;"),  // 6  
                createSpanData("background-image: url('f4c1f82540f8d287aa53492a44f5819b.png'); background-position: -42.8px center;"),  // 7
                createSpanData("background-image: url('f4c1f82540f8d287aa53492a44f5819b.png'); background-position: 0px center;")      // 8
            );

            String result = PriceSpriteDecoder.decodePrice(priceSpanData);
            assertEquals("5678", result);
        }

        @Test
        @DisplayName("红色版本精灵图价格解码 - 正常流程")
        void testDecodePriceWithSpriteRed() {
            // 构造红色版精灵图数据 - 价格9012
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-image: url('img_pricenumber_list_red.png'); background-position: -180px center;"), // 9
                createSpanData("background-image: url('img_pricenumber_list_red.png'); background-position: 0px center;"),    // 0
                createSpanData("background-image: url('img_pricenumber_list_red.png'); background-position: -20px center;"),  // 1
                createSpanData("background-image: url('img_pricenumber_list_red.png'); background-position: -40px center;")   // 2
            );

            String result = PriceSpriteDecoder.decodePrice(priceSpanData);
            assertEquals("9012", result);
        }

        @Test
        @DisplayName("混合样式格式解码测试")
        void testDecodePriceWithMixedStyleFormats() {
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                // 不同的CSS样式格式
                createSpanData("background-position:-107.0px center;background-image:url(a9da4f199beb8d74bffa9500762fd7b7.png)"), // 1
                createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -64.2px 0px;"), // 0
                createSpanData("background-position: -128.4px; background-image: url(\"a9da4f199beb8d74bffa9500762fd7b7.png\")") // 5
            );

            String result = PriceSpriteDecoder.decodePrice(priceSpanData);
            assertEquals("105", result);
        }

        @Test
        @DisplayName("备用映射表降级处理测试")
        void testDecodePriceWithFallbackMapping() {
            // 构造未知精灵图数据，但位置值与已知映射匹配
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-image: url('unknown_sprite.png'); background-position: -107.0px center;"), // 1
                createSpanData("background-image: url('unknown_sprite.png'); background-position: -171.2px center;")  // 2
            );

            String result = PriceSpriteDecoder.decodePrice(priceSpanData);
            // 应该使用默认映射表（V1版本）成功解码
            assertEquals("12", result);
        }
    }

    @Nested
    @DisplayName("精灵图类型识别测试")
    class SpriteTypeIdentificationTests {

        @Test
        @DisplayName("识别V1版本精灵图")
        void testIdentifySpriteV1() {
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-image: url('https://example.com/a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -107.0px center;")
            );

            PriceSpriteDecoder.SpriteImageType result = PriceSpriteDecoder.identifySpriteType(priceSpanData);
            assertEquals(PriceSpriteDecoder.SpriteImageType.SPRITE_V1, result);
        }

        @Test
        @DisplayName("识别V2版本精灵图")
        void testIdentifySpriteV2() {
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-image: url('/assets/f4c1f82540f8d287aa53492a44f5819b.png'); background-position: -128.4px center;")
            );

            PriceSpriteDecoder.SpriteImageType result = PriceSpriteDecoder.identifySpriteType(priceSpanData);
            assertEquals(PriceSpriteDecoder.SpriteImageType.SPRITE_V2, result);
        }

        @Test
        @DisplayName("识别红色版本精灵图")
        void testIdentifySpriteRed() {
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-image: url('img_pricenumber_list_red.png'); background-position: -20px center;")
            );

            PriceSpriteDecoder.SpriteImageType result = PriceSpriteDecoder.identifySpriteType(priceSpanData);
            assertEquals(PriceSpriteDecoder.SpriteImageType.SPRITE_RED, result);
        }

        @Test
        @DisplayName("无法识别精灵图类型")
        void testIdentifyUnknownSpriteType() {
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-image: url('unknown_sprite.png'); background-position: -20px center;")
            );

            PriceSpriteDecoder.SpriteImageType result = PriceSpriteDecoder.identifySpriteType(priceSpanData);
            assertNull(result);
        }

        @Test
        @DisplayName("多个span中第一个包含精灵图信息")
        void testIdentifySpriteTypeFromMultipleSpans() {
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-position: -20px center;"), // 没有background-image
                createSpanData("background-image: url('img_pricenumber_list_red.png'); background-position: -40px center;") // 有完整信息
            );

            PriceSpriteDecoder.SpriteImageType result = PriceSpriteDecoder.identifySpriteType(priceSpanData);
            assertEquals(PriceSpriteDecoder.SpriteImageType.SPRITE_RED, result);
        }
    }

    @Nested
    @DisplayName("边界条件和异常场景测试")
    class BoundaryAndExceptionTests {

        @Test
        @DisplayName("空数据处理")
        void testDecodeWithNullOrEmptyData() {
            assertNull(PriceSpriteDecoder.decodePrice(null));
            assertNull(PriceSpriteDecoder.decodePrice(Collections.emptyList()));
        }

        @Test
        @DisplayName("无效CSS样式处理")
        void testDecodeWithInvalidStyles() {
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("color: red; font-size: 14px;"), // 没有background-position
                createSpanData("background-position: invalid-value;"), // 无效position值
                createSpanData(null) // null样式
            );

            String result = PriceSpriteDecoder.decodePrice(priceSpanData);
            assertNull(result);
        }

        @Test
        @DisplayName("部分未知位置值处理")
        void testDecodeWithPartialUnknownPositions() {
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -107.0px center;"), // 已知位置：1
                createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -999px center;")   // 未知位置
            );

            String result = PriceSpriteDecoder.decodePrice(priceSpanData);
            assertNull(result); // 有未知位置时应返回null
        }

        @Test
        @DisplayName("精灵图类型识别空数据处理")
        void testIdentifySpriteTypeWithEmptyData() {
            assertNull(PriceSpriteDecoder.identifySpriteType(null));
            assertNull(PriceSpriteDecoder.identifySpriteType(Collections.emptyList()));
        }

        @Test
        @DisplayName("精灵图类型识别无背景图片样式")
        void testIdentifySpriteTypeWithoutBackgroundImage() {
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-position: -20px center; color: red;")
            );

            PriceSpriteDecoder.SpriteImageType result = PriceSpriteDecoder.identifySpriteType(priceSpanData);
            assertNull(result);
        }
    }

    @Nested
    @DisplayName("价格验证功能测试")
    class PriceValidationTests {

        @Test
        @DisplayName("有效价格范围测试")
        void testValidPriceRange() {
            assertTrue(PriceSpriteDecoder.isValidPrice("500"));   // 最低价格
            assertTrue(PriceSpriteDecoder.isValidPrice("3000"));  // 中等价格
            assertTrue(PriceSpriteDecoder.isValidPrice("50000")); // 最高价格
            assertTrue(PriceSpriteDecoder.isValidPrice("1500.5")); // 小数价格
        }

        @Test
        @DisplayName("无效价格测试")
        void testInvalidPrices() {
            assertFalse(PriceSpriteDecoder.isValidPrice(null));     // null
            assertFalse(PriceSpriteDecoder.isValidPrice(""));       // 空字符串
            assertFalse(PriceSpriteDecoder.isValidPrice("499"));    // 低于最低价格
            assertFalse(PriceSpriteDecoder.isValidPrice("50001"));  // 高于最高价格
            assertFalse(PriceSpriteDecoder.isValidPrice("abc"));    // 非数字
            assertFalse(PriceSpriteDecoder.isValidPrice("-100"));   // 负数
        }
    }

    @Nested
    @DisplayName("未知精灵图检测功能测试")
    class UnknownSpriteDetectionTests {

        @Test
        @DisplayName("检测已知精灵图")
        void testDetectKnownSprite() {
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -107.0px center;")
            );

            PriceSpriteDecoder.SpriteDetectionResult result = PriceSpriteDecoder.detectUnknownSprite(priceSpanData);
            assertTrue(result.isKnownSprite());
            assertEquals(PriceSpriteDecoder.SpriteImageType.SPRITE_V1, result.getSpriteType());
            assertTrue(result.getMessage().contains("识别到已知精灵图"));
        }

        @Test
        @DisplayName("检测未知精灵图")
        void testDetectUnknownSprite() {
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-image: url('unknown_sprite.png'); background-position: -30px center;")
            );

            PriceSpriteDecoder.SpriteDetectionResult result = PriceSpriteDecoder.detectUnknownSprite(priceSpanData);
            assertFalse(result.isKnownSprite());
            assertNull(result.getSpriteType());
            assertTrue(result.getMessage().contains("发现未知精灵图"));
            assertTrue(result.getMessage().contains("unknown_sprite.png"));
        }

        @Test
        @DisplayName("检测空数据")
        void testDetectWithEmptyData() {
            PriceSpriteDecoder.SpriteDetectionResult result = PriceSpriteDecoder.detectUnknownSprite(Collections.emptyList());
            assertFalse(result.isKnownSprite());
            assertNull(result.getSpriteType());
            assertEquals("无价格数据", result.getMessage());
        }
    }

    @Nested
    @DisplayName("工具方法测试")
    class UtilityMethodTests {

        @Test
        @DisplayName("更新映射表功能")
        void testUpdateMapping() {
            // 获取更新前的映射表大小
            int originalSize = PriceSpriteDecoder.getMappingTable().size();
            
            // 添加新映射
            PriceSpriteDecoder.updateMapping("-999px", "X");
            
            Map<String, String> updatedMapping = PriceSpriteDecoder.getMappingTable();
            assertEquals(originalSize + 1, updatedMapping.size());
            assertEquals("X", updatedMapping.get("-999px"));
        }

        @Test
        @DisplayName("获取映射表功能")
        void testGetMappingTable() {
            Map<String, String> mapping = PriceSpriteDecoder.getMappingTable();
            assertNotNull(mapping);
            assertFalse(mapping.isEmpty());
            
            // 验证是副本而不是原对象
            mapping.put("test", "test");
            Map<String, String> freshMapping = PriceSpriteDecoder.getMappingTable();
            assertFalse(freshMapping.containsKey("test"));
        }

        @Test
        @DisplayName("枚举类型功能测试")
        void testSpriteImageTypeEnum() {
            assertEquals("a9da4f199beb8d74bffa9500762fd7b7", 
                        PriceSpriteDecoder.SpriteImageType.SPRITE_V1.getIdentifier());
            assertEquals("第一版精灵图", 
                        PriceSpriteDecoder.SpriteImageType.SPRITE_V1.getDescription());
            
            assertEquals("f4c1f82540f8d287aa53492a44f5819b", 
                        PriceSpriteDecoder.SpriteImageType.SPRITE_V2.getIdentifier());
            assertEquals("第二版精灵图", 
                        PriceSpriteDecoder.SpriteImageType.SPRITE_V2.getDescription());
            
            assertEquals("img_pricenumber_list_red", 
                        PriceSpriteDecoder.SpriteImageType.SPRITE_RED.getIdentifier());
            assertEquals("红色版精灵图", 
                        PriceSpriteDecoder.SpriteImageType.SPRITE_RED.getDescription());
        }
    }

    @Nested
    @DisplayName("复杂场景集成测试")
    class ComplexScenarioTests {

        @Test
        @DisplayName("完整价格解码流程测试")
        void testCompleteDecodingWorkflow() {
            // 模拟真实的房租价格：2580元
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-image: url('https://static.ziroom.com/phoenix/pc/images/a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -171.2px center;"), // 2
                createSpanData("background-image: url('https://static.ziroom.com/phoenix/pc/images/a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -128.4px center;"), // 5
                createSpanData("background-image: url('https://static.ziroom.com/phoenix/pc/images/a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: 0px center;"),        // 8
                createSpanData("background-image: url('https://static.ziroom.com/phoenix/pc/images/a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -64.2px center;")   // 0
            );

            // 1. 解码价格
            String price = PriceSpriteDecoder.decodePrice(priceSpanData);
            assertEquals("2580", price);

            // 2. 验证价格合理性
            assertTrue(PriceSpriteDecoder.isValidPrice(price));

            // 3. 验证精灵图识别
            PriceSpriteDecoder.SpriteImageType spriteType = PriceSpriteDecoder.identifySpriteType(priceSpanData);
            assertEquals(PriceSpriteDecoder.SpriteImageType.SPRITE_V1, spriteType);

            // 4. 验证检测结果
            PriceSpriteDecoder.SpriteDetectionResult detection = PriceSpriteDecoder.detectUnknownSprite(priceSpanData);
            assertTrue(detection.isKnownSprite());
        }

        @Test
        @DisplayName("多种精灵图混合数据处理")
        void testMixedSpriteTypesHandling() {
            // 错误场景：混合不同版本的精灵图数据（实际不应该发生）
            List<Map<String, Object>> priceSpanData = Arrays.asList(
                createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -107.0px center;"),      // V1: 1
                createSpanData("background-image: url('f4c1f82540f8d287aa53492a44f5819b.png'); background-position: -171.2px center;")       // V2: 2
            );

            // 应该能识别第一个精灵图类型
            PriceSpriteDecoder.SpriteImageType spriteType = PriceSpriteDecoder.identifySpriteType(priceSpanData);
            assertEquals(PriceSpriteDecoder.SpriteImageType.SPRITE_V1, spriteType);
            
            // 解码可能成功或失败，取决于具体实现
            String result = PriceSpriteDecoder.decodePrice(priceSpanData);
            // 结果可能是"12"或null，这里不做强断言
        }

        @Test
        @DisplayName("大量数据性能测试")
        void testPerformanceWithLargeDataset() {
            // 构造大量数据（模拟长价格）
            List<Map<String, Object>> priceSpanData = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                priceSpanData.add(createSpanData("background-image: url('a9da4f199beb8d74bffa9500762fd7b7.png'); background-position: -107.0px center;")); // 1
            }

            long startTime = System.currentTimeMillis();
            String result = PriceSpriteDecoder.decodePrice(priceSpanData);
            long endTime = System.currentTimeMillis();

            // 验证结果正确性
            assertEquals("1".repeat(100), result);
            
            // 验证性能（应该在合理时间内完成，这里设为1秒）
            assertTrue(endTime - startTime < 1000, "解码耗时过长: " + (endTime - startTime) + "ms");
        }
    }

    /**
     * 创建测试用的span数据
     */
    private Map<String, Object> createSpanData(String style) {
        Map<String, Object> spanData = new HashMap<>();
        spanData.put("style", style);
        return spanData;
    }
}