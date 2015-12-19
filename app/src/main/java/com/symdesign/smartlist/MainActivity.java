package com.symdesign.smartlist;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
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
    static final long second = 1000, minute = 60*second, hour = 60*minute, day = 24*hour, week = 7*day;
    static final int MSG_REPEAT=1;
    private static final String SQL_CREATE =
            "CREATE TABLE itemDb(_id INTEGER PRIMARY KEY, name TEXT, inList INT, last_time INT, last_avg INT, ratio REAL)";
    static Handler slHandler = new SLHandler();
    final String[] avgCols={"last_time","last_avg","inList"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sld = SLDialog.newInstance();
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
        itemDb = new ItemDb(context);
        db = itemDb.getWritableDatabase();

		db.execSQL("DROP TABLE itemDb");
		db.execSQL(SQL_CREATE);
		initDB(db);

        log("starting smartlist");
        updateAdapters();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id) {
                //               logF("clickLocation = %d, adapter = %s",clickLocation,parent.getAdapter().toString());
                ImageView delView = (ImageView) item.findViewById(R.id.del);
                if(!selected[position]) {
                    delView.setVisibility(View.VISIBLE);
                    setSelected(item,position,true);
                }
                else {
                    delView.setVisibility(View.INVISIBLE);
                    setSelected(item,position,false);
                }
                switch (clickLocation) {
                    case box:
                        clickLocation = ClickLocation.none;
                        logF("avgCols = %s", avgCols);
                        updateAvgs(id,0);
                        break;
                    case name:
                        log("list name clicked");
                        break;
                    case del:
                        listValues.clear();
                        db.delete("itemDb", "_id=" + Long.toString(id), null);
                        updateAdapters();
                        setSelected(item,position,false);
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
                if(!selected[position]){
                    delView.setVisibility(View.VISIBLE);
                    setSelected(item,position,true);
                }
                else {
                    delView.setVisibility(View.INVISIBLE);
                    setSelected(item,position,false);
                }
                switch (clickLocation) {
                    case box:
                        clickLocation = ClickLocation.none;
                        listValues.put("inList",1);
                        db.update("itemDb", listValues, "_id=" + Long.toString(id), null);
                        updateAdapters();
                        break;
                    case name:
                        log("suggest name clicked");
                        break;
                    case del:
                        listValues.clear();
                        db.delete("itemDb","_id=" + Long.toString(id), null);
                        updateAdapters();
                        setSelected(item,position,false);
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
                sld.show(ft, "sldialog tag");
            }
        });
    }
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
        listValues.put("inList", inList);
        listValues.put("last_time",ct);
        db.update("itemDb", listValues, "_id=" + Long.toString(id), null);
        updateAdapters();
        curs.close();
    }

    public void setSelected(View view,int pos, boolean on) {
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

    @Override
    protected void onStop() {
        super.onStop();
        db.close();
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
        listValues.put("inList",il);
        listValues.put("last_time", lt);
        listValues.put("last_avg", la);
        listValues.put("ratio", r);
        db.insert("itemDb", null, listValues);
    }
    public static void newItem(String nm){
        addItem(nm,1,System.currentTimeMillis(),10*minute,0);
    }
    public static void log(String str) {
        Log.v("smartlist", str);
    }
    public static void logF(String fmt,Object... arg){
        Log.v("smartlist",String.format(fmt,arg));
    }
}
