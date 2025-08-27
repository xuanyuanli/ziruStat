# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个Java项目，名为**Rent Radar（租房价格雷达）**，用于抓取北京地区自如租房数据，并在高德地图上可视化展示每个地铁站附近的房租价格信息。项目采用现代化的架构设计，具有清晰的职责分离、完善的异常处理和缓存机制。

## 技术栈

**环境要求**
- JDK 21
- Maven 3.6+
- 目标城市：北京

**核心技术栈**
- **后端框架**: 基于 xuanyuanli/jujube-parent 3.1.2
- **网页爬虫**: playwright-stealth-pool + jsoup
- **精灵图解码**: CSS精灵图智能识别系统
- **数据处理**: fastjson2
- **地图API**: 高德地图
- **测试框架**: JUnit Jupiter

## 核心架构

### 主要包结构

**Package**: `cn.xuanyuanli.rentradar`

**主程序入口**
- `ZiruStatApplication.java` - 应用程序主入口点，协调各个服务

**配置管理层** (`config/`)
- `AppConfig.java` - 配置管理器，处理应用配置文件和环境变量

**数据爬虫层** (`crawler/`)
- `ZiroomCrawler.java` - 自如网站数据爬虫实现

**异常处理层** (`exception/`)
- `CrawlerException.java` - 爬虫相关异常
- `LocationServiceException.java` - 地理位置服务异常

**数据模型层** (`model/`)
- `POI.java` - 地理位置点信息模型
- `RentalPrice.java` - 租房价格数据模型
- `Subway.java` - 地铁站信息模型

**服务层** (`service/`)
- `CacheManager.java` - 缓存管理器，统一管理数据缓存策略
- `SubwayDataService.java` - 地铁数据收集和处理服务
- `LocationService.java` - 地理位置服务，调用高德地图API
- `VisualizationService.java` - 数据可视化服务，生成HTML地图

**工具类层** (`utils/`)
- `DirectoryUtils.java` - 目录操作工具
- `FileUtils.java` - 文件操作工具
- `JsonUtils.java` - JSON处理工具
- `RetryUtils.java` - 重试机制工具
- `ServiceContainer.java` - 依赖注入容器
- `PriceSpriteDecoder.java` - CSS精灵图价格解码器

### 核心特性

**🔄 智能分级缓存机制**
- **三级缓存策略**: stations(90天) → locations(依赖stations) → prices(7天)
- **智能依赖管理**: 位置缓存依赖站点数据，自动失效机制
- **缓存代理模式**: CacheManager 统一管理缓存策略
- **高效复用**: 避免重复网络请求，显著提升运行效率

**⚙️ 灵活配置管理**
- 支持环境变量配置，便于容器化部署
- 多层级配置优先级（环境变量 > 配置文件 > 默认值）
- 占位符解析支持，配置文件可引用其他配置项

**🛡️ 反爬虫保护**
- 使用 playwright-stealth-pool 规避检测
- 智能请求频率控制
- 支持代理和 User-Agent 轮换

**🔍 精灵图智能识别系统**
- **JSON配置驱动**: 精灵图映射数据存储在META-INF/sprite目录下的JSON文件中
- **动态配置加载**: 启动时自动扫描并加载所有精灵图配置文件
- **自动格式兼容**: 智能处理-107px和-107.0px等不同CSS格式
- **用户交互扩展**: 遇到未知精灵图时引导用户手动输入配置信息
- **配置持久化**: 新识别的精灵图配置自动保存到文件系统
- **测试环境友好**: 自动检测测试环境，避免用户交互阻塞测试运行

**🎯 现代化架构设计**
- **清晰的服务分层**: 配置层、服务层、工具层职责分离
- **缓存代理模式**: CacheManager 统一管理缓存策略
- **函数式编程**: 使用 Supplier 实现优雅的数据获取
- **依赖注入容器**: ServiceContainer 管理组件生命周期

### 数据流程

1. **数据采集**: 三级数据获取流程
   - 获取地铁站基础信息（站名、URL、线路）
   - 获取地理位置数据（调用高德地图API）
   - 获取租房价格数据（爬取自如网站）
2. **数据可视化**: 在地图上标记各站点价格信息
3. **智能缓存**: 分级缓存优化，避免重复请求

### 精灵图配置文件结构

项目使用JSON文件管理精灵图配置，位于`src/main/resources/META-INF/sprite/`：

```
META-INF/sprite/
├── a9da4f199beb8d74bffa9500762fd7b7.json  # 第一版精灵图
├── f4c1f82540f8d287aa53492a44f5819b.json  # 第二版精灵图  
├── img_pricenumber_list_red.json          # 红色版精灵图
└── c4b718a0002eb143ea3484b373071495.json  # 新版精灵图
```

**配置文件格式**:
```json
{
  "identifier": "a9da4f199beb8d74bffa9500762fd7b7",
  "description": "第一版精灵图",
  "pixelInterval": 21.4,
  "digitOrder": "8670415923",
  "mapping": {
    "0px": "8",
    "-21.4px": "6",
    "-42.8px": "7"
  }
}
```

### 生成文件

运行程序会在`build`目录下生成结构化的文件：
```
build/
├── data/
│   ├── subway-stations.json     # 地铁站基础信息
│   ├── subway-locations.json    # 地铁站地理位置数据
│   └── subway-prices.json       # 地铁站租金价格数据
└── output/
    └── show.html               # 可视化地图页面
```

## 配置管理

**配置优先级**
系统支持多种配置方式，优先级如下：
1. **环境变量** (最高优先级)
2. **配置文件** (`application.properties`)
3. **默认值** (最低优先级)

**主要配置项**
- `data.defaultSquareMeter=10` - 默认显示面积（平方米）
- `gaode.api.key` - 高德地图API密钥
- `gaode.api.privateKey` - 高德地图API私钥
- `crawler.retry.maxAttempts=3` - 爬虫重试次数
- `data.cache.enabled=true` - 是否启用缓存
- `data.cache.*.expireDays` - 各级缓存过期时间配置

**运行命令**
```bash
# 编译项目
mvn compile

# 执行主程序
mvn exec:java -Dexec.mainClass="cn.xuanyuanli.rentradar.ZiruStatApplication"
```

## 规则
- properties 文件编码是ISO-8859-1，写入中文需要注意编码问题
- 使用中文回答问题
- 总是使用最新的JDK API（比如HttpClient等）和框架API