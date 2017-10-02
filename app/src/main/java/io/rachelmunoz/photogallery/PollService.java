package io.rachelmunoz.photogallery;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
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

	public static final String ACTION_SHOW_NOTIFICATION = "io.rachelmunoz.photogallery.SHOW_NOTIFICATION";
	public static final String PERM_PRIVATE = "io.rachelmunoz.photogallery.PRIVATE";
	public static final String REQUEST_CODE = "REQUEST_CODE";
	public static final String NOTIFICATION = "NOTIFICATION";

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

		QueryPreferences.setAlarmOn(context, isOn);
	}

	public static boolean isServiceAlarmOn(Context context){  // check of whether there is a PendingIntent hooked up to the Alarm
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

			String CHANNEL_ID = "channel_1";
			Resources resources = getResources();
			Intent i = PhotoGalleryActivity.newIntent(this);
			PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

			NotificationManager mNotificationManager =
					(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//			NotificationManagerCompat mNotificationManager = NotificationManagerCompat.from(this);

			NotificationChannel mChannel;
			int importance = NotificationManager.IMPORTANCE_DEFAULT;

			NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
					.setTicker(resources.getString(R.string.new_pictures_title))
					.setSmallIcon(android.R.drawable.ic_menu_report_image)
					.setContentTitle(resources.getString(R.string.new_pictures_title))
					.setContentText(resources.getString(R.string.new_pictures_text))
					.setContentIntent(pi)
					.setContentTitle(resources.getString(R.string.new_pictures_title))
					.setAutoCancel(true);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				mChannel = new NotificationChannel(CHANNEL_ID, this.getString(R.string.app_name), importance);
				mChannel.setDescription("notification");
				mNotificationManager.createNotificationChannel(mChannel);
			}

//			mNotificationManager.notify(0, builder.build());
//			sendBroadcast(new Intent(ACTION_SHOW_NOTIFICATION), PERM_PRIVATE);

			showBackgroundNotification(0, builder.build());
		}

		QueryPreferences.setLastResultId(this, resultId);
	}

	private void showBackgroundNotification(int requestCode, Notification notification) {
		Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
		i.putExtra(REQUEST_CODE, requestCode);
		i.putExtra(NOTIFICATION, notification);
		sendOrderedBroadcast(i, PERM_PRIVATE, null, null, Activity.RESULT_OK, null, null);
	}

	private boolean isNetworkAvailableAndConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
		boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

		return isNetworkConnected;
	}
}
