package com.pisen.router.ui.phone.resource.v2;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.ui.base.FragmentSupport;

public class ChoiceNavigationBar extends NavigationBar implements View.OnClickListener {

	public interface OnChoiceItemClickListener {

		void onNavigationBarItemCheckAll(boolean checked);

		void onNavigationBarItemCheckCancel();

	}

	private View rootView;
	private View choiceBar;
	private Button choiceCancel;
	private Button choiceAll;
	private TextView choiceMessage;
	private boolean checkedAll = true; // 默认为全选按钮

	private OnChoiceItemClickListener itemSelected;

	public ChoiceNavigationBar(Activity activity) {
		super(activity);
		this.rootView = activity.findViewById(R.id.choiceActionbar);
		initChoiceBarViews();
	}

	public ChoiceNavigationBar(FragmentSupport fragment) {
		super(fragment);
		this.rootView = fragment.findViewById(R.id.choiceActionbar);
		initChoiceBarViews();
	}

	private void initChoiceBarViews() {
		// hideChoiceBar();
		choiceBar = rootView.findViewById(R.id.choiceBar);
		choiceCancel = (Button) rootView.findViewById(R.id.choiceCancel);
		choiceAll = (Button) rootView.findViewById(R.id.choiceAll);
		choiceMessage = (TextView) rootView.findViewById(R.id.choiceMessage);

		choiceCancel.setOnClickListener(this);
		choiceAll.setOnClickListener(this);

		hideChoiceBar();
	}

	@Override
	public View getView() {
		return rootView;
	}

	public void setOnItemClickListener(OnChoiceItemClickListener listener) {
		this.itemSelected = listener;
	}

	public void setCheckedTextCount(int checkedCount) {
		setCheckedText(String.format("已选中%s个", checkedCount));
	}

	/**
	 * 选中状态
	 * 
	 * @param checkAll
	 *            true子项被全部选中按钮为"取消全选" | false子项未选中或选中部分按钮为"全选"
	 */
	public void setCheckedChanged(boolean checkAll) {
		choiceAll.setText(checkAll ? "取消全选" : "全选");
		this.checkedAll = !checkAll;
	}

	/**
	 * 设置选中文字
	 * 
	 * @param text
	 */
	public void setCheckedText(String text) {
		choiceMessage.setText(text);
	}

	public boolean isShowChoiceBar() {
		return choiceBar.getVisibility() == View.VISIBLE;
	}

	public void showChoiceBar() {
		choiceBar.setVisibility(View.VISIBLE);
		actionBar.setVisibility(View.GONE);
	}

	public void hideChoiceBar() {
		showActionBar();
		setCheckedTextCount(0);
		setCheckedChanged(false);
	}

	@Deprecated
	public void showActionBar() {
		actionBar.setVisibility(View.VISIBLE);
		choiceBar.setVisibility(View.GONE);
	}

	public void setChoiceMessage(String text) {
		this.choiceMessage.setText(text);
	}

	public void setAllButton() {
		checkedAll = true;
		choiceAll.setText("全选");
	}

	public void setUnAllButton() {
		checkedAll = false;
		choiceAll.setText("取消全选");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.choiceCancel:
			setCheckedChanged(false);
			if (itemSelected != null) {
				itemSelected.onNavigationBarItemCheckCancel();
			}
			break;
		case R.id.choiceAll:
			boolean checked = checkedAll;
			setCheckedChanged(checked);
			if (itemSelected != null) {
				itemSelected.onNavigationBarItemCheckAll(checked);
			}
			break;
		default:
			break;
		}
	}
}
