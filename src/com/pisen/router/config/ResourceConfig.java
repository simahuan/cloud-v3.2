package com.pisen.router.config;

import android.content.Context;
import android.studio.os.LogCat;
import android.text.TextUtils;

import com.pisen.router.core.monitor.entity.Return;
import com.pisen.router.core.monitor.entity.RouterConfig.Model;
import com.pisen.router.ui.phone.device.bean.CQRouterImageFile;
import com.pisen.router.ui.phone.device.bean.ZFirmwareInfo;

/**
 * 资源数据存储
 * 
 * @author Liuhc
 * @version 1.0 2015年6月2日 上午9:29:41
 */
public class ResourceConfig {

	public static ResourceConfig instance = null;
	private Context context;
	private String deviceName;
	private Model DeviceMode;
	private ZFirmwareInfo zFirmwareInfo;
	private CQRouterImageFile CQImageFile;
	

	// 路由信息
	private Return routerInfo = new Return();

	private ResourceConfig(Context ctx) {
		this.context = ctx;
	}

	public static ResourceConfig getInstance(Context ctx) {
		if (instance == null) {
			instance = new ResourceConfig(ctx);
		}
		return instance;
	}

	
	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public Model getDeviceMode() {
		return DeviceMode;
	}

	public void setDeviceMode(Model deviceMode) {
		DeviceMode = deviceMode;
	}

	public void setFirmwareInfo(ZFirmwareInfo firmwareInfo){
		this.zFirmwareInfo = firmwareInfo;
	}
	
	public ZFirmwareInfo getFirmwareInfo(){
		return zFirmwareInfo;
	}
	
	public CQRouterImageFile getCQImageFile() {
		return CQImageFile;
	}
	
	public void setCQImageFile(CQRouterImageFile cQImageFile) {
		this.CQImageFile = cQImageFile;
	}
	
	public Return getRouterInfo() {
		return routerInfo;
	}

	public void setRouterInfo(Return routerInfo) {
		this.routerInfo = routerInfo;
		if (routerInfo == null) {
			return;
		}
		String model = routerInfo.model;
		LogCat.e("model = "+model);
		if (!TextUtils.isEmpty(model)) {
			if ("WFR101N".equals(model)) {
				deviceName = "净·音·云路由";
				DeviceMode = Model.R300M;
			} else if ("WPR003N".equals(model)) {
				deviceName = "300M云路由(mini型)";
				DeviceMode = Model.R300M;
			} else if ("WMB001N".equals(model)) {
				deviceName = "音乐云盒";
				DeviceMode = Model.R300M;
			} else if ("WMP002N".equals(model)) {
				deviceName = "追剧·云路由";
				DeviceMode = Model.RZHIXIANG;
			} else if ("WPR001N".equals(model)) {
				deviceName = "150M云路由(mini型)";
				DeviceMode = Model.R150M;
			} else if ("WMN011N".equals(model)) {
				deviceName = "150M云盘(16GB)";
				DeviceMode = Model.R150M;
			} else if ("TS-D084".equals(model)) {
				deviceName = "路由式电霸";
				DeviceMode = Model.R300M;
			} else if ("WMM003N".equals(model)) {
				deviceName = "云座·易充";
				DeviceMode = Model.R300M;
			} else if ("WHR001N".equals(model)) {
				deviceName = "穿墙王·云路由";
				DeviceMode = Model.R300M;
			} else {
				deviceName = "品胜·云路由";
				DeviceMode = Model.R150M;
			}
		} else{
			deviceName = "品胜·云路由";
			DeviceMode = Model.R150M;
		}
	}
}
