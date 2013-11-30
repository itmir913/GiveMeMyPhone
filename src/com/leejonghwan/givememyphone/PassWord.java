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
	 * 1.2 ������Ʈ
	 * �� ��Ƽ��Ƽ�� �������� ����� ��й�ȣ�� �ִٸ�
	 * ��й�ȣ�� �Է��϶�� �޼����� ���� ��Ƽ��Ƽ �Դϴ�
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
		
		// ������ Ȯ���մϴ�
		answer = pref.getString("password", "");
		Log.d("����", answer);
//		if(answer==0)
//			finish();

		password = (EditText) findViewById(R.id.password);
		password.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable arg0) { }

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }

			/**
			 * �Է��� ���� ���Ҷ����� ������ Ȯ���մϴ�
			 */
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				/**
				 * ���� �Է��� ���� ����(����, "")�̶�� �������� ������ �߹Ƿ� �� ��ü�� try������ ���� �������Ḧ �����ϴ�
				 */
				try {
//					Password = Integer.parseInt(arg0.toString());
					if(arg0.toString().equals(answer))
						finish(); // ������ ������쿡�� �� ��Ƽ��Ƽ�� �ݽ��ϴ�
					} catch (Exception e) { }
			}
		});
	}
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	/**
        	 * �ڷΰ��� Ű�� ���� MainActivity�� �Ѿ�� ����� �����մϴ�
        	 */
        	moveTaskToBack(true);
        	finish();
        	android.os.Process.killProcess(android.os.Process.myPid());
        	return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
