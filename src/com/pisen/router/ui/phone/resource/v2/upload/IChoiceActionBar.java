package com.pisen.router.ui.phone.resource.v2.upload;

import java.util.List;

public interface IChoiceActionBar<T> {

	/** 当前总项 */
	List<T> getItemAll();

	/** 当前选中项 */
	List<T> getCheckedItemAll();

	/** 全选/全不选 */
	void onActionBarItemCheckAll(boolean checked);
}
