package com.pisen.router.ui.phone.flashtransfer;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.studio.os.LogCat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.view.TimeAxisStickyGridHeadersGridView;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.flashtransfer.FlashTransferManager;
import com.pisen.router.ui.adapter.TimeAxisSectionAdapter;
import com.pisen.router.ui.phone.flashtransfer.LocalResourceScanner.OnScanCompleteListener;
import com.pisen.router.ui.phone.resource.OnSelectedCountChangeListener;

/**
 * 图片互传
 * 
 * @author ldj
 * @version 1.0 2015年5月18日 下午1:19:58
 */
public class PictureTransferFragment extends FlashTransferContentFragment implements OnSelectedCountChangeListener, OnItemClickListener,
		OnItemLongClickListener {
	private View contentView;
	private View emptyView;
	private TimeAxisStickyGridHeadersGridView gridView;
	private TextView tipView;
	private TimeAxisSectionAdapter adapter;
	private List<ResourceInfo> data;
	private MeidaChangedReceiver receiver;
	private boolean init;

	public PictureTransferFragment() {
		setTitle("图片");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LogCat.e("===onCreateView===");
		contentView = inflater.inflate(R.layout.flash_transfer_content_stickygrid, container, false);
		findView();
		initView();
		init = true;
		return contentView;
	}

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			scanLocalPicture(true);
		};
	};
	@Override
	public void onResume() {
		LogCat.d("onResume");
		super.onResume();
		registReceiver();
		if(init){
			init = false;
			handler.sendEmptyMessageDelayed(0, 200);
		}else {
			scanLocalPicture(false);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver();
	}

	/**
	 * 注册刷新视图广播接收器
	 */
	private void registReceiver() {
		IntentFilter in = new IntentFilter(FlashTransferManager.ACTION_TRANSFER_COMPLETE_RECEIVE_IMAGE);
		receiver = new MeidaChangedReceiver();
		getActivity().registerReceiver(receiver, in);
	}

	/**
	 * 注销广播接收器
	 */
	private void unregisterReceiver() {
		getActivity().unregisterReceiver(receiver);
	}

	private void findView() {
		emptyView = contentView.findViewById(R.id.emptyLayout);
		gridView = (TimeAxisStickyGridHeadersGridView) contentView.findViewById(R.id.grd);
		gridView.setHeadersIgnorePadding(true);
		gridView.setAreHeadersSticky(false);
		
		tipView = (TextView) contentView.findViewById(R.id.txtTip);
	}

	private void initView() {
		adapter = new PictureTransferAdapter(getActivity());
		gridView.setAdapter(adapter);
		gridView.setNumColumns(adapter.getNumCloumns());

		adapter.setOnSelectedCountChangeListener(this);
		adapter.setOnItemClickListener(this);
		adapter.setOnItemLongClickListener(this);
	}

	/**
	 * 扫描本地图片,并刷新gridview
	 */
	private void scanLocalPicture(final boolean showLoading) {
		LogCat.d("===scanLocalPicture===");
		if(showLoading){
			gridView.setVisibility(View.GONE);
			tipView.setVisibility(View.VISIBLE);
			tipView.setText("图片加载中...");
		}
		LocalPictureScanner scanner = new LocalPictureScanner();
		scanner.setOnScanCompleteListener(new OnScanCompleteListener<ResourceInfo>() {

			@Override
			public void complete(final List<ResourceInfo> list) {
				getActivity().runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						if(showLoading) {
							gridView.setVisibility(View.VISIBLE);
							tipView.setVisibility(View.GONE);
						}
						data = list;
						refreshGridView();
					}
				});
			}
		});
		scanner.startScan(getActivity());

	}

	/**
	 * 刷新界面
	 */
	private void refreshGridView() {
		adapter.setData(data);
		adapter.notifyDataSetChanged();
		handleEmptyView();
	}

	private void handleEmptyView() {
		if (data == null || data.isEmpty()) {
			if (emptyView.getVisibility() != View.VISIBLE) {
				emptyView.setVisibility(View.VISIBLE);
			}
		} else {
			if (emptyView.getVisibility() != View.GONE) {
				emptyView.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public void selectedCount(int count) {
		showBottomMenu(count);
	}

	@Override
	protected void startTransferSelectedResource() {
		if(adapter.getSelectedData().size()<=0) {
			Toast.makeText(getActivity(), "请选择数据", Toast.LENGTH_SHORT).show();
		}else {
			// 开始传送任务
			sendResource(adapter.getSelectedData());
			adapter.clearSelect();
			
			dismissBottomMenu();
		}
	}

	private class MeidaChangedReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			scanLocalPicture(false);
		}

	}

	@Override
	protected void cancelTransferSelectedResource() {
		if (adapter != null)
			adapter.clearSelect();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		ResourceInfo.doOpenFile(getActivity(), data.get(position), data);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		showBottomMenu(adapter.getSelectedData().size());
		return true;
	}
}
