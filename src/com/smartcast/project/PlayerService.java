package com.smartcast.project;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.Toast;
// this is the various listeners needed here
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;

import java.io.IOException;

//player needs to extend all of thes listeners
public class PlayerService extends Service implements OnCompletionListener, OnBufferingUpdateListener, OnErrorListener,
        OnInfoListener, OnSeekCompleteListener, OnPreparedListener{
    // backround service of the player
    // the media player
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String setPodcastLink;
    private String currentPodcast;// current podcast playing


    public void onCreate(){

        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnErrorListener(this);
        mediaPlayer.setOnBufferingUpdateListener(this);
        mediaPlayer.setOnInfoListener(this);
        mediaPlayer.setOnSeekCompleteListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.reset();

    }
    /*===============================================================================
     ===============================================================================
    - ON START COMMAND

    ===============================================================================
    ================================================================================*/
    public int onStartCommand(Intent intent, int flags, int startId){

        // looks in the folder the rder it came i to start playing
        currentPodcast = intent.getExtras().getString("nextOnQueue");
        mediaPlayer.reset();// reset the player
        Toast.makeText(this, "Player has started", Toast.LENGTH_LONG).show();

        //setup media player using the link
        if(!mediaPlayer.isPlaying()){
            try{
                mediaPlayer.setDataSource(currentPodcast);
                mediaPlayer.prepareAsync();// making it async so its not holding up resources
               // mediaPlayer.start();
            }
            catch(IOException e){
                e.printStackTrace();
            }
            catch(IllegalArgumentException e){
                e.printStackTrace();
            }
            catch(IllegalStateException e){
                e.printStackTrace();
            }
        }
        return START_STICKY;
    }

    /*===============================================================================
     ===============================================================================
    - ON DESTROY COMMAND

    ===============================================================================
    ================================================================================*/
    public void onDestroy(){

        super.onDestroy();
        if(mediaPlayer != null){
            if(mediaPlayer.isPlaying()){
                mediaPlayer.stop();
            }
            mediaPlayer.release();// releases all memory media player is holding
        }
        Toast.makeText(this, "Player has stopped", Toast.LENGTH_LONG).show();
    }


    /*===============================================================================
   ===============================================================================
  - ON BUFFERING METHOD

  ===============================================================================
  ================================================================================*/
    public void onBufferingUpdate(MediaPlayer mp, int arg0){

    }


    /*===============================================================================
     ===============================================================================
    - ON COMPLETION METHOD

    ===============================================================================
    ================================================================================*/
    @Override
    public void onCompletion(MediaPlayer mp){

        stopPodcast();
        stopSelf();// stop the service

    }
    /*===============================================================================
     ===============================================================================
    - ON ERROR METHOD
    - error handlers
    ===============================================================================
    ================================================================================*/
    public boolean onError(MediaPlayer mp, int arg0, int arg1){
        switch(arg0){
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Toast.makeText(this, "Media not valid for progressive playback " + arg1, Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Toast.makeText(this, "Media error unknown" + arg1, Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                Toast.makeText(this, "Session Timed out" + arg1, Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                Toast.makeText(this, "Media not supported" + arg1, Toast.LENGTH_SHORT).show();
                break;
            case MediaPlayer.MEDIA_ERROR_IO:
                Toast.makeText(this, "Media ran into IO error" + arg1, Toast.LENGTH_SHORT).show();
                break;
        }
        return false;
    }
    /*===============================================================================
     ===============================================================================
    - ON INFOR METHOD

    ===============================================================================
    ================================================================================*/
    public boolean onInfo(MediaPlayer mp, int arg0, int arg1){

        return true;
    }
    /*===============================================================================
     ===============================================================================
    - ON SEEK METHOD

    ===============================================================================
    ================================================================================*/
    public void onSeekComplete(MediaPlayer mp){

    }
    /*===============================================================================
     ===============================================================================
    - ON PREPARE LISTENER
    - this is called from the onstartcommand and is used when !mediaPlayer.isPlaying
    - it calls a method which in turn plays the podcast prepared
    ===============================================================================
    ================================================================================*/
    public void onPrepared(MediaPlayer mp){

        playPodcast();

    }


    /*===============================================================================
    ===============================================================================
    - BINDER

    ===============================================================================
    ================================================================================*/
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    /*===============================================================================
    ===============================================================================
    - PLAY PODCAST METHOD
    -called from onPrepared method

    ===============================================================================
    ================================================================================*/
    public void playPodcast(){
        //start the mediaplayer
        if(!mediaPlayer.isPlaying()){
            mediaPlayer.start();
            Globals.podcastPlaying = true;
            Globals.btnPlay.setImageResource(R.drawable.btn_pause);
        }
    }

    /*===============================================================================
    ===============================================================================
    - STOP PODCAST METHOD
    -called from oncompletion method

    ===============================================================================
    ================================================================================*/
    public void stopPodcast(){
        //stop the mediaplayer
        if(mediaPlayer.isPlaying()){
            mediaPlayer.stop();
            Globals.podcastPlaying = false;
            Globals.btnPlay.setImageResource(R.drawable.btn_play);
        }
    }

}
