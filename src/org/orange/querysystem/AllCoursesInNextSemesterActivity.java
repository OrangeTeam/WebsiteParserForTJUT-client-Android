package org.orange.querysystem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.orange.querysystem.R;
import org.orange.querysystem.SettingsActivity;
import org.orange.querysystem.content.InsertDBFragmentActivity;
import org.orange.querysystem.content.ListCoursesFragment;
import org.orange.querysystem.content.ListCoursesFragment.SimpleCourse;
import org.orange.querysystem.content.TabsAdapter;
import org.orange.querysystem.util.Network;
import org.orange.querysystem.util.ReadDB;
import org.orange.querysystem.util.ReadDB.OnPostExcuteListerner;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.BitOperate.BitOperateException;
import util.webpage.Course;
import util.webpage.Course.TimeAndAddress;
import util.webpage.Student;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

public class AllCoursesInNextSemesterActivity extends FragmentActivity implements OnPostExcuteListerner{
	private int mYear = 0;
	private int mMonth = 0;
	private int mDay = 0;
	private int mWeek = 0;
	private int mDayOfWeek = 0;
	private int start_resume = 0;
	
	private TextView currentTime;
	
	protected static final int COURSE_NUMBER = 6;
	public static final String ARRAYLIST_OF_COURSES_KEY
		= AllCoursesInNextSemesterActivity.class.getName()+"ARRAYLIST_OF_COURSES_KEY";
	
