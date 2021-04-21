package com.alex_nechaev.mymediaplayer.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.alex_nechaev.mymediaplayer.fragments.MediaContainerFragment;
import com.alex_nechaev.mymediaplayer.fragments.BigPlayerContainerFragment;

public class PagerAdapter extends FragmentStateAdapter {

    private final int numOfTabs;

    public PagerAdapter(@NonNull Fragment fragment,int numOfTabs) {
        super(fragment);
        this.numOfTabs = numOfTabs;
    }


    @Override
    public int getItemCount() {
        return this.numOfTabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new BigPlayerContainerFragment();
        }
        return new MediaContainerFragment();
    }
}
