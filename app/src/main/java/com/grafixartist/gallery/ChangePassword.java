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
        setContentView(R.layout.password_confim);

        Button cancelButton = (Button)findViewById(R.id.pwdCancel);
        Button okButton = (Button)findViewById(R.id.pwdOk);

        cancelButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

        okButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        EditText pwd = (EditText) findViewById(R.id.old_pwd);
                        ChangePassword.this.dh = new DatabaseHelper(ChangePassword.this);
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

                        EditText newP1 = (EditText) findViewById(R.id.new_pwd1);
                        EditText newP2 = (EditText) findViewById(R.id.new_pwd2);

                        if (!(pwd.getText().toString().equals(ChangePassword.this.dh.selectFirst(settings.getString(OPT_EMAIL, ""), pwd.getText().toString())))) {
                            Toast.makeText(ChangePassword.this, "Current Password Incorrect", Toast.LENGTH_SHORT).show();
                        } else if (!(newP1.getText().toString().equals(newP2.getText().toString()))) {
                            Toast.makeText(ChangePassword.this, "Passwords Do Not Match", Toast.LENGTH_SHORT).show();
                        } else {

                            ChangePassword.this.dh.updatePassword(settings.getString(OPT_EMAIL, ""), newP1.getText().toString());
                            Toast.makeText(ChangePassword.this, "Password Updated", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                }
        );
    }
}
