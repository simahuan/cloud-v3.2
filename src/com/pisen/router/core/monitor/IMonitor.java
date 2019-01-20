package com.pisen.router.core.monitor;

import android.content.Context;

public interface IMonitor {

	/**
	 * 启动监听
	 */
	void startMonitor(Context context);

	/**
	 * 停止监听
	 */
	void stopMonitor(Context context);

}
