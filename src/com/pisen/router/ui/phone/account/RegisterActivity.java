package com.pisen.router.ui.phone.account;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.studio.os.PreferencesUtils;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.extend.VolleyManager;
import com.android.volley.extend.VolleyManager.HttpIDRequestLisenter;
import com.google.gson.GsonUtils;
import com.pisen.router.CloudApplication;
import com.pisen.router.R;
import com.pisen.router.common.dialog.LoadingDialog;
import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.common.utils.Validator;
import com.pisen.router.config.HttpKeys;
import com.pisen.router.ui.base.NavigationBarActivity;
import com.pisen.router.ui.phone.account.bean.HttpResult;
import com.pisen.router.ui.phone.account.bean.UserInfoDto;

public class RegisterActivity extends NavigationBarActivity implements HttpIDRequestLisenter, OnClickListener {

	VolleyManager volleyManager;
	static final int ACTION_ID_GET_ICONCODE = 111;
	static final int ACTION_ID_GET_VERIFYCODE = 112;
	static final int ACTION_ID_REGISTER = 113;
	static final int ACTION_ID_LOGIN = 114;
	String iconCode;
	EditText edtPhone, edtPwd, edtPwdConfirm, edtVerifyCode;
	TextView txtGetVerifyCode, txtAgreeChoice, txtAgreement;
	Button btnRegister;
	boolean agreed = true;
	static int count = 60;
	private Handler mHandler;
	String phone,password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.account_register_activity);
		setTitle("注册");
		edtPhone = (EditText) findViewById(R.id.account_register_edt_phone);
		edtPwd = (EditText) findViewById(R.id.account_register_edt_password);
		edtPwdConfirm = (EditText) findViewById(R.id.account_register_edt_password_confirm);
		edtVerifyCode = (EditText) findViewById(R.id.account_register_edt_verifycode);
		txtGetVerifyCode = (TextView) findViewById(R.id.account_register_txt_getVerifycode);
		txtAgreeChoice = (TextView) findViewById(R.id.account_register_txt_agreechoice);
		txtAgreement = (TextView) findViewById(R.id.account_register_txt_agreement);
		txtGetVerifyCode.setOnClickListener(this);
		txtAgreeChoice.setOnClickListener(this);
		txtAgreement.setOnClickListener(this);
		btnRegister = (Button) findViewById(R.id.account_register_btn_register);
		btnRegister.setOnClickListener(this);
		edtPhone.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				if (count == 60) {
					String phone = edtPhone.getText().toString().trim();
					if (phone.length() != 11) {
						txtGetVerifyCode.setEnabled(false);
						txtGetVerifyCode.setTextColor(getResources().getColor(R.color.account_disable));
					} else {
						txtGetVerifyCode.setEnabled(true);
						txtGetVerifyCode.setTextColor(getResources().getColor(R.color.register_verifycode));
					}
				}
			}
		});
		volleyManager = VolleyManager.getInstance();
		mHandler = new Handler();
		
		if(!NetUtil.isNetAvailable(this)){
			UIHelper.showToast(this,"网络异常，请保持网络连接正常~");
		}
	}
	private LoadingDialog mLoadingDialog;

	private void showLoadingDialog() {
		if (mLoadingDialog == null) {
			mLoadingDialog = new LoadingDialog(this);
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
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
	}

	public void getImgCode(View v) {
		volleyManager.post(HttpKeys.ACCOUNT_URL, AccountApiConfig.getIconCodeMap(), this, ACTION_ID_GET_ICONCODE);
		showLoadingDialog();
	}

	public void getVerifyCode(View v) {
		if (txtGetVerifyCode.isEnabled() && checkPhone()) {
			volleyManager.post(HttpKeys.ACCOUNT_URL, AccountApiConfig.getPhoneCodeMap(edtPhone.getText().toString(), 0), this, ACTION_ID_GET_VERIFYCODE);
			showLoadingDialog();
			txtGetVerifyCode.setEnabled(false);
			mHandler.post(countTicker);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		refreshAgreeChoice();
	}

	private void refreshAgreeChoice() {
		txtAgreeChoice.setCompoundDrawablesWithIntrinsicBounds(agreed ? R.drawable.item_ic_check_on_orgi : R.drawable.item_ic_check_off_orgi, 0, 0, 0);
		btnRegister.setBackgroundResource(agreed ? R.drawable.account_btn_bg_enable : R.drawable.account_btn_bg_disable);
	}

	Runnable countTicker = new Runnable() {

		@Override
		public void run() {
			if (count < 0) {
				txtGetVerifyCode.setText("获取验证码");
				count = 60;
				String phone = edtPhone.getText().toString().trim();
				if (phone.length() != 11) {
					txtGetVerifyCode.setEnabled(false);
					txtGetVerifyCode.setTextColor(getResources().getColor(R.color.account_disable));
				} else {
					txtGetVerifyCode.setEnabled(true);
					txtGetVerifyCode.setTextColor(getResources().getColor(R.color.register_verifycode));
				}
			} else {
				String str = count + "s后获取验证码";
				SpannableString mst = new SpannableString(str);
				int index = str.indexOf("s");
				mst.setSpan(new ForegroundColorSpan(Color.RED), 0, index + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				mst.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.account_disable)), index + 1, str.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
				txtGetVerifyCode.setText(mst);
				count--;
				mHandler.postDelayed(this, 1000);
			}

		}
	};

	public boolean checkPhone() {
		if (TextUtils.isEmpty(edtPhone.getText())) {
			UIHelper.showToast(RegisterActivity.this, "手机号码不能为空");
			edtPhone.requestFocus();
			return false;
		}
		String phone = edtPhone.getText().toString().trim();
		if (!Validator.checkMobileNumber(phone)) {
			UIHelper.showToast(RegisterActivity.this, "手机号码非法");
			edtPhone.setText(null);
			edtPhone.requestFocus();
			return false;
		}
		return true;
	}

	public void doRegister(View v) {
		if (!agreed) {
			UIHelper.showToast(RegisterActivity.this, "请先同意品胜用户协议");
			return;
		}
		if (!checkPhone()) {
			return;
		}
		if (TextUtils.isEmpty(edtPwd.getText())) {
			UIHelper.showToast(RegisterActivity.this, "密码不能为空");
			edtPwd.requestFocus();
			return;
		}
		String pwd = edtPwd.getText().toString();
		if (pwd.length() < 6 || pwd.length() > 16) {
			UIHelper.showToast(RegisterActivity.this, "请输入6-16位密码");
			edtPwd.requestFocus();
			return;
		}
		String pwdConfirm = edtPwdConfirm.getText().toString();
		if (!pwd.equals(pwdConfirm)) {
			UIHelper.showToast(RegisterActivity.this, "两次密码输入不一致");
			edtPwdConfirm.setText(null);
			edtPwdConfirm.requestFocus();
			return;
		}
		if (TextUtils.isEmpty(edtVerifyCode.getText())) {
			UIHelper.showToast(RegisterActivity.this, "验证码不能为空");
			return;
		}
		phone = edtPhone.getText().toString().trim();
		password = pwd;
		volleyManager.post(HttpKeys.ACCOUNT_URL,
				AccountApiConfig.getRegisterMap(phone, password, edtVerifyCode.getText().toString()), this, ACTION_ID_REGISTER);
		showLoadingDialog();
	}

	@Override
	public void onHttpFinished(boolean success, String response, int actionId) {
		if (mLoadingDialog == null || !mLoadingDialog.isShowing()) {
			return;
		}
		dismissLoadingDialog();
		if (success) {
			switch (actionId) {
			case ACTION_ID_GET_VERIFYCODE: {
				HttpResult result = GsonUtils.jsonDeserializer(response, HttpResult.class);
				if (result != null && result.IsSuccess && !result.IsError) {
					UIHelper.showToast(RegisterActivity.this, "验证码已发送");
				} else {
					UIHelper.showToast(RegisterActivity.this, "验证码发送失败");
				}
				break;
			}
			case ACTION_ID_REGISTER: {
				HttpResult dto = GsonUtils.jsonDeserializer(response, HttpResult.class);
				if (dto != null && dto.IsSuccess && !dto.IsError) {
					VolleyManager.getInstance().post(HttpKeys.ACCOUNT_URL,
							AccountApiConfig.getLoginMap(phone, password), RegisterActivity.this, ACTION_ID_LOGIN);
					showLoadingDialog();
				} else {
					String tip = dto.ErrMsg != null ? dto.ErrMsg : dto.Message != null ? dto.Message : dto.DetailError != null ? dto.DetailError : "注册失败";
					UIHelper.showToast(RegisterActivity.this, tip);
				}
				break;
			}
			case ACTION_ID_LOGIN:
				UserInfoDto dto = GsonUtils.jsonDeserializer(response, UserInfoDto.class);
				if (dto != null && dto.IsSuccess && !dto.IsError) {
					UIHelper.showToast(RegisterActivity.this, "注册成功，已自动登录");
					CloudApplication.userInfo = dto;
					PreferencesUtils.setString(AccountApiConfig.KEY_ACCOUNT,response);
					exit(true);
				} else {
//					String tip = dto.ErrMsg != null ? dto.ErrMsg : dto.Message != null ? dto.Message : dto.DetailError != null ? dto.DetailError : "登录失败";
					UIHelper.showToast(RegisterActivity.this, "注册成功,自动登录失败");
					exit(false);
				}
				break;
			default:
				break;
			}
		} else {
			switch (actionId) {
			case ACTION_ID_GET_VERIFYCODE:
				UIHelper.showToast(RegisterActivity.this, "验证码发送失败");
				break;
			case ACTION_ID_REGISTER:
				UIHelper.showToast(RegisterActivity.this, "注册失败");
				break;
			case ACTION_ID_LOGIN:
				UIHelper.showToast(RegisterActivity.this, "注册成功,自动登录失败");
				exit(false);
				break;
			}
		}

	}
	
	private void exit(boolean isLogin) {
		setResult(isLogin ? RESULT_OK : RESULT_CANCELED);
		finish();
	}

	@Override
	public void onClick(View paramView) {
		switch (paramView.getId()) {
		case R.id.account_register_btn_register:
			doRegister(paramView);
			break;
		case R.id.account_register_txt_getVerifycode:
			getVerifyCode(paramView);
			break;
		case R.id.account_register_txt_agreechoice:
			agreed = !agreed;
			refreshAgreeChoice();
			break;
		case R.id.account_register_txt_agreement:
			startActivity(new Intent(this, AgreementContentActivity.class));
			break;
		default:
			break;
		}

	}

}
