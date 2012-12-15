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
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
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
		
	public static final String TAG = "org.orange.querysystem";
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
		
       	     
        title.setText(R.string.title);
        userName.setText(R.string.userName);
        password.setText(R.string.password);
        SharedPreferences shareData = getSharedPreferences("data", 0);
        
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

        Calendar firstDay = SettingsActivity.getMondayOfZeroWeek(this);
        if(firstDay == null){
//        	showDialog(DATE_DIALOG_ID);
        	showDialog(DATE_DIALOG_PROMPT);
        	startTime.setText("开课时间：请点击此处设置第一天开课日期！");
        }else{
            firstDay.set(Calendar.WEEK_OF_YEAR, 1 + firstDay.get(Calendar.WEEK_OF_YEAR));
            startTime.setText("开课时间：" + firstDay.get(Calendar.YEAR) + "年"
                    + (firstDay.get(Calendar.MONTH) - Calendar.JANUARY + 1) + "月"
                    + firstDay.get(Calendar.DAY_OF_MONTH) + "日");
        }
        startTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(LoginActivity.this ,SettingsActivity.class));
			}
		});
    }      
	   
	@Override   
	protected Dialog onCreateDialog(int id) {
        switch (id) {
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
                            startActivity(new Intent(LoginActivity.this ,SettingsActivity.class));
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
//				Intent intent = new Intent(LoginActivity.this, ListCoursesActivity.class);
//				startActivity(intent);
				Editor editor = getSharedPreferences("data", 0).edit();
				editor.putBoolean("changeUser", true);
				editor.commit();
				finish();
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

