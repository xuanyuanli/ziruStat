package cn.xuanyuanli.rentradar.utils;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class RetryUtilsTest {

    @Test
    void testExecuteWithRetry_SuccessOnFirstAttempt() {
        Supplier<String> operation = () -> "success";
        
        String result = RetryUtils.executeWithRetry(operation, 3, 100);
        
        assertEquals("success", result);
    }

    @Test
    void testExecuteWithRetry_SuccessOnSecondAttempt() {
        AtomicInteger attemptCount = new AtomicInteger(0);
        Supplier<String> operation = () -> {
            if (attemptCount.incrementAndGet() == 1) {
                throw new RuntimeException("第一次失败");
            }
            return "success";
        };
        
        String result = RetryUtils.executeWithRetry(operation, 3, 50);
        
        assertEquals("success", result);
        assertEquals(2, attemptCount.get());
    }

    @Test
    void testExecuteWithRetry_SuccessOnLastAttempt() {
        AtomicInteger attemptCount = new AtomicInteger(0);
        Supplier<String> operation = () -> {
            if (attemptCount.incrementAndGet() < 3) {
                throw new RuntimeException("前两次失败");
            }
            return "success";
        };
        
        String result = RetryUtils.executeWithRetry(operation, 3, 50);
        
        assertEquals("success", result);
        assertEquals(3, attemptCount.get());
    }

    @Test
    void testExecuteWithRetry_AllAttemptsFail() {
        AtomicInteger attemptCount = new AtomicInteger(0);
        Supplier<String> operation = () -> {
            attemptCount.incrementAndGet();
            throw new RuntimeException("总是失败");
        };
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            RetryUtils.executeWithRetry(operation, 3, 50)
        );
        
        assertTrue(exception.getMessage().contains("操作失败，已重试3次"));
        assertEquals(3, attemptCount.get());
        assertNotNull(exception.getCause());
        assertEquals("总是失败", exception.getCause().getMessage());
    }

    @Test
    void testExecuteWithRetry_SingleAttempt() {
        AtomicInteger attemptCount = new AtomicInteger(0);
        Supplier<String> operation = () -> {
            attemptCount.incrementAndGet();
            throw new RuntimeException("失败");
        };
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            RetryUtils.executeWithRetry(operation, 1, 50)
        );
        
        assertTrue(exception.getMessage().contains("操作失败，已重试1次"));
        assertEquals(1, attemptCount.get());
    }

    @Test
    void testExecuteWithRetry_DelayTiming() {
        AtomicInteger attemptCount = new AtomicInteger(0);
        long delayMs = 200;
        Supplier<String> operation = () -> {
            if (attemptCount.incrementAndGet() < 2) {
                throw new RuntimeException("需要重试");
            }
            return "success";
        };
        
        long startTime = System.currentTimeMillis();
        String result = RetryUtils.executeWithRetry(operation, 2, delayMs);
        long duration = System.currentTimeMillis() - startTime;
        
        assertEquals("success", result);
        assertTrue(duration >= delayMs, "应该至少延迟了指定时间");
        assertTrue(duration < delayMs + 100, "延迟时间不应该过长"); // 允许100ms误差
    }

    @Test
    void testExecuteWithRetry_InterruptedException() {
        Supplier<String> operation = () -> {
            throw new RuntimeException("失败");
        };
        
        // 先中断当前线程
        Thread.currentThread().interrupt();
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            RetryUtils.executeWithRetry(operation, 2, 1000)
        );
        
        assertTrue(exception.getMessage().contains("重试被中断"));
        assertTrue(Thread.currentThread().isInterrupted());
        
        // 清除中断状态以免影响其他测试
        Thread.interrupted();
    }

    @Test
    void testExecuteWithRetry_DifferentExceptionTypes() {
        AtomicInteger attemptCount = new AtomicInteger(0);
        Supplier<String> operation = () -> {
            int attempt = attemptCount.incrementAndGet();
            switch (attempt) {
                case 1:
                    throw new IllegalArgumentException("参数错误");
                case 2:
                    throw new IllegalStateException("状态错误");
                default:
                    return "success";
            }
        };
        
        String result = RetryUtils.executeWithRetry(operation, 3, 50);
        
        assertEquals("success", result);
        assertEquals(3, attemptCount.get());
    }

    @Test
    void testExecuteWithRetryVoid_Success() {
        AtomicInteger counter = new AtomicInteger(0);
        Runnable operation = () -> counter.incrementAndGet();
        
        RetryUtils.executeWithRetryVoid(operation, 3, 50);
        
        assertEquals(1, counter.get());
    }

    @Test
    void testExecuteWithRetryVoid_SuccessAfterRetry() {
        AtomicInteger attemptCount = new AtomicInteger(0);
        AtomicInteger successCount = new AtomicInteger(0);
        
        Runnable operation = () -> {
            if (attemptCount.incrementAndGet() == 1) {
                throw new RuntimeException("第一次失败");
            }
            successCount.incrementAndGet();
        };
        
        RetryUtils.executeWithRetryVoid(operation, 3, 50);
        
        assertEquals(2, attemptCount.get());
        assertEquals(1, successCount.get());
    }

    @Test
    void testExecuteWithRetryVoid_AllAttemptsFail() {
        AtomicInteger attemptCount = new AtomicInteger(0);
        Runnable operation = () -> {
            attemptCount.incrementAndGet();
            throw new RuntimeException("总是失败");
        };
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            RetryUtils.executeWithRetryVoid(operation, 2, 50)
        );
        
        assertTrue(exception.getMessage().contains("操作失败，已重试2次"));
        assertEquals(2, attemptCount.get());
    }

    @Test
    void testExecuteWithRetry_ReturnNull() {
        AtomicInteger attemptCount = new AtomicInteger(0);
        Supplier<String> operation = () -> {
            if (attemptCount.incrementAndGet() == 1) {
                throw new RuntimeException("第一次失败");
            }
            return null;
        };
        
        String result = RetryUtils.executeWithRetry(operation, 3, 50);
        
        assertNull(result);
        assertEquals(2, attemptCount.get());
    }

    @Test
    void testExecuteWithRetry_ZeroDelay() {
        AtomicInteger attemptCount = new AtomicInteger(0);
        Supplier<String> operation = () -> {
            if (attemptCount.incrementAndGet() < 3) {
                throw new RuntimeException("需要重试");
            }
            return "success";
        };
        
        long startTime = System.currentTimeMillis();
        String result = RetryUtils.executeWithRetry(operation, 3, 0);
        long duration = System.currentTimeMillis() - startTime;
        
        assertEquals("success", result);
        assertTrue(duration < 50, "零延迟应该很快完成");
    }

    @Test
    void testExecuteWithRetry_MaxAttemptsZero() {
        Supplier<String> operation = () -> "success";
        
        // maxAttempts为0应该不执行任何操作
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            RetryUtils.executeWithRetry(operation, 0, 50)
        );
        
        assertTrue(exception.getMessage().contains("操作失败，已重试0次"));
    }

    @Test
    void testExecuteWithRetry_LargeDelayInterrupted() {
        AtomicInteger attemptCount = new AtomicInteger(0);
        Supplier<String> operation = () -> {
            attemptCount.incrementAndGet();
            throw new RuntimeException("失败");
        };
        
        // 在另一个线程中中断当前线程
        Thread currentThread = Thread.currentThread();
        new Thread(() -> {
            try {
                Thread.sleep(100); // 等待重试开始
                currentThread.interrupt();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            RetryUtils.executeWithRetry(operation, 3, 5000)
        );
        
        assertTrue(exception.getMessage().contains("重试被中断"));
        assertEquals(1, attemptCount.get());
        
        // 清除中断状态
        Thread.interrupted();
    }
}