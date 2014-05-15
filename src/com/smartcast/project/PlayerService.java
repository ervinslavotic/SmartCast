package com.smartcast.project;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.media.MediaPlayer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
// this is the various listeners needed here
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Handler;
import java.io.IOException;

//player needs to extend all of thes listeners
public class PlayerService extends Service implements OnCompletionListener, OnBufferingUpdateListener, OnErrorListener,
        OnInfoListener, OnSeekCompleteListener, OnPreparedListener{
    // backround service of the player
    // the media player
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private String setPodcastLink;
    private String currentPodcast;// current podcast playing
    //notification ID
    private static final int NOTIFICATION_ID = 1;
    //for incoming call vars
    private boolean isPausedInCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    // seek bar postion vars
    int podcastFileLengthInMilliSeconds;
    String sntSeekPos; // seek position
    int seekPosition;
    int podcastPosition;
    int podcastMax;
    private final Handler handler = new Handler();// used for multithreadedness
    private static int songEnded;
    public static final String BROADCAST_ACTION ="com.smartcast.project.seekprogress";

    //setup broadcast identifier
    public static final String BROADCAST_BUFFER = "com.smartcast.project.broadcastbuffer";
    Intent bufferIntent;
    Intent seekIntent;

    /*===============================================================================
     ===============================================================================
    - ONCREATE
    - for seekbar we need an intent
    ===============================================================================
    ================================================================================*/
    public void onCreate(){

        bufferIntent = new Intent(BROADCAST_BUFFER);
        //setup progress bar
        seekIntent = new Intent(BROADCAST_ACTION);

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


        //manage incoming phonecalls, resume on hangup
        Log.v("Phone", "Starting telephony");
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        Log.v("Phone", "Starting listener");
        phoneStateListener = new PhoneStateListener(){
        // method for call states
            public void onCallStateChanged(int state, String incomingNumber){
                   switch (state){
                       case TelephonyManager.CALL_STATE_OFFHOOK:
                       case TelephonyManager.CALL_STATE_RINGING:
                           if(mediaPlayer != null){
                               pausePodcast();
                               isPausedInCall = true;
                           }
                           break;
                       case TelephonyManager.CALL_STATE_IDLE:
                           //start playing its idle
                           if(mediaPlayer != null){
                               if(isPausedInCall){
                                   isPausedInCall = false;
                                   playPodcast();
                               }
                           }
                           break;
                   }
            }
        };

        //register listener with telephony manager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        // show notification
        initNotification();

        // init the progress bar
        initProgressbar();
        // looks in the folder the rder it came i to start playing
        currentPodcast = intent.getExtras().getString("nextOnQueue");
        mediaPlayer.reset();// reset the player
        Toast.makeText(this, "Player has started", Toast.LENGTH_LONG).show();


        // set up the progressbar


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

        //turn phone listener call off
        if(phoneStateListener != null){
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        Toast.makeText(this, "Player has stopped", Toast.LENGTH_LONG).show();
        //cancel the notification
        cancelNotification();
    }


    /*===============================================================================
   ===============================================================================
  - ON BUFFERING METHOD

  ===============================================================================
  ================================================================================*/
    public void onBufferingUpdate(MediaPlayer mp, int percent){
        //set progress
        Globals.seekBarProgress.setSecondaryProgress(percent);

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
    - PAUSE PODCAST METHOD
    -called from phoneListener

    ===============================================================================
    ================================================================================*/
    public void pausePodcast(){
        //start the mediaplayer
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            Globals.btnPlay.setImageResource(R.drawable.btn_play);
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


    /*===============================================================================
    ===============================================================================
    - START NOTIFICATION
    - called form on start command
    - initializes the notification panel
    ===============================================================================
    ================================================================================*/
    private void initNotification(){

        String ns = Context.NOTIFICATION_SERVICE;//get the service going
        NotificationManager notificationManager =(NotificationManager) getSystemService(ns);
        int icon = R.drawable.icon;//
        CharSequence tickerText = "SmartCast";
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, tickerText, when);
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        Context context = getApplicationContext();
        CharSequence contentTitle = "SmartCast";
        CharSequence contentText = " Listen to podcast on the go ";
        Intent notificationIntent= new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
        notificationManager.notify(NOTIFICATION_ID, notification);

    }
    /*===============================================================================
    ===============================================================================
    - STOP NOTIFICATION
    - called form on DESTROY command
    - stops the notification panel
    ===============================================================================
    ================================================================================*/
    private void cancelNotification(){
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager notificationManager = (NotificationManager) getSystemService(ns);
        notificationManager.cancel(NOTIFICATION_ID);
    }

    /*===============================================================================
    ===============================================================================
    - START PROGRESS BAR


    ===============================================================================
    ================================================================================*/
    private void initProgressbar(){

        podcastFileLengthInMilliSeconds = mediaPlayer.getDuration();
        primarySeekBarUpdater();
    }

    /*===============================================================================
    ===============================================================================
    - PRIMARY PROGRESS BAR


    ===============================================================================
    ================================================================================*/
    private void primarySeekBarUpdater(){


    }
}
