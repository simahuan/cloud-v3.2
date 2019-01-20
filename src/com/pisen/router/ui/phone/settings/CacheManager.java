package com.pisen.router.ui.phone.settings;

import java.io.File;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.studio.os.LogCat;

import com.pisen.router.Helper;

/**
 * @author mahuan
 * @version 1.0 2015年5月19日 上午11:35:00
 */
public class CacheManager {
	public static final double CACHE_IGNORE_SIZE = 0.01 * 1024f * 1024f;
	public static final int CACHE_FILE_SIZE = 0x0;
	public static final int CACHE_FILE_CLEAR = 0x1;
	public static final int CACHE_FILE_NOT_CLEAR = 0x2;

	private int size = 0;
	private Handler mHandler;

	public static File CACHE_PATH; // 缓存保存地址

	public CacheManager(Context context, Handler mHandler) {
		this.mHandler = mHandler;
		CACHE_PATH = context.getExternalCacheDir();
	}

	public class UpdateTextTask extends AsyncTask<Integer,Object,Integer> {
		@Override
		protected Integer doInBackground(Integer... params) {
			size = 0;//首先清零
			File file = CACHE_PATH;
			if (file.exists()) {
				size = fileCount(file);
			}
			return size;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
//			LogCat.e("Cache_file_size = "+ result);
//			LogCat.e("Cache_file_size = "+ Helper.formatFromSize(result));
			Message msg = mHandler.obtainMessage();
			msg.what = CACHE_FILE_SIZE;
			msg.arg1 = size;
			mHandler.sendMessage(msg);
		}
	}

	public class delTextTask extends AsyncTask {
		@Override
		protected Object doInBackground(Object... params) {
			File file = CACHE_PATH;
			if (file.exists()) {
				deleteDir(file);
			}
			return null;
		}

		@Override
		protected void onPostExecute(Object result) {
			if (size < CacheManager.CACHE_IGNORE_SIZE) {
				mHandler.sendEmptyMessage(CACHE_FILE_NOT_CLEAR);
			} else {
				Message msg = mHandler.obtainMessage();
				msg.what = CACHE_FILE_CLEAR;
				msg.arg1 = size;
				mHandler.sendMessage(msg);
				size = 0;
			}
		}
	}

	private int fileCount(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				fileCount(files[i]);
			}
		} else {
			size += file.length();
		}
		return size;
	}

	private void deleteDir(File file) {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				deleteDir(files[i]);
			}
		} else {
			file.delete();
		}
	}
}
