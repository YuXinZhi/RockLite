package com.zhntd.nick.rocklite.views;

import com.zhntd.nick.rocklite.App;

import android.support.v4.view.ViewPager.PageTransformer;
import android.view.View;

public class PlayPageTransformer implements PageTransformer {

	@Override
	public void transformPage(View view, float position) {
		if (position < -1) { // [-Infinity,-1) ��߿�������
			view.setAlpha(0.0f);
		} else if (position <= 0) { // [-1,0]������м� �� �м������
			view.setAlpha(1 + position);
			view.setTranslationX(App.sScreenWidth * (-position));
		} else if (position <= 1) { // (0,1] �ұ����м� �� �м����ұ�
			view.setAlpha(1);
			// view.setTranslationX(mScreenWidth * -position);
		} else if (position > 1) { // (1,+Infinity] �ұ߿�������
			view.setAlpha(0.0f);
		}
	}
}
