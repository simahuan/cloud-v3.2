package com.pisen.router.core.monitor.entity;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/**
 * wifi信息获取
 * 
 * @author MouJunFeng
 * @version 1.0, 2014-6-3 上午10:08:38
 */
public class WifiInformationUtils {
	public static boolean isWifiConnect(Context context) {
		WifiManager mWifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if(mWifi.isWifiEnabled()){
			WifiInfo info = mWifi.getConnectionInfo();
			if(info != null){
				String mac = "3c:40:4f";
				String bssid = info.getBSSID();
				if (bssid != null && !bssid.equals("")) {
				    if (bssid.contains(mac)) {
					return true;
				    }
				}
			}
		}
		return false;
	}
}
