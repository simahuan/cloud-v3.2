package com.pisen.router.ui.phone.device;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.studio.os.LogCat;
import cn.jpush.android.data.p;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pisen.router.CloudApplication;
import com.pisen.router.config.Config;
import com.pisen.router.config.HttpKeys;
import com.pisen.router.core.device.AbstractDevice;

/**
 * （净音云路由）
 * 路由器请求参数及缓存信息
 * @author Liuhc
 * @version 1.0 2015年5月12日 下午2:17:09
 */

public class PisenConstant {
	public static String sessionId = "";
	public static String username = "";
	
	public static final String Module_Action_login    = "0";
	public static final String Module_Action_sysInfo  = "3";
	
	public static final String Module_Action_wanMode  = "5";
	
	public static final String Module_Action_wire     = "6";
	public static final String Module_Action_pppoe    = "7";
	public static final String Module_Action_relay    = "8";
	public static final String Module_Action_wireless = "9";
	public static final String Module_Action_reset    = "21";
	public static final String Module_Action_recover  = "22";
	public static final String Module_Action_query    = "23";
	
	/**登陆*/
	public static final String Action_Login = "login";
	/**退出*/
	public static final String Action_LoginOut = "loginOut";
	/**验证登陆*/
	public static final String Action_CheckLogin = "checkLogin";
	/**查询是否已有其它用户登录*/
	public static final String Action_Search = "search";
	/**查询路由器是否已进行过配置*/
	public static final String Action_UserInitRead = "userInitRead";
	/**设置路由器为已配置*/
	public static final String Action_UserInitSave = "userInitSave";
	/**查询用户是否同意协议*/
	public static final String Action_AgreementStaRead = "agreementStaRead";
	/**用户同意协议*/
	public static final String Action_SaveAgreementSel = "saveAgreementSel";
	
	/**读取上网模式配置*/
	public static final String Action_CurrentLinkModeRead = "currentLinkModeRead";
	public static final String Action_CQCurrentLinkModeRead = "getConfig";
	public static final String WanMode_CQDefaultMode = "static";
	
	/**读取有线上网配置*/
	public static final String Action_WiredConfigRead = "WiredConfigRead";
	public static final String Action_CQWiredConfigRead = "getConfig";
	
	/** 上网拔号 */
	public static final String Action_CQPPPoeConfigRead = "getConfig";
	
	/**读取无线中继上网配置*/
	public static final String Action_ReadRepeatStatus = "readRepeatStatus";
	public static final String Action_CQWirelessConfigRead = "getConfig";
	
	
	/**读取无线配置*/
	public static final String Action_WireLesscfgRead = "WireLesscfgRead";
	public static final String Action_CQWireLessgetConfig = "getConfig";
	
	/**修改无线配置*/
	public static final String Action_WireLesscfgSave = "WireLesscfgSave";
	public static final String Action_CQWireLesscfg = "setConfig";
	
	/**无线中继个数扫描 & 设置无线中继上网*/
	public static final String Action_LinkAp = "linkAp";
	public static final String Action_CQRelayConnect = "connect";
	public static final String Action_CQScan = "scan";
	/**无线中继列表扫描 */
	public static final String Action_ReadValidAps = "readValidAps";
	public static final String Action_CQReadValidAps = "getInfo";
	/**查询无线中继是否成功 */
	public static final String Action_ReadApLinkSta = "readApLinkSta";
	
	/**设置静态获取IP上网 */
	public static final String Action_WiredConfigSave = "WiredConfigSave";
	public static final String Action_CQWiredConfigSave = "setConfig";
	/**设置动态获取IP上网*/
	public static final String Action_DynamicConfigSave = "DynamicConfigSave";
	public static final String Action_CQDynamicConfig = "setConfig";
	/**设置/断开宽带拨号上网*/
	public static final String Action_PppoeAccountSave = "pppoeAccountSave";
	public static final String Action_CQPppoeAccountConnect = "connect";
	/**读取宽带拨号上网*/
	public static final String Action_PppoeAccountRead = "pppoeAccountRead";
	public static final String Action_CQPppoeAccountRead = "getConfig";
	public static final String Action_CQPppoeStatusRead = "getStatus";
	
	/**设置恢复出厂*/
	public static final String Action_ResetToDefaultAction = "resetToDefaultAction";
	public static final String Action_CQRecoverToDefaultAction = "recover";
	/**恢复出厂后重启路由器*/
	public static final String Action_RouteReboot = "routeReboot";
	public static final String Action_CQRouteReset = "reset";
	
