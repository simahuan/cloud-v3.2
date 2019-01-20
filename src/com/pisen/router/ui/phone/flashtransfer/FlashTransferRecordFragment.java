package com.pisen.router.ui.phone.flashtransfer;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.transfer.TransferCTag;
import com.pisen.router.core.filemanager.transfer.TransferControl;
import com.pisen.router.core.filemanager.transfer.TransferDbHelper;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.core.filemanager.transfer.TransferStatus;
import com.pisen.router.core.flashtransfer.FlashTransferConfig;
import com.pisen.router.core.flashtransfer.FlashTransferManager;
import com.pisen.router.ui.phone.resource.OnSelectedCountChangeListener;
import com.sticky.listheaders.StickyListHeadersListView;

/**
 * 互传记录
 * @author ldj
 * @version 1.0 2015年5月4日 下午1:56:57
 */
public class FlashTransferRecordFragment extends AbstractFlashTransferRecordFragment implements OnSelectedCountChangeListener, OnItemClickListener{
	private static final String TAG = FlashTransferRecordFragment.class.getSimpleName();
	private static final boolean DEBUG = false;

	private AbstractFlashTransferRecordAdapter adapter;
	private TransferDbHelper dbHelper;
	private BroadcastReceiver dataChangedReceiver;
	private StickyListHeadersListView listView;
	private View emptyView;
	private List<TransferInfo> data;
	private static final byte[] LOCK = new byte[0];
	private Handler handler;

	public FlashTransferRecordFragment() {
		super();
		setPageTitle("互传记录");
	}

	@Override
	public void onResume() {
		refreshAdapterData();
		registReceiver();
		super.onResume();
	}

	@Override
	public void onPause() {
		unRegistReceiver();
		super.onPause();
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = inflater.inflate(R.layout.flash_transfer_record_fragment, container, false);
		findView();
		initView();
		return contentView;
	}

	private void findView() {
		emptyView = contentView.findViewById(R.id.emptyLayout);
		listView = (StickyListHeadersListView) contentView.findViewById(R.id.lst);
	}

	private void initView() {
		HandlerThread ht = new HandlerThread(TAG);
		ht.start();
		handler = new Handler(ht.getLooper());
		dbHelper = TransferDbHelper.getInstance(getActivity());

		adapter = new FlashTransferRecordAdapter(getActivity());
		adapter.setMultiChoiceCountChangeListener(this);
		adapter.setOnItemLongClickListener(this);
		adapter.setOnItemClickListener(this);
		listView.setAdapter(adapter);

		setAdapter(adapter);
	}

	@Override
	public void refreshAdapterData() {
		synchronized (LOCK) {
			data = dbHelper.queryFlashTransferTask();
			adapter.setData(data);
			adapter.notifyDataSetChanged();
			handleEmptyView();
		}
	}

	public void handleEmptyView() {
		if(data == null || data.isEmpty()) {
			if(emptyView.getVisibility() != View.VISIBLE) {
				emptyView.setVisibility(View.VISIBLE);
			}
		}else {
			if(emptyView.getVisibility() != View.GONE) {
				emptyView.setVisibility(View.GONE);
			}
		}
		((FlashTransferRecordActivity) getActivity()).handleEmptyView();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
		if(!adapter.multiChoiceMode) {
			((FlashTransferRecordActivity) getActivity()).showMultichoice();
		}
		return true;
	}
	
	@Override
	public void deleteSelectedData() {
		showLoading();
		AsyncTaskUtils.execute(new InBackgroundCallback<Boolean>() {
			@Override
			public Boolean doInBackground() {
				try {
					List<TransferInfo> data = adapter.selectedData;
					if(data != null && !data.isEmpty()) {
						int size = data.size();
						for(int i=0; i<size; i++) {
							deleteRecord(data.get(i));
						}
					}
					return true;
				} catch (Exception e) {
					e.printStackTrace();
					return false;
				}
			}

			@Override
			public void onPostExecute(Boolean result) {
				dismissMultiChoice();
				refreshAdapterData();
				dismissLoading();
			}
		});
	}

	/**
	 * 删除记录,只删记录（TransferControl标记为删除），不删文件
	 * @param info
	 */
	private void deleteRecord(TransferInfo info) {
		info.takeControl = TransferControl.DELETE;
		//更新数据库
		updateToDb(info);
		if(info.status == TransferStatus.PENDING || info.status== TransferStatus.RUNNING) {
			switch (info.ctag) {
			case FlashRecv:
				FlashTransferManager.removeRecvTask(info);
				break;
			case FlashSend:
				FlashTransferManager.removeSendTask(info);
				break;
			default:
				break;
			}
		}
	}

	private void updateToDb(TransferInfo info) {
		if(dbHelper != null && info != null) {
			ContentValues values = new ContentValues();
			values.put(TransferInfo.Table.filename, info.filename);
			values.put(TransferInfo.Table.filesize, info.filesize);
			values.put(TransferInfo.Table.storageDir, info.storageDir);
			values.put(TransferInfo.Table.remoteHostName, info.remoteHostName);
			values.put(TransferInfo.Table.remoteHostType, info.remoteHostType);
			values.put(TransferInfo.Table.takeControl, info.takeControl.value);
			values.put(TransferInfo.Table.currentBytes, info.currentBytes);
			values.put(TransferInfo.Table.status, info.status.value);
			values.put(TransferInfo.Table.lastUpdated, System.currentTimeMillis());
			values.put(TransferInfo.Table.isDir, info.isDir);
			values.put(TransferInfo.Table.ctag, info.ctag.value);
//			values.put(TransferInfo.Table.inboxRecordDeleted, info.inboxRecordDeleted); //不能更新该字段，会与收件箱冲突

			dbHelper.update(values, info._id);
		}
	}

