package com.lastorder.pushnotifications;
import java.text.ParseException;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;
 
import com.google.android.gcm.GCMBaseIntentService;
import com.lastorder.pushnotifications.data.PromotionDAO;
 
import static com.lastorder.pushnotifications.CommonUtilities.SENDER_ID;
import static com.lastorder.pushnotifications.CommonUtilities.displayMessage;
 
public class GCMIntentService extends GCMBaseIntentService {
 
    private static final String TAG = "GCMIntentService";
    
    LastOrderApplication application;
    GPSTracker gps;

    public GCMIntentService() {
        super(SENDER_ID);
    }
 
    /**
     * Method called on device registered
     **/
    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        displayMessage(context, "Your device registred with GCM");
        
        ServerUtilities.register(context, MainActivity.email, registrationId); 
    }
 
    /**
     * Method called on device un registred
     * */
    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        displayMessage(context, getString(R.string.gcm_unregistered));
        ServerUtilities.unregister(context, registrationId);
    }
 
    /**
     * Method called on Receiving a new message
     * */
    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        String message = intent.getExtras().getString("price");
        application = (LastOrderApplication) getApplication();

    	
        try {
			JSONObject x = new JSONObject(message);
			
	        Promotion prom = new Promotion();
	        prom.venue = x.getString("venue");
	        prom.name = x.getString("name");
	        prom.description = x.getString("description");
	        prom.address = x.getString("address");
	        prom.lat = x.getDouble("lat");
	        prom.lon = x.getDouble("lon");
	        prom.price = x.getDouble("price");
	        prom.discount = x.getInt("discount");
	        prom.url_image = x.getString("url_image");
	        try {
				prom.expiration.setTime(PromotionDAO.df.parse(x.getString("expiration")));
				Log.i(TAG, PromotionDAO.df.format( prom.expiration.getTime()));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        application.dataManager.insertPromotion(prom);

	        gps = new GPSTracker(getApplication().getApplicationContext());
	        
            // check if GPS enabled
            if (gps.canGetLocation()) {
            	Location userLocation = new Location("userLocation");
            	userLocation.setLatitude(gps.getLatitude());
            	userLocation.setLongitude(gps.getLongitude());

            	Location promLocation = new Location("promLocation");
            	promLocation.setLatitude(prom.lat);
        		promLocation.setLongitude(prom.lon);

        		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplication().getApplicationContext());
        		if (Math.round(userLocation.distanceTo(promLocation)) <= preferences.getInt("distance", 5000)) {
        	        generateNotification(context, "New promotion within reach.");
        		} else {
        	        generateNotification(context, "Promotion received but not within reach.");
        		}

            } else {
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gps.showSettingsAlert();
            }

	        displayMessage(context, message);
	    
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    /**
     * Method called on receiving a deleted message
     * */
    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        String message = getString(R.string.gcm_deleted, total);
        displayMessage(context, message);
        // notifies user
        generateNotification(context, message);
    }
 
    /**
     * Method called on Error
     * */
    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
		displayMessage(context, getString(R.string.gcm_error, errorId));
    }
 
    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        displayMessage(context, getString(R.string.gcm_recoverable_error,
                errorId));
        return super.onRecoverableError(context, errorId);
    }
 
    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
 
        String title = context.getString(R.string.app_name);
 
        Intent notificationIntent = new Intent(context, MainActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
 
        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;
 
        // Vibrate if vibrate is enabled
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(0, notification);     
 
    }
    
 
}
