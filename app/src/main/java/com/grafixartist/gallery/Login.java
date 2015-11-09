package com.grafixartist.gallery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

// import android.view.View;
// import android.view.View.OnClickListener;
//For Android 3.0 and above comment out the lines below

//For Android 3.0 and above uncomment the lines below
// import android.app.FragmentManager;
// import android.app.FragmentActivity; 

public class Login extends FragmentActivity implements View.OnClickListener {
   private DatabaseHelper dh;
   private final String PREFS_NAME = "MyPrefsFile";
   private EditText userNameEditableField;
   private EditText passwordEditableField;
   private final static String OPT_NAME="name";
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        if (!settings.getBoolean("my_first_time", true)){
            // Bring up the Gallery
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        FragmentManager.enableDebugLogging(true);
        setContentView(R.layout.login);
        
        userNameEditableField=(EditText)findViewById(R.id.username_text);
        passwordEditableField=(EditText)findViewById(R.id.password_text);
        View btnLogin=findViewById(R.id.login_button);
        btnLogin.setOnClickListener(this);
        View btnCancel=findViewById(R.id.cancel_button);
        btnCancel.setOnClickListener(this);
        View btnNewUser=findViewById(R.id.new_user_button);
        if (btnNewUser!=null) btnNewUser.setOnClickListener(this);
        
     }
    
    private void checkLogin() {
        String username = this.userNameEditableField.getText().toString();
        String password = this.passwordEditableField.getText().toString();
        this.dh = new DatabaseHelper(this);
        List<String> names = this.dh.selectAll(username, password);
        if (names.size() > 0) { // Login successful
            // Save username as the name

            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

            if (settings.getBoolean("my_first_time", true)) {
                Log.d("Hello", "preferences called");
                settings.edit().putBoolean("my_first_time", false).apply();
                settings.edit().putString(OPT_NAME, username).apply();
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
