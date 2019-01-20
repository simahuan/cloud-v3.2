package com.pisen.router.ui.phone.resource.transfer;

import java.util.ArrayList;
import java.util.List;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
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
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.core.filemanager.transfer.TransferInfo;
import com.pisen.router.core.filemanager.transfer.TransferServiceV2;
import com.pisen.router.ui.base.CloudActivity;
import com.pisen.router.ui.base.INavigationBar;
import com.pisen.router.ui.phone.resource.v2.ChoiceNavigationBar;
import com.pisen.router.ui.phone.resource.v2.ChoiceNavigationBar.OnChoiceItemClickListener;
import com.pisen.router.ui.phone.resource.v2.panel.ISelectionActionBar;

/**
 * 文件传输记录Activity
 * @author ldj
 */
public class TransferRecordActivity extends CloudActivity implements OnClickListener, OnChoiceItemClickListener, AnimationListener, OnPageChangeListener {

	private ChoiceNavigationBar navigationBar;
	private ISelectionActionBar<TransferInfo> selectionActionBar;

	private ViewPager viewPager;
	private PagerSlidingTabStrip tabStrip;
	private TransferRecordFragmentAdapter adapter;

	private View deleteView;

	private Animation deleteInAnim;
	private Animation deleteOutAnim;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transfer_record_activity);

		startTransferService();
		findView();
		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();

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

	public void handleEmptyView(boolean isEmpty) {
		if (isEmpty) {
			navigationBar.getRightButton().setVisibility(View.GONE);
		} else {
			navigationBar.getRightButton().setVisibility(View.VISIBLE);
		}
	}
	
	public void handleEmptyView() {
		List<TransferInfo> data = getCurrentFragment().getData();
		if (data == null || data.isEmpty()) {
			navigationBar.getRightButton().setVisibility(View.GONE);
		} else {
			navigationBar.getRightButton().setVisibility(View.VISIBLE);
		}
	}

	private TransferRecordFragment getCurrentFragment() {
		return (TransferRecordFragment) adapter.getItem(viewPager.getCurrentItem());
	}

	private void startTransferService() {
		Intent intent = new Intent(this, TransferServiceV2.class);
		startService(intent);
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
	private void initView() {
		deleteInAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_in);
		deleteOutAnim = AnimationUtils.loadAnimation(this, R.anim.bottom_out);
		deleteInAnim.setAnimationListener(this);
		deleteOutAnim.setAnimationListener(this);

		adapter = new TransferRecordFragmentAdapter(getSupportFragmentManager());
		selectionActionBar = (ISelectionActionBar<TransferInfo>) getCurrentFragment();

		viewPager.setAdapter(adapter);
		tabStrip.setViewPager(viewPager);

		deleteView.setOnClickListener(this);
		tabStrip.setOnPageChangeListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && navigationBar.isShowChoiceBar()) {
			selectionActionBar.onActionBarItemCheckCancel();
			updateActionBarChanged();
			dismissMultiChoice();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();

		switch (id) {
		case R.id.txtDelete: // 删除已选项
			handleDeleteSelected();
			break;
		default:
			break;
		}
	}

	/**
	 * 删除已选项
	 */
	private void handleDeleteSelected() {
		final TransferRecordFragment fragment = getCurrentFragment();
		if (fragment.getSelectedCount() > 0) {
			ConfirmDialog.show(this, "确定要删除选中项吗?", "删除", "确定", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					fragment.deleteSelectedData();
					dismissMultiChoice();
				}
			}, "取消", null);
		} else {
			UIHelper.showToast(TransferRecordActivity.this, "请选择数据");
		}
	}

	public void showMultichoice() {
		showDelete();
		TransferRecordFragment tmp = (TransferRecordFragment) adapter.getItem(viewPager.getCurrentItem());
		tmp.showMultiChoice();
		navigationBar.showChoiceBar();
	}

	/**
	 * 隐藏多选相关视图
	 */
	public void dismissMultiChoice() {
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
		if (deleteView.getVisibility() == View.VISIBLE) {
			deleteView.setAnimation(deleteOutAnim);
			deleteOutAnim.startNow();
		}
	}

	/**
	 * 传输记录fragment适配器
	 * 
	 * @author ldj
	 * @version 1.0 2015年5月4日 上午11:59:52
	 */
	private class TransferRecordFragmentAdapter extends FragmentPagerAdapter {
		private List<TransferRecordFragment> fragmentList;

		public TransferRecordFragmentAdapter(FragmentManager fm) {
			super(fm);
			fragmentList = new ArrayList<TransferRecordFragment>();
			fragmentList.add(new UploadTransferRecordFragment());
			fragmentList.add(new DownloadTransferRecordFragment());
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return fragmentList.get(position).getPageTitle();
		}

		@Override
		public Fragment getItem(int arg0) {
			return fragmentList.get(arg0);
		}

		@Override
		public int getCount() {
			return fragmentList.size();
		}
	}

	@Override
	public void onAnimationStart(Animation animation) {
		if (animation == deleteInAnim) {
			deleteView.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onAnimationEnd(Animation animation) {
		if (animation == deleteOutAnim) {
			deleteView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onPageSelected(int arg0) {

		dismissMultiChoice();
		selectionActionBar.onActionBarItemCheckCancel();
		selectionActionBar = (ISelectionActionBar<TransferInfo>) adapter.getItem(arg0);
		handleEmptyView();
	}

	@Override
	public void onNavigationBarItemCheckAll(boolean checked) {
		selectionActionBar.onActionBarItemCheckAll(checked);
		updateActionBarChanged();
	}

	@Override
	public void onNavigationBarItemCheckCancel() {
		selectionActionBar.onActionBarItemCheckCancel();
		updateActionBarChanged();
		dismissMultiChoice();
	}
}
