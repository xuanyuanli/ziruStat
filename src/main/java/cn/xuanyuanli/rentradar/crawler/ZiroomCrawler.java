package cn.xuanyuanli.rentradar.crawler;

import cn.xuanyuanli.playwright.stealth.config.PlaywrightConfig;
import cn.xuanyuanli.playwright.stealth.config.StealthMode;
import cn.xuanyuanli.playwright.stealth.manager.PlaywrightManager;
import cn.xuanyuanli.rentradar.config.AppConfig;
import cn.xuanyuanli.rentradar.exception.CrawlerException;
import cn.xuanyuanli.rentradar.model.RentalPrice;
import cn.xuanyuanli.rentradar.model.Subway;
import cn.xuanyuanli.rentradar.utils.RetryUtils;
import com.microsoft.playwright.Page;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xuanyuanli
 */
@SuppressWarnings("CallToPrintStackTrace")
public class ZiroomCrawler {
    private static final PlaywrightConfig PLAYWRIGHT_CONFIG = new PlaywrightConfig()
            .setHeadless(true)
            .setStealthMode(StealthMode.FULL)
            .setDisableImageRender(true);

    private final AppConfig config;
    private final PlaywrightManager playwrightManager;

    public ZiroomCrawler(PlaywrightManager playwrightManager) {
        this.config = AppConfig.getInstance();
        this.playwrightManager = playwrightManager;
    }

    public List<Subway> getSubwayStations() throws CrawlerException {
        try {
            return RetryUtils.executeWithRetry(
                    this::crawlSubwayStations,
                    config.getCrawlerMaxRetry(),
                    config.getCrawlerDelay()
            );
        } catch (Exception e) {
            throw new CrawlerException("获取地铁站列表失败", e);
        }
    }

    public double getAveragePrice(String url) {
        try {
            return RetryUtils.executeWithRetry(
                    () -> crawlPriceData(url),
                    config.getCrawlerMaxRetry(),
                    config.getCrawlerDelay()
            );
        } catch (Exception e) {
            System.out.println("价格获取失败，URL: " + url + ", 错误: " + e.getMessage());
            return 0.0;
        }
    }

    private List<Subway> crawlSubwayStations() {
        List<Subway> subways = new ArrayList<>();

        playwrightManager.execute(PLAYWRIGHT_CONFIG, page -> {
            try {
                System.out.println("开始获取地铁站列表...");

                // 1. 访问租房首页
                page.navigate("https://www.ziroom.com/z/");
                page.waitForLoadState();

                // 2. 点击地铁选项展开地铁线路
                page.locator("span:has-text('地铁')").click();
                page.waitForTimeout(2000);

                String html = page.content();
                Document document = Jsoup.parse(html);

                // 3. 获取所有地铁线路链接（从dropdown中）
                Elements subwayLines = document.select("a[href*='/z/s'][href*='-q']:not([href*='-t'])");

                System.out.println("找到 " + subwayLines.size() + " 条地铁线路");

                for (Element lineElement : subwayLines) {
                    try {
                        String lineName = lineElement.text().trim();
                        String lineHref = lineElement.attr("href");

                        if (lineHref.isEmpty() || lineName.isEmpty()) {
                            continue;
                        }

                        if (!lineHref.startsWith("http")) {
                            lineHref = "https://www.ziroom.com" + lineHref;
                        }

                        System.out.println("正在处理地铁线路: " + lineName);

                        // 访问线路页面获取站点
                        List<Subway> stationsInLine = getStationsInLine(page, lineHref, lineName);
                        subways.addAll(stationsInLine);

                        // 限制抓取数量，避免过度请求
                        if (subways.size() > 50) {
                            System.out.println("已获取50个地铁站，停止抓取");
                            break;
                        }

                    } catch (Exception e) {
                        System.out.println("处理地铁线路出错: " + e.getMessage());
                        e.printStackTrace();
                    }
                }

            } catch (Exception e) {
                System.out.println("获取地铁站链接失败: " + e.getMessage());
                e.printStackTrace();
            }
        });

        System.out.println("总共获取到 " + subways.size() + " 个地铁站");
        return subways;
    }

