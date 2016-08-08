package com.symdesign.smartlist;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.symdesign.smartlist.SLAdapter.*;

// Version of SmartList that uses file to store databae on phone

public class MainActivity extends AppCompatActivity {

    static ItemDb itemDb;
    static Context context;
    static ContentValues listValues = new ContentValues();
    public static SQLiteDatabase db;
    static ListView listView,suggestView;
    static SLDialog sld;
    static boolean[] selected = new boolean[256];
    public enum ClickLocation {none,del,name,box}
    static ClickLocation clickLocation;
    static final long second=1, minute = 60*second, hour = 60*minute,
            day = 24*hour, week = 7*day, repeat_time =5*minute;
    static int scrn_width,scrn_height,VOICE_RECOGNITION_REQUEST_CODE=2;
    static final int MSG_REPEAT=1;
    static Activity thisActivity;
    static final String SQL_CREATE =
            "CREATE TABLE itemDb(_id INTEGER PRIMARY KEY, name TEXT, inList INT, last_time INT, last_avg INT, ratio REAL)";
    static Handler slHandler = new SLHandler();
    final String[] avgCols={"last_time","last_avg","inList"};
    static AssetManager assetManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisActivity=this;
        context = this;
        setContentView(R.layout.activity_main);
        assetManager = getAssets();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView micView = (ImageView) findViewById(R.id.mic);
        Button syncButton = (Button) findViewById(R.id.sync);
        Button testButton = (Button) findViewById(R.id.test);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view);
        scrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log("ScrollView clicked");
            }
        });
        setSupportActionBar(toolbar);
        Message repeat = Message.obtain(slHandler,MSG_REPEAT);
        slHandler.sendMessageDelayed(repeat, repeat_time);
        //  Microphone button, response received by onActivityResult(...)
        micView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log("Microphone clicked");
                SpeechRecognitionHelper.run(thisActivity);
            }
        });
        //          Sync Button
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { new DatabaseSync().execute(); }
        });
        //          Test button
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SLAdapter.updateAdapters();
            }
        });
        //          Add Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sld = SLDialog.newInstance();
                SLDialog.edit=false;
                SLDialog.title = "New Item";
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                sld.show(ft, "sldialog tag");
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
        context=this;
        for(int i=0; i<256; i++)
            selected[i] = false;
        clickLocation = ClickLocation.none;
//        listAdd = (Button) findViewById(R.id.list_add);
        listView=(ListView) findViewById(R.id.list_view);
        suggestView = (ListView) findViewById(R.id.suggest_view);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        scrn_width = size.x;
        scrn_height = size.y;
        itemDb = new ItemDb(context);
        db = itemDb.getWritableDatabase();

