package cn.xuanyuanli.rentradar.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 文件操作工具类.
 * 提供常用的文件读写操作
 * @author xuanyuanli
 */
public final class FileUtils {

    private FileUtils() {
        // 工具类不应被实例化
    }

    public static boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    public static void writeToFile(String filePath, String content) throws IOException {
        Path path = Paths.get(filePath);

        // 确保父目录存在
        Path parentDir = path.getParent();
        if (parentDir != null && !Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        // 如果文件不存在则创建
        if (!Files.exists(path)) {
            Files.createFile(path);
        }

        Files.writeString(path, content, StandardOpenOption.TRUNCATE_EXISTING);
        System.out.println("文件已写入: " + filePath);
    }

    public static String readFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("文件不存在: " + filePath);
        }

        return Files.readString(path);
    }

    public static List<String> readAllLines(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("文件不存在: " + filePath);
        }

        return Files.readAllLines(path);
    }

    public static void createFileIfNotExists(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            Files.createFile(path);
            System.out.println("创建文件: " + filePath);
        }
    }

    public static void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (Files.exists(path)) {
            Files.delete(path);
            System.out.println("删除文件: " + filePath);
        }
    }

    public static long getFileSize(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return 0;
        }
        return Files.size(path);
    }

    public static boolean isCacheExpired(String filePath, Duration maxAge) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return true;
        }

        FileTime lastModified = Files.getLastModifiedTime(path);
        Instant fileInstant = lastModified.toInstant();
        Instant now = Instant.now();

        Duration fileAge = Duration.between(fileInstant, now);
        return fileAge.compareTo(maxAge) > 0;
    }

    public static boolean isCacheExpired(String filePath, int daysToExpire) throws IOException {
        return isCacheExpired(filePath, Duration.ofDays(daysToExpire));
    }

    public static void deleteExpiredCache(String filePath, Duration maxAge) throws IOException {
        if (isCacheExpired(filePath, maxAge)) {
            deleteFile(filePath);
        }
    }

    public static void deleteExpiredCache(String filePath, int daysToExpire) throws IOException {
        deleteExpiredCache(filePath, Duration.ofDays(daysToExpire));
    }
}