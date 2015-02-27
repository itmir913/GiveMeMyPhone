package com.leejonghwan.givememyphone;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;

@SuppressWarnings("deprecation")
public class GiveMePhoneService extends Service implements SensorEventListener {
	SharedPreferences pref;
	PowerManager mPm;
	DevicePolicyManager DeviceManager;
	
	int Save_Min, Save_Delay;
	
	long lastTime;
	float lastX, lastY, lastZ, lastSpeed;
	final int DATA_X = SensorManager.DATA_X, DATA_Y = SensorManager.DATA_Y, DATA_Z = SensorManager.DATA_Z;
	
	SensorManager sensorManager;
	
	private void showNotify(Context mContext, boolean num) {
		NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, MainActivity.class), 0);
		
		/**
		 * 2.0������Ʈ
		 * ���� ���並 �ݿ��Ͽ� ���� ��ܹ� �������� �����Ͽ����ϴ� 
		 */
		if(num){
			Notification notification;
			if(pref.getBoolean("clear_icon", false))
				notification = new Notification(R.drawable.clear_icon, mContext.getString(R.string.app_name), System.currentTimeMillis());
			else
				notification = new Notification(R.drawable.ic_launcher, mContext.getString(R.string.app_name), System.currentTimeMillis());
			
			notification.flags = Notification.FLAG_ONGOING_EVENT;
			
			notification.setLatestEventInfo(mContext, mContext.getString(R.string.app_name), mContext.getString(R.string.running), contentIntent);
			nm.notify(1234, notification);
			/**
			 * 1.4 ������Ʈ : ��� ��ġ
			 * Min���� 250 �Ʒ��̸� �ɰ��� �����̹Ƿ� ���� ǥ�ø� �Ѵ�
			 */
		}else if (!num){
			
			Notification notification;
			if(pref.getBoolean("clear_icon", false))
				notification = new Notification(R.drawable.clear_icon, mContext.getString(R.string.app_name)+" "+mContext.getString(R.string.error), System.currentTimeMillis());
			else
				notification = new Notification(R.drawable.ic_launcher, mContext.getString(R.string.app_name)+" "+mContext.getString(R.string.error), System.currentTimeMillis());
			
	    	notification.flags = Notification.FLAG_AUTO_CANCEL;
	    	notification.setLatestEventInfo(mContext, mContext.getString(R.string.Service_Error_1), String.format(mContext.getString(R.string.Service_Error_2), Save_Min), contentIntent);
	    	nm.notify(4444, notification);
	    }
	}
	
	private void DeleteNotify(Context mContext){
		NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(1234);
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		pref = getSharedPreferences("preference", 0);
		
		/**
		 * �˸��� ������ �޶�� ���� ���並 �ݿ��Ͽ� �������� �˸� ��Ȱ��ȭ ���θ� �����Ҽ� �ֽ��ϴ�
		 */
		if(pref.getBoolean("notification", true))
			showNotify(this, true);
		
		mPm = (PowerManager) getSystemService(Context.POWER_SERVICE);
	    DeviceManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		Sensor accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		if (accelerormeterSensor != null)
			sensorManager.registerListener(this, accelerormeterSensor, SensorManager.SENSOR_DELAY_GAME);
		
		Save_Min = pref.getInt("MinSenser", 1000);
		Save_Delay = pref.getInt("Delay", 1);
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		if (sensorManager != null)
			sensorManager.unregisterListener(this);
		
		if(pref.getBoolean("notification", false))
			DeleteNotify(this);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(mPm.isScreenOn())
			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				long currentTime = System.currentTimeMillis();
				long gabOfTime = currentTime - lastTime;
				if (gabOfTime > 100){
					lastTime = currentTime;
					float x = event.values[SensorManager.DATA_X];
					float y = event.values[SensorManager.DATA_Y];
					float z = event.values[SensorManager.DATA_Z];
					
					float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;
					
					if (Save_Min < 250){
						if (sensorManager != null)
							sensorManager.unregisterListener(this);
						if(pref.getBoolean("notification", false)){
							DeleteNotify(this);
							showNotify(this, false);
						}
						onDestroy();
						
						Intent myIntent = new Intent(getBaseContext(), GiveMePhoneService.class);
						stopService(myIntent);
					}else if (speed > Save_Min) {
						/**
						 * 1.2 ������Ʈ
						 * ���� ���� ����
						 */
						if(pref.getBoolean("Vibrator", false)){
							Vibrator vide = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
							vide.vibrate(500);
						}
						DeviceManager.lockNow();
					}
					lastX = event.values[DATA_X];
					lastY = event.values[DATA_Y];
					lastZ = event.values[DATA_Z];
				}
	       }
	}
}