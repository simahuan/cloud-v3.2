package com.pisen.router.ui.phone.device;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.studio.os.LogCat;
import android.text.TextUtils;
import android.util.Log;

import com.pisen.router.config.ResourceConfig;
import com.pisen.router.core.monitor.WifiMonitor;
import com.pisen.router.core.monitor.entity.RouterConfig.Model;
import com.pisen.router.ui.phone.leftmenu.LeftMenuFragment;

public class WifiConnectUtils {

	private static final String TAG = "WifiConnectUtils";

	public enum EncryptionType {
		TYPE_NO_PASSWD, TYPE_WEP, TYPE_WPA
	}

	// public static final int TYPE_NO_PASSWD = 0x11;
	// public static final int TYPE_WEP = 0x12;
	// public static final int TYPE_WPA = 0x13;

	public static final int WIFI_CONNECTED = 0x01;
	public static final int WIFI_CONNECT_FAILED = 0x02;
	public static final int WIFI_CONNCTING = 0x03;
	public static final int WIFI_DEVICE_RESTART = 0x04;
	public static final int WIFI_ERROR = 0x5;
	public static final int WIFI_CONNECTED_TIMEOUT = 0x06;
	public static final int WIFI_OPEN_WIFI = 0x07;
	public static final int WIFI_OPEN_WIFI_FAILED = 0x08;
	public static final int WIFI_REFRESH = 0x09;
	public static final int WIFI_PASSWORD_ERROR = 0x10;

	private WifiManager mWifiManager;
	private WifiInfo mWifiInfo;
	// 扫描出的网络连接列表
	private List<ScanResult> mWifiList = new ArrayList<ScanResult>();
	private List<WifiConfiguration> mWifiConfiguration;
	private WifiLock mWifiLock;
	private Context mContext = null;
	private boolean mIsWifiScanCompleted = false;
	private Handler handler;
	private boolean isConnectedActive = false;
	private Timer timer;
	
	public WifiConnectUtils(Context context) {
		this.mContext = context;
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
	}

	/** WIFI状态 */
	public static enum WIFIState {
		WIFI_CONNECTING, // 正在连接
		WIFI_CONNECT_SUCCESS, // 连接成功
		WIFI_CONNECT_FAILED, // 连接失败
		WIFI_DEVICE_RESTART, // 没有扫描到设备信息,正在重启
		WIFI_ERROR_NOTOPEN, // 没有开启WIFI功能
		WIFI_ERROR_PASSWORD, // 密码错误
		WIFI_ERROR_TIMEOUT, // 连接超时
		WIFI_ERROR_CANNOT_CONN// 无法连接
	}

	// 扫描结果通过该接口返回给Caller
	public interface IWifiStateChangedListener {
		public void onStateChanged(WIFIState errorType);
	}

	// public void setOnWifiStateChanged(IWifiStateChangedListener listener) {
	// this.mWifiListener = listener;
	// }

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public boolean isWifiEnabled() {
		return mWifiManager.isWifiEnabled();
	}

