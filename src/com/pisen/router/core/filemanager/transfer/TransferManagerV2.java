package com.pisen.router.core.filemanager.transfer;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.studio.os.LogCat;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;
import android.util.Log;

import com.pisen.router.CloudApplication;
import com.pisen.router.core.filemanager.ResourceInfo;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件传输管理
 * @author ldj
 */
public class TransferManagerV2 implements ITransferManager {
	private static final String TAG = TransferManagerV2.class.getSimpleName();
	private static final boolean DEBUG = true;

	private Context context;
	private ContentResolver resolver;
	private ExecutorService downloadExecutor;
	private ExecutorService uploadExecutor;
	private LongSparseArray<TransferInfo> transfersTasks;
	private static TransferManagerV2 instance;

	private TransferManagerV2(Context context) {
		super();
		this.context = context;

		init();
		initUnFinishTask();
	}

	public static TransferManagerV2 getInstance(Context context) {
		if (instance == null) {
			instance = new TransferManagerV2(context);
		}
		return instance;
	}

	private void init() {
		resolver = context.getContentResolver();
		transfersTasks = new LongSparseArray<TransferInfo>();
		downloadExecutor = Executors.newFixedThreadPool(1);
		uploadExecutor = Executors.newFixedThreadPool(1);
	}

	public LongSparseArray<TransferInfo> getTransfersTask() {
		return transfersTasks;
	}

	/**
	 * 加入新上传的任务
	 * 
	 * @param infos
	 * @deprecated
	 */
	public void addUploadTask(List<ResourceInfo> infos) {
		for (ResourceInfo iterator : infos) {
			long id = insertToDatabase(iterator, TransferCTag.Upload);
			final Cursor cursor = resolver.query(TransferProvider.CONTENT_URI, null, TransferInfo.Table._ID + "=?", new String[] { String.valueOf(id) }, null);
			try {
				if (cursor.moveToNext()) {
					TransferInfo info = TransferInfo.newTransferInfo(cursor);
					enqueue(info);
				}
			} finally {
				cursor.close();
			}
		}
	}

	public void addJpjcUploadTask(String url, List<ResourceInfo> infos) {
		addUploadTask(url, infos);
	}

	/**
	 * 
	 * @param url  上传 数据地址
	 * @param infos
	 */
	public void addUploadTask(String url, List<ResourceInfo> infos) {
		context.sendBroadcast(new Intent(UploadSardineTask.ACTION_ADD));
		for (ResourceInfo iterator : infos) {
			iterator.destPath = url;
			long id = insertToDatabase(iterator, TransferCTag.Upload);
			final Cursor cursor = resolver.query(TransferProvider.CONTENT_URI, null, TransferInfo.Table._ID + "=?", new String[] { String.valueOf(id) }, null);
			try {
				if (cursor.moveToNext()) {
					TransferInfo info = TransferInfo.newTransferInfo(cursor);
					enqueue(info);
				}
			} finally {
				cursor.close();
			}
		}
	}

	/**
	 * 加入新  下载任务
	 * 
	 * @param infos
	 */
	public void addDownloadTask(List<ResourceInfo> infos) {
		context.sendBroadcast(new Intent(UploadSardineTask.ACTION_ADD));
		for (ResourceInfo iterator : infos) {
			long id = insertToDatabase(iterator, TransferCTag.Download);
			final Cursor cursor = resolver.query(TransferProvider.CONTENT_URI, null, TransferInfo.Table._ID + "=?", new String[] { String.valueOf(id) }, null);
			try {
				if (cursor.moveToNext()) {
					TransferInfo info = TransferInfo.newTransferInfo(cursor);
					enqueue(info);
				}
			} finally {
				cursor.close();
			}
		}
	}
	
	public void addDownloadTask(ResourceInfo info) {
		context.sendBroadcast(new Intent(UploadSardineTask.ACTION_ADD));
		long id = insertToDatabase(info, TransferCTag.Download);
		final Cursor cursor = resolver.query(TransferProvider.CONTENT_URI, null, TransferInfo.Table._ID + "=?", new String[] { String.valueOf(id) }, null);
		try {
			if (cursor.moveToNext()) {
				TransferInfo t = TransferInfo.newTransferInfo(cursor);
				enqueue(t);
			}
		} finally {
			cursor.close();
		}
	}

