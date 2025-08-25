package cn.xuanyuanli.rentradar.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

    @TempDir
    Path tempDir;

    private String testFilePath;
    private String nonExistentFilePath;

    @BeforeEach
    void setUp() {
        testFilePath = tempDir.resolve("test.txt").toString();
        nonExistentFilePath = tempDir.resolve("nonexistent.txt").toString();
    }

    @Test
    void testExists_FileExists() throws IOException {
        Files.createFile(Path.of(testFilePath));
        
        assertTrue(FileUtils.exists(testFilePath));
    }

    @Test
    void testExists_FileNotExists() {
        assertFalse(FileUtils.exists(nonExistentFilePath));
    }

    @Test
    void testWriteToFile_NewFile() throws IOException {
        String content = "测试内容\n第二行";
        
        FileUtils.writeToFile(testFilePath, content);
        
        assertTrue(Files.exists(Path.of(testFilePath)));
        String readContent = Files.readString(Path.of(testFilePath));
        assertEquals(content, readContent);
    }

    @Test
    void testWriteToFile_OverwriteExisting() throws IOException {
        String originalContent = "原始内容";
        String newContent = "新内容";
        
        Files.writeString(Path.of(testFilePath), originalContent);
        FileUtils.writeToFile(testFilePath, newContent);
        
        String readContent = Files.readString(Path.of(testFilePath));
        assertEquals(newContent, readContent);
    }

    @Test
    void testWriteToFile_CreateDirectories() throws IOException {
        Path nestedPath = tempDir.resolve("nested").resolve("deep").resolve("test.txt");
        String content = "测试内容";
        
        FileUtils.writeToFile(nestedPath.toString(), content);
        
        assertTrue(Files.exists(nestedPath));
        assertEquals(content, Files.readString(nestedPath));
    }

    @Test
    void testReadFromFile_Success() throws IOException {
        String content = "测试读取内容\n包含中文和特殊字符!@#$%";
        Files.writeString(Path.of(testFilePath), content);
        
        String readContent = FileUtils.readFromFile(testFilePath);
        
        assertEquals(content, readContent);
    }

    @Test
    void testReadFromFile_FileNotExists() {
        IOException exception = assertThrows(IOException.class, () -> 
            FileUtils.readFromFile(nonExistentFilePath)
        );
        
        assertTrue(exception.getMessage().contains("文件不存在"));
    }

    @Test
    void testReadAllLines_Success() throws IOException {
        List<String> lines = Arrays.asList("第一行", "第二行", "第三行包含中文");
        Files.write(Path.of(testFilePath), lines);
        
        List<String> readLines = FileUtils.readAllLines(testFilePath);
        
        assertEquals(lines, readLines);
    }

    @Test
    void testReadAllLines_EmptyFile() throws IOException {
        Files.createFile(Path.of(testFilePath));
        
        List<String> readLines = FileUtils.readAllLines(testFilePath);
        
        assertTrue(readLines.isEmpty());
    }

    @Test
    void testReadAllLines_FileNotExists() {
        IOException exception = assertThrows(IOException.class, () -> 
            FileUtils.readAllLines(nonExistentFilePath)
        );
        
        assertTrue(exception.getMessage().contains("文件不存在"));
    }

    @Test
    void testCreateFileIfNotExists_NewFile() throws IOException {
        FileUtils.createFileIfNotExists(testFilePath);
        
        assertTrue(Files.exists(Path.of(testFilePath)));
        assertTrue(Files.isRegularFile(Path.of(testFilePath)));
    }

    @Test
    void testCreateFileIfNotExists_ExistingFile() throws IOException {
        String originalContent = "原始内容";
        Files.writeString(Path.of(testFilePath), originalContent);
        
        FileUtils.createFileIfNotExists(testFilePath);
        
        String content = Files.readString(Path.of(testFilePath));
        assertEquals(originalContent, content);
    }

    @Test
    void testDeleteFile_ExistingFile() throws IOException {
        Files.createFile(Path.of(testFilePath));
        assertTrue(Files.exists(Path.of(testFilePath)));
        
        FileUtils.deleteFile(testFilePath);
        
        assertFalse(Files.exists(Path.of(testFilePath)));
    }

    @Test
    void testDeleteFile_NonExistentFile() throws IOException {
        // 删除不存在的文件应该不报错
        assertDoesNotThrow(() -> FileUtils.deleteFile(nonExistentFilePath));
    }

    @Test
    void testGetFileSize_ExistingFile() throws IOException {
        String content = "测试文件大小";
        Files.writeString(Path.of(testFilePath), content);
        
        long size = FileUtils.getFileSize(testFilePath);
        
        assertTrue(size > 0);
        assertEquals(content.getBytes().length, size);
    }

    @Test
    void testGetFileSize_NonExistentFile() throws IOException {
        long size = FileUtils.getFileSize(nonExistentFilePath);
        
        assertEquals(0, size);
    }

    @Test
    void testGetFileSize_EmptyFile() throws IOException {
        Files.createFile(Path.of(testFilePath));
        
        long size = FileUtils.getFileSize(testFilePath);
        
        assertEquals(0, size);
    }

    @Test
    void testIsCacheExpired_NonExistentFile() throws IOException {
        boolean expired = FileUtils.isCacheExpired(nonExistentFilePath, Duration.ofDays(1));
        
        assertTrue(expired, "不存在的文件应该被认为是过期的");
    }

    @Test
    void testIsCacheExpired_FreshFile() throws IOException {
        Files.createFile(Path.of(testFilePath));
        
        boolean expired = FileUtils.isCacheExpired(testFilePath, Duration.ofDays(1));
        
        assertFalse(expired, "刚创建的文件应该不过期");
    }

    @Test
    void testIsCacheExpired_ZeroDuration() throws IOException {
        Files.createFile(Path.of(testFilePath));
        // 等待一毫秒确保文件创建时间在过去
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        boolean expired = FileUtils.isCacheExpired(testFilePath, Duration.ZERO);
        
        assertTrue(expired, "零持续时间应该让文件立即过期");
    }

    @Test
    void testIsCacheExpired_DaysOverload() throws IOException {
        Files.createFile(Path.of(testFilePath));
        
        boolean expired = FileUtils.isCacheExpired(testFilePath, 7);
        
        assertFalse(expired, "刚创建的文件在7天内不应该过期");
    }

    @Test
    void testDeleteExpiredCache_ExpiredFile() throws IOException, InterruptedException {
        Files.createFile(Path.of(testFilePath));
        
        // 等待一小段时间确保文件创建时间在过去
        Thread.sleep(10);
        
        FileUtils.deleteExpiredCache(testFilePath, Duration.ofMillis(1));
        
        assertFalse(Files.exists(Path.of(testFilePath)), "过期的缓存文件应该被删除");
    }

    @Test
    void testDeleteExpiredCache_FreshFile() throws IOException {
        Files.createFile(Path.of(testFilePath));
        
        FileUtils.deleteExpiredCache(testFilePath, Duration.ofDays(1));
        
        assertTrue(Files.exists(Path.of(testFilePath)), "未过期的缓存文件不应该被删除");
    }

    @Test
    void testDeleteExpiredCache_DaysOverload() throws IOException, InterruptedException {
        Files.createFile(Path.of(testFilePath));
        
        // 等待一小段时间确保文件创建时间在过去
        Thread.sleep(10);
        
        FileUtils.deleteExpiredCache(testFilePath, 0);
        
        assertFalse(Files.exists(Path.of(testFilePath)), "0天过期时间应该删除文件");
    }

    @Test
    void testFileOperations_WithSpecialCharacters() throws IOException {
        String specialPath = tempDir.resolve("测试文件(带特殊字符).txt").toString();
        String content = "内容包含特殊字符：@#￥%…&*（）—+";
        
        FileUtils.writeToFile(specialPath, content);
        String readContent = FileUtils.readFromFile(specialPath);
        
        assertEquals(content, readContent);
        assertTrue(FileUtils.exists(specialPath));
        
        FileUtils.deleteFile(specialPath);
        assertFalse(FileUtils.exists(specialPath));
    }

    @Test
    void testWriteReadCycle_LargeContent() throws IOException {
        StringBuilder largeContent = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            largeContent.append("这是第").append(i).append("行内容\n");
        }
        
        FileUtils.writeToFile(testFilePath, largeContent.toString());
        String readContent = FileUtils.readFromFile(testFilePath);
        
        assertEquals(largeContent.toString(), readContent);
        assertTrue(FileUtils.getFileSize(testFilePath) > 100000);
    }
}