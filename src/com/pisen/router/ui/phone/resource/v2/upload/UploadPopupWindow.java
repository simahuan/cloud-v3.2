package com.pisen.router.ui.phone.resource.v2.upload;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.pisen.router.R;
import com.pisen.router.core.camera.CameraActivity;
import com.pisen.router.core.filemanager.ResourceCategory.FileType;
import com.pisen.router.ui.base.BasePopupWindow;

public class UploadPopupWindow extends BasePopupWindow implements OnClickListener {
	private ImageButton closeButton;
	private Button allButton;
	private Button cameraButton;
	private Button imageButton;
	private Button videoButton;
	private Button musicButton;
	private Button documentButton;
	
	private ObjectAnimator rotateAnim;
	private ObjectAnimator rotateExitAnim;
	private static final long ANIM_TIME = 300;
	private ObjectAnimator scaleXEnterAnim;
	private ObjectAnimator scaleYEnterAnim;
	private ObjectAnimator scaleXExitAnim;
	private ObjectAnimator scaleYExitAnim;

	public interface OnUploadItemClickListener {
		void onUploadItemClick(FileType type);
	}

	private Activity activity;

	private OnUploadItemClickListener uploadItemClickListener;

	public UploadPopupWindow(Activity activity) {
		super(activity, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
		this.activity = activity;

		setContentView(activity, R.layout.resource_home_upload);

		findView();
		initView();
		initAnim();
	}
	
	private void findView() {
		closeButton = (ImageButton) findViewById(R.id.btnClose);
		cameraButton = (Button) findViewById(R.id.btnCamera);
		allButton = (Button) findViewById(R.id.btnAll);
		imageButton = (Button) findViewById(R.id.btnImage);
		videoButton = (Button) findViewById(R.id.btnVideo);
		musicButton = (Button) findViewById(R.id.btnMusic);
		documentButton = (Button) findViewById(R.id.btnDocument);
	}
	
	private void initView() {
		closeButton.setOnClickListener(this);
		cameraButton.setOnClickListener(this);
		allButton.setOnClickListener(this);
		imageButton.setOnClickListener(this);
		videoButton.setOnClickListener(this);
		musicButton.setOnClickListener(this);
		documentButton.setOnClickListener(this);
	}
	
	private void initAnim() {
		rotateAnim = ObjectAnimator.ofFloat(closeButton, "rotation", 180 + 45, 0).setDuration(ANIM_TIME + 150);
		rotateExitAnim = rotateAnim.clone();
		rotateExitAnim.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
				exit();
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {}
		});
		scaleXEnterAnim = ObjectAnimator.ofFloat(allButton, "scaleX", 0,1);
		scaleYEnterAnim = ObjectAnimator.ofFloat(allButton, "scaleY", 0,1);
		scaleXExitAnim = ObjectAnimator.ofFloat(allButton, "scaleX",1, 0);
		scaleYExitAnim = ObjectAnimator.ofFloat(allButton, "scaleY",1, 0);
	}
	
	@Override
	public void showAsDropDown(View anchor, int xoff, int yoff) {
		super.showAsDropDown(anchor, xoff, yoff);
		showEnterAnim();
	}
	
	@Override
	public void dismiss() {
		showExitAnim();
	}
	
	private AnimatorSet getEnterScaleAnimatorSet(final View target, long delay) {
		AnimatorSet set = new AnimatorSet();
		set.playTogether(scaleXEnterAnim.clone(), scaleYEnterAnim.clone());
		set.setTarget(target);
		set.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator arg0) {
				target.setScaleX(0);
				target.setScaleY(0);
				target.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onAnimationRepeat(Animator arg0) {
			}
			
			@Override
			public void onAnimationEnd(Animator arg0) {
			}
			
			@Override
			public void onAnimationCancel(Animator arg0) {
			}
		});
		if(delay >0) set.setStartDelay(delay);
		
		return set;
	}
	
	private AnimatorSet getExitScaleAnimatorSet(final View target, long delay) {
		AnimatorSet set = new AnimatorSet();
		set.playTogether(scaleXExitAnim.clone(), scaleYExitAnim.clone());
		set.setTarget(target);
		if(delay >0)  set.setStartDelay(delay);
		
		return set;
	}
	
	private void showEnterAnim() {
		rotateAnim.start();
		
		getEnterScaleAnimatorSet(allButton, 0).start();
		getEnterScaleAnimatorSet(cameraButton, 0).start();
		getEnterScaleAnimatorSet(imageButton, 100).start();
		getEnterScaleAnimatorSet(videoButton, 100).start();
		getEnterScaleAnimatorSet(musicButton, 200).start();
		getEnterScaleAnimatorSet(documentButton, 200).start();
	}


	private void showExitAnim() {
		rotateExitAnim.reverse();
		
		getExitScaleAnimatorSet(allButton, 200).start();
		getExitScaleAnimatorSet(cameraButton, 200).start();
		getExitScaleAnimatorSet(imageButton, 100).start();
		getExitScaleAnimatorSet(videoButton, 100).start();
		getExitScaleAnimatorSet(musicButton, 0).start();
		getExitScaleAnimatorSet(documentButton, 0).start();
	}
	
	private void exit() {
		super.dismiss();
	}

	public void setOnUploadItemClickListener(OnUploadItemClickListener listener) {
		this.uploadItemClickListener = listener;
	}

	@Override
	public void onClick(View v) {
		FileType fileType = null;
		switch (v.getId()) {
		case R.id.btnClose:
			dismiss();
			break;
		case R.id.btnCamera:
			dismiss();
			activity.startActivity(new Intent(activity, CameraActivity.class));
			break;
		case R.id.btnImage:
			fileType = FileType.Image;
			break;
		case R.id.btnVideo:
			fileType = FileType.Video;
			break;
		case R.id.btnMusic:
			fileType = FileType.Audio;
			break;
		case R.id.btnDocument:
			fileType = FileType.Document;
			break;
		case R.id.btnAll:
		default:
			fileType = FileType.All;
			break;
		}

		if (fileType != null) {
			dismiss();
			if (uploadItemClickListener != null) {
				uploadItemClickListener.onUploadItemClick(fileType);
			}
			// activity.startActivity(new Intent(activity,
			// RootUploadActivity.class).setData(Uri.parse(fileType.name())));
		}
	}
}
