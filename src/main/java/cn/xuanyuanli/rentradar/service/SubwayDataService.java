package cn.xuanyuanli.rentradar.service;

import cn.xuanyuanli.rentradar.config.AppConfig;
import cn.xuanyuanli.rentradar.crawler.ZiroomCrawler;
import cn.xuanyuanli.rentradar.exception.CrawlerException;
import cn.xuanyuanli.rentradar.exception.LocationServiceException;
import cn.xuanyuanli.rentradar.model.POI;
import cn.xuanyuanli.rentradar.model.Subway;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xuanyuanli
 */
public class SubwayDataService {

    private final AppConfig config;
    private final ZiroomCrawler crawler;
    private final LocationService locationService;
    private final CacheManager cacheManager;

    public SubwayDataService(ZiroomCrawler crawler, LocationService locationService) {
        this.config = AppConfig.getInstance();
        this.crawler = crawler;
        this.locationService = locationService;
        this.cacheManager = new CacheManager();
    }

    public List<Subway> collectAllSubwayData() throws Exception {
        System.out.println("开始收集地铁数据...");

        // 1. 获取地铁站基础信息
        List<Subway> stations = getStationsData();

        // 2. 获取地理位置数据  
        List<Subway> stationsWithLocation = getLocationData(stations);

        // 3. 获取价格数据
        List<Subway> stationsWithPrice = getPriceData(stationsWithLocation);

        System.out.println("地铁数据收集完成，共 " + stationsWithPrice.size() + " 个站点");
        return stationsWithPrice;
    }

    /**
     * 第一步：获取地铁站基础信息（站名、URL、线路信息）
     */
    private List<Subway> getStationsData() throws Exception {
        System.out.println("开始获取地铁站基础信息...");
        
        return cacheManager.getCachedData(
                config.getStationsJsonFile(),
                config.getStationsCacheExpireDays(),
                () -> {
                    try {
                        return crawler.getSubwayStations();
                    } catch (CrawlerException e) {
                        throw new RuntimeException("获取地铁站数据失败", e);
                    }
                },
                Subway.class
        );
    }

    /**
     * 第二步：获取地铁站地理位置数据
     */
    private List<Subway> getLocationData(List<Subway> stations) throws Exception {
        System.out.println("开始获取地理位置数据...");
        
        return cacheManager.getCachedDataWithDependency(
                config.getLocationsJsonFile(),
                config.getStationsJsonFile(),
                () -> enrichWithLocations(stations),
                Subway.class
        );
    }

    /**
     * 为地铁站数据添加地理位置信息
     */
    private List<Subway> enrichWithLocations(List<Subway> stations) {
        for (Subway station : stations) {
            try {
                String keyword = station.getLineName() + " " + station.getName();
                POI poi = locationService.getPOI(keyword);

                if (poi != null && poi.isValid()) {
                    station.setLongitude(poi.getLongitude());
                    station.setLatitude(poi.getLatitude());
                    System.out.println("获取位置: " + station.getDisplayName() +
                            " = [" + poi.getLongitude() + ", " + poi.getLatitude() + "]");
                } else {
                    System.out.println("位置获取失败: " + station.getDisplayName());
                }

                // 添加延迟避免API限流
                Thread.sleep(config.getCrawlerDelay());

            } catch (LocationServiceException | InterruptedException e) {
                System.out.println("位置服务错误: " + station.getDisplayName() + ", " + e.getMessage());
            }
        }

        // 过滤掉无效数据
        return stations.stream()
                .filter(Subway::hasValidLocation)
                .collect(Collectors.toList());
    }

    /**
     * 第三步：获取地铁站价格数据
     */
    private List<Subway> getPriceData(List<Subway> stationsWithLocation) throws Exception {
        System.out.println("开始获取价格数据...");
        
        return cacheManager.getCachedData(
                config.getPricesJsonFile(),
                config.getPricesCacheExpireDays(),
                () -> enrichWithPrices(stationsWithLocation),
                Subway.class
        );
    }

    /**
     * 为地铁站数据添加价格信息
     */
    private List<Subway> enrichWithPrices(List<Subway> stations) {
        for (Subway station : stations) {
            try {
                double avgPrice = crawler.getAveragePrice(station.getUrl());
                station.setSquareMeterOfPrice(avgPrice);
                System.out.println("获取价格: " + station.getDisplayName() + " = " + avgPrice + " 元/㎡");

                // 添加延迟避免请求过于频繁
                Thread.sleep(config.getCrawlerDelay());

            } catch (Exception e) {
                System.out.println("获取价格失败: " + station.getDisplayName() + ", " + e.getMessage());
            }
        }

        // 过滤掉无效数据
        return stations.stream()
                .filter(Subway::hasValidPrice)
                .collect(Collectors.toList());
    }
}