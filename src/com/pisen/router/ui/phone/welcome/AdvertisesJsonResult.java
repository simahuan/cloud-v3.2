package com.pisen.router.ui.phone.welcome;

import java.io.Serializable;
import java.util.List;

import com.pisen.router.ui.phone.settings.upgrade.JsonResult;

/**
 * JSON返回消息
 * 
 * @author yangyp
 * @version 1.0, 2014-7-3 下午5:21:10
 */
public class AdvertisesJsonResult extends JsonResult implements Serializable {

	private static final long serialVersionUID = 1L;
	public List<Advertise> Advertises;

	/**
	 * 判断内容是否为空
	 * 
	 * @return true为空，false不为空
	 */
	public boolean isDataNull() {
		return Advertises == null || Advertises.isEmpty();
	}

}
