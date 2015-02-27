package com.leejonghwan.givememyphone;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BroadCast extends BroadcastReceiver {

	@Override
	public void onReceive(Context mContext, Intent intent) {
		SharedPreferences pref = mContext.getSharedPreferences("preference", 0);
        
		if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
			if(pref.getBoolean("boot", true)){
				
				DevicePolicyManager devicePolicyManager = (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
				ComponentName adminComponent = new ComponentName(mContext, AdminReceiver.class);
				
				if (devicePolicyManager.isAdminActive(adminComponent)){
		        	try { Thread.sleep(20000); } catch (InterruptedException e) {}
		        	mContext.startService(new Intent(mContext, GiveMePhoneService.class));
		        	mContext.startService(new Intent(mContext, ScreenService.class));
				}
        	}
        }else if(intent.getAction().equals("android.intent.action.SCREEN_OFF")){
        	while(isServiceRunningCheck(mContext)){
        		mContext.stopService(new Intent(mContext, GiveMePhoneService.class));
        	}
        }else if(intent.getAction().equals("android.intent.action.SCREEN_ON")){
        	try { Thread.sleep(pref.getInt("Delay", 1)*1000); } catch (InterruptedException e) {}
        	while(!isServiceRunningCheck(mContext)){
        		mContext.startService(new Intent(mContext, GiveMePhoneService.class));
        	}
        }else if(intent.getAction().equals("ACTION_SERVICE_RESTART")){
        	mContext.startService(new Intent(mContext, ScreenService.class));
        }
		System.gc();
	}
	
	public boolean isServiceRunningCheck(Context context) {
    	ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    	for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
    	    if ("com.leejonghwan.givememyphone.GiveMePhoneService".equals(service.service.getClassName()))
    	        return true;
    	return false;
    }
}
