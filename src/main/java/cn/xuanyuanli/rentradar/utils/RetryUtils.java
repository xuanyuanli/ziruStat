package cn.xuanyuanli.rentradar.utils;

import java.util.function.Supplier;

/**
 * 重试工具类
 * 提供带重试机制的操作执行
 */
public final class RetryUtils {
    
    private RetryUtils() {
        // 工具类不应被实例化
    }
    
    public static <T> T executeWithRetry(Supplier<T> operation, int maxAttempts, long delayMs) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.get();
            } catch (Exception e) {
                lastException = e;
                System.out.println("第" + attempt + "次尝试失败: " + e.getMessage());
                
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }
        
        throw new RuntimeException("操作失败，已重试" + maxAttempts + "次", lastException);
    }
    
    public static void executeWithRetryVoid(Runnable operation, int maxAttempts, long delayMs) {
        executeWithRetry(() -> {
            operation.run();
            return null;
        }, maxAttempts, delayMs);
    }
}