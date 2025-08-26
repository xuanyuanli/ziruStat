# Rent Radar - 北京租房价格雷达

## 项目背景

换公司后面临通勤问题，为了全面了解北京各地铁站周边的租房价格分布，开发了这个数据可视化工具。通过抓取自如网站数据，在高德地图上直观展示各地铁站附近的租房价格信息。

![效果图](images/QQ%E6%88%AA%E5%9B%BE20180424162421.png)

## 技术架构

**环境要求**
- JDK 21
- Maven 3.6+
- 目标城市：北京

**核心技术栈**
- **后端框架**: 基于 xuanyuanli/jujube-parent 3.1.2
- **网页爬虫**: playwright-stealth-pool + jsoup
- **OCR识别**: Tesseract（智能降级处理）
- **数据处理**: fastjson2
- **地图API**: 高德地图
- **测试框架**: JUnit Jupiter

## 设计思路

### 数据来源选择
在众多租房平台中（安居客、58、我爱我家、自如等），选择**自如**作为主要数据源，理由：
- 数据标准化程度高，便于处理
- 价格相对透明，可信度较高
- 服务标准化，数据质量稳定

### 实现流程
1. **数据采集**: 三级数据获取流程
   - 获取地铁站基础信息（站名、URL、线路）
   - 获取地理位置数据（调用高德地图API）
   - 获取租房价格数据（爬取自如网站）
2. **数据可视化**: 在地图上标记各站点价格信息
3. **智能缓存**: 分级缓存优化，避免重复请求

## 快速开始

### 1. 运行程序
```bash
# 编译项目
mvn compile

# 执行主程序
mvn exec:java -Dexec.mainClass="cn.xuanyuanli.rentradar.ZiruStatApplication"
```

### 2. 查看结果
程序运行完成后，会在 `build` 目录下生成：
```
build/
├── data/
│   ├── subway-stations.json     # 地铁站基础信息
│   ├── subway-locations.json    # 地铁站地理位置数据
│   └── subway-prices.json       # 地铁站租金价格数据
└── output/
    └── show.html               # 可视化地图页面
```

用浏览器打开 `build/output/show.html` 即可查看价格分布地图。


## 配置说明

### 配置优先级
系统支持多种配置方式，优先级如下：
1. **环境变量** (最高优先级)
2. **配置文件** (`application.properties`)
3. **默认值** (最低优先级)

### 主要配置项

#### 方式一：配置文件
编辑 `src/main/resources/application.properties`：

```properties
# 默认显示面积（平方米）
data.defaultSquareMeter=10

# 高德地图API配置
gaode.api.key=your_api_key_here
gaode.api.privateKey=your_private_key_here
gaode.api.city=010

# 爬虫行为配置
crawler.retry.maxAttempts=1

# 分级缓存配置
data.cache.enabled=true
# 地铁站基础信息缓存过期时间（天数）
data.cache.stations.expireDays=90
# 地铁站位置数据缓存过期时间（天数）- 依赖stations文件，stations不变则永久有效
data.cache.locations.expireDays=-1
# 地铁站价格数据缓存过期时间（天数）
data.cache.prices.expireDays=7

# 输出目录配置
data.output.baseDir=build
data.output.dataDir=build/data
data.output.outputDir=build/output

# 价格分析配置
price.analysis.minReasonablePrice=10.0
price.analysis.maxReasonablePrice=1000.0
```

#### 方式二：环境变量（推荐用于敏感信息）
```bash
# 高德地图API密钥（将点号转为下划线，转为大写）
export GAODE_API_KEY="your_api_key_here"
export GAODE_API_PRIVATEKEY="your_private_key_here"

# 其他配置示例
export DATA_DEFAULTSQUAREMETER="15"
export CRAWLER_RETRY_MAXATTEMPTS="5"
```

### 重新运行程序
如需重新抓取数据，可以选择删除对应的缓存文件：

```bash
# 删除所有缓存（完全重新抓取）
# Windows
rmdir /s build\data
# macOS/Linux  
rm -rf build/data

# 仅删除特定缓存文件
rm build/data/subway-stations.json    # 重新获取地铁站基础信息
rm build/data/subway-locations.json   # 重新获取位置数据  
rm build/data/subway-prices.json      # 重新获取价格数据
```

## 项目特色

### ⚙️ 灵活配置管理
- 支持环境变量配置，便于容器化部署
- 多层级配置优先级（环境变量 > 配置文件 > 默认值）
- 占位符解析支持，配置文件可引用其他配置项

### 🔄 智能分级缓存机制
- **三级缓存策略**: stations(90天) → locations(依赖stations) → prices(7天)
- **智能依赖管理**: 位置缓存依赖站点数据，自动失效机制
- **缓存代理模式**: 统一的缓存管理，业务逻辑与缓存逻辑分离
- **高效复用**: 避免重复网络请求，显著提升运行效率

### 🛡️ 反爬虫保护
- 使用 playwright-stealth-pool 规避检测
- 智能请求频率控制
- 支持代理和 User-Agent 轮换

### 📊 数据质量保障
- 多层数据验证和清洗
- 异常处理和重试机制
- 详细的执行日志和统计信息
- 价格合理性检查和过滤

### 🔍 智能OCR识别
- **Tesseract OCR引擎**: 基于tess4j的数字识别能力
- **智能降级处理**: OCR不可用时自动回退，保证系统稳定性
- **多路径Tessdata**: 自动检测多种可能的训练数据路径
- **图像预处理**: 放大、灰度化、对比度增强提升识别准确率
- **结果验证**: 数字过滤、长度限制、价格合理性检查

### 🎯 现代化架构设计
- **清晰的服务分层**: 配置层、服务层、工具层职责分离
- **缓存代理模式**: CacheManager 统一管理缓存策略
- **函数式编程**: 使用 Supplier 实现优雅的数据获取
- **依赖注入容器**: ServiceContainer 管理组件生命周期
- **易于测试**: Mock 友好的设计，完整的单元测试覆盖

## 注意事项

- ⚠️ 高德地图API有请求频率限制，大量数据获取需要时间
- ⚠️ 网页结构可能变化，影响爬虫效果
- ⚠️ 建议在网络稳定环境下运行
- ⚠️ 请合理使用，遵守网站robots.txt规则

## 项目结构

```
src/main/java/cn/xuanyuanli/rentradar/
├── ZiruStatApplication.java          # 主程序入口
├── config/AppConfig.java            # 配置管理
├── crawler/ZiroomCrawler.java        # 爬虫实现
├── service/                          # 服务层
│   ├── CacheManager.java             # 缓存管理器
│   ├── SimpleOCRService.java         # OCR识别服务（智能降级）
│   ├── SubwayDataService.java        # 数据收集服务
│   ├── LocationService.java          # 位置服务
│   └── VisualizationService.java     # 可视化服务
├── model/                           # 数据模型
├── exception/                       # 异常定义
└── utils/                          # 工具类
```

---

💡 **提示**: 首次运行需要联网获取数据，后续运行会使用本地缓存加速。
