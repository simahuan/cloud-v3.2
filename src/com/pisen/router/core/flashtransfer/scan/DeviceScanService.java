package com.pisen.router.core.flashtransfer.scan;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.core.flashtransfer.FlashTransferConfig;
import com.pisen.router.core.flashtransfer.FlashTransferManager;
import com.pisen.router.core.flashtransfer.WifiApManager;
import com.pisen.router.core.flashtransfer.scan.DeviceScanHandler.OnScanResultCallback;
import com.pisen.router.core.flashtransfer.scan.protocol.UserInfoPtlV2;
import com.pisen.router.ui.phone.flashtransfer.FlashTransferNetUtil;

/**
 * 设备扫描服务
 * @author ldj
 * @version 1.0 2015年5月22日 上午9:14:47
 */
public class DeviceScanService extends Service implements OnScanResultCallback{
	private static final boolean DEBUG = false;
	private static final String TAG = DeviceScanService.class.getSimpleName();

	private DeviceScanBinder binder = new DeviceScanBinder();
	private DeviceScanHandler deviceScanHandler;
	private ConnectChangedReceiver receiver;
	private List<OnScanResultCallback> callbacks;
	private FlashTransferNetUtil netUtil;

	@Override
	public void onCreate() {
		super.onCreate();

		if(DEBUG) Log.d(TAG, "===onCreate===");
		init();
		registReceiver();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	private void registReceiver() {
		IntentFilter in = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		receiver = new ConnectChangedReceiver();
		registerReceiver(receiver, in);
	}

	private void unregisterReceiver() {
		if(receiver != null) {
			unregisterReceiver(receiver);
			receiver = null;
		}
	}

	@Override
	public void onDestroy() {
		if(DEBUG) Log.e(TAG, "===onDestroy===");
		releaseAllResouce();
		super.onDestroy();
	}

	public void releaseAllResouce() {
		releaseNetworkRelated();
		unregisterReceiver();
		if(netUtil.isWifiApEnabled()) {
			netUtil.closeAp();
		}
	}

	private void init() {
		netUtil = FlashTransferNetUtil.getInstance(getApplicationContext());
//		netUtil = new FlashTransferNetUtil(getApplicationContext());
		callbacks = new ArrayList<DeviceScanHandler.OnScanResultCallback>();
	}

	/**
	 * 初始化DeviceScanHandler
	 */
	private boolean initScanHandler() {
		if(NetUtil.isWifiConnected(getApplicationContext()) || netUtil.isWifiApEnabled()) {
			InetAddress broadcastAddr = NetUtil.getBroadcast(NetUtil.getLocalIpAddress());
			if(broadcastAddr != null) {
				deviceScanHandler = new DeviceScanHandler(broadcastAddr, FlashTransferConfig.PORT_UDP_BROADCAST, FlashTransferConfig.PORT_UDP_SINGLE_SEND);
				deviceScanHandler.setOnScanResultCallback(this);
				if(DEBUG) Log.d(TAG, "init called, deviceScanHandlered,broadcastAddr->" + broadcastAddr.getHostAddress());
				return true;
			}else {
				if(DEBUG) Log.e(TAG, "init called, but can not get broadcast addr!!");
				return false;
			}
		}else {
			if(DEBUG) Log.e(TAG, "init called, but wifi is not valided, skip init deviceScanHandler");
			return false;
		}
	}

	/**
	 * 扫描局域网设备
	 */
	public boolean scanDevice() {
		if(deviceScanHandler != null) {
			deviceScanHandler.stopTimerScan();
			noticeIOSOffline();
			deviceScanHandler = null;
		}
		boolean result = initScanHandler();
		if(result) {
			deviceScanHandler.startTimerScan();
		}

		return result;
	}

	public void stopScanDevice() {
		releaseNetworkRelated();
	}

	/**
	 * 释放网络相关资源
	 */
	private void releaseNetworkRelated() {
		noticeIOSOffline();
		// 无连接时停止扫描
		if (deviceScanHandler != null) {
			deviceScanHandler.stopTimerScan();
			deviceScanHandler = null;
		}

		FlashTransferManager.release(true);
		DeviceContainer.getInstance(getApplicationContext()).reset();
	}

	public void requestConnect(String targetIp) {
		if (deviceScanHandler != null && !TextUtils.isEmpty(targetIp)) {
			deviceScanHandler.requestConnect(targetIp);
		}
	}

	/**
	 * 通知ios设备下线
	 * XXX 
	 */
	private void noticeIOSOffline() {
		if(DEBUG) Log.d(TAG, "===noticeIOSOffline===");
		final List<UserInfoPtlV2> users = new ArrayList<UserInfoPtlV2>(DeviceContainer.getInstance(getApplicationContext()).getUserList());
		new Thread(new Runnable() {
			@Override
			public void run() {
				DefaultHttpClient client = new DefaultHttpClient(); 
				// 设置连接超时时间为5s
				HttpConnectionParams.setConnectionTimeout(client.getParams(), 5000); 
				HttpConnectionParams.setSoTimeout(client.getParams(),  10000);
				for(UserInfoPtlV2 u : users) {
					if(u.hostType.contains(FlashTransferConfig.PHONE_TYPE_IOS)) {
						HttpHead request = new HttpHead(String.format("http://%s:%s", u.hostIp, FlashTransferConfig.PORT_HTTP_RECV_FILE));
						request.setHeader("actionType", "stopServer");
						request.setHeader("Content-Length", "0");
						try {
							HttpResponse response = client.execute(request);
							StatusLine statusLine = response.getStatusLine();
							int statusCode = statusLine.getStatusCode();
							if(statusCode == HttpStatus.SC_OK) {
								if(DEBUG) Log.d(TAG, "success");
							}else {
								if(DEBUG) Log.d(TAG, "failed");
							}
							request.abort();
						} catch (ClientProtocolException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				client.getConnectionManager().shutdown();
			}
		}).start();

	}

	@Override
	public void online(UserInfoPtlV2 user) {
		DeviceContainer.getInstance(getApplicationContext()).addDevice(user);
		for(OnScanResultCallback s : callbacks) {
			s.online(user);
		}
	}

	@Override
	public void offline(UserInfoPtlV2 user) {
		DeviceContainer.getInstance(getApplicationContext()).removeDevice(user.hostIp);
		for(OnScanResultCallback s : callbacks) {
			s.offline(user);
		}
	}

	/**
	 * 设置用户扫描结果回调接口
	 * @param callback
	 */
	public void registOnScanResultCallback(OnScanResultCallback callback) {
		callbacks.add(callback);
	}

	public void unregistOnScanResultCallback(OnScanResultCallback callback) {
		callbacks.remove(callback);
	}


	public class DeviceScanBinder extends Binder {

		public DeviceScanService getService() {
			return DeviceScanService.this;
		}
	}

	private class ConnectChangedReceiver extends BroadcastReceiver {
		private ConnectivityManager cm;

		public ConnectChangedReceiver() {
			cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if (!new WifiApManager(getApplicationContext()).isWifiApEnabled() &&(ni == null ||  !ni.isConnectedOrConnecting())) {
				if(DEBUG) Log.d(TAG, "connect is disconnct!!");
				releaseNetworkRelated();
			} 
		}
	}
}
