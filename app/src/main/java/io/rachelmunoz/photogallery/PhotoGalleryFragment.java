package io.rachelmunoz.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;

/**
 * Created by rachelmunoz on 8/5/17.
 */

public class PhotoGalleryFragment extends Fragment {
	private RecyclerView mPhotoRecyclerView;

	private static final String TAG = "PhotoGalleryFragment";


	public static PhotoGalleryFragment newInstance(){
		return new PhotoGalleryFragment();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		new FetchItemsTask().execute();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

		mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view);
		mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

		return v;
	}

	private class FetchItemsTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			new FlickrFetchr().fetchItems();
			return null;
		}
	}
}
