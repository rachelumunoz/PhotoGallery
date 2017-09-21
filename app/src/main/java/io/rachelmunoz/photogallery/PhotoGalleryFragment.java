package io.rachelmunoz.photogallery;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rachelmunoz on 8/5/17.
 */

public class PhotoGalleryFragment extends Fragment {
	private static final String TAG = "PhotoGalleryFragment";

	private RecyclerView mPhotoRecyclerView;
	private List<GalleryItem> mItems = new ArrayList<>();


	public static PhotoGalleryFragment newInstance(){ //static factory method so can inject dependencies if needed
		return new PhotoGalleryFragment();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		new FetchItemsTask().execute(); // executes FlickrFetchr Api call in background thread
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

		mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view);
		mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

		setUpAdapter();

		return v;
	}

	private void setUpAdapter() {
		if (isAdded()){
			mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
		}

	}

	class PhotoViewHolder extends RecyclerView.ViewHolder {
		private TextView mTitleTextView;

		public PhotoViewHolder(View itemView) {
			super(itemView);

			mTitleTextView = (TextView) itemView;
		}

		public void bindGalleryItem(GalleryItem item){
			mTitleTextView.setText(item.toString());
		}
	}

	class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder>{
		private List<GalleryItem> mGalleryItems;

		public PhotoAdapter(List<GalleryItem> galleryItems) {
			mGalleryItems = galleryItems;
		}

		@Override
		public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			TextView textView = new TextView(getActivity());
			return new PhotoViewHolder(textView);
		}

		@Override
		public void onBindViewHolder(PhotoViewHolder holder, int position) {
			GalleryItem galleryItem = mGalleryItems.get(position);
			holder.bindGalleryItem(galleryItem);
		}

		@Override
		public int getItemCount() {
			return mGalleryItems.size();
		}
	}




	class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {
		@Override
		protected List<GalleryItem> doInBackground(Void... voids) {
			return new FlickrFetchr().fetchItems();
		}

		@Override
		protected void onPostExecute(List<GalleryItem> galleryItems) {
			mItems = galleryItems;
			setUpAdapter();
		}
	}
}
