package com.pisen.router.ui.phone.device.bean;

import java.io.Serializable;

/**
 * 
 * @author Liuhc
 * @version 1.0 2015年5月25日 下午2:26:31
 */
public class RouterBean implements Serializable{
	/** TODO */
	private static final long serialVersionUID = 1L;
	/**
	 * up 
	 * down
	 */
	public static enum InternetState{
		up,//可以访问
		down,//无法访问
		unknown//未知
	}
	
	/**
	 * sta 无线
	 * wan　有线
	 * unknown　未知
	 */
	public static enum InternetType{
		sta,//无线
		wan,//有线
		unknown//未知
	}
	
	//网络状态
	public InternetState state;
	//上网方式
	public InternetType type;
}
