package com.symdesign.smartlist;

import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;

import static android.support.v4.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER;
import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.OptionDialog.hashName;
//import static com.symdesign.smartlist.MainActivity.listTable;

/**
 * Created by dennis on 10/17/16.
 */

public class LongClickDialog extends DialogFragment {
    EditText nameView;
    Button deleteView;
    String listName;
    ContentValues values = new ContentValues();
    int listId;

    public LongClickDialog() {
        // Empty contstuctor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final Context context;
        context = getActivity();
        View optionView = inflater.inflate(R.layout.long_click, container, false);
        nameView = (EditText) optionView.findViewById(R.id.list_name);
        listName = this.getArguments().getString("name");
        listId = this.getArguments().getInt("_id");
        nameView.setText(listName);
        deleteView = (Button) optionView.findViewById(R.id.delete_button);
        ImageView checkView = (ImageView) optionView.findViewById(R.id.check);
        values.clear();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        );
        checkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                values.clear();
                listName = nameView.getText().toString();
                values.put("name",listName);
                db.update("lists",values,"_id="+listId,null);
                OptionDialog.showLists(context);
                MainActivity.printLists();
                getDialog().dismiss();
            }
        });
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.delete("lists","name='"+listName+"'",null);
                db.execSQL("drop table '"+listName+"'");
                MainActivity.currList = "'Groceries'";
                OptionDialog.showLists(context);
                MainActivity.printLists();
                getDialog().dismiss();
            }
        });
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return optionView;
    }
}