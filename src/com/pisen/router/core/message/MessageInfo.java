package com.pisen.router.core.message;

import java.io.Serializable;

import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * (消息实体设计)
 * @author yangyp
 */
public class MessageInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	/** 新消息 */
	public static final String MESSAGE_NEW = "message_new";
	/** 未读消息 */
	public static final int MESSAGE_UNREAD = 0;
	/** 已读消息 */
	public static final int MESSAGE_READ = 1;

	/** 自增id */
	public long id;
	/** 标题 */
	public String title;
	/** 内容 */
	public String content;
	/** 10普通消息|20超文本消息 */
	public int type;
	/** 接收时间 */
	public long recvTime;
	/** 是否已读:0 未读 ,1 已读 */
	public int readFlag;
	/** 阅读时间 */
	public long readTime;
	/** 拓展参数 */
	public String parameter;

	/**
	 * 数据库表字段设计
	 */
	public static final class Table implements BaseColumns {
		/** 消息表字段 */
		public static final String TABLE_NAME = "jpush_message";

		/** 消息标题 */
		public static final String MSG_TITLE = "msg_title";
		/** 消息内容 */
		public static final String MSG_CONTENT = "msg_content";
		/** 消息内型 */
		public static final String MSG_TYPE = "msg_type";
		/** 消息接收时间 */
		public static final String MSG_RECV_TIME = "msg_recv_time";
		/** 消息阅读状态 */
		public static final String MSG_READ_FLAG = "msg_read_flag";
		/** 消息阅读时间 */
		public static final String MSG_READ_TIME = "msg_read_time";
		/** 拓展参数 */
		public static final String EXPAND_PARAMETER = "expand_parameter";
	}

	/**
	 * @param cursor
	 * @return 将实体生成传输字段
	 */
	public static MessageInfo cursor2bean(Cursor cursor) {
		MessageInfo info = new MessageInfo();
		info.id = cursor.getLong(cursor.getColumnIndexOrThrow(Table._ID));
		info.title = cursor.getString(cursor.getColumnIndexOrThrow(Table.MSG_TITLE));
		info.content = cursor.getString(cursor.getColumnIndexOrThrow(Table.MSG_CONTENT));
		info.type = cursor.getInt(cursor.getColumnIndexOrThrow(Table.MSG_TYPE));
		info.recvTime = cursor.getLong(cursor.getColumnIndexOrThrow(Table.MSG_RECV_TIME));
		info.readFlag = cursor.getInt(cursor.getColumnIndexOrThrow(Table.MSG_READ_FLAG));
		info.readTime = cursor.getLong(cursor.getColumnIndexOrThrow(Table.MSG_READ_TIME));
		info.parameter = cursor.getString(cursor.getColumnIndexOrThrow(Table.EXPAND_PARAMETER));
		return info;
	}
}
