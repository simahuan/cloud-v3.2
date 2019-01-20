package com.pisen.router.core.flashtransfer.scan;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.text.TextUtils;
import android.util.Log;

import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.config.WifiConfig;
import com.pisen.router.core.flashtransfer.FlashTransferConfig;
import com.pisen.router.core.flashtransfer.scan.protocol.IpMessageConst;
import com.pisen.router.core.flashtransfer.scan.protocol.ProtocolContext;
import com.pisen.router.core.flashtransfer.scan.protocol.UdpMsgProtocol;
import com.pisen.router.core.monitor.WifiMonitor;
import com.pisen.router.core.monitor.WifiMonitor.WifiStateCallback;

/**
 * UDP协议收发基础类
 * 
 * @author ldj
 * @version 1.0 2015年3月31日 上午11:28:23
 */
public class DeviceScan implements WifiStateCallback {
	private static final String TAG = DeviceScan.class.getSimpleName();
	private static final boolean DEBUG = true;
	// 广播地址
	protected InetAddress broadcastAddr;
	private DatagramSocket broadcastSocket;
	protected int multiPort;
	private DatagramSocket singleSocket;
	protected int singlePort;
	private UdpReceiveThread singleUdpReceiveThread;
	private UdpReceiveThread broadcastUdpReceiveThread;
	private DeviceScanCallback deviceScanCallback;

	private static ExecutorService executorService = Executors.newFixedThreadPool(2);
	private String localIpAddr;

//	public DeviceScan(InetAddress broadcastAddr, int broadcastPort) {
//		this(broadcastAddr, broadcastPort, broadcastPort + 1);
//	}

	public DeviceScan(InetAddress broadcastAddr, int broadcastPort, int singlePort) {
		this.broadcastAddr = broadcastAddr;
		this.multiPort = broadcastPort;
		this.singlePort = singlePort;
		WifiMonitor.getInstance().registerObserver(this);
	}

	/**
	 * 扫描5秒/次
	 * @throws Exception 
	 */
	public void startScan(DeviceScanCallback callback) throws Exception {
		this.deviceScanCallback = callback;
		initSocket();
	}

	/**
	 * 停止扫描
	 */
	public void stopScan() {
		localIpAddr = null;
		releaseSingleSocket();
		releaseBroadcastSocket();
	}
	
	/**
	 * 单播udp初始化
	 */
	private void initSocket() {
		releaseSingleSocket();
		try {
			broadcastSocket = new DatagramSocket(multiPort);
			broadcastUdpReceiveThread = new UdpReceiveThread(broadcastSocket);
			broadcastUdpReceiveThread.setDaemon(true);
			broadcastUdpReceiveThread.start();
			
			singleSocket = new DatagramSocket(singlePort);
			singleUdpReceiveThread = new UdpReceiveThread(singleSocket);
			singleUdpReceiveThread.setDaemon(true);
			singleUdpReceiveThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 单播通信资源释放
	 */
	private void releaseSingleSocket() {
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
	 * 是否广播资源
	 */
	private void releaseBroadcastSocket() {
		if (broadcastUdpReceiveThread != null) {
			broadcastUdpReceiveThread.cancel();
			broadcastUdpReceiveThread = null;
		}

		if (broadcastSocket != null) {
			broadcastSocket.close();
			broadcastSocket = null;
		}
	}
	public static void setThreadPool(ExecutorService asyncRunner) {
		executorService = asyncRunner;
	}

	/**
	 * 发送广播数据
	 * 
	 * @param data
	 * @throws IOException
	 */
	public void sendBroadcastMessage(UdpMsgProtocol msg) throws IOException {
		if(broadcastAddr == null) return;
		byte[] data = msg.getMsg().getBytes(IpMessageConst.CHARSET);
		if(DEBUG) Log.d(TAG, "send broadcast->" + msg.getMsg());
		DatagramPacket dataPacket = new DatagramPacket(data, data.length, broadcastAddr,FlashTransferConfig.PORT_UDP_BROADCAST);
		if(broadcastSocket != null) broadcastSocket.send(dataPacket);
	}

	public void sendMessage(UdpMsgProtocol msg, String targetIp) throws IOException {
		byte[] data = msg.getMsg().getBytes(IpMessageConst.CHARSET);
		if(DEBUG) Log.d(TAG, "send single->" + msg.getMsg());
		DatagramPacket dataPacket = new DatagramPacket(data, data.length, InetAddress.getByName(targetIp), singlePort);
		if(singleSocket != null) singleSocket.send(dataPacket);
	}

	/**
	 * 通知线程来完成消息处理
	 * 
	 * @param dataPack
	 * @param clientIp
	 */
	private void executeThread(final DatagramPacket dataPack, final String clientIp) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					String protocolString = new String(dataPack.getData(), 0, dataPack.getLength(), IpMessageConst.CHARSET);
					Log.d(TAG, "recv->" +protocolString );
					if(TextUtils.isEmpty(protocolString)|| TextUtils.isEmpty(localIpAddr)) return;
					ProtocolContext cmd = new ProtocolContext(clientIp, protocolString);
					handleReceive(cmd);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 处理接收的消息
	 * 
	 * @param cmd
	 * @throws IOException
	 */
	protected void handleReceive(ProtocolContext ctx) throws IOException {
		if (deviceScanCallback != null) {
			deviceScanCallback.messageReceived(ctx);
		}
	}

	/**
	 * udp数据接收线程
	 * 
	 * @author ldj
	 * @version 1.0 2015年3月30日 下午5:10:02
	 */
	private class UdpReceiveThread extends Thread {
		private DatagramSocket socket;
		// 接收数据缓存大小
		private static final int BUFFER_LENGTH = 1024;
		private boolean isCancelled;

		public UdpReceiveThread(DatagramSocket socket) {
			this.socket = socket;
			this.isCancelled = false;
		}

		/**
		 * 关闭数据接收
		 */
		public void cancel() {
			isCancelled = true;
		}

		@Override
		public void run() {
			while (!isCancelled) {
				try {
					byte[] buff = new byte[BUFFER_LENGTH];
					final DatagramPacket dataPack = new DatagramPacket(buff, buff.length);
					socket.receive(dataPack);
					if (dataPack.getLength() > 0) {
						// 过滤掉本机发送的广播消息
						final String clientIp = dataPack.getAddress().getHostAddress();
						if(TextUtils.isEmpty(localIpAddr)) {
							localIpAddr = NetUtil.getLocalIpAddressString();
						}
						if (!clientIp.equals(localIpAddr)) {
							executeThread(dataPack, clientIp);
						}
					}
				} catch (SocketException e) {
//					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onConnected(WifiConfig config) {
		localIpAddr = NetUtil.getLocalIpAddressString();
	}

	@Override
	public void onDisconnected(WifiConfig config) {
		localIpAddr = null;
	}

}
