package com.pisen.router.core.filemanager.async;

import java.io.IOException;
import java.util.List;

import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.ResourceException;
import com.pisen.router.core.filemanager.ResourceInfo;

/**
 * 删除目标异步任务
 * 
 * @author mugabutie
 *
 */
public class DeleteAsyncTask extends ResourceAsyncProgressTask {
	public DeleteAsyncTask(IResource resourceManager, List<ResourceInfo> source, ResourceItemCallback callback) {
		super(resourceManager, callback);
		this.sourceList = source;
	}

	@Override
	protected void doInBackground(ResourceResult result) throws ResourceException, Exception {
		for (ResourceInfo info : sourceList) {
			checkCancelled();
			doDelete(result, info);
		}
	}

	/**
	 * @des  删除文件
	 * @param result
	 * @param info
	 * @throws ResourceException
	 * @throws Exception
	 * @throws IOException
	 */
	private void doDelete(ResourceResult result, ResourceInfo info) throws ResourceException, Exception, IOException {
		resourceManager.delete(info.path);
		result.mPath = info.path;
		updateProgress(result);
	}

}
