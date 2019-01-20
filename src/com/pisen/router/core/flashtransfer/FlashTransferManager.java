package com.pisen.router.core.flashtransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.core.filemanager.transfer.TransferInfo;

/**
 * (闪电互传)
 * @author yangyp
 */
public class FlashTransferManager extends Service {
	private static final String TAG =  FlashTransferManager.class.getSimpleName();
	private static final boolean DEBUG = true;
	
	private static SendFlashTransfer sendFlashTransfer;
	private static RecvFlashTransfer recvFlashTransfer;
	private static HeadHttpReceiver headHttpReceiver;
	private static HeadHttpSender headIconSender;
	private FlashTransferBinder binder = new FlashTransferBinder();
	//所有正在进行中的发送任务
	private static List<TransferInfo> taskList;
	private WakeLock lock;
	
	/*任务完成*/
	public static final String ACTION_TRANSFER_COMPLETE_RECEIVE_IMAGE = "ft_complete_recv_img";
	public static final String ACTION_TRANSFER_COMPLETE_RECEIVE_MOVIE = "ft_complete_recv_movie";
	public static final String ACTION_TRANSFER_COMPLETE_RECEIVE_MUSIC = "ft_complete_recv_music";
	public static final String ACTION_TRANSFER_COMPLETE_RECEIVE_DOCUMENT = "ft_complete_recv_doc";
	public static final String ACTION_TRANSFER_COMPLETE_RECEIVE_APK = "ft_complete_recv_apk";
	public static final String ACTION_TRANSFER_COMPLETE_SEND = "flashtransfer_complete_send";
	
	public static final String ACTION_TRANSFER_RECEIVE_BEGIN = "flashtransfer_complete_recv_changed";
	public static final String ACTION_TRANSFER_SEND_BEGIN = "flashtransfer_complete_recv_changed";
	/*数量变更*/
	public static final String ACTION_TRANSFER_RECEIVE_CHANGED = "flashtransfer_complete_recv_changed";
	public static final String ACTION_TRANSFER_SEND_CHANGED = "flashtransfer_complete_send_changed";
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		init();
		acquireLock();
	}
	
	/**
	 * 设置电源管理策略
	 */
	private void acquireLock() {
		releaseLock();
		PowerManager pm = (PowerManager) getSystemService(Service.POWER_SERVICE);
		lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		lock.acquire();
	}
	
	/**
	 * 是否配置的电源管理策略
	 */
	private void releaseLock() {
		if(lock != null && lock.isHeld()) {
			lock.release();
		}
		lock = null;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
	
	@Override
	public void onDestroy() {
		releaseLock();
		
		super.onDestroy();
	}
	
	private void init() {
		taskList = new ArrayList<TransferInfo>();
	}
	
	/**
	 * 启动发送服务
	 */
	public static void startSendService(Context ctx) {
		if(NetUtil.isWifiConnected(ctx) || new WifiApManager(ctx).isWifiApEnabled()) {
			if(sendFlashTransfer == null) {
				sendFlashTransfer = SendFlashTransfer.getInstance(ctx);
			}
		}
	}
	
	/**
	 * 启动接收服务
	 */
	public static void startRecvService(Context ctx) {
		startHeadRecvService(ctx);
		if(recvFlashTransfer != null && recvFlashTransfer.isRunning()) {
			return;
		}
		
		if(NetUtil.isWifiConnected(ctx) || new WifiApManager(ctx).isWifiApEnabled()) {
			if(recvFlashTransfer == null) {
				recvFlashTransfer = RecvFlashTransfer.getInstance(ctx);
			}
			recvFlashTransfer.startRecvService();
		}
		
	}
	
	/**
	 * 启动头像接收服务
	 * @param ctx
	 */
	private static void startHeadRecvService(Context ctx) {
		if(headHttpReceiver != null && headHttpReceiver.isRunning()) {
			return;
		}
		
		if(NetUtil.isWifiConnected(ctx) || new WifiApManager(ctx).isWifiApEnabled()) {
			if(headHttpReceiver == null) {
				headHttpReceiver = HeadHttpReceiver.getInstance(ctx);
			}
			headHttpReceiver.startRecvService();
		}
	}
	
	/**
	 * 是否资源
	 * @param forceStopRecv	是否强制停止接收数据。用于传输大文件时，由于当网络阻塞时接收不到用户在线广播导致接收线程被终止
	 */
	public static void release(boolean forceStopRecv) {
		if(taskList != null)
		taskList.clear();
		
		Log.e("FlashTransferManager", "===stopRecvService===");
		if(forceStopRecv && recvFlashTransfer != null) {
			recvFlashTransfer.stopRecvService();
		}
		recvFlashTransfer = null;
		
		if(forceStopRecv && sendFlashTransfer != null) {
			sendFlashTransfer.removeAllTask();
		}
		sendFlashTransfer = null;
		
		if(forceStopRecv && headHttpReceiver != null) {
			headHttpReceiver.stopRecvService();
		}
		headHttpReceiver = null;
	}
	
	/**
	 * 开始单个发送任务
	 * @param url	接收地址
	 * @param info	发送的文件
	 */
	public void startSendTask(String url, TransferInfo info) {
		if(sendFlashTransfer != null) {
			sendFlashTransfer.sendFile(url, info);
			taskList.add(info);
		}else {
			Log.d("FlashTransferManager", "sendFlashTransfer is null!");
		}
	}
	
	/**
	 * 发送头像
	 * @param url
	 * @param file
	 */
	public static void startSendHeadIcon(String url, File file) {
		if(headIconSender == null) {
			headIconSender = HeadHttpSender.getInstance();
		}
		
		headIconSender.sendFile(url, file);
	}
	
	/**
	 * 开始批量发送任务
	 * @param url	接收地址
	 * @param infos	发送的文件集
	 */
	public synchronized void startSendTask(String url, TransferInfo... infos) {
		if(sendFlashTransfer != null) {
			sendFlashTransfer.sendFile(url, infos);
		}
	}
	
	/**
	 * 暂停发送任务（不支持断点续传）
	 * @param info
	 */
	public static void pauseSendTask(TransferInfo info) {
		if(sendFlashTransfer != null) {
			sendFlashTransfer.pauseTask(info);
		}
	}
	
	/**
	 * 删除发送任务
	 * @param info
	 */
	public static void removeSendTask(TransferInfo info) {
		if(sendFlashTransfer != null) {
			sendFlashTransfer.removeTask(info);
		}
	}
	
	/**
	 * 删除接收任务
	 * @param info
	 */
	public static void removeRecvTask(TransferInfo info) {
		if(recvFlashTransfer != null) {
			recvFlashTransfer.removeTask(info);
		}
	}
	
	public class FlashTransferBinder extends Binder {
		
		public FlashTransferManager getFlashTransferManager() {
			return FlashTransferManager.this;
		}
	}
}
