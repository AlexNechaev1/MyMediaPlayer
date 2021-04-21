package com.alex_nechaev.mymediaplayer.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.MutableLiveData;

import com.alex_nechaev.mymediaplayer.FileManager;
import com.alex_nechaev.mymediaplayer.MainActivity;
import com.alex_nechaev.mymediaplayer.R;
import com.alex_nechaev.mymediaplayer.Song;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MusicPlayerService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {


    private final String PLAY_AND_PAUSE = "PLAY_AND_PAUSE";
    private final String PLAY_FROM_LIST = "PLAY_FROM_LIST";
    private final String NEXT = "NEXT_SONG";
    private final String PREV = "PREV_SONG";

    private final String PLAYER_CHANNEL_ID = "CHANNEL_ID";

    private final String MEDIA_SESSION_TAG = "MEDIA_SESSION_TAG";
    private final int PLAYER_NOTIFICATION_ID = 1;

    private int currentSongProgress = 0;
    private int trackNumber = 0;
    private int maxProgress = 0;
    private int currentProgress = 0;
    private boolean isPlaying = false;
    private boolean isPausePressed = true;
    private boolean isFirstRun = true;

    PendingIntent nextPendingIntent;
    PendingIntent playPendingIntent;
    PendingIntent prevPendingIntent;
    PendingIntent activityPendingIntent;

    private List<Song> songList;

    private MutableLiveData<Boolean> isPlayingMutableLiveData;
    private MutableLiveData<Song> songMutableLiveData;
    public MutableLiveData<Integer> songDurationMutableLiveDate;
    public MutableLiveData<Integer> songCurrentProgressMutableLiveData;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private final MediaPlayer player = new MediaPlayer();

    private final Handler handler = new Handler();
    private Runnable runnable;

    NotificationManager notificationManager;
    private NotificationCompat.Builder builder;
    private MediaSessionCompat mediaSessionCompat;

    public void setNewProgress(int progress) {
        if (player.isPlaying()) {
            currentSongProgress = progress;
            currentProgress = progress;
            setNotificationCurrentProgress(currentSongProgress);
            updateNotification();
            player.seekTo(progress);
        }
    }

    private Bitmap notificationIconPic;

    public int getCurrentPosition() {
        return trackNumber;
    }

    public void setCurrentServicePosition(int currentSongPosition) {
        if (currentSongPosition >= 0) {
            trackNumber = currentSongPosition;
        }
    }

    public class ServiceBinder extends Binder {
        public MusicPlayerService getService() {
            return MusicPlayerService.this;
        }
    }

    private final IBinder binder = new ServiceBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        player.setOnCompletionListener(this);
        player.setOnPreparedListener(this);
        player.reset();

        mediaSessionCompat = new MediaSessionCompat(this, MEDIA_SESSION_TAG);

        sp = this.getSharedPreferences("frag_state", MODE_PRIVATE);

        trackNumber = sp.getInt("track_number", 0);

        //<------- Song list init vis File Input Stream ------->//
        songList = null;
        songList = (List<Song>) FileManager.readFromFile(this, "song_list");
        if (songList == null) {
            songList = new ArrayList<>();
        }


        runnable = new Runnable() {
            @Override
            public void run() {
                if (player.isPlaying()) {
                    currentProgress = player.getCurrentPosition();
                    songCurrentProgressMutableLiveData.setValue(currentProgress);
                }
                handler.postDelayed(runnable, 100);
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //TODO shared preference to trackNumber

        int position = intent.getIntExtra("position", trackNumber);
        String commend = intent.getStringExtra("commend");

        editor = sp.edit();
        editor.putInt("track_number", trackNumber).apply();

        onSwitchCommend(commend, position);

        return super.onStartCommand(intent, flags, startId);
    }


    public void onSwitchCommend(String commend, int position) {
        Song song = null;

        switch (commend) {
            case PLAY_AND_PAUSE:
                if (player.isPlaying()) {

                    editor = sp.edit();
                    editor.putBoolean("is_playing", false).apply();

                    isPlaying = false;
                    isPausePressed = true;
                    player.pause();
                    currentSongProgress = player.getCurrentPosition();
                    player.reset();
                    isPlayingMutableLiveData.setValue(player.isPlaying());

                    PlaybackState.Builder playerState = new PlaybackState.Builder().setState(PlaybackState.STATE_PAUSED, currentSongProgress, 0);
                    mediaSessionCompat.setPlaybackState(PlaybackStateCompat.fromPlaybackState(playerState.build()));

                    if (!isFirstRun) {
                        updateNotification();
                    } else {
                        foregroundNotificationInit();
                        isFirstRun = false;
                    }

                    if(!sp.getBoolean("is_main_running",true)) {
                        stopForeground(false);
                    }

                } else {
                    position = (position % songList.size());
                    song = songList.get(position);
                    songMutableLiveData.setValue(song);
                    trackNumber = position;


                    try {
                        player.setDataSource(song.getUrlLink());
                        player.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                break;
            case PLAY_FROM_LIST:

                currentSongProgress = 0;

                //< --- sets the position in bounds of the size of the song list ---- >//
                trackNumber = position % songList.size();
                song = songList.get(trackNumber);

                if (player.isPlaying()) {
                    player.stop();
                    player.reset();
                }

                try {
                    if (song != null) {
                        songMutableLiveData.setValue(song);
                        player.setDataSource(song.getUrlLink());
                        player.prepareAsync();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case NEXT:

                //< ---- resets the song progress to position zero --- >//
                currentSongProgress = 0;

                //< ---- gets next song in the list in range of the list size ---- > //
                trackNumber = (++trackNumber) % songList.size();
                song = songList.get(trackNumber);

                player.stop();
                player.reset();

                //< ---- updates the UI with the new song attributes --- >//
                songMutableLiveData.setValue(song);

                try {
                    player.setDataSource(song.getUrlLink());
                    player.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case PREV:

                //< ---- resets the song progress to position zero --- >//
                currentSongProgress = 0;

                //< ---- when we hit the first song and the previous one we get the last song in the song list ---- >//
                if (trackNumber == 0) {
                    trackNumber = songList.size();
                }

                //< ---- gets previous song in the list ---- >//
                song = songList.get((--trackNumber) % songList.size());

                player.stop();
                player.reset();

                //< ---- updates the UI with the new song attributes --- >//
                songMutableLiveData.setValue(song);

                try {
                    player.setDataSource(song.getUrlLink());
                    player.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
        //< ---- if there is a selected song -> save it's attribute in the shared preference ---- >//
        if (song != null) {
            loadPicture(song);
            editor = sp.edit();
            editor
                    .putString("song_name", song.getSongName())
                    .putString("artist_name", song.getArtistName())
                    .putString("album_img", song.getImgUri())
                    .putString("song_lyrics",song.getLyrics())
                    .putInt("controller_visibility", 0)
                    .apply();
        }
    }


    @Override
    public void onPrepared(MediaPlayer mp) {

        setNotificationCurrentProgress(currentSongProgress);

        player.seekTo(currentSongProgress);
        player.start();

        currentProgress = player.getCurrentPosition();
        maxProgress = player.getDuration();
        songDurationMutableLiveDate.setValue(maxProgress);

        editor = sp.edit();
        editor
                .putBoolean("is_playing", true)
                .putBoolean("is_service_running", true)
                .apply();

        isPlaying = true;
        isPausePressed = false;
        isPlayingMutableLiveData.setValue(player.isPlaying());

        if (!isFirstRun) {
            updateNotification();
        } else {
            foregroundNotificationInit();
            isFirstRun = false;
        }

        onRequestSongStatus();
    }

    private void setNotificationCurrentProgress(int currentSongDuration) {
        PlaybackState.Builder playerState = new PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING, currentSongDuration, 1);
        mediaSessionCompat.setPlaybackState(PlaybackStateCompat.fromPlaybackState(playerState.build()));
    }

    public void onRequestSongStatus() {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
            runnable.run();
        }
    }

    private void foregroundNotificationInit() {

        Intent activityIntent = new Intent(this, MainActivity.class);
        activityPendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent prevIntent = new Intent(this, MusicPlayerService.class);
        prevIntent.putExtra("commend", PREV);
        prevPendingIntent = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent playIntent = new Intent(this, MusicPlayerService.class);
        playIntent.putExtra("commend", PLAY_AND_PAUSE);
        playPendingIntent = PendingIntent.getService(this, 2, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, MusicPlayerService.class);
        nextIntent.putExtra("commend", NEXT);
        nextPendingIntent = PendingIntent.getService(this, 3, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel notificationChannel = new NotificationChannel(PLAYER_CHANNEL_ID, "Notification_name", NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);
            notificationChannel.setSound(null, null);
            notificationManager.createNotificationChannel(notificationChannel);

        }
        //< ---- Calls the builder for update all the notification's attribute ---- >//
        updateNotificationBuilder();
        //< ---- Starts the notification as a foreground service ***first run only*** ---- >//
        startForeground(PLAYER_NOTIFICATION_ID, builder.build());
    }

    public void updateNotificationBuilder() {
        MediaMetadata.Builder mediaMetadata = new MediaMetadata.Builder().putLong(MediaMetadata.METADATA_KEY_DURATION, player.getDuration());
        mediaSessionCompat.setMetadata(MediaMetadataCompat.fromMediaMetadata(mediaMetadata.build()));

        builder = new NotificationCompat.Builder(this, PLAYER_CHANNEL_ID);

        builder.setSmallIcon(R.drawable.icon_forground)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentTitle(songList.get(trackNumber).getSongName())
                .setContentText(songList.get(trackNumber).getArtistName())
                .setLargeIcon(notificationIconPic)
                .setContentIntent(activityPendingIntent)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .addAction(R.drawable.btn_notification_previous, "prev", prevPendingIntent)
                .addAction(isPlaying ? R.drawable.btn_notification_pause : R.drawable.btn_notification_play, isPlaying ? "pause" : "play", playPendingIntent)
                .addAction(R.drawable.btn_notification_next, "next", nextPendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSessionCompat.getSessionToken()));
    }

    public void updateNotification() {
        //< ---- Calls the builder for update all the notification's attribute ---- >//
        updateNotificationBuilder();
        notificationManager.notify(PLAYER_NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (!isPausePressed) {
            onSwitchCommend(NEXT, 0);
            if (!player.isPlaying()) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                stopSelf();
            }
        }
    }

    public void setSongList(List<Song> songArrayList) {
        this.songList = songArrayList;
        updateNotification();
    }

    public void loadPicture(Song song) {
        Glide
                .with(this)
                .asBitmap()
                .load(song.getImgUri())
                .placeholder(R.drawable.music_album_icon)
                .into(new CustomTarget<Bitmap>(150, 150) {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        notificationIconPic = resource;
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "onDestroy: ");
        editor = sp.edit();
        editor
                .putInt("controller_visibility", 8)
                .putBoolean("is_service_running", false)
                .apply();
    }

    public void setMutableLiveData() {
        isPlayingMutableLiveData = new MutableLiveData<>();
        songMutableLiveData = new MutableLiveData<>();
        songDurationMutableLiveDate = new MutableLiveData<>();
        songCurrentProgressMutableLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<Boolean> getIsPlayingMutableLiveData() {
        return isPlayingMutableLiveData;
    }

    public MutableLiveData<Song> getSongMutableLiveData() {
        return songMutableLiveData;
    }

    public MutableLiveData<Integer> getSongDurationMutableLiveData() {
        return songDurationMutableLiveDate;
    }

    public MutableLiveData<Integer> getSongProgressMutableLiveData() {
        return songCurrentProgressMutableLiveData;
    }


}
