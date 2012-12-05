package org.orange.querysystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import org.orange.querysystem.content.ListCoursesActivity;

import util.webpage.SchoolWebpageParser;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
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
	static final int DATE_DIALOG_PROMPT = 2;
	private static final int LOGIN_TIME_OUT = 3000;//3s
	   
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
        if(shareData.getString("start_year", null) == null || shareData.getString("start_month", null) == null || shareData.getString("start_day", null) == null ){
        	Editor editor = getSharedPreferences("data", 0).edit();
            editor.putString("start_year", String.valueOf(mYear));
            editor.putString("start_month", String.valueOf(mMonth));
            editor.putString("start_day", String.valueOf(mDayOfMonth));
            editor.commit();
        }
        
        if(shareData.getString("userName", null) == null){
            DBManager manager = new DBManager();
    		try {
    			manager.openHelper(this);
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        }
        
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
        if(shareData.getString("start_year", null) == "0" || shareData.getString("start_month", null) == "0" || shareData.getString("start_day", null) == "0" ){
//        	//TODO 时间选择时判断有效的日期
//        	showDialog(DATE_DIALOG_ID);
        	showDialog(DATE_DIALOG_PROMPT);
        	startTime.setText("开课时间：请点击此处设置第一天开课日期！");
        }else{
        	startTime.setText("开课时间：" + shareData.getString("start_year", null) + "年" + shareData.getString("start_month", null) + "月" + shareData.getString("start_day", null) + "日");
        }
        startTime.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog(DATE_DIALOG_ID);
			}
		});
    }      
	   
	@Override   
	protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this,
                            mDateSetListener,
                            mYear, mMonth, mDayOfMonth);
            case DATE_DIALOG_PROMPT:
            	LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
            	RelativeLayout relativeLayout = new RelativeLayout(LoginActivity.this);
            	relativeLayout.setLayoutParams(params);
            	TextView textView = new TextView(LoginActivity.this);
           	 	textView.setText("请先设置开课时间，以保证日课程和周课程能正常显示！");
           	 	RelativeLayout.LayoutParams tvlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
           	 	relativeLayout.addView(textView, tvlp);
           	 	return new AlertDialog.Builder(this)
                 //.setIconAttribute(android.R.attr.alertDialogIcon)
//                 .setTitle(R.string.alert_dialog_text_entry)
                 .setView(relativeLayout)
                 .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                       	 
                            /* User clicked OK so do some stuff */
                       	 	
                       	 	showDialog(DATE_DIALOG_ID);
                        }
                    })
//                    .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int whichButton) {
//
//                            /* User clicked cancel so do some stuff */
//                        }
//                    })
                    .create();
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
                		startTime.setText("开课时间：请点击此处设置第一天开课日期！");
                	}
                	else{
                		Editor editor = getSharedPreferences("data", 0).edit();
                        editor.putString("start_year", Integer.toString(year_choice));
                        editor.putString("start_month", Integer.toString(month_choice+1));
                        editor.putString("start_day", Integer.toString(day_choice));
                        editor.commit();
                        startTime.setText("开课时间：" + year_choice + "年" + (month_choice+1) + "月" + day_choice + "日");
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

	private class LogIn extends AsyncTask<Void,Void, Boolean>{
		protected Boolean doInBackground(Void... args) {
			Boolean hasLogined = null;
			try {
				SchoolWebpageParser parser = new SchoolWebpageParser();
				parser.setUser(userNameBox.getText().toString(), passwordBox.getText().toString());
				parser.setTimeout(LOGIN_TIME_OUT);
				hasLogined = parser.getCurrentHelper().doLogin();
				File imageFile = new File("data/data/org.orange.querysystem/files/", "student_image.jpg");
    			imageFile.delete();
    			File fileObject = new File("data/data/org.orange.querysystem/files/", "student_info.txt");
    			fileObject.delete();
			} catch (IOException e) {
				System.err.println(e);
			}
			return hasLogined;
		}  

		protected void onPostExecute(Boolean hasLogined){
			if(hasLogined == null){
				//TODO 之后放到R.string
				Toast.makeText(LoginActivity.this, "登录失败，请检查网络连通性并重试。", Toast.LENGTH_LONG).show();
				return;
			}
			if(!hasLogined){
				passwordBox.setText("");
				Animation shake = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.shake);
				userNameBox.startAnimation(shake);
				passwordBox.startAnimation(shake);
				//TODO 之后放到R.string
				Toast.makeText(LoginActivity.this, "用户名或密码输入错误", Toast.LENGTH_LONG).show();
			}
			else{
				Intent intent = new Intent(LoginActivity.this, ListCoursesActivity.class);
				startActivity(intent);
			}
		}
	}
	private class DBManager{
		protected void openHelper(Context context) throws IOException{
			 String dbDirPath = "/data/data/org.orange.querysystem/databases";
			 File dbDir = new File(dbDirPath);
			 if(!dbDir.exists())
				 dbDir.mkdir();
			 InputStream is = context.getResources().openRawResource(R.raw.studentinf);
			 FileOutputStream os = new FileOutputStream(dbDirPath+"/studentInf.db");
			 byte[] buffer = new byte[1024];
			 int count = 0;
			 while ((count = is.read(buffer)) > 0) {    os.write(buffer, 0, count);  }  
			 is.close();
			 os.close();
		}
	}
}

