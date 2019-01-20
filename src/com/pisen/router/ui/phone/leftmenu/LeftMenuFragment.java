package com.pisen.router.ui.phone.leftmenu;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.graphics.Paint;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.studio.os.LogCat;
import android.studio.os.NetUtils;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.dialog.DeviceSearchDialog;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.common.utils.WifiSearcher;
import com.pisen.router.common.utils.WifiSearcher.ErrorType;
import com.pisen.router.common.utils.WifiSearcher.SearchWifiListener;
import com.pisen.router.config.ResourceConfig;
import com.pisen.router.config.WifiConfig;
import com.pisen.router.core.device.AbstractDevice;
import com.pisen.router.core.monitor.DiskMonitor;
import com.pisen.router.core.monitor.DiskMonitor.OnDiskChangedListener;
import com.pisen.router.core.monitor.WifiMonitor;
import com.pisen.router.core.monitor.WifiMonitor.WifiStateCallback;
import com.pisen.router.core.monitor.entity.RouterConfig.Model;
import com.pisen.router.ui.HomeActivity;
import com.pisen.router.ui.base.FragmentActivity;
import com.pisen.router.ui.phone.device.ChaseDramaConstant;
import com.pisen.router.ui.phone.device.DeviceListActivity;
import com.pisen.router.ui.phone.device.FirmwareUpgradeActivity;
import com.pisen.router.ui.phone.device.IRouterResponse;
import com.pisen.router.ui.phone.device.LoginActivity;
import com.pisen.router.ui.phone.device.MeasureDevice;
import com.pisen.router.ui.phone.device.NetworkSettingActivity;
import com.pisen.router.ui.phone.device.PisenConstant;
import com.pisen.router.ui.phone.device.RouterManagerActivity;
import com.pisen.router.ui.phone.device.WifiConnectUtils;
import com.pisen.router.ui.phone.device.WifiSettingActivity;
import com.pisen.router.ui.phone.device.bean.WifiBean;
import com.pisen.router.ui.phone.flashtransfer.FlashTransferNetUtil;
import com.pisen.router.ui.phone.settings.HuiYuanDiFragment;

/**
 * 菜单栏
 * @dec fake fragment
 * @author yangyp
 */
public class LeftMenuFragment implements View.OnClickListener, WifiStateCallback, OnDiskChangedListener {

	public static final int MSG_REFRESH_DEVICE_SUCCESS = 0x1001;// 刷新设备型号
	public static final int MSG_REFRESH_DEVICE_FAILED = 0x1002;// 获取设备型号失败
	public static final int MSG_REFRESH_DEVICE_DIALOG = 0x1003;// 显示对话框
	public static final int MSG_AP_ENABLED = 0x1004; // 设备热点开启
	public static final int MSG_SCAN_DEVICE_TIMEOUT = 0x1005;// 扫描设备超时
	public static final int MSG_SCAN_DEVICE_GIVEUP = 0x1006;// 用户放弃搜索设备
	public static final int MSG_REFRESH_SSID = 0x1007;// 刷新SSID
	public static final int MSG_DEVICE_SCAN_FAILED = 0x1008;// 扫描设备失败
	
	public static Model   deviceMode = null;
	public static String  deviceName = null;
	public static boolean isRefresh = true;
	private String wifiSsid;

	private LinearLayout menuNewLayout;
	private LinearLayout menuOldLayout;
	private LinearLayout menuBottomLayout;

	private TextView txtDeviceSwitch;
	private TextView txtDeviceMode;
	private TextView txtWifiSsid;
	private TextView txtDevice;
	private TextView txtWifi;
	private TextView txtNetwork;
	private TextView txtStorage;
	private TextView txtUpgrade;
	private TextView txtFactoryReset;
//	private TextView txtShutdown;
	private TextView txtRestart;
	private TextView txtRouterOld;
	private TextView txtStorageOld;

	private Handler mHandler = null;
	private DiskMonitor diskMonitor;
	private WifiMonitor wifiMonitor;

	private Context activity;
	private View rootView;
	private WifiConnectUtils wifiMgr;
	private FlashTransferNetUtil mFlashTransferNetUtil;
	
	public static final int REQUEST_LOGIN = 0X1000;

	public LeftMenuFragment(HomeActivity homeActivity) {
		this.activity = homeActivity;
		this.rootView = homeActivity.findViewById(R.id.leftMenu);
		initView();
	}

	public LeftMenuFragment(HomeActivity homeActivity, View rootView) {
		this.activity = homeActivity;
		this.rootView = rootView.findViewById(R.id.leftMenu);
		initView();
	}
	
