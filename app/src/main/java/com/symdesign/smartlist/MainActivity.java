package com.symdesign.smartlist;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.ArrayList;

import static android.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER;
import static com.symdesign.smartlist.R.id.navList;
import static com.symdesign.smartlist.SLAdapter.*;

// Version of SmartList that uses file to store databae on phone

public class MainActivity extends AppCompatActivity {

    static ItemDb itemDb;
    //    static Context context;
    static ContentValues values = new ContentValues();
    public static SQLiteDatabase db;
    static ListView listView, suggestView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("SmartList") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://www.symdesign.us/listmate"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    public enum ClickLocation {none, del, name, box}

    static ClickLocation clickLocation;
    static final long second = 1, minute = 60 * second, hour = 60 * minute,
            day = 24 * hour, week = 7 * day, repeat_time = 10 ;
    static int scrn_width, scrn_height, VOICE_RECOGNITION_REQUEST_CODE = 2;
    static final int MSG_REPEAT = 1;
    static Activity mainActivity;
    static final String SQL_CREATE =
            "CREATE TABLE Groceries(_id INTEGER PRIMARY KEY, name TEXT, inList INT, last_time INT, last_avg INT, ratio REAL)";
    static final String SQL_LISTS = "CREATE TABLE lists(_id INTEGER PRIMARY KEY, name TEXT, tableid TEXT)";
    static Handler slHandler = new SLHandler();
    static AssetManager assetManager;
    static Toast toast;
    static boolean changed = false;
    static ArrayList<String> deleteList = new ArrayList<>();
    static int nDelete;
//    static ArrayList<ListItem> listTable = new ArrayList<>(5);
    static String currList,currTable,installed;     // Current Shared Prefs
    static android.support.v7.app.ActionBar actionBar;
    static SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        //          Get Shared Preferences
        itemDb = new ItemDb(getContext());
        db = itemDb.getWritableDatabase();
        prefs = getPreferences(MODE_PRIVATE);
//        Code to clear prefs and databases
/*        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.commit();
        */
//        db.execSQL("drop table if exists itemDb");
//        db.execSQL("drop table if exists 'Grocery List'");
//        db.execSQL("drop table if exists lists");
/*        db.execSQL("drop table if exists 'Garden Supplies'");
        db.delete("lists","name='Garden supplies'",null);
        SharedPreferences.Editor ed = prefs.edit();     // Initialize shared preferences
        ed.putString("currList","Groceries");
        ed.putString("currTable","Groceries");
        ed.putString("installed","yes");
        ed.apply();
*/
        //        db.execSQL("drop table if exists lists");
//        db.execSQL(SQL_LISTS);

//        Code to do one time install code

        installed = prefs.getString("installed","no");
//        installed = "no";
        if(installed.equals("no")) {
            db.execSQL("drop table if exists Groceries");   // Remove any exisitng Groceries
            db.execSQL("drop table if exists lists");
            SharedPreferences.Editor ed = prefs.edit();     // Initialize shared preferences
            ed.putString("installed","yes");
            ed.putString("currList","Groceries");
            ed.putString("currTable","Groceries");
            ed.apply();
            db.execSQL(SQL_LISTS);                          // Create "lists" table
            OptionDialog.addToLists("Groceries");
            db.execSQL(SQL_CREATE);
        }
        setupOptions();     // Setup up sidebar
        OptionDialog.showLists();
        printLists();

        currList = prefs.getString("currList","Groceries");
        currTable = prefs.getString("currTable","Groceries");

        assetManager = getAssets();
        PickList.prepareListData();
                    // Get view links
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scroll_view);


/*		db.execSQL("DROP TABLE itemDb");
        db.execSQL(SQL_CREATE);
		initDB(db);
        db.execSQL(SQL_LISTS);
*/
//        final SLOptions slOptions = new SLOptions(drawer);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        actionBar.setTitle(currList);

//        Setup task handler
        Message repeat = Message.obtain(slHandler, MSG_REPEAT);
        slHandler.sendMessageDelayed(repeat, repeat_time);
        deleteList.clear();

//        Setup floating "add" button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("com.symdesign.smartlist.intent.action.PickList");
                intent.putExtra("id", -1);
                intent.putExtra("name", "");
                intent.putExtra("inLists", false);
                MainActivity.getContext().startActivity(intent);
            }
        });

