package com.symdesign.smartlist;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static com.symdesign.smartlist.MainActivity.email;
import static com.symdesign.smartlist.MainActivity.log;
import static com.symdesign.smartlist.MainActivity.passwd;

/**
 * Created by dennis on 11/20/16.
 */

public class AdminDialog extends DialogFragment {
    EditText emailView,passwdView;

    public AdminDialog() {
        // Empty contstuctor required for DialogFragment
    }
    public interface AdminDialogListener {
        void onFinishAdminDialog(String email,String passwd);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View optionView = inflater.inflate(R.layout.admin, container, false);
        emailView = (EditText) optionView.findViewById(R.id.email);
        passwdView = (EditText) optionView.findViewById(R.id.passwd);
        ImageView checkView = (ImageView) optionView.findViewById(R.id.check);
        getDialog().setTitle("Registration");
        emailView.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        checkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = emailView.getText().toString();
                passwd = passwdView.getText().toString();
                SharedPreferences.Editor ed = MainActivity.prefs.edit();
                ed.putString("email",email);
                ed.putString("passwd",passwd);
                ed.putBoolean("syncReg",false);
                ed.apply();
                AdminDialogListener activity = (AdminDialogListener) getActivity();
                activity.onFinishAdminDialog(email,passwd);
                getDialog().dismiss();
            }
        });
        return optionView;
    }
        protected void onPostExecute(String result) {
            log("post execute admin");
        }
}
