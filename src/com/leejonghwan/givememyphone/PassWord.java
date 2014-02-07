package com.leejonghwan.givememyphone;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
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
		
		pref = getSharedPreferences("preference", 0);
		
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
					if(arg0.toString().equals(answer)){
						Intent i = new Intent(PassWord.this, MainActivity.class);
						i.putExtra("PassWord_Enable", true);
						startActivity(i);
						finish();
					}
				} catch (Exception e) { }
			}
		});
	}
}
