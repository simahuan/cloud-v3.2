package com.pisen.router.ui.widget;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.studio.os.LogCat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.pisen.router.R;

/**
 * User: special Date: 13-12-10 Time: 下午10:44 Mail: specialcyci@gmail.com
 */
public class ResideMenu extends FrameLayout {
	public  static final int DIRECTION_LEFT  = 0;			//方向向左　
	public  static final int DIRECTION_RIGHT = 1;			//方向向右
	private static final int PRESSED_MOVE_HORIZONTAL = 2;	//按压水平移动
	private static final int PRESSED_DOWN = 3;				//按下　
	private static final int PRESSED_DONE = 4;
	private static final int PRESSED_MOVE_VERTICAL = 5;		//按压垂直移动

	private ImageView imageViewShadow;
	private ImageView imageViewBackground;
	private LinearLayout layoutLeftMenu;
	private LinearLayout layoutRightMenu;
	private LinearLayout scrollViewMenu;
	/** Current attaching activity.        */
	private Activity activity;
	/** The DecorView of current activity. */
	private ViewGroup viewDecor;			// 视图装饰容器
	private TouchDisableView contentView;	// 主内容面版　
	/** The flag of menu opening status.   */
	private boolean isOpened;				//侧滑栏开合状态
	private float shadowAdjustScaleX;
	private float shadowAdjustScaleY;
	/** Views which need stop to intercept touch events. */
	private List<View> ignoredViews;		//拦截Touch事件
	private DisplayMetrics displayMetrics = new DisplayMetrics();
	private OnMenuListener menuListener;
	private float lastRawX;
	private boolean isInIgnoredView = false;
	private int scaleDirection = DIRECTION_LEFT;
	private int pressedState = PRESSED_DOWN;
	private List<Integer> disabledSwipeDirection = new ArrayList<Integer>();
	// Valid scale factor is between 0.0f and 1.0f.
	private float mScaleValue = 0.8f;
	//是否可滑动
	private boolean scrollable = true;
	private Drawable backgroudDrawable;

	public ResideMenu(Context context) {
		super(context);
		initViews(context);
	}

