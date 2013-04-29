package org.orange.querysystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Scanner;

import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.querysystem.content.ListViewAdapter;
import org.orange.querysystem.util.Network;

import util.webpage.Constant;
import util.webpage.SchoolWebpageParser;
import util.webpage.SchoolWebpageParser.ParserException;
import util.webpage.Student;
import util.webpage.Student.StudentException;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StudentInfoActivity extends ListActivity{
	private static final String KEY_AUTHENTICATED = "authenticated";
	private ListView listView;
	private ListViewAdapter adapter;
	private View showImage;
	private ImageView imageView;
	private boolean authenticated;
	private static final String FILE_NAME = "student_info.txt";
	public static final int PASSWORD_PROMPT = 1;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.student_info);

		if(savedInstanceState == null){
			authenticated = false;
			showDialog(PASSWORD_PROMPT);
		}else{
			authenticated = savedInstanceState.getBoolean(KEY_AUTHENTICATED, false);
			if(authenticated)
				enterActivity();
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_AUTHENTICATED, authenticated);
	}

	public void enterActivity(){
		showImage = getLayoutInflater().inflate(R.layout.show_image, null);
		imageView = (ImageView)showImage.findViewById(R.id.studentImageView);
		
		listView = getListView();
		listView.setCacheColorHint(Color.TRANSPARENT);
		listView.addFooterView(showImage);
		initAdapter();
		setListAdapter(adapter);
		
		File fileObject = new File("data/data/org.orange.querysystem/files/" + FILE_NAME);
		if(fileObject.exists())
		{
			new StudentInfoFromFile().execute();
		}else{
			new StudentInfoFromWeb().execute();
		}
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch(id){
		case PASSWORD_PROMPT:
			final TextView textView = new TextView(this);
			textView.setText("请输入登陆密码：");
			textView.setTextSize(14);
			textView.setId(1);
			final EditText editText = new EditText(this);
			editText.setId(2);
			editText.setEnabled(true);
			editText.setCursorVisible(true);
			editText.setLongClickable(true);
			editText.setFocusable(true);
			editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
       	 	
			RelativeLayout.LayoutParams tvlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			RelativeLayout.LayoutParams etlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			RelativeLayout relativeLayout = new RelativeLayout(this);
			tvlp.addRule(RelativeLayout.ALIGN_BASELINE, 2);
			etlp.addRule(RelativeLayout.RIGHT_OF, 1);
			relativeLayout.addView(textView, tvlp);
			relativeLayout.addView(editText, etlp);
			return new AlertDialog.Builder(this)
            .setView(relativeLayout)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int whichButton) {
                  	 
            		/* User clicked OK so do some stuff */
            		if(editText.getText().toString().equals(SettingsActivity.getAccountPassword(StudentInfoActivity.this))){
						authenticated = true;
            			enterActivity();
					}else{
            			editText.setText("");
            			Toast.makeText(StudentInfoActivity.this, "密码输入错误，请重试！！", Toast.LENGTH_LONG).show();
            			finish();
            		}
                      
                }
            })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int whichButton) {

            		/* User clicked cancel so do some stuff */
            		finish();
                }
            }).setOnKeyListener(new OnKeyListener(){
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if(keyCode == KeyEvent.KEYCODE_BACK){
						finish();
						return true;
					}
					return false;
				}
            }).create();
		}
		return null;
	}
	
	private void initAdapter(){
		Resources res = getResources();
		ArrayList<String> items = new ArrayList<String>();
		items.add(res.getString(R.string.student_number));
		items.add(res.getString(R.string.student_name));
		items.add(res.getString(R.string.gender));
		items.add(res.getString(R.string.birthday));
		items.add(res.getString(R.string.academic_period));
		items.add(res.getString(R.string.admission_time));
		items.add(res.getString(R.string.school));
		items.add(res.getString(R.string.major));
		items.add(res.getString(R.string.class_name));
		items.add(res.getString(R.string.photo));
		adapter = new ListViewAdapter(this, items);
	}
	
	public Bitmap getHttpBitmap(String url){
		URL myFileURL;
		Bitmap theBitmap = null;
		try{
			myFileURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)myFileURL.openConnection();
			conn.setConnectTimeout(100000);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			InputStream is = conn.getInputStream();
			theBitmap = BitmapFactory.decodeStream(is);
			is.close();
		}catch(Exception e){
		     
		}
		return theBitmap;
	}
	
	private void storeImage(Bitmap bitmap){
		File file = new File("data/data/org.orange.querysystem/files/");
		File imageFile = new File(file, "student_image.jpg");
		try{
			imageFile.createNewFile();
			FileOutputStream os = new FileOutputStream(imageFile);
			bitmap.compress(CompressFormat.JPEG, 50, os);
			os.flush();
			os.close();
		}catch(FileNotFoundException e){
			
		}catch(IOException e){
			
		}
	}
	
	private Bitmap getBitmap(){
		File imageFile = new File("data/data/org.orange.querysystem/files/", "student_image.jpg");
		Bitmap theBitmap = null;
		if(imageFile.exists()){
			try{
				theBitmap = BitmapFactory.decodeStream(new FileInputStream(imageFile));
			}catch(FileNotFoundException e){
				
			}
		}
		return theBitmap;
	}

	private class StudentInfoFromWeb extends AsyncTask<Object,Void,Student> {
		protected Student doInBackground(Object... args) {
			Student student = new Student();
			SchoolWebpageParser studentInfo;
			try {
				studentInfo = new SchoolWebpageParser();
                studentInfo.setUser(SettingsActivity.getAccountStudentID(StudentInfoActivity.this), SettingsActivity.getAccountPassword(StudentInfoActivity.this));
				studentInfo.parseScores(Constant.url.个人全部成绩, student);
			} catch (ParserException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			if(student.getName() != null)
			{
				PrintWriter outputStream = null;
		        try{
		        	outputStream = new PrintWriter(openFileOutput(FILE_NAME, MODE_PRIVATE));
		        }catch(FileNotFoundException e){
		        	System.out.println("Error opening the file");
		        	e.printStackTrace();
		        }
				if(outputStream != null){
					outputStream.println(student.getNumber());
					outputStream.println(student.getName());
					outputStream.println(Gender.getGenderCode(student.isMale()));
					outputStream.println(student.getBirthdayString());
					outputStream.println(Byte.toString(student.getAcademicPeriod()));
					outputStream.println(student.getAdmissionTimeString());
					outputStream.println(student.getSchoolName());
					outputStream.println(student.getMajorName());
					outputStream.println(student.getClassName());
					outputStream.close();
				}
		        storeImage(getHttpBitmap(student.getUrlOfFacedPhoto()));
		        new StudentInfoFromFile().execute();
			}
			return student;
		}

		protected void onPostExecute(Student student){
			if(student.getName() == null)
			{
				Toast.makeText(StudentInfoActivity.this, "学生信息更新失败，请点击刷新来更新信息", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private class StudentInfoFromFile extends AsyncTask<Void,Void,Student>{
		protected Student doInBackground(Void...agrs){
			Scanner inputStream = null;
			try{
				inputStream = new Scanner(openFileInput(FILE_NAME));
			}catch(FileNotFoundException e){
				System.out.println("Error opening files");
			}
			if(inputStream != null){
				Student student = new Student();
				student.setNumber(inputStream.nextLine());
				student.setName(inputStream.nextLine());
				student.setIsMale(Gender.isMale(Integer.parseInt(inputStream.nextLine())));
				try {student.setBirthday(inputStream.nextLine());} catch (ParseException e) {}
				try {student.setAcademicPeriod(Byte.parseByte(inputStream.nextLine()));}
				catch (StudentException e) {}
				try {student.setAdmissionTime(inputStream.nextLine());} catch (ParseException e) {}
				student.setSchoolName(inputStream.nextLine());
				student.setMajorName(inputStream.nextLine());
				student.setClassName(inputStream.nextLine());
				inputStream.close();
				return student;
			}else
				return null;
		}

		protected void onPostExecute(Student student){
			if(student == null)
				return;
			Resources res = getResources();
			ArrayList<String> items = new ArrayList<String>();
			items.add(res.getString(R.string.student_number) + "：" + student.getNumber());
			items.add(res.getString(R.string.student_name) + "：" + student.getName());
			items.add(res.getString(R.string.gender) + "："
					+ res.getString(Gender.getStringId(student.isMale())));
			items.add(res.getString(R.string.birthday) + "：" + student.getBirthdayString());
			items.add(res.getString(R.string.academic_period) + "：" + student.getAcademicPeriod());
			items.add(res.getString(R.string.admission_time) + "：" + student.getAdmissionTimeString());
			items.add(res.getString(R.string.school) + "：" + student.getSchoolName());
			items.add(res.getString(R.string.major) + "：" + student.getMajorName());
			items.add(res.getString(R.string.class_name) + "：" + student.getClassName());
			imageView.setImageBitmap(getBitmap());
			adapter = new ListViewAdapter(StudentInfoActivity.this, items);
			setListAdapter(adapter);
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.refresh);
        menu.add(0, 2, 2, R.string.settings);
        
        return super.onCreateOptionsMenu(menu); 
    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// TODO Auto-generated method stub\
    	if(item.getItemId() == 1){
			if(Network.isConnected(this)){
    			new StudentInfoFromWeb().execute();
//    			start_resume = 1;
//        		startActivity(new Intent(this, InsertDBFragmentActivity.class));
        		//TODO startActivity后不会继续运行
//        		readDB();
            }
            else{
            	Toast.makeText(this, "网络异常！请检查网络设置！", Toast.LENGTH_LONG).show();
            }
    	}
    	else if(item.getItemId() == 2){
    		startActivity(new Intent(this, SettingsActivity.class));
    	}
    	return super.onMenuItemSelected(featureId, item);
    }


	/**
	 * 与性别相关的工具类
	 */
	private static class Gender{
		private static final int unknown	= 0;
		private static final int male		= 1;
		private static final int female		= 2;
		private Gender(){}
		/**
		 * 取得与指定性别相对应的String资源id
		 * @param isMale 性别 。true表示男，false表示女，null表示未知
		 * @return 相应的strings.xml中字符串资源id
		 */
		public static int getStringId(Boolean isMale){
			if(isMale == null)
				return R.string.unknown;
			else if(isMale)
				return R.string.male;
			else
				return R.string.female;
		}
		/**
		 * 把性别转化为{@link Gender}中的性别代码
		 * @param isMale 性别 。true表示男，false表示女，null表示未知
		 * @return {@link Gender}中的性别代码
		 * @see #isMale(int)
		 */
		public static int getGenderCode(Boolean isMale){
			if(isMale == null)
				return unknown;
			else if(isMale)
				return male;
			else
				return female;
		}
		/**
		 * 把{@link Gender}中的性别代码恢复为性别
		 * @param gender 从{@link #getGenderCode(Boolean)}得到的性别代码
		 * @return 性别 。true表示男，false表示女，null表示未知
		 * @see #getGenderCode(Boolean)
		 */
		public static Boolean isMale(int gender){
			switch(gender){
			case male:return true;
			case female:return false;
			case unknown:return null;
			default:throw new IllegalArgumentException("非法参数：" + gender);
			}
		}
	}
}
