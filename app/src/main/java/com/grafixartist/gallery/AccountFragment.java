package com.grafixartist.gallery;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class AccountFragment extends Fragment implements OnClickListener {
	private EditText etEmail;
	private EditText etPassword;
	private EditText etConfirm;
	private DatabaseHelper dh;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    	// Inflate the layout for this fragment
    	View v =  inflater.inflate(R.layout.accountfragment, container, false);
        etEmail = (EditText)v.findViewById(R.id.email);
        etPassword= (EditText)v.findViewById(R.id.password);
        etConfirm = (EditText)v.findViewById(R.id.password_confirm);
        View btnAdd= v.findViewById(R.id.done_button);
        // Set on click listeners
        btnAdd.setOnClickListener(this); 
        View btnCancel= v.findViewById(R.id.clear_button);
        btnCancel.setOnClickListener(this);
        return v;
    }

    private void CreateAccount(){
        // Get email, password, and confirmation password
    	String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirm	= etConfirm.getText().toString();
        // Grab instance of databasehelper for DB access
        this.dh = new DatabaseHelper(getContext());
        // Check if email entered is empty
        if(email.equals("")) {
            Toast.makeText(this.getActivity(), "Please enter an email", Toast.LENGTH_LONG).show();
        }
        // Check if email already exists
        else if (this.dh.checkEmailExists(email)) {
            Toast.makeText(this.getActivity(), "Email already exists", Toast.LENGTH_LONG).show();
        }
        // Check password fields are not empty
        else if((password.equals(""))||(confirm.equals(""))){
            Toast.makeText(AccountFragment.this.getActivity(), "Password or Confirm Password must not be blank", Toast.LENGTH_LONG).show();
        }
        // Check password is at least 5 characters
        else if (password.length() < 5){
            Toast.makeText(this.getActivity(), "Password must be at least 5 characters", Toast.LENGTH_LONG).show();
        }
        // Check password is equal to confirmation password
        else if(!password.equals(confirm)){
            new AlertDialog.Builder(this.getActivity())
                    .setTitle("Error")
                    .setMessage("Passwords do not match")
                    .setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).show();
        }
        // Check email is in valid format
        else if (!isValidEmail(email)) {
            Toast.makeText(this.getActivity(), "Email address is not in valid format", Toast.LENGTH_LONG).show();
        }
        else {
            // Create new account using email and password and exit activity back to login
            dh.insert(email, password);
            Toast.makeText(this.getActivity(), "Successfully created account", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return target != null && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public void onClick(View v) {
		switch (v.getId()) {
        // If create button is pressed, send to create account method
  		case R.id.done_button:
		    CreateAccount();
		    break;
        // If clear button is pressed, clear text fields
		case R.id.clear_button:
			etEmail.setText("");
	        etPassword.setText("");
	        etConfirm.setText("");
	    	break;
		}
    }
}
