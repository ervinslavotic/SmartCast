package com.smartcast.project.stream;


import com.smartcast.project.stream.RssItem;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import java.util.List;


/**
 * Created by Slavotic on 20/05/14.
 */
public class ListListener implements OnItemClickListener{

    private List<RssItem> listItem;
    Activity activity;

    public ListListener(List<RssItem> listItem, Activity activity){
        this.listItem = listItem;
        this.activity = activity;
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(listItem.get(position).getLink()));
        activity.startActivity(intent);
    }


}
