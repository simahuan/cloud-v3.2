package com.pisen.router.ui.phone.resource;

import java.util.List;


/**
 * 多选接口
 * @author ldj
 * @version 1.0 2015年5月7日 下午2:32:34
 */
public interface IMultiChoice<T> {
	/**
	 * 显示多选
	 */
	void showMultiChoice();
	/**
	 * 隐藏多想
	 */
	void dismissMultiChoice();
	/**
	 * 全选
	 */
	void selectAll();
	
	/**
	 * 取消全选
	 */
	void cancelSelectAll();
	
	List<T> getSelectedData();
	
	/**
	 * 删除已选择数据
	 *//*
	void deleteSelectedData();*/
	/**
	 * 获取已选数量
	 * @return
	 */
	int getSelectedCount();
}
