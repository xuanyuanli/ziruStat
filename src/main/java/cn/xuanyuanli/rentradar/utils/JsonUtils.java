package cn.xuanyuanli.rentradar.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;

import java.util.List;

/**
 * JSON操作工具类<br>
 * 封装fastjson2的常用操作，提供统一的JSON序列化和反序列化方法<br>
 * 包括异常处理和数据有效性验证功能
 *
 * @author xuanyuanli
 */
public final class JsonUtils {

    private JsonUtils() {
        // 工具类不应被实例化
    }

    /**
     * 将对象序列化为JSON字符串<br>
     * 序列化失败时返回空对象"{}"
     * 
     * @param <T> 对象类型
     * @param object 要序列化的对象
     * @return JSON字符串，失败时返回"{}"
     */
    public static <T> String toJsonString(T object) {
        try {
            return JSON.toJSONString(object);
        } catch (JSONException e) {
            System.err.println("JSON序列化失败: " + e.getMessage());
            return "{}";
        }
    }

    /**
     * 将JSON字符串反序列化为指定类型的对象<br>
     * 反序列化失败时返回null
     * 
     * @param <T> 目标对象类型
     * @param jsonString JSON字符串
     * @param clazz 目标类的Class对象
     * @return 反序列化后的对象，失败时返回null
     */
    public static <T> T parseObject(String jsonString, Class<T> clazz) {
        try {
            return JSON.parseObject(jsonString, clazz);
        } catch (JSONException e) {
            System.err.println("JSON反序列化失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 将JSON数组字符串反序列化为指定类型的列表<br>
     * 反序列化失败时返回null
     * 
     * @param <T> 列表元素类型
     * @param jsonString JSON数组字符串
     * @param clazz 元素类的Class对象
     * @return 反序列化后的列表，失败时返回null
     */
    public static <T> List<T> parseArray(String jsonString, Class<T> clazz) {
        try {
            return JSON.parseArray(jsonString, clazz);
        } catch (JSONException e) {
            System.err.println("JSON数组反序列化失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 验证JSON字符串的有效性<br>
     * 检查给定的字符串是否为合法的JSON格式
     * 
     * @param jsonString 要验证的JSON字符串
     * @return JSON有效返回true，否则返回false
     */
    public static boolean isValidJson(String jsonString) {
        try {
            JSON.parse(jsonString);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }
}