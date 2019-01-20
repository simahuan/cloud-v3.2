package com.pisen.router.core.filemanager.async;

import java.util.ArrayList;
import java.util.List;

import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.studio.os.LogCat;

import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.ui.phone.resource.v2.panel.ISelectionActionBar;

/**
 * 
 * 移动资源异步任务，移动之后会删除原始资源
 * 
 * @author Liuhc
 * @version 1.0 2015年6月3日 下午8:18:33
 */
public class MoveAsyncTask extends ResourceAsyncProgressTask {

	private String destPath;
	private ISelectionActionBar<ResourceInfo> selectionActionBar;
	public MoveAsyncTask(IResource resourceManager, List<ResourceInfo> source, String destPath, ISelectionActionBar<ResourceInfo> selectionActionBar,ResourceItemCallback callback) {
		super(resourceManager, callback);
		this.sourceList = source;
		this.destPath = destPath;
		this.selectionActionBar = selectionActionBar;
		this.result.opeartorType = operatorType.Move;
	}

	@Override
	protected void doInBackground(ResourceResult result) {
		 List<String> sourcePath = new ArrayList<String>() ;
		for ( ResourceInfo info : sourceList) {
			sourcePath.add(info.path);
		}
		LogCat.e("===soureceList = " + sourceList.size());
		if (sourceList != null && !sourceList.isEmpty()) {
			try {
				for (ResourceInfo info : sourceList) {
					getSpaceCount(result, info,destPath);
				}
				LogCat.d("===文件总个数:===="+result.mTotal);
				if (result.mTotal <= 0) {
					result.mCount = 1;
					result.mTotal = 1;
					result.filename = "";
				}else{
					for (ResourceInfo info : sourceList) {
						move(result, info,destPath);
					}
				}
				
				moveOldFileDir(resourceManager,sourcePath); //移除旧文件Dir
				result.setmStatus(ResourceResult.Sucess);
				updateProgress(result,false); //更新进度
				
			} catch (Exception e) {
				e.printStackTrace();
				result.setmStatus(ResourceResult.UNKNOWN_ERROR);
				updateProgress(result,false);
			}
		}
	}

	/**
	 * @des   移除移动后文件夹
	 * @param sardineManager
	 * @param sourcePath
	 */
	public void moveOldFileDir(final IResource sardineManager,final List<String> sourcePath){
		AsyncTaskUtils.execute(new InBackgroundCallback<Boolean>() {
			@Override
			public Boolean doInBackground() {
				try {
					for (final String info : sourcePath) {
						sardineManager.delete(info);
					}
					return true;
				} catch (Exception e) {
					return false;
				}
			}

			@Override
			public void onPostExecute(Boolean result) {
				if (result) {
					selectionActionBar.onActionBarCompleted();
//					LogCat.d("=====移除成功======");
				} else {
//					LogCat.d("======移除失败========");
				}
			}
		});
	}
	
	/**
	 * 计算总空间大小和文件个数
	 * @param result
	 * @param info
	 * @throws Exception
	 */
	private void getSpaceCount(final ResourceResult result, final ResourceInfo info,final String destPath)throws Exception {
		if (info.isDirectory) {
			if (!resourceManager.exists(destPath + info.name + "/")) {
				// 生成新的目录
				resourceManager.createDir(destPath + info.name + "/");
			}
			String subPath = destPath + info.name + "/";
			List<ResourceInfo> infolist = resourceManager.list(info.path);
			for (ResourceInfo res : infolist) {
				getSpaceCount(result, res,subPath);
			}
		} else {
			result.mTotal ++;
//			result.mTotalBytes = result.mTotalBytes + info.size;
		}
	}
	
	protected void move(final ResourceResult result, final ResourceInfo info,final String destPath) throws Exception {
		if (info.isDirectory) {
			if (!resourceManager.exists(destPath + info.name + "/")) {
				// 生成新的目录
				resourceManager.createDir(destPath + info.name + "/");
			}
			String subPath = destPath + info.name + "/";
			List<ResourceInfo> infolist = resourceManager.list(info.path);
			for (ResourceInfo res : infolist) {
				move(result, res,subPath);
			}
		} else {
//			LogCat.e("=====移动单个文件进度.....");
			result.mCount++;
			result.filename = info.name;
			resourceManager.move(info.path, destPath + info.name);
			updateProgress(result,false); //更新进度
		}
	}
}
