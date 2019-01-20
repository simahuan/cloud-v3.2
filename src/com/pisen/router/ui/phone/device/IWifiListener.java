package com.pisen.router.ui.phone.device;

import java.util.List;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;

public interface IWifiListener {
	
	public enum NetWorkType{
		scanning,connecting,connected,error;
	}
	public void onWifiListChanged(List<ScanResult> list);
	public void onConnecting(NetWorkType type);
	public void onConnected(WifiInfo info);
	public void onDisConnected();
}
