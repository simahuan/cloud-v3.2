package com.pisen.router.core.monitor;

import android.database.Observable;

import com.pisen.router.core.monitor.RedHotMonitor.RedHotCallBack;
import com.pisen.router.ui.phone.settings.upgrade.AppVersion;
import com.pisen.router.ui.phone.settings.upgrade.DownLoadApp;

/**
 * @author  mahuan
 * @version 1.0 2015年6月16日 下午3:21:47
 */
public class RedHotMonitor extends Observable<RedHotCallBack> {

	public interface RedHotCallBack {
		void update(AppVersion ver, DownLoadApp app);
	}
	
	static RedHotMonitor instance = null;

	private RedHotMonitor() { }

	public static RedHotMonitor getInstance() {
		if (instance == null) {
			instance = new RedHotMonitor();
		}
		return instance;
	}
	
	/**
	 * 通知更新
	 */
	public void notifyUpdate(AppVersion ver, DownLoadApp app) {
		synchronized (mObservers) {
			for (RedHotCallBack observer : mObservers) {
				 observer.update(ver,app);
			}
		}
	}
}
