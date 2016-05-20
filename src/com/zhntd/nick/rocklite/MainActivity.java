package com.zhntd.nick.rocklite;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.dk.animation.SwitchAnimationUtil;
import com.dk.animation.SwitchAnimationUtil.AnimationType;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.zhntd.nick.rocklite.fragment.AllTracks;
import com.zhntd.nick.rocklite.fragment.Base;
import com.zhntd.nick.rocklite.fragment.MenuDrawer;
import com.zhntd.nick.rocklite.fragment.Online;
import com.zhntd.nick.rocklite.fragment.Praised;
import com.zhntd.nick.rocklite.service.CoreService;
import com.zhntd.nick.rocklite.service.CoreService.MyBinder;
import com.zhntd.nick.rocklite.service.CoreService.StateChangedListener;

/**
 * ��ҳ��
 *
 */
public class MainActivity extends FragmentActivity implements OnClickListener, StateChangedListener {

	// �󶨷���
	private ServiceConnection mServiceConnection;
	private CoreService mCoreService;

	// ������������
	SectionsPagerAdapter mSectionsPagerAdapter;

	// Actionbar�е�ͼ��
	private ImageView mLogoImageView, mLocalImageView, mFavouriteImageView, mInternetImageView;
	private List<ImageView> mNaViews;

	// ViewPager�еĸ�����ҳ��
	private List<Base> mFragments;

	private Animation mAnimation, mAnimationFade;

	private ViewPager mViewPager;

	// �ײ����������ư�ť
	private ImageButton mPlayButton, mNextButton, mPreviousButton, mPraiseButton;
	private TextView titleTextView;
	private ImageView mArtImageView;

	// ������
	private RelativeLayout mRootLayout;

