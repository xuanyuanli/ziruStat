package cn.xuanyuanli.rentradar.service;

import cn.xuanyuanli.rentradar.config.AppConfig;
import cn.xuanyuanli.rentradar.model.Subway;
import cn.xuanyuanli.rentradar.utils.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 可视化服务类<br>
 * 负责生成基于高德地图的HTML可视化页面<br>
 * 将地铁站价格数据渲染为带有颜色标记的地图标点<br>
 * 支持价格统计信息显示和自定义地图模板
 *
 * @author xuanyuanli
 */
public class VisualizationService {

    private final AppConfig config;

    public VisualizationService() {
        this.config = AppConfig.getInstance();
    }

    /**
     * 生成HTML可视化地图<br>
     * 根据地铁站数据生成包含价格标记的高德地图HTML页面
     * 
     * @param subways 包含完整信息的地铁站列表
     * @throws IOException 文件操作异常
     */
    public void generateHtmlVisualization(List<Subway> subways) throws IOException {
        System.out.println("开始生成HTML可视化...");

        String template = loadTemplate();
        String markers = buildMarkers(subways);
        String statistics = buildStatistics(subways);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String html = template
                .replace("{{GAODE_API_KEY}}", config.getGaodeApiKey())
                .replace("{{MARKERS}}", markers)
                .replace("{{STATISTICS}}", statistics)
                .replace("{{GENERATED_TIME}}", timestamp);

        String outputFile = config.getHtmlOutputFile();
        FileUtils.writeToFile(outputFile, html);

        System.out.println("HTML可视化已生成: " + outputFile);
        System.out.println("包含 " + subways.size() + " 个地铁站的价格信息");
    }

    private String loadTemplate() throws IOException {
        // 从resources加载模板
        String templatePath = config.getMapTemplate();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(templatePath)) {
            if (input != null) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            System.out.println("模板文件加载失败: " + templatePath);
        }
        throw new IOException("模板文件未找到: " + templatePath);
    }

    private String buildMarkers(List<Subway> subways) {
        return subways.stream()
                .filter(Subway::hasValidLocation)
                .filter(Subway::hasValidPrice)
                .map(this::createMarkerScript)
                .collect(Collectors.joining("\n\t\t"));
    }

    private String createMarkerScript(Subway subway) {
        double displayPrice = Math.round(subway.getSquareMeterOfPrice() *
                config.getDefaultSquareMeter() * 10) / 10.0;

        // 根据价格设置不同颜色
        String color = getPriceColor(subway.getSquareMeterOfPrice());

        return String.format(
                """
                        new AMap.Marker({
                          position: [%s, %s],
                          map: map,
                          title: '%s',
                          icon: new AMap.Icon({
                            size: new AMap.Size(25, 34),
                            image: 'https://webapi.amap.com/theme/v1.3/markers/n/mark_b%s.png'
                          }),
                          label: {
                            offset: new AMap.Pixel(20, 20),
                            content: '%s<br/>¥%.1f'
                          }
                        });""",
                subway.getLongitude(), subway.getLatitude(),
                subway.getDisplayName(),
                color,
                subway.getName(), displayPrice
        );
    }

    /**
     * 根据每平方米价格获取对应的地图标记颜色<br>
     * 价格分级：绿色(<50) -> 蓝色(<80) -> 黄色(<120) -> 红色(>=120)
     * 
     * @param pricePerMeter 每平方米价格
     * @return 颜色代码(1-4)
     */
    private String getPriceColor(double pricePerMeter) {
        // 绿色
        if (pricePerMeter < 50) {
            return "1";
        }
        // 蓝色
        else if (pricePerMeter < 80) {
            return "2";
        }
        // 黄色
        else if (pricePerMeter < 120) {
            return "3";
        }
        // 红色
        else {
            return "4";
        }
    }

    private String buildStatistics(List<Subway> subways) {
        if (subways.isEmpty()) {
            return "无数据";
        }

        DoubleSummaryStatistics stats = subways.stream()
                .filter(Subway::hasValidPrice)
                .mapToDouble(Subway::getSquareMeterOfPrice)
                .summaryStatistics();

        long validCount = subways.stream()
                .filter(s -> s.hasValidLocation() && s.hasValidPrice())
                .count();

        return String.format(
                """
                        <div class='statistics'>
                          <h3>数据统计</h3>
                          <p>有效站点数: %d</p>
                          <p>平均租金: %.1f 元/㎡</p>
                          <p>最低租金: %.1f 元/㎡</p>
                          <p>最高租金: %.1f 元/㎡</p>
                          <p>显示价格为 %d 平米房屋月租</p>
                        </div>""",
                validCount,
                stats.getAverage(),
                stats.getMin(),
                stats.getMax(),
                config.getDefaultSquareMeter()
        );
    }

}