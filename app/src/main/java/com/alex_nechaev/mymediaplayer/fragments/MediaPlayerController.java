package com.alex_nechaev.mymediaplayer.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.alex_nechaev.mymediaplayer.MediaPlayerViewModel;
import com.alex_nechaev.mymediaplayer.R;
import com.alex_nechaev.mymediaplayer.Song;
import com.bumptech.glide.Glide;

public class MediaPlayerController extends Fragment {

    public interface OnMediaPlayerControllerListener {
        void onPrevious();

        void onPlayPause();

        void onNext();
    }

    private ImageButton previousSongBtn;
    private ImageButton playPauseSongBtn;
    private ImageButton nextSongBtn;
    private ImageView songAlbum;
    private TextView songArtist;
    private TextView songName;

    SharedPreferences sp;

    private OnMediaPlayerControllerListener callBack;

    public static MediaPlayerController newInstance() {
        MediaPlayerController mediaPlayerController = new MediaPlayerController();
        return mediaPlayerController;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callBack = (OnMediaPlayerControllerListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException("The calling activity must implement OnMediaPlayerControllerListener");
        }
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        sp = getActivity().getSharedPreferences("frag_state",Context.MODE_PRIVATE);

        View view = inflater.inflate(R.layout.media_player_controller, container, false);

        songAlbum = view.findViewById(R.id.song_album_img);
        songArtist = view.findViewById(R.id.artist_name_text_view);
        songName = view.findViewById(R.id.song_name_text_view);
        playPauseSongBtn = view.findViewById(R.id.play_pause_btn);

        playPauseSongBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), sp.getBoolean("is_playing",false) ? R.drawable.btn_pause_selector : R.drawable.btn_play_selector));

        songName.setText(sp.getString("song_name",""));
        songArtist.setText(sp.getString("artist_name",""));

        Glide
                .with(getContext())
                .load(sp.getString("album_img",""))
                .placeholder(R.drawable.music_album_icon)
                .centerCrop()
                .into(songAlbum);



        container.setVisibility(sp.getInt("controller_visibility",8));


        MediaPlayerViewModel mediaPlayerViewModel = new ViewModelProvider(getActivity()).get(MediaPlayerViewModel.class);

        Observer<Boolean> playerObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                if (aBoolean) {
                    playPauseSongBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.btn_pause_selector));
                } else {
                    playPauseSongBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.btn_play_selector));
                }
            }
        };


        Observer<Song> songObserver = new Observer<Song>() {
            @Override
            public void onChanged(Song song) {
                Glide
                        .with(getContext())
                        .load(song.getImgUri())
                        .placeholder(R.drawable.music_album_icon)
                        .centerCrop()
                        .into(songAlbum);
                container.setVisibility(View.VISIBLE);
                songName.setText(song.getSongName());
                songArtist.setText(song.getArtistName());
            }
        };

        mediaPlayerViewModel.isPlaying().observe(this, playerObserver);
        mediaPlayerViewModel.getSongMutableLiveData().observe(this, songObserver);


        previousSongBtn = view.findViewById(R.id.previous_btn);
        previousSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onPrevious();
            }
        });

        playPauseSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onPlayPause();
            }
        });

        nextSongBtn = view.findViewById(R.id.next_btn);
        nextSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onNext();
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
