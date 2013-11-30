package com.leejonghwan.givememyphone;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

public class PassWord extends Activity {
	/**
	 * 1.2 업데이트
	 * 이 액티비티는 내폰내놔 실행시 비밀번호가 있다면
	 * 비밀번호를 입력하라는 메세지를 띄우는 액티비티 입니다
	 */
	SharedPreferences pref;
	EditText password;
	
//	int Password;
	String answer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pass_word);
		
		pref = getSharedPreferences("prefs", 0);
		
		// 정답을 확인합니다
		answer = pref.getString("password", "");
		Log.d("정답", answer);
//		if(answer==0)
//			finish();

		password = (EditText) findViewById(R.id.password);
		password.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) { }

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

			/**
			 * 입력한 값이 변할때마다 정답을 확인합니다
			 */
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				/**
				 * 만약 입력한 값이 공백(없음, "")이라면 강제종료 오류가 뜨므로 이 전체를 try문으로 감싸 강제종료를 막습니다
				 */
				try {
//					Password = Integer.parseInt(arg0.toString());
					if(arg0.toString().equals(answer))
						finish(); // 정답이 맞을경우에만 이 액티비티를 닫습니다
					} catch (Exception e) { }
			}
		});
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	/**
        	 * 뒤로가기 키를 눌러 MainActivity로 넘어가는 편법을 예방합니다
        	 */
        	moveTaskToBack(true);
        	finish();
        	android.os.Process.killProcess(android.os.Process.myPid());
        	return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
