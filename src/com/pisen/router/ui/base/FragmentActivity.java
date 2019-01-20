package com.pisen.router.ui.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class FragmentActivity extends CloudActivity {

	static final int CONTAINER_ID = Integer.MAX_VALUE;
	static final String FragmentPackageName = "FragmentPackageName";
	private Fragment rootFragment;
	private Fragment lastFragment;

	public static void startFragment(Context context, Class<?> clss) {
		startFragment(context, clss, new Bundle());
	}

	public static void startFragment(Context context, Class<?> clss, Bundle args) {
		Intent intent = new Intent(context, FragmentActivity.class);
		intent.putExtra(FragmentPackageName, clss.getName());
		intent.putExtras(args);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState == null) {
			Intent intent = getIntent();
			if (intent != null) {
				String packName = intent.getStringExtra(FragmentPackageName);
				if (packName != null) {
					setContentView(createRootView());
					addFragmentToStack(packName, intent.getExtras());
				}
			}
		}
	}

	private FrameLayout createRootView() {
		FrameLayout rootView = new FrameLayout(this);
		rootView.setId(CONTAINER_ID);
		return rootView;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (lastFragment instanceof FragmentSupport) {
			FragmentSupport baseFragment = (FragmentSupport) lastFragment;
			if (baseFragment.onKeyDown(keyCode, event)) {
				return true;
			}
		}

		if (keyCode == KeyEvent.KEYCODE_BACK) {
			popBackStack();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (lastFragment instanceof FragmentSupport) {
			FragmentSupport baseFragment = (FragmentSupport) lastFragment;
			return baseFragment.onTouchEvent(event) || super.onTouchEvent(event);
		}
		return super.onTouchEvent(event);
	}

	public void addFragmentToStack(String packName, Bundle args) {
		FragmentSupport fragment = (FragmentSupport) Fragment.instantiate(this, packName);
		fragment.setArguments(args);
		addFragmentToStack(fragment);
	}

	public void addFragmentToStack(Fragment fragment) {
		if (rootFragment == null) {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(CONTAINER_ID, fragment);
			ft.commit();
			rootFragment = fragment;
		} else {
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(CONTAINER_ID, fragment);
			ft.addToBackStack(null);
			ft.commit();
		}
		lastFragment = fragment;
	}

	public void popBackStack() {
		FragmentManager fm = getSupportFragmentManager();
		int backCount = fm.getBackStackEntryCount();
		if (backCount > 0) {
			fm.popBackStack();
			if (backCount <= 1) {
				lastFragment = rootFragment;
			} else {
				lastFragment = fm.getFragments().get(backCount - 2);
			}
		} else {
			finish();
		}
	}

	public void popHome() {
		FragmentManager fm = getSupportFragmentManager();
		if (fm.getBackStackEntryCount() > 0) {
			fm.popBackStack(fm.getBackStackEntryAt(0).getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
			// fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
			// newFragment = fm.findFragmentById(0);
		}
	}
}
