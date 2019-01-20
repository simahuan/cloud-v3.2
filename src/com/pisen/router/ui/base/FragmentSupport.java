package com.pisen.router.ui.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.pisen.router.ui.phone.resource.v2.NavigationBar;

public abstract class FragmentSupport extends Fragment {

	private INavigationBar navigationBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = onInjectCreateView(inflater, container, savedInstanceState);
		//ButterKnife.inject(this, view);
		return view;
	}

	public View onInjectCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return null;
	}

	@Override
	public void onViewCreated(View view, Bundle bundle) {
		super.onViewCreated(view, bundle);
		navigationBar = newNavigationBar();
	}

	/**
	 * 创建导航
	 * 
	 * @return
	 */
	protected INavigationBar newNavigationBar() {
		return new NavigationBar(this);
	}

	/**
	 * 获取导航
	 * 
	 * @return
	 */
	public INavigationBar getNavigationBar() {
		return navigationBar;
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		//ButterKnife.reset(this);
	}

	public final View findViewById(int id) {
		if (getView() == null) {
			return null;
		}
		return getView().findViewById(id);
	}

	public FragmentActivity getFragmentActivity() {
		Activity activity = getActivity();
		if (activity instanceof FragmentActivity) {
			return (FragmentActivity) activity;
		}
		return null;
	}

	public void startFragment(Class<? extends Fragment> clss) {
		startFragment(clss, null);
	}

	public void startFragment(Class<? extends Fragment> clss, Bundle args) {
		FragmentActivity activity = getFragmentActivity();
		if (activity != null) {
			activity.addFragmentToStack(clss.getName(), args);
		}
	}

	public void startFragment(Fragment fragment) {
		FragmentActivity activity = getFragmentActivity();
		if (activity != null) {
			activity.addFragmentToStack(fragment);
		}
	}

	public void popBackStack() {
		FragmentActivity activity = getFragmentActivity();
		if (activity != null) {
			activity.popBackStack();
		}
	}

	public void popHome() {
		FragmentActivity activity = getFragmentActivity();
		if (activity != null) {
			activity.popHome();
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return false;
	}

}
