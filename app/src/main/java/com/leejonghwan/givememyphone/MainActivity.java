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
     * 2.0 업데이트
     * 촌티나는 UI를 모두 변경하여 심플하고, 편리한 UI로 변경
     * UI변경 작업으로 새로 만들어진 액티비티 : MainActivity.java, SettingActivity.java
     * 제거된 액티비티 : PassWord_Make.java
     * 기능 추가로 추가된 액티비티 : ScreenService.java
     */

    /**
     * 1.1 업데이트
     * 설정값을 저장할수 있는 SharedPreferences을 사용하여 구현
     * SharedPreferences을 이용해서 값이 변할때마다 실시간으로 변한 값을 저장하고,
     * 서비스와 브로드캐스트리시버에서는 저장한 값을 얻어와서 구현에 성공했습니다
     */
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    /**
     * 관리자 권한을 얻기위한 코드
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
            editor.putBoolean("welcome", false).commit();
            startActivity(new Intent(this, Welcome.class));
        }

//        boolean unlocked_password = getIntent().getBooleanExtra("PassWord_Enable", false);
//        if(!unlocked_password)
//            if(pref.getBoolean("password_enable", false)){
//                startActivity(new Intent(this, PassWord.class));
//                finish();
//            }

        /**
         * 1.6 업데이트
         * 업데이트마다 로그를 표시하도록 설정
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
         * 관리자 권한이 없을경우, 권한 승인창을 표시합니다
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
         * 2.1.1 업데이트
         * 서비스가 실행된 상태에서 어플을 켰을때 중지버튼으로 바뀌지 않던 오류 해결
         */
        if(isServiceRunningCheck()){
            start_Btn.setText(R.string.stopBtn);
            start_Btn.setBackgroundResource(R.drawable.stop);
        }
    }

    public void start_btn(View v){

        // 관리자 권한이 없을경우 알림을 띄우고 서비스 실행을 중단합니다
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

        }else{ // 관리자 권한이 있을경우 else가 실행됩니다

            int MinSenserInt = pref.getInt("MinSenser", 1000);
//			int DelayInt = pref.getInt("Delay", 0);

//			Intent myIntent = new Intent(this, GiveMePhoneService.class);

            /**
             * 브로드캐스트리시버 수신을 받기위한 인텐트필터를 생성합니다.
             */
//			IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
//			filter.addAction(Intent.ACTION_SCREEN_OFF);
//			filter.addAction(Intent.ACTION_BOOT_COMPLETED);

            /**
             * 1.1 업데이트
             * MinSenserInt이 250아래가 아닐경우 if가 true가 되서 진행이 가능하다
             */

            if(!(MinSenserInt<=250))
                if(isServiceRunningCheck()) { // 서비스가 돌아가고 있는 상태일경우 서비스를 중단합니다 isServiceRunningCheck()는 아래에 정의되어 있습니다
                    /**
                     * 1.1 업데이트 버그 픽스
                     * 희안하게 unregisterReceiver(myReceiver);에서 강제종료 오류가 뜨므로
                     * try-catch로 잡아줌
                     */
//			 		  try {
//					      unregisterReceiver(myReceiver);
//					      Log.d("브로드캐스트", "중지"); } catch (Exception e) { }

                    stopService(new Intent(this, GiveMePhoneService.class));
                    stopService(new Intent(this, ScreenService.class));

                    editor.remove("Service_Running").commit();

                    start_Btn.setText(R.string.serviceBtn);
                    start_Btn.setBackgroundResource(R.drawable.start);

                    if (devicePolicyManager.isAdminActive(adminComponent))
                        devicePolicyManager.removeActiveAdmin(adminComponent);
                }else{ // 서비스가 돌아가지 않는 상태일경우 서비스를 실행합니다
//			 		  registerReceiver(myReceiver, filter);
//			 	      Log.d("브로드캐스트", "시작");

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
     * 2.0 업데이트
     * 어플 제거 버튼을 내장하여 난독증 있는 분들을 배려
     * 서비스가 실행중이면 제거가 불가능하게 하였고 관리자 권한 취소와 함께 어플 제거 까지 이루어 진다
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
     * 서비스가 실행되었는지 확인하는 메소드로
     * 실행중이면 true, 실행이 안되어 있으면 false를 반환하는 메소드 입니다
     *
     * 1.1업데이트
     * 브로드캐스트리시버에서 참조를 위해 static으로 선언 변경
     */
    boolean isServiceRunningCheck() {
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
            if ("com.leejonghwan.givememyphone.GiveMePhoneService".equals(service.service.getClassName()))
                return true;
        /**
         * 1.1 업데이트
         * 코드의 간결화 : 쓸대없는 { 와 }의 사용을 방지
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
