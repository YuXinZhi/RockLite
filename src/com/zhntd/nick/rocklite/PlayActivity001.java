package com.zhntd.nick.rocklite;

import com.zhntd.nick.rocklite.service.CoreService;
import com.zhntd.nick.rocklite.service.CoreService.MyBinder;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;

public class PlayActivity001 extends Activity implements OnClickListener {

	private CoreService mService;
	private ServiceConnection mServiceConnection;

	private void bindToService() {
		mServiceConnection = new ServiceConnection() {

			@Override
			public void onServiceDisconnected(ComponentName name) {
				mService = null;
			}

			@Override
			public void onServiceConnected(ComponentName name, IBinder iBinder) {
				MyBinder myBinder=(MyBinder) iBinder;
				mService=myBinder.getService();
			}
		};
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(featureId)
	}

	@Override
	public void onClick(View v) {

	}
}
