package com.pisen.router.config;

import android.studio.os.PreferencesUtils;

public class AppConfig {

	/** 当前用户所在磁盘位置 */
	public static final String pref_current_disk_path = "pref_current_disk_path";

	public static String getCurrentDiskPath() {
		return PreferencesUtils.getString(pref_current_disk_path, null);
	}

	public static void setCurrentDiskPath(String current_disk_path) {
		PreferencesUtils.setString(pref_current_disk_path, current_disk_path);
	}

}
