package com.lastorder.pushnotifications;

import com.lastorder.pushnotifications.data.DataManager;

import android.app.Application;

public class LastOrderApplication extends Application {

	public static DataManager dataManager;
	
	@Override
	public void onCreate() {
		super.onCreate();
		dataManager = new DataManager(this);
	}
	
	@Override
	public void onTerminate() {
		dataManager.closeDb();
		super.onTerminate();
	}
	
	
}
