package com.exercise.AndroidWifiMonitor;

public class AccessPoint {
	private float rssi;
	private String bssid;		//is this needed? Since the map is being used anyway..
	private float weight;		//function - use weight to find the average at the end
	AccessPoint() {
		rssi = 0;
		bssid = "NULL";	
		weight = 0;	
	}
	
	AccessPoint(float rssi, String bssid, int weight) {
		this.rssi = rssi;
		this.bssid = bssid;
		this.weight = weight;
	}

	public float getRssi() {
		return rssi;
	}
	public void setRssi(float rssi) {
		this.rssi = rssi;
	}
	public String getBssid() {
		return bssid;
	}
	public void setBssid(String bssid) {
		this.bssid = bssid;
	}
	public float getWeight() {
		return weight;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
}
