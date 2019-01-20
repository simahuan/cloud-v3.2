package com.pisen.router.ui;

import java.io.File;

import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.StatFs;
import android.studio.os.LogCat;
import android.studio.os.PreferencesUtils;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import cn.jpush.android.api.InstrumentedActivity;
import cn.jpush.android.api.JPushInterface;

import com.pisen.router.CloudApplication;
import com.pisen.router.R;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.common.view.FragmentTabHost;
import com.pisen.router.core.flashtransfer.scan.DeviceScanService;
import com.pisen.router.core.flashtransfer.scan.DeviceScanService.DeviceScanBinder;
import com.pisen.router.core.monitor.RedHotMonitor;
import com.pisen.router.core.monitor.RedHotMonitor.RedHotCallBack;
import com.pisen.router.ui.base.CloudActivity;
import com.pisen.router.ui.base.FragmentSupport;
import com.pisen.router.ui.phone.flashtransfer.FlashTransferFragment;
import com.pisen.router.ui.phone.leftmenu.LeftMenuFragment;
import com.pisen.router.ui.phone.resource.v2.RouterFragment;
import com.pisen.router.ui.phone.settings.SettingsFragment;
import com.pisen.router.ui.phone.settings.upgrade.AppVersion;
import com.pisen.router.ui.phone.settings.upgrade.DownLoadApp;
import com.pisen.router.ui.phone.teleplay.TeleplayFragment;
import com.pisen.router.ui.phone.upgrade.AppUpgraderActivity;
import com.pisen.router.ui.widget.ResideMenu;

public class HomeActivity extends CloudActivity {

