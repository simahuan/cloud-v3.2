package com.pisen.router.core.filemanager.transfer;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;

import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.SardineCacheResource;

/**
 * (上传传输类)
 * 
 * @author yangyp
 */
public abstract class SardineTransferTask extends TransferTask {

//	protected IResource sardine;

	public SardineTransferTask(Context context, TransferInfo info, String username, String password) {
		super(context, info);
		// SardineFactory.begin(username, password);
//		sardine = new SardineCacheResource();
	}

	@Deprecated
	public void dispatchTransferTask(TransferInfo info) {
		if (info.isDir) {
			// 读取数据库下级目录
			List<TransferInfo> subList = getTransferChildById(info);
			for (TransferInfo subInfo : subList) {
				dispatchTransferTask(subInfo);
			}
		} else {
			executeTransferTask(info);
		}
	}

	@Deprecated
	private List<TransferInfo> getTransferChildById(TransferInfo info) {
		List<TransferInfo> results = new ArrayList<TransferInfo>();
		Cursor cursor = mContext.getContentResolver().query(info.getTransferAllUri(), null, TransferInfo.Table.parentId + "=?",
				new String[] { String.valueOf(info._id) }, null);
		try {
			while (cursor.moveToNext()) {
				results.add(TransferInfo.newTransferInfo(cursor));
			}
		} finally {
			cursor.close();
		}
		return results;
	}

	@Deprecated
	private void executeTransferTask(TransferInfo info) {

	}
}
