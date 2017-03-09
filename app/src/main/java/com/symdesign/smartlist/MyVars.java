package com.symdesign.smartlist;

import android.app.Application;
import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.widget.ListView;

/**
 * Created by dennis on 3/8/17.
 */

public class MyVars extends Application {
    ListView listView,suggestView,lists;
    public Context context;

    private static MyVars instance;

    public static MyVars get() {
        return instance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }
}
