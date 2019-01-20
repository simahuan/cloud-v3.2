package com.pisen.router.ui.phone.flashtransfer;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.studio.os.NetUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.pisen.router.R;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.config.WifiConfig;
import com.pisen.router.core.flashtransfer.FlashTransferConfig;
import com.pisen.router.core.flashtransfer.FlashTransferManager;
import com.pisen.router.core.flashtransfer.scan.DeviceContainer;
import com.pisen.router.core.flashtransfer.scan.DeviceScanHandler.OnScanResultCallback;
import com.pisen.router.core.flashtransfer.scan.DeviceScanService;
import com.pisen.router.core.flashtransfer.scan.DeviceScanService.DeviceScanBinder;
import com.pisen.router.core.flashtransfer.scan.protocol.UserInfoPtlV2;
import com.pisen.router.core.monitor.WifiSSIDMonitor;
import com.pisen.router.core.monitor.WifiSSIDMonitor.WifiSSIDCallback;
import com.pisen.router.ui.base.FragmentActivity;
import com.pisen.router.ui.phone.flashtransfer.FlashTransferNetUtil.ScanFlashApListener;
import com.pisen.router.ui.phone.flashtransfer.SearchDeviceLayout.OnDeviceSelectListener;
import com.pisen.router.ui.phone.flashtransfer.SearchDeviceLayout.TransferDevice;
/**
 * 连接管理（创建连接、加入）
 * @author ldj
 * @version 1.0 2015年5月20日 上午11:06:05
 */
public class ConnectManageActivity extends Activity implements OnClickListener, OnDeviceSelectListener, OnScanResultCallback {
	private static final String TAG = ConnectManageActivity.class.getSimpleName();
	private static final boolean DEBUG = false;
	
	private ImageButton createApButton;
	private ImageButton searchButton;
	private ImageButton infoButton;
	private ImageButton closeButton;
	private ImageButton refreshButton;
	private TextView createTipView;
	private TextView searchTipView;
	private TextView inviteInstall;
	private TextView txtWifiName;
	private CreateApLayout createApLayout;
	private SearchDeviceLayout searchDeviceView;
	private RelativeLayout relNetWorkStateShown;
	
	// 当前状态
	private int curStatus = STATUS_DEFAULT;
	// 默认视图状态
	private static final int STATUS_DEFAULT = 1;
	// 创建AP视图状态
	private static final int STATUS_CREATE = 2;
	// 搜索视图状态
	private static final int STATUS_SEARCH = 3;

	// progress权值，用于提高动画细腻度
	private static final float PROGRESS_WEIGHT = 1.2f;
	// ap创建过程占用的最大progress
	private static final float PROGRESS_MAX_CREATE = 70 * PROGRESS_WEIGHT;
	private static final int WHAT_OPENAP_START = 1;
	private static final int WHAT_OPENAP_RESULT_SUCCEED = 2;
	private static final int WHAT_OPENAP_RESULT_SUCCEED_NO_SSID = 0X200;
	private static final int WHAT_OPENAP_RESULT_FAILED = 3;
	private static final int WHAT_OPENAP_CANCEL = 4;
	private static final int WHAT_SHOW_DEFAULT_VIEW = 5;
	//没有用户连接ap
	private static final int WHAT_AP_NO_USER_CONNECT = 6;
	//用户连接ap失败
	private static final int WHAT_CONNECT_AP_FAILED = 8;
	//扫描局域网设备超时
	private static final int WHAT_WLAN_SCAN_TIMEOUT = 9;
	//等待用户连接ap时间
	private static final int TIME_WAIT_CONNECT = 30*1000;

	private DeviceScanService deviceScanService;
	private ConnectChangedReceiver connectChangedReceiver;
	private AnimatorListener createApShowAnimListener;
	private AnimatorListener searchDeviceShowAnimListener;

	private FlashTransferNetUtil transferNetUtil;
	/* 设备扫描相关 */
	private WifiManager wifiManager;
	private ServiceConnection conn;
	//设备是否已接入局域网进行传输（非网络是否已连接）
	private boolean isConnected;
	private boolean waitConnectToHost;
	private int progress = 0;
	private boolean once = false;
	
