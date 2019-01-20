package com.pisen.router.core.filemanager.async;

import android.os.SystemClock;
import android.studio.os.LogCat;

import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.ResourceException;

public abstract class ResourceAsyncProgressTask extends ResourceAsyncTask {

	public static final int MIN_PROGRESS_STEP = 2 * 1024;
	public static final long MIN_PROGRESS_TIME = 100;

	// 通知更新
	private long mLastUpdateBytes;
	private long mLastUpdateTime;

	public ResourceAsyncProgressTask(IResource resourceManager, ResourceItemCallback callback) {
		super();
		this.resourceManager = resourceManager;
		this.itemCallback = callback;
	}

	public void setItemCallback(ResourceItemCallback callback) {
		this.itemCallback = callback;
	}

	@Override
	protected final ResourceResult doInBackground(Void... params) {
		ResourceResult result = new ResourceResult();
		try {
			LogCat.i("doInBackground");
			checkConnectivity();
			doInBackground(result);
		} catch (ResourceException e) {
			result.mStatus = e.getStatus();
		} catch (Throwable t) {
			result.mStatus = ResourceResult.UNKNOWN_ERROR;
		}
		return result;
	}

	private void checkConnectivity() {

	}

	protected abstract void doInBackground(ResourceResult result) throws ResourceException, Exception;

	/**
	 * 检查用户是否取消
	 * 
	 * @throws ResourceException
	 */
	protected void checkCancelled() throws ResourceException {
		if (isCancelled()) {
			ResourceException.throwException(ResourceResult.CANCELED, "use cancel.");
		}
	}

	/**
	 * 更新任务进度
	 * 
	 * @param result
	 */
	protected void updateProgress(ResourceResult result) {
		long now = SystemClock.elapsedRealtime();
		if (now - mLastUpdateTime > MIN_PROGRESS_TIME) {
			mLastUpdateTime = now;
			publishProgress(result);
		}
	}
	
	/**
	 * 更新任务进度
	 * 是否延迟
	 * @param result
	 */
	protected void updateProgress(ResourceResult result,boolean isDelay) {
		if (isDelay) {
			long now = SystemClock.elapsedRealtime();
			if (now - mLastUpdateTime > MIN_PROGRESS_TIME) {
				mLastUpdateTime = now;
				publishProgress(result);
			}
		}else{
			publishProgress(result);
		}
	}

	@Override
	protected void onProgressUpdate(ResourceResult... values) {
		super.onProgressUpdate(values);
		if (itemCallback != null && values[0] != null) {
			itemCallback.onItemCallback(values[0]);
		}
	}

	@Override
	protected void onPostExecute(ResourceResult result) {
		super.onPostExecute(result);
		if (itemCallback != null) {
			itemCallback.onItemCallback(result);
		}
	}

}
