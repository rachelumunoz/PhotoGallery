package io.rachelmunoz.photogallery;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

/**
 * Created by rachelmunoz on 10/3/17.
 */

public class PhotoPageFragment extends VisibleFragment {
	private static final String ARG_URI = "photo_page_url";

	private Uri mUri;
	private WebView mWebView;

	public static PhotoPageFragment newInstance(Uri uri){
		Bundle args = new Bundle();
		args.putParcelable(ARG_URI, uri);

		PhotoPageFragment fragment = new PhotoPageFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mUri = getArguments().getParcelable(ARG_URI);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_photo_page, container, false);

		mWebView = (WebView) view.findViewById(R.id.web_view);
		return view;
	}
}
