package org.orange.querysystem;

import java.io.IOException;
import java.util.Calendar;

import org.orange.querysystem.content.ListCoursesActivity;
import util.webpage.SchoolWebpageParser;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources.NotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity{
	private EditText userNameBox;
	private EditText passwordBox;
	private TextView startTime;
	private TextView title;
	private TextView userName;
	private TextView password;
	private Button denglu;
	private CheckBox rememberPS;
	private CheckBox autoDengLu;	
	private int mYear = 0;
	private int mMonth = 0;
	private int mDayOfMonth = 0;
	private int mDayOfYear = 0;
		
	public static final String TAG = "org.orange.querysystem";
	static final int DATE_DIALOG_ID = 1;	
	   
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
       
        startTime = (TextView)findViewById(R.id.start_time);
        userNameBox = (EditText)findViewById(R.id.userNameBox);
        passwordBox = (EditText)findViewById(R.id.passwordBox);
        title = (TextView)findViewById(R.id.title);
        userName = (TextView)findViewById(R.id.userName);
        password = (TextView)findViewById(R.id.password);
        denglu = (Button)findViewById(R.id.denglu);
        rememberPS = (CheckBox)findViewById(R.id.rememberPS);
        autoDengLu = (CheckBox)findViewById(R.id.autoDengLu);
        Calendar calendar = Calendar.getInstance();
		mYear = calendar.get(Calendar.YEAR);
		mMonth = calendar.get(Calendar.MONTH);//比正常少一个月
		mDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
		mDayOfYear = calendar.get(Calendar.DAY_OF_YEAR);
		
       	     
        title.setText(R.string.title);
        userName.setText(R.string.userName);
        password.setText(R.string.password);
        
        SharedPreferences shareData = getSharedPreferences("data", 0);
        userNameBox.setText(shareData.getString("userName", null));
        if(shareData.getBoolean("logIn_auto", false)){
        	autoDengLu.setChecked(true);
        	startActivity(new Intent(this, ListCoursesActivity.class));
        }
        if(shareData.getBoolean("rememberPS", true)){
        	rememberPS.setChecked(true);
        	passwordBox.setText(shareData.getString("password", null));
        }else{
        	passwordBox.setText("");
        }	    
        if(shareData.getString("start_year", null) == null || shareData.getString("start_month", null) == null || shareData.getString("start_day", null) == null ){
        	//TODO 时间选择时判断有效的日期
        	showDialog(DATE_DIALOG_ID);
        }
        else{
        	startTime.setText("开学时间：" + shareData.getString("start_year", null) + "年" + shareData.getString("start_month", null) + "月" + shareData.getString("start_day", null) + "日");
        }
    }      
	   
	@Override   
	protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                            mDateSetListener,
                            mYear, mMonth, mDayOfMonth);
        }
        return null;
    }    
	    
	@Override    
	protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DATE_DIALOG_ID:
            	//设置初始时间为当前时间
                ((DatePickerDialog) dialog).updateDate(mYear, mMonth, mDayOfMonth);
                break;
        }
    }        

	private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year_choice, int month_choice,
                        int day_choice) {
                	Calendar calendar_2 = Calendar.getInstance();
                	calendar_2.set(year_choice, month_choice, day_choice);
                	if(calendar_2.get(Calendar.YEAR) > mYear || ((mYear-calendar_2.get(Calendar.YEAR))*365 + mDayOfYear - calendar_2.get(Calendar.DAY_OF_YEAR)) < 0 || ((mYear-calendar_2.get(Calendar.YEAR))*365 + mDayOfYear - calendar_2.get(Calendar.DAY_OF_YEAR)) > 22*7){
                		Toast.makeText(LoginActivity.this, "此为不恰当的开学日期，请重新设置！", Toast.LENGTH_LONG).show();
                		startTime.setText("开学时间：请登录后进入主菜单设置！");
                	}
                	else{
                		Editor editor = getSharedPreferences("data", 0).edit();
                        editor.putString("start_year", Integer.toString(year_choice));
                        editor.putString("start_month", Integer.toString(month_choice+1));
                        editor.putString("start_day", Integer.toString(day_choice));
                        editor.commit();
                        startTime.setText("开学时间：" + year_choice + "年" + (month_choice+1) + "月" + day_choice + "日");
                	}
                	
                }
            };    
	   
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
	    
    //通过Intent方法对登陆信息进行获取和传递
    public void dengLu(View view){
		ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if(networkInfo !=null && networkInfo.isConnected())
			new LogIn().execute();
		else{
			Toast.makeText(this, "网络异常！请检查网络设置！", Toast.LENGTH_LONG).show();  
		}
	}  
	  
    public class LogIn extends AsyncTask<Void,Void, Boolean>{
		protected Boolean doInBackground(Void... args) {
			Boolean isLogIn = false;
			try {
				SchoolWebpageParser parser = new SchoolWebpageParser(new MyParserListener(), userNameBox.getText().toString(), passwordBox.getText().toString());
				if(!parser.getCurrentHelper().doLogin()){
					isLogIn = false;
				}
				else{
					isLogIn = true;
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
			return isLogIn;
		}  

		protected void onPostExecute(Boolean isLogIn){
			if(!isLogIn){
				passwordBox.setText("");
				Animation shake = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.shake);
				userNameBox.startAnimation(shake);
				passwordBox.startAnimation(shake);
				Toast.makeText(LoginActivity.this, "用户名或密码输入错误", Toast.LENGTH_LONG).show();
			}
			else{
				Intent intent = new Intent(LoginActivity.this, ListCoursesActivity.class);
				startActivity(intent);		
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
	}
}
	    
