package com.smartcast.project.stream;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
/**
 * Created by Slavotic on 20/05/14.
 */
public class ConfigFolder {

    Context context;

    public ConfigFolder(Context context){
        this.context = context;
        getDeviceRoot().mkdirs();
    }


    // methods
    public File getDeviceRoot(){
        return new File(getSmartCastRoot(), "podcasts");
    }

    public File getSmartCastRoot(){
        SharedPreferences sc_preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String file = sc_preferences.getString("SmartCastRoot", null);
        if(file==null){
            SharedPreferences.Editor editor = sc_preferences.edit();
            editor.putString("SmartCastRoot", new File(android.os.Environment.getExternalStorageDirectory(), "Smart_Cast").toString());
            editor.commit();
            file = sc_preferences.getString("SmartCastRoot", null);
        }
        return new File(file);
    }


    public File getPodcastRootPath(String path) {
        return new File(getPodcastsRoot(), path);
    }

    public File getSmartCastPath(String path) {
        return new File(getSmartCastRoot(), path);
    }

    public File getPodcastsRoot() {
        return new File(getSmartCastRoot(), "podcasts");
    }

}
