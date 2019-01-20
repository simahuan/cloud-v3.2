package com.pisen.router.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.sticky.gridheaders.StickyGridHeadersSimpleAdapter;

/**
 * grid分段适配器
 * @author ldj
 * @version 1.0 2015年6月8日 下午12:19:57
 */
public abstract  class GridSectionChoiceAdapter<T> extends ResourceChoiceSectionAdapter<T> implements StickyGridHeadersSimpleAdapter {
	
	public GridSectionChoiceAdapter(Context context) {
		super(context);
	}
	
	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = newHeaderView(position, convertView, parent);
		}
		handleHeaderView(position, convertView, parent);
		return convertView;
	}

	public abstract View newHeaderView(final int position, View convertView, ViewGroup parent);

	public abstract void handleHeaderView(final int position, View convertView, ViewGroup parent);
}
