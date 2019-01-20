package com.pisen.router.common.view;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.text.method.SingleLineTransformationMethod;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 跑马灯效果代码实现
 * @author Liuhc
 * @version 1.0 2015年7月1日 上午11:25:27
 */

public class MarqueeTextView extends TextView{

	public MarqueeTextView(Context context) {
		super(context);
	}

	public MarqueeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MarqueeTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * 开启跑马灯效果
	 */
	public void startMarguee(){
		setTransformationMethod(SingleLineTransformationMethod.getInstance());
        setSingleLine(true);
        setEllipsize(TruncateAt.MARQUEE);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setHorizontallyScrolling(true);
        setMarqueeRepeatLimit(-1);
        requestFocus();
	}
	
	/**
	 * 设置失去焦点
	 */
	public void setUnFocusable(){
		setFocusable(false);
	}
}
