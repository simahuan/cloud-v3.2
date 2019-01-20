package com.pisen.router.core.image;

import uk.co.senab.photoview.PhotoViewAttacher;
import uk.co.senab.photoview.PhotoViewAttacher.OnPhotoTapListener;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.pisen.router.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.Priority;

/**
 * 图片显示Fragment (ViewPager使用Fragment展示图片)
 */
public class ImageDetailFragment extends Fragment implements OnPhotoTapListener{

	private String mImageUrl;
	private ImageView mImageView;
	private PhotoViewAttacher mAttacher;

	private View loadingLayout;
	private ImageView imgLoding;

	public static ImageDetailFragment newInstance(Uri imageUrl) {
		final ImageDetailFragment f = new ImageDetailFragment();

		final Bundle args = new Bundle();
		args.putString("url", imageUrl.toString());
		f.setArguments(args);

		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mImageUrl = getArguments() != null ? getArguments().getString("url") : null;
	}
	
	/**
	 * 获取图片路径
	 * @return
	 */
	public String getImageUrl() {
		return mImageUrl;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View v = inflater.inflate(R.layout.imagebrower_detail_fragment, container, false);
		mImageView = (ImageView) v.findViewById(R.id.image);
		loadingLayout = v.findViewById(R.id.loadingLayout);
		imgLoding = (ImageView) v.findViewById(R.id.imgLoding);
		return v;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		showLoadingView();
		
		Picasso.with(getActivity()).load(mImageUrl).priority(Priority.HIGH).error(R.drawable.thumbnail_pic_fail).into(mImageView, new Callback(){
			@Override
			public void onError() {
				hideLoadingView();
			}

			@Override
			public void onSuccess() {
				hideLoadingView();
				mAttacher = new PhotoViewAttacher(mImageView);
				mAttacher.setOnPhotoTapListener(ImageDetailFragment.this);
			}			
		});
	}

	private void showLoadingView() {
		loadingLayout.setVisibility(View.VISIBLE);
		((AnimationDrawable)imgLoding.getDrawable()).start();
	}

	private void hideLoadingView() {
		((AnimationDrawable)imgLoding.getDrawable()).stop();
		loadingLayout.setVisibility(View.GONE);
	}

	@Override
	public void onPhotoTap(View view, float x, float y) {
		ImageViewerActivity.getInstance().layoutControlToggle();
	}
}
