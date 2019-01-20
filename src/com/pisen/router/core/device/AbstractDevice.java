package com.pisen.router.core.device;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.AvoidXfermode.Mode;
import android.os.Handler;
import android.studio.os.LogCat;
import android.studio.os.NetUtils;
import android.text.TextUtils;

import com.pisen.router.CloudApplication;
import com.pisen.router.common.utils.HttpRequest;
import com.pisen.router.common.utils.HttpRequest.HttpRequestException;
import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.config.HttpKeys;
import com.pisen.router.config.ResourceConfig;
import com.pisen.router.core.monitor.entity.RouterConfig.Model;
import com.pisen.router.ui.phone.device.bean.CQRouterImageFile;
import com.pisen.router.ui.phone.device.bean.CQUpdateProgress;
import com.pisen.router.ui.phone.device.bean.FirmwareData;
import com.pisen.router.ui.phone.device.bean.RelayConfBean;
import com.pisen.router.ui.phone.device.bean.RouterBean;
import com.pisen.router.ui.phone.device.bean.WanBean;
import com.pisen.router.ui.phone.device.bean.WifiBean;
import com.pisen.router.ui.phone.device.bean.ZFirmwareInfo;

/**
 * 设备管理接口
 * 
 * @author Liuhc
 * @version 1.0 2015年5月25日 下午1:59:10
 */
public abstract class AbstractDevice {
	public static  final String CHUANQIANGWANG = "穿墙王·云路由";
	/** 穿墙王.路由型号 */
	public static  final  String  CQ_MODEL = "WHR001N";
	/** 追剧.路由型号 */
	public static  final  String  ZJ_MODEL = "WMP002N";

	
	/**
	 * @desc{超时接口}
	 */
	public static interface OnLoginTimeoutCallback {
		void onLoginTimeout();
	}

	public static AbstractDevice getInstance() {
		ResourceConfig config = ResourceConfig.getInstance(CloudApplication.getInstance());
		if (Model.RZHIXIANG.equals(config.getDeviceMode())) {
			return new ZhiXiangDevice();
		} else if (config.getRouterInfo() != null && CQ_MODEL.equalsIgnoreCase(config.getRouterInfo().model)) {
//			LogCat.e("穿墙王　object create...");
			return new ChuanQiangDevice(CloudApplication.getInstance());
		} else if ( Model.R150M.equals(config.getDeviceMode())) {
			return new PisenOldDevice();
		} else {
//			LogCat.e("OldPisenDevice　object create...");
//			LogCat.e("Device_Model = " + config.getRouterInfo().model);
//			LogCat.e("OldPisenDevice deviceName...=" + config.getDeviceName());
			return new PisenOldDevice();
		}
	}

	/**
	 * @return
	 */
	public static String getRequestURL(String baseUrl) {
		String ipAddress = NetUtil.getGetwayIPAddress(CloudApplication.getInstance());
		if (TextUtils.isEmpty(ipAddress)) {
			LogCat.e("error:ipAddress null");
		}
		return String.format(baseUrl, ipAddress);
	}

	/**
	 * @param urlAddress
	 * @return
	 */
	public static String requestGet(String url) {
		LogCat.i("请求：" + url);
		String result = HttpRequest.get(url).body();
		LogCat.i("返回：" + result);
		return filterObj(result);
	}
	
	/**
	 * @param urlAddress
	 * @return
	 */
	public static String requestGet(String url, Map<?, ?> params) {
		String result = "";
		if (params != null) {
			LogCat.i("请求：" + url + " 参数：" + params.toString());
			HttpRequest httpRequest = HttpRequest.get(url, params, true);
			try {
				result = httpRequest.body();
			} catch (HttpRequestException e) {
				LogCat.e("httpRequest异常");
				e.printStackTrace();
			}
		} else {
			LogCat.i("请求：" + url);
			result = HttpRequest.get(url).body();
		}
		LogCat.i("返回：%s" , result);
		return filterObj(result);
	}

	
	/**
	 * @param url
	 * @param params
	 * @return
	 */
	public static String requestPost(String url, String params) {
		LogCat.i("请求：" + url + " 参数：" + params);
		String result = "";
		HttpRequest req = HttpRequest.post(url);
		if (!TextUtils.isEmpty(params)) {
			req.send(params.getBytes());
		}
		if (req.ok()) {
			result = req.body();
		}
		LogCat.i("返回：" + result);
		return filterObj(result);
	}

	public static String requestPost(String url, Map<String, String> params) {
		LogCat.i("请求：" + url + " 参数：" + params);
		String result = HttpRequest.post(url, encodeParameters(params, "utf-8"));
		LogCat.i("返回：" + result);
		return filterObj(result);
	}

	protected OnLoginTimeoutCallback callback;
	
//	public static String requestGet(String url, Map<String, String> params) {
//		LogCat.i("请求：" + url + " 参数：" + params);
//		String result = HttpRequest.post(HttpKeys.ROUTER_URL, encodeParameters(params, "utf-8"));
//		LogCat.i("返回：" + result);
//		return filterObj(result);
//	}

