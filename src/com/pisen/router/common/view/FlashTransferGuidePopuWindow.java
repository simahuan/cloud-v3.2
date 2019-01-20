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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.pisen.router.R;

public class FlashTransferGuidePopuWindow extends PopupWindow implements OnClickListener {
    private View mMenuView;
    private ImageView imgGuideTransfer , imgGuideRecord;

    public FlashTransferGuidePopuWindow(final Activity context) {
        super(context);
        mMenuView = LayoutInflater.from(context).inflate(R.layout.popupwindow_flashtransfer, (ViewGroup)null);
        //初始化图标步骤
        imgGuideTransfer = (ImageView) mMenuView.findViewById(R.id.imgGuideTransfer);
        imgGuideRecord = (ImageView) mMenuView.findViewById(R.id.imgGuideRecord);
        
        imgGuideRecord.setOnClickListener(this);
        imgGuideTransfer.setOnClickListener(this);

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
            case R.id.imgGuideRecord:
            	imgGuideRecord.setVisibility(View.GONE);
            	imgGuideRecord.setEnabled(false);
            	imgGuideTransfer.setVisibility(View.VISIBLE);
            	imgGuideTransfer.setEnabled(true);
                break;
            case R.id.imgGuideTransfer:
            	imgGuideTransfer.setVisibility(View.GONE);
            	imgGuideTransfer.setEnabled(false);
            	dismiss();
                break;
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        recycleView(imgGuideRecord);
        recycleView(imgGuideTransfer);
    }
}
