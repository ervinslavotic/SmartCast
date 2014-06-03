package com.smartcast.project.podcast;

/**
 * Created by Slavotic on 24/05/14.
 * Meta data about podcasts
 */
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Arrays;

import java.util.List;
import java.util.SortedSet;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.smartcast.project.stream.ConfigFolder;

public class MetaInfo {
    private Context context;
    private ConfigFolder configFolder;

    private List<MetaFile> metaFiles = new ArrayList<MetaFile>();

    public MetaInfo(Context context){
        this.context = null;
    }

    //over load constructor
    public MetaInfo(Context context, File current){
        this.context = context;
        this.configFolder = new ConfigFolder(context);
        loadMetaData(current);
    }


    public void delete(int i ){
        metaFiles.get(i).delete();
        metaFiles.remove(i);
    }

    public MetaFile extractData(int i){
        return metaFiles.remove(i);
    }

    public MetaFile get(int current){
        return metaFiles.get(current);
    }

    public int getSize(){
        return metaFiles.size();
    }


    //assuming metafiles is empty
    private void loadMetaData(File current){
        String currentName =  current == null ? null : current.getName();
        int index = -1;
        boolean fileAddedtoOrder = false;
        File [] files = configFolder.getPodcastsRoot().listFiles();
        File order = configFolder.getPodcastRootPath("podcast-order.txt");

        if(files == null){
            return;
        }
        //load the files i order of simpe data strcuture
        if(order.exists()){
            try{
                DataInputStream dis = new DataInputStream(new FileInputStream(order));
                String line = null;
                while((line = dis.readLine()) != null){
                    File file = configFolder.getPodcastRootPath(line);
                    if(file.exists()){
                        metaFiles.add(new MetaFile(file));
                        if(currentName != null && currentName.equals(file.getName())){
                            index = metaFiles.size() - 1;
                            Log.d("smartCast", "currentIndex: " + index);
                        }
                    }
                }
            }catch(IOException e){
                Log.e("SmartCast", "reading order file", e);
            }
        }
        if(0<=index && index < metaFiles.size()){
            String previousFile, currentFile;
            do{
                index += 1;
                previousFile = metaFiles.get(index -1).getBaseFile();
                currentFile = metaFiles.get(index).getBaseFile();
            }while(index < metaFiles.size() && currentFile.equals(previousFile));
        }

        //look for found files in the list
        ArrayList<File> foundFiles = new ArrayList<File>();
        for(File file: files){
            if(file.length()==0){
                file.delete();
                continue;
            }
            if(file.getName().endsWith(".mp3")||file.getName().endsWith("3pg") || file.getName().endsWith(".ogg")){
                if(!alreadyAdded(file)){
                    if(0 <= index && isPriority(file)){
                        Log.d("CarCast", "adding: " + index + " " + file.getName());
                        metaFiles.add(index++, new MetaFile(file));
                        fileAddedtoOrder = true;
                    }else{
                        // append the new file
                        foundFiles.add(file);
                    }
                }
            }
        }
        //sort the files
        Collections.sort(foundFiles, new Comparator<File>() {
            @Override
            public int compare(File obj1, File obj2) {
                return obj1.getName().compareTo(obj2.getName());
            }
        });

        Log.i("Smartcast", "loadMeta found:"+foundFiles.size()+" meta:"+metaFiles.size());
        for(File file:foundFiles){
            metaFiles.add(new MetaFile(file));
        }

        ///save any changes
        if(fileAddedtoOrder)
            saveOrder();
    }


    boolean alreadyAdded(File file){
        for(MetaFile metaFile : metaFiles){
            if(metaFile.getFileName().equals(file.getName())){
                return true;
            }
        }
        return false;
    }


    // sorted set manipulatation allowing user to move podcasts up and down the data structure
    public SortedSet<Integer> moveTop(SortedSet<Integer> checkedFiles){
        List<MetaFile> topFiles = new ArrayList<MetaFile>();
        Integer [] checkedArray = checkedFiles.toArray(new Integer[0]);

        for(int i = checkedFiles.size()-1;i>=0;i--){
            topFiles.add(0, metaFiles.get(checkedArray[i]));
            metaFiles.remove(metaFiles.get(checkedArray[i]));
        }
        for(MetaFile metaFile: topFiles){
            metaFiles.add(0,metaFile);
        }
        checkedFiles.clear();
        for(MetaFile top: topFiles){
            checkedFiles.add(metaFiles.indexOf(top));
        }
        saveOrder();
        return checkedFiles;
    }


    public SortedSet<Integer> moveUp(SortedSet<Integer> checkedFiles){
        for(int i = 0; i < metaFiles.size();i++){
            if(checkedFiles.contains(i)){
                if(!checkedFiles.contains(i-1)){
                    swapBack(checkedFiles, i);
                }
            }
        }
        saveOrder();
        return checkedFiles;
    }

    private void swapBack(SortedSet<Integer> checkedFiles, int i){
        if(i==0){
            return;
        }
        checkedFiles.remove(i);
        checkedFiles.add(i-1);
        MetaFile temp = metaFiles.remove(i);
        metaFiles.add(i-1, temp);
    }


    public SortedSet<Integer> moveBottom(SortedSet<Integer> checkedFiles){
        List<MetaFile> bottomFiles = new ArrayList<MetaFile>();
        Integer [] checkedArray = checkedFiles.toArray(new Integer[0]);

        for (int i = checkedFiles.size() - 1; i >= 0; i--) {
            bottomFiles.add(0, metaFiles.get(checkedArray[i]));
            metaFiles.remove(metaFiles.get(checkedArray[i]));
        }
        for (MetaFile metaFile : bottomFiles) {
            metaFiles.add(metaFile);
        }
        checkedFiles.clear();
        for (MetaFile atop : bottomFiles) {
            checkedFiles.add(metaFiles.indexOf(atop));
        }
        saveOrder();
        return checkedFiles;
    }


    public SortedSet<Integer> moveDown(SortedSet<Integer> checkedFiles) {
        for (int i = metaFiles.size() - 2; i >= 0; i--) {
            if (checkedFiles.contains(i)) {
                if (!checkedFiles.contains(i + 1)) {
                    swapForward(checkedFiles, i);
                }
            }
        }
        saveOrder();
        return checkedFiles;
    }


    private void swapForward(SortedSet<Integer> checkedFiles, int i) {
        checkedFiles.remove(i);
        checkedFiles.add(i + 1);
        MetaFile o = metaFiles.remove(i);
        metaFiles.add(i + 1, o);
    }


    private void saveOrder(){

        File order = configFolder.getPodcastRootPath("podcast-order.txt");
        StringBuilder sb = new StringBuilder();
        for(MetaFile metaFile:metaFiles){
            sb.append(metaFile.getFileName());
            sb.append('\n');
        }
        try{
            FileOutputStream fos = new FileOutputStream(order);
            fos.write(sb.toString().getBytes());
            fos.close();
        }catch (Exception e){
            Log.e("carcast", "saving order", e);
        }
    }

    //regex used in this method must match file naming used in the podcastDownloader
    private boolean isPriority(File file){
        String regex = "^\\d+:\\d\\d:\\d+\\..*"; // E.g. "YYYY:00:XXXX.mp3"
        boolean is_priority = file.getName().matches(regex);
        Log.d("SmartCast", "priority: " + is_priority + " " + file.getName());
        return is_priority;
    }


}
