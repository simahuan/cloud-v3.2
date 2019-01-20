package com.pisen.router.core.filemanager.transfer;

/**
 * 传输类型
 * 
 * @author yangyp
 */
public enum TransferCTag {

	Upload("file_upload"), // 文件上传
	Download("file_download"), // 文件下载
	CameraUpload("camera_upload"),

	FlashSend("flash_send"), // 闪电发送
	FlashRecv("flash_recv"); // 闪电接收

	public String value;

	TransferCTag(String value) {
		this.value = value;

	}

	public String getValue() {
		return value;
	}

	public static TransferCTag valueOfEnum(String value) {
		for (TransferCTag ctag : TransferCTag.values()) {
			if (ctag.value.equals(value)) {
				return ctag;
			}
		}
		return null;
	}

}
