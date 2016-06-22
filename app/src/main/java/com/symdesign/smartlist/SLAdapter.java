/**
 * Created by dennis on 10/27/15.
 */
package com.symdesign.smartlist;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.HandlerThread;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.symdesign.smartlist.TextHandler;

import java.util.ArrayList;

import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.MainActivity.clickLocation;
import static com.symdesign.smartlist.MainActivity.context;
import static com.symdesign.smartlist.MainActivity.listView;
import static com.symdesign.smartlist.MainActivity.suggestView;
import static com.symdesign.smartlist.MainActivity.listValues;
import static com.symdesign.smartlist.MainActivity.minute;
import static com.symdesign.smartlist.MainActivity.getTime;
import static com.symdesign.smartlist.MainActivity.Item;


public class SLAdapter extends ArrayAdapter<Item> {
    Activity activity;
    static Cursor listItems,suggestItems;
    static SLAdapter listAdapter,suggestAdapter;
    static ContentValues values = new ContentValues();
    static ArrayList<Item> items = new ArrayList<Item>();
    public Cursor cursor;
    public TextHandler textHandler;
    public boolean checked=false;
    static String[] cols = {"_id","name","inList","last_time","last_avg","ratio"};

    public SLAdapter(Context context,ArrayList<Item> items) {
        super(context,0,items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = getItem(position);
        if(convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_entry,parent,false);
        TextView slName = (TextView) convertView.findViewById(R.id.name);
        slName.setText(item.name);
        ChkBox ck = (ChkBox) convertView.findViewById(R.id.list_cbox);
        return convertView;
    }

    public static void updateAdapters(){


        // get cursor for shopping list
        updateRatios();
        listItems = db.query("itemDb",cols,"inList=1 OR inList=-1",null,"","",null);
        int i=0; items.clear();
        for(listItems.moveToFirst();!listItems.isAfterLast(); listItems.moveToNext()){
            items.add(new Item(listItems.getInt(0),listItems.getString(1),
                    listItems.getInt(2), listItems.getInt(3), listItems.getInt(4),
                    ((float)(getTime()- suggestItems.getLong(3)))/
                    ((float)(suggestItems.getLong(4)))));
        }
        listAdapter = new SLAdapter(MainActivity.context,
                R.layout.list_entry,listItems,new String[] {"name"},
                new int[] {R.id.name},CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listAdapter.checked = false;
        listView.setAdapter(listAdapter);

        // get cursor for suggestion list
        suggestItems = db.query("itemDb",cols,"inList=0",null,"","",
                "ratio DESC");
        suggestAdapter = new SLAdapter(MainActivity.context,
                R.layout.suggest_entry,suggestItems,new String[] {"name"},
                new int[] {R.id.name},CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
//		}
        suggestAdapter.checked = true;
        suggestView.setAdapter(suggestAdapter);
//        prtSuggestions();   // debug
//		setListeners(suggestItems,suggestAdapter,suggestView);
//		listItems.close();
    }
    /**
     *      Update ratio column in database
     */
    static public void updateRatios() {

        suggestItems = db.query("itemDb",cols,"inList=0",null,"","","ratio DESC");
        for(suggestItems.moveToFirst();!suggestItems.isAfterLast(); suggestItems.moveToNext()){
            listValues.clear();
            float rat = ((float)(getTime()- suggestItems.getLong(3)))/
                    ((float)(suggestItems.getLong(4)));
            listValues.put("ratio",rat);
//            logF("%s, rat = %f, lt = %d, la = %d",suggestItems.getString(1),rat,
//                    suggestItems.getLong(3)/minute,suggestItems.getLong(4)/minute);
            long id = suggestItems.getLong(0);
            db.update("itemDb", listValues, "_id="+Long.toString(id),null);
        }
    }
    public static void log(String str) {
        Log.v("smartlist", str);
    }
    public static void logF(String fmt, Object... arg){
        Log.v("smartlist",String.format(fmt,arg));
    }

    public static void prtSuggestions() {
        log("list:");
        listItems = db.query("itemDb",cols,"inList=1 OR inList=-1",null,"","","ratio DESC");
        for(listItems.moveToFirst();!listItems.isAfterLast(); listItems.moveToNext()){
            logF("id=%d, nm=%s, lt=%d, la=%d, rat=%f",
                    listItems.getLong(0),listItems.getString(1),listItems.getLong(3),listItems.getLong(4),
                    listItems.getFloat(5));
        }
        log("suggestion:");
        suggestItems = db.query("itemDb",cols,"inList=0",null,"","","ratio DESC");
        for(suggestItems.moveToFirst();!suggestItems.isAfterLast(); suggestItems.moveToNext()){
            logF("id=%d, nm=%s, lt=%d, la=%d, rat=%f",
                    suggestItems.getLong(0),suggestItems.getString(1),suggestItems.getLong(3),suggestItems.getLong(4),
                    suggestItems.getFloat(5));
        }
    }


}
