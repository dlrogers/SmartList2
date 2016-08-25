package com.symdesign.smartlist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.symdesign.smartlist.MainActivity.day;
import static com.symdesign.smartlist.MainActivity.logF;
import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.getTime;


/**
 * Created by dennis on 7/1/16.
 */
public class PickList extends Activity implements AdapterView.OnItemSelectedListener {
    Activity thisActivity;
    Context context;
    ExpandableListAdapter expListAdapter;
    ExpandableListView expListView;
    EditText nameView;
    Button checkView;
    int groupPos,childPos=-1;
    static Spinner frequency;
    static long freq;

    static ArrayList<String> catagories;         // Food catagories
    static ArrayList<ArrayList<String>> pickItems = new ArrayList<ArrayList<String>>(); // Mapping from catagories to lists of food items
    static ArrayList<String> currItems = new ArrayList<String>();
    static SQLiteDatabase db;
    static CharSequence name;
    static int id;
    static boolean inLists;
    final String[] cols = {"_id","name","inList","last_time","last_avg","ratio"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisActivity=this;
        context = this;
        setContentView(R.layout.pick_list);
        Bundle extras = getIntent().getExtras();
        nameView = (EditText) findViewById(R.id.name);
        if(extras != null) {
            name = extras.getString("name");
            inLists = extras.getBoolean("inLists");
            nameView.setText(name);
            id=extras.getInt("id");
        }
        checkView = (Button) findViewById(R.id.pick_button);
        freq = -1;

        ArrayAdapter<CharSequence> freq_adapter = ArrayAdapter.createFromResource(MainActivity.context,
                R.array.frequencies,android.R.layout.simple_spinner_dropdown_item);
        frequency = (Spinner) findViewById(R.id.freq);
        frequency.setAdapter(freq_adapter);
        frequency.setOnItemSelectedListener(this);

        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        expListAdapter = new ExpandableListAdapter(this, catagories, pickItems);
        // setting list adapter
        expListView.setAdapter(expListAdapter);
        nameView.addTextChangedListener(new TextWatcher(){      // Set up text listener
            CharSequence text;
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                logF("onTC: %s\t%s\t%d\t%d\t%d", s,
//                        s.subSequence(start, start + count).toString(),start,before,count);
                if(count!=0 && s.charAt(start)=='\n'){
                    newItem(nameView.getText().toString());
                    backToMain();
                }
            }
            public void afterTextChanged(Editable s) {
            }
        });
        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                Toast.makeText(getApplicationContext(),
                "Group Clicked " + catagories.get(groupPosition),
                Toast.LENGTH_SHORT).show();
                return false;   // continue click processing
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
                groupPos = groupPosition;
                childPos = childPosition;
                nameView.setText(pickItems.get(groupPosition).get(childPosition).substring(1));
                return false;
            }
        });

        // Select listener
        checkView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Item item;
                String nm = nameView.getText().toString();
                logF("freq = %d",freq);
                if(inLists) {      // Edit (item selected from list) ?
                    item = getDbItem(name.toString());   // update db entry
                    if(!(nm.equals(name))) {      // if name has been changed ?
                        item.name = nm;
                        if (freq > 0)
                            item.last_avg = freq;
                        changeItem(item);
                    } else if(freq>0) {
                        item.last_avg = freq;
                        changeItem(item);
                    }
                } else {                // new name edited or entered directly
                    item = Item.newItem(nm);
                    item.last_time = getTime();
                    item.last_avg = freq;
                    if(inDb(nm)) {      // if already in dB
                        return;
                    } else {
                        if(freq>0)
                            addItem(item);
                        else
                            addItem(item);
                    }
                }
                backToMain();
                return;
            }
        });
    }
    public void showToast(String text){
        Toast.makeText(getApplicationContext(),
                text,Toast.LENGTH_SHORT).show();

    }
    public boolean inDb(String name) {
        Cursor curs = db.query("itemDb",cols,
                "name='"+name+"\'",null,"","","name ASC");
        if(curs.getCount()!=0)
            return true;
        else
            return false;
    }
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        logF("item = %s",parent.getItemAtPosition(pos));
        switch(pos){
            case 0: freq = (long) (3.5*day);
                break;
            case 1: freq = 7*day;
                break;
            case 2: freq = 14*day;
                break;
            case 3: freq = 30*day;
                break;
            case 4: freq = 36500*day;
                break;
        }
        //    logF("selected item %d",pos);
    }
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void backToMain() {
        Intent intent = new Intent(context,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
    public Item getDbItem(String name) {
        Cursor curs =db.query("itemDb",cols,
                "name=\'"+name+"\'",null,"","","name ASC");
        curs.moveToFirst();
        if(curs.getCount()>0)
            return new Item(curs.getLong(0),curs.getString(1),curs.getInt(2),curs.getLong(3),curs.getLong(4),curs.getFloat(5));
        return null;
    }

    public void addItem(Item item) {
        addItem(item.name,(int) item.inList,item.last_time,item.last_avg,item.ratio);
    }
    public void changeItem(Item item) {
        changeItem(item.name,(int) item.inList,item.last_time,item.last_avg,item.ratio,item.id);
    }
    public static void changeItem(String nm,int il,long lt,long la,double r,long id){
        ContentValues listValues = new ContentValues();
        listValues.clear();
        listValues.put("name",nm);
        listValues.put("inList",il);
        listValues.put("last_time", Math.abs(lt));
        listValues.put("last_avg", la);
        listValues.put("ratio", r);
        db.update("itemDb", listValues, "_id=" + Long.toString(id), null);
    }
    public static void addItem(String nm,int il,long lt,long la,double r) {
        ContentValues listValues = new ContentValues();
        listValues.clear();
        listValues.put("name",nm);
        listValues.put("inList", il);
        listValues.put("last_time", lt);
        listValues.put("last_avg", la);
        listValues.put("ratio", r);
        db.insert("itemDb", null, listValues);
    }
    public static void newItem(String nm){
        addItem(nm, 1, MainActivity.getTime(), 3 * MainActivity.day, 0);
    }
    /*
     * Preparing the list data
     */
    public static void prepareListData() {

        BufferedReader rdr;
        InputStream input;
        String currCat;
        catagories = new ArrayList<String>();

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
                        pickItems.add(currItems);
                        first = false;
                    }
                    first = false;
                    if((currCat = rdr.readLine())==null) break;
                    currCat = ((char) chr)+currCat;
                    catagories.add(currCat);
                    currItems = new ArrayList<String>();
//                    logF("Catagory = %s",currCat);

                }
                else {                      // is item
                    String itm =rdr.readLine();
                    currItems.add(itm);
//                    logF("item = %s",itm);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
   }
    @Override public void onPause() {
        super.onPause();
        log("Pausing PickList");
        db.close();
    }
    @Override public void onResume() {
        super.onResume();
        log("Resuming PickList");
        db = MainActivity.itemDb.getWritableDatabase();
    }

}
