package com.pisen.router.core.filemanager.async;

import java.util.List;

import android.os.AsyncTask;

import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.ResourceManager;

/**
 * 获取给定目录的列表，当要刷新当前目录的时候，也可以调用此接口
 * @author mugabutie
 *
 */

public class OpenDirOrRefreshAsnyTask extends AsyncTask<Void, List<ResourceInfo>, List<ResourceInfo>> {
	private String operatorDir; //操作目录
	private ResourceManager resourceManager;
	public OpenDirOrRefreshAsnyTask(ResourceManager resourceManager,String source) {
		super();
		this.operatorDir = source;
		this.resourceManager = resourceManager;
	}

	@Override
	protected List<ResourceInfo> doInBackground(Void... params) {
		try {
			if(resourceManager.exists(operatorDir))
			{
			   return resourceManager.list(operatorDir);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
  @Override
  	protected void onPostExecute(List<ResourceInfo> result) {
  		super.onPostExecute(result);
  	}	
	
}
