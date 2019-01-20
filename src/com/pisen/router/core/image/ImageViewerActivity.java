package com.pisen.router.core.image;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.studio.os.AsyncTaskUtils;
import android.studio.os.AsyncTaskUtils.InBackgroundCallback;
import android.studio.os.LogCat;
import android.studio.util.URLUtils;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pisen.router.R;
import com.pisen.router.common.dialog.ConfirmDialog;
import com.pisen.router.common.utils.UIHelper;
import com.pisen.router.core.filemanager.IResource;
import com.pisen.router.core.filemanager.ResourceInfo;
import com.pisen.router.core.filemanager.ResourceInfo.RSource;
import com.pisen.router.core.filemanager.SardineCacheResource;
import com.pisen.router.core.filemanager.transfer.TransferManagerV2;
import com.pisen.router.core.filemanager.transfer.TransferServiceV2;
import com.pisen.router.ui.base.CloudActivity;
import com.pisen.router.ui.base.INavigationBar;
import com.squareup.picasso.Picasso;

/**
 * 图片详情，根据各种转换的URI异步加载图片(需要确定调用本activity时传递进来的参数)
 */
public class ImageViewerActivity extends CloudActivity implements OnClickListener {
	private static final String STATE_POSITION = "STATE_POSITION";
	public static final String EXTRA_IMAGE_INDEX = "image_index";
	public static final String EXTRA_IMAGE_URLS = "image_urls";
	private HackyViewPager mPager;
	private static List<ResourceInfo> playlist;
	private static ResourceInfo curInfo;
	private int itemSelectedIndex;
	private static INavigationBar navigationBar;
	public static LinearLayout llayoutControl;
	private int currentItemIndex = 0;
	private static ImageViewerActivity instance;

	ImagePagerAdapter mAdapter;
	TransferManagerV2 transManger;
	IResource sardineManager;
	private boolean isImageDeleted;

	public static ImageViewerActivity getInstance() {
		if (null == instance) {
			instance = new ImageViewerActivity();
		}
		return instance;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.imagebrower_detail_pager);
		
		initData();
		initView();
		sardineManager = new SardineCacheResource();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		bindService();
		mPager.setCurrentItem(itemSelectedIndex);
	}
	
	@Override
	protected void onPause() {
		unbindService();
		super.onPause();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putInt(STATE_POSITION, mPager.getCurrentItem());
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			itemSelectedIndex = savedInstanceState.getInt(STATE_POSITION);
		}
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	private void initView() {
		initNavigation();
		initPager();
		findViewById(R.id.btnDownload).setOnClickListener(this);
		findViewById(R.id.btnDelete).setOnClickListener(this);

		llayoutControl = (LinearLayout) this.findViewById(R.id.llayoutControl);
		if (curInfo.source == RSource.Local) {
			llayoutControl.setVisibility(View.GONE);
		}
	}

	private void initNavigation() {
		navigationBar = getNavigationBar();
		navigationBar.setBackgroundResource(R.drawable.video_topshadow);
		navigationBar.setLeftButton(null, R.drawable.menu_ic_back, new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	public void finish() {
		Intent data = new Intent();
		data.putExtra("refresh", isImageDeleted);
		setResult(RESULT_OK, data);
		super.finish();
	}

	private void initPager() {
		mPager = (HackyViewPager) findViewById(R.id.pager);
		mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), getImageUrls());
		mPager.setAdapter(mAdapter);
		refreshNavigation(0, mAdapter.getCount());

		// 更新下标
		mPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageSelected(int position) {
				currentItemIndex = position;
				refreshNavigation(position, mAdapter.getCount());
			}

		});
