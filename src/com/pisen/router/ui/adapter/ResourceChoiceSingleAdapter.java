package com.pisen.router.ui.adapter;

import android.content.Context;

/**
 * 不支持分段选择的资源adapter
 * @author ldj
 * @version 1.0 2015年7月20日 上午11:26:40
 */
public abstract class ResourceChoiceSingleAdapter<T> extends ResourceChoiceAdapter<T> {

	public ResourceChoiceSingleAdapter(Context context) {
		super(context);
	}
}
