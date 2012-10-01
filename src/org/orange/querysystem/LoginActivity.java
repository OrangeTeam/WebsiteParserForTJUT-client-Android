package org.orange.querysystem;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.orange.querysystem.R;
import org.orange.querysystem.content.AllCourseListActivity;

import util.webpage.SchoolWebpageParser;

public class LoginActivity extends Activity{
		private EditText userNameBox;
		private EditText passwordBox;
		private TextView title;
		private TextView userName;
		private TextView password;
		private Button denglu;
		private CheckBox rememberPS;
		private CheckBox autoDengLu;
		
		public static final String TAG = "org.orange.querysystem";
	   
	   @Override
	   public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.login_activity);
	        ApplicationExit appExit = (ApplicationExit)getApplication();
	        appExit.setExit(false);
	        
	        SharedPreferences shareData = getSharedPreferences("data", 0);	        
	        userNameBox = (EditText)findViewById(R.id.userNameBox);
	        passwordBox = (EditText)findViewById(R.id.passwordBox);
	        title = (TextView)findViewById(R.id.title);
	        userName = (TextView)findViewById(R.id.userName);
	        password = (TextView)findViewById(R.id.password);
	        denglu = (Button)findViewById(R.id.denglu);
	        rememberPS = (CheckBox)findViewById(R.id.rememberPS);
	        autoDengLu = (CheckBox)findViewById(R.id.autoDengLu);
	       	     
	        title.setText(R.string.title);
	        userName.setText(R.string.userName);
	        password.setText(R.string.password);
	        
	        userNameBox.setText(shareData.getString("userName", null));
	        if(shareData.getBoolean("logIn_auto", true)){
	        	autoDengLu.setChecked(true);
	        	startActivity(new Intent(this, AllCourseListActivity.class));
	        }
	        if(shareData.getBoolean("rememberPS", true)){
	        	rememberPS.setChecked(true);
	        	passwordBox.setText(shareData.getString("password", null));
	        }else{
	        	passwordBox.setText("");
	        }	        
	    }
	   
	   @Override
	   protected void onPause(){
		   super.onPause();
		   Editor editor = getSharedPreferences("data", 0).edit();

		   if(autoDengLu.isChecked()){
	    	   editor.putBoolean("logIn_auto", true); 
	       }
	       else{
	    	   editor.putBoolean("logIn_auto", false);
	       }
		   if(rememberPS.isChecked()){
	        	editor.putBoolean("rememberPS", true);
			}
	        else{
	        	editor.putBoolean("rememberPS", false);
	        }
		   
	       editor.putString("userName", userNameBox.getText().toString()); 
	       editor.putString("password", passwordBox.getText().toString());
	       editor.commit();
//		  
	   }
	   
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
	    	try {
				SchoolWebpageParser parser = new SchoolWebpageParser(new MyParserListener(), userNameBox.getText().toString(), passwordBox.getText().toString());
				if(!parser.getCurrentHelper().doLogin()){
					passwordBox.setText("");
					Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
					userNameBox.startAnimation(shake);
					passwordBox.startAnimation(shake);
					Toast.makeText(this, "用户名或密码输入错误", Toast.LENGTH_LONG).show();
					
				}
				else{
					Intent intent = new Intent(this, AllCourseListActivity.class);
					startActivity(intent);
				}
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();	
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    		
	    	
	    }
	    
	    private class MyParserListener extends SchoolWebpageParser.ParserListenerAdapter{

			/* (non-Javadoc)
			 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onError(int, java.lang.String)
			 */
			@Override
			public void onError(int code, String message) {
				Log.e(TAG, message);
			}

			/* (non-Javadoc)
			 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onWarn(int, java.lang.String)
			 */
			@Override
			public void onWarn(int code, String message) {
				Log.w(TAG, message);
			}

			/* (non-Javadoc)
			 * @see util.webpage.SchoolWebpageParser.ParserListenerAdapter#onInformation(int, java.lang.String)
			 */
			@Override
			public void onInformation(int code, String message) {
				Log.i(TAG, message);
			}
		}
	    
	    @Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			if(keyCode == KeyEvent.KEYCODE_BACK){
				new AlertDialog.Builder(LoginActivity.this)
					.setTitle(R.string.menu_activity_title)
					.setIcon(R.drawable.ic_action_refresh)
					.setMessage(R.string.menu_activity_inform)
					.setPositiveButton(R.string.alert_dialog_ok, 
							new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									ApplicationExit appExit = (ApplicationExit)getApplication();
									appExit.setExit(true);
									finish();
								}
							}
					)
					.setNegativeButton(R.string.alert_dialog_cancel, 
							new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									// TODO Auto-generated method stub
									
								}
							}
					).show();
			}
		
//				mSlideoutHelper.close();
//				return true;
//			
			return super.onKeyDown(keyCode, event);
		}
	    
	   
}
	    
