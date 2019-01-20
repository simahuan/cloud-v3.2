package com.pisen.router.ui.phone.device;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.studio.os.LogCat;
import android.studio.view.widget.BaseAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.pisen.router.config.Config;
import com.pisen.router.core.device.AbstractDevice;
import com.pisen.router.ui.base.NavigationBarActivity;
import com.pisen.router.ui.phone.device.bean.WifiBean;

/**
 * 无线中继扫描
 * @author Liuhc
 * @version 1.0 2015年5月11日 下午3:08:58
 */
public class WirelessRelayActivity extends NavigationBarActivity implements OnItemClickListener {

	private PullToRefreshListView lstWireless;
	private LinearLayout headLstLayout;
	private WirelessAdapter adapter;
	private WifiConnectUtils wifiHelper;

	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_device_wireless);
		setTitle("无线中继");

		headLstLayout = (LinearLayout) findViewById(R.id.headLstLayout);
		headLstLayout.setVisibility(View.GONE);
		lstWireless = (PullToRefreshListView) findViewById(R.id.lstWireless);
		lstWireless.setOnItemClickListener(this);
		lstWireless.setAdapter(adapter = new WirelessAdapter(this));
		lstWireless.setMode(Mode.PULL_DOWN_TO_REFRESH);
		lstWireless.setOnRefreshListener(new OnRefreshListener2() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase refreshView) {
				LogCat.e("onPullDownToRefresh...");
				if (!NetUtil.isWifiConnected(WirelessRelayActivity.this)) {
					handler.sendEmptyMessageDelayed(WifiConnectUtils.WIFI_OPEN_WIFI_FAILED, 1000);
					lstWireless.onRefreshComplete();
					adapter.setData(new ArrayList<WifiBean>());
					return;
				}
				new GetWirelessListAsyncTask().execute("");
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase refreshView) {
				LogCat.e("onPullUpToRefresh...");
				new GetWirelessListAsyncTask().execute("");
			}
		});

		wifiHelper = new WifiConnectUtils(this);
		wifiHelper.setHandler(handler);

//		 router = (RouterBean) getIntent().getSerializableExtra("router");
		showProgressDialog("加载中...");
		new GetWirelessListAsyncTask().execute("");
	}

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case WifiConnectUtils.WIFI_CONNECTED:
				dismissProgressDialog();
				UIHelper.showToast(WirelessRelayActivity.this, "连接成功");
				
