package com.symdesign.smartlist;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import static com.symdesign.smartlist.SLAdapter.updateAdapters;

/**
 * Created by dennis on 11/30/15.
 *
 * Button to edit entry
 */
public class EditDialog extends DialogFragment {
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle state) {
        View v = inflater.inflate(R.layout.sl_dialog, container, false);
        getDialog().setTitle("New Item");
        final Button doneAdd = (Button) v.findViewById(R.id.done);
        final EditText nameAdd = (EditText) v.findViewById(R.id.name);

        //      Process Done button
        doneAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Long date = System.currentTimeMillis();
                String nm = nameAdd.getText().toString();
                MainActivity.addItem(nm, 1, date, 7 * MainActivity.day, 0.0);
                updateAdapters();
                MainActivity.sld.dismiss();
            }
        });

        return v;
    }
}
