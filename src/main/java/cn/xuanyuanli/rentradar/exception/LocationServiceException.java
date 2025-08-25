package cn.xuanyuanli.rentradar.exception;

/**
 * @author xuanyuanli
 */
public class LocationServiceException extends Exception {

    public LocationServiceException(String message) {
        super(message);
    }

    public LocationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}