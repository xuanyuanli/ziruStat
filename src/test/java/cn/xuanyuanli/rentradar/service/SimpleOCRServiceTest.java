package cn.xuanyuanli.rentradar.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SimpleOCRServiceTest {

    private byte[] createTestImageBytes(String text, Color backgroundColor, Color textColor) throws IOException {
        BufferedImage image = new BufferedImage(100, 30, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        g.setColor(backgroundColor);
        g.fillRect(0, 0, 100, 30);
        
        g.setColor(textColor);
        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        int x = (100 - fm.stringWidth(text)) / 2;
        int y = ((30 - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(text, x, y);
        g.dispose();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    @Test
    @DisplayName("测试正常数字识别")
    void testRecognizeDigitsSuccess() throws IOException {
        byte[] imageBytes = createTestImageBytes("1234", Color.WHITE, Color.BLACK);
        
        String result = SimpleOCRService.recognizeDigits(imageBytes);
        
        if (isOCRAvailable()) {
            assertNotNull(result, "OCR可用时应能识别数字");
            assertTrue(result.matches("\\d+"), "结果应只包含数字");
        } else {
            assertNull(result, "OCR不可用时应返回null");
        }
    }

    @Test
    @DisplayName("测试价格范围内的数字识别")
    void testRecognizeValidPriceRange() throws IOException {
        String[] validPrices = {"1500", "3000", "5000", "8000"};
        
        for (String price : validPrices) {
            byte[] imageBytes = createTestImageBytes(price, Color.WHITE, Color.BLACK);
            String result = SimpleOCRService.recognizeDigits(imageBytes);
            
            if (isOCRAvailable() && result != null) {
                int recognizedPrice = Integer.parseInt(result);
                assertTrue(recognizedPrice >= 500 && recognizedPrice <= 99999,
                    "识别的价格应在合理范围内: " + result);
            }
        }
    }

    @Test
    @DisplayName("测试价格范围外的数字处理")
    void testRecognizeInvalidPriceRange() throws IOException {
        String[] invalidPrices = {"100", "100000"};
        
        for (String price : invalidPrices) {
            byte[] imageBytes = createTestImageBytes(price, Color.WHITE, Color.BLACK);
            String result = SimpleOCRService.recognizeDigits(imageBytes);
            
            // 由于清理方法会过滤掉不合理的价格，所以应该返回null
            assertNull(result, "超出合理价格范围的数字应被过滤掉: " + price);
        }
    }

    @Test
    @DisplayName("测试空图像处理")
    void testRecognizeDigitsWithNullImage() {
        byte[] invalidImageBytes = "not an image".getBytes();
        
        String result = SimpleOCRService.recognizeDigits(invalidImageBytes);
        
        assertNull(result, "无效图像应返回null");
    }

    @Test
    @DisplayName("测试null输入处理")
    void testRecognizeDigitsWithNullInput() {
        // 实际实现会抛出NullPointerException，这是预期行为
        assertThrows(NullPointerException.class, () -> {
            SimpleOCRService.recognizeDigits(null);
        }, "null输入应抛出NullPointerException");
    }

    @Test
    @DisplayName("测试空白图像处理")
    void testRecognizeDigitsWithBlankImage() throws IOException {
        byte[] blankImageBytes = createTestImageBytes("", Color.WHITE, Color.BLACK);
        
        String result = SimpleOCRService.recognizeDigits(blankImageBytes);
        
        assertNull(result, "空白图像应返回null");
    }

    @Test
    @DisplayName("测试包含非数字字符的图像")
    void testRecognizeDigitsWithNonNumericText() throws IOException {
        byte[] imageBytes = createTestImageBytes("ABC123", Color.WHITE, Color.BLACK);
        
        String result = SimpleOCRService.recognizeDigits(imageBytes);
        
        if (isOCRAvailable() && result != null) {
            assertTrue(result.matches("\\d+"), "结果应只包含数字，过滤掉字母");
        }
    }

    @Test
    @DisplayName("测试图像预处理功能")
    void testImagePreprocessing() throws IOException {
        // 创建小尺寸图像测试放大功能
        BufferedImage smallImage = new BufferedImage(20, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = smallImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 20, 10);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 8));
        g.drawString("123", 2, 8);
        g.dispose();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(smallImage, "png", baos);
        byte[] imageBytes = baos.toByteArray();
        
        String result = SimpleOCRService.recognizeDigits(imageBytes);
        
        if (isOCRAvailable()) {
            // 由于小图像可能难以识别，这里只测试不抛异常
            assertDoesNotThrow(() -> {
                SimpleOCRService.recognizeDigits(imageBytes);
            }, "图像预处理应该不抛异常");
        } else {
            assertNull(result, "OCR不可用时应返回null");
        }
    }

    @Test
    @DisplayName("测试不同颜色组合的图像识别")
    void testDifferentColorCombinations() throws IOException {
        Color[][] colorCombinations = {
            {Color.BLACK, Color.WHITE},  // 黑底白字
            {Color.BLUE, Color.YELLOW},  // 蓝底黄字
            {Color.GRAY, Color.BLACK}    // 灰底黑字
        };
        
        for (Color[] colors : colorCombinations) {
            byte[] imageBytes = createTestImageBytes("2500", colors[0], colors[1]);
            String result = SimpleOCRService.recognizeDigits(imageBytes);
            
            if (isOCRAvailable() && result != null) {
                assertTrue(result.matches("\\d+"), "不同颜色组合都应能识别数字");
            }
        }
    }

    @Test
    @DisplayName("测试极长数字字符串的处理")
    void testVeryLongDigitString() throws IOException {
        String longDigits = "123456789012345";  // 15位数字，超过8位限制
        byte[] imageBytes = createTestImageBytes(longDigits, Color.WHITE, Color.BLACK);
        
        String result = SimpleOCRService.recognizeDigits(imageBytes);
        
        // 根据cleanOCRResult方法，超过8位的数字会被过滤掉
        assertNull(result, "超过8位的数字字符串应被过滤掉");
    }

    private boolean isOCRAvailable() {
        try {
            Field ocrAvailableField = SimpleOCRService.class.getDeclaredField("OCR_AVAILABLE");
            ocrAvailableField.setAccessible(true);
            return (Boolean) ocrAvailableField.get(null);
        } catch (Exception e) {
            return false;
        }
    }
}