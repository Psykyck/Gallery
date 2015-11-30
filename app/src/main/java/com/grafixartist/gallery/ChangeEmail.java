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
        // Set view for the change email screen
        setContentView(R.layout.email_confirm);

        Button cancelButton = (Button)findViewById(R.id.emlCancel);
        Button okButton = (Button)findViewById(R.id.emlOk);

        // Set on click listener for cancel and okay button
        cancelButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // If pressed, exit activity
                        finish();
                    }
                });

        okButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // First get password entered from view
                        String pwd = ((EditText)findViewById(R.id.cureml_pwd)).getText().toString();
                        // Make instance of DB
                        dh = new DatabaseHelper(ChangeEmail.this);
                        // Find shared preferences file
                        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

                        // Get email and confirmation
                        EditText newE1 = (EditText)findViewById(R.id.new_email1);
                        EditText newE2 = (EditText)findViewById(R.id.new_email2);
                        String newEmail1 = newE1.getText().toString();
                        String newEmail2 = newE2.getText().toString();

                        // Check if current password is given
                        if(pwd.equals("")) {
                            Toast.makeText(getApplicationContext(), "Enter the current password for this account", Toast.LENGTH_LONG).show();
                        }
                        // Check if either emails given are invalid
                        else if(!isValidEmail(newEmail1) || !isValidEmail(newEmail2)) {
                            Toast.makeText(getApplicationContext(), "Email not in valid format", Toast.LENGTH_LONG).show();
                        }
                        // Check if password equals the password of the current account
                        else if(!(pwd.equals(dh.selectFirst(settings.getString(OPT_EMAIL, ""), pwd)))) {
                            Toast.makeText(getApplicationContext(), "Current Password Incorrect", Toast.LENGTH_LONG).show();
                        }
                        // Check email is same as confirmation
                        else if (!(newEmail1.equals(newEmail2))) {
                            Toast.makeText(getApplicationContext(), "Emails Do Not Match", Toast.LENGTH_LONG).show();
                        }
                        else {
                            // Update email of current account and reapply email in shared preferences file. Then exit activity
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
