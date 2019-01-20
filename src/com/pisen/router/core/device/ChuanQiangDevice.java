package com.pisen.router.core.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.studio.os.LogCat;
import android.studio.os.NetUtils;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonUtils;
import com.google.gson.reflect.TypeToken;
import com.pisen.router.common.utils.ChangeCharset;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.config.Config;
import com.pisen.router.config.HttpKeys;
import com.pisen.router.config.ResourceConfig;
import com.pisen.router.core.monitor.entity.RouterConfig.Model;
import com.pisen.router.ui.phone.device.ChaseDramaConstant;
import com.pisen.router.ui.phone.device.LoginActivity;
import com.pisen.router.ui.phone.device.PisenConstant;
import com.pisen.router.ui.phone.device.bean.CQRouterImageFile;
import com.pisen.router.ui.phone.device.bean.CQUpdateProgress;
import com.pisen.router.ui.phone.device.bean.FirmwareData;
import com.pisen.router.ui.phone.device.bean.PWifiInfo;
import com.pisen.router.ui.phone.device.bean.RelayConfBean;
import com.pisen.router.ui.phone.device.bean.RouterBean;
import com.pisen.router.ui.phone.device.bean.RouterBean.InternetState;
import com.pisen.router.ui.phone.device.bean.RouterBean.InternetType;
import com.pisen.router.ui.phone.device.bean.StaBean;
import com.pisen.router.ui.phone.device.bean.WanBean;
import com.pisen.router.ui.phone.device.bean.WifiBean;
import com.pisen.router.ui.phone.device.bean.ZFirmwareInfo;

/**
 * @author  mahuan
 * @version 1.0 2015年9月29日 下午2:46:10
 * @desc{穿墙王.云路由-协议交互实现类}
 */
public class ChuanQiangDevice extends AbstractDevice {
	private Context mCtx ;
	/**
	 * 
	 */
	public ChuanQiangDevice(Context ctx) {
		this.mCtx = ctx;
	}
	
	@Override
	public ArrayList<WifiBean> getDeviceList(Context ctx) {
		ArrayList<WifiBean> result = new ArrayList<WifiBean>();
//				限定品胜设备
//				WifiBean wifi = null;
//				WifiConnectUtils wifiSearch = new WifiConnectUtils(ctx);
//				if (wifiSearch.startScan()) {
//					List<ScanResult> list = wifiSearch.getWifiList();
//					for (ScanResult scanResult : list) {
//						if (scanResult.BSSID.startsWith(WifiMonitor.PISEN_BSSID_PREFIX)) {
//							wifi = new WifiBean();
//							wifi.setSsid(scanResult.SSID);
//							wifi.setEncryption(scanResult.capabilities);
//							wifi.setSignal(scanResult.level+"");
//							result.add(wifi);
//						}
//					}
//					list.clear();
//				}
		return result;
	}
	
