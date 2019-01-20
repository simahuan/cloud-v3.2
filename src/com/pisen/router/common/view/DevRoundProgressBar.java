package com.pisen.router.common.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import com.pisen.router.R;

/**
 * @author  mahuan
 * @version 1.0 2015年5月29日 下午1:25:51
 */
public class DevRoundProgressBar extends View {
	/** 外圆画笔 */
	private Paint outerPaint;
	/** 圆弧画笔 */
	private Paint arcPaint;
	/** 文本画笔 */
	private Paint textPaint;
	/** %百分号画笔 */
	private Paint percentPaint;
	
	/** 当前进度区域 */
	private RectF arcOval;
	
	/** 圆环的颜色 */
	private int devRoundColor;
	/** 圆环进度的颜色 */
	private int devRoundProgressColor;
	
	/** 中间进度百分比的字符串的颜色*/
	private int devTextColor ;
	
	/**
	 * 中间进度百分比的字符串的字体
	 */
	private float devTextSize = 40;
	
	/**
	 * 圆环的宽度
	 */
	private float devRoundWidth;
	private float defStrokeWidth = 0;
	
	/**
	 * 最大进度
	 */
	private int devMaxProgress = 100;
	
	/**
	 * 当前进度
	 */
	private int devCurrentProgress = 0;
	
	/** 线型渐变化 */
	private LinearGradient linearGradient;
	/** 渐变起始色 */
	private int startColor = 0xFF05FFF0;
	/** 渐变中间色 */
	private int middleColor = 0xFF0CFFDC;
	/** 渐变结束色 */
	private int endColor = 0xFF1DFFAD;
	
	private boolean bDiskNotNULL = false;
	private int restPercent;
	
	
	public DevRoundProgressBar(Context context) {
		this(context, null);
	}

	public DevRoundProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public DevRoundProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		TypedArray mTypedArray = context.obtainStyledAttributes(attrs,R.styleable.DevRoundProgressBar);
		devRoundColor = mTypedArray.getColor(R.styleable.DevRoundProgressBar_devRoundColor, Color.RED);
		devRoundProgressColor = mTypedArray.getColor(R.styleable.DevRoundProgressBar_devRoundProgressColor, Color.GREEN);
		devTextColor = mTypedArray.getColor(R.styleable.DevRoundProgressBar_devTextColor, Color.parseColor("#FF666666"));
		devTextSize = mTypedArray.getDimension(R.styleable.DevRoundProgressBar_devTextSize, 120);
		devRoundWidth = mTypedArray.getDimension(R.styleable.DevRoundProgressBar_devRoundWidth, 5);
		devMaxProgress = mTypedArray.getInteger(R.styleable.DevRoundProgressBar_devMax, 100);
		mTypedArray.recycle();
		mTypedArray = null;
		
