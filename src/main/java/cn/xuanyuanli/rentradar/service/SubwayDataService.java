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
 * 地铁数据服务类<br>
 * 负责协调地铁站数据的收集、处理和缓存管理<br>
 * 整合爬虫服务和位置服务，按照分层缓存策略收集完整的地铁站信息<br>
 * 包括地铁站基础信息、地理位置坐标和租房价格数据
 *
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

    /**
     * 收集所有地铁站数据<br>
     * 按照三个步骤依次执行：获取地铁站基础信息 -> 获取地理位置数据 -> 获取价格数据<br>
     * 利用分层缓存策略避免重复请求，提高数据收集效率
     * 
     * @return 包含完整信息的地铁站列表
     * @throws Exception 数据收集过程中的各种异常
     */
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
     * 获取地铁站基础信息<br>
     * 第一步：从自如网站爬取地铁站基础信息（站名、URL、线路信息）<br>
     * 使用长期缓存策略（90天），避免频繁重复爬取基础数据
     * 
     * @return 地铁站基础信息列表
     * @throws Exception 爬虫异常或缓存异常
     */
    public List<Subway> getStationsData() throws Exception {
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
     * 获取地铁站地理位置数据<br>
     * 第二步：调用高德地图API获取每个地铁站的经纬度坐标<br>
     * 依赖于地铁站基础数据，使用依赖缓存策略
     * 
     * @param stations 包含基础信息的地铁站列表
     * @return 包含地理位置信息的地铁站列表
     * @throws Exception 位置服务异常或缓存异常
     */
    public List<Subway> getLocationData(List<Subway> stations) throws Exception {
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
            } catch (LocationServiceException e) {
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
    public List<Subway> getPriceData(List<Subway> stationsWithLocation) throws Exception {
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