//        Setup left drawer
        drawers = (DrawerLayout) findViewById(R.id.drawer_layout);
        toolbar.setNavigationIcon(R.drawable.ic_menu_moreoverflow_normal_holo_dark);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 drawers.openDrawer(navList);
             }
        });
        clickLocation = ClickLocation.none;
        // Get view links
        listView = (ListView) findViewById(R.id.list_view);
        suggestView = (ListView) findViewById(R.id.suggest_view);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        scrn_width = size.x;
        scrn_height = size.y;

        log(String.format("Starting MainActivity, time=%d", getTime()));
        updateAdapters();
        nDelete = 0;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                EditText nameView = (EditText) item.findViewById(R.id.name);
                values.clear();
                long dBid = itemsList.get(position).id;
                switch (clickLocation) {
                    case box:
                        clickLocation = ClickLocation.none;
                        changed = true;
                        updateAvgs(dBid, 0);
                        break;
                    case name:
                        changed = true;
                        Intent intent = new Intent("com.symdesign.smartlist.intent.action.PickList");
                        intent.putExtra("id", dBid);
                        intent.putExtra("name", itemsList.get(position).name);
                        intent.putExtra("inLists", true);
                        startActivity(intent);
                        break;
                    case del:
                        deleteList.add(itemsList.get(position).name);
                        nDelete++;
                        db.delete(currList, "_id=" + Long.toString(dBid), null);
                        itemsList.remove(id);
                        updateAdapters();
//                        setSelected(item,position,false);
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
                values.clear();
                long dBid = itemsSuggest.get(position).id;
                switch (clickLocation) {
                    case box:
                        clickLocation = ClickLocation.none;
                        changed = true;
                        values.put("inList", 1);     // set il=1
                        db.update(currList, values,
                                "_id=" + Long.toString(dBid), null);
                        updateAdapters();
                        break;
                    case name:
                        changed = true;
                        //                       db.update(currList, values,
                        //                               "_id=" + Long.toString(dBid), null);
                        //                       updateAdapters();
                        Intent intent = new Intent("com.symdesign.smartlist.intent.action.PickList");
                        intent.putExtra("id", dBid);
                        intent.putExtra("name", itemsSuggest.get(position).name);
                        intent.putExtra("inLists", true);
                        startActivity(intent);
                        break;
                    case del:
                        deleteList.add(itemsSuggest.get(position).name);
                        nDelete++;

                        db.delete(currList, "_id=" + Long.toString(dBid), null);
                        itemsSuggest.remove(id);
                        updateAdapters();

                        log("list del clicked");
                        break;
                }
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }
        // Setup Sidebar options
    static DrawerLayout drawers;
    static ListView lists;
    LinearLayout navList;
    Button newList,closeList;

    void setupOptions() {
        navList = (LinearLayout) findViewById(R.id.navList);
        newList = (Button) findViewById(R.id.newList);
        lists = (ListView) findViewById(R.id.lists);
        closeList = (Button) findViewById(R.id.closeList);
            // Create list
        newList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                OptionDialog optionDialog = new OptionDialog();
                optionDialog.show(ft,"dialog");
            }
        });
            // List of lists
        OptionDialog.showLists();   // Regular tap
        lists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                Cursor c = (Cursor) parent.getItemAtPosition(position);
                currList = c.getString(2);
                actionBar.setTitle((CharSequence) c.getString(1));
                SharedPreferences.Editor ed = prefs.edit();
                ed.putString("currList",c.getString(1));
                ed.apply();
                updateAdapters();
                closeDrawer();
                logF("name = %s",c.getString(2));
            }
        });                         // Long tap
        lists.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View item, int position, long id) {
                Cursor c = (Cursor) parent.getItemAtPosition(position);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                LongClickDialog longClickDialog = new LongClickDialog();
                Bundle bundle = new Bundle();
                bundle.putString("name",c.getString(1));
                bundle.putInt("_id",c.getInt(0));
                longClickDialog.setArguments(bundle);
                longClickDialog.show(ft,"dialog");
                return true;
            }
        });
            // Close Button
        closeList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawers.closeDrawer(navList);
            }
        });
    }
    //      Show lists table
    static void printLists() {
        Cursor lsts = db.query("lists",new String[] {"name","tableid"},null,null,null,null,null);
        for(lsts.moveToFirst(); !lsts.isAfterLast(); lsts.moveToNext()) {
            logF("name = %s, tableid = %s",lsts.getString(0),lsts.getString(1));
        }
    }
    static public void closeDrawer() {drawers.closeDrawers();}

    public static Context getContext() {
        return mainActivity;
    }

    @Override
    protected void onStop() {
        super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
//        db.close();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    /**
     * Update Frequency averages in database for item id
     *
     * @param id     ID of currently selected item
     * @param inList New state of inList entry
     */
    public void updateAvgs(long id, int inList) {

        final String[] avgCols = {"last_time", "last_avg", "inList"};

        Cursor curs = db.query(currList, avgCols, "_id=" + Long.toString(id), null, "", "", "name ASC");
        curs.moveToFirst();
        long last = curs.getLong(0);
        long avg = curs.getLong(1);
        long ct = getTime();
        values.clear();
        //            values.put("last_avg",(long) (0.33333*(ct-last)+0.66667*avg));
        if (avg > 36499 * day) { // Check for "one time item"
            db.delete(currList, "_id=" + Long.toString(id), null); // and delete
        } else
            values.put("last_avg", running_avg(ct - last, avg));
        values.put("inList", Math.abs(inList));   // set inList, and dbChg true
        values.put("last_time", ct);
        logF("last_time = %d\t current time = %d\t, last_avg = %d\t, elapsed = %d",last,ct,avg,ct-last);
        db.update(currList, values, "_id=" + Long.toString(id), null);
        updateAdapters();
        curs.close();
    }

    public long running_avg(long elapsed_time, long last_avg) {
        return (long) (0.75 * elapsed_time + 0.25 * last_avg);
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
        switch (item.getItemId()) {
            case R.id.menu:
//                openDrawer();
                log("menu");
                break;
            case R.id.mic:
                log("Microphone clicked");
                SpeechRecognitionHelper.run(mainActivity);
                return true;
            case R.id.sync:
                toast = Toast.makeText(getApplicationContext(),
                        "\nSyncing\n",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 200);
                toast.show();
                new DatabaseSync().execute();
                break;
        }
 /*       if (id == R.id.action_settings) {
            return true;
        }
*/
        return super.onOptionsItemSelected(item);
    }

    public void initDB(SQLiteDatabase db) {
//		values = new ContentValues();
        Long date = getTime() - 30;
        values = new ContentValues();
        values.clear();
        addItem("apples", 1, date, 30, 0.0);
        addItem("oranges", 1, date, 30, 0.0);
        addItem("milk", 0, date, 30, 0.0);
        addItem("bacon", 0, date, 30, 0.0);
        addItem("eggs", 0, date, 30, 0.0);
        addItem("lettuce", 1, date, 30, 0.0);
        addItem("OJ", 0, date, 30, 0.0);
        addItem("cereal", 0, date, 30, 0.0);
        addItem("blueberries", 1, date, 30, 0.0);
        addItem("kefir", 0, date, 30, 0.0);
        addItem("potatoes", 0, date, 30, 0.0);
        addItem("wine", 0, date, 30, 0.0);
    }

    public static void addItem(String nm, int il, long lt, long la, double r) {
        values.clear();
        values.put("name", nm);
        values.put("inList", il);
        values.put("last_time", lt);
        values.put("last_avg", la);
        values.put("ratio", r);
        db.insert(currList, null, values);
    }

    public static void changeItem(String nm, int il, long lt, long la, double r, long id) {
        values.clear();
        values.put("name", nm);
        values.put("inList", il);
        values.put("last_time", Math.abs(lt));
        values.put("last_avg", la);
        values.put("ratio", r);
        db.update(currList, values, "_id=" + Long.toString(id), null);
    }

    public static void deleteItem(long id) {
        db.delete(currList, "_id=" + Long.toString(id), null);
    }

    public static void newItem(String nm) {
        addItem(nm, 1, getTime(), 3 * day, 0);
    }

    /**
     * Returns time in seconds since 1/1/1970 epoch
     *
     * @return
     */
    public static long getTime() {
        return System.currentTimeMillis() / 1000;
    }

    public static void log(String str) {
        Log.v("smartlist", str);
    }

    public static void logF(String fmt, Object... arg) {
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
            if (matches.size() > 0)
                Toast.makeText(this, ((CharSequence) matches.get(0)), Toast.LENGTH_LONG).show();
            Long date = getTime();
            String name = (String) matches.get(0);
            // A negative value of inList indicates a new item
            db = MainActivity.itemDb.getWritableDatabase();
            MainActivity.addItem((String) name, 1, getTime(), 7 * day, 0);
            updateAdapters();
            db.close();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Created by dennis on 2/18/16.
     */
    @Override
    public void onPause() {
        super.onPause();
        log("Pausing MainActivity");
        db.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        log("Resuming MainActivity");
        db = itemDb.getWritableDatabase();
    }
}

/*        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_moreoverflow_normal_holo_dark);
        actionBar.setDisplayHomeAsUpEnabled(true);
        if(actionBar!=null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        toolbar.setSubtitleTextAppearance(this,android.R.style.TextAppearance_Medium);
        toolbar.setSubtitle("Groceries");
*/
/*        scrollView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                log("ScrollView clicked");
            }
        });
*/
/*        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent("com.symdesign.smartlist.intent.action.PickList");
                intent.putExtra("id",-1);
                intent.putExtra("name","");
                intent.putExtra("inLists",false);
                MainActivity.getContext().startActivity(intent);
            }
        });
*/
/*
*/