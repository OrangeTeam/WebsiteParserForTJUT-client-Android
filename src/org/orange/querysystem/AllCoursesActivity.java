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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.orange.querysystem.content.ListCoursesFragment;
import org.orange.querysystem.content.ListCoursesFragment.SimpleCourse;

import util.BitOperate.BitOperateException;
import util.webpage.Course;
import util.webpage.Course.TimeAndAddress;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.os.Build;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class AllCoursesActivity extends CoursesInThisWeekActivity{
	private int mYear = 0;
	private int mMonth = 0;
	private int mDay = 0;
	private int mWeek = 0;
	private int mDayOfWeek = 0;

	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
	 */
	@TargetApi(11)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//3.0以上版本，使用ActionBar
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar mActionBar = getActionBar();
			mActionBar.setTitle("总课程表");
		}

		Calendar calendar = Calendar.getInstance();
		mYear = calendar.get(Calendar.YEAR);
		mMonth = calendar.get(Calendar.MONTH);//比正常少一个月
		mDay = calendar.get(Calendar.DAY_OF_MONTH);
        mWeek = calendar.get(Calendar.WEEK_OF_YEAR);//正常
        mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);//比正常的多一天
	}

	@Override
    public void showCoursesInfo(List<Course> courses, CourseToSimpleCourse converter){
		mTabsAdapter.clear();
		TextView currentTime = (TextView)findViewById(R.id.currentTime);
        currentTime.setText("总课程表" + "        " + mYear + "-" + (mMonth+1) + "-" + mDay);
        
		Bundle[] args = new Bundle[8];

		List<SimpleCourse>[][] lesson = getTimeTable(courses, converter);
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
		
		for(int day = 0;day<=7;day++){
			TabSpec tabSpec = mTabHost.newTabSpec(daysOfWeek[day]);
			mTabsAdapter.addTab(tabSpec.setIndicator(daysOfWeek[day]),
					ListCoursesFragment.class, args[day]);
		}

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB){
		}else
			currentTime.setLayoutParams(new LinearLayout.LayoutParams(0, 0));

		mTabHost.setCurrentTab(mDayOfWeek!=Calendar.SUNDAY ? mDayOfWeek-Calendar.SUNDAY : 7);
	}

	public static final CourseToSimpleCourse mCourseToSimpleCourse = new CourseToSimpleCourse(){
		@Override
		public SimpleCourse toSimpleCourse(Course course,
				TimeAndAddress timeAndAddress, Integer period) {
			SimpleCourse result;
			if(timeAndAddress != null)
				result = new SimpleCourse(course.getId(), course.getName(),
						period != null ? String.valueOf(period) : null,
						timeAndAddress.getWeekString()+ "\t\t" + timeAndAddress.getAddress());
			else
				result = new SimpleCourse(course.getId(), course.getName(), null, null);
			return result;
		}
	};
	/**
	 * {@link AllCoursesActivity#getTimeTable(List, CourseToSimpleCourse)}
	 * 使用的{@link Course}到{@link SimpleCourse}转化器
	 */
	public static interface CourseToSimpleCourse{
		/**
		 * 把{@link Course}转化为{@link SimpleCourse}
		 * @param course 待转化的{@link Course}
		 * @param timeAndAddress 当前课程表格子对应的{@link TimeAndAddress}，null表示星期未知，无对应{@link TimeAndAddress}
		 * @param period 课程节次，null表示未知，1～13分别表示第1节～第13节
		 * @return 转化后的{@link SimpleCourse}
		 */
		SimpleCourse toSimpleCourse(Course course, TimeAndAddress timeAndAddress, Integer period);
	}
	/**
	 * 把课程排到课程表里。返回排好的与课程表类似的二维表
	 * @param courses 待排的课程
	 * @return 类似课程表的二维表，第一个索引为星期（0表示周日，1～6表示表周一至周六，7表示星期未知），
	 * 第二个索引为节次（0不用都是null，1～13表示1～13节课，14表示节次未知）。表中每一格是一个List，可能有多门课。
	 */
	public static List<SimpleCourse>[][] getTimeTable(List<Course> courses,
			CourseToSimpleCourse converter){
		boolean hasPosition;
		@SuppressWarnings("unchecked")
		LinkedList<SimpleCourse>[][] timeTable = new LinkedList[8][15];
		for(int day=0 ; day<timeTable.length ; day++)
			for(int period=1; period<timeTable[day].length ; period++)
				timeTable[day][period] = new LinkedList<SimpleCourse>();
		try{
			for(Course course:courses){
				hasPosition = false;
				for(TimeAndAddress time:course.getTimeAndAddress()){
					if(time.isEmpty(TimeAndAddress.Property.DAY))
						continue;
					hasPosition = true;
					for(int dayOfWeek = 0; dayOfWeek<=6; dayOfWeek++)
						if(time.hasSetDay(dayOfWeek)){
							if(time.isEmpty(TimeAndAddress.Property.PERIOD))
								timeTable[dayOfWeek][14].addLast(converter.toSimpleCourse(course, time, null));
							else
								for(int period = 1; period<=13; period++){
									if(time.hasSetPeriod(period))
										timeTable[dayOfWeek][period].addLast(converter.toSimpleCourse(course, time, period));
								}
						}
				}
				if(!hasPosition)	//星期未知
					timeTable[7][1].addLast(converter.toSimpleCourse(course, null, null));
			}
		} catch(BitOperateException e){
			throw new RuntimeException(e);
		}
		return timeTable;
	}

}
