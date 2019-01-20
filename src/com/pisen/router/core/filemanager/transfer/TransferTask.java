package com.pisen.router.core.filemanager.transfer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.os.Process;
import android.os.SystemClock;
import android.studio.os.LogCat;
import android.text.format.DateUtils;
import android.util.Log;

import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.SardineCacheResource;

/**
 * 传输任务
 * 
 * @author yangyp
 */
public abstract class TransferTask implements Runnable {

	static final String TAG = "TransferTask";
	public static final String EXTRA_TRANSFER_ID = "extra_transfer_id";
	public static final String EXTRA_TOTAL_BYTES = "extra_total_bytes";
	public static final String EXTRA_CURRENT_BYTES = "extra_current_bytes";
	public static final String EXTRA_TRANSFER_STATUS = "extra_transfer_status";

	// 失败请求数
	static final int MAX_REDIRECTS = 3;
	public static final int BUFFER_SIZE = 8096;
	public static final int DEFAULT_TIMEOUT = (int) (20 * DateUtils.SECOND_IN_MILLIS);

	public static final int MIN_PROGRESS_STEP = 4 * 1024;
	public static final long MIN_PROGRESS_TIME = 500;

	protected final Context mContext;
	protected TransferInfo mInfo;
	protected List<TransferInfo> recursion;
	// 通知更新
	private long mLastUpdateBytes = 0;
	private long mLastUpdateTime;

	private String storageDirBak;
	protected IResource sardine;

	public TransferTask(Context context, TransferInfo info) {
		mContext = context;
		mInfo = info;
		storageDirBak = mInfo.storageDir;
		sardine = new SardineCacheResource();
	}

	/**
	 * 更新数据库 ，内容提供者自动更新进度
	 */
	public void writeToDatabase() {
		// mContext.getContentResolver().update(mInfo.getTransferUri(),
		// mInfo.toContentValues(), null, null);
	}

	/**
	 * 更新进度
	 */
	public void writeToDatabaseOrThrow() throws TransferException {
		if (mContext.getContentResolver().update(mInfo.getTransferUri(), mInfo.toContentValues(), TransferInfo.Table._ID + " == " + mInfo._id, null) == 0
				&& mInfo.takeControl != TransferControl.DELETE) {
			throw new TransferException(TransferStatus.CANCELED, "Download deleted or missing!");
		}
	}

