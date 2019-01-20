package com.pisen.router.ui.phone.settings;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.studio.os.PreferencesUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.pisen.router.CloudApplication;
import com.pisen.router.Helper;
import com.pisen.router.R;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.ui.base.NavigationFragment;
import com.pisen.router.ui.phone.account.AccountApiConfig;

/**
 * 设置
 * @author mahuan
 * @version 1.0 2015年5月20日 上午9:47:35
 */
public class SetupFragment extends NavigationFragment implements View.OnClickListener {
	private CacheManager cacheMgr;
	private TextView txtCache;
	private ImageView imgPush;
	private boolean turnOn;
	private Button btnLogout;
	private Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case CacheManager.CACHE_FILE_SIZE:
				txtCache.setText(Helper.formatSizeToMB(msg.arg1, CacheManager.CACHE_IGNORE_SIZE));
				break;
			case CacheManager.CACHE_FILE_CLEAR:
				UIHelper.showToast(getActivity(), "总共清理" + Helper.formatSizeToMB(msg.arg1));
				txtCache.setText("0.0M");
				break;
			case CacheManager.CACHE_FILE_NOT_CLEAR:
				UIHelper.showToast(getActivity(), "无需清除缓存");
				txtCache.setText("0.0M");
				break;
			}
			return false;
		}
	});
	
//	private Handler handler = new Handler() {
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case CacheManager.CACHE_FILE_SIZE:
//				txtCache.setText(Helper.formatSizeToMB(msg.arg1, CacheManager.CACHE_IGNORE_SIZE));
//				break;
//			case CacheManager.CACHE_FILE_CLEAR:
//				UIHelper.showToast(getActivity(), "总共清理" + Helper.formatSizeToMB(msg.arg1));
//				txtCache.setText("0.0M");
//				break;
//			case CacheManager.CACHE_FILE_NOT_CLEAR:
//				UIHelper.showToast(getActivity(), "无需清除缓存");
//				txtCache.setText("0.0M");
//				break;
//			}
//		};
//	};

	@Override
	protected View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.cloud_settings_setting, container, false);
		initView(view);
		return view;
	}

	public void initView(View view) {
		getNavigationBar().setTitle("设置");
		imgPush = (ImageView) view.findViewById(R.id.tbtn_msg_recv);
		imgPush.setOnClickListener(this);
		turnOn = PreferencesUtils.getBoolean(KeyUtils.TAKE_OFF, true);
		if (turnOn) {
			imgPush.setBackgroundResource(R.drawable.cloud_setting_switch_on);
		} else {
			imgPush.setBackgroundResource(R.drawable.cloud_setting_switch_off);
		}

		cacheMgr = new CacheManager(getActivity(), handler);
		view.findViewById(R.id.ltrow_setting_clear_cache).setOnClickListener(this);
		txtCache = (TextView) view.findViewById(R.id.txt_cache);
		btnLogout = (Button) view.findViewById(R.id.account_btn_logout);
		if (CloudApplication.isLogin()) {
			btnLogout.setVisibility(View.VISIBLE);
			btnLogout.setOnClickListener(this);
		}
	}

	@Override
	public void onResume() {
		cacheMgr.new UpdateTextTask().execute();
		super.onResume();
	}

	/**
	 * @des 推送消息on-off
	 */
	public void jpushOperator() {
		if (turnOn) {
			turnOn = false;
			PreferencesUtils.setBoolean(KeyUtils.TAKE_OFF, turnOn);
			imgPush.setBackgroundResource(R.drawable.cloud_setting_switch_off);
			// JPushInterface.stopPush(getActivity());
		} else {
			turnOn = true;
			PreferencesUtils.setBoolean(KeyUtils.TAKE_OFF, turnOn);
			imgPush.setBackgroundResource(R.drawable.cloud_setting_switch_on);
			// JPushInterface.resumePush(getActivity());
		}
	}

	/**
	 * @des 缓存清理
	 */
	public void cacheClear() {
		ConfirmDialog cd  = new ConfirmDialog(getActivity());
		cd.setMessage("确定要清空品胜云缓存数据?");
		cd.setPositiveButton( "确定", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cacheMgr.new delTextTask().execute();
			}
		});
		cd.setNegativeButton( "取消", null);
		cd.show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tbtn_msg_recv:
			jpushOperator();
			break;
		case R.id.ltrow_setting_clear_cache:
			cacheClear();
			break;
		case R.id.account_btn_logout:
			CloudApplication.userInfo = null;
			PreferencesUtils.remove(AccountApiConfig.KEY_ACCOUNT);
			UIHelper.showToast(getActivity(), "退出成功");
			popBackStack();
//			btnLogout.setVisibility(View.GONE);
			//TODO 如果后台有登出接口，还需处理
			break;
		}
	}
}
