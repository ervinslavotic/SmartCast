package com.smartcast.project.stream;

/**
 * Created by Slavotic on 24/05/14.
 * this class is intented for the download of podcasts and is the main program for that
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.SAXParserFactory;

import android.net.Uri;
import android.test.suitebuilder.TestSuiteBuilder;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;


import com.smartcast.project.frontend.OrderingPreference;
import com.smartcast.project.frontend.Subscription;
import com.smartcast.project.podcast.MetaNet;
import com.smartcast.project.podcast.PodcastHandler;
import com.smartcast.project.podcast.Util;
import com.smartcast.project.services.ContentService;
import com.smartcast.project.services.DownloadHistory;
import com.smartcast.project.services.VendorsHelper;
import com.smartcast.project.podcast.MetaFile;


public class PodcastDownloader {

    public String currentSubscription = "";
    public String currentTitle = "";
    int globalMax;
    StringBuilder newtext = new StringBuilder();
    int podcastBytesCurrent;
    int scannedSites;
    int totalPodcasts;
    int totalVendors;
    TextView tv;
    boolean isIdle;
    StringBuilder sb = new StringBuilder();

    //constructor
    public PodcastDownloader(int globalMax){
        this.globalMax = globalMax;
    }

    SimpleDateFormat sdf = new SimpleDateFormat("MMM-dd hh:mma");

    private String  getLocalFileFrmMimetype(String mimeType){
        if("audio/mp3".equals(mimeType)){
            return ".mp3";
        }
        if("audio/mp3".equals(mimeType)){
            return ".ogg";
        }
        return ".bin";
    }

    //download new podcats from vendors
    protected void downloadPodcasts(ContentService serviceContent, String accounts, boolean collectData){

        try{
            DownloadHistory history = new DownloadHistory(serviceContent);
            List<Subscription>sites = serviceContent.getSubscriptions();
            totalVendors = sites.size();

            List<MetaNet> enclosures = new ArrayList<MetaNet>();
            SAXParserFactory spf = SAXParserFactory.newInstance();

            for(Subscription sub: sites){
                PodcastHandler handler = new PodcastHandler(history, sub.priority);
                if(sub.enabled){
                    try{
                        URL url = new URL(sub.url);

                        String name = sub.name;
                        handler.setFeedName(name);
                        Util.findAvailablePodcasts(sub.url, handler);
                    }catch(Throwable e){
                        Log.e("BAH", "bad", e);
                    }
                }
                scannedSites++;
                if (sub.orderingPreference == OrderingPreference.LIFO)
                    Collections.reverse(handler.metaNets);

                enclosures.addAll(handler.metaNets);
            }//end foreach

            List<MetaNet> newPodcasts = new ArrayList<MetaNet>();
            for(MetaNet metaNet: enclosures){
                if(history.contains(metaNet))
                    continue;
                newPodcasts.add(metaNet);
            }

            totalPodcasts = newPodcasts.size();
            for(MetaNet metaNet: newPodcasts)
                totalPodcasts += metaNet.getSize();

            System.setProperty("http.maxRedirects", "50");


            byte [] buf = new byte[16383];

            int soFar = 0;
            for(int i = 0; i < newPodcasts.size();i++){
                String shortName = newPodcasts.get(i).getTitle();
                String localFile = getLocalFileFrmMimetype(newPodcasts.get(i).getMimetype());
                totalPodcasts = i+1;

                try{
                    ConfigFolder config = new ConfigFolder(serviceContent);
                    String prefix = "";

                    // IMPORTANT:
                    //*    The naming scheme used here *must* match MetaHolder.isPriority().

                    if(newPodcasts.get(i).getPriority())
                        if(serviceContent.currentMeta() != null)
                            prefix = serviceContent.currentMeta().getBaseFile() + ":00:";

                    String castFileName = prefix + System.currentTimeMillis() + localFile;
                    File castFile = config.getPodcastRootPath(castFileName);

                    Log.d("smartcast ", "New podcast file : " + castFileName);


                    currentSubscription = newPodcasts.get(i).getSubscription();
                    currentTitle = newPodcasts.get(i).getTitle();
                    File tempFile = config.getPodcastRootPath("tempFile");
                    InputStream is = getInputStream(new URL(newPodcasts.get(i).getUrl()));
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    int amt = 0;
                    int expectedSizeKilo = newPodcasts.get(i).getSize()/1024;
                    String preDownload = sb.toString();
                    int totalForCurrentPodcast = 0;


                    while((amt = is.read(buf)) >= 0){
                        fos.write(buf,0,amt);
                        podcastBytesCurrent += amt;
                        totalForCurrentPodcast += amt;
                    sb = new StringBuilder(preDownload+ String.format("%dk/%dk  %d", totalForCurrentPodcast / 1024, expectedSizeKilo,(int) ((totalForCurrentPodcast / 10.24) / expectedSizeKilo)) + "%\n");

                    }

                    fos.close();
                    is.close();


                    history.add(newPodcasts.get(i));


                    tempFile.renameTo(castFile);
                    new MetaFile(newPodcasts.get(i), castFile).saveData();

                    soFar++;
                }
                catch(Throwable e){
                    Log.i("in podcast downloader", e.getMessage());
                }
            }
        }
        catch(Exception e){

        }
    }



    // get the steam
    private InputStream getInputStream(URL url) throws IOException {
        int redirectLimit = 15;
        while (redirectLimit-- > 0) {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(false);
            con.setConnectTimeout(20 * 1000);
            con.setReadTimeout(30 * 1000);
            con.connect();
            if (con.getResponseCode() == 200) {
                return con.getInputStream();
            }
            if (con.getResponseCode() == 404 && url.getPath().contains(" ")){
                String newURL = url.getProtocol()+"://"+url.getHost()+(url.getPort()==-1?"":(":"+url.getPort()))+
                        url.getPath().replaceAll(" ", "%20");
                return getInputStream(new URL(newURL));
            }
            if (con.getResponseCode() > 300 && con.getResponseCode() > 399) {
                Log.d("podcastdownloader ",url + " gave resposneCode " + con.getResponseCode());
                throw new IOException();
            }
            url = null;
            for (int i = 0; i < 50; i++) {
                if (con.getHeaderFieldKey(i) == null)
                    continue;
                if (con.getHeaderFieldKey(i).toLowerCase().equals("location")) {
                    url = new URL(con.getHeaderField(i));
                }
            }
            if (url == null) {
                Log.d("Donaloerd", "Got 302 without Location");
            }
        }
        throw new IOException(" redirect limit reached");
    }


}
