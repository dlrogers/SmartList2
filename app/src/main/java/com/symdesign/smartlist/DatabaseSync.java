package com.symdesign.smartlist;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dennis on 2/18/16.
 */
public class DatabaseSync extends AsyncTask<Void,Void,String> {

    HttpURLConnection conn;
    String link = "http://sym-designs.com/cgi-bin/test.php";
    char[] buf = new char[140];
    protected String doInBackground(Void... arg0) {
        try {
            URL url = new URL(link);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os,"UTF-8"));
            writer.write("&name=Dennis");
            writer.flush();
            writer.close();
            os.close();

            int status = conn.getResponseCode();
//            MainActivity.logF("status = %d", status);
//            MainActivity.log("Input Stream opened");
//            MainActivity.log(conn.getResponseMessage());
            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String text;
            while(null != (text = reader.readLine())){
                MainActivity.log(text);
            }
        } catch (MalformedURLException e) {
            MainActivity.log("Malformed URL: "+e.toString());
        } catch (IOException e) {
            MainActivity.log("IOException: "+e.toString());
        }
        return "Done";
    }
    protected void onProgressUpdate(Integer... progress) {

    }
    protected void onPostExecute(Long result) {

    }
}