	private LeftMenuFragment leftMenu;
	private RadioButton rdoBtnMore;
	private ResideMenu resideMenu;
	private RadioGroup mTabHost;
	private FragmentTabHost tabHost;
	private long mKeyTime;
	private RedHotMonitor redHotMonitor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_home);

		mTabHost = (RadioGroup) findViewById(android.R.id.tabhost);
		tabHost = new FragmentTabHost(this, mTabHost, android.R.id.tabcontent);

		tabHost.addTab(RouterFragment.class);
		tabHost.addTab(FlashTransferFragment.class);
		tabHost.addTab(TeleplayFragment.class);
		tabHost.addTab(SettingsFragment.class);
		tabHost.setup(R.id.btnResource);

		resideMenu = new ResideMenu(this);
		View leftMenuView = View.inflate(this, R.layout.cloud_leftmenu, null);
		resideMenu.setMenuItemView(leftMenuView, ResideMenu.DIRECTION_LEFT);
		resideMenu.attachToActivity(this);
		resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

		leftMenu = new LeftMenuFragment(this, leftMenuView); // 构造对象左侧菜单
		rdoBtnMore = (RadioButton) findViewById(R.id.rdoBtnMore);
		if (PreferencesUtils.getBoolean(KeyUtils.APP_VERSION, false)) {
			setUpgradeRedHot(rdoBtnMore, R.drawable.toolbar_more_reddot);
		}
		redHotMonitor = RedHotMonitor.getInstance();
		redHotMonitor.registerObserver(callBack);

		checkStorageSize();
		CloudApplication.getInstance().bindService();
	}

	/**
	 * 检查可用存储空间，并提示
	 */
	private void checkStorageSize() {
		if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			return;
		}
		if (getSDCardAvailableSize() < 200) {
			UIHelper.showToast(this, R.string.storage_insufficient_hint, 5 * 1000);
		}
	}

	private long getSDCardAvailableSize() {
		long size = 0;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			boolean isHigherSdk = Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1;
			long blockSize = isHigherSdk ? sf.getBlockSizeLong() : sf.getBlockSize();
			long availCount = isHigherSdk ? sf.getAvailableBlocksLong() : sf.getAvailableBlocks();
			size = availCount * blockSize / 1024 / 1024;
		}
		return size;
	}

	private long getDataAvailableSize() {
		File root = Environment.getDataDirectory();
		StatFs sf = new StatFs(root.getPath());
		boolean isHigherSdk = Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1;
		long blockSize = isHigherSdk ? sf.getBlockSizeLong() : sf.getBlockSize();
		long availCount = isHigherSdk ? sf.getAvailableBlocksLong() : sf.getAvailableBlocks();
		return availCount * blockSize / 1024 / 1024;
	}

	/** redHot更新回调 */
	RedHotCallBack callBack = new RedHotCallBack() {
		@Override
		public void update(AppVersion ver, DownLoadApp app) {
			setUpgradeRedHot(rdoBtnMore, R.drawable.toolbar_more_reddot);
			showConfirmDialog(ver, app);
		}
	};

	/**
	 * @describtion 设置侧滑状态enable
	 * @param sroll
	 */
	public void setSlidingMenuScrollable(boolean sroll) {
		if (resideMenu != null) {
			resideMenu.setScrollable(sroll);
		}
	}

	/**
	 * @des 使能可滑动
	 */
	private void enableScroolabe() {
		if (!resideMenu.canScrollable()) {
			setSlidingMenuScrollable(true);
		}
	}

	public void toggleMenu() {
		resideMenu.toggle();
	}

	public void showConfirmDialog(final AppVersion ver, final DownLoadApp app) {
		final ConfirmDialog cd = new ConfirmDialog(HomeActivity.this);
		cd.setTitle("品胜云升级");
		cd.setMessage("有新版本为" + ver.Version + ",是否更新？" + "\r\n" + ver.VersionDescription, Gravity.CENTER_VERTICAL);
		if (0 == ver.IsAutoUpgrade) {
			cd.setNegativeButton("暂不升级", null);
		} else {
			cd.setCancelable(false);
		}
		cd.setPositiveButton("立即升级", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cd.dismiss();
				if (0 == ver.IsAutoUpgrade) {
					app.showDownloadDialog(HomeActivity.this);
				} else {
					AppUpgraderActivity.start(HomeActivity.this, ver.Link, ver.Version);
				}
			}
		});
		cd.show();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		leftMenu.onDestroyView();
		super.onDestroy();
		CloudApplication.getInstance().unbindService();
		redHotMonitor.unregisterObserver(callBack);
	}

	@Override
	protected void onResume() {
		super.onResume();
		LogCat.e("=====onResume===========");
		JPushInterface.onResume(this);
		leftMenu.onResume();
		enableScroolabe();
	}

	@Override
	protected void onPause() {
		super.onPause();
		JPushInterface.onPause(this);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		LogCat.e("onActivityResult");
		super.onActivityResult(requestCode, resultCode, data);
		tabHost.getCurrentTabFragment().onActivityResult(requestCode, resultCode, data);
		if (requestCode == LeftMenuFragment.REQUEST_LOGIN) {
			if (resultCode == RESULT_OK) {
				leftMenu.authorityFunctionClicked(data.getIntExtra("id", -1));
			}
		}
	}

	/**
	 * @des 版本更新更多reddot
	 */
	public void setUpgradeRedHot(RadioButton btn, int resId) {
		Drawable drawable = getResources().getDrawable(resId);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		btn.setCompoundDrawables(null, drawable, null, null);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Fragment tabFragment = tabHost.getCurrentTabFragment();
		if (!resideMenu.isOpened() && tabFragment instanceof FragmentSupport) {
			FragmentSupport fragment = (FragmentSupport) tabFragment;
			if (fragment.onKeyDown(keyCode, event)) {
				return true;
			}
		}

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (resideMenu.isOpened()) {
				toggleMenu();
				return true;
			}

			if (System.currentTimeMillis() - mKeyTime > 2000) {
				mKeyTime = System.currentTimeMillis();
				UIHelper.showToast(this, "再按一次退出程序");
			} else {
				stopFlashTransfer();
				exitApplication();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void stopFlashTransfer() {
		boolean result = bindService(new Intent(this, DeviceScanService.class), new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				((DeviceScanBinder) service).getService().releaseAllResouce();
				((DeviceScanBinder) service).getService().stopSelf();
				unbindService(this);
			}
		}, Service.BIND_AUTO_CREATE);
		if (!result) {
			LogCat.e("close connect called, but bind service failed!!!");
			stopService(new Intent(this, DeviceScanService.class));
		}
	}
}
