package com.leejonghwan.givememyphone;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AdminReceiver extends DeviceAdminReceiver {
    /**
     * 기기관리자가 해제되었을 할 작업을 명시해 줍니다
     */
    @Override
    public void onDisabled(Context context, Intent intent) {
        Toast.makeText(context, R.string.device_admin_disabled, Toast.LENGTH_SHORT).show();
    }

    /**
     * http://hns17.tistory.com/114 이사이트를 아주 많이 참조했습니다
     */
}