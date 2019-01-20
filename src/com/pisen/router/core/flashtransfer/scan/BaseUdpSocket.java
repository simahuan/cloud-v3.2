package com.pisen.router.core.flashtransfer.scan;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.pisen.router.BuildConfig;
import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.core.flashtransfer.WifiApManager;
import com.pisen.router.core.flashtransfer.scan.protocol.IpMessageConst;

/**
 * UDP协议收发基础类
 * 
 * @author ldj
 * @version 1.0 2015年3月31日 上午11:28:23
 */
public class BaseUdpSocket {
	private static final String TAG = BaseUdpSocket.class.getSimpleName();

	protected Context context;
	// 广播地址
	protected InetAddress broadcastAddr;
	// 广播监听端口
	protected int broadcastPort;
	protected int singlePort;
	protected String localIp;

	private MulticastSocket multicasetSocket;
	private UdpReceiveThread udpBroadcastReceiveThread;
	private OnDataReceivedListener broadcastDataListener;
	private DatagramSocket singleSocket;
	private UdpReceiveThread singleUdpReceiveThread;
	private OnDataReceivedListener singleDataListener;

	public BaseUdpSocket(Context ctx, InetAddress broadcastAddr, int broadcastPort) {
		this(ctx, broadcastAddr, broadcastPort, -1);
	}

	public BaseUdpSocket(Context ctx, InetAddress broadcastAddr, int broadcastPort, int singlePort) {
		this.context = ctx;
		this.broadcastAddr = broadcastAddr;
		this.broadcastPort = broadcastPort;
		this.singlePort = singlePort;
	}

	/**
	 * 初始化
	 * @throws NetworkErrorException 
	 */
	protected void initBroadcastSocket() throws NetworkErrorException {
		if (!isNetEnable()) {
			throw new NetworkErrorException("没有网络连接");
		}

		localIp = NetUtil.getLocalIpAddressString();
		if (localIp == null || "null".equalsIgnoreCase(localIp)) {
			throw new NullPointerException("获取本机ip为null");
		} else {
			// if(broadcastAddr != null && broadcastPort >0 ) {
			if (multicasetSocket != null) {
				releaseBroadcastSocket();
			}

			try {
				multicasetSocket = new MulticastSocket(broadcastPort);
				multicasetSocket.joinGroup(broadcastAddr);
				udpBroadcastReceiveThread = new UdpReceiveThread(multicasetSocket, broadcastDataListener);
				udpBroadcastReceiveThread.setDaemon(true);
				udpBroadcastReceiveThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			// }
		}
	}

	/**
	 * 广播通信资源释放
	 */
	protected void releaseBroadcastSocket() {
		if (udpBroadcastReceiveThread != null) {
			udpBroadcastReceiveThread.cancel();
			udpBroadcastReceiveThread = null;
		}

		if (multicasetSocket != null) {
			try {
				multicasetSocket.leaveGroup(broadcastAddr);
				multicasetSocket.close();
				multicasetSocket = null;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};

	/**
	 * 单播udp初始化
	 * @throws NetworkErrorException 
	 */
	protected void initSingleSocket() throws NetworkErrorException {
		if (!isNetEnable()) {
			throw new NetworkErrorException("没有网络连接");
		}


		if (singlePort > 0) {
			if (singleSocket != null) {
				releaseSingleSocket();
			}

			try {
				singleSocket = new DatagramSocket(singlePort);
				singleUdpReceiveThread = new UdpReceiveThread(singleSocket, singleDataListener);
				singleUdpReceiveThread.setDaemon(true);
				singleUdpReceiveThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 判断是否接入网络
	 * @return
	 */
	private boolean isNetEnable() {
		return isWifiEnable() || new WifiApManager(context).isWifiApEnabled();
	}

	/**
	 * 单播通信资源释放
	 */
	protected void releaseSingleSocket() {
		if (singleUdpReceiveThread != null) {
			singleUdpReceiveThread.cancel();
			singleUdpReceiveThread = null;
		}

		if (singleSocket != null) {
			singleSocket.close();
			singleSocket = null;
		}
	}

	/**
	 * 设置单播数据接收监听
	 * 
	 * @param listener
	 */
	public void setOnSingleDataReceivedListener(OnDataReceivedListener listener) {
		this.singleDataListener = listener;
	}

	/**
	 * 设置广播数据接收监听
	 * 
	 * @param listener
	 */
	public void setOnBroadcastDataReceivedListener(OnDataReceivedListener listener) {
		this.broadcastDataListener = listener;
	}

	/**
	 * 发送广播数据
	 * 
	 * @param data
	 * @throws IOException
	 */
	public void sendBroadcastMessage(DatagramPacket data) throws IOException {
		if (data != null && multicasetSocket != null) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "udp send broadcast -->" + new String(data.getData(), 0, data.getLength(), IpMessageConst.CHARSET));
			multicasetSocket.send(data);
		}
	};

	public void sendSingleMessage(DatagramPacket data) throws IOException {
		if (data != null && singleSocket != null) {
			if (BuildConfig.DEBUG)
				Log.d(TAG, "udp send single -->" + new String(data.getData(), 0, data.getLength(), IpMessageConst.CHARSET));
			singleSocket.send(data);
		}
	};

	/**
	 * udp数据接收回调监听
	 * 
	 * @author ldj
	 * @version 1.0 2015年3月31日 下午1:39:03
	 */
	public static interface OnDataReceivedListener {
		void receive(String msg);
	}

	/**
	 * 判断wifi是否可用
	 * 
	 * @return
	 */
	protected boolean isWifiEnable() {
		WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		return manager.isWifiEnabled();
	}

	/**
	 * 启用wifi
	 */
	protected void enableWifi() {
		WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		if (!manager.isWifiEnabled()) {
			manager.setWifiEnabled(true);
		}
	}

	/**
	 * udp数据接收线程
	 * 
	 * @author ldj
	 * @version 1.0 2015年3月30日 下午5:10:02
	 */
	public class UdpReceiveThread extends Thread {
		private DatagramSocket socket;
		private DatagramPacket dataPacketBuffer;
		private OnDataReceivedListener listener;
		// 接收数据缓存大小
		private static final int BUFFER_LENGTH = 1024;
		private boolean isRunning;

		public UdpReceiveThread(DatagramSocket socket, OnDataReceivedListener listener) {
			this.socket = socket;
			this.listener = listener;

			dataPacketBuffer = new DatagramPacket(new byte[BUFFER_LENGTH], BUFFER_LENGTH);
			isRunning = true;
		}

		/**
		 * 关闭数据接收
		 */
		public void cancel() {
			isRunning = false;
		}

		@Override
		public void run() {
			try {
				while (isRunning) {
					socket.receive(dataPacketBuffer);
					if (dataPacketBuffer.getLength() > 0) {
						// 过滤掉本机发送的广播消息
						if (!dataPacketBuffer.getAddress().toString().contains(localIp)) {
							if (BuildConfig.DEBUG)
								Log.d(TAG, "udp receive->" + new String(dataPacketBuffer.getData(), 0, dataPacketBuffer.getLength(), IpMessageConst.CHARSET));
							if (listener != null) {
								listener.receive(new String(dataPacketBuffer.getData(), 0, dataPacketBuffer.getLength(), IpMessageConst.CHARSET));
							}
						}
						dataPacketBuffer.setLength(BUFFER_LENGTH);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				dataPacketBuffer = null;
			}
		}
	}
}
