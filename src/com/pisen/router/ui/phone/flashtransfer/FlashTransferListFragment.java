package com.pisen.router.ui.phone.flashtransfer;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.studio.os.LogCat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.ui.phone.resource.OnSelectedCountChangeListener;


/**
 * list型互传
 * @author ldj
 * @version 1.0 2015年7月23日 下午2:32:09
 */
public abstract class FlashTransferListFragment extends FlashTransferContentFragment implements OnSelectedCountChangeListener, OnItemClickListener, OnItemLongClickListener, OnClickListener{
	private View contentView;
	private View emptyView;
	private ListView listView;
	private TextView tipView;
	protected FlashTransferListAdapter adapter;
	private List<ResourceInfo> data;
	private DataChangedReceiver receiver;
	private boolean init;
	private View headerView;
	private TextView countView;
	private TextView selectView;
	
	public FlashTransferListFragment() {
		setTitle(getFragmentTitle());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		contentView = inflater.inflate(R.layout.flash_transfer_content_stickylist, container, false);
		init = true;
		findView();
		initView();
		return contentView;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		registReceiver();
		if(init){
			init = false;
			refreshContent(true);
		}else {
			refreshContent(false);
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver();
	}
	
	/**
	 * 获取对应fragment标题，用于标签显示
	 * @return
	 */
	public abstract String getFragmentTitle();
	
	/**
	 * 获取类别描述,需满足格式：共%1$s首音乐
	 * @return
	 */
	public abstract String getTypeDescription();
	
	/**
	 * 获取刷新视图action
	 * @return
	 */
	public abstract String getRefreshAction();
	
	public abstract List<ResourceInfo> getData();
	
	/**
	 * 注册刷新视图广播接收器
	 */
	private void registReceiver() {
		IntentFilter in = new IntentFilter(getRefreshAction());
		receiver = new DataChangedReceiver();
		getActivity().registerReceiver(receiver, in);
	}

	/**
	 * 注销广播接收器
	 */
	private void unregisterReceiver() {
		getActivity().unregisterReceiver(receiver);
	}
	
	private void findView() {
		headerView = contentView.findViewById(R.id.layout_header);
		countView = (TextView) contentView.findViewById(R.id.txtCount);
		selectView = (TextView) contentView.findViewById(R.id.txtselect);
		emptyView = contentView.findViewById(R.id.emptyLayout);
		listView = (ListView) contentView.findViewById(R.id.lst);
		
		tipView = (TextView) contentView.findViewById(R.id.txtTip);
	}

	private void initView() {
		adapter = new FlashTransferListAdapter(getActivity());
		listView.setAdapter(adapter);
		
		adapter.setOnSelectedCountChangeListener(this);
		adapter.setOnItemClickListener(this);
		adapter.setOnItemLongClickListener(this);
		selectView.setOnClickListener(this);
	}

	/**
	 * 扫描资源并刷新
	 * @param showLoading 是否显示loading弹出框
	 */
	private void refreshContent(final boolean showLoading) {
		LogCat.d("===refreshContent===");
		if(showLoading){
			listView.setVisibility(View.GONE);
			tipView.setVisibility(View.VISIBLE);
			tipView.setText("加载中...");
		}
		AsyncTaskUtils.execute(new InBackgroundCallback<List<ResourceInfo>>() {

			@Override
			public List<ResourceInfo> doInBackground() {
				return getData();
			}

			@Override
			public void onPostExecute(List<ResourceInfo> list) {
				if(showLoading) {
					listView.setVisibility(View.VISIBLE);
					tipView.setVisibility(View.GONE);
				}
				data = list;
				refreshListView();
			}
		});
	}

	/**
	 * 刷新界面
	 */
	private void refreshListView() {
		adapter.setData(data);
		adapter.notifyDataSetChanged();
		
		handleEmptyView();
	}
	
	private void handleEmptyView() {
		if(data == null || data.isEmpty()) {
			headerView.setVisibility(View.GONE);
			if(emptyView.getVisibility() != View.VISIBLE) {
				emptyView.setVisibility(View.VISIBLE);
			}
		}else {
			headerView.setVisibility(View.VISIBLE);
			countView.setText(String.format(getTypeDescription(), adapter.getData().size()));
			
			if(emptyView.getVisibility() != View.GONE) {
				emptyView.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void selectedCount(int count) {
		if(adapter.isCheckEnabled()) {//处于选中状态
			if(count >= data.size()) {
				selectView.setText("取消全选");
			}else {
				selectView.setText("全选");
			}
		}else {//没有处于选中状态
			selectView.setText("选择");
		}
	
		showBottomMenu(count);
	}

	@Override
	protected void startTransferSelectedResource() {
		if(adapter.getSelectedData().size()<=0) {
			Toast.makeText(getActivity(), "请选择数据", Toast.LENGTH_SHORT).show();
		}else {
			// 开始传送任务
			sendResource(adapter.getSelectedData());
			
			adapter.reset();
			selectView.setText("选择");
			dismissBottomMenu();
		}
	}
	
	private class DataChangedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			//刷新视图
			refreshContent(false);
		}
	}

	@Override
	protected void cancelTransferSelectedResource() {
		if(adapter != null) adapter.reset();
		selectView.setText("选择");
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.txtselect:
			if(!adapter.isCheckEnabled()) {//点击“选择”
				adapter.setCheckEnabled(true);
			}else {
				if(adapter.getSelectedData().size() >= data.size()) {//点击“取消全选”
					adapter.getSelectedData().clear();
				}else {//点击"全选"
					adapter.setSelectedData(new ArrayList<ResourceInfo>(data));
				}
			}
			
			adapter.notifyDataSetChanged();
			adapter.nofityCountChanged();
			break;

		default:
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ResourceInfo.doOpenFile(getActivity(), data.get(position), data);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		int size = adapter.getSelectedData().size();
		showBottomMenu(size);
		selectedCount(size);
		return true;
	}
}
