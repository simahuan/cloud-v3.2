package com.pisen.router.ui.phone.flashtransfer;

import java.util.List;

import android.content.Context;

/**
 * 资源扫描器
 * @author ldj
 * @version 1.0 2015年5月18日 下午2:23:51
 */
public abstract class LocalResourceScanner<T> {

	protected OnScanCompleteListener<T> listener;
	
	/**
	 * 开始扫描
	 */
	public abstract void startScan(Context ctx);
	
	/**
	 * 扫描完成通知回调
	 */
	public void notifyComplete(List<T> data) {
		if(listener != null) {
			listener.complete(data);
		}
	}

	/**
	 * 设置扫描完成回调
	 * @param listener
	 */
	public void setOnScanCompleteListener(OnScanCompleteListener<T> listener) {
		this.listener = listener;
	}

	/**
	 * 扫描结果回调
	 * @author ldj
	 * @version 1.0 2015年5月18日 下午2:17:50
	 */
	public interface OnScanCompleteListener<T> {
		void complete(List<T> data);
	}

}