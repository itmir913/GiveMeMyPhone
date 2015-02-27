package com.leejonghwan.givememyphone;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;

@SuppressLint("NewApi")
public class ScreenService extends Service {
	BroadcastReceiver myReceiver = new BroadCast();
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_BOOT_COMPLETED);
		
		registerReceiver(myReceiver, filter);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			unregisterReceiver(myReceiver);
		} catch (Exception e) {}
		restartService();
	}
	
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		restartService();
	}

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		super.onTaskRemoved(rootIntent);
		restartService();
	}
	
	private void restartService() {
		SharedPreferences pref = getSharedPreferences("preference", 0);
		if(pref.getBoolean("Service_Running", false)){
			Intent intent = new Intent(this, BroadCast.class);
			intent.setAction("ACTION_SERVICE_RESTART");
			sendBroadcast(intent);
		}
	}
	
}