package org.dadazao.zirustat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;

public class App {
	/** 地铁位置信息的json保存路径 */
	private static final String SUBWAY_LOCATION_JSON_PATH = "subwayLocation.json";
	/** 价格信息的json保存路径 */
	private static final String SUBWAY_PRICE_JSON_PATH = "subway.json";
	/** 自如租房的首页地址 */
	private static final String INDEX_URL = "http://www.ziroom.com/z/nl/z2.html";
	/** 项目目录 */
	public static final String PROJECT_DIR = System.getProperty("user.dir");

	/** 默认计算的平方数 */
	public static final int DEFAULT_CLAC_SQUARE_METER = 10;

	public static void main(String[] args) throws IOException {
		obtainPriceInfo();
		obtainSubwayLocation();
		showHtml();
	}

	private static void showHtml() throws IOException {
		String pattern = "new AMap.Marker({ position : [%s,%s], map:map, label:{ offset : new AMap.Pixel(20, 20), content : '%s'}});";

		Path subwayPath = Paths.get(PROJECT_DIR, SUBWAY_LOCATION_JSON_PATH);
		String json = Files.readAllLines(subwayPath).get(0);
		List<Subway> subways = JSON.parseArray(json, Subway.class);
		StringBuilder markers = new StringBuilder();
		for (Subway subway : subways) {
			double price = subway.getSquareMeterOfPrice();
			price = (double) Math.round(price * 10 * DEFAULT_CLAC_SQUARE_METER) / 10; // 只保留一位小数。这里顺便计算了15平米的价格
			markers.append("		")
					.append(String.format(pattern, subway.getLongitude(), subway.getLatitude(), price + ""))
					.append("\n");
		}
		Path path = Paths.get(PROJECT_DIR, "showTemplate.html");
		String content = new String(Files.readAllBytes(path));
		content = content.replace("&&&", markers.toString());

		Path descPath = Paths.get(PROJECT_DIR, "show.html");
		if (!Files.exists(descPath)) {
			Files.createFile(descPath);
		}
		Files.write(descPath, content.getBytes());
	}

	/** 获得地铁的经纬度 */
	private static void obtainSubwayLocation() throws IOException {
		Path path = Paths.get(PROJECT_DIR, SUBWAY_LOCATION_JSON_PATH);
		if (!Files.exists(path)) {
			Path subwayPath = Paths.get(PROJECT_DIR, SUBWAY_PRICE_JSON_PATH);
			String json = Files.readAllLines(subwayPath).get(0);
			List<Subway> subways = JSON.parseArray(json, Subway.class);
			for (Subway subway : subways) {
				POI poi = GaodeMapUtils.getPOI(subway.getLineName() + " " + subway.getName());
				// System.out.println(poi);
				subway.setLatitude(poi.getLatitude());
				subway.setLongitude(poi.getLongitude());
			}
			// 数据暂时存到json文件中
			json = JSON.toJSONString(subways);
			Files.createFile(path);
			Files.write(path, json.getBytes());
		}
	}

	/** 获得价格信息 */
	private static void obtainPriceInfo() throws IOException {
		Path path = Paths.get(PROJECT_DIR, SUBWAY_PRICE_JSON_PATH);
		if (!Files.exists(path)) {
			List<Subway> subwaryUrls = getSubwaryUrls();
			for (Subway subway : subwaryUrls) {
				subway.setSquareMeterOfPrice(getSquareMeterOfPrice(subway.getUrl()));
				// System.out.println(subway);
			}
			// 数据暂时存到json文件中
			String json = JSON.toJSONString(subwaryUrls);
			Files.createFile(path);
			Files.write(path, json.getBytes());
		}
	}

	/** 获得地铁站附近房子每平米每月的价格 */
	private static double getSquareMeterOfPrice(String url) throws IOException {
		url = convertTenMinuteFilter(url);
		// System.out.println(url);
		Document document = Jsoup.connect(url).get();
		Elements liEles = document.select("#houseList li");
		List<Double> meterOfPrice = new ArrayList<Double>();
		for (Element liEle : liEles) {
			String priceText = liEle.child(2).child(0).text();
			if (priceText.contains("每月")) {
				// 获得平米数
				String text = liEle.child(1).child(2).child(0).child(0).text();
				text = text.replace("㎡", "").replace("约", "").trim();
				double meter = Double.parseDouble(text);
				// 获得价格
				text = priceText.replace("￥", "").replace("(每月)", "").trim();
				if (text.length() > 0) {
					double price = Double.parseDouble(text);
					meterOfPrice.add(price / meter);
				}
			}
		}
		return meterOfPrice.stream().collect(Collectors.summarizingDouble(t -> t)).getAverage();
	}

	/** 获得地铁每个站的链接 */
	private static List<Subway> getSubwaryUrls() throws IOException {
		List<Subway> list = new ArrayList<Subway>();
		Document document = Jsoup.connect(INDEX_URL).get();
		Elements lis = document.select("dl.zIndex5 dd ul li");
		for (Element liEle : lis) {
			Element tag = liEle.child(0);
			if (tag.is(".tag")) {
				String stationName = tag.text();
				Elements eles = liEle.child(1).select("span a");
				for (Element aEle : eles) {
					String name = aEle.text().trim();
					if (!name.equals("全部")) {
						Subway subway = new Subway();
						subway.setLineName(stationName);
						subway.setName(name);
						subway.setUrl(aEle.absUrl("href"));
						list.add(subway);
					}
				}
			}
		}
		return list;
	}

	/** url转换为这样的筛选条件：地铁十分钟 */
	private static String convertTenMinuteFilter(String url) {
		int length = url.length();
		return url.substring(0, length - 5) + "-x6" + url.substring(length - 5);
	}
}
