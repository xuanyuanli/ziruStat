package cn.xuanyuanli.rentradar.model;

import java.util.Objects;

public class Subway {
    private String name;
    private String lineName;
    private String url;
    private double squareMeterOfPrice;
    private String longitude;
    private String latitude;
    
    public Subway() {}
    
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
    
    // 工具方法
    public boolean hasValidLocation() {
        return longitude != null && !longitude.trim().isEmpty() 
            && latitude != null && !latitude.trim().isEmpty();
    }
    
    public boolean hasValidPrice() {
        return squareMeterOfPrice > 0;
    }
    
    public String getDisplayName() {
        return lineName + " " + name;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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