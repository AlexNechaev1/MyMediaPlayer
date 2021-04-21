package com.alex_nechaev.mymediaplayer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.alex_nechaev.mymediaplayer.fragments.AddSongFragment;
import com.alex_nechaev.mymediaplayer.fragments.BigPlayerContainerFragment;
import com.alex_nechaev.mymediaplayer.fragments.EditSongFragment;
import com.alex_nechaev.mymediaplayer.fragments.MainContainerFragment;
import com.alex_nechaev.mymediaplayer.fragments.MediaPlayerController;
import com.alex_nechaev.mymediaplayer.fragments.SongDeleteFragment;
import com.alex_nechaev.mymediaplayer.fragments.SongListFragment;
import com.alex_nechaev.mymediaplayer.fragments.SongLyricsFragment;
import com.alex_nechaev.mymediaplayer.services.MusicPlayerService;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MediaPlayerController.OnMediaPlayerControllerListener, BigPlayerContainerFragment.OnBigPlayerListener,
        SongListFragment.onSongListListener, AddSongFragment.OnAddSong, SongDeleteFragment.OnSongDeleteListener, EditSongFragment.OnEditSongListener, SongLyricsFragment.OnSongLyricsListener {

    //< ---- INTENTS TAG ---- >//
    private final String PLAY_AND_PAUSE = "PLAY_AND_PAUSE";
    private final String PLAY_FROM_LIST = "PLAY_FROM_LIST";
    private final String NEXT = "NEXT_SONG";
    private final String PREV = "PREV_SONG";
    private final String SONG_LIST_TAG = "SONG_LIST_TAG";

    //< ---- FRAGMENTS TAG ---- >//
    private final String ADD_SONG_FRAGMENT_TAG = "ADD_SONG_FRAGMENT_TAG";
    private final String DELETE_SONG_FRAGMENT_TAG = "DELETE_SONG_FRAGMENT_TAG";
    private final String EDIT_SONG_FRAGMENT_TAG = "EDIT_SONG_FRAGMENT_TAG";
    private final String SONG_LYRICS_FRAGMENT_TAG = "SONG_LYRICS_FRAGMENT_TAG";

    //< ---- Init for snackBack ---- >//
    private CoordinatorLayout coordinatorLayout;

    private MusicPlayerService playerService;
    private boolean isServiceBounded = false;

    //< ---- MVVM ---- >//
    public MediaPlayerViewModel mediaPlayerViewModel;

    private List<Song> songList;
    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sp = getSharedPreferences("frag_state", MODE_PRIVATE);

        spEditor = sp.edit();
        spEditor.putBoolean("is_main_running",true).apply();


        final String testSongLink0 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
        final String testSongLink1 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3";
        final String testSongLink2 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3";
        final String testSongLink3 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3";
        final String testSongLink4 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3";
        final String testSongLink5 = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3";
        final String testSongLink6 = "https://syntax.org.il/xtra/bob.m4a";
        final String testSongLink7 = "https://www.syntax.org.il/xtra/bob1.m4a";
        final String testSongLink9 = "https://freesound.org/data/previews/231/231341_3908940-lq.mp3";
        final String testSongLink10 = "https://freesound.org/data/previews/395/395487_2305278-lq.mp3";

        final String testImgLink0 = "https://dianearkenstone.com/wp-content/uploads/2019/07/trance-music-diane-arkenstone.jpg";
        final String testImgLink1 = "https://f4.bcbits.com/img/a4061981966_10.jpg";
        final String testImgLink2 = "https://i.pinimg.com/originals/06/5c/b3/065cb3a02ee15b1e7aeb9d3bef4e547a.jpg";
        final String testImgLink3 = "https://www.music-bazaar.com/album-images/vol31/1138/1138960/3006067-big/Trance-Music-Energy-Andromeda-CD1-cover.jpg";
        final String testImgLink4 = "https://f4.bcbits.com/img/a1685828922_10.jpg";
        final String testImgLink5 = "https://i.kfs.io/album/global/36847757,0v1/fit/500x500.jpg";
        final String testImgLink6 = "https://www.needsomefun.net/wp-content/uploads/2015/09/one_more_cup_of_cofee.jpg";
        final String testImgLink7 = "https://images.rapgenius.com/f9fa75848596b53395fe7f6ccd25844c.619x414x1.jpg";
        final String testImgLink9 = "https://d1csarkz8obe9u.cloudfront.net/posterpreviews/guitar-album-cover-design-template-61196f501ed1c2cc33291f31586b1565_screen.jpg";
        final String testImgLink10 = "https://e-cdns-images.dzcdn.net/images/cover/ce12269c4c746cca2c5e93dc217e2e6a/264x264.jpg";

        coordinatorLayout = findViewById(R.id.myCoordinatorLayout);

        mediaPlayerViewModel = new ViewModelProvider(this).get(MediaPlayerViewModel.class);

        MainContainerFragment mainContainerFragment = new MainContainerFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.main_container, mainContainerFragment, SONG_LIST_TAG).commit();

        songList = (ArrayList<Song>) FileManager.readFromFile(this, "song_list");

        if (songList == null) {

            songList = new ArrayList<>();

            songList.add(new Song(testImgLink0, testSongLink0, "T. Schürger", "SoundHelix Song 1", "", false));
            songList.add(new Song(testImgLink1, testSongLink1, "T. Schürger", "SoundHelix Song 2", "", false));
            songList.add(new Song(testImgLink2, testSongLink2, "T. Schürger", "SoundHelix Song 3", "", false));
            songList.add(new Song(testImgLink3, testSongLink3, "T. Schürger", "SoundHelix Song 4", "", false));
            songList.add(new Song(testImgLink4, testSongLink4, "T. Schürger", "SoundHelix Song 5", "", false));
            songList.add(new Song(testImgLink5, testSongLink5, "T. Schürger", "SoundHelix Song 6", "", false));
            songList.add(new Song(testImgLink6, testSongLink6, "Bob Dylan", "One More Cup Of Coffee", lyrics1, true));
            songList.add(new Song(testImgLink7, testSongLink7, "Bob Dylan", "Sara", lyrics2, true));
            songList.add(new Song(testImgLink9, testSongLink9, "Valentin Sosnitskiy", "Etude of Electric Guitar in Dm", "", false));
            songList.add(new Song(testImgLink10, testSongLink10, "Frankum & Frankumjay", "Tecno pop base and guitar 2", "", false));

            //< ---- Save Song list vis File Input Stream ---- >//
            //FileManager.writeToFile(this, songList, "song_list");
        }
        //< ---- Update the recyclerView ---- >//
        mediaPlayerViewModel.setSongListMutableLiveData(songList);

        Observer<List<Song>> listObserver = new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> songs) {
                FileManager.writeToFile(MainActivity.this, songs, "song_list");
            }
        };
        mediaPlayerViewModel.getSongListMutableLiveData().observe(this,listObserver);

        Observer<Song> onNewSongAdded = new Observer<Song>() {
            @Override
            public void onChanged(Song song) {
                songList.add(song);
                if(playerService != null){
                    playerService.setSongList(songList);
                }
                FileManager.writeToFile(MainActivity.this, songList, "song_list");
            }
        };
        mediaPlayerViewModel.getNewSongAddedMutableLiveData().observe(this,onNewSongAdded);

        Observer<List<Song>> listOrderObserver = new Observer<List<Song>>() {
            @Override
            public void onChanged(List<Song> songs) {
                if(playerService != null){
                    playerService.setSongList(songList);
                }
                songList.clear();
                songList.addAll(songs);
                FileManager.writeToFile(MainActivity.this, songList, "song_list");
            }
        };
        mediaPlayerViewModel.getSongListOrderMutableLiveData().observe(this,listOrderObserver);
    }


    //< ---------- Intents to MusicPlayerService ---------- >//
    private void playPauseSong() {
        Intent intent = new Intent(MainActivity.this, MusicPlayerService.class);
        intent.putExtra("commend", PLAY_AND_PAUSE);
        startService(intent);
    }

    public void playFromList(int position) {
        Intent intent = new Intent(MainActivity.this, MusicPlayerService.class);
        intent.putExtra("commend", PLAY_FROM_LIST).putExtra("position", position);
        startService(intent);
    }

    private void nextSong() {
        Intent intent = new Intent(MainActivity.this, MusicPlayerService.class);
        intent.putExtra("commend", NEXT);
        startService(intent);
    }

    private void previousSong() {
        Intent intent = new Intent(MainActivity.this, MusicPlayerService.class);
        intent.putExtra("commend", PREV);
        startService(intent);
    }
    //< ---------- END Intents to MusicPlayerService ---------- >//




    //< ---------- Service Binding ---------- >//
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            //<----- initialize player service and set Context if the service isn't null ----->//
            playerService = ((MusicPlayerService.ServiceBinder) service).getService();
            if (playerService != null) {

                //<----- sets new instances of Mutable live data in MusicPlayerService class ----->//
                playerService.setMutableLiveData();
            }

            //< ---------- Service Observers ---------->//
            Observer<Boolean> isMusicPlayingObserver = new Observer<Boolean>() {
                @Override
                public void onChanged(Boolean aBoolean) {
                    mediaPlayerViewModel.setPlayingMutableLiveData(aBoolean);
                }
            };
            playerService.getIsPlayingMutableLiveData().observe(MainActivity.this, isMusicPlayingObserver);


            Observer<Song> isSongChangedObserver = new Observer<Song>() {
                @Override
                public void onChanged(Song song) {
                    mediaPlayerViewModel.setSongMutableLiveData(song);
                }
            };
            playerService.getSongMutableLiveData().observe(MainActivity.this, isSongChangedObserver);

            Observer<Integer> songDurationObserver = new Observer<Integer>() {
                @Override
                public void onChanged(Integer integer) {
                    mediaPlayerViewModel.setSongDurationMutableLiveData(integer);
                }
            };
            playerService.getSongDurationMutableLiveData().observe(MainActivity.this, songDurationObserver);

            Observer<Integer> songCurrentPositionObserver = new Observer<Integer>() {
                @Override
                public void onChanged(Integer integer) {
                    mediaPlayerViewModel.setSongPositionMutableLiveData(integer);
                }
            };
            playerService.getSongProgressMutableLiveData().observe(MainActivity.this, songCurrentPositionObserver);

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playerService = null;
        }
    };

    //MusicPlayerService Binding methods
    private void doBindService() {
        if (!isServiceBounded) {
            bindService(new Intent(this, MusicPlayerService.class), serviceConnection, Context.BIND_AUTO_CREATE);
            isServiceBounded = true;
        }
    }

    private void doUnbindService() {
        if (isServiceBounded) {
            unbindService(serviceConnection);
            isServiceBounded = false;
        }
    }
    //< ---------- END Service Binding ---------- >//




    //< ---------- SongListFragment CallBacks ---------- >//
    @Override
    public void onAddSong() {
        AddSongFragment addSongFragment = new AddSongFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.main_container, addSongFragment, ADD_SONG_FRAGMENT_TAG).addToBackStack(null).commit();
    }

    @Override
    public void onSwipeRight() {
        SongDeleteFragment songDeleteFragment = SongDeleteFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.main_container, songDeleteFragment, DELETE_SONG_FRAGMENT_TAG).addToBackStack(null).commit();
    }

    @Override
    public void onSwipeLeft(int position) {
        Song song = songList.get(position);
        EditSongFragment editSongFragment = EditSongFragment.newInstance(song.getImgUri(), song.getUrlLink(), song.getArtistName(), song.getSongName(), song.getLyrics(),position);
        getSupportFragmentManager().beginTransaction().add(R.id.main_container, editSongFragment, EDIT_SONG_FRAGMENT_TAG).addToBackStack(null).commit();
    }

    @Override
    public void onSongDeleted(int position) {
        songList.remove(position);
        if(isServiceBounded) {
            playerService.setSongList(songList);
        }
        FileManager.writeToFile(this, songList, "song_list");
    }

    @Override
    public void onUndoDeletedSong(int position, Song song) {
        songList.add(position, song);
        playerService.setSongList(songList);
        FileManager.writeToFile(this, songList, "song_list");

    }

    @Override
    public int getCurrentServicePosition() {
        int position = -1;
        if(playerService!=null) {
            position = playerService.getCurrentPosition();
        }
        return (position != -1)?position:0;
    }

    @Override
    public void setCurrentSongPosition(int currentSongPosition) {
        if(playerService!=null) {
            playerService.setCurrentServicePosition(currentSongPosition);
        }
    }
    //< ---------- END SongListFragment CallBacks ---------- >//




    //< ---------- AddSongFragment CallBacks ---------- >//
    @Override
    public void onNewAddSong(String songName, String artistName, String imgUri, String songLink, String lyrics) {
        Song song = new Song(imgUri, songLink, artistName, songName, lyrics, false);
        mediaPlayerViewModel.setNewSongAddedMutableLiveData(song);
    }

    @Override
    public void onSnackBarRequest(String message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onCloseFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ADD_SONG_FRAGMENT_TAG);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            getSupportFragmentManager().popBackStack();
        }

    }
    //< ---------- END AddSongFragment CallBacks ---------- >//




    //< ---------- EditSongFragment CallBacks ---------- >//
    @Override
    public void onUpdateSong(String songName, String artistName, String imgUri, String songLink, String lyrics, int songPosition) {

        Log.d("TAG", "Song link:"+songLink+"\nImg link:"+imgUri);

        songList.get(songPosition).setSongName(songName);
        songList.get(songPosition).setArtistName(artistName);
        songList.get(songPosition).setImgUri(imgUri);
        songList.get(songPosition).setUrlLink(songLink);
        songList.get(songPosition).setLyrics(lyrics);

        mediaPlayerViewModel.setSongMutableLiveData(songList.get(songPosition));
        mediaPlayerViewModel.setEditedSongMutableLiveData(songPosition);

        FileManager.writeToFile(this,songList,"song_list");
        playerService.setSongList(songList);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(EDIT_SONG_FRAGMENT_TAG);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onCloseEditFragment(int songPosition) {
        mediaPlayerViewModel.setEditedSongMutableLiveData(songPosition);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(EDIT_SONG_FRAGMENT_TAG);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            getSupportFragmentManager().popBackStack();
        }
    }
    //< ---------- END EditSongFragment CallBacks ---------- >//




    //< ---------- SongDeleteFragment CallBacks ---------- >//
    //< ---- Comes from "SongDeleteFragment" and changes the UI in "SongListFragment" ---- >//
    //< ---- "User pressed response "YES' on "Delete Song?" ---- >//
    @Override
    public void onYesPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(DELETE_SONG_FRAGMENT_TAG);
        if (fragment != null) {
            mediaPlayerViewModel.setIsSongDeletedMutableLiveData(true);
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            getSupportFragmentManager().popBackStack();
        }
    }

    //< ---- Comes from "SongDeleteFragment" and changes the UI in "SongListFragment" ---- >//
    //< ---- "User pressed response "NO' on "Delete Song?" ---- >//
    @Override
    public void onNoPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(DELETE_SONG_FRAGMENT_TAG);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            getSupportFragmentManager().popBackStack();
            mediaPlayerViewModel.setIsSongDeletedMutableLiveData(false);
        }
    }
    //< ---------- END SongDeleteFragment CallBacks ---------- >//




    //< ---------- BigPlayerContainerFragment or MediaPlayerController CallBacks ---------- >//
    @Override
    public void onPrevious() {
        previousSong();
    }

    @Override
    public void onNext() {
        nextSong();
    }

    @Override
    public void onPlayPause() {
        playPauseSong();
    }

    @Override
    public void onSongSelected(int position) {
        playFromList(position);
    }



    @Override
    public void onChangeSongProgress(int progress) {
        playerService.setNewProgress(progress);
    }

    @Override
    public void onLyricsPressed(String songName, String songArtist, String songLyrics) {
        SongLyricsFragment songLyricsFragment = SongLyricsFragment.newInstance(songName,songArtist,songLyrics);
        getSupportFragmentManager().beginTransaction().add(R.id.main_container, songLyricsFragment, SONG_LYRICS_FRAGMENT_TAG).addToBackStack(null).commit();
    }
    //< ---------- END BigPlayerContainerFragment or MediaPlayerController CallBacks ---------- >//




    //< ---------- SongLyricsFragment CallBack ---------- >//
    @Override
    public void onCloseLyrics() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(SONG_LYRICS_FRAGMENT_TAG);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            getSupportFragmentManager().popBackStack();
        }
    }
    //< ---------- END SongLyricsFragment CallBack ---------- >//


    @Override
    protected void onStart() {
        super.onStart();
        doBindService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        doUnbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        spEditor = sp.edit();
        spEditor.putBoolean("is_main_running",false).apply();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //<------- Save Song list vis File Input Stream ------->//
        FileManager.writeToFile(this, songList, "song_list");
    }

    String lyrics1 = "Your breath is sweet\n" +
            "Your eyes are like two jewels in the sky\n" +
            "Your back is straight, your hair is smooth\n" +
            "On the pillow where you lie\n" +
            "I don't sense affection\n" +
            "Nor no gratitude or love\n" +
            "Your loyalty is not to me but to the stars above\n" +
            "One more cup of coffee for the road\n" +
            "One more cup of coffee before I go\n" +
            "To the valley below\n" +
            "Your daddy, he's an outlaw\n" +
            "And a wanderer by trade\n" +
            "He'll teach you how to pick an' choose\n" +
            "And how to throw the blade\n" +
            "He oversees his kingdom\n" +
            "Where no stranger does intrude\n" +
            "His voice it trembles as he calls out\n" +
            "For another plate of food\n" +
            "One more cup of coffee for the road\n" +
            "One more cup of coffee before I go\n" +
            "To the valley below\n" +
            "Your sister sees the future\n" +
            "Like your momma and yourself\n" +
            "She never learned to read or write\n" +
            "There's no books upon her shelf\n" +
            "And her pleasure knows no limits\n" +
            "Her voice is like a meadow lark\n" +
            "But her heart is like an ocean\n" +
            "So mysterious and dark\n" +
            "One more cup of coffee for the road\n" +
            "One more cup of coffee before I go\n" +
            "To the valley below\n" +
            "Thank you";

    String lyrics2 = "I laid on a dune, I looked at the sky\n" +
            "When the children were babies and played on the beach.\n" +
            "You came up behind me, I saw you go by\n" +
            "You were always so close and still within reach.\n" +
            "Sara, Sara\n" +
            "Whatever made you want to change your mind?\n" +
            "Sara, Sara\n" +
            "So easy to look at, so hard to define.\n" +
            "I can still see them playin' with their pails in the sand\n" +
            "They run to the water their buckets to fill.\n" +
            "I can still see the shells fallin' out of their hands\n" +
            "As they follow each other back up the hill.\n" +
            "Sara, Sara\n" +
            "Sweet virgin angel, sweet love of my life\n" +
            "Sara, Sara\n" +
            "Radiant jewel, a mystical wife.\n" +
            "Sleepin' in the woods by a fire in the night\n" +
            "Drinkin' white rum in a Portugal bar\n" +
            "Them playin' leapfrog and hearin' about Snow White\n" +
            "You in the marketplace in Savanna-la-Mar.\n" +
            "Sara, Sara\n" +
            "It's all so clear, I could never forget\n" +
            "Sara, Sara\n" +
            "Lovin' you is the one thing I'll never regret.\n" +
            "I can still hear the sounds of those Methodist bells\n" +
            "I'd taken the cure and had just gotten through\n" +
            "Stayin' up for days in the Chelsea Hotel\n" +
            "Writin' \"Sad-Eyed Lady of the Lowlands\" for you.\n" +
            "Sara, Sara\n" +
            "Wherever we travel we're never apart.\n" +
            "Sara, oh Sara\n" +
            "Beautiful lady, so dear to my heart.\n" +
            "How did I meet you? I don't know.\n" +
            "A messenger sent me in a tropical storm.\n" +
            "You were there in the winter, moonlight on the snow\n" +
            "And on Lily Pond Lane when the weather was warm.\n" +
            "Sara, oh Sara\n" +
            "Scorpio Sphinx in a calico dress\n" +
            "Sara, Sara\n" +
            "Ya must forgive me my unworthiness.\n" +
            "Now the beach is deserted except for some kelp\n" +
            "And a piece of an old ship that lies on the shore.\n" +
            "You always responded when I needed your help\n" +
            "You gave me a map and a key to your door.\n" +
            "Sara, oh Sara\n" +
            "Glamorous nymph with an arrow and bow\n" +
            "Sara, oh Sara\n" +
            "Don't ever leave me, don't ever go.";
}