	/**
	 * 获取无线配置
	 */
	@Override
	public WifiBean getWifiConfig() {
		WifiBean wifi = null;
		try {
			String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, PisenConstant.getCQWirelessParams());
			if (!TextUtils.isEmpty(result)) {
				if (isRouterNLogin(result)) {
					return wifi;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("action");
					if (PisenConstant.Action_CQWireLessgetConfig.equals(action)) {
						String info = obj.getString("values");
						if (!TextUtils.isEmpty(info)) {
							wifi = GsonUtils.jsonDeserializer(info, WifiBean.class);
							wifi.setEncryption(wifi.getEncrypt());
							wifi.setKey(wifi.getPassword());
							wifi.setSsid(wifi.getEssid());
							wifi.setOpen_wmm(wifi.getOpenWmm());
							wifi.setShortgi(wifi.getShortGi());
							wifi.setWireLessSwitch(wifi.getSw());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wifi;
	}


	/**
	 * 多种设备间协议转换,重点看看
	 */
	@Override
	public boolean setWifiConfig(WifiBean wifi) {
		try {
			wifi.setWireless_net_name(wifi.getSsid());
			wifi.setWireless_net_passwd(wifi.getKey());
			wifi.setWireless_net_secruity(wifi.getEncryption());
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("moduleId", PisenConstant.Module_Action_wireless);
			map.put("sessionId", PisenConstant.sessionId);
			map.put("userName", PisenConstant.username);
			map.put("module", "wireless");
			map.put("action", PisenConstant.Action_CQWireLesscfg);
			map.put("essid", wifi.getSsid());
			map.put("encrypt", wifi.getEncryption());
			map.put("password", wifi.getKey());
			map.put("hideEssid", wifi.getHideEssid());
			map.put("channel", wifi.getChannel());
			map.put("mode", wifi.getMode());
			map.put("rate", wifi.getRate());
			map.put("apSeparate", wifi.getApSeparate());
			map.put("shortGi", wifi.getShortGi());
			map.put("openWmm", wifi.getOpenWmm());
			map.put("power", wifi.getPower());
			
			String result = requestPost(HttpKeys.ROUTER_KINGWALL_URL, map);
			if (!TextUtils.isEmpty(result)) {
				if ("true".equals(result)) {
					return true;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("action");
					if (PisenConstant.Action_CQWireLesscfg.equals(action)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} 
		return false;
	}

	/**
	 * @desc 获取路由系统信息　
	 */
	public void getRouterSystemInfo() throws JSONException {
		String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, PisenConstant.getCQRouteSystemInfo());
		if (!TextUtils.isEmpty(result)) {
			if (isRouterNLogin(result)){
				return ;
			}
			JSONObject obj = new JSONObject(result);
			if (obj.getBoolean("result")) {
				obj  = obj.getJSONObject("values");
//				String action = obj.getString("action");
//				if (PisenConstant.Action_CQCurrentLinkModeRead.equals(action)) {
//					String mode = obj.getJSONObject("values").getString("mode");
//					return obj.getJSONObject("values").getString("mode");
//				}
			}
		}
	}
	
	/**
	 * 获取中继类型　
	 */
	@Override
	public RouterBean getRelayType() {
		RouterBean bean = new RouterBean();;
		try {
			String route = getCQWanMode();
			if ("relay".equals(route)) {
				// 无线中继
				bean.type = InternetType.sta;
				String info = getRelaySetting();
				if (!TextUtils.isEmpty(info)) {
					StaBean sta = GsonUtils.jsonDeserializer(info, StaBean.class);
					if ("ON".equalsIgnoreCase(sta.getSw())) { //无线中断开启开关
						bean.state = InternetState.up;
					} else {
						bean.state = InternetState.down;
					}
				}
			} else if ("static".equals(route) || "dynamic".equals(route) ||"pppoe".equals(route)) {//三种上网方式
				bean.type = InternetType.wan;
				String info = getWiredDialStatus();
				//有线上网或拔号配置
				if (!TextUtils.isEmpty(info)) {
					WanBean wan = GsonUtils.jsonDeserializer(info, WanBean.class);
					if ("ON".equalsIgnoreCase(wan.getWiredStatus())) {
						bean.state = InternetState.up;
					}else{
						bean.state = InternetState.down;
					}
				}
			} else {
			// 其它类型上网方式(3G上网)
				bean.type = InternetType.unknown;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bean;
	}

	/**
	 * 有线上网，那么mode为static或者dynamic，
	 * 宽带拨号，那么mode为pppoe
	 * 无线中继，那么mode为relay;
	 * @describtion 三种类型.上网模式描述
	 * @return  获取上网模式
	 * @throws JSONException
	 */
	private String getCQWanMode() throws JSONException {
		String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, PisenConstant.getCQWiredLinkModeParams());
		if (!TextUtils.isEmpty(result)) {
			if (isRouterNLogin(result)){
				return null;
			}
			
			JSONObject obj = new JSONObject(result);
			if (obj.getBoolean("result")) {
				String action = obj.getString("action");
				if (PisenConstant.Action_CQCurrentLinkModeRead.equals(action)) {
					return obj.getJSONObject("values").getString("mode");
				}
			}
		}
		return null;
	}
	
	
	/**
	 * @describtion 获取有线配置信息
	 * @return value 状态对像　字符串
	 * @throws JSONException
	 */
	private String getWiredCfg() throws JSONException {
		String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, PisenConstant.getCQWiredParams(Config.getCQRouterWiredProto()));
		if (!TextUtils.isEmpty(result)) {
			if (isRouterNLogin(result)){
				return null;
			}
			JSONObject obj = new JSONObject(result);
			if (obj.getBoolean("result")) {
				String action = obj.getString("action");
				if (PisenConstant.Action_CQWiredConfigRead.equals(action)) {
					return  obj.optString("values");
				}
			}
		}
		return null;
	}
	
	
	/**
	 * @describtion 获取 宽带拔号上网配置
	 * @return
	 * @throws JSONException
	 */
	private JSONObject getWiredDialCfg() throws JSONException {
		String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, PisenConstant.getCQWiredDialParams());
		if (!TextUtils.isEmpty(result)) {
			if (isRouterNLogin(result)){
				return null;
			}
			JSONObject obj = new JSONObject(result);
			if (obj.getBoolean("result")) {
				String action = obj.getString("action");
				if (PisenConstant.Action_CQPppoeAccountRead.equals(action)) {
					return obj.optJSONObject("values");
				}
			}
		}
		return null;
	}
	
	
	/**
	 * @describtion 获取宽带拔号上网状态
	 * @return
	 * @throws JSONException
	 */
	private String getWiredDialStatus() throws JSONException {
		String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, PisenConstant.getCQWiredDialStatusParams());
		if (!TextUtils.isEmpty(result)) {
			if (isRouterNLogin(result)){
				return null;
			}
			JSONObject obj = new JSONObject(result);
			if (obj.getBoolean("result")) {
				String action = obj.getString("action");
				if (PisenConstant.Action_CQPppoeStatusRead.equals(action)) {
					return obj.optJSONObject("values").getString("status");
				}
			}
		}
		return null;
	}
	
	
	/**
	 * @describtion 获取有线配置信息
	 * @param bean
	 * @return 中继Bean 
	 * @throws JSONException
	 */
	private RelayConfBean getWiredConfig(RelayConfBean bean) throws JSONException {
		String info = getWiredCfg();
		String mode = getCQWanMode();
//		getCQPPPoeCfg();
		
		if (!TextUtils.isEmpty(info)) {
			bean.setWan(GsonUtils.jsonDeserializer(info, WanBean.class));
			if (mode == null||"none".equalsIgnoreCase(mode)) {
				bean.wan.setPhysics_state("disconnect");
			} else {
				bean.wan.setPhysics_state("connect");
			}

			bean.wan.setExist("yes");
			bean.wan.setIpaddr(bean.wan.getIp());
			bean.wan.setNetmask(bean.wan.getNetMask());
			bean.wan.setGateway(bean.wan.getGateway());
			bean.wan.setDns1(bean.wan.getFirstDns());
			bean.wan.setDns2(bean.wan.getSecondDns());
			
			if ("static".equals(bean.wan.getMode())) {
				bean.wan.setProto("static");
			} else if("pppoe".equals(bean.wan.getMode()) || mode.equals("pppoe")){
				bean.wan.setProto("pppoe");
				JSONObject obj = getWiredDialCfg();
				if (obj != null) {
					bean.wan.setUsername(obj.optString("account"));
					bean.wan.setAccount(obj.optString("account"));
					bean.wan.setPassword(obj.optString("password"));
				}
			} else if ("dynamic".equals(bean.wan.getMode()) || "none".equals(bean.wan.getMode())) {
				bean.wan.setProto("dhcp");
			} 
		}
		return bean;
	}
	
	/**
	 * @desc    获取无线中继配置信息　
	 * @return 　无线中继上网配置信息 Values	值信息,字符串
	 * @throws JSONException
	 */
	private String getRelaySetting() throws JSONException {
		String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, PisenConstant.getCQWirelessStatusParams());
		
		if (!TextUtils.isEmpty(result)) {
			if (isRouterNLogin(result)){
				return null;
			}
			JSONObject obj = new JSONObject(result);
			if (obj.getBoolean("result")) {
				String action = obj.getString("action");
				if (PisenConstant.Action_CQWirelessConfigRead.equals(action)) {
					return obj.optString("values");
				}
			}
		}
		return null;
	}
	
	/**
	 * @describtion 获取无线中继.上网配置信息　
	 * @param bean
	 * @return 中继信息
	 */
	private RelayConfBean getWirelessCfg(RelayConfBean bean) throws JSONException {
		String info = getRelaySetting();
		if (!TextUtils.isEmpty(info)) {
			bean.setSta(GsonUtils.jsonDeserializer(info, StaBean.class));
			if ("on".equalsIgnoreCase(bean.sta.getSw())) {
				bean.sta.setNet_state("1");
			} else {
				bean.sta.setNet_state("0");
			}
			bean.sta.setExist("yes");
			bean.sta.setSsid(bean.sta.getEssid());
			bean.sta.setWirelessTerm(bean.sta.getWirelessTerm());
			bean.sta.setApPassword(bean.sta.getPassword());
		}
		return bean;
	}
	
	
	/**
	 * 1.先获取上网行为;以何种方式上网
	 * 获取中继配置信息　　
	 */
	@Override
	public RelayConfBean getRelayConfig() {
		RelayConfBean bean = new RelayConfBean();
		try {
//			String internetBehavior = getCQWanMode();
			getWirelessCfg(bean);
			getWiredConfig(bean);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bean;
	}

	/**
	 * @describtion 判断路由是否未登录
	 * @param result
	 * @return
	 */
	public boolean isRouterNLogin(String result){
		if (result.equalsIgnoreCase("WHR001N_NLogin")) {
			if (callback != null) {
				callback.onLoginTimeout();
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 获取无线配置
	 */
	@Override
	public WifiBean getWirelessedConfig() {
		try {
			String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, PisenConstant.getCQWirelessStatusParams());
			if (!TextUtils.isEmpty(result)) {
				if (isRouterNLogin(result)){
					return null;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("action");
					if (PisenConstant.Action_CQWirelessConfigRead.equals(action)) {
						String info = obj.optString("values");
						if (!TextUtils.isEmpty(info)) {
							StaBean sta = GsonUtils.jsonDeserializer(info, StaBean.class);
							WifiBean bean = new WifiBean();
							if ("on".equalsIgnoreCase(sta.getSw())) {
								bean.setConnnect(true);
							} else {
								bean.setConnnect(false);
							}
							bean.setSsid(sta.getEssid());
							bean.setKey(sta.getPassword());
							return bean;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @describtion 扫描中继wifi列表
	 * @return  增加单个wifi信息集合
	 * @throws  JSONException
	 */
	private List<WifiBean> scanRelayWifiList() throws JSONException {
		String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, PisenConstant.getCQWirelessListNumsParams());
		if (!TextUtils.isEmpty(result)) {
			if (isRouterNLogin(result)) {
				return null;
			}
			JSONObject obj = new JSONObject(result);
			if (obj.getBoolean("result")) {
				String action = obj.getString("action");
				if (PisenConstant.Action_CQScan.equals(action)) {
					String sumAps = obj.getJSONObject("values").getString("sumAps");
					if (Integer.parseInt(sumAps) > 0) {
						return new ArrayList<WifiBean>();
					}
				}
			}
		}
		return null;
	}
	
	
	/**
	 * @desc 获取中继wifi列表
	 * @return 扫描到wifi列表
	 */
	@Override
	public List<WifiBean> getRelayWifiList() {
		try {
			List<WifiBean> list = scanRelayWifiList();
			if (list != null) { // 扫描到可用wifi,发起第二次cgi.
				String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, PisenConstant.getCQWirelessListParams("0"));
				if (!TextUtils.isEmpty(result)) {
					if (isRouterNLogin(result)) {
						return null;
					}
					JSONObject obj = new JSONObject(result);
					if (obj.getBoolean("result")) {
						String action = obj.getString("action");
						if (PisenConstant.Action_CQReadValidAps.equals(action)) {
							String json = ((obj.getJSONObject("values")).getJSONObject("aps")).getString("apInfo");
							if (!TextUtils.isEmpty(json)) {
								Gson gson = new Gson();
								List<PWifiInfo> pisenList = gson.fromJson(json, new TypeToken<List<PWifiInfo>>() {
								}.getType());
								if (pisenList != null) {
									WifiBean bean = null;
									for (PWifiInfo wifiInfo : pisenList) {
										bean = new WifiBean();
										String ssid = ChangeCharset.getInstance()
												.decodeSpecialChar(wifiInfo.getEssid());
										bean.setSsid(ssid);
										bean.setEncryption(wifiInfo.getEncrypt());
										;
										bean.setKey(wifiInfo.getPassword());
										bean.setRate(wifiInfo.getRate());
										String sn = wifiInfo.getSn();
										if (!TextUtils.isEmpty(sn)) {
											sn = sn.substring(0, sn.indexOf(":"));
											bean.setSignal("" + 2 * (Integer.parseInt(sn)));
										}
										list.add(bean);
									}
									pisenList.clear();
								}
							}
						}
					}
				}
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 设置 中继Wifi配置
	 */
	@Override
	public boolean setRelayWifiConfig(String ssid, String charset, String encryption, String key, String channel, String disabled) {
		try {
			LogCat.e("...setRelayWifiConfig...设置中断wifi配置...");
			WifiBean bean = getWifiConfig();
			if (bean != null) {
				Config.setWifiConfig(GsonUtils.jsonSerializer(bean));
			}

			Map<String, String> map = new HashMap<String, String>();
			map.put("moduleId", PisenConstant.Module_Action_relay);
			map.put("sessionId", PisenConstant.sessionId);
			map.put("userName", PisenConstant.username);
			map.put("module", "relay");
			map.put("action", "connect");
			map.put("essid", ssid);
			map.put("password", key);
			map.put("encrypt", encryption);
			map.put("encoding", "utf-8");

			String result = requestPost(HttpKeys.ROUTER_KINGWALL_URL, map);
			if (!TextUtils.isEmpty(result)) {
				if ("true".equals(result)) {
					return true;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("action");
					if (PisenConstant.Action_CQRelayConnect.equals(action)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 有线动态Dynamic动态配置ip
	 */
	@Override
	public boolean setAutoAccess(String proto,String ipaddr,String netmask) {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("moduleId", PisenConstant.Module_Action_wire);
			map.put("sessionId", PisenConstant.sessionId);
			map.put("userName", PisenConstant.username);
			map.put("module", "wire");
			map.put("action", "setConfig");
			map.put("mode", "dynamic");
			
			String result = requestPost(HttpKeys.ROUTER_KINGWALL_URL, map);
			if (!TextUtils.isEmpty(result)) {
				if ("true".equals(result)) {
					return true;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("action");
					if (PisenConstant.Action_CQDynamicConfig.equals(action)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	

	@Override
	public boolean setPppoeAccess(String proto, String username, String password) {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("moduleId", PisenConstant.Module_Action_pppoe);
			map.put("sessionId", PisenConstant.sessionId);
			map.put("userName", PisenConstant.username);
			map.put("module", "pppoe");
			map.put("action", "connect");
			map.put("account", username);
			map.put("password", password);

			String result = requestPost(HttpKeys.ROUTER_KINGWALL_URL, map);
			if (!TextUtils.isEmpty(result)) {
				if ("true".equals(result)) {
					return true;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("action");
					if (PisenConstant.Action_CQPppoeAccountConnect.equals(action)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	

	@Override
	public boolean setStaticAccess(String proto, String ipaddr, String netmask, String gateway, String dns1, String dns2) {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("moduleId", PisenConstant.Module_Action_wire);
			map.put("sessionId", PisenConstant.sessionId);
			map.put("userName", PisenConstant.username);
			map.put("module", "wire");
			map.put("action", PisenConstant.Action_CQWiredConfigSave);
			map.put("mode", "static");
			map.put("ip", ipaddr);
			map.put("netMask", netmask);
			map.put("gateway", gateway);
			map.put("firstDns", dns1);
			map.put("secondDns", dns2);
			
			if (TextUtils.isEmpty(dns2)) {
				dns2 = "NULL1";
			}
			String result = requestPost(HttpKeys.ROUTER_KINGWALL_URL, map);
			LogCat.e("静态设置IP：result:" +result);
			if (!TextUtils.isEmpty(result)) {
				if ("true".equals(result)) {
					return true;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("action");
					if (PisenConstant.Action_CQWiredConfigSave.equals(action)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 恢复出厂设置
	 */
	@Override
	public boolean setFactoryReset() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("moduleId", PisenConstant.Module_Action_recover);
		map.put("sessionId", PisenConstant.sessionId);
		map.put("userName", PisenConstant.username);
		map.put("module", "restoreFactory");
		map.put("action", "recover");
		try {
			String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, map);
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("action");
					if (PisenConstant.Action_CQRecoverToDefaultAction.equals(action)) {
						return reStartDevice();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean shutDownDevice() {
		//pinse设备无此功能
		return false;
	}

	/**
	 * 重启路由设备实现
	 */
	@Override
	public boolean reStartDevice() {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("moduleId", PisenConstant.Module_Action_reset);
			map.put("sessionId", PisenConstant.sessionId);
			map.put("userName", PisenConstant.username);
			map.put("module", "reset");
			map.put("action", "reset");

			String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, map);
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("action");
					if (PisenConstant.Action_CQRouteReset.equals(action)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	
	@Override
	public boolean login(String password) {
		LogCat.e("login.password:"+password);
		if (!TextUtils.isEmpty(password)) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("moduleId", PisenConstant.Module_Action_login);
			map.put("password", password);
			map.put("module", "login");
			map.put("action", "login");
			
			try {
				String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, map);
				if (!TextUtils.isEmpty(result)) {
					JSONObject obj = new JSONObject(result);
					if (obj.getBoolean("result")) {
						//返回成功
						String action = obj.optString("action");
						if (PisenConstant.Action_Login.equals(action)) {
							//登陆成功
							String str  = obj.optString("values");
//							obj.getJSONObject("values").getString(name);
							JSONObject js = new JSONObject(str);
							PisenConstant.sessionId = js.optString("sessionId");
							PisenConstant.username = js.optString("userName");
							Config.setDeviceMgrPassword(password);
							Config.setRouterSessionId(PisenConstant.sessionId);
							Config.setRouterUserName(PisenConstant.username);
							LogCat.e("RouterParamsUtils.sessionId -> " + PisenConstant.sessionId);
							LogCat.e("RouterParamsUtils.username -> " + PisenConstant.username);
							return true;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public boolean isLogin(Context ctx) {
		this.mCtx = ctx;
		if (!NetUtils.isWifiConnected(ctx)) {
			UIHelper.showToast(ctx, "网络不给力");
			return false;
		}
		if (CHUANQIANGWANG.equals(ResourceConfig.getInstance(ctx).getDeviceName()) 
				&& Model.R300M.equals(ResourceConfig.getInstance(ctx).getDeviceMode())) {
			if (TextUtils.isEmpty(PisenConstant.sessionId) 
					|| TextUtils.isEmpty(PisenConstant.username)) {
				ctx.startActivity(new Intent(ctx, LoginActivity.class));
				return false;
			}
		}
		return true;
	}

	/**
	 * 获取连接 穿墙王.路由器 系统信息
	 */
	@Override
	public ZFirmwareInfo getFirmwareInfo() {
		ZFirmwareInfo zFirmwareInfo = null;
		String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, PisenConstant.getCQRouteSystemInfo());
		if (!TextUtils.isEmpty(result)) {
			try {
				if (isRouterNLogin(result)){
					return null;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String info = obj.getString("values");
					if (!TextUtils.isEmpty(info)) {
						zFirmwareInfo = GsonUtils.jsonDeserializer(info, ZFirmwareInfo.class);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return zFirmwareInfo;
	}

	
	/**
	 * @desc 获取穿墙王.在远程服务器上 镜像文件信息
	 */
	public CQRouterImageFile getRouterImageFile() {
		CQRouterImageFile imageFile = null;
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("moduleId", PisenConstant.Module_Action_query);
			map.put("sessionId", PisenConstant.sessionId);
			map.put("userName", PisenConstant.username);
			map.put("module", "autoUpgrade");
			map.put("action", "query");

			String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, map);
			LogCat.e("result = " + result);
			if (!TextUtils.isEmpty(result)) {
				if (isRouterNLogin(result)) {
					return null;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String info = obj.getString("values");
					if (info != null) {
						imageFile = GsonUtils.jsonDeserializer(info, CQRouterImageFile.class);
						ResourceConfig.getInstance(mCtx).setCQImageFile(imageFile);
						return imageFile;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return imageFile;

	}
	
	@Override
	public FirmwareData downloadFirmware(boolean isProgressQuery) {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("moduleId", PisenConstant.Module_Action_query);
			map.put("sessionId", PisenConstant.sessionId);
			map.put("userName", PisenConstant.username);
			map.put("module", "autoUpgrade");
			map.put("action", "download");

			String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, map);
			LogCat.e("downloadFirmware-->relsut:"+result);
			
			if (!TextUtils.isEmpty(result)) {
				if (isRouterNLogin(result)) {
					return null;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) { 
					return  GsonUtils.jsonDeserializer(obj.toString(), FirmwareData.class);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean executeUpgrade() {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("moduleId", PisenConstant.Module_Action_query);
			map.put("sessionId", PisenConstant.sessionId);
			map.put("userName", PisenConstant.username);
			map.put("module", "autoUpgrade");
			map.put("action", "upgrade");

			String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, map);
			if (!TextUtils.isEmpty(result)) {
				if (isRouterNLogin(result)) {
					return false;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) { 
					return  true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public CQUpdateProgress checkUpdateProgress(){
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("moduleId", PisenConstant.Module_Action_query);
			map.put("sessionId", PisenConstant.sessionId);
			map.put("userName", PisenConstant.username);
			map.put("module", "autoUpgrade");
			map.put("action", "progress");

			String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, map);
			LogCat.e("checkUpdateProgress-->result"+result);
			if (!TextUtils.isEmpty(result)) {
				if (isRouterNLogin(result)) {
					return null;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) { //下载成功
					return  GsonUtils.jsonDeserializer(obj.optString("values"), CQUpdateProgress.class);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * @desc 获取.查询中继是否成功   状态
	 */
	public String checkRelayStatus() {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("moduleId", PisenConstant.Module_Action_relay);
			map.put("sessionId", PisenConstant.sessionId);
			map.put("userName", PisenConstant.username);
			map.put("module", "relay");
			map.put("action", "getStatus");

			String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, map);
			if (!TextUtils.isEmpty(result)) {
				if (isRouterNLogin(result)) {
					return null;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("action");
					if (PisenConstant.Action_CQRouteRelayQuery.equals(action)) {
						return obj.getJSONObject("values").getString("status");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * @desc 获取.查询路由重启状态 
	 */
	public boolean getRouterRestarStatus() {
		try {
			Map<String, String> map = new HashMap<String, String>();
			map.put("moduleId", PisenConstant.Module_Action_reset);
			map.put("sessionId", PisenConstant.sessionId);
			map.put("userName", PisenConstant.username);
			map.put("module", "reset");
			map.put("action", "getStatus");

			String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, map);
			if (!TextUtils.isEmpty(result)) {
				if (isRouterNLogin(result)) {
					return false;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("action");
					if (PisenConstant.Action_CQRouteRestartStatus.equals(action)) {
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 获取有线拨号配置
	 */
	@Override
	public WanBean getDialConfig() {
		WanBean wan = null;
		try {
			String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, PisenConstant.getCQWiredDialParams());
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("action");
					if (PisenConstant.Action_CQPppoeAccountRead.equals(action)) {
						obj = obj.optJSONObject("values");
						if (obj != null) {
							wan = new WanBean();
							wan.setUsername(obj.optString("account"));
							wan.setPassword(obj.optString("password"));
							wan.setProto("pppoe");
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wan;
	}
	
	/**
	 * 读取 有线上网配置（wan口上网）
	 */
	@Override
	public WanBean getWanConfig() {
		WanBean wan = null;
		try {
			String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, PisenConstant.getCQWiredParams(Config.getCQRouterWiredProto()));
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("action");
					JSONObject jsValues = obj.getJSONObject("values");
					if (PisenConstant.Action_CQPppoeAccountRead.equals(action)) {
						if (jsValues != null) {
							wan = new WanBean();
							wan.setIpaddr(jsValues.getString("ip"));
							wan.setNetMask(jsValues.getString("netMask"));
							wan.setGateway(jsValues.getString("gateway"));
							wan.setDns1(jsValues.getString("firstDns"));
							wan.setDns2(jsValues.getString("secondDns"));
							wan.setProto(jsValues.getString("mode"));
							wan.setWired_ip(jsValues.getString("ip"));
							wan.setWired_submask(jsValues.getString("netMask"));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wan;
	}
	
	/**
	 * 读取有线上网配置
	 */
	@Override
	public WanBean getWiredConfig() {
		WanBean wan = null;
		try {
			String result = requestGet(HttpKeys.ROUTER_KINGWALL_URL, PisenConstant.getCQWiredParams("static"));
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("action");
					JSONObject jsValues = obj.getJSONObject("values");
					if (PisenConstant.Action_CQPppoeAccountRead.equals(action)) {
						if (jsValues != null) {
							wan = new WanBean();
							wan.setIpaddr(jsValues.getString("ip"));
							wan.setNetMask(jsValues.getString("netMask"));
							wan.setGateway(jsValues.getString("gateway"));
							wan.setDns1(jsValues.getString("firstDns"));
							wan.setDns2(jsValues.getString("secondDns"));
							wan.setProto(jsValues.getString("mode"));
							
							wan.setWired_ip(jsValues.getString("ip"));
							wan.setWired_submask(jsValues.getString("netMask"));
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wan;
	}
	
	
	
}
