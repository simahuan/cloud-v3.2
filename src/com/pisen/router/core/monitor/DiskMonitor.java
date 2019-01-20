package com.pisen.router.core.monitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.simpleframework.xml.SimpleXmlUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Observable;
import android.os.AsyncTask;
import android.os.Handler;
import android.studio.os.EnvironmentUtils;
import android.studio.os.LogCat;
import android.studio.os.NetUtils;
import android.studio.util.URLUtils;
import android.text.TextUtils;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.pisen.router.CloudApplication;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.common.utils.VersionUtil;
import com.pisen.router.config.AppFileUtils;
import com.pisen.router.config.ResourceConfig;
import com.pisen.router.core.device.AbstractDevice;
import com.pisen.router.core.monitor.DiskMonitor.DiskEntity.MountType;
import com.pisen.router.core.monitor.DiskMonitor.OnDiskChangedListener;
import com.pisen.router.core.monitor.entity.Return;
import com.pisen.router.core.monitor.entity.RouterConfig;
import com.pisen.router.core.monitor.entity.Section;
import com.pisen.router.ui.phone.device.FirmwareUpgradeActivity;
import com.pisen.router.ui.phone.device.ForceUpgradeActivity;
import com.pisen.router.ui.phone.device.bean.CQRouterImageFile;
import com.pisen.router.ui.phone.device.bean.ZFirmwareInfo;

public class DiskMonitor extends Observable<OnDiskChangedListener> implements IMonitor {

	/**
	 * @desc{磁盘挂载监听}
	 */
	public interface OnDiskChangedListener {

		/**
		 * 监听磁盘是变化
		 * 
		 * @param disk
		 */
		void onDiskChanged();
	}

	static public class DiskEntity {
		/**
		 * 磁盘类型 本地磁盘，网络磁盘
		 */
		static public enum MountType {
			Local, Network
		}

		public String volume; // 盘符名字
		public String path; // 访问路径
		public long total; // 总大小
		public long used; // 使用
		// public long free; // 空闲
		public MountType type; // 磁盘类型
		private boolean isExtUsb;//是否是外接U盘
		
		public DiskEntity(MountType type) {
			super();
			this.type = type;
		}

		/**
		 * 获取当前使用的百分比
		 * 
		 * @return
		 */
		public int getUsedPercent() {
			if (total == 0) {
				return 0;
			}

			float result = used * 100f / total;
			if (result < 1 && result > 0) {
				return 1;
			}

			return (int) result;
		}

		/**
		 * @describtion
		 * @return  外部磁盘是否挂载　
		 */
		public boolean getExtDiskMount(){
			return isExtUsb;
		}
		
		public String getTotalString() {
			// return FileUtils.getFileSize(total);
			return AppFileUtils.formatFileSize(total);
		}

		public String getUsedString() {
			// return FileUtils.getFileSize(used);
			return AppFileUtils.formatFileSize(used);
		}

		public String getFreeString() {
			// return FileUtils.getFileSize(free);
			return AppFileUtils.formatFileSize(total - used);
		}

	}

	private Context mContext = null;
	private NetDiskMonitor netDiskMonitor; // 网络盘监控器
	private boolean scannerFinished; // 扫描完成
	private List<DiskEntity> diskList = new ArrayList<DiskEntity>();

	private static DiskMonitor INSTANCE;