	private View findViewById(int id) {
		return rootView.findViewById(id);
	}

	public void initView() {
		menuNewLayout = (LinearLayout) findViewById(R.id.menuNewLayout);
		menuOldLayout = (LinearLayout) findViewById(R.id.menuOldLayout);
		menuBottomLayout = (LinearLayout) findViewById(R.id.menuBottomLayout);
		txtDeviceMode = (TextView) findViewById(R.id.txtDeviceMode);
		txtWifiSsid = (TextView) findViewById(R.id.txtWifiSsid);
		txtDeviceSwitch = (TextView) findViewById(R.id.txtDeviceSwitch);
		txtDeviceSwitch.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		
		txtDevice = (TextView) findViewById(R.id.txtDevice);
		txtWifi = (TextView) findViewById(R.id.txtWifi);
		txtNetwork = (TextView) findViewById(R.id.txtNetwork);
		txtStorage = (TextView) findViewById(R.id.txtStorage);
		txtUpgrade = (TextView) findViewById(R.id.txtUpgrade);
		txtFactoryReset = (TextView) findViewById(R.id.txtFactoryReset);
		//txtShutdown = (TextView) findViewById(R.id.txtShutdown);
		txtRestart = (TextView) findViewById(R.id.txtRestart);
		txtRouterOld = (TextView) findViewById(R.id.txtRouterOld);
		txtStorageOld = (TextView) findViewById(R.id.txtStorageOld);

		txtDeviceSwitch.setOnClickListener(this);
		txtDevice.setOnClickListener(this);
		
		txtWifi.setOnClickListener(this);
		txtNetwork.setOnClickListener(this);
		txtStorage.setOnClickListener(this);
		txtUpgrade.setOnClickListener(this);
		txtFactoryReset.setOnClickListener(this);
		//txtShutdown.setOnClickListener(this);
		txtRestart.setOnClickListener(this);
		txtRouterOld.setOnClickListener(this);
		txtStorageOld.setOnClickListener(this);

		mFlashTransferNetUtil = FlashTransferNetUtil.getInstance(activity);
		//refreshLeftMenuStatus();
		initHandler();

		wifiMonitor = WifiMonitor.getInstance();
		wifiMonitor.registerObserver(this);
		diskMonitor = DiskMonitor.getInstance();
		diskMonitor.registerObserver(this);
	}

	public void onResume() {
		isRefresh = true;
		mHandler.sendEmptyMessage(MSG_REFRESH_SSID);
		txtWifiSsid.requestFocus();
	}

	public void onDestroyView() {
		isRefresh = true;
		deviceMode = null;
		deviceName = "";
		wifiSsid = "";
		mHandler = null;
		PisenConstant.sessionId = "";
		PisenConstant.username = "";
		if (wifiMonitor != null) {
			wifiMonitor.unregisterObserver(this);
		}
		if (diskMonitor != null) {
			diskMonitor.unregisterObserver(this);
		}
	}