//		mPager.setOffscreenPageLimit(3);
	}
	
	private void refreshNavigation(int curPosition, int count) {
		String text = String.format("%1$d/%2$d", curPosition + 1, count);
		navigationBar.setTitle(text);
	}

	private int getCurrentItemIndex() {
		return currentItemIndex;
	}

	public static void start(Context context, ResourceInfo info, List<ResourceInfo> playlist) {
		ImageViewerActivity.playlist = playlist;
		curInfo = info;
		((Activity)context).startActivity(new Intent(context, ImageViewerActivity.class));
	}
	
	public static void startForResult(Activity context, int requestCode, ResourceInfo info, List<ResourceInfo> playlist) {
		ImageViewerActivity.playlist = playlist;
		curInfo = info;
		context.startActivityForResult(new Intent(context, ImageViewerActivity.class),requestCode);
	}

	private void initData() {
		if (playlist != null && curInfo != null && !playlist.isEmpty()) {
			itemSelectedIndex = getImageIndex();
		}
	}

	private ResourceInfo getResourceforIndex(int location) {
		return playlist.get(location);
	}

	private Uri[] getImageUrls() {
		List<Uri> list = new ArrayList<Uri>();
		for (int i = 0; i < playlist.size(); i++) {
			list.add(resourceInfoToUri(playlist.get(i)));
		}
		return list.toArray(new Uri[0]);
	}

	private int getImageIndex() {
		for (int i = 0; i < playlist.size(); i++) {
			if (playlist.get(i).path.equals(curInfo.path)) {
				return i;
			}
		}
		return -1;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		playlist = null;
		curInfo = null;
		super.onDestroy();
	}
	
	private class ImagePagerAdapter extends FragmentStatePagerAdapter {
		public Uri[] fileList;

		public ImagePagerAdapter(FragmentManager fm, Uri[] fileList) {
			super(fm);
			this.fileList = fileList;
		}

		public void setResourceAdapter(Uri[] fileList) {
			this.fileList = fileList;
		}

		@Override
		public int getCount() {
			return fileList == null ? 0 : fileList.length;
		}

		@Override
		public Fragment getItem(int position) {
			Uri url = fileList[position];
			return ImageDetailFragment.newInstance(url);
		}
		
		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
			Picasso.with(getApplicationContext()).invalidate(((ImageDetailFragment)object).getImageUrl());
			removeFragment(container, (ImageDetailFragment)object);
		}
	}
	
	/**
	 * 移除页
	 * @param container
	 * @param fragment
	 */
	public void removeFragment(ViewGroup container,ImageDetailFragment fragment) {
		if(container != null && fragment != null) {
			View contentView = fragment.getView();
			if(contentView != null) {
				((HackyViewPager)container).removeView(fragment.getView());
				ImageView imageView = (ImageView) contentView.findViewById(R.id.image);
				recycleView(imageView);
				contentView = null;
				
				System.gc();
			}
			
			fragment = null;
		}
	}
	
	private void recycleView(ImageView view) {
		if(view != null) {
			Drawable d = view.getDrawable();
			if (d != null && d instanceof BitmapDrawable) {
				Bitmap bmp = ((BitmapDrawable) d).getBitmap();
				if(bmp != null && !bmp.isRecycled()) {
					bmp.recycle();
					bmp = null;
					LogCat.e("recycleView");
				}
			}
			view.setImageBitmap(null);
			if (d != null) {
				d.setCallback(null);
			}
		}
	}

	/**
	 * 包装资源路径到UniversalImageLoader使用的路径(含本地资源与云端资源处理，图片浏览器使用)
	 * 
	 * @param ri
	 * @return
	 */
	public static Uri resourceInfoToUri(ResourceInfo ri) {
		if (ri.source == RSource.Local) {
			return Uri.fromFile(new File(ri.path)); // Scheme.FILE.wrap(ri.path);
		} else {
			return Uri.parse(URLUtils.encodeURL(ri.path));
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnDownload:
			curInfo = getResourceforIndex(getCurrentItemIndex());
			downloadPicture(curInfo);
			break;
		case R.id.btnDelete:
			curInfo = getResourceforIndex(getCurrentItemIndex());
			deletePicture(curInfo); // 远程图库要刷新，当前浏览集要删除
			break;
		}
	}

	public void layoutControlToggle() {
		if (null != llayoutControl && null != navigationBar && curInfo.source == RSource.Remote) {
			if (llayoutControl.getVisibility() == View.GONE) {
				llayoutControl.setVisibility(View.VISIBLE);
				navigationBar.getView().setVisibility(View.VISIBLE);
			} else {
				llayoutControl.setVisibility(View.GONE);
				navigationBar.getView().setVisibility(View.GONE);
			}
		}
	}

	/**
	 * @des 下载图片
	 */
	private void downloadPicture(final ResourceInfo info) {
		AsyncTaskUtils.execute(new InBackgroundCallback<Boolean>() {
			@Override
			public Boolean doInBackground() {
				try {
					transManger.addDownloadTask(info);
					return true;
				} catch (Exception e) {
					return false;
				}
			}

			@Override
			public void onPostExecute(Boolean result) {
				UIHelper.showToast(ImageViewerActivity.this, "已为你添加到下载列表");
			}
		});
	}

	/**
	 * 　
	 * 
	 * @des　删除图片
	 */
	private void deletePicture(final ResourceInfo info) {
		ConfirmDialog.show(this, "确定要删除选中项吗?", "删除", "确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				AsyncTaskUtils.execute(new InBackgroundCallback<Boolean>() {
					@Override
					public Boolean doInBackground() {
						try {
							LogCat.e("=====deleteResource===" + info.path);
							sardineManager.delete(info.path);
							return true;
						} catch (Exception e) {
							return false;
						}
					}

					@Override
					public void onPostExecute(Boolean result) {
						if (result) {
							isImageDeleted = true;
							UIHelper.showToast(ImageViewerActivity.this, "删除成功");
							playlist.remove(getCurrentItemIndex());
							Uri [] tmp = getImageUrls();
							if(tmp.length <=0) {
								finish();
							}else {
								mAdapter.setResourceAdapter(tmp);
								mAdapter.notifyDataSetChanged();
								mPager.setCurrentItem(getCurrentItemIndex(), true);
								refreshNavigation(getCurrentItemIndex(), mAdapter.getCount());
							}
						} else {
							UIHelper.showToast(ImageViewerActivity.this, "删除失败");
						}
					}
				});
			}
		}, "取消", null);
	}

	private ServiceConnection conn;

	private void bindService() {
		conn = new ServiceConnection() {
			@Override
			public void onServiceDisconnected(ComponentName name) {
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				transManger = ((TransferServiceV2.TransferBinder) service).getTransferManager();
			}
		};
		Intent in = new Intent(this, TransferServiceV2.class);
		this.bindService(in, conn, Service.BIND_AUTO_CREATE);
	}

	private void unbindService() {
		this.unbindService(conn);
	}
}