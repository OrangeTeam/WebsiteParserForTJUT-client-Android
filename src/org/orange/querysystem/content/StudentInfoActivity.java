package org.orange.querysystem.content;

import org.orange.querysystem.ApplicationExit;

import android.app.Activity;

public class StudentInfoActivity extends Activity{
	
	@Override
	protected void onStart(){
		super.onStart();
		ApplicationExit appExit = (ApplicationExit)getApplication();
		if(appExit.isExit()){
			finish();
		}
	}
}
