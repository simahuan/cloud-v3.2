package com.pisen.router.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;

import com.pisen.router.R;
import com.sticky.gridheaders.StickyGridHeadersGridView;

public class TimeAxisStickyGridHeadersGridView extends StickyGridHeadersGridView {

	private Paint paint;
	private float timeAxisPaddingLeft;

	public TimeAxisStickyGridHeadersGridView(Context context) {
		this(context, null);
	}

	public TimeAxisStickyGridHeadersGridView(Context context, AttributeSet attrs) {
		this(context, attrs, android.R.attr.gridViewStyle);
	}

	public TimeAxisStickyGridHeadersGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setHeadersIgnorePadding(true);

		TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.TimeAxisStickyGridHeadersGridView);
		int timeAxisColor = mTypedArray.getColor(R.styleable.TimeAxisStickyGridHeadersGridView_timeAxisColor, Color.GRAY);
		float timeAxisWidth = mTypedArray.getDimension(R.styleable.TimeAxisStickyGridHeadersGridView_timeAxisWidth, 1);
		timeAxisPaddingLeft = mTypedArray.getDimension(R.styleable.TimeAxisStickyGridHeadersGridView_timeAxisPaddingLeft, 50);
		
		mTypedArray.recycle();
		paint = new Paint(Paint.DITHER_FLAG);
		paint.setAntiAlias(true);
		paint.setStyle(Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeWidth(timeAxisWidth);
		paint.setColor(timeAxisColor);
	}

	public void setTimeAxisLine(float width, int color) {
		paint.setStrokeWidth(width);
		paint.setColor(color);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		if (getPaddingLeft() > 0) {
			canvas.drawLine(timeAxisPaddingLeft, 0, timeAxisPaddingLeft, getBottom(), paint);
		}
		super.dispatchDraw(canvas);
	}
}
