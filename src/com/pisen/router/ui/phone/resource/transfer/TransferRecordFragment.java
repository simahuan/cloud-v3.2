package com.pisen.router.ui.phone.resource.transfer;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.dialog.LoadingDialog;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.transfer.TransferCTag;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.core.filemanager.transfer.TransferStatus;
import com.pisen.router.core.filemanager.transfer.TransferTask;
import com.pisen.router.ui.phone.resource.IMultiChoice;
import com.pisen.router.ui.phone.resource.v2.panel.ISelectionActionBar;
import com.sticky.listheaders.StickyListHeadersListView;

/**
 * 传输纪录基础fragment
 * @author ldj
 * @version 1.0 2015年5月4日 下午1:55:39
 */
public abstract class TransferRecordFragment extends Fragment implements IMultiChoice<TransferInfo>, OnItemLongClickListener , OnItemClickListener, ISelectionActionBar<TransferInfo>{
	
	private View contentView;
	private View emptyView;
	private View pathLayout;
	private TextView txtPath;
	private StickyListHeadersListView listView;
	private TransferRecordAdapter adapter;
	private BroadcastReceiver dataChangedReceiver;
	//fragment标题
	private CharSequence pageTitle;
	//上传、下载路径
	private CharSequence path;
	private LoadingDialog mLoadingDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = inflater.inflate(R.layout.transfer_record_fragment, container, false);
		findView();
		initView();
		return contentView;
	}
	
	
	@Override
	public void onResume() {
		super.onResume();
		
		registReceiver();
		refreshAdapterData();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unRegistReceiver();
	}
	
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
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		TransferInfo info = adapter.data.get(position);
		if(info.ctag == TransferCTag.Download && info.status != TransferStatus.SUCCESS) {
			UIHelper.showToast(getActivity(), "未下载完成");
		}else {
			ResourceInfo.doOpenFile(getActivity(), info.convertToResouceInfo());
		}
	}
	
	

	/**
	 * 设置adapter
	 * @param adapter
	 */
	public void setAdapter(TransferRecordAdapter adapter) {
		this.adapter = adapter;
	}
	
	public CharSequence getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(CharSequence title) {
		this.pageTitle = title;
	}
	
	public void setPath(CharSequence path) {
		this.path = path;
	}
	
	protected void handleEmptyView(boolean showEmpty) {
		if(showEmpty) {
			if(emptyView.getVisibility() != View.VISIBLE) {
				emptyView.setVisibility(View.VISIBLE);
			}
		}else {
			if(emptyView.getVisibility() != View.GONE) {
				emptyView.setVisibility(View.GONE);
			}
		}
		
		((TransferRecordActivity) getActivity()).handleEmptyView(showEmpty);
	}
	
	/**
	 * 删除已选择数据
	 */
	public abstract void deleteSelectedData() ;
	
	/**
	 * 重新设置adapter数据
	 */
	public abstract void refreshAdapterData();
	
	/**
	 * 是否无数据
	 * @return
	 */
	public abstract boolean isEmpty();
	
	/**
	 * 获取进度更新广播action
	 * @return
	 */
	public abstract String getProgressUpdateAction();
	

	private void findView() {
		emptyView = contentView.findViewById(R.id.emptyLayout);
		listView = (StickyListHeadersListView) contentView.findViewById(R.id.lst);
		pathLayout = contentView.findViewById(R.id.pathLayout);
		txtPath = (TextView) contentView.findViewById(R.id.txtdownloadpath);
	}
	
	private void initView() {
		listView.setAdapter(adapter);
//		listView.setOnItemLongClickListener(this);
//		listView.setOnItemClickListener(this);
		
		if(!TextUtils.isEmpty(path)) {
			txtPath.setText(path);
			pathLayout.setVisibility(View.VISIBLE);
		}else {
			pathLayout.setVisibility(View.GONE);
		}
		
//		refreshAdapterData();
	}
	
	/**
	 * 注册进度更新广播接收器
	 */
	private void registReceiver() {
		dataChangedReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				long id = intent.getLongExtra(TransferTask.EXTRA_TRANSFER_ID, -1);
				TransferInfo info = new TransferInfo(TransferCTag.Download);
				info._id = id;
				info.status = (TransferStatus) intent.getSerializableExtra(TransferTask.EXTRA_TRANSFER_STATUS);
				info.filesize = intent.getLongExtra(TransferTask.EXTRA_TOTAL_BYTES, -1);
				info.currentBytes = intent.getLongExtra(TransferTask.EXTRA_CURRENT_BYTES, -1);
				
				if(info.status == TransferStatus.SUCCESS) {//需要调整数据position
					refreshAdapterData();
				}else {
					View convertView = listView.findViewWithTag(info._id);
					if(convertView != null) {
						adapter.refreshItemView(convertView, info);
					}
				}

				info = null;
			}

		};
		IntentFilter filter = new IntentFilter(getProgressUpdateAction());
		getActivity().registerReceiver(dataChangedReceiver, filter);
	}
	/**
	 * 取消注册进度更新广播接收器
	 */
	private void unRegistReceiver() {
		getActivity().unregisterReceiver(dataChangedReceiver);
	}

	/**
	 * 显示Toast提示
	 */
	protected void showToastTips(String msg) {
		if(!TextUtils.isEmpty(msg)) {
			UIHelper.showToast(getActivity(), msg);
		}
	}
	
	@Override
	public void showMultiChoice() {
		adapter.showMultiChoice();
	}

	@Override
	public void dismissMultiChoice() {
		adapter.dismissMultiChoice();
	}
	
	@Override
	public void selectAll() {
		adapter.selectAll();
	}
	
	@Override
	public void cancelSelectAll() {
		adapter.cancelSelectAll();
	}

	@Override
	public int getSelectedCount() {
		return adapter.getSelectedCount();
	}

	@Override
	public List<TransferInfo> getSelectedData() {
		return adapter.getSelectedData();
	}
	
	public List<TransferInfo> getData() {
		return adapter.getData();
	}
}
