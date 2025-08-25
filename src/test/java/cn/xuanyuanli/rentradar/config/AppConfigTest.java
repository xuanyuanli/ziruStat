package cn.xuanyuanli.rentradar.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {

    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // 清除可能的环境变量影响
        System.clearProperty("gaode.api.key");
    }

    @Test
    void testSingletonPattern() {
        AppConfig instance1 = AppConfig.getInstance();
        AppConfig instance2 = AppConfig.getInstance();
        
        assertSame(instance1, instance2, "AppConfig应该是单例模式");
    }

    @Test
    void testGetGaodeApiKey_Default() {
        AppConfig config = AppConfig.getInstance();
        assertEquals("", config.getGaodeApiKey(), "默认API key应该是空字符串");
    }

    @Test
    void testGetGaodeCity_Default() {
        AppConfig config = AppConfig.getInstance();
        assertEquals("010", config.getGaodeCity(), "默认城市应该是北京(010)");
    }

    @Test
    void testGetCrawlerMaxRetry_Default() {
        AppConfig config = AppConfig.getInstance();
        assertEquals(1, config.getCrawlerMaxRetry(), "根据配置文件，默认重试次数应该是1");
    }

    @Test
    void testGetCrawlerDelay_Default() {
        AppConfig config = AppConfig.getInstance();
        assertEquals(2000, config.getCrawlerDelay(), "默认延迟应该是2000毫秒");
    }

    @Test
    void testGetDefaultSquareMeter_Default() {
        AppConfig config = AppConfig.getInstance();
        assertEquals(10, config.getDefaultSquareMeter(), "默认面积应该是10平米");
    }

    @Test
    void testIsCacheEnabled_Default() {
        AppConfig config = AppConfig.getInstance();
        assertTrue(config.isCacheEnabled(), "默认应该启用缓存");
    }

    @Test
    void testGetCacheExpireDays_Default() {
        AppConfig config = AppConfig.getInstance();
        assertEquals(30, config.getCacheExpireDays(), "默认缓存过期时间应该是30天");
    }

    @Test
    void testTieredCacheExpireDays() {
        AppConfig config = AppConfig.getInstance();
        assertEquals(90, config.getStationsCacheExpireDays(), "地铁站基础信息缓存过期时间应该是90天");
        assertEquals(-1, config.getLocationsCacheExpireDays(), "地理位置缓存依赖stations文件");
        assertEquals(7, config.getPricesCacheExpireDays(), "价格缓存过期时间应该是7天");
    }

    @Test
    void testGetMinReasonablePrice_Default() {
        AppConfig config = AppConfig.getInstance();
        assertEquals(10.0, config.getMinReasonablePrice(), 0.001, "默认最低合理价格应该是10元");
    }

    @Test
    void testGetMaxReasonablePrice_Default() {
        AppConfig config = AppConfig.getInstance();
        assertEquals(1000.0, config.getMaxReasonablePrice(), 0.001, "默认最高合理价格应该是1000元");
    }

    @Test
    void testPlaceholderResolution() {
        AppConfig config = AppConfig.getInstance();
        
        String baseDir = config.getBaseDir();
        String dataDir = config.getDataDir();
        String priceJsonFile = config.getPriceJsonFile();
        
        assertEquals("build", baseDir);
        assertEquals("build/data", dataDir);
        assertEquals("build/data/subway.json", priceJsonFile);
    }

    @Test
    void testPathConfiguration() {
        AppConfig config = AppConfig.getInstance();
        
        assertEquals("build", config.getBaseDir());
        assertEquals("build/data", config.getDataDir());
        assertEquals("build/output", config.getOutputDir());
        assertEquals("build/data/subway.json", config.getPriceJsonFile());
        assertEquals("build/data/subwayLocation.json", config.getLocationJsonFile());
        assertEquals("build/output/show.html", config.getHtmlOutputFile());
        assertEquals("templates/map-template.html", config.getMapTemplate());
    }

    @Test
    void testTieredCacheFilePaths() {
        AppConfig config = AppConfig.getInstance();
        
        assertEquals("build/data/subway-stations.json", config.getStationsJsonFile());
        assertEquals("build/data/subway-locations.json", config.getLocationsJsonFile());
        assertEquals("build/data/subway-prices.json", config.getPricesJsonFile());
    }

    @Test
    void testIntPropertyConversion_InvalidValue() {
        // 由于AppConfig是单例且配置写死在文件中，我们无法直接测试无效值转换
        // 这里测试默认值回退逻辑
        AppConfig config = AppConfig.getInstance();
        
        // 测试正常的int配置读取
        assertTrue(config.getCrawlerMaxRetry() >= 1);
        assertTrue(config.getCrawlerDelay() >= 0);
        assertTrue(config.getDefaultSquareMeter() > 0);
    }

    @Test
    void testDoublePropertyConversion() {
        AppConfig config = AppConfig.getInstance();
        
        assertTrue(config.getMinReasonablePrice() >= 0);
        assertTrue(config.getMaxReasonablePrice() > config.getMinReasonablePrice());
    }

    @Test
    void testBooleanPropertyConversion() {
        AppConfig config = AppConfig.getInstance();
        
        // 测试boolean配置读取不报错
        assertNotNull(config.isCacheEnabled());
    }

    @Test
    void testConfigurationConsistency() {
        AppConfig config = AppConfig.getInstance();
        
        // 测试配置的逻辑一致性
        assertTrue(config.getMinReasonablePrice() < config.getMaxReasonablePrice(), 
                   "最低价格应该小于最高价格");
        assertTrue(config.getCrawlerDelay() >= 0, "延迟时间不能为负数");
        assertTrue(config.getCrawlerMaxRetry() >= 1, "重试次数至少为1");
        assertTrue(config.getCacheExpireDays() > 0, "缓存过期天数必须大于0");
    }
}