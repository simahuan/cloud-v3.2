/**
 * 
 */
package com.pisen.router.ui.phone.device.bean;

import java.io.Serializable;

/**
 * 
 * @author Liuhc
 * @version 1.0 2015年5月25日 下午2:26:31
 * @des 非标准javaBean
 */
public class RelayConfBean implements Serializable{

	private static final long serialVersionUID = 1L;
	/** 有线连接 */
	public WanBean wan;  
	/** 无线连接 */
	public StaBean sta; 
	
	public WanBean getWan() {
		return wan;
	}
	public void setWan(WanBean wan) {
		this.wan = wan;
	}
	public StaBean getSta() {
		return sta;
	}
	public void setSta(StaBean sta) {
		this.sta = sta;
	}
	
}
