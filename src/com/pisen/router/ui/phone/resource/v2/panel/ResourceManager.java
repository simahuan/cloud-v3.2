package com.pisen.router.ui.phone.resource.v2.panel;

import android.widget.FrameLayout;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.ui.phone.resource.v2.RouterFragment;

public class ResourceManager {

	private RouterFragment fragment;
	private FrameLayout categoryLayout;
	public FileType fileType;

	public ResourceManager(RouterFragment fragment) {
		this.fragment = fragment;
		this.categoryLayout = (FrameLayout) fragment.findViewById(R.id.categoryLayout);
	}

	public CategoryView switchPanel(String path, FileType model) {
		categoryLayout.removeAllViews();
		CategoryView panel = getPanel(path, model);
		if (panel != null) {
			categoryLayout.addView(panel);
		}
		return panel;
	}

	/**
	 * 判断是否分类显示
	 * 
	 * @return
	 */
	public boolean isCategoryPanel() {
		return categoryLayout.getChildCount() > 0;
	}

	/**
	 * 删除分类显示
	 */
	public void removeCategoryPanel() {
		categoryLayout.removeAllViews();
	}

	/**
	 * @des   展示面板（GridView,ListView）
	 * @param path
	 * @param model
	 * @return
	 */
	private CategoryView getPanel(String path, FileType model) {
		fileType = model;
		switch (model) {		
		case Image:
			return new GridCategoryView(fragment, path, model);
		case Video:
		case Audio:
		case Document:
		case Apk:
			return new ListCategoryView(fragment, path, model);
		default:
			return null;
		}
	}
}
