package com.zhntd.nick.rocklite;

import java.util.ArrayList;

import com.zhntd.nick.rocklite.modle.Track;
import com.zhntd.nick.rocklite.service.CoreService.StateChangedListener;
import com.zhntd.nick.rocklite.utils.ImageTools;
import com.zhntd.nick.rocklite.utils.MediaUtils;
import com.zhntd.nick.rocklite.views.CDView;
import com.zhntd.nick.rocklite.views.LrcView;
import com.zhntd.nick.rocklite.views.PagerIndicator;
import com.zhntd.nick.rocklite.views.PlayBgShape;
import com.zhntd.nick.rocklite.views.PlayPageTransformer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * liteplayer by loader
 * 
 * @author qibin
 */
public class PlayActivity2 extends BaseActivity implements OnClickListener, StateChangedListener {
	private LinearLayout mPlayContainer;
	private ImageView mPlayBackImageView; // back button
	private TextView mMusicTitle; // music title
	private ViewPager mViewPager; // cd or lrc
	private CDView mCdView; // cd
	private SeekBar mPlaySeekBar; // seekbar
	private ImageButton mStartPlayButton; // start or pause
	private TextView mSingerTextView; // singer
	private LrcView mLrcViewOnFirstPage; // single line lrc
	private LrcView mLrcViewOnSecondPage; // 7 lines lrc
	private PagerIndicator mPagerIndicator; // indicator

