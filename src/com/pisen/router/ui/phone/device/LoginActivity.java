package com.pisen.router.ui.phone.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.studio.os.NetUtils;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.pisen.router.R;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.config.Config;
import com.pisen.router.core.device.AbstractDevice;
import com.pisen.router.ui.base.CloudActivity;

/**
 * 固件升级
 * 
 * @author Liuhc
 * @version 1.0 2015年5月18日 上午9:55:18
 */
public class LoginActivity extends CloudActivity {

	public static final String ACTION_CLOSE_ACTIVITY = "ACTION_CLOSE_ACTIVITY";
	public static boolean isRunning = false;
	private Button btnDeviceLogin;
	private EditText etDeviceLoginPwd;
	private String password = "";
	private int id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_device_login);
		etDeviceLoginPwd = (EditText) findViewById(R.id.etDeviceLoginPwd);
		btnDeviceLogin = (Button) findViewById(R.id.btnDeviceLogin);
		btnDeviceLogin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				login();
			}
		});
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_CLOSE_ACTIVITY);
		registerReceiver(this.broadcastReceiver, filter);
		
		String pwd = Config.getDeviceMgrPassword();
		if (!TextUtils.isEmpty(pwd)) {
			etDeviceLoginPwd.setText(pwd);
		}
		
		id = getIntent().getIntExtra("id", -1);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK){
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		isRunning = true;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		isRunning = false;
		unregisterReceiver(broadcastReceiver);
	}
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			LoginActivity.this.finish();
		}
	};

	public void login() {
		if (!NetUtils.isWifiConnected(LoginActivity.this)) {
			UIHelper.showToast(LoginActivity.this, "网络不给力");
			return;
		}

		password = etDeviceLoginPwd.getText().toString().trim();
		if (TextUtils.isEmpty(password)) {
			UIHelper.showToast(LoginActivity.this, "密码不能为空");
			return;
		}

		new LoginAsyncTask().execute("");
	}

	private class LoginAsyncTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgressDialog("正在登录");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			return AbstractDevice.getInstance().login(password);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dismissProgressDialog();
			if (result) {
				Intent in = new Intent();
				in.putExtra("id", id);
				setResult(RESULT_OK, in);
				LoginActivity.this.finish();
			} else {
				UIHelper.showToast(LoginActivity.this, "登录失败");
			}
		}
	}

}
