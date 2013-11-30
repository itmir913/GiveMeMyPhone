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
	 * 1.2 업데이트
	 * 이 액티비티는 비밀번호를 만드는 액티비티 입니다
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
		 * 버튼을 누르면 비밀번호가 생성되고 이전 MainActivity로 화면이 바뀌게 설계되었습니다
		 */
		ok_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String Password_string = password_edittext.getText().toString();
				
				/**
				 * 공백이 아닐경우 작동합니다
				 */
				if(!Password_string.equals("")){
					/**
					 * 숫자 10개가 넘어갈경우
					 * E/AndroidRuntime(31811): java.lang.NumberFormatException: Invalid int: "2580369147"
					 * 이런 오류가 뜨며 강제종료 되었습니다
					 * 그러므로 최대 숫자 길이를 9자로 제한합니다 (XML)
					 */
					Password = Integer.parseInt(Password_string); // String을 int형으로 변환합니다 이때 string이 공백("")이면 오류가 뜨므로 위 if줄이 있는겁니다
				
				/**
				 * 비밀번호 0은 설정 확인을 위해 어플에서 쓰이므로 0으로의 설정을 제한합니다
				 */
				if(Password==0){
					Toast.makeText(PassWord_Make.this, "0으로는 설정할 수 없습니다", Toast.LENGTH_SHORT).show();
				}else{
					editor.putInt("password_check", 1); // 비밀번호 설정됨
					editor.putInt("Password", Password); // 비밀번호 값 저장
					editor.commit();
				
					Toast.makeText(PassWord_Make.this, "비밀번호 설정 완료", Toast.LENGTH_SHORT).show();
					finish();
				}
				}else{
					/**
					 * 내용이 공백일경우 실행됩니다
					 */
					editor.putInt("password_check", 0);
					editor.putInt("Password", 0);
					editor.commit();
					MainActivity.password_enable.setChecked(false);
					Toast.makeText(PassWord_Make.this, "비밀번호 설정을 취소합니다", Toast.LENGTH_SHORT).show();
					finish();
				}
			}});
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	/**
        	 * 뒤로가기 키를 방지합니다
        	 */
        	return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
