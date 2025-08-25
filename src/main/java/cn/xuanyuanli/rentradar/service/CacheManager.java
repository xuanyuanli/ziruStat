package cn.xuanyuanli.rentradar.service;

import cn.xuanyuanli.rentradar.config.AppConfig;
import cn.xuanyuanli.rentradar.utils.FileUtils;
import cn.xuanyuanli.rentradar.utils.JsonUtils;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

/**
 * 缓存管理器，统一管理三级缓存策略
 * 
 * @author xuanyuanli
 */
public class CacheManager {
    
    private final AppConfig config;
    
    public CacheManager() {
        this.config = AppConfig.getInstance();
    }


    /**
     * 通用缓存代理方法
     * 
     * @param cacheFile 缓存文件路径
     * @param expireDays 缓存过期天数
     * @param dataSupplier 数据获取逻辑
     * @param clazz 数据类型
     * @param <T> 数据泛型类型
     * @return 缓存或新获取的数据
     */
    public <T> List<T> getCachedData(
            String cacheFile,
            int expireDays,
            Supplier<List<T>> dataSupplier,
            Class<T> clazz) throws Exception {
        
        // 检查缓存是否有效
        if (config.isCacheEnabled() && isCacheValid(cacheFile, expireDays)) {
            System.out.println("从缓存加载数据: " + cacheFile);
            String json = FileUtils.readFromFile(cacheFile);
            List<T> cachedData = JsonUtils.parseArray(json, clazz);
            if (cachedData != null && !cachedData.isEmpty()) {
                return cachedData;
            }
        }

        // 获取新数据
        List<T> data = dataSupplier.get();

        // 缓存数据
        if (config.isCacheEnabled() && data != null && !data.isEmpty()) {
            String json = JsonUtils.toJsonString(data);
            FileUtils.writeToFile(cacheFile, json);
            System.out.println("数据已缓存到: " + cacheFile);
        }

        return data;
    }

    /**
     * 支持位置缓存依赖检查的特殊缓存代理方法
     */
    public <T> List<T> getCachedDataWithDependency(
            String cacheFile,
            String dependentFile,
            Supplier<List<T>> dataSupplier,
            Class<T> clazz) throws Exception {
        
        // 检查依赖缓存是否有效
        if (config.isCacheEnabled() && isDependentCacheValid(cacheFile, dependentFile)) {
            System.out.println("从缓存加载数据: " + cacheFile);
            String json = FileUtils.readFromFile(cacheFile);
            List<T> cachedData = JsonUtils.parseArray(json, clazz);
            if (cachedData != null && !cachedData.isEmpty()) {
                return cachedData;
            }
        }

        // 获取新数据
        List<T> data = dataSupplier.get();

        // 缓存数据
        if (config.isCacheEnabled() && data != null && !data.isEmpty()) {
            String json = JsonUtils.toJsonString(data);
            FileUtils.writeToFile(cacheFile, json);
            System.out.println("数据已缓存到: " + cacheFile);
        }

        return data;
    }

    /**
     * 检查缓存是否有效
     */
    private boolean isCacheValid(String cacheFile, int expireDays) throws IOException {
        return FileUtils.exists(cacheFile) && 
               (expireDays == -1 || !FileUtils.isCacheExpired(cacheFile, expireDays));
    }

    /**
     * 检查依赖缓存是否有效（用于locations缓存）
     */
    private boolean isDependentCacheValid(String cacheFile, String dependentFile) {
        try {
            // 缓存文件必须存在
            if (!FileUtils.exists(cacheFile)) {
                return false;
            }
            
            // 依赖文件必须存在
            if (!FileUtils.exists(dependentFile)) {
                return false;
            }
            
            // 检查缓存文件是否比依赖文件更新
            long cacheTime = FileUtils.getLastModifiedTime(cacheFile);
            long dependentTime = FileUtils.getLastModifiedTime(dependentFile);
            return cacheTime >= dependentTime;
        } catch (IOException e) {
            return false;
        }
    }
}