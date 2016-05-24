package com.zhntd.nick.rocklite.fragment;

import java.util.List;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.zhntd.nick.rocklite.MainActivity;
import com.zhntd.nick.rocklite.R;
import com.zhntd.nick.rocklite.loaders.TrackListAdapter;
import com.zhntd.nick.rocklite.modle.Track;
import com.zhntd.nick.rocklite.service.CoreService;
import com.zhntd.nick.rocklite.utils.MediaUtils;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class AllTracks extends Base {

	private ListView mListView;
	private TrackListAdapter mAdapter;

	private MainActivity mActivity;
	// 调用MainActivity中的服务获取播放列表
	private CoreService mServiceCallback;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mListView = (ListView) inflater.inflate(R.layout.fragment_all, null);
		display();
		return mListView;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onAttach(Activity activity) {
		mActivity = (MainActivity) activity;
		mServiceCallback = mActivity.getServiceCallback();
		Log.i("AllTracks", mServiceCallback.toString());
		super.onAttach(activity);
	}

	@Override
	public void onStart() {
		super.onStart();
	}

	void display() {
		new TrackLoaderTask().execute();
	}

	/**
	 * @return
	 */
	List<Track> getTracks() {
		return MediaUtils.getTrackList(getActivity());
	}

	/**
	 * @param tracks
	 */
	void inflateListView(final List<Track> tracks) {
		Log.i("tracks", tracks.size() + "=============");
		mAdapter = new TrackListAdapter(tracks, getActivity(), ImageLoader.getInstance());
		mListView.setAdapter(mAdapter);

		mServiceCallback.setupPLayList(tracks);

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

				if (mServiceCallback.getPlayList() != tracks)
					mServiceCallback.setupPLayList(tracks);
				mServiceCallback.setCurrentCursor(position);
				mServiceCallback.playTrack(position);
				// Intent intent = new Intent(mActivity, PlayActivity.class);
				// Bundle bundle =new Bundle();
				// intent.putExtras(bundle);
				// startActivity(intent);

			}
		});
	}

	final class TrackLoaderTask extends AsyncTask<Void, Void, List<Track>> {

		@Override
		protected List<Track> doInBackground(Void... arg0) {
			return getTracks();
		}

		@Override
		protected void onPostExecute(List<Track> result) {
			inflateListView(result);
			super.onPostExecute(result);
		}
	}

	@Override
	public void onPraisedPressed() {

	}

}