	private SoundPool pool;
	private int soundId;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flash_transfer_connect_activity);

		findView();
		initView();
		
		pool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
		soundId = pool.load(getApplicationContext(), R.raw.connected, 1);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registReceiver();
		if(!once) startInitAnim();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver();
	}
	
	@Override
	protected void onDestroy() {
		WifiSSIDMonitor.getInstance().unregisterObserver(callBack);
		if(pool != null) pool.release();
		super.onDestroy();
	}
	
	private void startInitAnim() {
		Animation scaleAnim  = new ScaleAnimation(0.0f, 1f, 0.0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnim.setDuration(500);
		scaleAnim.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				once = true;
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				final Animation txtAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.text_top_in);
				txtAnim.setStartOffset(100);
				createTipView.setAnimation(txtAnim);
				txtAnim.startNow();
				createTipView.setVisibility(View.VISIBLE);
			}
		});
		createApButton.setAnimation(scaleAnim);
		scaleAnim.startNow();
		
		Animation scaleAnimStartOffset  = new ScaleAnimation(0.0f, 1f, 0.0f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scaleAnimStartOffset.setStartOffset(250);
		scaleAnimStartOffset.setDuration(500);
		scaleAnimStartOffset.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				once = true;
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				final Animation txtAnim = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.text_top_in);
				txtAnim.setStartOffset(100);
				searchTipView.setAnimation(txtAnim);
				txtAnim.startNow();
				searchTipView.setVisibility(View.VISIBLE);
			}
		});
		searchButton.setAnimation(scaleAnimStartOffset);
		scaleAnimStartOffset.startNow();
	}
	
	@Override
	public void finish() {
		if(deviceScanService != null){
			deviceScanService.unregistOnScanResultCallback(this);
			//无连接（没有选择接入网络），则停止扫描及关闭服务
			if(!transferNetUtil.isWifiApEnabled() && !isConnected && DeviceContainer.getInstance(getApplicationContext()).getUserList().isEmpty()) {
				deviceScanService.stopScanDevice();
				stopService(new Intent(this, DeviceScanService.class));
			}else {
				FlashTransferManager.startSendService(getApplicationContext());
				FlashTransferManager.startRecvService(getApplicationContext());
			}
		}
		
		if(conn != null) {
			unbindService(conn);
			conn = null;
		}
		
		isConnected = false;
		waitConnectToHost = false;
		super.finish();
		overridePendingTransition(0, R.anim.alpha_activity_out);
	}
	
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;
			switch (what) {
			case WHAT_OPENAP_START:
				createApLayout.setProgress(++progress);
				startCreateApProgress(msg.what);
				break;
			case WHAT_OPENAP_RESULT_SUCCEED_NO_SSID:
			case WHAT_OPENAP_RESULT_SUCCEED:// 创建AP成功
				String ssid = transferNetUtil.getApSsid();
				if(!TextUtils.isEmpty(ssid) && !ssid.equals("null")) {
					createApLayout.setProgress(++progress);
					createApLayout.showCreateApSucceed(ssid);
					startCreateApProgress(WHAT_OPENAP_RESULT_SUCCEED);
				}else {
					if (progress <= PROGRESS_MAX_CREATE) {
						Log.e(TAG, "startCreateApProgress(WHAT_OPENAP_RESULT_SUCCEED_NO_SSID);");
						createApLayout.setProgress(++progress);
						startCreateApProgress(WHAT_OPENAP_RESULT_SUCCEED_NO_SSID);
					}else {
						showCreateApFailedView();
					}
				}
				break;
			case WHAT_OPENAP_RESULT_FAILED:// 创建AP失败
				createApLayout.showTip("连接创建失败");
				showCreateApFailedView();
				break;
			case WHAT_OPENAP_CANCEL:// 取消创建AP（关闭ap）
				removeMessages(WHAT_AP_NO_USER_CONNECT);
				removeMessages(WHAT_OPENAP_START);
				removeMessages(WHAT_OPENAP_RESULT_SUCCEED);
				removeMessages(WHAT_OPENAP_RESULT_FAILED);
				removeMessages(WHAT_OPENAP_RESULT_SUCCEED_NO_SSID);
				createApLayout.showCancleCreateAp();
				sendEmptyMessageDelayed(WHAT_SHOW_DEFAULT_VIEW, 500);// 500ms后显示默认视图
				break;
			case WHAT_SHOW_DEFAULT_VIEW:// 显示默认视图
				showDefaultView();
				break;
			case WHAT_AP_NO_USER_CONNECT:// 无用户连接ap
				showApNoConnectionView();
				break;
			case WHAT_CONNECT_AP_FAILED:// 连接设备失败
				showConnectDeviceFaildView();
				break;
			case WHAT_WLAN_SCAN_TIMEOUT:// 扫描局域网设备超时
				showSearchFailedView();
				break;
			default:
				break;
			}
		};
	};

	private void findView() {
		createApButton = (ImageButton) findViewById(R.id.imgCreateAp);
		searchButton = (ImageButton) findViewById(R.id.imgSearchAp);
		infoButton = (ImageButton) findViewById(R.id.imgInfo);
		closeButton = (ImageButton) findViewById(R.id.imgClose);
		refreshButton =(ImageButton) findViewById(R.id.ibtnRetry);
		createTipView = (TextView) findViewById(R.id.txtCreateAp);
		searchTipView = (TextView) findViewById(R.id.txtSearchAp);
		createApLayout = (CreateApLayout) findViewById(R.id.createApLayout);
		searchDeviceView = (SearchDeviceLayout) findViewById(R.id.searchDeviceLayout);
		txtWifiName = (TextView) findViewById(R.id.txtWifiName);
		inviteInstall = (TextView) findViewById(R.id.txtInviteInstall);
		relNetWorkStateShown = (RelativeLayout) findViewById(R.id.networkStateShow);
	}

	private void showNetWorkState(){
		if(relNetWorkStateShown.getVisibility() == View.GONE)
			relNetWorkStateShown.setVisibility(View.VISIBLE);
		     setNetWorkState();
	}
	
	private void hideNetWorkState(){
		if(relNetWorkStateShown.getVisibility() == View.VISIBLE)
			relNetWorkStateShown.setVisibility(View.GONE);
	}
	
	private void setNetWorkState() {
		if (NetUtils.isWifiConnected(this) && NetUtils.getWifiBSSID(this).contains(WifiConfig.PISEN_BSSID_PREFIX)) {
			txtWifiName.setText(NetUtils.getWifiSSID(this));
		}
	}

	WifiSSIDCallback callBack = new WifiSSIDCallback() {
		@Override
		public void networkChange(WifiConfig config, boolean connected) {
			if (!connected) {
				txtWifiName.setText("暂无连接");
			} 
			else if (config.isPisenWifi()) {
				txtWifiName.setText(config.getSSID());
			}
		}
	};
	
	private void initView() {
		transferNetUtil = FlashTransferNetUtil.getInstance(this);
//		transferNetUtil = new FlashTransferNetUtil(this);
		wifiManager = (WifiManager) getSystemService(Service.WIFI_SERVICE);

		createApLayout.setMax(100 * PROGRESS_WEIGHT);
		createApButton.setOnClickListener(this);
		searchButton.setOnClickListener(this);
		infoButton.setOnClickListener(this);
		closeButton.setOnClickListener(this);
		refreshButton.setOnClickListener(this);
		searchDeviceView.setOnDeviceSelectListener(this);
		inviteInstall.setOnClickListener(this);

		createApShowAnimListener = new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				createApLayout.showCreateAp();
				createApLayout.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				createAp();
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
			}
		};

		searchDeviceShowAnimListener = new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
				searchDeviceView.resetView();
				searchDeviceView.setVisibility(View.VISIBLE);
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				searchDeviceView.startScan();
				scanUseableAp();
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
			}
		};
		
		WifiSSIDMonitor.getInstance().registerObserver(callBack);
	}

	/**
	 * 执行创建ap动画
	 */
	private void startCreateApProgress(int what) {
		switch (what) {
		case WHAT_OPENAP_START:
			if (progress <= PROGRESS_MAX_CREATE) {
				handler.sendEmptyMessageDelayed(what, 90);
			}
			break;
		case WHAT_OPENAP_RESULT_SUCCEED_NO_SSID:
				handler.sendEmptyMessageDelayed(what, 500);
			break;
		case WHAT_OPENAP_RESULT_SUCCEED:
			if (progress <= 100 * PROGRESS_WEIGHT) {
				handler.sendEmptyMessageDelayed(what, 3);
			} else {
				// 等待设备连接
				handler.sendEmptyMessageDelayed(WHAT_AP_NO_USER_CONNECT, TIME_WAIT_CONNECT);
				handler.postDelayed(new Runnable() {
					
					@Override
					public void run() {
						beginScanWlanDevice();
					}
				}, 2000);
			}
			break;

		default:
			break;
		}
	}
	
	/**
	 * 开启手机热点
	 */
	private void createAp() {
		progress = 0;

		handler.sendEmptyMessage(WHAT_OPENAP_START);
		transferNetUtil.refreshApConfigure();
		boolean r = transferNetUtil.openAp();

		sendCreateResultMessage(r);
	}

	/**
	 * 发送开启AP结果消息
	 * @param r
	 */
	private void sendCreateResultMessage(boolean r) {
		if (r) {
			handler.sendEmptyMessageDelayed(WHAT_OPENAP_RESULT_SUCCEED, 3*1000);// 等待3s发送结果消息
		} else {
			handler.sendEmptyMessageDelayed(WHAT_OPENAP_RESULT_FAILED, 2*1000);
		}
	}

	/**
	 * 扫描可用互传热点,分两步执行，首先扫描合法互传网络名称，在进行已入网设备扫描
	 */
	private void scanUseableAp() {
		//清除以前数据
		DeviceContainer.getInstance(getApplicationContext()).reset();
		//关闭热点
		transferNetUtil.closeAp();
		
		transferNetUtil.setScanFlashApListener(new ScanFlashApListener() {

			@Override
			public void scanSuccess(List<ScanResult> result) {
				if (result != null && !result.isEmpty()) {
					List<TransferDevice> data = formatScanResult(result, getCurJoinSsid());
					if(data != null && !data.isEmpty()) {
						searchDeviceView.stopScan();
						searchDeviceView.addDevice(data);
					}
				}

				// 已入网设备扫描
				beginScanWlanDevice();
			}

			@Override
			public void scanFailed(String msg) {
				runOnUiThread(new Runnable() {
					public void run() {
						showSearchFailedView();
					}
				});
			}
		});

		// 扫描合法互传ap
		transferNetUtil.scanFlashTransferAp();
	}

	/**
	 * 格式化ScanResult为TransferDevice
	 */
	private List<TransferDevice> formatScanResult(List<ScanResult> data, String curJoinedSsid) {
		List<TransferDevice> result = null;
		if (data != null && !data.isEmpty()) {
			result = new ArrayList<SearchDeviceLayout.TransferDevice>();

			ScanResult tmp = null;
			int size = data.size();
			for (int i = 0; i < size; i++) {
				tmp = data.get(i);
				if(!curJoinedSsid.contains(tmp.SSID)) {//过滤掉已加入ap网络
					TransferDevice td = new TransferDevice();
					td.isGateway = true;
					td.ssid = tmp.SSID;
					String[] gatewayInfos = tmp.SSID.split("_");
					//设备名称
					if(gatewayInfos.length >1) {
						td.deviceName = gatewayInfos[1];
					}
					
					//设备类型，热点创建者只会为android设备，格式化该数据，把头像信息放入type内
					if(gatewayInfos.length >2) {
						td.deviceType = String.format("%s_%s", FlashTransferConfig.PHONE_TYPE_ANDROID, gatewayInfos[2]);
					}
					
					result.add(td);
				}
			}
		}
		return result;
	}

	/**
	 * 扫描局域网设备
	 */
	private void beginScanWlanDevice() {
		if(DEBUG) Log.e(TAG, "beginScanWlanDevice");
		if (wifiManager.isWifiEnabled() || transferNetUtil.isWifiApEnabled()) {
			try {
				handler.sendEmptyMessageDelayed(WHAT_WLAN_SCAN_TIMEOUT, 20 * 1000);
				if(conn == null || deviceScanService == null) {
					if(DEBUG) Log.e(TAG, "conn is null, bind service");
					bindService();
				} else {
					boolean result = deviceScanService.scanDevice();
					if(!result) {
						handler.removeMessages(WHAT_WLAN_SCAN_TIMEOUT);
						showSearchFailedView();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}else {
			UIHelper.showToast(this, "无网络");
		}
	}
	
	private void bindService() {
		Intent in = new Intent(this, DeviceScanService.class);
		//开启服务，在需要关闭时调用stopService即可
		getApplicationContext().startService(in);
		
		conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				DeviceScanBinder binder = (DeviceScanBinder) service;
				deviceScanService = binder.getService();
				deviceScanService.registOnScanResultCallback(ConnectManageActivity.this);
				if(!deviceScanService.scanDevice()) {//网络非正常，或正在连接等
					//XXX 需区分是创建ap后搜索还是直接搜索
					handler.removeMessages(WHAT_WLAN_SCAN_TIMEOUT);
					showSearchFailedView();
				}
			}
		};
		bindService(in, conn, Service.BIND_AUTO_CREATE);
	}

	/**
	 * UserInfoPtl转TransferDevice
	 */
	private TransferDevice formateUserInfoPtl(UserInfoPtlV2 user, String ssid) {
		TransferDevice td = null;
		if (user != null) {
			td = new TransferDevice();
			td.deviceName = user.hostName;
			td.deviceType = user.hostType;
			td.ip = user.hostIp;
			td.ssid = ssid;
			td.isGateway = false;
		}
		return td;
	}

	private void showDefaultView() {
		curStatus = STATUS_DEFAULT;
		if(DEBUG) Log.e(TAG, "===showDefaultView==");
		
		createApLayout.setVisibility(View.GONE);
		
		searchDeviceView.setVisibility(View.GONE);
		refreshButton.setVisibility(View.GONE);
		
		createApButton.setVisibility(View.VISIBLE);
		searchButton.setVisibility(View.VISIBLE);
		infoButton.setVisibility(View.VISIBLE);
		createTipView.setVisibility(View.VISIBLE);
		searchTipView.setVisibility(View.VISIBLE);
	}

	/**
	 * 显示ap创建
	 */
	private void showCreateApView() {
		curStatus = STATUS_CREATE;
		if(DEBUG) Log.e(TAG, "===showCreateApView==");
		createApButton.setVisibility(View.GONE);
		searchButton.setVisibility(View.GONE);
		infoButton.setVisibility(View.GONE);
		createTipView.setVisibility(View.GONE);
		searchTipView.setVisibility(View.GONE);

		AnimatorSet set = new AnimatorSet();
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(createApLayout, "scaleX", 0.5f, 1.2f).setDuration(300);
		scaleX.setInterpolator(new LinearInterpolator());
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(createApLayout, "scaleY", 0.5f, 1.2f).setDuration(300);
		scaleY.setInterpolator(new LinearInterpolator());

		ObjectAnimator scaleXReverse = ObjectAnimator.ofFloat(createApLayout, "scaleX", 1.2f, 1f).setDuration(200);
		scaleXReverse.setInterpolator(new LinearInterpolator());
		ObjectAnimator scaleYReverse = ObjectAnimator.ofFloat(createApLayout, "scaleY", 1.2f, 1f).setDuration(200);
		scaleYReverse.setInterpolator(new LinearInterpolator());
		set.play(scaleX).with(scaleY);
		set.play(scaleXReverse).with(scaleYReverse).after(scaleX);
		set.addListener(createApShowAnimListener);
		set.start();
	}

	/**
	 * 显示ap创建失败视图
	 */
	private void showCreateApFailedView() {
		createApLayout.showTip("创建连接失败");
	}
	
	/**
	 * 显示ap无客户端连接视图
	 */
	private void showApNoConnectionView() {
		createApLayout.showNoClientConnect();
	}
	
	/**
	 * 显示ap客户端已连接视图
	 */
	private void showApConnectedView(String hostName) {
		createApLayout.showTip(hostName + "连接成功");
	}

	/**
	 * 显示搜索加入
	 */
	private void showSearch() {
		curStatus = STATUS_SEARCH;

		transferNetUtil.closeAp();
		refreshButton.setVisibility(View.GONE);
		createApButton.setVisibility(View.GONE);
		searchButton.setVisibility(View.GONE);
		infoButton.setVisibility(View.GONE);
		createTipView.setVisibility(View.GONE);
		searchTipView.setVisibility(View.GONE);
		AnimatorSet set = new AnimatorSet();
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(searchDeviceView, "scaleX", 0.5f, 1.2f).setDuration(300);
		scaleX.setInterpolator(new LinearInterpolator());
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(searchDeviceView, "scaleY", 0.5f, 1.2f).setDuration(300);
		scaleY.setInterpolator(new LinearInterpolator());

		ObjectAnimator scaleXReverse = ObjectAnimator.ofFloat(searchDeviceView, "scaleX", 1.2f, 1f).setDuration(200);
		scaleXReverse.setInterpolator(new LinearInterpolator());
		ObjectAnimator scaleYReverse = ObjectAnimator.ofFloat(searchDeviceView, "scaleY", 1.2f, 1f).setDuration(200);
		scaleYReverse.setInterpolator(new LinearInterpolator());
		set.play(scaleX).with(scaleY);
		set.play(scaleXReverse).with(scaleYReverse).after(scaleX);
		set.addListener(searchDeviceShowAnimListener);
		set.start();
	}

	/**
	 * 显示扫描失败视图
	 */
	private void showSearchFailedView() {
		if(searchDeviceView.getVisibility() == View.VISIBLE && searchDeviceView.getDeviceCount() <=0) {
			searchDeviceView.stopScan();
			if(deviceScanService != null) deviceScanService.stopScanDevice();
			searchDeviceView.showTips("没找到可连接的人");
			refreshButton.setVisibility(View.VISIBLE);
			
			handler.removeMessages(WHAT_WLAN_SCAN_TIMEOUT);
		}
	}
	
	/**
	 * 显示连接设备失败视图
	 */
	private void showConnectDeviceFaildView() {
		searchDeviceView.setVisibility(View.VISIBLE);
		createApLayout.setVisibility(View.GONE);
		
		searchDeviceView.stopScan();
		searchDeviceView.showTips("连接设备失败，请重试");
		refreshButton.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 显示正在连接选择的设备视图
	 */
	private void showConnectingDeviceView(TransferDevice device) {
		searchDeviceView.stopScan();
		waitConnectToHost = true;
		
		searchDeviceView.setVisibility(View.GONE);
		createApLayout.setVisibility(View.VISIBLE);
		createApLayout.showConnectingDevice(device.deviceName, device.getIconResourceId());
		createApLayout.autoIncreaseProgress(0, 70, 29*1000);
		
		handler.sendEmptyMessageDelayed(WHAT_CONNECT_AP_FAILED, 30 * 1000);//30s没连接成功，则显示连接失败视图
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			close();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * @describtion  关闭互传
	 */
	private void close() {
		switch (curStatus) {
		case STATUS_DEFAULT:
			handler.removeMessages(WHAT_AP_NO_USER_CONNECT);
			setResult(RESULT_OK);
			finish();
			break;
		case STATUS_CREATE: // 创建ap
			transferNetUtil.closeAp();
			if(deviceScanService != null) deviceScanService.stopScanDevice();
			handler.sendEmptyMessage(WHAT_OPENAP_CANCEL);
			break;
		case STATUS_SEARCH:// 搜索加入
			handler.removeMessages(WHAT_WLAN_SCAN_TIMEOUT);
			handler.removeMessages(WHAT_CONNECT_AP_FAILED);
			waitConnectToHost = false;
			//停止扫描服务
			if(deviceScanService != null) deviceScanService.stopScanDevice();
			searchDeviceView.stopScan();
			showDefaultView();
			break;

		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		switch (id) {
		case R.id.imgCreateAp: // 创建热点
			showCreateApView();
			showNetWorkState();
			break;
		case R.id.imgSearchAp: // 搜索加入
			showSearch();
			showNetWorkState();
			break;
		case R.id.imgClose: //关闭互传
			close();
			hideNetWorkState();
			break;
		case R.id.imgInfo:
			FragmentActivity.startFragment(this, FindHelpFragment.class);
			break;
		case R.id.txtInviteInstall:
			FragmentActivity.startFragment(this, InvitedInstall.class);
			break;
		case R.id.ibtnRetry:
			showSearch();
			break;
		default:
			break;
		}
	}

	@Override
	public void selected(TransferDevice device) {
		handler.removeMessages(WHAT_WLAN_SCAN_TIMEOUT);
		if (wifiManager.isWifiEnabled() && wifiManager.getConnectionInfo().getSSID().contains(device.ssid)) {
			handleConnectSucceed();
		}else {
			// 连接网络
			boolean result = transferNetUtil.connectAp(device.ssid, null);
			if (result) {
				showConnectingDeviceView(device);
			}else {
				showConnectDeviceFaildView();
			}
		}
	}

	@Override
	public void online(UserInfoPtlV2 user) {
		Log.e(TAG, "online user->" + user.hostName);
		handler.removeMessages(WHAT_WLAN_SCAN_TIMEOUT);
		//ap主机，扫描到用户，则关闭activity，并返回
		if(transferNetUtil.isWifiApEnabled()) {	//热点host搜索到设备
			showApConnectedView(user.hostName);
			//延迟关闭界面
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					handleConnectSucceed();
				}
			}, 500);
		}else if(waitConnectToHost) {	//切换网络后搜索到设备
			//移除连接失败msg
			handler.removeMessages(WHAT_CONNECT_AP_FAILED);
			waitConnectToHost = false;
			//清除以前数据
			DeviceContainer.getInstance(getApplicationContext()).reset();
			DeviceContainer.getInstance(getApplicationContext()).addDevice(user);
			handleConnectSucceed();
		}else {
			searchDeviceView.stopScan();
			searchDeviceView.addDevice(formateUserInfoPtl(user, getCurJoinSsid()));
		}
	}
	
	private String getCurJoinSsid() {
		if(wifiManager.isWifiEnabled()) {
			WifiInfo info = wifiManager.getConnectionInfo();
			return info.getSSID();
		}
		
		return null;
	}

	/**
	 * 连接成功处理
	 */
	private void handleConnectSucceed() {
		playConnectedSound();
		requestConnect();
		isConnected = true;
		showDefaultView();
		close();
	}

	/**
	 * 播放连接成功音效
	 */
	private void playConnectedSound() {
		AudioManager mgr = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);  
		float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_SYSTEM);  
		float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);      
		float volume = streamVolumeCurrent/streamVolumeMax;  
		pool.play(soundId, volume, volume, 1, 0, 1.0f);
	}

	/**
	 * 向ios设备发送请求连接请求
	 */
	private void requestConnect() {
		final List<UserInfoPtlV2> devices = DeviceContainer.getInstance(getApplicationContext()).getUserList();
		if(devices != null && !devices.isEmpty() && deviceScanService != null) {
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					for(UserInfoPtlV2 u : devices) {
						if(u.hostType.startsWith(FlashTransferConfig.PHONE_TYPE_IOS)) {
							deviceScanService.requestConnect(u.hostIp);
						}
					}
					
				}
			}).start();
		}
	}

	@Override
	public void offline(UserInfoPtlV2 user) {
		searchDeviceView.removeDevice(formateUserInfoPtl(user, getCurJoinSsid()));
	}
	
	private void registReceiver() {
		IntentFilter in = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		connectChangedReceiver = new ConnectChangedReceiver();
		registerReceiver(connectChangedReceiver, in);
	}

	private void unregisterReceiver() {
		unregisterReceiver(connectChangedReceiver);
	}
	
	private class ConnectChangedReceiver extends BroadcastReceiver {
		private ConnectivityManager cm;

		public ConnectChangedReceiver() {
			cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			NetworkInfo ni = cm.getActiveNetworkInfo();
			if (waitConnectToHost && ni != null &&  ni.isConnectedOrConnecting()) {
				if(DEBUG) Log.e(TAG, "===waitConnectToHost and scanDevice===");
				deviceScanService.scanDevice();
			} 
		}
	}
}
