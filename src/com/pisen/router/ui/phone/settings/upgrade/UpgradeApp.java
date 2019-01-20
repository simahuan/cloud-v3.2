package com.pisen.router.ui.phone.settings.upgrade;

import android.content.Context;
import android.studio.os.LogCat;
import android.studio.os.PreferencesUtils;

import com.google.gson.GsonUtils;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.config.HttpKeys;
import com.pisen.router.ui.phone.settings.http.HttpGetRequest;
import com.pisen.router.ui.phone.settings.http.HttpManager.OnHttpCallBack;

/**
 * 更新app
 * 
 * @author mahuan
 * @version 1.0 2015年5月21日 上午11:43:58
 */
public class UpgradeApp {
	// 当前环境
	private Context ctx;

	public UpgradeApp(Context ctx) {
		this.ctx = ctx;
	}

	/**
	 * 更新
	 * 
	 * @param back
	 *            返回数据的接口
	 * @param isShow
	 *            是否显示提示框
	 */
	public void refresh(final boolean isShow, final UpgradeAppCallBack back) {
		LogCat.i("Update %s\n", "refresh....");
		if (this.ctx != null) {
			HttpGetRequest getRequest = new HttpGetRequest(this.ctx);
			getRequest.setDialogHide();
			LogCat.i("Update %s\n", "before execute");
			getRequest.execute(HttpKeys.REFRESH_APP, "", new OnHttpCallBack() {
				@Override
				public void getHttpResult(String result) {
					AppVersionResult jsonResult = GsonUtils.jsonDeserializer(result, AppVersionResult.class);
					if (jsonResult == null || jsonResult.isDataNull()) {
						back.callBack(result);
						return;
					}
					AppVersion appVersion = jsonResult.AppVersion;
					boolean tmp = false;
					try {//兼容老版本，以前版本该key存放versionname
						tmp = PreferencesUtils.getBoolean(KeyUtils.APP_VERSION, false);
					} catch (Exception e) {
					}
					if (!tmp || isShow) { 
						try {
							back.downLoad(appVersion);
						} catch (Exception e) {
							back.callBack(result);
							e.printStackTrace();
							LogCat.e("RefreshApp %s\n", "版本更新下载出错: refresh -> getHttpResult(String)");
						}
					}
				}
			});
		}
	}

	/**
	 * 软件更新返回数据接口
	 * 
	 * @author MouJunFeng
	 * @version 1.0, 2014-7-24 下午3:13:12
	 */
	public interface UpgradeAppCallBack {
		/**
		 * 出现错误时候
		 * 
		 * @param result
		 */
		void callBack(String result);

		/**
		 * 获得数据的时候
		 * 
		 * @param apkUrl
		 * @param HttpVersion
		 * @throws NumberFormatException
		 * @throws Exception
		 */
		void downLoad(AppVersion app) throws NumberFormatException, Exception;
	}

}
