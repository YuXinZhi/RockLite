package com.zhntd.nick.rocklite.fragment;

import com.zhntd.nick.rocklite.R;
import com.zhntd.nick.rocklite.service.CoreService;
import com.zhntd.nick.rocklite.views.CDView;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

public class PlayActivity extends Activity implements OnClickListener {
	private ImageView mPlayBackImageView;
	private TextView mMusicTitle;
	private TextView mArtistTextView;
	private CDView mCdView;
	private CoreService mService;
	private SeekBar mPlaySeekBar;

	private int screenWidth;
	private int screenHeight;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.play_activity_layout);
		initUI();
	}

	private void initUI() {
		mPlayBackImageView = (ImageView) findViewById(R.id.iv_play_back);
		mMusicTitle = (TextView) findViewById(R.id.tv_music_title);
		mPlaySeekBar = (SeekBar) findViewById(R.id.sb_play_progress);
	//	mStartPlayButton = (ImageButton) findViewById(R.id.ib_play_start);

		measureScreen();
		MarginLayoutParams p = (MarginLayoutParams) mPlaySeekBar.getLayoutParams();
		p.leftMargin = (int) (screenWidth * 0.1);
		p.rightMargin = (int) (screenWidth * 0.1);

		mPlaySeekBar.setOnSeekBarChangeListener(mSeekBarChangeListener);

		mPlayBackImageView.setOnClickListener(this);
	}

	private void measureScreen() {
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
	}

	/**
	 * 拖动进度条
	 */
	private SeekBar.OnSeekBarChangeListener mSeekBarChangeListener = 
			new SeekBar.OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int progress = seekBar.getProgress();
		//	mService.seek(progress);
		}
	};
	
	
	@Override
	public void onClick(View v) {

	}
}
