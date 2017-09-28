package io.rachelmunoz.photogallery;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by rachelmunoz on 9/26/17.
 */

@TargetApi(26)
public class PollService extends IntentService {
	private static final String TAG = "PollService";

	private static final long POLL_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);

	public static Intent newIntent(Context context){
		return new Intent(context, PollService.class);
	}

	public static void setServiceAlarm(Context context, boolean isOn){
		Intent i = PollService.newIntent(context);
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);

		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		if (isOn){
			alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), POLL_INTERVAL_MS, pi);
		} else {
			alarmManager.cancel(pi);
			pi.cancel();
		}
	}

	public static boolean isServiceAlarmOn(Context context){
		Intent i = PollService.newIntent(context);
		PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE); // return null if Alarm not on
		return pi != null;
	}

	public PollService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		if(!isNetworkAvailableAndConnected()) return;

		String query = QueryPreferences.getStoredQuery(this);
		String lastResultId = QueryPreferences.getLastResultId(this);
		List<GalleryItem> items;

		if (query == null){
			items = new FlickrFetchr().fetchRecentPhotos();
		} else {
			items = new FlickrFetchr().searchPhotos(query);
		}

		if (items.size() == 0) return;

		String resultId = items.get(0).getId();
		if (resultId.equals(lastResultId)){
			Log.i(TAG, "Got an old result " + resultId);
		} else {
			Log.i(TAG, "Got a new result " + resultId);

			Resources resources = getResources();
			Intent i = PhotoGalleryActivity.newIntent(this);
			PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

			String CHANNEL_ID = "channel_1";
			CharSequence name = getString(R.string.channel_name);
			int importance = NotificationManager.IMPORTANCE_DEFAULT;
			NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);

			Notification notification = new NotificationCompat.Builder(this)
					.setTicker(resources.getString(R.string.new_pictures_title))
					.setSmallIcon(android.R.drawable.ic_menu_report_image)
					.setContentTitle(resources.getString(R.string.new_pictures_title))
					.setContentText(resources.getString(R.string.new_pictures_text))
					.setContentIntent(pi)
					.setAutoCancel(true)
					.setChannel(CHANNEL_ID)
					.build();

			NotificationManager mNotificationManager =
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

//			NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);



//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				mNotificationManager.createNotificationChannel(mChannel);

//			}

			mNotificationManager.notify(0, notification);
		}

		QueryPreferences.setLastResultId(this, resultId);
	}

	private boolean isNetworkAvailableAndConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
		boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

		return isNetworkConnected;
	}
}
