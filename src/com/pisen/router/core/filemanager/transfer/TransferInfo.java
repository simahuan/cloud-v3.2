package com.pisen.router.core.filemanager.transfer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Pair;

import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.ResourceInfo.RSource;

/**
 * 任务实体
 * 
 * @author yangyp
 * @version 1.0, 2014-09-26 上午9:35:05
 */
public class TransferInfo {

	public long _id; // 自增主键
	public String url; // 传输地址，用于读取地址(upload 本地文件所取地址)
	public String filename; // 文件名
	public String filetype; // 文件类型
	public long filesize; // 大小
	public String storageDir; // 储存路径，用于写入的地址(upload 目标文件上传地址)
	public String ssid; // wifi标识
	public String remoteHostType; // 远程主机类型
	public String remoteHostName; // 远程主机名
	public TransferControl takeControl; // 控制状态
	public long currentBytes; // 已传输的大小
	public TransferStatus status; // 执行状态
	public long dataCreated; // 创建时间
	public long lastUpdated; // 最后更新时间
	public boolean isDir; // 是否目录
	public long parentId = -1; // 父节点
	public long totalBytes;
	// public boolean deleted; // 是否删除
	public TransferCTag ctag; // 传输类型
	public int inboxRecordDeleted; // 收件箱记录是否删除
	public String mETag;
	public volatile boolean hasActiveThread;
	public boolean isSelected;
	private List<Pair<String, String>> mRequestHeaders = new ArrayList<Pair<String, String>>();

	public static final class Table implements BaseColumns {
		public static final String TABLE_NAME = "transport";
		public static final String url = "url";
		public static final String filename = "filename";
		public static final String filetype = "filetype";
		public static final String filesize = "filesize";
		public static final String storageDir = "storageDir";
		public static final String ssid = "ssid";
		public static final String remoteHostType = "remoteHostType";
		public static final String remoteHostName = "remoteHostName";
		public static final String takeControl = "takeControl";
		public static final String currentBytes = "currentBytes";
		public static final String status = "status";
		public static final String dataCreated = "dataCreated";
		public static final String lastUpdated = "lastUpdated";
		public static final String isDir = "isDir";
		public static final String parentId = "parentId";
		public static final String ctag = "ctag";
		public static final String inboxRecordDeleted = "inboxRecordDeleted";

	}

	/**
	 * 转换为resouceinfo，资源默认为本地资源
	 * 
	 * @param transferInfo
	 * @return
	 */
	public ResourceInfo convertToResouceInfo() {
		ResourceInfo info = new ResourceInfo();
		info.createTime = dataCreated;
		info.destPath = url;
		info.isDirectory = isDir;
		info.lastModified = lastUpdated;
		info.mediaType = filetype;
		info.name = filename;
		//XXX 必须优化，url在不同地方含义不同！！！
		if(isUpload()) {
			if(ctag == TransferCTag.Upload || ctag == TransferCTag.CameraUpload) {
				info.path = url;
			}else {
				info.path = String.format("%s/%s", url, filename);
			}
		}else {
			info.path = String.format("%s/%s", storageDir, filename);
		}
//		info.path = String.format("%s/%s",isUpload()? url : storageDir, filename);//String.format("%s/%s", storageDir, filename);
		info.size = filesize;
		info.source = RSource.Local; //isUpload() ? RSource.Remote : RSource.Local;
		return info;
	}

	public boolean isUpload() {
		switch (ctag) {
		case Upload:
		//case RecordUpload:
		case CameraUpload:
		//case CameraPhotoUpload:
		//case CameraVideoUpload:
		case FlashSend:
			return true;
		case Download:
		case FlashRecv:
		default:
			return false;
		}
	}

	public TransferInfo(TransferCTag ctag) {
		super();
		this.ctag = ctag;
	}

	public Collection<Pair<String, String>> getHeaders() {
		return Collections.unmodifiableList(mRequestHeaders);
	}

	/**
	 * 生成传输对象
	 * 
	 * @param cursor
	 * @return
	 */
	public static TransferInfo newTransferInfo(Cursor cursor) {
		TransferCTag ctag = TransferCTag.valueOfEnum(cursor.getString(cursor.getColumnIndexOrThrow(Table.ctag)));
		TransferInfo info = new TransferInfo(ctag);
		return copyOf(cursor, info);
	}

