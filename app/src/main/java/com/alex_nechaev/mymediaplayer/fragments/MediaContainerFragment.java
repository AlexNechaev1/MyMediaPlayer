package com.alex_nechaev.mymediaplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alex_nechaev.mymediaplayer.R;

public class MediaContainerFragment extends Fragment {

    final String SONG_LIST_TAG = "SONG_LIST_TAG";
    final String MEDIA_PLAYER_TAG = "MEDIA_PLAYER_TAG";

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.tabbed_media_container,container,false);

        SongListFragment songListFragment = new SongListFragment();
        MediaPlayerController mediaPlayerController = MediaPlayerController.newInstance();

        getChildFragmentManager().beginTransaction().add(R.id.songs_list_container,songListFragment,SONG_LIST_TAG)
                .add(R.id.player_controller_container,mediaPlayerController,MEDIA_PLAYER_TAG).commit();

        return rootView;
    }

}

