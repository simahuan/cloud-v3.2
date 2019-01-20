package  com.pisen.router.ui.phone.settings.upgrade;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.studio.os.PreferencesUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.common.utils.KeyUtils;
import com.pisen.router.common.utils.UIHelper;

/**
 * app 版本更新
 * @author  mahuan
 * @version 1.0 2015年5月26日 下午5:19:59
 */
public class DownLoadApp {
	private Context mContext;
	// 返回的安装包url
	public String apkUrl = "";
	// 下载选择提示框
	public Dialog noticeDialog;
	// 下载进度条显示
	private Dialog downloadDialog;
	/* 下载包安装路径 */
	private static final String savePath = Environment
			.getExternalStorageDirectory().getPath()
			+ File.separator
			+ "PisenCloud";

	private static final String saveFileName = savePath + File.separator
			+ "UpdateCloudRelease.apk";

	/* 进度条与通知ui刷新的handler和msg常量 */
	private ProgressBar mProgress;
	/** apk文件大小 */
	private String apkFileSize;
	/** 下载文件大小 */
	private String tmpFileSize;
	// 正在下载
	private static final int DOWN_UPDATE = 1;
	// 下载完成
	private static final int DOWN_OVER = 2;
	//网络异常
	private static final int ERROR = 3;
	private int progress;
	private Thread downLoadThread;
	private boolean interceptFlag = false;
	private TextView mProgressPercent,mProgressNumber;
	
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_UPDATE:
				mProgress.setProgress(progress);
				if (mProgressPercent != null && progress < 100){
//					LogCat.i("size:DownApp = %d %%\n",(progress + 1));
					mProgressPercent.setText((progress + 1)+"%");
					mProgressNumber.setText(String.format("%s/%s", tmpFileSize,apkFileSize));
					}
				break;
			case DOWN_OVER:
				downloadDialog.dismiss();
				installApk();
				break;
			case ERROR:
				interceptFlag = false;
				UIHelper.showToast(mContext, "没有找到资源");
				break;
			}
		};
	};
	
	/**
	 * 显示正在下载的进度条提示框
	 */
	public void showDownloadDialog(final Context ctx) {
		this.mContext = ctx;
		AlertDialog.Builder builder = new Builder(ctx);
		downloadDialog = builder.create();
		downloadDialog.setCancelable(false);
		downloadDialog.setCanceledOnTouchOutside(false);
		downloadDialog.show();
		final LayoutInflater inflater = LayoutInflater.from(ctx);
		View v = inflater.inflate(R.layout.project_update_version, (ViewGroup)null);
//		downloadDialog.getWindow().setLayout(600, 280);
		downloadDialog.setContentView(v);
		mProgress = (ProgressBar) v.findViewById(R.id.progressBar);
		mProgressPercent = (TextView) v.findViewById(R.id.progressPercent);
	    mProgressNumber  = (TextView) v.findViewById(R.id.progressNumber);
	    
	    ((Button) v.findViewById(R.id.negativeButton)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				downloadDialog.dismiss();
				interceptFlag = true;
			}
		});
		downloadApk();
	}

	/**
	 * 下载的线程
	 */
	private Runnable mdownApkRunnable = new Runnable() {
		@Override
		public void run() {
			try {
				URL url = new URL(apkUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.connect();
				int length = conn.getContentLength();
				
				InputStream is = conn.getInputStream();
				//显示文件大小格式：2个小数点显示
		    	DecimalFormat df = new DecimalFormat("0.00");
		    	//进度条下面显示的总文件大小
		    	apkFileSize = df.format((float) length / 1024 / 1024) + "MB";

				File file = new File(savePath);
				if (!file.exists()) {
					file.mkdirs();
				}
				
				String apkFile = saveFileName;
				File ApkFile = new File(apkFile);
				FileOutputStream fos = new FileOutputStream(ApkFile);
				int count = 0;
				byte buf[] = new byte[1024];

				do {
					int numread = is.read(buf);
					count += numread;
					//进度条下面显示的当前下载文件大小
		    		tmpFileSize = df.format((float) count / 1024 / 1024) + "MB";
					progress = (int) (((float) count / length) * 100);
					// 更新进度
					mHandler.sendEmptyMessage(DOWN_UPDATE);
					if (numread <= 0) {
						// 下载完成通知安装
						mHandler.sendEmptyMessage(DOWN_OVER);
						break;
					}
					fos.write(buf, 0, numread);
				} while (!interceptFlag);// 点击取消就停止下载.

				fos.close();
				is.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
				mHandler.sendEmptyMessage(ERROR);
			} catch (IOException e) {
				e.printStackTrace();
				mHandler.sendEmptyMessage(ERROR);
			}
		}
	};

	/**
	 * 下载apk
	 * @param LoginKey
	 */
	private void downloadApk() {
		downLoadThread = new Thread(mdownApkRunnable);
		downLoadThread.start();
	}

	/**
	 * 安装apk
	 * @param LoginKey
	 */
	private void installApk() {
		File apkfile = new File(saveFileName);
		if (!apkfile.exists()) {
			return;
		}
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		mContext.startActivity(i);
		PreferencesUtils.setBoolean(KeyUtils.APP_VERSION, false);
	}
}
