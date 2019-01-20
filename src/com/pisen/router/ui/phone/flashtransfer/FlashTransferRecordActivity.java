package com.pisen.router.ui.phone.flashtransfer;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.studio.os.PreferencesUtils;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

import com.astuetz.PagerSlidingTabStrip;
import com.pisen.router.R;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.ui.base.CloudActivity;
import com.pisen.router.ui.base.INavigationBar;
import com.pisen.router.ui.phone.resource.v2.ChoiceNavigationBar;
import com.pisen.router.ui.phone.resource.v2.ChoiceNavigationBar.OnChoiceItemClickListener;
import com.pisen.router.ui.phone.resource.v2.panel.ISelectionActionBar;

/**
 * 闪电互传记录
 * @author ldj
 * @version 1.0 2015年5月27日 上午9:08:43
 */
public class FlashTransferRecordActivity extends CloudActivity  implements OnChoiceItemClickListener, OnClickListener, AnimationListener, OnPageChangeListener{
	
	private ChoiceNavigationBar navigationBar;
	private ISelectionActionBar<TransferInfo> selectionActionBar;
	
	private ViewPager viewPager;
	private PagerSlidingTabStrip tabStrip;
	private View deleteView;
	
	private TransferRecordFragmentAdapter adapter;
	private Animation deleteInAnim;
	private Animation deleteOutAnim;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.flash_transfer_record_activity);
		
		findView();
		initView();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		PreferencesUtils.setLong(KeyUtils.TIME_LAST_READ, System.currentTimeMillis());
		super.onPause();
	}
	
	@Override
	protected INavigationBar newNavigationBar() {
		navigationBar = new ChoiceNavigationBar(this);
		navigationBar.setTitle("传输列表");
		navigationBar.setBackgroundColor(Color.parseColor("#0073FF"));
		navigationBar.setLeftButton("返回", R.drawable.menu_ic_back, new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		navigationBar.setRightButton("选择", 0, new OnClickListener() {
			@Override
			public void onClick(View v) {
				showMultichoice();
			}
		});
		navigationBar.setOnItemClickListener(this);
		return navigationBar;
	}
	
	public void updateActionBarChanged() {
		int itemCount = selectionActionBar.getItemAll().size();
		int itemSelectedCount = selectionActionBar.getCheckedItemAll().size();
		boolean checkAll = itemCount > 0 && itemCount == itemSelectedCount;

		navigationBar.setCheckedTextCount(itemSelectedCount);
		navigationBar.setCheckedChanged(checkAll);
	}
	
	public void handleEmptyView() {
		handleEmptyView(getCurrentFragment());
	}
	
	public void handleEmptyView(AbstractFlashTransferRecordFragment fragment) {
		if(fragment != null && fragment.getAdapter() != null) {
			List data = fragment.getAdapter().data;
			if(data == null || data.isEmpty()) {
				navigationBar.getRightButton().setVisibility(View.GONE);
			}else {
				navigationBar.getRightButton().setVisibility(View.VISIBLE);
			}
		}
	}

	/**
	 * 初始化控件
	 */
	private void findView() {
		viewPager = (ViewPager) findViewById(R.id.pager);
		tabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
		deleteView = findViewById(R.id.txtDelete);
	}
	
	/**
	 * 控件界面初始
	 */
	@SuppressWarnings("unchecked")
	private void initView() {
		deleteInAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_in);
		deleteOutAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_out);
		deleteInAnim.setAnimationListener(this);
		deleteOutAnim.setAnimationListener(this);
		
		adapter = new TransferRecordFragmentAdapter(getSupportFragmentManager());
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(1);
		tabStrip.setViewPager(viewPager);
		
		deleteView.setOnClickListener(this);
		tabStrip.setOnPageChangeListener(this);
		
		selectionActionBar = (ISelectionActionBar<TransferInfo>)getCurrentFragment();
	}

	
	private AbstractFlashTransferRecordFragment getCurrentFragment() {
		return (AbstractFlashTransferRecordFragment) adapter.getItem(viewPager.getCurrentItem());
	}
	
	/**
	 * 控制多选按钮显示及隐藏
	 * @param show
	 */
	public void showSelectAll(boolean show) {
//		if(show) {
//			txtMultiChoice.setVisibility(View.VISIBLE);
//		}else {
//			txtMultiChoice.setVisibility(View.GONE);
//		}
		navigationBar.hideChoiceBar();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && navigationBar.isShowChoiceBar()) {
			cancelSelect();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		int id = v.getId();
		
		switch (id) {
		case R.id.txtDelete:	//删除已选项
			final AbstractFlashTransferRecordFragment fragment = (AbstractFlashTransferRecordFragment) getCurrentFragment();
			if(fragment.getSelectedCount()>0) {
					final ConfirmDialog cd = new ConfirmDialog(this);
//					cd.setTitle("确 认");
					cd.setMessage("是否确定删除已选数据？");
					cd.setNegativeButton("取消", null);
					cd.setPositiveButton("确定", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							fragment.deleteSelectedData();
							dismissMultiChoice();
//							selectionActionBar.onActionBarItemCheckCancel();
						}
					});
					cd.show();
			}else {
				UIHelper.showToast(FlashTransferRecordActivity.this, "请选择数据");
			}
			break;
		default:
			break;
		}
	}

	public void showMultichoice() {
		showDelete();
		AbstractFlashTransferRecordFragment tmp = (AbstractFlashTransferRecordFragment) getCurrentFragment();
		tmp.showMultiChoice();
		
		navigationBar.showChoiceBar();
	}

	/**
	 * 隐藏多选相关视图
	 */
	private void dismissMultiChoice() {
		navigationBar.hideChoiceBar();
		dismissDelete();
	}

	/**
	 * 显示删除按钮视图
	 */
	private void showDelete() {
		deleteView.setAnimation(deleteInAnim);
		deleteInAnim.startNow();
	}
	
	/**
	 * 隐藏删除按钮视图
	 */
	private void dismissDelete() {
		if(deleteView.getVisibility() == View.VISIBLE) {
			deleteView.setAnimation(deleteOutAnim);
			deleteOutAnim.startNow();
		}
	}
	
	/**
	 * 传输记录fragment适配器
	 * @author ldj
	 * @version 1.0 2015年5月4日 上午11:59:52
	 */
	private class TransferRecordFragmentAdapter extends FragmentPagerAdapter {
		private List<AbstractFlashTransferRecordFragment> fragmentList;

		public TransferRecordFragmentAdapter(FragmentManager fm) {
			super(fm);
			
			fragmentList = new ArrayList<AbstractFlashTransferRecordFragment>();
			fragmentList.add(new FlashTransferRecordFragment());
			fragmentList.add(new InboxFragment());
		}
		
		@Override
		public CharSequence getPageTitle(int position) {
			return fragmentList.get(position).getPageTitle();
		}

		@Override
		public Fragment getItem(int arg0) {
			//XXX 可优化实现
			return fragmentList.get(arg0);
		}

		@Override
		public int getCount() {
			return fragmentList.size();
		}
	}

	@Override
	public void onAnimationStart(Animation animation) {
		if(animation == deleteInAnim) {
			deleteView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if(animation == deleteOutAnim) {
			deleteView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {}

	@Override
	public void onPageScrollStateChanged(int arg0) {}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@SuppressWarnings("unchecked")
	@Override
	public void onPageSelected(int arg0) {
		dismissMultiChoice();
		int limit = viewPager.getOffscreenPageLimit();
		if(limit < adapter.getCount()) {
			viewPager.setOffscreenPageLimit(++limit);
		}
		
		selectionActionBar.onActionBarItemCheckCancel();
		selectionActionBar = (ISelectionActionBar<TransferInfo>)adapter.getItem(arg0);
		handleEmptyView((AbstractFlashTransferRecordFragment) adapter.getItem(arg0));
	}

	@Override
	public void onNavigationBarItemCheckAll(boolean checked) {
		selectionActionBar.onActionBarItemCheckAll(checked);
		updateActionBarChanged();
	}

	@Override
	public void onNavigationBarItemCheckCancel() {
		cancelSelect();
	}

	private void cancelSelect() {
		selectionActionBar.onActionBarItemCheckCancel();		
//		updateActionBarChanged();
		dismissMultiChoice();
	}

}
