package cn.xuanyuanli.rentradar.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author xuanyuanli
 */
public class AppConfig {
    private static final String CONFIG_FILE = "application.properties";
    private static volatile AppConfig instance;
    private Properties properties;

    private AppConfig() {
        loadProperties();
    }

    public static AppConfig getInstance() {
        if (instance == null) {
            synchronized (AppConfig.class) {
                if (instance == null) {
                    instance = new AppConfig();
                }
            }
        }
        return instance;
    }

    private void loadProperties() {
        properties = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                if (properties != null) {
                    properties.load(input);
                }
            }
        } catch (IOException e) {
            System.err.println("加载配置文件失败: " + e.getMessage());
        }
    }

    // 高德地图配置
    public String getGaodeApiKey() {
        return getProperty("gaode.api.key", "");
    }

    public String getGaodeApiPrivateKey() {
        return getProperty("gaode.api.privateKey", "");
    }

    public String getGaodeCity() {
        return getProperty("gaode.api.city", "010");
    }

    // 爬虫配置
    public int getCrawlerMaxRetry() {
        return getIntProperty("crawler.retry.maxAttempts", 3);
    }

    public int getCrawlerDelay() {
        return getIntProperty("crawler.delay.betweenRequests", 2000);
    }

    public int getCrawlerTimeout() {
        return getIntProperty("crawler.timeout.pageLoad", 30000);
    }

    // 数据配置
    public int getDefaultSquareMeter() {
        return getIntProperty("data.defaultSquareMeter", 10);
    }

    public boolean isCacheEnabled() {
        return getBooleanProperty();
    }

    public int getCacheExpireDays() {
        return getIntProperty("data.cache.expireDays", 7);
    }

    public String getBaseDir() {
        return getProperty("data.output.baseDir", "build");
    }

    public String getDataDir() {
        return getProperty("data.output.dataDir", "build/data");
    }

    public String getOutputDir() {
        return getProperty("data.output.outputDir", "build/output");
    }

    public String getPriceJsonFile() {
        return getProperty("data.output.priceJsonFile", "build/data/subway.json");
    }

    public String getLocationJsonFile() {
        return getProperty("data.output.locationJsonFile", "build/data/subwayLocation.json");
    }

    public String getHtmlOutputFile() {
        return getProperty("data.output.htmlFile", "build/output/show.html");
    }

    public String getMapTemplate() {
        return getProperty("data.template.mapTemplate", "templates/map-template.html");
    }

    // 价格分析配置
    public double getMinReasonablePrice() {
        return getDoubleProperty("price.analysis.minReasonablePrice", 10.0);
    }

    public double getMaxReasonablePrice() {
        return getDoubleProperty("price.analysis.maxReasonablePrice", 1000.0);
    }

    // 工具方法
    private String getProperty(String key, String defaultValue) {
        String value = properties.getProperty(key, defaultValue);
        return resolvePlaceholders(value);
    }

    /**
     * 解析配置中的占位符 ${key}
     */
    private String resolvePlaceholders(String value) {
        if (value == null || !value.contains("${")) {
            return value;
        }

        String result = value;
        // 防止无限循环
        int maxIterations = 10;

        for (int i = 0; i < maxIterations && result.contains("${"); i++) {
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\\{([^}]+)}");
            java.util.regex.Matcher matcher = pattern.matcher(result);

            StringBuilder sb = new StringBuilder();
            int lastEnd = 0;
            boolean found = false;

            while (matcher.find()) {
                String key = matcher.group(1);
                String replacement = properties.getProperty(key, "");

                sb.append(result, lastEnd, matcher.start());
                sb.append(replacement);
                lastEnd = matcher.end();
                found = true;
            }

            if (found) {
                sb.append(result.substring(lastEnd));
                result = sb.toString();
            } else {
                break;
            }
        }

        return result;
    }

    private int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private double getDoubleProperty(String key, double defaultValue) {
        try {
            return Double.parseDouble(getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private boolean getBooleanProperty() {
        return Boolean.parseBoolean(getProperty("data.cache.enabled", String.valueOf(true)));
    }
}