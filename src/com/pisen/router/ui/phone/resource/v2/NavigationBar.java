package com.pisen.router.ui.phone.resource.v2;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.ui.base.INavigationBar;

public class NavigationBar implements INavigationBar {

	protected View actionBar;
	private Button btnLeft;
	private Button btnRight;
	private TextView txtTitle;
	
	public NavigationBar(Activity activity) {
		this.actionBar = activity.findViewById(R.id.actionbar);
		initViews();
	}

	public NavigationBar(Fragment fragment) {
		this.actionBar = fragment.getView().findViewById(R.id.actionbar);
		initViews();
	}

	private void initViews() {
		if (actionBar != null) {
			btnLeft = (Button) actionBar.findViewById(R.id.btnLeft);
			btnRight = (Button) actionBar.findViewById(R.id.btnRight);
			txtTitle = (TextView) actionBar.findViewById(R.id.txtTitle);
		}
	}

	public View getActionBar() {
		return actionBar;
	}

	@Override
	public View getView() {
		return actionBar;
	}

	@Override
	public CharSequence getTitle() {
		return null;
	}

	@Override
	public void setTitle(int resid) {
		setTitle(actionBar.getResources().getText(resid));
	}

	@Override
	public void setTitle(CharSequence text) {
		txtTitle.setText(text);
	}

	@Override
	public void setBackgroundColor(int color) {
		actionBar.setBackgroundColor(color);
	}

	@Override
	public void setBackgroundResource(int resid) {
		actionBar.setBackgroundResource(resid);
	}

	@Override
	public void setBackground(Drawable background) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			actionBar.setBackground(background);
		} else {
			actionBar.setBackgroundDrawable(background);
		}
	}

	@Override
	public TextView getTitleView() {
		return txtTitle;
	}

	@Override
	public Button getLeftButton() {
		return btnLeft;
	}

	@Override
	public Button getRightButton() {
		return btnRight;
	}

	@Override
	public void setLeftButton(CharSequence text, int iconResId, OnClickListener l) {
		btnLeft.setVisibility(View.VISIBLE);
		btnLeft.setText(text);
		if (iconResId > 0) {
			btnLeft.setCompoundDrawablesWithIntrinsicBounds(iconResId, 0, 0, 0);
		}
		if (l != null) {
			btnLeft.setOnClickListener(l);
		}
	}

	@Override
	public void setRightButton(CharSequence text, int iconResId, OnClickListener l) {
		btnRight.setVisibility(View.VISIBLE);
		btnRight.setText(text);
		if (iconResId > 0) {
			btnRight.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconResId, 0);
		}
		if (l != null) {
			btnRight.setOnClickListener(l);
		}
	}
	
}
