package com.alex_nechaev.mymediaplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alex_nechaev.mymediaplayer.R;

public class SongLyricsFragment extends Fragment {

    public interface OnSongLyricsListener{
        void onCloseLyrics();
    }

    public OnSongLyricsListener callBack;

    private  SongLyricsFragment(){};

    public static SongLyricsFragment newInstance(String songName,String songArtist, String songLyrics){
        SongLyricsFragment songLyricsFragment = new SongLyricsFragment();
        Bundle bundle = new Bundle();
        bundle.putString("song_name",songName);
        bundle.putString("song_artist",songArtist);
        bundle.putString("song_lyrics",songLyrics);
        songLyricsFragment.setArguments(bundle);
        return  songLyricsFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            callBack = (OnSongLyricsListener)context;
        }catch (ClassCastException ex){
            throw new ClassCastException("The calling activity must implement OnSongLyricsListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.song_lyrics_fragment,container,false);

        TextView songNameTextView = view.findViewById(R.id.lyrics_song_name);
        TextView artistNameTextView = view.findViewById(R.id.lyrics_artist_name);
        TextView lyricsTextView = view.findViewById(R.id.lyrics_song_lyrics);
        ImageButton backButton = view.findViewById(R.id.lyric_back_btn);

        songNameTextView.setText(getArguments().getString("song_name",""));
        artistNameTextView.setText(getArguments().getString("song_artist",""));
        lyricsTextView.setText(getArguments().getString("song_lyrics",""));

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onCloseLyrics();
            }
        });

        return view;
    }
}
