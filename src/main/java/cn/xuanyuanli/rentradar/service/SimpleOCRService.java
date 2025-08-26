package cn.xuanyuanli.rentradar.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

/**
 * 真实的OCR服务，用于识别价格数字
 * 使用Tesseract进行数字识别
 * 
 * @author xuanyuanli
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
public class SimpleOCRService {
    
    private static final Tesseract TESSERACT;
    private static final boolean OCR_AVAILABLE;
    
    static {
        Tesseract tempTesseract = null;
        boolean available = false;
        
        try {
            tempTesseract = new Tesseract();
            
            // 尝试设置数据路径 - 多种路径尝试
            String[] possiblePaths = {
                "src/main/resources/tessdata",
                "tessdata",
                System.getProperty("user.dir") + "/tessdata",
                System.getenv("TESSDATA_PREFIX")
            };
            
            boolean dataPathSet = false;
            for (String path : possiblePaths) {
                if (path != null && new File(path).exists()) {
                    tempTesseract.setDatapath(path);
                    dataPathSet = true;
                    break;
                }
            }
            
            if (!dataPathSet) {
                // 如果没找到数据路径，尝试使用系统默认
                System.out.println("未找到tessdata目录，使用系统默认Tesseract配置");
            }
            
            // 配置OCR参数
            tempTesseract.setVariable("tessedit_char_whitelist", "0123456789");
            tempTesseract.setPageSegMode(8); // 单词识别模式
            tempTesseract.setOcrEngineMode(1); // LSTM模式
            
            // 测试OCR是否可用
            BufferedImage testImage = new BufferedImage(100, 30, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = testImage.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, 100, 30);
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("123", 10, 22);
            g.dispose();
            
            String testResult = tempTesseract.doOCR(testImage);
            if (testResult != null && testResult.contains("123")) {
                available = true;
                System.out.println("OCR功能初始化成功");
            } else {
                System.out.println("OCR测试失败，将使用降级模式");
            }
            
        } catch (Exception e) {
            System.out.println("OCR初始化失败: " + e.getMessage() + "，将使用智能识别模式");
        }
        
        TESSERACT = tempTesseract;
        OCR_AVAILABLE = available;
    }
    
    /**
     * 从截图字节数组识别数字
     * 
     * @param imageBytes 截图字节数组
     * @return 识别到的数字字符串，失败时返回null
     */
    public static String recognizeDigits(byte[] imageBytes) {
        try {
            // 转换为BufferedImage
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));
            if (image == null) {
                return null;
            }
            
            System.out.println("OCR接收到图像，尺寸: " + image.getWidth() + "x" + image.getHeight());
            
            // 使用真正的OCR
            if (OCR_AVAILABLE && TESSERACT != null) {
                return realOCRRecognition(image);
            } else {
                System.out.println("OCR功能不可用");
                return null;
            }
            
        } catch (IOException e) {
            System.out.println("OCR识别失败: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 真正的OCR识别
     */
    private static String realOCRRecognition(BufferedImage image) {
        try {
            // 图像预处理
            BufferedImage processedImage = preprocessImage(image);
            
            // OCR识别
            String result = TESSERACT.doOCR(processedImage);
            
            // 清理结果
            String cleaned = cleanOCRResult(result);
            
            if (cleaned != null) {
                System.out.println("真实OCR识别成功: " + cleaned);
                return cleaned;
            } else {
                System.out.println("真实OCR识别失败");
                return null;
            }
            
        } catch (TesseractException e) {
            System.out.println("Tesseract识别异常: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * 图像预处理，提高OCR准确率
     */
    private static BufferedImage preprocessImage(BufferedImage original) {
        // 1. 放大图像
        int scaleFactor = 3;
        int newWidth = original.getWidth() * scaleFactor;
        int newHeight = original.getHeight() * scaleFactor;
        
        BufferedImage scaledImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
        g2d.dispose();
        
        // 2. 转为灰度并增强对比度
        BufferedImage grayscaleImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2 = grayscaleImage.createGraphics();
        g2.drawImage(scaledImage, 0, 0, null);
        g2.dispose();
        
        return grayscaleImage;
    }
    
    /**
     * 清理OCR识别结果
     */
    private static String cleanOCRResult(String ocrResult) {
        if (ocrResult == null) {
            return null;
        }
        
        // 只保留数字
        String cleaned = ocrResult.replaceAll("[^0-9]", "");
        
        // 检查结果合理性
        if (cleaned.isEmpty() || cleaned.length() > 8) {
            return null;
        }
        
        // 检查价格合理性（北京租房价格）
        try {
            int price = Integer.parseInt(cleaned);
            if (price >= 500 && price <= 99999) {
                return cleaned;
            }
        } catch (NumberFormatException e) {
            return null;
        }
        
        return null;
    }
    
}