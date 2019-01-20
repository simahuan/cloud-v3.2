package com.pisen.router.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.pisen.router.R;

public class CustomDialog extends Dialog {

	protected Context context;

	public CustomDialog(Context context) {
		this(context, R.style.AppDialog);
	}

	public CustomDialog(Context context, int theme) {
		super(context, theme);
		this.context = context;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
	}
}
