package com.pisen.router.ui.phone.settings;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.studio.os.PreferencesUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.pisen.router.CloudApplication;
import com.pisen.router.R;
import com.pisen.router.common.TimeIntervalClickListener;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.common.utils.WindowUtils;
import com.pisen.router.core.monitor.RedHotMonitor;
import com.pisen.router.core.monitor.RedHotMonitor.RedHotCallBack;
import com.pisen.router.ui.HomeActivity;
import com.pisen.router.ui.base.FragmentActivity;
import com.pisen.router.ui.phone.HomeFragment;
import com.pisen.router.ui.phone.account.LoginActivity;
import com.pisen.router.ui.phone.settings.upgrade.AppVersion;
import com.pisen.router.ui.phone.settings.upgrade.DownLoadApp;
import com.squareup.picasso.Picasso;

/**
 * 更多设置
 * @author yangyp
 */
public class SettingsFragment extends HomeFragment {
	private ImageView imgVerUpdateMark;
	private EditText edtNickName;
	private ImageView imgChangeHead;
	private Activity activity;
	private InputMethodManager imm;
	private RedHotMonitor redHotMonitor;
	private TableRow ltrow_home_recommend_friend;
	private TimeIntervalClickListener clickListener;
	private TextView txtUserName;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (HomeActivity)activity;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.cloud_settings, container, false);
		imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
		initView(view);
		return view;
	}
	
	public void initView(final View view) {
		imgVerUpdateMark = (ImageView)view.findViewById(R.id.img_ver_update_mark);
		if (PreferencesUtils.getBoolean(KeyUtils.APP_VERSION, false)){
			imgVerUpdateMark.setVisibility(View.VISIBLE);
			} else {
			imgVerUpdateMark.setVisibility(View.GONE);
		}
		
		clickListener = new TimeIntervalClickListener() {
			@Override
			public void onTimeIntervalClick(View v) {
				SettingsFragment.this.onClick(v);
			}
		};
		
		view.findViewById(R.id.route).setOnClickListener(clickListener);
		view.findViewById(R.id.setting).setOnClickListener(clickListener);
		view.findViewById(R.id.ltrow_home_weixin_mall).setOnClickListener(clickListener);
		view.findViewById(R.id.ltrow_home_weixin_public_no).setOnClickListener(clickListener);
		view.findViewById(R.id.ltrow_home_scan_install).setOnClickListener(clickListener);
		ltrow_home_recommend_friend = (TableRow) view.findViewById(R.id.ltrow_home_recommend_friend);
		ltrow_home_recommend_friend.setOnClickListener(clickListener);
		
		view.findViewById(R.id.ltrow_home_help).setOnClickListener(clickListener);
		view.findViewById(R.id.ltrow_home_about).setOnClickListener(clickListener);

		imgChangeHead = (ImageView) view.findViewById(R.id.imgChangeHead);
		imgChangeHead.setOnClickListener(clickListener);
		edtNickName = (EditText) view.findViewById(R.id.edtNickName);
		txtUserName = (TextView) view.findViewById(R.id.txtUserName);
		txtUserName.setOnClickListener(clickListener);
		txtUserName.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		txtUserName.getPaint().setAntiAlias(true);
		
//		edtNickName.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		edtNickName.addTextChangedListener(renameTextWatcher);
		redHotMonitor = RedHotMonitor.getInstance();
		redHotMonitor.registerObserver(callBack);
	}
	
	TextWatcher renameTextWatcher = new TextWatcher() {
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
		@Override
		public void afterTextChanged(Editable s) {
			edtNickName.setGravity(Gravity.CENTER_HORIZONTAL);
		}
	};
	

	/** redHot更新回调 */
	RedHotCallBack callBack = new RedHotCallBack(){
		@Override
		public void update(AppVersion ver, DownLoadApp app) {
			imgVerUpdateMark.setVisibility(View.VISIBLE);
		}
	};
	
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ltrow_home_weixin_mall:
			FragmentActivity.startFragment(activity, HuiYuanDiFragment.class);
			break;
		case R.id.ltrow_home_weixin_public_no:
			FragmentActivity.startFragment(activity, WechatFragment.class);
			break;
		case R.id.ltrow_home_scan_install:
			FragmentActivity.startFragment(activity, ScanInstallFragment.class);
			break;
		case R.id.ltrow_home_recommend_friend:
			share();
			break;
		case R.id.ltrow_home_help:
			FragmentActivity.startFragment(activity, HelpFragment.class);
			break;
		case R.id.ltrow_home_about:
			FragmentActivity.startFragment(activity, AboutFragment.class);
			break;
		case R.id.route:
			hideSoftInputFromWindow(edtNickName);
			((HomeActivity) getActivity()).toggleMenu();
			break;
		case R.id.setting:
			FragmentActivity.startFragment(activity, SetupFragment.class);
			break;
		case R.id.imgChangeHead:
		case R.id.txtUserName:
