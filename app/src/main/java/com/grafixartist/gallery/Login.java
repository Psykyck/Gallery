package com.grafixartist.gallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.EditText;

import java.util.List;

public class Login extends FragmentActivity implements View.OnClickListener {
   private DatabaseHelper dh;
   private final String PREFS_NAME = "MyPrefsFile";
   private EditText emailEditableField;
   private EditText passwordEditableField;
   private final static String OPT_EMAIL="email";
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Fetch shared preferences file
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        // Check first time login
        if (!settings.getBoolean("first_time_login", true)) {
            // Bring up the Gallery
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        // Set up fragment and content view
        FragmentManager.enableDebugLogging(true);
        setContentView(R.layout.login);

        // Set up on click listeners
        emailEditableField=(EditText)findViewById(R.id.email_text);
        passwordEditableField=(EditText)findViewById(R.id.password_text);
        View btnLogin=findViewById(R.id.login_button);
        btnLogin.setOnClickListener(this);
        View btnCancel=findViewById(R.id.cancel_button);
        btnCancel.setOnClickListener(this);
        View btnNewUser=findViewById(R.id.new_user_button);
        if (btnNewUser!=null) btnNewUser.setOnClickListener(this);
     }
    
    public void checkLogin() {
        // Get email and password from screen
        String email = this.emailEditableField.getText().toString().trim();
        String password = this.passwordEditableField.getText().toString();
        // Set up database helper
        this.dh = new DatabaseHelper(this);
        // Get returned entries from email and password combination
        List<String> names = this.dh.selectAll(email, password);
        // Check results were given back
        if (names.size() > 0) {
            // Login successful
            // Get preferences file
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

            // Check if first time login value is available
            if (settings.getBoolean("first_time_login", true)) {
                // Update value to be false
                settings.edit().putBoolean("first_time_login", false).apply();
                settings.edit().putString(OPT_EMAIL, email).apply();
            }

            // Bring up the Gallery
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            // Try again?
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Login failed")
                    .setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
        }
    }

    public void onClick(View v) {
		switch (v.getId()) {
            // If login button pressed, check login
            case R.id.login_button:
                checkLogin();
                break;
            // If cancel button pressed, exit activity
            case R.id.cancel_button:
                finish();
                break;
            // If new user button pressed, direct to account class
            case R.id.new_user_button:
                startActivity(new Intent(this, Account.class));
                break;
		}
    }
}
