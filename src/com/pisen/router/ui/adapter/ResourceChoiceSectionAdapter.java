package com.pisen.router.ui.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

/**
 * 分段资源选择适配器
 * @author ldj
 * @version 1.0 2015年6月8日 下午12:19:57
 */
public abstract class ResourceChoiceSectionAdapter<T> extends ResourceChoiceAdapter<T> {
	protected ArrayList<Object> selectedSectionHeaders;
	
	public ResourceChoiceSectionAdapter(Context context) {
		super(context);

		init();
	}

	private void init() {
		selectedSectionHeaders = new ArrayList<Object>();
	}
	
	/**
	 * 是否选中所有指定headerID段
	 * @param headerID	
	 * @param checkAll
	 */
	public void checkSectionAll(Object headerID, boolean checkAll) {
		if(checkAll) {//全选该分段
			selectSection(headerID);
		}else {//取消全选该分段
			unselectSection(headerID);
		}
	}
	
	/**
	 * 通过position获取headerid
	 * @param position
	 * @return
	 */
	protected abstract Object getHeaderByPosition(int position);

	public boolean isSectionAllChecked(Object headerID) {
		int size = getCount();
		for(int i=0; i<size; i++) {
			if(headerID.equals(getHeaderByPosition(i))) {
				T tmp = getItem(i);
				if(!contains(selectedData,tmp)) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	public boolean containsHeader(List<Object> selectedSectionHeaders, Object headerID) {
		return selectedSectionHeaders == null ? false : selectedSectionHeaders.contains(headerID);
	}
	
	/**
	 * 全选分段
	 * @param headerID
	 */
	private void selectSection(Object headerID) {
//		selectedSectionHeaders.add(headerID);
		int size = getCount();
		for(int i=0; i<size; i++) {
			if(headerID.equals(getHeaderByPosition(i))) {
				T tmp = getItem(i);
				if(!contains(selectedData,tmp)) {
					selectedData.add(tmp);
				}
			}
		}
	}
	
	/**
	 * 取消全选分段
	 * @param headerID
	 */
	public void  unselectSection(Object headerID) {
		int size = getCount();
		for(int i=0; i<size; i++) {
			if(headerID.equals(getHeaderByPosition(i))) {
				T tmp = getItem(i);
				if(contains(selectedData,tmp)) {
					remove(selectedData, tmp);
				}
			}
		}
	}

	/**
	 * 清楚选择，并刷新界面
	 */
	public void clearSelect() {
		selectedData.clear();
		selectedSectionHeaders.clear();
		
		notifyDataSetChanged();
	}
}
