package com.pisen.router.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.studio.os.LogCat;
import android.text.TextUtils;
import android.util.Log;

/**
 * WIFI操作工具类
 * @author Liuhc
 * @version 1.0 2015年5月20日 下午12:00:21
 */
public class WifiSearcher {

	private static final int WIFI_SEARCH_TIMEOUT = 20; // 扫描WIFI的超时时间
	private Context mContext;
	private WifiManager mWifiManager;
	private WiFiScanReceiver mWifiReceiver;
	private Lock mLock;
	private Condition mCondition;
	private SearchWifiListener mSearchWifiListener;
	private boolean mIsWifiScanCompleted = false;

	public static enum ErrorType {
		SEARCH_WIFI_TIMEOUT, // 扫描WIFI超时（一直搜不到结果）
		NO_WIFI_FOUND, // 扫描WIFI结束，没有找到任何WIFI信号
		AP_ENABLED,//热点开启或开启中
	}

	// 扫描结果通过该接口返回给Caller
	public interface SearchWifiListener {
		public void onSearchWifiFailed(ErrorType errorType);
		public void onSearchWifiSuccess(List<ScanResult> results);
	}

	public WifiSearcher(Context context, SearchWifiListener listener) {
		mContext = context;
		mSearchWifiListener = listener;

		mLock = new ReentrantLock(); //重入锁机制
		mCondition = mLock.newCondition();
		mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
		mWifiReceiver = new WiFiScanReceiver();
	}

	public void search() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				// 如果WIFI没有打开，则打开WIFI
				if (!mWifiManager.isWifiEnabled()) {
					boolean result = mWifiManager.setWifiEnabled(true);
					if(!result) {
						Log.e("WifiSearcher", "Enable Wifi flased!!!");
						if(mSearchWifiListener != null) {
							mSearchWifiListener.onSearchWifiFailed(ErrorType.NO_WIFI_FOUND);
						}
						return;
					}
				}

				// 注册接收WIFI扫描结果的监听类对象
				mContext.registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
				// 开始扫描
				mWifiManager.startScan();
				mLock.lock();

				// 阻塞等待扫描结果
				try {
					mIsWifiScanCompleted = false;
					mCondition.await(WIFI_SEARCH_TIMEOUT, TimeUnit.SECONDS);
					if (!mIsWifiScanCompleted) {
						Method method = mWifiManager.getClass().getMethod("getWifiApState");
						int i = (Integer) method.invoke(mWifiManager);
						if (i == 2 || i == 3 || i == 11 || i == 13) {
							mSearchWifiListener.onSearchWifiFailed(ErrorType.AP_ENABLED);
						}else{
							mSearchWifiListener.onSearchWifiFailed(ErrorType.SEARCH_WIFI_TIMEOUT);
						}
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
				mLock.unlock();

				try {
					// 删除注册的监听类对象
					mContext.unregisterReceiver(mWifiReceiver);
				} catch (Exception e) {
				}
			}
		}).start();
	}

	/**
	 * 断开当前连接的网络
	 */
	public void disconnectWifi() {
		int netId = mWifiManager.getConnectionInfo().getNetworkId();
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}
	
	/**
	 *  提供一个外部接口，传入要连接的无线网
	 * @param SSID
	 * @param Password
	 * @return
	 */
	public boolean Connect(String SSID, String Password) {
		if (!this.OpenWifi()) {
			return false;
		}
		// 开启wifi功能需要一段时间(我在手机上测试一般需要1-3秒左右)，所以要等到wifi
		// 状态变成WIFI_STATE_ENABLED的时候才能执行下面的语句
		while (mWifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
			try {
				// 为了避免程序一直while循环，让它睡个100毫秒在检测……
				Thread.currentThread();
				Thread.sleep(100);
			} catch (InterruptedException ie) {
			}
		}

		WifiConfiguration wifiConfig = this.CreateWifiInfo(SSID, Password);
		if (wifiConfig == null) {
			return false;
		}

		WifiConfiguration tempConfig = this.IsExsits(SSID);

		if (tempConfig != null) {
			mWifiManager.removeNetwork(tempConfig.networkId);
		}

		int netID = mWifiManager.addNetwork(wifiConfig);
		return mWifiManager.enableNetwork(netID, true);
	}

	// 打开wifi功能
	private boolean OpenWifi() {
		boolean bRet = true;
		if (!mWifiManager.isWifiEnabled()) {
			bRet = mWifiManager.setWifiEnabled(true);
		}
		return bRet;
	}
		
	// 查看以前是否也配置过这个网络
	private WifiConfiguration IsExsits(String SSID) {
		List<WifiConfiguration> existingConfigs = mWifiManager.getConfiguredNetworks();
		if(existingConfigs != null) {
			for (WifiConfiguration existingConfig : existingConfigs) {
				if (existingConfig.SSID.equals("\"" + SSID + "\"")) {
					return existingConfig;
				}
			}
		}
		return null;
	}

	private WifiConfiguration CreateWifiInfo(String SSID, String Password) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + SSID + "\"";
		
		/*if (Type == WifiCipherType.WIFICIPHER_WEP) {
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
			config.wepKeys[0] = "";
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (Type == WifiCipherType.WIFICIPHER_WPA) {
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.status = WifiConfiguration.Status.ENABLED;
		} else {
			return null;
		}
		*/
		
		if (TextUtils.isEmpty(Password)) {
			config.wepKeys[0] = "\"" + "\"";  
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}else{
			config.preSharedKey = "\"" + Password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		return config;
	}
	
	// 系统WIFI扫描结果消息的接收者
	protected class WiFiScanReceiver extends BroadcastReceiver {
		public void onReceive(Context c, Intent intent) {
			// 提取扫描结果
			List<ScanResult> scanResults = mWifiManager.getScanResults();
			// 检测扫描结果
			if (scanResults.isEmpty()) {
				mSearchWifiListener.onSearchWifiFailed(ErrorType.NO_WIFI_FOUND);
			} else {
				mSearchWifiListener.onSearchWifiSuccess(scanResults);
			}

			mLock.lock();
			mIsWifiScanCompleted = true;
			mCondition.signalAll();
			mLock.unlock();
		}
	}
}
