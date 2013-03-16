package com.lastorder.pushnotifications;

import static com.lastorder.pushnotifications.CommonUtilities.SENDER_ID;
import static com.lastorder.pushnotifications.CommonUtilities.SERVER_URL;

import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
 
public class RegisterActivity extends Activity {
    // alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();
 
    // Internet detector
    ConnectionDetector cd;
 
    // UI elements
    //EditText txtName;
    EditText txtEmail;
 
    // Register button
    Button btnRegister;
    Button btnMaybeLater;
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
 
        cd = new ConnectionDetector(getApplicationContext());
 
        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(RegisterActivity.this,
                    "","Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }
 
        // Check if GCM configuration is set
        if (SERVER_URL == null || SENDER_ID == null || SERVER_URL.length() == 0
                || SENDER_ID.length() == 0) {
            // GCM sernder id / server url is missing
            alert.showAlertDialog(RegisterActivity.this, "Configuration Error!",
                    "Please set your Server URL and GCM Sender ID", false);
            // stop executing code by return
             return;
        }
        
        final String regId = GCMRegistrar.getRegistrationId(this);
        
        // Check if regid already presents
        if (regId.equals("")) {
  //txtName = (EditText) findViewById(R.id.txtName);
	        txtEmail = (EditText) findViewById(R.id.txtEmail);
	        btnRegister = (Button) findViewById(R.id.btnRegister);
	        btnMaybeLater = (Button) findViewById(R.id.btnMaybeLater);
	        /*
	         * Click event on Register button
	         * */
	        btnRegister.setOnClickListener(new View.OnClickListener() {
	 
	            @Override
	            public void onClick(View arg0) {
	                // Read EditText dat
//String name = txtName.getText().toString();
	                String email = txtEmail.getText().toString();
	                
	                
	                // Check if user filled the form
	                if(email.trim().length() > 0){
	                    // Launch Main Activity
	                    Intent i = new Intent(getApplicationContext(), MainActivity.class);
	 
	                    // Registering user on our server
	                    // Sending registraiton details to MainActivity
//i.putExtra("name", name);
	                    i.putExtra("email", email);
	                    startActivity(i);
	                    finish();
	                }else {
	                    // user doen't filled that data
	                    // ask him to fill the form
	                    alert.showAlertDialog(RegisterActivity.this, "","Please enter your email address", false);
	                }
	            }
	     
	        });
	        
	        btnMaybeLater.setOnClickListener(new View.OnClickListener() {
                
                @Override
                public void onClick(View arg0) {
                Intent i = new Intent(getApplicationContext(), MainActivity.class);

                // Registering user on our server
                // Sending registraiton details to MainActivity
//i.putExtra("name", android.os.Build.MODEL);
                i.putExtra("email", android.os.Build.MODEL);
                startActivity(i);
                finish();
                }
            });
	        
	        
        } 
       else {
       	 	Intent i = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
            finish();
       }
    }
 
}