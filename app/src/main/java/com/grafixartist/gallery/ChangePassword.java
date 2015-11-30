package com.grafixartist.gallery;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class ChangePassword extends AppCompatActivity {

    private DatabaseHelper dh;
    private final String PREFS_NAME = "MyPrefsFile";
    private final static String OPT_EMAIL="email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Bring up view for change password
        setContentView(R.layout.password_confim);

        Button cancelButton = (Button)findViewById(R.id.pwdCancel);
        Button okButton = (Button)findViewById(R.id.pwdOk);

        // Set on click listener for cancel and okay button
        cancelButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // exit activity
                        finish();
                    }
                });

        okButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Get current password. Make instance of DB and get shared preferences file
                        String pwd = ((EditText) findViewById(R.id.old_pwd)).getText().toString();
                        dh = new DatabaseHelper(ChangePassword.this);
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

                        // Get new password and confirmation
                        String newP1 = ((EditText) findViewById(R.id.new_pwd1)).getText().toString();
                        String newP2 = ((EditText) findViewById(R.id.new_pwd2)).getText().toString();

                        // Check current password is given
                        if(pwd.equals("")) {
                            Toast.makeText(ChangePassword.this, "Enter the current password for this account", Toast.LENGTH_LONG).show();
                        }
                        // Check if current password is same as password of current account
                        else if (!(pwd.equals(dh.selectFirst(settings.getString(OPT_EMAIL, ""), pwd)))) {
                            Toast.makeText(ChangePassword.this, "Current Password Incorrect", Toast.LENGTH_LONG).show();
                        }
                        // Check if new password is at least 5 characters
                        else if (newP1.length() < 5) {
                            Toast.makeText(getApplicationContext(), "Password must be at least 5 characters", Toast.LENGTH_LONG).show();
                        }
                        // Check password is same as confirmation
                        else if (!newP1.equals(newP2)) {
                            Toast.makeText(ChangePassword.this, "Passwords Do Not Match", Toast.LENGTH_LONG).show();
                        } else {
                            // Update password in database for current account and exit activity
                            dh.updatePassword(settings.getString(OPT_EMAIL, ""), newP1);
                            Toast.makeText(ChangePassword.this, "Password Updated", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }

                }
        );
    }
}
