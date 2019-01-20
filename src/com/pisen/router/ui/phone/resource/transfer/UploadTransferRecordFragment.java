package com.pisen.router.ui.phone.resource.transfer;

import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.pisen.router.core.filemanager.transfer.TransferCTag;
import com.pisen.router.core.filemanager.transfer.TransferDbHelper;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.core.filemanager.transfer.TransferManagerV2;
import com.pisen.router.core.filemanager.transfer.TransferServiceV2;
import com.pisen.router.core.filemanager.transfer.UploadSardineTask;
import com.pisen.router.ui.phone.resource.OnSelectedCountChangeListener;

/**
 * 上传纪录
 * @author ldj
 * @version 1.0 2015年5月4日 下午1:56:57
 */
public class UploadTransferRecordFragment extends TransferRecordFragment implements OnSelectedCountChangeListener {

	private TransferManagerV2 transferManager;
	private TransferRecordAdapter adapter;
	private TransferDbHelper dbHelper;
	private ServiceConnection conn;
	
	public UploadTransferRecordFragment() {
		super();
		setPageTitle("上传列表");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		init();
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	private void init() {
		adapter = new UpLoadTransferRecordAdapter(getActivity());
		adapter.setOnItemClickListener(this);
		adapter.setOnItemLongClickListener(this);
		adapter.setOnSelectedCountChangeListener(this);
		dbHelper = TransferDbHelper.getInstance(getActivity());
		setAdapter(adapter);
		bindService();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		unbindService();
	}
	
	private void bindService() {
		conn = new ServiceConnection() {
			
			@Override
			public void onServiceDisconnected(ComponentName name) {}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				transferManager = ((TransferServiceV2.TransferBinder)service).getTransferManager();
				adapter.setTransferManager(transferManager);
			}
		};
		Intent in = new Intent(getActivity(), TransferServiceV2.class);
		getActivity().bindService(in, conn, Service.BIND_AUTO_CREATE);
	}
	
	private void unbindService() {
		if(conn != null) {
			getActivity().unbindService(conn);
		}
		
		conn = null;
	}

	@Override
	public void refreshAdapterData() {
		adapter.setData(dbHelper.queryUnCompleteTransferTask(TransferCTag.Upload),  dbHelper.queryCompleted(TransferCTag.Upload));
		adapter.notifyDataSetChanged();
		handleEmptyView(isEmpty());
	}
	
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
		if(adapter != null && !adapter.isCheckEnabled()) {
			((TransferRecordActivity) getActivity()).showMultichoice();
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
					List<TransferInfo> data = adapter.getSelectedData();
					if(data != null) {
						int size = data.size();
						for(int i=0; i<size; i++) {
							transferManager.deletedTransfer(data.get(i)._id);
						}
					}
					adapter.getSelectedData().clear();
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

	@Override
	public String getProgressUpdateAction() {
		return UploadSardineTask.ACTION_PROGRESS;
	}

	@Override
	public boolean isEmpty() {
		if(adapter != null) {
			return adapter.data == null ? true : adapter.data.isEmpty();
		}
		return true;
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
		((TransferRecordActivity) getActivity()).updateActionBarChanged();
	}
	
}
