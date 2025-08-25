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
 * 服务容器类<br>
 * 负责管理和提供应用程序中各种服务的实例，实现依赖注入和生命周期管理<br>
 * 采用简单的服务定位器模式，统一管理Playwright浏览器管理器和各种业务服务
 *
 * @author xuanyuanli
 */
public class ServiceContainer {
    private final Map<Class<?>, Object> services = new HashMap<>();
    private PlaywrightBrowserManager playwrightManager;
    /**
     * Playwright浏览器配置<br>
     * 配置为非无头模式，禁用隐身模式，启用图片渲染和GPU加速
     */
    public static final PlaywrightConfig PLAYWRIGHT_CONFIG = new PlaywrightConfig()
            .setHeadless(false)
            .setStealthMode(StealthMode.DISABLED)
            .setDisableImageRender(false).setDisableAutomationControlled(true).setDisableGpu(false);

    /**
     * 构造函数<br>
     * 自动初始化所有服务实例并建立它们之间的依赖关系
     */
    public ServiceContainer() {
        initializeServices();
    }

    /**
     * 获取指定类型的服务实例
     *
     * @param <T>          服务类型泛型
     * @param serviceClass 服务类的Class对象
     * @return 服务实例，如果未注册则返回null
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> serviceClass) {
        return (T) services.get(serviceClass);
    }

    /**
     * 初始化所有服务实例<br>
     * 按照依赖关系顺序创建服务：LocationService -> ZiroomCrawler -> SubwayDataService -> VisualizationService<br>
     * 同时初始化Playwright浏览器管理器
     */
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

    /**
     * 关闭服务容器并释放资源<br>
     * 主要负责关闭Playwright浏览器管理器，释放浏览器进程和相关资源
     */
    public void shutdown() {
        if (playwrightManager != null) {
            playwrightManager.close();
            System.out.println("Playwright管理器已关闭");
        }
    }
}