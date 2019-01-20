package com.pisen.router.core.filemanager.transfer;

public interface ITransferManager {

	/**
	 * 加入新的任务
	 * 
	 * @param info
	 */
	void enqueue(TransferInfo info);

	/**
	 * 重新开始任务
	 * 
	 * @param id
	 */
	void restartTransfer(long id);

	/**
	 * 恢复任务
	 * 
	 * @param id
	 */
	void resumeTransfer(long id);

	/**
	 * 暂停任务
	 * 
	 * @param id
	 */
	void pauseTransfer(long id);

	/**
	 * 打上删除任务标记
	 * 
	 * @param id
	 */
	void deletedTransfer(long id);
}
