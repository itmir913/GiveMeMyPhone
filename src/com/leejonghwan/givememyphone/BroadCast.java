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
	 * 브로드 캐스트,java 1.1업데이트 내역
	 * 1. while문으로 서비스가 종료될때까지 while문이 실행됨
	 * 2. Intent myIntent를 context.startService문에 통합하여 간결하게 코드 유지
	 * 3. 화면 킨다음 여유시간을 두도록 설정
	 * 
	 * 4. 저장값(SharedPreferences)을 이용해서 화면 켜짐 딜레이를 성공적으로 구현
	 * 5. 부팅시 자동적용 적용
	 * 
	 * 1.3 업데이트
	 * Thread thread;구문을 지우고 thread.sleep을 Thread.sleep으로 변경
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
					 * 1.4 업데이트 : 긴급 패치
					 * 부팅후 20초의 여유를 두고 적용한다
					 * 왜냐하면 서비스의 Min값이 작아져서(0으로 예상) 화면을 키자마자 꺼지는 심각한 버그가 있기 때문이다
					 */
		        	try { Thread.sleep(20000); } catch (InterruptedException e) {}
	            	context.startService(new Intent(context, GiveMePhoneService.class));
	            	context.startService(new Intent(context, ScreenService.class));
	            	
	                Log.d("브로드캐스트", "부팅이 완료되었습니다 서비스를 시작합니다");
				}
        	}
        } else if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
        	while(isServiceRunningCheck(context)){
            	context.stopService(new Intent(context, GiveMePhoneService.class));
                Log.d("브로드캐스트", "화면이 꺼졌으므로 서비스 종료");
        	}
        } else if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
        	try { Thread.sleep(pref.getInt("Delay", 1)*1000); } catch (InterruptedException e) {}
        	while(!isServiceRunningCheck(context)){
            	context.startService(new Intent(context, GiveMePhoneService.class));
                Log.d("브로드캐스트", "화면이 켜졌습니다 서비스를 시작합니다");
        	}
        }else if(intent.getAction().equals("ACTION_SERVICE_RESTART")){
        	context.startService(new Intent(context, ScreenService.class));
        	Log.d("브로드캐스트", "서비스가 강제종료 당한듯 합니다 재시작 합니다");
        }
        /**
         * 브로드캐스트를 이용하는 이유는 화면이 꺼졌을 경우는
         * 센서의 작동이 필요 없으므로 서비스를 꺼서 자원의 낭비를 막기위해
         * 설계되었습니다
         * 
         * 또한 다른 사람의 공격이 들어올때도 이와 마찬가지로 운영됩니다
         */
		System.gc();
	}
	
	public boolean isServiceRunningCheck(Context context) {
    	ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    	for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
    	    if ("com.leejonghwan.givememyphone.GiveMePhoneService".equals(service.service.getClassName()))
    	        return true;
    	/** 
    	 * 1.1 업데이트
    	 * 코드의 간결화 : 쓸대없는 { 와 }의 사용을 방지
    	 */
    	return false;
    }
}
