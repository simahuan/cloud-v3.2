package com.pisen.router.ui.base;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupWindow;

public class BasePopupWindow extends PopupWindow {

	public BasePopupWindow(Context context) {
		this(context, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	}

	public BasePopupWindow(Context context, int width, int height) {
		this(context, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);
	}

	public BasePopupWindow(Context context, int width, int height, boolean focusable) {
		super(context);
		setFocusable(focusable);
		// setOutsideTouchable(false);
		setWidth(width);
		setHeight(height);
		setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
	}

	public View findViewById(int id) {
		View view = getContentView();
		if (view != null) {
			return view.findViewById(id);
		}
		return null;
	}

	public void setContentView(Context context, int layoutResID) {
		setContentView(View.inflate(context, layoutResID, null));
	}

	@Override
	public void showAsDropDown(View anchor, int xoff, int yoff) {
		super.showAsDropDown(anchor, xoff, yoff);
		setBackgroundAlpha(0.9f);
	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		super.showAtLocation(parent, gravity, x, y);
		setBackgroundAlpha(0.9f);
	}

	@Override
	public void dismiss() {
		super.dismiss();
		setBackgroundAlpha(1.0f);
	}

	private void setBackgroundAlpha(float alpha) {
		/*
		 * WindowManager.LayoutParams params =
		 * activity.getWindow().getAttributes(); params.alpha = alpha;
		 * activity.getWindow().setAttributes(params);
		 */
	}

}
