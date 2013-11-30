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
	/**
	 * 2.0 업데이트
	 * 새로 작성된 파일 입니다
	 * 이 서비스는 부팅후에 화면이 꺼져도 서비스를 종료하지 못하지 못하는 중요한 결함을
	 * 수정하는 서비스 입니다
	 * 또한 꺼지지 않도록 설정하여 작업관리자 면역이 되어 있습니다
	 */
	
	BroadcastReceiver myReceiver = new BroadCast();
	
	/**
	 * onCreate메소드는 서비스가 시작되면 자동으로 호출되는 메소드 입니다
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		
		/**
		 * MainActivity.java에서 담당했던 코드를 이전하여
		 * 부팅후에도 화면이 꺼지면 서비스 작동을 멈추는 기능을 적용하였습니다
		 */
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_BOOT_COMPLETED);
		
		registerReceiver(myReceiver, filter);
		
//		System.gc();
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
		/**
		 * 2.0 업데이트
		 * 서비스가 강제로 종료되면 다시 서비스를 실행합니다.
		 */
		SharedPreferences pref = getSharedPreferences("preference", 0);
		if(pref.getBoolean("Service_Running", false)){
			Intent intent = new Intent(this, BroadCast.class);
			intent.setAction("ACTION_SERVICE_RESTART");
			sendBroadcast(intent);
		}
	}
	
}