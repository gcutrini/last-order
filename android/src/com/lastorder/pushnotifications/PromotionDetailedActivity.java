package com.lastorder.pushnotifications;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lastorder.pushnotifications.data.ImageDownloader;

public class PromotionDetailedActivity extends Activity {

	private Context context;
	private Promotion promotion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_details);
    	promotion = (Promotion) getIntent().getSerializableExtra("promotion");
    	Location location = (Location)getIntent().getParcelableExtra("location");
    	ProgressBar bar;
    	TextView venue, name, summary, price, distance, expiration, discount, address;
    	ImageView image;
    	Button navigate;
    	context = this;
    	
    	ImageDownloader imageDownloader = new ImageDownloader();
    	
    	navigate = (Button)findViewById(R.id.navigate);
    	venue = (TextView)findViewById(R.id.venue);
    	name = (TextView)findViewById(R.id.name);
    	summary = (TextView)findViewById(R.id.description);
    	price = (TextView)findViewById(R.id.price);
    	distance = (TextView)findViewById(R.id.distance);
    	expiration = (TextView)findViewById(R.id.expiration);
    	discount = (TextView)findViewById(R.id.discount);
    	image = (ImageView)findViewById(R.id.image);
    	address = (TextView)findViewById(R.id.address);
    	bar = (ProgressBar)findViewById(R.id.progress);
    	
    	venue.setText(promotion.venue);
		name.setText(promotion.name);
		summary.setText(promotion.description);
		address.setText(promotion.address);
		discount.setText(promotion.discount + "%");
		expiration.setText(DateUtils.formatElapsedTime((promotion.expiration.getTimeInMillis() - System.currentTimeMillis())/1000));
		
		imageDownloader.download(promotion.url_image, image, this, bar);
		price.setText("$"+promotion.price);
		Location rest =  new Location("");
		rest.setLatitude(promotion.lat);
		rest.setLongitude(promotion.lon);
		distance.setText(Math.round(location.distanceTo(rest))+"m");
		navigate.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				launchNavigation(promotion.lat, promotion.lon);
			}
		});
    }
    
    public boolean canHandleIntent(Intent intent){
        PackageManager packageManager = context.getPackageManager();
        List activities = packageManager.queryIntentActivities(
            intent, 
            PackageManager.MATCH_DEFAULT_ONLY);
        return activities.size() > 0;
    }
     
    public void launchNavigation(Double latitude, Double longitude){
        String uri = "google.navigation:ll=%f,%f";
        Intent navIntent = new Intent(
            Intent.ACTION_VIEW,
            Uri.parse(String.format(uri, latitude, longitude)));
        if(canHandleIntent(navIntent))
            startActivity(navIntent);
        else
            Toast.makeText(context, "Please install Google Navigation", Toast.LENGTH_LONG).show();
    }
}
