package com.pisen.router.core.filemanager.async;

/**
 * 资源操作结果数据结构。
 * 这里把最后数据进度更新和操作失败与否合并在一起。
 * 如有好的方法扩展查询结果集，希望能修改。
 */
import java.util.List;

import android.os.AsyncTask;

import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.async.ResourceAsyncTask.ResourceResult;

public abstract class ResourceAsyncTask extends AsyncTask<Void, ResourceResult, ResourceResult> {

	protected IResource resourceManager;
	protected List<ResourceInfo> sourceList;
	protected ResourceInfo sourceSignal;
	// protected String URLAddress = "http://192.168.168.1";
	protected ResourceResult result = null;
	protected ResourceItemCallback itemCallback;

	public interface ResourceItemCallback {
		void onItemCallback(ResourceResult result);
	}

	public void setItemCallback(ResourceItemCallback callback) {
		this.itemCallback = callback;
	}

	public ResourceAsyncTask() {
		result = new ResourceResult();
		result.opeartorType = operatorType.Null;
	}

	public enum operatorType {
		Null(-1), Copy(0), Move(1), Delete(2), Rename(3), Search(4), Sort(5), NewFolder(6);
		public int value;

		private operatorType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static operatorType valueOfEnum(int value) {
			for (operatorType status : operatorType.values()) {
				if (status.value == value) {
					return status;
				}
			}
			return Null;
		}

	}

	static public class ResourceResult {
		public static final int PENDING = 1;
		public static final int UpdateProgress = 100;
		public int mTotal; // 文件个数
		public int mCount = 0; // 当前已移动文件个数
		public long mTotalBytes = -1;
		public long mCurrentBytes;
		public String mPath;
		public String filename;
		public static final int Sucess = 200;
		public static final int CANCELED = 3;
		public static final int UNKNOWN_ERROR = 500;
		public operatorType opeartorType = operatorType.Null;
		public int mStatus;

		public ResourceResult() {
			mStatus = Sucess;
		}

		public int getmStatus() {
			return mStatus;
		}

		public void setmStatus(int mStatus) {
			this.mStatus = mStatus;
		}
		
		public int getCurrentCount() {
			return mCount;
		}
		
		public int getTotalCount() {
			return mTotal;
		}

		public int getProgressPercent() {
			if (mTotalBytes == -1) {
				return 0;
			}

			return (int) (mCurrentBytes * 100 / mTotalBytes);
		}
	}

	@Override
	protected void onPostExecute(ResourceResult result) {
		super.onPostExecute(result);

	}
}
