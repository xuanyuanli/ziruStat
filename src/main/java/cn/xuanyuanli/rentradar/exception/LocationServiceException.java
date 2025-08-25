package cn.xuanyuanli.rentradar.exception;

/**
 * 位置服务异常类<br>
 * 用于封装高德地图API调用过程中发生的各种异常情况<br>
 * 包括API密钥错误、网络请求失败、响应解析异常等
 *
 * @author xuanyuanli
 */
public class LocationServiceException extends Exception {

    /**
     * 构造函数
     * 
     * @param message 异常描述信息
     */
    public LocationServiceException(String message) {
        super(message);
    }

    /**
     * 构造函数<br>
     * 包装其他异常为位置服务异常
     * 
     * @param message 异常描述信息
     * @param cause 原始异常原因
     */
    public LocationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}