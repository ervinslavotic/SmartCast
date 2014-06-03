package com.smartcast.project.podcast;

/**
 * Created by Slavotic on 24/05/14.
 * Meta data from a podcast .from Rss data
 */
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;


public class MetaFile {
    public File file;
    Properties properties = new Properties();
    private static final String defaultBaseFile = "0";

    String getFileName(){
        return file.getName();
    }

    // initializer
    MetaFile(File file){
        this.file = file;

        File metaFile = getMetaPropFile();
            if(metaFile.exists()){
                try{
                    properties.load(new FileInputStream(metaFile));
                }
                catch(Exception e){
                    Log.e("Meta", "Can't load properties");

                }
            }else{
                properties.setProperty("title", file.getName());
                properties.setProperty("vendorName", "unknown feed");
                properties.setProperty("currentPos", "0");
                computeDuration();
                saveData();
            }

    }

    /*===============================================================================
    ===============================================================================
    GET THE BASE FILE NAME
    ===============================================================================
    ================================================================================*/
    public String getBaseFile(){
        String name = getFileName();
        if(name == null) return defaultBaseFile;
        Log.d("SmartCast", "getBaseFile" + name);

        //get start of base file
        int index = name.lastIndexOf('/');

        //get end of base file
        int endOfBaseFile = index + 1;
        while(endOfBaseFile < name.length() && Character.isDigit(name.charAt(endOfBaseFile)))
            endOfBaseFile += 1;
        if(name.length() <= endOfBaseFile)
            return defaultBaseFile;
        if(index + 1 == endOfBaseFile)
            return defaultBaseFile;

        return name.substring(index + 1, endOfBaseFile);

    }


    public void computeDuration(){
        MediaPlayer mediaPlayer = new MediaPlayer();
        try{
            mediaPlayer.setDataSource(file.toString());
            mediaPlayer.prepare();
            setDuration(mediaPlayer.getDuration());
        }catch (Exception e){
            setDuration(0);
        }finally {
            mediaPlayer.reset();
            mediaPlayer.release();
        }
    }

    //constructor for over loading the initializer
    public MetaFile(MetaNet metaNet, File castFile){
        file = castFile;
        properties = metaNet.properties;
        computeDuration();
    }

    public void delete(){
        file.delete();
        getMetaPropFile().delete();
    }

    public int getCurrentPosition(){
        if(properties.getProperty("currentPos") == null)
            return 0;
        return Integer.parseInt(properties.getProperty("currentPos"));

    }


    public int getDuration(){
        if(properties.getProperty("duration") == null)
            return -1;
        return Integer.parseInt(properties.getProperty("duration"));

    }

    public String getFeedName(){
        if(properties.get("feedName")==null)
            return "unknown";
        return properties.get("feedName").toString();
    }

    private File getMetaPropFile(){
        //check for the meta data here
        String name = file.getName();
        int last = name.lastIndexOf('.');
        if(last != -1){
            name = name.substring(0, last);
        }
        name += ".meta";
        return new File(file.getParent(), name);

    }

    public String getTitle(){
        if(properties.get("title")== null){
            String title = file.getName();
            int last = title.lastIndexOf('.');
            return title.substring(0, last);
        }
        return properties.get("title").toString();
    }

    public String getUrl(){
        return properties.getProperty("url");
    }

    public void saveData(){
        FileOutputStream fos = null;
        try{

        }catch(Throwable e){
            Log.e("MetaFile", "saving meta data", e);
        }finally {
            if(fos != null){
                try{
                    fos.close();
                }catch(IOException e){
                    Log.e("MetaFile", "io exception", e);
                }
            }
        }
    }


    public void setCurrentPosition(int i){
        properties.setProperty("currentPos", Integer.toString(i));
        if(getDuration() == -1){
            return;
        }
        if(i > getDuration()*.9){
            setListenedTo();
        }
    }

    public void setDuration(int duration) {
        properties.setProperty("duration", Integer.toString(duration));
    }

    public void setListenedTo() {
        properties.setProperty("listenedTo", "true");
    }

    public boolean isListenedTo() {
        return properties.getProperty("listenedTo" ) != null;
    }

    public String getDescription(){
        return properties.getProperty("description");
    }
}
