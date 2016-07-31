package com.symdesign.smartlist;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by dennis on 7/1/16.
 */
public class PickList extends Activity {
    Activity thisActivity;
    Context context;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;

    static List<String> catagories;         // Food catagories
    static HashMap<String, List<String>> items; // Mapping from catagories to lists of food items


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisActivity=this;
        context = this;
        setContentView(R.layout.pick_list);
        expListView = (ExpandableListView) findViewById(R.id.lvExp);


        listAdapter = new ExpandableListAdapter(this, catagories, items);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // Toast.makeText(getApplicationContext(),
                // "Group Clicked " + catagories.get(groupPosition),
                // Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        catagories.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        catagories.get(groupPosition) + " Collapsed",
                        Toast.LENGTH_SHORT).show();

            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
                Toast.makeText(
                        getApplicationContext(),
                        catagories.get(groupPosition)
                                + " : "
                                + items.get(
                                catagories.get(groupPosition)).get(
                                childPosition), Toast.LENGTH_SHORT)
                        .show();
                return false;
            }
        });
    }
    /*
     * Preparing the list data
     */
    public static void prepareListData() {

        BufferedReader rdr;
        InputStream input;
        catagories = new ArrayList<String>();
        items = new HashMap<String, List<String>>();
        String currCat = null;
        List<String> currItems = new ArrayList<String>();

        try{
            input = MainActivity.assetManager.open("Master_Grocery_List.txt");
            rdr = new BufferedReader(new InputStreamReader(input));
            boolean first = true;
            int chr;
//            for(chr=rdr.read(); chr!=-1; chr=rdr.read()){
            for(;;){
                chr = rdr.read();
                if(chr==-1) break;
                if(!(chr==61551)) {      // is a Catagory
                    if(!first) {
                        items.put(currCat,currItems);
                        first = false;
                    }
                    first = false;
                    if((currCat = rdr.readLine())==null) break;
                    currCat = ((char) chr)+currCat;
                    currItems.clear();
                    catagories.add(currCat);
                }
                else {                      // is item
                    currItems.add(rdr.readLine());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(String cat : catagories){
            MainActivity.logF("catagory = %s",cat);
            for(String str : items.get(cat))
                MainActivity.logF("item = %s",str);
        }
    }

}