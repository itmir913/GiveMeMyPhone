package com.leejonghwan.givememyphone;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity implements SensorEventListener, OnCheckedChangeListener {
	
	/**
	 * 1.1 업데이트
	 * 설정값을 저장할수 있는 SharedPreferences을 사용하여 구현
	 * SharedPreferences을 이용해서 값이 변할때마다 실시간으로 변한 값을 저장하고,
	 * 서비스와 브로드캐스트리시버에서는 저장한 값을 얻어와서 구현에 성공했습니다
	 */
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	
	/**
	 * 메인 액티비티에서 브로드캐스트리시버를 호출하고
	 * (동시에 서비스도 실행합니다 키자마자 작동해야 하니)
	 * 브로드캐스트 리시버가 화면 꺼짐을 감지하면 서비스는 중단되고,
	 * 화면 켜짐이 감지되면 서비스가 재시작 됩니다
	 */
	static BroadcastReceiver myReceiver = new BroadCast();
	static IntentFilter filter;
	
	/**
	 * 기본 값을 저장해 두는 변수들
	 */
	static int MinSenserInt=1000, DelayInt=1;
	
	/**
	 * 각종 변수를 선언하고 있다
	 */
	Intent myIntent;
	SeekBar MinSenser, Delay;
	TextView MinText, test_status, DelayText;
	Button service, Test, plus, Minus;
	static CheckBox bootable, Vibrator, password_enable;
	
	/**
	 * 관리자 권한을 얻기위한 코드
	 */
	public static DevicePolicyManager devicePolicyManager;
	public static ComponentName adminComponent;
	
	/**
	 * 센서관련 코드들 (테스트를 위한 값들)
	 */
		long lastTime;
	    float speed, lastX, lastY, lastZ, x, y, z;
		static final int DATA_X = SensorManager.DATA_X, DATA_Y = SensorManager.DATA_Y, DATA_Z = SensorManager.DATA_Z;
	 
	    SensorManager sensorManager;
	    Sensor accelerormeterSensor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		pref = getSharedPreferences("prefs", 0);
		editor = pref.edit();
		
		if (pref.getInt("welcome", 0)==0){
			editor.putInt("welcome", 1);
			editor.commit();
			startActivity(new Intent(this, Welcome.class));
		}
		
		/**
		 * 1.6 업데이트
		 * 업데이트마다 로그를 표시하도록 설정
		 */
		try {
			PackageManager packageManager = this.getPackageManager();
			PackageInfo infor =  packageManager.getPackageInfo(this.getPackageName(), PackageManager.GET_META_DATA);
		    int code = infor.versionCode;
		    
		    if (pref.getInt("update_code", 0)!=code){
				editor.putInt("update_code", code);
				editor.commit();
				
				AlertDialog.Builder alert = new AlertDialog.Builder(this);
				alert.setTitle(R.string.ok);
				alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				    @Override
				    public void onClick(DialogInterface dialog, int which) {
				    dialog.dismiss();
				    }
				});
				alert.setMessage(R.string.update_log);
				alert.show();
				
			}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		adminComponent = new ComponentName(this, AdminReceiver.class);
		devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
		
		/**
		 * 관리자 권한이 없을경우, 권한 승인창을 표시합니다
		 */
		if (!devicePolicyManager.isAdminActive(adminComponent)){
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
					getString(R.string.device_admin));
			startActivityForResult(intent, 1);
		}
		
		/**
		 * 브로드캐스트리시버 수신을 받기위한 인텐트필터를 생성합니다.
		 */
        filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        
        /**
         * 서비스 실행을 위한 인텐트
         */
		myIntent = new Intent(getBaseContext(), GiveMePhoneService.class);
		
		/**
		 * 각종 개체의 id값을 지정합니다
		 */
		service = (Button) findViewById(R.id.service);
		Test = (Button) findViewById(R.id.Test);
		plus = (Button) findViewById(R.id.Plus);
		Minus = (Button) findViewById(R.id.Minus);
		bootable = (CheckBox) findViewById(R.id.bootable);
		Vibrator = (CheckBox) findViewById(R.id.Vibrator);
		password_enable = (CheckBox) findViewById(R.id.password_enable);
		
		if(pref.getInt("boot", 0)==1) // 부팅시 자동적용이 설정되어 있으면 체크함
			bootable.setChecked(true);
		if(pref.getInt("Vibrator", 0)==1) // 부팅시 자동적용이 설정되어 있으면 체크함
			Vibrator.setChecked(true);
		if(pref.getInt("password_check", 0)==1){
			password_enable.setChecked(true);
			startActivity(new Intent(this, PassWord.class));
		}
		bootable.setOnCheckedChangeListener(this);
		Vibrator.setOnCheckedChangeListener(this);
		password_enable.setOnCheckedChangeListener(this);
		
		/**
		 * 서비스 시작, 중단 버튼 리스너 부분입니다
		 */
		service.setOnClickListener(new View.OnClickListener() {
		       public void onClick(View v) {
		    	   // 관리자 권한이 없을경우 알림을 띄우고 서비스 실행을 중단합니다
		    	   if (!devicePolicyManager.isAdminActive(adminComponent)){
		   			AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
		   			alert.setTitle(R.string.alert);
		   			alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		   				@Override
		   				public void onClick(DialogInterface dialog, int which) {
		   					dialog.dismiss();
		   					finishMothod(); // 재시작 메뉴와 중복되는 부분이므로 메소드로 중복 코드를 줄였습니다
		   					}
		   				});
		   			alert.setMessage(R.string.alert_message);
		   			alert.show();
		   			
		   		}else{ // 관리자 권한이 있을경우 else가 실행됩니다

			    /**
			     * 1.1 업데이트
			     * MinSenserInt이 250아래가 아닐경우 if가 true가 되서 진행이 가능하다
			     */
		   		 if(!(MinSenserInt<=250))
		    	   if(isServiceRunningCheck(getBaseContext())) { // 서비스가 돌아가고 있는 상태일경우 서비스를 중단합니다 isServiceRunningCheck()는 아래에 정의되어 있습니다
		    		   Test.setEnabled(true);
		    		   MinSenser.setEnabled(true);
		    		   plus.setEnabled(true);
		    		   Minus.setEnabled(true);
		    		   Delay.setEnabled(true);
		    		   /**
		    		    * 1.1 업데이트 버그 픽스
		    		    * 희안하게 unregisterReceiver(myReceiver);에서 강제종료 오류가 뜨므로
		    		    * try-catch로 잡아줌
		    		    */
		    		   try {
				       unregisterReceiver(myReceiver);
				       Log.d("브로드캐스트", "중지"); } catch (Exception e) { }
			           stopService(myIntent);
			           service.setText(R.string.serviceBtn);
			           Log.d("버튼클릭", "중지됨, 시작버튼으로 변경");
		    	   }else{ // 서비스가 돌아가지 않는 상태일경우 서비스를 실행합니다
		    		   Test.setEnabled(false);
		    		   MinSenser.setEnabled(false);
		    		   plus.setEnabled(false);
		    		   Minus.setEnabled(false);
		    		   Delay.setEnabled(false);
		    		   
		    		   registerReceiver(myReceiver, filter);
		    	       Log.d("브로드캐스트", "시작");
		    		   startService(myIntent);
			           Toast.makeText(MainActivity.this, getString(R.string.serviceBtn)+"!", Toast.LENGTH_SHORT).show();
			           service.setText(R.string.stopBtn);
			           Log.d("버튼클릭", "시작됨, 중지버튼으로 변경");
		    	   }
		   	    }
		    }
		});
		
		/**
		 * 테스트 버튼을 위한 리스너 입니다
		 */
		Test.setOnClickListener(new View.OnClickListener() {
		       public void onClick(View v) {
		    	   /**
		    	    * 1.1 업데이트
		    	    * MinSenserInt이 250아래가 아닐경우 if가 true가 되서 진행이 가능하다
		    	    */
			   		 if(!(MinSenserInt<=250)){
		    	   /**
		    	    * 테스트 관련 텍스트 설정
		    	    */
		    	   test_status.setVisibility(View.VISIBLE);
		    	   test_status.setText(R.string.now_test);
		    	   
		    	   Test.setEnabled(false);
		    	   service.setEnabled(false);
		    	   plus.setEnabled(false);
		    	   Minus.setEnabled(false);
		    	   MinSenser.setEnabled(false);
		    	   Delay.setEnabled(false);
		    	   
		    	   bootable.setEnabled(false);
		    	   Vibrator.setEnabled(false);
		    	   password_enable.setEnabled(false);
		    	   
		    	   /**
		    	    * 센서관련 코드들
		    	    */
		    	   sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		    	   accelerormeterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		    	   
		    	   if (accelerormeterSensor != null)
		    		   sensorManager.registerListener(MainActivity.this, accelerormeterSensor,
		    				   SensorManager.SENSOR_DELAY_GAME);
		    	   }
		       }});
		
		MinText = (TextView) findViewById(R.id.MinText);
		DelayText = (TextView) findViewById(R.id.DelayText);
		test_status = (TextView) findViewById(R.id.test_status);
		
		MinSenser = (SeekBar) findViewById(R.id.MinSenser);
		MinSenser.setOnSeekBarChangeListener(new MinSenserChangeListener());
		
		Delay = (SeekBar) findViewById(R.id.Delay);
		Delay.setOnSeekBarChangeListener(new DelayChangeListener());

		MinText.setText(String.format(getString(R.string.min), MinSenserInt));
		DelayText.setText(String.format(getString(R.string.Delay), DelayInt));
		
		/**
		 * 설정값을 가져오고 값을 저장합니다
		 * 만약 설정된것이 없을경우 기본값 1000또는 1으로 설정합니다
		 */
		MinSenser.setProgress(pref.getInt("MinSenser", 1000));
		MinSenserInt = pref.getInt("MinSenser", 1000);
		
		Delay.setProgress(pref.getInt("Delay", 1));
		DelayInt = pref.getInt("Delay", 1);
		
		/**
		 * 서비스가 이미 실행되어 있을경우 이미 실행되어 있는 경우를 지정합니다
		 */
		if(isServiceRunningCheck(this)) {
			Log.d("서비스", "서비스가 이미 실행되었습니다");
			
			// 서비스에 저장된 변수들을 모두 불러들어 복원합니다
			MinSenserInt = GiveMePhoneService.Save_Min;
			DelayInt = GiveMePhoneService.Save_Delay;
			
			MinSenser.setProgress(MinSenserInt);
			Delay.setProgress(DelayInt);
			
			Test.setEnabled(false);
			MinSenser.setEnabled(false);
			Delay.setEnabled(false);
			plus.setEnabled(false);
			Minus.setEnabled(false);
 		   
			service.setText(R.string.stopBtn);
			Toast.makeText(this, R.string.service_already, Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 서비스가 실행되었는지 확인하는 메소드로
	 * 실행중이면 true, 실행이 안되어 있으면 false를 반환하는 메소드 입니다
	 * 
	 * 1.1업데이트
	 * 브로드캐스트리시버에서 참조를 위해 static으로 선언 변경
	 */
	 public static boolean isServiceRunningCheck(Context context) {
	    	ActivityManager manager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
	    	for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
	    	    if ("com.leejonghwan.givememyphone.GiveMePhoneService".equals(service.service.getClassName()))
	    	        return true;
	    	/** 
	    	 * 1.1 업데이트
	    	 * 코드의 간결화 : 쓸대없는 { 와 }의 사용을 방지
	    	 */
	    	return false;
	}
	
	/**
	 * 이 메소드들은 프로그래스바의 값을 하나 늘리는 버튼에 연결된 메소드 입니다
	 */
	public void Plus(View v){
 	   if(!isServiceRunningCheck(this)) {
		MinSenser.setProgress(++MinSenserInt);
		save_min(); // 중복되는 기능이므로 메소드로 분리해 코드의 중복을 막습니다
 	   }
	}
	
	public void Minus(View v){
	 if(!isServiceRunningCheck(this)) {
		MinSenser.setProgress(--MinSenserInt);
		save_min();
	 }
	}
	
	/**
	 * 옵션메뉴관련 메소드입니다
	 * 이 어플은 메뉴키를 눌러 관리자 권한을 지우는 기능을 내장하고 있습니다
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.delete_permission:
			if (devicePolicyManager.isAdminActive(adminComponent))
				devicePolicyManager.removeActiveAdmin(adminComponent);
			break;
		case R.id.restart:
			finishMothod();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 프로그래스바가 변경되었을경우 실행되는 리스너입니다
	 */
	public class MinSenserChangeListener implements OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			MinSenserInt = progress;
			save_min();
			
			if (MinSenserInt>1400)
				MinText.setText(String.format(getString(R.string.min)+getString(R.string.not_work), progress));
			/**
			 * 1.1 업데이트
			 * 값을 250아래로 설정한경우 글자색을 빨강색으로 바꾸고 사용할수 없다는 텍스트를 표시함
			 */
			else if(MinSenserInt<=250)
				MinText.setText(Html.fromHtml("<font color='#FF0000'>"+String.format(getString(R.string.min)+getString(R.string.not_used), progress)+"</font>"));
			else if(MinSenserInt<=480)
				MinText.setText(String.format(getString(R.string.min)+getString(R.string.very_sore), progress));
			else
				MinText.setText(String.format(getString(R.string.min), progress));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
	}
	
	public class DelayChangeListener implements OnSeekBarChangeListener {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			DelayInt = progress;
			save_delay();
			if (DelayInt>6)
				DelayText.setText(String.format(getString(R.string.Delay)+getString(R.string.not_work), progress));
			else if(DelayInt<1)
				DelayText.setText(String.format(getString(R.string.Delay)+getString(R.string.very_sore), progress));
			else
				DelayText.setText(String.format(getString(R.string.Delay), progress));
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
  
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
               finish();
               return true;
        }
        return super.onKeyDown(keyCode, event);
    }
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			long currentTime = System.currentTimeMillis();
			long gabOfTime = (currentTime - lastTime);
			if (gabOfTime > 100) {
				lastTime = currentTime;
				x = event.values[SensorManager.DATA_X];
				y = event.values[SensorManager.DATA_Y];
				z = event.values[SensorManager.DATA_Z];
				
				speed = Math.abs(x + y + z - lastX - lastY - lastZ) / gabOfTime * 10000;
				
				if (speed > MinSenserInt) {
					// 센서 가속도가 최소 값보다 크다!
					Vibrator vide = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					vide.vibrate(500);
					
					/**
					 * 테스트는 한번이면 되므로 리스너와, 비활성화 했던 것들을 원상복구함
					 */
					if (sensorManager != null)
						sensorManager.unregisterListener(this);
			   		test_status.setText(R.string.test_result);
			   		Test.setEnabled(true);
			   		service.setEnabled(true);
			   		plus.setEnabled(true);
			   		Minus.setEnabled(true);
			   		MinSenser.setEnabled(true);
			   		/**
			   		 * 1.2 업데이트
			   		 * 딜레이 시크바와 체크박스의 비활성화 여부도 추가
			   		 */
		    		Delay.setEnabled(true);
		    		
		    		bootable.setEnabled(true);
		    		Vibrator.setEnabled(true);
		    		password_enable.setEnabled(true);
					}
				lastX = event.values[DATA_X];
				lastY = event.values[DATA_Y];
				lastZ = event.values[DATA_Z];
	           }
	       }
	}
	
	public void finishMothod(){
		try {
			unregisterReceiver(myReceiver);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stopService(myIntent);
		moveTaskToBack(true);
		finish();
		android.os.Process.killProcess(android.os.Process.myPid());
	}
	
	public void save_min(){
		editor.putInt("MinSenser", MinSenserInt);
		editor.commit();
	}
	
	public void save_delay(){
		editor.putInt("Delay", DelayInt);
		editor.commit();
	}

	/**
	 * 체크박스가 변경될때마다 호출되는 메소드
	 */
	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub
		switch (arg0.getId()){
		case R.id.bootable:
			if(bootable.isChecked()){
				editor.putInt("boot", 1);
				Toast.makeText(this, "부팅 자동 적용 체크됨", Toast.LENGTH_SHORT).show();
			}else{
				editor.putInt("boot", 0);
				Toast.makeText(this, "부팅 자동 적용 비활성화", Toast.LENGTH_SHORT).show();
			}
			break;
			
		case R.id.Vibrator:
			if(Vibrator.isChecked()){
				editor.putInt("Vibrator", 1);
				Toast.makeText(this, "진동 설정", Toast.LENGTH_SHORT).show();
			}else{
				editor.putInt("Vibrator", 0);
				Toast.makeText(this, "진동 해제", Toast.LENGTH_SHORT).show();
			}
			break;
			
		case R.id.password_enable:
			if(password_enable.isChecked()){
				if(pref.getInt("Password", 0)==0)
					startActivity(new Intent(this, PassWord_Make.class));
			}else{
				editor.putInt("password_check", 0);
				editor.putInt("Password", 0);
			}
			break;
		}
		editor.commit();
	}
	
}
