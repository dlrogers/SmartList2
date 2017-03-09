package com.symdesign.smartlist;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import static android.support.v4.widget.CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER;
import static com.symdesign.smartlist.MainActivity.currList;
import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.MainActivity.email;
import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.passwd;


/**
 * Created by dennis on 10/17/16.
 *
 * Handles short taps on drawere lists
 *
 */

public class OptionDialog extends DialogFragment {
    AutoCompleteTextView nameView;
//    static Activity activity;
    Context context;

    public OptionDialog() {
        // Empty contstuctor required for DialogFragment
    }

    public interface Listener {
        void showLists();
    }

    private Listener listener;

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public static OptionDialog newInstance(int title) {
        OptionDialog frag = new OptionDialog();
//        activity = a;
        Bundle args = new Bundle();
        args.putInt("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View optionView = inflater.inflate(R.layout.options, container, false);
        context = MyVars.get().context;
        ImageView checkView = (ImageView) optionView.findViewById(R.id.check);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, PickList.srchItems);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE
        );
        nameView = (AutoCompleteTextView) optionView.findViewById(R.id.name);
        nameView.setAdapter(adapter);
        nameView.setHint("List Name");
        checkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String listName = nameView.getText().toString();
                currList = listName;
                if(listName.length() > 0) {
                    addToLists(listName);
                    String SQL = "CREATE TABLE '"+currList+"'(" +
                            "_id INTEGER PRIMARY KEY, name TEXT, flags INT, " +
                            "last_time INT, last_avg INT, ratio REAL)";
                    log(SQL);
                    db.execSQL(SQL);
                    listener.showLists();
//                    new Auth((MainActivity) getActivity(),email,passwd,currList,"add");
                }
//                MainActivity.closeDrawer();
//                MainActivity.printLists();
                getDialog().dismiss();
            }
        });
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return optionView;
    }
    static public void addToLists(String name) {	// Add name lists Db
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("tableId", hashName(name));
        db.insert("lists", null, values);

    }
    static public void deleteFromLists(String name) {	// Add name lists Db
        ContentValues values = new ContentValues();
        db.delete("lists","name="+name,null);
    }
	static public String hashName(String name){
		  return name.replace("@", "_").replace(".", "");
	}
}
/*    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.alert_dialog_icon)
                .setTitle(title)
                .setPositiveButton(R.string.option_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog

                        int title = savedInstanceState.getInt("title");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.option_dialog_title)
                .setPositiveButton(R.string.option_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // FIRE ZE MISSILES!
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
*/
