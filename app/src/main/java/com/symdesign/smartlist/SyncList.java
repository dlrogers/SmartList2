package com.symdesign.smartlist;

import android.content.ContentValues;
import android.os.AsyncTask;
import android.os.SystemClock;

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

import static com.symdesign.smartlist.SLAdapter.itemsList;
import static com.symdesign.smartlist.SLAdapter.itemsSuggest;
import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.MainActivity.currList;
import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.logF;


/**
 * Created by dennis on 1/23/17.
 */

public class SyncList extends AsyncTask<Void,Void,Boolean> {
    String email,passwd,list,ans;
    static boolean exists;
    MainActivity activity;
	BufferedOutputStream bos;
	InputStream is;
	BufferedReader reader;
	static ContentValues values = new ContentValues();
	HttpURLConnection link;
    URL url,nurl;

    public SyncList (MainActivity a, String em, String pwd, String lst) {
        this.activity=a;
        email = em;
        passwd = pwd;
        list = lst;
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
		Item item;
        InputStream is = null;
        OutputStream os = null;


        try {       // Send post request
            log("starting auth");
            url = new URL(MainActivity.serverAddr+"auth.php");
            link = (HttpURLConnection) url.openConnection();
            link.setRequestMethod("POST");
            link.setDoInput(true);
            link.setDoOutput(true);

            os = link.getOutputStream();
            bos = new BufferedOutputStream(os);
            bos.write((email + "\n").getBytes("UTF-8"));
            bos.write((passwd + "\n").getBytes("UTF-8"));
            bos.write((list + "\n").getBytes("UTF-8"));
            bos.flush();
            is = link.getInputStream();
            reader = new BufferedReader(new InputStreamReader(is));
            ans=reader.readLine();
			is.close();
            os.close();
            log(ans);
        } catch (MalformedURLException e) {
            log("Malformed URL: " + e.toString());
        } catch (IOException e) {
            log("IOException: " + e.getMessage());
            for(int i=0; i<4; i++) {
                log(e.getStackTrace()[i].toString());
                log(String.format("    line no. = %d", e.getStackTrace()[i].getLineNumber()));
            }
        } finally {
            log("Disconnecting");
            link.disconnect();
        }
        SystemClock.sleep(2000)  ;
        if(ans.equals("ok")){
            try {       // Send post request
                log("starting sync");
                url = new URL(MainActivity.serverAddr+"sync.php");
                link = (HttpURLConnection) url.openConnection();
                link.setRequestMethod("POST");
//                link.setDoInput(true);
                link.setDoOutput(true);
                link.setConnectTimeout(3000);
                log(String.format("timeout = %d",link.getConnectTimeout()));
                bos = new BufferedOutputStream(link.getOutputStream());
                bos.write((email + "\n").getBytes("UTF-8"));
                bos.write((passwd + "\n").getBytes("UTF-8"));
                bos.write((list + "\n").getBytes("UTF-8"));
                bos.flush();
            } catch (MalformedURLException e) {
                log("Malformed URL: " + e.toString());
            } catch (IOException e) {
                log("IOException: " + e.getMessage());
                for(int i=0; i<4; i++) {
                    log(e.getStackTrace()[i].toString());
                    log(String.format("    line no. = %d", e.getStackTrace()[i].getLineNumber()));
                }
            } finally {
                link.disconnect();
            }
        } else {

        }
        return true;
    }
/*            if(ans.equals("exists")) {
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
				bos.close();
				link.disconnect();
            } else {
                
            }
*/
//            is.close();
	public void sync(String email,String list,String passwd){
		  Item item;
		  MainActivity.log("Sending data to server");
		  // Receive Post reply

	}
    @Override
    protected void onPostExecute(Boolean exists) {
        activity.onFinishSyncList(SyncList.exists);
    }
}
