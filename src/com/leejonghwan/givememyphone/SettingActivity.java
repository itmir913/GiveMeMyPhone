package com.leejonghwan.givememyphone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends Activity {
	
	/**
	 * 1.1 ������Ʈ
	 * �������� �����Ҽ� �ִ� SharedPreferences�� ����Ͽ� ����
	 * SharedPreferences�� �̿��ؼ� ���� ���Ҷ����� �ǽð����� ���� ���� �����ϰ�,
	 * ���񽺿� ��ε�ĳ��Ʈ���ù������� ������ ���� ���ͼ� ������ �����߽��ϴ�
	 */
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	
	SeekBar MinSenser, Delay;
	TextView MinText, DelayText, madeby;
	CheckBox bootable, Vibrator, password_enable, notification, icon_clear;

	@SuppressLint("CommitPrefEdits")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		pref = getSharedPreferences("preference", 0);
		editor = pref.edit();
		
		bootable = (CheckBox) findViewById(R.id.bootable);
		Vibrator = (CheckBox) findViewById(R.id.Vibrator);
		password_enable = (CheckBox) findViewById(R.id.password_enable);
		notification = (CheckBox) findViewById(R.id.notification);
		icon_clear = (CheckBox) findViewById(R.id.notification_clear_icon);
		
		MinText = (TextView) findViewById(R.id.MinText);
		DelayText = (TextView) findViewById(R.id.DelayText);
		
		Delay = (SeekBar) findViewById(R.id.Delay);
		MinSenser = (SeekBar) findViewById(R.id.MinSenser);
		
		if(pref.getBoolean("boot", true)) // ���ý� �ڵ������� �����Ǿ� ������ üũ��
			bootable.setChecked(true);
		if(pref.getBoolean("Vibrator", false))
			Vibrator.setChecked(true);
		if(pref.getBoolean("password_enable", false))
			password_enable.setChecked(true);
		if(pref.getBoolean("notification", false)){
			notification.setChecked(true);
			icon_clear.setVisibility(View.VISIBLE);
			if(pref.getBoolean("clear_icon", false))
				icon_clear.setChecked(true);
		}
		
		MinText.setText(String.format(getString(R.string.min), pref.getInt("MinSenser", 1000)));
		DelayText.setText(String.format(getString(R.string.Delay), pref.getInt("Delay", 1)));
		
		MinSenser.setProgress(pref.getInt("MinSenser", 1000));
		Delay.setProgress(pref.getInt("Delay", 1));
		
		CheckBoxListener();
		SeekBarListener();
		
		if(isServiceRunningCheck(this)) {
			MinSenser.setEnabled(false);
			Delay.setEnabled(false);
			bootable.setEnabled(false);
			Vibrator.setEnabled(false);
			password_enable.setEnabled(false);
			notification.setEnabled(false);
			icon_clear.setEnabled(false);
		}
		
		madeby = (TextView) findViewById(R.id.madeby);
		try{
			PackageManager packageManager = this.getPackageManager();
			PackageInfo infor =  packageManager.getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
			
			madeby.setText(String.format(getString(R.string.madeby), infor.versionName));
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public void SeekBarListener(){
		MinSenser.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				
				if (progress>1400)
					MinText.setText(String.format(getString(R.string.min)+getString(R.string.not_work), progress));
				/**
				 * 1.1 ������Ʈ
				 * ���� 250�Ʒ��� �����Ѱ�� ���ڻ��� ���������� �ٲٰ� ����Ҽ� ���ٴ� �ؽ�Ʈ�� ǥ����
				 */
				else if(progress<=250){
					MinText.setText(Html.fromHtml("<font color='#FF0000'>"+String.format(getString(R.string.min)+getString(R.string.not_used), progress)+"</font>"));
					editor.putInt("MinSenser", 251).commit();
					MinSenser.setProgress(250);
					return;
				}else if(progress<=480)
					MinText.setText(String.format(getString(R.string.min)+getString(R.string.very_sore), progress));
				else
					MinText.setText(String.format(getString(R.string.min), progress));
				
				editor.putInt("MinSenser", progress).commit();
			}
		});
		
		Delay.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				
				if (progress>6)
					DelayText.setText(String.format(getString(R.string.Delay)+getString(R.string.not_work), progress));
				else if(progress<1)
					DelayText.setText(String.format(getString(R.string.Delay)+getString(R.string.very_sore), progress));
				else
					DelayText.setText(String.format(getString(R.string.Delay), progress));
				
				editor.putInt("Delay", progress).commit();
			}
		});
		
	}
	
	public void CheckBoxListener(){
		bootable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					editor.putBoolean("boot", true).commit();
//					Toast.makeText(SettingActivity.this, "���� �ڵ� ���� üũ��", Toast.LENGTH_SHORT).show();
				}else{
					editor.putBoolean("boot", false).commit();
//					Toast.makeText(SettingActivity.this, "���� �ڵ� ���� ��Ȱ��ȭ", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		Vibrator.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					editor.putBoolean("Vibrator", true).commit();
//					Toast.makeText(SettingActivity.this, "���� ����", Toast.LENGTH_SHORT).show();
				}else{
					editor.putBoolean("Vibrator", false).commit();
//					Toast.makeText(SettingActivity.this, "���� ����", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		password_enable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				if(isChecked){
					LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					final View view = inflater.inflate(R.layout.activity_pass_word_make, null);
					
					AlertDialog.Builder alert = new AlertDialog.Builder(SettingActivity.this);
					alert.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							String Inputpassword = ((EditText) view.findViewById(R.id.password_edittext)).getText().toString();
							if(Inputpassword.equals("")){
								Toast.makeText(SettingActivity.this, R.string.blank, Toast.LENGTH_SHORT).show();
								password_enable.setChecked(false);
							}else{
								editor.putString("password", Inputpassword);
								editor.putBoolean("password_enable", true).commit();
								Toast.makeText(SettingActivity.this, R.string.enter_password_ok, Toast.LENGTH_SHORT).show();
							}
							dialog.dismiss();
						}
					});
					alert.setNegativeButton(R.string.exit, new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							password_enable.setChecked(false);
							dialog.dismiss();
						}
					});
					alert.setView(view);
					alert.show();
				}else{
					editor.remove("password_enable");
					editor.remove("password").commit();
				}
			}
		});
		
		notification.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					editor.putBoolean("notification", true).commit();
					icon_clear.setVisibility(View.VISIBLE);
				}else{
					editor.remove("clear_icon");
					editor.putBoolean("notification", false).commit();
					icon_clear.setChecked(false);
					icon_clear.setVisibility(View.GONE);
				}
			}
		});
		
		icon_clear.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					editor.putBoolean("clear_icon", true).commit();
				}else{
					editor.remove("clear_icon").commit();
				}
			}
		});
	}
	
	
	boolean isServiceRunningCheck(Context context) {
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
