package com.grafixartist.gallery;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


public class ChangeEmail extends AppCompatActivity{

    private DatabaseHelper dh;
    private final String PREFS_NAME = "MyPrefsFile";
    private final static String OPT_EMAIL="email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.email_confirm);

        Button cancelButton = (Button)findViewById(R.id.emlCancel);
        Button okButton = (Button)findViewById(R.id.emlOk);

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
                        EditText pwd = (EditText)findViewById(R.id.cureml_pwd);
                        dh = new DatabaseHelper(ChangeEmail.this);
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

                        EditText newE1 = (EditText)findViewById(R.id.new_email1);
                        EditText newE2 = (EditText)findViewById(R.id.new_email2);
                        String newEmail1 = newE1.getText().toString();
                        String newEmail2 = newE2.getText().toString();

                        if(!isValidEmail(newEmail1) || !isValidEmail(newEmail2)) {
                            Toast.makeText(getApplicationContext(), "Email not in valid format", Toast.LENGTH_LONG).show();
                        } else if(!(pwd.getText().toString().equals(dh.selectFirst(settings.getString(OPT_EMAIL, ""), pwd.getText().toString())))) {
                            Toast.makeText(getApplicationContext(), "Current Password Incorrect", Toast.LENGTH_LONG).show();
                        } else if (!(newEmail1.equals(newEmail2))) {
                            Toast.makeText(getApplicationContext(), "Emails Do Not Match", Toast.LENGTH_LONG).show();
                        } else {
                            dh.updateEmail(settings.getString(OPT_EMAIL, ""), newEmail1);
                            settings.edit().putString(OPT_EMAIL, newEmail1).apply();
                            Toast.makeText(ChangeEmail.this, "Email Updated to " + settings.getString(OPT_EMAIL, "FAILED"), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                }
        );
    }

    public static boolean isValidEmail(CharSequence target) {
        return target != null && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}
