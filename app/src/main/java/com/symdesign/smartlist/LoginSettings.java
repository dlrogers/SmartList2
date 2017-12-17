package com.symdesign.smartlist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
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
import android.widget.Toast;
import android.database.sqlite.SQLiteDatabase;

import static com.symdesign.smartlist.MainActivity.email;
import static com.symdesign.smartlist.MainActivity.logF;
import static com.symdesign.smartlist.MainActivity.passwd;
import static com.symdesign.smartlist.MainActivity.db;
import static com.symdesign.smartlist.MainActivity.currList;
import static com.symdesign.smartlist.MainActivity.syncReg;
import static com.symdesign.smartlist.MainActivity.mainActivity;
import static com.symdesign.smartlist.MainActivity.listView;
import static com.symdesign.smartlist.MainActivity.suggestView;
/*
 * Created by dennis on 2/12/17.
 */

public class LoginSettings extends Activity {
    EditText emailView,passwdView;
    TextView resetView;
    ImageView checkView;
    Toast toast;
    Context context;

    public LoginSettings() {
        // Empty contstuctor required for DialogFragment
    }
    public interface SettingsListener {
        void onFinishSettings(String email,String passwd);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = (Context) this;
        setContentView(R.layout.login);
        emailView = (EditText) findViewById(R.id.email);
        emailView.requestFocus();
        emailView.setText(MainActivity.prefs.getString("email","no_email"));
        passwdView = (EditText) findViewById(R.id.passwd);
        resetView = (TextView) findViewById(R.id.reset);
        checkView = (ImageView) findViewById(R.id.check);
        emailView.requestFocus();
        checkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get email and passwd from views
                String nemail = emailView.getText().toString();
                String npasswd = passwdView.getText().toString();
                syncReg = true;
                // Use SyncList to get database from server
                new GetLists(mainActivity,nemail,npasswd).execute();
                finish();
            }
        });
        resetView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toast = Toast.makeText(context,
                        "\nAn Email is being sent to "+email+" containing instructions to reset your password\n",
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 200);
                toast.show();
                new Lostpw(email).execute();
            }
        });
        return ;
    }
}
