# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

这是一个Java项目，名为**Rent Radar（租房价格雷达）**，用于抓取北京地区自如租房数据，并在高德地图上可视化展示每个地铁站附近的房租价格信息。项目采用现代化的架构设计，具有清晰的职责分离、完善的异常处理和缓存机制。

## 技术栈

- **语言**: Java 21
- **构建工具**: Maven
- **项目结构**: 基于xuanyuanli的jujube-parent 3.1.2
- **主要依赖**:
  - playwright-stealth-pool 1.0.0 (网页自动化和反爬虫)
  - jsoup (HTML解析和网页爬取) 
  - fastjson2 (JSON序列化/反序列化)
  - junit-jupiter (单元测试)

## 核心架构

### 主要包结构

```
cn.xuanyuanli.rentradar/
├── ZiruStatApplication.java          # 主程序入口
├── config/
│   └── AppConfig.java               # 配置管理器
├── crawler/
│   └── ZiroomCrawler.java           # 自如网站爬虫
├── exception/
│   ├── CrawlerException.java        # 爬虫异常
│   └── LocationServiceException.java # 位置服务异常
├── model/
│   ├── POI.java                     # 地理位置数据模型
│   ├── RentalPrice.java             # 租房价格数据模型
│   └── Subway.java                  # 地铁站数据模型
├── service/
│   ├── LocationService.java         # 地理位置服务
│   ├── SubwayDataService.java       # 地铁数据服务
│   └── VisualizationService.java    # 可视化服务
└── utils/
    ├── DirectoryUtils.java          # 目录工具类
    ├── FileUtils.java              # 文件工具类
    ├── JsonUtils.java              # JSON工具类
    ├── RetryUtils.java             # 重试工具类
    └── ServiceContainer.java        # 服务容器
```

### 服务层架构

- **ZiruStatApplication**: 主程序入口，负责协调各服务
- **SubwayDataService**: 地铁数据服务，管理价格和位置数据收集
- **LocationService**: 地理位置服务，调用高德地图API获取经纬度
- **VisualizationService**: 可视化服务，生成HTML地图展示
- **ZiroomCrawler**: 爬虫服务，负责从自如网站获取租房数据

### 数据流程

1. **初始化**: 创建项目目录结构，加载配置
2. **数据收集**: 
   - 爬取自如网站获取地铁站链接和租房价格
   - 调用高德地图API获取地铁站经纬度坐标
3. **数据处理**: 清洗和验证数据，过滤无效记录
4. **缓存管理**: 将数据缓存到JSON文件避免重复请求
5. **可视化生成**: 基于模板生成包含地图标记的HTML文件

### 生成文件

运行程序会在`build`目录下生成结构化的文件：
```
build/
├── data/
│   ├── subway.json              # 地铁附近每平米房价信息
│   └── subwayLocation.json      # 地铁的地理位置数据
└── output/
    └── show.html               # 房价在地图上的分布信息展示页面
```

## 配置管理

项目使用`application.properties`进行配置管理，支持占位符解析：

### 关键配置项

- **gaode.api.key**: 高德地图API密钥
- **gaode.api.city**: 城市编码（默认北京 "010"）
- **data.defaultSquareMeter**: 默认显示面积（默认10平米）
- **crawler.retry.maxAttempts**: 爬虫重试次数
- **crawler.delay.betweenRequests**: 请求间隔时间
- **data.cache.enabled**: 是否启用缓存功能

## 常用命令

### 构建和运行
```bash
# 编译项目
mvn compile

# 运行主程序
mvn exec:java -Dexec.mainClass="cn.xuanyuanli.rentradar.ZiruStatApplication"

# 打包
mvn package

# 清理
mvn clean
```

### 测试
```bash
# 运行测试
mvn test
```

## 重要特性

### 缓存机制
- 支持数据缓存，避免重复抓取
- 如需重新运行完整流程，删除`build/data/`目录下的JSON文件

### 错误处理
- 完善的异常处理机制
- 支持重试机制，提高数据获取成功率
- 详细的日志输出，便于问题排查

### 反爬虫保护
- 使用playwright-stealth-pool库避免反爬虫检测
- 智能延迟控制，避免请求过于频繁
- 支持代理和User-Agent轮换

## 注意事项

- 高德地图API调用有频率限制，大量地铁站数据获取需要时间
- 网页爬取依赖目标网站结构，网站改版可能导致程序失效
- 确保网络连接稳定，避免数据获取中断
- 建议在非高峰期运行程序，提高成功率