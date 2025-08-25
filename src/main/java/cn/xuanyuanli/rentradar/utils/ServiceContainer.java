package cn.xuanyuanli.rentradar.utils;

import cn.xuanyuanli.playwright.stealth.config.PlaywrightConfig;
import cn.xuanyuanli.playwright.stealth.config.StealthMode;
import cn.xuanyuanli.playwright.stealth.manager.PlaywrightBrowserManager;
import cn.xuanyuanli.playwright.stealth.manager.PlaywrightManager;
import cn.xuanyuanli.rentradar.crawler.ZiroomCrawler;
import cn.xuanyuanli.rentradar.service.LocationService;
import cn.xuanyuanli.rentradar.service.SubwayDataService;
import cn.xuanyuanli.rentradar.service.VisualizationService;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xuanyuanli
 */
public class ServiceContainer {
    private final Map<Class<?>, Object> services = new HashMap<>();
    private PlaywrightBrowserManager playwrightManager;
    public static final PlaywrightConfig PLAYWRIGHT_CONFIG = new PlaywrightConfig()
            .setHeadless(false)
            .setStealthMode(StealthMode.DISABLED)
            .setDisableImageRender(false).setDisableAutomationControlled(true).setDisableGpu(false);

    public ServiceContainer() {
        initializeServices();
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        return (T) services.get(serviceClass);
    }

    private void initializeServices() {
        // 初始化Playwright管理器
        playwrightManager = new PlaywrightBrowserManager(PLAYWRIGHT_CONFIG, 1);

        // 创建服务实例
        LocationService locationService = new LocationService();
        ZiroomCrawler crawler = new ZiroomCrawler(playwrightManager);
        SubwayDataService dataService = new SubwayDataService(crawler, locationService);
        VisualizationService visualizationService = new VisualizationService();

        // 注册服务
        services.put(LocationService.class, locationService);
        services.put(ZiroomCrawler.class, crawler);
        services.put(SubwayDataService.class, dataService);
        services.put(VisualizationService.class, visualizationService);
    }

    public void shutdown() {
        if (playwrightManager != null) {
            playwrightManager.close();
            System.out.println("Playwright管理器已关闭");
        }
    }
}