	// 打开WIFI
	public boolean openWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			return mWifiManager.setWifiEnabled(true);
		}
		return true;
	}

	// 关闭WIFI
	public void closeWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	/**
	 * 断开当前连接的网络
	 */
	public void disconnectCurrent() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		if (mWifiInfo != null) {
			mWifiManager.disableNetwork(mWifiInfo.getNetworkId());
			mWifiManager.disconnect();
//			mWifiManager.removeNetwork(mWifiInfo.getNetworkId());
		}
		ResourceConfig.getInstance(mContext).setDeviceMode(null);
		ResourceConfig.getInstance(mContext).setDeviceName(null);
		LeftMenuFragment.deviceMode = null;
		LeftMenuFragment.deviceName = null;
		LeftMenuFragment.isRefresh = true;
	}

	/**
	 * 设置wifi
	 * 
	 * @param ssid
	 *            名称
	 * @param passwd
	 *            密码
	 * @param type
	 *            加密类型
	 */
	public void addNetwork(final boolean isSleep, final String ssid, final String passwd, final String encryptionType) {
		new Thread(new Runnable() {
			public void run() {
				try {
					
					if (TextUtils.isEmpty(ssid)) {
						LogCat.e("addNetwork() ## nullpointer error!");
						handler.sendEmptyMessage(WifiConnectUtils.WIFI_ERROR);
						return;
					}

					if (handler == null) {
						LogCat.e("handler is null");
						return;
					}

					EncryptionType type = null;
					if (encryptionType.contains("WEP")) {
						type = EncryptionType.TYPE_WEP;
					} else if (encryptionType.contains("WPA") || encryptionType.contains("mixed-psk")) {
						type = EncryptionType.TYPE_WPA;
					} else if (encryptionType.contains("WPA2") || encryptionType.contains("psk2")) {
						type = EncryptionType.TYPE_WPA;
					} else {
						type = EncryptionType.TYPE_NO_PASSWD;
					}

					String LSSID = ssid;
					if (ssid.contains("\"")) {
						LSSID = LSSID.replace("\"", "");
					}

					LogCat.e(".....passwd="+passwd);
					String PASSWORD = passwd;
					if ( null != PASSWORD && PASSWORD.contains("\"")) {
						PASSWORD = PASSWORD.replace("\"", "");
					}

					final String SSID = LSSID;
//					LogCat.e(TAG, SSID + " : " + PASSWORD);

					Model model = ResourceConfig.getInstance(mContext).getDeviceMode();
					LogCat.e("****************model:"+model.toString());
					disconnectCurrent();
					// 如果WIFI没有打开，则打开WIFI
					if (!mWifiManager.isWifiEnabled()) {
						boolean result = mWifiManager.setWifiEnabled(true);
						if(!result) {
							LogCat.e("Enable Wifi flased!!!");
							handler.sendEmptyMessage(WifiConnectUtils.WIFI_OPEN_WIFI_FAILED);
							return;
						}
					}

					if (isSleep) {
						LogCat.e("*************设备正在重启*** 休息8秒...");
						handler.sendEmptyMessage(WifiConnectUtils.WIFI_DEVICE_RESTART);
						Thread.sleep(8000);
					}
					handler.sendEmptyMessage(WifiConnectUtils.WIFI_CONNCTING);

					int times = 10;
					boolean tag = false;
					while (!tag) {
						tag = checkNormal(SSID);
						if (!tag) {
							LogCat.e("**************** 扫描wifi %s",times);
							Thread.sleep(3000);
							times--;
							if (times < 0) {
								handler.sendEmptyMessage(WifiConnectUtils.WIFI_CONNECTED_TIMEOUT);
								return;
							}
						}
					}
					LogCat.e(".....PASSWORD = "+PASSWORD+",model= "+model);
					connect(SSID, PASSWORD, type,model);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	/**
	 * 连接wifi
	 * @param ssid
	 * @param passwd
	 * @param encryptionType
	 * @param model 
	 */
	private void connect(final String ssid, final String passwd, final EncryptionType encryptionType, Model model) {
		LogCat.e("开始连接...");
		isConnectedActive = false;
		mSSID = ssid;
		timeout = System.currentTimeMillis();
		
		LogCat.e("**************** WIFI名称：%s,长度:%s,WIFI密码：%s,长度:%s", ssid,ssid.length(),passwd,passwd == null ? 0 : passwd.length());
		registerConnReceiver(mContext);
		isConnectedActive = addNetwork(createWifiInfo(ssid, passwd, encryptionType));

		if (!isConnectedActive) {
			LogCat.e("**************** 连接失败");
			handler.sendEmptyMessage(WifiConnectUtils.WIFI_ERROR);
			unRegisterConnReceiver(mContext);
			return;
		} else {
			LogCat.e("**************** 连接success *****");
			if (Model.R300M.equals(model)) {
				if (timer == null) {
					timer = new Timer();
				}
				timer.schedule(new TimerTask() {
					@Override
					public void run() {
						LogCat.e("**************** 连接超时  connect");
						if (!isWifiConnected(mSSID)) {
							handler.sendEmptyMessage(WifiConnectUtils.WIFI_CONNECTED_TIMEOUT);
							unRegisterConnReceiver(mContext);
							disconnectCurrent();
							removeNetwork(mSSID);
						}
					}
				}, CONNECT_TIMEOUT + 5 * 1000);
			}
		}
	}

	/**
	 * 直接连接设备
	 * 
	 * @param ssid
	 * @param passwd
	 * @param encryptionType
	 */
	public void connectNetwork(final String ssid, final String passwd, final String encryptionType) {
		new Thread(new Runnable() {
			public void run() {
				try {
                    LogCat.e(".....connectNetwork........");
					if (TextUtils.isEmpty(ssid)) {
						Log.e(TAG, "addNetwork() ## nullpointer error!");
						handler.sendEmptyMessage(WifiConnectUtils.WIFI_ERROR);
						return;
					}

					if (handler == null) {
						Log.e(TAG, "handler is null");
						return;
					}

					EncryptionType type = null;
					if (encryptionType.contains("WEP")) {
						type = EncryptionType.TYPE_WEP;
					} else if (encryptionType.contains("WPA") || encryptionType.contains("mixed-psk")) {
						type = EncryptionType.TYPE_WPA;
					} else if (encryptionType.contains("WPA2") || encryptionType.contains("psk2")) {
						type = EncryptionType.TYPE_WPA;
					} else {
						type = EncryptionType.TYPE_NO_PASSWD;
					}

					disconnectCurrent();
					if (!mWifiManager.isWifiEnabled()) {
						boolean result = mWifiManager.setWifiEnabled(true);
						if(!result) {
							LogCat.e("Enable Wifi flased!!!");
							handler.sendEmptyMessage(WifiConnectUtils.WIFI_OPEN_WIFI_FAILED);
							return;
						}else{
							handler.sendEmptyMessage(WifiConnectUtils.WIFI_OPEN_WIFI);
							Thread.sleep(6000);
						}
					}
					
					handler.sendEmptyMessage(WifiConnectUtils.WIFI_CONNCTING);
					connect(ssid, passwd, type,null);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	private String mSSID;
	private long timeout;
	private BroadcastReceiver receiverWifi;
	private void registerConnReceiver(Context ctx) {
		if (receiverWifi == null) {
			receiverWifi = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					if (System.currentTimeMillis() - timeout > CONNECT_TIMEOUT || CONNECT_TIMEOUT >= 70*1000) {
						// 超时
						LogCat.e("**************** 连接超时 onReceive");
						if (!isWifiConnected(mSSID)) {
							handler.sendEmptyMessage(WifiConnectUtils.WIFI_CONNECTED_TIMEOUT);
							unRegisterConnReceiver(context);
							disconnectCurrent();
							removeNetwork(mSSID);
							return;
						}
					}

					if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction())) {
						if (!isConnectedActive) {
							return;
						}
						int errorCode = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
						if (errorCode == WifiManager.ERROR_AUTHENTICATING) {
							LogCat.e("**************** WIFI密码错误");
							handler.sendEmptyMessage(WifiConnectUtils.WIFI_PASSWORD_ERROR);
							unRegisterConnReceiver(context);
							removeNetwork(mSSID);
							return;
						}
						Bundle bundle = intent.getExtras();
						SupplicantState st = (SupplicantState) bundle.getSerializable(WifiManager.EXTRA_NEW_STATE);
						WifiInfo info = mWifiManager.getConnectionInfo();

						switch (st) {
						case ASSOCIATING:
							break;
						case ASSOCIATED:
							break;
						case COMPLETED:
							if (info != null && mSSID != null && info.getSSID() != null) {
								LogCat.e("complete info.getSSID() = "+info.getSSID()+"; mSSID = "+mSSID);
								if (info.getSSID().contains(mSSID)) {
//									time = time+10 * 1000;
									mWifiManager.saveConfiguration();
									LogCat.e("**************** 正在获取IP...");
									if (timer != null) {
										timer.cancel();
										timer = null;
									}
								}
							}
							break;
						case DISCONNECTED:
							break;
						case DORMANT:
							break;
						case INTERFACE_DISABLED:
						case UNINITIALIZED:
							LogCat.e("**************** 失败");
							handler.sendEmptyMessage(WifiConnectUtils.WIFI_CONNECT_FAILED);
							unRegisterConnReceiver(context);
							removeNetwork(mSSID);
							break;
						case FOUR_WAY_HANDSHAKE:
						case GROUP_HANDSHAKE:
							break;
						case INACTIVE:
						case INVALID:
						case SCANNING:
						default:
						}
					} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {// 网络状态变化
						ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
						NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
						if (netInfo != null && netInfo.isConnected()) {
							int netType = netInfo.getType();
							if (netType == ConnectivityManager.TYPE_WIFI) {
								String bssid = getBSSID();
								String ssid = getSSID();
								LogCat.e("**************** 连接成功 ssid:" + ssid+"; mSSID = "+mSSID);
								if (formatSSID(ssid).equals(formatSSID(mSSID))) {
									unRegisterConnReceiver(context);
									if (bssid != null && bssid.contains(WifiMonitor.PISEN_BSSID_PREFIX)) {
										handler.sendEmptyMessage(WifiConnectUtils.WIFI_CONNECTED);
									} else {
										handler.sendEmptyMessage(WifiConnectUtils.WIFI_CONNECT_FAILED);
									}
								}
							}
						}
					}
				}

			};
		}
		
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		ctx.registerReceiver(receiverWifi, intentFilter);
	}

	private void unRegisterConnReceiver(Context ctx) {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (receiverWifi != null) {
			ctx.unregisterReceiver(receiverWifi);
			receiverWifi = null;
		}
	}

	private static final long CONNECT_TIMEOUT = 15 * 1000;
	

	private boolean addNetwork(WifiConfiguration wcg) {
		int wcgID = mWifiManager.addNetwork(wcg);
		return mWifiManager.enableNetwork(wcgID, true);
	}

	private WifiConfiguration createWifiInfo(String SSID, String password, EncryptionType type) {

		Log.v(TAG, "SSID = " + SSID + "## Password = " + password + "## Type = " + type);

		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";

		WifiConfiguration tempConfig = this.IsExsits(SSID);
		if (tempConfig != null) {
			mWifiManager.removeNetwork(tempConfig.networkId);
		}

		// 分为三种情况：1没有密码2用wep加密3用wpa加密
		if (type == EncryptionType.TYPE_NO_PASSWD) {// WIFICIPHER_NOPASS
			// config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			// config.wepTxKeyIndex = 0;
		} else if (type == EncryptionType.TYPE_WEP) { // WIFICIPHER_WEP
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + password + "\"";
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		} else if (type == EncryptionType.TYPE_WPA) { // WIFICIPHER_WPA
			config.preSharedKey = "\"" + password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.status = WifiConfiguration.Status.ENABLED;
		}

		return config;
	}

	/**
	 * 判断wifi是否连接成功,不是network
	 * 
	 * @param context
	 * @return
	 */
	public boolean isWifiContected(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		Log.v(TAG, "isConnectedOrConnecting = " + wifiNetworkInfo.isConnectedOrConnecting());
		Log.d(TAG, "wifiNetworkInfo.getDetailedState() = " + wifiNetworkInfo.getDetailedState());
		if (wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {
			mWifiManager.getConnectionInfo();
			return true;
		}
		return false;
	}
	
	public boolean isWifiConnected(String SSID) {
		ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetworkInfo.getDetailedState() == DetailedState.CONNECTED) {
			if (mWifiManager.getConnectionInfo().getSSID().equals(SSID)) {
				return true;
			}
		}
		return false;
	}

	private WifiConfiguration IsExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
		for (WifiConfiguration existingConfig : existingConfigs) {
			if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
				return existingConfig;
			}
		}
		return null;
	}

	// 断开指定ID的网络
	public void disconnectWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}

	// 检查当前WIFI状态
	public int checkState() {
		return mWifiManager.getWifiState();
	}

	// 锁定WifiLock
	public void acquireWifiLock() {
		mWifiLock.acquire();
	}

	// 解锁WifiLock
	public void releaseWifiLock() {
		// 判断时候锁定
		if (mWifiLock.isHeld()) {
			mWifiLock.acquire();
		}
	}

	// 创建一个WifiLock
	public void creatWifiLock() {
		mWifiLock = mWifiManager.createWifiLock("Test");
	}

	// 得到配置好的网络
	public List<WifiConfiguration> getConfiguration() {
		return mWifiConfiguration;
	}

	// 指定配置好的网络进行连接
	public boolean connectConfiguration(int index) {
		// 索引大于配置好的网络索引返回
		boolean ret = true;
		if (index < 0 && index > mWifiManager.getConfiguredNetworks().size()) {
			ret = false;
		}
		if (ret) {
			ret = mWifiManager.enableNetwork(mWifiManager.getConfiguredNetworks().get(index).networkId, true);
		}
		if (ret) {
			timeout = System.currentTimeMillis();
			mSSID = mWifiManager.getConfiguredNetworks().get(index).SSID;
			registerConnReceiver(mContext);
			handler.sendEmptyMessage(WifiConnectUtils.WIFI_CONNCTING);
		}
		// 连接配置好的指定ID的网络
		return ret;
	}
	
	public boolean startScan() {
		mWifiManager.startScan();
		mWifiConfiguration = mWifiManager.getConfiguredNetworks();
		mWifiList = mWifiManager.getScanResults();
		if (mWifiList != null) {
			if (!mWifiList.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 扫描手机WIFI列表
	 * 
	 * @param ssid
	 * @return
	 */
	private boolean checkNormal(String ssid) {
		startScan();
		LogCat.i("List.size:" + mWifiList.size());
		for (ScanResult wifi : mWifiList) {
			if (wifi.SSID.contains(ssid)) {
				return true;
			}
		}
		return false;
	}

	// 得到网络列表
	public List<ScanResult> getWifiList() {
		return mWifiList;
	}

	// 得到MAC地址
	public String getMacAddress() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? "" : mWifiInfo.getMacAddress();
	}

	// 得到接入点的BSSID
	public String getSSID() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? "" : mWifiInfo.getSSID();
	}

	// 得到接入点的BSSID
	public String getBSSID() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? "" : mWifiInfo.getBSSID();
	}

	// 得到IP地址
	public int getIPAddress() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	// 得到连接的ID
	public int getNetworkId() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	// 得到WifiInfo的所有信息包
	public String getWifiInfo() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}
	
	/**
	 * 移除网络
	 * @param mSSID
	 */
	public void removeNetwork(String mSSID) {
		int p = findConfiguredNetworks(mSSID);
		if (p > -1) {
			mWifiManager.removeNetwork(mWifiManager.getConfiguredNetworks().get(p).networkId);
			mWifiManager.saveConfiguration();
		}
	}
	
	public int findConfiguredNetworks(String ssid) {
		int position = -1;
		WifiManager manager = (WifiManager) mContext.getSystemService(Service.WIFI_SERVICE);
		List<WifiConfiguration> data = manager.getConfiguredNetworks();
		if (!TextUtils.isEmpty(ssid) && data != null) {
			ssid = WifiConnectUtils.formatSSID(ssid);
			int size = data.size();
			WifiConfiguration w = null;
			for (int i = 0; i < size; i++) {
				w = data.get(i);
				if (ssid.equals(WifiConnectUtils.formatSSID(w.SSID))) {
					position = i;
					break;
				}
			}
		}
		return position;
	}
	
	/**
	 * 如果SSID为"SSID"去掉引号
	 */
	public static String formatSSID(String ssid) {
		if (ssid != null && ssid.startsWith("\"")) {
			return ssid.substring(1, ssid.length() - 1);
		}
		return ssid;
	}
	
}
