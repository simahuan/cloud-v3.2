package com.pisen.router.ui.phone.resource.v2.upload;

import java.io.IOException;
import java.util.List;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.pisen.router.R;
import com.pisen.router.common.dialog.LoadingDialog;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.transfer.TransferManagerV2;
import com.pisen.router.core.filemanager.transfer.TransferServiceV2;
import com.pisen.router.ui.base.CloudActivity;
import com.pisen.router.ui.base.FragmentSupport;
import com.pisen.router.ui.phone.resource.v2.FileChooserActivity;
import com.pisen.router.ui.phone.resource.v2.NavigationBar;

/**
 * 目录选择器
 * 
 * @author yangyp
 */
public class RootUploadActivity extends CloudActivity implements OnClickListener {

	private Button btnUpdatePath;
	private Button btnOk;
	private String curentPath;
	TransferManagerV2 transManger;
	private UploadFragment checkedActionBar;
	private NavigationBar navigationBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.resource_upload_root);
		initFileChooserViews();
		bindService();

		if (savedInstanceState == null) {
			setContentFragment();
		}
	}

	private void setContentFragment() {
		try {
			Intent intent = getIntent();
			curentPath = intent.getDataString();
			updateCurrentPathText();
			FileType fileType = FileType.valueOf(intent.getType());
			switch (fileType) {
			case All:
				addFragment(new RootUploadFragment(this));
				break;
			case Image:
			case Video:
				addFragment(new GridUploadFragment(this, null, fileType));
				break;
			case Audio:
			case Document:
				addFragment(new ListUploadFragment(this, null, fileType));
			default:
				break;
			}
		} catch (Exception e) {
			addFragment(new RootUploadFragment(this));
		}
	}

	private void addFragment(UploadFragment fragment) {
		checkedActionBar = fragment;
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.add(R.id.frmContent, (FragmentSupport) fragment);
		ft.commit();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (checkedActionBar != null) {
			if (checkedActionBar.onKeyDown(keyCode, event)) {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
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
		Intent in = new Intent(this, TransferServiceV2.class);
		bindService(in, conn, Service.BIND_AUTO_CREATE);
	}

	private void unbindService() {
		unbindService(conn);
	}

	@Override
	protected void onDestroy() {
		unbindService();
		super.onDestroy();
	}

	boolean checkAll = true;

	/**
	 * 初始化按钮控件
	 */
	public void initFileChooserViews() {
		btnUpdatePath = (Button) findViewById(R.id.btnUpdatePath);
		btnUpdatePath.setOnClickListener(this);
		btnOk = (Button) findViewById(R.id.btnOk);
		btnOk.setOnClickListener(this);
		navigationBar = (NavigationBar) getNavigationBar();
		navigationBar.setTitle("选择文件");
		navigationBar.setBackgroundColor(Color.parseColor("#58A5FF"));
		navigationBar.setLeftButton("返回", R.drawable.menu_ic_back, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				backPressed();
//				finish();
			}
		});

		navigationBar.setRightButton("全选", 0, new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkedActionBar.onActionBarItemCheckAll(checkAll);
				updateActionBarChanged();
			}
		});
	}

	public void backPressed(){
		Runtime runtime = Runtime.getRuntime();
		try {
			runtime.exec("input keyevent " + KeyEvent.KEYCODE_BACK);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private LoadingDialog mLoadingDialog;
	protected void showLoading() {
		if(mLoadingDialog == null) {
			mLoadingDialog = new LoadingDialog(this);
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
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnUpdatePath:
			startActivityForResult(new Intent(this, FileChooserActivity.class), 1);
			break;
		case R.id.btnOk:
			if (TextUtils.isEmpty(curentPath)) {
				UIHelper.showToast(this, "请选择目录");
				return;
			}

			@SuppressWarnings("unchecked")
			final List<ResourceInfo> itemCheckedList = checkedActionBar.getCheckedItemAll();
			if (itemCheckedList.isEmpty()) {
				UIHelper.showToast(this, "请选择文件");
				return;
			}
			
			uploadData(itemCheckedList);
			break;
		}
	}

	/**
	 * @des 上传　传输数据
	 * @param itemCheckedList
	 */
	private void uploadData(final List<ResourceInfo> itemCheckedList) {
		showLoading();
		AsyncTaskUtils.execute(new InBackgroundCallback<Boolean>() {
			@Override
			public Boolean doInBackground() {
				try {
					transManger.addUploadTask(curentPath, itemCheckedList);
					return true;
				} catch (Exception e) {
					return false;
				}
			}

			@Override
			public void onPostExecute(Boolean result) {
				dismissLoading();
				UIHelper.showToast(RootUploadActivity.this, "已为你添加到上传列表");
				finish();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == 1) {
				curentPath = data.getData().toString();
				updateCurrentPathText();
			}
		} else {

		}
	}

	private void updateCurrentPathText() {
		btnUpdatePath.setText(Uri.parse(curentPath).getPath());
	}

	public void updateActionBarChanged() {
		updateUpdateButtonText();

		int itemCount = checkedActionBar.getItemAll().size();
		int itemSelectedCount = checkedActionBar.getCheckedItemAll().size();
		
		checkAll = itemCount > 0 && itemCount == itemSelectedCount;
		navigationBar.setRightButton(checkAll ? "取消全选" : "全选", 0, null);
		checkAll = !checkAll;
	}

	/**
	 * 更新上传数量
	 */
	public void updateUpdateButtonText() {
		btnOk.setText(String.format("上传(%s)", checkedActionBar.getCheckedItemAll().size()));
	}
}
