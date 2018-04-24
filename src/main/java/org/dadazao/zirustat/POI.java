package org.dadazao.zirustat;

/** 地图POI数据 */
public class POI {
	private String longitude; // 经度
	private String latitude; // 纬度
	private String tel;
	private String postcode;

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

	@Override
	public String toString() {
		return "POI [longitude=" + longitude + ", latitude=" + latitude + ", tel=" + tel + ", postcode=" + postcode
				+ "]";
	}

}