	@Override
	public void run() {
		Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
		PowerManager.WakeLock wakeLock = null;
		final PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
		mInfo.status = TransferStatus.RUNNING;
		try {
			wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "transfer");
			wakeLock.acquire();
			checkConnectivity();
			dispatchExecuteTransfer(mInfo);
			mInfo.status = TransferStatus.SUCCESS;
		} catch (TransferException e) {
			e.printStackTrace();
			mInfo.status = e.getFinalStatus();
			mInfo.currentBytes = 0;
		} catch (Exception e) {
			e.printStackTrace();
			mInfo.status = TransferStatus.NET_NO_CONNECTION_ERROR;
			mInfo.currentBytes = 0;
		} catch (Throwable t) {
			t.printStackTrace();
			mInfo.status = TransferStatus.UNKNOWN_ERROR;
			mInfo.currentBytes = 0;
		} finally {
			if (mInfo.status != TransferStatus.PAUSE) {
				try {
					finalizeDestination();
				} catch (Exception e) {
				}
			}
			if (wakeLock != null) {
				wakeLock.release();
				wakeLock = null;
			}
		}
	}

	/**
	 * 检查当前网络连接是有效的。
	 */
	protected void checkConnectivity() throws TransferException {
		ConnectivityManager connectivity = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if (info == null || info.getType() != ConnectivityManager.TYPE_WIFI) {
			throw new TransferException(TransferStatus.NET_NO_CONNECTION_ERROR, "NET_NO_CONNECTION_ERROR");
		}
	}

	/**
	 * @des   派发执行任务
	 * @param info
	 * @throws Exception
	 */
	protected void dispatchExecuteTransfer(TransferInfo info) throws Exception {
		if (info.isDir) {
			addRecursionList(info);
			for (TransferInfo infoTemp : recursion) {
				executeTransfer(infoTemp);
			}
		} else {
			executeTransfer(info);
		}

	}

	protected void addRecursionList(TransferInfo info) throws Exception {
		if (recursion == null)
			recursion = new ArrayList<TransferInfo>();

		if (info.isDir) {
			switch (info.ctag) {
			case Upload:
				info.storageDir = String.format("%s%s/", info.storageDir, info.filename);
				break;
			case Download:
				info.storageDir = String.format("%s/%s/", info.storageDir, info.filename);
				break;
			default:
				break;
			}
			
			checkDownloadFile(info);
			List<TransferInfo> listFiles = listFile(info.url);
			if (listFiles != null) {
				for (TransferInfo _t : listFiles) {
					_t.storageDir = info.storageDir;
					addRecursionList(_t);
				}
			}
		} else {
			mInfo.totalBytes += info.filesize;
			mInfo.filesize = mInfo.totalBytes;
			recursion.add(info);
		}
	}

	/**
	 * 根据URL获取当前目录下所有文件或子目录
	 * 
	 * @param url
	 * @return
	 */
	protected abstract List<TransferInfo> listFile(String url) throws Exception;

	/**
	 * 执行下载操作
	 */
	protected abstract void executeTransfer(TransferInfo info) throws Exception;

	/**
	 * 检测文件夹是否存在，不存在则创建
	 * 
	 * @param info
	 * @throws TransferException
	 */
	protected abstract void checkDownloadFile(TransferInfo info) throws TransferException;

	/**
	 * 线程结束之前调用
	 */
	protected void finalizeDestination() {
		notifyUpdateProgress(mContext, mInfo);
	}

	/**
	 * 检查暂停或取消
	 * 
	 * @param sardine
	 */
	public void checkPausedOrCanceled() throws TransferException {
		synchronized (mInfo) {
			mInfo.storageDir = storageDirBak;
			if (mInfo.takeControl == TransferControl.PAUSE) {
				throw new TransferException(TransferStatus.PAUSE, "download paused by owner");
			}
			if (mInfo.status == TransferStatus.CANCELED || mInfo.takeControl == TransferControl.DELETE) {
				throw new TransferException(TransferStatus.CANCELED, "download canceled");
			}
			if (mInfo.status == TransferStatus.UNKNOWN_ERROR) {
				throw new TransferException(TransferStatus.UNKNOWN_ERROR, "download error by service");
			}
		}
	}

	/**
	 * 更新上传任务进度
	 * 
	 * @param info
	 * 
	 * @param mInfoDelta
	 */
	protected void updateProgress(TransferInfo info) throws IOException, TransferException {
		final long now = SystemClock.elapsedRealtime();
		final long currentBytes = mInfo.currentBytes;
		if (currentBytes - mLastUpdateBytes > MIN_PROGRESS_STEP && now - mLastUpdateTime > MIN_PROGRESS_TIME) {
			mLastUpdateBytes = currentBytes;
			mLastUpdateTime = now;
			notifyUpdateProgress(mContext, info);
		}
	}

	protected abstract String notifyUpdateProgressAction();

	/**
	 * 发送广播通知进度更新
	 * 
	 * @param context
	 * @param infoId
	 * @param status
	 * @param progress
	 */
	protected void notifyUpdateProgress(Context context, TransferInfo info) {
		writeToDatabaseOrThrow();
		Intent intent = new Intent(notifyUpdateProgressAction());
		intent.setPackage(context.getPackageName());
		intent.putExtra(EXTRA_TRANSFER_STATUS, info.status);
		intent.putExtra(EXTRA_TRANSFER_ID, info._id);
		intent.putExtra(EXTRA_TOTAL_BYTES, info.filesize);
		intent.putExtra(EXTRA_CURRENT_BYTES, info.currentBytes);
		context.sendBroadcast(intent);
	}

	protected void logDebug(String msg) {
		LogCat.i("[" + mInfo._id + "] " + msg);
	}

	protected void logError(String msg, Throwable t) {
		LogCat.e("[" + mInfo._id + "] " + msg, t);
	}

}
