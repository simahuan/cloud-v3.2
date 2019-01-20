package cn.sharesdk.onekeyshare;

import com.mob.tools.utils.R;

import cn.sharesdk.framework.TitleLayout;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @author  mahuan
 * @version 1.0 2015年5月20日 下午6:43:39
 */
public class CTitleLayout extends LinearLayout {

	private ImageView btnBack;
	private TextView tvTitle;
	private TextView btnRight;
	private TextView btnLeft;
	
	public CTitleLayout(Context paramContext) {
		super(paramContext);
		init(paramContext);
	}

	public CTitleLayout(Context paramContext, AttributeSet paramAttributeSet) {
		super(paramContext, paramAttributeSet);
		init(paramContext);
	}

	private void init(Context paramContext) {
		this.btnBack = new ImageView(paramContext);
		this.btnBack.setBackgroundColor(Color.parseColor("#0073FF"));
		int i = R.getBitmapRes(paramContext, "menu_ic_back");
		if (i > 0)
			this.btnBack.setImageResource(i);
		this.btnBack.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		int j = R.dipToPx(paramContext, 35);
		this.btnBack.setLayoutParams(new LinearLayout.LayoutParams(j, -1));
		addView(this.btnBack);
		
		this.btnLeft = new TextView(paramContext);
		btnLeft.setText("返回");
		btnLeft.setTextSize(1, 16.0F);
		btnLeft.setTextColor(-1);
		btnLeft.setGravity(Gravity.CENTER_VERTICAL|Gravity.START);
		this.btnLeft.setBackgroundColor(Color.parseColor("#0073FF"));
		btnLeft.setLayoutParams(new LinearLayout.LayoutParams(j, -1));
		addView(btnLeft);
		
		this.tvTitle = new TextView(paramContext);
		this.tvTitle.setBackgroundColor(Color.parseColor("#0073FF"));
		int l = R.dipToPx(paramContext, 23);
		this.tvTitle.setPadding(l, 0, l, 0);
		this.tvTitle.setSingleLine();
		this.tvTitle.setTextColor(-1);
		this.tvTitle.setTextSize(1, 20.0F);
		this.tvTitle.setTypeface(Typeface.DEFAULT);
		this.tvTitle.setGravity(16);
		LinearLayout.LayoutParams localLayoutParams = new LinearLayout.LayoutParams(-2, -1);
		localLayoutParams.weight = 1.0F;
		this.tvTitle.setLayoutParams(localLayoutParams);
		addView(this.tvTitle);
		
		this.btnRight = new CTextView(this, paramContext, null);
		this.btnRight.setVisibility(4);
		this.btnRight.setBackgroundColor(Color.parseColor("#0073FF"));
		int i1 = R.dipToPx(paramContext, 50);
		this.btnRight.setMinWidth(i1);
		this.btnRight.setTextColor(-1);
		this.btnRight.setTextSize(1, 12.0F);
		this.btnRight.setGravity(Gravity.CENTER);
		
		int dp_16 = R.dipToPx(paramContext, 16);
		this.btnRight.setPadding(dp_16, dp_16, dp_16, dp_16);
		this.btnRight.setLayoutParams(new LinearLayout.LayoutParams(-2, -1));
		addView(this.btnRight);
	}

	public ImageView getBtnBack() {
		return this.btnBack;
	}

	public TextView getTvTitle() {
		return this.tvTitle;
	}

	public TextView getBtnRight() {
		return this.btnRight;
	}

	public TextView getBtnLeft(){
		return this.btnLeft;
	}
}
