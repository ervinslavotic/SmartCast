package com.smartcast.project;

import android.media.MediaPlayer;
import android.widget.ImageButton;
import android.widget.SeekBar;
/**
 * Created by Slavotic on 14/05/14.
 * Allowing access to global members to each class
 * used by calling --> Globals.variable = someValue
 * This is usefull in terms of changing the state of play and pause and keeping track of which podcast is playing
 */
public class Globals {

    public static boolean podcastPlaying = false;
    public static ImageButton btnPlay;
    public static SeekBar seekBarProgress;
    public static MediaPlayer mediaPlayer = new MediaPlayer();
}
