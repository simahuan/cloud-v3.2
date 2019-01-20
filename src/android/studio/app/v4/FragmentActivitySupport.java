package android.studio.app.v4;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.studio.ApplicationSupport;
import android.studio.app.IController;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;

public abstract class FragmentActivitySupport extends FragmentActivity implements IController {

	public static final String TAG = FragmentActivitySupport.class.getSimpleName();
	public static final String DEFAULT_LOADING_MESSAGE = "加载中，请稍候..."; // "Loading. Please wait...";
	private static int retainCount = 0;

	protected Dialog progressDialog;
	private boolean destroyed = false;

	// 写一个广播的内部类，当收到动作时，结束activity
	private BroadcastReceiver exitReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	};

	// ***************************************
	// Activity methods
	// ***************************************
	@Override
	public ApplicationSupport getApplicationContext() {
		return (ApplicationSupport) super.getApplicationContext();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		retain();
		registerReceiver(exitReceiver, new IntentFilter(getPackageName())); // 注册
	}

	/**
	 * 退出整个应用
	 */
	public void exitApplication() {
		sendBroadcast(new Intent(getPackageName()));
		// getApplicationContext().onActivityTerminate();
		// retainCount = 0;
	}

	@Override
	protected void onDestroy() {
		release();
		destroyed = true;
		unregisterReceiver(exitReceiver);
		super.onDestroy();
	}

	private void retain() {
		if (retainCount == 0) {
			getApplicationContext().onActivityLauncher();
		}
		retainCount++;
		Log.i(TAG, "retainCount: " + retainCount);
	}

	private void release() {
		retainCount--;
		Log.i(TAG, "retainCount: " + retainCount);
		if (retainCount == 0) {
			getApplicationContext().onActivityTerminate();
		}
	}

	// ***************************************
	// Public methods
	// ***************************************

	@Override
	public void showLoadingProgressDialog() {
		showProgressDialog(DEFAULT_LOADING_MESSAGE);
	}

	@Override
	public void showProgressDialog(CharSequence message) {
		showProgressDialog(message, new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				cancelProgressDialog();
			}
		});
	}

	@Override
	public void showProgressDialog(CharSequence message, final DialogInterface.OnCancelListener listener) {
		if (progressDialog == null) {
			progressDialog = newLoadingDialog();
			// 点击ProgressDialog以外的区域不ProgressDialog dismiss掉
			// progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setOnCancelListener(null);
			progressDialog.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
						if (listener != null) {
							listener.onCancel(dialog);
						}
					}
					return true;
				}
			});
		}

		if (progressDialog instanceof ProgressDialog) {
			((ProgressDialog) progressDialog).setMessage(message);
		} else {
			progressDialog.setTitle(message);
		}
		progressDialog.show();
	}

	public Dialog newLoadingDialog() {
		return new ProgressDialog(this);
	}

	public void cancelProgressDialog() {
		dismissProgressDialog();
	}

	@Override
	public void dismissProgressDialog() {
		if (progressDialog != null && !destroyed) {
			progressDialog.dismiss();
		}
	}

}
