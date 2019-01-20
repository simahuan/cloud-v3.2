package com.pisen.router.core.message;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.studio.database.sqlite.ICursor;
import android.studio.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import cn.jpush.android.api.JPushInterface;

/**
 * @author  mahuan
 * @version 1.0 2015年3月25日 下午3:14:36 数据库建设及相应操作方法
 * @updated [2015年3月25日 下午3:14:36]:
 */
public class MessageCenterDbHelper extends SQLiteOpenHelper {
	static final String DB_NAME    = "message_center.db";
	static final int 	DB_VERSION = 20150325;
	public MessageCenterDbHelper(Context context) {
		super(context, DB_NAME, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createTableInfo(db);
		onUpgrade(db, 0, DB_VERSION);
	}

	public void createTableInfo(SQLiteDatabase db) {
		db.execSQL("create table " + MessageInfo.Table.TABLE_NAME + "("
				+ MessageInfo.Table._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ MessageInfo.Table.MSG_TITLE + " TEXT NOT NULL,"
				+ MessageInfo.Table.MSG_CONTENT + " TEXT NOT NULL,"
				+ MessageInfo.Table.MSG_TYPE + " INTEGER NOT NULL,"
				+ MessageInfo.Table.MSG_RECV_TIME + " INTEGER,"
				+ MessageInfo.Table.MSG_READ_FLAG + " INTEGER,"
				+ MessageInfo.Table.MSG_READ_TIME + " INTEGER,"
				+ MessageInfo.Table.EXPAND_PARAMETER + " TEXT"
				+ ")"
				);
		}
	
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (newVersion) {
		case 20150330:
		case 20150325:
			break;
		}
	}
	
	
	/**
	 * @describtion
	 * @param pageSize  每页显示多少条记录/数据
	 * @param offset    从第几条数据开始查寻/偏移量
	 * @return
	 */
	public List<MessageInfo> findMessageInfo(int offset , int pageSize) {
		String sql = " select * from " + MessageInfo.Table.TABLE_NAME + " limit ? , ? ";
		return rawQuery(sql, new String[]{String.valueOf(offset),String.valueOf(pageSize)}, new RowQuery<MessageInfo>() {
			@Override
			public MessageInfo rowQuery(ICursor cursor, int arg1) {
				return MessageInfo.cursor2bean(cursor);
			}
		});
	}
	

	/**
	 * @describtion   增加激光推送消息
	 * @param intent  意图数据 
	 */
	public void addJPushMessage(Intent intent){
		MessageInfo info =  new MessageInfo();
		Bundle bundle 	 =  intent.getExtras();
		info.readFlag 	 =  MessageInfo.MESSAGE_UNREAD;
		info.title 		 =  bundle.getString(JPushInterface.EXTRA_NOTIFICATION_TITLE);
		info.content 	 =  bundle.getString(JPushInterface.EXTRA_ALERT);
		String extra 	 =  bundle.getString(JPushInterface.EXTRA_EXTRA);
		if (!TextUtils.isEmpty(extra)){
//			JSONObject obj = JSONObject.parseObject(extra);
//			info.type = obj.getIntValue("type");
//			info.parameter = obj.getString("para");
//			info.recvTime = obj.getString("time");
		}
		addMessage(info);
	}
	
	
	/**
	 * @describtion 向数据库插入数据
	 * @param info  实体消息
	 * @return      增加数据受影条数
	 */
	public long addMessage(MessageInfo info) {
		ContentValues cv = new ContentValues();
		cv.put(MessageInfo.Table.MSG_TITLE, info.title);
		cv.put(MessageInfo.Table.MSG_CONTENT, info.content);
		cv.put(MessageInfo.Table.MSG_TYPE, info.type);
		cv.put(MessageInfo.Table.MSG_RECV_TIME, info.recvTime);
		cv.put(MessageInfo.Table.MSG_READ_FLAG, info.readFlag);
		cv.put(MessageInfo.Table.EXPAND_PARAMETER, info.parameter);
		return getWritableDatabase().insert(MessageInfo.Table.TABLE_NAME, null, cv);	
	}

	
	/**
	 * @describtion 删除消息
	 * @param ids   根据id删除 
	 * @return      删除受影响条数
	 */
	public long deleteMessage(long...ids){
		return getWritableDatabase().delete(MessageInfo.Table.TABLE_NAME, 
				getWhereClauseForIds(ids), getWhereArgsForIds(ids));
	}
	
	
	/**
	 * @describtion 更新消息状态
	 * @param ids 	数据库主键 
	 * @return 		更新受影响条数
	 */
	public long updateMessageByRead(long...ids) {
		ContentValues cv = new ContentValues();
		cv.put(MessageInfo.Table.MSG_READ_FLAG, MessageInfo.MESSAGE_READ);
		return getWritableDatabase().update(MessageInfo.Table.TABLE_NAME, cv, getWhereClauseForIds(ids), getWhereArgsForIds(ids));
	}
	
	
	/**
	 * @describtion 是否有未读消息
	 * @return	true-有未读消息  false-无未读消息
	 */
	public Boolean haveNewMessage(){
		String  selection  = MessageInfo.Table.MSG_READ_FLAG + " = '"+MessageInfo.MESSAGE_UNREAD+"' ";
		String  orderBy    = MessageInfo.Table.MSG_RECV_TIME + " desc";
		return query(MessageInfo.Table.TABLE_NAME, selection, null, orderBy, " 200", new RowQuery<Boolean>() {
			@Override
			public Boolean rowQuery(ICursor arg0, int arg1) {
				return true;
			}
		}).size() > 0;
	}
	

	/**
	 * 生成 {@link SQLiteDatabase} where条件
	 */
	static String getWhereClauseForIds(long... ids) {
		StringBuilder whereClause = new StringBuilder();
		whereClause.append("(");
		for (int i = 0; i < ids.length; i++) {
			if (i > 0) {
				whereClause.append("OR ");
			}
			whereClause.append(MessageInfo.Table._ID);
			whereClause.append(" = ? ");
		}
		whereClause.append(")");
		return whereClause.toString();
	}

	
	/**
	 * 生成 {@link SQLiteDatabase} where条件参数
	 */
	static String[] getWhereArgsForIds(long... ids) {
		String[] whereArgs = new String[ids.length];
		for (int i = 0; i < ids.length; i++) {
			whereArgs[i] = Long.toString(ids[i]);
		}
		return whereArgs;
	}
	
	
	public  List<MessageInfo> getMessageInfo(int offSet ,int pageSize){
		String sql = "select * from "+ MessageInfo.Table.TABLE_NAME + " limit " + offSet + ","+ pageSize;
		Cursor mCursor = getWritableDatabase().rawQuery(sql, null);
//		Cursor mCursor = (Cursor) rawQuery(sql, null, null);
		if(mCursor != null){
			List<MessageInfo> mList = new ArrayList<MessageInfo>();
			if(mCursor.moveToFirst()){
				do{
					MessageInfo info = new MessageInfo();
					setValues(info, mCursor);
					mList.add(info);
				}while(mCursor.moveToNext());
				return mList;
			}
		}
		return null;
	}
	
	public static void setValues(Object o, Cursor mCursor) {
		int count = mCursor.getColumnCount();
		for (int i = 0; i < count; i++) {
			try {
				Field f = o.getClass().getField(mCursor.getColumnName(i));
				if (f == null) {
					continue;
				}
				String value = mCursor.getString(i);
				if (value == null)
					value = "";
				f.set(o, value);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
	}
}
