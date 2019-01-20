package  com.pisen.router.ui.phone.settings.upgrade;

import com.pisen.router.ui.phone.settings.upgrade.UpgradeApp.UpgradeAppCallBack;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;


/**
 * App在线升级服务
 * @author  mahuan
 * @version 1.0 2015年6月16日 上午9:21:28
 */
public class UpgradeAppService extends Service {
	// 绑定服务的binder
	private MyBinder binder = new MyBinder();
	// 更新的返回接口
	private UpgradeAppCallBack back;
	// 是否显示提示框
	private boolean isShow;

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	/**
	 * 软件更新
	 */
	public void refresh() {
		UpgradeApp app = new UpgradeApp(this);
		if (back != null) {
			app.refresh(UpgradeAppService.this.isShow, back);
		}
	}

	/**
	 * 绑定service的binder 实现通讯
	 * 
	 * @author MouJunFeng
	 * @version 1.0, 2014-7-24 下午3:22:29
	 * 
	 */
	public class MyBinder extends Binder {
		/**
		 * 获得RefreshAppService实例
		 * 
		 * @return RefreshAppService
		 */
		public UpgradeAppService getUpgradeAppService() {
			return UpgradeAppService.this;
		}

		/**
		 * 获得数据接口
		 * 
		 * @param back
		 *            RefreshAppCallBack
		 */
		public void setUpgradeAppCallBack(UpgradeAppCallBack back) {
			UpgradeAppService.this.back = back;
		}

		/**
		 * 是否显示提示框
		 * 
		 * @param show
		 *            boolean
		 */
		public void setIsShow(boolean show) {
			UpgradeAppService.this.isShow = show;
		}
	}

}
