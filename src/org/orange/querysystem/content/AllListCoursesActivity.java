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
package org.orange.querysystem.content;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import org.orange.querysystem.R;
import org.orange.querysystem.content.ListCoursesFragment.SimpleCourse;
import org.orange.querysystem.content.ReadDB.OnPostExcuteListerner;

import util.BitOperate.BitOperateException;
import util.webpage.Course;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

public class AllListCoursesActivity extends FragmentActivity implements OnPostExcuteListerner{
	private int mYear = 0;
	private int mMonth = 0;
	private int mDay = 0;
	private int mWeek = 0;
	private int mDayOfWeek = 0;
	
	private TextView currentTime;
	
	protected static final int COURSE_NUMBER = 6;
	public static final String ARRAYLIST_OF_COURSES_KEY
		= ListCoursesActivity.class.getName()+"ARRAYLIST_OF_COURSES_KEY";
	
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
	}
		
	public void readDB(){
		SharedPreferences shareData = getSharedPreferences("data", 0);
    	new ReadDB(this, this).execute(shareData.getString("userName", null));
    }
    
    @Override
	public void onPostReadFromDB(ArrayList<Course> courses) {
			showCoursesInfo(courses);
	}
    
    public void showCoursesInfo(ArrayList<Course> courses){
        currentTime = (TextView)findViewById(R.id.currentTime);
        currentTime.setText("总课程表" + "        " + mYear + "-" + (mMonth+1) + "-" + mDay);
        
    	Bundle[] args = new Bundle[7]; 
    	
    	/*String[星期几][第几大节]*/
    	LinkedList<SimpleCourse>[][] lesson = new LinkedList[7][14];
    	for(int day=0;day<=6;day++)
    		for(int period=1;period<=13;period++)
    			lesson[day][period] = new LinkedList<SimpleCourse>();
    	for(Course course:courses)
			for(Course.TimeAndAddress time:course.getTimeAndAddress())
				for(int dayOfWeek = 0; dayOfWeek<=6; dayOfWeek++)			
					for(int period = 1; period<=13; period++)
						try{
							if(time.hasSetDay(dayOfWeek)&&time.hasSetPeriod(period)){
								lesson[dayOfWeek][period].addLast(new SimpleCourse(course.getId(),course.getName(),String.valueOf(period), time.getWeekString()+ "          " + time.getAddress()));
							}
						} catch(BitOperateException e){
							e.printStackTrace();
						}
    	
    	for(int dayOfWeek = 0; dayOfWeek<=6; dayOfWeek++){
    		ArrayList<SimpleCourse> coursesInADay = new ArrayList<SimpleCourse>();
    		for(int period = 1; period<=13; period++){
    			for(SimpleCourse course:lesson[dayOfWeek][period])
    				coursesInADay.add(course);
    		}
    		Bundle argForFragment = new Bundle();
    		argForFragment.putParcelableArrayList(ListCoursesFragment.COURSES_KEY, coursesInADay);
    		args[dayOfWeek] = argForFragment;
    	}
    	
		String[] daysOfWeek = getResources().getStringArray(R.array.days_of_week);
		
		for(int day = 0;day<=6;day++)
			mTabsAdapter.addTab(mTabHost.newTabSpec(daysOfWeek[day]).setIndicator(daysOfWeek[day]),
					ListCoursesFragment.class, args[day]);
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
			TabWidget tabWidget = mTabHost.getTabWidget();
			for (int i = 0; i < tabWidget.getChildCount(); i++) {  
				View child = tabWidget.getChildAt(i);  

				final TextView tv = (TextView)child.findViewById(android.R.id.title);  
				RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tv.getLayoutParams();  
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0); //取消文字底边对齐  
				params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE); //设置文字居中对齐  

				child.getLayoutParams().height = 80;
			}
		}
		mTabHost.setCurrentTab(mDayOfWeek-1);
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

}
