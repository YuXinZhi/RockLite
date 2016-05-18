package com.zhntd.nick.rocklite;

import com.zhntd.nick.rocklite.views.CDView;
import com.zhntd.nick.rocklite.views.LrcView;
import com.zhntd.nick.rocklite.views.PagerIndicator;

import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.View.OnClickListener;
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
	private PagerIndicator mPagerIndicator; // indicator

	@Override
	public void onPublish(int progress) {
		
	}

	@Override
	public void onChange(int position) {

	}

	@Override
	public void onClick(View v) {

	}

}