	public long insertToDatabase(ResourceInfo filebean, TransferCTag CTag) {
		ContentValues cv = new ContentValues();
		cv.put(TransferInfo.Table.filename, filebean.name);
		if (TextUtils.isEmpty(filebean.destPath)) {// 默认存储目录
			filebean.destPath = CloudApplication.DOWNLOAD_PATH.getAbsolutePath();
		}
		cv.put(TransferInfo.Table.storageDir, filebean.destPath);
		cv.put(TransferInfo.Table.filesize, filebean.size);
		cv.put(TransferInfo.Table.ssid, "");
		cv.put(TransferInfo.Table.currentBytes, 0);
		cv.put(TransferInfo.Table.dataCreated, System.currentTimeMillis());
		cv.put(TransferInfo.Table.takeControl, TransferControl.START.value);
		cv.put(TransferInfo.Table.status, TransferStatus.PENDING.value);
		cv.put(TransferInfo.Table.ctag, CTag.value);
		cv.put(TransferInfo.Table.url, filebean.path);
		cv.put(TransferInfo.Table.lastUpdated, System.currentTimeMillis());
		cv.put(TransferInfo.Table.isDir, filebean.isDirectory);
		cv.put(TransferInfo.Table.inboxRecordDeleted, 0);
		return ContentUris.parseId(resolver.insert(TransferProvider.CONTENT_URI, cv));
	}

	/**
	 * 开始上次未完成任务
	 */
	private void initUnFinishTask() {
		if (DEBUG)
			Log.e(TAG, "===initUnFinishTask===");
		Cursor cursor = resolver.query(TransferProvider.CONTENT_URI,
						null,
						"(" + TransferInfo.Table.ctag + "=? or " + TransferInfo.Table.ctag + "=? )and " + TransferInfo.Table.takeControl + "=? and ("
								+ TransferInfo.Table.status + "=? or " + TransferInfo.Table.status + "=? )",
						new String[] { String.valueOf(TransferCTag.Download.value), String.valueOf(TransferCTag.Upload.value),
								String.valueOf(TransferControl.START.value), String.valueOf(TransferStatus.PENDING.value),
								String.valueOf(TransferStatus.RUNNING.value) }, null);
		try {
			while (cursor.moveToNext()) {
				TransferInfo info = TransferInfo.newTransferInfo(cursor);
				info.status = TransferStatus.PENDING;// 需更新running态的数据为pending
				info.currentBytes = 0;// 不支持断点续传，需置0
				restartTransfer(info);
			}
		} finally {
			if(cursor != null)cursor.close();
		}
	}

	@Override
	public void enqueue(TransferInfo info) {
		if (DEBUG)
			LogCat.e("=====enqueue======");
		transfersTasks.put(info._id, info);
		addTask(info);
	}

	@Override
	public void restartTransfer(long id) {
		final Cursor cursor = resolver.query(TransferProvider.CONTENT_URI, null, TransferInfo.Table._ID + "=?", new String[] { String.valueOf(id) }, null);
		try {
			if (cursor.moveToNext()) {
				restartTransfer(TransferInfo.newTransferInfo(cursor));
			}
		} finally {
			cursor.close();
		}
	}

	/**
	 * 重新开始任务
	 * 
	 * @param info
	 */
	private synchronized void restartTransfer(TransferInfo info) {
		if (DEBUG)
			Log.d(TAG, "===restartTransfer===");
		if (info != null) {
			TransferInfo tmp = transfersTasks.get(info._id);
			if (tmp != null) {
				Log.d("restartTransfer", "find old data!!");
				tmp.status = TransferStatus.PAUSE;
				tmp.takeControl = TransferControl.PAUSE;
				transfersTasks.remove(tmp._id);
				tmp = null;
			}

			transfersTasks.put(info._id, info);
			info.status = TransferStatus.PENDING;
			info.takeControl = TransferControl.START;
			info.currentBytes = 0;
			 //删除本地文件
//			 if(info.ctag == TransferCTag.Download)
//			 deleteFileIfExists(CloudApplication.DOWNLOAD_PATH + "/" +
//			 info.filename);
			
//			LogCat.e("transferInfo.storageDir = "+info.storageDir);
			addTask(info);
			// 更新数据库
			updateDatabase(info);
		}
	}

	/**
	 * 恢复传输（断点续传）
	 */
	@Override
	public void resumeTransfer(long id) {
	}