	/**
	 * @describtion 刷新左侧状态
	 */
	private void refreshLeftMenuStatus() {
		if (TextUtils.isEmpty(deviceName)) {
			txtDeviceMode.setText(activity.getString(R.string.dev_unconnected));
			txtWifiSsid.setVisibility(View.GONE);
			LogCat.e("未：deviceName:" + deviceName + " wifiSsid:" + wifiSsid +" DeviceMode:"+deviceMode);
		} else {
			LogCat.e("已：deviceName:" + deviceName + " wifiSsid:" + wifiSsid+" DeviceMode:"+deviceMode);
			
			if (deviceMode != null) {
				deviceName = ResourceConfig.getInstance(activity).getDeviceName();
			}
			
			txtDeviceMode.setText(deviceName);
			if (TextUtils.isEmpty(wifiSsid) || wifiSsid.contains("unknown")|| wifiSsid.contains("0x")) {
				txtWifiSsid.setVisibility(View.GONE);
			} else {
				txtWifiSsid.setText(wifiSsid + activity.getString(R.string.dev_wifi_connected));
				txtWifiSsid.setVisibility(View.VISIBLE);
				if (deviceMode != null) {
					isRefresh = false;
				}
			}
		}

		if (deviceMode == null) {
			return;
		}
		
		
		// if (DeviceMode == Model.RZHIXIANG) {
		// txtRestart.setVisibility(View.GONE);
		// txtShutdown.setVisibility(View.VISIBLE);
		// } else {
		// txtRestart.setVisibility(View.VISIBLE);
		// txtShutdown.setVisibility(View.GONE);
		// }

		if (deviceMode == Model.R150M) {
			menuNewLayout.setVisibility(View.GONE);
			menuBottomLayout.setVisibility(View.GONE);
			menuOldLayout.setVisibility(View.VISIBLE);
		} else {
			menuNewLayout.setVisibility(View.VISIBLE);
			menuBottomLayout.setVisibility(View.VISIBLE);
			menuOldLayout.setVisibility(View.GONE);
		}

		if (deviceMode != Model.R300M) {
			if (LoginActivity.isRunning) {
				LoginActivity.isRunning = false;
				Intent intent = new Intent();
				intent.setAction(LoginActivity.ACTION_CLOSE_ACTIVITY);
				activity.sendBroadcast(intent);
			}
		}
	}

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == MSG_REFRESH_DEVICE_SUCCESS) {
					refreshLeftMenuStatus();
				} else if (msg.what == MSG_REFRESH_SSID) {
					refreshLeftMenuStatus();
				} else if (msg.what == MSG_REFRESH_DEVICE_FAILED) {
					refreshLeftMenuStatus();
				} else if (msg.what == MSG_REFRESH_DEVICE_DIALOG) {
					showDeviceSearchDialog();
				} else if (msg.what == MSG_SCAN_DEVICE_TIMEOUT) {
					UIHelper.showToast(activity, activity.getString(R.string.dev_search_timeout));
				} else if (msg.what == MSG_AP_ENABLED) {
					UIHelper.showToast(activity, "请先确认WIFI功能已经开启");
				} else if(msg.what == MSG_DEVICE_SCAN_FAILED){
					UIHelper.showToast(activity, "扫描设备失败,请检查后再试");
				}
			}
		};
		
		deviceMode = ResourceConfig.getInstance(activity).getDeviceMode();
		LogCat.e("==deviceMode==:" +deviceMode);
		if (deviceMode != null ) {
			deviceName = ResourceConfig.getInstance(activity).getDeviceName();
			wifiSsid = NetUtils.getWifiSSID(activity);
			LogCat.e("wifiSsid:---- "+wifiSsid);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.txtDeviceSwitch:
		case R.id.txtDevice:
			searchDevices();
			return;
		}

		String mode = txtDeviceMode.getText().toString().trim();
		if (!NetUtils.isWifiConnected(activity) || activity.getString(R.string.dev_unconnected).equals(mode)) {
			UIHelper.showToast(activity, activity.getString(R.string.dev_unconnected_tip));
			return;
		}
		
		if (deviceMode == null) {
			if (NetUtils.isWifiConnected(activity)) {
				UIHelper.showToast(activity, activity.getString(R.string.dev_requesting_devmode));
			} else {
				showDeviceSearchDialog();
			}
			return;
		}

		if (deviceMode == Model.R150M) {
			// 代工设备
			switch (v.getId()) {
			case R.id.txtStorageOld:
				activity.startActivity(new Intent(activity, MeasureDevice.class));
				break;
			case R.id.txtRouterOld:
				activity.startActivity(new Intent(activity, RouterManagerActivity.class));
				break;
			}
			return;
		}

		if (v.getId() == R.id.txtStorage) {
			activity.startActivity(new Intent(activity, MeasureDevice.class));
			return;
		}

		// 智享和pisen300M新设备
		if (deviceMode == Model.R300M) {
			if (TextUtils.isEmpty(PisenConstant.sessionId) || TextUtils.isEmpty(PisenConstant.username)) {
				// 如果是pisen设备需要登陆
				Intent in = new Intent(activity, LoginActivity.class);
				in.putExtra("id", v.getId());
				((HomeActivity)activity).startActivityForResult(in, REQUEST_LOGIN);
				return;
			}
		}

		authorityFunctionClicked(v.getId());
	}
	

	/**
	 * 需登录的功能点击事件处理:通过认证函数单击跳调
	 * @param id
	 */
	public void authorityFunctionClicked(int id) {
		switch (id) {
		case R.id.txtWifi:
			activity.startActivity(new Intent(activity, WifiSettingActivity.class));
			break;
		case R.id.txtNetwork:
			activity.startActivity(new Intent(activity, NetworkSettingActivity.class));
			break;
		case R.id.txtUpgrade:
			activity.startActivity(new Intent(activity, FirmwareUpgradeActivity.class));
			break;
		case R.id.txtFactoryReset:
			factoryReset();
			break;
		case R.id.txtRestart:
			restartRouter();
			break;
		default:
			break;
		}
	}

	/**
	 * @desc 恢复出厂
	 */
	private void factoryReset() {
		ConfirmDialog dialog = new ConfirmDialog(activity);
		dialog.setTitle("恢复出厂设置");
		dialog.setMessageCenter("此操作将还原所有设置,但不会删除存储数据,确定还原设备将会重启");
		dialog.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new FactoryResetAsyncTask().execute("");
			}
		});
		dialog.setNegativeButton("取消", null);
		dialog.show();
	}

	/**
	 * 重启路由设备
	 */
	private void restartRouter() {
		ConfirmDialog dialog3 = new ConfirmDialog(activity);
		dialog3.setTitle("重新启动");
		dialog3.setMessageCenter("您的云路由将重新启动");
		dialog3.setPositiveButton("确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new ReStartAsyncTask().execute("");
			}
		});
		dialog3.setNegativeButton("取消", null);
		dialog3.show();
	}

	/**
	 * 显示查找设备对话框
	 */
	private void showDeviceSearchDialog() {
		// 未发现设备
		ConfirmDialog dialog = new ConfirmDialog(activity);
		dialog.setTitle("未检测到可连接的设备");
		dialog.setMessage("1,如果您已经有品胜云路由请检查后重新查找  \n2,如果您还没有品胜云路由可以点击购买");
		dialog.setNegativeButton("重新查找", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				dialog = null;
				searchDevices();
			}
		});
		dialog.setPositiveButton("点击购买", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				FragmentActivity.startFragment(activity, HuiYuanDiFragment.class);
				dialog.dismiss();
				dialog = null;
			}
		});
		dialog.show();
	}

	
	boolean isGiveUp = false;
	private DeviceSearchDialog waitDialog;

	/**
	 * @查找设备
	 */
	public void searchDevices() {
		isGiveUp = false;
		if (waitDialog == null) {
			waitDialog = new DeviceSearchDialog(activity);
			waitDialog.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					isGiveUp = true;
					releaseDialog();
					return true;
				}
			});
		}
		if (!waitDialog.isShowing()) {
			waitDialog.show();
		}
		
		if (mFlashTransferNetUtil.isWifiApEnabled()){
			mFlashTransferNetUtil.closeAp();
		}
		
		WifiSearcher searcher = new WifiSearcher(activity, new SearchWifiListener() {
			@Override
			public void onSearchWifiSuccess(List<ScanResult> results) {
				releaseDialog();
				LogCat.i("wifi list:" + results.size());
				if (isGiveUp) {
					return;
				}
				if (!results.isEmpty()) {
					ArrayList<WifiBean> result = new ArrayList<WifiBean>();
					WifiBean wifi = null;
					for (ScanResult scanResult : results) { //把结果集每个wifi信息对应的数据提取成bean.
						if (scanResult.BSSID.startsWith(WifiMonitor.PISEN_BSSID_PREFIX)) {
							wifi = new WifiBean();
							wifi.setSsid(scanResult.SSID);
							wifi.setSignal(scanResult.level + "");
							String pwdType = scanResult.capabilities;
							if (pwdType.contains("WEP")) {
								wifi.setEncryption("WEP");
							} else if (pwdType.contains("WPA")) {
								wifi.setEncryption("WPA");
							} else if (pwdType.contains("WPA2")) {
								wifi.setEncryption("WPA2");
							} else {
								wifi.setEncryption("");
							}
							if (!result.contains(wifi)) {
								result.add(wifi);
							}
						}
					}

					Intent i = new Intent(activity, DeviceListActivity.class);
					i.putExtra("list", result);
					activity.startActivity(i);
					results.clear();
					result.clear();
				} else {
					mHandler.sendEmptyMessage(MSG_REFRESH_DEVICE_DIALOG);
				}
			}

			@Override
			public void onSearchWifiFailed(ErrorType errorType) {
				releaseDialog();
				if (mHandler != null) {
					if (errorType == ErrorType.NO_WIFI_FOUND) {
						mHandler.sendEmptyMessage(MSG_REFRESH_DEVICE_DIALOG);
					} else if (errorType == ErrorType.SEARCH_WIFI_TIMEOUT) {
						mHandler.sendEmptyMessage(MSG_SCAN_DEVICE_TIMEOUT);
					} else if (errorType == ErrorType.AP_ENABLED) {
						mHandler.sendEmptyMessage(MSG_AP_ENABLED);
					}else{
						mHandler.sendEmptyMessage(MSG_DEVICE_SCAN_FAILED);
					}
				}
			}
		});
		searcher.search();
	}

	/**
	 * 用户停止搜索
	 */
	private void releaseDialog() {
		if (waitDialog != null) {
			waitDialog.dismiss();
			waitDialog = null;
		}
	}

	/**
	 * 恢复出厂设置
	 */
	public void setFactoryReset() {
		ChaseDramaConstant.request(ChaseDramaConstant.Url_SYS_FACTORY_RESET, new IRouterResponse() {
			@Override
			public void onSuccess(String result) {
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject obj = new JSONObject(result);
						if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
							UIHelper.showToast(activity, "恢复出厂设置成功,请重新启动设备");
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onError(String errorInfo) {
			}
		});
	}

	/**
	 * 关闭设备
	 */
	public void shutdownDecive() {
		ChaseDramaConstant.request(ChaseDramaConstant.Url_SYS_SHUTDOWN, new IRouterResponse() {
			@Override
			public void onSuccess(String result) {
				if (!TextUtils.isEmpty(result)) {
					try {
						JSONObject obj = new JSONObject(result);
						if ("OK".equals(obj.getJSONObject("status").optString("errorcode"))) {
							UIHelper.showToast(activity, "设置成功");
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onError(String errorInfo) {
			}
		});
	}

	/**
	 * @desc 恢复出厂异步任务
	 */
	private class FactoryResetAsyncTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			// showProgressDialog("");
			UIHelper.showToast(activity, "正在恢复出厂设置");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			return AbstractDevice.getInstance().setFactoryReset();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				UIHelper.showToast(activity, "恢复出厂设置成功,云路由即将重启");
				if (wifiMgr == null) {
					wifiMgr = new WifiConnectUtils(activity);
				}
				wifiMgr.disconnectCurrent();
				release();
				mHandler.sendEmptyMessage(MSG_REFRESH_SSID);
			} else {
				UIHelper.showToast(activity, "恢复出厂设置失败");
			}
		}
	}

	private class ShutdownAsyncTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			UIHelper.showToast(activity, "正在关闭设备");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			return AbstractDevice.getInstance().setFactoryReset();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				UIHelper.showToast(activity, "设置成功");
				if (wifiMgr == null) {
					wifiMgr = new WifiConnectUtils(activity);
				}
				wifiMgr.disconnectCurrent();
				release();
				mHandler.sendEmptyMessage(MSG_REFRESH_SSID);
			} else {
				UIHelper.showToast(activity, "设置失败");
			}
		}
	}

	/**
	 * @desc 重启路由异步任务
	 */
	private class ReStartAsyncTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			return AbstractDevice.getInstance().reStartDevice();
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				UIHelper.showToast(activity, "设置成功,云路由即将重启");
				if (wifiMgr == null) {
					wifiMgr = new WifiConnectUtils(activity);
				}
				wifiMgr.disconnectCurrent();
				release();
				mHandler.sendEmptyMessage(MSG_REFRESH_SSID);
			} else {
				UIHelper.showToast(activity, "设置失败");
			}
		}
	}

	@Override
	public void onConnected(WifiConfig config) {
		if (config.isPisenWifi()) {
			deviceMode = ResourceConfig.getInstance(activity).getDeviceMode();
			String name = config.getSSID().trim();
			if (deviceMode == null || (!TextUtils.isEmpty(wifiSsid) && !wifiSsid.equals(name))) {
				release();
				deviceName = activity.getString(R.string.dev_requesting);
				wifiSsid = name;
				mHandler.sendEmptyMessage(MSG_REFRESH_SSID);
				LogCat.e("deviceName -> " + deviceName + "  Model -> " + deviceMode);
			}
		}
	}

	@Override
	public void onDisconnected(WifiConfig config) {
		release();
		mHandler.sendEmptyMessage(MSG_REFRESH_SSID);
	}

	private void release(){
		ResourceConfig.getInstance(activity).setDeviceMode(null);
		ResourceConfig.getInstance(activity).setDeviceName(null);
		PisenConstant.sessionId = null;
		PisenConstant.username = null;
		deviceMode = null;
		deviceName = null;
		wifiSsid = null;
		isRefresh = true;
	}
	
	@Override
	public void onDiskChanged() {
		if (isRefresh) {
			deviceMode = ResourceConfig.getInstance(activity).getDeviceMode();
		}
		
		if (isRefresh == true && mHandler != null && deviceMode != null) {
			deviceName = ResourceConfig.getInstance(activity).getDeviceName();
			wifiSsid = NetUtils.getWifiSSID(activity);
			mHandler.sendEmptyMessage(MSG_REFRESH_DEVICE_SUCCESS);
			LogCat.e("deviceName -> " + deviceName + "  Model -> " + deviceMode + "wifiSsid:"+wifiSsid+"wifi.length:"+wifiSsid.length());
		}
	}
}
