package com.smartcast.project;

import java.util.ArrayList;
import java.util.HashMap;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.ActivityGroup;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

import java.io.IOException;



/*
	 * for the play button, send to method to play next podcast on the play queue

	 */

public class MainActivity extends ActivityGroup {


	// Media Player
	private  MediaPlayer mp;
	// Handler to update UI timer, progress bar etc,.
	//private Handler mHandler = new Handler();
	public PodcastOrganizer podcast_organizer;
    public FeedOrganizer vendor_organizer;
	public ArrayList<HashMap<String, String>> podcasts = new ArrayList<HashMap<String, String>>();
    public ArrayList<HashMap<String, String>> vendors = new ArrayList<HashMap<String, String>>();
    public Intent serviceIntent;
    private String setPodcastLink;
    private String currentPodcast;
    //private boolean podcastPlaying = false;


    // on creation of the app
	protected void onCreate(Bundle icicle) {
		 super.onCreate(icicle);
		 
		 setContentView(R.layout.activity_main);
         //set the view of the player
         try{
            serviceIntent = new Intent(this, PlayerService.class);
            initViews();
            putListeners();
            //
        }
        catch(Exception e){
            e.printStackTrace();
        }


        Log.w("WAr", "inside");
	}



    /*===============================================================================
      ===============================================================================
     - init the views here

     ===============================================================================
     ================================================================================*/
    private void initViews(){


        //initialize play button
        Globals.btnPlay = (ImageButton) findViewById(R.id.btnPlay);

        //media player
        mp = new MediaPlayer();
        podcast_organizer = new PodcastOrganizer();
        vendor_organizer = new FeedOrganizer();

        //mp.setOnCompletionListener((OnCompletionListener) this);
        //get all podcasts in the list streamed and alos get the list of vendors
        podcasts = podcast_organizer.getPodcasts();
        vendors = vendor_organizer.getVendors();// gets the list of vendors that arte supplied in the podcats streams

        //tabs
        TabHost tabHost = (TabHost)findViewById(R.id.tabHost);

        //set up the tabs

        tabHost.setup(getLocalActivityManager());

        TabHost.TabSpec spec = tabHost.newTabSpec("tag1");

        //tab1 content here
        spec.setContent(R.id.tab1);
        spec.setIndicator("Player");
        tabHost.addTab(spec);

        //tab2 content here
        //test
        Intent stream_activity_intent = new Intent(this, StreamActivity.class);
        stream_activity_intent.putExtra("streamList", podcasts);
        spec = tabHost
                .newTabSpec("tag2")
                .setIndicator("Streams")
                .setContent(stream_activity_intent);// maybe startactivity() here?
        tabHost.addTab(spec);

        //tab 3 conten here
        Intent feed_activity_intent = new Intent(this, FeedActivity.class);
        feed_activity_intent.putExtra("feedList", vendors);
        spec = tabHost
                .newTabSpec("tag3")
                .setIndicator("Feeds")
                .setContent(feed_activity_intent);// maybe startactivity() here?
        tabHost.addTab(spec);

        //tab 4 content here
        spec=tabHost.newTabSpec("tag4");
        spec.setContent(R.id.tab4);
        spec.setIndicator("Settings");
        tabHost.addTab(spec);
        // creating tab within two tabhosts





    }


    /*===============================================================================
      ===============================================================================
     - init the listeners here

     ===============================================================================
     ================================================================================*/

	//get playlist in folder currently 
	private void putListeners() {
		//todo
        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        Globals.btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check for already playing
                buttonPlayStopClick();
            }
        });
	}

	

     /*===============================================================================
      ===============================================================================
     - play stop button

     ===============================================================================
     ================================================================================*/
    private void buttonPlayStopClick(){

        if(!Globals.podcastPlaying){
            Globals.btnPlay.setImageResource(R.drawable.btn_pause);
            playPodcast();
            Globals.podcastPlaying = true;
        }
        else{
            if(Globals.podcastPlaying){
                Globals.btnPlay.setImageResource(R.drawable.btn_play);
                stopPodcastService();
                Globals.podcastPlaying = false;
            }
            Globals.btnPlay.setImageResource(R.drawable.btn_play);
        }
    }



    /*===============================================================================
      ===============================================================================
     - play the podcast

     ===============================================================================
     ================================================================================*/
    private void playPodcast(){

        //firstly we set the link to the folder
        //then play the first song on the queue as it was streamed
        setPodcastLink  = getString(R.string.podcast_uri);
        String nextPodcastToPlay = podcasts.get(0).get("podcastName");//gets the next podcast on the queue
        //add extras to intent
        serviceIntent.putExtra("nextOnQueue", setPodcastLink + nextPodcastToPlay);

        //after that we need to start the service
        try{
            startService(serviceIntent);
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

    }


     /*===============================================================================
      ===============================================================================
     - init the listeners here

     ===============================================================================
     ================================================================================*/
    private void stopPodcastService(){

        //after that we need to start the service
        try{
            stopService(serviceIntent);
        }catch(Exception e){
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), e.getClass().getName() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
        Globals.podcastPlaying = false;

    }
}