	@Override
	public synchronized void pauseTransfer(long id) {
		if (DEBUG)
			Log.e(TAG, "===pauseTransfer===");
		TransferInfo info = transfersTasks.get(id);
		if (info != null) {
			// transfersTasks.remove(id);
			info.takeControl = TransferControl.PAUSE;
			info.status = TransferStatus.PAUSE;
			
//			//删除Router端文件
//			if (info.ctag == TransferCTag.Upload) {
//				deleteFileIfExists(info.storageDir + "/" + info.filename);
//				LogCat.e("transferInfo.storageDir = "+info.storageDir);
//			}

			// 更新数据库
			updateDatabase(info);
			sendBroadCast(info);
		} else {
			if (DEBUG)
				Log.e(TAG, "pauseTransfer, but can  not find data");
		}
	}

	@Override
	public void deletedTransfer(long id) {
		if (DEBUG)
			Log.e(TAG, "===deletedTransfer===");
		TransferInfo info = transfersTasks.get(id);
		if (info == null) {
			final Cursor cursor = resolver.query(TransferProvider.CONTENT_URI, null, TransferInfo.Table._ID + "=?", new String[] { String.valueOf(id) }, null);
			try {
				if (cursor.moveToNext()) {
					info = TransferInfo.newTransferInfo(cursor);
				}
			} finally {
				cursor.close();
			}
		}
		if (info != null) {
			// transfersTasks.remove(info._id);
			info.takeControl = TransferControl.DELETE;
			// 更新数据库
			resolver.delete(info.getTransferUri(), TransferInfo.Table._ID + " == " + info._id, null);
			sendBroadCast(info);

			// 如果为下载，则删除本地文件
			if (info.ctag == TransferCTag.Download) {
				deleteFileIfExists(CloudApplication.DOWNLOAD_PATH + "/" + info.filename);
				deleteFileIfExists(CloudApplication.DOWNLOAD_PATH + "/" + info.filename+".tmp");
			}
//			else if (info.ctag == TransferCTag.Upload){
//				deleteFileIfExists(info.storageDir + "/" + info.filename); 
//				deleteFileIfExists(info.storageDir + "/" + info.filename+".tmp");
//			}
		} else {
			Log.e("deletedTransfer", "deletedTransfer called,but can not find the info!!!");
		}
	}

	private void deleteFileIfExists(String path) {
		if (!TextUtils.isEmpty(path)) {
			LogCat.e("deleteFileIfExists() deleting " + path);
			final File file = new File(path);
			if (file.exists() && !file.delete()) {
				LogCat.e("file: '" + path + "' couldn't be deleted");
			}
		}else {
			LogCat.e("删除路径为空");
		}
	}

	/**
	 * 刷新任务
	 * 
	 * @param info
	 */
	private void addTask(TransferInfo info) {
		// 发送广播
		sendBroadCast(info);
		TransferTask task = null;
		switch (info.ctag) {
		case Upload:
			task = new UploadSardineTask(context, resolver, info);
			uploadExecutor.execute(task);
			break;
		case Download:
			task = new DownloadSardineTask(context, resolver, info);
			downloadExecutor.execute(task);
			break;
		default:
			break;
		}
	}

	private void sendBroadCast(TransferInfo info) {
		String action;
		if (info.ctag == TransferCTag.Download) {
			action = DownloadSardineTask.ACTION_PROGRESS;
		} else {
			action = UploadSardineTask.ACTION_PROGRESS;
		}
		Intent intent = new Intent(action);
		intent.setPackage(context.getPackageName());
		intent.putExtra(TransferTask.EXTRA_TRANSFER_STATUS, info.status);
		intent.putExtra(TransferTask.EXTRA_TRANSFER_ID, info._id);
		intent.putExtra(TransferTask.EXTRA_TOTAL_BYTES, info.filesize);
		intent.putExtra(TransferTask.EXTRA_CURRENT_BYTES, info.currentBytes);
		context.sendBroadcast(intent);
	}

	/**
	 * 更新数据库数据
	 * 
	 * @param info
	 */
	private void updateDatabase(TransferInfo info) {
		ContentValues values = new ContentValues();
		values.put(TransferInfo.Table.takeControl, info.takeControl.value);
		values.put(TransferInfo.Table.status, info.status.value);
		values.put(TransferInfo.Table.currentBytes, info.currentBytes);
		values.put(TransferInfo.Table.filesize, info.filesize);
		values.put(TransferInfo.Table.lastUpdated, System.currentTimeMillis());
		resolver.update(info.getTransferUri(), values, TransferInfo.Table._ID + " == " + info._id, null);
	}
}
