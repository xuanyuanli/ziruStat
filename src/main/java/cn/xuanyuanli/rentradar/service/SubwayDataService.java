package cn.xuanyuanli.rentradar.service;

import cn.xuanyuanli.rentradar.config.AppConfig;
import cn.xuanyuanli.rentradar.crawler.ZiroomCrawler;
import cn.xuanyuanli.rentradar.exception.CrawlerException;
import cn.xuanyuanli.rentradar.exception.LocationServiceException;
import cn.xuanyuanli.rentradar.model.POI;
import cn.xuanyuanli.rentradar.model.Subway;
import cn.xuanyuanli.rentradar.utils.FileUtils;
import cn.xuanyuanli.rentradar.utils.JsonUtils;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class SubwayDataService {
    
    private final AppConfig config;
    private final ZiroomCrawler crawler;
    private final LocationService locationService;
    
    public SubwayDataService(ZiroomCrawler crawler, LocationService locationService) {
        this.config = AppConfig.getInstance();
        this.crawler = crawler;
        this.locationService = locationService;
    }
    
    public List<Subway> collectAllSubwayData() throws CrawlerException, IOException {
        System.out.println("开始收集地铁数据...");
        
        // 1. 获取价格数据
        List<Subway> subwaysWithPrice = getPriceData();
        
        // 2. 获取地理位置数据  
        List<Subway> subwaysWithLocation = getLocationData(subwaysWithPrice);
        
        System.out.println("地铁数据收集完成，共 " + subwaysWithLocation.size() + " 个站点");
        return subwaysWithLocation;
    }
    
    private List<Subway> getPriceData() throws CrawlerException, IOException {
        String priceFile = config.getPriceJsonFile();
        
        if (config.isCacheEnabled() && FileUtils.exists(priceFile)) {
            System.out.println("从缓存加载价格数据: " + priceFile);
            String json = FileUtils.readFromFile(priceFile);
            List<Subway> subways = JsonUtils.parseArray(json, Subway.class);
            if (subways != null && !subways.isEmpty()) {
                return subways;
            }
        }
        
        System.out.println("开始抓取价格数据...");
        List<Subway> subways = crawler.getSubwayStations();
        
        // 批量获取价格数据
        for (Subway subway : subways) {
            try {
                double avgPrice = crawler.getAveragePrice(subway.getUrl());
                subway.setSquareMeterOfPrice(avgPrice);
                System.out.println("获取价格: " + subway.getDisplayName() + " = " + avgPrice + " 元/㎡");
                
                // 添加延迟避免请求过于频繁
                Thread.sleep(config.getCrawlerDelay());
                
            } catch (Exception e) {
                System.out.println("获取价格失败: " + subway.getDisplayName() + ", " + e.getMessage());
            }
        }
        
        // 过滤掉无效数据
        List<Subway> validSubways = subways.stream()
            .filter(Subway::hasValidPrice)
            .collect(Collectors.toList());
        
        // 缓存数据
        if (config.isCacheEnabled()) {
            String json = JsonUtils.toJsonString(validSubways);
            FileUtils.writeToFile(priceFile, json);
            System.out.println("价格数据已缓存到: " + priceFile);
        }
        
        return validSubways;
    }
    
    private List<Subway> getLocationData(List<Subway> subways) throws IOException {
        String locationFile = config.getLocationJsonFile();
        
        if (config.isCacheEnabled() && FileUtils.exists(locationFile)) {
            System.out.println("从缓存加载位置数据: " + locationFile);
            String json = FileUtils.readFromFile(locationFile);
            List<Subway> cachedSubways = JsonUtils.parseArray(json, Subway.class);
            if (cachedSubways != null && !cachedSubways.isEmpty()) {
                return cachedSubways;
            }
        }
        
        System.out.println("开始获取地理位置数据...");
        
        for (Subway subway : subways) {
            try {
                String keyword = subway.getLineName() + " " + subway.getName();
                POI poi = locationService.getPOI(keyword);
                
                if (poi != null && poi.isValid()) {
                    subway.setLongitude(poi.getLongitude());
                    subway.setLatitude(poi.getLatitude());
                    System.out.println("获取位置: " + subway.getDisplayName() + 
                        " = [" + poi.getLongitude() + ", " + poi.getLatitude() + "]");
                } else {
                    System.out.println("位置获取失败: " + subway.getDisplayName());
                }
                
                // 添加延迟避免API限流
                Thread.sleep(config.getCrawlerDelay());
                
            } catch (LocationServiceException | InterruptedException e) {
                System.out.println("位置服务错误: " + subway.getDisplayName() + ", " + e.getMessage());
            }
        }
        
        // 过滤掉无效数据
        List<Subway> validSubways = subways.stream()
            .filter(Subway::hasValidLocation)
            .collect(Collectors.toList());
        
        // 缓存数据
        if (config.isCacheEnabled()) {
            String json = JsonUtils.toJsonString(validSubways);
            FileUtils.writeToFile(locationFile, json);
            System.out.println("位置数据已缓存到: " + locationFile);
        }
        
        return validSubways;
    }
}