/*		db.execSQL("DROP TABLE itemDb");
		db.execSQL(SQL_CREATE);
		initDB(db);
*/
        PickList.prepareListData();

        log(String.format("Starting Smartlist, time=%d",getTime()));
        updateAdapters();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                EditText nameView = (EditText) item.findViewById(R.id.name);
                listValues.clear();
                long dBid = itemsList.get(position).id;
                switch (clickLocation) {
                    case box:
                        clickLocation = ClickLocation.none;
                        updateAvgs(dBid,0);
                        break;
                    case name:
                        sld = SLDialog.newInstance();
                        SLDialog.edit=true;
                        SLDialog.list=true;
                        SLDialog.name = nameView.getText();
                        SLDialog.id=dBid;
                        SLDialog.title = "Edit Item";
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        sld.show(ft, "sldialog tag");
                        log("list name clicked");
                        break;
                    case del:
                        db.delete("itemDb", "_id=" + Long.toString(dBid), null);
                        itemsList.remove(id);
                        updateAdapters();
//                        setSelected(item,position,false);
                        log("list del clicked");
                        break;
                }
            }
        });
        suggestView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item,
                                    int position, long id) {
                EditText nameView = (EditText) item.findViewById(R.id.name);
//                nameView.setWidth((int) .8*item.getWidth());
                listValues.clear();
                long dBid = itemsSuggest.get(position).id;
                switch (clickLocation) {
                    case box:
                        clickLocation = ClickLocation.none;
                        listValues.put("inList",1);
                        db.update("itemDb", listValues,
                                "_id=" + Long.toString(dBid), null);
                        updateAdapters();
                        break;
                    case name:
                        sld = SLDialog.newInstance();
                        SLDialog.edit=true;
                        SLDialog.list=false;
                        SLDialog.name = nameView.getText();
                        SLDialog.id=dBid;
                        SLDialog.title = "Edit Item";
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        sld.show(ft, "sldialog tag");
                        log("suggest name clicked");
                        break;
                    case del:
                        db.delete("itemDb","_id=" + Long.toString(dBid), null);
                        itemsSuggest.remove(id);
                        updateAdapters();
//                        setSelected(item,position,false);
                        log("list del clicked");
                        break;
                }
            }
        });
    }
    @Override public void onPause() {
        super.onPause();
        log("Pausing");
        //itemDb.close();
        db.close();
    }
    @Override public void onResume() {
        super.onResume();
        log("Resuming");
        db = itemDb.getWritableDatabase();
    }
    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }
    /**
     * Update Frequency averages in database for item id
     * @param id ID of currently selected item
     * @param inList New state of inList entry
     */
    public void updateAvgs(long id, int inList) {

        Cursor curs = db.query("itemDb", avgCols, "_id=" + Long.toString(id), null, "", "", "name ASC");
        curs.moveToFirst();
        long last = curs.getLong(0);
        long avg = curs.getLong(1);
        long ct = getTime();
        listValues.clear();
        //            listValues.put("last_avg",(long) (0.33333*(ct-last)+0.66667*avg));
        if (avg > 36499 * day){ // Check for "one time item"
            db.delete("itemDb", "_id=" + Long.toString(id), null); // and delete
        }
        else
            listValues.put("last_avg", running_avg(ct-last, avg));
        listValues.put("inList", Math.abs(inList));
        listValues.put("last_time", ct);
        db.update("itemDb", listValues, "_id=" + Long.toString(id), null);
        updateAdapters();
        curs.close();
    }
    public long running_avg(long elapsed_time,long last_avg) {
        return (long) (0.75*elapsed_time+0.25*last_avg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    public void initDB(SQLiteDatabase db) {
//		listValues = new ContentValues();
        Long date = getTime()-30;
        listValues = new ContentValues();
        listValues.clear();
        addItem("apples",1,date,30,0.0);
        addItem("oranges",1,date,30,0.0);
        addItem("milk", 0, date, 30 ,0.0);
        addItem("bacon", 0, date, 30 ,0.0);
        addItem("eggs",0,date,30,0.0);
        addItem("lettuce",1,date,30,0.0);
        addItem("OJ",0,date,30,0.0);
        addItem("cereal",0,date,30,0.0);
        addItem("blueberries",1,date,30,0.0);
        addItem("kefir",0,date,30,0.0);
        addItem("potatoes",0,date,30,0.0);
        addItem("wine",0,date,30,0.0);
/*        addItem("beer",1,date,30,0.0);
        addItem("chili",1,date,30,0.0);
        addItem("chocolate",1,date,30,0.0);
        addItem("vitamins",0,date,30,0.0);
        addItem("selzer",0,date,30,0.0);
        addItem("bananas",1,date,30,0.0);
*/    }
    public static void addItem(String nm,int il,long lt,long la,double r) {
        listValues.clear();
        listValues.put("name",nm);
        listValues.put("inList", il);
        listValues.put("last_time", lt);
        listValues.put("last_avg", la);
        listValues.put("ratio", r);
        db.insert("itemDb", null, listValues);
    }
    public static void changeItem(String nm,int il,long lt,long la,double r,long id){
        listValues.clear();
        listValues.put("name",nm);
        listValues.put("inList",il);
        listValues.put("last_time", Math.abs(lt));
        listValues.put("last_avg", la);
        listValues.put("ratio", r);
        db.update("itemDb", listValues, "_id=" + Long.toString(id), null);
    }
    public static void deleteItem(long id) {
        db.delete("itemDb", "_id=" + Long.toString(id), null);
    }
    public static void newItem(String nm){
        addItem(nm, 1, getTime(), 3 * day, 0);
    }

    /**
     * Returns time in seconds since 1/1/1970 epoch
     * @return
     */
    public static long getTime(){
        return System.currentTimeMillis()/1000;
    }
    public static void log(String str) {
        Log.v("smartlist", str);
    }
    public static void logF(String fmt,Object... arg){
        Log.v("smartlist", String.format(fmt, arg));
    }
    // Activity Results handler
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // if it’s speech recognition results
        // and process finished ok
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE && resultCode == RESULT_OK) {

            // receiving a result in string array
            // there can be some strings because sometimes speech recognizing inaccurate
            // more relevant results in the beginning of the list
            ArrayList matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

            // in “matches” array we holding a results... let’s show the most relevant
            if (matches.size() > 0) Toast.makeText(this, ((CharSequence) matches.get(0)), Toast.LENGTH_LONG).show();
            Long date = getTime();
            String name = (String) matches.get(0);
            // A negative value of inList indicates a new item
            MainActivity.addItem((String) name, -1, getTime(), 7*day, 0);
            updateAdapters();

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    /**
     * Created by dennis on 2/18/16.
     */
}
