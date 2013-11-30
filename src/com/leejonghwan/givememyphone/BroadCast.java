package com.leejonghwan.givememyphone;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BroadCast extends BroadcastReceiver {
//	SharedPreferences pref;
//	Thread thread;

	/**
	 * ��ε� ĳ��Ʈ,java 1.1������Ʈ ����
	 * 1. while������ ���񽺰� ����ɶ����� while���� �����
	 * 2. Intent myIntent�� context.startService���� �����Ͽ� �����ϰ� �ڵ� ����
	 * 3. ȭ�� Ų���� �����ð��� �ε��� ����
	 * 
	 * 4. ���尪(SharedPreferences)�� �̿��ؼ� ȭ�� ���� �����̸� ���������� ����
	 * 5. ���ý� �ڵ����� ����
	 * 
	 * 1.3 ������Ʈ
	 * Thread thread;������ ����� thread.sleep�� Thread.sleep���� ����
	 */
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		SharedPreferences pref = context.getSharedPreferences("prefs", 0);
        
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
			if(pref.getBoolean("boot", true)){
				
				DevicePolicyManager devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
				ComponentName adminComponent = new ComponentName(context, AdminReceiver.class);
				
				if (devicePolicyManager.isAdminActive(adminComponent)){
					/**
					 * 1.4 ������Ʈ : ��� ��ġ
					 * ������ 20���� ������ �ΰ� �����Ѵ�
					 * �ֳ��ϸ� ������ Min���� �۾�����(0���� ����) ȭ���� Ű�ڸ��� ������ �ɰ��� ���װ� �ֱ� �����̴�
					 */
		        	try { Thread.sleep(20000); } catch (InterruptedException e) {}
	            	context.startService(new Intent(context, GiveMePhoneService.class));
	            	context.startService(new Intent(context, ScreenService.class));
	            	
	                Log.d("��ε�ĳ��Ʈ", "������ �Ϸ�Ǿ����ϴ� ���񽺸� �����մϴ�");
				}
        	}
        } else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
        	while(isServiceRunningCheck(context)){
            	context.stopService(new Intent(context, GiveMePhoneService.class));
                Log.d("��ε�ĳ��Ʈ", "ȭ���� �������Ƿ� ���� ����");
        	}
        } else if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
        	try { Thread.sleep(pref.getInt("Delay", 1)*1000); } catch (InterruptedException e) {}
        	while(!isServiceRunningCheck(context)){
            	context.startService(new Intent(context, GiveMePhoneService.class));
                Log.d("��ε�ĳ��Ʈ", "ȭ���� �������ϴ� ���񽺸� �����մϴ�");
        	}
        }else if(intent.getAction().equals("ACTION_SERVICE_RESTART")){
        	context.startService(new Intent(context, ScreenService.class));
        	Log.d("��ε�ĳ��Ʈ", "���񽺰� �������� ���ѵ� �մϴ� ����� �մϴ�");
        }
        /**
         * ��ε�ĳ��Ʈ�� �̿��ϴ� ������ ȭ���� ������ ����
         * ������ �۵��� �ʿ� �����Ƿ� ���񽺸� ���� �ڿ��� ���� ��������
         * ����Ǿ����ϴ�
         * 
         * ���� �ٸ� ����� ������ ���ö��� �̿� ���������� ��˴ϴ�
         */
		System.gc();
	}
	
	public boolean isServiceRunningCheck(Context context) {
    	ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    	for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
    	    if ("com.leejonghwan.givememyphone.GiveMePhoneService".equals(service.service.getClassName()))
    	        return true;
    	/** 
    	 * 1.1 ������Ʈ
    	 * �ڵ��� ����ȭ : ������� { �� }�� ����� ����
    	 */
    	return false;
    }
}
