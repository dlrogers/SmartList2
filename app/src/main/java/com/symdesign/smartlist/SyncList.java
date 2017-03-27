package com.symdesign.smartlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.widget.ListView;

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

import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.logF;
import static com.symdesign.smartlist.MainActivity.currList;
import static com.symdesign.smartlist.MainActivity.db;


/**
 * @ Copyright Dennis Rogers 1/23/17
 * Created by dennis on 1/23/17.
 * Syncs a list on the phone with the server
 * returns: "ok" if password was ok and sync was successful
 *          "nok" if account does not exist or password is not valid
 */

class SyncList extends AsyncTask<Void,Void,Boolean> {
    private String email,passwd,list;
    private ListView listView,suggestView;
    private MainActivity activity;
//	BufferedOutputStream bos;
	private static ContentValues values = new ContentValues();
    private static String col;
//	HttpURLConnection link;
    private URL url;
    Context context;

    SyncList (MainActivity a, String em, String pwd, String lst, ListView lv,ListView sv) {
        this.activity=a;
        context = a;
        listView = lv;
        suggestView = sv;
        email = em;
        passwd = pwd;
        list = lst;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        InputStream is;
        OutputStream os;
        Cursor cursor;


        try {       // Send post request
            log("starting SyncList");
            long lastTime=System.currentTimeMillis();
            url = new URL(MainActivity.serverAddr+"sync.php");
            HttpURLConnection link = (HttpURLConnection) url.openConnection();
            link.setRequestMethod("POST");
            link.setDoInput(true);
            link.setDoOutput(true);

            os = link.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);
            bos.write((email+"\n").getBytes());
            bos.write((passwd+"\n").getBytes("UTF-8"));
            bos.write((list+"\n").getBytes("UTF-8"));
            bos.flush();
            //          Send list items to server
            String str="";
            cursor = db.query("'"+MainActivity.currList+"'", SLAdapter.cols, "flags=1 OR flags=3", null, "", "", null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int flgs = cursor.getInt(2);
                str = String.format(Locale.getDefault(),"%s,%d,%d,%d,%f\n",
                    cursor.getString(1),cursor.getInt(2),cursor.getInt(3),cursor.getInt(4),cursor.getFloat(5));
                bos.write(str.getBytes("UTF-8"));
                if((flgs & 2)>0){
                    db.delete("'"+currList+"'","name='"+cursor.getString(1)+"'",null);
                }
                log(str+"\n");
            }
            cursor = db.query("'"+MainActivity.currList+"'", SLAdapter.cols, "flags=0 OR flags=2", null, "", "", null);
            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                int flgs = cursor.getInt(2);
                if(flgs==0||flgs==2) {
                    str = String.format(Locale.getDefault(),"%s,%d,%d,%d,%f\n",
                            cursor.getString(1),cursor.getInt(2),cursor.getInt(3),cursor.getInt(4),cursor.getFloat(5));
                    bos.write(str.getBytes("UTF-8"));
                }
                if((flgs & 2)>0){
                    db.delete("'"+currList+"'","name='"+cursor.getString(1)+"'",null);
                }
                log(str+"\n");
            }
            cursor.close();
            bos.flush();
            //          Receive items from server that are not on phone
            is = link.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            log("Reading back from phone");
            while(null != (col = reader.readLine())){
                log(col);
                col=col.replaceAll("\n","");
                String[] cols = col.split(",");
                switch(cols[0]) {
                    case "d":
                        log("deleteing");
                        db.delete("'"+currList+"'","name='"+cols[1]+"'",null);
                        break;
                    case "u":   //Time changed on server, update item
//                        int delbit = (Integer.parseInt(cols[2]))&2;
//                        if(delbit<1) {
                            values.clear();
                            values.put("name", cols[1]);
                            values.put("flags", cols[2]);
                            values.put("last_time", cols[3]);
                            values.put("last_avg", cols[4]);
                            values.put("ratio", cols[5]);
                            log("name='"+cols[1]+"'");
                            db.update("'"+currList+"'",values,"name='"+cols[1]+"'",null);
//                        } else {//                           db.delete("'"+currList+"'",null,null);
//                        }
                        break;
                    case "i":   //Item added to server, insert into database
                        values.clear();
                        values.put("name", cols[1]);
                        values.put("flags", cols[2]);
                        values.put("last_time", cols[3]);
                        values.put("last_avg", cols[4]);
                        values.put("ratio", cols[5]);
                        db.insert("'"+currList+"'",null,values);
                        break;    // logF("cols changed = %d",id);
//                log(col);
//                String[] st = text.split(",");
                }
            }
            Cursor rows = db.query("'"+currList+"'",new String[] {"name","flags"},"flags=1",null,null,null,null);
            for(rows.moveToFirst(); !rows.isAfterLast(); rows.moveToNext()) {
                logF("name = %s, flags = %d",rows.getString(0),rows.getInt(1));
            }
            logF("Sync time = %d",System.currentTimeMillis()-lastTime);
            rows.close();
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
            log("Disconnecting");
        }
        SystemClock.sleep(2000)  ;
        return true;
    }
    @Override
    protected void onPostExecute(Boolean exists) {
        MainActivity.updateAdapters(context,listView,suggestView);
        log("SyncList finished");
    }
}


    /*            if(ans.equals("exists")) {
//				bos.flush();
//				bos.close();
				link.disconnect();
				log("Receiving data from server");
				url = new URL(MainActivity.serverAddr+"admin.php");
				link = (HttpURLConnection) url.openConnection();
				link.setRequestMethod("POST");
				link.setDoInput(true);
//				link.setDoOutput(true);
				InputStream is = link.getInputStream();
				BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            } else {
                
            }
*/
//            is.close();
