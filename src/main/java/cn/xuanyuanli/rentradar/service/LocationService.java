package cn.xuanyuanli.rentradar.service;

import cn.xuanyuanli.rentradar.config.AppConfig;
import cn.xuanyuanli.rentradar.exception.LocationServiceException;
import cn.xuanyuanli.rentradar.model.POI;
import cn.xuanyuanli.rentradar.utils.JsonUtils;
import cn.xuanyuanli.rentradar.utils.RetryUtils;
import org.jsoup.Connection.Method;
import org.jsoup.helper.HttpConnection;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class LocationService {
    private static final String SEARCH_URL = "http://restapi.amap.com/v3/place/text?key=%s&keywords=%s&city=%s&citylimit=%s";
    
    private final AppConfig config;
    
    public LocationService() {
        this.config = AppConfig.getInstance();
    }
    
    public POI getPOI(String keyword) throws LocationServiceException {
        try {
            return RetryUtils.executeWithRetry(
                () -> fetchPOIFromApi(keyword),
                config.getCrawlerMaxRetry(),
                config.getCrawlerDelay()
            );
        } catch (Exception e) {
            throw new LocationServiceException("获取POI信息失败: " + keyword, e);
        }
    }
    
    private POI fetchPOIFromApi(String keyword) {
        try {
            String url = String.format(SEARCH_URL, 
                config.getGaodeApiKey(), 
                keyword, 
                config.getGaodeCity(), 
                "true");
                
            System.out.println("查询地理位置: " + keyword);
            
            String body = HttpConnection.connect(url)
                .method(Method.GET)
                .ignoreContentType(true)
                .execute()
                .body();
            
            return parsePOIFromResponse(body);
            
        } catch (IOException e) {
            throw new RuntimeException("API调用失败", e);
        }
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private POI parsePOIFromResponse(String responseBody) {
        try {
            Map<String, Object> map = JsonUtils.parseObject(responseBody, Map.class);
            if (map == null) {
                throw new RuntimeException("响应解析失败");
            }
            
            List pois = (List) map.get("pois");
            if (pois == null || pois.isEmpty()) {
                throw new RuntimeException("未找到位置信息");
            }
            
            Map firstPoi = (Map) pois.get(0);
            String location = (String) firstPoi.get("location");
            
            if (location == null) {
                throw new RuntimeException("位置信息为空");
            }
            
            String[] coords = location.split(",");
            if (coords.length != 2) {
                throw new RuntimeException("位置格式错误");
            }
            
            POI poi = new POI();
            poi.setLongitude(coords[0]);
            poi.setLatitude(coords[1]);
            
            return poi;
            
        } catch (Exception e) {
            throw new RuntimeException("解析POI响应失败", e);
        }
    }
}