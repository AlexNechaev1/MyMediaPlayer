package com.alex_nechaev.mymediaplayer;

import java.io.Serializable;

public class Song implements Serializable {
    private String imgUri;
    private String urlLink;
    private String artistName;
    private String songName;
    private String lyrics;
    private boolean isLiked;

    public Song(String imgUri,String urlLink, String artistName, String songName,String lyrics ,boolean isLiked) {
        this.imgUri = imgUri;
        this.urlLink = urlLink;
        this.artistName = artistName;
        this.songName = songName;
        this.lyrics = lyrics;
        this.isLiked = isLiked;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    public String getUrlLink() {
        return urlLink;
    }

    public void setUrlLink(String urlLink) {
        this.urlLink = urlLink;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }
}
