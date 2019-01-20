package com.pisen.router.core.filemanager.transfer;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.studio.database.sqlite.ICursor;
import android.studio.database.sqlite.SQLiteOpenHelper;

import com.pisen.router.core.filemanager.transfer.TransferInfo.Table;

/**
 * 文件传输数据库
 * 
 * @author yangyp
 * @version 1.0 2014年9月26日 下午2:48:13
 */
public class TransferDbHelper extends SQLiteOpenHelper {

	private final static String DB_NAME = "transfer";
	private static final int VERSION = 20141014;
	private static TransferDbHelper dbHelper;

	private TransferDbHelper(Context context) {
		super(context, DB_NAME, VERSION);
	}
	
	public static TransferDbHelper getInstance(Context context) {
		if(dbHelper == null) {
			dbHelper = new TransferDbHelper(context);
		}
		
		return dbHelper;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTransferInfo(db);
		onUpgrade(db, 0, VERSION);
	}

	// 创建传输管理表
	private void createTransferInfo(SQLiteDatabase db) {
		db.execSQL("create table " + TransferInfo.Table.TABLE_NAME + "(" //
				+ TransferInfo.Table._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," //
				+ TransferInfo.Table.url + " TEXT," //
				+ TransferInfo.Table.filename + " TEXT," //
				+ TransferInfo.Table.filetype + " TEXT," //
				+ TransferInfo.Table.filesize + " INTEGER," //
				+ TransferInfo.Table.storageDir + " TEXT," //
				+ TransferInfo.Table.ssid + " TEXT," //
				+ TransferInfo.Table.remoteHostType + " TEXT," //
				+ TransferInfo.Table.remoteHostName + " TEXT," //
				+ TransferInfo.Table.takeControl + " INTEGER," //
				+ TransferInfo.Table.currentBytes + " INTEGER," //
				+ TransferInfo.Table.status + " INTEGER," //
				+ TransferInfo.Table.dataCreated + " INTEGER," //
				+ TransferInfo.Table.lastUpdated + " INTEGER," //
				+ TransferInfo.Table.isDir + " INTEGER," //
				+ TransferInfo.Table.parentId + " TEXT," //
				+ TransferInfo.Table.ctag + " TEXT," //
				+ TransferInfo.Table.inboxRecordDeleted + " INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// 以下用于处理后期字段更新升级
		switch (newVersion) {
		case 20140926:
		case 20141014:
			// addColumn(db, TransferInfo.Table.TABLE_NAME,
			// TransferInfo.Table.COMPLETED_DELETE, "INTEGER DEFAULT 0");
			// addColumn(db, TransferInfo.Table.TABLE_NAME,
			// TransferInfo.Table.CUSTOM_TAG, "TEXT");
			break;
		default:
			break;
		}
	}

	/**
	 * 根据ID查询传输信息
	 * 
	 * @param ids
	 * @return
	 */
	public synchronized List<TransferInfo> query(long... ids) {
		return query(TransferInfo.Table.TABLE_NAME, new RowQuery<TransferInfo>() {
			@Override
			public TransferInfo rowQuery(ICursor cursor, int arg1) {
				return TransferInfo.newTransferInfo(cursor);
			}

		}, ids);
	}

	/**
	 * 查询未被删除的未完成指定ctag任务(创建时间升序)
	 * @param ctag
	 * @return
	 */
	public synchronized List<TransferInfo> queryUnCompleteTransferTask(TransferCTag ctag) {
		String selection = Table.ctag + "=? AND " + Table.status + "!=? AND " + Table.takeControl + "!=?";
		String[] selectionArgs = new String[] { //
		ctag.value, //
				String.valueOf(TransferStatus.SUCCESS.value), //
				String.valueOf(TransferControl.DELETE.value) };
		return query(TransferInfo.Table.TABLE_NAME, selection, selectionArgs, Table.dataCreated + " ASC ", null, new RowQuery<TransferInfo>() {
			@Override
			public TransferInfo rowQuery(ICursor cursor, int num) {
				return TransferInfo.newTransferInfo(cursor);
			}
		});
	}

	/**
	 * 查询未完成的任务
	 * 
	 * @return
	 */
	public synchronized List<TransferInfo> queryTransferTask() {
		String selection = Table.status + "!=" + TransferStatus.SUCCESS.value;
		return query(TransferInfo.Table.TABLE_NAME, selection, null, Table.dataCreated + " DESC ", null, new RowQuery<TransferInfo>() {
			@Override
			public TransferInfo rowQuery(ICursor cursor, int num) {
				return TransferInfo.newTransferInfo(cursor);
			}
		});
	}

	/**
	 * 查询已完成传输的数据
	 * 
	 * @return
	 */
	public synchronized List<TransferInfo> queryCompleted(TransferCTag ctag) {
		String selection = Table.ctag + "=? AND " + Table.status + "=" + TransferStatus.SUCCESS.value + " AND " + Table.takeControl + "!=" + TransferControl.DELETE.value;
		String[] selectionArgs = new String[] { ctag.value };
		return query(TransferInfo.Table.TABLE_NAME, selection, selectionArgs, Table.lastUpdated + " DESC ", null, new RowQuery<TransferInfo>() {
			@Override
			public TransferInfo rowQuery(ICursor cursor, int num) {
				return TransferInfo.newTransferInfo(cursor);
			}
		});
	}
	
	/**
	 * 查询闪电互传收件箱数据
	 * @return
	 */
	public synchronized List<TransferInfo> queryFlashTransferInboxData() {
		String selection = Table.ctag + "=? AND " + Table.status + "=" + TransferStatus.SUCCESS.value + " AND " + Table.inboxRecordDeleted + "!=" + 1;
		String[] selectionArgs = new String[] { TransferCTag.FlashRecv.value };
		return query(TransferInfo.Table.TABLE_NAME, selection, selectionArgs, Table.lastUpdated + " DESC ", null, new RowQuery<TransferInfo>() {
			@Override
			public TransferInfo rowQuery(ICursor cursor, int num) {
				return TransferInfo.newTransferInfo(cursor);
			}
		});
	}
	
	/**
	 * 查询没有被删除的闪电互传数据，以创建时间降序排列
	 * @return
	 */
	public synchronized List<TransferInfo> queryFlashTransferTask() {
		String selection = "("+Table.ctag + "=? or " +  Table.ctag + "=? ) AND " + Table.takeControl + "!=" + TransferControl.DELETE.value;
		String[] selectionArgs = new String[] { TransferCTag.FlashRecv.value,TransferCTag.FlashSend.value};
		return query(TransferInfo.Table.TABLE_NAME, selection, selectionArgs, Table.dataCreated + " DESC ", null, new RowQuery<TransferInfo>() {
			@Override
			public TransferInfo rowQuery(ICursor cursor, int num) {
				return TransferInfo.newTransferInfo(cursor);
			}
		});
	}

	/**
	 * 是否存在传输任务
	 * 
	 * @return
	 */
	public synchronized boolean hasTransferTask() {
//		String sql = "select count(*) from " + TransferInfo.Table.TABLE_NAME + " where " + TransferInfo.Table.status + "!= " + TransferStatus.SUCCESS.value + " AND "
//				+ Table.takeControl + "!=" + TransferControl.DELETE.value;
//		
		String sql = "select count(*) from " + TransferInfo.Table.TABLE_NAME + " where " +  TransferInfo.Table.status + "!= " + TransferStatus.SUCCESS.value + " AND "
				+ Table.takeControl + "!=" + TransferControl.DELETE.value + " AND " 
				+ "("+Table.ctag + "='"+ TransferCTag.Upload.value +"' or " +  Table.ctag + "='"+TransferCTag.Download.value+"' or "+ Table.ctag+ "='"+TransferCTag.CameraUpload.value+"' )" ;
		return rawForInt(sql, null) > 0;
	}

	public synchronized List<TransferInfo> findTransfer(int pageSize, int offset) {
		return null;
	}

	public synchronized void addTransfer(TransferInfo info) {
		ContentValues values = new ContentValues();
		values.put(TransferInfo.Table.url, info.url);
		values.put(TransferInfo.Table.filename, info.filename);
		values.put(TransferInfo.Table.filetype, info.filetype);
		values.put(TransferInfo.Table.filesize, info.filesize);
		values.put(TransferInfo.Table.storageDir, info.storageDir);
		values.put(TransferInfo.Table.ssid, info.ssid);
		values.put(TransferInfo.Table.remoteHostType, info.remoteHostType);
		values.put(TransferInfo.Table.remoteHostName, info.remoteHostName);
		values.put(TransferInfo.Table.takeControl, info.takeControl.value);
		values.put(TransferInfo.Table.currentBytes, info.currentBytes);
		values.put(TransferInfo.Table.status, info.status.value);
		values.put(TransferInfo.Table.dataCreated, info.dataCreated);
		values.put(TransferInfo.Table.lastUpdated, info.lastUpdated);
		values.put(TransferInfo.Table.isDir, info.isDir);
		values.put(TransferInfo.Table.parentId, info.parentId);
		values.put(TransferInfo.Table.ctag, info.ctag.value);
		values.put(TransferInfo.Table.inboxRecordDeleted, info.inboxRecordDeleted);
		insert(TransferInfo.Table.TABLE_NAME, values);
	}

	public synchronized long insert(ContentValues values) {
		return insertForGeneratedKey(TransferInfo.Table.TABLE_NAME, values);
	}

	public synchronized void update(ContentValues values, long... ids) {
		update(TransferInfo.Table.TABLE_NAME, values, ids);
	}

	public synchronized int update(ContentValues values, String selection, String[] selectionArgs) {
		return update(TransferInfo.Table.TABLE_NAME, values, selection, selectionArgs);
	}

	public synchronized int delete(String selection, String[] selectionArgs) {
		return delete(TransferInfo.Table.TABLE_NAME, selection, selectionArgs);
	}

}
