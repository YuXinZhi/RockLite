package com.zhntd.nick.rocklite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressLint("SetJavaScriptEnabled")
public class OnlineMusicSite extends Activity {
	private WebView mWebView;
	private String mUrl = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_online_music);
		
	}

	@Override
	protected void onStart() {
		mUrl = getIntent().getStringExtra("url");
		mWebView = (WebView) findViewById(R.id.wb_online_music);
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setSupportZoom(true);
		mWebView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		mWebView.requestFocus();
		mWebView.loadUrl(mUrl);
		mWebView.setWebViewClient(new MyWebViewClient());
		super.onStart();
	}

	class MyWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onDestroy() {
		mWebView.clearHistory();
		mWebView.removeAllViewsInLayout();
		mWebView.clearDisappearingChildren();
		mWebView.clearFocus();
		mWebView.clearView();
		mWebView.destroy();
		super.onDestroy();
	}
}
