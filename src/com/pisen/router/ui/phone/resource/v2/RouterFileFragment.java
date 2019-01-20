package com.pisen.router.ui.phone.resource.v2;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.studio.os.AsyncTaskUtils.TaskContainer;
import android.studio.os.LogCat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.pisen.router.R;
import com.pisen.router.common.dialog.InputDialog;
import com.pisen.router.common.dialog.LoadingDialog;
import com.pisen.router.common.dialog.InputDialog.SimpleClickListener;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.SardineCacheResource;
import com.pisen.router.core.filemanager.SortComparator.FileSort;
import com.pisen.router.core.filemanager.transfer.TransferDbHelper;
import com.pisen.router.core.filemanager.transfer.TransferManagerV2;
import com.pisen.router.core.monitor.WifiMonitor;
import com.pisen.router.ui.HomeActivity;
import com.pisen.router.ui.phone.resource.ResourceFragment;
import com.pisen.router.ui.phone.resource.transfer.TransferRecordActivity;
import com.pisen.router.ui.phone.resource.v2.category.ResourceListAdapter;
import com.pisen.router.ui.phone.resource.v2.panel.ISelectionActionBar;

/**
 * 硬盘显示区
 * 
 * @author yangyp
 */
public class RouterFileFragment extends ResourceFragment implements View.OnClickListener, OnItemClickListener, OnItemLongClickListener,
		ISelectionActionBar<ResourceInfo> {

	private PullToRefreshListView listContent;
	private ResourceListAdapter resourceAdapter;
	private TextView msgToast;

	public IResource sardineManager;
	private RouterFragment routerFragment;
	public String parentPath;
	private FileSort sort = FileSort.NAME_ASC;

	TransferManagerV2 transManger;
	private TaskContainer taskContainer;

	private TransferDbHelper dbHelper;
	private ImageButton btnTransfer;
	
	private static final int REQUEST_VIEW_LIST = 0x3331;

	public RouterFileFragment() {
	}

	public RouterFileFragment(RouterFragment routerFragment, String parentPath) {
		this.routerFragment = routerFragment;
		this.parentPath = parentPath;
	}

	public String getParentPath() {
		return parentPath;
	}
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == REQUEST_VIEW_LIST) {
			if(resultCode == Activity.RESULT_OK && data != null && data.getBooleanExtra("refresh", false)) {
				refreshResourceData();
			}
		}
	}

	@Override
	public View onInjectCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.resource_home_file, container, false);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		if (taskContainer != null) {
			taskContainer.cancelRequest();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		checkTransferTaskStatus();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		initViews();

		sardineManager = new SardineCacheResource(null, null);
		WifiMonitor wifi = WifiMonitor.getInstance();
		if (wifi.isPisenWifiConnected()) {
			refreshResourceData();
		} else {
			showErrorView();
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initViews() {
		if (routerFragment == null) {
			return;
		}
		routerFragment.updateNavigationBarToDefault();
		findViewById(R.id.btnNewFolder).setOnClickListener(this);
		findViewById(R.id.btnSort).setOnClickListener(this);
		findViewById(R.id.btnSearch).setOnClickListener(this);
		findViewById(R.id.btnTransfer).setOnClickListener(this);
		btnTransfer = (ImageButton) findViewById(R.id.btnTransfer);

		listContent = (PullToRefreshListView) findViewById(R.id.listContent);
		listContent.setOnRefreshListener(new OnRefreshListener2() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				LogCat.e("onPullDownToRefresh...");
				loadResourceData();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {
				LogCat.e("onPullUpToRefresh...");
				loadResourceData();
			}
		});

		msgToast = (TextView) findViewById(R.id.msgToast);
		msgToast.setVisibility(View.GONE);
		resourceAdapter = new ResourceListAdapter(getActivity());
		resourceAdapter.setOnItemClickListener(this);
		resourceAdapter.setOnItemLongClickListener(this);
		listContent.setAdapter(resourceAdapter);
		listContent.setMode(Mode.PULL_DOWN_TO_REFRESH);
	}

	private void refreshResourceData() {
		showLoading();
		loadResourceData();
	}

	/**
	 * @describtion  加载路由数据
	 */
	private void loadResourceData() {
		taskContainer = AsyncTaskUtils.execute(new InBackgroundCallback<List<ResourceInfo>>() {
			boolean isExceptionThrow = false;
			@Override
			public List<ResourceInfo> doInBackground() {
				List<ResourceInfo> results = null;
				try {
					results = sardineManager.list(parentPath); // 未见异常,数据为空
					LogCat.e("results = " + results.size());
					sardineManager.sort(results, sort);
				} catch (Exception e) {
					isExceptionThrow = true;
				}
				return results;
			}

			@Override
			public void onPostExecute(List<ResourceInfo> result) {
				if (listContent != null) {
					listContent.onRefreshComplete();
				}
				
				hideLoading();
				
				if(isExceptionThrow && isAdded()) {
					UIHelper.showToast(getActivity(), "获取数据异常");
				} else {
					resourceAdapter.setData(result);
					if (result == null || result.isEmpty()) {
						showEmptyView();
					}
				}
			}
		});
	}

	/**
	 * 加载中
	 */
	private void showLoading() {
		// msgToast.setVisibility(View.VISIBLE);
		// msgToast.setText("读取中,请稍候...");
		getHomeActivity().showProgressDialog("读取中,请稍候...");
	}
	
	private void hideLoading() {
		if (msgToast != null) {
			msgToast.setVisibility(View.GONE);
		}
		getHomeActivity().dismissProgressDialog();
	}

	/**
	 * 未连接品胜Wifi或Wifi已关闭
	 */
	private void showErrorView() {
		if (msgToast != null) {
			msgToast.setVisibility(View.VISIBLE);
			msgToast.setText("未检测到您的路由设备~");
			msgToast.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.no_route, 0, 0);
		}
	}

	private void showEmptyView() {
		if (msgToast != null) {
			msgToast.setVisibility(View.VISIBLE);
			msgToast.setText("这里是空的哦,快来上传文件吧~");
			msgToast.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_no_file, 0, 0);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnNewFolder:
			newFolder();
			break;
		case R.id.btnSort:
			sortFile();
			break;
		case R.id.btnSearch:
			ShearchFragment.start(getActivity(), parentPath);
			break;
		case R.id.btnTransfer:
			startActivity(new Intent(getActivity(), TransferRecordActivity.class));
			break;
		default:
			break;
		}
	}

	public boolean checkTransferTaskStatus() {
		boolean isExist = false;
		if (btnTransfer != null) {
			if (dbHelper == null) {
				dbHelper = TransferDbHelper.getInstance(getActivity());
			}
			isExist = dbHelper.hasTransferTask();
			btnTransfer.setImageResource(isExist ? R.drawable.top_banner_transmission_new:R.drawable.tab_ic_transfer);
			dbHelper = null;
		}
		return isExist;
	}
	
	private LoadingDialog mLoadingDialog;
	protected void showLoading(String msg) {
		if(mLoadingDialog == null) {
			mLoadingDialog = new LoadingDialog(getActivity());
			mLoadingDialog.setTitle(msg);
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
	 * 判断文件名是否合法
	 * @param fileName
	 * @return
	 */
	private boolean isValidFileName(String fileName) {
		if (TextUtils.isEmpty(fileName)) {
			return false;
		}
		Pattern pa = Pattern.compile("^[^\\\\/:*?\"<>|]+$");
		Matcher ma = pa.matcher(fileName);
		return ma.matches();
	}

	/**
	 * 创建目录
	 */
	private void newFolder() {
		InputDialog createNewFolder = new InputDialog(getActivity());
		createNewFolder.setTitle("新建文件夹");
		createNewFolder.setOnClickListener(new SimpleClickListener() {
			@Override
			public void onOk(DialogInterface dialog, final String inputText) {
				if (TextUtils.isEmpty(inputText)) {
					UIHelper.showToast(getActivity(), "请输入文件夹名称");
					return;
				}
				
				if (!isValidFileName(inputText)) {
					UIHelper.showToast(getActivity(), "文件名不合法");
					return;
				}
				
				showLoading("创建中...");
				AsyncTaskUtils.execute(new InBackgroundCallback<Integer>() {
					@Override
					public Integer doInBackground() {
						try {
							if (sardineManager.exists(getParentPath() + inputText + File.separator)) {
								return -1;
							}
							sardineManager.createDir(parentPath + inputText);
							return 1;
						} catch (Exception e) {
							return 0;
						}
					}

					@Override
					public void onPostExecute(Integer result) {
						dismissLoading();
						if (result == 1) {
							refreshResourceData();
							UIHelper.showToast(getActivity(), "新建文件夹成功");
						} else if (result == -1) {
							UIHelper.showToast(getActivity(), "文件名已存在");
						} else {
							UIHelper.showToast(getActivity(), "文件夹创建失败");
						}
					}
				});
			}

			@Override
			public void onCancel(DialogInterface dialog) {
				super.onCancel(dialog);
				dialog.dismiss();
			}
		});
		createNewFolder.show();
	}

	private void sortFile() {
		sort = sort.nextShort();
		if (sort.equals(FileSort.NAME_ASC)) {
			UIHelper.showToast(getActivity(), "已按名称排序");
			((ImageButton) findViewById(R.id.btnSort)).setImageResource(R.drawable.tab_ic_sort_name);
		} else if (sort.equals(FileSort.DATA_DESC)) {
			UIHelper.showToast(getActivity(), "已按时间排序");
			((ImageButton) findViewById(R.id.btnSort)).setImageResource(R.drawable.tab_ic_sort_date);
		}

		AsyncTaskUtils.execute(new InBackgroundCallback<Boolean>() {
			@Override
			public Boolean doInBackground() {
				sardineManager.sort(resourceAdapter.getData(), sort);
				return true;
			}

			@Override
			public void onPostExecute(Boolean arg0) {
				resourceAdapter.notifyDataSetChanged();
			}
		});
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (resourceAdapter.isCheckedEnabled()) {
			resourceAdapter.toggleItemChecked(position);
			routerFragment.updateActionBarChanged();
		} else {
			ResourceInfo resource = resourceAdapter.getItem(position);
			if (resource.isDirectory) {
				routerFragment.startChildFragment(new RouterFileFragment(routerFragment, resource.path));
			} else {
				ResourceInfo.doOpenFileForResult(getActivity(), REQUEST_VIEW_LIST, resource, resourceAdapter.getData());
			}
		}
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		resourceAdapter.setCheckedEnabled(true);
		resourceAdapter.toggleItemChecked(position);
		showCheckedMenu();
		return false;
	}

	/**
	 * 显示选中菜单
	 */
	private void showCheckedMenu() {
		routerFragment.showSelectionMenu();
		setEnableNavigationBar(false);
		((HomeActivity)getActivity()).setSlidingMenuScrollable(false);
	}

	/**
	 * 取消选中菜单
	 */
	private void hideCheckedMenu() {
		resourceAdapter.setCheckedEnabled(false);
		routerFragment.hideSelectionMenu();
		setEnableNavigationBar(true);
		if (isAdded()){
			((HomeActivity)getActivity()).setSlidingMenuScrollable(true);
		}
	}

	/**
	 * 禁用或启用导航菜单
	 * 
	 * @param enabled
	 */
	private void setEnableNavigationBar(boolean enabled) {
		View v = findViewById(R.id.btnNewFolder);
		if (v != null) {
			ImageButton btn = (ImageButton) v;
			if (enabled) {
				btn.setEnabled(true);
				btn = (ImageButton) findViewById(R.id.btnSort);
				btn.setEnabled(true);
				btn = (ImageButton) findViewById(R.id.btnSearch);
				btn.setEnabled(true);
				btn = (ImageButton) findViewById(R.id.btnTransfer);
				btn.setEnabled(true);
			} else {
				btn.setEnabled(false);
				btn = (ImageButton) findViewById(R.id.btnSort);
				btn.setEnabled(false);
				btn = (ImageButton) findViewById(R.id.btnSearch);
				btn.setEnabled(false);
				btn = (ImageButton) findViewById(R.id.btnTransfer);
				btn.setEnabled(false);
			}
		}
	}

	@Override
	public void onActionBarCompleted() {
		hideCheckedMenu();
		refreshResourceData();
	}

	@Override
	public List<ResourceInfo> getItemAll() {
		return resourceAdapter.getData();
	}

	@Override
	public List<ResourceInfo> getCheckedItemAll() {
		return resourceAdapter.getItemCheckedAll();
	}

	@Override
	public void onActionBarItemCheckAll(boolean checked) {
		resourceAdapter.setItemCheckedAll(checked);
		resourceAdapter.notifyDataSetChanged();
	}

	@Override
	public void onActionBarItemCheckCancel() {
		hideCheckedMenu();
	}

}
