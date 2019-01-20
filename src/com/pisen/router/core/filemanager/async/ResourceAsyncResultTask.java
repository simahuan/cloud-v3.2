package com.pisen.router.core.filemanager.async;

import java.io.IOException;

import android.studio.os.LogCat;

import com.pisen.router.core.filemanager.ResourceException;
import com.pisen.router.core.filemanager.ResourceManager;

public abstract class ResourceAsyncResultTask extends ResourceAsyncTask {

	public ResourceAsyncResultTask(ResourceManager resourceManager, ResourceItemCallback callback) {
		super();
		this.resourceManager = resourceManager;
		this.itemCallback = callback;
	}



	@Override
	protected ResourceResult doInBackground(Void... params) {
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
	protected void updateProgress(ResourceResult result) throws IOException, ResourceException {
	
	}

	@Override
	protected void onProgressUpdate(ResourceResult... values) {
		super.onProgressUpdate(values);
		if (itemCallback != null) {
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
