package com.alex_nechaev.mymediaplayer.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
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

public class BigPlayerContainerFragment extends Fragment {

    public interface OnBigPlayerListener {
        void onPrevious();

        void onPlayPause();

        void onNext();

        void onChangeSongProgress(int progress);

        void onSnackBarRequest(String message);

        void onLyricsPressed(String songName, String songArtist, String songLyrics);
    }

    private OnBigPlayerListener callBack;

    private ImageButton previousSongBtn;
    private ImageButton playPauseSongBtn;
    private ImageButton nextSongBtn;
    private ImageButton songLyricBtn;
    private ImageView songAlbum;
    private TextView songArtist;
    private TextView songName;
    private SeekBar songSeekBar;

    private int currentPosition;
    private int newPosition;
    private int songDuration;

    private Song currentSong;
    private String imgRes;

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    public static BigPlayerContainerFragment newInstance() {
        BigPlayerContainerFragment bigPlayerContainerFragment = new BigPlayerContainerFragment();
        return bigPlayerContainerFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            this.callBack = (OnBigPlayerListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException("The calling activity must implement OnBigPlayerListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        sp = getActivity().getSharedPreferences("frag_state", Context.MODE_PRIVATE);

        View view = inflater.inflate(R.layout.tabbed_big_player_controller, container, false);

        songAlbum = view.findViewById(R.id.big_album_image);
        songName = view.findViewById(R.id.big_song_name_text_view);
        songArtist = view.findViewById(R.id.big_artist_name_text_view);
        songSeekBar = view.findViewById(R.id.song_seek_bar);
        songLyricBtn = view.findViewById(R.id.big_text_btn);
        previousSongBtn = view.findViewById(R.id.big_previous_btn);
        playPauseSongBtn = view.findViewById(R.id.big_play_pause_btn);
        nextSongBtn = view.findViewById(R.id.big_next_btn);

        if(sp.getBoolean("is_playing",false)){
            songName.setVisibility(View.VISIBLE);
            songArtist.setVisibility(View.VISIBLE);
            previousSongBtn.setVisibility(View.VISIBLE);
            nextSongBtn.setVisibility(View.VISIBLE);
            songSeekBar.setVisibility(View.VISIBLE);
            songLyricBtn.setVisibility(View.VISIBLE);
            imgRes = sp.getString("album_img", "");

            Song song = new Song(sp.getString("album_img",""),"",sp.getString("artist_name", ""),sp.getString("song_name", ""),sp.getString("song_lyrics", ""),false);
            currentSong = song;

        }else{
            if (!sp.getBoolean("is_service_running",false)){
                songName.setVisibility(View.INVISIBLE);
                songArtist.setVisibility(View.INVISIBLE);
                previousSongBtn.setVisibility(View.INVISIBLE);
                nextSongBtn.setVisibility(View.INVISIBLE);
                songSeekBar.setVisibility(View.INVISIBLE);
                songLyricBtn.setVisibility(View.INVISIBLE);
                imgRes = "";
            }
        }

        playPauseSongBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), sp.getBoolean("is_playing",false) ? R.drawable.btn_pause_selector : R.drawable.btn_play_selector));
        songName.setText(sp.getString("song_name", ""));
        songArtist.setText(sp.getString("artist_name", ""));

        Glide
                .with(this)
                .load(imgRes)
                .centerCrop()
                .placeholder(R.drawable.music_album_icon)
                .into(songAlbum);


        MediaPlayerViewModel mediaPlayerViewModel = new ViewModelProvider(getActivity()).get(MediaPlayerViewModel.class);

        Observer<Boolean> playerObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                playPauseSongBtn.setImageDrawable(ContextCompat.getDrawable(getContext(), aBoolean ? R.drawable.btn_pause_selector : R.drawable.btn_play_selector));
            }
        };

        Observer<Song> songObserver = new Observer<Song>() {
            @Override
            public void onChanged(Song song) {
                Glide
                        .with(getContext())
                        .load(song.getImgUri())
                        .centerCrop()
                        .placeholder(R.drawable.music_album_icon)
                        .into(songAlbum);
                songName.setText(song.getSongName());
                songArtist.setText(song.getArtistName());
                songName.setVisibility(View.VISIBLE);
                songArtist.setVisibility(View.VISIBLE);
                previousSongBtn.setVisibility(View.VISIBLE);
                nextSongBtn.setVisibility(View.VISIBLE);
                songSeekBar.setVisibility(View.VISIBLE);
                songLyricBtn.setVisibility(View.VISIBLE);
                currentSong = song;
            }
        };

        Observer<Integer> songPositionObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                currentPosition = integer;
                songSeekBar.setProgress(currentPosition);
                if (currentPosition >= songDuration) {
                    songSeekBar.setProgress(0);
                }
            }
        };

        Observer<Integer> songDurationObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                songDuration = integer;
                songSeekBar.setProgress(0);
                songSeekBar.setMax(songDuration);
            }
        };

        mediaPlayerViewModel.isPlaying().observe(this, playerObserver);
        mediaPlayerViewModel.getSongMutableLiveData().observe(this, songObserver);
        mediaPlayerViewModel.getSongPositionMutableLiveData().observe(this, songPositionObserver);
        mediaPlayerViewModel.getSongDurationMutableLiveData().observe(this, songDurationObserver);


        songLyricBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentSong != null) {
                    if (currentSong.getLyrics().equals("")) {
                        callBack.onSnackBarRequest("The song doesn't contain lyrics");
                    } else {
                        callBack.onLyricsPressed(currentSong.getSongName(), currentSong.getArtistName(), currentSong.getLyrics());
                    }
                }
            }
        });

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

        nextSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onNext();
            }
        });


        songSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    newPosition = progress;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                callBack.onChangeSongProgress(newPosition);
                seekBar.setProgress(newPosition);
            }
        });

        return view;
    }
}