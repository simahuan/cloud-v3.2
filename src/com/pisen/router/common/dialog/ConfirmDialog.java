package com.pisen.router.common.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.pisen.router.R;

/**
 * 确定类对话框
 * @author mahuan
 * @version 1.0 2015年5月23日 下午2:59:40
 */
public class ConfirmDialog extends CustomAlertDialogWrapper {

	private String title;
	private String message;
	private TextView txtTitle;
	private TextView txtMessage;
	private TextView txtContent;
	private int gravity = Gravity.LEFT;

	public ConfirmDialog(Context context) {
		super(context);
	}

	public static ConfirmDialog show(Context context, String message, String title, String positiveText, DialogInterface.OnClickListener positiveListener) {
		return show(context, message, title, positiveText, positiveListener, null, null);
	}

	public static ConfirmDialog show(Context context, String message, String title, String positiveText, DialogInterface.OnClickListener positiveListener,
			String negativeText, DialogInterface.OnClickListener negativeListener) {
		ConfirmDialog dialog = new ConfirmDialog(context);
		dialog.setTitle(title);
		dialog.setMessageCenter(message);
		dialog.setPositiveButton(positiveText, positiveListener);
		dialog.setNegativeButton(negativeText, negativeListener);
		dialog.show();
		return dialog;
	}

	@Override
	public void setTitle(int title) {
		setTitle(context.getString(title));
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public void setMessage(int message) {
		setMessage(context.getString(message));
	}

	@Override
	public void setMessage(String message) {
		this.message = message;
	}

	public void setMessageCenter(String message) {
		setMessage(message, Gravity.CENTER);
	}

	public void setMessage(String message, int gravity) {
		setMessage(message);
		this.gravity = gravity;
	}

	@Override
	public View onCreateContentView(Context context) {
		View view = View.inflate(context, R.layout.app_dialog_confirm, null);
		if (title == null) {
			view.findViewById(R.id.confirm2).setVisibility(View.GONE);
			view.findViewById(R.id.txtContent).setVisibility(View.VISIBLE);
			txtContent = (TextView) view.findViewById(R.id.txtContent);
			txtContent.setText(message);
			//txtContent.setGravity(gravity);
		} else {
			view.findViewById(R.id.txtContent).setVisibility(View.GONE);
			view.findViewById(R.id.confirm2).setVisibility(View.VISIBLE);
			txtTitle = (TextView) view.findViewById(R.id.txtTitle);
			txtMessage = (TextView) view.findViewById(R.id.txtMessage);
			txtTitle.setText(title);
			txtMessage.setText(message);
			txtMessage.setGravity(gravity);
		}
		return view;
	}
}
