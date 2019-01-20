package com.pisen.router.ui.phone.flashtransfer;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.os.Environment;

import com.pisen.router.core.filemanager.LocalResourceManager;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.core.filemanager.ResourceInfo;


/**
 * 本地视频扫描
 * @author ldj
 * @version 1.0 2015年5月18日 下午2:14:44
 */
public class LocalMovieScanner extends LocalResourceScanner<ResourceInfo> {

	@Override
	public void startScan(final Context ctx) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				LocalResourceManager rm = new LocalResourceManager(ctx);
				List<ResourceInfo> data = rm.listRecursively(Environment.getExternalStorageDirectory().getAbsolutePath(), FileType.Video);
				if(data != null && !data.isEmpty()) {
					//排序
					Collections.sort(data, new Comparator<ResourceInfo>() {
	
						@Override
						public int compare(ResourceInfo lhs, ResourceInfo rhs) {
							return -(int) (lhs.lastModified - rhs.lastModified) ;
						}
					});
				}
				notifyComplete(data);
			}
		}).start();
	}

}
