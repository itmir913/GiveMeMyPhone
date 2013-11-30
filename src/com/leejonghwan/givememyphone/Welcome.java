package com.leejonghwan.givememyphone;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public class Welcome extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_welcome);
	}
	
	public void pass(View v){
		finish();
	}

}
