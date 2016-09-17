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
import static com.symdesign.smartlist.SLAdapter.itemsList;
import static com.symdesign.smartlist.SLAdapter.itemsSuggest;
import static com.symdesign.smartlist.SLAdapter.updateAdapters;
import static com.symdesign.smartlist.MainActivity.listValues;
import static com.symdesign.smartlist.MainActivity.changed;

/**
 * Created by dennis on 2/18/16.
 * Class DatabaseSync is a AsyncTask that syncs phone database with
 * MYSQL database on the Shopping Mate server
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
        Item item;

        try {       // Send post request
            URL url = new URL(link);
            dB = (HttpURLConnection) url.openConnection();
            dB.setRequestMethod("POST");
            dB.setDoInput(true);
            dB.setDoOutput(true);
            BufferedOutputStream bos = new BufferedOutputStream(dB.getOutputStream());
            log("Sending data to server");
            bos.write((changed ? "1\n" : "0\n").getBytes("UTF-8"));
            changed=false;
            int cnt = itemsList.size();
            for(int i=0; i<cnt; i++){
                item = itemsList.get(i);
                String str = String.format("%s,%d,%d,%d,%f\n",
                    item.name,item.inList,item.last_avg,item.last_time,item.ratio);
                log(str+"\n");
                bos.write(str.getBytes("UTF-8"));
            }
            cnt = itemsSuggest.size();
            for(int i=0; i<cnt; i++){
                item = itemsSuggest.get(i);
                String str = String.format("%s,%d,%d,%d,%f\n",
                    item.name,item.inList,item.last_avg,item.last_time,item.ratio);
                item.inList &= 1;
                log(str+"\n");
                bos.write(str.getBytes("UTF-8"));
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
        MainActivity.toast.cancel();
        MainActivity.listView.invalidate();
        MainActivity.suggestView.invalidate();
    }
}

