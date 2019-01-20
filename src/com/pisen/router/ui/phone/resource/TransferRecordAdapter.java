package com.pisen.router.ui.phone.resource;


import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.transfer.TransferInfo.Table;

public class TransferRecordAdapter extends CursorAdapter {
	private LayoutInflater inflater;
	public TransferRecordAdapter(Context context, Cursor c) {
		super(context, c);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LinearLayout progresslay = (LinearLayout) inflater.inflate(R.layout.filetransfer_item_content, null);
		ProgressBar pogress = (ProgressBar) progresslay.findViewById(R.id.progress_bar);
		return progresslay;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		LinearLayout progresslay = (LinearLayout) inflater.inflate(R.layout.filetransfer_item_content, null);
		ProgressBar pogress = (ProgressBar) progresslay.findViewById(R.id.progress_bar);
		Log.i("currentBytes","currentBytes: "+cursor.getColumnIndex(Table.currentBytes));
		pogress.setProgress(cursor.getInt(cursor.getColumnIndex(Table.currentBytes)));
	}

	class volder
	{
		ProgressBar progress;
	}
}
