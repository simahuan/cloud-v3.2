package com.pisen.router.core.filemanager.transfer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * 传输服务
 * @author ldj
 * @version 1.0 2015年5月12日 上午11:44:45
 */
public class TransferServiceV2 extends Service {

	private TransferManagerV2 transferManager;
	private TransferBinder binder;
	
	@Override
	public void onCreate() {
		super.onCreate();
		transferManager = TransferManagerV2.getInstance(this);
		binder = new TransferBinder();
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}
	
	public class TransferBinder extends Binder {
		
		public TransferManagerV2 getTransferManager() {
			return transferManager;
		}
	}
}
