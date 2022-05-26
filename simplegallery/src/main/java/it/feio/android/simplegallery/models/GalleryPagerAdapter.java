package com.note.simplegallery.models;

import android.net.Uri;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.note.simplegallery.GalleryPagerFragment;

import java.util.List;


public class GalleryPagerAdapter extends FragmentStatePagerAdapter {

    private List<Uri> resources;


    public GalleryPagerAdapter(FragmentActivity activity, List<Uri> resources) {
        super(activity.getSupportFragmentManager());
        this.resources = resources;
    }

    @Override
    public Fragment getItem(int position) {
        return GalleryPagerFragment.create(position, resources.get(position));
    }

    @Override
    public int getCount() {
        return resources.size();
    }
}