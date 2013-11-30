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
	 * 1.1 ������Ʈ
	 * �������� �����Ҽ� �ִ� SharedPreferences�� ����Ͽ� ����
	 * SharedPreferences�� �̿��ؼ� ���� ���Ҷ����� �ǽð����� ���� ���� �����ϰ�,
	 * ���񽺿� ��ε�ĳ��Ʈ���ù������� ������ ���� ���ͼ� ������ �����߽��ϴ�
	 */
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	
	/**
	 * ���� ��Ƽ��Ƽ���� ��ε�ĳ��Ʈ���ù��� ȣ���ϰ�
	 * (���ÿ� ���񽺵� �����մϴ� Ű�ڸ��� �۵��ؾ� �ϴ�)
	 * ��ε�ĳ��Ʈ ���ù��� ȭ�� ������ �����ϸ� ���񽺴� �ߴܵǰ�,
	 * ȭ�� ������ �����Ǹ� ���񽺰� ����� �˴ϴ�
	 */
	static BroadcastReceiver myReceiver = new BroadCast();
	static IntentFilter filter;
	
	/**
	 * �⺻ ���� ������ �δ� ������
	 */
	static int MinSenserInt=1000, DelayInt=1;
	
	/**
	 * ���� ������ �����ϰ� �ִ�
	 */
	Intent myIntent;
	SeekBar MinSenser, Delay;
	TextView MinText, test_status, DelayText;
	Button service, Test, plus, Minus;
	static CheckBox bootable, Vibrator, password_enable;
	
	/**
	 * ������ ������ ������� �ڵ�
	 */
	public static DevicePolicyManager devicePolicyManager;
	public static ComponentName adminComponent;
	
	/**
	 * �������� �ڵ�� (�׽�Ʈ�� ���� ����)
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
		 * 1.6 ������Ʈ
		 * ������Ʈ���� �α׸� ǥ���ϵ��� ����
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
		 * ������ ������ �������, ���� ����â�� ǥ���մϴ�
		 */
		if (!devicePolicyManager.isAdminActive(adminComponent)){
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
			intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
					getString(R.string.device_admin));
			startActivityForResult(intent, 1);
		}
		
		/**
		 * ��ε�ĳ��Ʈ���ù� ������ �ޱ����� ����Ʈ���͸� �����մϴ�.
		 */
        filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        
        /**
         * ���� ������ ���� ����Ʈ
         */
		myIntent = new Intent(getBaseContext(), GiveMePhoneService.class);
		
		/**
		 * ���� ��ü�� id���� �����մϴ�
		 */
		service = (Button) findViewById(R.id.service);
		Test = (Button) findViewById(R.id.Test);
		plus = (Button) findViewById(R.id.Plus);
		Minus = (Button) findViewById(R.id.Minus);
		bootable = (CheckBox) findViewById(R.id.bootable);
		Vibrator = (CheckBox) findViewById(R.id.Vibrator);
		password_enable = (CheckBox) findViewById(R.id.password_enable);
		
		if(pref.getInt("boot", 0)==1) // ���ý� �ڵ������� �����Ǿ� ������ üũ��
			bootable.setChecked(true);
		if(pref.getInt("Vibrator", 0)==1) // ���ý� �ڵ������� �����Ǿ� ������ üũ��
			Vibrator.setChecked(true);
		if(pref.getInt("password_check", 0)==1){
			password_enable.setChecked(true);
			startActivity(new Intent(this, PassWord.class));
		}
		bootable.setOnCheckedChangeListener(this);
		Vibrator.setOnCheckedChangeListener(this);
		password_enable.setOnCheckedChangeListener(this);
		
		/**
		 * ���� ����, �ߴ� ��ư ������ �κ��Դϴ�
		 */
		service.setOnClickListener(new View.OnClickListener() {
		       public void onClick(View v) {
		    	   // ������ ������ ������� �˸��� ���� ���� ������ �ߴ��մϴ�
		    	   if (!devicePolicyManager.isAdminActive(adminComponent)){
		   			AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
		   			alert.setTitle(R.string.alert);
		   			alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
		   				@Override
		   				public void onClick(DialogInterface dialog, int which) {
		   					dialog.dismiss();
		   					finishMothod(); // ����� �޴��� �ߺ��Ǵ� �κ��̹Ƿ� �޼ҵ�� �ߺ� �ڵ带 �ٿ����ϴ�
		   					}
		   				});
		   			alert.setMessage(R.string.alert_message);
		   			alert.show();
		   			
		   		}else{ // ������ ������ ������� else�� ����˴ϴ�

			    /**
			     * 1.1 ������Ʈ
			     * MinSenserInt�� 250�Ʒ��� �ƴҰ�� if�� true�� �Ǽ� ������ �����ϴ�
			     */
		   		 if(!(MinSenserInt<=250))
		    	   if(isServiceRunningCheck(getBaseContext())) { // ���񽺰� ���ư��� �ִ� �����ϰ�� ���񽺸� �ߴ��մϴ� isServiceRunningCheck()�� �Ʒ��� ���ǵǾ� �ֽ��ϴ�
		    		   Test.setEnabled(true);
		    		   MinSenser.setEnabled(true);
		    		   plus.setEnabled(true);
		    		   Minus.setEnabled(true);
		    		   Delay.setEnabled(true);
		    		   /**
		    		    * 1.1 ������Ʈ ���� �Ƚ�
		    		    * ����ϰ� unregisterReceiver(myReceiver);���� �������� ������ �߹Ƿ�
		    		    * try-catch�� �����
		    		    */
		    		   try {
				       unregisterReceiver(myReceiver);
				       Log.d("��ε�ĳ��Ʈ", "����"); } catch (Exception e) { }
			           stopService(myIntent);
			           service.setText(R.string.serviceBtn);
			           Log.d("��ưŬ��", "������, ���۹�ư���� ����");
		    	   }else{ // ���񽺰� ���ư��� �ʴ� �����ϰ�� ���񽺸� �����մϴ�
		    		   Test.setEnabled(false);
		    		   MinSenser.setEnabled(false);
		    		   plus.setEnabled(false);
		    		   Minus.setEnabled(false);
		    		   Delay.setEnabled(false);
		    		   
		    		   registerReceiver(myReceiver, filter);
		    	       Log.d("��ε�ĳ��Ʈ", "����");
		    		   startService(myIntent);
			           Toast.makeText(MainActivity.this, getString(R.string.serviceBtn)+"!", Toast.LENGTH_SHORT).show();
			           service.setText(R.string.stopBtn);
			           Log.d("��ưŬ��", "���۵�, ������ư���� ����");
		    	   }
		   	    }
		    }
		});
		
		/**
		 * �׽�Ʈ ��ư�� ���� ������ �Դϴ�
		 */
		Test.setOnClickListener(new View.OnClickListener() {
		       public void onClick(View v) {
		    	   /**
		    	    * 1.1 ������Ʈ
		    	    * MinSenserInt�� 250�Ʒ��� �ƴҰ�� if�� true�� �Ǽ� ������ �����ϴ�
		    	    */
			   		 if(!(MinSenserInt<=250)){
		    	   /**
		    	    * �׽�Ʈ ���� �ؽ�Ʈ ����
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
		    	    * �������� �ڵ��
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
		 * �������� �������� ���� �����մϴ�
		 * ���� �����Ȱ��� ������� �⺻�� 1000�Ǵ� 1���� �����մϴ�
		 */
		MinSenser.setProgress(pref.getInt("MinSenser", 1000));
		MinSenserInt = pref.getInt("MinSenser", 1000);
		
		Delay.setProgress(pref.getInt("Delay", 1));
		DelayInt = pref.getInt("Delay", 1);
		
		/**
		 * ���񽺰� �̹� ����Ǿ� ������� �̹� ����Ǿ� �ִ� ��츦 �����մϴ�
		 */
		if(isServiceRunningCheck(this)) {
			Log.d("����", "���񽺰� �̹� ����Ǿ����ϴ�");
			
			// ���񽺿� ����� �������� ��� �ҷ���� �����մϴ�
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
	 * ���񽺰� ����Ǿ����� Ȯ���ϴ� �޼ҵ��
	 * �������̸� true, ������ �ȵǾ� ������ false�� ��ȯ�ϴ� �޼ҵ� �Դϴ�
	 * 
	 * 1.1������Ʈ
	 * ��ε�ĳ��Ʈ���ù����� ������ ���� static���� ���� ����
	 */
	 public static boolean isServiceRunningCheck(Context context) {
	    	ActivityManager manager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);
	    	for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
	    	    if ("com.leejonghwan.givememyphone.GiveMePhoneService".equals(service.service.getClassName()))
	    	        return true;
	    	/** 
	    	 * 1.1 ������Ʈ
	    	 * �ڵ��� ����ȭ : ������� { �� }�� ����� ����
	    	 */
	    	return false;
	}
	
	/**
	 * �� �޼ҵ���� ���α׷������� ���� �ϳ� �ø��� ��ư�� ����� �޼ҵ� �Դϴ�
	 */
	public void Plus(View v){
 	   if(!isServiceRunningCheck(this)) {
		MinSenser.setProgress(++MinSenserInt);
		save_min(); // �ߺ��Ǵ� ����̹Ƿ� �޼ҵ�� �и��� �ڵ��� �ߺ��� �����ϴ�
 	   }
	}
	
	public void Minus(View v){
	 if(!isServiceRunningCheck(this)) {
		MinSenser.setProgress(--MinSenserInt);
		save_min();
	 }
	}
	
	/**
	 * �ɼǸ޴����� �޼ҵ��Դϴ�
	 * �� ������ �޴�Ű�� ���� ������ ������ ����� ����� �����ϰ� �ֽ��ϴ�
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
	 * ���α׷����ٰ� ����Ǿ������ ����Ǵ� �������Դϴ�
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
			 * 1.1 ������Ʈ
			 * ���� 250�Ʒ��� �����Ѱ�� ���ڻ��� ���������� �ٲٰ� ����Ҽ� ���ٴ� �ؽ�Ʈ�� ǥ����
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
					// ���� ���ӵ��� �ּ� ������ ũ��!
					Vibrator vide = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					vide.vibrate(500);
					
					/**
					 * �׽�Ʈ�� �ѹ��̸� �ǹǷ� �����ʿ�, ��Ȱ��ȭ �ߴ� �͵��� ���󺹱���
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
			   		 * 1.2 ������Ʈ
			   		 * ������ ��ũ�ٿ� üũ�ڽ��� ��Ȱ��ȭ ���ε� �߰�
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
	 * üũ�ڽ��� ����ɶ����� ȣ��Ǵ� �޼ҵ�
	 */
	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
		// TODO Auto-generated method stub
		switch (arg0.getId()){
		case R.id.bootable:
			if(bootable.isChecked()){
				editor.putInt("boot", 1);
				Toast.makeText(this, "���� �ڵ� ���� üũ��", Toast.LENGTH_SHORT).show();
			}else{
				editor.putInt("boot", 0);
				Toast.makeText(this, "���� �ڵ� ���� ��Ȱ��ȭ", Toast.LENGTH_SHORT).show();
			}
			break;
			
		case R.id.Vibrator:
			if(Vibrator.isChecked()){
				editor.putInt("Vibrator", 1);
				Toast.makeText(this, "���� ����", Toast.LENGTH_SHORT).show();
			}else{
				editor.putInt("Vibrator", 0);
				Toast.makeText(this, "���� ����", Toast.LENGTH_SHORT).show();
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
