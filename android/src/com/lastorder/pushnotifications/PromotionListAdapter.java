package com.lastorder.pushnotifications;

import java.util.ArrayList;

import com.lastorder.pushnotifications.data.ImageDownloader;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PromotionListAdapter extends BaseAdapter {
	
	private ArrayList<Promotion> promotions;
	private LayoutInflater inflater;
	private Context context;
	private Location myLocation;
	private long mStartTime;
	private ImageDownloader imageDownloader;
	public PromotionListAdapter(ArrayList<Promotion> promos, Context c, Location myLoc) {
		promotions = promos;
		context = c;
		inflater = LayoutInflater.from(context);
		myLocation = myLoc;
		mStartTime = System.currentTimeMillis();
		mHandler.removeCallbacks(mUpdateTimeTask);
		mHandler.postDelayed(mUpdateTimeTask, 1000);
		imageDownloader = new ImageDownloader();
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return promotions.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return promotions.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		
		ViewHolder holder;
		if(convertView == null) {
			holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.promotion_list, null);
			holder.venue = (TextView)convertView.findViewById(R.id.venue);
			holder.name = (TextView)convertView.findViewById(R.id.name);
			holder.description = (TextView)convertView.findViewById(R.id.description);
			holder.image_url = (ImageView)convertView.findViewById(R.id.url_image);
			holder.address = (TextView)convertView.findViewById(R.id.address);
			holder.discount = (TextView)convertView.findViewById(R.id.discount);
			holder.distance = (TextView)convertView.findViewById(R.id.distance);
			holder.expiration = (TextView)convertView.findViewById(R.id.expiration);
			holder.price = (TextView)convertView.findViewById(R.id.price);
			holder.bar = (ProgressBar)convertView.findViewById(R.id.progress);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		holder.venue.setText(promotions.get(position).venue);
		holder.name.setText(promotions.get(position).name);
		holder.description.setText(promotions.get(position).description);
		holder.address.setText(promotions.get(position).address);
		holder.discount.setText(promotions.get(position).discount + "%");
		holder.expiration.setText(DateUtils.formatElapsedTime((promotions.get(position).expiration.getTimeInMillis() - System.currentTimeMillis())/1000));
		
		imageDownloader.download(promotions.get(position).url_image, holder.image_url, context, holder.bar);
		holder.price.setText("$"+promotions.get(position).price);
		Location rest =  new Location("");
		rest.setLatitude(promotions.get(position).lat);
		rest.setLongitude(promotions.get(position).lon);
		holder.distance.setText(Math.round(myLocation.distanceTo(rest))+"m");
		
		return convertView;
	}
	
	private class ViewHolder {
		TextView venue;
		TextView name;
		TextView description;
		ImageView image_url;
		TextView discount;
		TextView distance;
		TextView price;
		TextView address;
		TextView expiration;
		ProgressBar bar;
		
	}
	
	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}
	
	public void updateLocation(Location loc) {
		myLocation = loc;
		notifyDataSetChanged();
	}
	
	public void updatePromotions(ArrayList<Promotion> prom) {
		promotions = prom;
		notifyDataSetChanged();
	}
	private Handler mHandler = new Handler();
	
	private Runnable mUpdateTimeTask = new Runnable() {
		   public void run() {
		       notifyDataSetChanged();
		     
		       mHandler.postDelayed(mUpdateTimeTask,1000);
		   }
		};

}
