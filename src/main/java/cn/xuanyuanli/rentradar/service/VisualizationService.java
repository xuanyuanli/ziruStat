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
 * @author xuanyuanli
 */
public class VisualizationService {

    private final AppConfig config;

    public VisualizationService() {
        this.config = AppConfig.getInstance();
    }

    public void generateHtmlVisualization(List<Subway> subways) throws IOException {
        System.out.println("开始生成HTML可视化...");

        String template = loadTemplate();
        String markers = buildMarkers(subways);
        String statistics = buildStatistics(subways);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String html = template
                .replace("{{MARKERS}}", markers)
                .replace("{{STATISTICS}}", statistics)
                .replace("{{GENERATED_TIME}}", timestamp);

        String outputFile = config.getHtmlOutputFile();
        FileUtils.writeToFile(outputFile, html);

        System.out.println("HTML可视化已生成: " + outputFile);
        System.out.println("包含 " + subways.size() + " 个地铁站的价格信息");
    }

    private String loadTemplate() {
        // 尝试从resources加载模板
        String templatePath = config.getMapTemplate();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(templatePath)) {
            if (input != null) {
                return new String(input.readAllBytes(), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            System.out.println("模板文件加载失败: " + templatePath + ", 使用默认模板");
        }

        // 如果资源文件不存在，使用内置模板
        return getDefaultTemplate();
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

    private String getDefaultTemplate() {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="utf-8">
                    <title>北京地铁租房价格分布图</title>
                    <script src="https://webapi.amap.com/maps?v=1.4.15&key=YOUR_KEY"></script>
                    <style>
                        body { margin: 0; padding: 0; font-family: Arial, sans-serif; }
                        #container { height: 100vh; width: 100%; }
                        .statistics { position: absolute; top: 10px; right: 10px; z-index: 999; background: white; padding: 15px; border-radius: 5px; box-shadow: 0 2px 5px rgba(0,0,0,0.2); }
                        .info { position: absolute; bottom: 10px; left: 10px; z-index: 999; background: white; padding: 10px; border-radius: 5px; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div id="container"></div>
                    {{STATISTICS}}
                    <div class='info'>生成时间: {{GENERATED_TIME}}</div>
                   \s
                    <script>
                        var map = new AMap.Map('container', {
                            zoom: 10,
                            center: [116.397428, 39.90923]
                        });
                       \s
                        // 地铁站标记
                        {{MARKERS}}
                    </script>
                </body>
                </html>""";
    }
}