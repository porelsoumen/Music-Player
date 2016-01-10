package com.corp.sporel.musicplayer;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sporel on 12/13/15.
 */
public class PlayService extends Service implements  MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener{
    MediaPlayer player = null;
    private final String ACTION_PLAY = "com.sporel.action.play";
    private final String ACTION_PAUSE = "com.sporel.action.pause";
    private final String ACTION_NEXT = "com.sporel.action.next";
    private final String ACTION_PREV = "com.sporel.action.prev";

    ArrayList<ListItem> songs;
    Long songId = -1l;
    private String currentAction;
    private final IBinder mBinder = new LocalBinder();

    /**
     * Called when the end of a media source is reached during playback.
     *
     * @param mp the MediaPlayer that reached the end of the file
     */
    @Override
    public void onCompletion(MediaPlayer mp) {
        ListItem song = new ListItem();
        song.setId(songId);

        int pos = songs.indexOf(song);
        songId = songs.get((pos+1)%songs.size()).getId();

        playSong(songId);
    }

    public class LocalBinder extends Binder{
        PlayService getService()
        {
            return PlayService.this;
        }
    }

    public void onCreate(){
        super.onCreate();
        songs = new GetSongs(getBaseContext()).getAllSongs();
        player = new MediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
    }

    public int onStartCommand(Intent intent, int flags, int startId){

        switch (intent.getAction())
        {
            case ACTION_PLAY:
                if (songId != intent.getLongExtra("id",0l))
                {
                    songId = intent.getLongExtra("id",0l);
                    currentAction = intent.getAction();

                    playSong(songId);
                }
                else
                {
                    player.start();
                }
                break;
            case ACTION_PAUSE:
                if (player != null)
                {
                    player.pause();
                }
                break;

            case ACTION_NEXT:
                songId = songs.get(intent.getIntExtra("pos",-1)+2).getId();

                playSong(songId);

            case ACTION_PREV:
                songId = songs.get(intent.getIntExtra("pos",-1)-2).getId();

                playSong(songId);
        }

        return 1;
    }

    private void playSong(Long id)
    {
       Uri uri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

        if (player == null)
        {
            player = new MediaPlayer();
            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        }
        else
        {
            player.reset();
        }
        try {
            player.setDataSource(getApplicationContext(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.prepareAsync();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        player.start();
    }

    @Override
    public void onDestroy() {
        if (player != null) player.release();
        //stopSelf();
    }

    public int getCurrentPosition()
    {
        return player.getCurrentPosition();
    }

    public int getDuration()
    {
        return player.getDuration();
    }

    public MediaPlayer getPlayer()
    {
        return player;
    }
}
