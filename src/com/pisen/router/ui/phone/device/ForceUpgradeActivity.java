package com.pisen.router.ui.phone.device;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.studio.os.LogCat;
import android.studio.os.NetUtils;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.config.ResourceConfig;
import com.pisen.router.core.device.AbstractDevice;
import com.pisen.router.ui.base.CloudActivity;
import com.pisen.router.ui.phone.device.bean.FirmwareData;
import com.pisen.router.ui.phone.device.bean.ZFirmwareInfo;

/**
 * 智享固件强制升级
 * @author ldj
 * @version 1.0 2015年9月14日 下午5:15:12
 */
public class ForceUpgradeActivity extends CloudActivity implements OnClickListener {

	private static final String FIRMWARE_STATE_DOWNLOAD = "download"; // 正在下载
	private static final String FIRMWARE_STATE_ABNORMAL = "abnormal"; // 下载被异常中断
	private static final String FIRMWARE_STATE_COMPLET = "complet"; // 下载完成
	
	private Button btnDeviceUpgrade;
	private ImageView ivPoint,ivlogo;
	private TextView tvDeviceVersion, tvDeviceNewVersion;
	private TextView tvDeviceMode, tvTip,txtProgress,txtProgressPa,textLabel;
	private Handler handler;
	private ZFirmwareInfo firmwareInfo;
	private View bottomLayout;
	private Button cancelButton,retryButton, OkButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_device_firmwareupgrade_force);

		firmwareInfo = (ZFirmwareInfo) getIntent().getSerializableExtra("info");
		bottomLayout  = findViewById(R.id.bottomLayout);
		cancelButton = (Button) findViewById(R.id.btnCancel);
		retryButton = (Button) findViewById(R.id.btnRetry);
		OkButton =  (Button) findViewById(R.id.btnOk);
		ivPoint = (ImageView) findViewById(R.id.ivPoint);
		ivlogo = (ImageView) findViewById(R.id.ivlogo);
		btnDeviceUpgrade = (Button) findViewById(R.id.btnDeviceUpgrade);
		tvDeviceVersion = (TextView) findViewById(R.id.tvDeviceVersion);
		tvDeviceMode = (TextView) findViewById(R.id.tvDeviceMode);
		tvDeviceNewVersion = (TextView) findViewById(R.id.tvDeviceNewVersion);
		txtProgress = (TextView) findViewById(R.id.txtProgress);
		txtProgressPa = (TextView) findViewById(R.id.txtProgressPa);
		tvTip = (TextView) findViewById(R.id.tvTip);
		textLabel = (TextView) findViewById(R.id.txtLbl);
		btnDeviceUpgrade.setOnClickListener(this);
		retryButton.setOnClickListener(this);
		cancelButton.setOnClickListener(this);
		OkButton.setOnClickListener(this);
		tvDeviceMode.setText(ResourceConfig.getInstance(this).getDeviceName());
		
		 handler = new Handler(){
				@Override
				public void handleMessage(Message msg) {
					super.handleMessage(msg);
					if (msg.what == 0) {
						new FirmwareDownAsyncTask().execute(true);
					}else if(msg.what == 0x100) {
						startUpdate();
					}
				}
		 };

		 if (!NetUtils.isWifiConnected(this)) {
			 UIHelper.showToast(this, "网络不给力");
		 }
		 
		 if(firmwareInfo == null) {
			 UIHelper.showToast(this, "获取数据异常");
			 finish();
		 }
		 
		 setData(firmwareInfo);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnDeviceUpgrade:
		case R.id.btnRetry:
			if (NetUtils.isWifiConnected(this)) {
				startDownload();
			} else {
				UIHelper.showToast(this, "网络不给力");
				showErrorView();
			}
			break;
		case R.id.btnCancel:
			finish();
			break;
		case R.id.btnOk:
			exitApplication();
			break;
		default:
			break;
		}

	}

	public void back(View view) {
		finish();
	}

	public void startDownload() {
		new FirmwareDownAsyncTask().execute(false);
	}
	
	public void startUpdate() {
		new ExecuteAsyncTask().execute();
	}

	public void startAnimation() {
		if (!AbstractDevice.getInstance().isLogin(ForceUpgradeActivity.this)) {
			return;
		}
		if (ivPoint != null) {
			ivPoint.clearAnimation();
			final RotateAnimation animation = new RotateAnimation(0f, 359f, Animation.RELATIVE_TO_PARENT, 0.03f, Animation.RELATIVE_TO_PARENT, 0.5f);
			animation.setDuration(2000);
			animation.setRepeatCount(-1);
			animation.setInterpolator(new LinearInterpolator());
			animation.setFillAfter(false);
			ivPoint.startAnimation(animation);
		}
	}

	public void stopAnimation() {
		if (ivPoint != null) {
			ivPoint.clearAnimation();
		}
	}

	/**
	 * 刷新界面数据
	 * 
	 * @param info
	 */
	private void setData(ZFirmwareInfo info) {
		this.firmwareInfo = info;
		// 当前版本
		tvDeviceVersion.setText("当前版本:v" + info.getCur_version_name());
		tvDeviceVersion.setVisibility(View.VISIBLE);

		 if (!TextUtils.isEmpty(info.getService_version_name())) {
			 tvTip.setText("检测到您有新的版本需要更新");
			 tvDeviceNewVersion.setText("固件版本:v" + info.getService_version_name());
			// 有新版本
			if (info.getService_version_name().equals(info.getNew_version_name())) {
				// 服务器已经下载完成最新版本,立即更新
//				tvTip.setText("最新固件版本v" + info.getService_version_name() + "已完成下载");
//				tvDeviceNewVersion.setText("");
//				tvDeviceNewVersion.setVisibility(View.GONE);
//				btnDeviceUpgrade.setText(getString(R.string.dev_upgrade_execute));
			} else {
//				tvDeviceNewVersion.setVisibility(View.VISIBLE);
//				btnDeviceUpgrade.setText(getString(R.string.dev_upgrade_download));
			}
			btnDeviceUpgrade.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * 更新下载进度
	 * @param progress
	 */
	private void upgradeProgress(boolean isReset,String progress){
		LogCat.e("当前下载进度:%s", progress+"%");
		if (isReset) {
			txtProgress.setText("0");
			txtProgress.setVisibility(View.GONE);
			txtProgressPa.setVisibility(View.GONE);
			ivlogo.setVisibility(View.VISIBLE);
		}else{
			txtProgress.setText(progress);
			txtProgress.setVisibility(View.VISIBLE);
			txtProgressPa.setVisibility(View.VISIBLE);
			ivlogo.setVisibility(View.INVISIBLE);
		}
	}

	/**
	 * 显示开始下载界面
	 */
	private void showStartDownloadView() {
		tvTip.setText("正在下载最新固件,请稍后...\n\n\n");
		textLabel.setText("请保持设备不断电及网络畅通");
		textLabel.setVisibility(View.VISIBLE);
		tvDeviceNewVersion.setVisibility(View.GONE);
		btnDeviceUpgrade.setVisibility(View.GONE);
		bottomLayout.setVisibility(View.GONE);
	}
	
	/**
	 * 显示固件下载完成，准备更新界面
	 */
	private void showUpdatingView() {
		tvTip.setText("最新固件已完成下载，准备更新...\n\n");
		textLabel.setText("路由设备即将重启以完成固件升级，\n您的手机会与设备断开，请稍后...");
		textLabel.setVisibility(View.VISIBLE);
		tvDeviceNewVersion.setVisibility(View.GONE);
		btnDeviceUpgrade.setVisibility(View.GONE);
		bottomLayout.setVisibility(View.GONE);
	}

	/**
	 * 显示更新失败界面
	 */
	private void showErrorView() {
		isRunning = false;
		stopAnimation();
		upgradeProgress(true,"0");
		tvTip.setText("Oops！\n设备更新失败\n");
		textLabel.setText("请检查网络是否畅通并重试");
		textLabel.setVisibility(View.VISIBLE);
		tvDeviceNewVersion.setVisibility(View.GONE);
		btnDeviceUpgrade.setVisibility(View.GONE);
		bottomLayout.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 显示更新成功界面
	 */
	private void showSucceedView() {
		isRunning = false;
		stopAnimation();
		upgradeProgress(true,"0");
		tvTip.setText("设备更新中\n\n");
		textLabel.setText("更新过程中请勿断电或重启路由器，蓝灯闪烁更新完成");
		textLabel.setVisibility(View.VISIBLE);
		tvDeviceNewVersion.setVisibility(View.GONE);
		btnDeviceUpgrade.setVisibility(View.GONE);
		bottomLayout.setVisibility(View.GONE);
		OkButton.setVisibility(View.VISIBLE);
	}

	private boolean isRunning = false;
	private class FirmwareDownAsyncTask extends AsyncTask<Boolean, Void, FirmwareData> {
		boolean isProgressQuery;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showStartDownloadView();
			if (!isRunning) {
				isRunning = true;
				startAnimation();
			}
		}


		@Override
		protected FirmwareData doInBackground(Boolean... params) {
			isProgressQuery = params[0];
			return AbstractDevice.getInstance().downloadFirmware(isProgressQuery);
		}

		@Override
		protected void onPostExecute(FirmwareData result) {
			super.onPostExecute(result);
			if (result != null) {
				if(!isProgressQuery) {
					upgradeProgress(false,"0");
					//间隔一秒去请求一次
					handler.sendEmptyMessageDelayed(0, 1000);
					return;
				}
				
				if (FIRMWARE_STATE_COMPLET.equals(result.getState())) {
					LogCat.e("下载完成");
					upgradeProgress(false,"100");
					showUpdatingView();
					handler.sendEmptyMessageDelayed(0x100, 6000);
					return;
				}else if( FIRMWARE_STATE_DOWNLOAD.equals(result.getState())){
					if (!TextUtils.isEmpty(result.getReceived_Percentage())) {
						upgradeProgress(false,result.getReceived_Percentage());
						//间隔一秒去请求一次
						handler.sendEmptyMessageDelayed(0, 1000);
						return;
					}
				}
				LogCat.e("下载被异常中断:%s",result.getState());
				showErrorView();
			}else{
				showErrorView();
			}
		}
	}

	
	/**
	 * 执行升级
	 * 
	 * @author Liuhc
	 * @version 1.0 2015年7月3日 下午1:33:29
	 */
	private class ExecuteAsyncTask extends AsyncTask<String, Void, Boolean> {
//		@Override
//		protected void onPreExecute() {
//			super.onPreExecute();
//			showUpdatingView();
//			startAnimation();
//		}

		@Override
		protected Boolean doInBackground(String... params) {
			return AbstractDevice.getInstance().executeUpgrade();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			// 停止动画
			stopAnimation();
			isRunning = false;
			if (result) {
				WifiConnectUtils wifiMgr = new WifiConnectUtils(ForceUpgradeActivity.this);
				wifiMgr.disconnectCurrent();
				showSucceedView();
			} else {
				showErrorView();
			}
		}
	}
}
