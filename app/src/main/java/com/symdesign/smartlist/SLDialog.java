package com.symdesign.smartlist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import static com.symdesign.smartlist.SLAdapter.log;
import static com.symdesign.smartlist.SLAdapter.logF;
import static com.symdesign.smartlist.MainActivity.getTime;
import static com.symdesign.smartlist.MainActivity.sld;
import static com.symdesign.smartlist.MainActivity.changeItem;
import static com.symdesign.smartlist.MainActivity.addItem;
import static com.symdesign.smartlist.MainActivity.deleteItem;
import static com.symdesign.smartlist.MainActivity.day;

import static com.symdesign.smartlist.SLAdapter.updateAdapters;

/**
 * Created by dennis on 11/27/15.
 * The dialog that appears when "+" is tapped and when item is touched to edit
 */
public class SLDialog extends DialogFragment implements AdapterView.OnItemSelectedListener{
//    final long day = 86400;
    static Spinner frequency;
    static Button pickButton;
    static long freq;
    static String title="Create new item";
    static CharSequence name;
    static boolean edit,list;
    static long id;
    EditText nameView;

    public SLDialog() {

    }
    public static SLDialog newInstance(int title) {
        SLDialog frag = new SLDialog();
        Bundle args = new Bundle();
        args.putInt("title",title);
        frag.setArguments(args);
        return frag;
    }
    @Override
    public Dialog onCreateDialog(Bundle state) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.sl_dialog,null);
        builder.setView(v);
        nameView = (EditText) v.findViewById(R.id.name);
        if(nameView.requestFocus()) {
            InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imgr.showSoftInput(nameView, InputMethodManager.SHOW_IMPLICIT);
        }
        ArrayAdapter<CharSequence> freq_adapter = ArrayAdapter.createFromResource(MainActivity.context,
                R.array.frequencies,android.R.layout.simple_spinner_dropdown_item);
        frequency = (Spinner) v.findViewById(R.id.freq);
        frequency.setAdapter(freq_adapter);
        if(edit)
            nameView.setText(name);
        frequency.setOnItemSelectedListener(this);
        pickButton = (Button) v.findViewById(R.id.pick_button);
        pickButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent("com.symdesign.smartlist.intent.action.PickList");
                MainActivity.context.startActivity(intent);
            }
        });
        nameView.addTextChangedListener(new TextWatcher(){      // Set up text listener
            CharSequence text;
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
/*                    text = s;
                    log("beforeTC " + s.toString() + " "
                    + s.subSequence(start, start + count).toString());
*/          }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                logF("onTC: %s\t%s\t%d\t%d\t%d", s,
//                        s.subSequence(start, start + count).toString(),start,before,count);
                if(count!=0 && s.charAt(start)=='\n'){
                    Long date = getTime();
                    name = nameView.getText().toString();
                    // Remove return
                    int ccnt = 0;
                    while(name.charAt(ccnt)!='\n'){
                        ccnt++;
                    }
                    CharSequence str=name.subSequence(0,ccnt);
                    // A negative value of inList indicates a new item
                    int inList = list ? 1 : 0;
                    if(!edit)
                        addItem((String) str,inList, date, freq, 0);
                    else
                        changeItem((String) str,-inList,date,freq,0,SLDialog.id);
                    MainActivity.logF("current time = %l",date);
                    updateAdapters();
                    sld.dismiss();
                }
            }
            public void afterTextChanged(Editable s) {
//                    log("afterTC " + s.toString());
            }
        });
        builder.setPositiveButton(R.string.done,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        long date = getTime();
                        name = nameView.getText().toString();
                        if(!edit)
                            addItem((String) name,1,date,freq,0);
                        else
                            if(SLDialog.list)
                                changeItem((String) name,1,date,freq,0,SLDialog.id);
                            else
                                changeItem((String) name,0,date,freq,0,SLDialog.id);
                        updateAdapters();
                        logF("Got name: %s\n",name.toString());
                        sld.dismiss();
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        updateAdapters();
                        sld.dismiss();
                        log("Cancel pressed");
                    }
                });
        if(edit)
            builder.setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteItem(SLDialog.id);
                        updateAdapters();
                        log("Delete pressed");
                        sld.dismiss();
                    }
                 });
        return builder.create();

    }
    @Override public void onDismiss(DialogInterface dialog) {
        log("dismissed");
        updateAdapters();
        dialog.dismiss();
    }
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
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


    public void onClick(DialogInterface dialog,int which) {
        logF("Selected %d",which);
    }

    static SLDialog newInstance() {
        return new SLDialog();
    }
}
