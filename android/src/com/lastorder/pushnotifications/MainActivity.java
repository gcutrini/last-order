package com.lastorder.pushnotifications;

import static com.lastorder.pushnotifications.CommonUtilities.DISPLAY_MESSAGE_ACTION;
import static com.lastorder.pushnotifications.CommonUtilities.SENDER_ID;
import static com.lastorder.pushnotifications.CommonUtilities.EXTRA_MESSAGE;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.lastorder.pushnotifications.data.MyComparator;
 
public class MainActivity extends Activity implements LocationListener {
    // label to display gcm messages
 
    // Asyntask
    AsyncTask<Void, Void, Void> mRegisterTask;
 
    // Alert dialog manager
    AlertDialogManager alert = new AlertDialogManager();
 
    // Connection detector
    ConnectionDetector cd;
 
    public static String name;
    public static String email;
    private ListView promotionList;
    private ArrayList<Promotion> promotions = new ArrayList<Promotion>();
    LastOrderApplication application;
    private LocationManager locationManager;
    private String provider;
    private Location location;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
        cd = new ConnectionDetector(getApplicationContext());
 
        // Check if Internet present
        if (!cd.isConnectingToInternet()) {
            // Internet Connection is not present
            alert.showAlertDialog(MainActivity.this,
                    "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            // stop executing code by return
            return;
        }
        application = (LastOrderApplication) getApplication();
        // Getting name, email from intent
        Intent i = getIntent();
 
        name = i.getStringExtra("name");
        email = i.getStringExtra("email");     
 
        // Make sure the device has the proper dependencies.
        GCMRegistrar.checkDevice(this);
 
        // Make sure the manifest was properly set - comment out this line
        // while developing the app, then uncomment it when it's ready.
        GCMRegistrar.checkManifest(this);
        
        loadPref();
        
        promotionList = (ListView)findViewById(R.id.lvPromotions);
 
        // Get GCM registration id
        final String regId = GCMRegistrar.getRegistrationId(this);
 
        // Check if regid already presents
        if (regId.equals("")) {
            // Registration is not present, register now with GCM
            GCMRegistrar.register(this, SENDER_ID);
        } else {
            // Device is already registered on GCM
            if (GCMRegistrar.isRegisteredOnServer(this)) {
                // Skips registration.
                Toast.makeText(getApplicationContext(), "Already registered with GCM", Toast.LENGTH_LONG).show();
            } else {
                // Try to register again, but not in the UI thread.
                // It's also necessary to cancel the thread onDestroy(),
                // hence the use of AsyncTask instead of a raw thread.
                final Context context = this;
                mRegisterTask = new AsyncTask<Void, Void, Void>() {
 
                    @Override
                    protected Void doInBackground(Void... params) {
                        // Register on our server
                        // On server creates a new user
                        ServerUtilities.register(context, name, email, regId);
                        return null;
                    }
 
                    @Override
                    protected void onPostExecute(Void result) {
                        mRegisterTask = null;
                    }
 
                };
                mRegisterTask.execute(null, null, null);
            }
        }
        
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        location = locationManager.getLastKnownLocation(provider);

        promotions = removeExpiredPromsSortAndFilter(application.dataManager.selectAllPromotion());
        registerReceiver(mHandleMessageReceiver, new IntentFilter(
                DISPLAY_MESSAGE_ACTION));
        promotionList.setAdapter(new PromotionListAdapter(promotions, this, location));
        // Initialize the location fields
          System.out.println("Provider " + provider + " has been selected.");
          onLocationChanged(location);
        
          
    }      
 
    /**
     * Receiving push messages
     * */
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
            promotions = removeExpiredPromsSortAndFilter(application.dataManager.selectAllPromotion());
            // Waking up mobile if it is sleeping
            WakeLocker.acquire(getApplicationContext());
            
            /**
             * Take appropriate action on this message
             * depending upon your app requirement
             * For now i am just displaying it on the screen
             * */
            ((PromotionListAdapter)promotionList.getAdapter()).updatePromotions(promotions);
            //promotionList.setAdapter(new PromotionListAdapter(promotions, context, location != null ? location : locationManager.getLastKnownLocation(provider)));
            // Showing received message
            //Toast.makeText(getApplicationContext(), "New Message: " + newMessage, Toast.LENGTH_LONG).show();
 
            // Releasing wake lock
            WakeLocker.release();
        }
    };
 
    @Override
    protected void onDestroy() {
        if (mRegisterTask != null) {
            mRegisterTask.cancel(true);
        }
        try {
            unregisterReceiver(mHandleMessageReceiver);
            GCMRegistrar.onDestroy(this);
        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
        super.onDestroy();
    }
 



/* Request updates at startup */
@Override
protected void onResume() {
  super.onResume();
  locationManager.requestLocationUpdates(provider, 400, 1, this);
}

/* Remove the locationlistener updates when Activity is paused */
@Override
protected void onPause() {
  super.onPause();
  locationManager.removeUpdates(this);
}

@Override
public void onLocationChanged(Location loc) {
	 location = loc;
	 ((PromotionListAdapter)promotionList.getAdapter()).updateLocation(location);
	 //promotionList.setAdapter(new PromotionListAdapter(promotions, this, location));

}




@Override
public void onProviderDisabled(String provider) {
	// TODO Auto-generated method stub
	
}




@Override
public void onProviderEnabled(String provider) {
	// TODO Auto-generated method stub
	
}




@Override
public void onStatusChanged(String provider, int status, Bundle extras) {
	// TODO Auto-generated method stub
	
}
@Override
public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
}

@Override
public boolean onOptionsItemSelected(MenuItem item) {

	/*
	 * Because it's onlt ONE option in the menu.
	 * In order to make it simple, We always start SetPreferenceActivity
	 * without checking.
	 */
	
	Intent intent = new Intent();
    intent.setClass(MainActivity.this, com.lastorder.pushnotifications.SetPreferenceActivity.class);
    startActivityForResult(intent, 0); 
	
    return true;
}

@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	// TODO Auto-generated method stub
	//super.onActivityResult(requestCode, resultCode, data);
	
	/*
	 * To make it simple, always re-load Preference setting.
	 */
	
	loadPref();
	promotions = removeExpiredPromsSortAndFilter(application.dataManager.selectAllPromotion());
	((PromotionListAdapter)promotionList.getAdapter()).updatePromotions(promotions);
}

private int distance;
private void loadPref(){
	SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	distance = mySharedPreferences.getInt("distance", 5000);
	Log.i("act", ""+distance);

}


private ArrayList<Promotion> removeExpiredPromsSortAndFilter(ArrayList<Promotion> proms) {
	for(int x = 0; x < proms.size(); x++) {
		if(proms.get(x).expiration.getTimeInMillis() - System.currentTimeMillis() < 0) {
			application.dataManager.deletePromotion(proms.get(x).id);
			proms.remove(x);
			continue;
		} 
		Location loc = new Location("");
		loc.setLatitude(proms.get(x).lat);
		loc.setLongitude(proms.get(x).lon);
		if(location.distanceTo(loc) > distance) {
			proms.remove(x);
		}
		
	}
	
	Collections.sort(proms, new MyComparator(location));
	
	return proms;
	
}

}

