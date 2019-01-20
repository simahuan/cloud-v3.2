package com.pisen.router.config;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

/**
 * 获取Wifi配置信息
 * 
 * @author yangyp
 * @created 2015/05/27
 */
public class WifiConfig {

	public static final String PISEN_BSSID_PREFIX = "3c:40:4f";

	private WifiInfo wifiInfo;
	private String gatewayIp; // 网关IP

	public WifiConfig(WifiInfo wifiInfo, String gatewayIp) {
		this.wifiInfo = wifiInfo;
		this.gatewayIp = gatewayIp;
	}

	public boolean isPisenWifi() {
		return getBSSID() != null && getBSSID().contains(PISEN_BSSID_PREFIX);
	}

	/**
	 * Wifi名称
	 * 
	 * @return
	 */
	public String getSSID() {
		String ssid = wifiInfo.getSSID();
		if (!TextUtils.isEmpty(ssid)) {
			if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
				ssid = ssid.replace("\"", "");
			}
		}
		return ssid;
	}

	/**
	 * Wifi信号强度
	 * 
	 * @param numLevels
	 * @return
	 */
	public int getSignalLevel(int numLevels) {
		return WifiManager.calculateSignalLevel(wifiInfo.getRssi(), 5);
	}

	/**
	 * Wifi Mac地址
	 * 
	 * @return the BSSID, in the form of a six-byte MAC address:
	 *         {@code XX:XX:XX:XX:XX:XX}
	 */
	public String getBSSID() {
		return wifiInfo.getBSSID();
	}

	/**
	 * Wifi速度
	 * 
	 * @return
	 */
	public int getLinkSpeed() {
		return wifiInfo.getLinkSpeed();
	}

	public String getGatewayIp() {
		return gatewayIp;
	}

}
