/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orange.querysystem;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.orange.querysystem.AllCoursesActivity.CourseToSimpleCourse;
import org.orange.querysystem.content.InsertDBFragmentActivity;
import org.orange.querysystem.content.ListCoursesFragment;
import org.orange.querysystem.content.ListCoursesFragment.SimpleCourse;
import org.orange.querysystem.content.TabsAdapter;
import org.orange.querysystem.util.Network;
import org.orange.querysystem.util.ReadDB;
import org.orange.querysystem.util.ReadDB.OnPostExcuteListerner;

import util.webpage.Course;
import util.webpage.Course.TimeAndAddress;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class CoursesInThisWeekActivity extends FragmentActivity implements OnPostExcuteListerner{
	private int start_resume = 0;
	
	private TextView currentTime;
	
	protected static final int COURSE_NUMBER = 6;
	public static final String ARRAYLIST_OF_COURSES_KEY
		= CoursesInThisWeekActivity.class.getName()+"ARRAYLIST_OF_COURSES_KEY";
	static final int DATE_DIALOG_ID = 1;
	
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
			String title = getResources().getString(R.string.curriculum_schedule_in_this_week);
			Integer weekNumber = SettingsActivity.getCurrentWeekNumber(this);
			if(weekNumber != null)
				title +="("+getResources().getString(R.string.week_of_semester,weekNumber)+")";
			mActionBar.setTitle(title);
			//横屏时，为节省空间隐藏ActionBar
			if(getResources().getConfiguration().orientation == 
					android.content.res.Configuration.ORIENTATION_LANDSCAPE)
				mActionBar.hide();
		}

        readDB();
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
            		startActivity(new Intent(CoursesInThisWeekActivity.this, SettingsActivity.class));
                      
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
    	new ReadDB(this, this).execute(SettingsActivity.getAccountStudentID(this), "this");
    }
    
    @Override
	public void onPostReadFromDB(ArrayList<Course> courses) {
			showCoursesInfo(courses, mCourseToSimpleCourse);
	}
    
    public void showCoursesInfo(ArrayList<Course> courses, CourseToSimpleCourse converter){
		mTabsAdapter.clear();
		Integer weekNumber = SettingsActivity.getCurrentWeekNumber(this);
        currentTime = (TextView)findViewById(R.id.currentTime);
        currentTime.setText("本周课程表" + "        " + DateFormat.getDateInstance().format(new Date()) + "    " + "第" + weekNumber + "周");
        
		Bundle[] args = new Bundle[8];

		List<SimpleCourse>[][] lesson = AllCoursesActivity.getTimeTable(courses, converter);

		//把每天的课程放到传到ListCoursesFragment的参数容器中
		for(int dayOfWeek = 0; dayOfWeek<=7; dayOfWeek++){
    		ArrayList<SimpleCourse> coursesInADay = new ArrayList<SimpleCourse>();
			for(int period = 1; period < lesson[dayOfWeek].length ; period++){
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
		
		for(int day = 0 ; day <= 7 ; day++){
			TabSpec tabSpec = mTabHost.newTabSpec(daysOfWeek[day]);
			mTabsAdapter.addTab(tabSpec.setIndicator(daysOfWeek[day]),
					ListCoursesFragment.class, args[day]);
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
		}else
			currentTime.setLayoutParams(new LinearLayout.LayoutParams(0, 0));

		int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
		mTabHost.setCurrentTab(dayOfWeek!=Calendar.SUNDAY ? dayOfWeek-Calendar.SUNDAY-1 : 6);
		
	}

	private static final CourseToSimpleCourse mCourseToSimpleCourse = new CourseToSimpleCourse(){
		@Override
		public SimpleCourse toSimpleCourse(Course course,
				TimeAndAddress timeAndAddress, Integer period) {
			SimpleCourse result;
			if(timeAndAddress != null)
				result = new SimpleCourse(course.getId(), course.getName(),
								period != null ? String.valueOf(period) : null,
								timeAndAddress.getAddress());
			else
				result = new SimpleCourse(course.getId(), course.getName(), null, null);
			return result;
		}
	};

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
			if(Network.isConnected(this)){
    			start_resume = 1;
        		startActivity(new Intent(this, InsertDBFragmentActivity.class));
        		//TODO startActivity后不会继续运行
            }
            else{
            	Network.openNoConnectionDialog(this);
            }
    	}
    	else if(item.getItemId() == 2){
    		start_resume = 1;
    		startActivity(new Intent(this, SettingsActivity.class));
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
}
