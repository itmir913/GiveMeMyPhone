package com.leejonghwan.givememyphone;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AdminReceiver extends DeviceAdminReceiver {
	 /**
	  * �������ڰ� �����Ǿ����� �� �۾��� ����� �ݴϴ�
	  */
	 @Override
	 public void onDisabled(Context context, Intent intent) {
		 Toast.makeText(context, R.string.device_admin_disabled, Toast.LENGTH_SHORT).show();
	 }
	 
	 /**
	  * http://hns17.tistory.com/114 �̻���Ʈ�� ���� ���� �����߽��ϴ�
	  */
}