	/**
	 * 注册进度更新广播接收器
	 */
	private void registReceiver() {
		dataChangedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				TransferInfo info  = null;
				if(FlashTransferConfig.ACTION_TRANSFER_SEND_REFRESH.equals(action)) {
					info = new TransferInfo(TransferCTag.FlashSend);
				}else if(FlashTransferConfig.ACTION_TRANSFER_RECV_REFRESH.equals(action)) {
					info = new TransferInfo(TransferCTag.FlashRecv);
				}
				info._id = intent.getLongExtra(FlashTransferConfig.EXTRA_TRANSFERINFO_ID, -1);

				TransferInfo tmp = getTransferInfoById(data, info._id);
				if(tmp == null) {//有新任务
					final long id = info._id;
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							final List<TransferInfo> tmpList = dbHelper.query(id);
							if(tmpList != null && !tmpList.isEmpty() && tmpList.get(0).takeControl != TransferControl.DELETE) {
								getActivity().runOnUiThread(new Runnable() {
									
									@Override
									public void run() {
										synchronized (LOCK) {
											if(getTransferInfoById(data, tmpList.get(0)._id) != null) return;
											data.add(0, tmpList.get(0));
											adapter.setData(data);
											adapter.notifyDataSetChanged();
											if(data.size() <=1) handleEmptyView();
										}
									}
								});
							}
						}
					});
				}else {
					if(tmp.status != TransferStatus.SUCCESS) {
						info.status = (TransferStatus) intent.getSerializableExtra(FlashTransferConfig.EXTRA_TRANSFERINFO_STATUS);
						info.filesize = intent.getLongExtra(FlashTransferConfig.EXTRA_TRANSFERINFO_FILESIZE, -1);
						info.currentBytes = intent.getLongExtra(FlashTransferConfig.EXTRA_TRANSFERINFO_CURBYTES, -1);
						refreshItemView(intent, info);
					}
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(FlashTransferConfig.ACTION_TRANSFER_RECV_REFRESH);
		filter.addAction(FlashTransferConfig.ACTION_TRANSFER_SEND_REFRESH);
		getActivity().registerReceiver(dataChangedReceiver, filter);
	}
	
	private void refreshItemView(Intent intent, TransferInfo info) {
		View convertView = contentView.findViewWithTag(info._id);
		adapter.refreshItemView(convertView, info);
	}

	/**
	 * 判断数据集中是否包含指定id数据
	 * @param data
	 * @param _id
	 * @return
	 */
	protected boolean contains(List<TransferInfo> data, long _id) {
		boolean result =false;
		if(data != null && !data.isEmpty()) {
			int size = data.size();
			for(int i=0; i<size; i++) {
				if(data.get(i)._id == _id) {
					result = true;
					break;
				}
			}
		}
		return result;
	}
	
	protected TransferInfo getTransferInfoById(List<TransferInfo> data, long _id) {
		TransferInfo result = null;
		synchronized (LOCK) {
			if(data != null && !data.isEmpty()) {
				int size = data.size();
				TransferInfo tmp = null;
				for(int i=0; i<size; i++) {
					tmp = data.get(i);
					if(tmp._id == _id) {
						result = tmp;
						break;
					}
				}
			}
		}
		return result;
	}

	/**
	 * 取消注册进度更新广播接收器
	 */
	private void unRegistReceiver() {
		getActivity().unregisterReceiver(dataChangedReceiver);
	}

	@Override
	public void onActionBarItemCheckCancel() {
		adapter.dismissMultiChoice();
	}

	@Override
	public void onActionBarCompleted() {

	}

	@Override
	public List<TransferInfo> getItemAll() {			
		return adapter.getData();
	}

	@Override
	public List<TransferInfo> getCheckedItemAll() {		
		return adapter.getSelectedData();
	}

	@Override
	public void onActionBarItemCheckAll(boolean checked) {
		if(checked) {
			adapter.selectAll();
		}else {
			adapter.cancelSelectAll();
		}
	}

	@Override
	public void selectedCount(int count) {
		((FlashTransferRecordActivity) getActivity()).updateActionBarChanged();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		TransferInfo info = data.get(position);
		if(info == null) return;
		
		if(info.ctag ==TransferCTag.FlashRecv) {
			if(info.status == TransferStatus.SUCCESS) {
				ResourceInfo.doOpenFile(getActivity(), data.get(position).convertToResouceInfo());
			}else {
				Toast.makeText(getActivity(), "文件未接收成功", Toast.LENGTH_SHORT).show();
			}
		}else if(info.ctag ==TransferCTag.FlashSend) {
			ResourceInfo.doOpenFile(getActivity(), data.get(position).convertToResouceInfo());
		}
	}
}
