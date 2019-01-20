package com.pisen.router.ui.phone.flashtransfer;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import com.pisen.router.R;

/**
 * 渐变进度条
 * @author ldj
 * @version 1.0 2015年5月11日 上午10:08:43
 */
public class GradientProgressBar extends View {
	// 画笔对象的引用
	private Paint paintCenter;
	private Paint paintStroke;
	// 最大进度
	private float max;
	// 当前进度
	private float progress;
	// 当前进度圆环区域
	private RectF arcOval;

	private float strokeWidth;
	private float spaceWidth;

	private int startColor;
	private int middleColor;
	private int endColor;
	private LinearGradient linearGradient;
	//圆弧颜色
	private int arcColor;

	public GradientProgressBar(Context context) {
		this(context, null);
	}

	public GradientProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public GradientProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.GradientProgressBar);
		strokeWidth = mTypedArray.getDimension(R.styleable.GradientProgressBar_paintStrokeWidth, 35);
		spaceWidth = mTypedArray.getDimension(R.styleable.GradientProgressBar_spaceWidth, 10);
		startColor = mTypedArray.getColor(R.styleable.GradientProgressBar_startColor, 0xff25eba8);
		middleColor = mTypedArray.getColor(R.styleable.GradientProgressBar_centerColor, 0xff3ec5c1);
		endColor = mTypedArray.getColor(R.styleable.GradientProgressBar_endColor, 0xff7E64FE);

		mTypedArray.recycle();
		init();
	}

	/**
	 * 初始化参数
	 */
	private void init() {

		paintCenter = new Paint(Paint.DITHER_FLAG);
		paintCenter.setAntiAlias(true); // 消除锯齿
		paintCenter.setColor(Color.WHITE); // 设置圆环的颜色
		paintCenter.setStyle(Paint.Style.FILL); // 设置实心

		
		paintStroke = new Paint(Paint.DITHER_FLAG);
		paintStroke.setAntiAlias(true); // 消除锯齿
		paintStroke.setStrokeWidth(strokeWidth); // 设置圆环的宽度
		paintStroke.setStrokeCap(Cap.ROUND);
		paintStroke.setStyle(Paint.Style.STROKE);

	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int centre = getWidth() / 2;
		int radius = (int) (centre - 1.5 * strokeWidth - spaceWidth); 
		//画背景圆
		canvas.drawCircle(centre, centre, radius, paintCenter); 

		/* 画当前进度 */
		if (arcOval == null || linearGradient == null) {
			arcOval = new RectF(strokeWidth / 2, strokeWidth / 2, getWidth() - strokeWidth / 2, getHeight() - strokeWidth / 2);
			linearGradient = new LinearGradient(0, 0, getWidth(), getHeight(), new int[] { startColor, middleColor, endColor }, null, Shader.TileMode.REPEAT);
			paintStroke.setShader(linearGradient);
		}
		
		if(arcColor != 0) {
			//圆弧为纯色
			paintStroke.setColor(arcColor);
			paintStroke.setShader(null);
		}else {
			if (linearGradient == null) {
				linearGradient = new LinearGradient(0, 0, getWidth(), getHeight(), new int[] { startColor, middleColor, endColor }, null, Shader.TileMode.REPEAT);
			}
			//使用着色器
			paintStroke.setShader(linearGradient);
			paintStroke.setColor(-1);
		}
		//画圆弧
		if(progress >0) {
		canvas.drawArc(arcOval, 0, 360 * progress / max, false, paintStroke); 
		}
	}
	
	/**
	 * 设置圆弧颜色
	 * @param color
	 */
	public void setArcColor(int color) {
		this.arcColor = color;
		postInvalidate();
	}
	
	/**
	 * 设置圆弧颜色及进度
	 * @param color
	 * @param progress
	 */
	public void setArcColor(int color, int progress) {
		this.arcColor = color;
		this.progress = progress;
		
		postInvalidate();
	}

	public synchronized float getMax() {
		return max;
	}

	/**
	 * 设置进度的最大值
	 * 
	 * @param max
	 */
	public synchronized void setMax(float max) {
		if (max < 0) {
			throw new IllegalArgumentException("max not less than 0");
		}
		this.max = max;
	}

	/**
	 * 获取进度
	 * @return
	 */
	public synchronized float getProgress() {
		return progress;
	}

	/**
	 * @param progress
	 */
	public synchronized void setProgress(float progress) {
		if (progress < 0) {
			throw new IllegalArgumentException("progress not less than 0");
		}
		if (progress > max) {
			progress = max;
		}
		if (progress <= max) {
			this.progress = progress;
			postInvalidate();
		}
	}

}
