package com.zhntd.nick.rocklite.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhntd.nick.rocklite.MainActivity;
import com.zhntd.nick.rocklite.Project;
import com.zhntd.nick.rocklite.R;
import com.zhntd.nick.rocklite.modle.Track;
import com.zhntd.nick.rocklite.receiver.TrackNextReceiver;
import com.zhntd.nick.rocklite.receiver.TrackPlayReceiver;
import com.zhntd.nick.rocklite.utils.QuerTools;
import com.zhntd.nick.rocklite.views.BitmapToBlur;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * 主要服务
 *
 */
public class CoreService extends Service {

	public static final String ACTION_NEXT_TRACK = "com.zhntd.nick.player.next";
	public static final String ACTION_PLAY_TRACK = "com.zhntd.nick.player.play";

	private MyBinder myBinder = new MyBinder();
	private StateChangedListener mStateChangedListener;
	private final MediaPlayer mediaPlayer = new MediaPlayer();
	// 播放列表
	private List<Track> mPlayList;

	// 当前歌曲在列表中的位置
	private int mCursor;

	// 电话状态监听器
	BroadcastReceiver mHeadSetPlugBroadcastReceiver, mPhoneStateChangeListener, mNotiControlReceiver;

	private RemoteViews mRemoteView;
	// intent
	private Intent playIntent, nextIntent;
	private PendingIntent playPendingIntent, nextPendingIntent;
	// notification
	private NotificationManager mNotificationManager;
	private Notification mNotification;
	private int NOTI_ID = 1;

	private Activity mActivityCallback;

	private QuerTools mQuerTools;

	// 设置歌曲列表当前歌曲
	public void setCurrentCursor(int cursor) {
		this.mCursor = cursor;
	}

	public int getCurrentCursor() {
		return this.mCursor;
	}

	public String getCurrentTitle() {
		if (mPlayList != null)
			return mPlayList.get(mCursor).getTitle();
		return null;
	}

	/**
	 * @return
	 */
	public int getPlayListSize() {
		return mPlayList.size();
	}

	/**
	 * @return
	 */
	public String getCurrentAlbumPath() {
		String priFix = "content://media/external/audio/albumart";
		if (mPlayList != null)
			return priFix + File.separator + mPlayList.get(mCursor).getAlbumId();
		return null;
	}

	public long getCurrentAlbumId() {
		if (mPlayList != null) {
			return mPlayList.get(mCursor).getAlbumId();
		}
		return -1;
	}

	public long getCurrentDuration() {
		if (mPlayList != null) {
			return mPlayList.get(mCursor).getDuration();
		}
		return -1;
	}

	/**
	 * @return
	 */
	public Bitmap getCurrentTrackArt() {

		return ImageLoader.getInstance().loadImageSync(getCurrentAlbumPath());
	}

	// 背景图片
	public Drawable getBluredCurrentArt() {
		Bitmap bm = getCurrentTrackArt();
		if (bm == null) {
			bm = BitmapFactory.decodeResource(getResources(), R.drawable.default_artist);
		}
		return BitmapToBlur.BoxBlurFilter(bm);
	}

	public String getCurrentArtist() {
		if (mPlayList != null)
			return mPlayList.get(mCursor).getArtist();
		return null;
	}

	/**
	 * @return
	 */
	public List<Track> getPlayList() {
		return this.mPlayList;
	}

	/**
	 * @param playList
	 */
	public void setupPLayList(List<Track> playList) {
		if (playList != null && playList.size() > 0) {
			this.mPlayList = playList;
		}
	}

	/**
	 * @return
	 */
	public Uri getCurrentAlbumUri() {
		if (mPlayList != null) {
			String urlPrefix = "content://media/external/audio/albumart";
			String urlString = urlPrefix + File.separator + getPlayList().get(getCurrentCursor()).getAlbumId();
			return Uri.parse(urlString);
		}
		return null;
	}

	public String getCurrentFilePath() {
		if (mPlayList != null)
			return mPlayList.get(mCursor).getUrl();
		return null;
	}

