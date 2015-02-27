package com.leejonghwan.givememyphone;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public class PassWord extends Activity {
	SharedPreferences pref;
	EditText password;
	String answer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pass_word);
		
		pref = getSharedPreferences("preference", 0);
		
		answer = pref.getString("password", "");
		
		password = (EditText) findViewById(R.id.password);
		password.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s){}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after){}
			
			@Override
			public void onTextChanged(CharSequence charS, int start, int before, int count) {
				try {
					if(charS.toString().equals(answer)){
						Intent i = new Intent(PassWord.this, MainActivity.class);
						i.putExtra("PassWord_Enable", true);
						startActivity(i);
						finish();
					}
				} catch (Exception e){}
			}
		});
	}
}
