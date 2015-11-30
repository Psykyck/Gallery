package com.grafixartist.gallery;


import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

//For Android 3.0 and above comment out the lines below

// For Android 3.0 and above uncomment the lines below 
// import android.app.FragmentManager;
// import android.app.FragmentTransaction;
// import android.app.FragmentActivity; 

public class Account extends FragmentActivity implements OnClickListener {
	   
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account);
        
        //Create the fragment
        AccountFragment accountFragment = new AccountFragment();
        // Install the Account fragment
        // For Android 3.0 and above comment out the line below
        FragmentManager fragmentManager = getSupportFragmentManager();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
 
        fragmentTransaction.add(R.id.accountdetails, accountFragment);

        fragmentTransaction.commit();
        
        // Initialize the Exit button
        View buttonExit= (Button)findViewById(R.id.exit_button);
        buttonExit.setOnClickListener(this);
    }

    public void onClick(View v) {
		switch (v.getId()) {
        // Exit activity if exit button pressed
		case R.id.exit_button:
		   	finish();
	    	break;
		}
    }
}
