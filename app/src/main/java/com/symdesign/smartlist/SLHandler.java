package com.symdesign.smartlist;

import android.app.Activity;
import android.content.Context;
import android.os.Message;
import android.os.Handler;
import android.widget.ListView;

import static com.symdesign.smartlist.MainActivity.db;


/**
 * Created by dennis on 12/10/15.
 */
public class SLHandler extends Handler {
    Context context;
    ListView listView,suggestView;

    static final int MSG_REPEAT=1;

    public SLHandler(Context c,ListView lv, ListView sv) {
        context = c;
        listView = lv;
        suggestView = sv;
    }

    @Override public void handleMessage(Message msg) {
        switch(msg.what) {
            case MSG_REPEAT:
                Message repeat = Message.obtain(this,MSG_REPEAT);
                sendMessageDelayed(repeat,1000*MainActivity.repeat_time);
//                MainActivity.log("updateRatios:");
                db = MainActivity.itemDb.getWritableDatabase();
                MainActivity.updateAdapters(context,listView,suggestView);
//                MainActivity.log("update repeat");
                break;
        }
    }
}