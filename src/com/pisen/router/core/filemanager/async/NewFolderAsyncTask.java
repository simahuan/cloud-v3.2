package com.pisen.router.core.filemanager.async;

/**
 * 新建资源文件异步任务，建立文件夹
 */
import com.pisen.router.core.filemanager.ResourceManager;

public class NewFolderAsyncTask extends ResourceAsyncTask {

	private String dir = "";

	public NewFolderAsyncTask(ResourceManager resourceManager, String dir) {
		super();
		this.resourceManager = resourceManager;
		this.dir = dir;
		this.result.opeartorType = operatorType.NewFolder;
	}

	@Override
	protected ResourceResult doInBackground(Void... params) {
		try {
			resourceManager.createDir(this.dir);
		} catch (Exception e) {
			e.printStackTrace();
			result.setmStatus(ResourceResult.UNKNOWN_ERROR);
		}
		result.setmStatus(ResourceResult.Sucess);
		return result;
	}
}
