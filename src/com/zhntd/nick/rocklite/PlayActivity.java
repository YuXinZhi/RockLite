package com.zhntd.nick.rocklite;

import java.util.ArrayList;

import com.zhntd.nick.rocklite.utils.ImageTools;
import com.zhntd.nick.rocklite.utils.MediaUtils;
import com.zhntd.nick.rocklite.views.CDView;
import com.zhntd.nick.rocklite.views.LrcView;
import com.zhntd.nick.rocklite.views.PagerIndicator;
import com.zhntd.nick.rocklite.views.PlayBgShape;
import com.zhntd.nick.rocklite.views.PlayPageTransformer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayActivity extends BaseActivity implements OnClickListener {

	private LinearLayout mPlayContainer;
	private ImageView mPlayBackImageView; // 后退按钮
	private TextView mMusicTitle;
	private ViewPager mViewPager; // 歌曲封面或歌词
	private CDView mCdView; // cd
	private SeekBar mPlaySeekBar; // seekbar
	private ImageButton mStartPlayButton; // start or pause
	private TextView mSingerTextView; // singer
	private LrcView mLrcViewOnFirstPage; // 第一页的一行歌词
	private LrcView mLrcViewOnSecondPage; // 7 lines lrc
	private PagerIndicator mPagerIndicator; // 翻页的指示器

	private  int mScreenWidth;

	// PagerView的两个页面
	private ArrayList<View> mViewPagerContent = new ArrayList<View>(2);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.play_activity_layout);
		MeasureScreen();
		setupViews();
	}

	@Override
	protected void onResume() {
		super.onResume();
		allowBindService();
	}

	@Override
	protected void onPause() {
		allowUnbindService();
		super.onPause();
	}

	private void setupViews() {
		mPlayContainer = (LinearLayout) findViewById(R.id.ll_play_container);
		mPlayBackImageView = (ImageView) findViewById(R.id.iv_play_back);
		mMusicTitle = (TextView) findViewById(R.id.tv_music_title);
		mViewPager = (ViewPager) findViewById(R.id.vp_play_container);
		mPlaySeekBar = (SeekBar) findViewById(R.id.sb_play_progress);
		mStartPlayButton = (ImageButton) findViewById(R.id.ib_play_start);
		mPagerIndicator = (PagerIndicator) findViewById(R.id.pi_play_indicator);

		// 动态设置seekbar的margin

		MarginLayoutParams p = (MarginLayoutParams) mPlaySeekBar.getLayoutParams();
		p.leftMargin = (int) (mScreenWidth * 0.1);
		p.rightMargin = (int) (mScreenWidth * 0.1);

		// 设置进度条监听
		mPlaySeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

		initViewPagerContent();
		mViewPager.setPageTransformer(true, new PlayPageTransformer());
		// 设置原点指示器的数量
		mPagerIndicator.create(mViewPagerContent.size());
		mViewPager.setOnPageChangeListener(mPageChangeListener);
		mViewPager.setAdapter(mPagerAdapter);

		mPlayBackImageView.setOnClickListener(this);
	}

	// 初始化PagerView
	private void initViewPagerContent() {
		// 圆形封面
		View cd = View.inflate(this, R.layout.play_pager_item_1, null);
		mCdView = (CDView) cd.findViewById(R.id.play_cdview);
		mSingerTextView = (TextView) cd.findViewById(R.id.play_singer);
		mLrcViewOnFirstPage = (LrcView) cd.findViewById(R.id.play_first_lrc);

		View lrcView = View.inflate(this, R.layout.play_pager_item_2, null);
		mLrcViewOnSecondPage = (LrcView) lrcView.findViewById(R.id.play_first_lrc_2);

		mViewPagerContent.add(cd);
		mViewPagerContent.add(lrcView);
	}

	@Override
	public void onPublish(int progress) {

	}

	@Override
	public void onChange(int position) {
		setBackground(position);
		onPlay();
		setLrc(position);
	}

	private void setLrc(int position) {
		// Track track = M;
		String lrcPath = MediaUtils.getLrcDir() + mPlayService.getCurrentTitle() + ".lrc";
		mLrcViewOnFirstPage.setLrcPath(lrcPath);
		mLrcViewOnSecondPage.setLrcPath(lrcPath);
	}

	/**
	 * 播放时调用 主要设置显示当前播放音乐的信息
	 * 
	 * @param position
	 */
	private void onPlay() {
		// Music music = MusicUtils.sMusicList.get(position);

		mMusicTitle.setText(mPlayService.getCurrentTitle());
		mSingerTextView.setText(mPlayService.getCurrentArtist());
		mPlaySeekBar.setMax((int) mPlayService.getCurrentDuration());
		Bitmap bmp = mPlayService.getCurrentTrackArt();
		if (bmp == null)
			bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		mCdView.setImage(ImageTools.scaleBitmap(bmp, (int) (mScreenWidth * 0.7)));

		if (mPlayService.isPlaying()) {
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

		case R.id.iv_play_back:
			// 左上角返回按钮
			finish();
			break;
		default:
			break;
		}
	}

	/**
	 * 拖动进度条
	 */
	private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			// 当进度条停止拖动时更新歌曲信息
			int progress = seekBar.getProgress();
			mPlayService.seek(progress);
			mLrcViewOnFirstPage.onDrag(progress);
			mLrcViewOnSecondPage.onDrag(progress);
		}
	};

	/**
	 * 测量屏幕
	 */

	private void MeasureScreen() {
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		mScreenWidth = dm.widthPixels;
	}

	// PageView监听器
	private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			if (position == 0) {
				// 当歌曲正在播放时圆形封面旋转
				if (mPlayService.isPlaying())
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

	// PageView适配器
	private PagerAdapter mPagerAdapter = new PagerAdapter() {
		@Override
		public int getCount() {
			return mViewPagerContent.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mViewPagerContent.get(position));
			return mViewPagerContent.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}
	};

	private void setBackground(int position) {
		Bitmap bgBitmap = mPlayService.getCurrentTrackArt();
		if (bgBitmap == null) {
			bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		}
		mPlayContainer.setBackgroundDrawable(new ShapeDrawable(new PlayBgShape(bgBitmap)));
	}

	// 上一曲
	public void pre(View view) {
		mPlayService.playPreviousTrack();
	}

	// 下一曲
	public void next(View view) {
		mPlayService.playNextTrack();
	}

	// 播放/暂停
	public void play(View view) {
		if (mPlayService.isPlaying()) {
			mPlayService.pausePlayer();
			; // 暂停
			mCdView.pause();
			mStartPlayButton.setImageResource(R.drawable.player_btn_play_normal);
		} else {
			onPlay(); // 播放
		}
	}

}
