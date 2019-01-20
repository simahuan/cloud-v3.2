package com.pisen.router.core.music;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.event.EventListener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Handler;
import android.studio.os.LogCat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.charon.dmc.engine.DLNAContainer;
import com.charon.dmc.engine.MultiPointController;
import com.pisen.router.BuildConfig;
import com.pisen.router.common.utils.NetUtil;
import com.pisen.router.core.playback.MusicPlayback;
import com.pisen.router.core.playback.PlayStatus;

/**
 * dlna音乐播放器
 * 
 * @author ldj
 * @version 1.0 2015年4月15日 下午2:54:16
 */
public class CyberLinkMusicPlayback extends MusicPlayback implements EventListener{

	private MultiPointController controller;
	// 媒体服务器，为dlna设备提供数据
	private PisenMediaServer server;
	// 远端设备
	private static Device remoteDevice;

	// 当前播放歌曲
	private Music curPlayMusic;
	// 当前播放位置
	private String curPlayPosition;
	//音乐总时长
	private String mediaDuration;
	// private String curMusicDuration;
	// 当前播放状态
	private PlayStatus playStatus = PlayStatus.STATE_IDLE;
	// 选中项index
	private int itemSelectedIndex = 0;
	// 获取播放进度
	private static final int WHAT_GET_PROGRESS = 0x6000;
	private static final int WHAT_PLAY_FINISH = 0x6001;
	// 是否进行过跳转请求标识，修复在暂停状态下跳转请求后，不能正常播放问题
	private boolean isSeekCalled;
	private ExecutorService executor = Executors.newSingleThreadExecutor();
	private ExecutorService progressExecutor = Executors.newSingleThreadExecutor();

	/**
	 * 获取dlna播放器实例
	 */
	public CyberLinkMusicPlayback() {
		controller = new MultiPointController();
		server = new PisenMediaServer();
	}

	/**
	 * 设置远程设备
	 * 
	 * @param device
	 */
	public void setRemoteDevice(Device device) {
		remoteDevice = device;
	}

