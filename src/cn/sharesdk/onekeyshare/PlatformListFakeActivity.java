package cn.sharesdk.onekeyshare;

import android.content.Context;
import android.content.Intent;
import android.studio.os.LogCat;
import android.view.KeyEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mob.tools.FakeActivity;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.ShareSDK;

public class PlatformListFakeActivity extends FakeActivity {
	protected HashMap<String, Object> shareParamsMap;
	protected boolean silent;
	protected ArrayList<CustomerLogo> customerLogos;
	protected HashMap<String, String> hiddenPlatforms;
	private boolean canceled = false;
	protected View backgroundView;

	protected OnShareButtonClickListener onShareButtonClickListener;
	protected boolean dialogMode = false;
	protected ThemeShareCallback themeShareCallback;

	/**
	 * 分享按键触发
	 * @author  mahuan
	 * @version 1.0 2015年7月7日 下午3:26:58
	 */
	public static interface OnShareButtonClickListener {
		void onClick(View v, List<Object> checkPlatforms);
	}

	public void onCreate() {
		super.onCreate();

		canceled = false;

		if(themeShareCallback == null) {
			finish();
		}
	}

	public boolean onKeyEvent(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			LogCat.e("onKeyEvent.....KeyCode_back....");
			canceled = true;
		}
		return super.onKeyEvent(keyCode, event);
	}

	protected void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public boolean onFinish() {

		// 取消分享菜单的统计
		if (canceled) {
			ShareSDK.logDemoEvent(2, null);
		}

		return super.onFinish();
	}

	@Override
	public void show(Context context, Intent i) {
		super.show(context, i);
	}

	public HashMap<String, Object> getShareParamsMap() {
		return shareParamsMap;
	}

	public void setShareParamsMap(HashMap<String, Object> shareParamsMap) {
		this.shareParamsMap = shareParamsMap;
	}

	public boolean isSilent() {
		return silent;
	}

	public void setSilent(boolean silent) {
		this.silent = silent;
	}

	public ArrayList<CustomerLogo> getCustomerLogos() {
		return customerLogos;
	}

	public void setCustomerLogos(ArrayList<CustomerLogo> customerLogos) {
		this.customerLogos = customerLogos;
	}

	public HashMap<String, String> getHiddenPlatforms() {
		return hiddenPlatforms;
	}

	public void setHiddenPlatforms(HashMap<String, String> hiddenPlatforms) {
		this.hiddenPlatforms = hiddenPlatforms;
	}

	public View getBackgroundView() {
		return backgroundView;
	}

	public void setBackgroundView(View backgroundView) {
		this.backgroundView = backgroundView;
	}

	public OnShareButtonClickListener getOnShareButtonClickListener() {
		return onShareButtonClickListener;
	}

	public void setOnShareButtonClickListener(OnShareButtonClickListener onShareButtonClickListener) {
		this.onShareButtonClickListener = onShareButtonClickListener;
	}

	public boolean isDialogMode() {
		return dialogMode;
	}

	public void setDialogMode(boolean dialogMode) {
		this.dialogMode = dialogMode;
	}

	public ThemeShareCallback getThemeShareCallback() {
		return themeShareCallback;
	}

	public void setThemeShareCallback(ThemeShareCallback themeShareCallback) {
		this.themeShareCallback = themeShareCallback;
	}

	/**
	 * 分享按钮在点击时　触发事件　
	 * @des
	 * @param v
	 * @param checkedPlatforms
	 */
	protected void onShareButtonClick(View v, List<Object> checkedPlatforms) {

		if(onShareButtonClickListener != null) {
			onShareButtonClickListener.onClick(v, checkedPlatforms);
		}

		HashMap<Platform, HashMap<String, Object>> silentShareData = new HashMap<Platform, HashMap<String,Object>>();
		final List<Platform> supportEditPagePlatforms = new ArrayList<Platform>();

		Platform plat;
		HashMap<String, Object> shareParam;
		for(Object item : checkedPlatforms) {
			if(item instanceof CustomerLogo){
				CustomerLogo customerLogo = (CustomerLogo)item;
				customerLogo.listener.onClick(v);
				continue;
			}

			plat = (Platform)item;
			String name = plat.getName();

			// EditPage不支持微信平台、Google+、QQ分享、Pinterest、信息和邮件，总是执行直接分享
			if(silent || ShareCore.isDirectShare(plat)) {
				shareParam = new HashMap<String, Object>(shareParamsMap);
				shareParam.put("platform", name);
				silentShareData.put(plat, shareParam);
			} else {
				supportEditPagePlatforms.add(plat);
			}
		}
		if (silentShareData.size() > 0) {
			themeShareCallback.doShare(silentShareData);
		}

		// 跳转EditPage分享
		if(supportEditPagePlatforms.size() > 0) {
			showEditPage(supportEditPagePlatforms);
		}

		finish();
	}

	protected void showEditPage(List<Platform> platforms) {
		showEditPage(getContext(), platforms);
	}

	public void showEditPage(Context context, Platform platform) {
		ArrayList<Platform> platforms = new ArrayList<Platform>(1);
		platforms.add(platform);
		showEditPage(context, platforms);
	}

	/**
	 * @des   显示带编辑页的平台
	 * @param context
	 * @param platforms
	 */
	protected void showEditPage(Context context, List<Platform> platforms) {
		EditPageFakeActivity editPageFakeActivity;
		String editPageClass = ((Object)this).getClass().getPackage().getName()+".EditPage";
		try {
			editPageFakeActivity = (EditPageFakeActivity) Class.forName(editPageClass).newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		editPageFakeActivity.setBackgroundView(backgroundView);
		editPageFakeActivity.setShareData(shareParamsMap);
		editPageFakeActivity.setPlatforms(platforms);
		if (dialogMode) {
			editPageFakeActivity.setDialogMode();
		}
		editPageFakeActivity.showForResult(context, null, new FakeActivity() {
			public void onResult(HashMap<String, Object> data) {
				if(data == null)
					return;
//				LogCat.e("==================showForResult=======================");
				if (data.containsKey("editRes")) {
					@SuppressWarnings("unchecked")
					HashMap<Platform, HashMap<String, Object>> editRes
							= (HashMap<Platform, HashMap<String, Object>>) data.get("editRes");
					themeShareCallback.doShare(editRes);
				}
			}
		});
	}
}
