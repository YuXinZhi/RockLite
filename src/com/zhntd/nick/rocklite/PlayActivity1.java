package com.zhntd.nick.rocklite;

import java.util.ArrayList;

import com.zhntd.nick.rocklite.service.CoreService;
import com.zhntd.nick.rocklite.service.CoreService.MyBinder;
import com.zhntd.nick.rocklite.service.CoreService.StateChangedListener;
import com.zhntd.nick.rocklite.views.CDView;
import com.zhntd.nick.rocklite.views.LrcView;
import com.zhntd.nick.rocklite.views.PagerIndicator;

import android.content.ComponentName;
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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
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
	private SeekBar mPlaySeekBar; // seekbar
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
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.play_activity_layout);
		findTop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void findTop() {
		mRootLayout = (LinearLayout) findViewById(R.id.ll_play_container);
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
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mPagerAapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}
		});
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
				// 告诉服务监听MainActivity
				mCoreService.setActivityCallback(PlayActivity1.this);

				// 服务连接后更新页面更新页面
				onPlayStateChanged();
			}
		};

		final Intent intent = new Intent();
		intent.setClass(PlayActivity1.this, CoreService.class);
		bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

	}

	
	//服务的回调函数
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


}
