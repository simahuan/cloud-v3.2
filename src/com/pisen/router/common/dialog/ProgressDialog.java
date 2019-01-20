package com.pisen.router.common.dialog;

import java.text.NumberFormat;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pisen.router.R;

/**
 * @author mahuan
 * @des 确定型进度加载
 * @version 1.0 2015年5月11日 下午4:14:36
 */
public class ProgressDialog extends CustomDialog implements OnClickListener {

	private ProgressBar mProgress;
	private TextView mProgressNumber;
	private TextView mProgressPercent;
	private TextView txtTitle,txtFileName;

	private int mMax;
	private int mProgressVal;
	private Handler mViewUpdateHandler;

	public ProgressDialog(Context context) {
		super(context);
	}

	public static ProgressDialog show(Context context, CharSequence title, CharSequence message) {
		return show(context, title, message, null);
	}

	public static ProgressDialog show(Context context, CharSequence title, CharSequence message, OnCancelListener cancelListener) {
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setTitle(title);
		dialog.setCancelable(false);
		dialog.setOnCancelListener(cancelListener);
		dialog.show();
		return dialog;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_dialog_progress);
		mViewUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				int progress = mProgress.getProgress();
				int max = mProgress.getMax();
				// mProgressNumber.setText(text);

				double percent = (double) progress / (double) max;
				String tmp = NumberFormat.getPercentInstance().format(percent);
				mProgressPercent.setText(tmp);
				mProgressNumber.setText(String.format("%s/%s", progress, max));
			}
		};

		mProgress = (ProgressBar) findViewById(R.id.progressBar1);
		mProgressNumber = (TextView) findViewById(R.id.progressNumber);
		mProgressPercent = (TextView) findViewById(R.id.progressPercent);
		txtTitle = (TextView) findViewById(R.id.txtTitle);
		txtFileName = (TextView) findViewById(R.id.txtFileName);
		mViewUpdateHandler.sendEmptyMessage(0);

//		findViewById(R.id.btnCancel).setOnClickListener(this);
	}

	public void setTitle(String title){
		if (txtTitle != null) {
			if (TextUtils.isEmpty(title)) {
				txtTitle.setText("请稍候");
			}else{
				txtTitle.setText(title);
				txtTitle.setVisibility(View.VISIBLE);
			}
		}
	}
	
	public void setFileName(String name){
		if (txtFileName != null) {
			if (TextUtils.isEmpty(name)) {
				txtFileName.setVisibility(View.GONE);
			}else{
				txtFileName.setText("正在移动 "+name);
				txtFileName.setVisibility(View.VISIBLE);
			}
		}
	}
	
	public void setProgress(int value) {
		if (mProgress != null) {
			mProgress.setProgress(value);
			onProgressChanged();
		} else {
			mProgressVal = value;
		}
	}

	public int getProgress() {
		if (mProgress != null) {
			return mProgress.getProgress();
		}
		return mProgressVal;
	}

	public int getMax() {
		if (mProgress != null) {
			return mProgress.getMax();
		}
		return mMax;
	}

	public void setMax(int max) {
		if (mProgress != null) {
			mProgress.setMax(max);
			onProgressChanged();
		} else {
			mMax = max;
		}
	}

	private void onProgressChanged() {
		if (mViewUpdateHandler != null && !mViewUpdateHandler.hasMessages(0)) {
			mViewUpdateHandler.sendEmptyMessage(0);
		}
	}
	
	public void setProgressText(int value) {
		if (mProgress != null) {
			mProgress.setProgress(value);
		} 
		mProgressPercent.setText(value+"%");
		
	}
	
	public void setMaxText(int cur,int total) {
		mProgressNumber.setText(String.format("%s/%s", cur, total));
	}

	@Override
	public void onClick(View v) {

	}

}
