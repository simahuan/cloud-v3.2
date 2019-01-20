package com.pisen.router.ui.phone.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.studio.os.PreferencesUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pisen.router.CloudApplication;
import com.pisen.router.R;
import com.pisen.router.common.TimeIntervalClickListener;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.core.monitor.RedHotMonitor;
import com.pisen.router.core.monitor.RedHotMonitor.RedHotCallBack;
import com.pisen.router.ui.base.FragmentActivity;
import com.pisen.router.ui.base.NavigationFragment;
import com.pisen.router.ui.phone.account.LoginActivity;
import com.pisen.router.ui.phone.settings.upgrade.AppVersion;
import com.pisen.router.ui.phone.settings.upgrade.DownLoadApp;
import com.pisen.router.ui.phone.settings.upgrade.UpgradeApp.UpgradeAppCallBack;
import com.pisen.router.ui.phone.settings.upgrade.UpgradeAppService;
import com.pisen.router.ui.phone.settings.upgrade.UpgradeAppService.MyBinder;

/**
 * @author  mahuan
 * @version 1.0 2015年5月18日 下午2:28:36
 */
public class AboutFragment extends NavigationFragment{
	private ImageView     imgVerUpdateMark;
	private boolean       isBind = false;
	private long 		  lastClick = 0;
	private String appVersion ;
	private Activity activity;
	private RedHotMonitor redHotMonitor;
	private TimeIntervalClickListener clickListener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (FragmentActivity)activity;
	}
	
	@Override
	protected View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setTitle("关于");
		View view  = inflater.inflate(R.layout.cloud_settings_about, container,false);
		initView(view);
		return view;
	}
	
	public void initView(View view){
		 TextView txtVersionUpdate = (TextView) view.findViewById(R.id.txtVersionUpdate);
		 txtVersionUpdate.setText(UIHelper.getCloudVersion(activity));
		 clickListener = new TimeIntervalClickListener() {
			@Override
			public void onTimeIntervalClick(View v) {
				AboutFragment.this.onClick(v);
			}
		};
		 
		 view.findViewById(R.id.ltrow_version_update).setOnClickListener(clickListener);
		 view.findViewById(R.id.ltrow_service_term).setOnClickListener(clickListener);
		 view.findViewById(R.id.ltrow_about_feedback).setOnClickListener(clickListener);
		 view.findViewById(R.id.ltrow_contact_customer_service).setOnClickListener(clickListener);
		 imgVerUpdateMark = (ImageView) view.findViewById(R.id.img_ver_update_mark);
		 upgradeRedHotState();
		 
		 Intent intent = new Intent(activity, UpgradeAppService.class);
		 this.isBind = activity.bindService(intent, connection,Service.BIND_AUTO_CREATE);
		 appVersion = UIHelper.getVersion(activity);
		 
		 redHotMonitor = RedHotMonitor.getInstance();
		 redHotMonitor.registerObserver(callBack);
	}
	
	/** redHot更新回调 */
	RedHotCallBack callBack = new RedHotCallBack(){
		@Override
		public void update(AppVersion ver, DownLoadApp app) {
			imgVerUpdateMark.setVisibility(View.VISIBLE);
		}
	};
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ltrow_version_update://UI红点消失
			upgradeVersion();  
			break;
		case R.id.ltrow_service_term:
			FragmentActivity.startFragment(activity, ServiceTermsFragment.class);
			break;
		case R.id.ltrow_about_feedback:
			FragmentActivity.startFragment(activity, FeedbackFragment.class);
			break;
		case R.id.ltrow_contact_customer_service:
			if (!CloudApplication.isLogin()) {
				UIHelper.showToast(activity, "请先登录");
				activity.startActivity(new Intent(activity, LoginActivity.class));
			} else {
//				activity.startActivity(new Intent(activity, com.pisen.router.ui.phone.easemob.LoginActivity.class).putExtra(Constant.MESSAGE_TO_INTENT_EXTRA,
//						Constant.MESSAGE_TO_AFTER_SALES));
			}
			break;
		}
	}
	
	@Override
	public void onViewCreated(View view, Bundle bundle) {
		view.setOnTouchListener(new View.OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		super.onViewCreated(view, bundle);
	}

	/**
	 * 更新版本信息
	 * @des
	 */
	private void upgradeVersion() {
		if (System.currentTimeMillis() - lastClick >= 300) {
			if (!PreferencesUtils.getBoolean(KeyUtils.APP_VERSION, false)) {
				UIHelper.showToast(activity, "已是最新版本 !");
			} else {
				appService.refresh();
			}
		}
		lastClick = System.currentTimeMillis();
	}
	
	public void upgradeRedHotState() {
		if (PreferencesUtils.getBoolean(KeyUtils.APP_VERSION, false)) {
			imgVerUpdateMark.setVisibility(View.VISIBLE);
		} else {
			imgVerUpdateMark.setVisibility(View.GONE);
		}
	}
	
	private UpgradeAppService appService;
	private ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			MyBinder binder = (MyBinder) service;
			binder.setUpgradeAppCallBack(back);
			binder.setIsShow(true);
			appService = binder.getUpgradeAppService();
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			appService = null;
		}
	};
	
	private UpgradeAppCallBack back = new UpgradeAppCallBack() {
		@Override
		public void downLoad(final AppVersion ver) throws NumberFormatException, Exception {
			final DownLoadApp app = new DownLoadApp();
			app.apkUrl = ver.Link;
			final String appVersion = UIHelper.getVersion(activity);
			if (!appVersion.equals(ver.Version)) {
				 showConfirmDialog(ver,app);
			} else {
				PreferencesUtils.setBoolean(KeyUtils.APP_VERSION, false);
				UIHelper.showToast(activity, "已是最新版本 !");
			}
		}
		
		@Override
		public void callBack(String result) {
			if (isAdded()){
				UIHelper.showToast(activity, activity.getResources().getString(R.string.network_disconnect));
			}
		};
	};
	
	public void showConfirmDialog(final AppVersion ver, final DownLoadApp app) {
		final ConfirmDialog cd = new ConfirmDialog(activity);
		cd.setTitle("品胜云升级");
		cd.setMessage("有新版本为" + ver.Version + ",是否更新？" + "\r\n" + ver.VersionDescription,Gravity.CENTER_VERTICAL);
		cd.setNegativeButton("暂不升级", null);
		cd.setPositiveButton("立即升级", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cd.dismiss();
				app.showDownloadDialog(activity);
			}
		});
		cd.show();
	}
	
	public void onStop() {
		super.onStop();
		if (isBind) {
			activity.unbindService(connection);
			isBind = false;
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		conVersionUpdate();
	}
	
	private void conVersionUpdate(){
		if (!appVersion.equals(PreferencesUtils.getString(KeyUtils.APP_NETVER, appVersion))){
			PreferencesUtils.setBoolean(KeyUtils.APP_VERSION, true); 
		}
	}
}
