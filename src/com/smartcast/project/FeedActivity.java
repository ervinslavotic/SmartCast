package com.smartcast.project;

import java.io.File;
import android.os.Bundle;
import android.app.Activity;
import android.app.ListActivity;
import android.view.Menu;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.ListView;
import java.util.ArrayList;
import android.view.View;
import android.content.Intent;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map.Entry;
import android.util.Log;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import android.media.MediaPlayer;
import com.smartcast.project.R;

public class FeedActivity extends ListActivity {

    //LIST OF ARRAY STRINGS WHICH WILL SERVE AS LIST ITEMS
    ArrayList<String> listItems=new ArrayList<String>();


    // tage constant goes here
    private static final String TAG = "FeedActivity";
    //DEFINING A STRING ADAPTER WHICH WILL HANDLE THE DATA OF THE LISTVIEW
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_feed);
        Intent reciever = getIntent();
        Bundle bundle = getIntent().getExtras();// get content from main activity
        File getSelectedPodcast;// used for playing the audio file in the play() method


        ArrayList<HashMap<String,String>> stream_list = (ArrayList<HashMap<String, String>>) reciever.getSerializableExtra("feedList");

        ArrayList<HashMap<String,String>> test = new ArrayList<HashMap<String, String>>();


        HashMap<String, String> tester = new HashMap<String, String>();


        tester.put("This is a ", "Test");

        test.add(tester);
        // iterator to iterate throught the items in the area obtained by the podcast organizer class
        Log.w(TAG, "before the for loop iterator ");
        // loop through arraylist of hashmaps
        for (int a =0; a<stream_list.size();a++){
            for(int b=a+1; b<stream_list.size();b++){
                Log.w(TAG, "inside the loop iterator ");
                for (HashMap<String, String> hm : stream_list){

                    //String insert = hm.get()
                    String value = hm.get("VendorName");
                    if(!listItems.contains(value))// check duplicate views
                        listItems.add(value);
                }
            }
        }

        // displaying items to the listveiw
        // here is the TODO

        adapter = new ArrayAdapter<String>(this, R.layout.dynamic_content_feed, listItems);

        final ListView listView = getListView();

        setListAdapter(adapter);

        listView.setAdapter(adapter);


        final Intent intent = new Intent(this, MainActivity.class);




        listView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View itemView, int itemPosition, long itemId) {



            }
        });



/*



    // intent to be sent to main activity
                TextView txt = (TextView) parent.getChildAt(itemPosition - listView.getFirstVisiblePosition()).findViewById(R.id.text1);
                String keyword = txt.getText().toString();
                Log.v("value ", "result is " + keyword);
                intent.putExtra("userSelect", keyword);
                startActivity(intent);

        public void sendMessage(View view) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.
            startActivity(intent);
        }

         Intent stream_activity_intent = new Intent(this, StreamActivity.class);
        stream_activity_intent.putExtra("streamList", podcasts);
        */



    }


    public void playPodcast()
    {
        //todo
    }


    //onclick dynamic listener for podcasts

    // handling insertion
    public void addItems(View v) {
        listItems.add("Clicked : ");
        adapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.stream, menu);
        return true;
    }

}


