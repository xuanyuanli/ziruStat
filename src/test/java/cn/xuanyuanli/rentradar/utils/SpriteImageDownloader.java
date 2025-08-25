package cn.xuanyuanli.rentradar.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 精灵图下载工具类
 * 用于下载自如网站的真实精灵图到测试资源目录
 * 
 * @author xuanyuanli
 */
public class SpriteImageDownloader {
    
    private static final String[] SPRITE_URLS = {
        "https://static8.ziroom.com/phoenix/pc/images/price/new-list/a9da4f199beb8d74bffa9500762fd7b7.png",
        "https://static8.ziroom.com/phoenix/pc/images/price/new-list/f4c1f82540f8d287aa53492a44f5819b.png", 
        "https://static8.ziroom.com/phoenix/pc/images/2020/list/img_pricenumber_list_red.png"
    };
    
    private static final String[] FILE_NAMES = {
        "sprite_v1_a9da4f199beb8d74bffa9500762fd7b7.png",
        "sprite_v2_f4c1f82540f8d287aa53492a44f5819b.png",
        "sprite_red_img_pricenumber_list_red.png"
    };
    
    /**
     * 下载所有精灵图到test/resources目录
     */
    public static void downloadAllSprites() {
        String resourcesPath = "src/test/resources/sprites/";
        
        // 创建目录
        try {
            Path spritesDir = Paths.get(resourcesPath);
            Files.createDirectories(spritesDir);
            System.out.println("创建精灵图目录: " + spritesDir.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("创建目录失败: " + e.getMessage());
            return;
        }
        
        // 下载每个精灵图
        for (int i = 0; i < SPRITE_URLS.length; i++) {
            String url = SPRITE_URLS[i];
            String fileName = FILE_NAMES[i];
            String filePath = resourcesPath + fileName;
            
            try {
                downloadImage(url, filePath);
                System.out.println("下载成功: " + fileName);
            } catch (IOException e) {
                System.err.println("下载失败 " + fileName + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * 下载单个图片
     */
    private static void downloadImage(String imageUrl, String filePath) throws IOException {
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        // 设置请求头，模拟浏览器行为
        connection.setRequestProperty("User-Agent", 
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        connection.setRequestProperty("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
        connection.setRequestProperty("Referer", "https://www.ziroom.com/");
        
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(30000);
        
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP错误代码: " + responseCode);
        }
        
        // 检查Content-Type
        String contentType = connection.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("不是有效的图片文件，Content-Type: " + contentType);
        }
        
        // 下载文件
        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(filePath)) {
            
            byte[] buffer = new byte[8192];
            int bytesRead;
            long totalBytes = 0;
            
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytes += bytesRead;
            }
            
            System.out.printf("文件大小: %.2f KB%n", totalBytes / 1024.0);
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * 验证已下载的精灵图文件
     */
    public static boolean validateDownloadedSprites() {
        String resourcesPath = "src/test/resources/sprites/";
        boolean allValid = true;
        
        for (String fileName : FILE_NAMES) {
            Path filePath = Paths.get(resourcesPath + fileName);
            
            if (!Files.exists(filePath)) {
                System.err.println("精灵图文件不存在: " + fileName);
                allValid = false;
                continue;
            }
            
            try {
                long fileSize = Files.size(filePath);
                if (fileSize < 1024) { // 小于1KB认为无效
                    System.err.println("精灵图文件太小，可能损坏: " + fileName + " (size: " + fileSize + " bytes)");
                    allValid = false;
                } else {
                    System.out.println("精灵图文件有效: " + fileName + " (size: " + fileSize + " bytes)");
                }
            } catch (IOException e) {
                System.err.println("检查文件失败: " + fileName + " - " + e.getMessage());
                allValid = false;
            }
        }
        
        return allValid;
    }
    
    /**
     * 获取本地精灵图文件路径
     */
    public static String[] getLocalSpritePaths() {
        String resourcesPath = "src/test/resources/sprites/";
        String[] paths = new String[FILE_NAMES.length];
        
        for (int i = 0; i < FILE_NAMES.length; i++) {
            paths[i] = resourcesPath + FILE_NAMES[i];
        }
        
        return paths;
    }
    
    /**
     * 主方法，用于手动下载精灵图
     */
    public static void main(String[] args) {
        System.out.println("开始下载精灵图...");
        downloadAllSprites();
        
        System.out.println("\n验证下载结果...");
        boolean valid = validateDownloadedSprites();
        
        if (valid) {
            System.out.println("\n✓ 所有精灵图下载成功！");
        } else {
            System.out.println("\n✗ 部分精灵图下载失败，请检查网络连接或URL是否有效");
        }
    }
}