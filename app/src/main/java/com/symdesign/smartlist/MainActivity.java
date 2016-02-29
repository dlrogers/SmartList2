package com.symdesign.smartlist;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import static com.symdesign.smartlist.SLAdapter.*;

public class MainActivity extends AppCompatActivity {

    ItemDb itemDb;
    static Context context;
    static ContentValues listValues = new ContentValues();
    static SQLiteDatabase db;
    static ListView listView,suggestView;
    static Button listAdd,doneAdd;
    static EditText nameAdd;
    static SLDialog sld;
    static boolean[] selected = new boolean[256];
    public static enum ClickLocation {none,del,name,box};
    static ClickLocation clickLocation;
    static final long second = 1000, minute = 60*second, hour = 60*minute,
            day = 24*hour, week = 7*day, repeat_time =40*second;
    static int scrn_width,scrn_height,VOICE_RECOGNITION_REQUEST_CODE=2;
    static final int MSG_REPEAT=1;
    static Activity thisActivity;
    private static final String SQL_CREATE =
            "CREATE TABLE itemDb(_id INTEGER PRIMARY KEY, name TEXT, inList INT, last_time INT, last_avg INT, ratio REAL)";
    static Handler slHandler = new SLHandler();
    final String[] avgCols={"last_time","last_avg","inList"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisActivity=this;
        context = this;
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageView micView = (ImageView) findViewById(R.id.mic);
        Button syncButton = (Button) findViewById(R.id.sync);
        Button testButton = (Button) findViewById(R.id.test);
        setSupportActionBar(toolbar);
        Message repeat = Message.obtain(slHandler,MSG_REPEAT);
        slHandler.sendMessageDelayed(repeat, repeat_time);
        micView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log("Microphone clicked");
                SpeechRecognitionHelper helper = new SpeechRecognitionHelper();
                SpeechRecognitionHelper.run(thisActivity);
            }
        });
        syncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatabaseSync().execute();
            }
        });
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SLAdapter.prtSuggestions();
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sld = SLDialog.newInstance();
                sld.edit=false;
                sld.title = "New Item";
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
        listAdd = (Button) findViewById(R.id.list_add);
        listView=(ListView) findViewById(R.id.list_view);
        suggestView = (ListView) findViewById(R.id.suggest_view);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        scrn_width = size.x;
        scrn_height = size.y;
        itemDb = new ItemDb(context);
        db = itemDb.getWritableDatabase();

//		db.execSQL("DROP TABLE itemDb");
//		db.execSQL(SQL_CREATE);
//		initDB(db);

        log("starting smartlist");
        updateAdapters();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                //               logF("clickLocation = %d, adapter = %s",clickLocation,parent.getAdapter().toString());
                ImageView delView = (ImageView) item.findViewById(R.id.del);
                EditText nameView = (EditText) item.findViewById(R.id.name);
                switch (clickLocation) {
                    case box:
                        clickLocation = ClickLocation.none;
                        logF("avgCols = %s", avgCols);
                        updateAvgs(id,0);
                        break;
                    case name:
                        sld = SLDialog.newInstance();
                        sld.edit=true;
                        sld.list=true;
                        sld.name = nameView.getText();
                        sld.id=id;
                        sld.title = "Edit Item";
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        sld.show(ft, "sldialog tag");
                        log("list name clicked");
                        break;
                    case del:
                        listValues.clear();
                        db.delete("itemDb", "_id=" + Long.toString(id), null);
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
                ImageView delView = (ImageView) item.findViewById(R.id.del);
                EditText nameView = (EditText) item.findViewById(R.id.name);
                nameView.setWidth((int) .8*item.getWidth());
/*                if(!selected[position]){
                    delView.setVisibility(View.VISIBLE);
                    setSelected(item,position,true);
                }
                else {
                    delView.setVisibility(View.INVISIBLE);
                    setSelected(item,position,false);
                }
*/                switch (clickLocation) {
                    case box:
                        clickLocation = ClickLocation.none;
                        listValues.put("inList",1);
                        db.update("itemDb", listValues, "_id=" + Long.toString(id), null);
                        updateAdapters();
                        break;
                    case name:
                        sld = SLDialog.newInstance();
                        sld.edit=true;
                        sld.list=false;
                        sld.name = nameView.getText();
                        sld.id=id;
                        sld.title = "Edit Item";
                        FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        sld.show(ft, "sldialog tag");
                        log("suggest name clicked");
                        break;
                    case del:
                        listValues.clear();
                        db.delete("itemDb","_id=" + Long.toString(id), null);
                        updateAdapters();
//                        setSelected(item,position,false);
                        log("list del clicked");
                        break;
                }
            }
        });
        //      Process Add Button
        listAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sld = SLDialog.newInstance();
                FragmentManager fm = getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                sld.title="Create new item";
                sld.show(ft, "sldialog tag");
            }
        });
    }
    @Override public void onPause() {
        super.onPause();
    }
    @Override public void onResume() {
        super.onResume();
        db = itemDb.getWritableDatabase();
    }
    @Override
    protected void onStop() {
        super.onStop();
        db.close();
    }

    /**
     * Update Frequency averages in database
     * @param id ID of currently selected item
     * @param inList New state of inList entry
     */
    public void updateAvgs(long id, int inList) {
        Cursor curs = db.query("itemDb", avgCols, "_id=" + Long.toString(id), null, "", "", "name ASC");
        curs.moveToFirst();
        long last = curs.getLong(0);
        long avg = curs.getLong(1);
        long ct = System.currentTimeMillis();
        listValues.clear();
        if(avg == 0)
            listValues.put("last_avg",ct-last);
        else {
            listValues.put("last_avg",(long) (0.33333*(ct-last)+0.66667*avg));
        }
        listValues.put("inList", Math.abs(inList));
        listValues.put("last_time", ct);
        db.update("itemDb", listValues, "_id=" + Long.toString(id), null);
        updateAdapters();
        curs.close();
    }

