/*
 * 官网地站:http://www.mob.com
 * 技术支持QQ: 4006852216
 * 官方微信:ShareSDK   （如果发布新版本的话，我们将会第一时间通过微信将版本更新内容推送给您。如果使用过程中有任何问题，也可以通过微信与我们取得联系，我们将会在24小时内给予回复）
 *
 * Copyright (c) 2013年 mob.com. All rights reserved.
 */

package cn.sharesdk.onekeyshare.theme;
import static com.mob.tools.utils.R.getBitmapRes;
import static com.mob.tools.utils.R.getStringRes;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import cn.sharesdk.onekeyshare.PlatformListFakeActivity;

import com.pisen.router.R;
import com.pisen.router.common.utils.UIHelper;

/**
 * 分享平台列表展示页
 * @author  mahuan
 * @version 1.0 2015年6月30日 上午11:00:08
 */
public class PlatformListPage extends PlatformListFakeActivity implements View.OnClickListener {
	// page container
	private FrameLayout flPage;
	// gridview of platform list
	private PlatformGridView grid;
	// cancel button
	private Button btnCancel;
	// sliding up animation
	private Animation animShow;
	// sliding down animation
	private Animation animHide;
	private boolean finishing;
	private LinearLayout llPage;

	public void onCreate() {
		super.onCreate();

		finishing = false;
		initPageView();
		initAnim();
		activity.setContentView(flPage);

		// set the data for platform gridview
		grid.setData(shareParamsMap, silent);
		grid.setHiddenPlatforms(hiddenPlatforms);
		grid.setCustomerLogos(customerLogos);
		grid.setParent(this);
		btnCancel.setOnClickListener(this);

		// display gridviews
		llPage.clearAnimation();
		llPage.startAnimation(animShow);
	}

	//初始化弹出选项
	private void initPageView() {
		flPage = new FrameLayout(getContext());
		flPage.setOnClickListener(this);
		flPage.setBackgroundDrawable(new ColorDrawable(0x55333333));

		// container of the platform gridview
		llPage = new LinearLayout(getContext()) {
			public boolean onTouchEvent(MotionEvent event) {
				return true;
			}
		};
		
		int dp_13 = com.mob.tools.utils.R.dipToPx(getContext(), 13);
		llPage.setPadding(dp_13, dp_13, dp_13, dp_13);
		llPage.setOrientation(LinearLayout.VERTICAL);
		llPage.setBackgroundDrawable(new ColorDrawable(0x00ffffff));
		FrameLayout.LayoutParams lpLl = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
		lpLl.gravity = Gravity.BOTTOM;
		llPage.setLayoutParams(lpLl);
		flPage.setLayoutParams(lpLl);
		flPage.addView(llPage);
		
// 		gridview
		grid = new PlatformGridView(getContext());
		grid.setBackgroundResource(R.drawable.classic_platform_corners_bg);
//		grid.setEditPageBackground(getBackgroundView());
		
		LinearLayout.LayoutParams lpWg = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		grid.setGravity(Gravity.CENTER);
		grid.setLayoutParams(lpWg);
		llPage.addView(grid);

		// cancel button
		btnCancel = new Button(getContext());
		btnCancel.setTextColor(0xff0073FF); //设置Cancel字色
		btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
		int resId = getStringRes(getContext(), "cancel");
		if (resId > 0) {
			btnCancel.setText(resId);
		}
		btnCancel.setPadding(0, 0, 0, com.mob.tools.utils.R.dipToPx(getContext(), 0));

		resId = getBitmapRes(getContext(), "classic_platform_corners_bg");
		if(resId > 0){
			btnCancel.setBackgroundResource(resId);
		}else {
		    btnCancel.setBackgroundDrawable(new ColorDrawable(0xffffffff));
		}
		
		LinearLayout.LayoutParams lpBtn = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT, com.mob.tools.utils.R.dipToPx(getContext(), 60));
		int dp_10 = com.mob.tools.utils.R.dipToPx(getContext(), 10);
		lpBtn.setMargins(0, dp_10, 0, 0);
		btnCancel.setLayoutParams(lpBtn);
		llPage.addView(btnCancel);
	}

	private void initAnim() {
		animShow = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 1,
				Animation.RELATIVE_TO_SELF, 0);
		animShow.setDuration(300);

		animHide = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 0,
				Animation.RELATIVE_TO_SELF, 1);
		animHide.setDuration(300);
	}

	public void onConfigurationChanged(Configuration newConfig) {
		if (grid != null) {
			grid.onConfigurationChanged();
		}
	}

	public boolean onFinish() {
		if (finishing) {
			return super.onFinish();
		}

		if (animHide == null) {
			finishing = true;
			return false;
		}

		finishing = true;
		animHide.setAnimationListener(new Animation.AnimationListener() {
			public void onAnimationStart(Animation animation) {

			}

			public void onAnimationRepeat(Animation animation) {

			}

			public void onAnimationEnd(Animation animation) {
				flPage.setVisibility(View.GONE);
				finish();
			}
		});
		llPage.clearAnimation();
		llPage.startAnimation(animHide);
		return true;
	}

	/**
	 * 退出弹框层
	 */
	@Override
	public void onClick(View v) {
		if (v.equals(flPage) || v.equals(btnCancel)) {
			setCanceled(true);
			finish();
		}
	}

	/**
	 * @des 把调用该方法的当前对象 传进来
	 * @param v
	 * @param platforms
	 */
	public void onPlatformIconClick(View v, ArrayList<Object> platforms) {
		onShareButtonClick(v, platforms);
	}
	
	private void hideSoftwareWindow(String s){
		UIHelper.showToast(activity, "方法调用..." + s);
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);  
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);  
//		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
}
