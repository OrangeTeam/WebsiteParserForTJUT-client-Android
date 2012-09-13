package org.orange.querysystem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.orange.querysystem.R;

public class LoginActivity extends Activity{
		private EditText userNameBox;
		private EditText passwordBox;
		private TextView title;
		private TextView userName;
		private TextView password;
		private Button denglu;
	   
	   @Override
	   public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.login_activity);
	        ApplicationExit appExit = (ApplicationExit)getApplication();
	        appExit.setExit(false);
	     
	        userNameBox = (EditText)findViewById(R.id.userNameBox);
	        passwordBox = (EditText)findViewById(R.id.passwordBox);
	        title = (TextView)findViewById(R.id.title);
	        userName = (TextView)findViewById(R.id.userName);
	        password = (TextView)findViewById(R.id.password);
	        denglu = (Button)findViewById(R.id.denglu);
	       	     
	        title.setText(R.string.title);
	        userName.setText(R.string.userName);
	        password.setText(R.string.password);
	        	        
	        
	    }
	   
//	 @Override
//	  public void onStart(){
//		   super.onStart();
//		   ApplicationExit appExit = (ApplicationExit)getApplication();
//		   if(appExit.isExit()){
//			   finish();
//		   }
//	  }
	   
	  @Override
	  public boolean onCreateOptionsMenu(Menu menu) {
	    	menu.add(0, 1, 1, R.string.exit);
	    	menu.add(0, 2, 2, R.string.about);
	    	return super.onCreateOptionsMenu(menu);
	       
	  }
	   
	  @Override
	  public boolean onOptionsItemSelected(MenuItem item) {
			if(item.getItemId() == 1){
				ApplicationExit appExit = (ApplicationExit)getApplication();
				appExit.setExit(true);
				finish();
			}
			return super.onOptionsItemSelected(item);
		}
	        
	    
	    //通过Intent方法对登陆信息进行获取和传递
	    public void dengLu(View view){
	    	
	    	Intent intent = new Intent(this, SampleActivity.class);
//	    	String userNameStr = userNameBox.getText().toString();
//	    	String passwordStr = passwordBox.getText().toString();
//			
//	    	intent.putExtra("userName", userNameStr);
//	    	intent.putExtra("password", passwordStr);
//	    	
	    	startActivity(intent);
	    	finish();
	    }
	    
	   
}
	    