	TabHost mTabHost;
    ViewPager  mViewPager;
    TabsAdapter mTabsAdapter;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@TargetApi(11)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_tabs_pager);
		
		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup();

		mViewPager = (ViewPager)findViewById(R.id.pager);

		mTabsAdapter = new TabsAdapter(this, mTabHost, mViewPager);
		
		//3.0以上版本，使用ActionBar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar mActionBar = getActionBar();
			mActionBar.setTitle("下学期课程表");
			//横屏时，为节省空间隐藏ActionBar
			if(getResources().getConfiguration().orientation == 
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				mActionBar.hide();
		}else{
			TabWidget tabWidget = mTabHost.getTabWidget();
			for (int i = 0; i < tabWidget.getChildCount(); i++) {  
				View child = tabWidget.getChildAt(i);  

				final TextView tv = (TextView)child.findViewById(android.R.id.title);  
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();  
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0); //取消文字底边对齐  
				params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE); //设置文字居中对齐  

				//child.getLayoutParams().height = tv.getHeight();
			}
		}						
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			if(getResources().getConfiguration().orientation == 
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				getActionBar().hide();
		}
		
		Calendar calendar = Calendar.getInstance();
		mYear = calendar.get(Calendar.YEAR);
		mMonth = calendar.get(Calendar.MONTH);//比正常少一个月
		mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mWeek = calendar.get(Calendar.WEEK_OF_YEAR);//正常
        mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);//比正常的多一天
        readDB();
        new RefreshCurrentSemester().execute();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if(InsertDBFragmentActivity.logIn_error == true){
			showDialog(InsertDBFragmentActivity.LOG_IN_ERROR_DIALOG_ID);
		}
		if(start_resume == 0){
			
		}
		else if(start_resume == 1){
			readDB();
		}
	}
	
	@Override
    protected Dialog onCreateDialog(int id) {
		// TODO Auto-generated method stub
		switch(id){
		case InsertDBFragmentActivity.LOG_IN_ERROR_DIALOG_ID:
			final TextView textView = new TextView(this);
			textView.setText("用户名或密码错误，请重新设置！");
			textView.setTextSize(14);
			RelativeLayout.LayoutParams tvlp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			RelativeLayout relativeLayout = new RelativeLayout(this);
			
			tvlp.addRule(RelativeLayout.CENTER_IN_PARENT);
			relativeLayout.addView(textView, tvlp);
			return new AlertDialog.Builder(this)
            .setView(relativeLayout)
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int whichButton) {
                  	 
            		/* User clicked OK so do some stuff */
            		InsertDBFragmentActivity.logIn_error = false;
            		startActivity(new Intent(AllCoursesInNextSemesterActivity.this, SettingsActivity.class));
                      
                }
            })
            .setOnKeyListener(new OnKeyListener(){

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent even) {
					// TODO Auto-generated method stub
					finish();
					return false;
				}
				
            	
            }).create();
		}
		return null;
	}
		
	public void readDB(){
    	new ReadDB(this, this).execute(SettingsActivity.getAccountStudentID(this), "next");
    }
    
    @Override
	public void onPostReadFromDB(ArrayList<Course> courses) {
		showCoursesInfo(courses);
	}
    
    public void showCoursesInfo(ArrayList<Course> courses){
		mTabsAdapter.clear();
        currentTime = (TextView)findViewById(R.id.currentTime);
        currentTime.setText("下学期总课程表" + "        " + mYear + "-" + (mMonth+1) + "-" + mDay);
        
		Bundle[] args = new Bundle[8];
    	
    	/*String[星期几][第几大节]*/
		LinkedList<SimpleCourse>[][] lesson = new LinkedList[8][14];
		for(int day=0;day<=7;day++)
    		for(int period=1;period<=13;period++)
    			lesson[day][period] = new LinkedList<SimpleCourse>();
		//根据课程时间，把课程排到课表中
		for(Course course:courses){
			List<TimeAndAddress> times = course.getTimeAndAddress();
			if(!times.isEmpty())
				for(TimeAndAddress time:times)
					for(int dayOfWeek = 0; dayOfWeek<=6; dayOfWeek++)			
						for(int period = 1; period<=13; period++)
							try{
								if(time.hasSetDay(dayOfWeek)&&time.hasSetPeriod(period)){
									lesson[dayOfWeek][period].addLast(new SimpleCourse(course.getId(),course.getName(),String.valueOf(period), time.getWeekString()+ "          " + time.getAddress()));
								}
							} catch(BitOperateException e){
								e.printStackTrace();
							}
			else
				lesson[7][1].addLast(new SimpleCourse(course.getId(),course.getName(), null, null));
		}
		//把每天的课程放到传到ListCoursesFragment的参数容器中
		for(int dayOfWeek = 0; dayOfWeek<=7; dayOfWeek++){
    		ArrayList<SimpleCourse> coursesInADay = new ArrayList<SimpleCourse>();
    		for(int period = 1; period<=13; period++){
    			for(SimpleCourse course:lesson[dayOfWeek][period])
    				coursesInADay.add(course);
    		}
    		Bundle argForFragment = new Bundle();
    		argForFragment.putParcelableArrayList(ListCoursesFragment.COURSES_KEY, coursesInADay);
    		args[dayOfWeek] = argForFragment;
    	}
		//交换周日args[0]和时间未定args[7]，把周日显示在最后
		Bundle temp = args[0];
		args[0] = args[7];
		args[7] = temp;

		String[] daysOfWeek = getResources().getStringArray(R.array.days_of_week);
		
		for(int day = 0;day<=7;day++){
			TabSpec tabSpec = mTabHost.newTabSpec(daysOfWeek[day]);
			mTabsAdapter.addTab(tabSpec.setIndicator(daysOfWeek[day]),
					ListCoursesFragment.class, args[day]);
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			TabWidget tabWidget = mTabHost.getTabWidget();
			for (int i = 0; i < tabWidget.getChildCount(); i++) {  
				View child = tabWidget.getChildAt(i);

				child.setBackgroundResource(R.drawable.tab);

				final TextView tv = (TextView)child.findViewById(android.R.id.title);  
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();  
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0); //取消文字底边对齐  
				params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE); //设置文字居中对齐  

				//TODO Do not use hard-coded pixel values in your application code
				//{@link http://developer.android.com/intl/zh-CN/guide/practices/screens_support.html#screen-independence}
				child.getLayoutParams().height = 80;
			}
		}else
			currentTime.setLayoutParams(new LinearLayout.LayoutParams(0, 0));

		mTabHost.setCurrentTab(mDayOfWeek!=Calendar.SUNDAY ? mDayOfWeek-Calendar.SUNDAY : 7);
    }
	

	/* (non-Javadoc)
	 * @see android.app.Activity#onWindowFocusChanged(boolean)
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);

		mTabsAdapter.adjustSelectedTabToCenter();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
	    mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
		super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", mTabHost.getCurrentTabTag());
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
    		if(Network.getInstance(this).isConnected()){
    			start_resume = 1;
        		startActivity(new Intent(this, InsertDBFragmentActivity.class));
        		//TODO startActivity后不会继续运行
            }
            else{
            	Network.openNoConnectionDialog(this);
            }
    	}
    	else if(item.getItemId() == 2){
    		startActivity(new Intent(this, SettingsActivity.class));
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
    
    private class RefreshCurrentSemester extends AsyncTask<Object,Void,Student>{
    	protected Student doInBackground(Object... args){
    		int currentWeek = 0;
    		StudentInfDBAdapter studentInfDBAdapter = new StudentInfDBAdapter(AllCoursesInNextSemesterActivity.this);
    		studentInfDBAdapter.open();
    		currentWeek = SettingsActivity.getCurrentWeekNumber(AllCoursesInNextSemesterActivity.this);
    		if(currentWeek < 5){
	        	studentInfDBAdapter.updateCurrentSemester();
	        }
    		studentInfDBAdapter.close();
    		return null;
    	}
    }
}
