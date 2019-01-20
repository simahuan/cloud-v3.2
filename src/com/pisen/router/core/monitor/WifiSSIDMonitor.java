package com.pisen.router.core.monitor;

import android.database.Observable;

import com.pisen.router.config.WifiConfig;
import com.pisen.router.core.monitor.WifiSSIDMonitor.WifiSSIDCallback;

/**
 * @author  mahuan
 * @version 1.0 2015年6月16日 下午3:21:47
 */
public class WifiSSIDMonitor extends Observable<WifiSSIDCallback> {

	public interface WifiSSIDCallback {
		void networkChange(WifiConfig config,boolean connected);
	}
	
	static WifiSSIDMonitor instance = null;

	private WifiSSIDMonitor() { }

	public static WifiSSIDMonitor getInstance() {
		if (instance == null) {
			instance = new WifiSSIDMonitor();
		}
		return instance;
	}
	
	/**
	 * 通知更新
	 */
	public void notifyChange(WifiConfig config,boolean connected) {
		synchronized (mObservers) {
			for (WifiSSIDCallback observer : mObservers) {
				 observer.networkChange(config,connected);
			}
		}
	}
}
