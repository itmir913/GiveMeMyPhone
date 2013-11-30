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
	 * 1.1 업데이트
	 * 메인 액티비티에서 설정한 값을 저장하는대, 그 저장한 값을 이용해서
	 * min을 설정하도록 변경
	 */
	SharedPreferences pref;
	
	/**
	 * 1.1업데이트
	 * 화면이 켜져 있을때만 작동하도록 코드 구현
	 * 즉 화면이 꺼져있을때 진동이 울리지 않도록 설정
	 */
	PowerManager mPm;
	
	/**
	 * 이 서비스에서는 쓰래드를 사용합니다
	 * 사용하는 이유는 비밀입니다
	 */
	Thread thread;
	
	/**
	 * 서비스가 실행되었을때 설정값을 저장하는 역할도 합니다
	 */
	static int Save_Min, Save_Delay;
	
	/**
	 * boolean isThread는 서비스가 종료되었을때 쓰래드(run메소드)가 중단되도록 설계되었다
	 * 만 지금은 필요없다
	 */
//	boolean isThread;
	
	/**
	 * 다른사람의 공격이 들어왔을때
	 * DevicePolicyManager를 사용하여 스마트폰의 화면을 강제로 종료시킵니다
	 */
	DevicePolicyManager DeviceManager;
	
	/**
	 * 센서관련 코드들
	 */
		long lastTime, currentTime, gabOfTime;
	    float speed, lastX, lastY, lastZ, x, y, z, lastSpeed, tmp;
		static final int DATA_X = SensorManager.DATA_X, DATA_Y = SensorManager.DATA_Y, DATA_Z = SensorManager.DATA_Z;
	 
	    public static SensorManager sensorManager;
	    public static Sensor accelerormeterSensor;

	    /**
	     * 1.3 업데이트
	     * 알림을 표시하는 메소드이다
	     * 서비스가 시작되면 먼저 알림을 표시하도록 하고
	     * 서비스가 종료된다면 알림을 종료하도록 설정하였다
	     */
	    private void showNotify(Context context, int num) {
    		NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
    		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
    		
	    	if(num==1){
	    		Notification notification = new Notification(R.drawable.ic_launcher, context.getString(R.string.app_name), System.currentTimeMillis());
	    		notification.flags = Notification.FLAG_ONGOING_EVENT;
	    		// FLAG_AUTO_CANCEL:은 알림(확인하면 지워짐), FLAG_ONGOING_EVENT은 진행중표시
	    		notification.setLatestEventInfo(context, context.getString(R.string.app_name), context.getString(R.string.running), contentIntent);
	    		nm.notify(1234, notification);
	    		/**
	    		 * 1.4 업데이트 : 긴급 패치
	    		 * Min값이 250 아래이면 심각한 에러이므로 에러 표시를 한다
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
	 * onCreate메소드는 서비스가 시작되면 자동으로 호출되는 메소드 입니다
	 */
	public void onCreate() {
		super.onCreate();
		Log.d("내폰내놔 서비스", "시작");
		showNotify(this, 1);
		
//		if(MainActivity.Boot_Service_check!=1){
//			/**
//			 * 1.5 업데이트
//			 * 서비스에서 브로드캐스트를 관장하지 않습니다 에서
//			 * 부팅시에(브로드캐스트리시버에서 서비스를 시작하므로 메인액티비티에 있는 Boot_Service_check는 1이 아닙니다)
//			 * 리시버가 자동 실행되도록 설정
//			 */
//	        // 수신을 받기위한 인텐트필터를 생성합니다.
//	        filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
//	        filter.addAction(Intent.ACTION_SCREEN_OFF);
//	        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
//	        registerReceiver(myReceiver, filter);
//		}
		
		// SharedPreferences을 얻는 과정
		pref = getSharedPreferences("prefs", 0);
		
		// 화면이 켜져 있을때만 작동하도록 하는 PowerManager
		// 시스탬 서비스로 존재하므로 서비스를 얻음
		mPm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		// 기기 화면을 잠그기 위한 작업
	    DeviceManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);

	    /**
		 * 센서관련 코드들
		 */
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		if (accelerormeterSensor != null)
			sensorManager.registerListener(this, accelerormeterSensor,
					SensorManager.SENSOR_DELAY_GAME);
		// 위 if아래 구문은 두줄처럼 보이지만 실제로는 엔터를 누른 한줄이다
		
		/**
		 * 메인 액티비티에서 두개의 값을 가져옵니다
		 */
		//Save_Min = MainActivity.MinSenserInt;
		//Save_Delay = MainActivity.DelayInt;
		
		/**
		 * 1.1 업데이트
		 * 메인 액티비티에서 가져오지 않고 저장한 값에서 얻어옵니다
		 * 서비스가 실행중일경우 어플을 실행하면 이 값에서 얻어옵니다
		 */
		Save_Min = pref.getInt("MinSenser", 1000);
		Save_Delay = pref.getInt("Delay", 1);
		
		/**
		 * 쓰래드를 돌립니다
		 * 사실 쓰래드를 이용하는 이유는 센서를 게속 돌리면 과부하가 엄청난대
		 * 센서값 확인의 쉼을 위해서 사용합니다
		 * 
		 * 1.1 업데이트:쓰래드를 사용하지 않겠습니다
		 */
//		thread = new Thread(this);
//		thread.start();
	}
	
	/**
	 * implements Runnable에서 필요하지만 나에겐 필요없는 메소드
	 * 지울수도 없다
	 */
	@Override
	public IBinder onBind(Intent arg0) { return null; }
	
	/**
	 * 쓰래드가 시작되면 호출되는 메소드
	 * 지금은 할일이 없다
	 * 
	 * 1.1 업데이트:쓰래드를 사용하지 않음으로 run메소드가 필요 없습니다
	 */
//	@Override
//	public void run() {
//		// TODO Auto-generated method stub
//	}
	
	/**
	 * 서비스가 종료될때 마지막으로 호출되는 메소드
	 * 센서를 종료하고 서비스 종료 토스트 메세지를 띄웁니다
	 */
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		/**
		 * 센서 값 확인을 중단합니다
		 */
		if (sensorManager != null)
			sensorManager.unregisterListener(this);
		
//		unregisterReceiver(myReceiver);
//      Log.d("브로드캐스트", "중지");
		
		DeleteNotify(this);
		Toast.makeText(this, "내폰내놔 서비스가 종료되었습니다", Toast.LENGTH_LONG).show();
		Log.d("내폰내놔 서비스", "종료");
	}
	
	/**
	 * implements SensorEventListener에서 필요한 메소드
	 * 정작 나도 이 메소드가 뭐하는지는 잘 모르겠다
	 * 아는건 이 메소드는 필요없는 메소드라는거
	 * 하지만 지울수는 없다
	 * 
	 * 정확도 태스트 라고 한다
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
				 * 1.4 업데이트 : 긴급 패치
				 * Min값이 250아래로 떨어질경우 화면을 키자마자 꺼져버리므로 에러상황이다
				 * 그러므로 작동하지 않도록 처리한다
				 */
				if (Save_Min < 250){
					Log.e("내폰과 대화 서비스", "Min값이 250아래로 떨어졌습니다");
					Log.e("내폰과 대화 서비스", "아주 심각합니다 서비스를 종료합니다");
					Log.e("내폰과 대화 서비스", "Save_Min값 : "+Save_Min);
					if (sensorManager != null)
						sensorManager.unregisterListener(this);
					DeleteNotify(this);
					showNotify(this, 2);
					onDestroy();
					/**
					 * 1.5 업데이트
					 * Save_Min이 250아래일때, 서비스까지 완전히 종료하도록 소스 개선
					 */
					Intent myIntent = new Intent(getBaseContext(), GiveMePhoneService.class);
					stopService(myIntent);
				}else if (speed > Save_Min) {
					// 센서 가속도가 최소 값보다 크다!
					Log.d("가속도 센서", "Speed :"+speed+" 감도: "+Save_Min);
					
					/**
					 * 1.2 업데이트
					 * 진동 설정 가능
					 */
					if(pref.getInt("Vibrator", 0)==1){ // 진동을 활성화 했을경우
						Vibrator vide = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
						/**
						 * 1.1 업데이트
						 * 진동 시간을 대폭 축소
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
	// 센서 끝
	
}