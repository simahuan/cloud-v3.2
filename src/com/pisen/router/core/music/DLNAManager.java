package com.pisen.router.core.music;


import com.charon.dmc.engine.DLNAContainer;
import com.charon.dmc.engine.DLNAContainer.DeviceChangeListener;
import com.charon.dmc.service.DLNAService;

import android.content.Context;
import android.content.Intent;

/**
 * DLNA设备管理类
 * @author ldj
 * @version 1.0 2015年5月13日 下午4:19:24
 */
public class DLNAManager {
	
	/**
	 * 开始设备扫描
	 * @param ctx
	 */
	public static void startSearch(Context ctx) {
		//清除历史数据
//		DLNAContainer.getInstance().clear();
		Intent intent = new Intent(ctx.getApplicationContext(), DLNAService.class);
		ctx.getApplicationContext().startService(intent);
	}
	
	/**
	 * 停止设备扫描
	 * @param ctx
	 */
	public static void stopSearch(Context ctx) {
		Intent intent = new Intent(ctx.getApplicationContext(), DLNAService.class);
		ctx.getApplicationContext().stopService(intent);
	}
	
	/**
	 * 设置设备数据变更监听
	 * @param listener
	 */
	public static void setOnDeviceChangListener(DeviceChangeListener listener) {
		DLNAContainer.getInstance().setDeviceChangeListener(listener);
	}
}
