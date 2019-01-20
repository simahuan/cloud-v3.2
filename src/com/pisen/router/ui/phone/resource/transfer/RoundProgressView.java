package com.pisen.router.ui.phone.resource.transfer;

import java.security.InvalidParameterException;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.pisen.router.R;

/**
 * 支持播放、暂停、不可用三种状态的圆形进度控件
 * @author ldj
 * @version 1.0 2015年5月11日 上午10:08:43
 */
public class RoundProgressView extends View {
	// 画笔对象的引用
	private Paint paint;
	// 圆环的颜色
	private int roundColor;
	// 圆环进度的颜色
	private int roundProgressColor;
	// 圆环disable状态进度的颜色
	private int roundProgressDisabledColor;
	// 圆环的宽度
	private float roundWidth;
	// 最大进度
	private int max;
	// 当前进度
	private int progress;
	//默认背景资源
	private Drawable defaultDrawable;
	//运行中背景资源
	private Drawable runningDrawable;
	//不可用背景资源
	private Drawable disableRunningDrawable;
	//不可用背景资源
	private Drawable disableIdelDrawable;
	//普通状态
	public static final int STATUS_IDLE = 0X100;
	//运行状态
	public static final int STATUS_RUNNING = 0X101;
	//不可用状态
	public static final int STATUS_DISABLE = 0X102;
	//当前进度圆环区域
	private RectF arcOval;
	//当前状态
	private int curStatus = STATUS_IDLE;
	
	public RoundProgressView(Context context) {
		this(context, null);
	}

	public RoundProgressView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RoundProgressView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		init();
		TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RoundProgressBar);
		roundColor = mTypedArray.getColor(R.styleable.RoundProgressBar_rpbarRoundColor, Color.GRAY);
		roundProgressColor = mTypedArray.getColor(R.styleable.RoundProgressBar_rpbarRundProgressColor, Color.GREEN);
		roundProgressDisabledColor = mTypedArray.getColor(R.styleable.RoundProgressBar_rpbarDisabledColor, Color.RED);
		roundWidth = mTypedArray.getDimension(R.styleable.RoundProgressBar_rpbarRoundWidth, 5);
		max = mTypedArray.getInteger(R.styleable.RoundProgressBar_rpbarMax, 100);

		mTypedArray.recycle();
		//设置默认背景
		setDrawableBackground(defaultDrawable);
	}
	
	/**
	 * 初始化参数
	 */
	private void init() {
		paint = new Paint();
		defaultDrawable = getResources().getDrawable(R.drawable.transfer_idle_normal);
		runningDrawable = getResources().getDrawable(R.drawable.transfer_pause_normal);
		disableIdelDrawable = getResources().getDrawable(R.drawable.transfer_idle_disable);
		disableRunningDrawable = getResources().getDrawable(R.drawable.transfer_pause_disable);
	}

	/**
	 * 设置进度控件状态
	 * @param status
	 */
	public void setStatus(int status) {
		if(curStatus == status) return;
		
		switch (status) {
		case STATUS_IDLE:
			setDrawableBackground(defaultDrawable);
			break;
		case STATUS_RUNNING:
			setDrawableBackground(runningDrawable);
			break;
		case STATUS_DISABLE:
			setDrawableBackground(curStatus == STATUS_RUNNING ? disableRunningDrawable : disableIdelDrawable);
			break;

		default:
			break;
		}
		
		curStatus = status;
	}

	private void setDrawableBackground(Drawable drawable) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			super.setBackground(drawable);
		} else {
			setBackgroundDrawable(drawable);
		}
	}

	public void toggle() {
		if(curStatus == STATUS_DISABLE) {
			throw new InvalidParameterException();
		}
		
		int tmp  = curStatus == STATUS_IDLE ? STATUS_RUNNING : STATUS_IDLE;
		setStatus(tmp);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		/* 画背景圆环 */
		int centre = getWidth() / 2; // 获取圆心的x坐标
		int radius = (int) (centre - roundWidth / 2); // 圆环的半径
		paint.setStrokeWidth(roundWidth); // 设置圆环的宽度
		paint.setColor(roundColor); // 设置圆环的颜色
		paint.setStyle(Paint.Style.STROKE); // 设置空心
		paint.setAntiAlias(true); // 消除锯齿
		canvas.drawCircle(centre, centre, radius, paint); // 画出圆环
		/*画当前进度*/
		if(curStatus == STATUS_DISABLE) { // 设置进度的颜色
			paint.setColor(roundProgressDisabledColor);
		}else {
			paint.setColor(roundProgressColor);
		}
		if(arcOval == null) {
			arcOval = new RectF(centre - radius, centre - radius, centre + radius, centre + radius); // 用于定义的圆弧的形状和大小的界限
		}
		if(curStatus == STATUS_IDLE && progress <=0) {
			canvas.drawArc(arcOval, 0, 360, false, paint); // 根据进度画圆弧
		}else {
			canvas.drawArc(arcOval, 0, 360 * progress / max, false, paint); // 根据进度画圆弧
		}
	}

	public synchronized int getMax() {
		return max;
	}

	/**
	 * 设置进度的最大值
	 * 
	 * @param max
	 */
	public synchronized void setMax(int max) {
		if (max < 0) {
			throw new IllegalArgumentException("max not less than 0");
		}
		this.max = max;
	}

	/**
	 * 获取进度.需要同步
	 * 
	 * @return
	 */
	public synchronized int getProgress() {
		return progress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步 刷新界面调用postInvalidate()能在非UI线程刷新
	 * @param progress
	 */
	public synchronized void setProgress(int progress) {
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

	public int getCricleColor() {
		return roundColor;
	}

	public void setCricleColor(int cricleColor) {
		this.roundColor = cricleColor;
	}

	public int getCricleProgressColor() {
		return roundProgressColor;
	}

	public void setCricleProgressColor(int cricleProgressColor) {
		this.roundProgressColor = cricleProgressColor;
	}

	public float getRoundWidth() {
		return roundWidth;
	}

	public void setRoundWidth(float roundWidth) {
		this.roundWidth = roundWidth;
	}

}
