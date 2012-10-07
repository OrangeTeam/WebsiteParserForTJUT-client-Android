package org.orange.querysystem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import org.orange.querysystem.content.ListCoursesActivity;
import org.orange.querysystem.content.ListCoursesFragment.SimpleCourse;
import org.orange.studentinformationdatabase.StudentInfDBAdapter;

import util.BitOperate.BitOperateException;
import util.webpage.Course;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.RemoteViews;

public class MyAppWidgetProvider extends AppWidgetProvider {
	private int mMinute = 0;
	private int mHour = 0;
	private int mWeek = 0;
	private int mDayOfWeek = 0;
	private int calculate_week = 0;
	StudentInfDBAdapter studentInfDBAdapter = null;
	
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		long triggerAtTime = SystemClock.elapsedRealtime() + 5 * 1000;
		int interval = 15*60*1000;
		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, triggerAtTime, interval, pendingIntent);
		updateCourse(context,appWidgetManager, appWidgetIds);
	}
	
	public void onReceive(Context context, Intent intent){
		String action = intent.getAction();
		if(AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)){
			Bundle extras = intent.getExtras();
			if(extras != null){
				int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
				if(appWidgetIds != null && appWidgetIds.length > 0){
					this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
				}
			}
		}
	}
	
	public void updateCourse(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
		SharedPreferences shareData = context.getSharedPreferences("data", 0);
		ArrayList<Course> courses = null;
		studentInfDBAdapter = new StudentInfDBAdapter(context);
		try{
			studentInfDBAdapter.open();
			courses = studentInfDBAdapter.getCoursesFromDB(StudentInfDBAdapter.KEY_YEAR + "=" + 0, null, shareData.getString("userName", null));
		}catch(SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			closeDatabase();
		}
		int thePeriod = getTime();
		Calendar calendar = Calendar.getInstance();
		mWeek = calendar.get(Calendar.WEEK_OF_YEAR);
		mDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)-Calendar.MONDAY+1;
		Calendar calendar_2 = Calendar.getInstance();
		calendar_2.set(Integer.parseInt(shareData.getString("start_year", null)), Integer.parseInt(shareData.getString("start_month", null))-1, Integer.parseInt(shareData.getString("start_day", null)));
		calculate_week =  mWeek - calendar_2.get(Calendar.WEEK_OF_YEAR);
		LinkedList<SimpleCourse>[][] lesson = new LinkedList[7][14];
    	for(int day=0;day<=6;day++)
    		for(int period=1;period<=13;period++)
    			lesson[day][period] = new LinkedList<SimpleCourse>();
    	for(Course course:courses)
			for(Course.TimeAndAddress time:course.getTimeAndAddress())
				try{
					if(time.hasSetDay(mDayOfWeek)&&time.hasSetPeriod(thePeriod)&&time.hasSetWeek(calculate_week + 1)){
						lesson[mDayOfWeek][thePeriod].addLast(new SimpleCourse(course.getId(),course.getName(),String.valueOf(thePeriod),time.getAddress()));
					}
				} catch(BitOperateException e){
					e.printStackTrace();
				}
    	ArrayList<SimpleCourse> coursesInADay = new ArrayList<SimpleCourse>();
    	for(SimpleCourse course:lesson[mDayOfWeek][thePeriod])
			coursesInADay.add(course);
		
		String str = " ";
		if(coursesInADay.size() != 0){
			str = coursesInADay.get(0).getName();
			str = str + "  " + coursesInADay.get(0).getOtherInfo();
		}
		
		final int N = appWidgetIds.length;
		for(int i = 0; i < N; i++){
			int appWidgetId = appWidgetIds[i];
			Intent intent = new Intent(context, ListCoursesActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.myappwidget);
			views.setOnClickPendingIntent(R.id.widgetButton, pendingIntent);
			views.setTextViewText(R.id.appWidgetTextView, str);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
	
	private void closeDatabase(){
		if(studentInfDBAdapter != null)
			try{
				studentInfDBAdapter.close();
			}catch(NullPointerException e){
				e.printStackTrace();
			}
	}
	
	private int getTime(){
		int period = 0;
		Calendar calendar = Calendar.getInstance();
		mMinute = calendar.get(Calendar.MINUTE);
		mHour = calendar.get(Calendar.HOUR_OF_DAY);
		if((mHour <= 8) || ((mHour == 8)&&(mMinute <= 30))){
			period = 1;
		}else{
			if(((mHour == 8)&&(mMinute > 30)) || ((mHour ==9)&&(mMinute <= 25))){
				period = 2;
			}else{
				if(((mHour == 9)&&(mMinute > 25)) || ((mHour == 10)&&(mMinute <= 40))){
					period = 3;
				}else{
					if(((mHour == 10)&&(mMinute > 40)) || ((mHour == 11)&&(mMinute <= 35))){
						period = 4;
					}else{
						if(((mHour == 11)&&(mMinute > 35)) || ((mHour == 12)&&(mMinute <= 45))){
							period = 5;
						}else{
							if(((mHour == 12)&&(mMinute > 45)) || (mHour == 13) || ((mHour == 14)&&(mMinute <= 30))){
								period = 7;
							}else{
								if(((mHour == 14)&&(mMinute > 30)) || ((mHour == 15)&&(mMinute <= 25))){
									period = 8;
								}else{
									if(((mHour == 15)&&(mMinute > 25)) || ((mHour == 16)&&(mMinute <= 40))){
										period = 9;
									}else{
										if(((mHour == 16)&&(mMinute > 40)) || ((mHour == 17)&&(mMinute <= 35))){
											period = 10;
										}else{
											if(((mHour == 17)&&(mMinute > 35)) || (mHour == 18)){
												period = 11;
											}else{
												if((mHour == 19)&&(mMinute <= 55)){
													period = 12;
												}else{
													if(((mHour == 19)&&(mMinute > 55)) || ((mHour == 21)&&(mMinute <= 5))){
														period = 13;
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return period;
	}
}
