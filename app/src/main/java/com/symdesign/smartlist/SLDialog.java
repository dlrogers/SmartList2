package com.symdesign.smartlist;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import static com.symdesign.smartlist.SLAdapter.updateAdapters;

/**
 * Created by dennis on 11/27/15.
 */
public class SLDialog extends DialogFragment {
    final long day = 86400000;
    static Spinner frequency;

    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle state) {
        View v = inflater.inflate(R.layout.sl_dialog,container,false);
        getDialog().setTitle("Create new list item");
        final Button doneAdd = (Button) v.findViewById(R.id.done);
        final EditText nameAdd = (EditText) v.findViewById(R.id.name);
        frequency = (Spinner) v.findViewById(R.id.freq);
        ArrayAdapter<CharSequence> freq_adapter = ArrayAdapter.createFromResource(MainActivity.context,
                R.array.frequencies,android.R.layout.simple_spinner_dropdown_item);
        frequency.setAdapter(freq_adapter);

        //      Process Done button
        doneAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long date = System.currentTimeMillis();
                String nm = nameAdd.getText().toString();
                MainActivity.newItem(nm);
                updateAdapters();
                MainActivity.sld.dismiss();
            }
        });

        return v;
    }

    static SLDialog newInstance() {
        return new SLDialog();
    }
}
