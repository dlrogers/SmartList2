/**
 * Created by dennis on 10/27/15.
 */
package com.symdesign.smartlist;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;

import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.MainActivity.context;
import static com.symdesign.smartlist.MainActivity.listView;
import static com.symdesign.smartlist.MainActivity.suggestView;
import static com.symdesign.smartlist.MainActivity.getTime;


public class SLAdapter extends ArrayAdapter<Item> {
    static Cursor listItems,suggestItems;
    static SLAdapter listAdapter,suggestAdapter;
    static ArrayList<Item> itemsList = new ArrayList<>();
    static ArrayList<Item> itemsSuggest = new ArrayList<>();
    public boolean checked=false;
    static String[] cols = {"_id","name","inList","last_time","last_avg","ratio"};
    int layout;

    public SLAdapter(Context context,ArrayList<Item> items, int box_layout)
    {
        super(context,0,items);
        layout = box_layout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = getItem(position);
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(layout,parent,false);
        TextView slName = (TextView) convertView.findViewById(R.id.name);
        slName.setText(item.name);
//        ChkBox ck = (ChkBox) convertView.findViewById(R.id.list_cbox);
        return convertView;
    }

    public static void updateAdapters(){

        // get cursor for shopping list
        long time_millis = System.currentTimeMillis();
        listItems = db.query("itemDb",cols,"inList=1 OR inList=-1",null,"","",null);
        itemsList.clear(); int n=0;
        for(listItems.moveToFirst();!listItems.isAfterLast(); listItems.moveToNext()) {
            itemsList.add(new Item(listItems.getLong(0),listItems.getString(1),
                    listItems.getLong(2), listItems.getLong(3), listItems.getLong(4),
                    ((float)(getTime()- listItems.getLong(3)))/((float)(listItems.getLong(4)))));
            n++;
        }
        Collections.sort(itemsList);
        listAdapter = new SLAdapter(context,itemsList,R.layout.list_entry);
        listAdapter.checked = false;
        listView.setAdapter(listAdapter);

        // get cursor for suggestion list
        suggestItems = db.query("itemDb",cols,"inList=0",null,"","",
                "ratio DESC");
        itemsSuggest.clear(); n = 0;
        for(suggestItems.moveToFirst();!suggestItems.isAfterLast(); suggestItems.moveToNext()) {
            itemsSuggest.add(new Item(suggestItems.getLong(0),suggestItems.getString(1),
                    suggestItems.getLong(2), suggestItems.getLong(3), suggestItems.getLong(4),
                    ((float)(getTime()- suggestItems.getLong(3)))/
                            ((float)(suggestItems.getLong(4)))));
            n++;
        }
        Collections.sort(itemsSuggest);
//        prtElapsed(time_millis);
//        log("suggest\n");
//        prtItems(n,itemsSuggest);
        suggestAdapter = new SLAdapter(context,itemsSuggest,R.layout.suggest_entry);
        suggestAdapter.checked = true;
        suggestView.setAdapter(suggestAdapter);

//        prtSuggestions();   // debug
//		setListeners(suggestItems,suggestAdapter,suggestView);
//		listItems.close();
    }
    static public void prtItems(int n,ArrayList<Item> items) {
        for(int i=0;i<n;i++) {
            Item item = items.get(i);
            logF("id=%d, nm=%s, il=%d, t-lt=%d, la=%d, rat=%f5.11",
                    item.id,item.name,item.inList,(getTime()-item.last_time),item.last_avg,item.ratio);
        }

    }
    static public void prtElapsed(long time) {
        logF("Elapsed time = %d",System.currentTimeMillis()-time);
    }
    /**
     *      Update ratio column in database
     */
    static public void updateRatios() {
        Item item;
        int n,i;
        n=itemsSuggest.size();
        for(i=0;i<n;i++) {
            item = itemsSuggest.get(i);
            item.ratio = (getTime()-item.last_time)/item.last_avg;
        }
        n=itemsList.size();
        for(i=0;i<n;i++) {
            item = itemsList.get(i);
            item.ratio = (getTime()-item.last_time)/item.last_avg;
        }
    }
    public static void log(String str) {
        Log.v("smartlist", str);
    }
    public static void logF(String fmt, Object... arg){
        Log.v("smartlist",String.format(fmt,arg));
    }

    public static void prtSuggestions() {
        listItems = db.query("itemDb",cols,"inList=1 OR inList=-1",null,"","","ratio DESC");
        for(listItems.moveToFirst();!listItems.isAfterLast(); listItems.moveToNext()){
            logF("id=%d, nm=%s, lt=%d, la=%d, rat=%f",
                    listItems.getLong(0),listItems.getString(1),listItems.getLong(3),listItems.getLong(4),
                    listItems.getFloat(5));
        }
        suggestItems = db.query("itemDb",cols,"inList=0",null,"","","ratio DESC");
        for(suggestItems.moveToFirst();!suggestItems.isAfterLast(); suggestItems.moveToNext()){
            logF("id=%d, nm=%s, lt=%d, la=%d, rat=%f",
                    suggestItems.getLong(0),suggestItems.getString(1),suggestItems.getLong(3),suggestItems.getLong(4),
                    suggestItems.getFloat(5));
        }
    }


}
