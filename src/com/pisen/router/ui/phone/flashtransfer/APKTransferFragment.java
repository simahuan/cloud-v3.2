package com.pisen.router.ui.phone.flashtransfer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pisen.router.R;
import com.pisen.router.core.filemanager.ApkResourceInfo;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.flashtransfer.FlashTransferManager;

public class APKTransferFragment extends FlashTransferListFragment {
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v =  super.onCreateView(inflater, container, savedInstanceState);
		adapter.setOnItemClickListener(null);
		return v;
	}

	@Override
	public String getFragmentTitle() {
		return "应用";
	}

	@Override
	public String getTypeDescription() {
		return getResources().getString(R.string.flashtransfer_count_apk);
	}

	@Override
	public String getRefreshAction() {
		return FlashTransferManager.ACTION_TRANSFER_COMPLETE_RECEIVE_APK;
	}

	@Override
	public List<ResourceInfo> getData() {
		List<ResourceInfo> data = convertData(getActivity(), getAllApps(getActivity()));
		if(data != null && !data.isEmpty()) {
			// 排序
			Collections.sort(data, new Comparator<ResourceInfo>() {

				@Override
				public int compare(ResourceInfo lhs, ResourceInfo rhs) {
					return -(int) (lhs.lastModified - rhs.lastModified);
				}
			});
		}
		
		return data;
	}
	
	protected List<ResourceInfo> convertData(Context ctx, List<PackageInfo> packages) {
		PackageManager pManager = ctx.getPackageManager();
		List<ResourceInfo> data = null;
		if (packages != null && !packages.isEmpty()) {
			data = new ArrayList<ResourceInfo>();
			try {
				for (PackageInfo pinfo : packages) {
					ApkResourceInfo r = new ApkResourceInfo();
					r.path = pManager.getApplicationInfo(pinfo.applicationInfo.packageName, 0).sourceDir;
					File apkFile = new File(r.path);
					if (apkFile.exists() && apkFile.isFile()) {
						r.name = apkFile.getName();
						r.apkName = pManager.getApplicationLabel(pinfo.applicationInfo).toString();
						r.createTime = apkFile.lastModified() /1000;
						r.lastModified = apkFile.lastModified() /1000;
						r.size = apkFile.length();
						r.icon = pManager.getApplicationIcon(pinfo.applicationInfo);

						data.add(r);
					}
				}
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	/**
	 * 查询手机内非系统应用
	 * 
	 * @param context
	 * @return
	 */
	public List<PackageInfo> getAllApps(Context context) {
		List<PackageInfo> apps = new ArrayList<PackageInfo>();
		PackageManager pManager = context.getPackageManager();
		// 获取手机内所有应用
		List<PackageInfo> paklist = pManager.getInstalledPackages(0);
		for (int i = 0; i < paklist.size(); i++) {
			PackageInfo pak = (PackageInfo) paklist.get(i);
			// 判断是否为非系统预装的应用程序
			if ((pak.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {
				apps.add(pak);
			}
		}
		return apps;
	}
}
