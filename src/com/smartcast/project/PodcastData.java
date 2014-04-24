package com.smartcast.project;


import android.util.Log;

//type for custom adaptor
public class PodcastData {
	protected String mItem;
	protected String mDescription;

	PodcastData(String item, String description){
    	mItem = item;
    	mDescription = description;    		
    }
	@Override
	public String toString() {
		return mItem + " " +  mDescription;
	}
}
