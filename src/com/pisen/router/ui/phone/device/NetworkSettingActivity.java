package com.pisen.router.ui.phone.device;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.studio.os.LogCat;
import android.studio.os.NetUtils;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.config.ResourceConfig;
import com.pisen.router.core.device.AbstractDevice;
import com.pisen.router.core.device.AbstractDevice.OnLoginTimeoutCallback;
import com.pisen.router.core.monitor.entity.RouterConfig.Model;
import com.pisen.router.ui.base.NavigationBarActivity;
import com.pisen.router.ui.phone.device.bean.RelayConfBean;

/**
 * 联网设置
 * @author Liuhc
 * @version 1.0 2015年5月8日 下午3:58:21
 */
public class NetworkSettingActivity extends NavigationBarActivity implements OnClickListener, OnLoginTimeoutCallback {

	private TextView rbWirelessConn;
	private TextView rbWireledConn;
	private RelayConfBean config;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_device_network);
		setTitle("联网设置");

		rbWirelessConn = (TextView) findViewById(R.id.rbWirelessConn);
		rbWireledConn = (TextView) findViewById(R.id.rbWireledConn);
		rbWirelessConn.setOnClickListener(this);
		rbWireledConn.setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (NetUtils.isWifiConnected(this)) {
			showProgressDialog("加载中...");
		}
		getRouterStatus();
	}

	@Override
	public void onClick(View v) {
		if (!AbstractDevice.getInstance().isLogin(NetworkSettingActivity.this)) {
			return;
		}

		switch (v.getId()) {
		case R.id.rbWirelessConn: //无线中继
			Intent i = new Intent(NetworkSettingActivity.this, WirelessRelayActivity.class);
			startActivity(i);
			break;
		case R.id.rbWireledConn: //有线连接
			Intent in = new Intent(NetworkSettingActivity.this, WiredConnSettingActivity.class);
			in.putExtra("config", config);
			startActivity(in);
			break;
		default:
			break;
		}
	}

	/**
	 * @des 获取路由状态　
	 */
	public void getRouterStatus() {
		if (!NetUtils.isWifiConnected(NetworkSettingActivity.this)) {
			UIHelper.showToast(NetworkSettingActivity.this, "网络不给力");
			return;
		}
		
		LogCat.e("=====获取路由状态======");
		if (!AbstractDevice.getInstance().isLogin(this)) {
			return;
		} else {
			new GetWiredConfigAsyncTask().execute();
		}
	}

	/**
	 * @desc{获取有线配置异步任务}
	 */
	private class GetWiredConfigAsyncTask extends AsyncTask<String, Void, RelayConfBean> {
		@Override
		protected void onPreExecute() {
			showProgressDialog("加载中...");
		}

		@Override
		protected RelayConfBean doInBackground(String... params) {
			AbstractDevice device = AbstractDevice.getInstance();
			device.setOnLoginTimeoutCallback(NetworkSettingActivity.this); //超时跳入login界面
//			穿墙王.判断路由以何种形式 上网
			return device.getRelayConfig();  
		}

		@Override
		protected void onPostExecute(RelayConfBean result) {
			dismissProgressDialog();
			if (result != null) {
				config = result;
				setWifiStatus(result);
			} else {
				UIHelper.showToast(NetworkSettingActivity.this, "获取有线配置信息失败");
			}
		}
	}

	/**
	 * @describtion  配置网络状态　
	 * @param config
	 */
	private void setWifiStatus(RelayConfBean config) {
		// 无线连接
		if (config.sta != null) {
			LogCat.e("无线网络状态:" + config.sta.getNet_state());
			LogCat.e("无线网络SSId:" + config.sta.getSsid());

			if ("1".equalsIgnoreCase(config.sta.getNet_state()) && !TextUtils.isEmpty(config.sta.getSsid())) {
				rbWirelessConn.setText("已中继" + config.sta.getSsid());
			} else {
				rbWirelessConn.setText("暂未中继网络");
			}
		} else {
			rbWirelessConn.setText("数据异常");
		}

		// 有线连接
		if (config.wan != null) {
			if ("connect".equalsIgnoreCase(config.wan.getPhysics_state())) { //路由器返回数据
				if ("dhcp".equals(config.wan.getProto())) {
					rbWireledConn.setText("自动获取");
				} else if ("static".equals(config.wan.getProto())) {
					rbWireledConn.setText("静态IP");
				} else if ("pppoe".equals(config.wan.getProto())) {
					rbWireledConn.setText("拨号上网");
				}
			} else {
				rbWireledConn.setText("网线未接入");
			}
		} else {
			rbWireledConn.setText("数据异常");
		}
	}

	@Override
	public void onLoginTimeout() {
		startActivity(new Intent(NetworkSettingActivity.this, LoginActivity.class));
	}
	
}
