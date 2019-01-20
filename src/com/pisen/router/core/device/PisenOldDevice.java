package com.pisen.router.core.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.studio.os.LogCat;
import android.studio.os.NetUtils;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonUtils;
import com.google.gson.reflect.TypeToken;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.config.Config;
import com.pisen.router.config.HttpKeys;
import com.pisen.router.config.ResourceConfig;
import com.pisen.router.core.monitor.entity.RouterConfig.Model;
import com.pisen.router.ui.phone.device.LoginActivity;
import com.pisen.router.ui.phone.device.PisenConstant;
import com.pisen.router.ui.phone.device.bean.CQRouterImageFile;
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
 * Pisen旧设备
 * @author Liuhc
 * @version 1.0 2015年5月25日 下午1:54:48
 */
public class PisenOldDevice extends AbstractDevice {

	@Override
	public ArrayList<WifiBean> getDeviceList(Context ctx) {
		ArrayList<WifiBean> result = new ArrayList<WifiBean>();
		//		WifiBean wifi = null;
		//		WifiConnectUtils wifiSearch = new WifiConnectUtils(ctx);
		//		if (wifiSearch.startScan()) {
		//			List<ScanResult> list = wifiSearch.getWifiList();
		//			for (ScanResult scanResult : list) {
		//				if (scanResult.BSSID.startsWith(WifiMonitor.PISEN_BSSID_PREFIX)) {
		//					wifi = new WifiBean();
		//					wifi.setSsid(scanResult.SSID);
		//					wifi.setEncryption(scanResult.capabilities);
		//					wifi.setSignal(scanResult.level+"");
		//					result.add(wifi);
		//				}
		//			}
		//			list.clear();
		//		}
		return result;
	}
	
