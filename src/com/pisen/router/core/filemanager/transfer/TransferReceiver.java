package com.pisen.router.core.filemanager.transfer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Receives system broadcasts (boot, network connectivity)
 */
public class TransferReceiver extends BroadcastReceiver {

	public static final String ACTION_RETRY = "android.intent.action.TRANSFER_WAKEUP";

	@Override
	public void onReceive(final Context context, final Intent intent) {
		final String action = intent.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			startService(context);

		} else if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
			startService(context);

		} else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			final ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo info = connManager.getActiveNetworkInfo();
			if (info != null && info.isConnected()) {
				startService(context);
			}

		} else if (TransferReceiver.ACTION_RETRY.equals(action)) {
			startService(context);

		}
	}

	private void startService(Context context) {
		Log.i("startService"," Transferreceiver oncreate...");
		context.startService(new Intent(context, TransferServiceV2.class));
	}
}
