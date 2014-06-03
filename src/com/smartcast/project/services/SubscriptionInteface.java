package com.smartcast.project.services;

import com.smartcast.project.frontend.Subscription;
/**
 * Created by Slavotic on 24/05/14.
 */
import java.util.List;

public interface SubscriptionInteface {
    public boolean addSubscription(Subscription toAdd);
    public void deleteAllSubscriptions();
    public boolean editSubscription(Subscription original, Subscription updated);
    public List<Subscription> getSubscriptions();
    public boolean removeSubscription(Subscription toRemove);
    public List<Subscription> resetToDemoSubscriptions();
    boolean toggleSubscription(Subscription toToggle);
}
