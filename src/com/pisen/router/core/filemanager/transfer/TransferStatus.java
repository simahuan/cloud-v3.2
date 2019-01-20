package com.pisen.router.core.filemanager.transfer;

/**
 * 任务执行状态
 * 
 * @author yangyp
 */
public enum TransferStatus {

	PENDING(100), // 等待传输
	RUNNING(110), // 正在传输
	PAUSE(120), // 暂停
	SUCCESS(200), // 传输完成
	CANCELED(-1), // 用户取消
	UNKNOWN_ERROR(500), // 传输出错
	INSUFFICIENT_STORAGE_ERROR(507),//云盘空间不足
	NET_NO_CONNECTION_ERROR(510), // 网络未连接错误
	CANNOT_RESUME_ERROR(510), // 不能恢复传输
	HTTP_ERROR(520); // HTTP错误

	public int value;

	private TransferStatus(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static TransferStatus valueOfEnum(int value) {
		for (TransferStatus status : TransferStatus.values()) {
			if (status.value == value) {
				return status;
			}
		}
		return UNKNOWN_ERROR;
	}
}