/*    public void setSelected(View view,int pos, boolean on) {
        ImageView delView = (ImageView) view.findViewById(R.id.del);
        if(on) {
            selected[pos]=true;
            view.setBackgroundColor(Color.CYAN);
            delView.setVisibility(View.VISIBLE);
        } else {
            selected[pos]=false;
            delView.setVisibility(View.INVISIBLE);
            view.setBackgroundColor(Color.parseColor("#f8f8f8"));
        }
    }
*/    @Override
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

/*    public void onCheckboxClicked(CheckBox view) {
        boolean checked = view.isChecked();
        if(checked)
            view.setChecked(false);
        else
            view.setChecked(true);
        log("checkbox clicked.");
    }
*/
    public class ItemDb extends SQLiteOpenHelper {

        public ItemDb(Context context) {
            super(context,"items.db",null,1);
        }
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS itemDb");
        }
        public void onDownGrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            onUpgrade(db,oldVersion,newVersion);
        }
    }

    public void initDB(SQLiteDatabase db) {
//		listValues = new ContentValues();
        Long date = System.currentTimeMillis();
        listValues = new ContentValues();
        listValues.clear();
        addItem("apples",1,date,30*second,0.0);
        addItem("oranges",1,date,30*second,0.0);
        addItem("milk", 0, date, 30 *second,0.0);
        addItem("bacon", 0, date, 30 *second,0.0);
/*        addItem("eggs",0,date,30*second,0.0);
        addItem("lettuce",1,date,30*second,0.0);
        addItem("OJ",0,date,30*second,0.0);
        addItem("cereal",0,date,30*second,0.0);
        addItem("blueberries",1,date,30*second,0.0);
        addItem("kefir",0,date,30*second,0.0);
        addItem("potatoes",0,date,30*second,0.0);
        addItem("wine",0,date,30*second,0.0);
        addItem("beer",1,date,30*second,0.0);
        addItem("chili",1,date,30*second,0.0);
        addItem("chocolate",1,date,30*second,0.0);
        addItem("vitamins",0,date,30*second,0.0);
        addItem("selzer",0,date,30*second,0.0);
        addItem("bananas",1,date,30*second,0.0);
*/    }
    public static void addItem(String nm,int il,long lt,long la,double r){
        listValues.clear();
        listValues.put("name",nm);
        listValues.put("inList", il);
        listValues.put("last_time", Math.abs(lt));
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
        addItem(nm, 1, System.currentTimeMillis(), 3 * day, 0);
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
            Long date = System.currentTimeMillis();
            String name = (String) matches.get(0);
            // A negative value of inList indicates a new item
            MainActivity.addItem((String) name, -1, System.currentTimeMillis(), 7*day, 0);
            updateAdapters();

        }

        super.onActivityResult(requestCode, resultCode, data);
    }
    /**
     * Created by dennis on 2/18/16.
     */
}