		initView();
	}
	
	private void initView(){
		outerPaint  = new Paint(Paint.DITHER_FLAG);
		outerPaint.setColor(devRoundColor);
		outerPaint.setStyle(Paint.Style.STROKE);
		outerPaint.setStrokeWidth(devRoundWidth);
		outerPaint.setAntiAlias(true);
		
		arcPaint = new Paint(Paint.DITHER_FLAG);
		arcPaint.setColor(devRoundColor);
		arcPaint.setAntiAlias(true);
		arcPaint.setStrokeWidth(devRoundWidth);
		arcPaint.setStrokeCap(Cap.ROUND);
		arcPaint.setStyle(Paint.Style.STROKE);
		
		textPaint = new Paint(Paint.DITHER_FLAG);
		textPaint.setAntiAlias(true);
		textPaint.setStyle(Paint.Style.FILL);
		textPaint.setTypeface(Typeface.SANS_SERIF);
		textPaint.setStrokeWidth(defStrokeWidth);
		textPaint.setColor(devTextColor);
		textPaint.setTextSize(devTextSize);
		
		percentPaint = new Paint(Paint.DITHER_FLAG);
		percentPaint.setAntiAlias(true);
		percentPaint.setTypeface(Typeface.MONOSPACE);
		percentPaint.setStyle(Paint.Style.FILL);
		percentPaint.setStrokeWidth(defStrokeWidth);
		percentPaint.setColor(devTextColor);
		percentPaint.setTextSize(devTextSize/4);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		/**
		 * 画最外层的大圆环
		 */
		int centre = getWidth()/2; //获取圆心的x坐标,获取可显示物理设备的中心点
		int radius = (int) (centre - devRoundWidth/2); //圆环的半径
		canvas.drawCircle(centre, centre, radius, outerPaint); //画出圆环
		
		/**
		 * 画圆弧 ,画圆环的进度
		 */
		if (arcOval == null || linearGradient == null){
			arcOval = new RectF(centre - radius, centre - radius, centre + radius, centre + radius);  //用于定义的圆弧的形状和大小的界限
			linearGradient = new LinearGradient(0, 0, getWidth(), getHeight(), new int[] { startColor,middleColor,endColor}, null, Shader.TileMode.MIRROR);
			arcPaint.setShader(linearGradient);
		}
		if (devCurrentProgress > 0){
			canvas.drawArc(arcOval, 270, 360 * (float) devCurrentProgress / (float) devMaxProgress, false, arcPaint); // 根据进度画圆弧
		}

		/**
		 * 画进度百分比
		 */
		if (getDiskMounted()){
			restPercent  = (int) ((1- ((float)devCurrentProgress/(float)devMaxProgress))*100);
		}else {
			restPercent  = (int) (((float)devCurrentProgress/(float)devMaxProgress)*100);
		}
		float textWidth = textPaint.measureText(restPercent + "");   //测量字体宽度，我们需要根据字体的宽度设置在圆环中间
		canvas.drawText(restPercent + "", centre - textWidth / 2 - 10, centre + devTextSize/3, textPaint); //画出进度百分比
		canvas.drawText("%", centre + textWidth/2, centre +30, percentPaint);
	}
	
	
	/**
	 * @describtion 获取最大进度
	 * @return
	 */
	public synchronized int getMax() {
		return devMaxProgress;
	}

	/**
	 * 设置进度的最大值
	 * @param max
	 */
	public synchronized void setMax(int max) {
		if(max < 0){
			throw new IllegalArgumentException("max not less than 0");
		}
		this.devMaxProgress = max;
	}

	/**
	 * 获取进度.需要同步
	 * @return
	 */
	public synchronized int getProgress() {
		return devCurrentProgress;
	}

	/**
	 * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
	 * 刷新界面调用postInvalidate()能在非UI线程刷新
	 * @param progress
	 */
	public synchronized void setProgress(int progress) {
		if(progress > devMaxProgress){
			progress = devMaxProgress;
		}
		if(progress <= devMaxProgress){
			this.devCurrentProgress = progress;
			postInvalidate();
		}
	}
	
	
	public int getCricleColor() {
		return devRoundColor;
	}

	public void setCricleColor(int cricleColor) {
		this.devRoundColor = cricleColor;
	}

	public int getCricleProgressColor() {
		return devRoundProgressColor;
	}

	public void setCricleProgressColor(int cricleProgressColor) {
		this.devRoundProgressColor = cricleProgressColor;
	}

	public int getTextColor() {
		return devTextColor;
	}

	public void setTextColor(int textColor) {
		this.devTextColor = textColor;
	}

	public float getTextSize() {
		return devTextSize;
	}

	public void setTextSize(float textSize) {
		this.devTextSize = textSize;
	}

	public float getRoundWidth() {
		return devRoundWidth;
	}

	public void setRoundWidth(float roundWidth) {
		this.devRoundWidth = roundWidth;
	}
	
	public void setDiskMounted(boolean mounted){
		this.bDiskNotNULL = mounted;
	}
	public boolean getDiskMounted(){
		return bDiskNotNULL;
	}
}
