package org.orange.querysystem.content;

import java.util.ArrayList;
import java.util.HashMap;

import org.orange.querysystem.ApplicationExit;
import org.orange.querysystem.LoginActivity;
import org.orange.querysystem.R;
import org.orange.querysystem.content.ReadDB.OnPostExcuteListerner;

import util.BitOperate.BitOperateException;
import util.webpage.Course;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class AllCourseListActivity extends ListActivity implements OnPostExcuteListerner{
	
	private Button refresh;
	private Button refresh_db;
	private Spinner spinner = null;
	private int mStackLevel = 0;

	@Override
	protected void onStart(){
		super.onStart();
		ApplicationExit appExit = (ApplicationExit)getApplication();
		if(appExit.isExit()){
			finish();
		}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.all_course_list_main);

        if (savedInstanceState != null) {
            mStackLevel = savedInstanceState.getInt("level");
        }
        if(isNetworkConnected()){
        	readDB();
        }
        else{
        	Toast.makeText(this, "网络异常！请检查网络设置！", Toast.LENGTH_LONG).show();
        }
        
        refresh = (Button)findViewById(R.id.refresh);
        refresh.setBackgroundResource(R.drawable.ic_action_refresh);
        refresh_db = (Button)findViewById(R.id.refresh_db);
		ArrayAdapter<CharSequence> menu_adapter = ArrayAdapter.createFromResource(this, R.array.all_course_menu_array, android.R.layout.simple_spinner_item); 
		menu_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner = (Spinner)findViewById(R.id.course_spinner_menu);
		spinner.setAdapter(menu_adapter);
		spinner.setPrompt("课程表");
		spinner.setOnItemSelectedListener(new SpinnerOnSelectedListener());
    }
    
    class SpinnerOnSelectedListener implements OnItemSelectedListener{
    	@Override
    	public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id){
    		if(adapterView.getItemIdAtPosition(position) == 0){
    			
    		}
    		if(adapterView.getItemIdAtPosition(position) == 1){
    			startActivity(new Intent(AllCourseListActivity.this, WeekCourseListActivity.class));
    		}
    		if(adapterView.getItemIdAtPosition(position) == 2){
    			startActivity(new Intent(AllCourseListActivity.this, DayCourseListActivity.class));
    		}
    			
    	}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
			// TODO Auto-generated method stub
			
		}
    }
    
    public void readDB(){
    	SharedPreferences shareData = getSharedPreferences("data", 0);
    	new ReadDB(this, this).execute(shareData.getString("userName", null));
    }
    
    @Override
	public void onPostReadFromDB(ArrayList<Course> courses) {
			showCoursesInfo(courses);
	}
    
    public void showCoursesInfo(ArrayList<Course> courses) {
    	/*String[星期几][第几大节]*/
    	String[][] lesson = new String[8][7];
    	for(int i=0;i<lesson.length;i++)
    		for(int j=0;j<lesson[i].length;j++)
    			lesson[i][j]="";
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        for(int i=1;i<=7;i++)
        	list.add(new HashMap<String, String>());
        
		list.get(0).put("0", "");
		list.get(0).put("1", "周一");
		list.get(0).put("2", "周二");
		list.get(0).put("3", "周三");
		list.get(0).put("4", "周四");
		list.get(0).put("5", "周五");
		list.get(0).put("6", "周六");
		list.get(0).put("7", "周日");
		list.get(1).put("0", "1-2节");
		list.get(2).put("0", "3-4节");
		list.get(3).put("0", "5-6节");
		list.get(4).put("0", "7-8节");
		list.get(5).put("0", "9-10节");
		list.get(6).put("0", "11-13节");
		for(Course course:courses)
			for(Course.TimeAndAddress time:course.getTimeAndAddress())
				for(int dayOfWeek = 1; dayOfWeek<=7; dayOfWeek++)
					for(int period = 1; period<=13; period++)
						try{
							if(time.hasSetDay(dayOfWeek)&&time.hasSetPeriod(period)){
								if((period&1) == 1)
									period++;	//period为奇数，此大节已加，自动跳过一小节
								int bigPeriod = period/2;	//转为大节，下面处理11-13特殊情况
								if(bigPeriod==7){
									bigPeriod=6;	//若period为13，对应第6大节
									if(time.hasSetPeriod(11)||time.hasSetPeriod(12))
										continue;	//若period为13，且11或12小节已记，则跳过此次
								}
								lesson[dayOfWeek][bigPeriod]+=course.getName() + "\n" + time.getWeekString() + "\n" + time.getAddress() + "\n";
							}
						} catch(BitOperateException e){
							e.printStackTrace();
						}
		for(int row = 1 ;row<=6;row++)
			for(int dayOfWeek = 1;dayOfWeek<=7;dayOfWeek++)
				list.get(row).put(String.valueOf(dayOfWeek), lesson[dayOfWeek][row]);
		
		SimpleAdapter listAdapter = new MySimpleAdapter(this, list, R.layout.all_course_list_listview, new String[]{"0", "1", "2", "3", "4", "5", "6", "7"}, new int[] {R.id.blank, R.id.Monday, R.id.Tuesday, R.id.Wednesday, R.id.Thursday, R.id.Friday, R.id.Saturday, R.id.Sunday});
		setListAdapter(listAdapter);
		
//		ListView listView = (ListView)findViewById(android.R.id.list);
//		Drawable drawable = getResources().getDrawable(R.drawable.pic);
//		listView.setSelector(drawable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 1, 1, R.string.exit);
        menu.add(0, 2, 2, R.string.change_number);
        menu.add(0, 3, 3, R.string.about);
        menu.add(0, 4, 4, R.string.course_query);
        menu.add(0, 5, 5, R.string.score_query);
        menu.add(0, 6, 6, R.string.all_post_query);
        menu.add(0, 7, 7, R.string.day_and_week_courses);
        menu.add(0, 8, 8, R.string.student_info);
        
        return super.onCreateOptionsMenu(menu); 
    }
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	// TODO Auto-generated method stub\
    	if(item.getItemId() == 1){
    		finish();
    	}
    	else if(item.getItemId() == 2){
    		Editor editor = getSharedPreferences("data", 0).edit();
    		editor.putBoolean("logIn_auto", false);
    		editor.commit();
    		startActivity(new Intent(this, LoginActivity.class));
    	}
    	else if(item.getItemId() == 3){
    		startActivity(new Intent(this, AllListCoursesActivity.class));
    	}
    	else if(item.getItemId() == 4){
    		readDB();
    	}
    	else if(item.getItemId() == 5){
    		startActivity(new Intent(this, MainMenuActivity.class));
    	}
    	else if(item.getItemId() == 6){
    		startActivity(new Intent(this, ListPostsActivity.class));
    	}
    	else if(item.getItemId() == 7){
    		startActivity(new Intent(this, ListCoursesActivity.class));
    	}
    	else if(item.getItemId() == 8){
    		startActivity(new Intent(this, StudentInfoActivity.class));
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
    public void onBackPressed() {
    	//实现Home键效果
    	//super.onBackPressed();这句话一定要注掉,不然又去调用默认的back处理方式了
    	Intent i= new Intent(Intent.ACTION_MAIN);
    	i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	i.addCategory(Intent.CATEGORY_HOME);
    	startActivity(i);
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
    
    public void refreshdb(View view){
    	if(isNetworkConnected()){
    		startActivity(new Intent(this, InsertDBFragmentActivity.class));
        }
        else{
        	Toast.makeText(this, "网络异常！请检查网络设置！", Toast.LENGTH_LONG).show();
        }
      	   	
    }
    public void refreshActivity(View view){
    	if(isNetworkConnected()){
    		readDB();
        }
        else{
        	Toast.makeText(this, "网络异常！请检查网络设置！", Toast.LENGTH_LONG).show();
        }
    	
    }
}