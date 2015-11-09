package com.grafixartist.gallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
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

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if (!settings.getBoolean("first_time_login", true)){
            // Bring up the Gallery
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        FragmentManager.enableDebugLogging(true);
        setContentView(R.layout.login);
        
        emailEditableField=(EditText)findViewById(R.id.email_text);
        passwordEditableField=(EditText)findViewById(R.id.password_text);
        View btnLogin=findViewById(R.id.login_button);
        btnLogin.setOnClickListener(this);
        View btnCancel=findViewById(R.id.cancel_button);
        btnCancel.setOnClickListener(this);
        View btnNewUser=findViewById(R.id.new_user_button);
        if (btnNewUser!=null) btnNewUser.setOnClickListener(this);
        
     }
    
    private void checkLogin() {
        String email = this.emailEditableField.getText().toString();
        String password = this.passwordEditableField.getText().toString();
        this.dh = new DatabaseHelper(this);
        List<String> names = this.dh.selectAll(email, password);
        if (names.size() > 0) { // Login successful
            // Save username as the name

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

            if (settings.getBoolean("first_time_login", true)) {
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
  		case R.id.login_button:
		    checkLogin();
		    break;
  		case R.id.cancel_button:
	    	finish();
    		break;
    	case R.id.new_user_button:
    	    startActivity(new Intent(this, Account.class));
    	    break;
		}
    }
}
