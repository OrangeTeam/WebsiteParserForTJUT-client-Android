package org.orange.querysystem;

import java.io.IOException;
import java.util.ArrayList;

import util.webpage.Constant;
import util.webpage.Course;
import util.webpage.ReadPageHelper;
import util.webpage.SchoolWebpageParser;
import util.webpage.SchoolWebpageParser.ParserException;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.korovyansk.android.slideout.SlideoutActivity;

public class SampleActivity extends Activity {
	public static final int PICK_CONTENT_REQUEST = 1;
	public static final int RESULT_COURSE = RESULT_FIRST_USER;
	public static final int RESULT_SCORE = RESULT_FIRST_USER+1;
	
	private TextView content;
	private String charset = "GB2312";
	private int timeout = 6000;
	private EditText editUserName;
	private EditText editPassword;
	private Button refresh;

	@TargetApi(11)
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sample);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
	    	getActionBar().hide();
	    }
		refresh = (Button)findViewById(R.id.refresh);
	    refresh.setBackgroundResource(R.drawable.ic_action_refresh);
		findViewById(R.id.sample_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
						SlideoutActivity.prepare(SampleActivity.this, R.id.inner_content, width);
						startActivity(new Intent(SampleActivity.this,
								MenuActivity.class));
						overridePendingTransition(0, 0);
					}
				});
		content = (TextView)findViewById(R.id.sample_content);
	}
	
	@Override
	protected void onStart(){
		   super.onStart();
		   ApplicationExit appExit = (ApplicationExit)getApplication();
		   if(appExit.isExit()){
			   finish();
		   }
	  }
	
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	        menu.add(0, 1, 1, R.string.exit);
	        menu.add(0, 2, 2, R.string.change_number);
	        menu.add(0, 3, 3, R.string.about);
	        return super.onCreateOptionsMenu(menu); 
	    }
	    @Override
	    public boolean onMenuItemSelected(int featureId, MenuItem item) {
	    	// TODO Auto-generated method stub\
	    	if(item.getItemId()==1){
	    		finish();
	    	}
	    	if(item.getItemId()==2){
	    		startActivity(new Intent(SampleActivity.this, LoginActivity.class));
	    	}
	    	return super.onMenuItemSelected(featureId, item);
	    }
	
	 @Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if(keyCode == KeyEvent.KEYCODE_BACK){
				int width = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, getResources().getDisplayMetrics());
				SlideoutActivity.prepare(SampleActivity.this, R.id.inner_content, width);
				startActivity(new Intent(SampleActivity.this,
						MenuActivity.class));
				overridePendingTransition(0, 0);
			}
			return super.onKeyDown(keyCode, event);
	    }
	 
	public void test(View view){
    	System.out.println("hello");
    }
	
}
