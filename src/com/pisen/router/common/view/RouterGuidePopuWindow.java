package com.pisen.router.common.view;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.studio.os.LogCat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.pisen.router.R;

public class RouterGuidePopuWindow extends PopupWindow implements OnClickListener {
	private View mMenuView;
	private ImageView imgGuideRouter, imgGuideCloud, imgGuideRecord;

	public RouterGuidePopuWindow(final Activity context) {
		super(context);
		mMenuView = LayoutInflater.from(context).inflate(R.layout.popupwindow_router, (ViewGroup) null);
		// 初始化图标步骤
		imgGuideRouter = (ImageView) mMenuView.findViewById(R.id.imgGuideRouter);
		imgGuideCloud = (ImageView) mMenuView.findViewById(R.id.imgGuideCloud);
		imgGuideRecord = (ImageView) mMenuView.findViewById(R.id.imgGuideRecord);

		imgGuideRouter.setOnClickListener(this);
		imgGuideCloud.setOnClickListener(this);
		imgGuideRecord.setOnClickListener(this);

		this.setContentView(mMenuView);
		this.setWidth(ActionBar.LayoutParams.MATCH_PARENT);
		this.setHeight(ActionBar.LayoutParams.MATCH_PARENT);
		this.setFocusable(true);
		this.setAnimationStyle(R.style.PopupGuideAnimation);
		ColorDrawable dw = new ColorDrawable(0);
		this.setOutsideTouchable(false);
		this.setBackgroundDrawable(dw);
	}

	private void recycleView(ImageView view) {
		if(view != null) {
			Drawable d = view.getDrawable();
			if (d != null && d instanceof BitmapDrawable) {
				Bitmap bmp = ((BitmapDrawable) d).getBitmap();
				if(bmp != null && !bmp.isRecycled()) {
					bmp.recycle();
					bmp = null;
					LogCat.e("recycleView");
				}
			}
			view.setImageBitmap(null);
			if (d != null) {
				d.setCallback(null);
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imgGuideRouter:
			imgGuideRouter.setVisibility(View.GONE);
			imgGuideRouter.setEnabled(false);
			imgGuideCloud.setVisibility(View.VISIBLE);
			imgGuideCloud.setEnabled(true);
			break;
		case R.id.imgGuideCloud:
			imgGuideCloud.setVisibility(View.GONE);
			imgGuideCloud.setEnabled(false);
			imgGuideRecord.setVisibility(View.VISIBLE);
			imgGuideRecord.setEnabled(true);
			break;

		case R.id.imgGuideRecord:
			imgGuideRecord.setVisibility(View.GONE);
			imgGuideRecord.setEnabled(false);
			dismiss();
			break;
		}
	}

	@Override
	public void dismiss() {
		super.dismiss();
		recycleView(imgGuideRouter);
		recycleView(imgGuideCloud);
		recycleView(imgGuideRecord);
	}
}
