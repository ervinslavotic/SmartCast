package com.smartcast.project.services;

/**
 * Created by Slavotic on 02/06/14.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.smartcast.project.frontend.Sayer;
import com.smartcast.project.podcast.MetaNet;
import com.smartcast.project.stream.ConfigFolder;
/**
 * The history of all downloaded episodes the data is backed into a file on the SD-card
 */
public class DownloadHistory implements Sayer {


    private static final String UNKNOWN_SUBSCRIPTION = "unknown";
    private final static String HISTORY_TWO_HEADER = "history version 2";
    private List<Entry> historyEntries = new ArrayList<Entry>();
    StringBuilder sb = new StringBuilder();

    Context context;
//    private static File historyFile = new File(Config.PodcastsRoot, "history.prop");


    /**
     * Create a object that represents the download history. It is backed to a file.
     */
    @SuppressWarnings("unchecked")
    public DownloadHistory(Context context) {
        this.context = context;
        ConfigFolder config = new ConfigFolder(context);
        File historyFile = config.getPodcastRootPath("history.prop");
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream(historyFile));
            String line = dis.readLine();
            if (!line.startsWith(HISTORY_TWO_HEADER)) {
                // load old format.
                historyEntries.add(new Entry(UNKNOWN_SUBSCRIPTION, line));
                while ((line = dis.readLine()) != null) {
                    historyEntries.add(new Entry(UNKNOWN_SUBSCRIPTION, line));
                }
            } else {
                ObjectInputStream ois = new ObjectInputStream(dis);
                historyEntries = (List<Entry>) ois.readObject();
                ois.close();
            }
        } catch (Throwable e) {
            // would be nice to ask the user if we can submit his history file
            // to the devs for review
            Log.e(VendorsHelper.class.getName(), "error reading history file " + historyFile.toString(), e);
        }
    }

    /**
     * Add a item to the history
     *
     * @param metaNet podcast metadata
     */
    public void add(MetaNet metaNet) {
        historyEntries.add(new Entry(metaNet.getSubscription(), metaNet.getUrl()));
        save();
    }

    /**
     * Check if a item is in the history
     *
     * @param metaNet the item to check for
     * @return true it the item is in the history
     */
    public boolean contains(MetaNet metaNet) {
        for (Entry historyEntry : historyEntries) {
            if (!historyEntry.subscription.equals(UNKNOWN_SUBSCRIPTION) &&
                    !historyEntry.subscription.equals(metaNet.getSubscription())) {
                continue;
            }
            if (historyEntry.podcastURL.equals(metaNet.getUrl())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Remove history of all downloaded podcasts
     *
     * @return number of history items deleted
     */
    public int eraseHistory() {
        int size = historyEntries.size();
        historyEntries = new ArrayList<Entry>();
        save();
        return size;
    }

    /**
     * Remove history of all downloaded podcasts for the specified subscription
     *
     * @return number of history items deleted
     */
    public int eraseHistory(String subscription) {
        int size = historyEntries.size();
        List<Entry> nh = new ArrayList<Entry>();
        for (Entry he : historyEntries) {
            if (!he.subscription.equals(subscription))
                nh.add(he);
        }
        historyEntries = nh;
        save();
        return size - nh.size();
    }


    private void save() {
        ConfigFolder config = new ConfigFolder(context);
        File historyFile = config.getPodcastRootPath("history.prop");
        try {
            DataOutputStream dosDataOutputStream = new DataOutputStream(new FileOutputStream(historyFile));
            dosDataOutputStream.write(HISTORY_TWO_HEADER.getBytes());
            dosDataOutputStream.write('\n');
            ObjectOutputStream oos = new ObjectOutputStream(dosDataOutputStream);
            oos.writeObject(historyEntries);
            oos.close();
        } catch (IOException e) {
            say("problem writing history file: " + historyFile + " ex:" + e);
        }
    }


    public void say(String text) {
        sb.append(text);
        sb.append('\n');
    }

    /**
     * Get the current size of the download history
     *
     * @return the size
     */
    public int size() {
        return historyEntries.size();
    }

    @Override
    public void Sayer(String text) {

    }
}