	@Override
	public WifiBean getWifiConfig() {
		WifiBean wifi = null;
		try {
			String result = requestGet(HttpKeys.ROUTER_URL, PisenConstant.getWirelessParams());
			if (!TextUtils.isEmpty(result)) {
				if(result.equalsIgnoreCase("NLogin")) {
					if(callback != null) {
						callback.onLoginTimeout();
					}
					return wifi;
				}
				
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("actName");
					if (PisenConstant.Action_WireLesscfgRead.equals(action)) {
						String info = obj.getString("values");
						if (!TextUtils.isEmpty(info)) {
							wifi = GsonUtils.jsonDeserializer(info, WifiBean.class);
							wifi.setEncryption(wifi.getWireless_net_secruity());
							wifi.setKey(wifi.getWireless_net_passwd());
							wifi.setSsid(wifi.getWireless_net_name());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return wifi;
	}


	@Override
	public boolean setWifiConfig(WifiBean wifi) {
		try {
			wifi.setWireless_net_name(wifi.getSsid());
			wifi.setWireless_net_passwd(wifi.getKey());
			wifi.setWireless_net_secruity(wifi.getEncryption());

			JSONObject j3 = new JSONObject();
			j3.put("wireless_name", wifi.getWireless_net_name());
			j3.put("wireless_net_secruity", wifi.getEncryption());
			j3.put("wireless_net_passwd", wifi.getWireless_net_passwd());
			j3.put("wireless_hide_ssid", wifi.getHide_wifi());
			j3.put("wireless_channel", wifi.getChannel());
			j3.put("wireless_mode", wifi.getMode());
			j3.put("wireless_rate", wifi.getRate());
			j3.put("ap_separate", wifi.getAp_separate());
			j3.put("short_gi", wifi.getShortgi());
			j3.put("open_wmm", wifi.getOpen_wmm());
			j3.put("capacity", wifi.getCapacity());
			JSONObject j2 = new JSONObject();
			j2.put("sessionId", PisenConstant.sessionId);
			j2.put("username", PisenConstant.username);
			j2.put("actName", PisenConstant.Action_WireLesscfgSave);
			j2.put("reqNames", "saveWireLesscfg");
			j2.put("cfgvalues", j3);
			Map<String, String> map = new HashMap<String, String>();
			map.put("datas", j2.toString());

			String result = requestPost(HttpKeys.ROUTER_URL, map);
			if (!TextUtils.isEmpty(result)) {
				if ("true".equals(result)) {
					return true;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("actName");
					if (PisenConstant.Action_WireLesscfgSave.equals(action)) {
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

	@Override
	public RouterBean getRelayType() {
		RouterBean bean = null;
		try {
			//上网配置
			String result = requestGet(HttpKeys.ROUTER_URL, PisenConstant.getWiredLinkModeParams());
			if (!TextUtils.isEmpty(result)) {
				if(result.equalsIgnoreCase("NLogin")) {
					if(callback != null) {
						callback.onLoginTimeout();
					}
					return bean;
				}
				
				bean = new RouterBean();
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("actName");
					if (PisenConstant.Action_CurrentLinkModeRead.equals(action)) {
						String route = obj.optJSONObject("values").optString("curLinkMode");
						if ("repeat".equals(route)) {
							bean.type = InternetType.sta;
							result = requestGet(HttpKeys.ROUTER_URL, PisenConstant.getWirelessStatusParams());
							if (!TextUtils.isEmpty(result)) {
								obj = new JSONObject(result);
								if (obj.getBoolean("result")) {
									action = obj.getString("actName");
									if (PisenConstant.Action_ReadRepeatStatus.equals(action)) {
										String info = obj.optString("values");
										if (!TextUtils.isEmpty(info)) {
											StaBean sta = GsonUtils.jsonDeserializer(info, StaBean.class);
											if ("ON".equals(sta.getRelaySwitch())) {
												bean.state = InternetState.up;
											}else{
												bean.state = InternetState.down;
											}
										}
									}
								}
							}
						}else if ("wired".equals(route) || "pppoe".equals(route)) {
							bean.type = InternetType.wan;
							//有线上网配置
							result = requestGet(HttpKeys.ROUTER_URL, PisenConstant.getWiredParams());
							if (!TextUtils.isEmpty(result)) {
								obj = new JSONObject(result);
								if (obj.getBoolean("result")) {
									action = obj.getString("actName");
									if (PisenConstant.Action_WiredConfigRead.equals(action)) {
										String info = obj.optString("values");
										if (!TextUtils.isEmpty(info)) {
											WanBean wan = GsonUtils.jsonDeserializer(info, WanBean.class);
											if ("ON".equals(wan.getWiredStatus())) {
												bean.state = InternetState.up;
											}else{
												bean.state = InternetState.down;
											}
										}
									}
								}
							}
						}else {
							bean.type = InternetType.unknown;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bean;
	}

	@Override
	public RelayConfBean getRelayConfig() {
		RelayConfBean bean = new RelayConfBean();
		try {
			//有线上网配置
			String result = requestGet(HttpKeys.ROUTER_URL, PisenConstant.getWiredParams());
			if (!TextUtils.isEmpty(result)) {
				if(result.equalsIgnoreCase("NLogin")) {
					if(callback != null) {
						callback.onLoginTimeout();
					}
					return bean;
				}
				
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("actName");
					if (PisenConstant.Action_WiredConfigRead.equals(action)) {
						String info = obj.optString("values");
						if (!TextUtils.isEmpty(info)) {
							bean.setWan(GsonUtils.jsonDeserializer(info, WanBean.class));
							if ("ON".equals(bean.wan.getWiredStatus())) {
								//bean.wan.setNet_state("1");
								bean.wan.setPhysics_state("connect");
							}else{
								//bean.wan.setNet_state("0");
								bean.wan.setPhysics_state("disconnect");
							}

							bean.wan.setExist("yes");
							bean.wan.setIpaddr(bean.wan.getWired_ip());
							bean.wan.setNetmask(bean.wan.getWired_submask());
							bean.wan.setGateway(bean.wan.getWired_gateway());
							bean.wan.setDns1(bean.wan.getWired_first_dns());
							bean.wan.setDns2(bean.wan.getWired_spare_dns());

							if ("static".equals(bean.wan.getWiredMode())) {
								bean.wan.setProto("static");
							}else if ("dynamic".equals(bean.wan.getWiredMode()) || "none".equals(bean.wan.getWiredMode())) {
								bean.wan.setProto("dhcp");
							}else{
								bean.wan.setProto("pppoe");
								result = requestGet(HttpKeys.ROUTER_URL, PisenConstant.getWiredDialParams());
								if (!TextUtils.isEmpty(result)) {
									obj = new JSONObject(result);
									if (obj.getBoolean("result")) {
										action = obj.getString("actName");
										if (PisenConstant.Action_PppoeAccountRead.equals(action)) {
											obj = obj.optJSONObject("values");
											if (obj != null) {
												bean.wan.setUsername(obj.optString("username"));
												bean.wan.setPassword(obj.optString("password"));
											}
										}
									}
								}
							}
						}
					}
				}
			}

			//无线中继上网配置
			result = requestGet(HttpKeys.ROUTER_URL, PisenConstant.getWirelessStatusParams());
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("actName");
					if (PisenConstant.Action_ReadRepeatStatus.equals(action)) {
						String info = obj.optString("values");
						if (!TextUtils.isEmpty(info)) {
							bean.setSta(GsonUtils.jsonDeserializer(info, StaBean.class));
							if ("ON".equals(bean.sta.getRelaySwitch())) {
								bean.sta.setNet_state("1");
								//bean.sta.setPhysics_state("connect");
							}else{
								bean.sta.setNet_state("0");
								//bean.sta.setPhysics_state("disconnect");
							}

							bean.sta.setExist("yes");
							bean.sta.setSsid(bean.sta.getCurrentAp());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bean;
	}

	@Override
	public WifiBean getWirelessedConfig() {
		try {
			//查询当前中继
			String result = requestGet(HttpKeys.ROUTER_URL, PisenConstant.getWirelessStatusParams());
			if (!TextUtils.isEmpty(result)) {
				if(result.equalsIgnoreCase("NLogin")) {
					if(callback != null) {
						callback.onLoginTimeout();
					}
					return null;
				}
				
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("actName");
					if (PisenConstant.Action_ReadRepeatStatus.equals(action)) {
						String info = obj.optString("values");
						if (!TextUtils.isEmpty(info)) {
							StaBean sta = GsonUtils.jsonDeserializer(info, StaBean.class);
							WifiBean bean = new WifiBean();
							if ("ON".equals(sta.getRelaySwitch())) {
								bean.setConnnect(true);
							}else{
								bean.setConnnect(false);
							}

							bean.setSsid(sta.getCurrentAp());
							bean.setKey(sta.getApPassword());
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

	@Override
	public List<WifiBean> getRelayWifiList() {
		List<WifiBean> list = null;
		try {
			String result = requestGet(HttpKeys.ROUTER_URL, PisenConstant.getWirelessListNumsParams());
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("actName");
					if (PisenConstant.Action_LinkAp.equals(action)) {
						String sumAps = obj.getJSONObject("values").getString("sumAps");
						if (Integer.parseInt(sumAps) > 0) {
							list = new ArrayList<WifiBean>();
						}
					}
				}
			}

			if (list != null) {
				result = requestGet(HttpKeys.ROUTER_URL, PisenConstant.getWirelessListParams());
				if (!TextUtils.isEmpty(result)) {
					JSONObject obj = new JSONObject(result);
					if (obj.getBoolean("result")) {
						String action = obj.getString("actName");
						if (PisenConstant.Action_ReadValidAps.equals(action)) {
							String json = ((obj.getJSONObject("values")).getJSONObject("aps")).getString("validAps");
							if (!TextUtils.isEmpty(json)) {
								Gson gson = new Gson();
								List<PWifiInfo> pisenList = gson.fromJson(json, new TypeToken<List<PWifiInfo>>(){}.getType());
								if (pisenList != null) {
									WifiBean bean = null;
									for (PWifiInfo wifiInfo : pisenList) {
										bean = new WifiBean();
										bean.setSsid(wifiInfo.getEssid());
										bean.setEncryption(wifiInfo.getEncrypt());;
										bean.setKey(wifiInfo.getPasswd());
										bean.setRate(wifiInfo.getRate());
										String sn = wifiInfo.getSn();
										if (!TextUtils.isEmpty(sn)) {
											sn = sn.substring(0, sn.indexOf(":"));
											bean.setSignal(""+2*(Integer.parseInt(sn)));
										}
										list.add(bean);
									}
									pisenList.clear();
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public boolean setRelayWifiConfig(String ssid, String charset, String encryption, String key, String channel, String disabled) {
		try {

			String localCof = Config.getWifiConfig();
			if (TextUtils.isEmpty(localCof)) {
				WifiBean bean = getWifiConfig();
				if (bean != null) {
					Config.setWifiConfig(GsonUtils.jsonSerializer(bean));
				}
			}

			JSONObject j2 = new JSONObject();
			j2.put("repeatSwitch", "ON");
			j2.put("essid", ssid);
			j2.put("password", key);
			j2.put("encrypt", encryption);
			j2.put("encoding", "utf-8");
			JSONObject j = new JSONObject();
			j.put("sessionId", PisenConstant.sessionId);
			j.put("username", PisenConstant.username);
			j.put("actName", PisenConstant.Action_LinkAp);
			j.put("reqNames", "linkAp");
			j.put("cfgvalues", j2);
			Map<String, String> map = new HashMap<String, String>();
			map.put("datas", j.toString());

			String result = requestPost(HttpKeys.ROUTER_URL, map);
			if (!TextUtils.isEmpty(result)) {
				if ("true".equals(result)) {
					return true;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("actName");
					if (PisenConstant.Action_LinkAp.equals(action)) {
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
	public boolean setAutoAccess(String proto,String ipaddr,String netmask) {
		try {
			JSONObject j2 = new JSONObject();
			j2.put("wired_ip", ipaddr);
			j2.put("wired_subnetmask", netmask);
			JSONObject j = new JSONObject();
			j.put("sessionId", PisenConstant.sessionId);
			j.put("username", PisenConstant.username);
			j.put("actName", PisenConstant.Action_DynamicConfigSave);
			j.put("reqNames", "saveDynamicConfig");
			j.put("cfgvalues", j2);
			Map<String, String> map = new HashMap<String, String>();
			map.put("datas", j.toString());

			String result = requestPost(HttpKeys.ROUTER_URL, map);
			if (!TextUtils.isEmpty(result)) {
				if ("true".equals(result)) {
					return true;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("actName");
					if (PisenConstant.Action_DynamicConfigSave.equals(action)) {
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
			JSONObject j2 = new JSONObject();
			j2.put("account", username);
			j2.put("password", password);
			JSONObject j = new JSONObject();
			j.put("sessionId", PisenConstant.sessionId);
			j.put("username", PisenConstant.username);
			j.put("actName", PisenConstant.Action_PppoeAccountSave);
			j.put("reqNames", "savePppoeAccount");
			j.put("cfgvalues", j2.toString());
			Map<String, String> map = new HashMap<String, String>();
			map.put("datas", j.toString());

			String result = requestPost(HttpKeys.ROUTER_URL, map);
			if (!TextUtils.isEmpty(result)) {
				if ("true".equals(result)) {
					return true;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("actName");
					if (PisenConstant.Action_PppoeAccountSave.equals(action)) {
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
			JSONObject j2 = new JSONObject();
			j2.put("wired_ip", ipaddr);
			j2.put("wired_subnetmask", netmask);
			j2.put("wired_gateway", gateway);
			j2.put("wired_first_dns", dns1);
			if (TextUtils.isEmpty(dns2)) {
				dns2 = "NULL1";
			}
			j2.put("wired_spare_dns", dns2);
			JSONObject j = new JSONObject();
			j.put("sessionId", PisenConstant.sessionId);
			j.put("username", PisenConstant.username);
			j.put("actName", PisenConstant.Action_WiredConfigSave);
			j.put("reqNames", "SaveWiredConfig");
			j.put("cfgvalues", j2);
			Map<String, String> map = new HashMap<String, String>();
			map.put("datas", j.toString());

			String result = requestPost(HttpKeys.ROUTER_URL, map);
			if (!TextUtils.isEmpty(result)) {
				if ("true".equals(result)) {
					return true;
				}
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("actName");
					if (PisenConstant.Action_WiredConfigSave.equals(action)) {
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
	public boolean setFactoryReset() {
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("sessionId", PisenConstant.sessionId);
			jsonObject.put("username", PisenConstant.username);
			jsonObject.put("actName", PisenConstant.Action_ResetToDefaultAction);
			jsonObject.put("reqNames", "resetToDefaultAction");
			Map<String, String> map = new HashMap<String, String>();
			map.put("datas", jsonObject.toString());

			String result = requestGet(HttpKeys.ROUTER_URL, map);
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("actName");
					if (PisenConstant.Action_ResetToDefaultAction.equals(action)) {
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

	@Override
	public boolean reStartDevice() {
		try {
			JSONObject j2 = new JSONObject();
			j2.put("rebootabc", "reboot");
			JSONObject j = new JSONObject();
			j.put("sessionId", PisenConstant.sessionId);
			j.put("username", PisenConstant.username);
			j.put("actName", PisenConstant.Action_RouteReboot);
			j.put("reqNames", "routerReboot");
			j.put("cfgvalues", j2);
			Map<String, String> map = new HashMap<String, String>();
			map.put("datas", j.toString());

			String result = requestGet(HttpKeys.ROUTER_URL, map);
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("actName");
					if (PisenConstant.Action_RouteReboot.equals(action)) {
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
		if (!TextUtils.isEmpty(password)) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("password", password);
			map.put("actName", "login");
			JSONObject jsonObject = new JSONObject(map);
			map.clear();
			map.put("datas", jsonObject.toString());

			try {
				String result = requestGet(HttpKeys.ROUTER_URL, map);
				if (!TextUtils.isEmpty(result)) {
					JSONObject obj = new JSONObject(result);
					if (obj.getBoolean("result")) {
						//返回成功
						String action = obj.optString("actName");
						if (PisenConstant.Action_Login.equals(action)) {
							//登陆成功
							PisenConstant.sessionId = obj.optString("sessionId");
							PisenConstant.username = obj.optString("username");
							Config.setDeviceMgrPassword(password);
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
		if (!NetUtils.isWifiConnected(ctx)) {
			UIHelper.showToast(ctx, "网络不给力");
			return false;
		}
		if (Model.R300M.equals(ResourceConfig.getInstance(ctx).getDeviceMode())) {
			if (TextUtils.isEmpty(PisenConstant.sessionId) 
					|| TextUtils.isEmpty(PisenConstant.username)) {
				ctx.startActivity(new Intent(ctx, LoginActivity.class));
				return false;
			}
		}
		return true;
	}

	@Override
	public ZFirmwareInfo getFirmwareInfo() {
//		获取旧设备信息
		return null;
	}

	@Override
	public FirmwareData downloadFirmware(boolean isProgressQuery) {
		return null;
	}

	@Override
	public boolean executeUpgrade() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 *  获取宽带拔号上网配置
	 */
	@Override
	public WanBean getWanConfig() {
		WanBean wan = null;
		try {
			String result = requestGet(HttpKeys.ROUTER_URL, PisenConstant.getWiredDialParams());
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if (obj.getBoolean("result")) {
					String action = obj.getString("actName");
					if (PisenConstant.Action_PppoeAccountRead.equals(action)) {
						obj = obj.optJSONObject("values");
						if (obj != null) {
							wan = new WanBean();
							wan.setUsername(obj.optString("pppoeAccount"));
							wan.setPassword(obj.optString("pppoePassword"));
						}
					}
				}
			}
		} catch (Exception e) {
		}
		return wan;
	}

}
