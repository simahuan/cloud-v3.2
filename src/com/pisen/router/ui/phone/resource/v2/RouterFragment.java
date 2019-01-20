package com.pisen.router.ui.phone.resource.v2;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.studio.os.LogCat;
import android.studio.os.PreferencesUtils;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.pisen.router.R;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.dialog.InputDialog;
import com.pisen.router.common.dialog.LoadingDialog;
import com.pisen.router.common.dialog.ProgressDialog;
import com.pisen.router.common.utils.FileUtils;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.common.view.RouterGuidePopuWindow;
import com.pisen.router.config.AppConfig;
import com.pisen.router.config.Config;
import com.pisen.router.config.WifiConfig;
import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.SardineCacheResource;
import com.pisen.router.core.filemanager.async.MoveAsyncTask;
import com.pisen.router.core.filemanager.async.ResourceAsyncTask.ResourceItemCallback;
import com.pisen.router.core.filemanager.async.ResourceAsyncTask.ResourceResult;
import com.pisen.router.core.filemanager.transfer.DownloadSardineTask;
import com.pisen.router.core.filemanager.transfer.TransferManagerV2;
import com.pisen.router.core.filemanager.transfer.TransferServiceV2;
import com.pisen.router.core.filemanager.transfer.TransferStatus;
import com.pisen.router.core.filemanager.transfer.TransferTask;
import com.pisen.router.core.filemanager.transfer.UploadSardineTask;
import com.pisen.router.core.monitor.DiskMonitor;
import com.pisen.router.core.monitor.DiskMonitor.DiskEntity;
import com.pisen.router.core.monitor.DiskMonitor.OnDiskChangedListener;
import com.pisen.router.core.monitor.WifiMonitor;
import com.pisen.router.core.monitor.WifiMonitor.WifiStateCallback;
import com.pisen.router.ui.HomeActivity;
import com.pisen.router.ui.base.INavigationBar;
import com.pisen.router.ui.phone.resource.ResourceFragment;
import com.pisen.router.ui.phone.resource.transfer.TransferRecordActivity;
import com.pisen.router.ui.phone.resource.v2.CategoryPopupWindow.OnCategoryItemClickCallback;
import com.pisen.router.ui.phone.resource.v2.ChoiceNavigationBar.OnChoiceItemClickListener;
import com.pisen.router.ui.phone.resource.v2.ToolbarPopupWindow.OnToolbarItemClickCallback;
import com.pisen.router.ui.phone.resource.v2.panel.ISelectionActionBar;
import com.pisen.router.ui.phone.resource.v2.panel.ResourceManager;
import com.pisen.router.ui.phone.resource.v2.upload.RootUploadActivity;
import com.pisen.router.ui.phone.resource.v2.upload.UploadPopupWindow;
import com.pisen.router.ui.phone.resource.v2.upload.UploadPopupWindow.OnUploadItemClickListener;

/**
 * 私有云
 * @author yangyp
 * <p>王姜强2015-10-12:修改了磁盘列表界面的下拉刷新</p>
 * <p>王姜强2015-10-14:修改了重命名文件不允许修改后缀</p>
 */
