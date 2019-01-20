package com.pisen.router.ui.base;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public interface INavigationBar {

	View getView();

	CharSequence getTitle();

	void setTitle(int titleId);

	void setTitle(CharSequence title);

	void setBackgroundColor(int color);

	void setBackgroundResource(int resid);

	void setBackground(Drawable background);

	TextView getTitleView();

	Button getLeftButton();

	Button getRightButton();

	void setLeftButton(CharSequence text, int iconResId, OnClickListener l);

	void setRightButton(CharSequence text, int iconResId, OnClickListener l);
}
