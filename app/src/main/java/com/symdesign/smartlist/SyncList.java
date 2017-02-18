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

import static com.symdesign.smartlist.MainActivity.log;


/**
 * @ Copyright Dennis Rogers 1/23/17
 * Created by dennis on 1/23/17.
 * Syncs a list on the phone with the server
 * returns: "ok" if password was ok and sync was successful
 *          "nok" if account does not exist or password is not valid
 */

class SyncList extends AsyncTask<Void,Void,Boolean> {
    private String email,passwd,list,ans;
    static boolean exists;
    private MainActivity activity;
//	BufferedOutputStream bos;
	InputStream is;
	private BufferedReader reader;
	static ContentValues values = new ContentValues();
//	HttpURLConnection link;
    private URL url,nurl;

    SyncList (MainActivity a, String em, String pwd, String lst) {
        this.activity=a;
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
		Item item;
        InputStream is;
        OutputStream os;


        try {       // Send post request
            log("starting SyncList");
            url = new URL(MainActivity.serverAddr+"sync.php");
            HttpURLConnection link = (HttpURLConnection) url.openConnection();
            link.setRequestMethod("POST");
            link.setDoInput(true);
            link.setDoOutput(true);

            os = link.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);
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
        log("SyncList finished");
    }
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
