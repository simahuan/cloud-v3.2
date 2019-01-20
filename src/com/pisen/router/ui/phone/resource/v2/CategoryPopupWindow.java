package com.pisen.router.ui.phone.resource.v2;

import android.app.Activity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager.LayoutParams;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;
import com.pisen.router.R;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.ui.base.BasePopupWindow;

public class CategoryPopupWindow extends BasePopupWindow implements View.OnClickListener {
	private View categoryContentView;
	private ObjectAnimator enterAnim;
	private ObjectAnimator exitAnim;
	private static final long ANIM_TIME = 400;

	private OnCategoryItemClickCallback callback;
	
	public interface OnCategoryItemClickCallback {
		void onCategoryItemClick(FileType type);
	}

	public CategoryPopupWindow(Activity activity) {
		super(activity);

		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.MATCH_PARENT);
		setContentView(activity, R.layout.resource_home_category);

		categoryContentView = findViewById(R.id.menuCategoryContent);
		findViewById(R.id.dismissView).setOnClickListener(this);
		findViewById(R.id.btnAll).setOnClickListener(this);
		findViewById(R.id.btnImage).setOnClickListener(this);
		findViewById(R.id.btnVideo).setOnClickListener(this);
		findViewById(R.id.btnMusic).setOnClickListener(this);
		findViewById(R.id.btnDocument).setOnClickListener(this);
		findViewById(R.id.btnApk).setOnClickListener(this);
		
		initAnim();
	}

	private void initAnim() {
		int widthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.AT_MOST);
		int heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		categoryContentView.measure(widthSpec, heightSpec);
		enterAnim = ObjectAnimator.ofFloat(categoryContentView, "translationY",  - categoryContentView.getMeasuredHeight(), 0);
		enterAnim.setDuration(ANIM_TIME);
		
		exitAnim = ObjectAnimator.ofFloat(categoryContentView, "translationY", 0f, - categoryContentView.getMeasuredHeight());
		exitAnim.setDuration(ANIM_TIME);
	}
	
	private void exit() {
		super.dismiss();
	}

	public void setOnCategoryItemClickCallback(OnCategoryItemClickCallback callback) {
		this.callback = callback;
	}

	@Override
	public void onClick(View v) {
		if(enterAnim.isRunning() || exitAnim.isRunning()) return;
		
		showExitAnimation(v);
	}

	@Override
	public void showAsDropDown(View anchor) {
		super.showAsDropDown(anchor);
		
		showEnterAnimation();
	}
	
	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		super.showAtLocation(parent, gravity, x, y);
		
		showEnterAnimation();
	}
	
	@Override
	public void dismiss() {
		if(enterAnim.isRunning() || exitAnim.isRunning()) return;
		
		showExitAnimation(null);
	}
	
	private void buttonClicked(View v) {
		switch (v.getId()) {
		case R.id.dismissView:
			break;
		case R.id.btnAll:
			setItemCategoryClick(FileType.All);
			break;
		case R.id.btnImage:
			setItemCategoryClick(FileType.Image);
			break;
		case R.id.btnVideo:
			setItemCategoryClick(FileType.Video);
			break;
		case R.id.btnMusic:
			setItemCategoryClick(FileType.Audio);
			break;
		case R.id.btnDocument:
			setItemCategoryClick(FileType.Document);
			break;
		case R.id.btnApk:
			setItemCategoryClick(FileType.Apk);
			break;
		default:
			break;
		}
	}
	
	private void showExitAnimation(final View v) {
		exitAnim.removeAllListeners();
		exitAnim.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				exit();
				if(v != null && v.getId() != R.id.dismissView) {
					buttonClicked(v);
				}
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
				
			}
		});
		exitAnim.start();
	}

	private void showEnterAnimation() {
		enterAnim.start();
	}

	private void setItemCategoryClick(FileType type) {
		if (callback != null) {
			callback.onCategoryItemClick(type);
		}
	}
}