	// ����ʽ�˵�
	private MenuDrawer mNavigationDrawerFragment;
	private DrawerLayout mDrawerLayout = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// �������Ƽ�������������
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.activity_main);

		if (mRootLayout == null) {
			mRootLayout = (RelativeLayout) findViewById(R.id.root_layout);
		}

		// ��ʼ��Actionbar
		styleActionBar();

		// ��ʼ��������
		findControlButtons();

		initImageLoader(this);

		// ��ʼ����ҳ��
		initPages();
		// �󶨷���
		bindToService();
		// ��������
		// startService();

		new SwitchAnimationUtil().startAnimation(getWindow().getDecorView(), AnimationType.SCALE);

		initAnim();

		// ��ʼ���˵�
		initDrawer();

	}

	@Override
	protected void onResume() {
		if (mCoreService != null)
			onPlayStateChanged();

		super.onResume();
	}

	private void initDrawer() {
		mNavigationDrawerFragment = (MenuDrawer) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
		// ���ò˵�����
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

		// ��ʼ���˵�
		mDrawerLayout = mNavigationDrawerFragment.mDrawerLayout;
	}

	// ���Ʋ˵�
	private void toogleDrawer() {

		if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
			mDrawerLayout.closeDrawer(GravityCompat.START);
		} else {
			mDrawerLayout.openDrawer(GravityCompat.START);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			toogleDrawer();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@SuppressWarnings("deprecation")
	private void initPager() {

		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		// ����ViewPager
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				// Actionbar�б�ѡ�е�ͼ��ͬ������
				updateNaviItem(position);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		mViewPager.startAnimation(mAnimation);
	}

	private void initPages() {
		mFragments = new ArrayList<Base>();
		mFragments.add(new AllTracks());
		mFragments.add(new Praised());
		mFragments.add(new Online());
	}

	private void styleActionBar() {
		// ����Actionbar����
		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowCustomEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);
		actionBar.setCustomView(R.layout.cust_action_bar);

		// 4��ͼ��
		mLogoImageView = (ImageView) findViewById(R.id.app_icon);
		mFavouriteImageView = (ImageView) findViewById(R.id.favour);
		mLocalImageView = (ImageView) findViewById(R.id.local);
		mInternetImageView = (ImageView) findViewById(R.id.internet);

		// ����ͼ�������
		mLogoImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// �����˵�
				toogleDrawer();
			}
		});

		mInternetImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mViewPager.setCurrentItem(2, true);
			}
		});

		mFavouriteImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(1, true);
			}
		});

		mLocalImageView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mViewPager.setCurrentItem(0, true);
			}
		});
		// �Ҳ���������ͼ��
		mNaViews = new ArrayList<ImageView>();
		mNaViews.add(mLocalImageView);
		mNaViews.add(mFavouriteImageView);
		mNaViews.add(mInternetImageView);
	}

	private void updateNaviItem(int position) {

		// ����û�б�ѡ�е�ͼ�걳��
		for (int i = 0; i < mNaViews.size(); i++) {
			if (position != i)
				mNaViews.get(i).setBackground(getResources().getDrawable(R.drawable.pressed_to));
		}
		// ����ѡ�е�ͼ�걳��
		mNaViews.get(position).setBackground(getResources().getDrawable(R.drawable.seleted));

	}

	private void initAnim() {
		mAnimation = AnimationUtils.loadAnimation(this, R.anim.view_push_down_in);

		mAnimationFade = AnimationUtils.loadAnimation(this, R.anim.fade_in);
	}

	// �ײ�������
	private void findControlButtons() {
		mPlayButton = (ImageButton) findViewById(R.id.btn_play_local);
		mNextButton = (ImageButton) findViewById(R.id.btn_next_local);
		mPreviousButton = (ImageButton) findViewById(R.id.btn_pre_local);
		mPraiseButton = (ImageButton) findViewById(R.id.btn_praised);

		mPlayButton.setOnClickListener(this);
		mPreviousButton.setOnClickListener(this);
		mNextButton.setOnClickListener(this);
		// �ײ���������������
		mArtImageView = (ImageView) findViewById(R.id.iv_art_bottom);
		// ������
		titleTextView = (TextView) findViewById(R.id.title);
		titleTextView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, PlayActivity2.class);
				startActivity(intent);
			}
		});
		// �ղذ�ť
		mPraiseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (mCoreService != null) {
					if (mCoreService.onPraisedBtnPressed()) {
						mPraiseButton.setImageResource(R.drawable.desk2_btn_loved_prs);
					} else {
						mPraiseButton.setImageResource(R.drawable.desk2_btn_love_prs);
					}
				}
				// �����ղ�ҳ����ղذ�ť
				mFragments.get(1).onPraisedPressed();
			}
		});
	}

	public static void initImageLoader(Context context) {
		// This configuration tuning is custom. You can tune every option, you
		// may tune some of them,
		// or you can create default configuration by
		// ImageLoaderConfiguration.createDefault(this);
		// method.
		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
				.threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
				.diskCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO)
				.writeDebugLogs() // Remove for release app
				.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	void startService() {
		final Intent intent = new Intent();
		intent.setClass(MainActivity.this, CoreService.class);
		startService(intent);
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
				// ���߷������MainActivity
				mCoreService.setActivityCallback(MainActivity.this);

				// �������Ӻ����ҳ�����ҳ��
				initPager();
				onPlayStateChanged();
			}
		};

		final Intent intent = new Intent();
		intent.setClass(MainActivity.this, CoreService.class);
		bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

	}

	public CoreService getServiceCallback() {
		return mCoreService;
	}

	@Override
	protected void onDestroy() {
		unbindService(mServiceConnection);
		super.onDestroy();
	}

	// ����������
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			// DO NOTHING
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "MUSIC";
		}
	}

	/**
	 * @return
	 */
	public List<Base> getPages() {
		return mFragments;
	}

	public void updateControlButtonBackground() {
		if (mCoreService.getIsPlaying()) {
			mPlayButton.setImageResource(R.drawable.notification_pause);
		} else {
			mPlayButton.setImageResource(R.drawable.notification_play);
		}
	}

	public void updateArtImage(ImageView imageView) {

		if (mCoreService.getPlayList() != null) {
			ImageLoader.getInstance().displayImage(mCoreService.getCurrentAlbumUri().toString(), imageView);
		}
	}

	public void updateTitle(String title) {
		titleTextView.setText(title);
	}

	public void updatePrisedImg() {

		if (mCoreService != null && mCoreService.checkIfPraised()) {
			mPraiseButton.setImageResource(R.drawable.desk2_btn_loved_prs);
		} else {
			mPraiseButton.setImageResource(R.drawable.desk2_btn_love_prs);
		}
	}

	// ���Ű�ť
	@Override
	public void onClick(View v) {
		int btnId = v.getId();
		switch (btnId) {

		case R.id.btn_play_local:
			if (mCoreService.getIsPlaying()) {
				mCoreService.pausePlayer();
			} else {
				mCoreService.resumePlayer();
			}
			break;

		case R.id.btn_next_local:
			mCoreService.playNextTrack();
			break;

		case R.id.btn_pre_local:
			mCoreService.playPreviousTrack();
			break;

		default:
			break;
		}
	}

	/**
	 * service�еļ���
	 */
	@Override
	public void onPlayStateChanged() {
		// ����/��ͣ��ť
		updateControlButtonBackground();
		// ר������
		updateArtImage(mArtImageView);
		updateTitle(mCoreService.getCurrentTitle());
		updatePrisedImg();
	}

	public void onBlurReady(Drawable drawable) {

		if (drawable != null) {
			mRootLayout.setBackground(drawable);
			mRootLayout.startAnimation(mAnimationFade);
			drawable = null;
		}
	}
}
