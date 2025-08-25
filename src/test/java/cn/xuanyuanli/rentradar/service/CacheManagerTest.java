package cn.xuanyuanli.rentradar.service;

import cn.xuanyuanli.rentradar.config.AppConfig;
import cn.xuanyuanli.rentradar.model.Subway;
import cn.xuanyuanli.rentradar.utils.FileUtils;
import cn.xuanyuanli.rentradar.utils.JsonUtils;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CacheManagerTest {

    @Test
    void testGetCachedData_Basic() throws Exception {
        // 基本的缓存代理功能测试
        try (MockedStatic<AppConfig> mockAppConfig = Mockito.mockStatic(AppConfig.class);
             MockedStatic<FileUtils> mockFileUtils = Mockito.mockStatic(FileUtils.class);
             MockedStatic<JsonUtils> mockJsonUtils = Mockito.mockStatic(JsonUtils.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            mockAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            when(mockConfig.isCacheEnabled()).thenReturn(true);
            
            String cacheFile = "test-cache.json";
            List<Subway> testData = Arrays.asList(new Subway());
            String jsonData = "[{\"name\":\"test\"}]";
            
            // 模拟缓存不存在的情况
            mockFileUtils.when(() -> FileUtils.exists(cacheFile)).thenReturn(false);
            mockJsonUtils.when(() -> JsonUtils.toJsonString(testData)).thenReturn(jsonData);
            
            Supplier<List<Subway>> dataSupplier = () -> testData;
            
            CacheManager cacheManager = new CacheManager();
            List<Subway> result = cacheManager.getCachedData(cacheFile, 7, dataSupplier, Subway.class);
            
            assertEquals(testData, result);
            mockFileUtils.verify(() -> FileUtils.writeToFile(cacheFile, jsonData));
        }
    }
    
    @Test
    void testGetCachedDataWithDependency_Basic() throws Exception {
        // 测试依赖缓存功能
        try (MockedStatic<AppConfig> mockAppConfig = Mockito.mockStatic(AppConfig.class);
             MockedStatic<FileUtils> mockFileUtils = Mockito.mockStatic(FileUtils.class);
             MockedStatic<JsonUtils> mockJsonUtils = Mockito.mockStatic(JsonUtils.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            mockAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            when(mockConfig.isCacheEnabled()).thenReturn(true);
            
            String cacheFile = "test-locations.json";
            String dependentFile = "test-stations.json";
            List<Subway> testData = Arrays.asList(new Subway());
            String jsonData = "[{\"name\":\"test\"}]";
            
            // 模拟依赖关系无效的情况
            mockFileUtils.when(() -> FileUtils.exists(cacheFile)).thenReturn(false);
            mockJsonUtils.when(() -> JsonUtils.toJsonString(testData)).thenReturn(jsonData);
            
            Supplier<List<Subway>> dataSupplier = () -> testData;
            
            CacheManager cacheManager = new CacheManager();
            List<Subway> result = cacheManager.getCachedDataWithDependency(cacheFile, dependentFile, dataSupplier, Subway.class);
            
            assertEquals(testData, result);
            mockFileUtils.verify(() -> FileUtils.writeToFile(cacheFile, jsonData));
        }
    }
    
    @Test
    void testCacheDisabled() throws Exception {
        // 测试缓存禁用时的行为
        try (MockedStatic<AppConfig> mockAppConfig = Mockito.mockStatic(AppConfig.class)) {
            
            AppConfig mockConfig = mock(AppConfig.class);
            mockAppConfig.when(AppConfig::getInstance).thenReturn(mockConfig);
            when(mockConfig.isCacheEnabled()).thenReturn(false);
            
            List<Subway> testData = Arrays.asList(new Subway());
            Supplier<List<Subway>> dataSupplier = () -> testData;
            
            CacheManager cacheManager = new CacheManager();
            List<Subway> result = cacheManager.getCachedData("test.json", 7, dataSupplier, Subway.class);
            
            assertEquals(testData, result);
        }
    }
}