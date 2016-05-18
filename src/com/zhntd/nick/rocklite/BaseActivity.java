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
	 * ���ֲ��ŷ���ص��ӿڵ�ʵ����
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
	 * Fragment��view������ɺ�ص�
	 */
	public void allowBindService() {
		bindService(new Intent(this, CoreService.class), mPlayServiceConnection, Context.BIND_AUTO_CREATE);
	}

	/**
	 * fragment��view��ʧ��ص�
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
	 * ���½���
	 * 
	 * @param progress
	 *            ����
	 */
	public abstract void onPublish(int progress);

	/**
	 * �л�����
	 * 
	 * @param position
	 *            ������list�е�λ��
	 */
	public abstract void onChange(int position);
}
