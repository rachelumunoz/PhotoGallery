package io.rachelmunoz.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by rachelmunoz on 9/21/17.
 */

public class ThumbnailDownloader<T> extends HandlerThread {
	private static final String TAG = "ThumbnailDownloader";

	private static final int MESSAGE_DOWNLOAD = 0;
	private  boolean mHasQuit = false;
	private Handler mRequestHandler;
	private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
	private Handler mResponseHandler;
	private ThumbnailDownloadListener<T> mThumbnailDownloadListener;


	public interface ThumbnailDownloadListener<T> { // delegates responsibility to class where image is downloaded, sep of concerns
		void onThumbnailDownloaded(T target, Bitmap thumbnail);
	}

	public void setThumbnailDownloadListener(ThumbnailDownloadListener<T> listener) {
		mThumbnailDownloadListener = listener;
	}

	public ThumbnailDownloader(Handler responseHandler) {
		super(TAG);
		mResponseHandler = responseHandler;
	}

	@Override
	public boolean quit() {
		mHasQuit = true;
		return super.quit();
	}

	@Override
	protected void onLooperPrepared() { // called before Looper check queue for the first time
		mRequestHandler = new Handler(){ // initialize requestHandler in HandlerThread -- makes Looper in this class
			@Override
			public void handleMessage(Message msg) { // called when message pulled off queue and ready to be processed
				if (msg.what == MESSAGE_DOWNLOAD){
					T target = (T) msg.obj;
					Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
					handleRequest(target);
				}
			}
		};
	}


	private void handleRequest(final T target) {
		try {
			final String url = mRequestMap.get(target);

			if (url == null) return;

			byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
			final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length); // construct bitmap
			Log.i(TAG, "Bitmap created");

			mResponseHandler.post(new Runnable() { // tied to UI, implementation declared there
				@Override
				public void run() {
					if(mRequestMap.get(target) != url || mHasQuit) return;

					mRequestMap.remove(target);
					mThumbnailDownloadListener.onThumbnailDownloaded(target, bitmap);
				}
			});

		} catch (IOException ioe){
			Log.e(TAG, "Error downloading image", ioe);
		}
	}

	public void  queueThumbnail(T target, String url){
		Log.i(TAG, "Got a url: " + url);

		if (url == null){
			mRequestMap.remove(target);
		} else {
			mRequestMap.put(target, url);
			mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget();
		}
	}

	public void clearQueue(){
		mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
		mRequestMap.clear();
	}
}
