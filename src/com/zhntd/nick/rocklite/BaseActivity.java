package com.zhntd.nick.rocklite;

import com.zhntd.nick.rocklite.service.CoreService;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;

public abstract class BaseActivity extends FragmentActivity {
	protected CoreService mPlayService;

	private ServiceConnection mPlayServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mPlayService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mPlayService = ((CoreService.MyBinder) service).getService();
			mPlayService.setOnMusicEventListener(mMusicEventListener);
			onChange(mPlayService.getPlayingPosition());
		}
	};


	/**
	 * 音乐播放服务回调接口的实现类
	 */
	private CoreService.OnMusicEventListener mMusicEventListener = new CoreService.OnMusicEventListener() {
		@Override
		public void onPublish(int progress) {
			BaseActivity.this.onPublish(progress);
		}

		@Override
		public void onChange(int position) {
			BaseActivity.this.onChange(position);
		}
	};

	/**
	 * Fragment的view加载完成后回调
	 */
	public void allowBindService() {
		bindService(new Intent(this, CoreService.class), mPlayServiceConnection, Context.BIND_AUTO_CREATE);
	}

	/**
	 * fragment的view消失后回调
	 */
	public void allowUnbindService() {
		unbindService(mPlayServiceConnection);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}


	/**
	 * 更新进度
	 * 
	 * @param progress
	 *            进度
	 */
	public abstract void onPublish(int progress);

	/**
	 * 切换歌曲
	 * 
	 * @param position
	 *            歌曲在list中的位置
	 */
	public abstract void onChange(int position);
}
