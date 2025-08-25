package cn.xuanyuanli.rentradar;

import cn.xuanyuanli.rentradar.config.AppConfig;
import cn.xuanyuanli.rentradar.model.Subway;
import cn.xuanyuanli.rentradar.service.SubwayDataService;
import cn.xuanyuanli.rentradar.service.VisualizationService;
import cn.xuanyuanli.rentradar.utils.DirectoryUtils;
import cn.xuanyuanli.rentradar.utils.ServiceContainer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 自如租房数据统计应用
 * 重构版本 - 职责清晰，易于维护
 *
 * @author xuanyuanli
 */
public class ZiruStatApplication {

    private final ServiceContainer serviceContainer;

    public ZiruStatApplication() {
        this.serviceContainer = new ServiceContainer();
    }

    public static void main(String[] args) {
        ZiruStatApplication app = new ZiruStatApplication();
        app.run();
    }

    private void run() {
        printWelcomeMessage();

        try {
            // 初始化目录结构
            System.out.println("=== 初始化项目结构 ===");
            DirectoryUtils.initializeDirectories();

            // 获取服务实例
            SubwayDataService dataService = serviceContainer.getService(SubwayDataService.class);
            VisualizationService visualizationService = serviceContainer.getService(VisualizationService.class);

            // 执行核心业务流程
            System.out.println("=== 开始执行数据收集任务 ===");
            List<Subway> subwayData = dataService.collectAllSubwayData();

            System.out.println("=== 开始生成可视化 ===");
            visualizationService.generateHtmlVisualization(subwayData);

            // 显示结果统计
            printResultSummary(subwayData);

        } catch (Exception e) {
            System.err.println("程序执行失败: " + e.getMessage());
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            System.exit(1);
        } finally {
            // 清理资源
            serviceContainer.shutdown();
        }
    }

    private void printWelcomeMessage() {
        System.out.println("================================");
        System.out.println("    自如租房数据统计系统");
        System.out.println("     重构版本 v2.0");
        System.out.println("================================");
        System.out.println("启动时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println();
    }

    private void printResultSummary(List<Subway> subways) {
        AppConfig config = AppConfig.getInstance();

        System.out.println();
        System.out.println("=== 执行结果统计 ===");

        long totalStations = subways.size();
        long validPriceCount = subways.stream()
                .filter(s -> s.getSquareMeterOfPrice() > 0)
                .count();
        long validLocationCount = subways.stream()
                .filter(Subway::hasValidLocation)
                .count();

        double avgPrice = subways.stream()
                .filter(s -> s.getSquareMeterOfPrice() > 0)
                .mapToDouble(Subway::getSquareMeterOfPrice)
                .average()
                .orElse(0.0);

        System.out.printf("总站点数量: %d%n", totalStations);
        System.out.printf("有价格数据: %d (%.1f%%)%n", validPriceCount,
                totalStations > 0 ? (double) validPriceCount / totalStations * 100 : 0);
        System.out.printf("有位置数据: %d (%.1f%%)%n", validLocationCount,
                totalStations > 0 ? (double) validLocationCount / totalStations * 100 : 0);
        System.out.printf("平均租金: %.1f 元/㎡%n", avgPrice);

        System.out.println();
        System.out.println("=== 生成的文件 ===");
        System.out.printf("- %s: 地铁价格数据%n", config.getPriceJsonFile());
        System.out.printf("- %s: 地铁位置数据%n", config.getLocationJsonFile());
        System.out.printf("- %s: 可视化地图页面%n", config.getHtmlOutputFile());

        System.out.println();
        System.out.printf("任务完成！请打开 %s 查看地图可视化结果。%n", config.getHtmlOutputFile());
        System.out.println("完成时间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
}