package com.pisen.router.ui.phone.device;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.studio.net.HttpURLConnectionUtils;
import android.studio.os.LogCat;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.GsonUtils;
import com.pisen.router.CloudApplication;
import com.pisen.router.common.utils.NetUtil;

/**
 * （追剧·云路由） 路由器请求参数及缓存信息
 * 
 * @author Liuhc
 * @version 1.0 2015年5月12日 下午2:17:09
 */
public class ChaseDramaConstant {

	/** token */
	public static final String token = "";
	/** 获取获取设备sn */
	public static final String Url_GETSYS_SN = "http://%s/index.php/config/sys/sys_get_sn";
	/** 2.1.1 描述：获取(设备自身)wlan配置*/
	public static final String Url_GET_WALN = "http://%s/index.php/config/lan/get_wlan_config?index=wlan";
	/** 2.1.2 描述：设置(设备自身)wlan配置*/
	public static final String Url_SET_WALN = "http://%s/index.php/config/lan/set_wlan_config?index=wlan";
	/** 2.1.3 描述：获取当前设备（router）的外网连接状态 */
	public static final String Url_GET_ROUTER_STATUS = "http://%s/index.php/config/wan/get_wan_status";
	/** 2.1.4 描述：设置当前设备（router）的外网配置信息 */
	public static final String Url_SET_ROUTER_STATUS = "http://%s/index.php/config/wan/set_wan_config";
	/** 2.1.5 描述：自动探测当前设备（router）的有线网络的可用连接类型*/
	public static final String Url_GET_WALN_TYPE = "http://%s/index.php/config/wan/get_wan_type";
	/** 2.1.6 描述：获取设备（router）的当前的sta连接状态 */
	public static final String Url_GET_STA_STATUS = "http://%s/index.php/config/wan/get_sta_status";
	/** 2.1.7 描述：获取(设备连接到的WIFI)中继配置 */
	public static final String Url_GET_STA_CONFIG = "http://%s/index.php/config/wan/get_sta_config";
	/** 2.1.8 描述：设置(设备连接到的WIFI)中继配置 */
	public static final String Url_SET_STA_CONFIG = "http://%s/index.php/config/wan/set_sta_config";
	/** 2.1.9  获取设备（router）扫描的wifi列表 */
	public static final String Url_GET_STA_LIST = "http://%s/index.php/config/wan/get_sta_list";
	/** 2.10描述：获取当前设备（router）的internet连接状态 */
	public static final String Url_GET_WAN_STATUS = "http://%s/index.php/config/wan/get_internet_status";
	/** 2.15 描述：读取当前设备（router）的外网配置信息（写入config/network文件） */
	public static final String Url_GET_WAN_CONFIG = "http://%s/index.php/config/wan/get_wan_config";
	
	/** 2.1.11 描述：使能wlan配置 */
	public static final String Url_SET_WLAN_RELOAD = "http://%s/index.php/config/lan/set_wlan_reload";
	/** 5.1 描述：获取无线宝固件信息 */
	public static final String Url_SYS_GET_DEVINFO = "http://%s/index.php/config/sys/sys_get_devinfo";
	/** 5.2 描述：执行固件升级接口 */
	public static final String Url_SYS_UPGRADE = "http://%s/index.php/config/sys/sys_software_upgrade";
	/** 5.3 描述：手动触发固件下载接口 */
	public static final String Url_SYS_DOWN_FIRMWARE = "http://%s/index.php/config/sys/sys_down_upgrade";
	/** 8.3 描述: 盒子reset */
	public static final String Url_SYS_FACTORY_RESET = "http://%s/index.php/config/sys/sys_factory_reset";
	/** 7.6 描述: 盒子重启 */
	public static final String Url_SYS_REBOOT = "http://%s/index.php/config/sys/sys_reboot";
	/** 8.4 描述: 关机 */
	public static final String Url_SYS_SHUTDOWN = "http://%s/index.php/config/sys/sys_shutdown";
	
	public static String doGet(String url) throws IOException{
		String ipAddress = NetUtil.getGetwayIPAddress(CloudApplication.getInstance());
		if (TextUtils.isEmpty(ipAddress)) {
			LogCat.e("<ZX>error:ipAddress null");
			return "";
		}

		url = String.format(url, ipAddress);
		LogCat.i("<ZX>请求:" + url);
		String result = HttpURLConnectionUtils.doGet(url, null);
		LogCat.i("<ZX>返回:" + result);
		return result;
	}
	
	public static String doPost(String url,Map<String,String> map) throws IOException{
		String ipAddress = NetUtil.getGetwayIPAddress(CloudApplication.getInstance());
		if (TextUtils.isEmpty(ipAddress)) {
			LogCat.e("<ZX>error:ipAddress null");
			return "";
		}
		String entity = GsonUtils.jsonSerializer(map);
		url = String.format(url, ipAddress);
		LogCat.i("<ZX>请求:" + url + " \n 参数："+entity);
		String result = HttpURLConnectionUtils.doPost(url, "application/json", entity.getBytes());
//		String result =HttpURLConnectionUtils.doPost(url, map.toString().getBytes(), "UTF-8");
		LogCat.i("<ZX>返回:" + result);
		return result;
	}
	
	/**
	 * 请求接口基础方法(封装用于统一日志变量信息)
	 * @param url
	 * @param listener
	 */
	public static void request(String url, final IRouterResponse listener) {
		String ipAddress = NetUtil.getGetwayIPAddress(CloudApplication.getInstance());
		if (TextUtils.isEmpty(ipAddress)) {
			LogCat.e("<ZX>error:ipAddress null");
			return;
		}

		url = String.format(url, ipAddress);
		LogCat.i("<ZX>请求:" + url);

		StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Listener<String>() {
			@Override
			public void onResponse(String response) {
				LogCat.i("response -> " + response);
				listener.onSuccess(response);
			}
		}, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				LogCat.e("error:" + error.getMessage());
				listener.onError(error.getMessage());
			}
		});

		RetryPolicy retryPolicy = new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		stringRequest.setRetryPolicy(retryPolicy);
		Volley.newRequestQueue(CloudApplication.getInstance()).add(stringRequest);
	}

	/**
	 * * 请求路由器配置信息
	 * 
	 * @param action
	 *            操作方法
	 * @param listener
	 *            回调
	 * @param params
	 *            请求参数
	 */
	public static void request(String url, final HashMap<String, String> params, final IRouterResponse listener) {
		String ipAddress = NetUtil.getGetwayIPAddress(CloudApplication.getInstance());
		if (TextUtils.isEmpty(ipAddress)) {
			LogCat.e("<ZX>error:ipAddress null");
			return;
		}

		url = String.format(url, ipAddress);
		LogCat.i("<ZX>请求:" + url);

		StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Listener<String>() {
			@Override
			public void onResponse(String response) {
				LogCat.i("<ZX>response -> " + response);
				listener.onSuccess(response);
			}
		}, new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				LogCat.e("error:" + error.getMessage());
				listener.onError(error.getMessage());
			}
		}) {
			@Override
			public byte[] getBody() throws AuthFailureError {
				JSONObject obj = new JSONObject(params);
				LogCat.i("<ZX>参数 -> " + obj.toString());
				return params == null ? super.getBody() : obj.toString().getBytes();
			}
		};
		
		RetryPolicy retryPolicy = new DefaultRetryPolicy(15000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		stringRequest.setRetryPolicy(retryPolicy);
		Volley.newRequestQueue(CloudApplication.getInstance()).add(stringRequest);
	}
}
