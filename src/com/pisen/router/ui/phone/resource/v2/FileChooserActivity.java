package com.pisen.router.ui.phone.resource.v2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.config.WifiConfig;
import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.SardineCacheResource;
import com.pisen.router.core.monitor.DiskMonitor;
import com.pisen.router.core.monitor.DiskMonitor.DiskEntity;
import com.pisen.router.core.monitor.DiskMonitor.OnDiskChangedListener;
import com.pisen.router.core.monitor.WifiMonitor;
import com.pisen.router.core.monitor.WifiMonitor.WifiStateCallback;
import com.pisen.router.ui.base.FragmentActivity;

/**
 * 目录选择器
 * 
 * @author yangyp
 */
public class FileChooserActivity extends FragmentActivity implements WifiStateCallback, OnDiskChangedListener, OnClickListener, OnItemClickListener {

	public static final String Action_File_Chooser = "pisen.android.intent.action.FileChooser.back";
	public static final String FileChooser_Path = "FileChooser_path";

	private TextView txtMessage;
	private ListView lstContent;
	private RouterDiskAdapter diskAdapter;

	private WifiMonitor wifiMonitor;
	public DiskMonitor diskMonitor;
	public IResource sardineManager;

	public static void start(Context context, String title) {
		Intent intent = new Intent(context, FileChooserActivity.class);
		intent.setData(Uri.parse(title));
		context.startActivity(intent);
	}

	public static void startActivityForResult(Activity activity, int requestCode, String title) {
		Intent intent = new Intent(activity, FileChooserActivity.class);
		intent.setData(Uri.parse(title));
		activity.startActivityForResult(intent, requestCode);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resource_file_chooser);
		initFileChooserViews();

		sardineManager = new SardineCacheResource(null, null);

		wifiMonitor = WifiMonitor.getInstance();
		wifiMonitor.registerObserver(this);

		diskMonitor = DiskMonitor.getInstance();
		diskMonitor.registerObserver(this);

		if (wifiMonitor.isPisenWifiConnected()) {
			if (diskMonitor.isScannerFinished()) {
				loadDiskData();
			} else {
				showLoading("正在扫描磁盘，请稍候...");
			}
		} else {
			showLoading("Pisen Wifi未连接");
		}
	}

	@Override
	protected void onDestroy() {
		diskMonitor.unregisterObserver(this);
		super.onDestroy();
	}

	/**
	 * 初始化按钮控件
	 */
	public void initFileChooserViews() {
		txtMessage = (TextView) findViewById(R.id.txtMessage);
		lstContent = (ListView) findViewById(R.id.lstContent);
		findViewById(R.id.btnOk).setOnClickListener(this);
		findViewById(R.id.btnCancel).setOnClickListener(this);

		diskAdapter = new RouterDiskAdapter(this);
		lstContent.setAdapter(diskAdapter);
		lstContent.setOnItemClickListener(this);

		NavigationBar navigationBar = (NavigationBar) getNavigationBar();
		// navigationBar.setTitle("选择移动位置");
		navigationBar.setBackgroundColor(Color.parseColor("#58A5FF"));
		navigationBar.setLeftButton("返回", R.drawable.menu_ic_back, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				popBackStack();
			}
		});

		Intent intent = getIntent();
		if (intent != null && intent.getData() != null) {
			getNavigationBar().setTitle(intent.getDataString());
		} else {
			getNavigationBar().setTitle("选择目录");
		}
	}

	private void showLoading(String message) {
		txtMessage.setVisibility(View.VISIBLE);
		txtMessage.setText(message);
	}

	private void hideLoading() {
		txtMessage.setVisibility(View.GONE);
	}

	/**
	 * 创建目录
	 */
	/*
	 * private void newFolder() { InputDialog createNewFolder = new
	 * InputDialog(this); createNewFolder.setTitle("新建文件夹");
	 * createNewFolder.setOnClickListener(new SimpleClickListener() {
	 * 
	 * @Override public void onOk(DialogInterface dialog, final String
	 * inputText) { if (TextUtils.isEmpty(inputText)) {
	 * UIHelper.showToast(FileChooserActivity.this, "请输入目录"); return; }
	 * AsyncTaskUtils.execute(new InBackgroundCallback<Boolean>() {
	 * 
	 * @Override public Boolean doInBackground() { try {
	 * sardineManager.createDir(curentPath + inputText); return true; } catch
	 * (Exception e) { return false; } }
	 * 
	 * @Override public void onPostExecute(Boolean result) { if (result) {
	 * loadResourceData(); } else { UIHelper.showToast(getActivity(), inputText
	 * + " 目录创建失败."); } } }); } }); createNewFolder.show(); }
	 */

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnCancel:
			finish();
			break;
		case R.id.btnOk:

			if (lastFragment == null) {
				UIHelper.showToast(this, "请选择目录");
				return;
			}

			String curentPath = lastFragment.getParentPath();
			LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);
			lbm.sendBroadcast(new Intent(Action_File_Chooser).putExtra(FileChooser_Path, curentPath));

			setResult(RESULT_OK, new Intent().setData(Uri.parse(curentPath)));
			finish();
			break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		DiskEntity disk = diskAdapter.getItem(position);
		startChildFragment(new FileChooserFragment(this, disk.path));
	}

	FileChooserFragment lastFragment;

	public void startChildFragment(FileChooserFragment fragment) {
		lastFragment = fragment;
		getSupportFragmentManager().beginTransaction().add(R.id.childLayout, fragment).addToBackStack(null).commit();
	}

	@Override
	public void popBackStack() {
		FragmentManager fm = getSupportFragmentManager();
		int backCount = fm.getBackStackEntryCount();
		if (backCount > 0) {
			fm.popBackStack();
			if (backCount <= 1) {
				lastFragment = null;
			} else {
				lastFragment = (FileChooserFragment) fm.getFragments().get(backCount - 2);
			}
		} else {
			finish();
		}
	}

	@Override
	public void onDiskChanged() {
		loadDiskData();
	}

	@Override
	public void onDisconnected(WifiConfig config) {

	}

	@Override
	public void onConnected(WifiConfig config) {

	}

	private void loadDiskData() {
		hideLoading();
		DiskEntity[] disList = diskMonitor.getNetDisk();
		diskAdapter.setData(disList);
		diskAdapter.notifyDataSetChanged();
	}

}
