package com.pisen.router.ui.phone.resource.v2.panel;

import com.pisen.router.ui.phone.resource.v2.upload.IChoiceActionBar;

public interface ISelectionActionBar<T> extends IChoiceActionBar<T> {

	/** 取消选中 */
	void onActionBarItemCheckCancel();

	/** 操作完成 boolean refresh */
	void onActionBarCompleted();
}
