/**
 * Created by dennis on 10/27/15.
 * Main adapter for each item
 */
package com.symdesign.smartlist;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;

import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.MainActivity.getTime;

/**
 * Adapter class for main display.
 */

class SLAdapter extends ArrayAdapter<Item> {
    static ArrayList<Item> itemsList = new ArrayList<>();
    static ArrayList<Item> itemsSuggest = new ArrayList<>();
    boolean checked = false;
    static String[] cols = {"_id", "name", "flags", "last_time", "last_avg", "ratio"};
    int layout;

    SLAdapter(Context context, ArrayList<Item> items, int box_layout) {
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

    public static void log(String str) {
        Log.v("smartlist", str);
    }

    public static void logF(String fmt, Object... arg) {
        Log.v("smartlist", String.format(fmt, arg));
    }
}
