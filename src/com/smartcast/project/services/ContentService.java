package com.smartcast.project.services;

/**
 * Created by Slavotic on 24/05/14.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.SortedSet;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;


import com.smartcast.project.debug.TraceUtil;
import com.smartcast.project.frontend.PodcastMode;
import com.smartcast.project.frontend.SmartCastApp;
import com.smartcast.project.frontend.Subscription;
import com.smartcast.project.podcast.MetaInfo;
import com.smartcast.project.stream.ConfigFolder;
import com.smartcast.project.stream.PodcastDownloader;
import com.smartcast.project.frontend.Location;
import com.smartcast.project.podcast.MetaFile;


public class ContentService extends Service implements MediaPlayer.OnCompletionListener{

    private final IBinder binder  = new LocalBinder();
    int currentPodcastPlaying = -1;
    PodcastDownloader podcastDownloader;
    Location location;
    PodcastMode podcastMode = PodcastMode.UnInitialized;
    MediaPlayer mediaPlayer = null;
    MetaInfo metaInfo;
    boolean pausedByPhone;
    private PlayStatusListener playStatusListener;
    private Context context;
    private ConfigFolder configFolder;
    VendorSubscription vendorSubscription;
    //set the context
    public void setApplicationContext(Context context){
        this.context = context;
        try {
            if(mediaPlayer == null)
            {
                mediaPlayer = new MediaPlayer();
                fullReset();
            }
        } catch (Exception e) {
            Log.d("CarCast", "Error doing reset", e);
        }
    }
    /**
     * We need only a local binder and dont need and IPC
     */
    public class LocalBinder extends Binder {
        public ContentService getService() {
            return ContentService.this;
        }
    }

    public static String getTimeString(int time){
        StringBuilder sb = new StringBuilder();
        int min  = time /(1000*6);
        if(min < 10)
            sb.append('0');
        sb.append(min);
        sb.append(':');
        int sec = (time - min*60*1000)/1000;
        if(sec < 10)
            sb.append('0');
        sb.append(sec);
        return sb.toString();
    }

    public boolean addSubscription(Subscription toAdd){
        return vendorSubscription.addSubscription(toAdd);
    }
    


    // perhps need to insert some media player properties here


    // get meta file data from the particular podcast
    public MetaFile currentMeta(){
        if(metaInfo.getSize() == 0) return null;
        if(currentPodcastPlaying == -1)return null;// if podcast is not there
        if(metaInfo.getSize() < currentPodcastPlaying)return null;
        return metaInfo.get(currentPodcastPlaying);
    }


    // perhaps need to have a referent for te current file playing DURATION here
    public File currentFile(){
        MetaFile meta = currentMeta();
        return meta == null ? null : meta.file;
    }



    // full reset of the podcast player
    private boolean fullReset()throws Exception{
        if(mediaPlayer != null){
            mediaPlayer.reset();
        }

        if(currentPodcastPlaying > metaInfo.getSize())
            return false;

        mediaPlayer.setDataSource(currentFile().toString());
        mediaPlayer.prepare();
        mediaPlayer.setOnCompletionListener(this);

        mediaPlayer.seekTo(metaInfo.get(currentPodcastPlaying).getCurrentPosition());
        return true;
    }


    public List<Subscription> getSubscriptions() {
        List<Subscription> subscriptions = vendorSubscription.getSubscriptions();
        return subscriptions;
    }

    // called when user hits button (might be playing or not playing) and called
    // when
    // the playback engine his the "onCompletion" event (ie. a podcast has
    // finished, in which case
    // we are actually no longer playing but we were just were a millisecond or
    // so ago.)
    void next(boolean inTheActOfPlaying) {
        podcastMode = PodcastMode.UnInitialized;

        // if we are at end.
        if (currentPodcastPlaying + 1 >= metaInfo.getSize()) {
            saveState();
            // activity.disableJumpButtons();
            mediaPlayer.reset();
            // say(activity, "That's all folks");

            return;
        }

        currentPodcastPlaying++;
        if (inTheActOfPlaying)
            play();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (currentMeta() == null)
            return;
        currentMeta().setCurrentPosition(0);
        currentMeta().setListenedTo();
        currentMeta().saveData();
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("autoPlayNext", true)) {
            next(true);
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("SmartCast", "ContentService unbinding " + intent);
        return super.onUnbind(intent);
    }



    public void saveState() {
        try {
            final File stateFile = configFolder.getPodcastRootPath("state.dat");
            location = Location.save(stateFile, currentTitle());
        } catch (Throwable e) {
            // bummer.
        }
    }


    public String currentTitle() {

        return currentMeta().getTitle();
    }


    /// media player stuff

    private void play() {
        try {
            if (!fullReset())
                return;

            // say(activity, "started " + currentTitle());
            mediaPlayer.start();
            podcastMode =PodcastMode.Playing;
            saveState();
        } catch (Exception e) {
            TraceUtil.report(e);
        }
    }


    private void initDirs() {
        File legacyFile = configFolder.getSmartCastPath("podcasts.txt");
        File siteListFile = configFolder.getSmartCastPath("podcasts.properties");
        vendorSubscription = new VendorSubscription(siteListFile, legacyFile);
    }


    private WakeLock partialWakeLock;

    private static final Class<?>[] mSetForegroundSignature = new Class[]{boolean.class};
    private static final Class<?>[] mStartForegroundSignature = new Class[]{int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[]{boolean.class};

    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    /// ONCREATE METHOD
    @Override
    public void onCreate() {
        super.onCreate();
        WifiConnectedReceiver.registerForWifiBroadcasts(getApplicationContext());

        configFolder = new ConfigFolder(getApplicationContext());

        initDirs();

        // Google handles surprise exceptions now, so we dont have to.
        //ExceptionHandler.register(this);

        partialWakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                SmartCastApp.getAppTitle());
        partialWakeLock.setReferenceCounted(false);

        metaInfo = new MetaInfo(getApplicationContext());
//		mediaPlayer.setOnCompletionListener(this);

        // restore state;
        currentPodcastPlaying = 0;

        try {
            mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
            try {
                mSetForeground = getClass().getMethod("setForeground", mSetForegroundSignature);
            } catch (NoSuchMethodException e1) {
                throw new IllegalStateException("OS doesn't have Service.startForeground OR Service.setForeground!");
            }
        }


    }
}