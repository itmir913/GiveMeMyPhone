package com.leejonghwan.givememyphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

public class BroadCast extends BroadcastReceiver {
	SharedPreferences pref;
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
		pref = context.getSharedPreferences("prefs", 0);
        
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
			if(pref.getInt("boot", 0) == 1){
				/**
				 * 1.4 ������Ʈ : ��� ��ġ
				 * ������ 20���� ������ �ΰ� �����Ѵ�
				 * �ֳ��ϸ� ������ Min���� �۾�����(0���� ����) ȭ���� Ű�ڸ��� ������ �ɰ��� ���װ� �ֱ� �����̴�
				 */
	        	try { Thread.sleep(20000); } catch (InterruptedException e) {}
            	context.startService(new Intent(context, GiveMePhoneService.class));
            	
                Log.d("��ε�ĳ��Ʈ", "������ �Ϸ�Ǿ����ϴ� ���񽺸� �����մϴ�");
        	}
        } else 
        	if (intent.getAction().equals("android.intent.action.SCREEN_OFF")) {
        	while(MainActivity.isServiceRunningCheck(context)){
            	context.stopService(new Intent(context, GiveMePhoneService.class));
                Log.d("��ε�ĳ��Ʈ", "ȭ���� �������Ƿ� ���� ����");
        	}
        } else if (intent.getAction().equals("android.intent.action.SCREEN_ON")) {
        	try { Thread.sleep(pref.getInt("Delay", 1)*1000); } catch (InterruptedException e) {}
        	while(!MainActivity.isServiceRunningCheck(context)){
            	context.startService(new Intent(context, GiveMePhoneService.class));
                Log.d("��ε�ĳ��Ʈ", "ȭ���� �������ϴ� ���񽺸� �����մϴ�");
        	}
        }
        /**
         * ��ε�ĳ��Ʈ�� �̿��ϴ� ������ ȭ���� ������ ����
         * ������ �۵��� �ʿ� �����Ƿ� ���񽺸� ���� �ڿ��� ���� ��������
         * ����Ǿ����ϴ�
         * 
         * ���� �ٸ� ����� ������ ���ö��� �̿� ���������� ��˴ϴ�
         */
	}
}
