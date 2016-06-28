package com.symdesign.smartlist;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by dennis on 6/28/16.
 */
public class ItemDb extends SQLiteOpenHelper {

    public ItemDb(Context context) {
        super(context,"items.db",null,1);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MainActivity.SQL_CREATE);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS itemDb");
    }
    public void onDownGrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db,oldVersion,newVersion);
    }
}
