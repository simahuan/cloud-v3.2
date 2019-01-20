package com.pisen.router.ui.phone.flashtransfer;

import java.util.List;

import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView.OnItemLongClickListener;

import com.pisen.router.common.dialog.LoadingDialog;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.ui.phone.resource.IMultiChoice;
import com.pisen.router.ui.phone.resource.v2.panel.ISelectionActionBar;

/**
 * 闪电互传记录视图
 * @author ldj
 * @version 1.0 2015年5月27日 上午11:15:33
 */
public abstract class AbstractFlashTransferRecordFragment extends Fragment implements IMultiChoice<TransferInfo>, OnItemLongClickListener,
		ISelectionActionBar<TransferInfo> {

	protected View contentView;
	private AbstractFlashTransferRecordAdapter adapter;
	// fragment标题
	private CharSequence pageTitle;
	private LoadingDialog mLoadingDialog;

	/**
	 * 设置adapter
	 * 
	 * @param adapter
	 */
	public void setAdapter(AbstractFlashTransferRecordAdapter adapter) {
		this.adapter = adapter;
	}

	public CharSequence getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(CharSequence title) {
		this.pageTitle = title;
	}

	/**
	 * 删除已选择数据
	 */
	public abstract void deleteSelectedData();

	/**
	 * 重新设置adapter数据
	 */
	public abstract void refreshAdapterData();

	/**
	 * 显示Toast提示
	 */
	protected void showToastTips(String msg) {
		if (!TextUtils.isEmpty(msg)) {
			UIHelper.showToast(getActivity(), msg);
		}
	}

	@Override
	public void showMultiChoice() {
		if (adapter != null)
			adapter.showMultiChoice();
	}

	@Override
	public void dismissMultiChoice() {
		if (adapter != null)
			adapter.dismissMultiChoice();
	}

	@Override
	public void selectAll() {
		if (adapter != null)
			adapter.selectAll();
	}

	@Override
	public void cancelSelectAll() {
		if (adapter != null)
			adapter.cancelSelectAll();
	}

	@Override
	public int getSelectedCount() {
		return adapter.getSelectedCount();
	}

	@Override
	public List<TransferInfo> getSelectedData() {
		return adapter.getSelectedData();
	}

	public AbstractFlashTransferRecordAdapter getAdapter() {
		return adapter;
	}

	protected void showLoading() {
		if(mLoadingDialog == null) {
			mLoadingDialog = new LoadingDialog(getActivity());
			mLoadingDialog.setTitle("请稍候...");
			mLoadingDialog.setCancelable(false);
		}
		
		mLoadingDialog.show();
	}

	protected void dismissLoading() {
		if(mLoadingDialog != null) {
			mLoadingDialog.dismiss();
		}
		
	}
}