	public static final String Action_CQRouteQueryInfo = "query";
	
	public static final String Action_CQRouteRelayQuery = "getStatus";
	public static final String Action_CQRouteRestartStatus = "getStatus";
	
	/** 读取穿墙王 .系统信息 */
	public static final String Action_CQRouteSystemInfo = "getInfo";
	
	/** 读取穿墙王 .镜像文件下载 */
	public static final String Action_CQDownLoad = "download";
	
	
	
	/**
	 * 请求路由器配置信息
	 * @param action  操作方法
	 * @param listener 回调
	 */
	public static void request(final String action,final IRouterResponse listener) {
		Map<String, String> params = new HashMap<String, String>();
		if (Action_Login.equals(action)) {
			params = PisenConstant.getLoginParams();
		}else if(Action_WireLesscfgRead.equals(action)){
			params = PisenConstant.getWirelessParams();
		}else if(Action_LinkAp.equals(action)){
			params = getWirelessListNumsParams();
		}else if(Action_ReadValidAps.equals(action)){
			params = getWirelessListParams();
		}else if(Action_WiredConfigRead.equals(action)){
			params = getWiredParams();
		}
		request(action, params, listener);
	}
	
	/**
	 * * 请求路由器配置信息
	 * @param action  操作方法
	 * @param listener 回调
	 * @param params 请求参数
	 */
	public static void request(final String action,final Map<String, String> params,final IRouterResponse listener) {
		StringRequest stringRequest = new StringRequest(Request.Method.POST, HttpKeys.ROUTER_URL, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				LogCat.i("response -> " + response);
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
			protected Map<String, String> getParams() throws AuthFailureError {
				LogCat.i("<Pisen>参数 -> " + params.toString());
				return params;
			}
//			@Override
//			public byte[] getBody() throws AuthFailureError {
//				LogCat.i("<Pisen>参数 -> " + params.toString());
//				return params.toString().getBytes();
//			}
		};

		RetryPolicy retryPolicy = new DefaultRetryPolicy(30000
				, DefaultRetryPolicy.DEFAULT_MAX_RETRIES
				, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
		stringRequest.setRetryPolicy(retryPolicy);
		Volley.newRequestQueue(CloudApplication.getInstance()).add(stringRequest);
	}
	
	/**
	 * 检查登陆验证
	 * @return
	 */
	public static Map<String, String> getCheckLoginParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		map.put("username", "pisen");
		map.put("actName", "checkLogin");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
	
	/**
	 * 获取登陆参数
	 * @return
	 */
	public static Map<String, String> getLoginParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("password", "pisen");
		map.put("actName", "login");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
	
	
	/**
	 * 穿墙王.读取读系统信息
	 * @return
	 */
	public static Map<String, String> getCQRouteSystemInfo() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("moduleId", Module_Action_sysInfo);
		map.put("sessionId", Config.getRouterSessionId());
		map.put("username", Config.getRouterUserName());
		map.put("module", "sysInfo");
		map.put("action", "getInfo");
		return map;
	}
	
	/**
	 * 读取上网模式配置
	 * @return
	 */
	public static Map<String, String> getWiredLinkModeParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("actName", Action_CurrentLinkModeRead);
		map.put("reqNames", "readCurrentLinkMode");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
	
