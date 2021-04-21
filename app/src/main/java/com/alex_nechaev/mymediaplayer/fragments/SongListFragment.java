package com.alex_nechaev.mymediaplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alex_nechaev.mymediaplayer.FileManager;
import com.alex_nechaev.mymediaplayer.MediaPlayerViewModel;
import com.alex_nechaev.mymediaplayer.R;
import com.alex_nechaev.mymediaplayer.Song;
import com.alex_nechaev.mymediaplayer.adapters.SongAdapter;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class SongListFragment extends Fragment {

    private List<Song> songList;
    private int position;
    SongAdapter songAdapter;
    boolean isSongDeleted;

    public interface onSongListListener {
        void onSongSelected(int position);

        void onAddSong();

        void onSwipeRight();

        void onSwipeLeft(int position);

        void onSongDeleted(int position);

        void onUndoDeletedSong(int position,Song song);

        int getCurrentServicePosition();

        void setCurrentSongPosition(int to);
    }

    private onSongListListener callBack;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            callBack = (onSongListListener) context;
        } catch (ClassCastException ex) {
            throw new ClassCastException("The calling activity must implement onSongListListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.media_song_list, container, false);

        MediaPlayerViewModel mediaPlayerViewModel = new ViewModelProvider(getActivity()).get(MediaPlayerViewModel.class);

        ImageButton addSongBtn = rootView.findViewById(R.id.add_song_btn);


        addSongBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callBack.onAddSong();
            }
        });

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        songList = new ArrayList<>();

        //< ------ Observers for changes in the songList Object ------ >//
        Observer<List<Song>> songListObserver = new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> songs) {
                songList.addAll(songs);
            }
        };
        mediaPlayerViewModel.getSongListMutableLiveData().observe(this, songListObserver);

        //< ------ Observes for new song that added to the songList Object ------ >//
        Observer<Song> newSongAdded = new Observer<Song>() {
            @Override
            public void onChanged(Song song) {
                songList.add(song);
                songAdapter.notifyItemInserted(songList.size()-1);
                FileManager.writeToFile(getContext(),songList,"song_file");
            }
        };
        mediaPlayerViewModel.getNewSongAddedMutableLiveData().observe(this, newSongAdded);

        Observer<Boolean> isSongDeletedObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                isSongDeleted = true;
                Song song = songList.remove(position);
                songAdapter.notifyItemRemoved(position);
                if (!aBoolean) {
                    songList.add(position, song);
                    songAdapter.notifyItemInserted(position);
                    isSongDeleted = false;
                } else {
                    Snackbar.make(getView(), song.getSongName() + " by " + song.getArtistName() + " has been removed", Snackbar.LENGTH_LONG)
                            .setAction("UNDO", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    songList.add(position, song);
                                    songAdapter.notifyItemInserted(position);
                                    isSongDeleted = false;
                                    callBack.onUndoDeletedSong(position,song);
                                    FileManager.writeToFile(getContext(),songList,"song_file");
                                }
                            }).show();
                }
                if(isSongDeleted){
                    callBack.onSongDeleted(position);
                }
            }
        };
        mediaPlayerViewModel.getIsSongDeletedMutableLiveData().observe(this, isSongDeletedObserver);

        Observer<Integer> onSongEditedObserver = new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                songAdapter.notifyItemChanged(integer);
            }
        };
        mediaPlayerViewModel.getEditedSongPosition().observe(this,onSongEditedObserver);

        songAdapter = new SongAdapter(this.getContext(), songList);

        songAdapter.setCallBack(new SongAdapter.OnSongListener() {
            @Override
            public void onSongClicked(int position, View view) {
                mediaPlayerViewModel.setSongMutableLiveData(songList.get(position));
                callBack.onSongSelected(position);
            }

            @Override
            public void onSongLongClicked(int position, View view) {
            }
        });

        ItemTouchHelper.SimpleCallback touchHelperCallback = new ItemTouchHelper.SimpleCallback( ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

                final int from = viewHolder.getAdapterPosition();
                final int to = target.getAdapterPosition();
                final int currentSongPosition = callBack.getCurrentServicePosition();

                Log.d("TAG", "onMove: form: " + from);
                Log.d("TAG", "onMove: to: " + to);
                Log.d("TAG", "onMove: callBack: " + callBack.getCurrentServicePosition());

                if (from == currentSongPosition) {
                    callBack.setCurrentSongPosition(to);
                } else if (from < currentSongPosition && currentSongPosition <= to ) {
                    callBack.setCurrentSongPosition(currentSongPosition - 1);
                } else if (to <= currentSongPosition && currentSongPosition < from) {
                    callBack.setCurrentSongPosition(currentSongPosition + 1);
                }


                songAdapter.notifyItemMoved(from,to);
                Song song = songList.remove(from);
                songList.add(to,song);

                mediaPlayerViewModel.setSongListOrderMutableLiveData(songList);

                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                position = viewHolder.getAdapterPosition();
                Log.d("TAG", "position: "+position);
                if (direction == ItemTouchHelper.RIGHT) {
                    callBack.onSwipeRight();
                }else if(direction == ItemTouchHelper.LEFT){
                    callBack.onSwipeLeft(position);
                }
            }

        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(touchHelperCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(songAdapter);

        return rootView;
    }

}
