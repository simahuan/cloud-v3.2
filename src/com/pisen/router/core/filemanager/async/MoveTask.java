package com.pisen.router.core.filemanager.async;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;
import android.studio.io.CountingInputStreamEntity;
import android.studio.io.CountingOutputStream.CountingListener;
import android.studio.os.LogCat;

import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.ResourceException;
import com.pisen.router.core.filemanager.ResourceInfo;

/**
 * 移动并删除
 * @author Liuhc
 * @version 1.0 2015年6月4日 下午3:44:14
 */
public class MoveTask extends ResourceAsyncProgressTask {
	private String destPath; // 复制之后的目标路径
	private boolean isMoveing = false;
	private ResourceResult res = null;
	private List<ResourceInfo> queueList = new ArrayList<ResourceInfo>();
	
	public MoveTask(IResource resourceManager, List<ResourceInfo> source, String destPath, ResourceItemCallback callback) {
		super(resourceManager, callback);
		this.sourceList = source;
		this.destPath = destPath;
	}

	@Override
	protected void doInBackground(ResourceResult result) throws ResourceException, Exception {
		res = new ResourceResult();
		for (ResourceInfo info : sourceList) {
			checkCancelled();
			copy(info,destPath);
		}
		isMoveing = false;
		startMove();
	}

	protected void copy(final ResourceInfo info,String destPath) throws Exception {
		if (info.isDirectory) {
			String dpath = destPath;
			if (!resourceManager.exists(dpath + info.name + "/")) {
				// 生成新的目录
				resourceManager.createDir(dpath + info.name + "/");
			}
			dpath = dpath + info.name + File.separator;
			List<ResourceInfo> infolist = resourceManager.list(info.path);
			for (ResourceInfo res : infolist) {
				copy(res,dpath);
			}
		} else {
			info.destPath = destPath;
			res.mTotalBytes += info.size;
			queueList.add(info);
		}
	}
	
	/**
	 * 移动
	 */
	public void startMove(){
		if (isMoveing) {
			return;
		}
		if (queueList == null) {
			return;
		}
		if (queueList.isEmpty()) {
			//没有内容
			return;
		}
		
		new Thread(new Runnable() {
			public void run() {
				if (res.mTotal <= 0) {
					res.mTotal = queueList.size();
				}
				res.mCount = (res.mTotal - queueList.size())+1;
				final ResourceInfo info = queueList.remove(0);
				res.filename = info.name;
				
				LogCat.e("正在移动:"+res.mCount);
				if (info != null) {
					try {
						String operatorDestPath = getDestAddress(info.destPath, info.name);
						InputStream inStream = resourceManager.get(info.path);
						isMoveing = true;
						resourceManager.put(operatorDestPath, new CountingInputStreamEntity(inStream, -1, new CountingListener() {
							@Override
							public void onChange(long counterBytes, int bytesRead) throws IOException {
								try {
									checkCancelled();
									res.mCurrentBytes += bytesRead;
									updateProgress(res,true);
									if (counterBytes == info.size) {
										isMoveing = false;
										updateProgress(res,false);
										//移动完后移动下一个
										startMove();
									}
								} catch (ResourceException e) {
									res.mStatus = e.getStatus();
									isMoveing = false;
								} catch (Exception e) {
									e.printStackTrace();
									isMoveing = false;
									try {
										startMove();
									} catch (Exception e1) {
										e1.printStackTrace();
									}
								}
							}
						}));
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		}).start();
		
	}
	
	public void delete(){
		for (ResourceInfo info : sourceList) {
			try {
				checkCancelled();
				resourceManager.delete(info.path);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getDestAddress(String preAddress, String filename) {
		return preAddress + filename;
	}
}
