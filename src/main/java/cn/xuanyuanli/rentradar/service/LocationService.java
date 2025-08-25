package cn.xuanyuanli.rentradar.service;

import cn.xuanyuanli.rentradar.config.AppConfig;
import cn.xuanyuanli.rentradar.exception.LocationServiceException;
import cn.xuanyuanli.rentradar.model.POI;
import cn.xuanyuanli.rentradar.utils.JsonUtils;
import cn.xuanyuanli.rentradar.utils.RetryUtils;
import org.jsoup.Connection.Method;
import org.jsoup.helper.HttpConnection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 位置服务
 *
 * @author xuanyuanli
 */
@SuppressWarnings("AlibabaLowerCamelCaseVariableNaming")
public class LocationService {
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
            Map<String, String> params = new TreeMap<>();
            params.put("key", config.getGaodeApiKey());
            params.put("keywords", keyword);
            params.put("city", config.getGaodeCity());
            params.put("citylimit", "true");

            String privateKey = config.getGaodeApiPrivateKey();
            if (privateKey != null && !privateKey.trim().isEmpty()) {
                String signature = generateDigitalSignature(params, privateKey);
                params.put("sig", signature);
            }

            String url = buildRequestUrl(params);
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

    /**
     * 根据高德地图API要求生成数字签名
     * 算法：将请求参数按参数名的字母顺序排序，然后拼接私钥，最后进行MD5加密
     *
     * @param params     请求参数（已排序）
     * @param privateKey API私钥
     * @return MD5签名
     */
    private String generateDigitalSignature(Map<String, String> params, String privateKey) {
        try {
            // 构建待签名字符串：按字母顺序排列的参数对 + 私钥
            String paramString = params.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.joining("&"));

            String signString = paramString + privateKey;

            // 计算MD5签名
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(signString.getBytes(StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 构建完整的请求URL
     *
     * @param params 包含所有参数（包括签名）的Map
     * @return 完整的请求URL
     */
    private String buildRequestUrl(Map<String, String> params) {
        String baseUrl = "https://restapi.amap.com/v3/place/text";
        String queryString = params.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        return baseUrl + "?" + queryString;
    }
}