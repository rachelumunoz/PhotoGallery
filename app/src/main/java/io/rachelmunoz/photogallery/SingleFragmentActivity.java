package io.rachelmunoz.photogallery;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by rachelmunoz on 7/2/17.
 */

public abstract class SingleFragmentActivity extends AppCompatActivity {
	protected abstract Fragment createFragment();

	@LayoutRes
	protected int getLayoutResId(){
		return R.layout.activity_fragment;
	} //default layout

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(getLayoutResId()); // container for whole view

		FragmentManager fm = getSupportFragmentManager();
		Fragment fragment = fm.findFragmentById(R.id.fragment_container); // in Activity xml, specific spot fragment will go

		if (fragment == null){
			fragment = createFragment();
			fm.beginTransaction() // FragmentTransaction returned so can compose
					.add(R.id.fragment_container, fragment) // add the fragment to its spot
					.commit();
		}


	}
}
