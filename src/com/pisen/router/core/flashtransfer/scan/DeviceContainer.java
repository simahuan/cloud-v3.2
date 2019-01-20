package com.pisen.router.core.flashtransfer.scan;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.cybergarage.upnp.Icon;

import android.content.Context;
import android.studio.os.LogCat;
import android.studio.os.PreferencesUtils;
import android.util.Log;

import com.pisen.router.CloudApplication;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.core.flashtransfer.FlashTransferConfig;
import com.pisen.router.core.flashtransfer.FlashTransferManager;
import com.pisen.router.core.flashtransfer.scan.protocol.UserInfoPtlV2;
import com.pisen.router.ui.phone.settings.IconResource;

/**
 * 闪电互传设备容器
 * @author ldj
 * @version 1.0 2015年5月25日 下午4:43:39
 */
public class DeviceContainer {
	private static final String TAG = DeviceContainer.class.getSimpleName();
	private static final boolean DEBUG = false;
	
	private static Context context;
	private List<OnDeviceChangeListener> listeners;
	private static DeviceContainer instance;
	//当前所有在线用户
	private List<UserInfoPtlV2> userList;
	private Timer timer;
	//10s
	private static final long OFFLINE_TIMEOUT = 15 * 1000;
	private static final byte[] LOCK = new byte[0];

	private DeviceContainer() {
		init();
	}
	
	private void startTimeoutCheck() {
		stopTimeoutCheck();
		
		timer = new Timer(true);
		TimerTask task = new TimerTask() {
			
			@Override
			public void run() {
				checkTimeout();
			}
		};
		
		timer.schedule(task, 0, OFFLINE_TIMEOUT);
	}
	
	private void stopTimeoutCheck() {
		if(timer != null) {
			timer.cancel();
			timer = null;
		}
	}

	/**
	 * 检测在线数据是否超时
	 */
	protected void checkTimeout() {
		if(DEBUG) Log.e(TAG, "checkTimeout" );
		if(!userList.isEmpty()) {
			long validTime = System.currentTimeMillis() - OFFLINE_TIMEOUT;
			synchronized (LOCK) {
				List<UserInfoPtlV2> tmp = new ArrayList<UserInfoPtlV2>();
				for(UserInfoPtlV2 u : userList) {
					if(u.lastModified < validTime && !u.hostType.contains(FlashTransferConfig.PHONE_TYPE_IOS)) {//IOS设备不执行超时检查（兼容）
						tmp.add(u);
					}
				}
				if(!tmp.isEmpty()) {
					if(DEBUG) Log.e(TAG, "device timout, size->" + tmp.size());
					userList.removeAll(tmp);
					notifyChanged();
				}
				
			}
		}
	}


	public static DeviceContainer getInstance(Context context) {
		synchronized (LOCK) {
			if(instance == null) {
				DeviceContainer.context = context;
				instance = new DeviceContainer();
			}
		}
		
		return instance;
	}
	
	private void init() {
		listeners = new ArrayList<DeviceContainer.OnDeviceChangeListener>();
		userList = new ArrayList<UserInfoPtlV2>();
	}
	
	/**
	 * 注册设备变化监听
	 * @param listener
	 */
	public void registOnDeviceChangeListener(OnDeviceChangeListener listener) {
		if(!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	/**
	 * 注销设备变化监听
	 * @param listener
	 */
	public void unregistOnDeviceChangeListener(OnDeviceChangeListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * 新增设备
	 * @param device
	 */
	public boolean addDevice(UserInfoPtlV2 device) {
		if(DEBUG) Log.e(TAG, "addDevice" );
		boolean result = false;
		synchronized (LOCK) {
			UserInfoPtlV2 tmp = getDevice(device.hostIp);
			if(tmp == null) {
				result = userList.add(device);
				tmp = device;
			}
			tmp.lastModified = System.currentTimeMillis();
			if(result) {
				sendHeadIcon(device);
				notifyChanged();
			}
		}
		return result;
	}
	
	/**
	 * 发送头像给新发现用户
	 * @param device
	 */
	private void sendHeadIcon(UserInfoPtlV2 device) {
		int icon = -1;
		try {
			icon = PreferencesUtils.getInt(KeyUtils.NICK_HEAD, -1);
		} catch (Exception e) {
		}
		
		if(!IconResource.isOriginalIcon(icon)) {
			LogCat.d("sendHeadIcon");
			FlashTransferManager.startSendHeadIcon(getUrl(device.hostIp), new File(CloudApplication.HEAD_PATH, icon + ".png"));
		}
	}

	private String getUrl(String hostIp) {
		return String.format("http://%s:%s", hostIp, FlashTransferConfig.PORT_HTTP_RECV_HEAD);
	}

	/**
	 * 移除设备
	 * @param device
	 */
	public boolean removeDevice(UserInfoPtlV2 device) {
		boolean result = false;
		synchronized (LOCK) {
			UserInfoPtlV2 tmp = getDevice(device.hostIp);
			if(tmp != null) {
				result = userList.remove(device);
			}
		}
		if(result) {
			notifyChanged();
		}
		
		return result;
	}
	
	public void removeDevice(String ip) {
		synchronized (LOCK) {
			UserInfoPtlV2 tmp = getDevice(ip);
			if(tmp != null) {
				userList.remove(tmp);
				notifyChanged();
			}
		}
	}
	
	
	public UserInfoPtlV2 getDevice(String ip) {
		if(!userList.isEmpty()) {
			for(UserInfoPtlV2 u : userList) {
				if(ip.equals(u.hostIp)) {
					return u;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * 重置数据
	 */
	public void reset() {
		synchronized (LOCK) {
			userList.clear();
			notifyChanged();
		}
	}
	
	public synchronized void notifyChanged() {
		if(listeners != null ) {
			for(OnDeviceChangeListener l : listeners) {
				l.deviceChanged(userList);
			}
		}
		
		if(userList.isEmpty()) {
			if(DEBUG) Log.e(TAG, "userlist is empty, FlashTransferManager.release()!!! ");
			stopTimeoutCheck();
			FlashTransferManager.release(false);
		}else {
			if(timer == null) {
				startTimeoutCheck();
			}
			if(DEBUG) Log.e(TAG, "userlist is not empty, FlashTransferManager.startRecvService()!!! ");
			FlashTransferManager.startRecvService(context);
		}
	}

	
	
	public List<UserInfoPtlV2> getUserList() {
		return userList;
	}
	
	/**
	 * 设备变更监听
	 * @author ldj
	 * @version 1.0 2015年5月25日 下午4:44:29
	 */
	public interface OnDeviceChangeListener {
		void deviceChanged(List<UserInfoPtlV2> users);
	}

}
