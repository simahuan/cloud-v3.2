package com.pisen.router.ui.base;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.pisen.router.ui.base.impl.DefaultNavigationBar;

public abstract class NavigationBarActivity extends CloudActivity {

	private LinearLayout rootView;
	private INavigationBar navigationBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		rootView = createRootView();
		super.setContentView(rootView);

		LayoutInflater inflater = getLayoutInflater();
		navigationBar = createNavigationBar(inflater);
		if (navigationBar != null) {
			rootView.addView(navigationBar.getView(), new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		}
	}

	private LinearLayout createRootView() {
		LinearLayout rootView = new LinearLayout(this);
		rootView.setOrientation(LinearLayout.VERTICAL);
		rootView.setBackgroundColor(Color.WHITE);
		return rootView;
	}

	/**
	 * @describtion  构建导航操作
	 * @param inflater
	 * @return
	 */
	protected INavigationBar createNavigationBar(LayoutInflater inflater) {
		DefaultNavigationBar navBar = new DefaultNavigationBar(this);
		navBar.getLeftButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
				View view = getCurrentFocus();
				if (imm.isActive() &&  view!= null){
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}
				finish();
			}
		});

		return navBar;
	}

	
	public INavigationBar getNavigationBar() {
		return navigationBar;
	}

	@Override
	public void setTitle(int titleId) {
		setTitle(getText(titleId));
	}

	@Override
	public void setTitle(CharSequence title) {
		navigationBar.setTitle(title);
	}

	@Override
	public void setContentView(int layoutResID) {
		setContentView(View.inflate(this, layoutResID, null));
	}

	@Override
	public void setContentView(View view) {
		setContentView(view, rootView.getLayoutParams());
	}

	@Override
	public void setContentView(View view, LayoutParams params) {
		if (view != null) {
			rootView.addView(view, rootView.getLayoutParams());
		}
	}
}
