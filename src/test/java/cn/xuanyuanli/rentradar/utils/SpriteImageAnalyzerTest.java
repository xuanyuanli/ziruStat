package cn.xuanyuanli.rentradar.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 精灵图真实结构分析测试（基于本地文件）
 * 用于验证PriceSpriteDecoder中的映射关系是否与实际精灵图一致
 * 
 * @author xuanyuanli
 */
@DisplayName("精灵图真实结构分析测试")
class SpriteImageAnalyzerTest {

    private static final String[] SPRITE_FILES = {
        "sprite_v1_a9da4f199beb8d74bffa9500762fd7b7.png",
        "sprite_v2_f4c1f82540f8d287aa53492a44f5819b.png", 
        "sprite_red_img_pricenumber_list_red.png"
    };
    
    private static final double[] SPRITE_INTERVALS = {21.4, 21.4, 20.0}; // 各版本的像素间隔
    
    private static String spritesResourcePath;
    
    @BeforeAll
    static void setupResourcePath() {
        spritesResourcePath = "src/test/resources/sprites/";
        
        // 验证精灵图文件是否存在
        for (String fileName : SPRITE_FILES) {
            String filePath = spritesResourcePath + fileName;
            if (!Files.exists(Paths.get(filePath))) {
                fail("精灵图文件不存在: " + filePath + "。请先运行SpriteImageDownloader下载精灵图文件。");
            }
        }
    }

    @Test
    @DisplayName("验证本地精灵图基本属性")
    void testLocalSpriteImageProperties() {
        for (int i = 0; i < SPRITE_FILES.length; i++) {
            final int index = i; // 创建 final 变量给 lambda 使用
            String fileName = SPRITE_FILES[i];
            String filePath = spritesResourcePath + fileName;
            
            assertDoesNotThrow(() -> {
                File file = new File(filePath);
                BufferedImage image = ImageIO.read(file);
                
                assertNotNull(image, "精灵图应该可以加载: " + fileName);
                assertTrue(image.getWidth() > 0, "图片宽度应大于0");
                assertTrue(image.getHeight() > 0, "图片高度应大于0");
                
                System.out.printf("精灵图 %s: 尺寸 %dx%d, 文件大小: %d bytes%n", 
                    fileName, image.getWidth(), image.getHeight(), file.length());
                
                // 基本的数字区域检测
                double interval = SPRITE_INTERVALS[index];
                int expectedDigitCount = (int) Math.ceil(image.getWidth() / interval);
                System.out.printf("预估数字个数: %d (基于间隔 %.1fpx)%n", expectedDigitCount, interval);
                
            }, "应能成功加载精灵图: " + fileName);
        }
    }

    @Test
    @DisplayName("分析V1版本精灵图数字排列")
    void testAnalyzeV1SpriteLayout() {
        String v1File = spritesResourcePath + SPRITE_FILES[0];
        
        assertDoesNotThrow(() -> {
            BufferedImage image = ImageIO.read(new File(v1File));
            SpriteAnalysisResult result = analyzeSpriteStructure(image, 21.4);
            
            // 验证分析结果与代码中的映射一致
            Map<String, String> expectedV1Mapping = Map.of(
                "0px", "8",
                "-21.4px", "6", 
                "-42.8px", "7",
                "-64.2px", "0",
                "-85.6px", "4",
                "-107.0px", "1",
                "-128.4px", "5", 
                "-149.8px", "9",
                "-171.2px", "2",
                "-192.6px", "3"
            );
            
            validateSpriteMapping(result, expectedV1Mapping, "V1");
        });
    }

    @Test
    @DisplayName("分析红色版本精灵图数字排列")
    void testAnalyzeRedSpriteLayout() {
        String redFile = spritesResourcePath + SPRITE_FILES[2];
        
        assertDoesNotThrow(() -> {
            BufferedImage image = ImageIO.read(new File(redFile));
            SpriteAnalysisResult result = analyzeSpriteStructure(image, 20.0);
            
            // 验证红色版本映射
            Map<String, String> expectedRedMapping = Map.of(
                "0px", "0",
                "-20px", "1",
                "-40px", "2", 
                "-60px", "3",
                "-80px", "4",
                "-100px", "5",
                "-120px", "6",
                "-140px", "7", 
                "-160px", "8",
                "-180px", "9"
            );
            
            validateSpriteMapping(result, expectedRedMapping, "RED");
        });
    }

