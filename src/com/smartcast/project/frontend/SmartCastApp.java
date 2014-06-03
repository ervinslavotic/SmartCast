package com.smartcast.project.frontend;

import android.app.Application;

/**
 * Created by Slavotic on 02/06/14.
 */


import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.os.IBinder;
import android.util.Log;

import com.smartcast.project.services.ContentService;
import com.smartcast.project.services.ContentService.LocalBinder;
import com.smartcast.project.debug.TraceUtil;


public class SmartCastApp extends Application {


    private Intent serviceIntent;
    private ContentService contentService;
    private ContentServiceListener contentServiceListener;

    @Override
    public void onCreate() {
        super.onCreate();
        serviceIntent = new Intent(this, ContentService.class);
        startService(serviceIntent);
        //WifiConnectedReceiver.registerForWifiBroadcasts(getApplicationContext());
    }

    private ServiceConnection contentServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder iservice) {
            Log.i("CarCast", "onServiceConnected; CN is " + name + "; binder is " + iservice);
            if (name.getClassName().equals(ContentService.class.getName())) {
                contentService = ((LocalBinder) iservice).getService();
                contentService.setApplicationContext(getApplicationContext());
                contentServiceListener.onContentServiceChanged(contentService);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("CarCast", "onServiceDisconnected; CN is " + name);
            if (name.getClassName().equals(ContentService.class.getName())) {
                contentService = null;
                contentServiceListener.onContentServiceChanged(contentService);
            }
        }
    };

    public void setContentServiceListener(ContentServiceListener listener) {
        this.contentServiceListener = listener;
        // make sure the service is running (may have been shut down by stopping
        // CarCast previously). Note that after the service has been stopped, we
        // need to bind to it again.
        // BIND_AUTO_CREATE forces the service to start running and continue
        // running until unbound.
        bindService(serviceIntent, contentServiceConnection, Context.BIND_AUTO_CREATE);

        // notify immediately if we have a contentService:
        listener.onContentServiceChanged(contentService);
    }


    public static String getVersionName(Context context, Class<?> cls) {
        try {
            ComponentName comp = new ComponentName(context, cls);
            PackageInfo pinfo = context.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
            return pinfo.versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static void report(Throwable e1) {
        TraceUtil.report(e1);
    }

    public static void esay(Throwable re) {
        TraceUtil.report(re);
    }

    public static String getAppTitle() {
        return "Smart Cast";
    }

    public void stopContentService() {
        Log.i("SmartCast", "requesting stop; contentService is " + contentService);
        stopService(serviceIntent);
    }


}
