package com.smartcast.project.stream;

import android.os.AsyncTask;
import android.content.Context;
import android.os.PowerManager;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Slavotic on 01/06/14.
 */
public class StreamPodcasts extends AsyncTask<String, Integer, String> {

    private Context context;
    private PowerManager.WakeLock wakeLock;

    StreamPodcasts(Context context){
        this.context = context;
    }


    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;

        try{
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            //expect 200 to be ok
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                return "Server returned Http " + connection.getResponseCode() + " " + connection.getResponseCode();
            }

            /// useful for download percentage
            int fileLength = connection.getContentLength();


            // download trhe file
            input = connection.getInputStream();
            output = new FileOutputStream("/storage/sdcard0/smartcast/file");

            byte data [] = new byte[4096];
            long total = 0;
            int count;
            while((count = input.read(data)) != -1){
                // allow cancelling
                if(isCancelled()){
                    input.close();
                    return null;
                }
                total += count;
                // publish progress
                if(fileLength > 0)
                    publishProgress((int)(total *100/fileLength));
                output.write(data, 0, count);
            }
        }
        catch (Exception e){
            return e.toString();
        }finally {
            try{
                if (output!=null)
                    output.close();
                if(input != null)
                    input.close();
            }
            catch (IOException ignore){

            }
            if(connection!=null)
                connection.disconnect();
        }
        return null;
    }
}
