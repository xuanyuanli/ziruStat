package cn.xuanyuanli.rentradar.utils;

import cn.xuanyuanli.rentradar.config.AppConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 目录管理工具类.
 * 负责创建和管理项目输出目录结构
 * @author xuanyuanli
 */
public final class DirectoryUtils {

    private DirectoryUtils() {
        // 工具类不应被实例化
    }

    /**
     * 初始化项目输出目录结构
     */
    public static void initializeDirectories() throws IOException {
        AppConfig config = AppConfig.getInstance();

        // 创建基础目录
        createDirectoryIfNotExists(config.getBaseDir());
        createDirectoryIfNotExists(config.getDataDir());
        createDirectoryIfNotExists(config.getOutputDir());

        System.out.println("项目目录结构初始化完成");
        System.out.println("- 基础目录: " + config.getBaseDir());
        System.out.println("- 数据目录: " + config.getDataDir());
        System.out.println("- 输出目录: " + config.getOutputDir());
    }

    /**
     * 创建目录（如果不存在）
     */
    private static void createDirectoryIfNotExists(String dirPath) throws IOException {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
            System.out.println("创建目录: " + dirPath);
        }
    }

    /**
     * 清理临时文件和缓存
     */
    public static void cleanupTempFiles() {
        AppConfig config = AppConfig.getInstance();

        try {
            // 删除旧的生成文件
            deleteFileIfExists(config.getPriceJsonFile());
            deleteFileIfExists(config.getLocationJsonFile());
            deleteFileIfExists(config.getHtmlOutputFile());

            System.out.println("临时文件清理完成");
        } catch (Exception e) {
            System.out.println("清理临时文件时出错: " + e.getMessage());
        }
    }

    private static void deleteFileIfExists(String filePath) throws IOException {
        FileUtils.deleteFile(filePath);
    }
}