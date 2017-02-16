package com.symdesign.smartlist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import static com.symdesign.smartlist.MainActivity.email;
import static com.symdesign.smartlist.MainActivity.passwd;

/**
 * Created by dennis on 2/12/17.
 */

public class Settings extends DialogFragment {
    EditText emailView,passwdView;

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
        passwdView = (EditText) optionView.findViewById(R.id.passwd);
        ImageView checkView = (ImageView) optionView.findViewById(R.id.check);
        Dialog dialog = getDialog();
//        TextView tv = (TextView) dialog.findViewById(android.R.id.title);
//        tv.setText("Setting");
//        tv.setGravity(Gravity.CENTER_HORIZONTAL);
//        dialog.setTitle(R.string.settings_title);
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        emailView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
/*                email = emailView.getText().toString();
                passwd = passwdView.getText().toString();
                SharedPreferences.Editor ed = MainActivity.prefs.edit();
                ed.putString("email",email);
                ed.putString("passwd",passwd);
                ed.putBoolean("syncReg",false);
                ed.apply();
                SettingsListener activity = (SettingsListener) getActivity();
                activity.onFinishSettings(email,passwd);
*/                getDialog().dismiss();
            }
        });
        return optionView;
    }
//        protected void onPostExecute(String result) {
//            log("post execute admin");
//        }
}
