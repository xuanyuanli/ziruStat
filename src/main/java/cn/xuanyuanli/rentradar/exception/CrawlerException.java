package cn.xuanyuanli.rentradar.exception;

/**
 * 爬虫异常类<br>
 * 用于封装网页爬取过程中发生的各种异常情况<br>
 * 包括网络连接失败、页面解析错误、反爬虫阻拦等
 *
 * @author xuanyuanli
 */
public class CrawlerException extends Exception {

    /**
     * 构造函数
     * 
     * @param message 异常描述信息
     */
    public CrawlerException(String message) {
        super(message);
    }

    /**
     * 构造函数<br>
     * 包装其他异常为爬虫异常
     * 
     * @param message 异常描述信息
     * @param cause 原始异常原因
     */
    public CrawlerException(String message, Throwable cause) {
        super(message, cause);
    }
}