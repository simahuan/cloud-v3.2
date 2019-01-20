package com.pisen.router.core.filemanager.async;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.studio.io.CountingInputStreamEntity;
import android.studio.io.CountingOutputStream.CountingListener;
import android.studio.os.LogCat;

import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.ResourceException;
import com.pisen.router.core.filemanager.ResourceInfo;

/**
 * 复制带进度
 * @author Liuhc
 * @version 1.0 2015年6月11日 下午6:10:07
 */
public class CopyAsyncTask extends ResourceAsyncProgressTask {
	private String destPath; // 复制之后的目标路径

	public CopyAsyncTask(IResource resourceManager, List<ResourceInfo> source, String destPath, ResourceItemCallback callback) {
		super(resourceManager, callback);
		this.sourceList = source;
		this.destPath = destPath;
	}

	@Override
	protected void doInBackground(ResourceResult result) throws ResourceException, Exception {
		
		for (ResourceInfo info : sourceList) {
			getSpaceCount(result, info,destPath);
		}
		LogCat.e("文件总个数:"+result.mTotal);
		LogCat.e("文件大小:"+result.mTotalBytes);
		
		for (ResourceInfo info : sourceList) {
			checkCancelled();
			copy(result, info,destPath);
		}
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
			result.mTotalBytes = result.mTotalBytes + info.size;
		}
	}
	
	protected void copy(final ResourceResult result, final ResourceInfo info,final String destPath) throws Exception {
		if (info.isDirectory) {
			if (!resourceManager.exists(destPath + info.name + "/")) {
				// 生成新的目录
				resourceManager.createDir(destPath + info.name + "/");
			}
			String subPath = destPath + info.name + "/";
			List<ResourceInfo> infolist = resourceManager.list(info.path);
			for (ResourceInfo res : infolist) {
				copy(result, res,subPath);
			}
		} else {
			result.mCount++;
			String operatorDestPath = getDestAddress(destPath, info.name);
			final InputStream inStream = resourceManager.get(info.path);
			resourceManager.put(operatorDestPath, new CountingInputStreamEntity(inStream, -1, new CountingListener() {
				@Override
				public void onChange(long counterBytes, int bytesRead) throws IOException {
					try {
						checkCancelled();
						result.mCurrentBytes += bytesRead;
						result.filename = info.name;
						updateProgress(result);
						if (counterBytes == info.size) {
							updateProgress(result, false);
						}
					} catch (ResourceException e) {
						result.mStatus = e.getStatus();
					} 
				}
			}));
		}
	}

	public String getDestAddress(String preAddress, String filename) {
		return preAddress + filename;
	}
}
