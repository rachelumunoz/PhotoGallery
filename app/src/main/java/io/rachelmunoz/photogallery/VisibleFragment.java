package io.rachelmunoz.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by rachelmunoz on 10/1/17.
 */

public abstract class VisibleFragment extends Fragment {
	private static final String TAG = "VisibleFragment";

	@Override
	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION); // Standalone declared in manifest--?? no?
		getActivity().registerReceiver(mOnShowNotification, filter, PollService.PERM_PRIVATE, null);
	}

	@Override
	public void onStop() {
		super.onStop();
		getActivity().unregisterReceiver(mOnShowNotification);
	}

	private BroadcastReceiver mOnShowNotification = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// cancel notification/Intent because that means this app is running and don't need to be notified
			Log.i(TAG, "cancelling notification");
			setResultCode(Activity.RESULT_CANCELED);
		}
	};
}

// this abstract class registers a DynamicReceiver with the private signature so only accessbile by this app