	private static final String AVTransport1 = "urn:schemas-upnp-org:service:AVTransport:1";
	public void subscribe() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(remoteDevice != null && DLNAContainer.getInstance().getControlPoint() != null) {
					DLNAContainer.getInstance().getControlPoint().subscribe(remoteDevice.getService(AVTransport1));
					DLNAContainer.getInstance().getControlPoint().addEventListener(CyberLinkMusicPlayback.this);
				}}
		}).start();
	}

	public void unsubscribe() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if(remoteDevice != null && DLNAContainer.getInstance().getControlPoint() != null) {
					DLNAContainer.getInstance().getControlPoint().unsubscribe();
					DLNAContainer.getInstance().getControlPoint().removeEventListener(CyberLinkMusicPlayback.this);
				}
			}
		}).start();
	}

	public Device getRemoteDevice() {
		return remoteDevice;
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int what = msg.what;

			switch (what) {
			case WHAT_GET_PROGRESS:
				getProgress();
				startAutoGetProgress();
				break;
			case WHAT_PLAY_FINISH:
				next();
				break;
			default:
				break;
			}
		};
	};

	/**
	 * 停止播放，并回收资源
	 */
	public void release() {
		unsubscribe();
		stopPlay();
		stopAutoGetProgress();
		progressExecutor.shutdown();
	}

	@Override
	public void startPlay() {
		// 停止获取进度
		stopAutoGetProgress();
		progressExecutor.shutdownNow();

		executor.execute(new Runnable() {

			@Override
			public void run() {
				if (!server.isRunning()) {
					server.start();
				}

				if (remoteDevice == null) {
					if (listener != null) {
						listener.onError(PlayStatus.ERROR_OPERATE_FAILED,"操作失败");
					}
					return;
				}

				if (playStatus == PlayStatus.STATE_IDLE) {// 开始播放
					play();
				} else if (playStatus == PlayStatus.STATE_PLAYING) {//正在播放时切歌，首先停止当前歌曲，成功后进行播放
					boolean result = controller.stop(remoteDevice);
					if (result) {
						//开始播放
						playStatus = PlayStatus.STATE_IDLE;
						play();
					} else {
						if (listener != null) {
							listener.onError(PlayStatus.ERROR_OPERATE_FAILED,"操作失败");
						}
					}
				} else if (playStatus == PlayStatus.STATE_PAUSED) {// 继续播放
					goon();
				}
			}
		});
	}

	/**
	 * 播放音乐
	 */
	private void play() {
		if (remoteDevice == null) return;
		//重置播放时长
		mediaDuration = "";
		curPlayMusic = playList.get(itemSelectedIndex);
		if (curPlayMusic != null) {
			String path = curPlayMusic.getSavePath();
			boolean result = false;
			Log.e("play", "url->" + path);
			if (path.startsWith("http")) {
				result = controller.play(remoteDevice, path);
			} else {
				result = controller.play(remoteDevice, getLocalUrl(path));
			}
			if (result) {
				if (BuildConfig.DEBUG) LogCat.d("play succeed->" + curPlayMusic.getMusicName());
				playStatus = PlayStatus.STATE_PLAYING;
				startAutoGetProgress();
				if (listener != null) {
					listener.onPlay();
				}
			} else {
				if (BuildConfig.DEBUG)
					LogCat.d("play failed->" + curPlayMusic.getMusicName());
				if (listener != null) {
					listener.onError(PlayStatus.ERROR_OPERATE_FAILED,"播放失败");
				}
			}
		}
	}

	/**
	 * 继续播放
	 */
	private void goon() {
		if (remoteDevice == null)
			return;
		boolean result = false;
		if (isSeekCalled) {
			isSeekCalled = false;
			result = controller.pause(remoteDevice) && controller.goon(remoteDevice, curPlayPosition);
		} else {
			result = controller.goon(remoteDevice, curPlayPosition);
		}

		if (result) {
			startAutoGetProgress();
			playStatus = PlayStatus.STATE_PLAYING;
			if (listener != null) {
				listener.onPlay();
			}
		} else {
			if (listener != null) {
				listener.onError(PlayStatus.ERROR_OPERATE_FAILED,"播放失败");
			}
		}
	}

	@Override
	public void startPlayByIndex(int playIndex) {
		if (!isPlaying() || playIndex != itemSelectedIndex) {
			itemSelectedIndex = playIndex;
			startPlay();
		}
	}

	@Override
	public void seekTo(final int msec) {
		executor.execute(new Runnable() {

			@Override
			public void run() {
				if (remoteDevice == null)
					return;
				boolean result = controller.seek(remoteDevice, MusicPlaybackUtil.secToTime(msec / 1000));
				if (BuildConfig.DEBUG) LogCat.d("seek to " + MusicPlaybackUtil.secToTime(msec / 1000));
				if (result) {
					if (BuildConfig.DEBUG) LogCat.d("seek succeed!!");
					isSeekCalled = true;
//					if( playStatus == PlayStatus.STATE_STOP) {
//						startPlay();
//					}else if( playStatus == PlayStatus.STATE_PAUSED) {
//						isSeekCalled = true;
//						goon();
//					}
				} else {
					if (BuildConfig.DEBUG)
						LogCat.d("seek failed!!");
					if (listener != null) {
						listener.onError(PlayStatus.ERROR_OPERATE_FAILED,"操作失败");
					}
				}
			}
		});
	}

	@Override
	public void pausePlay() {
		executor.execute(new Runnable() {

			@Override
			public void run() {
				if (remoteDevice == null)
					return;
				boolean result = controller.pause(remoteDevice);
				if (result) {
					// 停止获取进度
					stopAutoGetProgress();
					playStatus = PlayStatus.STATE_PAUSED;
					if (listener != null) {
						listener.onPaused();
					}
				} else {
					if (listener != null) {
						listener.onError(PlayStatus.ERROR_OPERATE_FAILED,"操作失败");
					}
				}
			}
		});
	}

	@Override
	public void stopPlay() {
		executor.execute(new Runnable() {

			@Override
			public void run() {
				if (playStatus == PlayStatus.STATE_PLAYING) {
					if (remoteDevice == null) {
						return;
					}
					boolean result = controller.stop(remoteDevice);
					if (result) {
						// 停止获取进度
//						stopAutoGetProgress();
						playStatus = PlayStatus.STATE_IDLE;
						if (listener != null) {
							listener.onStopped();
						}
					} else {
						if (listener != null) {
							listener.onError(PlayStatus.ERROR_OPERATE_FAILED,"操作失败");
						}
					}
				}
			}
		});
	}

	@Override
	public boolean isPlaying() {
		return playStatus == PlayStatus.STATE_PLAYING;
	}

	@Override
	public int getCurrentPosition() {
		return MusicPlaybackUtil.getIntLength(curPlayPosition) * 1000;
	}

	@Override
	public int getDuration() {
		return MusicPlaybackUtil.getIntLength(mediaDuration) * 1000;
	}

	@Override
	public void next() {
		itemSelectedIndex = getNextIndex();
		startPlay();
	}

	@Override
	public void prev() {
		itemSelectedIndex = getPreIndex();
		startPlay();
	}

	/**
	 * 获取播放进度
	 */
	private void getProgress() {
		progressExecutor.execute(new Runnable() {
			public void run() {
				if (remoteDevice == null) return;
				curPlayPosition = controller.getPositionInfo(remoteDevice);
				int positon = MusicPlaybackUtil.getIntLength(curPlayPosition);

				if (positon <=0) {
					if (BuildConfig.DEBUG)
						LogCat.d("media positon is 0!!!");
					return;
				}

				if(TextUtils.isEmpty(mediaDuration) || MusicPlaybackUtil.getIntLength(mediaDuration) <=0) {
					mediaDuration = controller.getMediaDuration(remoteDevice);
					if (BuildConfig.DEBUG)
						LogCat.d("getMediaDuration ->" + mediaDuration);
				}else {
					if (BuildConfig.DEBUG)
						LogCat.d("old media Duration->" + mediaDuration);
				}
				int duration = MusicPlaybackUtil.getIntLength(mediaDuration);
				if (duration <=0) {
					if (BuildConfig.DEBUG)
						LogCat.d("media Duration is 0!!!");
					return;
				}
				if (BuildConfig.DEBUG)
					LogCat.d("position-->" + positon + "/" + duration);
				if (listener != null) {
					listener.onProgressUpdate(positon * 100 / duration, convertTime(curPlayPosition), convertTime(mediaDuration));
				}
				if (duration > 0 && positon >= duration) {
					stopAutoGetProgress();
					// handler.sendEmptyMessage(WHAT_PLAY_FINISH);//关闭自动播放下一首功能
					if (listener != null) {
						listener.onCompleted();
					}
				}

			}
		});
	}

	private String convertTime(String time) {
		if (!TextUtils.isEmpty(time)) {
			if (time.startsWith("00")) {
				time = time.substring(3);
			} else if (time.startsWith("0:")) {
				time = time.substring(2);
			}
		}

		return time;
	}

	/**
	 * 进行自动获取进度
	 */
	private void startAutoGetProgress() {
		if(progressExecutor == null || progressExecutor.isShutdown()) {
			progressExecutor = Executors.newSingleThreadExecutor();
		}
		handler.sendEmptyMessageDelayed(WHAT_GET_PROGRESS, 1000);
	}

	/**
	 * 停止自动获取进度
	 */
	private void stopAutoGetProgress() {
		handler.removeMessages(WHAT_GET_PROGRESS);
	}

	/**
	 * 获取本地音频文件外部访问目录
	 * 
	 * @param musicPath
	 * @return
	 */
	private String getLocalUrl(String musicPath) {
		if (!TextUtils.isEmpty(musicPath) && server != null) {
			musicPath = String.format("http://%s:%s%s%s", NetUtil.getLocalIpAddressString(), server.getHTTPPort(), PisenMediaServer.PRE_PATH,
					MusicPlaybackUtil.encodeString(musicPath));
		}
		return musicPath;
	}

	@Override
	public int getItemSelectedIndex() {
		return itemSelectedIndex;
	}

	@Override
	public void setVolume(final float progress) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (progress >= 0 && progress <= 1 && remoteDevice != null && controller != null) {
					int maxVolume = controller.getMaxVolumeValue(remoteDevice);
					LogCat.i("maxVolume:" + maxVolume);
					boolean res = controller.setVoice(remoteDevice, (int) ( maxVolume * progress));
					LogCat.i("setVolume result->" + res);
				}
			}
		}).start();
	}

	@Override
	public void eventNotifyReceived(String paramString1, long paramLong, String paramString2, String paramString3) {
		parseXML(paramString3);
	}

	private void parseXML(String xmlMsg) {
//		Log.e("eventNotifyReceived", xmlMsg);
		if(!TextUtils.isEmpty(xmlMsg)) {
			XmlPullParser parser = Xml.newPullParser();
			try {
				parser.setInput(new ByteArrayInputStream(xmlMsg.getBytes()), null);
				int event = parser.getEventType();
				while (event != XmlPullParser.END_DOCUMENT) {
					switch (event) {
					case XmlPullParser.START_DOCUMENT:
						break;
					case XmlPullParser.START_TAG:
						if ("AVTransportURI".equals(parser.getName())) {
							String value = parser.getAttributeValue(0);
							if(TextUtils.isEmpty(value) || !isLocalMusicPlay(value)) {//解决同时推送状态监听问题
								stopPlay();
								stopAutoGetProgress();
								progressExecutor.shutdown();
								if(listener != null) {
									listener.onError(PlayStatus.ERROR_OTHERCLIENT_PUSH,"推送失败");
								}
							}
							return;
						}
						break;
					}

					event = parser.next();
				}
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 判断是否播放的本机音乐
	 * @param value
	 * @return
	 */
	private boolean isLocalMusicPlay(String value) {
		boolean result = false;
		if(!TextUtils.isEmpty(value)) {
			try {
				String regexString = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
				Pattern p = Pattern.compile(regexString);
				Matcher m = p.matcher(value);
				boolean find = m.find();
				if (find) {
					String ip = m.group();
					if (!TextUtils.isEmpty(ip) && NetUtil.getLocalIpAddressString().equals(ip)) {
						result = true;
					}
				}
			} catch (Exception e) {
			}
		}
		return result;
	}
}
