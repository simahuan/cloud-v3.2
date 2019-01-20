package com.pisen.router.ui.phone.welcome;

import java.io.Serializable;

/**
 * 主页广告图片
 * 
 * @author yangyp
 * @version 1.0, 2014-7-3 下午5:18:40
 */
public class Advertise implements Serializable {

	private static final long serialVersionUID = 1L;

	// 默认显示图片
	public int defImageResId;

	public String ImageUrl;
	public String LinkTitle;
	public String LinkUrl;
	public String Name;
	public int OpenType;
	public String BeginDate; // 开启日期"/Date(1403861400000+0800)/"
	public String EndDate; // 结束时间"/Date(1403861400000+0800)/"
	public int DisplayOrder; // 排序

}
