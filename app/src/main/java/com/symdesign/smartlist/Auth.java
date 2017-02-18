package com.symdesign.smartlist;

import android.content.ContentValues;
import android.os.AsyncTask;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.symdesign.smartlist.MainActivity.log;

/**
 * An AdyncTask that sends an email, password and list
 * to the server auth.php in order to set up a new list/account
 * returns: "exists" if it already exists
 *          "ok" if a new account/list was created
 * Created by dennis on 2/2/17.
 */

public class Auth extends AsyncTask<Void,Void,Boolean> {
    String email, passwd, list, ans;
    static boolean exists;
    MainActivity activity;
    BufferedOutputStream bos;
    InputStream is;
    BufferedReader reader;
    static ContentValues values = new ContentValues();
    HttpURLConnection link;
    URL url;

    public Auth(MainActivity a, String em, String pwd, String lst) {
        this.activity = a;
        email = em;
        passwd = pwd;
        list = lst;
    }

    @Override
    protected Boolean doInBackground(Void... arg0) {
        InputStream is;
        OutputStream os;

        try {       // Send post request
            log("starting auth");
            url = new URL(MainActivity.serverAddr+"auth.php");
            link = (HttpURLConnection) url.openConnection();
            link.setRequestMethod("POST");
            link.setDoInput(true);
            link.setDoOutput(true);

            os=link.getOutputStream();
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
            activity.onFinishAuth("error");
        } catch (IOException e) {
            log("IOException: " + e.getMessage());
            for(int i=0; i<4; i++) {
                log(e.getStackTrace()[i].toString());
                log(String.format("    line no. = %d", e.getStackTrace()[i].getLineNumber()));
                activity.onFinishAuth("error");
            }
        } finally {
            log("Disconnecting");
            link.disconnect();
        }
    return true;
    }
    @Override
    protected void onPostExecute(Boolean result){
        log("starting PostExecute");
//        new SyncList(this.activity,email,passwd,"Groceries").execute();
    }
}
