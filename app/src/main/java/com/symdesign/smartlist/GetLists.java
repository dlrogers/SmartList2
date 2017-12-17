package com.symdesign.smartlist;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.Gravity;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import static com.symdesign.smartlist.MainActivity.email;
import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.logF;
import static com.symdesign.smartlist.MainActivity.currList;
import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.MainActivity.passwd;


/**
 * @ Copyright Dennis Rogers 8/9/17
 * Created by dennis on 8/9/17.
 * Receives and email and password from a phone and 
 * sends all lists corresponding to that email.
 * returns: "ok" if email and password are ok followed by lists data
 *          "nok" if account does not exist or password is not valid
 */

class GetLists extends AsyncTask<Void,Void,Boolean> {
    private String email,passwd,list;
    private ListView listView,suggestView;
    private MainActivity activity;
    //	BufferedOutputStream bos;
    private static ContentValues values = new ContentValues();
    private static String col;
    //	HttpURLConnection link;
    private URL url;
    Context context;
    String nemail,npasswd;

    final String table_row = "(_id INTEGER PRIMARY KEY, name TEXT, flags INT, last_time INT, last_avg INT, ratio REAL)";

    GetLists (MainActivity a, String em, String pwd) {
        this.activity=a;
        context = a;
        nemail = em;
        npasswd = pwd;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        InputStream is;
        OutputStream os;
        String[] rows;
        String line;

        try {       // Send post request
            log("starting GetLists");
//            long lastTime = System.currentTimeMillis();
            url = new URL(MainActivity.serverAddr+"export.php");
            HttpURLConnection link = (HttpURLConnection) url.openConnection();
            link.setRequestMethod("POST");
            link.setDoInput(true);
            link.setDoOutput(true);
            // Get output stream and send email and password
            os = link.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);
            bos.write((nemail + "\n").getBytes());
            bos.write((npasswd + "\n").getBytes("UTF-8"));
            bos.flush();
            //  Get Input stream
            is = link.getInputStream();
            // Access Database
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            ItemDb itemDb = new ItemDb(context);
            db = itemDb.getWritableDatabase();
            String ans = reader.readLine();
            // Error message if email not ok or password wrong
            if ((!ans.equals("ok")))
                showToast(context,"\nAccount not found!\n",Toast.LENGTH_LONG);
            else {
                email=nemail;
                passwd=npasswd;
                // update Shared Preferences
                SharedPreferences.Editor ed = MainActivity.prefs.edit();
                ed.putString("email",nemail);
                ed.putString("passwd",npasswd);
                ed.putString("currList","Groceries");
                ed.putBoolean("syncReg",false);
                ed.apply();
                // Remove existing lists except Groceries
                Cursor listsCursor = db.query("lists", new String[] {"_id","name"}, null, null, null, null, null);
                for (listsCursor.moveToFirst(); !listsCursor.isAfterLast(); listsCursor.moveToNext()) {
                    String listName = listsCursor.getString(1);
                    logF("list = %s", listName);
                    db.execSQL("DROP TABLE IF EXISTS '"+listName+"'");
                    db.execSQL("DELETE FROM lists where name='"+listName+"'");
                }
                db.execSQL(MainActivity.SQL_CREATE_GROCERIES);
//                values.put("name","Groceries");
//                db.insert("lists",null,values);
//                listsCursor = db.query("lists", new String[] {"_id","name"}, null, null, null, null, null);
                log("List of lists entries");
                for (listsCursor.moveToFirst(); !listsCursor.isAfterLast(); listsCursor.moveToNext()) {
                    logF("lists item = %s",listsCursor.getString(1));
                }
//                db.delete("lists","name != Groceries",null);
                line = reader.readLine();
                do {
                    // Read list name and insert in lists
                    String listName = line;
                    values.clear();
                    values.put("name",listName);
                    db.insert("lists",null,values);
                    String sql = "CREATE TABLE IF NOT EXISTS "+listName+table_row;  //  Add list table
                    logF("sql = %s",sql);
                    db.execSQL(sql);
                    line = reader.readLine();     // Read first list item
                    while(line.contains(",")){
                        rows = line.split(",");     // Split out columns
                        //  Insert into table
                        values.clear();
                        values.put("name", rows[0]);
                        values.put("flags", rows[1]);
                        values.put("last_time", rows[2]);
                        values.put("last_avg", rows[3]);
                        values.put("ratio", rows[4]);
    //                            log("name='"+rows[1]+"'");
                        db.insert(listName,null,values);
                        line = reader.readLine();
                    }
                } while(!line.equals("end"));
            }
            os.close();
            bos.close();
            is.close();
            link.disconnect();
        } catch (MalformedURLException e) {
            log("Malformed URL: " + e.toString());
        } catch (IOException e) {
            log("IOException: " + e.getMessage());
            for(int i=0; i<4; i++) {
                log(e.getStackTrace()[i].toString());
                log(String.format(Locale.getDefault(),"    line no. = %d", e.getStackTrace()[i].getLineNumber()));
            }
        } finally {
            log("Disconnecting GeLists");
        }
        SystemClock.sleep(2000)  ;
        return true;
    }
    @Override
    protected void onPostExecute(Boolean exists) {
        MainActivity.updateAdapters();
        showToast(context,"\nSync Done\n",Toast.LENGTH_LONG);
    }
    static void showToast(Context ctx,String msg,int len){
        Toast t = Toast.makeText(ctx,msg,len);
        t.setGravity(Gravity.TOP, 0, 200);
        t.show();
    }
}
