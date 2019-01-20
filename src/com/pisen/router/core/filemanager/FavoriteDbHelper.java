package com.pisen.router.core.filemanager;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.studio.database.sqlite.SQLiteOpenHelper;

/**
 * 收藏数据库
 * 
 * @author yangyp
 * @version 1.0, 2014年10月16日 上午11:59:40
 */
public class FavoriteDbHelper extends SQLiteOpenHelper {

	protected final static String DATABASE_NAME = "router_fav.db";
	protected final static int DATABASE_VERSION = 2;
	private final static String TABLE_NAME = "favorite";
	public final static String FIELD_ID = "_id";
	public final static String FIELD_LOCATION = "location";
	
	private static FavoriteDbHelper instance;

	public FavoriteDbHelper(Context context) {
		super(context, DATABASE_NAME, DATABASE_VERSION);
		instance = this;
	}

	public static FavoriteDbHelper getInstance() {
		return instance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "Create table " + TABLE_NAME + "(" + FIELD_ID + " integer primary key autoincrement,"  + FIELD_LOCATION
				+ " text );";
		db.execSQL(sql);
		onUpgrade(db, 0, DATABASE_VERSION);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}

	public boolean isFavorite(String path) {
		String selection = FIELD_LOCATION + "=?";
		String[] selectionArgs = new String[] { path };
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null);
		if (cursor == null)
			return false;
		boolean ret = cursor.getCount() > 0;
		cursor.close();
		return ret;
	}

	public Cursor query() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
		return cursor;
	}
	/**
	 * 查询所有收藏连接
	 * 
	 * @return
	 */
	public List<String> findAll() {
		ArrayList<String> result = new ArrayList<String>(); // 收藏的集合
		Cursor c = query(); // 查找数据集
		while (c.moveToNext()) {
			String location = c.getString(2);
			result.add(location); // 添加数据到收藏数据集中
		}
		c.close();
//		close();
		return result;
	}

	public long insert(String location) {
		if (isFavorite(location))
			return -1;

		SQLiteDatabase db = this.getWritableDatabase();
		long ret = db.insert(TABLE_NAME, null, createValues(location));
		return ret;
	}

	public void delete(long id, boolean notify) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_ID + "=?";
		String[] whereValue = { Long.toString(id) };
		db.delete(TABLE_NAME, where, whereValue);
	}

	public void delete(String location) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_LOCATION + "=?";
		String[] whereValue = { location };
		db.delete(TABLE_NAME, where, whereValue);
	}

	public void update(int id, String title, String location) {
		SQLiteDatabase db = this.getWritableDatabase();
		String where = FIELD_ID + "=?";
		String[] whereValue = { Integer.toString(id) };
		db.update(TABLE_NAME, createValues(location), where, whereValue);
	}

	private ContentValues createValues(String location) {
		ContentValues cv = new ContentValues();
		cv.put(FIELD_LOCATION, location);
		return cv;
	}
}
