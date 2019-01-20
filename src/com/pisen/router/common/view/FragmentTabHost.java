package com.pisen.router.common.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RadioGroup;

import com.pisen.router.R;
import com.pisen.router.ui.HomeActivity;
import com.pisen.router.ui.phone.flashtransfer.FlashTransferFragment;
import com.pisen.router.ui.phone.resource.v2.RouterFragment;

public class FragmentTabHost implements RadioGroup.OnCheckedChangeListener {

	private Context mContext;
	private FragmentManager mFragmentManager;
	private RadioGroup mTabHost;
	private int mContentId;
	private List<Fragment> mTabs;
	private int mCurrentTab = -1;

	private OnTabChangeListener mOnTabChangeListener;

	public FragmentTabHost(HomeActivity fragmentActivity, RadioGroup mTabHost, int contentId) {
		this.mTabs = new ArrayList<Fragment>();
		this.mContext = fragmentActivity;
		this.mFragmentManager = fragmentActivity.getSupportFragmentManager();
		this.mTabHost = mTabHost;
		this.mContentId = contentId;
		mTabHost.setOnCheckedChangeListener(this);
		fragmentActivity.findViewById(mContentId).setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return true;
			}
		});
	}

	/**
	 * @des   选择默认页
	 * @param checkedId
	 */
	public void setup(int checkedId) {
		mTabHost.check(checkedId);
	}

	public void addTab(Class<? extends Fragment> clss) {
		Fragment newFragment = Fragment.instantiate(mContext, clss.getName());
		mTabs.add(newFragment);
		if (mTabs.size() == 1) {
			switchTab(0, false);
		}
	}

	@Override
	public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
		int tabId = getHostTabId(checkedId);
		switchTab(tabId, true);

		if (mOnTabChangeListener != null) {
			mOnTabChangeListener.onTabChanged(radioGroup, checkedId, tabId);
		}
	}

	private int getHostTabId(int checkedId) {
		for (int i = 0; i < mTabHost.getChildCount(); i++) {
			if (mTabHost.getChildAt(i).getId() == checkedId) {
				return i;
			}
		}
		return 0;
	}

	/**
	 * 切换tab
	 * 
	 * @param ft
	 * 
	 * @param idx
	 */
	private void switchTab(int tabId, boolean smooth) {
		if (tabId != mCurrentTab) {
			FragmentTransaction ft = obtainFragmentTransaction(tabId, smooth);
			Fragment newFragment = mTabs.get(tabId);
			if (!newFragment.isAdded()) {
				Fragment oldFragment = getCurrentTabFragment();
				if (oldFragment != null) {
					ft.hide(oldFragment);
				}
				ft.add(mContentId, newFragment);
			} else {
				Fragment oldFragment = getCurrentTabFragment();
				ft.hide(oldFragment).show(newFragment);
			}

			if(newFragment instanceof FlashTransferFragment) {
				((HomeActivity) mContext).setSlidingMenuScrollable(false);
			}else {
				((HomeActivity) mContext).setSlidingMenuScrollable(true);
			}
			
			if(newFragment instanceof RouterFragment) {
				((RouterFragment) newFragment).setTabHost(mTabHost);
			}
			
			ft.commit();
			mCurrentTab = tabId;
		}
	}

	/**
	 * 获取一个带动画的FragmentTransaction
	 * 
	 * @param index
	 * @param smooth
	 * @return
	 */
	private FragmentTransaction obtainFragmentTransaction(int index, boolean smooth) {
		FragmentTransaction ft = mFragmentManager.beginTransaction();
		if (smooth) {
			if (index > mCurrentTab) {
				ft.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out);
			} else {
				ft.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_right_out);
			}
		}

		return ft;
	}

	public int getCurrentTab() {
		return mCurrentTab;
	}

	public Fragment getCurrentTabFragment() {
		if (mCurrentTab != -1) {
			return mTabs.get(mCurrentTab);
		}
		return null;
	}

	public void setOnTabChangeListener(OnTabChangeListener l) {
		this.mOnTabChangeListener = l;
	}

	/**
	 * 切换tab额外功能功能接口
	 */
	public interface OnTabChangeListener {
		public void onTabChanged(RadioGroup radioGroup, int checkedId, int index);
	}
}
