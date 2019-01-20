package com.pisen.router.ui.phone.flashtransfer;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.ui.phone.settings.IconResource;

/**
 * AP创建视图布局
 * @author ldj
 * @version 1.0 2015年6月1日 下午5:22:02
 */
public class CreateApLayout extends FrameLayout{
	private static final String TAG = CreateApLayout.class.getSimpleName();
	private static final boolean DEBUG = true;
	
	private GradientProgressBar progressBar;
	private ViewGroup createSucceedLayout;
	private ViewGroup noBodyConnectLayout;
	private ViewGroup connectingLayout;
	private TextView tipView;
	
	public CreateApLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		View.inflate(context, R.layout.flash_transfer_createap, this);
		
		findView();
		initView();
	}

	private void findView() {
		progressBar = (GradientProgressBar) findViewById(R.id.pbar);
		createSucceedLayout = (ViewGroup) findViewById(R.id.createApSucceedLayout);
		noBodyConnectLayout = (ViewGroup) findViewById(R.id.createApNoBodyLayout);
		connectingLayout = (ViewGroup) findViewById(R.id.createApConnectingLayout);
		tipView = (TextView) findViewById(R.id.txtTip);
	}

	private void initView() {
	}
	
	/**
	 * 显示创建ap视图
	 */
	public void showCreateAp() {
		//清除之前可能设置的颜色
		progressBar.setArcColor(0,0);
		progressBar.setVisibility(View.VISIBLE);
		tipView.setVisibility(View.VISIBLE);
		createSucceedLayout.setVisibility(View.GONE);
		noBodyConnectLayout.setVisibility(View.GONE);
		connectingLayout.setVisibility(View.GONE);
		
		tipView.setText("正在创建连接...");
	}
	
	/**
	 * 显示没有客户端连接
	 */
	public void showNoClientConnect() {
		progressBar.setVisibility(View.VISIBLE);
		tipView.setVisibility(View.GONE);
		createSucceedLayout.setVisibility(View.GONE);
		noBodyConnectLayout.setVisibility(View.VISIBLE);
		connectingLayout.setVisibility(View.GONE);
		
		progressBar.setArcColor(Color.parseColor("#25EBA8"));
	}
	
	/**
	 * 显示取消创建ap视图
	 */
	public void showCancleCreateAp() {
		progressBar.setVisibility(View.VISIBLE);
		tipView.setVisibility(View.VISIBLE);
		createSucceedLayout.setVisibility(View.GONE);
		noBodyConnectLayout.setVisibility(View.GONE);
		connectingLayout.setVisibility(View.GONE);
		
		progressBar.setArcColor(Color.parseColor("#8698F6"));
		tipView.setText("取消创建连接");
	}
	
	/**
	 * ap创建成功
	 */
	public void showCreateApSucceed(String ssid) {
		progressBar.setVisibility(View.VISIBLE);
		tipView.setVisibility(View.GONE);
		createSucceedLayout.setVisibility(View.VISIBLE);
		noBodyConnectLayout.setVisibility(View.GONE);
		connectingLayout.setVisibility(View.GONE);
		
		progressBar.setArcColor(Color.parseColor("#24ECA8"));
		TextView tmp = (TextView)createSucceedLayout.findViewById(R.id.txtAp);
		tmp.setText(String.format("请朋友加入%s", ssid));
		tmp.requestFocus();
	}
	
	/**
	 *显示正在连接设备
	 */
	public void showConnectingDevice(String name, int iconRes) {
		progressBar.setVisibility(View.VISIBLE);
		tipView.setVisibility(View.GONE);
		createSucceedLayout.setVisibility(View.GONE);
		noBodyConnectLayout.setVisibility(View.GONE);
		connectingLayout.setVisibility(View.VISIBLE);
		
		progressBar.setArcColor(0,0);
		((TextView)connectingLayout.findViewById(R.id.txtName)).setText(name);
		((ImageView)connectingLayout.findViewById(R.id.imgHead)).setImageBitmap(IconResource.getIconWithCustom(getContext(), iconRes, true));
	}
	
	/**
	 * 设置进度
	 * @param progress
	 */
	public void setProgress(float progress) {
		progressBar.setProgress(progress);
	}
	
	/**
	 * XXX 需考虑显示其他视图时取消自动增加
	 * 自动增长进度
	 * @param from
	 * @param to
	 * @param milsecond
	 */
	public void autoIncreaseProgress(float from, final float to, long milsecond) {
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setProgress(0);
		
		if(task != null) {
			task.cancel();
			task = null;
		}
		if(timer != null) {
			timer.cancel();
			timer = null;
		}
		
		timer = new Timer();
		
		final float add = (to - from) / milsecond * PERIOD_POST;
		task = new TimerTask() {
			
			@Override
			public void run() {
				float p = progressBar.getProgress();
				if(p < to) {
					p +=add;
					progressBar.setProgress(p);
				}else {
					if(timer != null) {
					timer.cancel();
					timer = null;
					}
					task = null;
				}
			}
		};
		
		timer.schedule(task, 0, PERIOD_POST);
	}
	
	private static final long PERIOD_POST = 50;
	private Timer timer;
	private TimerTask task;
	
	public void setMax(float max) {
		progressBar.setMax(max);
	}

	public void showTip(String tip) {
		progressBar.setVisibility(View.VISIBLE);
		tipView.setVisibility(View.VISIBLE);
		createSucceedLayout.setVisibility(View.GONE);
		noBodyConnectLayout.setVisibility(View.GONE);
		tipView.setText(tip);
	}


}
