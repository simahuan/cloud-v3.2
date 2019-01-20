package com.pisen.router.ui.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.sticky.listheaders.StickyListHeadersAdapter;

/**
 * list型分段视图adapter
 * @author ldj
 * @version 1.0 2015年5月27日 下午4:10:26
 */
public abstract class ListSectionChoiceAdapter<T> extends ResourceChoiceSectionAdapter<T> implements StickyListHeadersAdapter {
	
	public ListSectionChoiceAdapter(Context context) {
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