package com.pisen.router.common.utils;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.text.format.Formatter;

public class NetUtil {

	/**
	 * 获取本机ip地址
	 * 
	 * @return 本机ip地址
//	 */
//	public static String getLocalIpAddress() {
//		String ip = null;
//		try {
//			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
//				NetworkInterface intf = en.nextElement();
//				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
//					InetAddress inetAddress = enumIpAddr.nextElement();
//					if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress()) 
//							&& (intf.getDisplayName().contains("wlan0") || intf.getDisplayName().contains("eth0") || intf
//									.getDisplayName().contains("ap0"))) {
//						ip= inetAddress.getHostAddress().toString();
////						if(!ip.startsWith("10")) return ip;
//					}
//				}
//			}
//		} catch (Exception e) {
//		}
//		return ip;
//	}
	
	public static InetAddress getLocalIpAddress() {
		InetAddress inetAddress = null;
		InetAddress myAddr = null;

		try {
			for (Enumeration<NetworkInterface> networkInterface = NetworkInterface.getNetworkInterfaces(); networkInterface.hasMoreElements();) {

				NetworkInterface singleInterface = networkInterface.nextElement();

				for (Enumeration<InetAddress> IpAddresses = singleInterface.getInetAddresses(); IpAddresses.hasMoreElements();) {
					inetAddress = IpAddresses.nextElement();

					if (!inetAddress.isLoopbackAddress()
							&& (singleInterface.getDisplayName().contains("wlan0") || singleInterface.getDisplayName().contains("wlan1") || singleInterface.getDisplayName().contains("eth0") || singleInterface
									.getDisplayName().contains("ap0"))) {

						myAddr = inetAddress;
					}
				}
			}

		} catch (SocketException ex) {
			ex.printStackTrace();
		}
		return myAddr;
	}
	
	public static String getLocalIpAddressString() {
		InetAddress myAddr = getLocalIpAddress();
		return myAddr == null ? null : myAddr.getHostAddress();
	}

	public static InetAddress getBroadcast(InetAddress localInetAddress) {
		if(localInetAddress == null) return null;
		
		NetworkInterface temp;
		InetAddress iAddr = null;
		try {
			temp = NetworkInterface.getByInetAddress(localInetAddress);
			if(temp != null) {
				List<InterfaceAddress> addresses = temp.getInterfaceAddresses();
				if(addresses != null) {
					for (InterfaceAddress inetAddress : addresses) {
						iAddr = inetAddress.getBroadcast();
					}
					return iAddr;
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取服务ip地址
	 * 
	 * @param ctx
	 * @return
	 */
	public static String getGetwayIPAddress(Context ctx) {
		WifiManager wifi_service = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
		DhcpInfo dhcpInfo = wifi_service.getDhcpInfo();
		return Formatter.formatIpAddress(dhcpInfo.gateway);
	}

	/**
	 * 判断wifi是否已连接
	 * 
	 * @return wifi是否已连接
	 */
	public static boolean isWifiConnected(Context ctx) {
		boolean connected = false;
		ConnectivityManager manager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (info != null && info.isConnected()) {
			connected = true;
		}
		return connected;
	}

	/**
	 * 判断当前网络是否可用
	 */
	public static boolean isNetAvailable(Context context) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isAvailable();
	}

	/**
	 * 判断WIFI是否使用
	 */
	public static boolean isWIFIActivate(Context context) {
		return ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).isWifiEnabled();
	}

	// /**
	// * 修改WIFI状态
	// *
	// * @param status
	// * true为开启WIFI，false为关闭WIFI
	// */
	// public static void changeWIFIStatus(Context context, boolean status) {
	// ((WifiManager) context.getSystemService(Context.WIFI_SERVICE))
	// .setWifiEnabled(status);
	// }

	/**
	 * 检测网络是否可用
	 * 
	 * @return
	 */
	public static boolean isNetworkConnected(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	/**
	 * 判断ip格式
	 * 
	 * @param format
	 * @return
	 */
	public static boolean isIpAddress(String format) {
		if (TextUtils.isEmpty(format) || format.contains("0.0.0.0")) {
			return false;
		}
		Pattern pa = Pattern
				.compile("^(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])$");
		Matcher ma = pa.matcher(format);
		return ma.matches();
	}
	
	/**
	 * 判断密码格式
	 * @param format
	 * @return
	 */
	public static boolean isPasswordAvailable(String format) {
		if (TextUtils.isEmpty(format) || format.contains("0.0.0.0")) {
			return false;//^[_0-9a-zA-Z!^&*(@.)$#%+=|,\\];/:~?\\[-]{8,63}$
		}
		Pattern pa = Pattern.compile("^[_0-9a-zA-Z!^&*(@.)$#%+=|,\\];()<>{}/:\\\\~?\\[-]{8,63}$");
		Matcher ma = pa.matcher(format);
		return ma.matches();
	}
}
