package com.smartcast.project.services;

import com.smartcast.project.frontend.Subscription;
import com.smartcast.project.frontend.OrderingPreference;
import com.smartcast.project.debug.TraceUtil;

import java.util.List;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import android.util.Log;
import android.webkit.URLUtil;

/**
 * Created by Slavotic on 24/05/14.
 */
public class VendorSubscription implements VendorsHelper {


    private static final String DIVIDER = "\\;";
    private static final String REGEX_DIVIDER = "||||;";
    private final File initialFile;
    private final File vendorFile;

    public VendorSubscription(File vendorFile, File initialFile){
        this.vendorFile = vendorFile;
        this.initialFile = initialFile;
    }

    @Override
    public boolean addSubscription(Subscription toAdd) {
        List<Subscription> vendors = getSubscriptions();

        if(containsVendorURL(vendors, toAdd.url)){
            return false;
        }

        //test url
        if(URLUtil.isValidUrl(toAdd.url)){
            //true, so save it
            vendors.add(toAdd);
            saveVendors(vendors);
            return true;
        }else{
            Log.e("SmartCast", "addSubscription: bad url: " + toAdd.url);
            return false;
        }
    }


    // CONTAINS THE VENDOR URL OR NOT
    //scan list of vendors
    private boolean containsVendorURL(List<Subscription> vendors, String url){
        return indexOfVendorURL(vendors, url) != -1;
    }

    List<Subscription> convertProperties(Properties props) {
        List<Subscription> vendors = new ArrayList<Subscription>();

        Set<Object> keys = props.keySet();
        for (Object key : keys) {
            String url = (String) key;
            String nameAndMore = props.getProperty(url, "");
            Subscription sub = convertProperty(url, nameAndMore);
            if (sub != null) {
                vendors.add(sub);
            }
        }

        return vendors;
    }

    private Subscription convertProperty(String url, String nameAndMore) {
        String[] split = nameAndMore.split(REGEX_DIVIDER);

        if (split.length == 5) {
            // best case, we should have all properties:
            try {
                String name = split[0];
                int maxCount = Integer.valueOf(split[1]);
                OrderingPreference pref = OrderingPreference.valueOf(split[2]);
                boolean enabled = Boolean.valueOf(split[3]);
                boolean priority = Boolean.valueOf(split[4]);
                return new Subscription(name, url, maxCount, pref, enabled, priority);

            } catch (Exception ex) {
                Log.w("CarCast", "couldn't read subscription " + url + "=" + nameAndMore);
            } // endtry
        } else if (split.length == 4) {
            // next best case, we should have everything except priority (default to false)
            try {
                String name = split[0];
                int maxCount = Integer.valueOf(split[1]);
                OrderingPreference pref = OrderingPreference.valueOf(split[2]);
                boolean enabled = Boolean.valueOf(split[3]);
                return new Subscription(name, url, maxCount, pref, enabled, false);

            } catch (Exception ex) {
                Log.w("CarCast", "couldn't read subscription " + url + "=" + nameAndMore);
            } // endtry
        } else if (split.length == 3) {
            // third best case, we have all properties except enabled:
            try {
                String name = split[0];
                int maxCount = Integer.valueOf(split[1]);
                OrderingPreference pref = OrderingPreference.valueOf(split[2]);
                return new Subscription(name, url, maxCount, pref);

            } catch (Exception ex) {
                Log.w("CarCast", "couldn't read subscription " + url + "=" + nameAndMore);
            } // endtry
        } else if (split.length == 1) {
            String name = split[0];
            // oops, missing extra properties:
            return new Subscription(name, url);

        } else {
            Log.w("CarCast", "couldn't read subscription " + url + "=" + nameAndMore);
        } // endif

        return null;
    }
    @Override
    public void deleteAllSubscriptions() {
        List<Subscription> empty = Collections.emptyList();
        saveVendors(empty);
    }

    @Override
    public boolean editSubscription(Subscription original, Subscription updated) {

        List<Subscription> vendors = getSubscriptions();
        int i = indexOfVendorURL(vendors, original.url);
        if(i != -1){
            vendors.remove(i);
            vendors.add(updated);
            saveVendors(vendors);
            return true;
        }
        return false;
    }