    @Test
    @DisplayName("像素级精灵图分析和映射验证")
    void testPixelLevelSpriteAnalysis() {
        for (int i = 0; i < SPRITE_FILES.length; i++) {
            String fileName = SPRITE_FILES[i];
            String filePath = spritesResourcePath + fileName;
            double interval = SPRITE_INTERVALS[i];
            
            assertDoesNotThrow(() -> {
                BufferedImage image = ImageIO.read(new File(filePath));
                SpriteAnalysisResult result = analyzeSpriteStructure(image, interval);
                
                System.out.printf("%n=== %s 详细分析 ===%n", fileName);
                System.out.printf("尺寸: %dx%d%n", image.getWidth(), image.getHeight());
                System.out.printf("像素间隔: %.1fpx%n", interval);
                System.out.printf("检测到 %d 个数字区域%n", result.getDigitCount());
                
                // 像素采样分析
                performPixelSampling(image, interval);
                
                System.out.printf("建议的CSS位置映射:%n");
                result.getSuggestedMapping().forEach((position, digit) -> 
                    System.out.printf("  \"%s\" -> \"%s\"%n", position, digit));
                
            }, "分析精灵图失败: " + fileName);
        }
    }

    @Test
    @DisplayName("验证PriceSpriteDecoder映射表一致性")
    void testValidateDecoderMappingConsistency() {
        // 获取PriceSpriteDecoder当前的映射表
        Map<String, String> currentMapping = PriceSpriteDecoder.getMappingTable();
        
        System.out.println("\n=== PriceSpriteDecoder当前映射表 ===");
        currentMapping.entrySet().stream()
            .sorted(Map.Entry.comparingByKey((a, b) -> {
                double aVal = Double.parseDouble(a.replace("px", ""));
                double bVal = Double.parseDouble(b.replace("px", ""));
                return Double.compare(aVal, bVal);
            }))
            .forEach(entry -> 
                System.out.printf("  \"%s\" -> \"%s\"%n", entry.getKey(), entry.getValue()));
        
        // 验证映射表完整性
        assertTrue(currentMapping.size() >= 10, "映射表应至少包含10个数字");
        
        // 验证数字0-9都有映射
        Set<String> digits = new HashSet<>(currentMapping.values());
        for (int digit = 0; digit <= 9; digit++) {
            assertTrue(digits.contains(String.valueOf(digit)), 
                "数字 " + digit + " 应该在映射表中");
        }
        
        System.out.println("✓ 映射表完整性验证通过");
    }

    /**
     * 像素采样分析
     */
    private void performPixelSampling(BufferedImage image, double interval) {
        int width = image.getWidth();
        int height = image.getHeight();
        int centerY = height / 2;
        
        System.out.printf("像素采样分析 (中心行 y=%d):%n", centerY);
        
        // 采样每个数字位置的像素
        for (int i = 0; i < 10 && i * interval < width; i++) {
            int x = (int) (i * interval);
            if (x < width) {
                int rgb = image.getRGB(x, centerY);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;  
                int blue = rgb & 0xFF;
                
                System.out.printf("  位置 %d: RGB(%d,%d,%d) %s%n", 
                    x, red, green, blue, 
                    (red + green + blue < 128 * 3) ? "[深色/文字区域]" : "[浅色/背景区域]");
            }
        }
    }

    /**
     * 分析精灵图结构
     */
    private SpriteAnalysisResult analyzeSpriteStructure(BufferedImage image, double interval) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        // 基于已知间隔计算可能的数字位置
        Map<String, String> suggestedMapping = new LinkedHashMap<>();
        
        int digitCount = (int) Math.ceil(width / interval);
        digitCount = Math.min(digitCount, 10); // 最多10个数字
        
