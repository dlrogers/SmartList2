package com.symdesign.smartlist;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.BufferedOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.logF;
import static com.symdesign.smartlist.SLAdapter.itemsList;
import static com.symdesign.smartlist.SLAdapter.itemsSuggest;
import static com.symdesign.smartlist.SLAdapter.updateAdapters;
import static com.symdesign.smartlist.MainActivity.values;
import static com.symdesign.smartlist.MainActivity.email;
import static com.symdesign.smartlist.MainActivity.passwd;
import static com.symdesign.smartlist.MainActivity.currList;

/**
 * Created by dennis on 2/18/16.
 * Class DatabaseSync is a AsyncTask that syncs phone database with
 * MYSQL database on the Shopping Mate server
 */
class DatabaseSync extends AsyncTask<Void,Void,String>  {

    protected String doInBackground(Void... arg0) {
        xferDb();
        return "Done";
    }

    private boolean xferDb() {
        Item item;

        try {       // Send post request
            URL url = new URL(MainActivity.serverAddr+"sm_test.php");
//            URL url = new URL("http://209.95.50.135/cgi-bin/admin.php");
            HttpURLConnection link = (HttpURLConnection) url.openConnection();
            link.setRequestMethod("POST");
            link.setDoInput(true);
            link.setDoOutput(true);
            BufferedOutputStream bos = new BufferedOutputStream(link.getOutputStream());
            log("Sending data to server");
            bos.write((email+"\n").getBytes("UTF-8"));
            bos.write((passwd+"\n").getBytes("UTF-8"));
            bos.write((currList+"\n").getBytes("UTF-8"));

            int cnt = itemsList.size();
            for(int i=0; i<cnt; i++){
                item = itemsList.get(i);
                String str = String.format(Locale.getDefault(),"%s,%d,%d,%d,%f\n",
                        item.name,item.flags,item.last_time,item.last_avg,item.ratio);
                bos.write(str.getBytes("UTF-8"));
				if((item.flags&2)>0){
                    db.delete("'"+currList+"'","name='"+item.name+"'",null);
                    itemsList.remove(i);
				}
                log(str+"\n");
            }
            cnt = itemsSuggest.size();
            for(int i=0; i<cnt; i++){
                item = itemsSuggest.get(i);
                String str = String.format(Locale.getDefault(),"%s,%d,%d,%d,%f\n",
                        item.name,item.flags,item.last_time,item.last_avg,item.ratio);
                bos.write(str.getBytes("UTF-8"));
				if((item.flags&2)>0){
					db.delete("'"+currList+"'","name='"+item.name+"'",null);
					itemsList.remove(i);
				}
                log(str+"\n");
            }
            bos.flush();
            bos.close();
                    // Receive Post reply
            InputStream is = link.getInputStream();
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
                        values.clear();
                        values.put("name", cols[1]);
                        values.put("flags", cols[2]);
                        values.put("last_time", cols[3]);
                        values.put("last_avg", cols[4]);
                        values.put("ratio", cols[5]);
                        log("name='" + cols[1] + "'");
                        db.update(MainActivity.currList, values, "name='" + cols[1] + "'", null);
                        break;
                    case "i":   //Item added to server, insert into database
                        logF("%s",col);
                        values.clear();
                        values.put("name", cols[1]);
                        values.put("flags", cols[2]);
                        values.put("last_time", cols[3]);
                        values.put("last_avg", cols[4]);
                        values.put("ratio", cols[5]);
                        db.insert(MainActivity.currList,null,values);
                        break;    // logF("cols changed = %d",id);
//                log(col);
//                String[] st = text.split(",");
                }
            }
            is.close();
            link.disconnect();
        } catch (MalformedURLException e) {
            MainActivity.log("Malformed URL: "+e.toString());
            return false;
        } catch (IOException e) {
            MainActivity.log("IOException: "+e.toString());
            return false;
        }
        return true;
    }
/*    protected void onProgressUpdate(Integer... progress) {

    }
   */
    protected void onPostExecute(String result) {
        log("post execute");
        updateAdapters();
        MainActivity.toast.cancel();
        MainActivity.listView.invalidate();
        MainActivity.suggestView.invalidate();
    }
}

