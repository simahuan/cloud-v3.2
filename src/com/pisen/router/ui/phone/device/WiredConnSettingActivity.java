package com.pisen.router.ui.phone.device;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.pisen.router.R;
import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.config.Config;
import com.pisen.router.config.ResourceConfig;
import com.pisen.router.core.device.AbstractDevice;
import com.pisen.router.core.monitor.entity.RouterConfig.Model;
import com.pisen.router.ui.base.NavigationBarActivity;
import com.pisen.router.ui.phone.device.bean.RelayConfBean;
import com.pisen.router.ui.phone.device.bean.WanBean;

/**
 * 有线连接
 * 
 * @author Liuhc
 * @version 1.0 2015年5月11日 上午11:25:02
 */
public class WiredConnSettingActivity extends NavigationBarActivity implements OnCheckedChangeListener, OnClickListener {

	private RadioGroup rgHead;
	private RadioButton rbAuto, rbDial, rbStaticIp;
	private LinearLayout dialLayout;
	private LinearLayout staticIpLayout;
	private EditText etDialAccount, etDialPwd;
	private EditText etStaticIp, etStaticNetmask, etStaticGateway, etStaticDns, etStaticDns2;
	private Button btnWiredCommit;
	private CheckBox cbVisible;
	private WanBean wan;
	// 保存有线连接方式类型
	private WanBean wiredConnectType;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_device_wiredconn);
		setTitle("有线连接");

		findView();
		initView();
		RelayConfBean tmp = (RelayConfBean) getIntent().getSerializableExtra("config");
		if (tmp != null && tmp.wan != null) {
			wiredConnectType = tmp.wan;
			// 获取连接配置信息(有线连接状态 )
			new GetWanConfigAsyncTask().execute(wiredConnectType.getProto());
		} else {
			// 获取当前连接状态(去获取有线连接状态)
			new GetWiredConfigAsyncTask().execute("");
		}
	}

	private void initView() {
		cbVisible.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					etDialPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				} else {
					etDialPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				}
			}
		});

		btnWiredCommit.setOnClickListener(this);
	}

	private void findView() {
		rgHead = (RadioGroup) findViewById(R.id.rgHead);
		rgHead.setOnCheckedChangeListener(this);

		rbAuto = (RadioButton) findViewById(R.id.rbAuto);
		rbDial = (RadioButton) findViewById(R.id.rbDial);
		rbStaticIp = (RadioButton) findViewById(R.id.rbStaticIp);

		dialLayout = (LinearLayout) findViewById(R.id.dialLayout);
		staticIpLayout = (LinearLayout) findViewById(R.id.staticIpLayout);
		etDialAccount = (EditText) findViewById(R.id.etDialAccount);
		etDialPwd = (EditText) findViewById(R.id.etDialPwd);
		etDialAccount.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		etDialPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		cbVisible = (CheckBox) findViewById(R.id.cbVisible);

		etStaticIp = (EditText) findViewById(R.id.etStaticIp);
		etStaticNetmask = (EditText) findViewById(R.id.etStaticNetmask);
		etStaticGateway = (EditText) findViewById(R.id.etStaticGateway);
		etStaticDns = (EditText) findViewById(R.id.etStaticDns);
		etStaticDns2 = (EditText) findViewById(R.id.etStaticDns2);

		btnWiredCommit = (Button) findViewById(R.id.btnWiredCommit);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.rbAuto:
			setAutoWay(wan);
			break;
		case R.id.rbDial:
			setDialWay(wan);
			break;
		case R.id.rbStaticIp:
			if(Model.RZHIXIANG.equals(ResourceConfig.getInstance(this).getDeviceMode())
					) {
				setStaticWay(wan);
			} else {
				setStaticWay(wiredConnectType); //从前页面跳转过来的数据
			}
		
			break;
		default:
			break;
		}
	}

	@Override
	public void onClick(View v) {
		if (AbstractDevice.getInstance().isLogin(WiredConnSettingActivity.this)) {
			commit();
		}
	}

	/**
	 * 设置动态获取
	 * 
	 * @param info
	 */
	private void setAutoWay(WanBean info) {
		dialLayout.setVisibility(View.GONE);
		staticIpLayout.setVisibility(View.GONE);
	}

	/**
	 * 设置拨号上网
	 * 
	 * @param info
	 */
	private void setDialWay(WanBean info) {
		dialLayout.setVisibility(View.VISIBLE);
		staticIpLayout.setVisibility(View.GONE);
		if (info != null) {
			if (!TextUtils.isEmpty(info.getUsername())) {
				etDialAccount.setText(info.getUsername().equalsIgnoreCase("null")? "" : info.getUsername());
			}

			if (!TextUtils.isEmpty(info.getPassword())) {
				etDialPwd.setText(info.getPassword().equalsIgnoreCase("null") ? "" : info.getPassword());
			}
		}
	}

	/**
	 * 设置静态IP
	 * @param info
	 */
	private void setStaticWay(WanBean info) {
		dialLayout.setVisibility(View.GONE);
		staticIpLayout.setVisibility(View.VISIBLE);
		if (info != null) {
			etStaticIp.setText(info.getIpaddr());
			etStaticNetmask.setText(info.getNetMask());
			etStaticGateway.setText(info.getGateway());

			String dns = info.getDns1();
			if(dns!=null && dns.contains("empty")){
				dns = dns.replace("empty", "");
			}
			String d1 = "";
			String d2 = "";
			if (!TextUtils.isEmpty(dns) && dns.contains(" ")) {
				String[] ds = dns.split(" ");
				d1 = ds[0];
				d2 = ds[1];
				if(TextUtils.isEmpty(d1) || !NetUtil.isIpAddress(d1)) {
					d1 = "";
				}
				if(TextUtils.isEmpty(d2) || !NetUtil.isIpAddress(d2)) {
					d2 = "";
				}
				etStaticDns.setText(d1);
				etStaticDns2.setText(d2);
			} else {
				if(TextUtils.isEmpty(dns) || !NetUtil.isIpAddress(dns)) {
					dns = "";
				}
				etStaticDns.setText(dns);
				String dns2 = info.getDns2();
				if (!TextUtils.isEmpty(dns2) && NetUtil.isIpAddress(dns2)) {
					etStaticDns2.setText(dns2);
				} else {
					info.setDns2("");
				}
			}
		}
	}

	public static String mode = "";
	String proto = "";
	String ipaddr = "";
	String netmask = "";
	String gateway = "";
	String dns1 = "";
	String dns2 = "";
	String username = "";
	String password = "";

	/**
	 * @desc  有线上网方式提交
	 */
	private void commit() {
		if (wan == null) {
			UIHelper.showToast(WiredConnSettingActivity.this, "有线连接异常,请检查网线是否正常连接");
			return;
		}

		if (staticIpLayout.isShown()) {
			// 静态
			ipaddr = etStaticIp.getText().toString().trim();
			netmask = etStaticNetmask.getText().toString().trim();
			gateway = etStaticGateway.getText().toString().trim();
			dns1 = etStaticDns.getText().toString().trim();
			dns2 = etStaticDns2.getText().toString().trim();

			if (!NetUtil.isIpAddress(ipaddr)) {
				UIHelper.showToast(WiredConnSettingActivity.this, "您的ip地址格式不正确");
				return;
			}
			if (!NetUtil.isIpAddress(netmask)) {
				UIHelper.showToast(WiredConnSettingActivity.this, "您的子网掩码格式不正确");
				return;
			}
			if (!NetUtil.isIpAddress(gateway)) {
				UIHelper.showToast(WiredConnSettingActivity.this, "您的网关格式不正确");
				return;
			}
			if (!NetUtil.isIpAddress(dns1)) {
				UIHelper.showToast(WiredConnSettingActivity.this, "您的首选dns格式不正确");
				return;
			}

			mode = "static";
		} else if (dialLayout.isShown()) {
			// 拨号上网
			username = etDialAccount.getText().toString();
			password = etDialPwd.getText().toString();
			if (TextUtils.isEmpty(username)) {
				UIHelper.showToast(WiredConnSettingActivity.this, "账号不能为空");
				return;
			}
			if (TextUtils.isEmpty(password)) {
				UIHelper.showToast(WiredConnSettingActivity.this, "密码不能为空");
				return;
			}
			mode = "pppoe";
		} else {
			// 动态获取
			mode = "dhcp";
			if (!Model.RZHIXIANG.equals(ResourceConfig.getInstance(this).getDeviceMode())) {
				ipaddr = wan.getWired_ip();
				netmask = wan.getWired_submask();
			}
		}
		proto = mode;
		new WiredConfigSetAsyncTask().execute("");

	}

	/**
	 * 读取有线上网配置
	 * @param wanBean
	 */
	private void setWiredStatus(WanBean wanBean) {
		if (wanBean != null) {
			if ("pppoe".equalsIgnoreCase(wanBean.getProto())) {
				// 拨号
				rbDial.setChecked(true);
			} else if ("static".equalsIgnoreCase(wanBean.getProto())) {
				// 静态
				rbStaticIp.setChecked(true);
			} else {
				// 动态
				rbAuto.setChecked(true);
			}
		} else {
			rbAuto.setChecked(true);
		}
	}

	private class GetWiredConfigAsyncTask extends AsyncTask<String, Void, RelayConfBean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!AbstractDevice.getInstance().isLogin(WiredConnSettingActivity.this)) {
				cancel(true);
				return;
			}
			showProgressDialog("加载中...");
		}

		@Override
		protected RelayConfBean doInBackground(String... params) {
			return AbstractDevice.getInstance().getRelayConfig(); //又调用一次上网连接方式
		}

		@Override
		protected void onPostExecute(RelayConfBean result) {
			super.onPostExecute(result);
			if (result != null && result.wan != null) {
				wiredConnectType = result.wan;
			} else {
				UIHelper.showToast(WiredConnSettingActivity.this, "获取连接状态信息失败");
			}
			new GetWanConfigAsyncTask().execute();
		}
	}

	/**
	 * 获取wan配置
	 * @author ldj
	 * @version 1.0 2015年7月22日 上午10:04:52
	 */
	private class GetWanConfigAsyncTask extends AsyncTask<String, Void, WanBean> {
		@Override
		protected void onPreExecute() {
			showProgressDialog("加载中...");
		}

		@Override
		protected WanBean doInBackground(String... params) {
			if (params[0].equals("pppoe")){
				return AbstractDevice.getInstance().getDialConfig();
			}else {
				return AbstractDevice.getInstance().getWanConfig();
			}
		}

		@Override
		protected void onPostExecute(WanBean result) {
			dismissProgressDialog();
			if (result != null) {
				wan = result;
			} else {
				UIHelper.showToast(WiredConnSettingActivity.this, "获取连接配置信息失败");
			}
			setWiredStatus(wan);
		}
	}

	/**
	 * @desc 获取有线配置
	 */
	private class WiredConfigSetAsyncTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (!AbstractDevice.getInstance().isLogin(WiredConnSettingActivity.this)) {
				cancel(true);
				return;
			}
			showProgressDialog("请稍候...");
		}

		@Override
		protected Boolean doInBackground(String... params) {
			if (mode.equals("static")) {
				return AbstractDevice.getInstance().setStaticAccess(proto, ipaddr, netmask, gateway, dns1, dns2);
			} else if (mode.equals("pppoe")) {
				return AbstractDevice.getInstance().setPppoeAccess(proto, username, password);
			} else if (mode.equals("dhcp")) {
				return AbstractDevice.getInstance().setAutoAccess(proto, ipaddr, netmask);
			}
			return AbstractDevice.getInstance().setAutoAccess(proto, ipaddr, netmask);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			dismissProgressDialog();
			if (result) {
				UIHelper.showToast(WiredConnSettingActivity.this, "配置完成");
				if (mode.equals("dhcp")) {
					Config.setCQRouterWiredProto("dynamic");
				} else if (mode.equals("static")) {
					Config.setCQRouterWiredProto("static");
				} else if (mode.equals("pppoe")){
					Config.setCQRouterWiredProto("pppoe");
				}
			} else {
				UIHelper.showToast(WiredConnSettingActivity.this, "配置失败");
				Config.setCQRouterWiredProto("static");
			}
		}
	}
}
