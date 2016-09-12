package com.symdesign.smartlist;

import android.database.Cursor;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.logF;
import static com.symdesign.smartlist.MainActivity.logF;
import static com.symdesign.smartlist.SLAdapter.cols;
import static com.symdesign.smartlist.SLAdapter.updateAdapters;
import static com.symdesign.smartlist.MainActivity.listValues;

/**
 * Created by dennis on 2/18/16.
 */
public class DatabaseSync extends AsyncTask<Void,Void,String>  {

    HttpURLConnection dB;
    String link = "http://symdesigns.ddns.net/cgi-bin/smartlist.php";
    char[] buf = new char[140];
    Cursor items;

    protected String doInBackground(Void... arg0) {
        xferDb();
        return "Done";
    }

    public boolean xferDb() {
        try {       // Send post request
            URL url = new URL(link);
            dB = (HttpURLConnection) url.openConnection();
            dB.setRequestMethod("POST");
            dB.setDoInput(true);
            dB.setDoOutput(true);
            items = db.query("itemDb",cols,"inList=0 OR inList=1 OR inList=-1",null,"","","ratio DESC");
            BufferedOutputStream bos = new BufferedOutputStream(dB.getOutputStream());
            log("Sending data to server");
            String cntstr = String.format("%d\n",items.getCount());
            bos.write(cntstr.getBytes("UTF-8"),0,cntstr.length());  //write to server
            for(items.moveToFirst();!items.isAfterLast(); items.moveToNext()) {
                String str = String.format("%s,%d,%d,%d,%f\n",items.getString(1),
                        items.getInt(2),items.getInt(3),items.getInt(4),
                        items.getFloat(5));
//                str=str.replaceAll("\'","");
                log(str+"\n");
                bos.write(str.getBytes("UTF-8"),0,str.length());    //write to server
            }
            bos.flush();
            bos.close();
                    // Receive Post reply
            InputStream is = dB.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String col;
//            SLAdapter.prtSuggestions();
            log("Receiving data from server");
            while(null != (col = reader.readLine())){
                col=col.replaceAll("\n","");
                String[] cols = col.split(",");
                switch(cols[0]) {
                    case "u":   //Time changed on server, update item
                        logF("%s", col);
                        listValues.clear();
                        listValues.put("name", cols[1]);
                        listValues.put("inList", cols[2]);
                        listValues.put("last_time", cols[3]);
                        listValues.put("last_avg", cols[4]);
                        listValues.put("ratio", cols[5]);
                        log("name='" + cols[1] + "'");
                        long id = db.update("itemDb", listValues, "name='" + cols[1] + "'", null);
                        break;
                    case "i":   //Item added to server, insert into database
                        logF("%s",col);
                        listValues.clear();
                        listValues.put("name", cols[1]);
                        listValues.put("inList", cols[2]);
                        listValues.put("last_time", cols[3]);
                        listValues.put("last_avg", cols[4]);
                        listValues.put("ratio", cols[5]);
                        db.insert("itemDb",null,listValues);
                        break;
    //                logF("cols changed = %d",id);
//                log(col);
//                String[] st = text.split(",");
                }
            }
            is.close();
            dB.disconnect();
        } catch (MalformedURLException e) {
            MainActivity.log("Malformed URL: "+e.toString());
            return false;
        } catch (IOException e) {
            MainActivity.log("IOException: "+e.toString());
            return false;
        }
        return true;
    }
    protected void onProgressUpdate(Integer... progress) {

    }
    protected void onPostExecute(String result) {
        log("post execute");
        updateAdapters();
        MainActivity.listView.invalidate();
        MainActivity.suggestView.invalidate();
    }
}