	// cd view and lrc view
	private ArrayList<View> mViewPagerContent = new ArrayList<View>(2);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.play_activity_layout);
		setupViews();
		// allowBindService();
	}

	/**
	 * ��ʼ��view
	 */
	private void setupViews() {
		mPlayContainer = (LinearLayout) findViewById(R.id.ll_play_container);
		mPlayBackImageView = (ImageView) findViewById(R.id.iv_play_back);
		mMusicTitle = (TextView) findViewById(R.id.tv_music_title);
		mViewPager = (ViewPager) findViewById(R.id.vp_play_container);
		mPlaySeekBar = (SeekBar) findViewById(R.id.sb_play_progress);
		mStartPlayButton = (ImageButton) findViewById(R.id.ib_play_start);
		mPagerIndicator = (PagerIndicator) findViewById(R.id.pi_play_indicator);

		// ��̬����seekbar��margin
		MarginLayoutParams p = (MarginLayoutParams) mPlaySeekBar.getLayoutParams();
		p.leftMargin = (int) (App.sScreenWidth * 0.1);
		p.rightMargin = (int) (App.sScreenWidth * 0.1);

		mPlaySeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

		initViewPagerContent();
		mViewPager.setPageTransformer(true, new PlayPageTransformer());
		mPagerIndicator.create(mViewPagerContent.size());
		mViewPager.setOnPageChangeListener(mPageChangeListener);
		mViewPager.setAdapter(mPagerAdapter);

		mPlayBackImageView.setOnClickListener(this);
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

	private OnPageChangeListener mPageChangeListener = new OnPageChangeListener() {
		@Override
		public void onPageSelected(int position) {
			if (position == 0) {
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

	/**
	 * �϶�������
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
			int progress = seekBar.getProgress();
			mPlayService.seek(progress);
			mLrcViewOnFirstPage.onDrag(progress);
			mLrcViewOnSecondPage.onDrag(progress);
		}
	};

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

	/**
	 * ��ʼ��viewpager������
	 */
	private void initViewPagerContent() {
		View cd = View.inflate(this, R.layout.play_pager_item_1, null);
		mCdView = (CDView) cd.findViewById(R.id.play_cdview);
		mSingerTextView = (TextView) cd.findViewById(R.id.play_singer);
		mLrcViewOnFirstPage = (LrcView) cd.findViewById(R.id.play_first_lrc);

		View lrcView = View.inflate(this, R.layout.play_pager_item_2, null);
		mLrcViewOnSecondPage = (LrcView) lrcView.findViewById(R.id.play_first_lrc_2);

		mViewPagerContent.add(cd);
		mViewPagerContent.add(lrcView);
	}

	private void setBackground(int position) {
		Bitmap bgBitmap = mPlayService.getCurrentTrackArt();
		if (bgBitmap == null) {
			bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		}

		mPlayContainer.setBackgroundDrawable(new ShapeDrawable(new PlayBgShape(bgBitmap)));
	}

	/**
	 * ��һ��
	 * 
	 * @param view
	 */
	public void pre(View view) {
		// mPlayService.pre(); // ��һ��
		mPlayService.playPreviousTrack();
		updateTrackInfo();
	}

	/**
	 * ���� or ��ͣ
	 * 
	 * @param view
	 */
	public void play(View view) {
		if (mPlayService.isPlaying()) {
			// mPlayService.pause(); // ��ͣ
			mPlayService.pausePlayer();
			mCdView.pause();
			mStartPlayButton.setImageResource(R.drawable.player_btn_play_normal);
		} else {
			onPlay(mPlayService.resume()); // ����
		}
	}

	/**
	 * ��һ��
	 * 
	 * @param view
	 */
	public void next(View view) {
		mPlayService.playNextTrack(); // ��һ��
		updateTrackInfo();
	}

	/**
	 * ����ʱ���� ��Ҫ������ʾ��ǰ�������ֵ���Ϣ
	 * 
	 * @param position
	 */
	private void onPlay(int position) {
		// Music music = MusicUtils.sMusicList.get(position);
		mMusicTitle.setText(mPlayService.getCurrentTitle());
		mSingerTextView.setText(mPlayService.getCurrentArtist());
		mPlaySeekBar.setMax((int) mPlayService.getCurrentDuration());
		// Bitmap bmp = mPlayService.getCurrentTrackArt();
		// if (bmp == null)
		// bmp = BitmapFactory.decodeResource(getResources(),
		// R.drawable.ic_launcher);
		// mCdView.setImage(ImageTools.scaleBitmap(bmp, (int) (App.sScreenWidth
		// * 0.7)));

		if (mPlayService.isPlaying()) {
			mCdView.start();
			mStartPlayButton.setImageResource(R.drawable.player_btn_pause_normal);
		} else {
			mCdView.pause();
			mStartPlayButton.setImageResource(R.drawable.player_btn_play_normal);
		}
	}

	private void setLrc(int position) {
		String lrcPath = MediaUtils.getLrcDir() + mPlayService.getCurrentTitle() + ".lrc";
		mLrcViewOnFirstPage.setLrcPath(lrcPath);
		mLrcViewOnSecondPage.setLrcPath(lrcPath);
	}

	@Override
	public void onPublish(int progress) {
		mPlaySeekBar.setProgress(progress);
		if (mLrcViewOnFirstPage.hasLrc())
			mLrcViewOnFirstPage.changeCurrent(progress);
		if (mLrcViewOnSecondPage.hasLrc())
			mLrcViewOnSecondPage.changeCurrent(progress);
	}

	@Override
	public void onChange(int position) {
		// setBackground(position);
		updateTrackInfo();
		onPlay(position);
		setLrc(position);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_play_back:
			finish();
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void updateTrackInfo() {
		// ��ȡר���������
		Bitmap trackArt = mPlayService.getCurrentTrackArt();
		if (trackArt == null) {
			trackArt = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
		}

		mCdView.setImage(ImageTools.scaleBitmap(trackArt, (int) (App.sScreenWidth * 0.7)));
		mPlayContainer.setBackgroundDrawable(new ShapeDrawable(new PlayBgShape(trackArt)));

		// // ����Բ��ר������
		// Bitmap bmp = mPlayService.getCurrentTrackArt();
		// if (bmp == null)
		// bmp = BitmapFactory.decodeResource(getResources(),
		// R.drawable.ic_launcher);
	}

	@Override
	public void onPlayStateChanged() {

	}
}
