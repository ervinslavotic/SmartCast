/*

*** THIS CLASS IS INTENDED TO RETIREVE FEED DATA FROM VENDORS IN ORDER TO DISPLAY THEM ACCORDINGLY IN THE LISTVIEW
*** IT DISPLAYS THE VENDORS AS TEXT IN THE LISTVIEW
*
* EXAMPLE USE CASE;
*
* FROM MainActivity :  CALL TO A FEEDORGANIZER OBJECT WILL RETURN A LIST OF VENDORS ORGANIZED AS TEXT



 */



package com.smartcast.project;

import android.util.Log;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;

public class FeedOrganizer {

    //SD card path
    String VENDOR_PATH = new String("/storage/sdcard0/smartcast/Vendors/");
    // create arraylist for podcasts
    ArrayList<HashMap<String, String>> vendors = new ArrayList<HashMap<String, String>>();



    //contructor
    FeedOrganizer(){

    }

    /*
     * Read all podcasts inside Smartcast Folder
     * - Store them into array list
     * - function for array list
     */
    public ArrayList<HashMap<String,String>> getVendors(){
        //create folder here for base
        File home = new File(VENDOR_PATH);
        //add all podcasts to playlist as according to user preference
        for(File file: home.listFiles()){
            HashMap<String, String> vendor = new HashMap<String, String>();
            vendor.put("VendorName", file.getName());
            vendors.add(vendor);
        }
        return vendors;

        //return all podcasts in array
    }

    // filter all mp3
    //do sumthing with meta data here

}
