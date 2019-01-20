package com.pisen.router.core.filemanager.transfer;

/**
 * 控制状态
 * 
 * @author yangyp
 */
public enum TransferControl {

	START(0), // 开始
	PAUSE(1), // 暂停
	DELETE(-1); // 删除

	public int value;

	private TransferControl(int value) {
		this.value = value;
	}

	public static TransferControl valueOfEnum(int value) {
		for (TransferControl ctrl : TransferControl.values()) {
			if (ctrl.value == value) {
				return ctrl;
			}
		}
		return DELETE;
	}
}
