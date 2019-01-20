package com.pisen.router.ui.phone.flashtransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.support.v4.app.Fragment;

import com.pisen.router.R;
import com.pisen.router.common.dialog.LoadingDialog;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.transfer.TransferCTag;
import com.pisen.router.core.filemanager.transfer.TransferControl;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.core.filemanager.transfer.TransferProvider;
import com.pisen.router.core.filemanager.transfer.TransferStatus;
import com.pisen.router.core.flashtransfer.FlashTransferConfig;
import com.pisen.router.core.flashtransfer.FlashTransferManager;
import com.pisen.router.core.flashtransfer.FlashTransferManager.FlashTransferBinder;
import com.pisen.router.core.flashtransfer.scan.DeviceContainer;
import com.pisen.router.core.flashtransfer.scan.protocol.UserInfoPtlV2;
import com.pisen.router.ui.phone.flashtransfer.TransferToolbarPopupWindow.IFlashTransferControl;
import com.pisen.router.ui.phone.resource.OnSelectedCountChangeListener;

/**
 * 闪电互传内容fragment
 * @author ldj
 * @version 1.0 2015年5月18日 上午11:52:45
 */
public abstract class FlashTransferContentFragment extends Fragment implements IFlashTransferControl{

	protected String title;
	protected OnSelectedCountChangeListener listener;
	private TransferToolbarPopupWindow bottomToolbarPopupWindow;
	private FlashTransferManager transferManager;
	private ServiceConnection conn;
	private ContentResolver resolver;
	
	@Override
	public void onResume() {
		super.onResume();
		bindTransferService();
	}

	@Override
	public void onPause() {
		super.onPause();
		unbindTransferService();
	}
	
