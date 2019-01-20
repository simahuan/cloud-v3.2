package com.pisen.router.ui.phone.settings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Service;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.studio.util.ValidatorUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.GsonUtils;
import com.pisen.router.R;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.common.utils.Validator;
import com.pisen.router.config.HttpKeys;
import com.pisen.router.ui.base.FragmentActivity;
import com.pisen.router.ui.base.NavigationFragment;
import com.pisen.router.ui.base.impl.DefaultNavigationBar;
import com.pisen.router.ui.phone.settings.http.HttpJsonRequest;
import com.pisen.router.ui.phone.settings.http.HttpManager.OnHttpCallBack;
import com.pisen.router.ui.phone.settings.upgrade.JsonResponse;

/**
 * 意见与反馈
 * @author  mahuan
 * @version 1.0 2015年5月19日 下午8:12:17
 */
public class FeedbackFragment extends NavigationFragment {
	// 内容
	private EditText edtFeedbackContent;
	// 名称
	private EditText edtFeedbackName;
	// 联系方式
	private EditText edtFeedbackContact;
	// 内容计数
	private TextView txtFeedbackFaceCount;
	private String  tempFeedbackContent= "";
	private String 	feedbackContent;
	private Activity activity;
	private long lastClick = 0;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (FragmentActivity)activity;
	}
	
	@Override
	protected View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.cloud_settings_feedback, container,false);
		initView(view);
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle bundle) {
		view.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
		super.onViewCreated(view, bundle);
	}
	
	public void initView(View view){
		final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
		DefaultNavigationBar navibar = (DefaultNavigationBar) getNavigationBar();
		navibar.setTitle("意见与反馈");
		navibar.setRightButton("提交", new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (System.currentTimeMillis() - lastClick >= 500)  
					 submitClick();
		        lastClick = System.currentTimeMillis();  
			}
		});
		navibar.setLeftButton("返回", new OnClickListener() {
			@Override
			public void onClick(View v) {
				View view = getActivity().getCurrentFocus();
				if (imm.isActive() &&  view!= null){
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
					activity.onBackPressed();
			}
		});
		
		/** 初始化数据*/
		edtFeedbackContent = (EditText) view.findViewById(R.id.edtFeedbackContent);
		edtFeedbackContent.setTextColor(Color.parseColor("#333333"));
		
		edtFeedbackName = (EditText) view.findViewById(R.id.edtFeedbackName);
		edtFeedbackName.setTextColor(Color.parseColor("#333333"));
		
		edtFeedbackContact = (EditText) view.findViewById(R.id.edtFeedbackContact);
		edtFeedbackContact.setTextColor(Color.parseColor("#333333"));
		
		txtFeedbackFaceCount = (TextView) view.findViewById(R.id.txt_feedback_face_count);
		
		TextWatcher renameTextWatcher = new TextWatcher() {
			private CharSequence temp;
			private int selectionStart;
			private int selectionEnd;
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				temp = s;
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			// 在文字改变后调用
			@Override
			public void afterTextChanged(Editable s) {
				selectionStart = edtFeedbackContent.getSelectionStart();
				selectionEnd = edtFeedbackContent.getSelectionEnd();
				if (temp.length() <= 200) {
					txtFeedbackFaceCount.setHint("您还可以输入" + (200 - temp.length() + "字"));
				} else {
					s.delete(selectionStart - 1, selectionEnd);
					int tempSelection = selectionStart;
					edtFeedbackContent.setText(s);
					edtFeedbackContent.setSelection(tempSelection);
					edtFeedbackContent.setHint("您已经输入最多200字");
				}
			}
		};
		edtFeedbackContent.addTextChangedListener(renameTextWatcher);
	}

	public void submitClick() {
		try {
				feedbackContent = edtFeedbackContent.getText().toString();
			if (ValidatorUtils.isEmpty(feedbackContent)) {
				UIHelper.showToast(activity, "请输入您反馈的内容!");
				return;
			}else if(feedbackContent.equals(tempFeedbackContent)){
				UIHelper.showToast(activity, "您已提交相同反馈的内容!");
				return;
			}
			
			final String feedbackContact = edtFeedbackContact.getText().toString();
			if (feedbackContact == null || feedbackContact.trim().equals("")) {
					submit();
			} else {
				if (Validator.checkMobileNumber(feedbackContact)) {
					submit();
				} else if (Validator.checkEmail(feedbackContact)) {
					submit();
				} else {
					UIHelper.showToast(activity, "您输入的手机或邮箱格式不正确");
				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
			UIHelper.showToast(activity, getResources().getString(R.string.network_disconnect));
		}
	}

	private void submit() throws JSONException {
		hideSoftInputFromWindow(edtFeedbackContent);
		HttpJsonRequest request = new HttpJsonRequest(getActivity());
		request.setDialogShow();
		JSONObject obj = new JSONObject();
		obj.put("content", feedbackContent);
		obj.put("name", edtFeedbackName.getText().toString());
		obj.put("contact", edtFeedbackContact.getText().toString());
		obj.put("source", "10");
		request.execute(new String[] { "", "正在提交，请稍候..." }, HttpKeys.FEED_BACK_URL, obj.toString(), new OnHttpCallBack() {
			@Override
			public void getHttpResult(String result) {
				JsonResponse jsonResult = GsonUtils.jsonDeserializer(result, JsonResponse.class);
				if (jsonResult == null || jsonResult.IsError) {
					UIHelper.showToast(activity, getResources().getString(R.string.network_disconnect));
					return;
				}
				tempFeedbackContent = feedbackContent;
				ConfirmDialog.show(getActivity(), "发送成功，谢谢您的意见反馈", null, "确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.onBackPressed();
					}
				});
			}
		});
	}
	
	private void hideSoftInputFromWindow(EditText et) {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Service.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
		}
	}
	

	
}
