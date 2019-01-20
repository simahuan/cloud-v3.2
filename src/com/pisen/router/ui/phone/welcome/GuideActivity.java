package com.pisen.router.ui.phone.welcome;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.ui.HomeActivity;
import com.pisen.router.ui.base.CloudActivity;

public class GuideActivity extends CloudActivity implements OnPageChangeListener {
	private ViewPager viewPager;
	private GuideViewPagerAdapter vpAdapter;
	private ArrayList<View> views;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_welcome_guide);
		initView();
		initData();
	};

	private void initView() {
		views = new ArrayList<View>();
		View skipView =  View.inflate(this,  R.layout.cloud_welcome_guide_view01, null);
		views.add(skipView);
		views.add(View.inflate(this, R.layout.cloud_welcome_guide_view02, null));
		views.add(View.inflate(this, R.layout.cloud_welcome_guide_view03, null));
		View lastView = View.inflate(this, R.layout.cloud_welcome_guide_view04, null);
		((TextView)lastView.findViewById(R.id.txtPublishVer)).setText(UIHelper.getCloudVersion(this));
		views.add(lastView);
		
		skipView.findViewById(R.id.imgSkip).setOnClickListener(mOnClickListener);
		lastView.findViewById(R.id.btnTiyan).setOnClickListener(mOnClickListener);
		viewPager = (ViewPager) findViewById(R.id.guide_viewpager);
		vpAdapter = new GuideViewPagerAdapter(views);
	}

	private void initData() {
		viewPager.setOnPageChangeListener(this);
		viewPager.setAdapter(vpAdapter);
	}

	private final OnClickListener mOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.imgSkip:
				skipButton();
				break;
			case R.id.btnTiyan:
				GuideActivity.this.finish();
				overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
				break;
			}
		}
	};
	
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		exitApplication();
		return super.onKeyDown(keyCode, event);
	};
	
	@Override
	public void onPageScrollStateChanged(int arg0) {}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@Override
	public void onPageSelected(int arg0) {}

	private void skipButton() {
		Intent intent = new Intent();
		intent.setClass(GuideActivity.this, HomeActivity.class);
		startActivity(intent);
		overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
		this.finish();
	}
}
