package com.symdesign.smartlist;

import android.os.AsyncTask;
import static com.symdesign.smartlist.MainActivity.email;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import static com.symdesign.smartlist.MainActivity.log;


/**
 * Created by dennis on 4/8/17.
 */

public class Lostpw extends AsyncTask<Void,Void,Boolean> {
    private String email;
    private HttpURLConnection link;
    private URL url;

    public Lostpw(String em){
        email=em;
    }
    @Override
    protected Boolean doInBackground(Void... arg0) {
        OutputStream os;
        try {
            url = new URL(MainActivity.serverAddr + "lostpw.php");
            link = (HttpURLConnection) url.openConnection();
            link.setRequestMethod("POST");
            link.setDoOutput(true);
            os=link.getOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(os);
            bos.write((email + "\n").getBytes("UTF-8"));
            bos.flush();
            os.close();
        } catch (MalformedURLException e) {
            log("Malformed URL: " + e.toString());
        } catch (IOException e) {
            log("IOException: " + e.getMessage());
        }
        return true;
    }
}