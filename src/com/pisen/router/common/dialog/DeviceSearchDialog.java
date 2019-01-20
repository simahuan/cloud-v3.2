package com.pisen.router.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;

import com.pisen.router.R;


/**
 *  设备查找对话框
 * @author Liuhc
 * @version 1.0 2015年5月18日 下午2:43:56
 */
public class DeviceSearchDialog extends Dialog{
	
	/**
	 * @param context
	 * @param theme
	 */
	public DeviceSearchDialog(Context context) {
		super(context, R.style.translucent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_dialog_devicesearch);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	
		return super.onKeyDown(keyCode, event);
	}
	
}

