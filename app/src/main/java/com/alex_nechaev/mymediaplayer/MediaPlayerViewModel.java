package com.alex_nechaev.mymediaplayer;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

public class MediaPlayerViewModel extends ViewModel {

    private MutableLiveData<Boolean> booleanMutableLiveData = new MutableLiveData<>();

    private MutableLiveData<Song> songMutableLiveData = new MutableLiveData<>();

    private MutableLiveData<List<Song>> songListMutableLiveData = new MutableLiveData<>();

    private MutableLiveData<List<Song>> songListOrderMutableLiveData = new MutableLiveData<>();

    private MutableLiveData<Integer> songPositionMutableLiveData = new MutableLiveData<>();

    private MutableLiveData<Integer> songDurationMutableLiveData = new MutableLiveData<>();

    private MutableLiveData<Boolean> isSongDeletedMutableLiveData = new MutableLiveData<>();

    private MutableLiveData<Song> onSongAddedMutableLiveData = new MutableLiveData<>();

    private MutableLiveData<Integer> editedSongMutableLiveDate = new MutableLiveData<>();

    public void setPlayingMutableLiveData(Boolean isPlaying) {
        booleanMutableLiveData.setValue(isPlaying);
    }

    public void setSongMutableLiveData(Song song){
        songMutableLiveData.setValue(song);
    }

    public void setSongListMutableLiveData(List<Song> songList){
        songListMutableLiveData.setValue(songList);
    }

    public void setSongListOrderMutableLiveData(List<Song> songList){
        songListOrderMutableLiveData.setValue(songList);
    }

    public void setSongPositionMutableLiveData(Integer songCurrentPosition){
        songPositionMutableLiveData.setValue(songCurrentPosition);
    }

    public void setSongDurationMutableLiveData(Integer songDuration){
        songDurationMutableLiveData.setValue(songDuration);
    }

    public void setNewSongAddedMutableLiveData(Song song){
        onSongAddedMutableLiveData.setValue(song);
    }

    public void setIsSongDeletedMutableLiveData(Boolean isDeleted){
        isSongDeletedMutableLiveData.setValue(isDeleted);
    }

    public void setEditedSongMutableLiveData(Integer songPosition){
        editedSongMutableLiveDate.setValue(songPosition);
    }

    public MutableLiveData<Boolean> isPlaying(){
        return booleanMutableLiveData;
    }

    public MutableLiveData<Song> getSongMutableLiveData(){
        return songMutableLiveData;
    }

    public MutableLiveData<List<Song>> getSongListMutableLiveData(){
        return songListMutableLiveData;
    }

    public MutableLiveData<List<Song>> getSongListOrderMutableLiveData(){
        return songListOrderMutableLiveData;
    }

    public MutableLiveData<Integer> getSongPositionMutableLiveData(){
        return songPositionMutableLiveData;
    }

    public MutableLiveData<Integer>getSongDurationMutableLiveData(){
        return songDurationMutableLiveData;
    }

    public MutableLiveData<Boolean> getIsSongDeletedMutableLiveData(){
        return isSongDeletedMutableLiveData;
    }

    public MutableLiveData<Song> getNewSongAddedMutableLiveData(){
        return onSongAddedMutableLiveData;
    }

    public MutableLiveData<Integer> getEditedSongPosition(){
        return editedSongMutableLiveDate;
    }
}
