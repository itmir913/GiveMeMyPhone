package com.leejonghwan.givememyphone;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	/**
	 * 2.0 ������Ʈ
	 * ��Ƽ���� UI�� ��� �����Ͽ� �����ϰ�, ���� UI�� ����
	 * UI���� �۾����� ���� ������� ��Ƽ��Ƽ : MainActivity.java, SettingActivity.java
	 * ���ŵ� ��Ƽ��Ƽ : PassWord_Make.java
	 * ��� �߰��� �߰��� ��Ƽ��Ƽ : ScreenService.java
	 */
	
	/**
	 * 1.1 ������Ʈ
	 * �������� �����Ҽ� �ִ� SharedPreferences�� ����Ͽ� ����
	 * SharedPreferences�� �̿��ؼ� ���� ���Ҷ����� �ǽð����� ���� ���� �����ϰ�,
	 * ���񽺿� ��ε�ĳ��Ʈ���ù������� ������ ���� ���ͼ� ������ �����߽��ϴ�
	 */
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	
	/**
	 * ������ ������ ������� �ڵ�
	 */
	DevicePolicyManager devicePolicyManager;
	ComponentName adminComponent;
	
	Button start_Btn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		pref = getSharedPreferences("preference", 0);
		editor = pref.edit();
		
		if (pref.getBoolean("welcome", true)){
			editor.putBoolean("Notification", true);
			editor.putInt("MinSenser", 1000);
			editor.putInt("Delay", 1);
			
			editor.putBoolean("welcome", false).commit();
			startActivity(new Intent(this, Welcome.class));
		}
		
		boolean unlocked_password = getIntent().getBooleanExtra("PassWord_Enable", false);
        if(!unlocked_password)
            if(pref.getBoolean("password_enable", false)){
                startActivity(new Intent(this, PassWord.class));
                finish();
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
				editor.putInt("update_code", code).commit();
				
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
		
		start_Btn = (Button) findViewById(R.id.service);
		
		/**
		 * 2.1.1 ������Ʈ
		 * ���񽺰� ����� ���¿��� ������ ������ ������ư���� �ٲ��� �ʴ� ���� �ذ�
		 */
		if(isServiceRunningCheck()){
			start_Btn.setText(R.string.stopBtn);
			start_Btn.setBackgroundResource(R.drawable.stop);
		}
	}
	
	public void start_btn(View v){
		
 	   // ������ ������ ������� �˸��� ���� ���� ������ �ߴ��մϴ�
 	   if (!devicePolicyManager.isAdminActive(adminComponent)){
			AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
			alert.setTitle(R.string.alert);
			alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
					intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
					intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
							getString(R.string.device_admin));
					startActivityForResult(intent, 1);
				}
			});
			alert.setMessage(R.string.alert_message);
			alert.show();
			
		}else{ // ������ ������ ������� else�� ����˴ϴ�
			
			int MinSenserInt = pref.getInt("MinSenser", 0);
//			int DelayInt = pref.getInt("Delay", 0);
			
//			Intent myIntent = new Intent(this, GiveMePhoneService.class);
			
			/**
			 * ��ε�ĳ��Ʈ���ù� ������ �ޱ����� ����Ʈ���͸� �����մϴ�.
			 */
//			IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
//			filter.addAction(Intent.ACTION_SCREEN_OFF);
//			filter.addAction(Intent.ACTION_BOOT_COMPLETED);
			
			/**
		     * 1.1 ������Ʈ
		     * MinSenserInt�� 250�Ʒ��� �ƴҰ�� if�� true�� �Ǽ� ������ �����ϴ�
		     */
			
			if(!(MinSenserInt<=250))
			 	   if(isServiceRunningCheck()) { // ���񽺰� ���ư��� �ִ� �����ϰ�� ���񽺸� �ߴ��մϴ� isServiceRunningCheck()�� �Ʒ��� ���ǵǾ� �ֽ��ϴ�
			 		  /**
			 		    * 1.1 ������Ʈ ���� �Ƚ�
			 		    * ����ϰ� unregisterReceiver(myReceiver);���� �������� ������ �߹Ƿ�
			 		    * try-catch�� �����
			 		    */
//			 		  try {
//					      unregisterReceiver(myReceiver);
//					      Log.d("��ε�ĳ��Ʈ", "����"); } catch (Exception e) { }
			 		   
			 		  stopService(new Intent(this, GiveMePhoneService.class));
			 		  stopService(new Intent(this, ScreenService.class));
			 		  
			 		  editor.remove("Service_Running").commit();
			 		  
			 		  start_Btn.setText(R.string.serviceBtn);
			 		  start_Btn.setBackgroundResource(R.drawable.start);
			 	  }else{ // ���񽺰� ���ư��� �ʴ� �����ϰ�� ���񽺸� �����մϴ�
//			 		  registerReceiver(myReceiver, filter);
//			 	      Log.d("��ε�ĳ��Ʈ", "����");
			 		  
			 		  startService(new Intent(this, GiveMePhoneService.class));
			 		  startService(new Intent(this, ScreenService.class));
			 		  
			 		  editor.putBoolean("Service_Running", true).commit();
			 		  
				      start_Btn.setText(R.string.stopBtn);
				      start_Btn.setBackgroundResource(R.drawable.stop);
				      
//				      Toast.makeText(this, R.string.serviceBtn, Toast.LENGTH_SHORT).show();
				  }
			else{
				Toast.makeText(this, R.string.service_not_start, Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	public void setting_btn(View v){
		startActivity(new Intent(this, SettingActivity.class));
	}
	
	/**
	 * 2.0 ������Ʈ
	 * ���� ���� ��ư�� �����Ͽ� ������ �ִ� �е��� ���
	 * ���񽺰� �������̸� ���Ű� �Ұ����ϰ� �Ͽ��� ������ ���� ��ҿ� �Բ� ���� ���� ���� �̷�� ����
	 */
	public void del_btn(View v){
		if(!isServiceRunningCheck()){
			if (devicePolicyManager.isAdminActive(adminComponent))
				devicePolicyManager.removeActiveAdmin(adminComponent);
			
			Uri uri = Uri.fromParts("package", "com.leejonghwan.givememyphone", null);    
			Intent it = new Intent(Intent.ACTION_DELETE, uri);    
			startActivity(it);
		}else{
			Toast.makeText(this, R.string.Not_Install, Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * ���񽺰� ����Ǿ����� Ȯ���ϴ� �޼ҵ��
	 * �������̸� true, ������ �ȵǾ� ������ false�� ��ȯ�ϴ� �޼ҵ� �Դϴ�
	 * 
	 * 1.1������Ʈ
	 * ��ε�ĳ��Ʈ���ù����� ������ ���� static���� ���� ����
	 */
	 boolean isServiceRunningCheck() {
	    	ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
	    	for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
	    	    if ("com.leejonghwan.givememyphone.GiveMePhoneService".equals(service.service.getClassName()))
	    	        return true;
	    	/** 
	    	 * 1.1 ������Ʈ
	    	 * �ڵ��� ����ȭ : ������� { �� }�� ����� ����
	    	 */
	    	return false;
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
  
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	System.gc();
        	finish();
        	return true;
        }
        return super.onKeyDown(keyCode, event);
    }
	
}
