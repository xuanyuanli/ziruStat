package cn.xuanyuanli.rentradar.crawler;

import cn.xuanyuanli.playwright.stealth.config.PlaywrightConfig;
import cn.xuanyuanli.playwright.stealth.config.StealthMode;
import cn.xuanyuanli.playwright.stealth.manager.PlaywrightManager;
import cn.xuanyuanli.rentradar.config.AppConfig;
import cn.xuanyuanli.rentradar.exception.CrawlerException;
import cn.xuanyuanli.rentradar.model.RentalPrice;
import cn.xuanyuanli.rentradar.model.Subway;
import cn.xuanyuanli.rentradar.utils.RetryUtils;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自如网站爬虫服务
 * <p>
 * 负责从自如租房网站爬取地铁站信息和租房价格数据，
 * 使用Playwright进行网页自动化操作和反爬虫规避。
 * </p>
 * 
 * <p>主要功能：</p>
 * <ul>
 *   <li>获取北京地区所有地铁站列表和链接</li>
 *   <li>爬取指定地铁站附近的租房价格数据</li>
 *   <li>解析房源信息并计算平均每平米价格</li>
 *   <li>支持重试机制和错误处理</li>
 * </ul>
 * 
 * @author xuanyuanli
 * @since 1.0.0
 */
@SuppressWarnings("CallToPrintStackTrace")
public class ZiroomCrawler {
    private static final PlaywrightConfig PLAYWRIGHT_CONFIG = new PlaywrightConfig()
            .setHeadless(true)
            .setStealthMode(StealthMode.FULL)
            .setDisableImageRender(true);

    private final AppConfig config;
    private final PlaywrightManager playwrightManager;

    /**
     * 构造函数
     * 
     * @param playwrightManager Playwright管理器，用于执行网页自动化操作
     */
    public ZiroomCrawler(PlaywrightManager playwrightManager) {
        this.config = AppConfig.getInstance();
        this.playwrightManager = playwrightManager;
    }

    /**
     * 获取所有地铁站信息
     * <p>
     * 从自如网站爬取北京地区所有地铁线路和站点信息，
     * 包括站点名称、所属线路和对应的租房页面链接。
     * </p>
     * 
     * @return 地铁站列表，包含站点名称、线路名称和页面链接
     * @throws CrawlerException 当爬取过程中发生错误时抛出
     */
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

    /**
     * 获取指定地铁站附近的平均房租价格
     * <p>
     * 爬取指定URL页面中的房源信息，解析每套房源的租金和面积，
     * 计算平均每平米价格。支持重试机制，失败时返回0.0。
     * </p>
     * 
     * @param url 地铁站对应的租房页面URL
     * @return 该地铁站附近房源的平均每平米价格，获取失败时返回0.0
     */
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

    /**
     * 爬取地铁站信息的核心实现方法
     * <p>
     * 通过Playwright访问自如网站首页，点击地铁选项展开地铁线路列表，
     * 然后依次访问每条线路页面获取该线路下的所有站点信息。
     * </p>
     * 
     * @return 爬取到的地铁站列表
     */
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

                // 3. 获取所有地铁线路链接（从dropdown中）
                Locator subwayLines = page.locator("a[href*='/z/s'][href*='-q']:not([href*='-t'])");
                int lineCount = subwayLines.count();

                System.out.println("找到 " + lineCount + " 条地铁线路");

                for (int i = 0; i < lineCount; i++) {
                    try {
                        Locator lineElement = subwayLines.nth(i);
                        String lineName = lineElement.textContent().trim();
                        String lineHref = lineElement.getAttribute("href");

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

    /**
     * 获取指定地铁线路下的所有站点信息
     * 
     * @param page 当前的Playwright页面对象
     * @param lineHref 地铁线路的页面链接
     * @param lineName 地铁线路名称
     * @return 该线路下的所有地铁站列表
     */
    private List<Subway> getStationsInLine(Page page, String lineHref, String lineName) {
        List<Subway> stations = new ArrayList<>();

        try {
            page.navigate(lineHref);
            page.waitForLoadState();
            page.waitForTimeout(2000);

            // 查找站点链接，从展开的地铁站列表中获取
            Locator stationLinks = page.locator("a[href*='/z/s'][href*='-t']");
            int stationCount = stationLinks.count();

            System.out.println("线路 " + lineName + " 找到 " + stationCount + " 个站点");

            for (int i = 0; i < stationCount; i++) {
                Locator stationElement = stationLinks.nth(i);
                String stationName = stationElement.textContent().trim();
                String stationHref = stationElement.getAttribute("href");

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

    /**
     * 爬取指定URL的房价数据
     * <p>
     * 访问地铁站对应的租房页面，查找页面中的房源列表项，
     * 解析每个房源的价格和面积信息，计算平均每平米价格。
     * </p>
     * 
     * @param url 要爬取的租房页面URL
     * @return 该页面房源的平均每平米价格，没有有效数据时返回0.0
     */
    private double crawlPriceData(String url) {
        List<Double> pricePerMeters = new ArrayList<>();

        playwrightManager.execute(PLAYWRIGHT_CONFIG, page -> {
            try {
                page.navigate(url);
                page.waitForLoadState();
                page.waitForTimeout(2000);

                // 根据实际网站结构查找房源列表项
                Locator houseItems = page.locator("div[class*='item'], .list-item, .room-item");
                int itemCount = houseItems.count();

                if (itemCount == 0) {
                    // 尝试其他可能的选择器
                    houseItems = page.locator("*:has-text('居室')");
                    itemCount = houseItems.count();
                }

                System.out.println("从页面找到 " + itemCount + " 个房源项");

                for (int i = 0; i < itemCount; i++) {
                    try {
                        Locator houseItem = houseItems.nth(i);
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

    /**
     * 解析单个房源元素的租金信息
     * <p>
     * 从房源元素的文本内容中提取租金价格和房间面积，
     * 使用正则表达式匹配价格（￥数字/月）和面积（数字㎡）。
     * </p>
     * 
     * @param element 包含房源信息的页面元素
     * @return 解析成功的租金价格对象，失败时返回null
     */
    private RentalPrice parseRentalInfo(Locator element) {
        String text = element.textContent();

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

    /**
     * 验证价格数据的合理性
     * <p>
     * 检查每平米价格是否在配置的合理范围内，
     * 用于过滤异常数据和无效信息。
     * </p>
     * 
     * @param price 要验证的租金价格对象
     * @return 价格合理返回true，否则返回false
     */
    private boolean isValidPrice(RentalPrice price) {
        double pricePerMeter = price.getPricePerSquareMeter();
        return pricePerMeter >= config.getMinReasonablePrice()
                && pricePerMeter <= config.getMaxReasonablePrice();
    }
}