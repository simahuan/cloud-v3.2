package com.pisen.router.ui.phone.resource.transfer;

import android.content.Context;

import com.pisen.router.R;

/**
 * 传输纪录adapter
 * 
 * @author ldj
 * @version 1.0 2015年5月4日 下午4:17:29
 */
class DownLoadTransferRecordAdapter extends UpLoadTransferRecordAdapter{
	public DownLoadTransferRecordAdapter(Context ctx) {
		super(ctx);

		completeCountLabel = ctx.getResources().getString(R.string.transfer_complete_count);
		runningCountLabel = ctx.getResources().getString(R.string.download_ing_count);
		typeLbl = "下载";
	}
}