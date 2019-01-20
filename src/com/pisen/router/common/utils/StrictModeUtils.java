package com.pisen.router.common.utils;

import android.os.StrictMode;


/**
 * @author  mahuan
 * @version 1.0 2015年9月17日 上午11:36:23
 * @desc{策略模式}
 */
public class StrictModeUtils {

	/**
	 * @describtion  设置严格策模式
	 * @param DEVELOPER_MODE
	 */
	public static void setStrictMode(Boolean DEVELOPER_MODE) {
		if (DEVELOPER_MODE) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads().detectDiskWrites()
					.detectNetwork()    // or .detectAll() for all detectable
										// problems
					.penaltyLog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects()
					.detectLeakedClosableObjects().penaltyLog().penaltyDeath().build());
		}
	}
}
