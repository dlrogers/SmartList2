package com.symdesign.smartlist;

import android.os.Message;
import android.os.Handler;

import static com.symdesign.smartlist.MainActivity.db;


/**
 * Created by dennis on 12/10/15.
 */
public class SLHandler extends Handler {
    static final int MSG_REPEAT=1;

    @Override public void handleMessage(Message msg) {
        switch(msg.what) {
            case MSG_REPEAT:
                Message repeat = Message.obtain(this,MSG_REPEAT);
                sendMessageDelayed(repeat,1000*MainActivity.repeat_time);
//                MainActivity.log("updateRatios:");
                db = MainActivity.itemDb.getWritableDatabase();
                SLAdapter.updateAdapters();
//                MainActivity.log("update repeat");
                break;
        }
    }
}
