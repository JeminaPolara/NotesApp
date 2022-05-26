package com.note.simplegallery.models.ui;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;

import androidx.viewpager.widget.ViewPager;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class CoverFlowPageTransformer implements ViewPager.PageTransformer {

	public void transformPage(View view, float position) {
		view.setRotationY(position * -30);
	}
}