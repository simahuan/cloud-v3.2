package com.pisen.router.ui.phone.device.bean;

import java.io.Serializable;

/**
 * 固件信息
 * @author Liuhc
 * @version 1.0 2015年7月3日09:37:14
 */
public class ZFirmwareInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * "distrib_id": "WefiOS", "distrib_name": "clover", "distrib_release":
	 * "1.0.2", "machine": "MT7620a zorlik evaluation board",
	 * "cur_version_name": "1.0.2", "new_version_name": "1.0.2",
	 * "new_firm_info": "固件描述信息", "service_version_name": "1.0.2"
	 * “service_firm_info”:”固件描述信息”
	 */
	String distrib_id;
	String distrib_name;
	String distrib_release;
	String machine;
	String cur_version_name;
	String new_version_name;
	String new_firm_info;
	String service_version_name;
	String service_firm_info;

	/** 穿墙王.firmwareInfo */
	String internetStatus;
	String staNum;
	String runTime;
	/** 穿墙王.特殊类型 */
	CQDev  dev;
	CQWifi wifi;
	
	public CQDev getDev() {
		return dev;
	}

	public void setDev(CQDev dev) {
		this.dev = dev;
	}

	public CQWifi getWifi() {
		return wifi;
	}

	public void setWifi(CQWifi wifi) {
		this.wifi = wifi;
	}

	public class CQDev implements Serializable {
		private static final long serialVersionUID = 1L;
		String model;
		String version;
		String gw;
		String mac;
		
		public String getModel() {
			return model;
		}
		public void setModel(String model) {
			this.model = model;
		}
		public String getVersion() {
			return version;
		}
		public void setVersion(String version) {
			this.version = version;
		}
		public String getGw() {
			return gw;
		}
		public void setGw(String gw) {
			this.gw = gw;
		}
		public String getMac() {
			return mac;
		}
		public void setMac(String mac) {
			this.mac = mac;
		}
	}

	public class CQWifi implements Serializable {
		private static final long serialVersionUID = 1L;
		String sw;
		String essid;
		String alarm;
		
		public String getSw() {
			return sw;
		}
		public void setSw(String sw) {
			this.sw = sw;
		}
		public String getEssid() {
			return essid;
		}
		public void setEssid(String essid) {
			this.essid = essid;
		}
		public String getAlarm() {
			return alarm;
		}
		public void setAlarm(String alarm) {
			this.alarm = alarm;
		}
	}
	
	public String getInternetStatus() {
		return internetStatus;
	}

	public void setInternetStatus(String internetStatus) {
		this.internetStatus = internetStatus;
	}

	public String getStaNum() {
		return staNum;
	}

	public void setStaNum(String staNum) {
		this.staNum = staNum;
	}

	public String getRunTime() {
		return runTime;
	}

	public void setRunTime(String runTime) {
		this.runTime = runTime;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getDistrib_id() {
		return distrib_id;
	}

	public void setDistrib_id(String distrib_id) {
		this.distrib_id = distrib_id;
	}

	public String getDistrib_name() {
		return distrib_name;
	}

	public void setDistrib_name(String distrib_name) {
		this.distrib_name = distrib_name;
	}

	public String getDistrib_release() {
		return distrib_release;
	}

	public void setDistrib_release(String distrib_release) {
		this.distrib_release = distrib_release;
	}

	public String getMachine() {
		return machine;
	}

	public void setMachine(String machine) {
		this.machine = machine;
	}

	public String getCur_version_name() {
		return cur_version_name;
	}

	public void setCur_version_name(String cur_version_name) {
		this.cur_version_name = cur_version_name;
	}

	public String getNew_version_name() {
		return new_version_name;
	}

	public void setNew_version_name(String new_version_name) {
		this.new_version_name = new_version_name;
	}

	public String getNew_firm_info() {
		return new_firm_info;
	}

	public void setNew_firm_info(String new_firm_info) {
		this.new_firm_info = new_firm_info;
	}

	public String getService_version_name() {
		return service_version_name;
	}

	public void setService_version_name(String service_version_name) {
		this.service_version_name = service_version_name;
	}

	public String getService_firm_info() {
		return service_firm_info;
	}

	public void setService_firm_info(String service_firm_info) {
		this.service_firm_info = service_firm_info;
	}
}
