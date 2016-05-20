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
import com.zhntd.nick.rocklite.views.CDView;
import com.zhntd.nick.rocklite.views.LrcView;
import com.zhntd.nick.rocklite.views.PagerIndicator;
import com.zhntd.nick.rocklite.views.PlayPageTransformer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
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
import android.view.Window;
import android.view.ViewGroup.MarginLayoutParams;
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
	// ������
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

	// �������붯��
	private Animation mAnimationFade;

	// ר������͸��ҳ��
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
		initPages();
		initSeekBar();
		initImageLoader(this);
		initAnim();
	}

	private void initSeekBar() {
		mSeekBar = (SeekBar) findViewById(R.id.sb_play_progress);
		// ��̬����seekbar��margin
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

		// ���ϽǷ���
		mPlayBackImageView = (ImageView) findViewById(R.id.iv_play_back);
		mTitleTextView = (TextView) findViewById(R.id.tv_music_title);
	}

	// ��ʼ��PagerView��ҳ��
	private void initPages() {
		mPages = new ArrayList<View>(2);
		View cdView = View.inflate(this, R.layout.play_pager_item_1, null);
		View lrcView = View.inflate(this, R.layout.play_pager_item_2, null);
		mPages.add(cdView);
		mPages.add(lrcView);
	}

	// ��ʼ��PagerView
	@SuppressWarnings("deprecation")
	private void initPager() {
		// ԭ��ָʾ��
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
	}

	private void updateTitle() {

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ib_play_pre:
			break;
		case R.id.ib_play_start:

			break;
		case R.id.ib_play_next:

			break;
		case R.id.iv_play_back:
			break;
		default:
			break;
		}
	}

	void bindToService() {
		/**
		 * Ӧ�����(�ͻ���)���Ե���bindService()�󶨵�һ��service��Androidϵͳ֮�����service��onBind()
		 * ������������һ��������service������IBinder��
		 */
		mServiceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName arg0) {

			}

			// ϵͳ���������������service��onBind()�з��ص�IBinder��
			@Override
			public void onServiceConnected(ComponentName arg0, IBinder iBinder) {
				// MyBinder�ڷ����ж���
				MyBinder myBinder = (MyBinder) iBinder;
				mCoreService = myBinder.getService();
				Log.i("music", mCoreService.toString());
				// ���߷������
				mCoreService.setActivityCallback(PlayActivity1.this);
				// �������ֽ��ȼ���
				mCoreService.setOnMusicEventListener(mMusicEventListener);

				// �������Ӻ����ҳ�����ҳ��
				onPlayStateChanged();
			}
		};

		final Intent intent = new Intent();
		intent.setClass(PlayActivity1.this, CoreService.class);
		bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

	}

	// ����Ļص�����
	public void onBlurReady(Drawable drawable) {
		if (drawable != null) {
			mRootLayout.setBackground(drawable);
			mRootLayout.startAnimation(mAnimationFade);
			drawable = null;
		}
	}

	private void initAnim() {
		mAnimationFade = AnimationUtils.loadAnimation(this, R.anim.fade_in);
	}

	/**
	 * ���ֲ��ŷ���ص��ӿڵ�ʵ����
	 */
	private OnMusicEventListener mMusicEventListener = new OnMusicEventListener() {
		@Override
		public void onPublish(int progress) {
		}

		@Override
		public void onChange(int position) {
		}
	};

	public static void initImageLoader(Context context) {
		// �Զ���ͼƬ��������
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		ImageLoader.getInstance().init(config);
	}
}