	/**
	 * 穿墙王.读取上网模式配置
	 * @return
	 */
	public static Map<String, String> getCQWiredLinkModeParams() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("moduleId", Module_Action_wanMode);
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("module", "wanMode");
		map.put("action", "getConfig");
		return map;
	}
	
	/**
	 * 获取有线上网参数
	 * @return
	 */
	public static Map<String, String> getWiredParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("actName", Action_WiredConfigRead);
		map.put("reqNames", "readWiredConfig");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
	
	/**
	 * 获取 穿墙王 有线上网参数
	 * @return
	 */
	public static Map<String, String> getCQWiredParams(String pMode){
		Map<String, String> map = new HashMap<String, String>();
		map.put("moduleId", Module_Action_wire);
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("module", "wire");
		map.put("action", "getConfig");
		map.put("mode", pMode);
		return map;
	}
	

	/**
	 * 获取 穿墙王 PPPoe上网数据 
	 * @return
	 */
	public static Map<String, String> getCQPPPoeParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("moduleId", Module_Action_pppoe);
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("module", "pppoe");
		map.put("action", "getConfig");
		return map;
	}
	
	/**
	 * 读取无线中继上网配置
	 * @return
	 */
	public static Map<String, String> getWirelessStatusParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("actName", Action_ReadRepeatStatus);
		map.put("reqNames", "readRepeatStatus");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
	
	/**
	 *  穿墙王.读取 无线中继上网配置
	 * @return
	 */
	public static Map<String, String> getCQWirelessStatusParams(){
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("moduleId",   Module_Action_relay);
		map.put("sessionId", sessionId);
		map.put("username",  username);
		map.put("module",   "relay");
		map.put("action",   "getConfig");
		return map;
	}
	
	/**
	 * 读取宽带拨号上网配置
	 * @return
	 */
	public static Map<String, String> getWiredDialParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("actName", Action_PppoeAccountRead);
		map.put("reqNames", "readPppoeAccount");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
	
	/**
	 * 穿墙王.读取宽带拨号上网配置
	 * @return
	 */
	public static Map<String, String> getCQWiredDialParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("moduleId", Module_Action_pppoe);
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("module", "pppoe");
		map.put("action", Action_CQPppoeAccountRead);
		return map;
	}
	
	/**
	 * 穿墙王.读取宽带拨号上网状态
	 * @return
	 */
	public static Map<String, String> getCQWiredDialStatusParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("moduleId", Module_Action_pppoe);
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("module", "pppoe");
		map.put("action", Action_CQPppoeStatusRead);
		return map;
	}
	
	/**
	 * 获取无线参数
	 * @return
	 */
	public static Map<String, String> getWirelessParams() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("actName", Action_WireLesscfgRead);
		map.put("reqNames", "readWireLesscfg");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
	/**
	 * 穿墙王.获取无线参数
	 * @return
	 */
	public static Map<String, String> getCQWirelessParams() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("moduleId", Module_Action_wireless);
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("module", "wireless");
		map.put("action", "getConfig");
		return map;
	}
	
	/**
	 * 无线中继个数扫描
	 * @return
	 */
	public static Map<String, String> getWirelessListNumsParams(){
		Map<String, String> map = new HashMap<String, String>();
		try {
			JSONObject j2 = new JSONObject();
			j2.put("resv", "resv");
			JSONObject j = new JSONObject();
			j.put("sessionId", PisenConstant.sessionId);
			j.put("username", PisenConstant.username);
			j.put("actName", PisenConstant.Action_LinkAp);
			j.put("reqNames", "scanValidAps");
			j.put("cfgvalues", j2);
			map.put("datas", j.toString());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return map;
	}
	
	/**
	 * 穿墙王.无线中继个数扫描
	 * @return
	 */
	public static Map<String, String> getCQWirelessListNumsParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("moduleId", Module_Action_relay);
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("module", "relay");
		map.put("action", "scan");
		return map;
	}
	
	/**
	 * 无线中继列表扫描
	 * @return
	 */
	public static Map<String, String> getWirelessListParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", PisenConstant.sessionId);
		map.put("username", PisenConstant.username);
		map.put("actName", PisenConstant.Action_ReadValidAps);
		map.put("reqNames", "0");
		return map;
	}
	
	/**
	 * 穿墙王.无线中继列表扫描
	 * @return
	 */
	public static Map<String, String> getCQWirelessListParams(String pStart){
		Map<String, String> map = new HashMap<String, String>();
		map.put("moduleId", Module_Action_relay);
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("module", "relay");
		map.put("action", "getInfo");
		map.put("start", pStart);
		return map;
	}
	
	/**
	 * 穿墙王.查询无线中继是否成功
	 * @return
	 */
	public static Map<String, String> getCQIsRelayWirelessSuccessParmas(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("moduleId", Module_Action_relay);
		map.put("sessionId", sessionId);
		map.put("username", username);
		map.put("module", "relay");
		map.put("action", "getStatus");
		return map;
	}
	
	/**
	 * 查询无线中继是否成功
	 * @return
	 */
	public static Map<String, String> getIsWirelessSuccessParams(){
		Map<String, String> map = new HashMap<String, String>();
		map.put("sessionId", PisenConstant.sessionId);
		map.put("username", PisenConstant.username);
		map.put("actName", PisenConstant.Action_ReadApLinkSta);
		map.put("reqNames", "readApLinkSta");
		JSONObject jsonObject = new JSONObject(map);
		map.clear();
		map.put("datas", jsonObject.toString());
		return map;
	}
}
