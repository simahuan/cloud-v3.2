package com.pisen.router.common.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.pisen.router.R;

/**
 * 输入型对话框
 * 
 * @author mahuan
 * @version 1.0 2015年5月23日 下午3:40:06
 */
public class InputDialog extends CustomDialog implements View.OnClickListener {

	public interface OnClickListener {
		void onOk(DialogInterface dialog, String inputText);

		void onCancel(DialogInterface dialog);
	}

	static public abstract class SimpleClickListener implements OnClickListener {
		@Override
		public void onCancel(DialogInterface dialog) {
		}
	}

	private CharSequence title;
	private CharSequence input;
	private CharSequence message;
	private TextView titleText;
	private TextView messageText;
	private EditText edtInput;
	private OnClickListener clickListener;
	private InputMethodManager imm;

	public InputDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_dialog_input);
		imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
		titleText = (TextView) findViewById(R.id.titleText);
		messageText = (TextView) findViewById(R.id.messageText);
		edtInput = (EditText) findViewById(R.id.inputText);
		titleText.setText(title);
		
		if (message != null) {
			messageText.setVisibility(View.VISIBLE);
			messageText.setText(message);
		}
		
		selectAllText();
		
		edtInput.setText(input);
		findViewById(R.id.negativeButton).setOnClickListener(this);
		findViewById(R.id.positiveButton).setOnClickListener(this);
	}

	/**
	 * @des 重命名选中文本
	 */
	private void selectAllText() {
		if(!TextUtils.isEmpty(input)){
			edtInput.setSelection(edtInput.getSelectionStart());
			edtInput.setSelectAllOnFocus(true);
		}
	}

	@Override
	public void show() {
		super.show();
		showKeyboard(edtInput);
	}
	
	
	public void setOnClickListener(OnClickListener l) {
		this.clickListener = l;
	}

	@Override
	public void setTitle(CharSequence title) {
		this.title = title;
	}

	public void setMessage(CharSequence message) {
		this.message = message;
	}

	public void setInputText(String input) {
		this.input = input;
	}

	public EditText getEditText() {
		return edtInput;
	}

	@Override
	public void onClick(View v) {
		hintKeyboard(edtInput);
		switch (v.getId()) {
		case R.id.negativeButton:
			if (clickListener != null) {
				clickListener.onCancel(this);
			}
			break;
		case R.id.positiveButton:
			if (clickListener != null) {
				clickListener.onOk(this, edtInput.getText().toString());
			}
			break;
		}
		dismiss();
	}

	/**
	 * 隐藏软键盘
	 */
	private void hintKeyboard(EditText et) {
		imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
	}

	/**
	 * @des 显示软键盘
	 * @param et
	 */
	private void showKeyboard(EditText et){
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		imm.showSoftInput(et, 0);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	
}
