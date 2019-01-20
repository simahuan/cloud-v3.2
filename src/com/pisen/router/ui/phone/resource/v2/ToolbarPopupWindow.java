package com.pisen.router.ui.phone.resource.v2;

import android.app.Activity;
import android.view.View;
import android.view.WindowManager.LayoutParams;

import com.pisen.router.R;
import com.pisen.router.ui.base.BasePopupWindow;

public class ToolbarPopupWindow extends BasePopupWindow implements View.OnClickListener {

	public interface OnToolbarItemClickCallback {
		void onToolbarItemClick(View v);
	}

	static public enum ItemClickType {
		Download, Move, Rename, Delete
	}

	private OnToolbarItemClickCallback callback;

	public ToolbarPopupWindow(Activity activity) {
		super(activity);
		setFocusable(false);
		setOutsideTouchable(false);
		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setContentView(activity, R.layout.resource_home_toolbar);

		findViewById(R.id.btnDownload).setOnClickListener(this);
		findViewById(R.id.btnMove).setOnClickListener(this);
		findViewById(R.id.btnRename).setOnClickListener(this);
		findViewById(R.id.btnDelete).setOnClickListener(this);
	}

	public void setOnToolbarItemClickCallback(OnToolbarItemClickCallback callback) {
		this.callback = callback;
	}

	public View findViewById(int id) {
		View view = getContentView();
		if (view != null) {
			return view.findViewById(id);
		}
		return null;
	}

	@Override
	public void onClick(View v) {
		if (callback != null) {
			callback.onToolbarItemClick(v);
		}
	}

}
