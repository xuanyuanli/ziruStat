package cn.xuanyuanli.rentradar.utils;

import cn.xuanyuanli.rentradar.service.SimpleOCRService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
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
     * 位置到数字的映射表
     * 根据CSS精灵图的background-position值映射对应的数字
     */
    private static final Map<String, String> POSITION_TO_DIGIT = new HashMap<>();

    /**
     * 精灵图映射表：精灵图文件名/哈希 -> 位置映射配置
     */
    private static final Map<String, Map<String, String>> SPRITE_MAPPINGS = new HashMap<>();

    /**
     * 已知的精灵图文件标识符
     */
    public enum SpriteImageType {
        SPRITE_V1("a9da4f199beb8d74bffa9500762fd7b7", "第一版精灵图"),
        SPRITE_V2("f4c1f82540f8d287aa53492a44f5819b", "第二版精灵图"),
        SPRITE_RED("img_pricenumber_list_red", "红色版精灵图"),
        SPRITE_NEW("c4b718a0002eb143ea3484b373071495", "新版精灵图");

        private final String identifier;
        private final String description;

        SpriteImageType(String identifier, String description) {
            this.identifier = identifier;
            this.description = description;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getDescription() {
            return description;
        }
    }

    static {
        // 初始化各种精灵图的映射关系
        initializeSpriteV1Mapping();
        initializeSpriteV2Mapping();
        initializeSpriteRedMapping();
        initializeSpriteNewMapping();

        // 默认使用第一版精灵图映射作为后备
        POSITION_TO_DIGIT.putAll(SPRITE_MAPPINGS.get(SpriteImageType.SPRITE_V1.getIdentifier()));
    }

    /**
     * 初始化第一版精灵图映射 (a9da4f199beb8d74bffa9500762fd7b7.png)
     * 数字排列顺序：8、6、7、0、4、1、5、9、2、3
     */
    private static void initializeSpriteV1Mapping() {
        Map<String, String> v1Mapping = new HashMap<>();

        // 21.4px间隔布局
        v1Mapping.put("0px", "8");        // 第1个数字：8
        v1Mapping.put("-21.4px", "6");    // 第2个数字：6  
        v1Mapping.put("-42.8px", "7");    // 第3个数字：7
        v1Mapping.put("-64.2px", "0");    // 第4个数字：0
        v1Mapping.put("-85.6px", "4");    // 第5个数字：4
        v1Mapping.put("-107.0px", "1");   // 第6个数字：1
        v1Mapping.put("-107px", "1");   // 第6个数字：1
        v1Mapping.put("-128.4px", "5");   // 第7个数字：5
        v1Mapping.put("-149.8px", "9");   // 第8个数字：9
        v1Mapping.put("-171.2px", "2");   // 第9个数字：2
        v1Mapping.put("-192.6px", "3");   // 第10个数字：3

        SPRITE_MAPPINGS.put(SpriteImageType.SPRITE_V1.getIdentifier(), v1Mapping);
    }

    /**
     * 初始化第二版精灵图映射 (f4c1f82540f8d287aa53492a44f5819b.png)
     * 数字排列顺序：4、9、7、8、1、2、3、6、0、5
     */
    private static void initializeSpriteV2Mapping() {
        Map<String, String> v2Mapping = new HashMap<>();

        // 21.4px间隔布局，数字排列顺序：4978123605
        v2Mapping.put("0px", "4");        // 第1个数字：4
        v2Mapping.put("-21.4px", "9");    // 第2个数字：9
        v2Mapping.put("-42.8px", "7");    // 第3个数字：7  
        v2Mapping.put("-64.2px", "8");    // 第4个数字：8
        v2Mapping.put("-85.6px", "1");    // 第5个数字：1
        v2Mapping.put("-107.0px", "2");   // 第6个数字：2
        v2Mapping.put("-107px", "2");   // 第6个数字：2
        v2Mapping.put("-128.4px", "3");   // 第7个数字：3
        v2Mapping.put("-149.8px", "6");   // 第8个数字：6
        v2Mapping.put("-171.2px", "0");   // 第9个数字：0
        v2Mapping.put("-192.6px", "5");   // 第10个数字：5

        SPRITE_MAPPINGS.put(SpriteImageType.SPRITE_V2.getIdentifier(), v2Mapping);
    }

    /**
     * 初始化红色版精灵图映射 (img_pricenumber_list_red.png)
     * 数字排列顺序：8、6、5、2、0、3、9、1、4、7
     */
    private static void initializeSpriteRedMapping() {
        Map<String, String> redMapping = new HashMap<>();

        // 20px间隔布局，数字排列顺序：8652039147
        redMapping.put("0px", "8");       // 第1个数字：8
        redMapping.put("-20px", "6");     // 第2个数字：6
        redMapping.put("-40px", "5");     // 第3个数字：5
        redMapping.put("-60px", "2");     // 第4个数字：2  
        redMapping.put("-80px", "0");     // 第5个数字：0
        redMapping.put("-100px", "3");    // 第6个数字：3
        redMapping.put("-120px", "9");    // 第7个数字：9
        redMapping.put("-140px", "1");    // 第8个数字：1
        redMapping.put("-160px", "4");    // 第9个数字：4
        redMapping.put("-180px", "7");    // 第10个数字：7

        SPRITE_MAPPINGS.put(SpriteImageType.SPRITE_RED.getIdentifier(), redMapping);
    }

    /**
     * 初始化新版精灵图映射 (c4b718a0002eb143ea3484b373071495.png)
     * 数字排列顺序：9、3、1、0、8、6、7、5、4、2
     */
    private static void initializeSpriteNewMapping() {
        Map<String, String> newMapping = new HashMap<>();

        // 21.4px间隔布局，数字排列顺序：9310867542
        newMapping.put("0px", "9");       // 第1个数字：9
        newMapping.put("-21.4px", "3");   // 第2个数字：3
        newMapping.put("-42.8px", "1");   // 第3个数字：1
        newMapping.put("-64.2px", "0");   // 第4个数字：0  
        newMapping.put("-85.6px", "8");   // 第5个数字：8
        newMapping.put("-107.0px", "6");  // 第6个数字：6
        newMapping.put("-107px", "6");  // 第6个数字：6
        newMapping.put("-128.4px", "7");  // 第7个数字：7
        newMapping.put("-149.8px", "5");  // 第8个数字：5
        newMapping.put("-171.2px", "4");  // 第9个数字：4
        newMapping.put("-192.6px", "2");  // 第10个数字：2

        SPRITE_MAPPINGS.put(SpriteImageType.SPRITE_NEW.getIdentifier(), newMapping);
    }

    /**
     * 解码精灵图价格（支持OCR降级）
     * <p>
     * 优先使用精灵图映射解码，失败时降级到OCR识别
     * </p>
     *
     * @param priceSpanData 价格span元素的样式数据列表，包含style属性
     * @param screenshot    价格区域截图，用于OCR降级
     * @return 解码后的价格字符串，解码失败时返回null
     */
    public static String decodePrice(List<Map<String, Object>> priceSpanData, Supplier<byte[]> screenshot) {
        // 1. 尝试精灵图映射解码
        String result = decodePriceBySpriteMapping(priceSpanData);
        if (isValidPrice(result)) {
            return result;
        }

        // 2. 降级到OCR识别
        if (screenshot != null) {
            String ocrResult = SimpleOCRService.recognizeDigits(screenshot.get());
            if (isValidPrice(ocrResult)) {
                return ocrResult;
            }
        }

        return null;
    }

    /**
     * 解码精灵图价格（自动识别精灵图类型）
     * <p>
     * 从包含价格数字的span元素列表中提取background-position值，
     * 自动识别精灵图类型并使用对应的映射表解码价格。
     * </p>
     *
     * @param priceSpanData 价格span元素的样式数据列表，包含style属性
     * @return 解码后的价格字符串，解码失败时返回null
     */
    public static String decodePrice(List<Map<String, Object>> priceSpanData) {
        return decodePrice(priceSpanData, null);
    }

    private static String decodePriceBySpriteMapping(List<Map<String, Object>> priceSpanData) {
        if (priceSpanData == null || priceSpanData.isEmpty()) {
            return null;
        }

        // 1. 识别精灵图类型
        SpriteImageType spriteType = identifySpriteType(priceSpanData);

        // 2. 使用对应的映射表解码
        Map<String, String> mapping;
        if (spriteType != null) {
            // 使用识别到的精灵图映射
            mapping = SPRITE_MAPPINGS.get(spriteType.getIdentifier());
        } else {
            // 无法识别精灵图类型时，使用默认映射表（V1版本）作为后备
            mapping = POSITION_TO_DIGIT;
        }
        return decodePriceWithMapping(priceSpanData, mapping);
    }

    /**
     * 识别精灵图类型
     * 通过分析背景图片URL来确定使用哪种精灵图
     *
     * @param priceSpanData 价格span数据
     * @return 精灵图类型，如果无法识别返回null
     */
    public static SpriteImageType identifySpriteType(List<Map<String, Object>> priceSpanData) {
        if (priceSpanData == null || priceSpanData.isEmpty()) {
            return null;
        }

        for (Map<String, Object> spanData : priceSpanData) {
            String style = (String) spanData.get("style");
            if (style == null) {
                continue;
            }

            // 提取背景图片URL
            String backgroundImage = extractBackgroundImage(style);
            if (backgroundImage != null) {
                // 根据URL中的关键字识别精灵图类型
                for (SpriteImageType type : SpriteImageType.values()) {
                    if (backgroundImage.contains(type.getIdentifier())) {
                        return type;
                    }
                }
            }
        }

        return null; // 无法识别
    }

    /**
     * 使用指定映射表解码价格
     *
     * @param priceSpanData 价格span数据
     * @param mapping       位置到数字的映射表
     * @return 解码后的价格，失败时返回null
     */
    private static String decodePriceWithMapping(List<Map<String, Object>> priceSpanData, Map<String, String> mapping) {
        StringBuilder price = new StringBuilder();

        for (Map<String, Object> spanData : priceSpanData) {
            String style = (String) spanData.get("style");
            if (style == null) {
                continue;
            }

            String position = extractBackgroundPosition(style);
            if (position != null) {
                String digit = mapping.get(position);
                if (digit != null) {
                    price.append(digit);
                } else {
                    // 如果找不到映射，记录未知的position值用于调试
                    return null;
                }
            }
        }

        return !price.isEmpty() ? price.toString() : null;
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
            // 北京租房合理价格范围：500-500000元/月
            return price >= 500 && price <= 500000;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 更新位置映射表
     * <p>
     * 运行时动态添加新的位置映射关系，用于适应网站的更新。
     * </p>
     *
     * @param position background-position值
     * @param digit    对应的数字
     */
    public static void updateMapping(String position, String digit) {
        POSITION_TO_DIGIT.put(position, digit);
    }

    /**
     * 获取当前的位置映射表（用于调试）
     *
     * @return 位置到数字的映射表副本
     */
    public static Map<String, String> getMappingTable() {
        return new HashMap<>(POSITION_TO_DIGIT);
    }
}