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
import android.widget.AdapterView.OnItemLongClickListener;

import com.pisen.router.CloudApplication;
import com.pisen.router.R;
import com.pisen.router.core.filemanager.transfer.DownloadSardineTask;
import com.pisen.router.core.filemanager.transfer.TransferCTag;
import com.pisen.router.core.filemanager.transfer.TransferDbHelper;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.core.filemanager.transfer.TransferManagerV2;
import com.pisen.router.core.filemanager.transfer.TransferServiceV2;
import com.pisen.router.ui.phone.resource.OnSelectedCountChangeListener;

/**
 * 下载纪录
 * @author ldj
 * @version 1.0 2015年5月4日 下午1:57:49
 */
public class DownloadTransferRecordFragment extends TransferRecordFragment implements OnItemLongClickListener,OnSelectedCountChangeListener{
	private TransferManagerV2 transferManager;
	private TransferRecordAdapter adapter;
	private TransferDbHelper dbHelper;
	private ServiceConnection conn;
	
	public DownloadTransferRecordFragment() {
		super();
		setPageTitle("下载列表");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setPath(getResources().getString(R.string.transfer_download_path, CloudApplication.DOWNLOAD_PATH));
		init();
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		bindService();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unbindService();
	}
	
	private void init() {
		adapter = new DownLoadTransferRecordAdapter(getActivity());
		adapter.setOnItemClickListener(this);
		adapter.setOnItemLongClickListener(this);
		adapter.setOnSelectedCountChangeListener(this);
		dbHelper = TransferDbHelper.getInstance(getActivity());
		
		setAdapter(adapter);
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
		getActivity().unbindService(conn);
	}

	@Override
	public void refreshAdapterData() {
		adapter.setData(dbHelper.queryUnCompleteTransferTask(TransferCTag.Download),  dbHelper.queryCompleted(TransferCTag.Download));
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
		return DownloadSardineTask.ACTION_PROGRESS;
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
