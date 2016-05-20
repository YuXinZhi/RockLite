package com.zhntd.nick.rocklite;

import java.util.ArrayList;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.zhntd.nick.rocklite.service.CoreService;
import com.zhntd.nick.rocklite.service.CoreService.MyBinder;
import com.zhntd.nick.rocklite.service.CoreService.OnMusicEventListener;
import com.zhntd.nick.rocklite.service.CoreService.StateChangedListener;
import com.zhntd.nick.rocklite.utils.ImageTools;
import com.zhntd.nick.rocklite.views.CDView;
import com.zhntd.nick.rocklite.views.LrcView;
import com.zhntd.nick.rocklite.views.PagerIndicator;
import com.zhntd.nick.rocklite.views.PlayBgShape;
import com.zhntd.nick.rocklite.views.PlayPageTransformer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayActivity1 extends FragmentActivity implements OnClickListener, StateChangedListener {

	private ServiceConnection mServiceConnection;
	private CoreService mCoreService;
	// 根布局
	private LinearLayout mRootLayout;
	private ImageView mPlayBackImageView; // back button
	private TextView mTitleTextView; // music title
	private TextView mArtistTextView; // singer
	private ViewPager mViewPager; // cd or lrc

	private CDView mCdView; // cd
	private SeekBar mSeekBar; // seekbar
	private ImageButton mStartPlayButton; // start or pause
	private LrcView mLrcViewOnFirstPage; // single line lrc
	private LrcView mLrcViewOnSecondPage; // 7 lines lrc
	private PagerIndicator mPagerIndicator; // indicator

	// 背景进入动画
	private Animation mAnimationFade;

	// 专辑封面和歌词页面
	private ArrayList<View> mPages;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.play_activity_layout);

		if (mRootLayout == null)
			mRootLayout = (LinearLayout) findViewById(R.id.ll_play_container);
		findTop();
		initImageLoader(this);
		initPages();
		initPager();
		initSeekBar();
		// 绑定服务
		bindToService();
		// 启动服务
		startService();

		initAnim();
	}

	void startService() {
		final Intent intent = new Intent();
		intent.setClass(PlayActivity1.this, CoreService.class);
		startService(intent);
	}

	private void initSeekBar() {
		mSeekBar = (SeekBar) findViewById(R.id.sb_play_progress);
		// 动态设置seekbar的margin
		MarginLayoutParams p = (MarginLayoutParams) mSeekBar.getLayoutParams();
		p.leftMargin = (int) (App.sScreenWidth * 0.1);
		p.rightMargin = (int) (App.sScreenWidth * 0.1);

		mSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
	}

	private OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int progress = seekBar.getProgress();
			mCoreService.seek(progress);
			mLrcViewOnFirstPage.onDrag(progress);
			mLrcViewOnSecondPage.onDrag(progress);
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

		}
	};

	@Override
	protected void onResume() {
		bindService(new Intent(this, CoreService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void findTop() {

		// 左上角返回
		mPlayBackImageView = (ImageView) findViewById(R.id.iv_play_back);
		mTitleTextView = (TextView) findViewById(R.id.tv_music_title);
	}

	// 初始化PagerView子页面
	private void initPages() {
		mPages = new ArrayList<View>(2);
		View cdView = View.inflate(this, R.layout.play_pager_item_1, null);
		View lrcView = View.inflate(this, R.layout.play_pager_item_2, null);
		mPages.add(cdView);
		mPages.add(lrcView);
	}

	// 初始化PagerView
	@SuppressWarnings("deprecation")
	private void initPager() {
		// 原点指示器
		mPagerIndicator = (PagerIndicator) findViewById(R.id.pi_play_indicator);
		mPagerIndicator.create(mPages.size());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setPageTransformer(true, new PlayPageTransformer());
		mViewPager.setAdapter(mPagerAapter);
		mViewPager.setOnPageChangeListener(mOnPageChangeListener);
		mViewPager.setAdapter(mPagerAapter);
	}

	private PagerAdapter mPagerAapter = new PagerAdapter() {

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

		@Override
		public int getCount() {
			return mPages.size();
		}

		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mPages.get(position));
			return mPages.get(position);
		};

		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
		};

	};

	private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			if (position == 0) {
				if (mCoreService.isPlaying())
					mCdView.start();
			} else {
				mCdView.pause();
			}
			mPagerIndicator.current(position);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}
	};

	@Override
	public void onPlayStateChanged() {
		updateTrackInfo();
		updateControlButtonBackground();
		updateBackground();
	}

	private void updateTrackInfo() {
		mTitleTextView.setText(mCoreService.getCurrentTitle());
		mArtistTextView.setText(mCoreService.getCurrentArtist());
		Bitmap bmp = mCoreService.getCurrentTrackArt();
		if (bmp == null)
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		mCdView.setImage(ImageTools.scaleBitmap(bmp, (int) (App.sScreenWidth * 0.7)));
	}

	private void updateControlButtonBackground() {
		if (mCoreService.isPlaying()) {
			mCdView.start();
			mStartPlayButton.setImageResource(R.drawable.player_btn_pause_normal);
		} else {
			mCdView.pause();
			mStartPlayButton.setImageResource(R.drawable.player_btn_play_normal);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ib_play_pre:
			mCoreService.playPreviousTrack();
			break;
		case R.id.ib_play_start:
			if (mCoreService.getIsPlaying()) {
				mCoreService.pausePlayer();
			} else {
				mCoreService.resumePlayer();
			}
			break;
		case R.id.ib_play_next:
			mCoreService.playNextTrack();
			break;
		case R.id.iv_play_back:
			finish();
			break;
		default:
			break;
		}
	}

	void bindToService() {
		/**
		 * 应用组件(客户端)可以调用bindService()绑定到一个service．Android系统之后调用service的onBind()
		 * 方法，它返回一个用来与service交互的IBinder．
		 */
		mServiceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName arg0) {

			}

			// 系统调用这个来传送在service的onBind()中返回的IBinder．
			@Override
			public void onServiceConnected(ComponentName arg0, IBinder iBinder) {
				// MyBinder在服务中定义
				MyBinder myBinder = (MyBinder) iBinder;
				mCoreService = myBinder.getService();
				Log.i("music", mCoreService.toString());
				// 告诉服务监听
				mCoreService.setActivityCallback(PlayActivity1.this);
				// 设置音乐进度监听
				mCoreService.setOnMusicEventListener(mMusicEventListener);

				// 服务连接后更新页面更新页面
				onPlayStateChanged();
			}
		};

		final Intent intent = new Intent();
		intent.setClass(PlayActivity1.this, CoreService.class);
		bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

	}

	private void initAnim() {
		mAnimationFade = AnimationUtils.loadAnimation(this, R.anim.fade_in);
	}

	/**
	 * 音乐播放服务回调接口的实现类
	 */
	private OnMusicEventListener mMusicEventListener = new OnMusicEventListener() {
		@Override
		public void onPublish(int progress) {
			mSeekBar.setProgress(progress);
			if (mLrcViewOnFirstPage.hasLrc())
				mLrcViewOnFirstPage.changeCurrent(progress);
			if (mLrcViewOnSecondPage.hasLrc())
				mLrcViewOnSecondPage.changeCurrent(progress);
		}

		@Override
		public void onChange(int position) {
		}
	};

	public static void initImageLoader(Context context) {
		// 自定义图片加载配置
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		ImageLoader.getInstance().init(config);
	}

	@SuppressWarnings("deprecation")
	private void updateBackground() {

		Bitmap bgBitmap = mCoreService.getCurrentTrackArt();
		if (bgBitmap == null) {
			bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		}

		mRootLayout.setBackgroundDrawable(new ShapeDrawable(new PlayBgShape(bgBitmap)));
	}

}