	public static TransferInfo copyOf(Cursor cursor, TransferInfo info) {
		info._id = cursor.getLong(cursor.getColumnIndexOrThrow(Table._ID));
		info.url = cursor.getString(cursor.getColumnIndexOrThrow(Table.url));
		info.filename = cursor.getString(cursor.getColumnIndexOrThrow(Table.filename));
		info.filetype = cursor.getString(cursor.getColumnIndexOrThrow(Table.filetype));
		info.filesize = cursor.getLong(cursor.getColumnIndexOrThrow(Table.filesize));
		info.storageDir = cursor.getString(cursor.getColumnIndexOrThrow(Table.storageDir));
		info.ssid = cursor.getString(cursor.getColumnIndexOrThrow(Table.ssid));
		info.remoteHostType = cursor.getString(cursor.getColumnIndexOrThrow(Table.remoteHostType));
		info.remoteHostName = cursor.getString(cursor.getColumnIndexOrThrow(Table.remoteHostName));
		info.takeControl = TransferControl.valueOfEnum(cursor.getInt(cursor.getColumnIndexOrThrow(Table.takeControl)));
		info.currentBytes = cursor.getLong(cursor.getColumnIndexOrThrow(Table.currentBytes));
		info.status = TransferStatus.valueOfEnum(cursor.getInt(cursor.getColumnIndexOrThrow(Table.status)));
		info.dataCreated = cursor.getLong(cursor.getColumnIndexOrThrow(Table.dataCreated));
		info.lastUpdated = cursor.getLong(cursor.getColumnIndexOrThrow(Table.lastUpdated));
		info.isDir = cursor.getInt(cursor.getColumnIndexOrThrow(Table.isDir)) == 1;
		info.parentId = cursor.getLong(cursor.getColumnIndexOrThrow(Table.parentId));
		info.inboxRecordDeleted = cursor.getInt(cursor.getColumnIndexOrThrow(Table.inboxRecordDeleted));
		return info;
	}

	public static boolean isStatusSuccess(TransferStatus status) {
		return (status.value >= 200 && status.value < 300);
	}

	public static boolean isStatusError(TransferStatus status) {
		return (status.value >= 400 && status.value < 600);
	}

	public static boolean isStatusCompleted(TransferStatus status) {
		return isStatusSuccess(status) || isStatusError(status);
	}

	/**
	 * 是否标记删除
	 * 
	 * @return
	 */
	public boolean hasDeleted() {
		return takeControl == TransferControl.DELETE;
	}

	public String getTempFileName() {
		return filename + ".~temp";
	}

	public String getFilename() {
		return filename;
	}

	/**
	 * 是否启动可以启动线程
	 * 
	 * @return
	 */
	public boolean isReadyToStart() {
		if (hasActiveThread || takeControl == TransferControl.PAUSE) {
			return false;
		}

		switch (status) {
		case PENDING:
		case RUNNING:
			return true;
		default:
			return false;
		}
	}

	/**
	 * 获取存储的文件路径,将要发送至的地址
	 * 
	 * @return
	 */
	/*public String getStoragePathEncode() {
		switch (ctag) {
		case Upload:
			return URLUtils.encodeURL(storageDir);
		case RecordUpload:
		case CameraUpload:
		case CameraPhotoUpload:
		case CameraVideoUpload:
		case FlashSend:
			return storageDir; // Http地址
		case Download:
			return new File(storageDir).getPath();
		case FlashRecv:
		default:
			return new File(storageDir, filename).getPath();
		}
	}*/

	/**
	 * 获取当前进度
	 * 
	 * @return
	 */
	public int getProgress() {
		return filesize <= 0 ? 0 : (int) (currentBytes * 100 / filesize);
	}

	public Uri getTransferAllUri() {
		return TransferProvider.CONTENT_URI;
	}

	public Uri getTransferUri() {
		return ContentUris.withAppendedId(TransferProvider.CONTENT_URI, _id);
	}

	public ContentValues toContentValues() {		
		ContentValues values = new ContentValues();
		values.put(TransferInfo.Table.url, url);
		values.put(TransferInfo.Table.filename, filename);
		values.put(TransferInfo.Table.filetype, filetype);
		values.put(TransferInfo.Table.filesize, filesize);
		values.put(TransferInfo.Table.storageDir, storageDir);
		values.put(TransferInfo.Table.ssid, ssid);
		values.put(TransferInfo.Table.remoteHostType, remoteHostType);
		values.put(TransferInfo.Table.remoteHostName, remoteHostName);
		values.put(TransferInfo.Table.takeControl, takeControl.value);
		values.put(TransferInfo.Table.currentBytes, currentBytes);
		values.put(TransferInfo.Table.status, status.value);
		values.put(TransferInfo.Table.dataCreated, dataCreated);
		values.put(TransferInfo.Table.lastUpdated, lastUpdated);
		values.put(TransferInfo.Table.isDir, isDir);
		values.put(TransferInfo.Table.parentId, parentId);
		values.put(TransferInfo.Table.ctag, ctag.value);
		values.put(TransferInfo.Table.inboxRecordDeleted, inboxRecordDeleted);
		return values;
	}
}
