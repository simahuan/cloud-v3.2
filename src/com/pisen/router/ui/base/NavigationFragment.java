package com.pisen.router.ui.base;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.pisen.router.ui.base.impl.DefaultNavigationBar;

public abstract class NavigationFragment extends FragmentSupport {

	private INavigationBar navigationBar;
	private View contentView;
	private LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LinearLayout rootView = createRootView();
		navigationBar = createNavigationBar(inflater, rootView, savedInstanceState);
		if (navigationBar != null) {
			rootView.addView(navigationBar.getView(), layoutParams);
		}

		contentView = createContentView(inflater, rootView, savedInstanceState);
		if (contentView != null) {
			rootView.addView(contentView, layoutParams);
		}

		return rootView;
	}

	private LinearLayout createRootView() {
		LinearLayout rootView = new LinearLayout(getActivity());
		rootView.setOrientation(LinearLayout.VERTICAL);
		rootView.setBackgroundColor(Color.WHITE);
		return rootView;
	}

	protected INavigationBar createNavigationBar(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		DefaultNavigationBar navBar = new DefaultNavigationBar(getActivity());
		navBar.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				popBackStack();
			}
		});

		return navBar;
	}

	protected abstract View createContentView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

	public INavigationBar getNavigationBar() {
		return navigationBar;
	}

	public void setTitle(int titleId) {
		setTitle(getText(titleId));
	}

	public void setTitle(CharSequence title) {
		navigationBar.setTitle(title);
	}

}
