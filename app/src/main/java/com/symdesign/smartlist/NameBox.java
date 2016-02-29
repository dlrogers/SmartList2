package com.symdesign.smartlist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.EditText;

/**
 * Created by dennis on 11/22/15.
 */
public class NameBox extends EditText {

    public NameBox(Context context,AttributeSet attrs) {
        super(context, attrs);

    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        MainActivity.clickLocation = MainActivity.ClickLocation.name;
        return false;
    }
}
