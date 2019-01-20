package com.pisen.router.common.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class NoScrollViewPager extends ViewPager {

	private boolean mScroll = false;

	public NoScrollViewPager(Context context) {
		super(context);
	}

	public NoScrollViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setScroll(boolean scroll) {
		this.mScroll = scroll;
	}

	@Override
	public void scrollTo(int x, int y) {
		if (mScroll) {
			super.scrollTo(x, y);
		}
	}

	@Override
	public void setCurrentItem(int item, boolean smoothScroll) {
		super.setCurrentItem(item, smoothScroll);
	}

}
