package cn.xuanyuanli.rentradar;

import cn.xuanyuanli.rentradar.utils.PriceSpriteDecoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NormalTest {
    public static void main(String[] args) {
        List<Map<String, Object>> unknownSpriteData = new ArrayList<>();
        unknownSpriteData.add(createSpanData("background-image: url('unknown_sprite_hash_12345.png'); background-position: -21.4px center;"));
        // 验证能正确识别为未知精灵图
        String decodePrice = PriceSpriteDecoder.decodePrice(unknownSpriteData);
        System.out.println("识别结果: " + decodePrice);
    }
    private static Map<String, Object> createSpanData(String style) {
        Map<String, Object> spanData = new HashMap<>();
        spanData.put("style", style);
        return spanData;
    }
}
