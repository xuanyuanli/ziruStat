package cn.xuanyuanli.rentradar.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;

import java.util.List;

/**
 * JSON操作工具类
 * 封装fastjson2的常用操作
 */
public final class JsonUtils {
    
    private JsonUtils() {
        // 工具类不应被实例化
    }
    
    public static <T> String toJsonString(T object) {
        try {
            return JSON.toJSONString(object);
        } catch (JSONException e) {
            System.err.println("JSON序列化失败: " + e.getMessage());
            return "{}";
        }
    }
    
    public static <T> T parseObject(String jsonString, Class<T> clazz) {
        try {
            return JSON.parseObject(jsonString, clazz);
        } catch (JSONException e) {
            System.err.println("JSON反序列化失败: " + e.getMessage());
            return null;
        }
    }
    
    public static <T> List<T> parseArray(String jsonString, Class<T> clazz) {
        try {
            return JSON.parseArray(jsonString, clazz);
        } catch (JSONException e) {
            System.err.println("JSON数组反序列化失败: " + e.getMessage());
            return null;
        }
    }
    
    public static boolean isValidJson(String jsonString) {
        try {
            JSON.parse(jsonString);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }
}