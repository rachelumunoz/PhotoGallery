package io.rachelmunoz.photogallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rachelmunoz on 8/5/17.
 */

public class PhotoGalleryFragment extends VisibleFragment {
	private static final String TAG = "PhotoGalleryFragment";

	private RecyclerView mPhotoRecyclerView;
	private List<GalleryItem> mItems = new ArrayList<>();
	private ThumbnailDownloader<PhotoViewHolder> mThumbnailDownloader; //subclass of HandlerThread


	public static PhotoGalleryFragment newInstance(){ //static factory method so can inject dependencies if needed
		return new PhotoGalleryFragment();
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		setHasOptionsMenu(true);
		updateItems(); // executes FlickrFetchr Api call in background thread

//		Intent i = PollService.newIntent(getActivity());
//		getActivity().startService(i);
//		PollService.setServiceAlarm(getActivity(), true);

		Handler responseHandler = new Handler();
		mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler); // pass in Handler attached to UI
		mThumbnailDownloader.setThumbnailDownloadListener(
				new ThumbnailDownloader.ThumbnailDownloadListener<PhotoViewHolder>(){
					@Override
					public void onThumbnailDownloaded(PhotoViewHolder target, Bitmap thumbnail) {
						Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
						target.bindDrawable(drawable);
					}
				}
		);

		mThumbnailDownloader.start(); // start the background thread
		mThumbnailDownloader.getLooper(); // ensures thread is ready -- obviates race condition
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
	public void onDestroyView() {
		super.onDestroyView();
		mThumbnailDownloader.clearQueue();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mThumbnailDownloader.quit();
		Log.i(TAG, "Background thread destroyed");
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_photo_gallery, menu);

		MenuItem searchItem = menu.findItem(R.id.menu_item_search); // menu item
		final SearchView searchView = (SearchView) searchItem.getActionView(); // search View

		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
			@Override
			public boolean onQueryTextSubmit(String query) {
				Log.d(TAG, "QueryTextSubmit " + query);
				QueryPreferences.setStoredQuery(getActivity(), query);
				updateItems();
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				Log.d(TAG, "QueryTextChange " + newText);
				return false;
			}
		});

		searchView.setOnSearchClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String query = QueryPreferences.getStoredQuery(getActivity());
				searchView.setQuery(query, false);
			}
		});

		MenuItem toggleitem = menu.findItem(R.id.menu_item_toggle_polling);
		if (PollService.isServiceAlarmOn(getActivity())){
			toggleitem.setTitle(R.string.stop_polling);
		} else {
			toggleitem.setTitle(R.string.start_polling);
		}


	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.menu_item_clear:
				QueryPreferences.setStoredQuery(getActivity(), null);
				updateItems(); // ensures images reflect most recent search query
				return true;
			case R.id.menu_item_toggle_polling:
				boolean shouldStartAlarm = !PollService.isServiceAlarmOn(getActivity());
				PollService.setServiceAlarm(getActivity(), shouldStartAlarm);
				getActivity().invalidateOptionsMenu();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void updateItems() { // method that gets most recent search and interfaces with Flickr API to update list
		String query = QueryPreferences.getStoredQuery(getActivity());
		new FetchItemsTask(query).execute();
	}

	private void setUpAdapter() {
		if (isAdded()){
			mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
		}

	}

	class PhotoViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
		private ImageView mItemImageView;
		private GalleryItem mGalleryItem;

		public PhotoViewHolder(View itemView) {
			super(itemView);

			mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
			itemView.setOnClickListener(this);
		}

		public void bindDrawable(Drawable drawable){
			mItemImageView.setImageDrawable(drawable);
		}

		public void bindGalleryItem(GalleryItem galleryItem) {
			mGalleryItem = galleryItem;
		}

		@Override
		public void onClick(View view) {
//			Intent i = new Intent(Intent.ACTION_VIEW, mGalleryItem.getPhotoPageUri());
			Intent i = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoPageUri());
			startActivity(i);
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
			holder.bindGalleryItem(galleryItem);
			Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
			holder.bindDrawable(placeholder);
			mThumbnailDownloader.queueThumbnail(holder, galleryItem.getUrl()); // request to download image is placed in ViewHolder of RecyclerView
		}

		@Override
		public int getItemCount() {
			return mGalleryItems.size();
		}
	}




	class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {
		private String mQuery;

		public FetchItemsTask(String query) {
			mQuery = query;
		}

		@Override
		protected List<GalleryItem> doInBackground(Void... voids) {
			String query = "robot";

			if (mQuery == null){
				return new FlickrFetchr().fetchRecentPhotos();
			} else {
				return new FlickrFetchr().searchPhotos(mQuery);
			}

		}

		@Override
		protected void onPostExecute(List<GalleryItem> galleryItems) {
			mItems = galleryItems;
			setUpAdapter();
		}
	}
}
