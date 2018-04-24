package org.dadazao.zirustat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection.Method;
import org.jsoup.helper.HttpConnection;

import com.alibaba.fastjson.JSON;

/** 高德地图工具类 */
public class GaodeMapUtils {
	private static final String SEARCH_URL = "http://restapi.amap.com/v3/place/text?key=%s&keywords=%s&city=%s&citylimit=%s";
	private static final String MAP_KEY = "e6cfe3060ec12d1212dc37895aa6e4c5";

	/** 根据关键词获得POI信息*/
	public static POI getPOI(String text) throws IOException {
		POI poi = new POI();
		String url = String.format(SEARCH_URL, MAP_KEY, text, "010", "true");
		String body = HttpConnection.connect(url).method(Method.GET).ignoreContentType(true).execute().body();
		@SuppressWarnings("unchecked")
		Map<String,Object> map = JSON.parseObject(body, Map.class);
		@SuppressWarnings("rawtypes")
		String location = (String)((Map)((List)map.get("pois")).get(0)).get("location");
		String[] arr = location.split(",");
		poi.setLongitude(arr[0]);
		poi.setLatitude(arr[1]);
		return poi;
	}
}
