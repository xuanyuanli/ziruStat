package cn.xuanyuanli.rentradar.crawler;

import cn.xuanyuanli.playwright.stealth.behavior.HumanBehaviorSimulator;
import cn.xuanyuanli.playwright.stealth.manager.PlaywrightBrowserManager;
import cn.xuanyuanli.rentradar.config.AppConfig;
import cn.xuanyuanli.rentradar.exception.CrawlerException;
import cn.xuanyuanli.rentradar.model.RentalPrice;
import cn.xuanyuanli.rentradar.model.Subway;
import cn.xuanyuanli.rentradar.utils.PriceSpriteDecoder;
import cn.xuanyuanli.rentradar.utils.RetryUtils;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    private final AppConfig config;
    private final PlaywrightBrowserManager playwrightManager;

    /**
     * 构造函数
     *
     * @param playwrightManager Playwright管理器，用于执行网页自动化操作
     */
    public ZiroomCrawler(PlaywrightBrowserManager playwrightManager) {
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
                    1000
            );
        } catch (Exception e) {
            throw new CrawlerException("获取地铁站列表失败", e);
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

        playwrightManager.execute(page -> {
            try {
                System.out.println("开始获取地铁站列表...");

                // 1. 访问租房首页
                page.navigate("https://www.ziroom.com/z/", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
                page.waitForSelector("span.opt-name");

                // 2. 点击地铁选项展开地铁线路
                page.locator("span.opt-name:has-text('地铁')").click();

                // 3. 获取所有地铁线路链接（从dropdown中）
                Locator subwayLines = page.locator("span.opt-name:has-text('地铁') + div").locator(".wrapper a.item");
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
                        if (lineHref.startsWith("//")) {
                            lineHref = "https:" + lineHref;
                        }

                        System.out.println("正在处理地铁线路: " + lineName);

                        // 访问线路页面获取站点
                        List<Subway> stationsInLine = getStationsInLine(page, lineHref, lineName);
                        subways.addAll(stationsInLine);
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
     * @param page     当前的Playwright页面对象
     * @param lineHref 地铁线路的页面链接
     * @param lineName 地铁线路名称
     * @return 该线路下的所有地铁站列表
     */
    private List<Subway> getStationsInLine(Page page, String lineHref, String lineName) {
        List<Subway> stations = new ArrayList<>();

        try {
            page.navigate(lineHref, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            page.waitForSelector(".grand-child-opt");

            HumanBehaviorSimulator.quickSimulate(page);

            // 查找站点链接，从展开的地铁站列表中获取
            Locator stationLinks = page.locator(".grand-child-opt a.checkbox");
            int stationCount = stationLinks.count();

            System.out.println("线路 " + lineName + " 找到 " + stationCount + " 个站点");

            for (int i = 0; i < stationCount; i++) {
                Locator stationElement = stationLinks.nth(i);
                String stationName = stationElement.textContent().trim();
                String stationHref = stationElement.getAttribute("href");

                if (stationHref.isEmpty() || stationName.isEmpty()) {
                    continue;
                }
                if (stationHref.startsWith("//")) {
                    stationHref = "https:" + stationHref;
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
    public double getAveragePrice(String url) {
        List<Double> pricePerMeters = new ArrayList<>();

        playwrightManager.execute(page -> {
            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            page.waitForSelector(".z_logo_footer");

            HumanBehaviorSimulator.simulate(page);

            // 根据实际网站结构查找房源列表项
            Locator houseItems = page.locator(".Z_list-box div.item");
            int itemCount = houseItems.count();

            for (int i = 0; i < itemCount; i++) {
                Locator houseItem = houseItems.nth(i);
                RentalPrice price = parsePriceFromSprites(houseItem);
                if (price != null && isValidPrice(price)) {
                    pricePerMeters.add(price.getPricePerSquareMeter());
                }
            }

            System.out.println("从 " + url + " 获取到 " + pricePerMeters.size() + " 个有效房源价格");
        });

        if (pricePerMeters.isEmpty()) {
            return 0.0;
        }

        return pricePerMeters.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }

    /**
     * 从CSS精灵图中解析价格
     * <p>
     * 查找页面中class为'num'的span元素，提取其background-position样式，
     * 使用PriceSpriteDecoder将位置信息解码为实际数字。
     * </p>
     *
     * @param element 包含房源信息的页面元素
     * @return 解码成功的租金价格对象，失败时返回null
     */
    private RentalPrice parsePriceFromSprites(Locator element) {
        // 提取面积信息
        String elementText = element.textContent();
        if (element.locator(".price-content").count() == 0) {
            return null;
        }
        double area = extractArea(elementText);
        if (area <= 0) {
            throw new RuntimeException("无法提取 " + element.page().url() + " 面积信息: " + elementText);
        }

        // 获取价格精灵图元素的样式信息
        Locator priceItem = element.locator(".price-content .price");
        // 删除第一个子元素-货币
        priceItem.locator(":first-child").evaluate("element => element.remove()");
        // 删除最后一个子元素-后缀
        priceItem.locator(":last-child").evaluate("element => element.remove()");

        Object result = priceItem.evaluate("""
                (element) => {
                const priceSpans = element.querySelectorAll('span.num');
                const spanData = [];
                for (let span of priceSpans) {
                    const style = span?.style?.cssText || span?.getAttribute('style') || '';
                    const bgImage = span?.style?.backgroundImage || '';
                    if(!bgImage) continue;
                    spanData.push({
                        style: style,
                        backgroundImage: bgImage,
                        backgroundPosition: span.style.backgroundPosition || ''
                    });
                }
                return spanData;
                }""");

        if (result instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> spanDataList = (List<Map<String, Object>>) result;

            if (!spanDataList.isEmpty()) {
                String priceStr = PriceSpriteDecoder.decodePrice(spanDataList, priceItem::screenshot);

                if (PriceSpriteDecoder.isValidPrice(priceStr)) {
                    double price = Double.parseDouble(priceStr);
                    return new RentalPrice(price, area);
                }
            }
        }
        throw new RuntimeException("无法提取 " + element.page().url() + " 价格信息: " + elementText);
    }

    /**
     * 从文本中提取面积信息
     *
     * @param text 包含面积信息的文本
     * @return 提取到的面积，未找到时返回0
     */
    private double extractArea(String text) {
        Pattern areaPattern = Pattern.compile("(\\d+(?:\\.\\d+)?)\\s*(㎡|m²)");
        Matcher areaMatcher = areaPattern.matcher(text);

        if (areaMatcher.find()) {
            try {
                return Double.parseDouble(areaMatcher.group(1));
            } catch (NumberFormatException e) {
                System.out.println("面积解析失败: " + areaMatcher.group(1));
            }
        }

        return 0;
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