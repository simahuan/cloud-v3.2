package com.pisen.router.ui.phone.flashtransfer;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.astuetz.PagerSlidingTabStrip;
import com.pisen.router.R;
import com.pisen.router.ui.base.NavigationFragment;

public class FindHelpFragment extends NavigationFragment implements OnPageChangeListener{
	private ViewPager viewPager;
	private PagerSlidingTabStrip tabStrip;
	private TransferHelpFragmentPager adapter;
	
	@Override
	protected View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		setTitle("如何连接苹果设备");
		View view  = inflater.inflate(R.layout.cloud_flashtransfer_helps, container, false);
		initNavigationBar(view);
		return view;
	}
	
	private void initNavigationBar(View view) {
		viewPager = (ViewPager) view.findViewById(R.id.pager);
		tabStrip = (PagerSlidingTabStrip) view.findViewById(R.id.tabs);
		adapter = new TransferHelpFragmentPager(getActivity().getSupportFragmentManager());
		viewPager.setAdapter(adapter);
		tabStrip.setViewPager(viewPager);
		tabStrip.setOnPageChangeListener(this);
	}
	

	private class TransferHelpFragmentPager extends FragmentPagerAdapter {
		private List<AHelpFragmentSupport> fragmentList;
		public TransferHelpFragmentPager(FragmentManager fm) {
			super(fm);
			fragmentList = new ArrayList<AHelpFragmentSupport>();
			fragmentList.add(new SameConnectHelpFragment());
			fragmentList.add(new DiffConnectHelpFragment());
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return fragmentList.get(position).getPageTitle();
		}

		@Override
		public int getCount() {
			return fragmentList.size();
		}

		@Override
		public Fragment getItem(int paramInt) {
			return fragmentList.get(paramInt);
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {}
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}
	@Override
	public void onPageSelected(int arg0) {}
}
