package com.pisen.router.common;

import android.view.View;
import android.view.View.OnClickListener;

public abstract class TimeIntervalClickListener implements OnClickListener {

	private long lastClickTime;
	//点击间隔时间
	private static final long INTERVAL = 500;
	
	private boolean isValidClick() {
		boolean valid = false;
		long cur = System.currentTimeMillis();
		if( cur - lastClickTime >=INTERVAL){
			valid =  true;
			lastClickTime = cur;
		}
		
		return valid;
	}
	
	public abstract void onTimeIntervalClick(View v);

	@Override
	public void onClick(View v) {
		if(isValidClick()) {
			onTimeIntervalClick(v);
		}
	}
}

