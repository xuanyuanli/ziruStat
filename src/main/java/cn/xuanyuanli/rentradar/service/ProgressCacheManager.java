package cn.xuanyuanli.rentradar.service;

import cn.xuanyuanli.rentradar.config.AppConfig;
import cn.xuanyuanli.rentradar.model.Subway;
import cn.xuanyuanli.rentradar.utils.JsonUtils;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 进度缓存管理器<br>
 * 用于管理价格获取的断点续传功能<br>
 * 支持逐个站点保存进度，程序异常退出后可从断点处继续
 *
 * @author xuanyuanli
 */
public class ProgressCacheManager {
    
    private final String progressCacheFile;
    
    public ProgressCacheManager() {
        this.progressCacheFile = AppConfig.getInstance().getDataDir() + "/subway-prices-progress.json";
    }

    /**
     * 价格获取进度缓存数据结构
     */
    public static class PriceProgress {
        private Set<String> completedStations = new HashSet<>();
        private Map<String, StationPrice> priceData = new HashMap<>();
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastProcessedTime = LocalDateTime.now();
        
        public Set<String> getCompletedStations() {
            return completedStations;
        }
        
        public void setCompletedStations(Set<String> completedStations) {
            this.completedStations = completedStations;
        }
        
        public Map<String, StationPrice> getPriceData() {
            return priceData;
        }
        
        public void setPriceData(Map<String, StationPrice> priceData) {
            this.priceData = priceData;
        }
        
        public LocalDateTime getLastProcessedTime() {
            return lastProcessedTime;
        }
        
        public void setLastProcessedTime(LocalDateTime lastProcessedTime) {
            this.lastProcessedTime = lastProcessedTime;
        }
    }
    
    /**
     * 单个站点价格数据
     */
    public static class StationPrice {
        private String stationKey;  // 使用 name+lineName 作为唯一标识
        private String name;
        private String displayName;
        private double price;
        private String url;
        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime processedAt = LocalDateTime.now();
        
        public StationPrice() {}
        
        public StationPrice(Subway station, double price) {
            this.stationKey = generateStationKey(station);
            this.name = station.getName();
            this.displayName = station.getDisplayName();
            this.price = price;
            this.url = station.getUrl();
            this.processedAt = LocalDateTime.now();
        }
        
        // Getters and Setters
        public String getStationKey() { return stationKey; }
        public void setStationKey(String stationKey) { this.stationKey = stationKey; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
        
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        
        public LocalDateTime getProcessedAt() { return processedAt; }
        public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    }
    
    /**
     * 生成站点唯一标识符
     */
    public static String generateStationKey(Subway station) {
        return station.getLineName() + "_" + station.getName();
    }
    
    /**
     * 读取进度缓存（检查有效期）
     */
    public PriceProgress loadProgress() {
        File file = new File(progressCacheFile);
        if (!file.exists()) {
            return new PriceProgress();
        }
        
        try {
            String jsonContent = java.nio.file.Files.readString(java.nio.file.Paths.get(progressCacheFile));
            PriceProgress progress = JsonUtils.parseObject(jsonContent, PriceProgress.class);
            
            // 检查进度缓存是否过期（与价格数据缓存一致）
            int expireDays = AppConfig.getInstance().getPricesCacheExpireDays();
            if (progress != null && expireDays > 0 && progress.getLastProcessedTime() != null) {
                LocalDateTime expiryTime = progress.getLastProcessedTime().plusDays(expireDays);
                if (LocalDateTime.now().isAfter(expiryTime)) {
                    System.out.println("进度缓存已过期，将重新开始");
                    return new PriceProgress();
                }
            }

            if (progress != null) {
                System.out.println("发现进度缓存：已处理 " + progress.getCompletedStations().size() +
                                 " 个站点，上次更新时间：" + progress.getLastProcessedTime());
            }
            return progress;
        } catch (Exception e) {
            System.out.println("读取进度缓存失败，将重新开始：" + e.getMessage());
            return new PriceProgress();
        }
    }
    
    /**
     * 保存单个站点价格到进度缓存
     */
    public void saveStationProgress(Subway station, double price, PriceProgress progress) {
        String stationKey = generateStationKey(station);
        
        // 更新进度数据
        progress.getCompletedStations().add(stationKey);
        progress.getPriceData().put(stationKey, new StationPrice(station, price));
        progress.setLastProcessedTime(LocalDateTime.now());
        
        // 立即保存到文件
        try {
            String jsonContent = JsonUtils.toJsonString(progress);
            Path path = Paths.get(progressCacheFile);
            java.nio.file.Files.createDirectories(path.getParent());
            java.nio.file.Files.writeString(path, jsonContent);
        } catch (Exception e) {
            System.err.println("保存进度缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 将进度缓存转换为地铁站列表
     */
    public List<Subway> convertToSubwayList(PriceProgress progress, List<Subway> originalStations) {
        List<Subway> result = new ArrayList<>();
        Map<String, Subway> stationMap = new HashMap<>();
        
        // 创建原始站点映射
        for (Subway station : originalStations) {
            stationMap.put(generateStationKey(station), station);
        }
        
        // 从进度缓存重建带价格的站点列表
        for (Map.Entry<String, StationPrice> entry : progress.getPriceData().entrySet()) {
            String stationKey = entry.getKey();
            StationPrice priceData = entry.getValue();
            
            Subway originalStation = stationMap.get(stationKey);
            if (originalStation != null) {
                // 创建新的Subway对象并设置价格
                Subway stationWithPrice = new Subway();
                stationWithPrice.setName(originalStation.getName());
                stationWithPrice.setLineName(originalStation.getLineName());
                stationWithPrice.setUrl(originalStation.getUrl());
                stationWithPrice.setLongitude(originalStation.getLongitude());
                stationWithPrice.setLatitude(originalStation.getLatitude());
                stationWithPrice.setSquareMeterOfPrice(priceData.getPrice());
                
                result.add(stationWithPrice);
            }
        }
        
        return result;
    }
    
    /**
     * 检查站点是否已完成处理
     */
    public boolean isStationCompleted(Subway station, PriceProgress progress) {
        return progress.getCompletedStations().contains(generateStationKey(station));
    }
    
    /**
     * 清除进度缓存
     */
    public void clearProgress() {
        File file = new File(progressCacheFile);
        if (file.exists()) {
            file.delete();
            System.out.println("已清除进度缓存");
        }
    }
    
    /**
     * 获取进度统计信息
     */
    public String getProgressInfo(PriceProgress progress, int totalStations) {
        int completed = progress.getCompletedStations().size();
        double percentage = totalStations > 0 ? (completed * 100.0 / totalStations) : 0;
        return String.format("进度：%d/%d (%.1f%%)", completed, totalStations, percentage);
    }
}