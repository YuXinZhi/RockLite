package com.zhntd.nick.rocklite;

import com.dk.animation.SwitchAnimationUtil;
import com.dk.animation.SwitchAnimationUtil.AnimationType;
import com.zhntd.nick.rocklite.fonts.FontsFactory;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.TextView;

/**
 * ��ӭҳ��
 *
 */
public class Welcome extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
		init();
		// ���ض���
		new SwitchAnimationUtil().startAnimation(getWindow().getDecorView(), AnimationType.ROTATE);

		FontsFactory.createRoboto(this);

		// 1��Ļ���
		new CountDownTimer(1000, 1000) {

			@Override
			public void onTick(long arg0) {

			}

			@Override
			public void onFinish() {
				// ��ת����ҳ��
				Intent intent = new Intent(Welcome.this, MainActivity.class);
				startActivity(intent);
				overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
				Welcome.this.finish();
			}
		}.start();

	}

	// ��ʼ����ӭҳ������
	private void init() {
		Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/hero.ttf");
		TextView splash = (TextView) findViewById(R.id.splash_text);
		splash.setTypeface(typeFace);
		splash.setText("��      ӭ");
	}

}
