package com.pisen.router.ui.phone.device;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.studio.view.widget.BaseAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.common.view.DevRoundProgressBar;
import com.pisen.router.config.AppFileUtils;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.SardineCacheResource;
import com.pisen.router.core.filemanager.SardineResourceManager;
import com.pisen.router.core.filemanager.async.DeleteAsyncTask;
import com.pisen.router.core.monitor.DiskMonitor;
import com.pisen.router.core.monitor.DiskMonitor.DiskEntity;
import com.pisen.router.core.monitor.DiskMonitor.OnDiskChangedListener;
import com.pisen.router.ui.base.CloudActivity;

/**
 * 云盘统计　
 * 
 * @author mahuan
 * @version 1.0 2015年5月29日 下午5:34:49
 */
public class MeasureDevice extends CloudActivity implements
		OnDiskChangedListener, OnClickListener {
	public static final int DEV_DISK_INFO = 0x01;
	public static final int DEV_DISK_USED_PROGRESS = 0x02;
	public static final int DISK_USED_PROGRESS_DRAWN_SPEED = 60;
	public static final int DISK_UNMOUNTED_PROGRESS  = 100;
	
	/** 云盘列表 */
	public ListView netLst;
	/** 显示的搜索　云盘设备适配器 */
	public DeviceAdapter netAdapter;
	/** 路由磁盘监听器 */
	public DiskMonitor diskMonit;
	/** 设备存储进度信息 */
	DevRoundProgressBar devProgressBar;
	/** 云端存储容量信息 */
	TextView devTotal, devUsed, devFree;
	long total, used, free;
	/** Item项 进度信息 */
	private int progress = 0;
	/** 记录点击时间 */
	long lastClick = 0;
	int circleProgressCount = 0;
	int circleProgress = 0;

	final Handler devHandler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case DEV_DISK_INFO:
				devTotal.setText(AppFileUtils.formatFileSize(total));
				devUsed.setText(AppFileUtils.formatFileSize(used));
				devFree.setText(AppFileUtils.formatFileSize(total-used));
				circleProgress = (int)(Math.round(used*100/(float)total));
//				LogCat.e("used = " +used+",total="+total);
//				LogCat.e("devHandler,circleProgress="+circleProgress);
				if (circleProgress >= 0 ){
					devHandler.sendEmptyMessageDelayed(DEV_DISK_USED_PROGRESS, DISK_USED_PROGRESS_DRAWN_SPEED);
					}
				break;
			case DEV_DISK_USED_PROGRESS:
				if (circleProgressCount <= circleProgress) {
					devProgressBar.setProgress(circleProgressCount++); //最终进度
				}
				devHandler.sendEmptyMessageDelayed(DEV_DISK_USED_PROGRESS,
						DISK_USED_PROGRESS_DRAWN_SPEED);
				break;
			default:
				break;
			}
			return true;
		}
	});

	private void loadDiskData() {
		total = 0;
		used = 0;
		free = 0;
		progress = 0;
		netAdapter.setData(diskMonit.getNetDisk());
		netAdapter.notifyDataSetChanged();
	}

	private void refreshView(DiskEntity... data) {
		total = 0;
		used = 0;
		free = 0;
		progress = 0;

		int size = data.length;
		DiskEntity entity = null;
		for (int i = 0; i < size; i++) {
			entity = data[i];
			total = total + entity.total;
			used = used + entity.used;
			free = free + (total - used);
			progress = progress + entity.getUsedPercent();
		}
		devHandler.sendEmptyMessage(DEV_DISK_INFO);
	}

	/**
	 * @describtion   磁盘类型排序,内置磁盘优先
	 * @param data
	 */
	private void setDiskOrder(DiskEntity... data) {
		if (data != null && data.length > 0) {
			Collections.sort(Arrays.asList(data), new Comparator<DiskEntity>() {
				@Override
				public int compare(DiskEntity lhs, DiskEntity rhs) {
					if (lhs.getExtDiskMount() == rhs.getExtDiskMount()) {
						return 0;
					} else if (lhs.getExtDiskMount() && !rhs.getExtDiskMount()) {
						return 1;
					} else {
						return -1;
					}
				}
			});
		}
	}
	
	class DeviceAdapter extends BaseAdapter<DiskEntity> {
		public DeviceAdapter(Context context) {
			super(context);
		}

		@Override
		public void setData(DiskEntity... data) {
			super.setData(data);
			if (data == null || data.length == 0){
				devProgressBar.setDiskMounted(false);
			} else {
				devProgressBar.setDiskMounted(true);
			}
			setDiskOrder(data);
			refreshView(data);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			View view = convertView;
			if (view == null) {
				view = LayoutInflater.from(MeasureDevice.this).inflate(
						R.layout.cloud_device_storage_space_measure_item,
						(ViewGroup) null);
				view.setTag(holder = new ViewHolder());
				holder.headImage = (ImageView) view
						.findViewById(R.id.imgDevice);
				holder.deviceName = (TextView) view
						.findViewById(R.id.txtUdiskName);
				holder.storage = (ProgressBar) view
						.findViewById(R.id.progressBarCloudStorage);
				holder.used = (TextView) view.findViewById(R.id.userStorage);
				holder.total = (TextView) view.findViewById(R.id.totalStorage);
				// holder.clearBtn = (ImageView)
				// view.findViewById(R.id.dev_delete);
				// holder.clearBtn.setOnClickListener(MeasureDevice.this);
			} else {
				holder = (ViewHolder) view.getTag();
			}

			final DiskEntity entity = getData().get(position);
			// 要根据不同section段,设备不同或是设备路径不同才能相加
			// total = total+entity.total;
			// used = used+entity.used;
			// free = free+(total - used);
			// progress = progress+entity.getUsedPercent();

			holder.storage.setProgress(entity.getUsedPercent());
			holder.used.setText("已用:" + entity.getUsedString());
			holder.total.setText("总容量:" + entity.getTotalString());
			holder.deviceName.setText(entity.volume);
			holder.headImage
					.setImageResource(entity.getExtDiskMount() ? R.drawable.equipment_usb
							: R.drawable.filetype_cloudusb);
			return view;
		}

		class ViewHolder {
			ImageView headImage;
			TextView deviceName;
			ProgressBar storage;
			TextView used;
			TextView total;
			// ImageView clearBtn;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloud_device_storage_space);

		diskMonit = DiskMonitor.getInstance();
		diskMonit.registerObserver(this);
		initialWidget();

		if (diskMonit.isScannerFinished()) {
			loadDiskData();
			
		} else {
			showProgressDialog("加载中...");
		}
	}

	@Override
	protected void onDestroy() {
		diskMonit.unregisterObserver(this);
		super.onDestroy();
	}

	/**
	 * 初始化控件
	 */
	public void initialWidget() {
		((TextView) this.findViewById(R.id.txtTitle)).setText("存储空间");
		Button btnLeft = (Button) this.findViewById(R.id.btnLeft);
		btnLeft.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		devTotal = (TextView) findViewById(R.id.devTotal);
		devUsed = (TextView) findViewById(R.id.devUsed);
		devFree = (TextView) findViewById(R.id.devFree);
		devProgressBar = (DevRoundProgressBar) findViewById(R.id.devRoundProgressBar);
//		devProgressBar.setProgress(100);

		netLst = (ListView) findViewById(R.id.deviceNumberlst);
		netAdapter = new DeviceAdapter(this);
		netLst.setAdapter(netAdapter);
	}

	@Override
	public void onClick(View v) {
		// switch (v.getId()) {
		// case R.id.dev_delete: // Pisen云盘　　才有清空按钮
		// List<ResourceInfo> res = new ArrayList<ResourceInfo>();
		// ResourceInfo resinfo = new ResourceInfo();
		// DiskEntity[] diskEntities = diskMonit.getNetDisk();
		// resinfo.path = diskEntities[0].path;
		// res.add(resinfo);
		//
		// if (System.currentTimeMillis() - lastClick > 500)
		// clearCloudRes(res);
		// lastClick = System.currentTimeMillis();
		// break;
		// }
	}

	/**
	 * @des 清理云端数据
	 */
	public void clearCloudRes(final List<ResourceInfo> res) {
		ConfirmDialog.show(this, "确定要清空Pisen云盘?", null, "确定",
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						final SardineResourceManager sardineManager = new SardineCacheResource();
						new DeleteAsyncTask(sardineManager, res, null)
								.execute();
						AsyncTaskUtils
								.execute(new InBackgroundCallback<Boolean>() {
									@Override
									public Boolean doInBackground() {
										for (ResourceInfo info : res) {
											checkCancelled();
											try {
												sardineManager
														.delete(info.path);
											} catch (Exception e) {
												return false;
											}
										}
										return true;
									}

									private void checkCancelled() {
									}

									@Override
									public void onPostExecute(Boolean result) {
										if (result) {
											showProgressDialog("正在重新统计存储空间");
										} else {
											UIHelper.showToast(
													MeasureDevice.this,
													"清除云盘失败");
										}
									}
								});
					}
				}, "取消", null);
	}

	@Override
	public void onDiskChanged() {
		dismissProgressDialog();
		loadDiskData();
	}
}
