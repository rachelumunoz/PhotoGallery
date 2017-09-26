package io.rachelmunoz.photogallery;

import android.graphics.drawable.Drawable;
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
import android.widget.ImageView;
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
	private ThumbnailDownloader<PhotoViewHolder> mThumbnailDownloader;


	public static PhotoGalleryFragment newInstance(){ //static factory method so can inject dependencies if needed
		return new PhotoGalleryFragment();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		new FetchItemsTask().execute(); // executes FlickrFetchr Api call in background thread

		mThumbnailDownloader = new ThumbnailDownloader<>();
		mThumbnailDownloader.start();
		mThumbnailDownloader.getLooper();
		Log.i(TAG, "Background thread started");
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		mThumbnailDownloader.quit();
		Log.i(TAG, "Background thread destroyed");
	}

	private void setUpAdapter() {
		if (isAdded()){
			mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
		}

	}

	class PhotoViewHolder extends RecyclerView.ViewHolder {
		private ImageView mItemImageView;

		public PhotoViewHolder(View itemView) {
			super(itemView);

			mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
		}

		public void bindDrawable(Drawable drawable){
			mItemImageView.setImageDrawable(drawable);
		}
	}

	class PhotoAdapter extends RecyclerView.Adapter<PhotoViewHolder>{
		private List<GalleryItem> mGalleryItems;

		public PhotoAdapter(List<GalleryItem> galleryItems) {
			mGalleryItems = galleryItems;
		}

		@Override
		public PhotoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			LayoutInflater inflater = LayoutInflater.from(getActivity());
			View view = inflater.inflate(R.layout.list_item_gallery, parent, false);
			return new PhotoViewHolder(view);
		}

		@Override
		public void onBindViewHolder(PhotoViewHolder holder, int position) {
			GalleryItem galleryItem = mGalleryItems.get(position);
//			holder.bindGalleryItem(galleryItem);
			Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
			holder.bindDrawable(placeholder);
			mThumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl());
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