	public static byte[] encodeParameters(Map<String, String> params, String paramsEncoding) {
		StringBuilder encodedParams = new StringBuilder();
		try {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				encodedParams.append(URLEncoder.encode(entry.getKey(), paramsEncoding));
				encodedParams.append('=');
				encodedParams.append(URLEncoder.encode(entry.getValue(), paramsEncoding));
				encodedParams.append('&');
			}
			return encodedParams.toString().getBytes(paramsEncoding);
		} catch (UnsupportedEncodingException uee) {
			throw new RuntimeException("Encoding not supported: " + paramsEncoding, uee);
		}
	}

	/**
	 * 登陆（针对品胜产品）
	 * 
	 * @return
	 */
	public abstract boolean login(String password);
	
	/**
	 * 是否登陆
	 * @param ctx
	 * @return
	 */
	public abstract boolean isLogin(Context ctx);

	/**
	 * 查找app可连接设备
	 * 
	 * @return
	 */
	public abstract ArrayList<WifiBean> getDeviceList(Context ctx);

	/**
	 * 获取WIFI配置信息
	 * 
	 * @return
	 */
	public abstract WifiBean getWifiConfig();

	/**
	 * 设置WIFI配置信息
	 * 
	 * @param ssid
	 * @param mode
	 * @param encryption
	 * @param key
	 */
	public abstract boolean setWifiConfig(WifiBean wifi);

	/**
	 * 获取中继联网方式
	 * 
	 * @return
	 */
	public abstract RouterBean getRelayType();
	
	/**
	 * 获取中继联网设置信息
	 * 
	 * @return
	 */
	public abstract RelayConfBean getRelayConfig();
	
	/**
	 * 读取当前设备（router）的外网配置信息
	 */
	public abstract WanBean getWanConfig();
	
	
	/**
	 * @describtion 获取有线配置信息
	 * @return
	 */
	public  WanBean getWiredConfig(){return null;};

	/**
	 * 查询当前无线中继上的配置信息
	 * @return
	 */
	public abstract WifiBean getWirelessedConfig();
	
	/**
	 * 扫描中继无线列表
	 * 
	 * @return
	 */
	public abstract List<WifiBean> getRelayWifiList();
	
	
	/**
	 * @desc 获取有线拨号配置
	 * @return
	 */
	public WanBean getDialConfig(){return null;}

	/**
	 * 设置中继无线上网配置
	 * 
	 * @param ssid
	 * @param charset
	 * @param encryption
	 * @param key
	 * @param channel
	 * @param disabled
	 * @return
	 */
	public abstract boolean setRelayWifiConfig(String ssid, String charset, String encryption, String key, String channel, String disabled);

	/**
	 * 设置有线动态IP上网
	 * 
	 * @param proto
	 * @return
	 */
	public abstract boolean setAutoAccess(String proto, String ipaddr, String netmask);

	/**
	 * 设置有线拨号上网
	 * 
	 * @param proto
	 * @param username
	 * @param password
	 * @return
	 */
	public abstract boolean setPppoeAccess(String proto, String username, String password);

	/**
	 * 设置有线静态IP上网
	 * 
	 * @param proto
	 * @param ipaddr
	 * @param netmask
	 * @param gateway
	 * @param dns1
	 * @param dns2
	 * @return
	 */
	public abstract boolean setStaticAccess(String proto, String ipaddr, String netmask, String gateway, String dns1, String dns2);

	/**
	 * 设置恢复出厂设置
	 * 
	 * @return
	 */
	public abstract boolean setFactoryReset();

	/**
	 * 关机
	 * 
	 * @return
	 */
	public abstract boolean shutDownDevice();
	
	/**
	 * 重启
	 * 
	 * @return
	 */
	public abstract boolean reStartDevice();
	
	
	/**
	 * @describtion
	 * @return  连接路由器,远程服务器上固件版本信息 
	 */
	public CQRouterImageFile getRouterImageFile(){ return null;}

	/**
	 * 获取固件信息
	 * 
	 * @return
	 */
	public abstract ZFirmwareInfo getFirmwareInfo();
	
	/**
	 * @describtion 穿强王.获取固件下载更新进度
	 * @return
	 */
	public CQUpdateProgress checkUpdateProgress(){return null;}
	
	/**
	 * 固件下载
	 * 
	 * @return
	 */
	public abstract FirmwareData downloadFirmware(boolean isProgressQuery);
	
	/**
	 * 执行固件升级接口
	 * 
	 * @return
	 */
	public abstract boolean executeUpgrade();
	

	public void setOnLoginTimeoutCallback(OnLoginTimeoutCallback callback) {
		this.callback = callback;
	}

	/**
	 * 过滤 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
	 * 
	 * @param input
	 * @return String
	 */
	public static String filterObj(String input) {
		if (input.contains("\\t")) {
			input = input.replace("\\t", "");
		}
		if (input.contains("\\r")) {
			input = input.replace("\\r", "");
		}
		if (input.contains("\\n")) {
			input = input.replace("\\n", "");
		}
		return input;
	}
}