//			FragmentActivity.startFragment(activity, ModifyHeadFragment.class);
//			hideSoftInputFromWindow(edtNickName);
			if (!CloudApplication.isLogin()) {
				activity.startActivity(new Intent(activity, LoginActivity.class));
			}
			break;
		default :
			break;
		}
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
		setNickName(edtNickName.getText().toString().trim());
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (imgVerUpdateMark.isShown()){
			PreferencesUtils.setBoolean(KeyUtils.APP_VERSION, true);
		}
		setCursorShown();
		setCursorHidden();
		getNickName(edtNickName);
//		getNickHead(imgChangeHead);
		WindowUtils.hideSoftwareWindow(activity, ltrow_home_recommend_friend);
		
		if (CloudApplication.isLogin()) {
			txtUserName.setText(TextUtils.isEmpty(CloudApplication.userInfo.NickName) ? CloudApplication.userInfo.Phone : CloudApplication.userInfo.NickName);
		} else {
			txtUserName.setText("立即登录");
		}
		setHeadImage();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		redHotMonitor.unregisterObserver(callBack);
	}
	
	/**
	 * @des 隐藏光标
	 */
	private void setCursorHidden() {
		edtNickName.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			    if (actionId == EditorInfo.IME_ACTION_DONE) {
			    	setEditable(edtNickName,false);
                }
				return false;
			}
		});
	}

	/**
	 * @des 显示光标
	 */
	private void setCursorShown() {
		edtNickName.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setEditable(edtNickName, true);
				edtNickName.setGravity(Gravity.CENTER_HORIZONTAL);
			}
		});
	}
	
	/**
	 * @des   设置呢称
	 * @param nickName
	 */
	public void setNickName(String nickName){
		if (!TextUtils.isEmpty(nickName)){
			PreferencesUtils.setString(KeyUtils.NICK_NAME, nickName);
		}else {
			PreferencesUtils.setString(KeyUtils.NICK_NAME, null);
		}
	}
	
	/**
	 * @des 获取呢称
	 */
	public void getNickName(EditText edtNickName){
		   final String name = PreferencesUtils.getString(KeyUtils.NICK_NAME,null);
		if (!TextUtils.isEmpty(name)){
			edtNickName.setText(name);
			edtNickName.setGravity(Gravity.CENTER_HORIZONTAL);
		}else {
			edtNickName.setText("");
			edtNickName.setHint(getResources().getString(R.string.settings_pls_input_nickname));
			edtNickName.setSelection(edtNickName.getSelectionStart());
		}
	}
	
	/**
	 * @des 获取人物头像
	 */
	public void getNickHead(ImageView imgChangeHead){
		int icon = -1;
		try {
			icon = PreferencesUtils.getInt(KeyUtils.NICK_HEAD, -1);
		} catch (Exception e) {
		}
		imgChangeHead.setImageBitmap(IconResource.getIconWithCustom(getActivity(), icon));
	}
	
	private void setHeadImage() {
		if (CloudApplication.isLogin()) {
			if (CloudApplication.userInfo.HeadImage != null) {
				Picasso.with(getActivity()).load(CloudApplication.userInfo.HeadImage).error(R.drawable.head_1).into(imgChangeHead);
			} else {
				getNickHead(imgChangeHead);
			}
		} else {
			imgChangeHead.setImageResource(R.drawable.account_headicon_unlogin);
		}
	}
	
	private void hideSoftInputFromWindow(EditText et){
		if (imm.isActive()){
			imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
			et.setCursorVisible(false);
		  }
	}
	
	private void setEditable(EditText mEdit, boolean value) {
		if (value) {
			mEdit.setCursorVisible(true);
			mEdit.setFocusableInTouchMode(true);
			mEdit.requestFocus();
		} else {
			mEdit.setCursorVisible(false);
		}
	}  
	
	/**
	 * @des 分享
	 */
	private void share() {
		OnekeyShare oks = new OnekeyShare();
		oks.disableSSOWhenAuthorize();
		oks.setTitle("品胜分享");
		if (isAdded()){
			oks.setText(activity.getResources().getString(R.string.settings_setting_shared));
		}
		oks.show(activity);
	}
}
