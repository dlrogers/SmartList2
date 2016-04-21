package com.symdesign.smartlist;

import android.os.Message;
import android.os.Handler;

import static com.symdesign.smartlist.SLAdapter.updateAdapters;
import static com.symdesign.smartlist.SLAdapter.updateRatios;

/**
 * Created by dennis on 12/10/15.
 */
public class SLHandler extends Handler {
    static final int MSG_REPEAT=1;
    static final long day = 86400000, hour = 3600000, minute = 60000, second = 1000;

    @Override public void handleMessage(Message msg) {
        switch(msg.what) {
            case MSG_REPEAT:
                Message repeat = Message.obtain(this,MSG_REPEAT);
                sendMessageDelayed(repeat,MainActivity.repeat_time);
//                MainActivity.log("updateRatios:");
                updateAdapters();
//                MainActivity.log("update repeat");
                break;
        }
    }
}
