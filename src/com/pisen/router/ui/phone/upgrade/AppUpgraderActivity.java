package com.pisen.router.ui.phone.upgrade;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.studio.os.PreferencesUtils;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.ui.base.CloudActivity;
import com.pisen.router.ui.phone.upgrade.AppDownloadAsync.AppDownloadListener;

/**
 * App版本强制更新
 * 
 * @author tanyixiu
 * @version 3.3, 2015年10月13日 上午11:11:35
 */
public class AppUpgraderActivity extends CloudActivity {

	private View layoutToUpdate;
	private View layoutUpdating;
	private View layoutUpdateFail;

	private Button btnToUpdate;
	private Button btnRetry;
	private ProgressBar progressbar;

	private String param_apkUrl = "";
	private String param_apkVersion = "";
	private AppDownloadAsync appDownloadAsync;

	private static final String PARAM_KEY_URL = "URL";
	private static final String PARAM_KEY_VERSION = "VERSION";

	public static void start(Activity activity, String url, String version) {
		if (TextUtils.isEmpty(url) || TextUtils.isEmpty(version)) {
			return;
		}
		Intent intent = new Intent(activity, AppUpgraderActivity.class);
		intent.putExtra(PARAM_KEY_URL, url);
		intent.putExtra(PARAM_KEY_VERSION, version);
		activity.startActivity(intent);
		activity.finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_phone_appupgrader);
		initParam();
		initView();
		initViewEvent();
	}

	private void initParam() {
		Intent intent = getIntent();
		if (null == intent) {
			return;
		}
		param_apkUrl = intent.getStringExtra(PARAM_KEY_URL);
		param_apkVersion = intent.getStringExtra(PARAM_KEY_VERSION);
	}

	private void initView() {
		layoutToUpdate = findViewById(R.id.appupgrader_llin_toupdate);
		layoutUpdating = findViewById(R.id.appupgrader_llin_updating);
		layoutUpdateFail = findViewById(R.id.appupgrader_llin_updatefail);
		btnToUpdate = (Button) findViewById(R.id.appupgrader_btn_update);
		btnRetry = (Button) findViewById(R.id.appupgrader_btn_retry);
		progressbar = (ProgressBar) findViewById(R.id.appupgrader_pbar_updating);
		String labelVersion = String.format(getResources().getString(R.string.appupgrade_text_version), param_apkVersion);
		((TextView) findViewById(R.id.appupgrader_txt_version1)).setText(labelVersion);
		((TextView) findViewById(R.id.appupgrader_txt_version2)).setText(labelVersion);
		progressbar.setMax(100);
		appDownloadAsync = new AppDownloadAsync(param_apkUrl, param_apkVersion);
		appDownloadAsync.setAppDownloadListener(appDownloadListener);
	}

	private void initViewEvent() {
		btnToUpdate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				btnUpdateClick();
			}
		});

		btnRetry.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				btnRetryClick();
			}
		});
	}

	protected void btnRetryClick() {
		btnUpdateClick();
	}

	protected void btnUpdateClick() {
		if (appDownloadAsync.isApkExist()) {
			installApk(appDownloadAsync.getFileName());
			return;
		}
		if (isWifiState()) {
			showUpdatingView();
			appDownloadAsync.execute();
			return;
		}
		final ConfirmDialog cd = new ConfirmDialog(AppUpgraderActivity.this);
		cd.setTitle(R.string.appupgrade_text_hint_title);
		cd.setMessage(getResources().getString(R.string.appupgrade_text_hint_wifi), Gravity.CENTER_VERTICAL);
		cd.setNegativeButton(R.string.appupgrade_text_text_cancel, null);
		cd.setCancelable(false);
		cd.setPositiveButton(R.string.appupgrade_text_text_ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cd.dismiss();
				showUpdatingView();
				appDownloadAsync.execute();
			}
		});
		cd.show();
	}

	private AppDownloadListener appDownloadListener = new AppDownloadListener() {

		@Override
		public void onDownloading(int downloadedSize, int totalsize) {
			int progress = (int) (((float) downloadedSize / totalsize) * 100);
			progressbar.setProgress(progress);
		}

		@Override
		public void onDownloadFinish(String fileName) {
			installApk(fileName);
		}

		@Override
		public void onDownloadFail() {
			UIHelper.showToast(AppUpgraderActivity.this, R.string.appupgrade_text_text_resourcenotfount);
			showFailedView();
		}
	};

	/**
	 * 是否是wifi环境
	 * 
	 * @return
	 */
	private boolean isWifiState() {
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (null == connectivity) {
			return false;
		}
		NetworkInfo[] info = connectivity.getAllNetworkInfo();
		if (null == info) {
			return false;
		}
		for (int i = 0; i < info.length; i++) {
			if (info[i].getTypeName().equals("WIFI") && info[i].isConnected()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 显示正在更新的视图
	 */
	private void showUpdatingView() {
		layoutToUpdate.setVisibility(View.GONE);
		layoutUpdating.setVisibility(View.VISIBLE);
		layoutUpdateFail.setVisibility(View.GONE);
	}

	/**
	 * 显示更新失败视图
	 */
	private void showFailedView() {
		layoutToUpdate.setVisibility(View.GONE);
		layoutUpdating.setVisibility(View.GONE);
		layoutUpdateFail.setVisibility(View.VISIBLE);
	}

	/**
	 * 开始安装
	 * 
	 * @param apkDirectory
	 */
	private void installApk(String apkDirectory) {
		if (TextUtils.isEmpty(apkDirectory)) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkDirectory), "application/vnd.android.package-archive");
		startActivityForResult(i, REQUEST_CODE);
	}

	private static final int REQUEST_CODE = 100;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (REQUEST_CODE != requestCode) {
			return;
		}

		if (RESULT_FIRST_USER == resultCode && null != data && null != data.getExtras()) {
			// 解析包有问题时
			appDownloadAsync.deleteApkFile();
			showFailedView();
			return;
		}

		PreferencesUtils.setBoolean(KeyUtils.APP_VERSION, false);
		AppUpgraderActivity.this.finish();

	}
}
