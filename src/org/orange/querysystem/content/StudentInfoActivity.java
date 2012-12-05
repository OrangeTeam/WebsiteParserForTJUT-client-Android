package org.orange.querysystem.content;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

import org.orange.querysystem.AboutActivity;
import org.orange.querysystem.LoginActivity;
import org.orange.querysystem.R;

import util.webpage.Constant;
import util.webpage.SchoolWebpageParser;
import util.webpage.SchoolWebpageParser.ParserException;
import util.webpage.Student;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class StudentInfoActivity extends ListActivity{
	private ListView listView;
	private ListViewAdapter adapter;
	private View showImage;
	private ImageView imageView;
	private static final String FILE_NAME = "student_info.txt";
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.student_info);
		
		showImage = getLayoutInflater().inflate(R.layout.show_image, null);
		imageView = (ImageView)showImage.findViewById(R.id.studentImageView);
		
		listView = getListView();
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
	
	private void initAdapter(){
		ArrayList<String> items = new ArrayList<String>();
		items.add("学号");items.add("姓名");
		items.add("性别");items.add("出生年月日");
		items.add("学制");items.add("入学时间");
		items.add("学院");items.add("专业名称");
		items.add("班级名称");items.add("照片");
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
			SharedPreferences shareData = getSharedPreferences("data", 0);
			SchoolWebpageParser studentInfo;
			try {
				studentInfo = new SchoolWebpageParser();
                studentInfo.setUser(shareData.getString("userName", null), shareData.getString("password", null));
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
		        outputStream.println(student.getNumber());
		        outputStream.println(student.getName());
		        outputStream.println(student.getGender());
		        outputStream.println(student.getBirthdayString());
		        outputStream.println(Byte.toString(student.getAcademicPeriod()));
		        outputStream.println(student.getAdmissionTimeString());
		        outputStream.println(student.getSchoolName());
		        outputStream.println(student.getMajorName());
		        outputStream.println(student.getClassName());
		        storeImage(getHttpBitmap(student.getUrlOfFacedPhoto()));
		        outputStream.close();
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
	
	private class StudentInfoFromFile extends AsyncTask<Void,Void,Void>{
		protected Void doInBackground(Void...agrs){
			return null;
		}
		
		protected void onPostExecute(Void args){
			Scanner inputStream = null;
			try{
				inputStream = new Scanner(openFileInput(FILE_NAME));
			}catch(FileNotFoundException e){
				System.out.println("Error opening files");
			}
			ArrayList<String> items = new ArrayList<String>();
			items.add(0, inputStream.nextLine());
			items.add(1, inputStream.nextLine());
			items.add(2, inputStream.nextLine());
			items.add(3, inputStream.nextLine());
			items.add(4, inputStream.nextLine());
			items.add(5, inputStream.nextLine());
			items.add(6, inputStream.nextLine());
			items.add(7, inputStream.nextLine());
			items.add(8, inputStream.nextLine());
			imageView.setImageBitmap(getBitmap());
			adapter = new ListViewAdapter(StudentInfoActivity.this, items);
			setListAdapter(adapter);
		}
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.main_menu);
        menu.add(0, 2, 2, R.string.change_number);
        menu.add(0, 3, 3, R.string.settings);
        menu.add(0, 4, 4, R.string.refresh);
        menu.add(0, 5, 5, R.string.about);
        
        return super.onCreateOptionsMenu(menu); 
    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// TODO Auto-generated method stub\
    	if(item.getItemId() == 1){
    		Editor editor = getSharedPreferences("data", 0).edit();
			editor.putString("passMainMenu", "true");
            editor.commit();		
    		startActivity(new Intent(this, MainMenuActivity.class));
    	}
    	else if(item.getItemId() == 2){
    		Editor editor = getSharedPreferences("data", 0).edit();
    		editor.putBoolean("logIn_auto", false);
    		editor.commit();
    		startActivity(new Intent(this, LoginActivity.class));
    	}
    	else if(item.getItemId() == 3){
//    		startActivity(new Intent(this, AllListCoursesActivity.class));
    	}
    	else if(item.getItemId() == 4){
    		if(isNetworkConnected()){
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
    	else if(item.getItemId() == 5){
    		startActivity(new Intent(this, AboutActivity.class));
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    public boolean isNetworkConnected(){
    	ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if(networkInfo !=null && networkInfo.isConnected()){
			return true;
		}
		else{
		    return false;
		}
    }
    
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Editor editor = getSharedPreferences("data", 0).edit();
			editor.putString("passMainMenu", "true");
            editor.commit();
		}
		return super.onKeyDown(keyCode, event);
	}
}
