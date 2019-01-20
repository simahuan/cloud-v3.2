package com.pisen.router.core.document;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.studio.util.URLUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.core.filemanager.ResourceCategory;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.ResourceInfo.RSource;
import com.pisen.router.ui.base.CloudActivity;
import com.pisen.router.ui.phone.resource.SendAndReceiveDataHelper;
import com.pisen.router.ui.phone.resource.v2.NavigationBar;

/**
 * 下载文档UI 从云端下载后，使用apk打开
 */
public class DownloadAndOpenDocActivity extends CloudActivity implements OnClickListener {

	private int fileLength; // 文件长度
	private int downedFileLength = 0; // 已下载
	private ImageView docIcon; // 文档Icon
	private TextView docName; // 文档名
	private ProgressBar docProgressBar; // 文档下载进度
	private TextView docCapacity; // 文件大小
	private Button btnOpenDoc;
	private ResourceInfo resourceInfo;
	private String dirPath = Environment.getExternalStorageDirectory() + "/tmpFile/";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.doc_main);
		initView();
		resourceInfo = new ResourceInfo();

		List<ResourceInfo> list = new ArrayList<ResourceInfo>();
		SendAndReceiveDataHelper.getIntentDataUseResourceInfo(getIntent(), list, null);
		resourceInfo = list.get(0);
		resourceInfo.path = URLUtils.encodeURL(resourceInfo.path);
		docName.setText(resourceInfo.name);
		docIcon.setImageResource(ResourceCategory.getIconResId(resourceInfo.name));

		NavigationBar naviBar = (NavigationBar) getNavigationBar();
		naviBar.setBackgroundColor(Color.parseColor("#0073FF"));
		naviBar.setTitle(resourceInfo.name);
		naviBar.setLeftButton(null, R.drawable.menu_ic_back, new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		/* 下载该文档到临时文件 */
		if (resourceInfo.source == RSource.Remote) {// 下载后打开
			Thread thread = new Thread() {
				public void run() {
					try {
						new DownloadUtil().downloadFile(resourceInfo.path, resourceInfo.name, handler);
					} catch (Exception e) {
						Log.i("", e.toString());
					}
				}
			};
			thread.start();
		} else {// 本地文件，直接打开
			File file = new File(resourceInfo.path);
			if (file != null) {
				finish();
				openFile(file);
			}
		}
	}

	private void initView() {
		docIcon = (ImageView) findViewById(R.id.docIcon);
		docName = (TextView) findViewById(R.id.docName);
		docProgressBar = (ProgressBar) findViewById(R.id.docProgressBar);
		docCapacity = (TextView) findViewById(R.id.docCapacity);
		btnOpenDoc = (Button) findViewById(R.id.btnOpenDoc);
		btnOpenDoc.setOnClickListener(this);

	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (!Thread.currentThread().isInterrupted()) {
				switch (msg.what) {
				case DownloadUtil.DownloadBegin:// 开始下载
					fileLength = (Integer) msg.obj;
					docProgressBar.setMax(fileLength);
					resourceInfo.size = fileLength;
					Log.i("文件长度----------->", docProgressBar.getMax() + "");
					break;
				case DownloadUtil.Downloading:// 更新下载进度
					// 更新进度条
					downedFileLength = (Integer) msg.obj;
					docProgressBar.setProgress(downedFileLength);
					String totalSize = FileCapacityUtil.formatSizeToMB(fileLength);
					totalSize = filterSize(totalSize);
					String curSize = FileCapacityUtil.formatSizeToMB(downedFileLength);
					curSize = filterSize(curSize);
					docCapacity.setText(curSize + "/" + totalSize);
					break;
				case DownloadUtil.DownloadEnd:// 下载完成

					break;

				default:
					break;
				}
			}
		}

	};

	private String filterSize(String size) {
		if (size.equals("0.0MB")) {
			return "0.1MB";
		}
		return size;
	}

	/**
	 * 打开文件
	 * 
	 * @param file
	 */
	private void openFile(File file) {
		// Uri uri = Uri.parse("file://"+file.getAbsolutePath());
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// 设置intent的Action属性
		intent.setAction(Intent.ACTION_VIEW);
		// 获取文件file的MIME类型
		String type = ResourceCategory.getMimeType(file.getName());
		// 设置intent的data和Type属性。
		intent.setDataAndType(/* uri */Uri.fromFile(file), type);
		if (isIntentAvailable(this, intent)) {
			startActivity(intent);
		} else {
			UIHelper.showToast(this, "无法打开这个文件" + file.toString());
		}
	}

	/**
	 * 判断是否安装了可以打开intent的应用
	 * 
	 * @param context
	 * @param intent
	 */
	private boolean isIntentAvailable(Context context, Intent intent) {
		final PackageManager packageManager = context.getPackageManager();
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.GET_ACTIVITIES);
		return list.size() > 0;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnOpenDoc:
			File file = new File(dirPath, resourceInfo.name);
			if (file != null) {
				openFile(file);
			}
			break;
		}

	}
}