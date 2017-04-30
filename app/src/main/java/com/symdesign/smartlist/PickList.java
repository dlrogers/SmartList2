package com.symdesign.smartlist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static com.symdesign.smartlist.MainActivity.day;
import static com.symdesign.smartlist.MainActivity.logF;
import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.getTime;
import static com.symdesign.smartlist.MainActivity.listView;
import static com.symdesign.smartlist.MainActivity.suggestView;


/**
 * PickList is Adapter for picking from list of items
 * Created by dennis on 7/1/16.
 */
public class PickList extends Activity implements AdapterView.OnItemSelectedListener {
    Activity thisActivity;
    Context context;
    ExpandableListAdapter expListAdapter;
    ExpandableListView expListView;
    AutoCompleteTextView nameView;
    Button checkView;
    int groupPos,childPos=-1;
    Spinner frequency;
    static long freq;

    static ArrayList<String> catagories;         // Food catagories
    static ArrayList<ArrayList<String>> pickItems = new ArrayList<>(); // Mapping from catagories to lists of food items
    static ArrayList<String> currItems = new ArrayList<>();
    static ArrayList<String> srchItems = new ArrayList<>();
    static SQLiteDatabase db;
    static CharSequence name;
    static int dBid;
    static boolean inLists;
    final String[] cols = {"_id","name","flags","last_time","last_avg","ratio"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisActivity=this;
        context = this;
        setContentView(R.layout.pick_list);
        Bundle extras = getIntent().getExtras();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.dropdown_layout,srchItems);
        nameView = (AutoCompleteTextView)
                findViewById(R.id.name);
        nameView.setAdapter(adapter);
        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        nameView.requestFocus();
        imm.showSoftInput(nameView, InputMethodManager.SHOW_IMPLICIT);
        if(extras != null) {
            name = extras.getString("name");
            inLists = extras.getBoolean("inLists");
            nameView.setText(name);
            dBid=extras.getInt("id");
        }
        checkView = (Button) findViewById(R.id.pick_button);
        freq = -1;

        ArrayAdapter<CharSequence> freq_adapter = ArrayAdapter.createFromResource(context,
                R.array.frequencies,R.layout.dropdown_layout);
        frequency = (Spinner) findViewById(R.id.freq);
        frequency.setAdapter(freq_adapter);
//        frequency.setPopupBackgroundResource(R.drawable.dialog_bk);
        frequency.setOnItemSelectedListener(this);
        db = MainActivity.itemDb.getWritableDatabase();
        if(inLists) {
            Item item = getDbItem(name.toString());
            if(item.last_avg < 433600)
                frequency.setSelection(0);
            else if(item.last_avg <907200)
                frequency.setSelection(1);
            else if(item.last_avg < 1900800)
                frequency.setSelection(2);
            else if(item.last_avg < 3888000)
                frequency.setSelection(3);
            else if(item.last_avg == 36500*day)
                frequency.setSelection(4);
        }
        expListView = (ExpandableListView) findViewById(R.id.lvExp);
        expListAdapter = new ExpandableListAdapter(this, catagories, pickItems);
        // setting list adapter
        expListView.setAdapter(expListAdapter);
        nameView.addTextChangedListener(new TextWatcher(){      // Set up text listener
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
/*                Toast.makeText(getApplicationContext(),
                "Group Clicked " + catagories.get(groupPosition),
                Toast.LENGTH_SHORT).show();
*/
                return false;   // continue click processing
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
/*                Toast.makeText(getApplicationContext(),
                        catagories.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();
*/            }
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
                String name = pickItems.get(groupPosition).get(childPosition).substring(1);
                addItem(name,1,MainActivity.getTime(),(long) 3.5*day,0.0);
                Toast toast = Toast.makeText(getApplicationContext(),
                        "\n"+name+" Added\n",Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 200);
                toast.show();
//                MainActivity.updateAdapters(context,listView,suggestView);
//                nameView.setText(pickItems.get(groupPosition).get(childPosition).substring(1));
                return false;
            }
        });

        // Select listener
        checkView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Item item;
//                String nm = (nameView.getText().toString()).replaceAll(" ","");
                String nm = (nameView.getText().toString()).trim();
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
//                return;
            }
        });
    }
/*    public void showToast(String text){
        Toast.makeText(getApplicationContext(),
                text,Toast.LENGTH_SHORT).show();

    }
*/  public boolean inDb(String name) {
        Cursor curs = db.query("'"+MainActivity.currList+"'",cols,
                "name='"+name+"\'",null,"","","name ASC");
        boolean hasCount = curs.getCount()!=0;
        curs.close();
        return hasCount;
    }
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
//        logF("item = %s",parent.getItemAtPosition(pos));
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
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivity(intent);
    }
    public Item getDbItem(String name) {
        Cursor curs =db.query("'"+MainActivity.currList+"'",cols,
                "name=\'"+name+"\'",null,"","","name ASC");
        curs.moveToFirst();
        Item c = new Item(curs.getLong(0), curs.getString(1), curs.getInt(2), curs.getLong(3), curs.getLong(4), curs.getFloat(5));
        boolean hasCount = curs.getCount()>0;
        curs.close();
        if(hasCount)
            return c;
        else
            return null;
    }

    public void addItem(Item item) {
        addItem(item.name,(int) item.flags,item.last_time,item.last_avg,item.ratio);
    }
    public void changeItem(Item item) {
        changeItem(item.name,(int) item.flags,item.last_time,item.last_avg,item.ratio,item.id);
    }
    public static void changeItem(String nm,int il,long lt,long la,double r,long id){
        ContentValues listValues = new ContentValues();
        listValues.clear();
        listValues.put("name",nm);
        listValues.put("flags",il);
        listValues.put("last_time", Math.abs(lt));
        listValues.put("last_avg", la);
        listValues.put("ratio", r);
        db.update("'"+MainActivity.currList+"'", listValues, "_id=" + Long.toString(id), null);
    }
    public static void addItem(String nm,int il,long lt,long la,double r) {
        ContentValues listValues = new ContentValues();
        listValues.clear();
        listValues.put("name",nm);
        listValues.put("flags", il);
        listValues.put("last_time", lt);
        listValues.put("last_avg", la);
        listValues.put("ratio", r);
        db.insert("'"+MainActivity.currList+"'", null, listValues);
    }
    public static void newItem(String nm){
        addItem(nm, 1, MainActivity.getTime(), 3 * MainActivity.day, 0);
    }
    /*
     * Preparing the list data
     *   Read data from "Master_Grocery_List.txt"
      *  into pickItems ArrayList
     */
    public static void prepareListData() {

        BufferedReader rdr;
        InputStream input;
        String currCat;
        catagories = new ArrayList<>();

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
//                        first = false;
                    }
                    first = false;
                    if((currCat = rdr.readLine())==null) break;
                    currCat = ((char) chr)+currCat;
                    catagories.add(currCat);
                    currItems = new ArrayList<>();
//                    logF("Catagory = %s",currCat);

                }
                else {                      // is item
                    String itm =rdr.readLine();
                    currItems.add(itm);
                    srchItems.add(itm);
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
