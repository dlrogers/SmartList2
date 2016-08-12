package com.symdesign.smartlist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
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

import static com.symdesign.smartlist.MainActivity.logF;
import static com.symdesign.smartlist.MainActivity.addItem;
import static com.symdesign.smartlist.MainActivity.changeItem;
import static com.symdesign.smartlist.MainActivity.changeItem;
import static com.symdesign.smartlist.MainActivity.getTime;
import static com.symdesign.smartlist.MainActivity.logF;
import static com.symdesign.smartlist.MainActivity.sld;
import static com.symdesign.smartlist.SLAdapter.updateAdapters;


/**
 * Created by dennis on 7/1/16.
 */
public class PickList extends Activity {
    Activity thisActivity;
    Context context;
    ExpandableListAdapter expListAdapter;
    ExpandableListView expListView;
    EditText nameView;
    Button checkView;
    int groupPos,childPos=-1;
    long freq;
    static Spinner frequency;

    static ArrayList<String> catagories;         // Food catagories
    static ArrayList<ArrayList<String>> items = new ArrayList<ArrayList<String>>(); // Mapping from catagories to lists of food items
    static ArrayList<String> currItems = new ArrayList<String>();
    static SQLiteDatabase db;
    static CharSequence name;
    ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisActivity=this;
        context = this;
        setContentView(R.layout.pick_list);
        Bundle extras = getIntent().getExtras();
        nameView = (EditText) findViewById(R.id.name);
        if(extras != null) {
            String name = extras.getString("name");
            nameView.setText(name);
        }
        checkView = (Button) findViewById(R.id.pick_button);

        ArrayAdapter<CharSequence> freq_adapter = ArrayAdapter.createFromResource(MainActivity.context,
                R.array.frequencies,android.R.layout.simple_spinner_dropdown_item);
        frequency = (Spinner) findViewById(R.id.freq);
        frequency.setAdapter(freq_adapter);

        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        expListAdapter = new ExpandableListAdapter(this, catagories, items);
        // setting list adapter
        expListView.setAdapter(expListAdapter);
        // Select listener
        checkView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                db = MainActivity.itemDb.getWritableDatabase();
                if(childPos!=-1) {
//                    changeItem(items.get(groupPos).get(childPos));
                } else
//                    changeItem(nameView.getText().toString());
                db.close();
                backToMain();
            }
        });
        nameView.addTextChangedListener(new TextWatcher(){      // Set up text listener
            CharSequence text;
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                logF("onTC: %s\t%s\t%d\t%d\t%d", s,
//                        s.subSequence(start, start + count).toString(),start,before,count);
                if(count!=0 && s.charAt(start)=='\n'){
                    db = MainActivity.itemDb.getWritableDatabase();
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
                nameView.setText(items.get(groupPosition).get(childPosition).substring(1));
                return false;
            }
        });
    }
    public void backToMain() {
        Intent intent = new Intent(context,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
                        items.add(currItems);
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
  /*      logF("Printing info\n");

        for(int i=0; i<items.size(); i++){
            logF("catagory %s",catagories.get(i));
            for(int j=0; j<items.get(i).size(); j++)
                logF("\titem = %s",items.get(i).get(j));
        }
 */   }

}