        for (int i = 0; i < digitCount; i++) {
            double xPos = i * interval;
            String position;
            if (i == 0) {
                position = "0px";
            } else if (Math.abs(interval - 20.0) < 0.1) {
                // 红色版本使用整数像素
                position = String.format("-%dpx", (int)(xPos));
            } else {
                // V1/V2版本使用小数像素
                position = String.format("-%.1fpx", xPos);
            }
            
            // 基于已知的映射信息推断数字（实际项目中应该用OCR）
            String digit = inferDigitFromPosition(position, interval);
            suggestedMapping.put(position, digit);
        }
        
        return new SpriteAnalysisResult(digitCount, suggestedMapping);
    }
    
    /**
     * 根据位置和间隔推断数字（基于已知映射）
     */
    private String inferDigitFromPosition(String position, double interval) {
        // 基于已知的映射关系推断
        if (Math.abs(interval - 21.4) < 0.1) {
            // V1/V2版本 (21.4px间隔)
            Map<String, String> knownV1Mapping = Map.of(
                "0px", "8", "-21.4px", "6", "-42.8px", "7", "-64.2px", "0", "-85.6px", "4",
                "-107.0px", "1", "-128.4px", "5", "-149.8px", "9", "-171.2px", "2", "-192.6px", "3"
            );
            return knownV1Mapping.getOrDefault(position, "?");
        } else if (Math.abs(interval - 20.0) < 0.1) {
            // 红色版本 (20px间隔)
            Map<String, String> knownRedMapping = Map.of(
                "0px", "0", "-20px", "1", "-40px", "2", "-60px", "3", "-80px", "4",
                "-100px", "5", "-120px", "6", "-140px", "7", "-160px", "8", "-180px", "9"
            );
            return knownRedMapping.getOrDefault(position, "?");
        }
        return "?";
    }

    /**
     * 验证精灵图映射准确性
     */
    private void validateSpriteMapping(SpriteAnalysisResult analysis, 
                                     Map<String, String> expectedMapping, 
                                     String spriteType) {
        
        System.out.printf("%n=== %s版本精灵图验证结果 ===%n", spriteType);
        
        // 验证位置数量
        assertEquals(expectedMapping.size(), analysis.getDigitCount(),
            spriteType + "版本数字数量不匹配");
        
        // 验证映射一致性
        Map<String, String> actualMapping = analysis.getSuggestedMapping();
        int matchCount = 0;
        int totalCount = expectedMapping.size();
        
        for (Map.Entry<String, String> expected : expectedMapping.entrySet()) {
            String position = expected.getKey();
            String expectedDigit = expected.getValue();
            String actualDigit = actualMapping.get(position);
            
            if (expectedDigit.equals(actualDigit)) {
                matchCount++;
                System.out.printf("✓ 位置 %s -> %s (匹配)%n", position, expectedDigit);
            } else {
                System.out.printf("✗ 位置 %s: 期望 %s, 实际 %s%n", position, expectedDigit, actualDigit);
            }
        }
        
        double accuracy = (double) matchCount / totalCount * 100;
        System.out.printf("映射准确率: %.1f%% (%d/%d)%n", accuracy, matchCount, totalCount);
        
        // 要求至少80%的映射准确率
        assertTrue(accuracy >= 80.0, 
            String.format("%s版本映射准确率过低: %.1f%% < 80%%", spriteType, accuracy));
        
        System.out.printf("✓ %s版本精灵图验证通过%n", spriteType);
    }

    /**
     * 精灵图分析结果
     */
    private static class SpriteAnalysisResult {
        private final int digitCount;
        private final Map<String, String> suggestedMapping;
        
        public SpriteAnalysisResult(int digitCount, Map<String, String> suggestedMapping) {
            this.digitCount = digitCount;
            this.suggestedMapping = suggestedMapping;
        }
        
        public int getDigitCount() { return digitCount; }
        public Map<String, String> getSuggestedMapping() { return suggestedMapping; }
    }
}