	/**
	 * 解绑传输服务
	 */
	private void bindTransferService() {
		Intent in = new Intent(getActivity(), FlashTransferManager.class);
		getActivity().startService(in);
		conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				FlashTransferBinder binder = (FlashTransferBinder) service;
				transferManager = binder.getFlashTransferManager();
			}
		};
		getActivity().bindService(in, conn, Service.BIND_AUTO_CREATE);
	}
	
	/**
	 * 绑定传输服务
	 */
	private void unbindTransferService() {
		getActivity().unbindService(conn);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	/**开始发送数据*/
	protected abstract void startTransferSelectedResource();
	/**取消数据发送*/
	protected abstract void cancelTransferSelectedResource();

	/**
	 * 设置选择数量变更监听
	 * @param listener
	 */
	public void setOnSelectedCountChangeListener(OnSelectedCountChangeListener listener) {
		this.listener = listener;
	}
	
	private LoadingDialog mLoadingDialog;
	protected void showLoading() {
		if(mLoadingDialog == null) {
			mLoadingDialog = new LoadingDialog(getActivity());
			mLoadingDialog.setTitle("请稍候...");
			mLoadingDialog.setCancelable(false);
		}
		
		mLoadingDialog.show();
	}

	protected void dismissLoading() {
		if(mLoadingDialog != null) {
			mLoadingDialog.dismiss();
		}
	}
	
	/**
	 * 显示底部操作菜单
	 */
	public void showBottomMenu(int count) {
		if(bottomToolbarPopupWindow == null) {
			bottomToolbarPopupWindow = new TransferToolbarPopupWindow(getActivity());
			bottomToolbarPopupWindow.setFlashTransferControl(this);
		}
		bottomToolbarPopupWindow.show(getActivity().findViewById(R.id.mainLayout), count);
	}
	
	public void dismissBottomMenu() {
		if(bottomToolbarPopupWindow != null) bottomToolbarPopupWindow.dismiss();
	}
	
	/**
	 * 发送数据
	 * @param data
	 */
	public void sendResource(List<ResourceInfo> data) {
		final List<ResourceInfo> tmp = new ArrayList<ResourceInfo>(data);
		showLoading();
		AsyncTaskUtils.execute(new InBackgroundCallback<Boolean>() {
			@Override
			public Boolean doInBackground() {
				try {
					if(tmp != null && !tmp.isEmpty() ) {
						List<UserInfoPtlV2> devices = new ArrayList<UserInfoPtlV2>(DeviceContainer.getInstance(getActivity().getApplicationContext()).getUserList());
						if(devices != null && !devices.isEmpty()) {
							for(UserInfoPtlV2 d: devices) {
								addSendTask(tmp, d);
							}
						}
					}
					return true;
				} catch (Exception e) {
					return false;
				}
			}

			@Override
			public void onPostExecute(Boolean result) {
				dismissLoading();
				getActivity().startActivity(new Intent(getActivity(), FlashTransferRecordActivity.class));
			}
		});
	}
	
	/**
	 * 开始传输任务
	 * @param data
	 */
	private void sendResource(TransferInfo data) {
		if(data != null ) {
			transferManager.startSendTask(String.format("http://%s:%s" ,data.storageDir, FlashTransferConfig.PORT_HTTP_RECV_FILE), data);
		}
	}

	@Override
	public void start() {
		//是否已有链接设备判断
		if(DeviceContainer.getInstance(getActivity().getApplicationContext()).getUserList().isEmpty()) {
			cancelTransferSelectedResource();
			
			Intent in = new Intent(getActivity(), ConnectManageActivity.class);
			startActivity(in);
			dismissBottomMenu();
		}else {
			startTransferSelectedResource();
		}
	}

	@Override
	public void cancle() {
		cancelTransferSelectedResource();
		dismissBottomMenu();
	}
	
	private void addSendTask(List<ResourceInfo> infos,  UserInfoPtlV2 device) {
		if(resolver == null) resolver = getActivity().getContentResolver();
		for (ResourceInfo iterator : infos) {
			if(iterator.size <=0) {
				/*修正大小，媒体库查询出来可能为0*/
				File f = new File(iterator.path);
				if(f.exists() && f.isFile()) {
					iterator.size = f.length();
				}
			}
			long id = insertToDatabase(iterator, device);
			//XXX 后期可考虑优化，减少数据库操作次数
			final Cursor cursor = resolver.query(TransferProvider.CONTENT_URI, null, TransferInfo.Table._ID + "=?", new String[] { String.valueOf(id) }, null);
			try {
				if (cursor.moveToNext()) {
					TransferInfo info = TransferInfo.newTransferInfo(cursor);
					sendResource(info);
				}
			} finally {
				cursor.close();
			}
		}
	}
	
	private long insertToDatabase(ResourceInfo filebean, UserInfoPtlV2 device) {
		if(resolver == null) resolver = getActivity().getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(TransferInfo.Table.storageDir, device.hostIp);
		cv.put(TransferInfo.Table.remoteHostName, device.hostName);
		cv.put(TransferInfo.Table.remoteHostType, device.hostType);
		cv.put(TransferInfo.Table.filename, filebean.name);
		cv.put(TransferInfo.Table.filesize, filebean.size);
		cv.put(TransferInfo.Table.ssid, "");
		cv.put(TransferInfo.Table.currentBytes, 0);
		cv.put(TransferInfo.Table.dataCreated, System.currentTimeMillis());
		cv.put(TransferInfo.Table.takeControl, TransferControl.START.value);
		cv.put(TransferInfo.Table.status, TransferStatus.PENDING.value);
		cv.put(TransferInfo.Table.ctag, TransferCTag.FlashSend.value);
		cv.put(TransferInfo.Table.url, filebean.path.substring(0, filebean.path.lastIndexOf("/")));
		cv.put(TransferInfo.Table.lastUpdated, System.currentTimeMillis());
		cv.put(TransferInfo.Table.isDir, 0);
		cv.put(TransferInfo.Table.inboxRecordDeleted, 0);
		return ContentUris.parseId(resolver.insert(TransferProvider.CONTENT_URI, cv));
	}
	
}
