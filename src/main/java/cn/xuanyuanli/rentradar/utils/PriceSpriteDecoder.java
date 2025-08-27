package cn.xuanyuanli.rentradar.utils;

import cn.xuanyuanli.core.util.Resources;
import com.alibaba.fastjson2.JSON;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CSS精灵图价格解码器
 * <p>
 * 用于解析自如网站使用CSS精灵图显示的价格数字，
 * 通过分析background-position来解码价格数字。
 * 支持多种精灵图文件的识别和解码。
 * </p>
 *
 * @author xuanyuanli
 * @since 1.0.0
 */
public class PriceSpriteDecoder {

    /**
     * 精灵图配置缓存：精灵图文件名/哈希 -> 精灵图配置
     */
    private static final Map<String, SpriteConfig> SPRITE_CONFIGS = new HashMap<>();

    /**
     * 精灵图配置数据模型
     */
    @SuppressWarnings("unused")
    static class SpriteConfig {
        private String identifier;
        private double pixelInterval;
        private String digitOrder;
        private Map<String, String> mapping;

        // Getters and setters
        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public void setPixelInterval(double pixelInterval) {
            this.pixelInterval = pixelInterval;
        }

        public void setDigitOrder(String digitOrder) {
            this.digitOrder = digitOrder;
        }

        public Map<String, String> getMapping() {
            return mapping;
        }

        public void setMapping(Map<String, String> mapping) {
            this.mapping = mapping;
        }

        public double getPixelInterval() {
            return pixelInterval;
        }

        public String getDigitOrder() {
            return digitOrder;
        }
    }


    static {
        // 动态加载所有精灵图配置文件
        scanAndLoadSpriteConfigs();
    }


    /**
     * 解码精灵图价格
     * <p>
     * 使用精灵图映射解码价格，如果遇到未知精灵图则提示用户手动输入
     * </p>
     *
     * @param priceSpanData 价格span元素的样式数据列表，包含style属性
     * @return 解码后的价格字符串，解码失败时返回null
     */
    public static String decodePrice(List<Map<String, Object>> priceSpanData) {
        if (priceSpanData == null || priceSpanData.isEmpty()) {
            return null;
        }

        StringBuilder price = new StringBuilder();

        for (Map<String, Object> spanData : priceSpanData) {
            String style = (String) spanData.get("style");
            if (style == null) {
                continue;
            }

            // 1. 识别当前span的精灵图类型
            String spriteId = identifySpriteTypeFromSingle(spanData);

            // 2. 获取精灵图配置
            SpriteConfig config = SPRITE_CONFIGS.get(spriteId);
            if (config == null && spriteId != null) {
                // 未知精灵图，提示用户手动输入
                config = handleUnknownSprite(spriteId);
                if (config == null) {
                    return null;
                }
            }

            // 3. 解码当前span
            if (config != null) {
                String position = extractBackgroundPosition(style);
                if (position != null) {
                    String digit = config.getMapping().get(position);
                    if (digit != null) {
                        price.append(digit);
                    } else {
                        // 找不到映射
                        return null;
                    }
                }
            }
        }

        return !price.isEmpty() ? price.toString() : null;
    }


    /**
     * 识别精灵图类型（从多个span数据中识别，返回第一个找到的）
     * 通过分析背景图片URL来确定使用哪种精灵图
     *
     * @param priceSpanData 价格span数据
     * @return 精灵图标识符，如果无法识别返回null
     */
    public static String identifySpriteType(List<Map<String, Object>> priceSpanData) {
        if (priceSpanData == null || priceSpanData.isEmpty()) {
            return null;
        }

        for (Map<String, Object> spanData : priceSpanData) {
            String spriteId = identifySpriteTypeFromSingle(spanData);
            if (spriteId != null) {
                return spriteId;
            }
        }

        return null; // 无法识别
    }

    /**
     * 从单个span数据识别精灵图类型
     *
     * @param spanData 单个span的样式数据
     * @return 精灵图标识符，如果无法识别返回null
     */
    private static String identifySpriteTypeFromSingle(Map<String, Object> spanData) {
        String style = (String) spanData.get("style");
        if (style == null) {
            return null;
        }

        // 提取背景图片URL
        String backgroundImage = extractBackgroundImage(style);
        if (backgroundImage != null) {
            // 根据URL中的关键字识别精灵图类型
            // 从URL中提取文件名（不包括后缀）作为标识符
            String fileName = backgroundImage.substring(backgroundImage.lastIndexOf('/') + 1);
            if (fileName.contains(".")) {
                return fileName.substring(0, fileName.lastIndexOf('.'));
            }
        }
        return null; // 无法识别
    }

