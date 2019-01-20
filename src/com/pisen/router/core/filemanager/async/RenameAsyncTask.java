package com.pisen.router.core.filemanager.async;
/**
 * 资源重命名
 */
import com.pisen.router.core.filemanager.ResourceException;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.ResourceManager;
public class RenameAsyncTask extends ResourceAsyncResultTask {
	private String newName = ""; //新的资源文件名称
	public RenameAsyncTask(ResourceManager resourceManager, ResourceInfo source, String newName,  ResourceItemCallback callback) {
		super(resourceManager, callback);
		this.resourceManager = resourceManager;
		this.sourceSignal = source;
		this.newName = newName;
		this.result.opeartorType = operatorType.Rename;
	}
	
	@Override
	protected void doInBackground(ResourceResult result)
			throws ResourceException, Exception {
		 try {
				resourceManager.rename(sourceSignal.path, this.newName);
			  } catch (Exception e) {
				 e.printStackTrace();
				 result.setmStatus(ResourceResult.UNKNOWN_ERROR);
			  }
			   result.setmStatus(ResourceResult.Sucess);
	}
 
}
