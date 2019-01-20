package com.pisen.router.common.utils;

import java.io.File;

import android.os.Environment;

/**
 * 静态常量类
 *
 * @author MouJunFeng
 * @time 2014年4月25日上午10:29:30
 */
public class KeyUtils {
	/**
	 * 文件保存路径
	 */
	public static String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "PisenRouter";
	/**
	 * 二维码保存文件的路径
	 */
	public static String QRCodePath = filePath + File.separator + "QR_Code";
	/**
	 * 图片保存文件的路径
	 */
	public static String imagePath = filePath + File.separator + "image";
	/**
	 * 广告图片保存路径
	 */
	public static String bannerPath = filePath + File.separator + "banner";
	/**
	 * 更多界面的帮助与反馈中常见问题请求action的类型
	 */
	public static String MORE_ABOUT_HELP_WEB_ACTION_TYPE = "";
	/**
	 * action的名称
	 */
	public static final String NET_MORK_TITLE = "net_title";
	/**
	 * 推送保存的文件
	 */
	//public static final String PISEN_PUSH = "PisenPush";
	/**
	 * 推送保存的键
	 */
	public static final String TAKE_OFF = "take_off";
	/**
	 * smb地址
	 */
	public static final String SMB_ADDRESS = "192.168.222.254";
	/**
	 * smb请求全部字符串
	 */
	public static final String SMB_URL = "smb://userName:pwd@" + SMB_ADDRESS + ":port/pisen-samba/";
	/**
	 * 保存网络应用版本号　
	 */
	public static final String APP_NETVER = "app_netver";
	/**
	 * 版本信息的版本号
	 */
	public static final String APP_VERSION = "app_version";
	
	public static final String UPDATE_NEXT = "update_next";
	
	
	/**
	 * 保存user数据集的文件
	 */
	//public static final String SAVE_USER = "save_user";
	/**
	 * user数据的键
	 */
	public static final String APP_USER = "app_user";
	/**
	 * 保存启动页的图片文件
	 */
	// public static final String SAVE_START_IMAGE = "save_start_image";
	/**
	 * 保存启动页图片的键
	 */
	public static final String APP_START_IMAGE = "app_start_image";
	/**
	 * 软件更新广播action
	 */
	public static final String REFRESH_APP_ACTION = "refresh_app_action";
	/**
	 * 广告数据保存
	 */
	public static final String homeBannerImage = "home_banner_image";
	
	/** 人物头像呢称 */
	public final static String NICK_NAME = "nickName";
	/** 人物卡通头像 */
	public final static String NICK_HEAD = "nickHead";
	/** 人物卡通头像所在下标 */
	public final static String NICK_HEAD_INDEX = "nickHeadIndex";
	
	public final static String TIME_LAST_READ = "timeLastRead";
	public final static String TIME_LAST_RECV = "timeLastRecv";

}