//				WifiBean wifi = GsonUtils.jsonDeserializer(Config.getWifiOutterConfig(), WifiBean.class);
//				setConnectedWifi(true, wifi);
					showProgressDialog("刷新中...");
					new GetWirelessListAsyncTask().execute("");
				break;
			case WifiConnectUtils.WIFI_PASSWORD_ERROR:
				dismissProgressDialog();
				UIHelper.showToast(WirelessRelayActivity.this, "连接失败,请尝试手动连接");
				break;
			case WifiConnectUtils.WIFI_CONNECT_FAILED:
				dismissProgressDialog();
				UIHelper.showToast(WirelessRelayActivity.this, "连接失败,请尝试手动连接");
				break;
			case WifiConnectUtils.WIFI_CONNCTING:
					showProgressDialog("WIFI连接-等待重新连接");
				break;
			case WifiConnectUtils.WIFI_DEVICE_RESTART:
					showProgressDialog("设备重启-等待重新连接");
				break;
			case WifiConnectUtils.WIFI_ERROR:
				dismissProgressDialog();
				UIHelper.showToast(WirelessRelayActivity.this, "连接出错");
				break;
			case WifiConnectUtils.WIFI_CONNECTED_TIMEOUT:
				dismissProgressDialog();
				UIHelper.showToast(WirelessRelayActivity.this, "连接超时,请尝试手动连接");
				break;
			case WifiConnectUtils.WIFI_OPEN_WIFI_FAILED:
				dismissProgressDialog();
				UIHelper.showToast(WirelessRelayActivity.this, "请先开启WIFI");
				break;
			default:
				break;
			}
		}
	};

	/**
	 * @desc  设置wifi连接
	 * @param isVisible
	 * @param wifi
	 */
	private void setConnectedWifi(boolean isVisible, WifiBean wifi) {
		if (wifi == null || TextUtils.isEmpty(wifi.getSsid()) || isVisible == false) {
			headLstLayout.setVisibility(View.GONE);
		} else {
			headLstLayout.setVisibility(View.VISIBLE);
			TextView txtName = (TextView) headLstLayout.findViewById(R.id.txtName);
			ImageView single = (ImageView) headLstLayout.findViewById(R.id.ivWifiState);
			ImageView ivEncrypt = (ImageView) headLstLayout.findViewById(R.id.ivEncryptPwd);

			if (wifi != null) {
				// if (selectedWifi == null) {
				// selectedWifi = new WifiBean();
				// }
				// selectedWifi.setSsid(wifi.getSsid());
				// selectedWifi.setEncryption(wifi.getEncryption());

				if (txtName != null) {
					txtName.setText(wifi.getSsid());
				}

				String signal = wifi.getSignal();
				if (!TextUtils.isEmpty(signal) && single != null) {
					int sign = 0;
					try {
						sign = Integer.parseInt(signal);
					} catch (Exception e) {
					}
					if (sign >= 66) {
						single.setImageResource(R.drawable.equipment_wifi_best_conn);
					} else if (sign < 33) {
						single.setImageResource(R.drawable.equipment_wifi_general_conn);
					} else {
						single.setImageResource(R.drawable.equipment_wifi_good_conn);
					}
				}

				if (ivEncrypt != null) {
					if (TextUtils.isEmpty(wifi.getEncryption()) || "none".equals(wifi.getEncryption())) {
						ivEncrypt.setVisibility(View.GONE);
					} else {
						ivEncrypt.setVisibility(View.VISIBLE);
					}
				}
			}
			// else{
			// txtName.setText(wifi.getSsid());
			// }
		}
	}

	private class WirelessAdapter extends BaseAdapter<WifiBean> {

		WifiBean info = null;

		public WirelessAdapter(Context context) {
			super(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder vh = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(WirelessRelayActivity.this).inflate(R.layout.cloud_device_wireless_item, (ViewGroup)null);
				vh = new ViewHolder();
				vh.txtName = (TextView) convertView.findViewById(R.id.txtName);
				vh.txtDescrible = (TextView) convertView.findViewById(R.id.txtDescrible);
				vh.single = (ImageView) convertView.findViewById(R.id.ivWifiState);
				vh.ivEncrypt = (ImageView) convertView.findViewById(R.id.ivEncryptPwd);
				convertView.setTag(vh);
			} else {
				vh = (ViewHolder) convertView.getTag();
			}

			info = getItem(position);
			if (info != null) {
				vh.txtName.setText(info.getSsid());
				if (TextUtils.isEmpty(info.getEncryption()) || "none".equals(info.getEncryption().trim())) {
					vh.txtDescrible.setVisibility(View.GONE);
					vh.ivEncrypt.setVisibility(View.GONE);
				} else {
					String des = "通过" + info.getEncryption() + "保护";
					vh.txtDescrible.setText(des);
					vh.txtDescrible.setVisibility(View.VISIBLE);
					vh.ivEncrypt.setVisibility(View.VISIBLE);
				}

				int sign = 0;
				try {
					sign = Integer.parseInt(info.getSignal());
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (sign >= 66) {
					vh.single.setImageResource(R.drawable.equipment_wifi_best);
				} else if (sign < 33) {
					vh.single.setImageResource(R.drawable.equipment_wifi_general);
				} else {
					vh.single.setImageResource(R.drawable.equipment_wifi_good);
				}
			}
			return convertView;
		}

		class ViewHolder {
			TextView txtName;
			TextView txtDescrible;
			ImageView single, ivEncrypt;
		}
	}

	@Override
	public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {
		selectedWifi = (WifiBean) parent.getItemAtPosition(position);
		if (TextUtils.isEmpty(selectedWifi.getEncryption()) || "none".equals(selectedWifi.getEncryption().trim())) {
			selectedWifi.setKey("");
			new SetWirelessAsyncTask().execute();
			return;
		}
		InputDialog dialog = new InputDialog(WirelessRelayActivity.this);
		dialog.setTitle("输入密码");
		dialog.setMessage("请输入" + selectedWifi.getSsid() + "的密码(至少8位)");
		dialog.show();
		dialog.setOnClickListener(new OnClickListener() {
			@Override
			public void onOk(DialogInterface dialog, String inputText) {
				if (!AbstractDevice.getInstance().isLogin(WirelessRelayActivity.this)) {
					return;
				}

				if (TextUtils.isEmpty(inputText) || inputText.length() < 8) {
					UIHelper.showToast(WirelessRelayActivity.this, "请输入至少8位数密码");
					return;
				}

				if (!NetUtil.isPasswordAvailable(inputText)) {
					UIHelper.showToast(WirelessRelayActivity.this, "密码格式不正确，请检查是否包含特殊符号");
					return;
				}

				if (!NetUtil.isWifiConnected(WirelessRelayActivity.this)) {
					UIHelper.showToast(WirelessRelayActivity.this, "网络不给力");
					return;
				}

				if (selectedWifi != null) {
					selectedWifi.setKey(inputText);
					new SetWirelessAsyncTask().execute(); //配置无线路由中继设备
				}
			}

			@Override
			public void onCancel(DialogInterface dialog) {
			}
		});
		dialog.show();
	}

	private WifiBean selectedWifi = null;
	private WifiBean connectedWifi = null;

	/**
	 * @desc{ 获取无线网络信号列表}
	 */
	private class GetWirelessListAsyncTask extends AsyncTask<String, Void, List<WifiBean>> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!AbstractDevice.getInstance().isLogin(WirelessRelayActivity.this)) {
				cancel(true);
				return;
			}
		}

		@Override
		protected List<WifiBean> doInBackground(String... params) {
			connectedWifi = AbstractDevice.getInstance().getWirelessedConfig();
			
			List<WifiBean> list = AbstractDevice.getInstance().getRelayWifiList(); //路由设备扫描ssid
			if (connectedWifi != null && connectedWifi.isConnnect() && list != null) {
				for (WifiBean bean : list) {
					if (bean.getSsid().equals(connectedWifi.getSsid())) {
						bean.setConnnect(true);
						connectedWifi = bean;
						list.remove(bean);
						break;
					}
				}
			}
			return list;
		}

		@Override
		protected void onPostExecute(List<WifiBean> result) {
			super.onPostExecute(result);
			dismissProgressDialog();
			lstWireless.onRefreshComplete();
			if (result != null) {
				LogCat.e("中继列表数：" + result.size());
				if (connectedWifi != null) {
					if (connectedWifi.isConnnect()) {
						setConnectedWifi(true, connectedWifi);
					} else {
						setConnectedWifi(false, null);
					}
				}
				adapter.setData(result);
			} else {
				UIHelper.showToast(WirelessRelayActivity.this, "获取失败,请尝试下拉刷新");
			}
		}
	}

	
	/**
	 * @desc{配置无线路由信息列表}
	 */
	private class SetWirelessAsyncTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!AbstractDevice.getInstance().isLogin(WirelessRelayActivity.this)) {
				lstWireless.onRefreshComplete();
				cancel(true);
				return;
			}
			showProgressDialog("正在设置");
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			return AbstractDevice.getInstance().setRelayWifiConfig(selectedWifi.getSsid(), selectedWifi.getCharset(), selectedWifi.getEncryption(),
					selectedWifi.getKey(), selectedWifi.getChannel(), "0");
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				showProgressDialog("无线中继配置完成");
				Config.setWifiOutterConfig(GsonUtils.jsonSerializer(selectedWifi));
				setConnectedWifi(false, null); // 不显示头部信息

				String localCof = Config.getWifiConfig();
				if (!TextUtils.isEmpty(localCof)) {

					// String config = Config.getWifiConfig();
					final WifiBean localWifi = GsonUtils.jsonDeserializer(localCof, WifiBean.class);
					String key = localWifi.getKey();
					LogCat.e("localWifi.getKey = " + key);

					wifiHelper.addNetwork(true, localWifi.getSsid(), key == null ? Config.getDeviceMgrPassword() : key,
							selectedWifi.getEncryption());
				} else {
					dismissProgressDialog();
					LogCat.e("无法获取到本地WIFI信息");
				}
			} else {
				dismissProgressDialog();
				UIHelper.showToast(WirelessRelayActivity.this, "设置失败");
			}
		}
	}
}
