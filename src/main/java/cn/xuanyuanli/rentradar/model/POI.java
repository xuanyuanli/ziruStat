package cn.xuanyuanli.rentradar.model;

import java.util.Objects;


/**
 * 地理位置兴趣点(Point Of Interest)数据模型<br>
 * 封装从高德地图API获取的地理位置信息，包括经纬度坐标<br>
 * 提供位置数据的有效性验证和基本操作方法
 *
 * @author xuanyuanli
 */
@SuppressWarnings("unused")
public class POI {
    private String longitude;
    private String latitude;
    private String tel;
    private String postcode;

    public POI() {
    }

    /**
     * 构造函数<br>
     * 使用经纬度坐标创建POI实例
     *
     * @param longitude 经度坐标
     * @param latitude 纬度坐标
     */
    public POI(String longitude, String latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    // Getters and Setters
    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    /**
     * 验证POI数据的有效性<br>
     * 检查经纬度坐标是否都不为空且不为空白字符串
     * 
     * @return 数据有效返回true，否则返回false
     */
    public boolean isValid() {
        return longitude != null && !longitude.trim().isEmpty()
                && latitude != null && !latitude.trim().isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        POI poi = (POI) o;
        return Objects.equals(longitude, poi.longitude)
                && Objects.equals(latitude, poi.latitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(longitude, latitude);
    }

    @Override
    public String toString() {
        return "POI{" +
                "longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                ", tel='" + tel + '\'' +
                ", postcode='" + postcode + '\'' +
                '}';
    }
}