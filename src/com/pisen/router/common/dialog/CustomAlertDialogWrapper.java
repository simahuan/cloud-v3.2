package com.pisen.router.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;

import com.pisen.router.R;

public abstract class CustomAlertDialogWrapper extends CustomDialog {

	private CustomAlertDialog.Builder builder;
	private boolean canCancelable = true;

	public CustomAlertDialogWrapper(Context context) {
		super(context);
		builder = new CustomAlertDialog.Builder(context, R.style.AppDialog);
	}

	@Override
	public void show() {
		builder.setContentView(onCreateContentView(context));
		Dialog dialog = builder.create();
		dialog.setCanceledOnTouchOutside(false);
		dialog.setCancelable(canCancelable);
		dialog.show();
	}

	public View onCreateContentView(Context context) {
		return null;
	}

	public void setTitle(int title) {
		builder.setTitle(title);
	}

	public void setTitle(String title) {
		builder.setTitle(title);
	}

	public void setMessage(int message) {
		builder.setMessage(message);
	}

	public void setMessage(String message) {
		builder.setMessage(message);
	}

	public void setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener) {
		builder.setPositiveButton(positiveButtonText, listener);
	}

	public void setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener) {
		builder.setPositiveButton(positiveButtonText, listener);
	}

	public void setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener) {
		builder.setNegativeButton(negativeButtonText, listener);
	}

	public void setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener) {
		builder.setNegativeButton(negativeButtonText, listener);
	}

	@Override
	public void setCancelable(boolean flag) {
		super.setCancelable(flag);
		this.canCancelable = flag;
	}

}
