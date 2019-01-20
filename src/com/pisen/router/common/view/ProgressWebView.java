package com.pisen.router.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

/**
 * @author  mahuan
 * @version 1.0 2015年7月7日 上午9:45:31
 */
public class ProgressWebView extends WebView {
	private ProgressBar progressBar;
	/**
	 * @param context
	 * @param attrs
	 */
	public ProgressWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		progressBar = new ProgressBar(context, attrs, android.R.attr.progressBarStyleHorizontal);
		progressBar.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 3, 0, 0));
		addView(progressBar);
		setWebChromeClient(new WebChromeClient());
	}

	public class WebChromeClient extends android.webkit.WebChromeClient{
		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			if (newProgress == 100){
				progressBar.setVisibility(View.GONE);
			}else {
				if (progressBar.getVisibility() == GONE){
					progressBar.setVisibility(VISIBLE);
				}
				progressBar.setProgress(newProgress);
			}
			super.onProgressChanged(view, newProgress);
		}
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		LayoutParams lp  = (LayoutParams) progressBar.getLayoutParams();
		lp.x = l;
		lp.y = t;
		progressBar.setLayoutParams(lp);
		super.onScrollChanged(l, t, oldl, oldt);
	}
}
