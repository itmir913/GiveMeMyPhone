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
import android.util.Log;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class GiveMePhoneService extends Service implements SensorEventListener {
	/**
	 * 1.1 ������Ʈ
	 * ���� ��Ƽ��Ƽ���� ������ ���� �����ϴ´�, �� ������ ���� �̿��ؼ�
	 * min�� �����ϵ��� ����
	 */
	SharedPreferences pref;
	
	/**
	 * 1.1������Ʈ
	 * ȭ���� ���� �������� �۵��ϵ��� �ڵ� ����
	 * �� ȭ���� ���������� ������ �︮�� �ʵ��� ����
	 */
	PowerManager mPm;
	
	/**
	 * �� ���񽺿����� �����带 ����մϴ�
	 * ����ϴ� ������ ����Դϴ�
	 */
	Thread thread;
	
	/**
	 * ���񽺰� ����Ǿ����� �������� �����ϴ� ���ҵ� �մϴ�
	 */
	static int Save_Min, Save_Delay;
	
	/**
	 * boolean isThread�� ���񽺰� ����Ǿ����� ������(run�޼ҵ�)�� �ߴܵǵ��� ����Ǿ���
	 * �� ������ �ʿ����
	 */
//	boolean isThread;
	
	/**
	 * �ٸ������ ������ ��������
	 * DevicePolicyManager�� ����Ͽ� ����Ʈ���� ȭ���� ������ �����ŵ�ϴ�
	 */
	DevicePolicyManager DeviceManager;
	
	/**
	 * �������� �ڵ��
	 */
		long lastTime, currentTime, gabOfTime;
	    float speed, lastX, lastY, lastZ, x, y, z, lastSpeed, tmp;
		static final int DATA_X = SensorManager.DATA_X, DATA_Y = SensorManager.DATA_Y, DATA_Z = SensorManager.DATA_Z;
	 
	    public static SensorManager sensorManager;
	    public static Sensor accelerormeterSensor;

	    /**
	     * 1.3 ������Ʈ
	     * �˸��� ǥ���ϴ� �޼ҵ��̴�
	     * ���񽺰� ���۵Ǹ� ���� �˸��� ǥ���ϵ��� �ϰ�
	     * ���񽺰� ����ȴٸ� �˸��� �����ϵ��� �����Ͽ���
	     */
	    private void showNotify(Context context, int num) {
    		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
    		
	    	if(num==1){
	    		Notification notification = new Notification(R.drawable.ic_launcher, context.getString(R.string.app_name), System.currentTimeMillis());
	    		notification.flags = Notification.FLAG_ONGOING_EVENT;
	    		// FLAG_AUTO_CANCEL:�� �˸�(Ȯ���ϸ� ������), FLAG_ONGOING_EVENT�� ������ǥ��
	    		notification.setLatestEventInfo(context, context.getString(R.string.app_name), context.getString(R.string.running), contentIntent);
	    		nm.notify(1234, notification);
	    		/**
	    		 * 1.4 ������Ʈ : ��� ��ġ
	    		 * Min���� 250 �Ʒ��̸� �ɰ��� �����̹Ƿ� ���� ǥ�ø� �Ѵ�
	    		 */
	    	}else if (num==2){
	    		Notification notification = new Notification(R.drawable.ic_launcher, context.getString(R.string.app_name)+" "+context.getString(R.string.error), System.currentTimeMillis());
	    		notification.flags = Notification.FLAG_AUTO_CANCEL;
	    		notification.setLatestEventInfo(context, context.getString(R.string.Service_Error_1), String.format(context.getString(R.string.Service_Error_2), Save_Min), contentIntent);
	    		nm.notify(4444, notification);
	    	}
	    }
	    
	    private void DeleteNotify(Context context){
	    	NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	    	nm.cancel(1234);
	    }
	    
	/**
	 * onCreate�޼ҵ�� ���񽺰� ���۵Ǹ� �ڵ����� ȣ��Ǵ� �޼ҵ� �Դϴ�
	 */
	public void onCreate() {
		super.onCreate();
		Log.d("�������� ����", "����");
		showNotify(this, 1);
		
//		if(MainActivity.Boot_Service_check!=1){
//			/**
//			 * 1.5 ������Ʈ
//			 * ���񽺿��� ��ε�ĳ��Ʈ�� �������� �ʽ��ϴ� ����
//			 * ���ýÿ�(��ε�ĳ��Ʈ���ù����� ���񽺸� �����ϹǷ� ���ξ�Ƽ��Ƽ�� �ִ� Boot_Service_check�� 1�� �ƴմϴ�)
//			 * ���ù��� �ڵ� ����ǵ��� ����
//			 */
//	        // ������ �ޱ����� ����Ʈ���͸� �����մϴ�.
//	        filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
//	        filter.addAction(Intent.ACTION_SCREEN_OFF);
//	        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
//	        registerReceiver(myReceiver, filter);
//		}
		
		// SharedPreferences�� ��� ����
		pref = getSharedPreferences("prefs", 0);
		
		// ȭ���� ���� �������� �۵��ϵ��� �ϴ� PowerManager
		// �ý��� ���񽺷� �����ϹǷ� ���񽺸� ����
		mPm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		// ��� ȭ���� ��ױ� ���� �۾�
	    DeviceManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

	    /**
		 * �������� �ڵ��
		 */
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		if (accelerormeterSensor != null)
			sensorManager.registerListener(this, accelerormeterSensor,
					SensorManager.SENSOR_DELAY_GAME);
		// �� if�Ʒ� ������ ����ó�� �������� �����δ� ���͸� ���� �����̴�
		
		/**
		 * ���� ��Ƽ��Ƽ���� �ΰ��� ���� �����ɴϴ�
		 */
		//Save_Min = MainActivity.MinSenserInt;
		//Save_Delay = MainActivity.DelayInt;
		
		/**
		 * 1.1 ������Ʈ
		 * ���� ��Ƽ��Ƽ���� �������� �ʰ� ������ ������ ���ɴϴ�
		 * ���񽺰� �������ϰ�� ������ �����ϸ� �� ������ ���ɴϴ�
		 */
		Save_Min = pref.getInt("MinSenser", 1000);
		Save_Delay = pref.getInt("Delay", 1);
		
		/**
		 * �����带 �����ϴ�
		 * ��� �����带 �̿��ϴ� ������ ������ �Լ� ������ �����ϰ� ��û����
		 * ������ Ȯ���� ���� ���ؼ� ����մϴ�
		 * 
		 * 1.1 ������Ʈ:�����带 ������� �ʰڽ��ϴ�
		 */
//		thread = new Thread(this);
//		thread.start();
	}
	
	/**
	 * implements Runnable���� �ʿ������� ������ �ʿ���� �޼ҵ�
	 * ������� ����
	 */
	@Override
	public IBinder onBind(Intent arg0) { return null; }
	
	/**
	 * �����尡 ���۵Ǹ� ȣ��Ǵ� �޼ҵ�
	 * ������ ������ ����
	 * 
	 * 1.1 ������Ʈ:�����带 ������� �������� run�޼ҵ尡 �ʿ� �����ϴ�
	 */
//	@Override
//	public void run() {
//		// TODO Auto-generated method stub
//	}
	
	/**
	 * ���񽺰� ����ɶ� ���������� ȣ��Ǵ� �޼ҵ�
	 * ������ �����ϰ� ���� ���� �佺Ʈ �޼����� ���ϴ�
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		/**
		 * ���� �� Ȯ���� �ߴ��մϴ�
		 */
		if (sensorManager != null)
			sensorManager.unregisterListener(this);
		
//		unregisterReceiver(myReceiver);
//      Log.d("��ε�ĳ��Ʈ", "����");
		
		DeleteNotify(this);
		Toast.makeText(this, "�������� ���񽺰� ����Ǿ����ϴ�", Toast.LENGTH_LONG).show();
		Log.d("�������� ����", "����");
	}
	
	/**
	 * implements SensorEventListener���� �ʿ��� �޼ҵ�
	 * ���� ���� �� �޼ҵ尡 ���ϴ����� �� �𸣰ڴ�
	 * �ƴ°� �� �޼ҵ�� �ʿ���� �޼ҵ��°�
	 * ������ ������� ����
	 * 
	 * ��Ȯ�� �½�Ʈ ��� �Ѵ�
	 */
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if(mPm.isScreenOn())
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			currentTime = System.currentTimeMillis();
			gabOfTime = currentTime - lastTime;
			if (gabOfTime > 100){
				lastTime = currentTime;
				x = event.values[SensorManager.DATA_X];
				y = event.values[SensorManager.DATA_Y];
				z = event.values[SensorManager.DATA_Z];
				
				speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;
				/**
				 * 1.4 ������Ʈ : ��� ��ġ
				 * Min���� 250�Ʒ��� ��������� ȭ���� Ű�ڸ��� ���������Ƿ� ������Ȳ�̴�
				 * �׷��Ƿ� �۵����� �ʵ��� ó���Ѵ�
				 */
				if (Save_Min < 250){
					Log.e("������ ��ȭ ����", "Min���� 250�Ʒ��� ���������ϴ�");
					Log.e("������ ��ȭ ����", "���� �ɰ��մϴ� ���񽺸� �����մϴ�");
					Log.e("������ ��ȭ ����", "Save_Min�� : "+Save_Min);
					if (sensorManager != null)
						sensorManager.unregisterListener(this);
					DeleteNotify(this);
					showNotify(this, 2);
					onDestroy();
					/**
					 * 1.5 ������Ʈ
					 * Save_Min�� 250�Ʒ��϶�, ���񽺱��� ������ �����ϵ��� �ҽ� ����
					 */
					Intent myIntent = new Intent(getBaseContext(), GiveMePhoneService.class);
					stopService(myIntent);
				}else if (speed > Save_Min) {
					// ���� ���ӵ��� �ּ� ������ ũ��!
					Log.d("���ӵ� ����", "Speed :"+speed+" ����: "+Save_Min);
					
					/**
					 * 1.2 ������Ʈ
					 * ���� ���� ����
					 */
					if(pref.getInt("Vibrator", 0)==1){ // ������ Ȱ��ȭ �������
						Vibrator vide = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						/**
						 * 1.1 ������Ʈ
						 * ���� �ð��� ���� ���
						 */
						vide.vibrate(100);
					}
					DeviceManager.lockNow();
				}
				lastX = event.values[DATA_X];
				lastY = event.values[DATA_Y];
				lastZ = event.values[DATA_Z];
	           }
	       }
	}
	// ���� ��
	
}