	public static DiskMonitor getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new DiskMonitor();
		}
		return INSTANCE;
	}

	private DiskMonitor() {
	}

	@Override
	public void startMonitor(Context context) {
		this.mContext = context;
		registerSDReceiver(context);

		netDiskMonitor = new NetDiskMonitor(mContext, this);
		netDiskMonitor.start();

		String[] externalStorage = EnvironmentUtils.getExternalStorageDirectoryAll();
		for (String path : externalStorage) {
			addDiskEntity(path);
		}
	}

	@Override
	public void stopMonitor(Context context) {
		context.unregisterReceiver(localDiskMonitor);
		netDiskMonitor.stop();
		netDiskMonitor = null;
	}

	/**
	 * 是否硬盘扫描完成
	 * 
	 * @return
	 */
	public boolean isScannerFinished() {
		return scannerFinished;
	}
	
	/**
	 * 注册本地硬盘广播
	 */
	private void registerSDReceiver(Context context) {
		IntentFilter iFilter = new IntentFilter();
		iFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
		iFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		iFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
		iFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		iFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		iFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		iFilter.addDataScheme("file");
		context.registerReceiver(localDiskMonitor, iFilter);
	}

	/**
	 * @return 获取所有硬盘
	 */
	public DiskEntity[] getDiskAll() {
		synchronized (diskList) {
			return diskList.toArray(new DiskEntity[diskList.size()]);
		}
	}

	/**
	 * @return 获取本地硬盘 1.内置存储器 2.外置SD卡
	 */
	public DiskEntity[] getLocalDisk() {
		synchronized (diskList) {
			List<DiskEntity> results = new ArrayList<DiskEntity>();
			for (DiskEntity disk : diskList) {
				if (disk.type == MountType.Local) {
					results.add(disk);
				}
			}
			return results.toArray(new DiskEntity[results.size()]);
		}
	}

	/**
	 * @return 获取网络硬盘
	 */
	public DiskEntity[] getNetDisk() {
		synchronized (diskList) {
			List<DiskEntity> results = new ArrayList<DiskEntity>();
			for (DiskEntity disk : diskList) {
				if (disk.type == MountType.Network) {
					results.add(disk);
				}
			}
			return results.toArray(new DiskEntity[results.size()]);
		}
	}

	/**
	 * 添加磁盘
	 * 
	 * @param path
	 */
	private void addDiskEntity(String path) {
		DiskEntity disk = new DiskEntity(MountType.Local);
		disk.volume = URLUtils.getParentURI(path);
		disk.path = path;
		disk.total = EnvironmentUtils.getStatFsTotalSize(path);
		disk.used = EnvironmentUtils.getStatFsAvailableSize(path);
		addDiskEntity(disk);
	}

	public void addDiskEntity(DiskEntity newDisk) {
		for (DiskEntity disk : diskList) {
			if (disk.path.equals(newDisk.path)) {
				diskList.remove(disk);
				break;
			}
		}

		diskList.add(newDisk);
	}

	/**
	 * 删除磁盘
	 * 
	 * @param path
	 */
	public void removeDiskEntity(String path) {
		for (DiskEntity disk : diskList) {
			if (disk.path.equals(path)) {
				diskList.remove(disk);
				notifyDiskChanged();
				break;
			}
		}
	}

	public void removeNetDiskEntityAll() {
		DiskEntity[] disks = getNetDisk();
		for (DiskEntity disk : disks) {
			diskList.remove(disk);
		}
	}

	public void notifyDiskChanged() {
		synchronized (mObservers) {
			for (OnDiskChangedListener observer : mObservers) {
				observer.onDiskChanged();
			}
		}
	}

	/**
	 * SD卡监听,只能监听插入 拔出广播 ,如何判断手机上已经存在SD卡
	 */
	private BroadcastReceiver localDiskMonitor = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogCat.d("action = %s\n" + action);
			if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
				String path = intent.getData().getPath();
				addDiskEntity(path);
			} else if (Intent.ACTION_MEDIA_EJECT.equals(action)) {
				String path = intent.getData().getPath();
				removeDiskEntity(path);
			} else {

			}
		}
	};

	/**
	 * 网络磁盘监控
	 * 
	 * @author fq
	 */
	public class NetDiskMonitor extends TimerTask {
		static final int TIME_SLEEP = 20 * 1000;
		public static final String SysInfo = "http://%s/cgi-bin/SysInfo";
		private DiskMonitor diskMount;
		private Timer timer = null;

		private boolean isRuning;
		private Context ctx;
		private Handler handler = new Handler();

		public NetDiskMonitor(Context ctx, DiskMonitor diskMount) {
			this.ctx = ctx;
			this.diskMount = diskMount;
		}

		/**
		 * 启动定时扫描
		 */
		public void start() {
			scannerFinished = false;
			timer = new Timer();
			timer.schedule(this, 0, TIME_SLEEP); //20s定时获取路由固件信息 
		}

		/**
		 * 清除定时扫描
		 */
		public void stop() {
			timer.cancel();
			timer = null;
			isRuning = false;
		}

		@Override
		public void run() {
			if (!isRuning) {
				isRuning = true;
				executeGetSysInfo();
				scannerFinished = true;
				isRuning = false;
			}
		}
		
		private String model;
		private void checkUpdate(Return routerInfo) {
			if (routerInfo == null) {
				return;
			}
		    model = routerInfo.model;
			if (!TextUtils.isEmpty(model)) {
				if (AbstractDevice.CQ_MODEL.equals(model) || AbstractDevice.ZJ_MODEL.equals(model)) {
					new FirmwareGetAsyncTask().execute("");
				}
			}
		}
		
		class FirmwareGetAsyncTask extends AsyncTask<String, Void, ZFirmwareInfo> {
			@Override
			protected ZFirmwareInfo doInBackground(String... params) {
				return AbstractDevice.getInstance().getFirmwareInfo();
			}

			@Override
			protected void onPostExecute(ZFirmwareInfo result) {
				if (result != null) {
					// 设置固件信息
					ResourceConfig.getInstance(ctx).setFirmwareInfo(result);
					// 不同设备获取版本号方法不同
					if (AbstractDevice.CQ_MODEL.equals(model)){
						LogCat.e("getCur_version_name->" + result.getDev().getVersion() );
					} else {
						LogCat.e("getCur_version_name->" + result.getCur_version_name() + " getService_version_name->" +result.getService_version_name());
					}
					// 判断是否有新版本
					 if (!TextUtils.isEmpty(result.getService_version_name()) && VersionUtil.isNewZXVersion(result.getCur_version_name(), result.getService_version_name())) {
							// 有新的强制更新版本
							if(VersionUtil.isForceZXVersion(result.getCur_version_name(), result.getService_version_name())) {
								Intent in = new Intent(mContext, ForceUpgradeActivity.class);
								in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								in.putExtra("info", result);
								mContext.startActivity(in);
							}
					 }
				} else {
					LogCat.e("检测版本信息出错");
				}
			}
		}

		/**
		 * @describtion
		 */
		private void executeGetSysInfo() {
			if (isPisenWifiConnect()) {
				try {
					String gateway = NetUtils.getGateway(ctx);
					String configURL = String.format(SysInfo, gateway);
					HttpRequest request = HttpRequest.post(configURL).connectTimeout(TIME_SLEEP)
							.form("data", "<getSysInfo><Storage/></getSysInfo>");
					if (request.ok()) {
						String rConfig = request.body();
						RouterConfig info = SimpleXmlUtils.read(rConfig, RouterConfig.class);
						if (info != null) {
							// 设置绑定 Router信息
							ResourceConfig.getInstance(ctx).setRouterInfo(info.getResult());
							// XXX 需考虑优化获取固件版本时机
							checkUpdate(info.getResult());
							removeNetDiskEntityAll();
							if (info.sectionList != null && !info.sectionList.isEmpty()) {
								for (Section section : info.sectionList) {
									final DiskEntity disk = new DiskEntity(MountType.Network);
									disk.volume = section.volume;
									disk.path = info.getSectionWebdavUrl(section);
									disk.total = section.getTotalUnit();
									disk.used = section.getUsedUnit();
									disk.isExtUsb = section.isExtUsb();
									diskMount.addDiskEntity(disk);
								}
							}

							handler.post(new Runnable() {
								@Override
								public void run() {
									notifyDiskChanged();
								}
							});
						}
					}
				} catch (Exception e) {
					LogCat.e("%s\n", e.getMessage());
				}
			}
		}

		/**
		 * 判断是否品胜Wifi
		 * @return
		 */
		private boolean isPisenWifiConnect() {
			if (ctx == null) {
				ctx = CloudApplication.getInstance();
			}
			String bssid = NetUtils.getWifiBSSID(ctx);
			return (bssid != null && bssid.startsWith(WifiMonitor.PISEN_BSSID_PREFIX));
		}
	}
}
