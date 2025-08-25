package cn.xuanyuanli.rentradar.model;

import java.util.Objects;

/**
 * 地铁站数据模型<br>
 * 封装地铁站的完整信息，包括站点名称、线路、URL、价格和地理位置<br>
 * 提供数据有效性验证和便捷的显示方法
 *
 * @author xuanyuanli
 */
@SuppressWarnings("unused")
public class Subway {
    private String name;
    private String lineName;
    private String url;
    private double squareMeterOfPrice;
    private String longitude;
    private String latitude;

    public Subway() {
    }

    /**
     * 构造函数<br>
     * 创建包含基础信息的地铁站实例
     *
     * @param name 地铁站名称
     * @param lineName 地铁线路名称
     * @param url 自如网站对应的URL链接
     */
    public Subway(String name, String lineName, String url) {
        this.name = name;
        this.lineName = lineName;
        this.url = url;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getSquareMeterOfPrice() {
        return squareMeterOfPrice;
    }

    public void setSquareMeterOfPrice(double squareMeterOfPrice) {
        this.squareMeterOfPrice = squareMeterOfPrice;
    }

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

    /**
     * 检查是否具有有效的地理位置信息
     * 
     * @return 经纬度都不为空返回true，否则返回false
     */
    public boolean hasValidLocation() {
        return longitude != null && !longitude.trim().isEmpty()
                && latitude != null && !latitude.trim().isEmpty();
    }

    /**
     * 检查是否具有有效的价格信息
     * 
     * @return 每平方米价格大于0返回true，否则返回false
     */
    public boolean hasValidPrice() {
        return squareMeterOfPrice > 0;
    }

    /**
     * 获取用于显示的地铁站完整名称<br>
     * 格式为"线路名 站点名"，如"1号线 天安门东"
     * 
     * @return 格式化的显示名称
     */
    public String getDisplayName() {
        return lineName + " " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Subway subway = (Subway) o;
        return Objects.equals(name, subway.name)
                && Objects.equals(lineName, subway.lineName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, lineName);
    }

    @Override
    public String toString() {
        return "Subway{" +
                "name='" + name + '\'' +
                ", lineName='" + lineName + '\'' +
                ", url='" + url + '\'' +
                ", squareMeterOfPrice=" + squareMeterOfPrice +
                ", longitude='" + longitude + '\'' +
                ", latitude='" + latitude + '\'' +
                '}';
    }
}