    /**
     * 从CSS样式中提取背景图片URL
     *
     * @param style CSS样式字符串
     * @return 背景图片URL，未找到时返回null
     */
    private static String extractBackgroundImage(String style) {
        Pattern pattern = Pattern.compile("background-image:\\s*url\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(style);

        if (matcher.find()) {
            String url = matcher.group(1);
            // 清理引号
            return url.replaceAll("[\"']", "");
        }

        return null;
    }

    /**
     * 从CSS样式字符串中提取background-position的X坐标值
     * <p>
     * 解析类似 "background-position: -149.8px center;" 的样式字符串，
     * 提取其中的X坐标值用于数字映射。
     * </p>
     *
     * @param style CSS样式字符串
     * @return 提取到的X坐标值，未找到时返回null
     */
    private static String extractBackgroundPosition(String style) {
        // 匹配 background-position: -149.8px center 或 background-position: -149.8px 0px
        Pattern pattern = Pattern.compile("background-position:\\s*(-?\\d+(?:\\.\\d+)?px)");
        Matcher matcher = pattern.matcher(style);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * 验证解码后的价格是否合理
     * <p>
     * 检查价格范围是否在合理区间内，用于过滤解码错误的数据。
     * </p>
     *
     * @param priceStr 价格字符串
     * @return 价格合理返回true，否则返回false
     */
    public static boolean isValidPrice(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) {
            return false;
        }

        try {
            double price = Double.parseDouble(priceStr);
            // 租房合理价格范围：100-500000元/月
            return price >= 100 && price <= 500000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 增强映射表，添加兼容性映射
     * 例如："-107.0px" -> "1" 同时添加 "-107px" -> "1"
     */
    private static void enhanceMapping(SpriteConfig config) {
        Map<String, String> originalMapping = config.getMapping();
        Map<String, String> enhancedMapping = new HashMap<>(originalMapping);

        // 为所有以.0px结尾的key添加无小数版本
        for (Map.Entry<String, String> entry : originalMapping.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.endsWith(".0px")) {
                String intKey = key.replace(".0px", "px");
                enhancedMapping.put(intKey, value);
            }
        }

        config.setMapping(enhancedMapping);
    }

    /**
     * 扫描并加载资源目录下的所有精灵图配置文件
     */
    private static void scanAndLoadSpriteConfigs() {
        Resource[] classPathAllResources = Resources.getClassPathAllResources("META-INF/sprite/*.json");
        for (Resource resource : classPathAllResources) {
            try (InputStream is = resource.getInputStream()) {
                String jsonContent = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                SpriteConfig config = JSON.parseObject(jsonContent, SpriteConfig.class);

                // 动态生成兼容性映射
                enhanceMapping(config);

                SPRITE_CONFIGS.put(config.getIdentifier(), config);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 处理未知精灵图，提示用户手动输入数据
     */
    private static SpriteConfig handleUnknownSprite(String spriteId) {
        if (spriteId == null) {
            System.err.println("无法识别的精灵图类型！");
            return null;
        }

        // 检测是否为测试环境
        if (isTestEnvironment()) {
            return null;
        }

        System.out.println("发现未知精灵图: " + spriteId);
        System.out.println("请手动输入以下信息：");

        Scanner scanner = new Scanner(System.in);

        // 获取用户输入
        System.out.print("像素间隔 (px): ");
        double pixelInterval = Double.parseDouble(scanner.nextLine().trim());

        System.out.print("数字顺序 (10位数字按出现顺序): ");
        String digitOrder = scanner.nextLine().trim();

        // 生成映射关系
        Map<String, String> mapping = generateMapping(digitOrder, pixelInterval);

        // 创建配置对象
        SpriteConfig config = new SpriteConfig();
        config.setIdentifier(spriteId);
        config.setPixelInterval(pixelInterval);
        config.setDigitOrder(digitOrder);
        config.setMapping(mapping);

        // 缓存配置
        SPRITE_CONFIGS.put(spriteId, config);

        saveSpriteConfig(config);

        return config;
    }

    /**
     * 检测是否为测试环境
     *
     * @return 如果在测试环境中运行返回true，否则返回false
     */
    private static boolean isTestEnvironment() {
        try {
            // 检查是否存在JUnit类
            Class.forName("org.junit.jupiter.api.Test");

            // 检查调用堆栈中是否包含JUnit相关的类
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            for (StackTraceElement element : stackTrace) {
                String className = element.getClassName();
                if (className.contains("org.junit") ||
                        className.contains("org.testng")) {
                    return true;
                }
            }
            return false;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 根据数字顺序和像素间隔生成映射关系
     */
    private static Map<String, String> generateMapping(String digitOrder, double pixelInterval) {
        Map<String, String> mapping = new HashMap<>();

        for (int i = 0; i < digitOrder.length() && i < 10; i++) {
            String position = (i == 0) ? "0px" : String.format("%.1fpx", -i * pixelInterval);
            String digit = String.valueOf(digitOrder.charAt(i));
            mapping.put(position, digit);

            // 兼容整数格式
            if (position.endsWith(".0px")) {
                String intPosition = position.replace(".0px", "px");
                mapping.put(intPosition, digit);
            }
        }

        return mapping;
    }

    /**
     * 保存精灵图配置到文件
     */
    private static void saveSpriteConfig(SpriteConfig config) {
        try {
            // 获取资源目录的物理路径
            String userDir = System.getProperty("user.dir");
            Path spritePath = Paths.get(userDir, "src", "main", "resources", "META-INF", "sprite");

            // 确保目录存在
            if (!Files.exists(spritePath)) {
                Files.createDirectories(spritePath);
            }

            // 保存JSON文件
            Path configFile = spritePath.resolve(config.getIdentifier() + ".json");
            String jsonContent = JSON.toJSONString(config);
            Files.writeString(configFile, jsonContent);

            System.out.println("精灵图配置已保存到: " + configFile);
        } catch (IOException e) {
            System.err.println("保存精灵图配置失败: " + e.getMessage());
        }
    }
}