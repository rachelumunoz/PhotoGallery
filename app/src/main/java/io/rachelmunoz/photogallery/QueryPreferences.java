package io.rachelmunoz.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by rachelmunoz on 9/26/17.
 */

public class QueryPreferences {
	private static final String PREF_SEARCH_QUERY = "searchQuery";

	public static String getStoredQuery(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getString(PREF_SEARCH_QUERY, null);
	}

	public static void setStoredQuery(Context context, String query){
		PreferenceManager.getDefaultSharedPreferences(context)
				.edit()
				.putString(PREF_SEARCH_QUERY, query)
				.apply();
	}
}
