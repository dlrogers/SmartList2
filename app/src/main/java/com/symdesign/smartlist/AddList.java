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
 * Created by dennis on 1/6/17.
 */

public class AddList extends AsyncTask<Void,Void,Boolean> {
    String email,passwd,list;
    static boolean exists;
    MainActivity activity;

    public AddList (MainActivity a, String em, String pwd, String lst) {
        this.activity=a;
        email = em;
        passwd = pwd;
        list = lst;
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
            bos.write(("auth" + "\n").getBytes("UTF-8"));
            bos.write((email + "\n").getBytes("UTF-8"));
            bos.write((passwd + "\n").getBytes("UTF-8"));
            bos.write((list + "\n").getBytes("UTF-8"));
            bos.flush();
            bos.close();
            InputStream is = link.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String ans=reader.readLine();
            MainActivity.log(ans);
            MainActivity.log(email);
            if(ans.equals("exists")) {
                
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
    protected void onPostExecute(Boolean exists)
    {
//        activity.onFinishAddList(AddList.exists);
    }
}
