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
 * Created by rachelmunoz on 8/7/17.
 */

public class ThumbnailDownloader<T> extends HandlerThread {
	private static final String TAG = "ThumbnailDownloader";
	private static final int MESSAGE_DOWNLOADED = 0;

	private boolean mHasQuit = false;
	private Handler mRequestHandler;
	private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();



	public ThumbnailDownloader(){
		super(TAG);
	}

	@Override
	protected void onLooperPrepared() {
		mRequestHandler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == MESSAGE_DOWNLOADED){
					T target = (T) msg.obj;
					Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
					handleRequest(target);
				}
			}
		};
	}

	@Override
	public boolean quit() {
		mHasQuit = true;
		return super.quit();
	}

	public void queueThumbnail(T target, String url){
		Log.i(TAG, "Got a URL: " + url);

		if (url == null){
			mRequestMap.remove(target);
		} else {
			mRequestMap.put(target, url);
			mRequestHandler.obtainMessage(MESSAGE_DOWNLOADED, target).sendToTarget();
		}
	}

	private void handleRequest(final T target){
		try {
			final String url = mRequestMap.get(target);

			if (url == null){
				return;
			}

			byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
			final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
			Log.i(TAG, "bitmap created");
		}catch (IOException ioe){
			Log.e(TAG, "Error downloading image", ioe);
		}
	}
}