    private List<Subway> getStationsInLine(Page page, String lineHref, String lineName) {
        List<Subway> stations = new ArrayList<>();

        try {
            page.navigate(lineHref);
            page.waitForLoadState();
            page.waitForTimeout(2000);

            String stationHtml = page.content();
            Document stationDocument = Jsoup.parse(stationHtml);

            // 查找站点链接，从展开的地铁站列表中获取
            Elements stationLinks = stationDocument.select("a[href*='/z/s'][href*='-t']");

            System.out.println("线路 " + lineName + " 找到 " + stationLinks.size() + " 个站点");

            for (Element stationElement : stationLinks) {
                String stationName = stationElement.text().trim();
                String stationHref = stationElement.attr("href");

                if (stationHref.isEmpty() || stationName.isEmpty()) {
                    continue;
                }

                if (!stationHref.startsWith("http")) {
                    stationHref = "https://www.ziroom.com" + stationHref;
                }

                Subway subway = new Subway(stationName, lineName, stationHref);
                stations.add(subway);

                System.out.println("添加地铁站: " + lineName + " - " + stationName);
            }

        } catch (Exception e) {
            System.out.println("获取线路站点失败: " + e.getMessage());
            e.printStackTrace();
        }

        return stations;
    }

    private double crawlPriceData(String url) {
        List<Double> pricePerMeters = new ArrayList<>();

        playwrightManager.execute(PLAYWRIGHT_CONFIG, page -> {
            try {
                page.navigate(url);
                page.waitForLoadState();
                page.waitForTimeout(2000);

                String html = page.content();
                Document document = Jsoup.parse(html);

                // 根据实际网站结构查找房源列表项
                Elements houseItems = document.select("div[class*='item'], .list-item, .room-item");

                if (houseItems.isEmpty()) {
                    // 尝试其他可能的选择器
                    houseItems = document.select("generic:contains(居室)");
                }

                System.out.println("从页面找到 " + houseItems.size() + " 个房源项");

                for (Element houseItem : houseItems) {
                    try {
                        RentalPrice price = parseRentalInfo(houseItem);
                        if (price != null && isValidPrice(price)) {
                            pricePerMeters.add(price.getPricePerSquareMeter());
                        }
                    } catch (Exception e) {
                        // 忽略单个房源解析错误
                    }
                }

                System.out.println("从 " + url + " 获取到 " + pricePerMeters.size() + " 个有效房源价格");

            } catch (Exception e) {
                System.out.println("访问页面出错 " + url + ": " + e.getMessage());
                e.printStackTrace();
            }
        });

        if (pricePerMeters.isEmpty()) {
            return 0.0;
        }

        return pricePerMeters.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    private RentalPrice parseRentalInfo(Element element) {
        String text = element.text();

        if (!text.contains("￥") && !text.contains("㎡")) {
            return null;
        }

        try {
            // 解析面积 - 更宽松的匹配模式
            Pattern areaPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*㎡");
            Matcher areaMatcher = areaPattern.matcher(text);

            // 解析价格 - 匹配数字后跟/月的模式
            Pattern pricePattern = Pattern.compile("￥(\\d+(?:,\\d+)?(?:\\.\\d+)?)\\s*/月");
            Matcher priceMatcher = pricePattern.matcher(text);

            // 如果没找到月租，尝试其他模式
            if (!priceMatcher.find()) {
                pricePattern = Pattern.compile("￥(\\d+(?:,\\d+)?(?:\\.\\d+)?)");
                priceMatcher = pricePattern.matcher(text);
            }

            if (areaMatcher.find() && priceMatcher.find()) {
                String areaStr = areaMatcher.group(1);
                String priceStr = priceMatcher.group(1).replace(",", "");

                double area = Double.parseDouble(areaStr);
                double price = Double.parseDouble(priceStr);

                if (area > 0 && price > 0) {
                    return new RentalPrice(price, area);
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("解析价格信息失败: " + text);
        }

        return null;
    }

    private boolean isValidPrice(RentalPrice price) {
        double pricePerMeter = price.getPricePerSquareMeter();
        return pricePerMeter >= config.getMinReasonablePrice()
                && pricePerMeter <= config.getMaxReasonablePrice();
    }
}