package com.pisen.router.ui.phone.flashtransfer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.studio.os.PreferencesUtils;

import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.common.utils.WifiSearcher;
import com.pisen.router.common.utils.WifiSearcher.ErrorType;
import com.pisen.router.common.utils.WifiSearcher.SearchWifiListener;
import com.pisen.router.core.flashtransfer.FinishScanListener;
import com.pisen.router.core.flashtransfer.WifiApManager;

/**
 * 闪电互传网络操作辅助类，包括hotspot及wifi
 * @author ldj
 * @version 1.0 2015年5月20日 下午4:04:47
 */
public class FlashTransferNetUtil implements SearchWifiListener {
	private WifiApManager apManager;
	private WifiConfiguration config;
	private WifiSearcher wifiSearcher;
	private ScanFlashApListener listener;

	public static final String PREFIX_AP = "PisenAp";
	private static FlashTransferNetUtil mInstance;

	public static FlashTransferNetUtil getInstance(Context ctx){
		if (null == mInstance){
			mInstance = new FlashTransferNetUtil(ctx);
		}
		return mInstance;
	}
	
	public void refreshApConfigure() {
		config = generateWifiConfiguration();
	}
	
	private FlashTransferNetUtil(Context ctx) {
		apManager = new WifiApManager(ctx);
		config = generateWifiConfiguration();
		wifiSearcher = new WifiSearcher(ctx, this);
	}
	
	/**
	 * 扫码指定类型wifi
	 */
	public void scanFlashTransferAp() {
		wifiSearcher.search();
	}
	
	/**
	 * 连接指定ap
	 * @param ssid
	 * @param psw
	 * @return
	 */
	public boolean connectAp(String ssid,String psw) {
		return wifiSearcher.Connect(ssid, psw);
	}

	/**
	 * 开启ap
	 * 
	 * @param ctx
	 * @return
	 */
	public boolean openAp() {
		boolean result = false;
		if(!apManager.isWifiApEnabled()) {//ap不可用，开启
			result = apManager.setWifiApEnabled(config, true);
		}else {
			result = true;
		}
		
		return result;
		
	}
	
	/**
	 * @describtion
	 * @return wifiAP是否开启
	 */
	public boolean isWifiApEnabled() {
		return apManager.isWifiApEnabled();
	}
	
	public String getApSsid() {
		String ssid = null;
		if(apManager.isWifiApEnabled()) {
			ssid = config.SSID;
		}
		return ssid;
	}
	
	/**
	 * 关闭ap
	 * @return
	 */
	public boolean closeAp() {
		boolean result = false;
		if(apManager.isWifiApEnabled()) {
			result = apManager.setWifiApEnabled(config, false);
		}else {
			result = true;
		}
		
		return result;
	}
	
	/**
	 * 扫描搜索失败
	 */
	@Override
	public void onSearchWifiFailed(ErrorType errorType) {
		if(listener != null) {
			listener.scanFailed("网络扫描失败");
		}
	}

	/**
	 * 扫描搜索成功
	 */
	@Override
	public void onSearchWifiSuccess(List<ScanResult> results) {
		List<ScanResult> aps = null;
		if(results != null && results.size() >0) {
			aps = getFlashTransferAp(results);
		}
		if(listener != null) {
			listener.scanSuccess(aps);
		}
	}
	
	/**
	 * 设置扫描闪传可用ap回调
	 * @param listener
	 */
	public void setScanFlashApListener(ScanFlashApListener listener) {
		this.listener = listener;
	}
	
	/**
	 * 获取连接AP的设备列表
	 * @param onlyReachables
	 * @param finishListner
	 */
	public void getApClientList(boolean onlyReachables, FinishScanListener finishListner) {
		apManager.getClientList(onlyReachables, finishListner);
	}
	
	/**
	 * 从扫码结果中获取闪电互传ap
	 * @param data
	 * @return
	 */
	private List<ScanResult> getFlashTransferAp(List<ScanResult> data) {
		List<ScanResult> result = new ArrayList<ScanResult>();
		
		ScanResult tmp = null;
		int size = data.size();
		for(int i=0; i<size; i++) {
			tmp = data.get(i);
			if(tmp.SSID.contains(PREFIX_AP)) {
				result.add(tmp);
			}
		}
		
		return result;
	}
	
	/**
	 * 生产默认网络配置
	 * @return
	 */
	private WifiConfiguration generateWifiConfiguration() {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		int headId = -1;
		try {
			headId = PreferencesUtils.getInt(KeyUtils.NICK_HEAD, -1);
		}catch(Exception e) {
		}
		config.SSID =  String.format("%s_%s_%s", PREFIX_AP ,
				PreferencesUtils.getString(KeyUtils.NICK_NAME, android.os.Build.MODEL), 
				headId);//+ Build.MODEL;
		config.wepKeys[0] = "";
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		config.wepTxKeyIndex = 0;
		
		return config;
	}

	/**
	 * 扫描合法闪电互传ap结果
	 * @author ldj
	 * @version 1.0 2015年5月21日 上午9:23:34
	 */
	public interface ScanFlashApListener {
		void scanSuccess(List<ScanResult> result);
		void scanFailed(String msg);
	}
}
