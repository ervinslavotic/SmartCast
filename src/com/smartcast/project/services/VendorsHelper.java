package com.smartcast.project.services;

import java.util.List;

import com.smartcast.project.frontend.Subscription;
/**
 * Created by Slavotic on 24/05/14.
 */
public interface VendorsHelper {
    public boolean addSubscription(Subscription toAdd);
    public void deleteAllSubscriptions();
    public boolean editSubscription(Subscription original, Subscription updated);
    public List<Subscription> getSubscriptions();
    public boolean removeSubscription(Subscription toRemove);
    public List<Subscription> resetToDemoSubscriptions();
    boolean toggleSubscription(Subscription toToggle);
}
