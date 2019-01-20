package com.pisen.router.ui.phone.settings.upgrade;

import com.google.gson.GsonUtils;

/**
 * app版本信息
 * @author yangyp
 * @version 1.0, 2014年8月1日 下午7:21:12
 */
public class AppVersionResult extends JsonResult {
	/** TODO */
	private static final long serialVersionUID = 1L;
	public AppVersion AppVersion;

	/**
	 * 判断内容是否为空
	 * @return true为空，false不为空
	 */
	public boolean isDataNull() {
		return AppVersion == null;
	}
	
	public static AppVersionResult json2bean(String json){
		return GsonUtils.jsonDeserializer(json, AppVersionResult.class);
	}
}
