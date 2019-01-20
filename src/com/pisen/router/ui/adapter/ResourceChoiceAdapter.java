package com.pisen.router.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.pisen.router.ui.phone.resource.OnSelectedCountChangeListener;

/**
 * 支持多选、长按的资源适配器
 * @author ldj
 * @version 1.0 2015年5月18日 下午2:57:42
 */
public abstract class ResourceChoiceAdapter<T> extends android.studio.view.widget.BaseAdapter<T> {
	//单击选项监听
	protected OnItemClickListener itemClickListener;
	//长按监听
	protected OnItemLongClickListener itemLongClickListener;
	protected OnSelectedCountChangeListener countChangeListener;
	//已选择数据
	protected List<T> selectedData;
	//是否可选
	private boolean checkEnabled;

	public ResourceChoiceAdapter(Context context) {
		super(context);
		
		init();
	}

	private void init() {
		selectedData = new ArrayList<T>();
	}
	
	public List<T> getSelectedData() {
		return selectedData;
	}

	public void setSelectedData(List<T> selectedData) {
		this.selectedData = selectedData;
	}
	
	public void setOnSelectedCountChangeListener(OnSelectedCountChangeListener listener) {
		this.countChangeListener = listener;
	}
	
	/**
	 * 设置单行点击监听
	 * @param itemClickListener
	 */
	public void setOnItemClickListener(OnItemClickListener itemClickListener) {
		this.itemClickListener = itemClickListener;
	}
	
	public void setOnItemLongClickListener(OnItemLongClickListener itemLongClickListener) {
		this.itemLongClickListener = itemLongClickListener;
	}
	

	public boolean isCheckEnabled() {
		return checkEnabled;
	}

	public void setCheckEnabled(boolean checkEnabled) {
		this.checkEnabled = checkEnabled;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		if(convertView == null) {
			convertView = newItemView(position, convertView, parent);
		}
		handleItemView(position, convertView, parent);
		return convertView;
	}
	
	public abstract View newItemView(final int position, View convertView, ViewGroup parent);

	public abstract void handleItemView(final int position, View convertView, ViewGroup parent);
	
	public abstract void toggleItemViewCheck(final int position, View convertView, ViewGroup parent);
	
	/**
	 * 数据集中是否包含
	 * @param data
	 * @param info
	 * @return
	 */
	public abstract boolean contains(List<T> data, T info);
	
	/**
	 * 数据集中移除指定item
	 * @param data
	 * @param info
	 * @return
	 */
	public abstract boolean remove(List<T> data,T info);
}
