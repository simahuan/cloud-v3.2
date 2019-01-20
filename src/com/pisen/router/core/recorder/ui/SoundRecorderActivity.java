package com.pisen.router.core.recorder.ui;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.pisen.router.R;
import com.pisen.router.core.recorder.IRecorder;
import com.pisen.router.core.recorder.IRecorder.OnStateChangedListener;
import com.pisen.router.core.recorder.RecorderFile;
import com.pisen.router.core.recorder.SoundRecorder;

/**
 * 录音机主界面
 */
public class SoundRecorderActivity extends Activity implements OnClickListener, OnStateChangedListener {
	private static int MAX_SEEKBAR = 10000;
	private View view; // 主界面
	private Button btnStartRecord, btnPauseRecord, btnStopRecord, btnRecordList;
	private Button btnPlayStart, btnPlayPause; // 播放、暂停按钮
	private TextView txtTimer;
	private SeekBar seekBar;
	private ListView lvPlaybackFiles; // 录音文件ListView
	private long lastClickTime; // 按钮最后点击时间
	private int lastButtonId; // 最后点击Id
	private IRecorder recorder; // 录音机
	private boolean isUiUpdateStoped;// 停止更新Ui: true 不进行录音时间与进度条的更新 || false
										// 进行更新
	private final Handler handler = new Handler();
	/**
	 * 更新进度条
	 */
	private Runnable updateSeekBarRunnable = new Runnable() {
		public void run() {
			if (!isUiUpdateStoped) {
				updateSeekBar();
			}
		}
	};
	/**
	 * 更新Timer
	 */
	private Runnable updateTimerRunnable = new Runnable() {
		@Override
		public void run() {
			if (!isUiUpdateStoped) {
				updateTimer();
			}

		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = LayoutInflater.from(this).inflate(R.layout.recorder_main, null);
		setContentView(view);
		initViews();
		setBtnListener();
		recorder = new SoundRecorder(this);
		recorder.setOnStateChangedListener(this);
		
	}

	private void initViews() {
		btnStartRecord = (Button) findViewById(R.id.recordStart);
		btnPauseRecord = (Button) findViewById(R.id.recordPause);
		btnStopRecord = (Button) findViewById(R.id.recordStop);
		btnRecordList = (Button) findViewById(R.id.recordList);
		btnPlayStart = (Button) findViewById(R.id.playStart);
		btnPlayPause = (Button) findViewById(R.id.playPause);
		txtTimer = (TextView) findViewById(R.id.txtTimer);
		seekBar = (SeekBar) findViewById(R.id.play_seek_bar);
		seekBar.setMax(MAX_SEEKBAR);
		lvPlaybackFiles = (ListView) findViewById(R.id.listViewPlaybackFiles);
		PlaybackFilesAdapter adapter = new PlaybackFilesAdapter(RecorderFile.getPlaybackFiles());
		lvPlaybackFiles.setAdapter(adapter);
		lvPlaybackFiles.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				((SoundRecorder) recorder).getRecorderFile().finalFile = (File) parent.getAdapter().getItem(position);
				recorder.startPlayback(0);
			}
		});
	}

	private void setBtnListener() {
		btnStartRecord.setOnClickListener(this);
		btnPauseRecord.setOnClickListener(this);
		btnStopRecord.setOnClickListener(this);
		btnRecordList.setOnClickListener(this);
		btnPlayStart.setOnClickListener(this);
		btnPlayPause.setOnClickListener(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// 300毫秒内的重复点击,不处理
		if (System.currentTimeMillis() - lastClickTime < 300) {
			// in order to avoid user click bottom too quickly
			return;
		}
		// 按钮非Enable状态 ,不处理
		if (!v.isEnabled()) {
			return;
		}
		// 点击相同按钮不处理，列表按钮除外
		if (v.getId() == lastButtonId && v.getId() != R.id.recordList) {
			// as the recorder state is async with the UI
			// we need to avoid launching the duplicated action
			return;
		}
		if (v.getId() == R.id.recordStop && System.currentTimeMillis() - lastClickTime < 1500) {
			// it seems that the media recorder is not robust enough
			// sometime it crashes when stop recording right after starting
			return;
		}

		lastClickTime = System.currentTimeMillis();
		lastButtonId = v.getId();

		switch (v.getId()) {
		case R.id.recordStart:
			stopAudioPlayback();
			startRecord();
			break;
		case R.id.recordPause:
			pauseRecord();
			break;
		case R.id.recordStop:
			stopRecord();
			break;
		case R.id.recordList:
			showList();
			break;
		case R.id.playStart:
			playStart();
			break;
		case R.id.playPause:
			playPause();
			break;
		}
	}

	/**
	 * 终止音频播放(录音前)
	 */
	private void stopAudioPlayback() {
		Intent i = new Intent("com.android.music.musicservicecommand");
		i.putExtra("command", "pause");
		sendBroadcast(i);
	}

	/**
	 * 开始录音
	 */
	private void startRecord() {
		recorder.startRecording(true);
	}

	/**
	 * 暂停录音
	 */
	private void pauseRecord() {
		recorder.pauseRecording();
	}

	/**
	 * 停止录音
	 */
	private void stopRecord() {
		recorder.stopRecording();
	}

	/**
	 * 显示播放列表
	 */
	private void showList() {
		// openSaveRecordFileDialog();
	}

	/**
	 * 开始播放
	 */
	private void playStart() {
		recorder.startPlayback(0);
	}

	/**
	 * 暂停播放
	 */
	private void playPause() {
		recorder.pausePlayback();
	}

	/**
	 * 更新进度条
	 */
	private void updateSeekBar() {
		if (recorder.getState() == SoundRecorder.PLAYING_STATE) {
			seekBar.setProgress((int) (MAX_SEEKBAR * recorder.getPlayProgress()));
			handler.postDelayed(updateSeekBarRunnable, 50);
		}
	}

	/**
	 * 更新Timer
	 */
	private void updateTimer() {
		if (recorder.getState() == SoundRecorder.RECORDING_STATE) {
			int time = recorder.getProgress();
			txtTimer.setText(time / 60 + ":" + time % 60);
			handler.postDelayed(updateTimerRunnable, 500);
		}

	}

	@Override
	public void onStateChanged(int state) {
		if (state == SoundRecorder.PLAYING_STATE) {
			updateSeekBar();
		}
		if (state == SoundRecorder.RECORDING_STATE) {
			updateTimer();
		}

	}

	@Override
	public void onError(int error) {

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			((SoundRecorder) recorder).stop();
			if (recorder.getState() == recorder.RECORDING_STATE) {
				// 弹出保存文件对话框
				openSaveRecordFileDialog();
			}
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void openSaveRecordFileDialog() {
		new AlertDialog.Builder(this).setTitle(R.string.record_dialog_title)
		// .setMessage(R.string.redord_dialog_message)
				.setPositiveButton(R.string.record_dialog_button_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).setNegativeButton(R.string.redord_dialog_button_cancel, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
	}

}
