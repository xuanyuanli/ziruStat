package cn.xuanyuanli.rentradar.model;

import java.util.Objects;

public class POI {
    private String longitude;
    private String latitude;
    private String tel;
    private String postcode;
    
    public POI() {}
    
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
    
    // 工具方法
    public boolean isValid() {
        return longitude != null && !longitude.trim().isEmpty()
            && latitude != null && !latitude.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
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