public class RouterFragment extends ResourceFragment implements WifiStateCallback, OnDiskChangedListener,
		OnCategoryItemClickCallback, OnItemClickListener, OnToolbarItemClickCallback, OnUploadItemClickListener,
		OnChoiceItemClickListener {

	private ChoiceNavigationBar navigationBar;
	private ToolbarPopupWindow toolbarPopupWindow;

	private CategoryPopupWindow categoryPopupWindow;
	private UploadPopupWindow uploadPopupWindow;
	private DiskMonitor diskMonitor;
	private WifiMonitor wifiMonitor;

	private RouterShopPanel shopLayout;
	private LinearLayout diskLayout;
	private ResourceManager resourcePanel;
	private RouterFileFragment lastFragment;
	private RadioGroup tabHost;
	
	private PullToRefreshListView listContent;
	private RouterDiskAdapter diskAdapter;
	private TextView msgToast;
	private FileType type;

	public IResource sardineManager;
	private ISelectionActionBar<ResourceInfo> selectionActionBar;

	TransferManagerV2 transManger;
	private static final String KEY_ROUTER_FIRST_INIT = "is_router_first_init";
	public static final int REQUEST_VIEW = 0X4443;

	@Override
	public View onInjectCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.resource_home, container, false);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_VIEW) {
			if (resourcePanel.isCategoryPanel() && resultCode == Activity.RESULT_OK && data != null
					&& data.getBooleanExtra("refresh", false)) {
				selectionActionBar.onActionBarCompleted();
			}
		}
		if(lastFragment != null) {
			lastFragment.onActivityResult(requestCode, resultCode, data);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		resourcePanel = new ResourceManager(this);
		shopLayout = new RouterShopPanel(this, getActivity());
		diskLayout = (LinearLayout) findViewById(R.id.diskLayout);
		msgToast = (TextView) findViewById(R.id.msgToast);
		listContent = (PullToRefreshListView) findViewById(R.id.listContent);
		listContent.setOnRefreshListener(new OnRefreshListener2() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				LogCat.d("RouterFragment:onPullDownToRefresh...");
				diskMonitor.stopMonitor(getActivity().getApplicationContext());
				diskMonitor.startMonitor(getActivity().getApplicationContext());
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {
				LogCat.d("RouterFragment:onPullUpToRefresh...");
			}
		});
		listContent.setMode(Mode.PULL_FROM_START);

		diskMonitor = DiskMonitor.getInstance();
		diskMonitor.registerObserver(this);
		wifiMonitor = WifiMonitor.getInstance();
		wifiMonitor.registerObserver(this);
		sardineManager = new SardineCacheResource();

		diskAdapter = new RouterDiskAdapter(getActivity());
		listContent.setAdapter(diskAdapter);
		listContent.setOnItemClickListener(this);
		listContent.setFocusable(false);

		if (wifiMonitor.isPisenWifiConnected()) {
			if (diskMonitor.isScannerFinished()) {
				showDiskView();
				loadDiskData();
			} else {
				msgToast.setVisibility(View.VISIBLE);
				msgToast.setText("正在扫描磁盘，请稍候...");
			}
		} else {
			showShopView();
		}

		bindService();
		registerTransferChangeBroadcast();
	}

	private ServiceConnection conn;

	private void bindService() {
		conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				transManger = ((TransferServiceV2.TransferBinder) service).getTransferManager();
			}
		};
		Intent in = new Intent(getActivity(), TransferServiceV2.class);
		getActivity().bindService(in, conn, Service.BIND_AUTO_CREATE);
	}

	private void unbindService() {
		getActivity().unbindService(conn);
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		try {
			unbindService();
			if (transferChangeBroadcast != null) {
				getActivity().unregisterReceiver(transferChangeBroadcast);
			}
			diskMonitor.unregisterObserver(this);
			wifiMonitor.unregisterObserver(this);
		} catch (Exception e) {
		}
	}

	@Override
	public ChoiceNavigationBar getNavigationBar() {
		return navigationBar;
	}

	@Override
	protected INavigationBar newNavigationBar() {
		navigationBar = new ChoiceNavigationBar(this);
		navigationBar.setOnItemClickListener(this);
		updateNavigationBarToDefault();
		setNavigationBarCategory(false);
		return navigationBar;
	}

	@Override
	public void onNavigationBarItemCheckAll(boolean checked) {
		selectionActionBar.onActionBarItemCheckAll(checked);
		updateActionBarChanged();
	}

	@Override
	public void onNavigationBarItemCheckCancel() {
		selectionActionBar.onActionBarItemCheckCancel();
		hideSelectionMenu();
	}

	/**
	 * @des 更新导航栏TO Default
	 */
	public void updateNavigationBarToDefault() {
		String title = lastFragment == null ? "私有云" : Uri.parse(lastFragment.getParentPath()).getLastPathSegment();
		navigationBar.setTitle(title);
		navigationBar.setLeftButton(null, R.drawable.route, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((HomeActivity) getActivity()).toggleMenu();
			}
		});
		navigationBar.getTitleView().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showCategoryPopupWindow();
			}
		});

		navigationBar.setRightButton(null, R.drawable.upload, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (uploadPopupWindow == null) {
					uploadPopupWindow = new UploadPopupWindow(getActivity());
					uploadPopupWindow.setOnUploadItemClickListener(RouterFragment.this);
					uploadPopupWindow.setAnimationStyle(0);
					uploadPopupWindow.setOnDismissListener(new OnDismissListener() {
						@Override
						public void onDismiss() {
							navigationBar.getRightButton().setVisibility(View.VISIBLE);
						}
					});
				}

				if (!uploadPopupWindow.isShowing()) {
					uploadPopupWindow.showAsDropDown(navigationBar.getView(), 0, -navigationBar.getView().getHeight());
					navigationBar.getRightButton().setVisibility(View.INVISIBLE);
				}
			}
		});

		refreshTransferStatus();
	}

	/**
	 * 显示分类popwindow
	 */
	private void showCategoryPopupWindow() {
		if (categoryPopupWindow == null) {
			categoryPopupWindow = new CategoryPopupWindow(getActivity());
			categoryPopupWindow.setOnCategoryItemClickCallback(RouterFragment.this);
			categoryPopupWindow.setAnimationStyle(0);
			categoryPopupWindow.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					navigationBar.getTitleView().setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.pulldown, 0);
				}
			});
		}

		if (!categoryPopupWindow.isShowing()) {
			categoryPopupWindow.showAsDropDown(navigationBar.getView());
			navigationBar.getTitleView().setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.personalcloudup, 0);
		}
	}

	/**
	 * 设置是否启用分类导航
	 * 
	 * @param enabled
	 */
	public void setNavigationBarCategory(boolean enabled) {
		navigationBar.getTitleView().setClickable(enabled);
		navigationBar.getTitleView()
				.setCompoundDrawablesWithIntrinsicBounds(0, 0, enabled ? R.drawable.pulldown : 0, 0);
		navigationBar.getRightButton().setVisibility(enabled ? View.VISIBLE : View.GONE);
		if (!enabled) {
			navigationBar.setTitle("私有云");
		}
	}

	/**
	 * 文件上传至路由操作类　
	 */
	@Override
	public void onUploadItemClick(FileType type) {
		Intent intent = new Intent(getActivity(), RootUploadActivity.class);
		intent.setDataAndType(Uri.parse(lastFragment.getParentPath()), type.name());
		startActivity(intent);
	}

	/**
	 * @describtion  网络磁盘排序
	 * @param data
	 */
	private void netDiskSort(DiskEntity... data){
		if(data != null && data.length >0) {
			Collections.sort(Arrays.asList(data), new Comparator<DiskEntity>() {
				@Override
				public int compare(DiskEntity lhs, DiskEntity rhs) {
					if(lhs.getExtDiskMount() == rhs.getExtDiskMount()) {
						return 0;
					}else if(lhs.getExtDiskMount() && !rhs.getExtDiskMount()) {
						return 1;
					}else {
						return -1;
					}
				}
			});
		}
	}
	
	private void loadDiskData() {
		msgToast.setVisibility(View.GONE);
		DiskEntity[] disList = diskMonitor.getNetDisk();
		netDiskSort(disList);
		diskAdapter.setData(disList);
		diskAdapter.notifyDataSetChanged();
		
		if (disList.length == 0) {
			msgToast.setVisibility(View.VISIBLE);
			msgToast.setText("未检测到您的存储设备哦~");
			msgToast.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.usb, 0, 0);
			Config.setHasStorage(false);
		}else{
			Config.setHasStorage(true);
		}
	}

	/**
	 * 显示磁盘界面
	 */
	private void showDiskView() {
		shopLayout.hide();
		diskLayout.setVisibility(View.VISIBLE);
	}

	/**
	 * 显示购买界面
	 */
	private void showShopView() {
		diskLayout.setVisibility(View.GONE);
		shopLayout.show();
	}

	@Override
	public void onDiskChanged() {
		loadDiskData();
		if(listContent != null){
			listContent.onRefreshComplete();
		}
	}

	@Override
	public void onConnected(WifiConfig config) {
		if (config.isPisenWifi()) {
			showDiskView();
		} else {
			showShopView();
		}
	}

	@Override
	public void onDisconnected(WifiConfig config) {
		showShopView();
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DiskEntity disk = (DiskEntity) parent.getItemAtPosition(position);
		AppConfig.setCurrentDiskPath(disk.path);
		startChildFragment(new RouterFileFragment(this, disk.path));
		setNavigationBarCategory(true);
		// 第一次初始化，默认显示分类视图
		if (PreferencesUtils.getBoolean(KEY_ROUTER_FIRST_INIT, true)) {
			PreferencesUtils.setBoolean(KEY_ROUTER_FIRST_INIT, false);
			getHomeActivity().cancelProgressDialog();
			initGuideStepView();
		}
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
	}
	
    private RouterGuidePopuWindow guideStepPopuWindow;
    private static View guideStepView;
    /**
     * @describtion 蒙板向导
     */
	private void GuideStepPopuView() {
		final Window window = getHomeActivity().getWindow();
		WindowManager.LayoutParams lp = window.getAttributes();
		lp.alpha = 0.8f;
		window.setAttributes(lp);
		guideStepPopuWindow = new RouterGuidePopuWindow(getHomeActivity());
		guideStepPopuWindow.showAtLocation(RouterFragment.this.findViewById(R.id.diskLayout), Gravity.CENTER
				| Gravity.CENTER, 0, 0);
		guideStepPopuWindow.update();
		guideStepPopuWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
			@Override
			public void onDismiss() {
				WindowManager.LayoutParams lp = window.getAttributes();
				lp.alpha = 1f;
				window.setAttributes(lp);
				guideStepView = null;
				guideStepPopuWindow = null;
				System.gc();
				
				showCategoryPopupWindow();
			}
		});
	}

	private  Handler mHandler;
    private void initGuideStepView() {
        guideStepView = findViewById(R.id.diskLayout);
        mHandler = new Handler();
        mHandler.post(mRunnable);
    }
	
	Runnable mRunnable = new Runnable() {
		@Override
		public void run() {
			if (guideStepView.getWidth() > 0) {
				GuideStepPopuView();
			} else {
				mHandler.postDelayed(this, 100);
			}
		}
	};
    
	/**
	 * @des  取消引导
	 */
	private void cancelGuidePopuView(){
		if (null != guideStepPopuWindow){
			guideStepPopuWindow.dismiss();
		}
	}
	
	/**
	 * 顶部菜单匹配
	 */
	@Override
	public void onCategoryItemClick(FileType type) {
		this.type = type;
		if (type == FileType.All) {
			updateNavigationBarToDefault();
			resourcePanel.removeCategoryPanel();
			selectionActionBar = lastFragment;
			return;
		}

		String categoryTitle = getCategoryTitle(type);
		updateNavigationBar(categoryTitle);
		selectionActionBar = resourcePanel.switchPanel(AppConfig.getCurrentDiskPath(), type); //拿到交换面板
		refreshTransferStatus();
	}

	private String getCategoryTitle(FileType type) {
		switch (type) {
		case Video:
			return "视频";
		case Image:
			return "图片";
		case Audio:
			return "音乐";
		case Document:
			return "文档";
		case Apk:
			return "应用";
		default:
			return "全部";
		}
	}

	private void updateNavigationBar(String title) {
		navigationBar.setTitle(title);
		navigationBar.setRightButton(null, R.drawable.down_transmission, new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getActivity(), TransferRecordActivity.class));
			}
		});
	}

	private boolean childPopBackStack() {
		FragmentManager fm = getChildFragmentManager();
		int backCount = fm.getBackStackEntryCount();
		if (backCount > 0) {
			fm.popBackStack();
			if (backCount <= 1) {
				lastFragment = null;
				selectionActionBar = null;
				setNavigationBarCategory(false);
				listContent.setVisibility(View.VISIBLE);
			} else {
				lastFragment = (RouterFileFragment) fm.getFragments().get(backCount - 2);
				selectionActionBar = lastFragment;
				updateNavigationBarToDefault();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 如果导航是选中状态，那么关闭选择导航
			if (navigationBar.isShowChoiceBar()) {
				hideSelectionMenu();
				selectionActionBar.onActionBarItemCheckCancel();
				return true;
			}

			// 如果是分类，那么先关闭分类
			if (resourcePanel.isCategoryPanel()) {
				resourcePanel.removeCategoryPanel();
				type = null;
				updateNavigationBarToDefault();
				selectionActionBar = lastFragment;
				return true;
			}

			// 如果是RouterFileFragment，那么事件传下去
			if (selectionActionBar instanceof RouterFileFragment) {
				if (((RouterFileFragment) selectionActionBar).onKeyDown(keyCode, event)) {
					return true;
				}
			}

			if (childPopBackStack()) {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	public void startChildFragment(RouterFileFragment fragment) {
		this.lastFragment = fragment;
		this.selectionActionBar = fragment;
		listContent.setVisibility(View.GONE);
		getChildFragmentManager().beginTransaction().add(R.id.listfileLayout, fragment).addToBackStack(null).commit();
	}

	/**
	 * @des  显示底部操作菜单　
	 */
	public void showSelectionMenu() {
		navigationBar.showChoiceBar();
		if (toolbarPopupWindow == null) {
			toolbarPopupWindow = new ToolbarPopupWindow(getActivity());
			toolbarPopupWindow.setOnToolbarItemClickCallback(this);
		}

		if (!toolbarPopupWindow.isShowing()) {
			// toolbarPopupWindow.showAsDropDown(getView());
			toolbarPopupWindow.showAtLocation(diskLayout, Gravity.BOTTOM, 0, 0);
			if (tabHost != null) {
				tabHost.setVisibility(View.INVISIBLE);
			}
		}
		// 展示前刷新选中个数
		updateActionBarChanged();
		((HomeActivity)getActivity()).setSlidingMenuScrollable(false);
	}

	/**
	 * @describtion 隐藏选项菜单
	 */
	public void hideSelectionMenu() {
		navigationBar.setCheckedTextCount(0);
		navigationBar.showActionBar();
		if (toolbarPopupWindow != null) {
			toolbarPopupWindow.dismiss();
		}
		if (tabHost != null) {
			tabHost.setVisibility(View.VISIBLE);
		}
		((HomeActivity)getActivity()).setSlidingMenuScrollable(true);
	}

	public void updateActionBarChanged() {
		int itemCount = selectionActionBar.getItemAll().size();
		int itemSelectedCount = selectionActionBar.getCheckedItemAll().size();
		boolean checkAll = itemCount > 0 && itemCount == itemSelectedCount;

		navigationBar.setCheckedTextCount(itemSelectedCount);
		navigationBar.setCheckedChanged(checkAll);
	}

	/**
	 * Item项　下载,移动,重命名,删除
	 */
	@Override
	public void onToolbarItemClick(View v) {
		List<ResourceInfo> itemCheckedList = selectionActionBar.getCheckedItemAll();
		if (itemCheckedList.isEmpty()) {
			UIHelper.showToast(getActivity(), "请选择文件");
			return;
		}

		switch (v.getId()) {
		case R.id.btnDownload:
			downloadResource();
			break;
		case R.id.btnMove:
			moveResource();
			break;
		case R.id.btnRename:
			renameResource();
			break;
		case R.id.btnDelete:
			deleteResource();
			break;
		default:
			break;
		}
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
	 * 下载文件
	 */
	private void downloadResource() {
		showLoading();
		AsyncTaskUtils.execute(new InBackgroundCallback<Boolean>() {
			@Override
			public Boolean doInBackground() {
				try {
					final List<ResourceInfo> itemCheckedList = selectionActionBar.getCheckedItemAll();
					transManger.addDownloadTask(itemCheckedList);
					return true;
				} catch (Exception e) {
					return false;
				}
			}

			@Override
			public void onPostExecute(Boolean result) {
				dismissLoading();
				UIHelper.showToast(getActivity(), "已为你添加到下载列表");
				selectionActionBar.onActionBarCompleted();
			}
		});
	}

	boolean isShowDialog = false;
	boolean isSuccess = true;

	
	
	/**
	 * @desc  List 指定目录下 所有文件及文件夹名称(非递归)
	 * @param sPath
	 * @return 指定路径下,所有 文件及文件夹 名称 集合
	 */
	List<ResourceInfo> fileList;
	private List<String> getSpecifyPathFileName(final String sPath) {
		List<String> resName = null;
		try {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					LogCat.e("bDuplicate-->path=" + sPath);
					if (sardineManager != null) {
						try {
							fileList = sardineManager.list(sPath);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			});
			thread.start();
			thread.join();//可能会阻塞UI线程,采用异步任务,Handler
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} 
		
		if (fileList != null && fileList.size() > 0) {
			LogCat.e("fileList-->size=" + fileList.size());
			resName = new ArrayList<String>();
			for (ResourceInfo resourceInfo : fileList) {
				resName.add(resourceInfo.name);
			}
		}
		return resName;
	}

	/**
	 * @desc  判读是否存在 同名文件夹或文件名
	 * @param sFileName
	 * @param resName
	 * @param fileList
	 * @return 存在 true | 不存在 false;
	 */
	private boolean judgeDirOrFileName(final String sFileName, final List<String> resName) {
		return resName != null && resName.contains(sFileName) ? true : false;
	}
	
	/**
	 * 移动文件
	 */
	private void moveResource() {
		FileChooserActivity.start(getActivity(), "选择移动位置");
		isShowDialog = false;
		LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(getActivity());
		lbm.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (FileChooserActivity.Action_File_Chooser.equals(intent.getAction())) {
					if (isShowDialog) {
						return;
					}
					isShowDialog = true;
					isSuccess = true;
					final List<ResourceInfo> itemCheckedList = selectionActionBar.getCheckedItemAll();
					if (itemCheckedList == null || itemCheckedList.size() <= 0) {
						UIHelper.showToast(getActivity(), "请先选择移动文件");
						return;
					}

					// 遍历要移动目标路径下 所有文件 ,文件夹名称
					final String path = intent.getStringExtra(FileChooserActivity.FileChooser_Path);
					boolean bDuplicate = false;
					List<String> resNameList = getSpecifyPathFileName(path);
					
					for (ResourceInfo resourceInfo : itemCheckedList) {
						String fileName = resourceInfo.name; // 提取选中文件,文件夹 名称
						
						String s = FileUtils.getUpFileName(resourceInfo.path);
						if (path.equals(s)) {
							UIHelper.showToast(getActivity(), "不能移动到源路径");
							selectionActionBar.onActionBarCompleted();
							return;
						}
						if (resourceInfo.isDirectory) {
							if (path.contains(resourceInfo.path)) {
								UIHelper.showToast(getActivity(), "该目标路径已选,请选择其他路径");
								selectionActionBar.onActionBarCompleted();
								return;
							}
						}
						/** 判断移动文件中是否存在同名文件或文件夹 */
						if (!bDuplicate) {
							if (judgeDirOrFileName(fileName, resNameList)) {
								bDuplicate = true;
							}
						}
					}

					if (bDuplicate) {
						ConfirmDialog.show(getActivity(), "已存在同名文件或文件夹是否覆盖?", null, "确定",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										executorMoveOperator(itemCheckedList, path);
									}
								}, "取消", null);
					} else {
						LogCat.e("执行移动操作..");
						executorMoveOperator(itemCheckedList, path);
					}
				}
			}

			
			/**
			 * @desc  执行移动文件操作
			 * @param itemCheckedList
			 * @param path
			 */
			private void executorMoveOperator(final List<ResourceInfo> itemCheckedList, final String path) {
				final ProgressDialog moveDialog = new ProgressDialog(getActivity());
				getHomeActivity().showProgressDialog("移动准备...");

				final MoveAsyncTask moveTask = new MoveAsyncTask(sardineManager, itemCheckedList, path,
						selectionActionBar, null);
				moveTask.setItemCallback(new ResourceItemCallback() {
					@Override
					public void onItemCallback(ResourceResult result) {
						if (result == null || result.filename == null) {
							return;
						}
						LogCat.e("moveDialog.isShowing()=" + moveDialog.isShowing());

						if (!moveDialog.isShowing()) {
							getHomeActivity().dismissProgressDialog();
							moveDialog.show();
							moveDialog.setTitle("正在移动文件");
						}
						// 不能显示,难道是因为在非UI线程,所以不能显示
						moveDialog.setFileName(result.filename);
						moveDialog.setProgressText((result.mCount * 100 / result.mTotal));
						moveDialog.setMaxText(result.mCount, result.mTotal);

						LogCat.e("result.mCurrentBytes:" + result.mCurrentBytes + ",result.mTotalBytes"
								+ result.mTotalBytes);
						if (result.mCount == result.mTotal) {
							moveDialog.dismiss();
							if (isSuccess) {
								isSuccess = false;
								selectionActionBar.onActionBarCompleted();
								UIHelper.showToast(getActivity(), "移动成功");
							}
						} else if (result.getmStatus() == ResourceResult.UNKNOWN_ERROR) {
							moveTask.cancel(true);
							moveDialog.dismiss();
							selectionActionBar.onActionBarCompleted();
							UIHelper.showToast(getActivity(), "移动出错");
						}

					}
				});
				moveTask.execute();
			}
		}, new IntentFilter(FileChooserActivity.Action_File_Chooser));
	}

	
	private void renameResource() {
		final List<ResourceInfo> itemCheckedList = selectionActionBar.getCheckedItemAll();
		if (itemCheckedList.size() != 1) {
			UIHelper.showToast(getActivity(), "重命名只能选择一个");
			return;
		}

		final ResourceInfo resource = itemCheckedList.get(0);
		// final String filename = FileUtils.getFileNameNoFormat(resource.name);
		// final String fileFormat = FileUtils.getFileFormat(resource.name);

		InputDialog renameDialog = new InputDialog(getActivity());
		renameDialog.setTitle("重命名");
		// 获取后缀名
		final String fileExtention = resource.name != null && resource.name.contains(".") ? resource.name
				.substring(resource.name.lastIndexOf(".")) : "";
		// 获取出去后缀的文件名
		final String nameExceptExtention = resource.name != null && resource.name.contains(".") ? resource.name
				.substring(0, resource.name.lastIndexOf(".")) : (resource.name != null ? resource.name : "");
		renameDialog.setInputText(nameExceptExtention);
		renameDialog.setOnClickListener(new InputDialog.SimpleClickListener() {
			@Override
			public void onOk(DialogInterface dialog, final String inputText) {
				if (TextUtils.isEmpty(inputText)) {
					UIHelper.showToast(getActivity(), "文件名称不能为空");
					return;
				}

				AsyncTaskUtils.execute(new InBackgroundCallback<Integer>() {
					@Override
					public Integer doInBackground() {
						try {
							if (lastFragment != null) {
								String destFile = resource.isDirectory ? (lastFragment.getParentPath() + inputText + File.separator)
										: (lastFragment.getParentPath() + inputText);
								if (sardineManager.exists(destFile)) {
									return -1;
								}
							}
							sardineManager.rename(resource.path, inputText + fileExtention);
							return 1;
						} catch (Exception e) {
							return 0;
						}
					}

					@Override
					public void onPostExecute(Integer result) {
						selectionActionBar.onActionBarCompleted();
						if (result == 1) {
							UIHelper.showToast(getActivity(), "重命名成功");
						} else if (result == -1) {
							UIHelper.showToast(getActivity(), "文件名已存在");
						} else {
							UIHelper.showToast(getActivity(), "重命名失败");
						}
					}
				});
			}
		});
		renameDialog.show();
	}

	private void deleteResource() {
		final List<ResourceInfo> itemCheckedList = selectionActionBar.getCheckedItemAll();
		ConfirmDialog.show(getActivity(), "确定要删除选中项吗?", "删除", "确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AsyncTaskUtils.execute(new InBackgroundCallback<Boolean>() {
					@Override
					public Boolean doInBackground() {
						try {
							for (ResourceInfo info : itemCheckedList) {
								LogCat.e("=====deleteResource==="+info.path);
								sardineManager.delete(info.path);
							}
							return true;
						} catch (Exception e) {
							return false;
						}
					}

					@Override
					public void onPostExecute(Boolean result) {
						selectionActionBar.onActionBarCompleted();
						if (result) {
							UIHelper.showToast(getActivity(), "删除成功");
						} else {
							UIHelper.showToast(getActivity(), "删除失败");
						}
					}
				});
			}
		}, "取消", null);
	}

	private BroadcastReceiver transferChangeBroadcast;

	/**
	 * @des 注册传输改变广播
	 */
	private void registerTransferChangeBroadcast() {
		transferChangeBroadcast = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (UploadSardineTask.ACTION_ADD.equals(intent.getAction())) {
					if (lastFragment != null) {
						refreshTransferStatus();
						return;
					}
				}

				TransferStatus status = (TransferStatus) intent
						.getSerializableExtra(TransferTask.EXTRA_TRANSFER_STATUS);
				if (TransferStatus.SUCCESS.equals(status) && lastFragment != null) {
					refreshTransferStatus();
				}
			}
		};
		IntentFilter filter = new IntentFilter();
		filter.addAction(UploadSardineTask.ACTION_ADD);
		filter.addAction(UploadSardineTask.ACTION_PROGRESS);
		filter.addAction(DownloadSardineTask.ACTION_PROGRESS);
		getActivity().registerReceiver(transferChangeBroadcast, filter);
	}

	/**
	 * 刷新传输状态
	 */
	private void refreshTransferStatus() {
		if (navigationBar != null) {
			if (type == null || type == FileType.All) {
				if (lastFragment != null) {
					navigationBar.setRightButton(null, R.drawable.upload, null);
					lastFragment.checkTransferTaskStatus();
				}
			} else {
				int bgid = lastFragment.checkTransferTaskStatus() ? R.drawable.down_transmission_new_message
						: R.drawable.down_transmission;
				navigationBar.setRightButton(null, bgid, null);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshTransferStatus();
	}

	@Override
	public void onPause() {
		super.onPause();
		// LogCat.e("RouterFragment onPause.....");
		cancelGuidePopuView();
	}

	@Override
	public void onStop() {
		super.onStop();
		// LogCat.e("RouterFragment onStop.....");
		if (null != mHandler) {
			mHandler.removeCallbacks(mRunnable);
		}
	}

	public void setTabHost(RadioGroup tab) {
		this.tabHost = tab;
	}
}
