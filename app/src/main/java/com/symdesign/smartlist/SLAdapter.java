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
import static com.symdesign.smartlist.MainActivity.listView;
import static com.symdesign.smartlist.MainActivity.suggestView;
import static com.symdesign.smartlist.MainActivity.getTime;

/**
 * Adapter class for main display.
 */

class SLAdapter extends ArrayAdapter<Item> {
    static private Cursor listCursor, suggestCursor;
    static ArrayList<Item> itemsList = new ArrayList<>();
    static ArrayList<Item> itemsSuggest = new ArrayList<>();
    public static SLAdapter listAdapter,suggestAdapter;
    boolean checked = false;
    static String[] cols = {"_id", "name", "flags", "last_time", "last_avg", "ratio"};
    int layout;

    public SLAdapter(Context context, ArrayList<Item> items, int box_layout) {
        super(context, 0, items);
        layout = box_layout;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = getItem(position);
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(layout, parent, false);
        TextView slName = (TextView) convertView.findViewById(R.id.name);
        slName.setText(item.name);
//        ChkBox ck = (ChkBox) convertView.findViewById(R.id.list_cbox);
        return convertView;
    }

    static void updateAdapters() {

        // get cursor for shopping list
//        long time_millis = System.currentTimeMillis();
        listCursor = db.query("'"+MainActivity.currList+"'", cols, "flags=1", null, "", "", null);
        itemsList.clear();
        int n = 0;
        for (listCursor.moveToFirst(); !listCursor.isAfterLast(); listCursor.moveToNext()) {
            itemsList.add(new Item(listCursor.getLong(0), listCursor.getString(1),
                    listCursor.getLong(2), listCursor.getLong(3), listCursor.getLong(4),
                    ((float) (getTime() - listCursor.getLong(3))) / ((float) (listCursor.getLong(4)))));
            n++;
        }
        Collections.sort(itemsList);
        listAdapter = new SLAdapter(MainActivity.getContext(), itemsList, R.layout.list_entry);
        listAdapter.checked = false;
        listView.setAdapter(listAdapter);
        // get cursor for suggestion list
        suggestCursor = db.query("'"+MainActivity.currList+"'", cols, "flags=0", null, "", "",
                "ratio DESC");
        itemsSuggest.clear();
        n = 0;
        for (suggestCursor.moveToFirst(); !suggestCursor.isAfterLast(); suggestCursor.moveToNext()) {
            itemsSuggest.add(new Item(suggestCursor.getLong(0), suggestCursor.getString(1),
                    suggestCursor.getLong(2), suggestCursor.getLong(3), suggestCursor.getLong(4),
                    ((float) (getTime() - suggestCursor.getLong(3))) /
                            ((float) (suggestCursor.getLong(4)))));
            n++;
        }
        Collections.sort(itemsSuggest);
        suggestAdapter = new SLAdapter(MainActivity.getContext(), itemsSuggest, R.layout.suggest_entry);
        suggestAdapter.checked = true;
        suggestView.setAdapter(suggestAdapter);
	}

    public static void log(String str) {
        Log.v("smartlist", str);
    }

    public static void logF(String fmt, Object... arg) {
        Log.v("smartlist", String.format(fmt, arg));
    }
}
/*
    public static void prtSuggestions() {
        listCursor = db.query(MainActivity.currList, cols, "flags=1 OR flags=-1", null, "", "", "ratio DESC");
        for (listCursor.moveToFirst(); !listCursor.isAfterLast(); listCursor.moveToNext()) {
            logF("id=%d, nm=%s, lt=%d, la=%d, rat=%f",
                    listCursor.getLong(0), listCursor.getString(1), listCursor.getLong(3), listCursor.getLong(4),
                    listCursor.getFloat(5));
        }
        suggestCursor = db.query(MainActivity.currList, cols, "flags=0", null, "", "", "ratio DESC");
        for (suggestCursor.moveToFirst(); !suggestCursor.isAfterLast(); suggestCursor.moveToNext()) {
            logF("id=%d, nm=%s, lt=%d, la=%d, rat=%f",
                    suggestCursor.getLong(0), suggestCursor.getString(1), suggestCursor.getLong(3), suggestCursor.getLong(4),
                    suggestCursor.getFloat(5));
        }
    }
}
*/


//        prtSuggestions();   // debug
//		setListeners(suggestCursor,suggestAdapter,suggestView);
//		listCursor.close();
/*    static public void prtItems(int n,ArrayList<Item> items) {
        for(int i=0;i<n;i++) {
            Item item = items.get(i);
            logF("id=%d, nm=%s, il=%d, t-lt=%d, la=%d, rat=%f5.11",
                    item.id,item.name,item.flags,(getTime()-item.last_time),item.last_avg,item.ratio);
        }

    }
    static public void prtElapsed(long time) {
        logF("Elapsed time = %d",System.currentTimeMillis()-time);
    }
*/
/**
 *      Update ratio column in database
 */
/*    void updateRatios() {
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
*/
