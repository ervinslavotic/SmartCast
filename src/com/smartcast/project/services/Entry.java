package com.smartcast.project.services;


import java.io.Serializable;
/**
 * Created by Slavotic on 02/06/14.
 */
public class Entry {

    String subscription;
    String podcastURL;

    public Entry(String subscription, String podcastURL) {
        super();
        this.subscription = subscription;
        this.podcastURL = podcastURL;
    }
}
