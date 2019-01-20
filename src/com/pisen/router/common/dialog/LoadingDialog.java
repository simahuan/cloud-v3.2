package com.pisen.router.common.dialog;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.pisen.router.R;

/**
 * 不确定进度 加载
 * author  mahuan
 * @version 1.0 2015年5月23日 下午2:31:32
 */
public class LoadingDialog extends CustomDialog {
	//private ImageView imgLoding;
	private TextView txtContent;
	private CharSequence message = "请稍候...";

	public LoadingDialog(Context context) {
		super(context, R.style.AppDialog_NoFrame);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_dialog_loading);
		//imgLoding = (ImageView) findViewById(R.id.imgLoding);
		txtContent = (TextView) findViewById(R.id.txtContent);
		txtContent.setText(message);
	}

	@Override
	public void setTitle(CharSequence title) {
		if (txtContent != null) {
			txtContent.setText(title);
		} else {
			this.message = title;
		}
	}

/*	private void startAnim() {
		Animation anim = AnimationUtils.loadAnimation(context, R.anim.loading_animation);
		imgLoding.startAnimation(anim);
	}

	@Override
	protected void onStart() {
		super.onStart();
		startAnim();
	}

	@Override
	protected void onStop() {
		imgLoding.clearAnimation();
		super.onStop();
	}*/
}