	public void playTrack(int cursor) {

		mediaPlayer.reset();
		try {
			mediaPlayer.setDataSource(mPlayList.get(cursor).getUrl());
			mediaPlayer.prepare();
			mediaPlayer.start();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		onStateChanged();
		updateNotification();
	}

	public void pausePlayer() {
		mediaPlayer.pause();
		onStateChanged();
		updateNotification();
	}

	public void resumePlayer() {
		mediaPlayer.start();
		onStateChanged();
		updateNotification();
	}

	public void playNextTrack() {

		int listSize = mPlayList.size();
		if (mCursor == listSize - 1) {
			setCurrentCursor(0);
		} else {
			setCurrentCursor(mCursor + 1);
		}
		playTrack(mCursor);
	}

	/**
	 * @return
	 */
	public long getCurrentSongId() {
		if (mPlayList != null)
			return mPlayList.get(mCursor).getId();
		return -1;
	}

	/**
	 * previous
	 */
	public void playPreviousTrack() {

		int listSize = mPlayList.size();
		if (mCursor == 0) {
			setCurrentCursor(listSize - 1);
		} else {
			setCurrentCursor(mCursor - 1);
		}
		playTrack(mCursor);
	}

	/**
	 * stop
	 */
	public void stopPLayer() {
		mediaPlayer.stop();
		mediaPlayer.release();
		onStateChanged();
		updateNotification();
	}

	/**
	 * @return
	 */
	public boolean getIsPlaying() {
		return mediaPlayer.isPlaying();
	}

	/**
	 * 收藏按钮被按下的回调
	 * 
	 * @return
	 */
	public boolean onPraisedBtnPressed() {

		boolean hadPraised = mQuerTools.checkIfHasAsFavourite(getCurrentSongId(), Project.DB_PRAISED_NAME,
				Project.TB_PRAISED_NAME, 1);

		if (!hadPraised) {
			addCurrentToDataBase();
			return true;
		} else {
			mQuerTools.removeTrackFrmDatabase(getCurrentSongId(), Project.DB_PRAISED_NAME, Project.TB_PRAISED_NAME, 1);
			return false;
		}

	}

	/**
	 * @return
	 */
	public boolean checkIfPraised() {
		return mQuerTools.checkIfHasAsFavourite(getCurrentSongId(), Project.DB_PRAISED_NAME, Project.TB_PRAISED_NAME,
				1);
	}

	/**
	 * add to.
	 */
	private void addCurrentToDataBase() {

		ContentValues values = new ContentValues();
		values.put("TITLE", getCurrentTitle());
		values.put("ARTIST", getCurrentArtist());
		values.put("PATH", getCurrentFilePath());
		values.put("SONG_ID", getCurrentSongId());
		values.put("ALBUM_ID", getCurrentAlbumId());
		values.put("DURATION", getCurrentDuration());
		mQuerTools.addToDb(values, Project.DB_PRAISED_NAME, Project.TB_PRAISED_NAME, 1);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return myBinder;
	}

	public void setActivityCallback(MainActivity activity) {
		// 监听activity状态变化
		mStateChangedListener = activity;
		mActivityCallback = activity;
	}

	@Override
	public void onCreate() {
		initHeadPluggedListener();
		initPhoneStateChangeListener();
		setNotiControlReceiver();
		initNotification();
		mediaPlayer.setOnCompletionListener(mOnCompletionListener);
		mQuerTools = new QuerTools(this);
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mHeadSetPlugBroadcastReceiver);
		unregisterReceiver(mPhoneStateChangeListener);
		unregisterReceiver(mNotiControlReceiver);
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	/*--------------------------------------------*/
	/*-----------------notification---------------*/
	/*--------------------------------------------*/

	private void initNotification() {

		mRemoteView = new RemoteViews(getPackageName(), R.layout.layout_notification);

		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setContent(mRemoteView)
				.setContentTitle(getCurrentTitle()).setContentText(getCurrentTitle()).setTicker(getCurrentTitle())
				.setSmallIcon(R.drawable.ic_launcher).setOngoing(true);

		if (playIntent == null) {
			playIntent = new Intent(this, TrackPlayReceiver.class);
		}
		if (nextIntent == null) {
			nextIntent = new Intent(this, TrackNextReceiver.class);
		}
		if (playPendingIntent == null) {
			playPendingIntent = PendingIntent.getBroadcast(this, 0, playIntent, 0);
		}
		if (nextPendingIntent == null) {
			nextPendingIntent = PendingIntent.getBroadcast(this, 0, nextIntent, 0);
		}

		// onClick
		mRemoteView.setOnClickPendingIntent(R.id.btn_noti_next, nextPendingIntent);
		mRemoteView.setOnClickPendingIntent(R.id.btn_noti_pause, playPendingIntent);
		// manager
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = mBuilder.build();
		mNotificationManager.notify(NOTI_ID, mNotification);
	}

	/**
	 * update notification content
	 */
	public void updateNotification() {

		Log.i("music", "update notification" + getCurrentCursor());

		mRemoteView.setTextViewText(R.id.noti_title, getCurrentTitle());

		if (getIsPlaying()) {
			mRemoteView.setImageViewResource(R.id.btn_noti_pause, R.drawable.notification_pause);
		} else {
			mRemoteView.setImageViewResource(R.id.btn_noti_pause, R.drawable.notification_play);
		}

		mRemoteView.setImageViewUri(R.id.iv_art_noti, getCurrentAlbumUri());

		mNotificationManager.notify(NOTI_ID, mNotification);
	}

	public void cancelNoti() {
		mNotificationManager.cancel(NOTI_ID);
	}

	/**
	 * initial broadcast receiver for notification
	 */
	private void setNotiControlReceiver() {

		mNotiControlReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				String action = intent.getAction();

				if (action.equals(ACTION_NEXT_TRACK)) {
					playNextTrack();
				}
				if (action.equals(ACTION_PLAY_TRACK)) {
					if (getIsPlaying()) {
						pausePlayer();
					} else {
						resumePlayer();
					}
				}
			}
		};

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_NEXT_TRACK);
		filter.addAction(ACTION_PLAY_TRACK);
		registerReceiver(mNotiControlReceiver, filter);
	}

	public class MyBinder extends Binder {
		public CoreService getService() {
			return CoreService.this;
		}
	}

	final class BlurImageCreater extends AsyncTask<Void, Void, Drawable> {

		@Override
		protected Drawable doInBackground(Void... arg0) {

			return getBluredCurrentArt();
		}

		@Override
		protected void onPostExecute(Drawable result) {

			notifyBlurIsReady(result);
			super.onPostExecute(result);
		}
	}

	public interface StateChangedListener {
		void onPlayStateChanged();
	}

	public OnCompletionListener mOnCompletionListener = new OnCompletionListener() {

		@Override
		public void onCompletion(MediaPlayer mp) {
			playNextTrack();
		}
	};

	// 监听耳机插入状态
	private void initHeadPluggedListener() {

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);

		mHeadSetPlugBroadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				if (mediaPlayer.isPlaying())
					pausePlayer();
			}

		};

		registerReceiver(mHeadSetPlugBroadcastReceiver, intentFilter);
	}

	private void initPhoneStateChangeListener() {
		/**
		 * 动态注册IntentFilter，监听通话状态
		 */
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_CALL);
		intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
		intentFilter.addAction(Intent.ACTION_DIAL);

		mPhoneStateChangeListener = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				pausePlayer();
			}

		};

		registerReceiver(mPhoneStateChangeListener, intentFilter);
	}

	void onStateChanged() {
		mStateChangedListener.onPlayStateChanged();
		new BlurImageCreater().execute();
	}

	private void notifyBlurIsReady(Drawable drawable) {

		// if (mFragments == null) {
		// mFragments = mActivityCallback.getPages();
		// }
		// for (int i = 0; i < mFragments.size(); i++) {
		// mFragments.get(i).onBlurReady(drawable);
		// }

		((MainActivity) mActivityCallback).onBlurReady(drawable);
	}

	/************************** PlayActivity **************************/
	/**
	 * 音乐播放回调接口
	 * 
	 */
	public interface OnMusicEventListener {
		public void onPublish(int percent);

		public void onChange(int position);
	}

	private OnMusicEventListener mListener;
	/**
	 * 更新进度的线程
	 */
	private Runnable mPublishProgressRunnable = new Runnable() {
		@Override
		public void run() {
			for (;;) {
				if (mediaPlayer != null && mediaPlayer.isPlaying() && mListener != null) {
					mListener.onPublish(mediaPlayer.getCurrentPosition());
				}
				SystemClock.sleep(200);
			}
		}
	};

	/**
	 * 设置回调
	 */
	public void setOnMusicEventListener(OnMusicEventListener l) {
		mListener = l;
	}

	/**
	 * 获取正在播放的位置
	 * @return
	 */
	public int getPlayingPosition() {
		return mCursor;
	}
	
}
