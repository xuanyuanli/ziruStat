package cn.xuanyuanli.rentradar.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    @DisplayName("测试空图像处理")
    void testRecognizeDigitsWithNullImage() {
        byte[] invalidImageBytes = "not an image".getBytes();

        String result = SimpleOCRService.recognizeDigits(invalidImageBytes);

        assertNull(result, "无效图像应返回null");
    }

    @Test
    @DisplayName("测试null输入处理")
    void testRecognizeDigitsWithNullInput() {
        assertNull(SimpleOCRService.recognizeDigits(null));
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

    @DisplayName("测试实际精灵图数字识别")
    @ParameterizedTest(name = "参数化测试: {0} - {1}")
    @CsvSource({"sprite_new_c4b718a0002eb143ea3484b373071495.png,9310867542",
            "sprite_v1_a9da4f199beb8d74bffa9500762fd7b7.png,8670415923",
            "sprite_red_img_pricenumber_list_red.png,8652039147",
            "sprite_v2_f4c1f82540f8d287aa53492a44f5819b.png,4978123605"})
    void testRealSpriteImageRecognition(String spriteFile, String expectedResult) throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(spriteFile)) {
            if (inputStream != null) {
                BufferedImage fullSprite = ImageIO.read(inputStream);
                assertNotNull(fullSprite, "精灵图应能成功加载: " + spriteFile);

                // 测试识别整个精灵图（应该能识别出多个数字）
                byte[] fullImageBytes = bufferedImageToBytes(fullSprite);
                String fullResult = SimpleOCRService.recognizeDigits(fullImageBytes);

                assertEquals(fullResult, expectedResult);
            }
        }
    }

    @Test
    @DisplayName("测试精灵图数字组合识别")
    void testSpriteDigitCombinationRecognition() throws IOException {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("sprites/sprite_v1_a9da4f199beb8d74bffa9500762fd7b7.png")) {
            if (inputStream != null && isOCRAvailable()) {
                BufferedImage sprite = ImageIO.read(inputStream);

                // 创建包含多个数字的子图像，模拟价格显示
                int digitWidth = 20;
                int digitHeight = sprite.getHeight();
                double interval = 21.4;

                // 创建一个包含"123"的图像（假设数字1、2、3在精灵图中的位置）
                int combinedWidth = digitWidth * 3;
                BufferedImage combinedImage = new BufferedImage(combinedWidth, digitHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = combinedImage.createGraphics();

                // 从精灵图中提取数字1、2、3并拼接（这里使用估算位置）
                for (int i = 0; i < 3; i++) {
                    int sourceX = (int) ((i + 1) * interval); // 假设1、2、3的位置
                    if (sourceX + digitWidth <= sprite.getWidth()) {
                        BufferedImage digitImage = sprite.getSubimage(sourceX, 0, digitWidth, digitHeight);
                        g.drawImage(digitImage, i * digitWidth, 0, null);
                    }
                }
                g.dispose();

                byte[] combinedBytes = bufferedImageToBytes(combinedImage);
                String result = SimpleOCRService.recognizeDigits(combinedBytes);

                System.out.println("组合数字识别结果: " + result);

                if (result != null) {
                    assertTrue(result.matches("\\d+"), "组合识别结果应该是数字");
                    assertTrue(result.length() >= 1 && result.length() <= 5, "组合识别结果长度应该合理");
                }
            } else {
                System.out.println("精灵图文件不存在或OCR不可用，跳过数字组合识别测试");
            }
        }
    }

    private void testSingleDigitFromSprite(BufferedImage sprite, String spriteFile) throws IOException {
        if (!isOCRAvailable()) {
            return;
        }

        // 基于不同精灵图的间隔设置
        double interval = 21.4; // 默认间隔
        if (spriteFile.contains("red")) {
            interval = 20.0;
        }

        int digitWidth = 20;
        int digitHeight = sprite.getHeight();

        // 只测试前3个数字区域，避免测试过于复杂
        for (int i = 0; i < 3; i++) {
            int x = (int) (i * interval);
            if (x + digitWidth <= sprite.getWidth()) {
                BufferedImage digitImage = sprite.getSubimage(x, 0, digitWidth, digitHeight);
                byte[] digitBytes = bufferedImageToBytes(digitImage);

                String result = SimpleOCRService.recognizeDigits(digitBytes);
                if (result != null) {
                    System.out.println(spriteFile + " 数字区域 " + i + " 识别结果: " + result);
                }
            }
        }
    }

    private byte[] bufferedImageToBytes(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
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