	private void initViews(Context context) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.residemenu, this);
		imageViewShadow = (ImageView) findViewById(R.id.iv_shadow);
		layoutLeftMenu = (LinearLayout) findViewById(R.id.layout_left_menu);
		layoutRightMenu = (LinearLayout) findViewById(R.id.layout_right_menu);
		imageViewBackground = (ImageView) findViewById(R.id.iv_background);
		
		backgroudDrawable = context.getResources().getDrawable(R.drawable.menu_bg);
	}

	@Override
	protected boolean fitSystemWindows(Rect insets) {
		// Applies the content insets to the view's padding, consuming that
		// content (modifying the insets to be 0),
		// and returning true. This behavior is off by default and can be
		// enabled through setFitsSystemWindows(boolean)
		// in api14+ devices.
		this.setPadding(contentView.getPaddingLeft() + insets.left, contentView.getPaddingTop() + insets.top, contentView.getPaddingRight() + insets.right,
				contentView.getPaddingBottom() + insets.bottom);
		insets.left = insets.top = insets.right = insets.bottom = 0;
		return true;
	}

	/**
	 * Set up the activity;
	 * 
	 * @param activity
	 */
	public void attachToActivity(Activity activity) {
		initValue(activity);
		setShadowAdjustScaleXByOrientation();
		viewDecor.addView(this, 0);
	}

	public void toggle() {
		if(isOpened) {
			closeMenu();
		}else {
			openMenu(DIRECTION_LEFT);
		}
	}
	
	public void setScrollable(boolean scrollable) {
//		LogCat.e("bSroll="+scrollable);
		this.scrollable = scrollable;
	}
	public boolean canScrollable(){
		return scrollable;
	}
	
	private void initValue(Activity activity) {
		this.activity = activity;
		ignoredViews = new ArrayList<View>();
		viewDecor = (ViewGroup) activity.getWindow().getDecorView();
		contentView = new TouchDisableView(this.activity);

		View mContent = viewDecor.getChildAt(0);
		viewDecor.removeViewAt(0);
		contentView.setContent(mContent);
		addView(contentView);

		ViewGroup parent = (ViewGroup) layoutLeftMenu.getParent();
		parent.removeView(layoutLeftMenu);
		parent.removeView(layoutRightMenu);
	}

	private void setShadowAdjustScaleXByOrientation() {
		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			shadowAdjustScaleX = 0.034f;
			shadowAdjustScaleY = 0.12f;
		} else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			shadowAdjustScaleX = 0.08f / 4f;
			shadowAdjustScaleY = 0.08f;
		}
	}

	/**
	 * Set the background image of menu;
	 * 
	 * @param imageResource
	 */
	public void setBackground(int imageResource) {
		imageViewBackground.setImageResource(imageResource);
	}

	/**
	 * The visibility of the shadow under the activity;
	 * 
	 * @param isVisible
	 */
	public void setShadowVisible(boolean isVisible) {
		if (isVisible){
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				imageViewShadow.setBackground(backgroudDrawable);
			} else {
				imageViewShadow.setBackgroundDrawable(backgroudDrawable);
			}
		} else
			imageViewShadow.setBackgroundResource(0);
	}

	/**
	 * Add a single items;
	 * 
	 * @param menuItem
	 * @param direction
	 */
	public void setMenuItemView(View menuItem, int direction) {
		if (direction == DIRECTION_LEFT) {
			layoutLeftMenu.removeAllViews();
			layoutLeftMenu.addView(menuItem);
		} else {
			layoutRightMenu.removeAllViews();
			layoutRightMenu.addView(menuItem);
		}
	}

	/**
	 * Return instances of menu items;
	 * @return
	 */
	public View getMenuItemView(int direction) {
		if (direction == DIRECTION_LEFT)
			return layoutLeftMenu;
		else
			return layoutRightMenu;
	}

	/**
	 * If you need to do something on closing or opening menu, set a listener
	 * here.
	 * 设置侧滑菜单监听,开菜单,关菜单(辅助事件)
	 * @return
	 */
	public void setMenuListener(OnMenuListener menuListener) {
		this.menuListener = menuListener;
	}

	public OnMenuListener getMenuListener() {
		return menuListener;
	}

	/**
	 * Show the menu;
	 */
	public void openMenu(int direction) {
		setScaleDirection(direction);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			setBackground(backgroudDrawable);
		} else {
			setBackgroundDrawable(backgroudDrawable);
		}

		isOpened = true;
		AnimatorSet scaleDown_activity = buildScaleDownAnimation(contentView, mScaleValue, mScaleValue);
		AnimatorSet scaleDown_shadow = buildScaleDownAnimation(imageViewShadow, mScaleValue + shadowAdjustScaleX, mScaleValue + shadowAdjustScaleY);
		AnimatorSet alpha_menu = buildMenuAnimation(scrollViewMenu, 1.0f);
		scaleDown_shadow.addListener(animationListener);
		scaleDown_activity.playTogether(scaleDown_shadow);
		scaleDown_activity.playTogether(alpha_menu);
		scaleDown_activity.start();
	}

	/**
	 * Close the menu;
	 */
	public void closeMenu() {

		isOpened = false;
		AnimatorSet scaleUp_activity = buildScaleUpAnimation(contentView, 1.0f, 1.0f);
		AnimatorSet scaleUp_shadow = buildScaleUpAnimation(imageViewShadow, 1.0f, 1.0f);
		AnimatorSet alpha_menu = buildMenuAnimation(scrollViewMenu, 0.0f);
		scaleUp_activity.addListener(animationListener);
		scaleUp_activity.playTogether(scaleUp_shadow);
		scaleUp_activity.playTogether(alpha_menu);
		scaleUp_activity.start();
	}

	@Deprecated
	public void setDirectionDisable(int direction) {
		disabledSwipeDirection.add(direction);
	}

	public void setSwipeDirectionDisable(int direction) {
		disabledSwipeDirection.add(direction);
	}

	private boolean isInDisableDirection(int direction) {
		return disabledSwipeDirection.contains(direction);
	}

	private void setScaleDirection(int direction) {

		int screenWidth = getScreenWidth();
		float pivotX;
		float pivotY = getScreenHeight() * 0.5f;

		if (direction == DIRECTION_LEFT) {
			scrollViewMenu = layoutLeftMenu;
			pivotX = screenWidth * 4.0f;
		} else {
			scrollViewMenu = layoutRightMenu;
			pivotX = screenWidth * -3.0f;
		}

		ViewHelper.setPivotX(contentView, pivotX);
		ViewHelper.setPivotY(contentView, pivotY);
		ViewHelper.setPivotX(imageViewShadow, pivotX);
		ViewHelper.setPivotY(imageViewShadow, pivotY);
		scaleDirection = direction;
	}

	/**
	 * return the flag of menu status;
	 * 
	 * @return
	 */
	public boolean isOpened() {
		return isOpened;
	}

	private OnClickListener viewActivityOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if (isOpened())
				closeMenu();
		}
	};

	private Animator.AnimatorListener animationListener = new Animator.AnimatorListener() {
		@Override
		public void onAnimationStart(Animator animation) {
			if (isOpened()) {
				showScrollViewMenu(scrollViewMenu);
				if (menuListener != null)
					menuListener.openMenu();
			}
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			// reset the view;
			if (isOpened()) {
				contentView.setTouchDisable(true);
				contentView.setOnClickListener(viewActivityOnClickListener);
			} else {
				contentView.setTouchDisable(false);
				contentView.setOnClickListener(null);
				hideScrollViewMenu(layoutLeftMenu);
				hideScrollViewMenu(layoutRightMenu);
				setBackgroundResource(0);
				if (menuListener != null)
					menuListener.closeMenu();
			}
		}

		@Override
		public void onAnimationCancel(Animator animation) {

		}

		@Override
		public void onAnimationRepeat(Animator animation) {

		}
	};

	/**
	 * A helper method to build scale down animation;
	 * 
	 * @param target
	 * @param targetScaleX
	 * @param targetScaleY
	 * @return
	 */
	private AnimatorSet buildScaleDownAnimation(View target, float targetScaleX, float targetScaleY) {

		AnimatorSet scaleDown = new AnimatorSet();
		scaleDown.playTogether(ObjectAnimator.ofFloat(target, "scaleX", targetScaleX), ObjectAnimator.ofFloat(target, "scaleY", targetScaleY));

		scaleDown.setInterpolator(AnimationUtils.loadInterpolator(activity, android.R.anim.decelerate_interpolator));
		scaleDown.setDuration(250);
		return scaleDown;
	}

	/**
	 * A helper method to build scale up animation;
	 * 
	 * @param target
	 * @param targetScaleX
	 * @param targetScaleY
	 * @return
	 */
	private AnimatorSet buildScaleUpAnimation(View target, float targetScaleX, float targetScaleY) {

		AnimatorSet scaleUp = new AnimatorSet();
		scaleUp.playTogether(ObjectAnimator.ofFloat(target, "scaleX", targetScaleX), ObjectAnimator.ofFloat(target, "scaleY", targetScaleY));

		scaleUp.setDuration(250);
		return scaleUp;
	}

	private AnimatorSet buildMenuAnimation(View target, float alpha) {

		AnimatorSet alphaAnimation = new AnimatorSet();
		alphaAnimation.playTogether(ObjectAnimator.ofFloat(target, "alpha", alpha));

		alphaAnimation.setDuration(250);
		return alphaAnimation;
	}

	/**
	 * If there were some view you don't want reside menu to intercept their
	 * touch event, you could add it to ignored views.
	 * 
	 * @param v
	 */
	public void addIgnoredView(View v) {
		ignoredViews.add(v);
	}

	/**
	 * Remove a view from ignored views;
	 * 
	 * @param v
	 */
	public void removeIgnoredView(View v) {
		ignoredViews.remove(v);
	}

	/**
	 * Clear the ignored view list;
	 */
	public void clearIgnoredViewList() {
		ignoredViews.clear();
	}

	/**
	 * If the motion event was relative to the view which in ignored view
	 * list,return true;
	 * 
	 * @param ev
	 * @return
	 */
	private boolean isInIgnoredView(MotionEvent ev) {
		Rect rect = new Rect();
		for (View v : ignoredViews) {
			v.getGlobalVisibleRect(rect);
			if (rect.contains((int) ev.getX(), (int) ev.getY()))
				return true;
		}
		return false;
	}

	private void setScaleDirectionByRawX(float currentRawX) {
		if (currentRawX < lastRawX)
			setScaleDirection(DIRECTION_RIGHT);
		else
			setScaleDirection(DIRECTION_LEFT);
	}

	private float getTargetScale(float currentRawX) {
		float scaleFloatX = ((currentRawX - lastRawX) / getScreenWidth()) * (1.0f / 4.0f);
		scaleFloatX = scaleDirection == DIRECTION_RIGHT ? -scaleFloatX : scaleFloatX;

		float targetScale = ViewHelper.getScaleX(contentView) - scaleFloatX;
		targetScale = targetScale > 1.0f ? 1.0f : targetScale;
		targetScale = targetScale < mScaleValue ? mScaleValue : targetScale;
		return targetScale;
	}

	private float lastActionDownX, lastActionDownY;

	/**
	 * 派发　TouchEvent
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if(!scrollable && !isOpened) return super.dispatchTouchEvent(ev);
		
		float currentActivityScaleX = ViewHelper.getScaleX(contentView);
		if (currentActivityScaleX == 1.0f)
			setScaleDirectionByRawX(ev.getRawX());

		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			lastActionDownX = ev.getX();
			lastActionDownY = ev.getY();
			isInIgnoredView = isInIgnoredView(ev) && !isOpened();
			pressedState = PRESSED_DOWN;
			break;

		case MotionEvent.ACTION_MOVE:
			if (isInIgnoredView || isInDisableDirection(scaleDirection))
				break;

			if (pressedState != PRESSED_DOWN && pressedState != PRESSED_MOVE_HORIZONTAL)
				break;

			int xOffset = (int) (ev.getX() - lastActionDownX);
			int yOffset = (int) (ev.getY() - lastActionDownY);

			if (pressedState == PRESSED_DOWN) {
				if (yOffset > 25 || yOffset < -25) {
					pressedState = PRESSED_MOVE_VERTICAL;
					break;
				}
				if (xOffset < -50 || xOffset > 50) {
					pressedState = PRESSED_MOVE_HORIZONTAL;
					ev.setAction(MotionEvent.ACTION_CANCEL);
				}
			} else if (pressedState == PRESSED_MOVE_HORIZONTAL) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					setBackground(backgroudDrawable);
				} else {
					setBackgroundDrawable(backgroudDrawable);
				}
				if (currentActivityScaleX < 0.98)
					showScrollViewMenu(scrollViewMenu);

				float targetScale = getTargetScale(ev.getRawX());
				ViewHelper.setScaleX(contentView, targetScale);
				ViewHelper.setScaleY(contentView, targetScale);
				ViewHelper.setScaleX(imageViewShadow, targetScale + shadowAdjustScaleX);
				ViewHelper.setScaleY(imageViewShadow, targetScale + shadowAdjustScaleY);
				ViewHelper.setAlpha(scrollViewMenu, (1 - targetScale) * 2.0f);

				lastRawX = ev.getRawX();
				return true;
			}

			break;

		case MotionEvent.ACTION_UP:

			if (isInIgnoredView)
				break;
			if (pressedState != PRESSED_MOVE_HORIZONTAL)
				break;

			pressedState = PRESSED_DONE;
			if (isOpened()) {
				if (currentActivityScaleX > 0.86f)
					closeMenu();
				else
					openMenu(scaleDirection);
			} else {
				if (currentActivityScaleX < 0.94f) {
					openMenu(scaleDirection);
				} else {
					closeMenu();
				}
			}

			break;

		}
		lastRawX = ev.getRawX();
		return super.dispatchTouchEvent(ev);
	}

	public int getScreenHeight() {
		activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics.heightPixels;
	}

	public int getScreenWidth() {
		activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		return displayMetrics.widthPixels;
	}

	public void setScaleValue(float scaleValue) {
		this.mScaleValue = scaleValue;
	}

	public interface OnMenuListener {

		/**
		 * This method will be called at the finished time of opening menu
		 * animations.
		 */
		public void openMenu();

		/**
		 * This method will be called at the finished time of closing menu
		 * animations.
		 */
		public void closeMenu();
	}

	private void showScrollViewMenu(View scrollViewMenu) {
		if (scrollViewMenu != null && scrollViewMenu.getParent() == null) {
			((ViewGroup)findViewById(R.id.layout_left_menu_layout)).addView(scrollViewMenu);
		}
	}

	private void hideScrollViewMenu(View scrollViewMenu) {
		if (scrollViewMenu != null && scrollViewMenu.getParent() != null) {
			removeView(scrollViewMenu);
		}
	}
}

