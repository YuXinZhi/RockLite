package com.zhntd.nick.rocklite.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.zhntd.nick.rocklite.MainActivity;
import com.zhntd.nick.rocklite.OnlineMusicSite;
import com.zhntd.nick.rocklite.R;
import com.zhntd.nick.rocklite.modle.Site;
import com.zhntd.nick.rocklite.service.CoreService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class Online extends Base {

	private ListView mListView;
	private List<Site> mSiteList;
	private MainActivity mActivity;
	private CoreService mCoreService;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mListView = (ListView) inflater.inflate(R.layout.fragment_online, null);
		initSites();
		initListView();
		return mListView;
	}

	private void initListView() {
		ArrayList<HashMap<String, Object>> siteNames = new ArrayList<HashMap<String, Object>>();
		for (int i = 0; i < mSiteList.size(); i++) {
			HashMap<String, Object> siteName = new HashMap<String, Object>();
			siteName.put("SITENAME", mSiteList.get(i).getName());
			siteNames.add(siteName);
		}

		// data List<? extends Map<String, ?>>
		SimpleAdapter adapter = new SimpleAdapter(mActivity, siteNames, R.layout.item_site, new String[] { "SITENAME" },
				new int[] { R.id.site_name });
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(getActivity(), OnlineMusicSite.class);
				intent.putExtra("url", mSiteList.get(position).getUrl());
				startActivity(intent);
			}
		});
	}

	// "http://m.xiami.com/", "http://m.kugou.com","http://m.kuwo.cn"
	private void initSites() {
		mSiteList = new ArrayList<Site>();
		mSiteList.add(new Site("œ∫√◊“Ù¿÷", "http://m.xiami.com"));
		mSiteList.add(new Site("ø·π∑“Ù¿÷", "http://m.kugou.com"));
		mSiteList.add(new Site("ø·Œ““Ù¿÷", "http://m.kuwo.cn"));
	}

	@Override
	public void onAttach(Activity activity) {
		mActivity = (MainActivity) activity;
		super.onAttach(activity);
	}

	@Override
	public void onPraisedPressed() {

	}

}
