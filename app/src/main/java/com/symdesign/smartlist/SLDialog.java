package com.symdesign.smartlist;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import static com.symdesign.smartlist.SLAdapter.updateAdapters;

/**
 * Created by dennis on 11/27/15.
 */
public class SLDialog extends DialogFragment implements AdapterView.OnItemSelectedListener {
    final long day = 86400000;
    static Spinner frequency;
    long freq;
    static String title="Create new item";
    static CharSequence name;
    static boolean edit,list;
    static long id;

    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle state) {
        View v = inflater.inflate(R.layout.sl_dialog,container,false);
        getDialog().setTitle(title);
        final Button doneButton = (Button) v.findViewById(R.id.done);
        final EditText nameView = (EditText) v.findViewById(R.id.name);
        final Button deleteButton = (Button) v.findViewById(R.id.delete);
        if(nameView.requestFocus()) {
            InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imgr.showSoftInput(nameView, InputMethodManager.SHOW_IMPLICIT);
        }
        frequency = (Spinner) v.findViewById(R.id.freq);
        ArrayAdapter<CharSequence> freq_adapter = ArrayAdapter.createFromResource(MainActivity.context,
                R.array.frequencies,android.R.layout.simple_spinner_dropdown_item);
        frequency.setAdapter(freq_adapter);
        frequency.setOnItemSelectedListener(this);
        if(edit)
            nameView.setText(name);
        nameView.addTextChangedListener(new TextWatcher(){
            CharSequence text;
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
/*                    text = s;
                MainActivity.log("beforeTC " + s.toString() + " "
                        + s.subSequence(start, start + count).toString());
*/              }
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                MainActivity.logF("onTC: %s\t%s\t%d\t%d\t%d", s,
                        s.subSequence(start, start + count).toString(),start,before,count);
                if(count!=0 && s.charAt(start)=='\n'){
                    Long date = System.currentTimeMillis();
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
                        MainActivity.addItem((String) str,inList, System.currentTimeMillis(), freq, 0);
                    else
                        MainActivity.changeItem((String) str,-inList,System.currentTimeMillis(),freq,0,SLDialog.id);
                    updateAdapters();
                    MainActivity.sld.dismiss();
                }
            }
            public void afterTextChanged(Editable s) {
//                    MainActivity.log("afterTC " + s.toString());
            }
        });
        //      Process Done button
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long date = System.currentTimeMillis();
                name = nameView.getText().toString();
                    // A negative value of inList indicates a new item
                if(!edit)
                    MainActivity.addItem((String) name, -1, System.currentTimeMillis(), freq, 0);
                else
                    MainActivity.changeItem((String) name,1,System.currentTimeMillis(),freq,0,SLDialog.id);
                updateAdapters();
                MainActivity.sld.dismiss();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.deleteItem(SLDialog.id);
                updateAdapters();
                MainActivity.sld.dismiss();
            }
        });

        return v;
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
            case 4: freq = 3*day;
                break;
        }
        MainActivity.logF("selected item %d",pos);
    }
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }


    public void onClick(DialogInterface dialog,int which) {
        MainActivity.logF("Selected %d",which);
    }

    static SLDialog newInstance() {
        return new SLDialog();
    }
}
