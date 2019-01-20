package com.pisen.router.ui.phone.flashtransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.transfer.TransferDbHelper;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.core.filemanager.transfer.TransferStatus;
import com.pisen.router.core.flashtransfer.FlashTransferConfig;
import com.pisen.router.ui.phone.resource.OnSelectedCountChangeListener;

/**
 * 收件箱
 * @author ldj
 * @version 1.0 2015年5月27日 下午4:19:21
 */
public class InboxFragment extends AbstractFlashTransferRecordFragment implements OnItemClickListener, OnSelectedCountChangeListener{

	private View emptyView;
	private ListView listView;
	private TransferDbHelper dbHelper;
	private BroadcastReceiver dataChangedReceiver;
	private AbstractFlashTransferRecordAdapter adapter;
	private List<TransferInfo> data;
	
	public InboxFragment() {
		super();
		setPageTitle("收件箱");
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
		contentView = inflater.inflate(R.layout.flash_transfer_inbox_fragment, container, false);
		findView();
		initView();
		return contentView;
	}
	
//	@Override
//	public void onViewCreated(View view, Bundle savedInstanceState) {
//		super.onViewCreated(view, savedInstanceState);
//	}
	
	
	private void findView() {
		emptyView = contentView.findViewById(R.id.emptyLayout);
		listView = (ListView) contentView.findViewById(R.id.lst);
	}
	
	private void initView() {
		dbHelper = TransferDbHelper.getInstance(getActivity());
		
		adapter = new InboxAdapter(getActivity());
		adapter.setMultiChoiceCountChangeListener(this);
		adapter.setOnItemLongClickListener(this);
		adapter.setOnItemClickListener(this);
		listView.setAdapter(adapter);
		
		setAdapter(adapter);
	}
	

	@Override
	public void refreshAdapterData() {
		data = dbHelper.queryFlashTransferInboxData();
		adapter.setData(data);
		adapter.notifyDataSetChanged();
		handleEmptyView();
	}
	
	private void handleEmptyView() {
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
					List<String> path = new ArrayList<String>();
					if(data != null && !data.isEmpty()) {
						int size = data.size();
						TransferInfo info = null;
						for(int i=0; i<size; i++) {
							info = data.get(i);
							path.add(String.format("%s/%s", info.storageDir, info.filename));
							deleteRecord(info);
							Log.e("scanFile", "delete. path->" +String.format("%s/%s", info.storageDir, info.filename) );
						}
						// 更新媒体库
						MediaScannerConnection.scanFile(getActivity(), (String[]) path.toArray(new String[path.size()]), null, null);
					}
					return true;
				} catch (Exception e) {
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
	
	private void deleteRecord(TransferInfo info) {
		info.inboxRecordDeleted = 1;
//		//更新数据库
		updateToDb(info);
		
		File tmp = new File(info.storageDir, info.filename);
		if(tmp.exists()) {
			tmp.delete();
		}else {
			Log.e("deleteRecord", "file not exist->" + tmp.getAbsolutePath());
		}
	}
	
	private void updateToDb(TransferInfo info) {
		if(dbHelper != null && info != null) {
			ContentValues values = new ContentValues();
//			values.put(TransferInfo.Table.filename, info.filename);
//			values.put(TransferInfo.Table.filesize, info.filesize);
//			values.put(TransferInfo.Table.storageDir, info.storageDir);
//			values.put(TransferInfo.Table.remoteHostName, info.remoteHostName);
//			values.put(TransferInfo.Table.remoteHostType, info.remoteHostType);
//			values.put(TransferInfo.Table.takeControl, info.takeControl.value); //不能更新该字段，会影响传输记录删除操作
//			values.put(TransferInfo.Table.currentBytes, info.currentBytes);
//			values.put(TransferInfo.Table.status, info.status.value);
//			values.put(TransferInfo.Table.lastUpdated, System.currentTimeMillis());
//			values.put(TransferInfo.Table.isDir, info.isDir);
//			values.put(TransferInfo.Table.ctag, info.ctag.value);
			values.put(TransferInfo.Table.inboxRecordDeleted, info.inboxRecordDeleted);
			
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
				if(((TransferStatus) intent.getSerializableExtra(FlashTransferConfig.EXTRA_TRANSFERINFO_STATUS))==  TransferStatus.SUCCESS) {
					refreshAdapterData();
				}
			}

		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(FlashTransferConfig.ACTION_TRANSFER_RECV_REFRESH);
		getActivity().registerReceiver(dataChangedReceiver, filter);
	}
	/**
	 * 取消注册进度更新广播接收器
	 */
	private void unRegistReceiver() {
		getActivity().unregisterReceiver(dataChangedReceiver);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ResourceInfo.doOpenFile(getActivity(), data.get(position).convertToResouceInfo());
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
}
