package com.pisen.router.ui.phone.device;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.studio.os.LogCat;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;

import com.google.gson.GsonUtils;
import com.pisen.router.R;
import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.config.Config;
import com.pisen.router.config.ResourceConfig;
import com.pisen.router.core.device.AbstractDevice;
import com.pisen.router.core.device.AbstractDevice.OnLoginTimeoutCallback;
import com.pisen.router.core.monitor.entity.RouterConfig.Model;
import com.pisen.router.ui.base.NavigationBarActivity;
import com.pisen.router.ui.phone.device.bean.WifiBean;

/**
 * WIFI设置
 * 
 * @author Liuhc
 * @version 1.0 2015年5月8日 下午3:58:21
 */
public class WifiSettingActivity extends NavigationBarActivity implements OnClickListener, OnLoginTimeoutCallback {

	private RadioButton rbWpa2,rbTogether,rb_nopwd;
	private EditText etWifiName,etWifiPwd;
	private Button btnCommit;
	private View vnopwd;
	private CheckBox cbVisible;
	private WifiBean wifi;
	private WifiConnectUtils wifiHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_device_wifi);
		setTitle("WIFI设置");
		
		etWifiName = (EditText) findViewById(R.id.etWifiName);
		etWifiPwd = (EditText) findViewById(R.id.etWifiPwd);
		etWifiName.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		cbVisible = (CheckBox) findViewById(R.id.cbVisible);
		cbVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					Config.setShowPassword(false);
					etWifiPwd.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
				}else{
					Config.setShowPassword(true);
					etWifiPwd.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				}
			}
		});
		
		if(Config.isShowPassword()){
			cbVisible.setChecked(false);
		}else{
			cbVisible.setChecked(true);
		}
		
		rbWpa2 = (RadioButton) findViewById(R.id.rb_wpa2);
		rbTogether = (RadioButton) findViewById(R.id.rb_together);
		rb_nopwd = (RadioButton) findViewById(R.id.rb_nopwd);
		vnopwd = findViewById(R.id.vnopwd);
		btnCommit = (Button) findViewById(R.id.btnCommit);
		btnCommit.setOnClickListener(this);
		
		wifiHelper = new WifiConnectUtils(this);
		wifiHelper.setHandler(handler);
		
		if (!Model.RZHIXIANG.equals(ResourceConfig.getInstance(this).getDeviceMode())) {
			rb_nopwd.setVisibility(View.VISIBLE);
			vnopwd.setVisibility(View.VISIBLE);
		}
	}
	
	protected void onResume() {
		super.onResume();
		new WifiConfigGetAsyncTask().execute("");
	};

	Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case WifiConnectUtils.WIFI_CONNECTED:
				dismissProgressDialog();
				UIHelper.showToast(WifiSettingActivity.this, "连接成功");
				break;
			case WifiConnectUtils.WIFI_CONNECT_FAILED:
				dismissProgressDialog();
				UIHelper.showToast(WifiSettingActivity.this, "连接失败");
				break;
			case WifiConnectUtils.WIFI_PASSWORD_ERROR:
				dismissProgressDialog();
				UIHelper.showToast(WifiSettingActivity.this, "密码错误");
				break;
			case WifiConnectUtils.WIFI_CONNCTING:
				showProgressDialog("等待重新连接");
				break;
			case WifiConnectUtils.WIFI_DEVICE_RESTART:
				showProgressDialog("等待重新连接");
				break;
			case WifiConnectUtils.WIFI_ERROR:
				dismissProgressDialog();
				UIHelper.showToast(WifiSettingActivity.this, "连接出错");
				break;
			case WifiConnectUtils.WIFI_CONNECTED_TIMEOUT:
				dismissProgressDialog();
				UIHelper.showToast(WifiSettingActivity.this, "连接超时,请尝试手动连接");
				break;
			case WifiConnectUtils.WIFI_OPEN_WIFI_FAILED:
				dismissProgressDialog();
				UIHelper.showToast(WifiSettingActivity.this, "请先开启WIFI");
				break;
			default:
				break;
			}
			return true;
		}
	});
	
	private void initViewStatus(){
		if (wifi != null) {
			etWifiName.setText(wifi.getSsid());
			etWifiPwd.setText(wifi.getKey());
			
			if (!Model.RZHIXIANG.equals(ResourceConfig.getInstance(this).getDeviceMode())) {
				//　需要转换
				if ("NONE".equals(wifi.getEncryption()) || "none".equals(wifi.getKey())) {
					rb_nopwd.setChecked(true);
					etWifiPwd.setText("无密码");
					wifi.setEncryption("NONE");
					wifi.setKey("none");
				}else{
					if ("WPA2".equals(wifi.getEncryption())){
						rbWpa2.setChecked(true);
					}else if ("WPA/WPA2".equals(wifi.getEncryption())){
						rbTogether.setChecked(true);
					}else{
						rb_nopwd.setChecked(true);
					}
				}
			} else {
				if ( wifi.getEncryption().equals("psk2") ){
					rbWpa2.setChecked(true);
				}else{
					rbTogether.setChecked(true);
				}
			}
			
			Config.setWifiConfig(GsonUtils.jsonSerializer(wifi));
		}
	}
	
	String name,pwd,encryption;
	@Override
	public void onClick(View v) {
		if (!AbstractDevice.getInstance().isLogin(WifiSettingActivity.this)) {
			return;
		}
		
		name = etWifiName.getText().toString();
		pwd = etWifiPwd.getText().toString();
		if (TextUtils.isEmpty(name)) {
			UIHelper.showToast(WifiSettingActivity.this, "请输入WIFI名称");
			return;
		}
		
//		if (name.contains(" ")) {
//			UIHelper.showToast(WifiSettingActivity.this, "WIFI名称不能包含空格");
//			return;
//		}
		
		
		if (name.length() > 32 || name.length() < 1) {
			UIHelper.showToast(WifiSettingActivity.this, "WIFI名称的长度需要小于32个字节");
			return;
		}
		
		if (!rb_nopwd.isChecked()) {
			if (TextUtils.isEmpty(pwd)) {
				UIHelper.showToast(WifiSettingActivity.this, "请输入WIFI密码");
				return;
			}
			if (pwd.length() <8 || pwd.length() >=64) {
				UIHelper.showToast(WifiSettingActivity.this, "密码长度不正确");
				return;
			}
			if (!NetUtil.isPasswordAvailable(pwd)) {
				UIHelper.showToast(WifiSettingActivity.this, "密码格式不正确，请检查是否包含特殊符号");
				return;
			}
		}
		
		
		if (!Model.RZHIXIANG.equals(ResourceConfig.getInstance(this).getDeviceMode())) {
			if (rbTogether.isChecked()) {
				encryption = "WPA/WPA2";
			}else if(rbWpa2.isChecked()){
				encryption = "WPA2";
			}else{
				encryption = "NONE";
				pwd = "none";
			}
		}else{
			if (rbTogether.isChecked()) {
				encryption = "mixed-psk";
			}else {
				encryption = "psk2";
			}
		}
		
		if (name.equals(wifi.getSsid()) 
				&& encryption.equals(wifi.getEncryption())) {
			if (rb_nopwd.isChecked()) {
				UIHelper.showToast(WifiSettingActivity.this, "WIFI信息没有变化");
				return;
			}else{
				if (pwd.equals(wifi.getKey()) ) {
					UIHelper.showToast(WifiSettingActivity.this, "WIFI信息没有变化");
					return;
				}
			}
		}
		
		new WifiConfigSetAsyncTask().execute("");
	}
	
	
	private class WifiConfigGetAsyncTask extends AsyncTask<String,Void,WifiBean>{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgressDialog("加载中...");
		}
		@Override
		protected WifiBean doInBackground(String... params) {
			LogCat.e("wifiConfigGetAsynTask.....");
			AbstractDevice device = AbstractDevice.getInstance();
			device.setOnLoginTimeoutCallback(WifiSettingActivity.this);
			return device.getWifiConfig();
		}
		@Override
		protected void onPostExecute(WifiBean result) {
			super.onPostExecute(result);
			dismissProgressDialog();
			if (result != null) {
				wifi = result;
				initViewStatus();
			}
		}
	}
	
	private class WifiConfigSetAsyncTask extends AsyncTask<String,Void,Boolean>{
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!AbstractDevice.getInstance().isLogin(WifiSettingActivity.this)) {
				cancel(true);
				return;
			}
			showProgressDialog("正在设置");
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			WifiBean bean = null;
			try {
				bean = (WifiBean) wifi.clone();
				bean.setSsid(name);
				bean.setKey(pwd);
				bean.setEncryption(encryption);
				return AbstractDevice.getInstance().setWifiConfig(bean);
			} catch (CloneNotSupportedException e) {
				LogCat.e("不支持克垄");
				e.printStackTrace();
			}
			return false;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result) {
				showProgressDialog("设置成功");
				if (rb_nopwd.isChecked()) {
					etWifiPwd.setText("无密码");
				}
				wifi.setSsid(name);
				wifi.setKey(pwd);
				wifi.setEncryption(encryption);
				Config.setWifiConfig(GsonUtils.jsonSerializer(wifi));
				
				wifiHelper.addNetwork(true,wifi.getSsid(), wifi.getKey(), wifi.getEncryption());
			}else{
				dismissProgressDialog();
				UIHelper.showToast(WifiSettingActivity.this, "设置失败");
			}
		}
	}

	@Override
	public void onLoginTimeout() {
		startActivity(new Intent(WifiSettingActivity.this, LoginActivity.class));
	}
}
