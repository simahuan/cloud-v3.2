package com.pisen.router.ui.phone.device;

/**
 * Router配置请求接口
 * @author Liuhc
 * @version 1.0 2015年5月12日 下午2:53:35
 */
public interface IRouterResponse {
	
	/**
	 * 成功返回
	 * @param result
	 */
	public void onSuccess(String result);
	
	/**
	 * 请求失败返回
	 * @param errorInfo
	 */
	public void onError(String errorInfo);
}
