package com.leejonghwan.givememyphone;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PassWord_Make extends Activity {
	int Password;
	/**
	 * 1.2 ������Ʈ
	 * �� ��Ƽ��Ƽ�� ��й�ȣ�� ����� ��Ƽ��Ƽ �Դϴ�
	 */
	
	SharedPreferences pref;
	SharedPreferences.Editor editor;
	
	EditText password_edittext;
	Button ok_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pass_word_make);
		
		pref = getSharedPreferences("prefs", 0);
		editor = pref.edit();
		
		password_edittext = (EditText) findViewById(R.id.password_edittext);
		ok_btn = (Button) findViewById(R.id.ok_Btn);
		
		/**
		 * ��ư�� ������ ��й�ȣ�� �����ǰ� ���� MainActivity�� ȭ���� �ٲ�� ����Ǿ����ϴ�
		 */
		ok_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String Password_string = password_edittext.getText().toString();
				
				/**
				 * ������ �ƴҰ�� �۵��մϴ�
				 */
				if(!Password_string.equals("")){
					/**
					 * ���� 10���� �Ѿ���
					 * E/AndroidRuntime(31811): java.lang.NumberFormatException: Invalid int: "2580369147"
					 * �̷� ������ �߸� �������� �Ǿ����ϴ�
					 * �׷��Ƿ� �ִ� ���� ���̸� 9�ڷ� �����մϴ� (XML)
					 */
					Password = Integer.parseInt(Password_string); // String�� int������ ��ȯ�մϴ� �̶� string�� ����("")�̸� ������ �߹Ƿ� �� if���� �ִ°̴ϴ�
				
				/**
				 * ��й�ȣ 0�� ���� Ȯ���� ���� ���ÿ��� ���̹Ƿ� 0������ ������ �����մϴ�
				 */
				if(Password==0){
					Toast.makeText(PassWord_Make.this, "0���δ� ������ �� �����ϴ�", Toast.LENGTH_SHORT).show();
				}else{
					editor.putInt("password_check", 1); // ��й�ȣ ������
					editor.putInt("Password", Password); // ��й�ȣ �� ����
					editor.commit();
				
					Toast.makeText(PassWord_Make.this, "��й�ȣ ���� �Ϸ�", Toast.LENGTH_SHORT).show();
					finish();
				}
				}else{
					/**
					 * ������ �����ϰ�� ����˴ϴ�
					 */
					editor.putInt("password_check", 0);
					editor.putInt("Password", 0);
					editor.commit();
					MainActivity.password_enable.setChecked(false);
					Toast.makeText(PassWord_Make.this, "��й�ȣ ������ ����մϴ�", Toast.LENGTH_SHORT).show();
					finish();
				}
			}});
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	/**
        	 * �ڷΰ��� Ű�� �����մϴ�
        	 */
        	return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
