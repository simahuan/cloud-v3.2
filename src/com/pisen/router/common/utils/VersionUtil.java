package com.pisen.router.common.utils;

import android.text.TextUtils;

/**
 * @author ldj 创建时间：2015年9月15日 下午4:47:28
 *
 */
public class VersionUtil {

	/**
	 * 是否为智享强制更新版本
	 * 
	 * @param curVersion
	 * @param serverVersion
	 * @return
	 */
	public static boolean isNewZXVersion(String curVersion, String serverVersion) {
		if (!TextUtils.isEmpty(curVersion) && !TextUtils.isEmpty(serverVersion)) {
			try {
				String[] curInfos = curVersion.split("\\.");
				String[] serverInfos = serverVersion.split("\\.");
				if (curInfos.length >= 3 && serverInfos.length >= 3) {
					if (Integer.parseInt(serverInfos[0]) >= Integer.parseInt(curInfos[0])) {
						if (Integer.parseInt(serverInfos[0]) > Integer.parseInt(curInfos[0])) {
							return true;
						} else {
							if (Integer.parseInt(serverInfos[1]) >= Integer.parseInt(curInfos[1])) {
								if (Integer.parseInt(serverInfos[1]) > Integer.parseInt(curInfos[1])) {
									return true;
								} else {
									if (Integer.parseInt(serverInfos[2]) > Integer.parseInt(curInfos[2])) {
										return true;
									}
								}
							}
						}

					}
				}
			} catch (Exception e) {
			}
		}
		return false;
	}

	/**
	 * 是否为智享强制更新版本
	 * 
	 * @param curVersion
	 * @param serverVersion
	 * @return
	 */
	public static boolean isForceZXVersion(String curVersion, String serverVersion) {
		if (!TextUtils.isEmpty(curVersion) && !TextUtils.isEmpty(serverVersion)) {
			try {
				String[] curInfos = curVersion.split("\\.");
				String[] serverInfos = serverVersion.split("\\.");
				if (curInfos.length >= 3 && serverInfos.length >= 3) {
					if (Integer.parseInt(serverInfos[0]) >= Integer.parseInt(curInfos[0])) {
						if (Integer.parseInt(serverInfos[0]) > Integer.parseInt(curInfos[0])) {
							return true;
						} else {
							if (Integer.parseInt(serverInfos[1]) > Integer.parseInt(curInfos[1])) {
										return true;
							}
						}
					}
				}
			} catch (Exception e) {
			}
		}
		return false;
	}

}
