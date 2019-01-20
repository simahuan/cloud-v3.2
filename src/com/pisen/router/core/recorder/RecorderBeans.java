package com.pisen.router.core.recorder;

/**
 * 
 * 列表bean
 * 
 */
public class RecorderBeans {
	// 下面是列表数据的每个属性
	public final int WEBDAV = 0;
	public final int LOCATION = 0;
	private int id;
	private String fileName;
	private String fileUrl;
	private String locType;
	private long date;
	private long size;
	private boolean edited;
	private int completed_status;
	private String storage_dir;

	public RecorderBeans() {

	}

	
	public int getCompleted_status() {
		return completed_status;
	}

	public void setCompleted_status(int completed_status) {
		this.completed_status = completed_status;
	}
	public String getStorage_dir() {
		return storage_dir;
	}

	public void setStorage_dir(String storage_dir) {
		this.storage_dir = storage_dir;
	}


	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public String getLocType() {
		return locType;
	}

	public void setLocType(String locType) {
		this.locType = locType;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getDate() {
		return date;
	}

	public void setDate(long date) {
		this.date = date;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public boolean isEdited() {
		return edited;
	}

	public void setEdited(boolean edited) {
		this.edited = edited;
	}

}
