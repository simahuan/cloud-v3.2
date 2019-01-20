package com.pisen.router.core.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Observable;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.studio.os.LogCat;
import android.text.format.Formatter;

import com.pisen.router.config.WifiConfig;
import com.pisen.router.core.monitor.WifiMonitor.WifiStateCallback;

/**
 * Pisen Wifi监听器
 * 
 * @author yangyp
 */
public class WifiMonitor extends Observable<WifiStateCallback> implements IMonitor {

	public interface WifiStateCallback {
		/**
		 * 网络已连接
		 */
		void onConnected(WifiConfig config);

		/**
		 * 网络已断开
		 */
		void onDisconnected(WifiConfig config);
	}
	
	/** 过滤品胜路由器*/
	public static final String PISEN_BSSID_PREFIX = "3c:40:4f";
	private boolean connected = false;
	private WifiConfig wifiConfig;
	static WifiMonitor instance = null;

	private WifiMonitor() {
	}

	public static WifiMonitor getInstance() {
		if (instance == null) {
			instance = new WifiMonitor();
		}
		return instance;
	}

	@Override
	public void startMonitor(Context context) {
		context.registerReceiver(wifiReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}

	@Override
	public void stopMonitor(Context context) {
		context.unregisterReceiver(wifiReceiver);
		unregisterAll();
	}

	public boolean isWifiConnected() {
		return connected;
	}

	public boolean isPisenWifiConnected() {
		return wifiConfig != null && wifiConfig.isPisenWifi();
	}

	@Deprecated
	public WifiConfig getWifiConfig() {
		return wifiConfig;
	}

	/**
	 * 网络已连接
	 */
	public void notifyConnected(WifiConfig config) {
		synchronized (mObservers) {
			for (WifiStateCallback observer : mObservers) {
				observer.onConnected(config);
			}
		}
	}

	/**
	 * 网络已断开
	 */
	public void notifyDisconnected(WifiConfig config) {
		synchronized (mObservers) {
			for (WifiStateCallback observer : mObservers) {
				observer.onDisconnected(config);
			}
		}
	}

	private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LogCat.i("ConnectivityReceiver.onReceive()...");
			ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
			if (netInfo != null && netInfo.isConnected()) {
				LogCat.i("Network connected Type  = " + netInfo.getTypeName() + ", State = " + netInfo.getState());
				connected(context, netInfo);
			} else {
				LogCat.i("Network unavailable");
				disconnected();
			}
		}
	};

	private void connected(Context context, NetworkInfo netInfo) {
		connected = true;
		int netType = netInfo.getType();
		if (netType == ConnectivityManager.TYPE_WIFI) {
			WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			if (wifiInfo != null) {
				DhcpInfo info = wifiManager.getDhcpInfo();
				String gateway = (info != null) ? Formatter.formatIpAddress(info.gateway) : null;
				wifiConfig = new WifiConfig(wifiInfo, gateway);
				// String bssid = wifiConfig.getBSSID();
				// if (bssid != null && bssid.contains(PISEN_BSSID_PREFIX)) {
				notifyConnected(wifiConfig);
				// }
			}
		}
	}

	private void disconnected() {
		if (wifiConfig != null) {
			connected = false;
			notifyDisconnected(wifiConfig);
			wifiConfig = null;
		}
	}
}
