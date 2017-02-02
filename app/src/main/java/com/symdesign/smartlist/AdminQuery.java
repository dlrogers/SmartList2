package com.symdesign.smartlist;

import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dennis on 1/1/17.
 */

public class AdminQuery extends AsyncTask<Void,Void,Boolean> {
    String email,passwd;
    static boolean exists;
    MainActivity activity;

    public AdminQuery(MainActivity act) {
        this.activity=act;
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        try {       // Send post request
            URL url = new URL(MainActivity.serverAddr+"admin.php");
            HttpURLConnection link = (HttpURLConnection) url.openConnection();
            link.setRequestMethod("POST");
            link.setDoInput(true);
            link.setDoOutput(true);
            BufferedOutputStream bos = new BufferedOutputStream(link.getOutputStream());
            bos.write(("query" + "\n").getBytes("UTF-8"));
            bos.write((MainActivity.email + "\n").getBytes("UTF-8"));
            bos.write((MainActivity.passwd + "\n").getBytes("UTF-8"));
            bos.write((MainActivity.currList + "\n").getBytes("UTF-8"));
            bos.flush();
            bos.close();
            InputStream is = link.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String ans=reader.readLine();
            if(ans.equals("found")){
                exists = true;
            } else {
                exists = false;
            }
            is.close();
            link.disconnect();
        } catch (MalformedURLException e) {
            MainActivity.log("Malformed URL: " + e.toString());
        } catch (IOException e) {
            MainActivity.log("IOException: " + e.toString());
        }
        return true;
    }
    @Override
    protected void onPostExecute(Boolean exists) {
        activity.onFinishSyncList(SyncList.exists);
    }
}
