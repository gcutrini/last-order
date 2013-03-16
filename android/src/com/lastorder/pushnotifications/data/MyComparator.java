package com.lastorder.pushnotifications.data;

import android.location.Location;

import com.lastorder.pushnotifications.Promotion;

public class MyComparator implements java.util.Comparator<Promotion>{
	
	
	private Location location;
	
	public MyComparator(Location loca) {
		
		this.location = loca;
	}
	
	
	@Override
	public int compare(Promotion lhs, Promotion rhs) {
		// TODO Auto-generated method stub
		Location locl = new Location("");
		locl.setLatitude(lhs.lat);
		locl.setLongitude(lhs.lon);
		
		Location locr = new Location("");
		locr.setLatitude(rhs.lat);
		locr.setLongitude(rhs.lon);
		
		
		return (int) (location.distanceTo(locl) - location.distanceTo(locr));
	}

}
