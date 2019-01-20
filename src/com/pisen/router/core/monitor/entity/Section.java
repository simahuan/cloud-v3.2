package com.pisen.router.core.monitor.entity;

import org.simpleframework.xml.Attribute;

import com.pisen.router.core.monitor.DiskUtils;

public class Section {

	@Attribute
	public String volume;// = "pisen";
	@Attribute
	public String vid;// = "8644";
	@Attribute
	public String pid;// = "8003";
	@Attribute
	public String total;// = "7.5GB";
	@Attribute
	public String used;// = "1.5GB";
	@Attribute
	public String free;// = "5.9GB";
	@Attribute
	public String fstype;// = "msdos";
	
	@Attribute(required = false)
	public String extusb;// = "yes";
	@Attribute(required = false)
	public String rw;
	@Attribute(required = false)
	public long total_byte;
	@Attribute(required = false)
	public long used_byte;
	@Attribute(required = false)
	public long free_byte; // 网络磁盘类型

	/**
	 * @describtion
	 * @return 网络磁盘 总大小
	 */
	public long getTotalUnit() {
		return DiskUtils.getFileSize(total);
	}

	/**
	 * @describtion
	 * @return 网络磁盘 已使用大小
	 */
	public long getUsedUnit() {
		return DiskUtils.getFileSize(used);
	}

	/**
	 * @describtion
	 * @return 网络磁盘 可用大小
	 */
	public long getFreeUnit() {
		return DiskUtils.getFileSize(free);
	}
	
	/***
	 * 是否外置存储卡
	 * @return
	 */
	public boolean isExtUsb()
	{
		return extusb == null || "yes".equals(extusb);
	}

}
