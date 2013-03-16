package com.lastorder.pushnotifications;

import com.lastorder.pushnotifications.data.ImageDownloader;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PromotionDetailedActivity extends Activity {

	
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_details);
    	Promotion promotion = (Promotion) getIntent().getSerializableExtra("promotion");
    	Location location = (Location)getIntent().getParcelableExtra("location");
    	ProgressBar bar;
    	TextView venue, name, summary, price, distance, expiration, discount, address;
    	ImageView image;
    	ImageDownloader imageDownloader = new ImageDownloader();
    	
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
    }
}
