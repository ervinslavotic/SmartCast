package com.smartcast.project;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

public class PodcastOrganizer {
	
	//SD card path 
	String PODCAST_PATH = new String("/storage/sdcard0/smartcast/");
	// create arraylist for podcasts
	ArrayList<HashMap<String, String>> podcasts = new ArrayList<HashMap<String, String>>();
	
	
	
	//contructor 
	PodcastOrganizer(){
		
	}
	
	/*
	 * Read all podcasts inside Smartcast Folder 
	 * - Store them into array list 
	 * - function for array list 
	 */
	public ArrayList<HashMap<String,String>> getPodcasts(){
		//create folder here for base 
		File home = new File(PODCAST_PATH);
		//add all podcasts to playlist as according to user preference 
		for(File file: home.listFiles()){
			HashMap<String, String> podcast = new HashMap<String, String>();
            if(file.isDirectory()==false)
            {
                podcast.put("podcastName", file.getName());
                podcasts.add(podcast);
            }
		}
		return podcasts;
		
		//return all podcasts in array
	}
	
	// filter all mp3
	//do sumthing with meta data here 

}
