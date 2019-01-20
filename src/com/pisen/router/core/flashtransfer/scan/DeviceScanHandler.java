package com.pisen.router.core.flashtransfer.scan;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;
import android.os.Looper;
import android.studio.os.LogCat;

import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.core.flashtransfer.FlashTransferConfig;
import com.pisen.router.core.flashtransfer.scan.protocol.ConnectRequestReply;
import com.pisen.router.core.flashtransfer.scan.protocol.IpMessageConst;
import com.pisen.router.core.flashtransfer.scan.protocol.ProtocolContext;
import com.pisen.router.core.flashtransfer.scan.protocol.UserInfoPtlV2;

public class DeviceScanHandler implements DeviceScanCallback {
	private DeviceScan deviceScan;
	private Timer udpScanTimer;
	private OnScanResultCallback scanResultCallback;
	// private DeviceContainer container;

	private Handler mainThreadHandler;
	private boolean isRunning;
	private static final byte[] LOCK = new byte[0];
	private String localIpAddr;

	public DeviceScanHandler(final InetAddress broadcastAddr, int broadcastPort, int singlePort) {
		this.deviceScan = new DeviceScan(broadcastAddr, broadcastPort, singlePort);

		mainThreadHandler = new Handler(Looper.getMainLooper());
	}

	/**
	 * 扫描5秒/次
	 * 
	 * @throws Exception
	 */
	public void startTimerScan() {
		synchronized (LOCK) {
			try {
				localIpAddr = NetUtil.getLocalIpAddressString();
				isRunning = true;
				deviceScan.startScan(this);
				udpScanTimer = new Timer(DeviceScanHandler.class.getSimpleName());
				udpScanTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						noticeOnline();
					}
				}, 200, 5000);
			} catch (Exception e) {
				release();
				e.printStackTrace();
			}
		}
	}

	/**
	 * 资源是否
	 */
	private void release() {
		isRunning = false;
		stopScanTimer();
		deviceScan.stopScan();
		localIpAddr = null;
	}

	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * 停止扫描
	 */
	public void stopTimerScan() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (LOCK) {
					// 必须先停止通知在线
					stopScanTimer();
					// 进行下线提示
					noticeOffline();
					release();
				}

			}

		}).start();
	}

	private void stopScanTimer() {
		if (udpScanTimer != null) {
			udpScanTimer.cancel();
			udpScanTimer = null;
		}
	}

	/**
	 * 通知上线
	 */
	public void noticeOnline() {
		LogCat.d("DeviceScanHandler", "===noticeOnline===");
		UserInfoPtlV2 user = new UserInfoPtlV2(IpMessageConst.IPMSG_BR_ENTRY, FlashTransferConfig.PORT_UDP_BROADCAST, localIpAddr);
		try {
			deviceScan.sendBroadcastMessage(user);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 通知下线
	 */
	public void noticeOffline() {
		LogCat.d("noticeOffline", "===noticeOffline===");
		UserInfoPtlV2 user = new UserInfoPtlV2(IpMessageConst.IPMSG_BR_EXIT, FlashTransferConfig.PORT_UDP_BROADCAST, localIpAddr);
		try {
			//发送10次
			for(int i=0; i<10; i++) {
				deviceScan.sendBroadcastMessage(user);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 请求连接
	 */
	public void requestConnect(String targetIp) {
		UserInfoPtlV2 user = new UserInfoPtlV2(IpMessageConst.IPMSG_CONNECT_REQUEST, FlashTransferConfig.PORT_UDP_SINGLE_SEND, localIpAddr);
		try {
			deviceScan.sendMessage(user, targetIp);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// -void requestConnect(); //请求连接
	// -void disconnect(); //断开连接

	@Override
	public void messageReceived(ProtocolContext ctx) {
		switch (ctx.getCommand()) {
		case IpMessageConst.IPMSG_BR_ENTRY:
			replyOnline(ctx);
			break;
		case IpMessageConst.IPMSG_BR_ENTRY_REPLY:
			handleEnterReply(ctx);

			break;
		case IpMessageConst.IPMSG_BR_EXIT:
		case IpMessageConst.IPMSG_BR_EXIT_IPHONE:
			replyOffline(ctx);
			break;
		case IpMessageConst.IPMSG_CONNECT_REQUEST:
			handleConnectRequest(ctx);
			break;
		case IpMessageConst.IPMSG_CONNECT_REQUEST_REPLY:

			break;
		default:
			break;
		}
	}

	// 应答上线
	public void replyOnline(ProtocolContext ctx) {
		LogCat.d("DeviceScanHandler", "===replyOnline===");
		// 增加发现新设备效率
		handleEnterReply(ctx);

		UserInfoPtlV2 user = new UserInfoPtlV2(IpMessageConst.IPMSG_BR_ENTRY_REPLY, FlashTransferConfig.PORT_UDP_SINGLE_SEND, localIpAddr);
		try {
			deviceScan.sendMessage(user, ctx.getHostIp());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 应答下线
	public void replyOffline(ProtocolContext ctx) {
		LogCat.d("DeviceScanHandler", "===replyOffline===");
		final UserInfoPtlV2 user = new UserInfoPtlV2(ctx.getCommand(), 0, localIpAddr);
		user.read(ctx.getExtraData());
		// 过滤掉ios的1002下线消息（兼容ios）
		if (ctx.getCommand() != IpMessageConst.IPMSG_BR_EXIT_IPHONE && user.hostType.contains(FlashTransferConfig.PHONE_TYPE_IOS)) {
			return;
		}
		if (scanResultCallback != null) {
			mainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					scanResultCallback.offline(user);
				}
			});
		}
	}

	private void handleConnectRequest(ProtocolContext ctx) {
		LogCat.d("DeviceScanHandler", "===handleConnectRequest===");
		ConnectRequestReply user = new ConnectRequestReply();
		try {
			deviceScan.sendMessage(user, ctx.getHostIp());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 处理用户扫描结果应答(UserSend)
	 * 
	 * @param pro
	 */
	private void handleEnterReply(ProtocolContext ctx) {
		final UserInfoPtlV2 user = new UserInfoPtlV2(ctx.getCommand(), 0, localIpAddr);
		user.read(ctx.getExtraData());
		LogCat.d("DeviceScanHandler", "find user-->" + user.hostName);
		if (scanResultCallback != null) {
			mainThreadHandler.post(new Runnable() {
				@Override
				public void run() {
					scanResultCallback.online(user);
				}
			});
		}
	}

	/**
	 * 设置用户扫描结果回调接口(UserSend)
	 * 
	 * @param callback
	 */
	public void setOnScanResultCallback(OnScanResultCallback callback) {
		this.scanResultCallback = callback;
	}

	public interface OnScanResultCallback {
		// 用户上线
		void online(UserInfoPtlV2 user);

		// 用户下线
		void offline(UserInfoPtlV2 user);
	}
}
