package org.dadazao.zirustat;

/** 地铁信息 */
public class Subway {
	private String name;
	private String lineName; // 线路名称
	private String url;
	private double squareMeterOfPrice; // 每平米每月价格

	private String longitude; // 经度
	private String latitude; // 纬度

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLineName() {
		return lineName;
	}

	public void setLineName(String lineName) {
		this.lineName = lineName;
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

	@Override
	public String toString() {
		return "Subway [name=" + name + ", lineName=" + lineName + ", url=" + url + ", squareMeterOfPrice="
				+ squareMeterOfPrice + ", longitude=" + longitude + ", latitude=" + latitude + "]";
	}

}
