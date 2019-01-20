package com.pisen.router.ui.phone.account;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.studio.os.PreferencesUtils;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.extend.VolleyManager;
import com.android.volley.extend.VolleyManager.HttpRequestLisenter;
import com.google.gson.GsonUtils;
import com.pisen.router.CloudApplication;
import com.pisen.router.R;
import com.pisen.router.common.dialog.LoadingDialog;
import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.common.utils.Validator;
import com.pisen.router.common.utils.VerifyCodeUtils;
import com.pisen.router.config.HttpKeys;
import com.pisen.router.ui.base.NavigationBarActivity;
import com.pisen.router.ui.phone.account.bean.UserInfoDto;

public class LoginActivity extends NavigationBarActivity implements HttpRequestLisenter, OnClickListener {

	private EditText edtPhone, edtPwd, edtVerifyCode;
	private ImageView imgVerifyCode;
	private int errorCount = 0;

	private VerifyCodeUtils verifyCodeUtils;
	private Handler mHandler;
	
	private static final int CODE_REQUEST_REGISTER = 0x11;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_login_activity);
		setTitle("登录");
		getNavigationBar().setRightButton("注册", -1, new OnClickListener() {

			@Override
			public void onClick(View paramView) {
				Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
				startActivityForResult(intent, CODE_REQUEST_REGISTER);
			}
		});
		edtPhone = (EditText) findViewById(R.id.account_login_edt_phone);
		edtPwd = (EditText) findViewById(R.id.account_login_edt_password);
		edtVerifyCode = (EditText) findViewById(R.id.account_login_edt_verifycode);
		imgVerifyCode = (ImageView) findViewById(R.id.account_login_img_verifycode);
		imgVerifyCode.setOnClickListener(this);
		if(!NetUtil.isNetAvailable(this)){
			UIHelper.showToast(this,"网络异常，请保持网络连接正常~");
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == CODE_REQUEST_REGISTER){
			if(resultCode == RESULT_OK){
				finish();
			}
		}
	}

	public void doLogin(View v) {
		if (TextUtils.isEmpty(edtPhone.getText())) {
			UIHelper.showToast(LoginActivity.this, "手机号码不能为空");
			edtPhone.requestFocus();
			return;
		}
		String text = edtPhone.getText().toString().trim();
		if (!Validator.checkMobileNumber(text)) {
			UIHelper.showToast(LoginActivity.this, "手机号码非法");
			edtPhone.setText(null);
			edtPhone.requestFocus();
			return;
		}
		if (TextUtils.isEmpty(edtPwd.getText())) {
			UIHelper.showToast(LoginActivity.this, "密码不能为空");
			edtPwd.requestFocus();
			return;
		}
		String password = edtPwd.getText().toString();
		if (password.length() < 6 || password.length() > 16) {
			UIHelper.showToast(LoginActivity.this, "请输入6-16位密码");
			edtPwd.requestFocus();
			return;
		}
		if (edtVerifyCode.isShown()) {
			if (TextUtils.isEmpty(edtVerifyCode.getText())) {
				UIHelper.showToast(LoginActivity.this, "验证码不能为空");
				return;
			}
			String verifyCode = edtVerifyCode.getText().toString().trim();
			if (!verifyCode.equalsIgnoreCase(verifyCodeUtils.getCode())) {
				UIHelper.showToast(LoginActivity.this, "验证码输入错误");
				edtVerifyCode.setText(null);
				edtVerifyCode.requestFocus();
				imgVerifyCode.setImageBitmap(verifyCodeUtils.createBitmap());
				return;
			}
		}
		if(!NetUtil.isNetAvailable(this)){
			UIHelper.showToast(this, "网络异常，请检查网络后重试~");
			return;
		}

		VolleyManager.getInstance().post(HttpKeys.ACCOUNT_URL, AccountApiConfig.getLoginMap(edtPhone.getText().toString(), password), this);
		showLoadingDialog();
	}

	public void goRetrivePwd(View v) {
//		startActivity(new Intent(this, RetrivePwdActivity.class));
	}

	private LoadingDialog mLoadingDialog;

	private void showLoadingDialog() {
		if (mLoadingDialog == null) {
			mLoadingDialog = new LoadingDialog(LoginActivity.this);
		}
		if (!mLoadingDialog.isShowing()) {
			mLoadingDialog.show();
		}
	}

	private void dismissLoadingDialog() {
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.dismiss();
		}
	}

	@Override
	public void onHttpFinished(boolean success, String response) {
		if (mLoadingDialog == null || !mLoadingDialog.isShowing()) {
			return;
		}
		dismissLoadingDialog();
		if (success) {
			UserInfoDto dto = GsonUtils.jsonDeserializer(response, UserInfoDto.class);
			if (dto != null && dto.IsSuccess && !dto.IsError) {
				UIHelper.showToast(LoginActivity.this, "登录成功");
				CloudApplication.userInfo = dto;
				PreferencesUtils.setString(AccountApiConfig.KEY_ACCOUNT, response);
				finish();
			} else {
				String tip = dto.ErrMsg != null ? dto.ErrMsg : dto.Message != null ? dto.Message : dto.DetailError != null ? dto.DetailError : "登录失败";
				UIHelper.showToast(LoginActivity.this, tip);
				errorCount++;
			}
		} else {
			UIHelper.showToast(LoginActivity.this, "登录出错");
		}
		if (errorCount >= 3) {
			findViewById(R.id.account_login_layout_verifycode).setVisibility(View.VISIBLE);
			findViewById(R.id.account_login_line_verifycode).setVisibility(View.VISIBLE);
			showVerifyCode();
		}
	}

	private void showVerifyCode() {
		verifyCodeUtils = VerifyCodeUtils.getInstance();
		mHandler = new Handler();
		mHandler.post(initVerifyCode);
	}

	Runnable initVerifyCode = new Runnable() {

		@Override
		public void run() {
			if (imgVerifyCode.getHeight() > 0 && imgVerifyCode.getWidth() > 0) {
				verifyCodeUtils.setRangeRect(imgVerifyCode.getWidth(), imgVerifyCode.getHeight());
				imgVerifyCode.setImageBitmap(verifyCodeUtils.createBitmap());
			} else {
				mHandler.postDelayed(this, 50);
			}
		}
	};

	@Override
	public void onClick(View paramView) {
		// TODO Auto-generated method stub
		switch (paramView.getId()) {
		case R.id.account_login_img_verifycode:
			imgVerifyCode.setImageBitmap(verifyCodeUtils.createBitmap());
			break;

		default:
			break;
		}
	}
}
