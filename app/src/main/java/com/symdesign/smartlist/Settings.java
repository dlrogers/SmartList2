package com.symdesign.smartlist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import static com.symdesign.smartlist.MainActivity.email;
import static com.symdesign.smartlist.MainActivity.passwd;
import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.MainActivity.currList;
/*
 * Created by dennis on 2/12/17.
 */

public class Settings extends DialogFragment {
    EditText emailView,passwdView;
    TextView resetView;
    ImageView checkView;

    public Settings() {
        // Empty contstuctor required for DialogFragment
    }
    public interface SettingsListener {
        void onFinishSettings(String email,String passwd);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View optionView = inflater.inflate(R.layout.settings, container, false);
        emailView = (EditText) optionView.findViewById(R.id.email);
        emailView.requestFocus();
		emailView.setText(MainActivity.prefs.getString("email","no_email"));
        passwdView = (EditText) optionView.findViewById(R.id.passwd);
        resetView = (TextView) optionView.findViewById(R.id.reset);
//		passwdView.setText(MainActivity.prefs.getString("passwd","no_passwd"));
        checkView = (ImageView) optionView.findViewById(R.id.check);
        Dialog dialog = getDialog();
//        TextView tv = (TextView) dialog.findViewById(android.R.id.title);
//        tv.setText("Setting");
//        tv.setGravity(Gravity.CENTER_HORIZONTAL);
//        dialog.setTitle(R.string.settings_title);
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        emailView.requestFocus();
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
				db.execSQL("delete from '"+currList+"'");				
                getDialog().dismiss();
            }
        });
        resetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //imitate code in index.php
                // email = emailView.getText().toString();
                StringBuilder email_body = new StringBuilder(1000);
                email_body = email_body.append("Dear SmartList User,\n\n");
                email_body = email_body.append("You have requested a password reset using the PASSWORD RESET button on the SmartList app.\n\n");
                email_body = email_body.append("Click this link to reset your password for the ");
                email_body = email_body.append(resetView.getText().toString());
                email_body = email_body.append(" shopping list.\n\n");
                email_body = email_body.append(MainActivity.serverAddr);
                email_body = email_body.append("reset_password.php?email=");
                email_body = email_body.append(emailView.getText().toString());
                email_body = email_body.append("&list=");
                email_body = email_body.append("&id=");
                // http://localhost/~BenParker/phpsamples/php-forgot-password-recover-code/reset_password.php?name=Ben Parker
                email_body = email_body.append("\n\nRegards,\nAdmin\n");
                Intent intent = new Intent(Intent.ACTION_SENDTO); // it's not ACTION_SEND
                intent.setType("text/plain");
                // intent.setType("message/rfc822");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Forgot Password Recovery");
                intent.putExtra(Intent.EXTRA_TEXT, email_body.toString());
                // Uri.fromParts("mailto",emailView.getText().toString(), null));
                // intent.setData(Uri.parse("mailto:" + "default@recipient.com")); // or just "mailto:" for blank
                intent.setData(Uri.parse("mailto:" + emailView.getText().toString())); // or just "mailto:" for blank
                // intent.setData(Uri.fromParts("mailto",emailView.getText().toString(), null)); // or just "mailto:" for blank
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // this will make such that when user returns to your app, your app is displayed, instead of the email app.
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                   // getActivity().startService(intent);
                }
            }
        });
        return optionView;
    }
//        protected void onPostExecute(String result) {
//            log("post execute admin");
//        }
}
