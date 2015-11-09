package com.grafixartist.gallery;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
        btnAdd.setOnClickListener(this); 
        View btnCancel= v.findViewById(R.id.cancel_button);
        btnCancel.setOnClickListener(this);
        return v;
    }

    private void CreateAccount(){
    	String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String confirm	= etConfirm.getText().toString();
        this.dh = new DatabaseHelper(this.getContext());
        if (email.equals("")&& this.dh.checkUsernameExists(email)) {
            Toast.makeText(this.getActivity(), "Username already exists", Toast.LENGTH_SHORT).show();
        }
        else if (email.length() < 6) {
            Toast.makeText(this.getActivity(), "Username must be at least 6 characters", Toast.LENGTH_SHORT).show();
        }
        else if((password.equals(""))||(confirm.equals(""))){
            Toast.makeText(AccountFragment.this.getActivity(), "Password or Confirm Password must not be blank", Toast.LENGTH_SHORT).show();
        }
        else if (password.length() < 5){
            Toast.makeText(this.getActivity(), "Password must be at least 5 characters", Toast.LENGTH_SHORT).show();
        }
        else if(!password.equals(confirm)){
            new AlertDialog.Builder(this.getActivity())
                    .setTitle("Error")
                    .setMessage("Passwords do not match")
                    .setNeutralButton("Try Again", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {}
                    }).show();
        }
        else {
            this.dh = new DatabaseHelper(this.getActivity());
            this.dh.insert(email, password);
            Toast.makeText(this.getActivity(), "Successfully created account", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

    }
    
    public void onClick(View v) {
		switch (v.getId()) {
  		case R.id.done_button:
		    CreateAccount();
		    break;
		case R.id.cancel_button:
			etEmail.setText("");
	        etPassword.setText("");
	        etConfirm.setText("");
	    	break;
		}
    }
}
