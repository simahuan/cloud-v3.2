package com.pisen.router.ui.phone.device;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.studio.os.LogCat;
import android.studio.os.NetUtils;
import android.studio.view.widget.BaseAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.GsonUtils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.pisen.router.R;
import com.pisen.router.common.dialog.InputDialog;
import com.pisen.router.common.dialog.InputDialog.OnClickListener;
import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.common.utils.WifiSearcher;
import com.pisen.router.common.utils.WifiSearcher.ErrorType;
import com.pisen.router.common.utils.WifiSearcher.SearchWifiListener;
import com.pisen.router.config.Config;
import com.pisen.router.config.WifiConfig;
import com.pisen.router.core.monitor.WifiMonitor;
import com.pisen.router.core.monitor.WifiMonitor.WifiStateCallback;
import com.pisen.router.ui.base.NavigationBarActivity;
import com.pisen.router.ui.phone.device.bean.WifiBean;

/**
 * 设备扫描
 * 
 * @author Liuhc
 * @version 1.0 2015年5月11日 下午3:08:58
 */
public class DeviceListActivity extends NavigationBarActivity 
				implements WifiStateCallback,OnItemClickListener,android.view.View.OnClickListener {

	private PullToRefreshListView lstDevice;
	private DeviceAdapter adapter;
	private WifiConnectUtils wifiHelper;
	private WifiMonitor wifiMonitor;
	private WifiBean wifi;
	private ArrayList<WifiBean> result = new ArrayList<WifiBean>();
	private boolean isConnected;
	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_device_list);
		setTitle("您有可连接的设备");
		
		wifiMonitor = WifiMonitor.getInstance();
		wifiMonitor.registerObserver(this);
		
		lstDevice = (PullToRefreshListView) findViewById(R.id.lstDevice);
		lstDevice.setOnItemClickListener(this);
		lstDevice.setAdapter(adapter = new DeviceAdapter(this));
		lstDevice.setMode(Mode.PULL_DOWN_TO_REFRESH);
		lstDevice.setOnRefreshListener(new OnRefreshListener2() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				LogCat.e("onPullDownToRefresh...");
				searchDevices();
			}
			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {
				LogCat.e("onPullUpToRefresh...");
				searchDevices();
			}
		});
		lstDevice.setEmptyView(findViewById(R.id.errorLayout));
		findViewById(R.id.btnRefresh).setOnClickListener(this);
		
		wifiHelper = new WifiConnectUtils(this);
		wifiHelper.setHandler(handler);

		ArrayList<WifiBean> deviceList = new ArrayList<WifiBean>();
		deviceList = (ArrayList<WifiBean>) getIntent().getSerializableExtra("list");
		
		result.addAll(deviceList);
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshDevices();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler = null;
		if (wifiMonitor != null) {
			wifiMonitor.unregisterObserver(this);
		}
	}
	
	/**
	 * 刷新设备
	 * @param deviceList
	 */
	public void refreshDevices() {
		if (result == null || result.isEmpty()) {
			return;
		}

		String ssid = NetUtils.getWifiSSID(DeviceListActivity.this);
		List<WifiBean> dels = new ArrayList<WifiBean>();
		for (WifiBean bean:result) {
			if (!TextUtils.isEmpty(ssid) && ssid.contains(bean.getSsid())) {
				bean.setConnnect(true);
				isConnected = true;
				dels.add(bean);
			}else{
				bean.setConnnect(false);
			}
		}
		
		WifiBean wifiBean = null;
		if(dels.size() > 0){
			wifiBean = dels.get(0);
			result.remove(wifiBean);
			result.add(0, wifiBean);
			dels.remove(wifiBean);
			if(dels.size() > 0){
				result.removeAll(dels);
			}
		}
		
		if (adapter != null) {
			adapter.setData(result);
		}
	}

	/**
	 * 查找设备
	 */
	public void searchDevices() {
		WifiSearcher searcher = new WifiSearcher(DeviceListActivity.this, new SearchWifiListener() {
			@Override
			public void onSearchWifiSuccess(List<ScanResult> results) {
				if (lstDevice != null) {
					lstDevice.onRefreshComplete();
				}
				LogCat.i("wifi list:"+results.size());
				if (!results.isEmpty()) {
					result.clear();
					WifiBean wifi = null;
					for (ScanResult scanResult : results) {
						if (scanResult.BSSID.startsWith(WifiMonitor.PISEN_BSSID_PREFIX)) {
							wifi = new WifiBean();
							wifi.setBssid(scanResult.BSSID);
							wifi.setSsid(scanResult.SSID);
							wifi.setSignal(scanResult.level+"");
							String pwdType = scanResult.capabilities;
							if (pwdType.contains("WEP")) {
								wifi.setEncryption("WEP");
							}else if (pwdType.contains("WPA")) {
								wifi.setEncryption("WPA");
							}else if (pwdType.contains("WPA2")) {
								wifi.setEncryption("WPA2");
							}else{
								wifi.setEncryption("");
							}
							result.add(wifi);
						}
					}
					refreshDevices();
				}
			}
			
			@Override
			public void onSearchWifiFailed(ErrorType errorType) {
				if (lstDevice != null) {
					runOnUiThread(new Runnable() {
						public void run() {
							lstDevice.onRefreshComplete();
						}
					});
				}
			}
		});
		searcher.search();
	}
	
	private class DeviceAdapter extends BaseAdapter<WifiBean> {
		private WifiBean info = null;
		public DeviceAdapter(Context context) {
			super(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(DeviceListActivity.this).inflate(R.layout.cloud_device_list_item, null);
			TextView txtName = (TextView) convertView.findViewById(R.id.txtName);
			TextView txtConn = (TextView) convertView.findViewById(R.id.txtConn);
			ImageView single = (ImageView) convertView.findViewById(R.id.ivWifiState);
			ImageView ivEncrypt = (ImageView) convertView.findViewById(R.id.ivEncryptPwd);
			info = getItem(position);
			if (info != null) {
				String tag = info.getSsid();
				txtName.setText(tag);
				if (info.isConnnect() && NetUtils.isWifiConnected(getContext())) {
//					connectedPos = position;
					txtName.setTextColor(getContext().getResources().getColor(R.color.lightblue));
					txtConn.setVisibility(View.VISIBLE);
				} else {
					txtConn.setVisibility(View.GONE);
				}
				
				if (Integer.parseInt(info.getSignal()) >= (-50)) {
					single.setImageResource(R.drawable.equipment_wifi_best);
				}else if (Integer.parseInt(info.getSignal()) < (-70)) {
					single.setImageResource(R.drawable.equipment_wifi_general);
				}else{
					single.setImageResource(R.drawable.equipment_wifi_good);
				}
				
				if (TextUtils.isEmpty(info.getEncryption()) || "none".equals(info.getEncryption())) {
					ivEncrypt.setVisibility(View.GONE);
				}else{
					ivEncrypt.setVisibility(View.VISIBLE);
				}
			}
			return convertView;
		}

	}

	@Override
	public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
		TextView txtConn = (TextView) view.findViewById(R.id.txtConn);
		if (txtConn != null && txtConn.isShown()) {
			UIHelper.showToast(DeviceListActivity.this, "该设备已连接");
			return;
		}
		
		wifi = (WifiBean) parent.getItemAtPosition(position);
		int p = wifiHelper.findConfiguredNetworks(wifi.getSsid());//找到配置网络,已联接过.
		if(p > -1) {
			if(wifiHelper.connectConfiguration(p)){
				return;
			}
		}
//		LogCat.e("wifi.getEncryption="+wifi.getEncryption());
		if (TextUtils.isEmpty(wifi.getEncryption())) {
//			LogCat.e("============wifi.setKey()================");
			wifi.setKey("");
			connectDevice(wifi);
			return;
		}
		
		showInputPasswordDialog(position);
	}
	
	/**
	 * 显示密码输入框
	 * @param position
	 */
	private void showInputPasswordDialog(final int position) {
		InputDialog dialog = new InputDialog(DeviceListActivity.this);
		dialog.setTitle("输入密码");
		dialog.setMessage("请输入" + wifi.getSsid() + "的密码(至少8位)");
		dialog.show();
		dialog.setOnClickListener(new OnClickListener() {
			@Override
			public void onOk(DialogInterface dialog, String inputText) {
				if (TextUtils.isEmpty(inputText) || inputText.length() < 8) {
					UIHelper.showToast(DeviceListActivity.this, "请输入至少8位数密码");
					return;
				}
				if (!NetUtil.isPasswordAvailable(inputText)) {
					UIHelper.showToast(DeviceListActivity.this, "密码格式不正确，请检查是否包含特殊符号");
					return;
				}
				
				dialog.dismiss();
				isConnected = false;
				if (adapter.getCount() > position) {
					adapter.getItem(position).setConnnect(false);
				}
				handler.sendEmptyMessage(WifiConnectUtils.WIFI_REFRESH);
				wifi.setKey(inputText);
				connectDevice(wifi);
			}

			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();
			}
		});
	}

	private void connectDevice(final WifiBean wifi) {
		wifiHelper.connectNetwork(wifi.getSsid(), wifi.getKey(), wifi.getEncryption());
//		wifiHelper.setOnWifiStateChanged(new IWifiStateChangedListener() {
//			@Override
//			public void onStateChanged(WIFIState stateType) {
//				if (WIFIState.WIFI_CONNECT_SUCCESS.equals(stateType)) {
//					//连接成功
//					handler.sendEmptyMessage(WifiConnectUtils.WIFI_CONNECTED);
//				}else if (WIFIState.WIFI_ERROR_PASSWORD.equals(stateType)) {
//					//密码错误
//					handler.sendEmptyMessage(WifiConnectUtils.WIFI_PASSWORD_ERROR);
//				}else if (WIFIState.WIFI_CONNECT_FAILED.equals(stateType)) {
//					//链接失败
//					handler.sendEmptyMessage(WifiConnectUtils.WIFI_CONNECT_FAILED);
//				}else if (WIFIState.WIFI_ERROR_CANNOT_CONN.equals(stateType)) {
//					//此链接无法使用
//					dismissProgressDialog();
//					UIHelper.showToast(DeviceListActivity.this, "无法使用此链接");
//				}
//			}
//		});
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case WifiConnectUtils.WIFI_REFRESH:
				adapter.notifyDataSetChanged();
				break;
			case WifiConnectUtils.WIFI_CONNECTED:
				dismissProgressDialog();
				UIHelper.showToast(DeviceListActivity.this, "连接成功");
				Config.setWifiConfig(GsonUtils.jsonSerializer(wifi));
				//LeftMenuFragment.deviceName = wifi.getMode();
				//LeftMenuFragment.wifiSsid = wifi.getSsid();
				//刷新
				refreshDevices();
				break;
			case WifiConnectUtils.WIFI_CONNECT_FAILED:
				dismissProgressDialog();
				UIHelper.showToast(DeviceListActivity.this, "连接失败");
				break;
			case WifiConnectUtils.WIFI_PASSWORD_ERROR:
				dismissProgressDialog();
				UIHelper.showToast(DeviceListActivity.this, "密码错误");
				break;
			case WifiConnectUtils.WIFI_CONNCTING:
				showProgressDialog("正在连接");
				break;
			case WifiConnectUtils.WIFI_OPEN_WIFI:
				showProgressDialog("正在开启WIFI");
				break;
			case WifiConnectUtils.WIFI_ERROR:
				dismissProgressDialog();
				UIHelper.showToast(DeviceListActivity.this, "连接出错");
				break;
			case WifiConnectUtils.WIFI_CONNECTED_TIMEOUT:
				dismissProgressDialog();
				UIHelper.showToast(DeviceListActivity.this, "连接超时,请尝试手动连接");
				break;
			case WifiConnectUtils.WIFI_OPEN_WIFI_FAILED:
				dismissProgressDialog();
				UIHelper.showToast(DeviceListActivity.this, "请先开启WIFI");
				break;
			default:
				break;
			}
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRefresh:
			lstDevice.setRefreshing();
			break;
		default:
			break;
		}
		
	}

	@Override
	public void onConnected(WifiConfig config) {
		if (config.isPisenWifi()) {
			refreshDevices();
		}
	}

	@Override
	public void onDisconnected(WifiConfig config) {
		refreshDevices();
		isConnected = false;
	}
}