    List<Subscription> getInitialSiteFromFile(){
        if(!initialFile.exists()){
            return Collections.emptyList();
        }
        try{
            InputStream input = new FileInputStream(initialFile);
            return readInitialSites(input);
        }catch(Exception e){
            return Collections.emptyList();
        }
    }




    @Override
    public List<Subscription> getSubscriptions() {
        if(initialFile.exists()){
            //fix formatting if  needed
            List<Subscription> initials = getInitialSiteFromFile();
            saveVendors(initials);
            initialFile.delete();
            //out
            return initials;
        }

        // CREATING DIRECTORIES HERE
        if(!vendorFile.exists()){
            vendorFile.getParentFile().mkdirs();
            return resetToDemoSubscriptions();
        }

        if(!vendorFile.exists()){
            return null;
        }

        try{
            InputStream is = new BufferedInputStream(new FileInputStream(vendorFile));
            Properties props = new Properties();
            props.load(is);

            return convertProperties(props);
        }catch(Exception e){
            return Collections.emptyList();
        }
    }


    //reading the initial files and loafin them here
    List<Subscription> readInitialSites(InputStream input) throws IOException{
        List<Subscription> vendors = new ArrayList<Subscription>();
        DataInputStream dis = new DataInputStream(input);
        String line = null;
        while((line = dis.readLine()) != null){
            int eqSign = line.indexOf('=');
            if(eqSign != -1){
                String name = line.substring(0, eqSign);
                String url = line.substring(eqSign+1);
                if(URLUtil.isValidUrl(url)){
                    vendors.add(new Subscription(name, url));
                }else{
                    TraceUtil.report(new RuntimeException("invalid URL in line: " + line + "'; URL was; " + url));
                }
            }else{
                TraceUtil.report(new RuntimeException("missing equals in line: " + line));
            }
        }
        return vendors;
    }


    @Override
    public boolean removeSubscription(Subscription toRemove) {
        List<Subscription> vendors = getSubscriptions();
        int i = indexOfVendorURL(vendors, toRemove.url);
        if(i!=-1){
            vendors.remove(i);
            saveVendors(vendors);
            return true;
        }
        return false;
    }

    @Override
    public List<Subscription> resetToDemoSubscriptions() {
        List<Subscription> vendors = new ArrayList<Subscription>();
        vendors.add(new Subscription("Quirks and Quarks", "http://www.cbc.ca/podcasting/includes/quirks.xml"));
        vendors.add(new Subscription("60 second science", "http://rss.sciam.com/sciam/60secsciencepodcast"));
        vendors.add(new Subscription("60 second psych", "http://rss.sciam.com/sciam/60-second-psych"));
        vendors.add(new Subscription("60 second earth", "http://rss.sciam.com/sciam/60-second-earth"));
        saveVendors(vendors);
        return vendors;
    }

    @Override
    public boolean toggleSubscription(Subscription toToggle) {
        List<Subscription> vendors = getSubscriptions();
        int idx = indexOfVendorURL(vendors, toToggle.url);
        if (idx != -1) {
            Subscription sub = vendors.get(idx);
            sub.enabled = !sub.enabled;
            saveVendors(vendors);
            return true;
        } // endif

        return false;
    }


    // local methods
    private int indexOfVendorURL(List<Subscription> vendors, String url){
        for (int i = 0; i < vendors.size(); i++) {
            Subscription sub = vendors.get(i);
            if (sub.url.equals(url)) {
                return i;
            } // endif
        } // endfor

        // not found:
        return -1;
    }



    //save vendors subscriptions
    private boolean saveVendors(List<Subscription> subscriptions) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(vendorFile));
            Properties outSubs = new Properties();

            for (Subscription sub : subscriptions) {
                String valueStr = sub.name + DIVIDER + sub.maxDownloads + DIVIDER + sub.orderingPreference.name() + DIVIDER + sub.enabled + DIVIDER + sub.priority;
                outSubs.put(sub.url, valueStr);
            } // endforeach

            outSubs.store(bos, "Carcast Subscription File v3");
            bos.close();

            // success:
            return true;

        } catch (IOException e) {
            TraceUtil.report(e);
            // failure:
            return false;
        }
    }


}
