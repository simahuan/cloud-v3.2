package com.pisen.router.core.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONObject;

import android.content.Context;
import android.studio.os.LogCat;
import android.studio.os.NetUtils;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonUtils;
import com.google.gson.reflect.TypeToken;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.config.Config;
import com.pisen.router.ui.phone.device.ChaseDramaConstant;
import com.pisen.router.ui.phone.device.bean.CQRouterImageFile;
import com.pisen.router.ui.phone.device.bean.FirmwareData;
import com.pisen.router.ui.phone.device.bean.RelayConfBean;
import com.pisen.router.ui.phone.device.bean.RouterBean;
import com.pisen.router.ui.phone.device.bean.StaBean;
import com.pisen.router.ui.phone.device.bean.WanBean;
import com.pisen.router.ui.phone.device.bean.WifiBean;
import com.pisen.router.ui.phone.device.bean.ZFirmwareInfo;
import com.pisen.router.ui.phone.device.bean.RouterBean.InternetState;
import com.pisen.router.ui.phone.device.bean.RouterBean.InternetType;

/**
 * 智享设备管理
 * 
 * @author Liuhc
 * @version 1.0 2015年5月25日 下午1:54:48
 */
public class ZhiXiangDevice extends AbstractDevice {

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
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_GET_WALN);
			String result = requestGet(urlAddress, null);
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
					String info = obj.optString("data");
					if (!TextUtils.isEmpty(info)) {
						wifi = GsonUtils.jsonDeserializer(info, WifiBean.class);
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
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_SET_WALN);

			JSONObject params = new JSONObject();
			params.put("ssid", wifi.getSsid());
			params.put("mode", wifi.getMode());
			params.put("encryption", wifi.getEncryption());
			params.put("key", wifi.getKey());

			String result = requestPost(urlAddress, params.toString());

			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
					urlAddress = getRequestURL(ChaseDramaConstant.Url_SET_WLAN_RELOAD);
					result = requestPost(urlAddress,"");
					if (!TextUtils.isEmpty(result)) {
						obj = new JSONObject(result);
						if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public RouterBean getRelayType(){
		RouterBean bean = null;
		try {
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_GET_WAN_STATUS);
			String result = requestGet(urlAddress, null);
			if (!TextUtils.isEmpty(result)) {
				bean = new RouterBean();
				JSONObject obj = new JSONObject(result);
				if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
					String state = obj.optJSONObject("data").optString("state");
					if ("up".equals(state)) {
						bean.state = InternetState.up;
					}else if ("down".equals(state)) {
						bean.state = InternetState.down;
					}else if ("unknown".equals(state)) {
						bean.state = InternetState.unknown;
					}
					
					String route = obj.optJSONObject("data").optString("route");
					if ("sta".equals(route)) {
						bean.type = InternetType.sta;
					}else if ("wan".equals(route)) {
						bean.type = InternetType.wan;
					}else if ("unknown".equals(route)) {
						bean.type = InternetType.unknown;
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
		RelayConfBean bean = null;
		try {
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_GET_ROUTER_STATUS);
			String result = requestGet(urlAddress, null);
			if (!TextUtils.isEmpty(result)) {
				bean = new RelayConfBean();
				JSONObject obj = new JSONObject(result);
				if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
					String info = obj.optString("data");
					if (!TextUtils.isEmpty(info)) {
						bean.setWan(GsonUtils.jsonDeserializer(obj.getJSONObject("data").optString("wan"), WanBean.class));
						bean.setSta(GsonUtils.jsonDeserializer(obj.getJSONObject("data").optString("sta"), StaBean.class));
//						if (bean.wan != null && "pppoe".equals(bean.wan.getProto())) {
//							urlAddress = getRequestURL(ChaseDramaConstant.Url_GET_ROUTER_STATUS);
//							result = requestGet(urlAddress, null);
//							if (!TextUtils.isEmpty(result)) {
//								obj = new JSONObject(result);
//								if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
//									obj = obj.optJSONObject("data");
//									if (obj != null) {
//										bean.wan.setUsername(obj.optString("username"));
//										bean.wan.setPassword(obj.optString("password"));
//									}	
//								}
//							}
//						}
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
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_GET_STA_STATUS);
			String result = requestGet(urlAddress, null);
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
					String json = obj.optString("data");
					if (!TextUtils.isEmpty(json)) {
						WifiBean wifi = GsonUtils.jsonDeserializer(json, WifiBean.class);
						if ("1".equals(wifi.getState())) {
							wifi.setConnnect(true);
						}else{
							wifi.setConnnect(false);
						}
						return wifi;
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
		List<WifiBean> list = new ArrayList<WifiBean>();
		try {
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_GET_STA_LIST);
			String result = requestGet(urlAddress, null);
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result); //typeMismath
				if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
					String json = obj.optString("data");
					if (!TextUtils.isEmpty(json)) {
						Gson gson = new Gson();
						list = gson.fromJson(json, new TypeToken<List<WifiBean>>(){}.getType());
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
			
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_SET_STA_CONFIG);
			JSONObject params = new JSONObject();
			params.put("ssid", ssid);
			params.put("charset", charset);
			params.put("channel", channel);
			params.put("encryption", encryption);
			params.put("key", key);
			params.put("disabled", disabled);

			String result = requestPost(urlAddress, params.toString());
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
					return true;
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
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_SET_ROUTER_STATUS);
			JSONObject params = new JSONObject();
			params.put("proto", proto);
			String result = requestPost(urlAddress, params.toString());
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
					return true;
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
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_SET_ROUTER_STATUS);
			JSONObject params = new JSONObject();
			params.put("proto", proto);
			params.put("password", password);
			params.put("username", username);
			String result = requestPost(urlAddress, params.toString());
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
					return true;
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
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_SET_ROUTER_STATUS);
			JSONObject params = new JSONObject();
			params.put("proto", proto);
			params.put("ipaddr", ipaddr);
			params.put("netmask", netmask);
			params.put("gateway", gateway);
			params.put("dns1", dns1);
			params.put("dns2", dns2);
			String result = requestPost(urlAddress, params.toString());
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
					return true;
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
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_SYS_FACTORY_RESET);
			String result = requestGet(urlAddress, null);
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean shutDownDevice() {
		//该功能已放弃
//		try {
//			String urlAddress = getRequestURL(ChaseDramaConstant.Url_SYS_SHUTDOWN);
//			String result = requestGet(urlAddress, null);
//			if (!TextUtils.isEmpty(result)) {
//				JSONObject obj = new JSONObject(result);
//				if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
//					return true;
//				}
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return false;
	}

	@Override
	public boolean reStartDevice() {
		try {
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_SYS_REBOOT);
			String result = requestGet(urlAddress, null);
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean login(String password) {
		// 智享设备无此功能
		return true;
	}

	@Override
	public boolean isLogin(Context ctx) {
		if (!NetUtils.isWifiConnected(ctx)) {
			UIHelper.showToast(ctx, "网络不给力");
			return false;
		}
		return true;
	}

	@Override
	public ZFirmwareInfo getFirmwareInfo() {
		ZFirmwareInfo info = null;
		try {
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_SYS_GET_DEVINFO);
			String result = requestGet(urlAddress, null);
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
					String data = obj.optString("data");
					if (!TextUtils.isEmpty(data)) {
						info = GsonUtils.jsonDeserializer(data, ZFirmwareInfo.class);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}

	@Override
	public FirmwareData downloadFirmware(boolean isProgressQuery) {
		FirmwareData info = null;
		try {
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_SYS_DOWN_FIRMWARE);
			HashMap<String, String> params = null;
			if(!isProgressQuery) {
				params = new HashMap<String, String>();
				params.put("operation", "start");
			}
			String result = requestGet(urlAddress, params);
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if ("Ok".equals(obj.getJSONObject("status").optString("errorcode"))) {
					String data = obj.optString("data");
					if (!TextUtils.isEmpty(data)) {
						info = GsonUtils.jsonDeserializer(data, FirmwareData.class);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}

	@Override
	public boolean executeUpgrade() {
		try {
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_SYS_UPGRADE);
			String result = requestGet(urlAddress, null);
			if (!TextUtils.isEmpty(result)) {
				JSONObject obj = new JSONObject(result);
				if ("PENDING".equals(obj.getJSONObject("status").optString("errorcode"))) {
					return true;
				}else{
					LogCat.e("升级固件出错：%s", obj.getJSONObject("status").optString("errorinfo"));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public WanBean getWanConfig() {
		WanBean bean = null;
		try {
			String urlAddress = getRequestURL(ChaseDramaConstant.Url_GET_WAN_CONFIG);
			String result = requestGet(urlAddress, null);
			if (!TextUtils.isEmpty(result)) {
				bean = new WanBean();
				JSONObject obj = new JSONObject(result);
				if ("OK".equalsIgnoreCase(obj.getJSONObject("status").optString("errorcode"))) {
					String info = obj.optString("data");
					if (!TextUtils.isEmpty(info)) {
						bean = GsonUtils.jsonDeserializer(info, WanBean.class);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bean;
	}
}
