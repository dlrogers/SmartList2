/**
 * Created by dennis on 10/27/15.
 */
package com.symdesign.smartlist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.HandlerThread;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;
import com.symdesign.smartlist.TextHandler;


public class SLAdapter extends SimpleCursorAdapter  {
    Context context;
    Activity activity;
    static Cursor listItems,suggestItems;
    static SLAdapter listAdapter,suggestAdapter;
    static ContentValues values = new ContentValues();
    public Cursor cursor;
    public TextHandler textHandler;
    public boolean checked=false;

    public SLAdapter(Context context,int layout,
                     Cursor c,String[] from, int[] to, int flag) {
        super(context,layout,c,from,to,flag);
        this.context=context;
        this.cursor = c;
        this.activity=(Activity) context;
        MainActivity.clickLocation = MainActivity.ClickLocation.none;
    }
    public static void updateAdapters(){

        String[] cols = {"_id","name","inList","last_time","last_avg","ratio"};  	// Columns to return

        // get cursor for shopping list
        listItems = MainActivity.db.query("itemDb",cols,"inList=1",null,"","","ratio DESC");
        listAdapter = new SLAdapter(MainActivity.context,
                R.layout.list_entry,listItems,new String[] {"name"},
                new int[] {R.id.name},CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        listAdapter.checked = false;
        MainActivity.listView.setAdapter(listAdapter);

        // get cursor for suggestion list
        suggestItems = MainActivity.db.query("itemDb",cols,"inList=0",null,"","",
                "ratio DESC");
        suggestAdapter = new SLAdapter(MainActivity.context,
                R.layout.suggest_entry,suggestItems,new String[] {"name"},
                new int[] {R.id.name},CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
//		}
        suggestAdapter.checked = true;
        MainActivity.suggestView.setAdapter(suggestAdapter);
        updateRatios();
        prtSuggestions();
//		setListeners(suggestItems,suggestAdapter,MainActivity.suggestView);
//		listItems.close();
    }
    static public void updateRatios() {

        for(suggestItems.moveToFirst();!suggestItems.isAfterLast(); suggestItems.moveToNext()){
            MainActivity.listValues.clear();
            MainActivity.listValues.put("ratio",((float)(System.currentTimeMillis()- suggestItems.getLong(3)))/
                    ((float)(suggestItems.getLong(4))));
            long id = suggestItems.getLong(0);
            MainActivity.db.update("itemDb",MainActivity.listValues,"_id="+Long.toString(id),null);
        }
    }
    public static void log(String str) {
        Log.v("smartlist", str);
    }
    public static void logF(String fmt,Object... arg){
        Log.v("smartlist",String.format(fmt,arg));
    }

    public static void prtSuggestions() {
        log("list:");
        for(listItems.moveToFirst();!listItems.isAfterLast(); listItems.moveToNext()){
            logF("id=%d nm=%s, la=%d, rat=%f",
                    listItems.getLong(0),listItems.getString(1),listItems.getLong(4),
                    listItems.getFloat(5));
        }
        log("suggestion:");
        for(suggestItems.moveToFirst();!suggestItems.isAfterLast(); suggestItems.moveToNext()){
            logF("id=%d nm=%s, la=%d, rat=%f",
                    suggestItems.getLong(0),suggestItems.getString(1),suggestItems.getLong(4),
                    suggestItems.getFloat(5));
        }
    }

}
